package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTestSpecialization;

import java.util.ArrayList;

public class TestSpecialization extends TestItem {

	public static final String AC_SUBCLASS = "TEST_SUBCLASS";
	public static final String AC_ARMOR_ABILITY = "TEST_ARMOR_ABILITY";

	{
		image = ItemSpriteSheet.MASK;
		defaultAction = AC_SUBCLASS;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SUBCLASS);
		actions.add(AC_ARMOR_ABILITY);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (AC_SUBCLASS.equals(action)) {
			GameScene.show(new WndTestSpecialization.ClassSelection(hero));
		} else if (AC_ARMOR_ABILITY.equals(action)) {
			if (hero.belongings.armor() == null) {
				GLog.n(Messages.get(this, "no_armor"));
				return;
			}
			GameScene.show(new WndTestSpecialization.ArmorClassSelection(hero));
		}
	}
}
