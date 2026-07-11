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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CapeOfThorns extends Artifact {

	private static final String COOLDOWN = "cooldown";

	{
		image = ItemSpriteSheet.ARTIFACT_CAPE;

		levelCap = 10;

		charge = 0;
		chargeCap = 100;
		cooldown = 0;

		defaultAction = AC_RELEASE;
	}

	public static final String AC_RELEASE = "RELEASE";

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Thorns();
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (cursed) {
			return actions;
		}
		if (isEquipped( hero ) && !cursed && charge >= chargeCap) {
			actions.add( AC_RELEASE );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (AC_RELEASE.equals( action )) {
			if (cursed) {
				GLog.i( Messages.get(this, "cursed") );
				return;
			} else if (!isEquipped( hero )) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				return;
			}
			if (!canApplyThorns(hero.buff(ThornsEffect.class) != null)) {
				GLog.i( Messages.get(this, "effect_conflict") );
				return;
			} else if (charge < chargeCap) {
				GLog.i( Messages.get(this, "no_charge") );
				return;
			}

			super.execute( hero, action );
			Invisibility.dispel();
			releaseEnergy(hero);
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			hero.sprite.operate(hero.pos);
			hero.spendAndNext( Actor.TICK );
		} else {
			super.execute( hero, action );
		}
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;
		gainCharge(4*amount, true);
	}

	@Override
	public void onHeroGainExp( float levelPercent, Hero hero ) {
		if (isEquipped( hero ) && !cursed) {
			gainExpCharge(levelPercent, hero);
		}
	}
	
	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (isEquipped( Dungeon.hero )) {
			desc += "\n\n";
			if (cursed)
				desc += Messages.get(this, "desc_cursed");
			else if (Dungeon.hero == null || Dungeon.hero.buff(ThornsEffect.class) == null)
				desc += Messages.get(this, "desc_inactive");
			else
				desc += Messages.get(this, "desc_active");
		}

		return desc;
	}

	private void gainCharge(float amount, boolean announceFull) {
		if ((Dungeon.hero != null && Dungeon.hero.buff(ThornsEffect.class) != null) || cursed || amount <= 0) {
			return;
		}

		boolean wasFull = charge >= chargeCap;
        amount=Math.min(amount,100);
		partialCharge += amount;
		int gained = (int)partialCharge;
		partialCharge -= gained;
		charge = Math.min(charge + gained, chargeCap);
        charge = Math.max(charge, 0);

		if (!wasFull && charge >= chargeCap && announceFull) {
			GLog.p( Messages.get(Thorns.class, "full_charge") );
		}
		updateQuickslot();
	}

	private void releaseEnergy(Hero hero) {
		charge = 0;
		partialCharge = 0;
		Buff.affect(hero, ThornsEffect.class, 10 + level()).setLevel(level());
		GLog.p( Messages.get(Thorns.class, "radiating") );
		updateQuickslot();
	}

	private void gainExp(int amount) {
		if (amount <= 0 || level() >= levelCap) {
			return;
		}

		exp += amount;
		while (level() < levelCap && exp >= 100 + level()*20) {
			exp -= 100 + level()*20;
			upgrade();
			Catalog.countUse(CapeOfThorns.class);
			GLog.p( Messages.get(Thorns.class, "levelup") );
		}
	}

	private void gainExpCharge(float levelPercent, Char target) {
		if (cursed || target.buff(MagicImmune.class) != null) return;
		gainCharge((50 + level()*5) * levelPercent * RingOfEnergy.artifactChargeMultiplier(target), true);
		gainExp(Math.round(100 * levelPercent));
	}

	public boolean applyTrinityThorns(Hero hero, int turns, int effectLevel) {
		if (!canApplyThorns(hero.buff(ThornsEffect.class) != null)) {
			GLog.i(Messages.get(this, "effect_conflict"));
			return false;
		}
		Buff.affect(hero, ThornsEffect.class, turns).setLevel(effectLevel);
		return true;
	}

	public static boolean canApplyThorns(boolean hasExistingEffect) {
		return !hasExistingEffect;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COOLDOWN, cooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		cooldown = bundle.getInt(COOLDOWN);
	}

	public class Thorns extends ArtifactBuff{

		@Override
		public boolean act(){
			// Migrate active effects saved before thorns was split into a flavour buff.
			if (cooldown > 0) {
				if (target.buff(ThornsEffect.class) == null) {
					Buff.append(target, ThornsEffect.class, cooldown).setLevel(level());
				}
				cooldown = 0;
			}
			spend(TICK);
			return true;
		}

		public int proc(int damage, Char attacker, Char defender){
			if (damage <= 0) {
				updateQuickslot();
				return damage;
			}

			if (cursed) {
				if (defender != null && Random.Float() < 0.1f) {
					float hpRatio = defender.HP / (float)defender.HT;
					Buff.affect(defender, Bleeding.class).set(hpRatio * hpRatio * defender.HT / 8f, CapeOfThorns.class);
				}
				updateQuickslot();
				return damage;
			}

			if (target.buff(ThornsEffect.class) == null){
				float chargeMultiplier = defender == null ? 1f : RingOfEnergy.artifactChargeMultiplier(defender);
				CapeOfThorns.this.gainCharge(damage*(0.5f+level()*0.05f)*chargeMultiplier, true);
			}
			updateQuickslot();
			return damage;
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}

		public void gainCharge(float levelPortion) {
			gainExpCharge(levelPortion, target);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
            bundle.put(COOLDOWN, cooldown);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
            cooldown = bundle.getInt(COOLDOWN);
		}

	}

	public static class ThornsEffect extends FlavourBuff {

		private static final String EFFECT_LEVEL = "effect_level";
		private int effectLevel;

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		public ThornsEffect setLevel(int effectLevel) {
			this.effectLevel = Math.max(0, effectLevel);
			return this;
		}

		public int proc(int damage, Char attacker) {
			if (damage <= 0) return damage;
			int deflected = Random.NormalIntRange(minimumDeflection(damage, effectLevel), damage);
			if (attacker != null) attacker.damage(deflected, this);
			return damage - deflected;
		}

		@Override
		public boolean act() {
			GLog.w(Messages.get(Thorns.class, "inert"));
			return super.act();
		}

		public static int minimumDeflection(int damage, int effectLevel) {
			return Math.min(damage, Math.round(damage * 0.05f * effectLevel));
		}

		@Override
		public int icon() {
			return BuffIndicator.THORNS;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(EFFECT_LEVEL, effectLevel);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			effectLevel = bundle.getInt(EFFECT_LEVEL);
		}
	}


}
