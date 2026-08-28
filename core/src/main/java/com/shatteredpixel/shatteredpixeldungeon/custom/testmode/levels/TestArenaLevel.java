package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerMobRules;

public class TestArenaLevel extends Level {

	public static final int DEPTH = 30;
	public static final int BRANCH = 2;
	public static final int RADIUS = 15;
	public static final int SIZE = RADIUS * 2 + 3;
	public static final int CENTER = SIZE / 2;

	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_HALLS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}

	@Override
	protected boolean build() {
		setSize(SIZE, SIZE);
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				int dx = x - CENTER;
				int dy = y - CENTER;
				map[x + y * width()] = dx * dx + dy * dy <= RADIUS * RADIUS
						? Terrain.EMPTY
						: Terrain.WALL;
			}
		}
		feeling = Feeling.NONE;
		return true;
	}

	public static int centerCell() {
		return CENTER + CENTER * SIZE;
	}

	public static boolean isLocation(int depth, int branch) {
		return depth == DEPTH && branch == BRANCH;
	}

	public static boolean isCurrentLevel() {
		return isLocation(
				com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth,
				com.shatteredpixel.shatteredpixeldungeon.Dungeon.branch);
	}

	@Override
	public Mob createMob() {
		return TowerMobRules.createNaturalSpawn();
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		int center = centerCell();
		if (Actor.findChar(center) == null) {
			return center;
		}
		for (int cell = 0; cell < length(); cell++) {
			if (passable[cell] && Actor.findChar(cell) == null
					&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])) {
				return cell;
			}
		}
		return -1;
	}
}
