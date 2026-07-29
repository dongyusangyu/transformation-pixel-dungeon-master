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
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DongyusangyuSpriteTest {

	private static Path corePath( String relativePath ) {
		Path workingDirectory = Paths.get( System.getProperty( "user.dir" ) );
		Path coreDirectory = workingDirectory.resolve( "core" );
		if (Files.isDirectory( coreDirectory )) {
			return coreDirectory.resolve( relativePath );
		}
		return workingDirectory.resolve( relativePath );
	}

	private static String readUtf8( Path path ) throws IOException {
		return new String( Files.readAllBytes( path ), StandardCharsets.UTF_8 );
	}

	@Test
	public void projectDeclaresDongyusangyuSpriteAndAsset() throws IOException {
		Path spriteSource = corePath(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java" );
		assertTrue( "DongyusangyuSprite.java must exist", Files.exists( spriteSource ) );

		Path assetsSource = corePath(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java" );
		String assets = readUtf8( assetsSource );
		assertTrue( assets.contains(
				"DONGYUSANGYU = \"sprites/dongyusangyu.png\"" ) );

		String sprite = readUtf8( spriteSource );
		assertTrue( sprite.contains( "new TextureFilm( texture, 16, 16 )" ) );
		assertTrue( sprite.contains( "idle = new Animation( 8, true )" ) );
		assertTrue( sprite.contains( "run = idle.clone()" ) );
		assertTrue( sprite.contains( "attack = idle.clone()" ) );
		assertTrue( sprite.contains( "zap = idle.clone()" ) );
		assertTrue( sprite.contains( "die = new Animation( 10, false )" ) );
	}

	@Test
	public void spriteSheetHasTenSixteenPixelFrames() throws IOException {
		Path sheetPath = corePath( "src/main/assets/sprites/dongyusangyu.png" );
		assertTrue( "dongyusangyu.png must exist", Files.exists( sheetPath ) );

		BufferedImage image = ImageIO.read( sheetPath.toFile() );
		assertNotNull( "dongyusangyu.png must be a readable PNG", image );
		assertEquals( 160, image.getWidth() );
		assertEquals( 16, image.getHeight() );

		for (int frame = 0; frame < 10; frame++) {
			int opaquePixels = 0;
			for (int y = 0; y < 16; y++) {
				for (int x = frame * 16; x < (frame + 1) * 16; x++) {
					if ((image.getRGB( x, y ) >>> 24) != 0) {
						opaquePixels++;
					}
				}
			}
			assertTrue( "frame " + frame + " must not be empty", opaquePixels > 0 );
		}
	}

	@Test
	public void spriteSheetUsesHardAlphaOnly() throws IOException {
		Path sheetPath = corePath( "src/main/assets/sprites/dongyusangyu.png" );
		assertTrue( "dongyusangyu.png must exist", Files.exists( sheetPath ) );

		BufferedImage image = ImageIO.read( sheetPath.toFile() );
		assertNotNull( "dongyusangyu.png must be a readable PNG", image );

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int alpha = image.getRGB( x, y ) >>> 24;
				assertTrue( "alpha must be 0 or 255 at " + x + "," + y,
						alpha == 0 || alpha == 255 );
			}
		}
	}
}
