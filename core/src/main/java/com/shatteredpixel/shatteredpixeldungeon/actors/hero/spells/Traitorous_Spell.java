package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class Traitorous_Spell extends TargetedClericSpell {
    public static final Traitorous_Spell INSTANCE = new Traitorous_Spell();
    @Override
    public int icon() {
        return HeroIcon.TRAITOROUS_SPELL;
    }
    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.TRAITOROUS_SPELL);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 3-hero.pointsInTalent(Talent.TRAITOROUS_SPELL);
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null) {
            return;
        }
        Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
        CursedWand.cursedZap(new WandOfBlastWave(),
                hero,
                new Ballistica(hero.pos, target, Ballistica.MAGIC_BOLT), new Callback() {
                    @Override
                    public void call() {
                        WondrousResin.forcePositive = false;
                        new WandOfBlastWave().wandUsed();
                    }
                });
        hero.busy();
        hero.sprite.operate(target);
        hero.spend( 1f );
        onSpellCast(tome, hero);
    }

}
