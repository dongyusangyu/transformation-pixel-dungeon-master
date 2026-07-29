package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasureGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.Builder;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.FigureEightBuilder;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.LoopBuilder;
import com.shatteredpixel.shatteredpixeldungeon.levels.builders.RegularBuilder;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ChronoSuccubus;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.DeferredScorpio;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.RaidKeyCarrier;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VaultArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VeilbreakerEye;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms.RaidEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms.RaidExtractionRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms.RaidRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Randomized combat floor for the extraction minigame.
 */
public class ExtractionRaidLevel extends Level {

	public static final int DEPTH = 31;
	public static final int BRANCH = 1;

	private static final String RAID_SEED = "raid_seed";
	private static final String RAID_ID = "raid_id";
	private static final String RAID_ROOM_COUNT = "raid_room_count";
	private static final String ENTRANCE_CELL = "raid_entrance_cell";
	private static final String EXTRACTION_CELL = "raid_extraction_cell";
	private static final String MOB_TARGET = "raid_mob_target";
	private static final String ROOMS = "raid_rooms";

	private long raidSeed = Random.Long(false);
	private long raidId = currentSessionRaidId(raidSeed);
	private int raidRoomCount;
	private int entranceCell;
	private int extractionCell;
	private int mobTarget;

	private ArrayList<Room> rooms;
	private Room roomEntrance;

	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}

	public static boolean isRaidLocation(int depth, int branch) {
		return depth == DEPTH && branch == BRANCH;
	}

	@Override
	protected long levelSeed() {
		return raidSeed;
	}

	public long raidSeed() {
		return raidSeed;
	}

	public long raidId() {
		return raidId;
	}

	public int raidRoomCount() {
		return raidRoomCount;
	}

	public int extractionCell() {
		return extractionCell;
	}

	public int raidMobTarget() {
		return mobTarget;
	}

	public void recordEntranceCell(int cell) {
		entranceCell = cell;
	}

	public void recordExtractionCell(int cell) {
		extractionCell = cell;
	}

	@Override
	public int entrance() {
		return entranceCell;
	}

	@Override
	public int exit() {
		return extractionCell;
	}

	@Override
	protected boolean build() {
		ArrayList<Room> initialRooms = initRooms();
		Random.shuffle(initialRooms);

		Builder builder = builder();
		do {
			for (Room room : initialRooms) {
				room.neigbours.clear();
				room.connected.clear();
			}
			rooms = builder.build((ArrayList<Room>) initialRooms.clone());
		} while (rooms == null);

		boolean painted = painter().paint(this, rooms);
		if (painted) ensureRaidTransitions();
		return painted;
	}

	private void ensureRaidTransitions() {
		if (entranceCell <= 0 || extractionCell <= 0) return;

		map[entranceCell] = Terrain.ENTRANCE;
		map[extractionCell] = Terrain.EXIT;

		boolean hasEntrance = false;
		boolean hasExit = false;
		for (LevelTransition transition : transitions) {
			if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE
					&& transition.cell() == entranceCell) {
				hasEntrance = true;
			} else if (transition.type == LevelTransition.Type.REGULAR_EXIT
					&& transition.cell() == extractionCell) {
				hasExit = true;
			}
		}
		if (!hasEntrance) {
			transitions.add(new LevelTransition(
					this, entranceCell, LevelTransition.Type.REGULAR_ENTRANCE));
		}
		if (!hasExit) {
			transitions.add(new LevelTransition(
					this, extractionCell, LevelTransition.Type.REGULAR_EXIT));
		}
	}

	private ArrayList<Room> initRooms() {
		raidRoomCount = Random.IntRange(12, 15);
		ArrayList<Room> result = new ArrayList<>();
		result.add(roomEntrance = new RaidEntranceRoom());
		result.add(new RaidExtractionRoom());
		for (int i = 2; i < raidRoomCount; i++) {
			result.add(new RaidRoom());
		}
		return result;
	}

	private Builder builder() {
		RegularBuilder result;
		if (Random.Int(2) == 0) {
			result = new LoopBuilder().setLoopShape(
					2, Random.Float(0.15f, 0.65f), Random.Float(0f, 0.5f));
		} else {
			result = new FigureEightBuilder().setLoopShape(
					2, Random.Float(0.3f, 0.8f), 0f);
		}
		return result
				.setPathLength(0.75f, new float[]{1})
				.setTunnelLength(new float[]{1}, new float[]{1})
				.setExtraConnectionChance(0.35f);
	}

	private Painter painter() {
		return new ExtractionRaidPainter()
				.setWater(0.12f, 5)
				.setGrass(0.08f, 3);
	}

	@Override
	public int mobLimit() {
		return mobTarget;
	}

	@Override
	protected void createMobs() {
		mobTarget = Random.IntRange(10, 15);

		ArrayList<Room> spawnRooms = new ArrayList<>();
		for (Room room : rooms) {
			if (room instanceof StandardRoom) {
				for (int i = 0; i < ((StandardRoom) room).mobSpawnWeight(); i++) {
					spawnRooms.add(room);
				}
			}
		}
		Random.shuffle(spawnRooms);

		boolean[] entranceFOV = new boolean[length()];
		Point entrancePoint = cellToPoint(entrance());
		ShadowCaster.castShadow(
				entrancePoint.x, entrancePoint.y, width(), entranceFOV, losBlocking, 8);

		boolean[] entranceWalkable = BArray.not(solid, null);
		PathFinder.buildDistanceMap(entrance(), entranceWalkable, 8);

		Iterator<Room> roomIterator = spawnRooms.iterator();
		int remaining = mobTarget;
		int placementBudget = mobTarget * 100;
		while (remaining > 0 && placementBudget-- > 0) {
			if (!roomIterator.hasNext()) {
				Random.shuffle(spawnRooms);
				roomIterator = spawnRooms.iterator();
			}

			Room room = roomIterator.next();
			Mob mob = createMob();
			int tries = 30;
			do {
				mob.pos = pointToCell(room.random());
				tries--;
			} while (tries >= 0 && !validInitialMobCell(mob, entranceFOV));

			if (tries >= 0) {
				mobs.add(mob);
				remaining--;
			}
		}

		while (remaining > 0) {
			Mob mob = createMob();
			int cell = fallbackSpawnCell(mob, entranceFOV, true);
			if (cell < 0) {
				throw new IllegalStateException("Extraction raid has no valid cell for a required mob");
			}
			mob.pos = cell;
			mobs.add(mob);
			remaining--;
		}

		for (int i = 0; i < 2; i++) {
			VaultArmoredStatue statue = createVaultStatue();
			int cell = randomRespawnCell(statue);
			if (cell < 0) {
				cell = fallbackSpawnCell(statue, entranceFOV, false);
			}
			if (cell < 0) {
				throw new IllegalStateException("Extraction raid has no valid cell for a required statue");
			}
			statue.pos = cell;
			mobs.add(statue);
		}

		ArrayList<Mob> keyCarrierCandidates = new ArrayList<>();
		for (Mob mob : mobs) {
			if (!(mob instanceof VaultArmoredStatue)) {
				keyCarrierCandidates.add(mob);
			}
		}
		if (keyCarrierCandidates.isEmpty()) {
			throw new IllegalStateException("Extraction raid has no ordinary mob for the crystal key");
		}
		Buff.affect(Random.element(keyCarrierCandidates), RaidKeyCarrier.class);
	}

	private boolean validInitialMobCell(Mob mob, boolean[] entranceFOV) {
		return findMob(mob.pos) == null
				&& !entranceFOV[mob.pos]
				&& PathFinder.distance[mob.pos] == Integer.MAX_VALUE
				&& passable[mob.pos]
				&& !solid[mob.pos]
				&& mob.pos != extractionCell
				&& traps.get(mob.pos) == null
				&& plants.get(mob.pos) == null;
	}

	private int fallbackSpawnCell(Char ch, boolean[] entranceFOV, boolean protectEntrance) {
		int start = Random.Int(length());
		for (int offset = 0; offset < length(); offset++) {
			int cell = (start + offset) % length();
			if (!passable[cell]
					|| solid[cell]
					|| findMob(cell) != null
					|| cell == entranceCell
					|| cell == extractionCell
					|| traps.get(cell) != null
					|| plants.get(cell) != null
					|| Char.hasProp(ch, Char.Property.LARGE) && !openSpace[cell]) {
				continue;
			}
			if (protectEntrance
					&& (entranceFOV[cell] || PathFinder.distance[cell] != Integer.MAX_VALUE)) {
				continue;
			}
			return cell;
		}
		return -1;
	}

	protected VaultArmoredStatue createVaultStatue() {
		VaultArmoredStatue statue = new VaultArmoredStatue();
		statue.generateRaidEquipment(raidId);
		return statue;
	}

	@Override
	public Mob createMob() {
		switch (Random.chances(new float[]{4, 3, 3})) {
			case 0:
			default:
				return new ChronoSuccubus();
			case 1:
				return new VeilbreakerEye();
			case 2:
				return new DeferredScorpio();
		}
	}

	@Override
	public int randomRespawnCell(Char ch) {
		for (int tries = 0; tries < 40; tries++) {
			Room room = Random.element(rooms);
			if (room == roomEntrance) {
				continue;
			}
			int cell = pointToCell(room.random());
			if (passable[cell]
					&& !solid[cell]
					&& findMob(cell) == null
					&& cell != extractionCell
					&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])) {
				return cell;
			}
		}
		return -1;
	}

	@Override
	protected void createItems() {
		int treasureCount = Random.IntRange(3, 4);
		for (int i = 0; i < treasureCount; i++) {
			Item treasure = createRaidTreasure().markForExtractionRaid(raidId);
			drop(treasure, nextLootCell());
		}

		int missileCount = Random.IntRange(1, 2);
		for (int i = 0; i < missileCount; i++) {
			MissileWeapon missile = createRaidMissile();
			missile.quantity(3);
			missile.cursed = false;
			missile.enchant(Weapon.Enchantment.random());
			missile.markForExtractionRaid(raidId);
			drop(missile, nextLootCell());
		}

		drop(createRaidGoldenKey(), nextLootCell());
		Heap lockedChest = drop(createRaidCrystalKey(), nextLootCell());
		lockedChest.type = Heap.Type.LOCKED_CHEST;
	}

	protected Treasures createRaidTreasure() {
		return TreasureGenerator.random();
	}

	protected MissileWeapon createRaidMissile() {
		Generator.Category category = missileTierForRoll(Random.Float()) == 4
				? Generator.Category.MIS_T4
				: Generator.Category.MIS_T5;
		return (MissileWeapon) Generator.random(category);
	}

	static int missileTierForRoll(float roll) {
		return roll < 0.2f ? 4 : 5;
	}

	protected Item createRaidGoldenKey() {
		return new GoldenKey(DEPTH);
	}

	protected Item createRaidCrystalKey() {
		return new CrystalKey(DEPTH);
	}

	public Item createCarrierCrystalKey() {
		return createRaidCrystalKey();
	}

	private int nextLootCell() {
		int start = Random.Int(length());
		for (int offset = 0; offset < length(); offset++) {
			int cell = (start + offset) % length();
			if (passable[cell]
					&& !solid[cell]
					&& cell != entranceCell
					&& cell != extractionCell
					&& findMob(cell) == null
					&& heaps.get(cell) == null
					&& traps.get(cell) == null
					&& plants.get(cell) == null) {
				return cell;
			}
		}
		throw new IllegalStateException("Extraction raid has no valid cell for required loot");
	}

	public boolean isHeroAtExtraction() {
		return Dungeon.hero != null && Dungeon.hero.pos == extractionCell;
	}

	public int crystalKeyCount() {
		return Notes.keyCount(new CrystalKey(DEPTH));
	}

	public boolean canExtract() {
		return canExtractWithKeyCount(crystalKeyCount());
	}

	public boolean canExtractWithKeyCount(int keyCount) {
		return Dungeon.hero != null
				&& meetsExtractionConditions(Dungeon.hero.pos, extractionCell, keyCount);
	}

	public static boolean meetsExtractionConditions(
			int heroPos, int requiredExtractionCell, int keyCount) {
		return heroPos == requiredExtractionCell && keyCount > 0;
	}

	public static void clearRaidKeys() {
		while (Notes.remove(new CrystalKey(DEPTH))) {
			// Remove only extraction-floor crystal keys.
		}
		while (Notes.remove(new GoldenKey(DEPTH))) {
			// Remove only extraction-floor golden keys.
		}
	}

	@Override
	public void occupyCell(Char ch) {
		super.occupyCell(ch);
		if (ch == Dungeon.hero && ch.pos == extractionCell) {
			ExtractionRaidRun.showExtractionPrompt(this);
		}
	}

	@Override
	public boolean activateTransition(
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,
			LevelTransition transition) {
		if (transition != null
				&& transition.type == LevelTransition.Type.REGULAR_EXIT
				&& hero != null
				&& hero.pos == extractionCell) {
			ExtractionRaidRun.showExtractionPrompt(this);
		}
		// Raid transitions are interaction markers only; neither one changes floors.
		return false;
	}

	private static long normalizeRaidId(long value) {
		if (value == Long.MIN_VALUE) {
			return Long.MAX_VALUE;
		}
		return Math.max(1L, Math.abs(value));
	}

	private static long currentSessionRaidId(long fallback) {
		if (Dungeon.hero != null) {
			ExtractionRaidRun.RaidSession session =
					Dungeon.hero.buff(ExtractionRaidRun.RaidSession.class);
			if (session != null && session.raidId() != 0) {
				return normalizeRaidId(session.raidId());
			}
		}
		return normalizeRaidId(fallback);
	}

	@Override
	public Actor addRespawner() {
		if (ExtractionRaidRun.hasActiveSession(Dungeon.hero)) {
			Overburden.ensureAttached(Dungeon.hero);
		}
		return null;
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
	public void playLevelMusic() {
		Music.INSTANCE.play(
				Statistics.amuletObtained ? Assets.Music.HALLS_TENSE : Assets.Music.HALLS_1,
				true);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(RAID_SEED, raidSeed);
		bundle.put(RAID_ID, raidId);
		bundle.put(RAID_ROOM_COUNT, raidRoomCount);
		bundle.put(ENTRANCE_CELL, entranceCell);
		bundle.put(EXTRACTION_CELL, extractionCell);
		bundle.put(MOB_TARGET, mobTarget);
		bundle.put(ROOMS, rooms);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		raidSeed = bundle.getLong(RAID_SEED);
		raidId = bundle.contains(RAID_ID)
				? normalizeRaidId(bundle.getLong(RAID_ID))
				: normalizeRaidId(raidSeed);
		raidRoomCount = bundle.getInt(RAID_ROOM_COUNT);
		entranceCell = bundle.getInt(ENTRANCE_CELL);
		extractionCell = bundle.getInt(EXTRACTION_CELL);
		mobTarget = bundle.getInt(MOB_TARGET);
		rooms = new ArrayList<>(
				(Collection<Room>) ((Collection<?>) bundle.getCollection(ROOMS)));
		for (Room room : rooms) {
			room.onLevelLoad(this);
			if (room.isEntrance()) {
				roomEntrance = room;
			}
		}
		ensureRaidTransitions();
	}
}
