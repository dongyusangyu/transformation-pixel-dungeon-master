package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

/** Detailed 32px plague-doctor boss sprite with phase-specific lighting. */
public class PestilenceKnightSprite extends MobSprite {

    private Emitter terminalShadow;

    public PestilenceKnightSprite() {
        texture(Assets.Sprites.PESTILENCE_KNIGHT);
        TextureFilm film = new TextureFilm(texture, 32, 32);

        idle = new Animation(2, true);
        idle.frames(film, 0, 1, 0, 1);

        run = new Animation(5, true);
        run.frames(film, 2, 3, 4, 5);

        attack = new Animation(12, false);
        attack.frames(film,  6, 7,8);

        zap = new Animation(8, false);
        zap.frames(film, 9,10, 11);

        die = new Animation(8, false);
        die.frames(film, 12,13, 14, 15,16);

        play(idle);
    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        syncPhaseFx();
    }

    @Override
    public void update() {
        super.update();
        syncPhaseFx();
    }

    public void cast() {
        play(zap);
    }

    public void throwPrescription(int cell, int image, Callback callback) {
        super.zap(cell);
        Item flask = new Item();
        flask.image = image;
        if (parent == null) {
            if (callback != null) callback.call();
            return;
        }
        ((MissileSprite) parent.recycle(MissileSprite.class))
                .reset(this, cell, flask, callback);
    }

    public void harvest() {
        play(zap);
        if (visible) emitter().burst(ShadowParticle.CURSE, 6);
    }

    public void diagnosis() {
        if (visible) emitter().burst(ShadowParticle.MISSILE, 4);
    }

    public void tenacity() {
        flash();
        if (visible) emitter().burst(ShadowParticle.UP, 3);
    }

    @Override
    public void onComplete(Animation anim) {
        if (anim == zap) idle();
        super.onComplete(anim);
    }

    private void syncPhaseFx() {
        if (!(ch instanceof PestilenceKnight)) return;
        PestilenceKnight boss = (PestilenceKnight) ch;
        switch (boss.phase()) {
            case OUTBREAK:
                tint(0.35f, 0.55f, 0.08f, 0.18f);
                stopTerminalShadow();
                break;
            case TERMINAL:
                tint(0.30f, 0.05f, 0.42f, 0.24f);
                if (terminalShadow == null || terminalShadow.parent == null) {
                    stopTerminalShadow();
                    terminalShadow = bottomEmitter();
                    if (terminalShadow != null) terminalShadow.pour(ShadowParticle.UP, 0.18f);
                }
                if (terminalShadow != null) {
                    terminalShadow.pos(x, y + height, width, 0);
                    terminalShadow.visible = visible;
                }
                break;
            default:
                resetColor();
                stopTerminalShadow();
                break;
        }
    }

    private void stopTerminalShadow() {
        if (terminalShadow != null) terminalShadow.killAndErase();
        terminalShadow = null;
    }

    @Override
    public void kill() {
        stopTerminalShadow();
        super.kill();
    }

    @Override
    public int blood() {
        return 0xFF779345;
    }
}
