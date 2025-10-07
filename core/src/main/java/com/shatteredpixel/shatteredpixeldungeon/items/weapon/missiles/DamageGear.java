package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class DamageGear  extends MissileWeapon{
    {
        image = ItemSpriteSheet.GEAR;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        bones = false;

        tier = 1;
        baseUses = 5;
    }


    @Override
    public int max(int lvl) {
        return  4 + 2*lvl;
    }

    @Override
    public int min(int lvl) {
        return  2 + lvl;
    }
    public static final String AC_FIX = "fix";
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add(AC_FIX);
        return actions;
    }
    @Override
    public void execute( Hero hero, String action ) {

        super.execute(hero, action);

        if (action.equals(AC_FIX)) {
            int recover=Math.min((int) Math.ceil(durability / durabilityPerUse()),hero.HT-hero.HP);
            detach(hero.belongings.backpack);
            hero.HP+=recover;
            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(recover), FloatingText.HEALING);
            durability=MAX_DURABILITY;
        }
    }
}
