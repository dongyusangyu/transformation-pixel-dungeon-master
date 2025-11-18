package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class HotLight extends ClericSpell {

    public static HotLight INSTANCE = new HotLight();

    @Override
    public int icon() {
        return HeroIcon.HOTLIGHT;
    }



    @Override
    public float chargeUse(Hero hero) {
        return 4-hero.pointsInTalent(Talent.HOTLIGHT);
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.HOTLIGHT);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        GameScene.flash( 0x80FFFFFF );
        Sample.INSTANCE.play(Assets.Sounds.BLAST);
        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
                Buff.affect(mob, Blindness.class, 3f);
                if (mob.isAlive() && hero.subClass== HeroSubClass.PRIEST){
                    Buff.affect(mob, GuidingLight.Illuminated.class);
                }
                if (mob.properties().contains(Char.Property.UNDEAD) || mob.properties().contains(Char.Property.DEMONIC)){
                    Buff.affect(mob, Burning.class).extend(3);
                }

            }
        }
        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);

        onSpellCast(tome, hero);

    }
    public static class Blessbuff extends FlavourBuff {

        public static float DURATION = 50f;

        private Emitter particles;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.EQUIPMENT_BLESS;
        }


        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
}
