package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Resurrection extends TargetedClericSpell {
    public static final Resurrection INSTANCE = new Resurrection();

    @Override
    public int icon() {
        return HeroIcon.RESURRECTION;
    }

    @Override
    public int targetingFlags(){
        return -1; //auto-targeting behaviour is often wrong, so we don't use it
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.RESURRECTION);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 8;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        Char ch = Actor.findChar(target);
        if (ch == null || !Dungeon.level.heroFOV[target]){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }
        if (ch.alignment != Char.Alignment.ENEMY || ch instanceof DemonSpawner

                || ch.properties().contains(Char.Property.BOSS)
                || ch.properties().contains(Char.Property.MINIBOSS)
                || !ch.buffs(ChampionEnemy.class).isEmpty()) {
            GLog.w(Messages.get(this, "no_target"));
            return;
        }

        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

        if(hero.pointsInTalent(Talent.RESURRECTION)==1){
            Buff.affect(ch,REsurrection.class,5);
        }else{
            Buff.affect(ch,REsurrection.class,10);
        }


        hero.busy();
        hero.sprite.operate(ch.pos);
        hero.spend( 1f );

        onSpellCast(tome, hero);
    }
    @Override
    public String desc(){
        String desc = Messages.get(this, "desc");
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }


    public static class REsurrection extends FlavourBuff {
        public static final float DURATION	= 10f;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.RESURRECTION;
        }

        @Override
        public void fx(boolean on) {
            if (on) target.sprite.aura( 0xFFFF00,5 );
            else target.sprite.clearAura();
        }
        @Override
        public String desc() {
           return Messages.get(this, "desc", dispTurns());
        }
        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
}
