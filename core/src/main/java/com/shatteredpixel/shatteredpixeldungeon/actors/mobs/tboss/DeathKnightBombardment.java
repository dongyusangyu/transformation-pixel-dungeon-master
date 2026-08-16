package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

/** Pure, deterministic geometry used by the death knight's bombardments. */
public final class DeathKnightBombardment {

    private static final double EPSILON = 0.000001;
    public enum Band {
        NONE, OUTER, INNER, CORE
    }

    public enum SkillShape {
        LINE, CONE, CROSS, RING
    }

    public static final class Grid {
        public final int width;
        public final int height;
        public final boolean[] passable;
        public final boolean[] solid;
        public final boolean[] occupied;

        public Grid(int width, int height, boolean[] passable,
                    boolean[] solid, boolean[] occupied) {
            int length = width * height;
            if (width <= 0 || height <= 0
                    || passable == null || passable.length != length
                    || solid == null || solid.length != length
                    || occupied == null || occupied.length != length) {
                throw new IllegalArgumentException("Grid arrays must match its dimensions");
            }
            this.width = width;
            this.height = height;
            this.passable = passable;
            this.solid = solid;
            this.occupied = occupied;
        }

        public int length() {
            return width * height;
        }

        public int x(int cell) {
            return cell % width;
        }

        public int y(int cell) {
            return cell / width;
        }

        public int cell(int x, int y) {
            return x + y * width;
        }

        public boolean inside(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        public boolean valid(int cell) {
            return cell >= 0 && cell < length();
        }
    }

    public static final class Plan {
        public final int[] cells;
        public final Band[] bands;
        public final int landingCell;

        private Plan(int[] cells, Band[] bands, int landingCell) {
            this.cells = cells.clone();
            this.bands = bands.clone();
            this.landingCell = landingCell;
        }

        public boolean contains(int cell) {
            for (int value : cells) if (value == cell) return true;
            return false;
        }

        public Band bandAt(int cell) {
            for (int i = 0; i < cells.length; i++) {
                if (cells[i] == cell) return bands[i];
            }
            return Band.NONE;
        }
    }

    private DeathKnightBombardment() {
    }

    public static Plan withLanding(Plan plan, int landingCell) {
        if (plan == null) return null;
        return new Plan(plan.cells, plan.bands, landingCell);
    }

    public static Plan line(Grid grid, int origin, int aimedCell, int width) {
        requireCell(grid, origin);
        requireCell(grid, aimedCell);
        int ox = grid.x(origin);
        int oy = grid.y(origin);
        double dx = grid.x(aimedCell) - ox;
        double dy = grid.y(aimedCell) - oy;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < EPSILON || width <= 0) return emptyPlan();
        double ux = dx / length;
        double uy = dy / length;
        double halfWidth = (width - 1) / 2d;
        LinkedHashSet<Integer> cells = new LinkedHashSet<>();
        for (int cell = 0; cell < grid.length(); cell++) {
            int rx = grid.x(cell) - ox;
            int ry = grid.y(cell) - oy;
            double forward = rx * ux + ry * uy;
            double perpendicular = Math.abs(rx * uy - ry * ux);
            if (forward > EPSILON
                    && perpendicular <= halfWidth + EPSILON
                    && visible(grid, origin, cell)) {
                cells.add(cell);
            }
        }
        return plan(cells, -1);
    }

    public static Plan cone(Grid grid, int origin, int aimedCell,
                            int degrees, int radius) {
        requireCell(grid, origin);
        requireCell(grid, aimedCell);
        int ox = grid.x(origin);
        int oy = grid.y(origin);
        double dx = grid.x(aimedCell) - ox;
        double dy = grid.y(aimedCell) - oy;
        double directionLength = Math.sqrt(dx * dx + dy * dy);
        if (directionLength < EPSILON || degrees <= 0 || radius <= 0) return emptyPlan();
        double ux = dx / directionLength;
        double uy = dy / directionLength;
        double threshold = Math.cos(Math.toRadians(degrees / 2d));
        LinkedHashSet<Integer> cells = new LinkedHashSet<>();
        for (int cell = 0; cell < grid.length(); cell++) {
            int rx = grid.x(cell) - ox;
            int ry = grid.y(cell) - oy;
            double distance = Math.sqrt(rx * rx + ry * ry);
            if (distance < EPSILON) continue;
            double cosine = (rx * ux + ry * uy) / distance;
            if (cosine + EPSILON >= threshold && visible(grid, origin, cell)) {
                cells.add(cell);
            }
        }
        return plan(cells, -1);
    }

