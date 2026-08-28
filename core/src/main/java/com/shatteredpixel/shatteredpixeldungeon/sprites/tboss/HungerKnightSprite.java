package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.utils.Callback;
import com.watabou.noosa.TextureFilm;

/** Thirty-frame, right-facing, 32px Famine boss sprite. */
public class HungerKnightSprite extends MobSprite {

    private final Animation charge;
    private final Animation thrust;

    public HungerKnightSprite() {
        texture(Assets.Sprites.HUNGER_KNIGHT);
        TextureFilm film = new TextureFilm(texture, 32, 32);

        idle = new Animation(5, true);
        idle.frames(film, 0, 1, 2, 3);
        run = new Animation(12, true);
        run.frames(film, 4, 5, 6, 7, 8, 9);
        attack = new Animation(14, false);
        attack.frames(film, 10, 11, 12, 13, 14);
        charge = new Animation(8, false);
        charge.frames(film, 15, 16, 17, 18);
        thrust = new Animation(16, false);
        thrust.frames(film, 19, 20, 21, 22, 23);
        die = new Animation(9, false);
        die.frames(film, 24, 25, 26, 27, 28, 29);
        play(idle);
    }

    public void charge() {
        play(charge);
    }

    public void thrust() {
        play(thrust);
    }

    public void thrust(Callback callback) {
        animCallback = callback;
        play(thrust);
    }

    public void cancelSpecialCallback() {
        animCallback = null;
    }

    public void phaseTransition() {
        flash();
        if (visible) emitter().burst(ShadowParticle.UP, 10);
        play(charge);
    }

    @Override
    public void onComplete(Animation animation) {
        if (animation == thrust || animation == charge) idle();
        super.onComplete(animation);
    }

    @Override
    public int blood() {
        return 0xFF6D3327;
    }
}
