package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.curriculum;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class AgentMinCurriculumLevel extends Level {

	private final AgentMinCurriculum.Course course;
	private final ArrayList<Integer> ratCells = new ArrayList<>();
	private final ArrayList<Integer> foodCells = new ArrayList<>();
	private final ArrayList<Integer> goldCells = new ArrayList<>();
	private final ArrayList<Integer> potionCells = new ArrayList<>();

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
		viewDistance = 8;
	}

	public AgentMinCurriculumLevel(AgentMinCurriculum.Course course) {
		this.course = course;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		ratCells.clear();
		foodCells.clear();
		goldCells.clear();
		potionCells.clear();

		if (course == AgentMinCurriculum.Course.DOOR) {
			return buildDoorLoop();
		}

		String[] layout = layoutFor(course);
		setSize(layout[0].length(), layout.length);

		for (int y = 0; y < layout.length; y++) {
			for (int x = 0; x < layout[y].length(); x++) {
				int cell = y * width() + x;
				switch (layout[y].charAt(x)) {
					case '.':
						map[cell] = Terrain.EMPTY;
						break;
					case '"':
						map[cell] = Terrain.GRASS;
						break;
					case '+':
						map[cell] = Terrain.DOOR;
						break;
					case '/':
						map[cell] = Terrain.OPEN_DOOR;
						break;
					case '@':
						map[cell] = Terrain.ENTRANCE;
						transitions.add(new LevelTransition(this, cell, LevelTransition.Type.REGULAR_ENTRANCE));
						break;
					case '>':
						map[cell] = Terrain.EXIT;
						transitions.add(new LevelTransition(this, cell, LevelTransition.Type.REGULAR_EXIT));
						break;
					case 'r':
						map[cell] = Terrain.EMPTY;
						ratCells.add(cell);
						break;
					case 'f':
						map[cell] = Terrain.EMPTY;
						foodCells.add(cell);
						break;
					case '$':
						map[cell] = Terrain.EMPTY;
						goldCells.add(cell);
						break;
					case 'h':
						map[cell] = Terrain.EMPTY;
						potionCells.add(cell);
						break;
					case '#':
					default:
						map[cell] = Terrain.WALL;
						break;
				}
			}
		}

		return !transitions.isEmpty();
	}

	private boolean buildDoorLoop() {
		setSize(121, 91);

		ArrayList<DoorRoom> rooms = new ArrayList<>();
		int roomCount = Random.IntRange(10, 20);
		float angleOffset = Random.Float((float)(Math.PI * 2));
		for (int i = 0; i < roomCount; i++) {
			DoorRoom room = createLoopRoom(i, roomCount, angleOffset, rooms);
			if (room != null) {
				rooms.add(room);
			}
		}
		if (rooms.size() < 10) {
			return false;
		}

		for (DoorRoom room : rooms) {
			paintDoorRoom(room);
		}

		for (int i = 0; i < rooms.size(); i++) {
			DoorRoom current = rooms.get(i);
			DoorRoom next = rooms.get((i + 1) % rooms.size());
			int currentDoor = randomDoorToward(current, next);
			int nextDoor = randomDoorToward(next, current);
			carveDoorCorridor(current, currentDoor, next, nextDoor);
			map[currentDoor] = Terrain.DOOR;
			map[nextDoor] = Terrain.DOOR;
		}

		DoorRoom startRoom = rooms.get(Random.Int(rooms.size()));
		int entrance = randomInteriorCell(startRoom);
		map[entrance] = Terrain.ENTRANCE;
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));

		return true;
	}

	private DoorRoom createLoopRoom(int index, int roomCount, float angleOffset, ArrayList<DoorRoom> rooms) {
		float baseAngle = angleOffset + (float)(Math.PI * 2 * index / roomCount);
		for (int tries = 0; tries < 32; tries++) {
			float angle = baseAngle + Random.Float(-0.10f, 0.10f);
			int roomWidth = Random.IntRange(6, 10);
			int roomHeight = Random.IntRange(6, 10);
			int centerX = 60 + Math.round((float)Math.cos(angle) * Random.IntRange(39, 50)) + Random.IntRange(-3, 3);
			int centerY = 45 + Math.round((float)Math.sin(angle) * Random.IntRange(26, 36)) + Random.IntRange(-3, 3);
			DoorRoom room = new DoorRoom(
					clamp(centerX - roomWidth / 2, 3, width() - roomWidth - 4),
					clamp(centerY - roomHeight / 2, 3, height() - roomHeight - 4),
					roomWidth,
					roomHeight,
					DoorRoom.Shape.random());
			if (!overlapsExistingRoom(room, rooms, 3)) {
				return room;
			}
		}
		for (int tries = 0; tries < 32; tries++) {
			float angle = baseAngle;
			int roomWidth = Random.IntRange(5, 8);
			int roomHeight = Random.IntRange(5, 8);
			int centerX = 60 + Math.round((float)Math.cos(angle) * Random.IntRange(43, 51));
			int centerY = 45 + Math.round((float)Math.sin(angle) * Random.IntRange(30, 37));
			DoorRoom room = new DoorRoom(
					clamp(centerX - roomWidth / 2, 3, width() - roomWidth - 4),
					clamp(centerY - roomHeight / 2, 3, height() - roomHeight - 4),
					roomWidth,
					roomHeight,
					DoorRoom.Shape.RECT);
			if (!overlapsExistingRoom(room, rooms, 1)) {
				return room;
			}
		}
		return null;
	}

	private boolean overlapsExistingRoom(DoorRoom room, ArrayList<DoorRoom> rooms, int margin) {
		for (DoorRoom other : rooms) {
			if (room.left - margin <= other.right + margin
					&& room.right + margin >= other.left - margin
					&& room.top - margin <= other.bottom + margin
					&& room.bottom + margin >= other.top - margin) {
				return true;
			}
		}
		return false;
	}

	private void paintDoorRoom(DoorRoom room) {
		fillRect(room.left, room.top, room.right, room.bottom, Terrain.WALL);
		switch (room.shape) {
			case ROUND:
				paintRoundRoom(room);
				break;
			case GRASS:
				fillRect(room.left + 1, room.top + 1, room.right - 1, room.bottom - 1, Terrain.EMPTY);
				for (int i = 0; i < Random.IntRange(2, 5); i++) {
					map[cell(Random.IntRange(room.left + 1, room.right - 1),
							Random.IntRange(room.top + 1, room.bottom - 1))] = Terrain.GRASS;
				}
				break;
			case PILLARS:
				fillRect(room.left + 1, room.top + 1, room.right - 1, room.bottom - 1, Terrain.EMPTY);
				if (room.width >= 8 && room.height >= 8) {
					map[cell(room.left + 2, room.top + 2)] = Terrain.STATUE;
					map[cell(room.right - 2, room.top + 2)] = Terrain.STATUE;
					map[cell(room.left + 2, room.bottom - 2)] = Terrain.STATUE;
					map[cell(room.right - 2, room.bottom - 2)] = Terrain.STATUE;
				}
				break;
			case RECT:
			default:
				fillRect(room.left + 1, room.top + 1, room.right - 1, room.bottom - 1, Terrain.EMPTY);
				break;
		}
	}

	private void paintRoundRoom(DoorRoom room) {
		float rx = Math.max(2.5f, (room.width - 2) / 2f);
		float ry = Math.max(2.5f, (room.height - 2) / 2f);
		float cx = (room.left + room.right) / 2f;
		float cy = (room.top + room.bottom) / 2f;
		for (int y = room.top + 1; y <= room.bottom - 1; y++) {
			for (int x = room.left + 1; x <= room.right - 1; x++) {
				float dx = (x - cx) / rx;
				float dy = (y - cy) / ry;
				if (dx * dx + dy * dy <= 1f) {
					map[cell(x, y)] = Terrain.EMPTY;
				}
			}
		}
	}

	private int randomDoorToward(DoorRoom room, DoorRoom target) {
		int dx = target.centerX() - room.centerX();
		int dy = target.centerY() - room.centerY();
		int x;
		int y;
		if (Math.abs(dx) > Math.abs(dy)) {
			x = dx > 0 ? room.right : room.left;
			y = Random.IntRange(room.top + 2, room.bottom - 2);
		} else {
			x = Random.IntRange(room.left + 2, room.right - 2);
			y = dy > 0 ? room.bottom : room.top;
		}
		return cell(clamp(x, 1, width() - 2), clamp(y, 1, height() - 2));
	}

	private void carveDoorCorridor(DoorRoom fromRoom, int fromDoor, DoorRoom toRoom, int toDoor) {
		openDoorInterior(fromRoom, fromDoor);
		openDoorInterior(toRoom, toDoor);
		int fromOutside = outsideDoorCell(fromRoom, fromDoor, 1);
		int fromRun = outsideDoorCell(fromRoom, fromDoor, 3);
		int toOutside = outsideDoorCell(toRoom, toDoor, 1);
		int toRun = outsideDoorCell(toRoom, toDoor, 3);
		map[fromOutside] = Terrain.EMPTY;
		map[toOutside] = Terrain.EMPTY;
		if (Random.Int(2) == 0) {
			carveCorridorSegment(fromOutside, fromRun);
			carveCorridorSegment(fromRun, cell(toRun % width(), fromRun / width()));
			carveCorridorSegment(cell(toRun % width(), fromRun / width()), toRun);
			carveCorridorSegment(toRun, toOutside);
		} else {
			carveCorridorSegment(fromOutside, fromRun);
			carveCorridorSegment(fromRun, cell(fromRun % width(), toRun / width()));
			carveCorridorSegment(cell(fromRun % width(), toRun / width()), toRun);
			carveCorridorSegment(toRun, toOutside);
		}
	}

	private void openDoorInterior(DoorRoom room, int doorCell) {
		int x = doorCell % width();
		int y = doorCell / width();
		if (x == room.left) {
			x++;
		} else if (x == room.right) {
			x--;
		} else if (y == room.top) {
			y++;
		} else {
			y--;
		}
		map[cell(x, y)] = Terrain.EMPTY;
	}

	private int outsideDoorCell(DoorRoom room, int doorCell, int distance) {
		int x = doorCell % width();
		int y = doorCell / width();
		if (x == room.left) {
			x -= distance;
		} else if (x == room.right) {
			x += distance;
		} else if (y == room.top) {
			y -= distance;
		} else {
			y += distance;
		}
		return cell(clamp(x, 1, width() - 2), clamp(y, 1, height() - 2));
	}

	private void carveCorridorSegment(int fromCell, int toCell) {
		int x1 = fromCell % width();
		int y1 = fromCell / width();
		int x2 = toCell % width();
		int y2 = toCell / width();
		if (y1 == y2) {
			carveHorizontal(x1, x2, y1);
		} else if (x1 == x2) {
			carveVertical(y1, y2, x1);
		}
	}

	private void carveHorizontal(int fromX, int toX, int y) {
		int start = Math.min(fromX, toX);
		int end = Math.max(fromX, toX);
		for (int x = start; x <= end; x++) {
			map[cell(x, y)] = Terrain.EMPTY;
		}
	}

	private void carveVertical(int fromY, int toY, int x) {
		int start = Math.min(fromY, toY);
		int end = Math.max(fromY, toY);
		for (int y = start; y <= end; y++) {
			map[cell(x, y)] = Terrain.EMPTY;
		}
	}

	private void fillRect(int left, int top, int right, int bottom, int terrain) {
		for (int y = top; y <= bottom; y++) {
			for (int x = left; x <= right; x++) {
				map[cell(x, y)] = terrain;
			}
		}
	}

	private int cell(int x, int y) {
		return y * width() + x;
	}

	private int randomInteriorCell(DoorRoom room) {
		for (int tries = 0; tries < 64; tries++) {
			int result = cell(Random.IntRange(room.left + 1, room.right - 1),
					Random.IntRange(room.top + 1, room.bottom - 1));
			if (map[result] == Terrain.EMPTY || map[result] == Terrain.GRASS) {
				return result;
			}
		}
		return cell(room.centerX(), room.centerY());
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static class DoorRoom {
		enum Shape {
			RECT,
			ROUND,
			GRASS,
			PILLARS;

			static Shape random() {
				switch (Random.Int(4)) {
					case 0:
						return ROUND;
					case 1:
						return GRASS;
					case 2:
						return PILLARS;
					case 3:
					default:
						return RECT;
				}
			}
		}

		final int left;
		final int top;
		final int right;
		final int bottom;
		final int width;
		final int height;
		final Shape shape;

		DoorRoom(int left, int top, int width, int height, Shape shape) {
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
			this.right = left + width - 1;
			this.bottom = top + height - 1;
			this.shape = shape;
		}

		int centerX() {
			return (left + right) / 2;
		}

		int centerY() {
			return (top + bottom) / 2;
		}
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition != null && transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
			return false;
		}
		return super.activateTransition(hero, transition);
	}

	@Override
	public Mob createMob() {
		TrainingRat rat = new TrainingRat();
		rat.state = rat.WANDERING;
		return rat;
	}

	@Override
	protected void createMobs() {
		for (int cell : ratCells) {
			Mob mob = createMob();
			mob.pos = cell;
			mobs.add(mob);
		}
	}

	@Override
	protected void createItems() {
		for (int cell : foodCells) {
			drop(new Food(), cell);
		}
		for (int cell : goldCells) {
			drop(new Gold(15), cell);
		}
		for (int cell : potionCells) {
			drop(new PotionOfHealing(), cell);
		}
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return entrance();
	}

	private static String[] layoutFor(AgentMinCurriculum.Course course) {
		switch (course) {
			case DOOR:
				return new String[]{
						"###################",
						"#.....#.....#.....#",
						"#.....+.....+.....#",
						"#..@..#.....#.....#",
						"#.....#.....#.....#",
						"###################"
				};
			case PICKUP:
				return new String[]{
						"###################",
						"#.....#...........#",
						"#..@..+....f..$...#",
						"#.....#...........#",
						"###################"
				};
			case COMBAT:
				return new String[]{
						"###################",
						"#.....#...........#",
						"#..@..+..r....h...#",
						"#.....#...........#",
						"###################"
				};
			case DESCEND:
				return new String[]{
						"###################",
						"#.....#...........#",
						"#..@..+.......>...#",
						"#.....#...........#",
						"###################"
				};
			case EXIT_ROOM:
			case NONE:
			default:
				return new String[]{
						"###################",
						"#.....#...........#",
						"#..@..+...........#",
						"#.....#...........#",
						"###################"
				};
		}
	}

	public static class TrainingRat extends Rat {
		{
			HP = HT = 4;
			defenseSkill = 0;
			maxLvl = 50;
		}

		@Override
		public int damageRoll() {
			return 1;
		}

		@Override
		public int attackSkill(Char target) {
			return 4;
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}
}
