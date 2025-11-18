package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.freeman;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TormentedSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ChallengeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SalesContract extends ArmorAbility {
    {
        baseChargeUse = 90f;
    }
    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        if (target == null){
            return;
        }
        if (target == hero.pos){
            GLog.w(Messages.get(this, "self_target"));
            return;
        }
        Char c = Actor.findChar(target);
        if(c == null){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }
        if(Dungeon.level.distance(target,hero.pos)>1){
            GLog.w(Messages.get(this, "more_distance"));
            return;
        }
        if (c.alignment != Char.Alignment.ENEMY || c instanceof DemonSpawner

                || c.properties().contains(Char.Property.BOSS)
                || c.properties().contains(Char.Property.MINIBOSS)
                || !c.buffs(ChampionEnemy.class).isEmpty()) {
            GLog.w(Messages.get(this, "strong_target"));
            return;
        }
        AllyBuff.affectAndLoot((Mob)c, hero, SaleSelf.class);
        //Buff.affect(c, SaleSelf.class);
        if(hero.hasTalent(Talent.ELITE_RECRUIT) && Dungeon.gold>=250-50*hero.pointsInTalent(Talent.ELITE_RECRUIT)){
            ChampionEnemy.rollGiveChampion(c);
            Dungeon.gold -= 250-50*hero.pointsInTalent(Talent.ELITE_RECRUIT);
            hero.sprite.showStatusWithIcon( CharSprite.NEUTRAL, Integer.toString(-250+50*hero.pointsInTalent(Talent.ELITE_RECRUIT)), FloatingText.GOLD );
        }
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        hero.sprite.operate( hero.pos );
        hero.busy();
        hero.spendAndNext(Actor.TICK);
    }

    @Override
    public int icon() {
        return HeroIcon.SALESCONTRACT;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.ELITE_RECRUIT, Talent.PRIM_ACCU, Talent.SOUL_CONTRACT, Talent.HEROIC_ENERGY};
    }


    public static class SaleSelf extends ScrollOfSirensSong.Enthralled {

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public void fx(boolean on) {
            if (on) target.sprite.add(CharSprite.State.COIN);
            else    target.sprite.remove(CharSprite.State.COIN);
        }

        @Override
        public int icon() {
            return BuffIndicator.SALESELF;
        }
    }

    public static class WorkerWraith extends Wraith {

    }
}
