package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.ElfWineCupSprite;
import com.watabou.utils.Bundle;

/** Neutral, one-hit objective used by the second phase. */
public class ElfWineCup extends Mob {
	public interface Listener { void onCupDestroyed(ElfWineCup cup, Char lastHit); }
	private static final String LAST_HIT = "elf_wine_cup_last_hit";
	private Char lastHit;
	private int lastHitId = -1;
	private transient Listener listener;
	public ElfWineCup() {
		HT = HP = 30; defenseSkill = 0; EXP = 0; maxLvl = 0;
		loot = null; lootChance = 0f; spriteClass = ElfWineCupSprite.class;
		alignment = Alignment.NEUTRAL;
		properties.add(Property.IMMOVABLE);
		properties.add(Property.UNSLEEP);
	}
	@Override protected boolean act() { spend(TICK); return true; }
	@Override public int attackSkill(Char target) { return 0; }
	@Override public int damageRoll() { return 0; }
	@Override public int drRoll() { return 0; }
	@Override public void damage(int damage, Object source, DamageTag... tags) {
		if (damage > 0 && source instanceof Char) {
			lastHit = (Char) source;
			lastHitId = lastHit.id();
		}
		super.damage(damage, source, tags);
	}
	@Override public boolean heroShouldInteract() { return false; }
	@Override public void die(Object cause) {
		super.die(cause);
		if (listener != null) listener.onCupDestroyed(this, lastHit);
	}
	public Char lastHit() { return lastHit; }
	public int lastHitId() { return lastHitId; }
	public void rebindLastHit(Actor actor) { lastHit = actor instanceof Char ? (Char) actor : null; }
	public void listener(Listener value) { listener = value; }
	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LAST_HIT, lastHitId);
	}
	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastHitId = bundle.contains(LAST_HIT) ? bundle.getInt(LAST_HIT) : -1;
		Actor actor = Actor.findById(lastHitId);
		lastHit = actor instanceof Char ? (Char) actor : null;
	}
}
