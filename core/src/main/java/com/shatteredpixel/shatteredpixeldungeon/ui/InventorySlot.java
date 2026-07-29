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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;

public class InventorySlot extends ItemSlot {

	private static final int NORMAL		= 0x9953564D;
	private static final int EQUIPPED	= 0x9991938C;
	private static final int TREASURE_RARE	= 0x99426D3B;
	private static final int TREASURE_EPIC	= 0x999E762C;
	private static final int TREASURE_LEGENDARY = 0x99FFFFFF;

	private ColorBlock bg;
	private boolean legendaryTreasure;
	private boolean pressed;

	public InventorySlot( Item item ) {

		super( item );
	}

	@Override
	protected void createChildren() {
		bg = new ColorBlock( 1, 1, NORMAL );
		add( bg );

		super.createChildren();
	}

	@Override
	protected void layout() {
		bg.size(width, height);
		bg.x = x;
		bg.y = y;

		super.layout();
	}

	@Override
	public void alpha(float value) {
		super.alpha(value);
		bg.alpha(value);
	}

	@Override
	public void item( Item item ) {

		super.item( item );
		legendaryTreasure = false;
		pressed = false;

		bg.visible = !(item instanceof Gold || item instanceof Bag);

		if (item != null) {

			boolean equipped = item.isEquipped(Dungeon.hero) ||
					item == Dungeon.hero.belongings.weapon ||
					item == Dungeon.hero.belongings.armor ||
					item == Dungeon.hero.belongings.artifact ||
					item == Dungeon.hero.belongings.misc ||
					item == Dungeon.hero.belongings.ring ||
					item == Dungeon.hero.belongings.secondWep;

			int background = equipped ? EQUIPPED : treasureBackground(item);
			bg.texture( TextureCache.createSolid( background ) );
			bg.resetColor();
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.3f;
				bg.ga = -0.15f;
				bg.ba = -0.15f;
			} else if (!item.isIdentified()) {
				if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown){
					bg.ba = +0.3f;
					bg.ra = -0.1f;
				} else {
					bg.ra = +0.35f;
					bg.ba = +0.35f;
				}
			}

			if (item.name() == null) {
				enable( false );
			} else if (Dungeon.hero.belongings.lostInventory()
					&& !item.keptThroughLostInventory()){
				enable(false);
			}
		} else {
			bg.texture( TextureCache.createSolid( NORMAL ) );
			bg.resetColor();
		}
	}

	private int treasureBackground(Item item) {
		if (item instanceof Treasures) {
			switch (((Treasures) item).rarity()) {
				case LEGENDARY:
					legendaryTreasure = true;
					return TREASURE_LEGENDARY;
				case EPIC:
					return TREASURE_EPIC;
				case RARE:
					return TREASURE_RARE;
				case COMMON:
				default:
					break;
			}
		}
		return NORMAL;
	}

	@Override
	public void update() {
		super.update();
		if (legendaryTreasure) {
			float phase = Game.timeTotal * 1.15f;
			float rainbowR = 0.5f + 0.5f * (float) Math.sin(phase);
			float rainbowG = 0.5f + 0.5f * (float) Math.sin(phase + 2.094f);
			float rainbowB = 0.5f + 0.5f * (float) Math.sin(phase + 4.189f);
			float brightness = pressed ? 1.5f : 1f;

			// Keep gold dominant; the rainbow component is deliberately restrained.
			bg.hardlight(
					brightness * (0.82f + 0.18f * rainbowR),
					brightness * (0.62f + 0.18f * rainbowG),
					brightness * (0.19f + 0.18f * rainbowB));
		}
	}

	public Item item(){
		return item;
	}

	@Override
	protected void onPointerDown() {
		pressed = true;
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
	}

	protected void onPointerUp() {
		pressed = false;
		bg.brightness( 1.0f );
	}

}
