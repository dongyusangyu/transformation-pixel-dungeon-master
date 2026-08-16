package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.AlienatedPrismaticGuardSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** A hostile prismatic guard which continuously mirrors the hero's combat equipment. */
public class AlienatedPrismaticGuard extends HeroReplicaMob {

	private static final int SPLIT_INTERVAL = 5;

	private static final String HERO_SEEN = "hero_seen";
	private static final String FIRST_SPLIT_PENDING = "first_split_pending";
	private static final String SPLIT_CHARGE = "split_charge";

	private boolean heroSeen;
	private boolean firstSplitPending;
	private int splitCharge;

	{
		spriteClass = AlienatedPrismaticGuardSprite.class;

		HP = HT = 100;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		alignment = Alignment.ENEMY;
		properties.add(Property.INORGANIC);
		immunities.add(ToxicGas.class);
		immunities.add(CorrosiveGas.class);
		immunities.add(Burning.class);
		immunities.add(AllyBuff.class);

		loot = ScrollOfMirrorImage.class;
		lootChance = 1f / 8f;
	}

	@Override
	protected HeroEquipmentReplica.Scope replicaScope() {
		return HeroEquipmentReplica.Scope.GUARD;
	}

	@Override
	protected boolean act() {
		refreshReplica();

		if (paralysed > 0 || state == SLEEPING) {
			return performBaseAct();
		}

		if (!heroSeen && canSeeHeroForSplit()) {
			heroSeen = true;
			firstSplitPending = true;
			splitCharge = 0;
		}

		if (firstSplitPending) {
			if (trySplit()) {
				firstSplitPending = false;
			}
			return performBaseAct();
		}

		if (heroSeen) {
			splitCharge = Math.min(SPLIT_INTERVAL, splitCharge + 1);
			if (splitCharge == SPLIT_INTERVAL && trySplit()) {
				splitCharge = 0;
				spend(TICK);
				return true;
			}
		}

		return performBaseAct();
	}

	protected boolean canSeeHeroForSplit() {
		if (Dungeon.hero == null || Dungeon.level == null) {
			return false;
		}
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);
		return fieldOfView != null
				&& Dungeon.hero.pos >= 0
				&& Dungeon.hero.pos < fieldOfView.length
				&& fieldOfView[Dungeon.hero.pos]
				&& Dungeon.hero.invisible <= 0;
	}

	private boolean trySplit() {
		int cell = findSplitCell();
		if (cell == -1) {
			return false;
		}

		TwistedMirror mirror = createTwistedMirror();
		mirror.pos = cell;
		mirror.alignment = alignment;
		mirror.state = mirror.HUNTING;
		boolean added = addTwistedMirrorToLevel(mirror, cell);
		if (added && sprite instanceof AlienatedPrismaticGuardSprite) {
			((AlienatedPrismaticGuardSprite) sprite).splitEffect(cell);
		}
		return added;
	}

	protected int findSplitCell() {
		if (Dungeon.level == null) {
			return -1;
		}

		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (cell >= 0
					&& cell < Dungeon.level.length()
					&& Dungeon.level.passable[cell]
					&& !Dungeon.level.pit[cell]
					&& Actor.findChar(cell) == null) {
				cells.add(cell);
			}
		}
		return cells.isEmpty() ? -1 : Random.element(cells);
	}

	protected TwistedMirror createTwistedMirror() {
		return new TwistedMirror();
	}

	protected boolean addTwistedMirrorToLevel(TwistedMirror mirror, int cell) {
		if (Dungeon.level == null || mirror == null || mirror.pos != cell) {
			return false;
		}
		GameScene.add(mirror);
		Dungeon.level.occupyCell(mirror);
		return true;
	}

	protected boolean performBaseAct() {
		return performReplicaBaseAct();
	}

	protected boolean heroSeen() {
		return heroSeen;
	}

	protected boolean firstSplitPending() {
		return firstSplitPending;
	}

	protected int splitCharge() {
		return splitCharge;
	}

	@Override
	public float lootChance() {
		return adjustedLootChance(1f / 8f);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HERO_SEEN, heroSeen);
		bundle.put(FIRST_SPLIT_PENDING, firstSplitPending);
		bundle.put(SPLIT_CHARGE, splitCharge);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		heroSeen = bundle.getBoolean(HERO_SEEN);
		firstSplitPending = bundle.getBoolean(FIRST_SPLIT_PENDING);
		splitCharge = Math.max(0, Math.min(SPLIT_INTERVAL, bundle.getInt(SPLIT_CHARGE)));
		normalizeSplitState();
	}

	private void normalizeSplitState() {
		if (!heroSeen) {
			firstSplitPending = false;
			splitCharge = 0;
		} else if (firstSplitPending) {
			splitCharge = 0;
		}
	}
}
