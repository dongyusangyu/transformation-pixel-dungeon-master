/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.Emitter.Factory;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class BloodParticle extends PixelParticle.Shrinking {
	
	public static final Emitter.Factory FACTORY = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((BloodParticle)emitter.recycle( BloodParticle.class )).reset( x, y );
		}
	};

	public static final Emitter.Factory BURST = new Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((BloodParticle)emitter.recycle( BloodParticle.class )).resetBurst( x, y );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

	public static final Emitter.Factory SPRAY_BASE       = colored(0xB2F2FF);
	public static final Emitter.Factory SPRAY_STRENGTH   = colored(0xCC0022);
	public static final Emitter.Factory SPRAY_HEAL       = colored(0x2EE62E);
	public static final Emitter.Factory SPRAY_MIND       = colored(0x919999);
	public static final Emitter.Factory SPRAY_TOXIC      = colored(0xA15CE5);
	public static final Emitter.Factory SPRAY_HASTE      = colored(0xCCBB00);
	public static final Emitter.Factory SPRAY_INVIS      = colored(0xD9D9D9);
	public static final Emitter.Factory SPRAY_LEVITATION = colored(0x195D80);
	public static final Emitter.Factory SPRAY_PURITY     = colored(0xFF4CD2);
	public static final Emitter.Factory SPRAY_EXP        = colored(0x404040);
	public static final Emitter.Factory HEAL             = SPRAY_HEAL;

	private static Emitter.Factory colored(final int color) {
		return new Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((BloodParticle)emitter.recycle( BloodParticle.class )).reset( x, y, color );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};
	}
	
	public BloodParticle() {
		super();
		
		color( 0xCC0000 );
		lifespan = 0.8f;
		
		acc.set( 0, +40 );
	}
	
	public void reset( float x, float y ) {
		revive();
		
		this.x = x;
		this.y = y;
		
		left = lifespan;
		
		size = 4;
		speed.set( 0 );
	}
	public void reset( float x, float y, int color ) {
		reset(x, y);
		color(color);
	}

	public void reset( float x, float y, float r, float g, float b ) {
		reset(x,y);
		hardlight(r,g,b);
	}

	public void resetBurst( float x, float y ) {
		revive();

		this.x = x;
		this.y = y;

		speed.polar( Random.Float(PointF.PI2), Random.Float( 16, 32 ) );
		size = 5;

		left = 0.5f;
	}

	
	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = p > 0.6f ? (1 - p) * 2.5f : 1;
	}
}
