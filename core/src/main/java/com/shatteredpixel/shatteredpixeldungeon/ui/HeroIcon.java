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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

//icons for hero subclasses and abilities atm, maybe add classes?
public class HeroIcon extends Image {

	private static TextureFilm film;
	private static final int SIZE = 16;

	//transparent icon
	public static final int NONE    = 127;

	//subclasses
	public static final int BERSERKER   = 0;
	public static final int GLADIATOR   = 1;
	public static final int BATTLEMAGE  = 2;
	public static final int WARLOCK     = 3;
	public static final int ASSASSIN    = 4;
	public static final int FREERUNNER  = 5;
	public static final int SNIPER      = 6;
	public static final int WARDEN      = 7;
	public static final int CHAMPION    = 8;
	public static final int MONK        = 9;
	public static final int PRIEST      = 10;
	public static final int PALADIN     = 11;
	public static final int WATERSLIME      = 13;
	public static final int DARKSLIME     = 14;
	public static final int FREEMAN     = 12;

	//abilities
	public static final int HEROIC_LEAP     = 16;
	public static final int SHOCKWAVE       = 17;
	public static final int ENDURE          = 18;
	public static final int ELEMENTAL_BLAST = 19;
	public static final int WILD_MAGIC      = 20;
	public static final int WARP_BEACON     = 21;
	public static final int SMOKE_BOMB      = 22;
	public static final int DEATH_MARK      = 23;
	public static final int SHADOW_CLONE    = 24;
	public static final int SPECTRAL_BLADES = 25;
	public static final int NATURES_POWER   = 26;
	public static final int SPIRIT_HAWK     = 27;
	public static final int CHALLENGE       = 28;
	public static final int ELEMENTAL_STRIKE= 29;
	public static final int FEINT           = 30;
	public static final int ASCENDED_FORM   = 31;
	public static final int TRINITY         = 32;
	public static final int POWER_OF_MANY   = 33;
	public static final int RATMOGRIFY      = 34;
	public static final int SPRING_SPELL         = 35;
	public static final int RAPID_WATERFALL   = 36;
	public static final int MAD_SLIME      = 37;

	//cleric spells
	public static final int GUIDING_LIGHT   = 40;
	public static final int HOLY_WEAPON     = 41;
	public static final int HOLY_WARD       = 42;
	public static final int HOLY_INTUITION  = 43;
	public static final int SHIELD_OF_LIGHT = 44;
	public static final int RECALL_GLYPH    = 45;
	public static final int SUNRAY          = 46;
	public static final int DIVINE_SENSE    = 47;
	public static final int BLESS           = 48;
	public static final int CLEANSE         = 49;
	public static final int RADIANCE        = 50;
	public static final int HOLY_LANCE      = 51;
	public static final int HALLOWED_GROUND = 52;
	public static final int MNEMONIC_PRAYER = 53;
	public static final int SMITE           = 54;
	public static final int LAY_ON_HANDS    = 55;
	public static final int AURA_OF_PROTECTION = 56;
	public static final int WALL_OF_LIGHT   = 57;
	public static final int DIVINE_INTERVENTION = 58;
	public static final int JUDGEMENT       = 59;
	public static final int FLASH           = 60;
	public static final int BODY_FORM       = 61;
	public static final int MIND_FORM       = 62;
	public static final int SPIRIT_FORM     = 63;
	public static final int BEAMING_RAY     = 64;
	public static final int LIFE_LINK       = 65;
	public static final int STASIS          = 66;
	public static final int TATTEKI          = 67;
	public static final int NINJAMASTER          = 68;

	public static final int SPIDERJAR          = 70;
	public static final int ONESWORD          = 71;

	//all cleric spells have a separate icon with no background for the action indicator
	public static final int SPELL_ACTION_OFFSET      = 32;

	//action indicator visuals
	public static final int BERSERK         = 104;
	public static final int COMBO           = 105;
	public static final int PREPARATION     = 106;
	public static final int MOMENTUM        = 107;
	public static final int SNIPERS_MARK    = 108;
	public static final int WEAPON_SWAP     = 109;
	public static final int MONK_ABILITIES  = 110;
	public static final int DARK_HOOK = 111;
	public static final int NINJA_NORMAL = 112;
	public static final int NINJA_WATER = 113;
	public static final int NINJA_GRASS = 114;

	public static final int ASCENSION_CURSE  = 128;

	public static final int DIVINE_PROTECTION =130;

	public static final int SHEPHERD_INTENTION=132;

	public static final int CONVERSION_HOLY=134;

	public static final int PURIFYING_EVIL=136;

	public static final int DIVINE_STORM=138;

	public static final int GENESIS=140;
	public static final int INDULGENCE=142;

	public static final int SILVER_LANGUAGE=144;

	public static final int RESURRECTION = 146;
	public static final int TRAITOROUS_SPELL = 148;

	public static final int SPRINT_SPELL = 150;
	public static final int FOCUS_LIGHT = 152;
	public static final int ZHUANYU_SPELL = 154;

	public static final int THORN_WHIP = 156;
	public static final int REVELATION = 158;

	public static final int HOLY_GRENADE = 160;

	public static final int BLADE_STAR = 162;

	public static final int SACRED_BLADE = 164;

	public static final int EQUIPMENT_BLESS = 166;

	public static final int HOTLIGHT = 168;






	public HeroIcon(HeroSubClass subCls){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(subCls.icon()));
	}

	public HeroIcon(ArmorAbility abil){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(abil.icon()));
	}

	public HeroIcon(ActionIndicator.Action action){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(action.actionIcon()));
	}
	public HeroIcon(ClericSpell spell){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(spell.icon()));
	}

}
