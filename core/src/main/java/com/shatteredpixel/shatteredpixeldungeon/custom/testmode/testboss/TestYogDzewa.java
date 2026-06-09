package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class TestYogDzewa extends Mob {

    private int phase = 1;
    private float abilityCooldown = 3;
    private float summonCooldown = 10;
    private int nextFist = 0;
    private ArrayList<Integer> targetedCells = new ArrayList<>();

    private static final String PHASE = "phase";
    private static final String ABILITY_CD = "ability_cd";
    private static final String SUMMON_CD = "summon_cd";
    private static final String NEXT_FIST = "next_fist";
    private static final String TARGETED_CELLS = "targeted_cells";

    private static final Class<? extends Mob>[] SUMMONS = new Class[]{
            YogDzewa.Larva.class,
            RipperDemon.class,
            Eye.class,
            Scorpio.class
    };

    private static final Class<? extends YogFist>[] FISTS = new Class[]{
            YogFist.BurningFist.class,
            YogFist.RottingFist.class,
            YogFist.BrightFist.class
    };

    {
        spriteClass = YogSprite.class;

        HP = HT = 1000;
        EXP = 50;
        state = HUNTING;
        viewDistance = 12;

        properties.add(Property.BOSS);
        properties.add(Property.IMMOVABLE);
        properties.add(Property.DEMONIC);
        properties.add(Property.STATIC);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int attackSkill(Char target) {
        return 36;
    }

    @Override
    protected boolean act() {
        TestBossUtil.assignBoss(this);
        if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
            fieldOfView = new boolean[Dungeon.level.length()];
        }
        Dungeon.level.updateFieldOfView(this, fieldOfView);

        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
            targetedCells.clear();
            spend(TICK);
            return true;
        }

        if (!targetedCells.isEmpty()) {
            fireDeathRays();
            targetedCells.clear();
            spend(TICK);
            return true;
        }

        Char target = enemy != null && enemy.isAlive() ? enemy : chooseEnemy();
        if (target != null) {
            enemy = target;
            enemySeen = fieldOfView != null && fieldOfView.length > target.pos && fieldOfView[target.pos] && target.invisible <= 0;
            if (abilityCooldown <= 0) {
                targetDeathRays(target);
                abilityCooldown = Math.max(3, Random.NormalFloat(10, 15) - phase);
                spend(TICK);
                return true;
            } else {
                abilityCooldown--;
            }
        }

        if (summonCooldown <= 0) {
            TestBossUtil.summonNear(this, SUMMONS[Random.Int(SUMMONS.length)], pos, 1, 4);
            summonCooldown = Math.max(3, Random.NormalFloat(8, 13) - phase);
        } else {
            summonCooldown--;
        }
        spend(TICK);
        return true;
    }

    private void targetDeathRays(Char target) {
        int beams = 1 + (HT - HP) / 400;
        if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
                && Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT)) {
            beams++;
        }

        targetedCells.add(target.pos);
        for (int i = 1; i < beams; i++) {
            int targetPos = target.pos + PathFinder.NEIGHBOURS8[Random.Int(PathFinder.NEIGHBOURS8.length)];
            if (Dungeon.level.insideMap(targetPos)) {
                targetedCells.add(targetPos);
            }
        }

        if (sprite != null && sprite.parent != null) {
            for (int cell : targetedCells) {
                Ballistica b = new Ballistica(pos, cell, Ballistica.WONT_STOP);
                for (int p : b.path) {
                    sprite.parent.add(new TargetedCell(p, 0xFF0000));
                }
            }
        }
    }

    private void fireDeathRays() {
        boolean terrainAffected = false;
        HashSet<Char> affected = new HashSet<>();

        for (int cell : targetedCells) {
            Ballistica b = new Ballistica(pos, cell, Ballistica.WONT_STOP);
            if (sprite != null && sprite.parent != null) {
                sprite.parent.add(new Beam.DeathRay(sprite.center(), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
            }
            for (int p : b.path) {
                Char ch = Actor.findChar(p);
                if (ch != null && ch != this && ch.alignment != alignment) {
                    affected.add(ch);
                }
                if (Dungeon.level.flamable[p]) {
                    Dungeon.level.destroy(p);
                    GameScene.updateMap(p);
                    terrainAffected = true;
                }
            }
        }

        if (terrainAffected) {
            Dungeon.observe();
        }
        Invisibility.dispel(this);

        for (Char ch : affected) {
            if (hit(this, ch, true)) {
                ch.damage(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
                        ? Random.NormalIntRange(30, 50)
                        : Random.NormalIntRange(20, 30), new Eye.DeathGaze());
                if (sprite != null && ch.sprite != null && Dungeon.level.heroFOV[pos]) {
                    ch.sprite.flash();
                }
            } else if (ch.sprite != null) {
                ch.sprite.showStatus(CharSprite.NEUTRAL, ch.defenseVerb());
            }
        }
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return findFist() != null || super.isInvulnerable(effect);
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        if (findFist() != null) {
            super.damage(dmg, src);
            return;
        }
        int preHP = HP;
        super.damage(dmg, src);
        int dmgTaken = preHP - HP;
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmgTaken);
        }
        while (phase < 4 && HP <= HT - 250 * phase && isAlive()) {
            HP = Math.max(HP, HT - 250 * phase);
            addTestFist();
            phase++;

            yell(Messages.get(this, "summon"));
        }
        if (phase >= 4 && HP <= 100 && isAlive()) {
            BossHealthBar.bleed(true);
            summonCooldown = Math.min(summonCooldown, 3);
        }
    }

    private void addTestFist() {
        YogFist fist = com.watabou.utils.Reflection.newInstance(FISTS[nextFist % FISTS.length]);
        nextFist++;
        int cell = TestBossUtil.randomSpawnCellNear(pos, 1, 3);
        if (cell == -1) {
            return;
        }
        fist.alignment = alignment;
        fist.updateSpriteState();
        fist.pos = cell;
        fist.state = fist.HUNTING;
        GameScene.add(fist, 4);
        Dungeon.level.occupyCell(fist);
        CellEmitter.get(cell).burst(ShadowParticle.UP, 30);
    }

    private YogFist findFist() {
        for (Char ch : Actor.chars()) {
            if (ch instanceof YogFist && ch.alignment == alignment && ch.isAlive()) {
                return (YogFist) ch;
            }
        }
        return null;
    }

    @Override
    public void die(Object cause) {
        TestBossUtil.bossSlain(this);
        super.die(cause);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
        yell(Messages.get(this, "notice"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase);
        bundle.put(ABILITY_CD, abilityCooldown);
        bundle.put(SUMMON_CD, summonCooldown);
        bundle.put(NEXT_FIST, nextFist);
        int[] bundleArr = new int[targetedCells.size()];
        for (int i = 0; i < targetedCells.size(); i++) {
            bundleArr[i] = targetedCells.get(i);
        }
        bundle.put(TARGETED_CELLS, bundleArr);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        abilityCooldown = bundle.getFloat(ABILITY_CD);
        summonCooldown = bundle.getFloat(SUMMON_CD);
        nextFist = bundle.getInt(NEXT_FIST);
        targetedCells.clear();
        int[] storedTargets = bundle.getIntArray(TARGETED_CELLS);
        for (int cell : storedTargets) {
            targetedCells.add(cell);
        }
        BossHealthBar.assignBoss(this);
    }
}
