/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class SakuraBlossomBlade extends AssassinsBlade {

	public static final String AC_EVOLVE = "EVOLVE";

	private static final int HITS_PER_SPECIAL = 5;
	private static final int KILLS_TO_EVOLVE = 58;
	private static final String HIT_COUNT = "hit_count";
	private static final String KILL_COUNT = "kill_count";
	private static final String EVOLVED = "evolved";

	private final State state = new State();

	{
		image = EXItemSpriteSheet.SAKURA_BLOSSOM;
		tier = 6;
		RCH = 1;
	}

	@Override
	public int image() {
		return state.image();
	}

	@Override
	public String name() {
		String baseName = Messages.get(this, state.isEvolved() ? "evolved_name" : "name");
		if (isEquipped(Dungeon.hero)
				&& !hasCurseEnchant()
				&& Dungeon.hero.buff(HolyWeapon.HolyWepBuff.class) != null
				&& (Dungeon.hero.subClass != HeroSubClass.PALADIN || enchantment == null)) {
			return Messages.get(HolyWeapon.class, "ench_name", baseName);
		}
		return enchantment != null && (cursedKnown || !enchantment.curse())
				? enchantment.name(baseName)
				: baseName;
	}

	@Override
	public String desc() {
		return Messages.get(this, state.isEvolved() ? "evolved_desc" : "desc");
	}

	@Override
	public String statsInfo() {
		if (state.isEvolved()) {
			return Messages.get(this, "evolved_stats_desc");
		} else if (canEvolve()) {
			return Messages.get(this, "stats_desc_ready");
		} else {
			return Messages.get(this, "stats_desc", killsUntilEvolution());
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		Dagger.sneakAbility(hero, target, 3, 2, this);
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc", 2);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return "2";
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (canEvolve()) actions.add(AC_EVOLVE);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (AC_EVOLVE.equals(action)) return Messages.get(this, "ac_evolve");
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_EVOLVE.equals(action) && evolve()) {
			hero.sprite.operate(hero.pos);
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			GLog.p(Messages.get(this, "evolved"));
			updateQuickslot();
			hero.spendAndNext(Actor.TICK);
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);
		if (attacker != Dungeon.hero
				|| !(attacker instanceof Hero)
				|| ((Hero) attacker).belongings.attackingWeapon() != this
				|| defender.alignment != Char.Alignment.ENEMY) {
			return damage;
		}

		if (state.isEvolved()) {
			int healing = Math.min(healingForDamage(damage), attacker.HT - attacker.HP);
			if (healing > 0 && attacker.isAlive()) attacker.heal(healing);
		} else if (recordSuccessfulHit()) {
			Buff.prolong(defender, Vulnerable.class, 2f);
			Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
			trajectory = new Ballistica(trajectory.collisionPos,
					trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
			WandOfBlastWave.throwChar(defender, trajectory, 2, true, false, this);
		}
		return damage;
	}

	boolean recordSuccessfulHit() {
		boolean triggered = state.recordSuccessfulHit();
		updateQuickslot();
		return triggered;
	}

	int hitsUntilSpecial() {
		return state.hitsUntilSpecial();
	}

	boolean recordKill() {
		boolean ready = state.recordKill();
		updateQuickslot();
		return ready;
	}

	public void onDirectKill() {
		if (recordKill()) GLog.p(Messages.get(this, "ready"));
	}

	public boolean canEvolve() {
		return state.canEvolve();
	}

	public int killsUntilEvolution() {
		return state.killsUntilEvolution();
	}

	public boolean isEvolved() {
		return state.isEvolved();
	}

	boolean evolve() {
		if (!state.evolve()) return false;
		updateQuickslot();
		return true;
	}

	static int healingForDamage(int damage) {
		return Math.max(0, Math.round(damage * 0.1f));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		state.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		state.restoreFromBundle(bundle);
	}

	static class State {

		private int hitCount;
		private int killCount;
		private boolean evolved;

		boolean recordSuccessfulHit() {
			if (evolved) return false;
			hitCount = (hitCount + 1) % HITS_PER_SPECIAL;
			return hitCount == 0;
		}

		int hitsUntilSpecial() {
			return evolved ? 0 : HITS_PER_SPECIAL - hitCount;
		}

		boolean recordKill() {
			if (evolved || killCount >= KILLS_TO_EVOLVE) return false;
			killCount++;
			return killCount == KILLS_TO_EVOLVE;
		}

		boolean canEvolve() {
			return !evolved && killCount >= KILLS_TO_EVOLVE;
		}

		int killsUntilEvolution() {
			return Math.max(0, KILLS_TO_EVOLVE - killCount);
		}

		boolean isEvolved() {
			return evolved;
		}

		boolean evolve() {
			if (!canEvolve()) return false;
			evolved = true;
			hitCount = 0;
			return true;
		}

		int image() {
			return evolved ? EXItemSpriteSheet.BLOOD_SAKURA : EXItemSpriteSheet.SAKURA_BLOSSOM;
		}

		void storeInBundle(Bundle bundle) {
			bundle.put(HIT_COUNT, hitCount);
			bundle.put(KILL_COUNT, killCount);
			bundle.put(EVOLVED, evolved);
		}

		void restoreFromBundle(Bundle bundle) {
			hitCount = Math.max(0, bundle.getInt(HIT_COUNT)) % HITS_PER_SPECIAL;
			killCount = Math.max(0, bundle.getInt(KILL_COUNT));
			evolved = bundle.getBoolean(EVOLVED);
			if (evolved) hitCount = 0;
		}
	}
}
