/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import com.watabou.utils.Bundle;
import com.watabou.noosa.audio.Sample;

public class LakeSword extends MeleeWeapon {

	public static final int TIER = 6;
	public static final String AC_DRAW = "DRAW";
	public enum State { SHEATHED, CHARGED, SPENT }

	private static final String STATE = "lake_sword_state";
	private static final String MAGIC_TURNS = "lake_sword_magic_turns";

	private State state = State.SHEATHED;
	private int magicTurns;

	{
		tier = TIER;
		ACC = 1f;
		DLY = 1f;
		RCH = 1;
		hitSound = Assets.Sounds.HIT_SLASH;
		syncImage();
	}

	@Override
	public int min(int level) {
		return 5 + level;
	}

	@Override
	public int max(int level) {
		return 30 + 7 * level;
	}

	@Override
	public int STRReq(int level) {
		return STRReq(TIER, level);
	}

	@Override
	public String desc() {
        String des = Messages.get(this, "desc");
		if (state == State.CHARGED) return des + "\n" + Messages.get(this, "charged_desc");
		if (state == State.SPENT) return des + "\n" + Messages.get(this, "spent_desc");
		return des;
	}

	public State state() {
		return state;
	}

	public int magicTurns() {
		return magicTurns;
	}

	public boolean isSheathed() {
		return state == State.SHEATHED;
	}

	public boolean isCharged() {
		return state == State.CHARGED;
	}

	@Override
	public String defaultAction() {
		Hero hero = Dungeon.hero;
		if (hero != null && canDraw(hero)) return AC_DRAW;
		return super.defaultAction();
	}

