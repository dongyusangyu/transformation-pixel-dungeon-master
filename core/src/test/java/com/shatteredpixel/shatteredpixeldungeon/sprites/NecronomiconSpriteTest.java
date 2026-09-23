package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NecronomiconSpriteTest {

    @Test
    public void frameUsesArtifactCell273AndDeclaredBounds() throws IOException {
        assertEquals(273, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.NECRONOMICON));
        assertEquals(12, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.NECRONOMICON));
        assertEquals(15, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.NECRONOMICON));

        BufferedImage sheet = ImageIO.read(new File(coreDirectory(),
                "src/main/assets/sprites/ex_items.png"));
        int opaque = 0;
        int white = 0;
        int minX = 16, minY = 272, maxX = -1, maxY = -1;
        for (int y = 272; y < 288; y++) {
            for (int x = 16; x < 32; x++) {
                int argb = sheet.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) continue;
                opaque++;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                if (red > 180 && green > 180 && blue > 180) white++;
            }
        }
        assertTrue("book cell should contain opaque pixels", opaque > 40);
        assertEquals("12px frame must start at the cell's left edge", 16, minX);
        assertEquals("15px frame must start at the cell's top edge", 272, minY);
        assertEquals("opaque art must use the declared 12px width", 27, maxX);
        assertEquals("opaque art must use the declared 15px height", 286, maxY);
        assertEquals("the book body must fill its whole declared silhouette",
                12 * 15, opaque);
        assertTrue("white skull must remain readable", white >= 8);
        for (int y = 272; y < 288; y++) {
            for (int x = 28; x < 32; x++) {
                assertEquals("pixels beyond the declared width must stay transparent",
                        0, (sheet.getRGB(x, y) >>> 24) & 0xFF);
            }
        }
        for (int x = 16; x < 32; x++) {
            assertEquals("pixels beyond the declared height must stay transparent",
                    0, (sheet.getRGB(x, 287) >>> 24) & 0xFF);
        }
    }

    @Test
    public void coverEmblemUsesTheEliteChampionCorruptBuffSilhouette() throws IOException {
        BufferedImage buffs = ImageIO.read(new File(coreDirectory(),
                "src/main/assets/interfaces/buffs.png"));
        BufferedImage sheet = ImageIO.read(new File(coreDirectory(),
                "src/main/assets/sprites/ex_items.png"));
        for (int sy = 0; sy < 7; sy++) {
            for (int sx = 0; sx < 7; sx++) {
                int source = buffs.getRGB(sx, 14 + sy);
                int sr = (source >>> 16) & 0xFF;
                int sg = (source >>> 8) & 0xFF;
                int sb = source & 0xFF;
                int target = sheet.getRGB(16 + 3 + sx, 272 + 4 + sy);
                int tr = (target >>> 16) & 0xFF;
                int tg = (target >>> 8) & 0xFF;
                int tb = target & 0xFF;
                if (sr + sg + sb < 90) {
                    assertTrue("buff skull holes remain dark", tr + tg + tb < 180);
                } else {
                    assertTrue("buff skull pixels are copied to the cover",
                            tr > 100 && tg > 100 && tb > 100);
                }
            }
        }
    }

    private static File coreDirectory() {
        File cwd = new File(System.getProperty("user.dir"));
        return cwd.getName().equals("core") ? cwd : new File(cwd, "core");
    }
}
