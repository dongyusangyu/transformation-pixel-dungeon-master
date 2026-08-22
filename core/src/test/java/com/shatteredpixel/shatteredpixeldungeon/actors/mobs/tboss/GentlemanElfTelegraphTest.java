package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class GentlemanElfTelegraphTest {
    @Test public void squareAndCorridorStayWithinBounds() {
        Grid grid=new Grid(7,7); int[] square=GentlemanElfTelegraph.square(grid,0,1);
        assertArrayEquals(new int[]{0,1,7,8},square);
        for(int c:GentlemanElfTelegraph.corridor(grid,24,28,3)) assertTrue(c>=0&&c<49);
    }
    @Test public void lineStopsAtObstacleAndLandingFallsBack() {
        Grid grid=new Grid(7,7); grid.blocked.add(25); int[] line=GentlemanElfTelegraph.line(grid,24,28);
        assertFalse(contains(line,25)); assertFalse(contains(line,26));
        grid.occupied.add(28); GentlemanElfTelegraph.Plan plan=GentlemanElfTelegraph.plan(grid,24,28,3);
        assertNotEquals(28,plan.landingCell); assertTrue(plan.landingCell>=0);
    }
	@Test public void shallowAngleLineActuallyEndsAtTheAimedCell() {
		Grid grid = new Grid(7, 7);
		int[] line = GentlemanElfTelegraph.line(grid, 15, 26);
		assertTrue(line.length <= 6);
		assertEquals(26, line[line.length - 1]);
	}
    private static boolean contains(int[] a,int v){for(int x:a)if(x==v)return true;return false;}
    private static class Grid implements GentlemanElfTelegraph.Grid {
        final int w,h; final Set<Integer>blocked=new HashSet<>(),occupied=new HashSet<>(); Grid(int w,int h){this.w=w;this.h=h;}
        public int width(){return w;} public int height(){return h;} public boolean passable(int c){return !blocked.contains(c);} public boolean occupied(int c){return occupied.contains(c);}
    }
}
