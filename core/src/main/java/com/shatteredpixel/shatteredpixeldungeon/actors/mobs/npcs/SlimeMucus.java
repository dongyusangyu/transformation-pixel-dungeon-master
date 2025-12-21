package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeMucusSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class SlimeMucus extends NPC{
    {

        HP = HT = 10;
        spriteClass = SlimeMucusSprite.class;

        alignment = Alignment.ALLY;

        properties.add(Property.IMMOVABLE);

        viewDistance = 2;
        immunities.add( Sleep.class );
        immunities.add( Terror.class );
        immunities.add( Dread.class );
        immunities.add( Vertigo.class );
        immunities.add( AllyBuff.class );
    }
    @Override
    public int drRoll() {
        int dr = super.drRoll();
        if (hero != null){
            return dr + hero.drRoll()/2;
        } else {
            return dr;
        }
    }
    @Override
    protected boolean act() {
        damage(1,this);

        if(HP<1){
            die(null);
            sprite.die();
        }
        return super.act();
    }

    @Override
    public int defenseSkill(Char enemy) {
        return 0;
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return false;
    }
    /*
    @Override
    public CharSprite sprite() {
        SlimeMucusSprite sprite = (SlimeMucusSprite) super.sprite();
        sprite.linkVisuals(this);
        return sprite;
    }
    @Override
    public void updateSpriteState() {
        super.updateSpriteState();
        sprite.place(pos);
    }

     */

    @Override
    public boolean canInteract(Char ch) {
        return true;
    }

    @Override
    public boolean interact( Char ch ) {
        if (ch != hero){
            return super.interact(ch);
        }
        if (Dungeon.level.distance(pos, hero.pos) > 1){
            return false;
        }
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                GameScene.show(new WndOptions( sprite(),
                        Messages.get(SlimeMucus.class, "absorb_title"),
                        Messages.get(SlimeMucus.class, "absorb_body"),
                        Messages.get(SlimeMucus.class, "absorb_confirm"),
                        Messages.get(SlimeMucus.class, "absorb_cancel") ){
                    @Override
                    protected void onSelect(int index) {
                        int healHP = 0;
                        if (index == 0){
                            die(null);
                            sprite.die();
                            healHP = (int)Math.min(hero.HT-hero.HP,hero.HT * Math.min(0.1 + 0.025 * hero.pointsInTalent(Talent.WATER_REVIVAL),0.15));
                            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healHP), FloatingText.HEALING);
                            hero.HP+=healHP;
                        }
                    }
                });
            }
        });
        return true;
    }


}
