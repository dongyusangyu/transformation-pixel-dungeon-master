package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

final class TestBossUtil {

    private TestBossUtil() {
    }

    static void assignBoss(Mob mob) {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(mob);
        }
    }

    static void bossSlain(Mob mob) {
        Dungeon.level.unseal();
        GameScene.bossSlain();
    }

    static boolean canUseCell(int cell) {
        return Dungeon.level.insideMap(cell)
                && Actor.findChar(cell) == null
                && (!Dungeon.level.solid[cell]
                || Dungeon.level.map[cell] == Terrain.DOOR
                || Dungeon.level.map[cell] == Terrain.OPEN_DOOR);
    }

    static int randomSpawnCellNear(int center, int minDistance, int maxDistance) {
        int best = -1;
        for (int i = 0; i < 80; i++) {
            int cell = Random.Int(Dungeon.level.length());
            if (canUseCell(cell)
                    && Dungeon.level.distance(center, cell) >= minDistance
                    && Dungeon.level.distance(center, cell) <= maxDistance) {
                return cell;
            }
            if (best == -1 && canUseCell(cell)) {
                best = cell;
            }
        }

        for (int ofs : PathFinder.NEIGHBOURS8) {
            int cell = center + ofs;
            if (canUseCell(cell)) {
                return cell;
            }
        }
        return best;
    }

    static Mob summonNear(Mob owner, Class<? extends Mob> type, int center, int minDistance, int maxDistance) {
        int cell = randomSpawnCellNear(center, minDistance, maxDistance);
        if (cell == -1) {
            return null;
        }
        Mob mob = Reflection.newInstance(type);
        mob.alignment = owner.alignment;
        mob.updateSpriteState();
        mob.pos = cell;
        mob.state = mob.HUNTING;
        GameScene.add(mob);
        Dungeon.level.occupyCell(mob);
        return mob;
    }

    static Char firstEnemy(Mob mob) {
        if (mob.enemy() != null && mob.enemy().isAlive()) {
            return mob.enemy();
        }
        if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
            return Dungeon.hero;
        }
        for (Char ch : Actor.chars()) {
            if (ch != mob && ch.isAlive() && ch.alignment != mob.alignment) {
                return ch;
            }
        }
        return null;
    }

    static boolean hasVisibleAttackableEnemy(Mob mob) {
        if (mob.fieldOfView == null || mob.fieldOfView.length != Dungeon.level.length()) {
            mob.fieldOfView = new boolean[Dungeon.level.length()];
        }
        Dungeon.level.updateFieldOfView(mob, mob.fieldOfView);
        for (Char ch : Actor.chars()) {
            if (ch != mob
                    && ch.isAlive()
                    && ch.invisible <= 0
                    && Dungeon.level.insideMap(ch.pos)
                    && mob.fieldOfView[ch.pos]
                    && Actor.isHostile(mob, ch)
                    && !ch.isInvulnerable(mob.getClass())) {
                return true;
            }
        }
        return false;
    }

    static Char visibleEnemyOrNull(Mob mob, Char chosen) {
        if (chosen != null
                && chosen.isAlive()
                && chosen.invisible <= 0
                && mob.fieldOfView != null
                && Dungeon.level.insideMap(chosen.pos)
                && mob.fieldOfView.length > chosen.pos
                && mob.fieldOfView[chosen.pos]
                && Actor.isHostile(mob, chosen)
                && !chosen.isInvulnerable(mob.getClass())) {
            return chosen;
        }
        if (!hasVisibleAttackableEnemy(mob)) {
            mob.clearEnemy();
        }
        return null;
    }

    static Char attackerToRetarget(Mob mob, Object src) {
        if (src instanceof Char) {
            Char attacker = (Char)src;
            if (attacker != mob
                    && attacker.isAlive()
                    && attacker.alignment != mob.alignment
                    && attacker.alignment != Char.Alignment.NEUTRAL) {
                return attacker;
            }
        }
        return null;
    }
}
