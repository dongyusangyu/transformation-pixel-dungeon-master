package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.dm400;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class Discharge_Gas extends ArmorAbility {
    {
        baseChargeUse = 35f;
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
                MagicMissile.boltFromChar(hero.sprite.parent,
                        MagicMissile.SPECK + Speck.TOXIC,
                        hero.sprite,
                        mob.pos,
                        new Callback() {
                            @Override
                            public void call() {
                                float vol=50;
                                if(hero.hasTalent(Talent.OVER_TIME)){
                                    vol*=1+1.15f*hero.pointsInTalent(Talent.OVER_TIME);
                                }
                                Ballistica trajectory = new Ballistica(hero.pos, mob.pos, Ballistica.STOP_TARGET);
                                for (int i : trajectory.subPath(0, trajectory.dist)){
                                    GameScene.add(Blob.seed(i, 5, DM400Gas.class));
                                }
                                GameScene.add(Blob.seed(trajectory.collisionPos, (int)vol, DM400Gas.class));
                            }
                        } );
            }
        }
        Sample.INSTANCE.play( Assets.Sounds.GAS );
        for (Buff b : hero.buffs()){
            if (b.type == Buff.buffType.NEGATIVE
                    && !(b instanceof AllyBuff)
                    && !(b instanceof LostInventory)){
                b.detach();
            }
        }
        if(hero.hasTalent(Talent.DISCHARGE_HAPPY)){
            Buff.affect(hero, Haste.class,2*hero.pointsInTalent(Talent.DISCHARGE_HAPPY));
        }
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(1f);

    }

    @Override
    public int icon() {
        return HeroIcon.DM400GAS;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.OVER_TIME,Talent.DEADLY_GAS,Talent.DISCHARGE_HAPPY, Talent.HEROIC_ENERGY};
    }

    public static class DM400Gas extends ToxicGas {

        @Override
        protected void evolve() {
            super.evolve();

            int damage = 1 + Dungeon.scalingDepth()/5;
            if(hero!=null && hero.hasTalent(Talent.DEADLY_GAS)){
                damage+=hero.pointsInTalent(Talent.DEADLY_GAS);
            }
            Char ch;
            int cell;

            for (int i = area.left; i < area.right; i++){
                for (int j = area.top; j < area.bottom; j++){
                    cell = i + j*Dungeon.level.width();
                    if (cur[cell] > 0 && (ch = Actor.findChar( cell )) != null) {
                        if (!ch.isImmune(this.getClass()) && !((ch instanceof Hero)|| (ch instanceof InstructionTool.Drone))) {
                            ch.damage(damage, this);
                        }
                    }
                }
            }
        }
    }
}