    public static Plan cross(Grid grid, int origin, int width) {
        requireCell(grid, origin);
        if (width <= 0) return emptyPlan();
        int ox = grid.x(origin);
        int oy = grid.y(origin);
        int halfWidth = (width - 1) / 2;
        LinkedHashSet<Integer> cells = new LinkedHashSet<>();
        for (int cell = 0; cell < grid.length(); cell++) {
            int dx = Math.abs(grid.x(cell) - ox);
            int dy = Math.abs(grid.y(cell) - oy);
            if ((dx <= halfWidth || dy <= halfWidth) && visible(grid, origin, cell)) {
                cells.add(cell);
            }
        }
        return plan(cells, -1);
    }

    public static Plan ring(Grid grid, int origin, int aimedCell,
                            int innerRadius, int outerRadius) {
        requireCell(grid, origin);
        requireCell(grid, aimedCell);
        if (innerRadius < 0 || outerRadius < innerRadius) return emptyPlan();
        int ox = grid.x(origin);
        int oy = grid.y(origin);
        double dx = grid.x(aimedCell) - ox;
        double dy = grid.y(aimedCell) - oy;
        double directionLength = Math.sqrt(dx * dx + dy * dy);
        double ux = directionLength < EPSILON ? 1d : dx / directionLength;
        double uy = directionLength < EPSILON ? 0d : dy / directionLength;
        LinkedHashSet<Integer> cells = new LinkedHashSet<>();
        for (int cell = 0; cell < grid.length(); cell++) {
            int rx = grid.x(cell) - ox;
            int ry = grid.y(cell) - oy;
            int distance = Math.max(Math.abs(rx), Math.abs(ry));
            if (distance < innerRadius || distance > outerRadius) continue;
            double forward = rx * ux + ry * uy;
            double perpendicular = Math.abs(rx * uy - ry * ux);
            boolean inForwardGap = forward > EPSILON && perpendicular <= 1d + EPSILON;
            if (!inForwardGap && visible(grid, origin, cell)) cells.add(cell);
        }
        return plan(cells, -1);
    }

    public static Plan firstSafePlan(Grid grid, SkillShape shape,
                                     int origin, int hero, int width,
                                     int angle, int radius) {
        requireCell(grid, origin);
        requireCell(grid, hero);
        // Cone strikes always keep the locked target on their centre axis. Their
        // naturally narrow area near the origin replaces the ordinary safe-step rule.
        if (shape == SkillShape.CONE) {
            return cone(grid, origin, hero, angle, radius);
        }
        int[] steps = reachableStepCells(grid, hero);
        if (steps.length == 0) return null;
        // A three-cell-wide line covers every adjacent cell around a target on its axis.
        // It therefore relies on its warning turns rather than the one-step safe-cell rule.
        if (shape == SkillShape.LINE) {
            return withLanding(line(grid, origin, hero, width),
                    landingCell(grid, origin, hero));
        }
        int[] rotations = {0, -45, 45, -90, 90, 180};
        for (int rotation : rotations) {
            int aimedCell = rotation == 0 ? hero : rotatedAimCell(grid, origin, hero,
                    rotation, Math.max(1, radius));
            Plan candidate;
            switch (shape) {
                case CONE:
                    candidate = cone(grid, origin, aimedCell, angle, radius);
                    break;
                case CROSS:
                    candidate = cross(grid, origin, width);
                    break;
                case RING:
                    candidate = ring(grid, origin, aimedCell, 2, radius);
                    break;
                case LINE:
                default:
                    candidate = line(grid, origin, aimedCell, width);
                    candidate = withLanding(candidate,
                            landingCell(grid, origin, aimedCell));
                    break;
            }
            for (int step : steps) {
                if (!candidate.contains(step)) return candidate;
            }
        }
        return null;
    }

