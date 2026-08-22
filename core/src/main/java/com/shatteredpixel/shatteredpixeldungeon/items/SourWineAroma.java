package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import java.util.ArrayList;

/** Zero-turn area consumable that applies the existing random-movement Vertigo. */
public class SourWineAroma extends Item {
	public float useTimeForTest() { return 0f; }
	private static final String AC_USE = "USE";
	{ image = EXItemSpriteSheet.SOUR_WINE_AROMA; stackable = true; defaultAction = AC_USE; bones = false; }
	@Override public boolean isUpgradable() { return false; }
	@Override public boolean isIdentified() { return true; }
	@Override public ArrayList<String> actions(Hero hero) { ArrayList<String> a = super.actions(hero); a.add(AC_USE); return a; }
	@Override public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_USE.equals(action)) {
			detach(hero.belongings.backpack);
			for (Char ch : Actor.chars()) {
				if (ch != hero && Actor.isHostile(hero, ch) && ch.isAlive()
						&& Dungeon.level.distance(hero.pos, ch.pos) <= 4) {
					Buff.affect(ch, Vertigo.class, 5f);
				}
			}
			Catalog.countUse(getClass());
		}
	}
}
