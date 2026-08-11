package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;

/**
 * Incremental implementation shell registered by the tower boss generator.
 * Combat behavior is added in the subsequent focused TDD stages.
 */
public class PestilenceKnight extends TowerBoss {

    public PestilenceKnight() {
        HP = HT = 1500;
        defenseSkill = 30;
        EXP = 0;
        maxLvl = 30;
    }

    @Override
    public String towerBossId() {
        return TowerBossGenerator.PESTILENCE_KNIGHT_ID;
    }

    @Override
    public int attackSkill(com.shatteredpixel.shatteredpixeldungeon.actors.Char target) {
        return 50;
    }

    @Override
    public int damageRoll() {
        return 0;
    }

    @Override
    public int drRoll() {
        return 0;
    }
}
