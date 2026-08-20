package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DeathKnightBombardmentTest {

    private static final int WIDTH = 15;

    @Test
    public void lineUsesRequestedWidthAndStopsAtWalls() {
        DeathKnightBombardment.Grid grid = openGrid();
        grid.solid[cell(7, 3)] = true;
        DeathKnightBombardment.Plan line = DeathKnightBombardment.line(
                grid, cell(7, 7), cell(7, 0), 3);

        assertTrue(line.contains(cell(6, 6)));
        assertTrue(line.contains(cell(7, 6)));
        assertTrue(line.contains(cell(8, 6)));
        assertFalse(line.contains(cell(7, 3)));
        assertFalse(line.contains(cell(7, 2)));
    }

    @Test
    public void lineUsesBossTargetAxisAndContinuesToArenaEdge() {
        DeathKnightBombardment.Grid grid = openGrid();
        DeathKnightBombardment.Plan horizontal = DeathKnightBombardment.line(
                grid, cell(2, 7), cell(5, 7), 3);

        assertTrue(horizontal.contains(cell(14, 6)));
        assertTrue(horizontal.contains(cell(14, 7)));
        assertTrue(horizontal.contains(cell(14, 8)));
        assertFalse(horizontal.contains(cell(14, 5)));
        assertFalse(horizontal.contains(cell(1, 7)));

        DeathKnightBombardment.Plan diagonal = DeathKnightBombardment.line(
                grid, cell(2, 2), cell(5, 5), 3);
        assertTrue(diagonal.contains(cell(14, 14)));
        assertFalse(diagonal.contains(cell(14, 11)));
    }

    @Test
    public void productionLinePlannerKeepsTheDirectBossTargetAxis() {
        DeathKnightBombardment.Plan line = DeathKnightBombardment.firstSafePlan(
                openGrid(), DeathKnightBombardment.SkillShape.LINE,
                cell(2, 7), cell(5, 7), 3, 0, 10);

        assertNotNull(line);
        assertTrue(line.contains(cell(14, 7)));
        assertTrue(line.contains(cell(14, 8)));
        assertFalse(line.contains(cell(14, 5)));
    }

    @Test
    public void coneUsesSixtyAndNinetyDegreeAnglesAndStopsAtWalls() {
        DeathKnightBombardment.Grid grid = openGrid();
        grid.solid[cell(9, 7)] = true;
        DeathKnightBombardment.Plan sixty = DeathKnightBombardment.cone(
                grid, cell(7, 7), cell(14, 7), 60, 6);
        DeathKnightBombardment.Plan ninety = DeathKnightBombardment.cone(
                grid, cell(7, 7), cell(14, 7), 90, 6);

        assertTrue(sixty.contains(cell(8, 7)));
        assertFalse(sixty.contains(cell(9, 7)));
        assertFalse(sixty.contains(cell(10, 7)));
        assertFalse(sixty.contains(cell(10, 10)));
        assertTrue(ninety.contains(cell(10, 10)));
    }

    @Test
    public void coneStartsAtAdjacentCellsWithoutAnArtificialDeadZone() {
        DeathKnightBombardment.Plan cone = DeathKnightBombardment.cone(
                openGrid(), cell(7, 7), cell(14, 7), 60, 6);

        assertTrue(cone.contains(cell(8, 7)));
        assertFalse(cone.contains(cell(7, 8)));
        assertTrue(cone.contains(cell(9, 7)));
    }

    @Test
    public void coneContinuesPastTargetToArenaEdge() {
        DeathKnightBombardment.Plan cone = DeathKnightBombardment.cone(
                openGrid(), cell(2, 7), cell(5, 7), 60, 3);

        assertTrue(cone.contains(cell(14, 7)));
        assertTrue(cone.contains(cell(14, 12)));
        assertFalse(cone.contains(cell(2, 0)));
    }

    @Test
    public void crossIsThreeCellsWideAndOverlapAppearsOnce() {
        DeathKnightBombardment.Plan cross = DeathKnightBombardment.cross(
                openGrid(), cell(7, 7), 3);

        assertTrue(cross.contains(cell(6, 1)));
        assertTrue(cross.contains(cell(13, 8)));
        assertEquals(1, occurrences(cross.cells, cell(7, 7)));
    }

    @Test
    public void ringLeavesInnerOuterAndTwoGapCellsSafe() {
        DeathKnightBombardment.Plan ring = DeathKnightBombardment.ring(
                openGrid(), cell(7, 7), cell(14, 7), 2, 4);

        assertFalse(ring.contains(cell(7, 7)));
        assertFalse(ring.contains(cell(12, 7)));
        assertFalse(ring.contains(cell(10, 7)));
        assertFalse(ring.contains(cell(10, 8)));
        assertTrue(ring.contains(cell(7, 3)));
    }

    @Test
    public void conePlannerKeepsTargetOnAxisAndDoesNotRotateForSafety() {
        DeathKnightBombardment.Grid grid = openGrid();
        DeathKnightBombardment.Plan first = DeathKnightBombardment.firstSafePlan(
                grid, DeathKnightBombardment.SkillShape.CONE,
                cell(7, 1), cell(7, 7), 0, 60, 8);
        DeathKnightBombardment.Plan second = DeathKnightBombardment.firstSafePlan(
                grid, DeathKnightBombardment.SkillShape.CONE,
                cell(7, 1), cell(7, 7), 0, 60, 8);

        assertNotNull(first);
        assertArrayEquals(first.cells, second.cells);
        assertTrue(first.contains(cell(7, 7)));
        boolean safe = false;
        for (int step : DeathKnightBombardment.reachableStepCells(grid, cell(7, 7))) {
            if (!first.contains(step)) safe = true;
        }
        assertFalse(safe);
    }

    @Test
    public void noReachableStepDoesNotCancelTheDirectCone() {
        DeathKnightBombardment.Grid grid = openGrid();
        for (int y = 6; y <= 8; y++) {
            for (int x = 6; x <= 8; x++) {
                if (x != 7 || y != 7) grid.passable[cell(x, y)] = false;
            }
        }
        DeathKnightBombardment.Plan cone = DeathKnightBombardment.firstSafePlan(
                grid, DeathKnightBombardment.SkillShape.CONE,
                cell(7, 1), cell(7, 7), 0, 90, 8);
        assertNotNull(cone);
        assertTrue(cone.contains(cell(7, 7)));
    }

    @Test
    public void executionBandsAndDamageMultipliersAreExact() {
        DeathKnightBombardment.Plan plan = DeathKnightBombardment.execution(
                openGrid(), cell(7, 1), cell(7, 7));
        assertEquals(plan.cells.length, plan.bands.length);
        for (DeathKnightBombardment.Band band : plan.bands) {
            assertNotEquals(DeathKnightBombardment.Band.NONE, band);
        }
        assertEquals(28, DeathKnightBombardment.scaledDamage(
                70, DeathKnightBombardment.Band.OUTER));
        assertEquals(49, DeathKnightBombardment.scaledDamage(
                70, DeathKnightBombardment.Band.INNER));
        assertEquals(70, DeathKnightBombardment.scaledDamage(
                70, DeathKnightBombardment.Band.CORE));
    }

    @Test
    public void landingStopsImmediatelyBeforeWall() {
        DeathKnightBombardment.Grid grid = openGrid();
        grid.solid[cell(7, 4)] = true;
        grid.passable[cell(7, 4)] = false;
        assertEquals(cell(7, 5), DeathKnightBombardment.landingCell(
                grid, cell(7, 10), cell(7, 1)));
    }

    @Test
    public void nearbyCoverCandidatesPreferNearestChebyshevDistance() {
        DeathKnightBombardment.Grid grid = coverGrid();
        int[] blast = {cell(7, 7)};
        grid.destructibleCover[cell(6, 7)] = true;
        grid.destructibleCover[cell(8, 7)] = true;
        grid.destructibleCover[cell(10, 7)] = true;

        assertArrayEquals(new int[]{cell(6, 7), cell(8, 7)},
                DeathKnightBombardment.nearbyCoverCandidates(
                        grid, blast, cell(7, 7)));
    }

    @Test
    public void nearbyCoverCandidatesIncludeBlastNeighborsAndSoftCover() {
        DeathKnightBombardment.Grid grid = coverGrid();
        grid.destructibleCover[cell(9, 9)] = true;
        assertArrayEquals(new int[]{cell(9, 9)},
                DeathKnightBombardment.nearbyCoverCandidates(
                        grid, new int[]{cell(8, 8)}, cell(12, 12)));
    }

    @Test
    public void nearbyCoverCandidatesReturnEmptyWhenNoCoverIsNearby() {
        DeathKnightBombardment.Grid grid = coverGrid();
        grid.destructibleCover[cell(1, 1)] = true;
        assertEquals(0, DeathKnightBombardment.nearbyCoverCandidates(
                grid, new int[]{cell(7, 7)}, cell(7, 7)).length);
    }

    @Test
    public void executionIgnoresInternalSolidCoverButRespectsArenaMask() {
        DeathKnightBombardment.Grid grid = coverGrid();
        grid.solid[cell(7, 5)] = true;
        grid.passable[cell(7, 5)] = false;
        DeathKnightBombardment.Plan execution = DeathKnightBombardment.execution(
                grid, cell(7, 1), cell(7, 7));

        assertTrue(execution.contains(cell(7, 6)));
        grid.arena[cell(7, 6)] = false;
        assertFalse(DeathKnightBombardment.execution(
                grid, cell(7, 1), cell(7, 7)).contains(cell(7, 6)));
    }

    private static DeathKnightBombardment.Grid openGrid() {
        boolean[] passable = new boolean[WIDTH * WIDTH];
        Arrays.fill(passable, true);
        return new DeathKnightBombardment.Grid(
                WIDTH, WIDTH, passable,
                new boolean[WIDTH * WIDTH], new boolean[WIDTH * WIDTH]);
    }

    private static DeathKnightBombardment.Grid coverGrid() {
        boolean[] passable = new boolean[WIDTH * WIDTH];
        boolean[] arena = new boolean[WIDTH * WIDTH];
        Arrays.fill(passable, true);
        Arrays.fill(arena, true);
        return new DeathKnightBombardment.Grid(
                WIDTH, WIDTH, passable,
                new boolean[WIDTH * WIDTH],
                new boolean[WIDTH * WIDTH],
                arena,
                new boolean[WIDTH * WIDTH]);
    }

    private static int cell(int x, int y) {
        return x + y * WIDTH;
    }

    private static int occurrences(int[] cells, int value) {
        int result = 0;
        for (int cell : cells) if (cell == value) result++;
        return result;
    }
}
