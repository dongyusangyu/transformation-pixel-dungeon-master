package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

/** Pure, deterministic warning-area geometry for the gentleman's skills. */
public final class GentlemanElfTelegraph {
	public interface Grid {
		int width();
		int height();
		boolean passable(int cell);
		boolean occupied(int cell);
	}
	public static final class Plan {
		public final int[] cells;
		public final int landingCell;
		public Plan(int[] cells, int landingCell) {
			this.cells = cells == null ? new int[0] : cells.clone();
			this.landingCell = landingCell;
		}
	}
	private GentlemanElfTelegraph() { }

	public static int[] square(Grid grid, int center, int radius) {
		if (grid == null || center < 0) return new int[0];
		int cx = center % grid.width(), cy = center / grid.width();
		java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
		for (int y = Math.max(0, cy-radius); y <= Math.min(grid.height()-1, cy+radius); y++) {
			for (int x = Math.max(0, cx-radius); x <= Math.min(grid.width()-1, cx+radius); x++) {
				int cell = y * grid.width() + x;
				if (grid.passable(cell)) result.add(cell);
			}
		}
		return result.stream().mapToInt(Integer::intValue).toArray();
	}

	public static int[] line(Grid grid, int from, int to) {
		if (grid == null || from < 0 || to < 0) return new int[0];
		java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
		for (int cell : geometricLine(grid, from, to)) {
			if (!grid.passable(cell)) break;
			result.add(cell);
		}
		return result.stream().mapToInt(Integer::intValue).toArray();
	}

	/** Returns the geometric segment between two cells, excluding both endpoints. */
	public static int[] intermediateSegment(Grid grid, int from, int to) {
		int[] full = geometricLine(grid, from, to);
		if (full.length <= 2) return new int[0];
		int[] result = new int[full.length - 2];
		System.arraycopy(full, 1, result, 0, result.length);
		return result;
	}

	private static int[] geometricLine(Grid grid, int from, int to) {
		if (grid == null || from < 0 || to < 0
				|| from >= grid.width() * grid.height() || to >= grid.width() * grid.height())
			return new int[0];
		int x = from % grid.width(), y = from / grid.width();
		int tx = to % grid.width(), ty = to / grid.width();
		int dx = Math.abs(tx - x), dy = Math.abs(ty - y);
		int sx = Integer.compare(tx, x), sy = Integer.compare(ty, y);
		int error = dx - dy;
		java.util.ArrayList<Integer> result = new java.util.ArrayList<>();
		while (true) {
			result.add(y * grid.width() + x);
			if (x == tx && y == ty) break;
			int doubled = error * 2;
			if (doubled > -dy) { error -= dy; x += sx; }
			if (doubled < dx) { error += dx; y += sy; }
		}
		return result.stream().mapToInt(Integer::intValue).toArray();
	}

	public static int endpointAtBoundary(Grid grid, int from, int toward) {
		if (grid == null || from < 0 || toward < 0 || from == toward) return toward;
		double fx = from % grid.width(), fy = from / grid.width();
		double vx = toward % grid.width() - fx, vy = toward / grid.width() - fy;
		double tx = vx > 0 ? (grid.width() - 1 - fx) / vx : vx < 0 ? -fx / vx : Double.POSITIVE_INFINITY;
		double ty = vy > 0 ? (grid.height() - 1 - fy) / vy : vy < 0 ? -fy / vy : Double.POSITIVE_INFINITY;
		double scale = Math.min(tx, ty);
		int x = Math.max(0, Math.min(grid.width() - 1, (int) Math.round(fx + vx * scale)));
		int y = Math.max(0, Math.min(grid.height() - 1, (int) Math.round(fy + vy * scale)));
		return x + y * grid.width();
	}

	public static int[] corridor(Grid grid, int from, int to, int width) {
		if (grid == null || width <= 0) return new int[0];
		int[] axis = line(grid, from, to);
		java.util.LinkedHashSet<Integer> cells = new java.util.LinkedHashSet<>();
		int half = width / 2;
		for (int cell : axis) {
			int x = cell % grid.width(), y = cell / grid.width();
			boolean horizontal = Math.abs((to % grid.width()) - (from % grid.width()))
					>= Math.abs((to / grid.width()) - (from / grid.width()));
			for (int i = -half; i <= half; i++) {
				int nx = horizontal ? x : x + i, ny = horizontal ? y + i : y;
				if (nx >= 0 && ny >= 0 && nx < grid.width() && ny < grid.height()) {
					int target = ny * grid.width() + nx;
					if (grid.passable(target)) cells.add(target);
				}
			}
		}
		return cells.stream().mapToInt(Integer::intValue).toArray();
	}

	public static Plan plan(Grid grid, int origin, int target, int width) {
		int[] cells = corridor(grid, origin, target, width);
		int landing = -1;
		if (grid != null && target >= 0 && grid.passable(target) && !grid.occupied(target)) landing = target;
		if (landing < 0 && grid != null) {
			int tx = target % grid.width(), ty = target / grid.width();
			int bestDistance = Integer.MAX_VALUE;
			for (int cell = 0; cell < grid.width() * grid.height(); cell++) {
				if (!grid.passable(cell) || grid.occupied(cell)) continue;
				int d = Math.abs(cell % grid.width() - tx) + Math.abs(cell / grid.width() - ty);
				if (d < bestDistance || (d == bestDistance && cell < landing)) { bestDistance = d; landing = cell; }
			}
		}
		return new Plan(cells, landing);
	}
}
