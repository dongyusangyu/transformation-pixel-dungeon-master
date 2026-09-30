package com.shatteredpixel.shatteredpixeldungeon.items;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.io.File;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeneratorArtifactTest {
	private Files previousFiles;

	@Before
	public void prepareAssets() {
		previousFiles = Gdx.files;
		GdxNativesLoader.load();
		Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
				new Class<?>[]{Files.class}, (proxy, method, args) -> {
					if (method.getReturnType() == FileHandle.class) {
						File assets = new File("core/src/main/assets");
						if (!assets.isDirectory()) assets = new File("src/main/assets");
						return new FileHandle(new File(assets, (String) args[0]));
					}
					return null;
				});
	}

	@After
	public void resetGenerator() {
		Generator.fullReset();
		Gdx.files = previousFiles;
	}

	@Test
	public void legacyArtifactPoolKeepsPreviouslyGeneratedArtifactsUnavailable() {
		int talismanIndex = artifactIndex(TalismanOfForesight.class);
		float[] legacyProbs = Arrays.copyOf(Generator.Category.ARTIFACT.defaultProbs,
				Generator.Category.ARTIFACT.defaultProbs.length - 2);
		legacyProbs[talismanIndex] = 0;

		Bundle bundle = new Bundle();
		bundle.put("artifact_probs", legacyProbs);

		Generator.restoreFromBundle(bundle);

		assertEquals(0f, Generator.Category.ARTIFACT.probs[talismanIndex], 0f);
		assertEquals(1f, Generator.Category.ARTIFACT.probs[artifactIndex(AlchemistsToolkit.class)], 0f);
	}

	@Test
	public void artifactCanOnlyBeClaimedOnce() {
		assertTrue(Generator.claimArtifact(TalismanOfForesight.class));
		assertFalse(Generator.claimArtifact(TalismanOfForesight.class));
	}

	@Test
	public void namedArtifactPoolMigrationDoesNotDependOnArtifactOrder() {
		Bundle bundle = new Bundle();
		bundle.put("artifact_probs", new float[]{1f, 0f});
		bundle.put("artifact_classes", new String[]{
				AlchemistsToolkit.class.getName(),
				TalismanOfForesight.class.getName()
		});

		Generator.restoreFromBundle(bundle);

		assertEquals(0f, Generator.Category.ARTIFACT.probs[artifactIndex(TalismanOfForesight.class)], 0f);
	}

	@Test
	public void claimedStartingArtifactSurvivesSaveAndLoad() {
		Generator.claimArtifact(CloakOfShadows.class);
		Bundle bundle = new Bundle();
		Generator.storeInBundle(bundle);

		Generator.restoreFromBundle(bundle);

		assertFalse(Generator.claimArtifact(CloakOfShadows.class));
	}

	@Test
	public void exhaustedArtifactFallbackDoesNotConsumeTheNaturalRingDeck() {
		Generator.fullReset();
		for (Class<?> artifact : Generator.Category.ARTIFACT.classes) {
			Generator.claimArtifact(artifact.asSubclass(Artifact.class));
		}
		float[] ringProbabilities = Generator.Category.RING.probs.clone();
		int naturalRingsDropped = Generator.Category.RING.dropped;

		assertTrue(Generator.random(Generator.Category.ARTIFACT)
				instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring);
		assertTrue(Arrays.equals(ringProbabilities, Generator.Category.RING.probs));
		assertEquals(naturalRingsDropped, Generator.Category.RING.dropped);
	}

	private int artifactIndex(Class<?> artifactClass) {
		for (int i = 0; i < Generator.Category.ARTIFACT.classes.length; i++) {
			if (Generator.Category.ARTIFACT.classes[i] == artifactClass) {
				return i;
			}
		}
		throw new AssertionError("Artifact class is missing from the generator pool");
	}
}
