package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PrecognitiveEye;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestArtifactSynchronizationTest {

	@Test
	public void precognitiveEyeUsesItsActualLevelCap() {
		assertEquals(5, TestArtifact.artifactLevelCap(PrecognitiveEye.class));
	}

	@Test
	public void precognitiveEyeLevelCapIsExposedAsArtifactMetadata() {
		assertEquals(5, new PrecognitiveEye().levelCap());
	}

	@Test
	public void artifactSelectionUsesTheGeneratorRegistry() {
		List<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact>> types =
				TestArtifact.registeredArtifactTypes();
		assertEquals(com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARTIFACT.classes.length,
				types.size());
	}
}
