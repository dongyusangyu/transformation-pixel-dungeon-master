package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
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
				&& specialActionAvailable(hero);
	}

	default boolean hasEnoughStrength(Hero hero) {
		return hero != null && hero.STR() >= specialActionWeapon().STRReq();
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
			if (!hasEnoughStrength(hero)) {
				GLog.w(Messages.get(WeaponSpecialAction.class, "insufficient_strength"));
				return;
			}
			specialActionWeapon().execute(hero, specialActionId());
		}
	}
}
