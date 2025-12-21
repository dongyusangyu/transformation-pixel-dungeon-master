package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane.useTargeting;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import sun.security.provider.JavaKeyStore;

public class DarkHook extends Buff implements ActionIndicator.Action{

    {
        revivePersists = true;
    }
    public boolean usesTargeting;

    public float energy;
    public int cooldown; //currently unused, abilities had cooldowns prior to v2.5


    @Override
    public boolean act() {
        if(target.buff(Talent.DarkHookCooldown.class)!=null){
            ActionIndicator.clearAction(this);
        }else{
            ActionIndicator.setAction(this);
            BuffIndicator.refreshHero();
        }

        spend(TICK);
        return true;
    }

    @Override
    public void detach() {
        super.detach();
        ActionIndicator.clearAction(this);
    }

    @Override
    public int icon() {
        return BuffIndicator.DARK_HOOK;
    }
    /*
    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.33f, 0.33f, 1f);
    }

     */


    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public int actionIcon() {
        return HeroIcon.DARK_HOOK;
    }

    @Override
    public int indicatorColor() {
        return 0x000000;
    }
    @Override
    public void doAction() {
        GameScene.selectCell(caster);
    }



    public CellSelector.Listener caster = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer target) {
            if(target != null){
                Char ch = Actor.findChar(target);

                if (ch != null && (Dungeon.level.visited[target] || Dungeon.level.mapped[target])){
                    Ballistica chain = new Ballistica(Dungeon.hero.pos, target, Ballistica.STOP_TARGET);
                    final Hero hero = Dungeon.hero;
                    final Char enemy = ch;

                    if (enemy.properties().contains(Char.Property.IMMOVABLE)) {
                        GLog.w( Messages.get(DarkHook.class, "cant_pull") );
                        return;
                    }

                    if(Dungeon.level.distance(target,hero.pos)>12){
                        GLog.w(Messages.get(DarkHook.class, "more_distance"));
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
                        GLog.i(Messages.get(DarkHook.class, "does_nothing"));
                        return;
                    }

                    final int pulledPos = bestPos;


                    hero.busy();
                    Sample.INSTANCE.play( Assets.Sounds.CHAINS );
                    hero.sprite.parent.add(new Chains(hero.sprite.center(),
                            enemy.sprite.center(),
                            Effects.Type.DARK_HOOK,
                            new Callback() {
                                public void call() {
                                    Actor.add(new Pushing(enemy, enemy.pos, pulledPos, new Callback() {
                                        public void call() {
                                            enemy.pos = pulledPos;

                                            Invisibility.dispel(hero);

                                            Dungeon.level.occupyCell(enemy);
                                            Dungeon.observe();
                                            GameScene.updateFog();
                                        }
                                    }));
                                    hero.next();
                                }
                            }));
                    Buff.affect(Dungeon.hero, Talent.DarkHookCooldown.class, 65f);
                    ActionIndicator.clearAction(hero.buff(DarkHook.class));
                    if (Dungeon.hero.pointsInTalent(Talent.DARK_LIQUID) >= 1){
                        Buff.affect(ch, Roots.class, 5f);
                    }else {
                        Buff.affect(ch, Roots.class, 3f);
                    }

                    if (Dungeon.hero.pointsInTalent(Talent.DARK_LIQUID) >= 2){
                        Buff.affect(ch, Ooze.class).set(Ooze.DURATION);
                    }
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(EtherealChains.class, "prompt");
        }
    };

}
