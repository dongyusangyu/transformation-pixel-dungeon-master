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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Momentum extends Buff implements ActionIndicator.Action {
	
	{
		type = buffType.POSITIVE;

		//acts before the hero
		actPriority = HERO_PRIO+1;
	}
	
	private int momentumStacks = 0;
	private int freerunTurns = 0;
	private int freerunCooldown = 0;

	private boolean movedLastTurn = true;

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	@Override
	public boolean act() {
		boolean fullyRecovered = freerunCooldown == 1;
		if (freerunCooldown > 0){
			freerunCooldown--;
		}

		if (freerunCooldown == 0 && !freerunning() && target.invisible > 0 && Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH) >= 1){
			momentumStacks = Math.min(momentumStacks + 2, 10);
			movedLastTurn = true;
			ActionIndicator.setAction(this);
			BuffIndicator.refreshHero();
		}

		if (freerunTurns > 0){
			if (target.invisible == 0 || Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH) < 2) {
				freerunTurns--;
				if (freerunTurns == 0) {
					onFreerunEnded();
				}
			}
		} else if (!movedLastTurn){
			momentumStacks = (int)GameMath.gate(0, momentumStacks-1, Math.round(momentumStacks * 0.667f));
			if (momentumStacks <= 0) {
				ActionIndicator.clearAction(this);
				BuffIndicator.refreshHero();
			} else {
				ActionIndicator.refresh();
			}
		}
		if (fullyRecovered && freerunCooldown == 0) {
			onFullyRecovered();
		}
		movedLastTurn = false;

		spend(TICK);
		return true;
	}
	
	public void gainStack(){
		movedLastTurn = true;
		if (freerunCooldown <= 0 && !freerunning()){
			postpone(target.cooldown()+(1/target.speed()));
			momentumStacks = Math.min(momentumStacks + 1, 10);
			ActionIndicator.setAction(this);
			BuffIndicator.refreshHero();
		}
	}

	public boolean freerunning(){
		return freerunTurns > 0;
	}

	public boolean recovering() {
		return freerunCooldown > 0 && !freerunning();
	}

	public float wandChargeMultiplier() {
		if (recovering() && target instanceof Hero) {
			return recoveryWandChargeMultiplier(
					((Hero) target).pointsInTalent(Talent.MOMENTUM_RESERVE));
		}
		return 1f;
	}

	public static int afterimageDuration(int heroLevel, int talentPoints) {
		if (talentPoints <= 0) return 0;
		float levelMultiplier = 0.05f + 0.05f * Math.min(3, talentPoints);
		return (int) Math.ceil(2f + heroLevel * levelMultiplier);
	}

	public static float recoveryWandChargeMultiplier(int talentPoints) {
		return 1f + 0.5f * Math.max(0, Math.min(3, talentPoints));
	}

	public static int warmupMomentum(int talentPoints) {
		return talentPoints >= 1 ? 4 : 0;
	}

	public static int warmupCloakCharge(int talentPoints) {
		return talentPoints >= 2 ? 2 : 0;
	}

	public static int warmupStaminaDuration(int talentPoints) {
		return talentPoints >= 3 ? 7 : 0;
	}

	private void onFreerunEnded() {
		if (!(target instanceof Hero)) return;
		Hero hero = (Hero) target;
		int duration = afterimageDuration(
				hero.lvl, hero.pointsInTalent(Talent.FREERUNNER_AFTERIMAGE));
		if (duration > 0) {
			Buff.affect(hero, GreaterHaste.class).set(duration);
		}
	}

	private void onFullyRecovered() {
		if (!(target instanceof Hero)) return;
		Hero hero = (Hero) target;
		int points = hero.pointsInTalent(Talent.WARMUP_PREPARATION);
		int momentum = warmupMomentum(points);
		if (momentum > 0) {
			momentumStacks = Math.min(10, momentumStacks + momentum);
			ActionIndicator.setAction(this);
			BuffIndicator.refreshHero();
		}
		int cloakCharge = warmupCloakCharge(points);
		if (cloakCharge > 0) {
			CloakOfShadows cloak = hero.belongings.getItem(CloakOfShadows.class);
			if (cloak != null) {
				cloak.directCharge(cloakCharge);
			}
		}
		int staminaDuration = warmupStaminaDuration(points);
		if (staminaDuration > 0) {
			Buff.prolong(hero, Stamina.class, staminaDuration);
		}
	}
	
	public float speedMultiplier(){
		if (freerunning()){
			return 2;
		} else if (target.invisible > 0 && Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH) == 3){
			return 2;
		} else {
			return 1;
		}
	}
	
	public int evasionBonus( int heroLvl, int excessArmorStr ){
		if (freerunTurns > 0) {
			return heroLvl/2 + excessArmorStr*Dungeon.hero.pointsInTalent(Talent.EVASIVE_ARMOR);
		} else {
			return 0;
		}
	}
	
	@Override
	public int icon() {
		if (momentumStacks > 0 || freerunCooldown > 0)  return BuffIndicator.MOMENTUM;
		else                                            return BuffIndicator.NONE;
	}
	
	@Override
	public void tintIcon(Image icon) {
		if (freerunCooldown == 0 || freerunTurns > 0){
			icon.hardlight(1,1,0);
		} else {
			icon.hardlight(0.5f,0.5f,1);
		}
	}

	@Override
	public float iconFadePercent() {
		if (freerunTurns > 0){
			return (20 - freerunTurns) / 20f;
		} else if (freerunCooldown > 0){
			return (freerunCooldown) / 30f;
		} else {
			return 0;
		}
	}

	@Override
	public String iconTextDisplay() {
		if (freerunTurns > 0){
			return Integer.toString(freerunTurns);
		} else if (freerunCooldown > 0){
			return Integer.toString(freerunCooldown);
		} else {
			return "";
		}
	}

	@Override
	public String name() {
		if (freerunTurns > 0){
			return Messages.get(this, "running");
		} else if (freerunCooldown > 0){
			return Messages.get(this, "resting");
		} else {
			return Messages.get(this, "momentum");
		}
	}
	
	@Override
	public String desc() {
		if (freerunTurns > 0){
			return Messages.get(this, "running_desc", freerunTurns);
		} else if (freerunCooldown > 0){
			return Messages.get(this, "resting_desc", freerunCooldown);
		} else {
			return Messages.get(this, "momentum_desc", momentumStacks);
		}
	}
	
	private static final String STACKS =        "stacks";
	private static final String FREERUN_TURNS = "freerun_turns";
	private static final String FREERUN_CD =    "freerun_CD";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, momentumStacks);
		bundle.put(FREERUN_TURNS, freerunTurns);
		bundle.put(FREERUN_CD, freerunCooldown);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		momentumStacks = bundle.getInt(STACKS);
		freerunTurns = bundle.getInt(FREERUN_TURNS);
		freerunCooldown = bundle.getInt(FREERUN_CD);
		movedLastTurn = false;
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.MOMENTUM;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text(Integer.toString((int)momentumStacks) );
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x444444;
	}

	@Override
	public void doAction() {
		freerunTurns = 2*momentumStacks;
		//cooldown is functionally 10+2*stacks when active effect ends
		freerunCooldown = 10 + 4*momentumStacks;
		Sample.INSTANCE.play(Assets.Sounds.MISS, 1f, 0.8f);
		target.sprite.emitter().burst(Speck.factory(Speck.JET), 5+ momentumStacks);
		SpellSprite.show(target, SpellSprite.HASTE, 1, 1, 0);
		momentumStacks = 0;
		BuffIndicator.refreshHero();
		ActionIndicator.clearAction(this);
	}

	@Override
	public boolean usable() {
		return momentumStacks > 0 && freerunTurns <= 0 && freerunCooldown <= 0;
	}

}
