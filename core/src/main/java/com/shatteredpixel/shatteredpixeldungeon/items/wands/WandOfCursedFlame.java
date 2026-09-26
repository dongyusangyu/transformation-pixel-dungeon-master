package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedFlameDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** A crafted or trap-converted wand that ignites a targeted 3x3 area with cursed flame. */
public class WandOfCursedFlame extends DamageWand {

    private static final float BATTLEMAGE_COOLDOWN = 9f;
    private static final String COOLDOWN_TARGETS = "cursed_flame_cooldown_targets";
    private static final String COOLDOWN_EXPIRES = "cursed_flame_cooldown_expires";
    // High bits identify the floor; low bits are the actor's persistent ID.
    private final HashMap<Long, Float> battlemageCooldowns = new HashMap<>();

    {
        image = EXItemSpriteSheet.WAND_CURSED_FLAME;
        collisionProperties = Ballistica.PROJECTILE;
    }

    @Override public boolean isKnown() {
        // A crafted wand has a fixed, recognizable identity in random mode.
        return true;
    }

    @Override public int image() {
        icon = Dungeon.hero != null && Dungeon.hero.randomMode
                ? ItemSpriteSheet.Icons.WAND_CURSED_FLAME : -1;
        int sprite = super.image();
        if (Dungeon.hero != null && Dungeon.hero.randomMode) {
            icon = ItemSpriteSheet.Icons.WAND_CURSED_FLAME;
        }
        return sprite;
    }

    @Override public String desc() {
        // Do not substitute the unidentified random-wand description.
        return Messages.get(this, "desc");
    }

    @Override public int min(int lvl) { return 2 + lvl; }
    @Override public int max(int lvl) { return 8 + 5 * lvl; }

    public static int flameDuration(int level) {
        return 2 + (Math.max(0, level) + 2) / 3;
    }

