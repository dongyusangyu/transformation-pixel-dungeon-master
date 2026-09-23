package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class GhoulLifeLinkTest {

	private Level previousLevel;
	private Hero previousHero;

	@Before
	public void setUp() {
		previousLevel = Dungeon.level;
		previousHero = Dungeon.hero;
		Actor.clear();
		Actor.resetNextID();
		Dungeon.level = openLevel(9, 9);
		Dungeon.hero = TestHeroFactory.create();
		Dungeon.hero.subClass = HeroSubClass.NONE;
	}

	@After
	public void tearDown() {
		Actor.clear();
		Actor.resetNextID();
		Dungeon.level = previousLevel;
		Dungeon.hero = previousHero;
	}

	@Test
	public void soulBoundDownedGhoulIsReattachedWhenItsHostDies() {
		Ghoul downed = ghoulAt(new SoulBoundGhoul(), 40);
		Ghoul host = ghoulAt(41);
		Buff.affect(downed, Necronomicon.SoulBound.class);
		Ghoul.GhoulLifeLink link = Buff.affect(host, Ghoul.GhoulLifeLink.class);
		link.set(5, downed);

		Actor.remove(downed);
		Dungeon.level.mobs.remove(downed);
		Buff.affect(downed, Necronomicon.SoulBound.class);

		Actor.remove(host);

		assertTrue("a soul-bound ghoul must not remain detached after its host dies",
				Actor.chars().contains(downed));
		assertTrue(Dungeon.level.mobs.contains(downed));
	}

	private static Ghoul ghoulAt(int pos) {
		return ghoulAt(new Ghoul(), pos);
	}

	private static Ghoul ghoulAt(Ghoul ghoul, int pos) {
		ghoul.pos = pos;
		ghoul.alignment = Char.Alignment.ENEMY;
		ghoul.fieldOfView = new boolean[Dungeon.level.length()];
		Arrays.fill(ghoul.fieldOfView, true);
		Actor.add(ghoul);
		Dungeon.level.mobs.add(ghoul);
		return ghoul;
	}

	private static class SoulBoundGhoul extends Ghoul {
		@Override
		public void die(Object cause) {
			if (cause instanceof GhoulLifeLink
					&& buff(Necronomicon.SoulBound.class) != null) {
				HP = HT;
				return;
			}
			super.die(cause);
		}
	}

	private static Level openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		level.blobs = new HashMap<>();
		level.plants = new SparseArray<>();
		level.traps = new SparseArray<>();
		level.mobs = new HashSet<>();
		return level;
	}

	private static class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
