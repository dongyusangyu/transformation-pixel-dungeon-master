package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChainShadowThiefAnimationContractTest {

	@Test
	public void visibleTheftUsesDedicatedOutAndBackChains() throws Exception {
		String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
		assertTrue(source.contains("new StealingChains("));
		assertFalse(source.contains("new Chains("));
	}

	@Test
	public void theftOccursAtContactAndEscapeStartsAtCompletion() throws Exception {
		String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
		int effect = source.indexOf("new StealingChains(");
		int provider = source.indexOf("public Item provide()", effect);
		int steal = source.indexOf("stealByChain(hero, toSteal)", provider);
		int completion = source.indexOf("public void call()", steal);
		int flee = source.indexOf("state = FLEEING", completion);
		int next = source.indexOf("next()", flee);
		assertTrue(effect >= 0);
		assertTrue(provider > effect);
		assertTrue(steal > provider);
		assertTrue(completion > steal);
		assertTrue(flee > completion);
		assertTrue(next > flee);
	}

	@Test
	public void chainTheftUsesShortRangeBeforeNormalActions() throws Exception {
		String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
		String compact = source.replaceAll("\\s+", "");

		assertTrue(compact.contains("Dungeon.level.distance(pos,hero.pos)>=4"));
		assertFalse(compact.contains("Dungeon.level.distance(pos,hero.pos)<4"));

		int wandering = source.indexOf("private class ChainWandering");
		int wanderingChain = source.indexOf("tryChainSteal()", wandering);
		int wanderingFallback = source.indexOf("super.act(enemyInFOV, justAlerted)", wanderingChain);
		assertTrue(wanderingChain > wandering);
		assertTrue(wanderingFallback > wanderingChain);

		int hunting = source.indexOf("private class ChainHunting");
		int huntingChain = source.indexOf("tryChainSteal()", hunting);
		int huntingFallback = source.indexOf("super.act(enemyInFOV, justAlerted)", huntingChain);
		assertTrue(huntingChain > hunting);
		assertTrue(huntingFallback > huntingChain);
	}

	private static Path sourcePath() {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java");
	}
}
