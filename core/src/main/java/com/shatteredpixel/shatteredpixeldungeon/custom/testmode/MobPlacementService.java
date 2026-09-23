package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Reflection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class MobPlacementService {

    enum Failure {
        NONE,
        NO_LEVEL,
        OUT_OF_BOUNDS,
        OCCUPIED,
        BLOCKED,
        BOSS_ALREADY_PRESENT,
        CREATE_FAILED
    }

    static final List<Class<? extends ChampionEnemy>> ELITE_BUFFS =
            Collections.unmodifiableList(Arrays.asList(
                    ChampionEnemy.Blazing.class,
                    ChampionEnemy.AntiMagic.class,
                    ChampionEnemy.Blessed.class,
                    ChampionEnemy.Giant.class,
                    ChampionEnemy.Growing.class,
                    ChampionEnemy.Projecting.class,
                    ChampionEnemy.Corrosion.class,
                    ChampionEnemy.Haste.class,
                    ChampionEnemy.Holy.class,
                    ChampionEnemy.Transform.class
            ));

    private MobPlacementService() {
    }

    static Failure validateTarget(int cell, MobPlacementCatalog.SpawnMode mode) {
        if (Dungeon.level == null) return Failure.NO_LEVEL;
        if (cell < 0 || cell >= Dungeon.level.length()) return Failure.OUT_OF_BOUNDS;
        if (Actor.findChar(cell) != null) return Failure.OCCUPIED;
        if (Dungeon.level.solid[cell]
                && Dungeon.level.map[cell] != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR
                && Dungeon.level.map[cell] != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.OPEN_DOOR) {
            return Failure.BLOCKED;
        }
        if ((mode == MobPlacementCatalog.SpawnMode.TEST_BOSS
                || mode == MobPlacementCatalog.SpawnMode.TOWER_BOSS)
                && BossHealthBar.isAssigned()) {
            return Failure.BOSS_ALREADY_PRESENT;
        }
        return Failure.NONE;
    }

    static Mob createPrepared(Class<? extends Mob> type,
                              MobPlacementCatalog.SpawnMode mode) {
        try {
            Mob mob = Reflection.newInstance(type);
            prepareHeroBoundMob(mob);
            if (mob instanceof Bee) {
                Bee bee = (Bee) mob;
                bee.spawn(Dungeon.level == null ? 0 : Dungeon.scalingDepth());
                bee.HP = bee.HT;
                bee.setPotInfo(-1, null);
                bee.state = mob.WANDERING;
            }
            if (mob instanceof Statue) {
                ((Statue) mob).createWeapon(false);
            }
            if (mob instanceof Wraith && Dungeon.level != null) {
                ((Wraith) mob).adjustStats(Dungeon.scalingDepth(), false);
            }
            if (mob instanceof Mimic) {
                ((Mimic) mob).items = null;
                ((Mimic) mob).setLevel(Dungeon.scalingDepth());
                if (mob instanceof CrystalMimic) {
                    ((CrystalMimic) mob).items = new java.util.ArrayList<>();
                    ((CrystalMimic) mob).items.add(Generator.random());
                }
            }
            if (mode == MobPlacementCatalog.SpawnMode.TOWER_MOB) {
                mob.state = mob instanceof MimicCrocodile ? mob.WANDERING : mob.SLEEPING;
            } else if (mode == MobPlacementCatalog.SpawnMode.BOSS_COMPONENT) {
                prepareBossComponent(mob);
            }
            if (mode == MobPlacementCatalog.SpawnMode.TOWER_BOSS && mob instanceof TowerBoss) {
                ((TowerBoss) mob).prepareForStandalonePlacement(Dungeon.hero);
            }
            return mob;
        } catch (Exception exception) {
            throw new PlacementException(Failure.CREATE_FAILED, exception);
        }
    }

    static String displayName(Class<? extends Mob> type) {
        try {
            Mob mob = Reflection.newInstance(type);
            if (mob instanceof WandOfWarding.Ward) {
                if (mob instanceof WandOfWarding.Ward.WardSentry) {
                    for (int i = 0; i < 4; i++) {
                        ((WandOfWarding.Ward) mob).upgrade(3);
                    }
                } else {
                    ((WandOfWarding.Ward) mob).upgrade(0);
                }
            }
            return mob.name();
        } catch (Exception exception) {
            throw new PlacementException(Failure.CREATE_FAILED, exception);
        }
    }

    private static void prepareBossComponent(Mob mob) {
        if (mob instanceof ElfWineCup) {
            mob.state = mob.PASSIVE;
            return;
        }
        mob.state = mob.HUNTING;
        if (mob instanceof Pylon) {
            mob.alignment = Mob.Alignment.ENEMY;
        }
    }

    private static void prepareHeroBoundMob(Mob mob) {
        if (!(mob instanceof MirrorImage) && !(mob instanceof PrismaticImage)) return;
        if (Dungeon.hero == null) {
            throw new IllegalStateException("hero-bound mob requires an active hero");
        }
        if (mob instanceof MirrorImage) {
            ((MirrorImage) mob).duplicate(Dungeon.hero);
        } else {
            ((PrismaticImage) mob).duplicate(Dungeon.hero,
                    PrismaticGuard.maxHP(Dungeon.hero));
        }
    }

    static Mob place(Class<? extends Mob> type,
                     MobPlacementCatalog.SpawnMode mode,
                     int cell,
                     int eliteOptions) {
        Failure failure = validateTarget(cell, mode);
        if (failure != Failure.NONE) throw new PlacementException(failure, null);

        Mob mob = createPrepared(type, mode);
        try {
            mob.pos = cell;
            GameScene.add(mob);
            if (mode == MobPlacementCatalog.SpawnMode.TOWER_BOSS) {
                // BossHealthBar only accepts actors already registered in the level.
                BossHealthBar.assignBoss(mob);
            }
            if (mode == MobPlacementCatalog.SpawnMode.STANDARD
                    || mode == MobPlacementCatalog.SpawnMode.TOWER_MOB) {
                applyEliteOptions(mob, eliteOptions);
            }
            ScrollOfTeleportation.appear(mob, cell);
            Dungeon.level.occupyCell(mob);
            return mob;
        } catch (Exception exception) {
            mob.destroy();
            ShatteredPixelDungeon.reportException(exception);
            throw new PlacementException(Failure.CREATE_FAILED, exception);
        }
    }

    static void applyEliteOptions(Mob mob, int eliteOptions) {
        for (int i = 0; i < ELITE_BUFFS.size(); i++) {
            if ((eliteOptions & (1 << i)) != 0) {
                Buff.affect(mob, ELITE_BUFFS.get(i));
            }
        }
    }

    static void logFailure(Class<?> owner, Failure failure) {
        String key;
        switch (failure) {
            case BOSS_ALREADY_PRESENT:
                key = "boss_present";
                break;
            case NO_LEVEL:
            case OUT_OF_BOUNDS:
                key = "out_of_bounds";
                break;
            case OCCUPIED:
            case BLOCKED:
                key = "forbidden";
                break;
            default:
                key = "create_failed";
                break;
        }
        GLog.w(com.shatteredpixel.shatteredpixeldungeon.custom.messages.M.L(owner, key));
    }

    static final class PlacementException extends RuntimeException {
        private final Failure failure;

        PlacementException(Failure failure, Throwable cause) {
            super(cause);
            this.failure = failure;
        }

        Failure failure() {
            return failure;
        }
    }
}
