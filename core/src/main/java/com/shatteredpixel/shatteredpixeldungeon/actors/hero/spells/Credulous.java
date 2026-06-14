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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class Credulous extends ClericSpell {

	public static final Credulous INSTANCE = new Credulous();

	@Override
	public int icon() {
		return HeroIcon.CREDULOUS;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.CREDULOUS);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		Buff.affect(hero, MagicImmune.class, 10f);
		Sample.INSTANCE.play(Assets.Sounds.READ);
		hero.sprite.operate(hero.pos);
		if (hero.pointsInTalent(Talent.CREDULOUS) < 2) {
			hero.spend(1f);
			hero.busy();
		}
		onSpellCast(tome, hero);
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.pointsInTalent(Talent.CREDULOUS) >= 2) {
			desc += "\n\n" + Messages.get(this, "desc_instant");
		}
		return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero));
	}
}
