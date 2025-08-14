package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Focus_Light extends TargetedClericSpell {
    public static final Focus_Light INSTANCE = new Focus_Light();

    @Override
    public int icon() {
        return HeroIcon.FOCUS_LIGHT;
    }



    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.FOCUS_LIGHT);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 2;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        Ballistica aim = new Ballistica(hero.pos, target, targetingFlags());

        if (Actor.findChar( aim.collisionPos ) == hero){
            GLog.i( Messages.get(Wand.class, "self_target") );
            return;
        }

        if (Actor.findChar(aim.collisionPos) != null) {
            QuickSlotButton.target(Actor.findChar(aim.collisionPos));
        } else {
            QuickSlotButton.target(Actor.findChar(target));
        }

        hero.busy();
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
        hero.sprite.zap(target);
        MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.FIRE_CONE, hero.sprite, aim.collisionPos, new Callback() {
            @Override
            public void call() {

                Char ch = Actor.findChar( aim.collisionPos );
                if (ch != null) {
                    ch.damage(Random.NormalIntRange(2+2*hero.pointsInTalent(Talent.FOCUS_LIGHT), 6+2*hero.pointsInTalent(Talent.FOCUS_LIGHT)), new WandOfMagicMissile());
                    Sample.INSTANCE.play( Assets.Sounds.BURNING );

                } else {
                    Dungeon.level.pressCell(aim.collisionPos);
                }
                for( int cell : aim.subPath(0, aim.dist) ){
                    if(cell == aim.sourcePos){
                    }else{
                        GameScene.add( Blob.seed( cell, 1, Fire.class ) );
                    }
                }
                hero.spend( 1f );
                hero.next();
                onSpellCast(tome, hero);

            }
        });
    }
    @Override
    public String desc(){
        String desc = Messages.get(this, "desc",2+2*hero.pointsInTalent(Talent.FOCUS_LIGHT),6+2*hero.pointsInTalent(Talent.FOCUS_LIGHT));
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }
}
