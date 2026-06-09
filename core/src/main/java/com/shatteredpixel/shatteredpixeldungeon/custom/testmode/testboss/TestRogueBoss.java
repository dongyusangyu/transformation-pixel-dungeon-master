package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RogueBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class TestRogueBoss extends Mob {

    private int phase = 0;
    private int invisibilityCooldown = 0;

    private static final String PHASE = "phase";
    private static final String INVIS_CD = "invisibility_cd";

    {
        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200;
        EXP = 20;
        defenseSkill = 15;
        spriteClass = RogueBossSprite.class;
        state = WANDERING;

        properties.add(Property.BOSS);
        immunities.add(Roots.class);
        immunities.add(Blindness.class);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int damageRoll() {
        int dmg = Random.NormalIntRange(6, HP * 2 <= HT ? 10 : 12);
        if (buff(RogueBoss.AbsoluteInvisibility.class) != null) {
            dmg *= 2;
        }
        if (buff(RogueBoss.MomentumTime.class) != null && buff(RogueBoss.MomentumTime.class).left > 0) {
            dmg = (int)(dmg * 1.3f);
        }
        return dmg;
    }

    @Override
    public int attackSkill(Char target) {
        int attack = HP * 2 <= HT ? 15 : 10;
        if (buff(RogueBoss.MomentumTime.class) != null && buff(RogueBoss.MomentumTime.class).left > 0) {
            attack += 5;
        }
        return attack;
    }

    @Override
    public int defenseSkill(Char enemy) {
        int defense = super.defenseSkill(enemy);
        if (HP * 2 <= HT) {
            defense = (int)(defense * 1.5f);
        }
        if (buff(RogueBoss.MomentumTime.class) != null && buff(RogueBoss.MomentumTime.class).left > 0) {
            defense += 5;
        }
        return defense;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 2);
    }

    @Override
    protected boolean act() {
        TestBossUtil.assignBoss(this);
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
            return super.act();
        }
        if (state != SLEEPING && enemy != null && enemy.isAlive()) {
            if (buff(RogueBoss.AbsoluteInvisibility.class) == null && buff(Paralysis.class) == null) {
                invisibilityCooldown++;
            }
            RogueBoss.AbsoluteInvisibility invis = buff(RogueBoss.AbsoluteInvisibility.class);
            if (invis != null && HP > HT / 2) {
                invis.InvisibilityAttack++;
                if (invis.InvisibilityAttack >= 10 && enemySeen) {
                    execute(enemy.pos);
                    spend(TICK);
                    return true;
                }
            }
            if (invisibilityCooldown >= 8) {
                Buff.affect(this, RogueBoss.AbsoluteInvisibility.class, 16f);
                if (TargetHealthIndicator.instance != null) {
                    TargetHealthIndicator.instance.target(null);
                }
                invisibilityCooldown = 0;
                yell(Messages.get(this, "invisibility"));
                jumpNearEnemy();
            }
        }
        return super.act();
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (HP > HT / 2 && buff(RogueBoss.AbsoluteInvisibility.class) != null) {
            return false;
        }
        if (HP <= HT / 2 && distance(enemy) <= 2) {
            return false;
        }
        return super.canAttack(enemy)
                || new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        damage = super.attackProc(enemy, damage);
        RogueBoss.MomentumTime momentum = buff(RogueBoss.MomentumTime.class);
        if (momentum != null && momentum.left <= 0) {
            damage = (int)(damage * 1.33f);
        }
        return damage;
    }

    @Override
    protected boolean getCloser(int target) {
        if (HP <= HT / 2 || buff(RogueBoss.AbsoluteInvisibility.class) != null) {
            return super.getFurther(target);
        }
        return super.getCloser(target);
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        if (buff(RogueBoss.AbsoluteInvisibility.class) != null) {
            RogueBoss.AbsoluteInvisibility.dispel(this);
            if(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
                Buff.affect(this, Adrenaline.class, 3f);
            }else{
                Buff.affect(this, Paralysis.class, 3f);
            }

        }
        RogueBoss.MomentumTime momentum = buff(RogueBoss.MomentumTime.class);
        if (momentum != null) {
            momentum.minus(3);
            if (momentum.left <= 0) {
                yell(Messages.get(this, "momentum-recovery"));
            }
        }

        boolean bleeding = HP * 2 <= HT;
        dmg = Math.min(25, dmg);
        int preHP = HP;
        super.damage(dmg, src);
        int dmgTaken = preHP - HP;

        if (phase == 0 && HP < HT / 2 && isAlive()) {
            HP = HT / 2;
            phase = 1;
            TestBossUtil.summonNear(this, RogueBoss.ShadowRogue.class, pos, 1, 4);
        }
        if (HP * 2 <= HT && !bleeding && isAlive()) {
            BossHealthBar.bleed(true);
            sprite.showStatus(CharSprite.WARNING, Messages.get(this, "enraged"));
            Buff.affect(this, RogueBoss.MomentumTime.class);
            yell(Messages.get(this, "momentum"));
        }

        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? dmgTaken * 0.67f : dmgTaken);
        }
    }

    public void execute(Integer target) {
        ScrollOfTeleportation.appear(this, target);
        Char victim = enemy == null ? Dungeon.hero : enemy;
        if (victim == null) {
            return;
        }
        if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
            Buff.affect(victim, Grim.GrimTracker.class).maxChance = 1;
        }
        victim.damage((int)(victim.HT * 0.9f), this);
        yell(Messages.get(this, "execute"));
        RogueBoss.AbsoluteInvisibility.dispel(this);
    }

    private void jumpNearEnemy() {
        Char target = TestBossUtil.firstEnemy(this);
        int center = target == null ? pos : target.pos;
        int newPos = TestBossUtil.randomSpawnCellNear(center, 2, 3);
        if (newPos != -1) {
            if (sprite != null) {
                sprite.move(pos, newPos);
            }
            move(newPos);
            Dungeon.level.occupyCell(this);
        }
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
        bundle.put(INVIS_CD, invisibilityCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        invisibilityCooldown = bundle.getInt(INVIS_CD);
        BossHealthBar.assignBoss(this);
        if (HP * 2 <= HT) {
            BossHealthBar.bleed(true);
        }
    }
}
