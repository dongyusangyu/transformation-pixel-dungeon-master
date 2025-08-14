package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.watabou.utils.Random;

public class TestStatue extends Mob {
    {
        spriteClass = StatueSprite.class;

        HP = HT = 2100000000;
        defenseSkill = 0;

        EXP = 4;

        state = PASSIVE;

        properties.add(Property.IMMOVABLE);
        properties.add(Property.BOSS);
        properties.add(Property.STATIC);
    }
    @Override
    protected boolean act() {
        alerted = false;
        HP=HT;
        return super.act();

    }




    @Override
    public void beckon(int cell) {
        //do nothing
    }

    @Override
    protected boolean getCloser(int target) {
        return false;
    }

    public static void spawn(Level level) {
        if (Dungeon.depth == 0 ) {

            TestStatue testStatue = new TestStatue();
            //level.randomRespawnCell(testStatue);
            testStatue.pos=level.width()+1;
            level.mobs.add( testStatue );

        }
    }





    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public int damageRoll() {
        return 0;
    }

    @Override
    public int attackSkill( Char target ) {
        return 0;
    }

    @Override
    public int drRoll() {
        return super.drRoll();
    }


}
