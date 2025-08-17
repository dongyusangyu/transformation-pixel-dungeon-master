package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Sacred_Blade extends TargetedClericSpell {

    public static Sacred_Blade INSTANCE = new Sacred_Blade();

    @Override
    public int icon() {
        return HeroIcon.SACRED_BLADE;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 1f;
    }

    @Override
    public boolean canCast(Hero hero) {
        return hero.hasTalent(Talent.SACRED_BLADE);
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null) {
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

        Char enemy = Actor.findChar(aim.collisionPos);
        if (enemy == null || enemy == hero){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }

        hero.busy();
        Sample.INSTANCE.play( Assets.Sounds.HIT );
        hero.sprite.zap(target);

        if (enemy != null) {
            ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
                    reset(hero.sprite,
                            enemy.sprite,
                            new HolBladeVFX(),
                            new Callback() {
                                @Override
                                public void call() {
                                    int max = hero.damageRoll();

                                    if(hero.pointsInTalent(Talent.SACRED_BLADE)>1){
                                        if (Char.hasProp(enemy, Char.Property.UNDEAD) || Char.hasProp(enemy, Char.Property.DEMONIC)){
                                            int i=0;
                                            while(i<100){
                                                i++;
                                                int a = hero.damageRoll();
                                                if(a>max){
                                                    max=a;
                                                }
                                            }
                                        }
                                        max *= 1.2f;
                                    }
                                    if(hero.pointsInTalent(Talent.SACRED_BLADE)>2 && (Char.hasProp(enemy, Char.Property.UNDEAD) || Char.hasProp(enemy, Char.Property.DEMONIC))){
                                        max *= 1.5f;
                                    }
                                    enemy.damage(max, Sacred_Blade.this);
                                    Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.8f, 1f) );
                                    Sample.INSTANCE.play( Assets.Sounds.HIT_STAB, 1, Random.Float(0.8f, 1f) );
                                    if (enemy.isActive()){
                                        Buff.affect(enemy, GuidingLight.Illuminated.class);
                                    }
                                    enemy.sprite.burst(0xFFFFFFFF, 10);
                                    hero.spendAndNext(1f);
                                    onSpellCast(tome, hero);
                                }
                            });
        } else {
            ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
                    reset(hero.sprite,
                            target,
                            new HolBladeVFX(),
                            new Callback() {
                                @Override
                                public void call() {
                                    Splash.at(target, 0xFFFFFFFF, 10);
                                    Dungeon.level.pressCell(aim.collisionPos);
                                    hero.spendAndNext(1f);
                                    onSpellCast(tome, hero);
                                }
                            });
        }

    }

    public static class HolBladeVFX extends Item {

        {
            image = ItemSpriteSheet.BOOMERANG;
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return new ItemSprite.Glowing(0xFFFFFF, 0.1f);
        }

        @Override
        public Emitter emitter() {
            Emitter emitter = new Emitter();
            emitter.pos( 5, 5, 0, 0);
            emitter.fillTarget = false;
            emitter.pour(SparkParticle.FACTORY, 0.025f);
            return emitter;
        }
    }



}