    @Override public void onZap(Ballistica bolt) {
        int center = bolt.collisionPos;
        int duration = flameDuration(buffedLvl());
        Char direct = Actor.findChar(center);
        CursedFlame flame = null;
        Set<Char> processed = Collections.newSetFromMap(new IdentityHashMap<Char, Boolean>());

        for (int offset : PathFinder.NEIGHBOURS9) {
            int cell = center + offset;
            if (!CursedFlame.canIgnite(Dungeon.level, cell)) continue;
            flame = Blob.seed(cell, duration, CursedFlame.class);
            Char target = Actor.findChar(cell);
            if (target != null && processed.add(target)) wandProc(target, chargesPerCast());
        }
        if (flame != null) GameScene.add(flame);

        if (direct != null) {
            CursedFlameDamage.applyMagical(direct, damageRoll(direct), this);
            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);
        } else {
            Dungeon.level.pressCell(center);
        }
        Sample.INSTANCE.play(Assets.Sounds.BURNING);
    }

    @Override public void fx(Ballistica bolt, Callback callback) {
        MagicMissile.boltFromChar(curUser.sprite.parent, MagicMissile.CURSED_FLAME,
                curUser.sprite, bolt.collisionPos, callback);
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    @Override public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        CursedBurning burning = defender.buff(CursedBurning.class);
        if (burning == null || !defender.isAlive()) return;

        float now = Actor.absoluteTime();
        for (Iterator<Map.Entry<Long, Float>> it = battlemageCooldowns.entrySet().iterator(); it.hasNext();) {
            if (it.next().getValue() <= now) it.remove();
        }
        long targetKey = ((long) Dungeon.depth << 32) | (defender.id() & 0xffffffffL);
        Float expires = battlemageCooldowns.get(targetKey);
        if (expires != null && expires > now) return;

        float remaining = burning.remaining();
        burning.detach();
        int level = Math.max(0, buffedLvl());
        int extra = Math.round((1f + remaining) * Random.IntRange(1 + level/3, 5 + level/3)
                * procChanceMultiplier(attacker));
        CursedFlameDamage.apply(defender, extra, this);
        if (defender.isAlive()) CursedBurning.apply(defender, CursedBurning.DURATION);
        battlemageCooldowns.put(targetKey, now + BATTLEMAGE_COOLDOWN);
    }

    @Override public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(0x60F802);
        particle.am = 0.6f;
        particle.setLifespan(0.6f);
        particle.acc.set(0, -40);
        particle.setSize(0f, 3f);
        particle.shuffleXY(1.5f);
    }

    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        long[] targets = new long[battlemageCooldowns.size()];
        float[] expires = new float[targets.length];
        int i = 0;
        for (Map.Entry<Long, Float> entry : battlemageCooldowns.entrySet()) {
            targets[i] = entry.getKey();
            expires[i++] = entry.getValue();
        }
        bundle.put(COOLDOWN_TARGETS, targets);
        bundle.put(COOLDOWN_EXPIRES, expires);
    }

    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        battlemageCooldowns.clear();
        if (!bundle.contains(COOLDOWN_TARGETS) || !bundle.contains(COOLDOWN_EXPIRES)) return;
        long[] targets = bundle.getLongArray(COOLDOWN_TARGETS);
        float[] expires = bundle.getFloatArray(COOLDOWN_EXPIRES);
        for (int i = 0; i < Math.min(targets.length, expires.length); i++) {
            battlemageCooldowns.put(targets[i], expires[i]);
        }
    }

    @Override public String statsDesc() {
        int level = levelKnown ? buffedLvl() : 0;
        return Messages.get(this, "stats_desc", min(level), max(level), flameDuration(level));
    }

    @Override public String upgradeStat2(int level) {
        return Integer.toString(flameDuration(level));
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        @Override public boolean testIngredients(ArrayList<Item> ingredients) {
            if (ingredients == null || ingredients.size() != 3) return false;
            boolean missile = false, fireblast = false, shard = false;
            for (Item item : ingredients) {
                if (item == null || item.quantity() < 1) return false;
                if (item.getClass() == WandOfMagicMissile.class && !missile
                        && validWand((Wand) item)) {
                    missile = true;
                } else if (item.getClass() == WandOfFireblast.class && !fireblast
                        && validWand((Wand) item)) {
                    fireblast = true;
                } else if (item.getClass() == MetalShard.class && !shard) {
                    shard = true;
                } else {
                    return false;
                }
            }
            return missile && fireblast && shard;
        }

        private static boolean validWand(Wand wand) {
            if (!wand.isIdentified() || wand.cursed || !wand.cursedKnown
                    || wand.isEquipped(Dungeon.hero)) return false;
            if (Dungeon.hero != null) {
                if (Dungeon.hero.belongings.weapon instanceof MagesStaff
                        && ((MagesStaff) Dungeon.hero.belongings.weapon).wand() == wand) return false;
                if (Dungeon.hero.belongings.secondWep instanceof MagesStaff
                        && ((MagesStaff) Dungeon.hero.belongings.secondWep).wand() == wand) return false;
            }
            return true;
        }

        @Override public int cost(ArrayList<Item> ingredients) { return 5; }

        @Override public Item brew(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;
            WandOfCursedFlame result = (WandOfCursedFlame) sampleOutput(ingredients);
            for (Item item : ingredients) item.quantity(item.quantity() - 1);
            return result;
        }

        @Override public Item sampleOutput(ArrayList<Item> ingredients) {
            int sum = 0;
            if (ingredients != null) {
                for (Item item : ingredients) {
                    if (item instanceof WandOfMagicMissile || item instanceof WandOfFireblast) {
                        sum += item.trueLevel();
                    }
                }
            }
            int level = Math.min(3, Math.max(0, (sum + 1) / 2));
            WandOfCursedFlame result = new WandOfCursedFlame();
            result.level(level);
            result.curCharges = result.maxCharges;
            result.cursed = false;
            result.identify(false);
            return result;
        }
    }
}
