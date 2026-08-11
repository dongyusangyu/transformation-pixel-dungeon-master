package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TwistedMirror;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MirrorSprite;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HeroReplicaSpriteContractTest {

	@Test
	public void bothSpritesReuseDynamicHeroAnimationInsteadOfFixedSheets() throws IOException {
		assertSame(MirrorSprite.class, AlienatedPrismaticGuardSprite.class.getSuperclass());
		assertSame(MirrorSprite.class, TwistedMirrorSprite.class.getSuperclass());

		String guard = source("AlienatedPrismaticGuardSprite.java");
		String mirror = source("TwistedMirrorSprite.java");
		assertTrue(guard.contains("Dungeon.hero.tier()"));
		assertTrue(guard.contains("super.updateArmor(tier)"));
		assertTrue(mirror.contains("Dungeon.hero.tier()"));
		assertTrue(mirror.contains("super.updateArmor(tier)"));
		assertTrue(!guard.contains("Assets.Sprites"));
		assertTrue(!mirror.contains("Assets.Sprites"));
	}

	@Test
	public void monstersUseDistinctDynamicReplicaSprites() {
		assertSame(AlienatedPrismaticGuardSprite.class,
				new AlienatedPrismaticGuard().spriteClass);
		assertSame(TwistedMirrorSprite.class, new TwistedMirror().spriteClass);
	}

	@Test
	public void filtersAreDistinctAndRestoredAcrossSpriteLifecycle() throws IOException {
		String guard = source("AlienatedPrismaticGuardSprite.java");
		String mirror = source("TwistedMirrorSprite.java");

		assertTrue(guard.contains("GUARD_ALPHA = 1f"));
		assertTrue(guard.contains("0x79CFFF"));
		assertTrue(guard.contains("tint(0.02f, 0.06f, 0.10f, 0.72f)"));
		assertLifecycleRestoresFilter(guard);

		assertTrue(mirror.contains("MIRROR_ALPHA = 0.52f"));
		assertTrue(mirror.contains("0x7A36B5"));
		assertTrue(mirror.contains("tint(0.025f, 0.01f, 0.06f, 0.80f)"));
		assertTrue(mirror.contains("ShadowParticle.CURSE"));
		assertTrue(mirror.contains("x + 1f"));
		assertLifecycleRestoresFilter(mirror);
	}

	private static void assertLifecycleRestoresFilter(String source) {
		assertTrue(source.contains("void link(Char ch)"));
		assertTrue(source.contains("void play(Animation animation)"));
		assertTrue(source.contains("void resetColor()"));
		assertTrue(source.contains("isState(State.INVISIBLE)"));
		assertTrue(count(source, "applyReplicaFilter();") >= 4);
	}

	private static int count(String source, String needle) {
		int count = 0;
		int offset = 0;
		while ((offset = source.indexOf(needle, offset)) >= 0) {
			count++;
			offset += needle.length();
		}
		return count;
	}

	private static String source(String name) throws IOException {
		return Files.readString(coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/" + name));
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = working.resolve("core");
		return Files.isDirectory(core) ? core : working;
	}
}
