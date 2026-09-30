package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

/** Raid-only visuals; inherited mob combat and equipment remain unchanged. */
public final class RaidDroneSprites {
	private RaidDroneSprites() {}

	public static TextureFilm configure(CharSprite sprite, int row) {
		sprite.texture(Assets.Sprites.UES_DRONES);
		TextureFilm frames = new TextureFilm(sprite.texture, 16, 16);
		int start = row * 18;
		sprite.idle = new CharSprite.Animation(10, true);
		sprite.idle.frames(frames, start, start + 1);
		sprite.run = new CharSprite.Animation(15, true);
		sprite.run.frames(frames, start + 2, start + 3, start + 4, start + 5, start + 6, start + 7);
		sprite.attack = new CharSprite.Animation(15, false);
		sprite.attack.frames(frames, start + 13, start + 14, start + 15, start + 16, start + 17, start);
		sprite.zap = sprite.attack.clone();
		sprite.die = new CharSprite.Animation(15, false);
		sprite.die.frames(frames, start + 8, start + 9, start + 10, start + 11, start + 12);
		sprite.play(sprite.idle, true);
		return frames;
	}

	public static final class DeathSound {
		private boolean played;
		public void play(CharSprite sprite) {
			if (!played) {
				played = true;
				if (sprite.visible) Sample.INSTANCE.play(Assets.Sounds.DRONEDIED);
			}
		}
	}

	public static class Guard extends StatueSprite {
		private final DeathSound deathSound = new DeathSound();
		public Guard() { configure(this, 0); }
		@Override public void setArmor(int tier) {
			// ArmoredStatue still calls this; UES drones have no armor-dependent frames.
		}
		@Override public int blood() { return 0xFFBBBBBB; }
		@Override public void die() { deathSound.play(this); super.die(); }
	}

	public static class Siren extends MobSprite {
		private final DeathSound deathSound = new DeathSound();
		public Siren() { configure(this, 2); }
		@Override public int blood() { return 0xFFBBBBBB; }
		@Override public void die() { deathSound.play(this); super.die(); }
	}

	public static class Gunner extends MobSprite {
		private final DeathSound deathSound = new DeathSound();
		public Gunner() { configure(this, 3); }
		@Override public int blood() { return 0xFFBBBBBB; }
		@Override public void die() { deathSound.play(this); super.die(); }

		@Override public void attack(final int cell) {
			if (ch == null || Dungeon.level == null) return;
			if (Dungeon.level.adjacent(cell, ch.pos)) {
				super.attack(cell);
				return;
			}
			final Char attacker = ch;
			// Explicit callback works with animations both enabled and disabled.
			super.zap(cell, () -> {
				idle();
				if (ch != attacker || !attacker.isAlive() || parent == null) return;
				((MissileSprite) parent.recycle(MissileSprite.class)).reset(
						this, cell, new DronesSprite.Bullet(), () -> {
							if (ch == attacker && attacker.isAlive()) attacker.onAttackComplete();
						});
			});
		}
	}
}
