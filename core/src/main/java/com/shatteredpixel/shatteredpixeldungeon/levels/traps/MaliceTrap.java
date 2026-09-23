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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class MaliceTrap extends Trap {

	{
		color = VIOLET;
		shape = DOTS;
	}

	@Override
	public void activate() {
		if (Dungeon.level != null
				&& Dungeon.level.heroFOV != null
				&& pos >= 0
				&& pos < Dungeon.level.heroFOV.length
				&& Dungeon.level.heroFOV[pos]) {
			CellEmitter.get(pos).burst(ShadowParticle.CURSE, 6);
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}

		int itemCursedCount = 0;
		Heap heap = Dungeon.level.heaps.get(pos);
		if (heap != null) {
			for (Item item : heap.items) {
				if (CursingTrap.curse(item)) itemCursedCount++;
			}
		}

		Hero hero = Dungeon.hero;
		int equippedCursedCount = 0;
		if (hero != null && hero.belongings != null && hero.pos == pos) {
			equippedCursedCount = curseEquippedItems(hero);
			if (hero.sprite != null) {
				EquipableItem.equipCursed(hero);
			}
		}

		if (equippedCursedCount > 0) {
			GLog.n(Messages.get(this, "curse"));
		}
		if (itemCursedCount > 0) {
			GLog.n(Messages.get(this, "item_curse"));
		}
		if (equippedCursedCount == 0 && itemCursedCount == 0) {
			GLog.n(Messages.get(this, "already_cursed"));
		}
	}

	static int curseEquippedItems(Hero hero) {
		if (hero == null || hero.belongings == null) {
			return 0;
		}

		int cursedCount = 0;
		for (Item item : hero.belongings) {
			if (item instanceof EquipableItem && item.isEquipped(hero)) {
				boolean changed = CursingTrap.curse(item);
				if (!item.cursed || !item.cursedKnown) changed = true;
				item.cursed = item.cursedKnown = true;
				if (changed) cursedCount++;
			}
		}
		return cursedCount;
	}
}
