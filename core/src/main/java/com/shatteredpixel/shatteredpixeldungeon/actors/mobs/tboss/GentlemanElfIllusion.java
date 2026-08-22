package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.GentlemanElfSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** A disposable mirror which vanishes after two positive damage events. */
public class GentlemanElfIllusion extends Mob {
	private static final String HITS = "illusion_positive_hits";
	private static final String OWNER = "illusion_owner";
	private int positiveHits;
	private int ownerId = -1;
	private transient GentlemanElf owner;
	private transient boolean deathNotified;

	public GentlemanElfIllusion() {
		HT = HP = 1; defenseSkill = 25; EXP = 0; maxLvl = 0;
		loot = null; lootChance = 0f; properties.add(Property.BOSS);
		properties.add(Property.UNSLEEP);
		spriteClass = GentlemanElfSprite.class;
		state = HUNTING;
	}
	public GentlemanElfIllusion(GentlemanElf owner) { this(); this.owner = owner; if (owner != null) ownerId = owner.id(); }
	@Override public int attackSkill(Char target) { return 50; }
	@Override public String name() { return Messages.get(GentlemanElf.class, "name"); }
	@Override public String description() { return Messages.get(GentlemanElf.class, "desc"); }
	@Override public int damageRoll() { return Random.NormalIntRange(15, 25); }
	@Override public int drRoll() { return Random.NormalIntRange(0, 20); }
	@Override public float speed() { return 1.6f; }
	@Override public float attackDelay() { return 0.5f; }
	@Override protected int modifyFinalDamage(int damage, Object source, DamageTag... tags) {
		int resolved = Math.max(0, super.modifyFinalDamage(damage, source, tags));
		if (resolved <= 0 || positiveHits >= 2) return resolved;
		positiveHits++;
		return positiveHits >= 2 ? resolved : 0;
	}
	@Override public void die(Object cause) {
		if (deathNotified) return;
		deathNotified = true;
		super.die(cause);
		if (owner != null) owner.illusionDied();
	}
	public int positiveHits() { return positiveHits; }
	public void recordPositiveHitForTest() { positiveHits = Math.min(2, positiveHits + 1); }
	public int ownerId() { return ownerId; }
	public void owner(GentlemanElf boss) { owner = boss; ownerId = boss == null ? -1 : boss.id(); }
	public int damageRollMinForTest() { return 15; }
	public int damageRollMaxForTest() { return 25; }
	public Class<?> spriteClassForTest() { return spriteClass; }
	public Class<?> identityMessageClassForTest() { return GentlemanElf.class; }
	@Override public void storeInBundle(Bundle bundle) { super.storeInBundle(bundle); bundle.put(HITS, positiveHits); bundle.put(OWNER, ownerId); }
	@Override public void restoreFromBundle(Bundle bundle) { super.restoreFromBundle(bundle); positiveHits = Math.max(0, Math.min(2, bundle.getInt(HITS))); ownerId = bundle.getInt(OWNER); }
}
