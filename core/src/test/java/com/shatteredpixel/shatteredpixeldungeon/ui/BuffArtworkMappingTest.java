package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PrecognitiveEye;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MercuryBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MountainGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SoulBlade;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidRun;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.Overburden;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.RaidKeyCarrier;
import com.watabou.noosa.Image;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuffArtworkMappingTest {

	@Test
	public void existingBuffsUseTheirDedicatedFrames() {
		assertEquals(94, new PrecognitiveEye.MomentaryForesight().icon());
		assertEquals(146, new ExtractionRaidRun.RaidSession().icon());
		assertEquals(147, new Overburden().icon());
		assertEquals(148, new RaidKeyCarrier().icon());
		assertEquals(151, new MercuryBlade.MercurySolidification().icon());
		assertEquals(152, new SoulBlade.DuskBuff().icon());
		assertEquals(153, new MountainGuard.MountainEnergyTracker().icon());
		assertEquals(154, new MountainGuard.MountainWallCounter().icon());
		assertEquals(155, new Talent.NaturalChildAction().icon());
		assertEquals(155, new Talent.NaturalChildBarkskin().icon());
		assertEquals(BuffIndicator.TIME, new Talent.NaturalChildCooldown().icon());
	}

	@Test
	public void actionIndicatorsUseDedicatedFramesAndColors() {
		ExtractionRaidRun.RaidSession raid = new ExtractionRaidRun.RaidSession();
		assertEquals(122, raid.actionIcon());
		assertEquals(0x009B00, raid.indicatorColor());

		Talent.NaturalChildAction nature = new Talent.NaturalChildAction();
		assertEquals(123, nature.actionIcon());
		assertEquals(0x013055, nature.indicatorColor());
	}

	@Test
	public void buffTintsUseRequestedColorsWithoutRecoloringPrecoloredArt() {
		Image eye = new Image();
		new PrecognitiveEye.MomentaryForesight().tintIcon(eye);
		assertEquals(0.3529f, eye.rm, 0f);
		assertEquals(0.8941f, eye.gm, 0f);
		assertEquals(0.9020f, eye.bm, 0f);

		Image cooldown = new Image();
		new Talent.NaturalChildCooldown().tintIcon(cooldown);
		assertEquals(0.0706f, cooldown.rm, 0f);
		assertEquals(0.5333f, cooldown.gm, 0f);
		assertEquals(0.6314f, cooldown.bm, 0f);

		Image tree = new Image();
		new Talent.NaturalChildBarkskin().tintIcon(tree);
		assertEquals(1f, tree.rm, 0f);
		assertEquals(1f, tree.gm, 0f);
		assertEquals(1f, tree.bm, 0f);
	}

	@Test
	public void reservedFramesExistInBothBuffAtlases() throws Exception {
		assertEquals(156, BuffIndicator.SERPENT_STAFF_SPOON);
		assertEquals(157, BuffIndicator.CURSE_BURNING);
		assertEquals(158, BuffIndicator.PRECISE_LOCK);

		BufferedImage small = load("buffs.png");
		BufferedImage large = load("large_buffs.png");
		for (int index : new int[]{94, 146, 147, 148, 151, 152, 153, 154, 155, 156, 157, 158}) {
			assertTrue("small buff frame " + index + " is empty", hasPixels(small, 7, index));
			assertTrue("large buff frame " + index + " is empty", hasPixels(large, 16, index));
		}
		assertTrue(hasPixels(load("hero_icons.png"), 16, 122));
		assertTrue(hasPixels(load("hero_icons.png"), 16, 123));
	}

	private static BufferedImage load(String name) throws Exception {
		File file = new File("src/main/assets/interfaces/" + name);
		if (!file.isFile()) file = new File("core/src/main/assets/interfaces/" + name);
		return ImageIO.read(file);
	}

	private static boolean hasPixels(BufferedImage sheet, int size, int index) {
		int x = index % (sheet.getWidth() / size) * size;
		int y = index / (sheet.getWidth() / size) * size;
		if (y + size > sheet.getHeight()) return false;
		for (int row = y; row < y + size; row++) {
			for (int col = x; col < x + size; col++) {
				if ((sheet.getRGB(col, row) >>> 24) != 0) return true;
			}
		}
		return false;
	}
}
