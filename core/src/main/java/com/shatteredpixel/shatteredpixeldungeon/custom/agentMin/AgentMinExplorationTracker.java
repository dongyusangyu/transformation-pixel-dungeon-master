package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class AgentMinExplorationTracker {

	private static final HashSet<String> discoveredDoors = new HashSet<>();
	private static final HashSet<String> reachedDoors = new HashSet<>();
	private static final HashSet<String> passedDoors = new HashSet<>();
	private static final HashSet<String> visitedRooms = new HashSet<>();
	private static final ArrayDeque<Integer> recentRooms = new ArrayDeque<>();
	private static final ArrayDeque<Integer> recentCells = new ArrayDeque<>();

	private static Object lastHeroRef;
	private static Snapshot snapshot;
	private static int stepsSinceMajorProgress;
	private static int pendingDoorExploreSteps;
	private static int pendingDoorKnownBaseline;
	private static int sameRoomMoveStreak;
	private static int smallAreaLoopStreak;
	private static int frontierLoopStreak;
	private static int lastObservedRoomId = -1;

	private AgentMinExplorationTracker() {
	}

	public static void ensureEpisode() {
		Object hero = Dungeon.hero;
		if (hero != lastHeroRef) {
			reset();
			lastHeroRef = hero;
		}
	}

	public static void reset() {
		discoveredDoors.clear();
		reachedDoors.clear();
		passedDoors.clear();
		visitedRooms.clear();
		recentRooms.clear();
		recentCells.clear();
		snapshot = null;
		stepsSinceMajorProgress = 0;
		pendingDoorExploreSteps = 0;
		pendingDoorKnownBaseline = 0;
		sameRoomMoveStreak = 0;
		smallAreaLoopStreak = 0;
		frontierLoopStreak = 0;
		lastObservedRoomId = -1;
	}

	public static ObserveProgress onObserve(Level level) {
		ensureEpisode();
		ObserveProgress progress = new ObserveProgress();
		if (level == null || Dungeon.hero == null) {
			snapshot = null;
			return progress;
		}
		Snapshot previous = snapshot;
		Snapshot current = Snapshot.build(level, Dungeon.hero.pos);
		snapshot = current;

		for (int doorCell : current.visibleDoors) {
			String key = doorKey(doorCell);
			if (discoveredDoors.add(key)) {
				progress.newDoorCount++;
			}
		}

		if (pendingDoorExploreSteps > 0) {
			int gained = current.knownCount - pendingDoorKnownBaseline;
			if (gained >= AgentMinRewardConfig.POST_DOOR_REVEAL_CELL_THRESHOLD) {
				progress.postDoorReveal = true;
				progress.postDoorRevealCells = gained;
				pendingDoorExploreSteps = 0;
			} else {
				pendingDoorExploreSteps--;
			}
		}

		if (current.heroRoomId >= 0) {
			String roomKey = roomKey(current.heroRoomId);
			if (previous == null) {
				visitedRooms.add(roomKey);
			} else if (visitedRooms.add(roomKey)) {
				progress.enteredNewRoom = true;
				progress.roomId = current.heroRoomId;
				stepsSinceMajorProgress = 0;
			}
			lastObservedRoomId = current.heroRoomId;
			recentRooms.addLast(current.heroRoomId);
			trimHistory(recentRooms, AgentMinRewardConfig.ROOM_STALL_WINDOW);
		}

		progress.frontierDistance = current.frontierDistance;
		return progress;
	}

	public static MoveProgress onMove(Level level, int fromCell, int toCell) {
		ensureEpisode();
		MoveProgress progress = new MoveProgress();
		if (level == null || snapshot == null) {
			return progress;
		}

		stepsSinceMajorProgress++;

		int fromFrontier = snapshot.frontierDistance(fromCell);
		int toFrontier = snapshot.frontierDistance(toCell);
		if (fromFrontier >= 0 && toFrontier >= 0) {
			progress.frontierDelta = fromFrontier - toFrontier;
		}
		recordRecentCell(fromCell, toCell);
		progress.immediateBacktrack = isImmediateBacktrack();
		progress.shortCycle = isShortCycle();
		boolean smallAreaLoop = isSmallAreaLoop();
		if (smallAreaLoop) {
			smallAreaLoopStreak++;
			progress.smallAreaLoop = smallAreaLoopStreak >= AgentMinRewardConfig.SMALL_AREA_LOOP_STREAK_THRESHOLD
					&& smallAreaLoopStreak % AgentMinRewardConfig.LOOP_PENALTY_REPEAT_INTERVAL == 0;
		} else {
			smallAreaLoopStreak = 0;
		}
		if (toFrontier >= 0
				&& toFrontier <= AgentMinRewardConfig.FRONTIER_LOOP_DISTANCE
				&& progress.frontierDelta <= 0
				&& !progress.shortCycle) {
			frontierLoopStreak++;
			progress.frontierLoop = frontierLoopStreak >= AgentMinRewardConfig.FRONTIER_LOOP_STREAK_THRESHOLD
					&& frontierLoopStreak % AgentMinRewardConfig.FRONTIER_LOOP_REPEAT_INTERVAL == 0;
		} else if (progress.frontierDelta > 0 || toFrontier > AgentMinRewardConfig.FRONTIER_LOOP_DISTANCE) {
			frontierLoopStreak = 0;
		}

		int doorCell = doorCrossingCell(level, fromCell, toCell);
		if (doorCell >= 0) {
			String key = doorKey(doorCell);
			if (passedDoors.add(key)) {
				progress.passedNewDoor = true;
				progress.passedDoorCell = doorCell;
				stepsSinceMajorProgress = 0;
				smallAreaLoopStreak = 0;
				frontierLoopStreak = 0;
				pendingDoorExploreSteps = AgentMinRewardConfig.POST_DOOR_REVEAL_STEPS;
				pendingDoorKnownBaseline = snapshot.knownCount;
			}
		}

		int reachedDoor = adjacentUnpassedDoor(level, toCell);
		if (reachedDoor >= 0) {
			String key = doorKey(reachedDoor);
			if (reachedDoors.add(key)) {
				progress.reachedNewDoor = true;
				progress.reachedDoorCell = reachedDoor;
			}
		}

		int moveRoomId = snapshot.roomIdAt(toCell);
		if (moveRoomId >= 0 && moveRoomId == lastObservedRoomId) {
			sameRoomMoveStreak++;
		} else if (moveRoomId >= 0) {
			sameRoomMoveStreak = 0;
		}

		if (sameRoomMoveStreak >= AgentMinRewardConfig.ROOM_STALL_STREAK_THRESHOLD
				&& sameRoomMoveStreak % AgentMinRewardConfig.ROOM_STALL_REPEAT_INTERVAL == 0) {
			progress.roomStall = true;
		}

		if (stepsSinceMajorProgress >= AgentMinRewardConfig.LONG_STALL_THRESHOLD
				&& stepsSinceMajorProgress % AgentMinRewardConfig.LONG_STALL_REPEAT_INTERVAL == 0) {
			progress.longStall = true;
		}

		return progress;
	}

	public static int frontierDistance(AgentMinState state, int cell) {
		if (state == null || cell < 0 || state.level.length <= 0) {
			return -1;
		}
		PlanningSnapshot planning = PlanningSnapshot.build(state);
		return planning.frontierDistance(cell);
	}

	public static boolean isDoorCell(AgentMinState state, int cell) {
		int code = codeAt(state, cell);
		return code != AgentMinState.CELL_UNKNOWN && AgentMinStateBuilder.doorChannelValue(code & 0xFFFF) != 0;
	}

	public static boolean isAdjacentToUnpassedDoor(AgentMinState state, int cell) {
		if (state == null || cell < 0) {
			return false;
		}
		for (int neighbor : neighbors4(cell, state.level.width, state.level.height)) {
			if (neighbor < 0) {
				continue;
			}
			int code = codeAt(state, neighbor);
			if (code == AgentMinState.CELL_UNKNOWN || AgentMinStateBuilder.doorChannelValue(code & 0xFFFF) == 0) {
				continue;
			}
			if (!passedDoors.contains(doorKey(neighbor))) {
				return true;
			}
		}
		return false;
	}

	private static int codeAt(AgentMinState state, int cell) {
		if (state.level.visibleMap != null && cell >= 0 && cell < state.level.visibleMap.length
				&& state.level.visibleMap[cell] != AgentMinState.CELL_UNKNOWN) {
			return state.level.visibleMap[cell];
		}
		if (state.level.exploredMap != null && cell >= 0 && cell < state.level.exploredMap.length) {
			return state.level.exploredMap[cell];
		}
		return AgentMinState.CELL_UNKNOWN;
	}

	private static int doorCrossingCell(Level level, int fromCell, int toCell) {
		if (isDoorTerrain(level.map[toCell])) {
			return toCell;
		}
		if (isDoorTerrain(level.map[fromCell])) {
			return fromCell;
		}
		return -1;
	}

	private static int adjacentUnpassedDoor(Level level, int cell) {
		for (int neighbor : neighbors4(cell, level.width(), level.height())) {
			if (neighbor >= 0 && isDoorTerrain(level.map[neighbor]) && !passedDoors.contains(doorKey(neighbor))) {
				return neighbor;
			}
		}
		return -1;
	}

	private static boolean isDoorTerrain(int terrain) {
		return terrain == Terrain.DOOR
				|| terrain == Terrain.OPEN_DOOR
				|| terrain == Terrain.LOCKED_DOOR
				|| terrain == Terrain.HERO_LKD_DR
				|| terrain == Terrain.CRYSTAL_DOOR;
	}

	private static int[] neighbors4(int cell, int width, int height) {
		int[] out = new int[4];
		int x = cell % width;
		int y = cell / width;
		out[0] = x > 0 ? cell - 1 : -1;
		out[1] = x + 1 < width ? cell + 1 : -1;
		out[2] = y > 0 ? cell - width : -1;
		out[3] = y + 1 < height ? cell + width : -1;
		return out;
	}

	private static String doorKey(int cell) {
		return Dungeon.depth + ":" + Dungeon.branch + ":door:" + cell;
	}

	private static String roomKey(int roomId) {
		return Dungeon.depth + ":" + Dungeon.branch + ":room:" + roomId;
	}

	public static class ObserveProgress {
		public int newDoorCount;
		public boolean enteredNewRoom;
		public int roomId = -1;
		public boolean postDoorReveal;
		public int postDoorRevealCells;
		public int frontierDistance = -1;
	}

	public static class MoveProgress {
		public int frontierDelta;
		public boolean reachedNewDoor;
		public int reachedDoorCell = -1;
		public boolean passedNewDoor;
		public int passedDoorCell = -1;
		public boolean roomStall;
		public boolean longStall;
		public boolean immediateBacktrack;
		public boolean shortCycle;
		public boolean smallAreaLoop;
		public boolean frontierLoop;
	}

	private static void recordRecentCell(int fromCell, int toCell) {
		if (fromCell < 0 || toCell < 0) {
			return;
		}
		if (recentCells.isEmpty()) {
			recentCells.addLast(fromCell);
		}
		recentCells.addLast(toCell);
		trimHistory(recentCells, AgentMinRewardConfig.LOOP_MEMORY_WINDOW);
	}

	private static void trimHistory(ArrayDeque<Integer> history, int configuredLimit) {
		int limit = Math.max(0, configuredLimit);
		while (history.size() > limit && history.pollFirst() != null) {
			// Continue until the configured history limit is reached.
		}
	}

	private static boolean isImmediateBacktrack() {
		Integer[] cells = recentCells.toArray(new Integer[0]);
		int n = cells.length;
		return n >= 3 && cells[n - 1].equals(cells[n - 3]);
	}

	private static boolean isShortCycle() {
		Integer[] cells = recentCells.toArray(new Integer[0]);
		int n = cells.length;
		if (n >= 5 && cells[n - 1].equals(cells[n - 5]) && uniqueLast(cells, 5) <= 4) {
			return true;
		}
		return n >= 6 && cells[n - 1].equals(cells[n - 4]) && uniqueLast(cells, 6) <= 4;
	}

	private static boolean isSmallAreaLoop() {
		Integer[] cells = recentCells.toArray(new Integer[0]);
		return cells.length >= AgentMinRewardConfig.LOOP_MEMORY_WINDOW
				&& uniqueLast(cells, AgentMinRewardConfig.LOOP_MEMORY_WINDOW) <= AgentMinRewardConfig.LOOP_UNIQUE_CELL_THRESHOLD;
	}

	private static int uniqueLast(Integer[] cells, int count) {
		HashSet<Integer> unique = new HashSet<>();
		int start = Math.max(0, cells.length - count);
		for (int i = start; i < cells.length; i++) {
			if (cells[i] != null) {
				unique.add(cells[i]);
			}
		}
		return unique.size();
	}

	private static class Snapshot {
		final int width;
		final int height;
		final int length;
		final boolean[] known;
		final boolean[] traversable;
		final boolean[] frontier;
		final int[] roomIds;
		final int heroRoomId;
		final int frontierDistance;
		final int knownCount;
		final ArrayList<Integer> visibleDoors;

		private Snapshot(int width, int height, int length, boolean[] known, boolean[] traversable,
				boolean[] frontier, int[] roomIds, int heroRoomId, int frontierDistance,
				int knownCount, ArrayList<Integer> visibleDoors) {
			this.width = width;
			this.height = height;
			this.length = length;
			this.known = known;
			this.traversable = traversable;
			this.frontier = frontier;
			this.roomIds = roomIds;
			this.heroRoomId = heroRoomId;
			this.frontierDistance = frontierDistance;
			this.knownCount = knownCount;
			this.visibleDoors = visibleDoors;
		}

		static Snapshot build(Level level, int heroPos) {
			int length = level.length();
			boolean[] known = new boolean[length];
			boolean[] traversable = new boolean[length];
			boolean[] frontier = new boolean[length];
			int[] roomIds = new int[length];
			java.util.Arrays.fill(roomIds, -1);
			ArrayList<Integer> visibleDoors = new ArrayList<>();
			int knownCount = 0;

			for (int cell = 0; cell < length; cell++) {
				known[cell] = level.visited[cell] || level.mapped[cell] || (level.heroFOV != null && level.heroFOV[cell]);
				if (known[cell]) {
					knownCount++;
				}
				traversable[cell] = known[cell] && traversableTerrain(level, cell);
				if (known[cell] && isDoorTerrain(level.map[cell])) {
					visibleDoors.add(cell);
				}
			}

			int nextRoomId = 0;
			int[] queue = new int[length];
			for (int cell = 0; cell < length; cell++) {
				if (!traversable[cell] || isDoorTerrain(level.map[cell]) || roomIds[cell] >= 0) {
					continue;
				}
				int head = 0;
				int tail = 0;
				queue[tail++] = cell;
				roomIds[cell] = nextRoomId;
				while (head < tail) {
					int cur = queue[head++];
					for (int nb : neighbors4(cur, level.width(), level.height())) {
						if (nb < 0 || roomIds[nb] >= 0 || !traversable[nb] || isDoorTerrain(level.map[nb])) {
							continue;
						}
						roomIds[nb] = nextRoomId;
						queue[tail++] = nb;
					}
				}
				nextRoomId++;
			}

			for (int cell = 0; cell < length; cell++) {
				if (!traversable[cell]) {
					continue;
				}
				if (isDoorTerrain(level.map[cell]) && !passedDoors.contains(doorKey(cell))) {
					frontier[cell] = true;
					continue;
				}
				for (int nb : neighbors4(cell, level.width(), level.height())) {
					if (nb >= 0 && known[nb] && isDoorTerrain(level.map[nb]) && !passedDoors.contains(doorKey(nb))) {
						frontier[cell] = true;
						break;
					}
				}
			}

			int heroRoomId = roomIdAt(level, roomIds, heroPos);
			int frontierDistance = bfsDistance(level.width(), level.height(), traversable, frontier, heroPos);
			return new Snapshot(level.width(), level.height(), length, known, traversable, frontier, roomIds,
					heroRoomId, frontierDistance, knownCount, visibleDoors);
		}

		int frontierDistance(int fromCell) {
			return bfsDistance(width, height, traversable, frontier, fromCell);
		}

		int roomIdAt(int cell) {
			if (cell < 0 || cell >= roomIds.length) {
				return -1;
			}
			if (roomIds[cell] >= 0) {
				return roomIds[cell];
			}
			if (isDoorTerrain(Dungeon.level.map[cell])) {
				for (int nb : neighbors4(cell, width, height)) {
					if (nb >= 0 && roomIds[nb] >= 0) {
						return roomIds[nb];
					}
				}
			}
			return -1;
		}

		private static int roomIdAt(Level level, int[] roomIds, int cell) {
			if (cell < 0 || cell >= roomIds.length) {
				return -1;
			}
			if (roomIds[cell] >= 0) {
				return roomIds[cell];
			}
			if (isDoorTerrain(level.map[cell])) {
				for (int nb : neighbors4(cell, level.width(), level.height())) {
					if (nb >= 0 && roomIds[nb] >= 0) {
						return roomIds[nb];
					}
				}
			}
			return -1;
		}
	}

	private static class PlanningSnapshot {
		final int width;
		final int height;
		final boolean[] traversable;
		final boolean[] frontier;

		private PlanningSnapshot(int width, int height, boolean[] traversable, boolean[] frontier) {
			this.width = width;
			this.height = height;
			this.traversable = traversable;
			this.frontier = frontier;
		}

		static PlanningSnapshot build(AgentMinState state) {
			int length = state.level.length;
			boolean[] traversable = new boolean[length];
			boolean[] frontier = new boolean[length];
			for (int cell = 0; cell < length; cell++) {
				int code = codeAt(state, cell);
				boolean known = code != AgentMinState.CELL_UNKNOWN;
				int doorValue = known ? AgentMinStateBuilder.doorChannelValue(code & 0xFFFF) : 0;
				boolean door = doorValue != 0;
				boolean passableDoor = doorValue > 0;
				traversable[cell] = known
						&& (((code & AgentMinState.FLAG_PASSABLE) != 0) || passableDoor)
						&& (((code & AgentMinState.FLAG_SOLID) == 0) || passableDoor);
				if (door && !passedDoors.contains(doorKey(cell))) {
					frontier[cell] = true;
				} else if (traversable[cell]) {
					for (int nb : neighbors4(cell, state.level.width, state.level.height)) {
						if (nb < 0) {
							continue;
						}
						int nbCode = codeAt(state, nb);
						if (nbCode != AgentMinState.CELL_UNKNOWN && AgentMinStateBuilder.doorChannelValue(nbCode & 0xFFFF) != 0
								&& !passedDoors.contains(doorKey(nb))) {
							frontier[cell] = true;
							break;
						}
					}
				}
			}
			return new PlanningSnapshot(state.level.width, state.level.height, traversable, frontier);
		}

		int frontierDistance(int fromCell) {
			return bfsDistance(width, height, traversable, frontier, fromCell);
		}
	}

	private static boolean traversableTerrain(Level level, int cell) {
		int terrain = level.map[cell];
		return (level.passable[cell] && !level.solid[cell])
				|| isDoorTerrain(terrain)
				|| terrain == Terrain.ENTRANCE
				|| terrain == Terrain.EXIT
				|| terrain == Terrain.UNLOCKED_EXIT;
	}

	private static int bfsDistance(int width, int height, boolean[] traversable, boolean[] targets, int start) {
		if (start < 0 || start >= traversable.length || !traversable[start]) {
			return -1;
		}
		if (targets[start]) {
			return 0;
		}
		boolean[] seen = new boolean[traversable.length];
		int[] queue = new int[traversable.length];
		int[] dist = new int[traversable.length];
		int head = 0;
		int tail = 0;
		queue[tail++] = start;
		seen[start] = true;
		while (head < tail) {
			int cell = queue[head++];
			for (int nb : neighbors4(cell, width, height)) {
				if (nb < 0 || seen[nb] || !traversable[nb]) {
					continue;
				}
				dist[nb] = dist[cell] + 1;
				if (targets[nb]) {
					return dist[nb];
				}
				seen[nb] = true;
				queue[tail++] = nb;
			}
		}
		return -1;
	}
}
