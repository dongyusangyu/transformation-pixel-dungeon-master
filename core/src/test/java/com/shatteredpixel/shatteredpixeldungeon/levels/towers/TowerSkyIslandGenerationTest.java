package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.TunnelRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerSkyIslandGenerationTest {

    @Test
    public void skyIslandUsesChasmAsItsUnpaintedBase() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        assertTrue(Arrays.stream(level.map).allMatch(tile -> tile == Terrain.CHASM));
    }

    @Test
    public void onlySkyIslandRemovesTowerTunnelFloor() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        assertEquals(Terrain.CHASM, level.tunnelTile());
        level.feeling = Level.Feeling.CHASM;
        assertEquals(Terrain.EMPTY_SP, level.tunnelTile());
        level.feeling = Level.Feeling.NONE;
        assertEquals(Terrain.EMPTY, level.tunnelTile());
    }

    @Test
    public void skyIslandExtendsOneConnectedLandingCellOutsideTheDoor() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        level.transitions = new ArrayList<LevelTransition>();

        TunnelRoom tunnel = new TunnelRoom();
        tunnel.set(2, 2, 9, 9);
        TestRoom destination = new TestRoom();
        destination.set(0, 3, 2, 7);
        Room.Door door = new Room.Door(2, 5);
        connect(tunnel, destination, door);
        level.map[level.pointToCell(destination.pointInside(door, 1))] = Terrain.EMPTY;

        TowerGenerationRules.retainSkyIslandDoorLandings(
                level, new ArrayList<>(Arrays.asList(tunnel, destination)));

        assertEquals(Terrain.EMPTY_SP, level.map[level.pointToCell(door)]);
        assertEquals(Terrain.EMPTY_SP,
                level.map[level.pointToCell(tunnel.pointInside(door, 1))]);
        assertEquals(Terrain.CHASM,
                level.map[level.pointToCell(tunnel.pointInside(door, 2))]);
    }

    @Test
    public void skyIslandDoesNotCreateALandingWithoutRoomSideFloor() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        level.transitions = new ArrayList<LevelTransition>();

        TunnelRoom tunnel = new TunnelRoom();
        tunnel.set(2, 2, 9, 9);
        TestRoom destination = new TestRoom();
        destination.set(0, 3, 2, 7);
        Room.Door door = new Room.Door(2, 5);
        connect(tunnel, destination, door);

        TowerGenerationRules.retainSkyIslandDoorLandings(
                level, new ArrayList<>(Arrays.asList(tunnel, destination)));

        assertEquals(Terrain.CHASM, level.map[level.pointToCell(door)]);
        assertEquals(Terrain.CHASM,
                level.map[level.pointToCell(tunnel.pointInside(door, 1))]);
    }

    @Test
    public void skyIslandExtendsLandingOutsideAnExistingDoor() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        level.transitions = new ArrayList<LevelTransition>();

        TunnelRoom tunnel = new TunnelRoom();
        tunnel.set(2, 2, 9, 9);
        TestRoom destination = new TestRoom();
        destination.set(0, 3, 2, 7);
        Room.Door door = new Room.Door(2, 5);
        door.set(Room.Door.Type.UNLOCKED);
        connect(tunnel, destination, door);
        level.map[level.pointToCell(door)] = Terrain.DOOR;
        level.map[level.pointToCell(destination.pointInside(door, 1))] = Terrain.EMPTY;

        TowerGenerationRules.retainSkyIslandDoorLandings(
                level, new ArrayList<>(Arrays.asList(tunnel, destination)));

        assertEquals(Terrain.DOOR, level.map[level.pointToCell(door)]);
        assertEquals(Terrain.EMPTY_SP,
                level.map[level.pointToCell(tunnel.pointInside(door, 1))]);
        assertEquals(Terrain.CHASM,
                level.map[level.pointToCell(tunnel.pointInside(door, 2))]);
    }

    @Test
    public void skyIslandDoesNotExtendLandingOutsideAWallConnection() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        level.transitions = new ArrayList<LevelTransition>();

        TunnelRoom tunnel = new TunnelRoom();
        tunnel.set(2, 2, 9, 9);
        TestRoom destination = new TestRoom();
        destination.set(0, 3, 2, 7);
        Room.Door door = new Room.Door(2, 5);
        door.set(Room.Door.Type.WALL);
        connect(tunnel, destination, door);
        level.map[level.pointToCell(door)] = Terrain.WALL;
        level.map[level.pointToCell(destination.pointInside(door, 1))] = Terrain.EMPTY;

        TowerGenerationRules.retainSkyIslandDoorLandings(
                level, new ArrayList<>(Arrays.asList(tunnel, destination)));

        assertEquals(Terrain.WALL, level.map[level.pointToCell(door)]);
        assertEquals(Terrain.CHASM,
                level.map[level.pointToCell(tunnel.pointInside(door, 1))]);
    }

    @Test
    public void skyIslandRestoresSacrificeRoomEntranceAsNormalFloor() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(12, 12);
        level.transitions = new ArrayList<LevelTransition>();

        TunnelRoom tunnel = new TunnelRoom();
        tunnel.set(2, 2, 9, 9);
        SacrificeRoom sacrifice = new SacrificeRoom();
        sacrifice.set(0, 2, 2, 8);
        Room.Door door = new Room.Door(2, 5);
        door.set(Room.Door.Type.TUNNEL);
        connect(tunnel, sacrifice, door);
        level.map[level.pointToCell(sacrifice.pointInside(door, 1))] = Terrain.EMPTY_SP;

        TowerGenerationRules.retainSkyIslandDoorLandings(
                level, new ArrayList<>(Arrays.asList(tunnel, sacrifice)));

        assertEquals(Terrain.EMPTY, level.map[level.pointToCell(door)]);
        assertEquals(Terrain.EMPTY_SP,
                level.map[level.pointToCell(tunnel.pointInside(door, 1))]);
        assertEquals(Terrain.CHASM,
                level.map[level.pointToCell(tunnel.pointInside(door, 2))]);
    }

    private static void connect(Room first, Room second, Room.Door door) {
        first.connected.put(second, door);
        second.connected.put(first, door);
    }

    private static class TestLevel extends Level {
        @Override
        protected boolean build() {
            return true;
        }

        @Override
        protected void createMobs() {
        }

        @Override
        protected void createItems() {
        }
    }

    private static class TestRoom extends Room {
        @Override
        public void paint(Level level) {
        }
    }
}
