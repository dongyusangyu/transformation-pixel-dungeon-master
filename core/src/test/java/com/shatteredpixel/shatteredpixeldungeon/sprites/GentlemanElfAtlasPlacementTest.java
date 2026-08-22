package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import static org.junit.Assert.*;

public class GentlemanElfAtlasPlacementTest {
    @Test public void reservedIndicesAndSourceFramesArePresent() throws Exception {
        assertEquals(127, BuffIndicator.NONE); assertEquals(149, BuffIndicator.DRUNKENNESS); assertEquals(150, BuffIndicator.EXHILARATION);
        BufferedImage buffs=read("interfaces/buffs.png"), large=read("interfaces/large_buffs.png"), items=read("sprites/ex_items.png");
        assertTrue(nonEmpty(buffs,149,18,7)); assertTrue(nonEmpty(buffs,150,18,7));
        assertTrue(nonEmpty(large,149,16,16)); assertTrue(nonEmpty(large,150,16,16));
        assertTrue(nonEmpty(items,20,16,16)); assertTrue(nonEmpty(items,21,16,16)); assertTrue(nonEmpty(items,22,16,16));
        assertTrue(empty(buffs,127,18,7)); assertTrue(empty(buffs,151,18,7));
    }
    private static BufferedImage read(String rel) throws Exception { Path c=Paths.get(System.getProperty("user.dir")); Path root=Files.isDirectory(c.resolve("core"))?c:c.getParent(); return ImageIO.read(root.resolve("core/src/main/assets").resolve(rel).toFile()); }
    private static boolean nonEmpty(BufferedImage im,int index,int cols,int size){ return !empty(im,index,cols,size); }
    private static boolean empty(BufferedImage im,int index,int cols,int size){ int x=index%cols*size,y=index/cols*size; for(int j=0;j<size;j++)for(int i=0;i<size;i++)if((im.getRGB(x+i,y+j)>>>24)!=0)return false; return true; }
}
