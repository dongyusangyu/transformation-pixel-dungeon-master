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

package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroRandomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SprayGun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RitualDagger;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseSubclass;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.ArrayList;

public class TengusMask extends Item {
	
	private static final String AC_WEAR	= "WEAR";
	
	{
		stackable = false;
		image = ItemSpriteSheet.MASK;

		defaultAction = AC_WEAR;

		unique = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_WEAR );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_WEAR )) {
			
			curUser = hero;
			if(curUser.heroClass== HeroClass.FREEMAN && !HeroRandomizer.active(curUser)){

				detach( curUser.belongings.backpack );
				Dungeon.level.drop(new ScrollOfMetamorphosis(),curUser.pos).sprite.drop();
				curUser.spend( Actor.TICK );
				curUser.busy();
				Talent.initSubclassTalents(curUser);
				curUser.sprite.operate( curUser.pos );
				Sample.INSTANCE.play( Assets.Sounds.MASTERY );
				Emitter e = curUser.sprite.centerEmitter();
				e.pos(e.x-2, e.y-6, 4, 4);
				e.start(Speck.factory(Speck.MASK), 0.05f, 20);
				GLog.p( Messages.get(this, "used"));
				hero.subClass=HeroSubClass.FREEMAN;
			}else GameScene.show( new WndChooseSubclass( this, hero ) );
			
		}
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		Badges.validateMastery();

		return super.doPickUp( hero, pos );
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public void choose( HeroSubClass way ) {
		
		detach( curUser.belongings.backpack );
		Catalog.countUse( getClass() );
		
		curUser.spend( Actor.TICK );
		curUser.busy();
		
		curUser.subClass = way;
		Talent.initSubclassTalents(curUser);
		grantRandomModeKit(way);

		if (way.is(HeroSubClass.ASSASSIN) && curUser.invisible > 0){
			Buff.affect(curUser, Preparation.class);
		}
		//史莱姆
		if (way.is(HeroSubClass.DARKSLIME)){
			Buff.affect(curUser, DarkHook.class);

		}
		if (way.is(HeroSubClass.TATTEKI_NINJA)){
			Tatteki t =new Tatteki();
			t.identify();
            if (!t.collect(curUser.belongings.backpack)){
                Dungeon.level.drop(t, curUser.pos).sprite.drop();
            }else{

            }
		}
		if (way.is(HeroSubClass.NINJA_MASTER)){
			Buff.affect(curUser, Ninja_Energy.class);
			//Ninja_Energy ninjaenergy=curUser.buff(Ninja_Energy.class);
			//ninjaenergy.energy=5;
		}
		if (way.is(HeroSubClass.CHAMPION) && HeroRandomizer.active(curUser)){
			Buff.affect(curUser, MeleeWeapon.Charger.class);
		}
		if (way.is(HeroSubClass.PIOUS) && curUser.belongings.getItem(RitualDagger.class) == null){
			RitualDagger dagger = new RitualDagger();
			dagger.identify();
			if (!dagger.collect(curUser.belongings.backpack)){
				Dungeon.level.drop(dagger, curUser.pos).sprite.drop();
			} else {
				dagger.resetRitual(curUser);
			}
		}
        if (way.is(HeroSubClass.COMBATMASTER)){
            Buff.affect(curUser, FightStance.class);

        }
		hero.updateHT(true);

		curUser.sprite.operate( curUser.pos );
		Sample.INSTANCE.play( Assets.Sounds.MASTERY );
		
		Emitter e = curUser.sprite.centerEmitter();
		e.pos(e.x-2, e.y-6, 4, 4);
		e.start(Speck.factory(Speck.MASK), 0.05f, 20);
		GLog.p( Messages.get(this, "used"));
		
	}

	private void grantRandomModeKit(HeroSubClass way) {
		if (!HeroRandomizer.active(curUser)) {
			return;
		}

		if ((way.is(HeroSubClass.BERSERKER) || way.is(HeroSubClass.GLADIATOR))
				&& !hasBrokenSeal()) {
			collectOrDrop(new BrokenSeal());
		}
		if ((way.is(HeroSubClass.BATTLEMAGE))
				&& curUser.belongings.getItem(MagesStaff.class) == null) {
			collectOrDrop(new MagesStaff((Wand) Generator.random(Generator.Category.WAND)));
		}
		if ((way.is(HeroSubClass.ASSASSIN))
				&& curUser.belongings.getItem(CloakOfShadows.class) == null) {
			collectOrDrop(new CloakOfShadows());
		}
		if ((way.is(HeroSubClass.SNIPER))
				&& curUser.belongings.getItem(SpiritBow.class) == null) {
			collectOrDrop(new SpiritBow());
		}
		if ((way.is(HeroSubClass.PRIEST) || way.is(HeroSubClass.PALADIN))
				&& curUser.belongings.getItem(HolyTome.class) == null) {
			collectOrDrop(new HolyTome());
		}
		if ((way.is(HeroSubClass.AT400) || way.is(HeroSubClass.AU400))
				&& curUser.belongings.getItem(InstructionTool.class) == null) {
			collectOrDrop(new InstructionTool());
		}
        /*
		if ((way.is(HeroSubClass.RUNEMAGE) || way.is(HeroSubClass.COMBATMASTER))
				&& curUser.belongings.getItem(RingOfKing.class) == null) {
			collectOrDrop(new RingOfKing());
		}

         */
		if (way.is(HeroSubClass.ALCHEMIST) && curUser.belongings.getItem(SprayGun.class) == null) {
			collectOrDrop(new SprayGun());
		}
        if (way.is(HeroSubClass.PIOUS) && curUser.buff(Reason.class) == null) {
            Buff.affect(curUser,Reason.class);
        }
	}

	private boolean hasBrokenSeal() {
		Armor armor = curUser.belongings.armor();
		return curUser.belongings.getItem(BrokenSeal.class) != null
				|| (armor != null && armor.checkSeal() != null);
	}

	private void collectOrDrop(Item item) {
		item.identify();
		if (!item.collect(curUser.belongings.backpack)){
			Dungeon.level.drop(item, curUser.pos).sprite.drop();
		}
	}
}
