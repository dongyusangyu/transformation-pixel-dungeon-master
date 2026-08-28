package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TextureLifecycleTest {

	@Test
	public void interlevelTextureCacheIsClearedBeforeSceneGraphicsAreCreated() throws Exception {
		String interlevelScene = readCoreSource(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/InterlevelScene.java");

		int cacheClear = interlevelScene.indexOf("TextureCache.clear();");
		int sceneCreate = interlevelScene.indexOf("super.create();");

		assertTrue(cacheClear >= 0);
		assertTrue(sceneCreate >= 0);
		assertTrue(cacheClear < sceneCreate);
	}

	@Test
	public void blobsEmitterCreatesFreshImagesInsteadOfRetainingStaticImageInstances() throws Exception {
		String blobsEmitter = readCoreSource(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/BlobsEmitter.java");
		blobsEmitter = blobsEmitter.replaceAll("(?m)//.*$", "");

		assertFalse(blobsEmitter.contains("ArrayList<Image> blobImages"));
		assertFalse(blobsEmitter.contains("blobImages.add("));
		assertTrue(blobsEmitter.contains("private static Image blobImage(int blobIndex)"));
		assertTrue(blobsEmitter.contains("icon(blobImage(blobIndex));"));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path root = Path.of(System.getProperty("user.dir"));
		Path source = Files.exists(root.resolve(relativePath))
				? root.resolve(relativePath)
				: root.resolve("core").resolve(relativePath);
		return Files.readString(source, StandardCharsets.UTF_8);
	}
}
