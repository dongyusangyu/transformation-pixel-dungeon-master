package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

/** Scene/projectile representation of the single green elf-wine cup. */
public class ElfWine extends Item {
	{ image = EXItemSpriteSheet.ELF_WINE; stackable = true; bones = false; }
	@Override public boolean isUpgradable() { return false; }
	@Override public boolean isIdentified() { return true; }
}
