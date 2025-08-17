package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
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

public class Thorn_Whip extends TargetedClericSpell {
    public static final Thorn_Whip INSTANCE = new Thorn_Whip();

    @Override
    public int icon() {
        return HeroIcon.THORN_WHIP;
    }


    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.THORN_WHIP);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 1;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }
        if(Dungeon.level.distance(target,hero.pos)>12){
            GLog.w(Messages.get(this, "more_distance"));
            return;
        }
        final Ballistica chain = new Ballistica(hero.pos, target, targetingFlags());
        Char enemy = Actor.findChar(chain.collisionPos);
        if(enemy == null || enemy instanceof Hero){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }
        if (enemy.properties().contains(Char.Property.IMMOVABLE)) {
            GLog.w( Messages.get(EtherealChains.class, "cant_pull") );
            return;
        }
        int bestPos = -1;
        for (int i : chain.subPath(1, chain.dist)){
            //prefer to the earliest point on the path
            if (!Dungeon.level.solid[i]
                    && Actor.findChar(i) == null
                    && (!Char.hasProp(enemy, Char.Property.LARGE) || Dungeon.level.openSpace[i])){
                bestPos = i;
                break;
            }
        }

        if (bestPos == -1) {
            GLog.i(Messages.get(this, "no_target"));
            return;
        }
        final int pulledPos = bestPos;
        Sample.INSTANCE.play( Assets.Sounds.GRASS );
        hero.sprite.parent.add(new Chains(hero.sprite.center(),
                enemy.sprite.center(),
                Effects.Type.WLIP,
                new Callback() {
                    public void call() {
                        Actor.add(new Pushing(enemy, enemy.pos, pulledPos, new Callback() {
                            public void call() {
                                enemy.pos = pulledPos;
                                Buff.affect(enemy, Bleeding.class).set(1+2*hero.pointsInTalent(Talent.THORN_WHIP));
                                Dungeon.level.occupyCell(enemy);
                                Dungeon.observe();
                                GameScene.updateFog();

                            }
                        }));
                        hero.next();
                    }
                }));
        hero.spend( 1f );
        onSpellCast(tome, hero);
    }
    @Override
    public String desc(){
        String desc = Messages.get(this, "desc",1+2*hero.pointsInTalent(Talent.THORN_WHIP));
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }
}
