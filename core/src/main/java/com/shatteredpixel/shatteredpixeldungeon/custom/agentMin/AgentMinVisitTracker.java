package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import java.util.HashMap;

public class AgentMinVisitTracker {

	private static final HashMap<String, boolean[]> visitedByLevel = new HashMap<>();

	private AgentMinVisitTracker() {
	}

	public static void reset() {
		visitedByLevel.clear();
	}

	public static float onHeroMove(int fromCell, int targetCell) {
		Level level = Dungeon.level;
		if (!AgentMinRewardConfig.ENABLED || level == null || targetCell < 0 || targetCell >= level.length()) {
			return 0f;
		}
		boolean[] visited = visited(level, false);
		if (fromCell >= 0 && fromCell < visited.length) {
			visited[fromCell] = true;
		}
		boolean newCell = !visited[targetCell];
		visited[targetCell] = true;
		return newCell ? AgentMinRewardConfig.AGENT_NEW_CELL_REWARD : AgentMinRewardConfig.AGENT_REVISIT_CELL_PENALTY;
	}

	public static float[][] encodeMatrix(AgentMinState state) {
		int size = AgentMinEncodedState.LOCAL_MAP_SIZE;
		int radius = size / 2;
		float[][] matrix = new float[size][size];
		int width = state == null ? 0 : Math.max(0, state.level.width);
		int height = state == null ? 0 : Math.max(0, state.level.height);
		if (width == 0 || height == 0 || Dungeon.level == null || state.level.heroPos < 0) {
			return matrix;
		}
		boolean[] visited = visited(Dungeon.level, true);
		int heroX = state.level.heroPos % width;
		int heroY = state.level.heroPos / width;
		for (int localY = 0; localY < size; localY++) {
			for (int localX = 0; localX < size; localX++) {
				int mapX = heroX + localX - radius;
				int mapY = heroY + localY - radius;
				if (mapX < 0 || mapY < 0 || mapX >= width || mapY >= height) {
					continue;
				}
				int cell = mapY * width + mapX;
				if (cell >= 0 && cell < visited.length && visited[cell]) {
					matrix[localY][localX] = 1f;
				}
			}
		}
		return matrix;
	}

	private static boolean[] visited(Level level, boolean markHeroPos) {
		String key = Dungeon.depth + ":" + Dungeon.branch + ":" + level.length();
		boolean[] visited = visitedByLevel.get(key);
		if (visited == null || visited.length != level.length()) {
			visited = new boolean[level.length()];
			visitedByLevel.put(key, visited);
		}
		if (markHeroPos && Dungeon.hero != null && Dungeon.hero.pos >= 0 && Dungeon.hero.pos < visited.length) {
			visited[Dungeon.hero.pos] = true;
		}
		return visited;
	}
}
