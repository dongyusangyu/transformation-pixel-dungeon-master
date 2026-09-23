package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.watabou.noosa.Visual;

/** A recurring special action exposed only by the effective main-hand weapon. */
public interface WeaponSpecialAction extends ActionIndicator1.Action {

	String specialActionId();

	default boolean specialActionAvailable(Hero hero) {
		return true;
	}

	default MeleeWeapon specialActionWeapon() {
		return (MeleeWeapon) this;
	}

	@Override
	default boolean usable() {
		Hero hero = Dungeon.hero;
		return hero != null && hero.belongings != null
				&& hero.belongings.weapon() == this
				&& hero.STR() >= specialActionWeapon().STRReq()
				&& specialActionAvailable(hero);
	}

	@Override
	default String actionName() {
		Hero hero = Dungeon.hero;
		return specialActionWeapon().actionName(specialActionId(), hero);
	}

	@Override
	default Visual primaryVisual() {
		return new ItemSprite(specialActionWeapon());
	}

	@Override
	default int indicatorColor() {
		return 0x5500BB;
	}

	@Override
	default void doAction() {
		Hero hero = Dungeon.hero;
		if (usable()) {
			specialActionWeapon().execute(hero, specialActionId());
		}
	}
}