	@Override
	public java.util.ArrayList<String> actions(Hero hero) {
		java.util.ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && canDraw(hero)) actions.add(AC_DRAW);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (AC_DRAW.equals(action)) return Messages.upperCase(Messages.get(this, "draw_name"));
		return super.actionName(action, hero);
	}

	private boolean canDraw(Hero hero) {
		return hero != null && isSheathed() && !hero.belongings.lostInventory()
				&& (isEquipped(hero) || (hero.belongings.weapon == null && hero.belongings.backpack.contains(this)));
	}

	@Override
	public void execute(final Hero hero, String action) {
		if (!AC_DRAW.equals(action)) {
			super.execute(hero, action);
			return;
		}
		usesTargeting = true;
		if (!canDraw(hero)) return;
		if (!isEquipped(hero)) {
			if (hero.belongings.weapon != null || !hero.belongings.backpack.contains(this)) return;
			detachAll(hero.belongings.backpack);
			hero.belongings.weapon = this;
			activate(hero);
			LakeSword.MagicTracker.sync(hero);
		}
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell != null) draw(hero, cell);
			}

			@Override
			public String prompt() {
				return Messages.get(LakeSword.this, "draw_prompt");
			}
		});
	}

	private void draw(Hero hero, int target) {
		if (!isSheathed() || !isEquipped(hero)) return;
		if (target == hero.pos) return;
		int level = buffedLvl();
		Ballistica beam =
				new Ballistica(
						hero.pos, target,
						Ballistica.WONT_STOP);
		int end = Math.min(drawRangeForLevel(level), beam.dist);
		if (end < 1) return;
		for (int i = 1; i <= end; i++) {
			int cell = beam.path.get(i);
			if (Dungeon.level.flamable[cell]) {
				Dungeon.level.destroy(cell);
                GameScene.updateMap( cell );
			}
			Char ch = Actor.findChar(cell);
			if (ch != null && ch != hero && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()) {
				com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon previous = hero.belongings.abilityWeapon;
				hero.belongings.abilityWeapon = this;
				try {
					hero.attack(ch, 1f, 0f, Float.POSITIVE_INFINITY,
						DamageTag.PHYSICAL, DamageTag.MELEE);
				} finally {
					hero.belongings.abilityWeapon = previous;
				}
			}
		}
		int endpoint = beam.path.get(end);
		if (hero.sprite != null && hero.sprite.parent != null) {
			hero.sprite.parent.add(new Beam.LightRay(hero.sprite.center(),
					DungeonTilemap.raisedTileCenterToWorld(endpoint)));
		}
		Invisibility.dispel();
		enterCharged(hero, level);
		hero.spendAndNext(1f);
        updateQuickslot();

	}

	@Override
	public void doDrop(Hero hero) {
		super.doDrop(hero);
		if (!hero.belongings.contains(this)) {
			exhaustMagic();
			MagicTracker.sync(hero);
		}
	}

	@Override
	public void onThrow(int cell) {
		super.onThrow(cell);
		if (curUser != null && !curUser.belongings.contains(this)) {
			exhaustMagic();
			MagicTracker.sync(curUser);
		}
	}

	public static int drawRangeForLevel(int level) {
		return Math.max(1, 5 + level);
	}

	public static int chargeDurationForLevel(int level) {
		return Math.max(1, 4 + level);
	}

	public static int chargedDamage(int baseDamage) {
		return Math.round(baseDamage * 1.5f);
	}

	public void enterCharged(Hero hero, int effectiveLevel) {
		state = State.CHARGED;
		magicTurns = chargeDurationForLevel(effectiveLevel);
		syncImage();
		MagicTracker.sync(hero);
	}

	void tickMagic() {
		if (state != State.CHARGED) return;
		if (--magicTurns <= 0) exhaustMagic();
	}

	public void exhaustMagic() {
		if (state == State.CHARGED) state = State.SPENT;
		magicTurns = 0;
		syncImage();
		updateQuickslot();
	}

	public boolean restoreSheath() {
		if (state == State.SHEATHED) return false;
		state = State.SHEATHED;
		magicTurns = 0;
		syncImage();
		updateQuickslot();
		return true;
	}

	private void syncImage() {
		image = state == State.SHEATHED
				? EXItemSpriteSheet.LAKE_SWORD_SHEATHED
				: EXItemSpriteSheet.LAKE_SWORD;
	}

	@Override
	public int damageRoll(Char owner) {
		int rolled = super.damageRoll(owner);
		return chargedMeleeAttack(owner) ? chargedDamage(rolled) : rolled;
	}

	@Override
	public int reachFactor(Char owner) {
		int reach = super.reachFactor(owner);
		return chargedMeleeAttack(owner) ? reach + 1 : reach;
	}

	private boolean chargedMeleeAttack(Char owner) {
		return state == State.CHARGED
				&& owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this
				&& (((Hero) owner).belongings.weapon() == this
				|| ((Hero) owner).belongings.secondWep() == this);
	}

	public static boolean hasChargedSword(Hero hero) {
		if (hero == null) return false;
		for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) {
			if (sword.isCharged()) return true;
		}
		return false;
	}

	public static LakeSword firstRestorable(Hero hero) {
		if (hero == null) return null;
		if (hero.belongings.weapon() instanceof LakeSword
				&& !((LakeSword) hero.belongings.weapon()).isSheathed()) {
			return (LakeSword) hero.belongings.weapon();
		}
		if (hero.belongings.secondWep() instanceof LakeSword
				&& !((LakeSword) hero.belongings.secondWep()).isSheathed()) {
			return (LakeSword) hero.belongings.secondWep();
		}
		for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) {
			if (sword != hero.belongings.weapon() && sword != hero.belongings.secondWep()
					&& !sword.isSheathed()) return sword;
		}
		return null;
	}

	public static boolean hasUnsheathedSword(Hero hero) {
		return firstRestorable(hero) != null;
	}

	public static boolean hasEquippedSheathedSword(Hero hero) {
		return hero != null && ((hero.belongings.weapon() instanceof LakeSword
				&& ((LakeSword) hero.belongings.weapon()).isSheathed())
				|| (hero.belongings.secondWep() instanceof LakeSword
				&& ((LakeSword) hero.belongings.secondWep()).isSheathed()));
	}

	public static float naturalRegenDelayFactor(Hero hero) {
		return hasEquippedSheathedSword(hero) ? 0.5f : 1f;
	}

	public static int windProtectionDurationForLevel(int level) {
		return Math.max(1, 2 + level);
	}

	void applyWindProtection(Hero hero) {
		Buff.prolong(hero, Invisibility.class, windProtectionDurationForLevel(buffedLvl()));
		Buff.prolong(hero, Haste.class, 5f);
	}

	@Override
	protected void duelistAbility(Hero hero, Integer ignored) {
		beforeAbilityUsed(hero, null);
		applyWindProtection(hero);
		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return 1;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, "ability_desc", windProtectionDurationForLevel(buffedLvl()));
	}

	public static class MagicTracker extends Buff {
		public static void sync(Hero hero) {
			if (hero == null) return;
			if (hasChargedSword(hero)) {
				Buff.affect(hero, MagicTracker.class);
			} else {
				MagicTracker tracker = hero.buff(MagicTracker.class);
				if (tracker != null) tracker.detach();
			}
		}

		@Override
		public boolean act() {
			Hero hero = (Hero) target;
			for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) {
				sword.tickMagic();
			}
			if (!hasChargedSword(hero)) {
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}
	}

	public static class Scabbard extends Item {
		{
			image = EXItemSpriteSheet.LAKE_SWORD_SCABBARD;
			stackable = false;
			dropsDownHeap = true;
		}

		@Override
		public boolean doPickUp(Hero hero, int pos) {
			LakeSword restored = firstRestorable(hero);
			if (restored == null) {
				GLog.w(Messages.get(this, "no_sword"));
				return false;
			}
			restored.restoreSheath();
			MagicTracker.sync(hero);
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			updateQuickslot();
			hero.spendAndNext(pickupDelay());
			return true;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public int value() {
			return 0;
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STATE, state.name());
		bundle.put(MAGIC_TURNS, magicTurns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		State restored = State.SHEATHED;
		if (bundle.contains(STATE)) {
			try {
				restored = State.valueOf(bundle.getString(STATE));
			} catch (IllegalArgumentException ignored) {
				restored = State.SHEATHED;
			}
		}
		state = restored;
		magicTurns = state == State.CHARGED
				? Math.max(1, bundle.getInt(MAGIC_TURNS)) : 0;
		syncImage();
	}
}
