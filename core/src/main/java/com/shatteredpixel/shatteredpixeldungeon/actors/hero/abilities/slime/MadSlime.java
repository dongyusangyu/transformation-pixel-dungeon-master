package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.slime;

import static com.shatteredpixel.shatteredpixeldungeon.actors.Actor.TICK;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class MadSlime extends ArmorAbility {


    {
        baseChargeUse = 90f;
    }
    @Override
    public float chargeUse( Hero hero ) {
        float chargeUse = super.chargeUse(hero);
        if(hero.hasTalent(Talent.DELICIOUS_DIGESTION) && ((ClassArmor)hero.belongings.armor).charge == 100){
            chargeUse *= Math.pow(0.84, hero.pointsInTalent(Talent.DELICIOUS_DIGESTION));
        }
        return chargeUse;
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
        if (c.alignment != Char.Alignment.ENEMY
                || (c.properties().contains(Char.Property.BOSS_MINION)
                || c.properties().contains(Char.Property.BOSS)
                || !c.buffs(ChampionEnemy.class).isEmpty())) {
            GLog.w(Messages.get(this, "strong_target"));
            return;
        }else{
            c.damage(c.HT,hero);
        }


        Buff.affect(hero, Hunger.class).satisfy(250* (1 + 0.2f*hero.pointsInTalent(Talent.NO_PICK)));
        if(hero.hasTalent(Talent.FOOD_BONUS)){
            Talent.onFoodEaten(hero,250* (1 + 0.2f*hero.pointsInTalent(Talent.NO_PICK)),null);
            if(hero.pointsInTalent(Talent.FOOD_BONUS)>1){
                Buff.affect(hero, ScrollOfChallenge.ChallengeArena.class).setup(hero.pos);
                GLog.w(Messages.get(this, "eat"));
            }
            if(hero.pointsInTalent(Talent.FOOD_BONUS)>2){
                Buff.affect(hero, Healing.class).setHeal((int)(hero.HT-hero.HP)/4,0.25f,0);
                GLog.w(Messages.get(this, "eat1"));
            }
            if(hero.pointsInTalent(Talent.FOOD_BONUS)==4){
                Buff.affect(hero, Barrier.class).setShield(hero.HT/4);
                GLog.w(Messages.get(this, "eat2"));
            }
        }
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        hero.sprite.operate( hero.pos );
        hero.busy();
        SpellSprite.show( hero, SpellSprite.FOOD );
        Sample.INSTANCE.play( Assets.Sounds.EAT );
        hero.spend(1f);
    }

    @Override
    public int icon() {
        return HeroIcon.MAD_SLIME;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.NO_PICK, Talent.FOOD_BONUS, Talent.DELICIOUS_DIGESTION, Talent.HEROIC_ENERGY};
    }
}
