package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeneratorArtifactTest {

	@After
	public void resetGenerator() {
		Generator.fullReset();
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

	private int artifactIndex(Class<?> artifactClass) {
		for (int i = 0; i < Generator.Category.ARTIFACT.classes.length; i++) {
			if (Generator.Category.ARTIFACT.classes[i] == artifactClass) {
				return i;
			}
		}
		throw new AssertionError("Artifact class is missing from the generator pool");
	}
}
