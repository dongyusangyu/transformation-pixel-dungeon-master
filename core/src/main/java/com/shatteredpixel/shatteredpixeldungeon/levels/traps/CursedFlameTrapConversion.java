package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;

/** Restricts cursed-flame wand conversion to its two recipe wands and its two traps. */
public final class CursedFlameTrapConversion {

    private CursedFlameTrapConversion() {}

    public static WandOfCursedFlame transformThrownWand(Wand input, int cell) {
        if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) return null;
        Trap trap = Dungeon.level.traps.get(cell);
        if (!isEligible(input, trap)) return null;

        WandOfCursedFlame output = new WandOfCursedFlame();
        output.level(0);
        output.cursed = true;
        return output;
    }

    public static boolean isEligible(Wand input, Trap trap) {
        if (input == null || trap == null || !trap.active) return false;
        boolean ingredient = input.getClass() == WandOfMagicMissile.class
                || input.getClass() == WandOfFireblast.class;
        boolean cursedFlameTrap = trap instanceof CursedFlameTrap || trap instanceof SoulScorchTrap;
        return ingredient && cursedFlameTrap;
    }
}