    public static int[] reachableStepCells(Grid grid, int hero) {
        requireCell(grid, hero);
        int hx = grid.x(hero);
        int hy = grid.y(hero);
        ArrayList<Integer> result = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int x = hx + dx;
                int y = hy + dy;
                if (!grid.inside(x, y)) continue;
                int cell = grid.cell(x, y);
                if (grid.passable[cell] && !grid.solid[cell] && !grid.occupied[cell]) {
                    result.add(cell);
                }
            }
        }
        return toIntArray(result);
    }

    public static Plan execution(Grid grid, int origin, int hero) {
        requireCell(grid, origin);
        requireCell(grid, hero);
        int ox = grid.x(origin);
        int oy = grid.y(origin);
        double dx = grid.x(hero) - ox;
        double dy = grid.y(hero) - oy;
        double length = Math.sqrt(dx * dx + dy * dy);
        double ux = length < EPSILON ? 1d : dx / length;
        double uy = length < EPSILON ? 0d : dy / length;
        ArrayList<Integer> cells = new ArrayList<>();
        ArrayList<Band> bands = new ArrayList<>();
        for (int cell = 0; cell < grid.length(); cell++) {
            if (!grid.passable[cell] || grid.solid[cell] || !visible(grid, origin, cell)) continue;
            int rx = grid.x(cell) - ox;
            int ry = grid.y(cell) - oy;
            double perpendicular = Math.abs(rx * uy - ry * ux);
            cells.add(cell);
            bands.add(perpendicular <= 0.5d + EPSILON ? Band.CORE
                    : perpendicular <= 1.5d + EPSILON ? Band.INNER : Band.OUTER);
        }
        return new Plan(toIntArray(cells), bands.toArray(new Band[0]), -1);
    }

    public static int scaledDamage(int damage, Band band) {
        switch (band) {
            case CORE:
                return damage;
            case INNER:
                return Math.round(damage * 0.7f);
            case OUTER:
                return Math.round(damage * 0.4f);
            default:
                return 0;
        }
    }

    public static int landingCell(Grid grid, int origin, int aimedCell) {
        requireCell(grid, origin);
        requireCell(grid, aimedCell);
        int result = origin;
        for (int cell : trace(grid, origin, aimedCell)) {
            if (cell == origin) continue;
            if (grid.solid[cell] || !grid.passable[cell]) break;
            result = cell;
        }
        return result;
    }

    private static int rotatedAimCell(Grid grid, int origin, int target,
                                      int degrees, int distance) {
        double dx = grid.x(target) - grid.x(origin);
        double dy = grid.y(target) - grid.y(origin);
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < EPSILON) return target;
        double radians = Math.toRadians(degrees);
        double ux = dx / length;
        double uy = dy / length;
        double rx = ux * Math.cos(radians) - uy * Math.sin(radians);
        double ry = ux * Math.sin(radians) + uy * Math.cos(radians);
        int x = clamp((int) Math.round(grid.x(origin) + rx * distance), 0, grid.width - 1);
        int y = clamp((int) Math.round(grid.y(origin) + ry * distance), 0, grid.height - 1);
        return grid.cell(x, y);
    }

    private static boolean visible(Grid grid, int origin, int target) {
        if (!grid.valid(target) || grid.solid[target]) return false;
        int[] trace = trace(grid, origin, target);
        for (int i = 1; i < trace.length; i++) {
            if (grid.solid[trace[i]]) return false;
        }
        return true;
    }

    private static int[] trace(Grid grid, int from, int to) {
        int x = grid.x(from);
        int y = grid.y(from);
        int targetX = grid.x(to);
        int targetY = grid.y(to);
        int dx = Math.abs(targetX - x);
        int dy = Math.abs(targetY - y);
        int sx = Integer.compare(targetX, x);
        int sy = Integer.compare(targetY, y);
        int error = dx - dy;
        ArrayList<Integer> cells = new ArrayList<>();
        while (true) {
            cells.add(grid.cell(x, y));
            if (x == targetX && y == targetY) break;
            int doubled = error * 2;
            if (doubled > -dy) {
                error -= dy;
                x += sx;
            }
            if (doubled < dx) {
                error += dx;
                y += sy;
            }
        }
        return toIntArray(cells);
    }

    private static Plan plan(LinkedHashSet<Integer> cells, int landingCell) {
        int[] values = new int[cells.size()];
        int index = 0;
        for (int cell : cells) values[index++] = cell;
        Band[] bands = new Band[values.length];
        Arrays.fill(bands, Band.NONE);
        return new Plan(values, bands, landingCell);
    }

    private static Plan emptyPlan() {
        return new Plan(new int[0], new Band[0], -1);
    }

    private static int[] toIntArray(ArrayList<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) result[i] = values.get(i);
        return result;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void requireCell(Grid grid, int cell) {
        if (grid == null || !grid.valid(cell)) {
            throw new IllegalArgumentException("Cell is outside the grid");
        }
    }
}
