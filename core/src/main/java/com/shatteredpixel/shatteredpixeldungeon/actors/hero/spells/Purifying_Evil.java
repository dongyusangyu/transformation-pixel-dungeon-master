package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Purifying_Evil extends TargetedClericSpell{
    public static final Purifying_Evil INSTANCE = new Purifying_Evil();

    @Override
    public int icon() {
        return HeroIcon.PURIFYING_EVIL;
    }

    @Override
    public int targetingFlags(){
        return -1; //auto-targeting behaviour is often wrong, so we don't use it
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.PURIFYING_EVIL);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 3;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        Char ch = Actor.findChar(target);
        if (ch == null || !Dungeon.level.heroFOV[target] || (ch instanceof  Hero)){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }

        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

        for (Buff b : ch.buffs()){
            if (b.type == Buff.buffType.POSITIVE
                    && !(b instanceof AllyBuff)
                    && !(b instanceof LostInventory)){
                b.detach();
            }
            if(hero.pointsInTalent(Talent.PURIFYING_EVIL)>1 && b instanceof ChampionEnemy){
                b.detach();
            }
        }
        if(hero.pointsInTalent(Talent.PURIFYING_EVIL)==3 && (ch.properties().contains(Char.Property.DEMONIC) || ch.properties().contains(Char.Property.UNDEAD))){
            int dmg=hero.HT/5;
            if(ch.properties().contains(Char.Property.BOSS)){
                dmg/=2;
            }
            ch.damage(dmg,new WandOfMagicMissile());
        }

        hero.busy();
        hero.sprite.operate(ch.pos);
        hero.spend( 1f );

        onSpellCast(tome, hero);
    }
    public String desc(){
        return Messages.get(this, "desc") +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

}
