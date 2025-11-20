package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DiedTenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;

public class PrisonNewBossLevel extends Level{
    {
        color1 = 0x6a723d;
        color2 = 0x88924c;

        viewDistance = 5;
    }

    public enum State {
        START,
        FIGHT,
        WON
    }

    public State state;
    private RogueBoss rogue;

    @Override
    public void playLevelMusic() {
        if (state == State.START){
            Music.INSTANCE.end();
        } else if (state == State.WON) {
            Music.INSTANCE.playTracks(PrisonLevel.PRISON_TRACK_LIST, PrisonLevel.PRISON_TRACK_CHANCES, false);
        } else {
            Music.INSTANCE.play(Assets.Music.PRISON_BOSS, true);
        }
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_PRISON;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_PRISON;
    }
    private static final String STATE	        = "state";
    private static final String ROGUE	        = "rogue";
    private static final String TENGU	        = "tengu";
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( STATE, state );
        bundle.put( ROGUE, rogue );
        //bundle.put( TENGU, tengu );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        state = bundle.getEnum( STATE, State.class );

        //in some states tengu won't be in the world, in others he will be.
        if (state == State.START) {
            rogue = (RogueBoss) bundle.get( ROGUE );
            if(rogue==null){
                for (Mob mob : mobs){
                    if (mob instanceof RogueBoss) {
                        rogue = (RogueBoss) mob;
                        break;
                    }
                }
            }
        } else {
            for (Mob mob : mobs){
                if (mob instanceof RogueBoss) {
                    rogue = (RogueBoss) mob;
                    break;
                }
            }
        }


    }

    private static final int ENTRANCE_POS = 10 + 27*32;
    private static final Rect entranceRoom = new Rect(8, 2, 13, 8);
    private static final Rect arena = new Rect(3, 1, 18, 16);
    private static final Rect startHallway = new Rect(9, 7, 12, 24);
    private static final Rect[] startCells = new Rect[]{ new Rect(5, 9, 10, 16), new Rect(11, 9, 16, 16),
            new Rect(5, 15, 10, 22), new Rect(11, 15, 16, 22)};
    private static final Rect tenguCell = new Rect(6, 23, 15, 32);
    private static final Point tenguCellCenter = new Point(10, 27);
    private static final Point tenguCellDoor = new Point(10, 23);
    private static final Point[] startTorches = new Point[]{ new Point(10, 2),
            new Point(7, 9), new Point(13, 9),
            new Point(7, 15), new Point(13, 15),
            new Point(8, 23), new Point(12, 23)};
    private static final Point endStart = new Point( startHallway.left+2, startHallway.top+2);
    private static final Point levelExit = new Point( endStart.x+11, endStart.y+6);

    private static int W = Terrain.WALL;
    private static int D = Terrain.WALL_DECO;
    private static int e = Terrain.EMPTY;
    private static int E = Terrain.EXIT;
    private static int C = Terrain.CHASM;
    private static final int[] endMap = new int[]{
            W, W, D, W, W, W, W, W, W, W, W, W, W, W,
            W, e, e, e, W, W, W, W, W, W, W, W, W, W,
            W, e, e, e, e, e, e, e, e, W, W, W, W, W,
            e, e, e, e, e, e, e, e, e, e, e, e, W, W,
            e, e, e, e, e, e, e, e, e, e, e, e, e, W,
            e, e, e, C, C, C, C, C, C, C, C, e, e, W,
            e, W, C, C, C, C, C, C, C, C, C, E, E, W,
            e, e, e, C, C, C, C, C, C, C, C, E, E, W,
            e, e, e, e, e, C, C, C, C, C, C, E, E, W,
            e, e, e, e, e, e, e, W, W, W, C, C, C, W,
            W, e, e, e, e, e, W, W, W, W, C, C, C, W,
            W, e, e, e, e, W, W, W, W, W, W, C, C, W,
            W, W, W, W, W, W, W, W, W, W, W, C, C, W,
            W, W, W, W, W, W, W, W, W, W, W, C, C, W,
            W, D, W, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            e, e, e, W, W, W, W, W, W, W, W, C, C, W,
            W, W, W, W, W, W, W, W, W, W, W, C, C, W
    };
    @Override
    protected boolean build() {
        setSize(32, 32);
        state = State.START;
        setMapStart();
        return true;
    }
    private void setMapStart() {
        transitions.add(new LevelTransition(this, ENTRANCE_POS, LevelTransition.Type.REGULAR_ENTRANCE));

        Painter.fill(this, 0, 0, 32, 32, Terrain.WALL);

        Painter.fill(this, 0, 0, 32, 32, Terrain.WALL);

        Painter.fill(this, entranceRoom, Terrain.WALL);
        Painter.fill(this, entranceRoom, 1, Terrain.EMPTY);

        Painter.fill(this, startHallway, Terrain.WALL);
        Painter.fill(this, startHallway, 1, Terrain.EMPTY);

        Painter.set(this, startHallway.left+1, startHallway.top, Terrain.LOCKED_DOOR);

        for (Rect r : startCells){
            Painter.fill(this, r, Terrain.WALL);
            Painter.fill(this, r, 1, Terrain.EMPTY);
        }

        Painter.set(this, startHallway.left, startHallway.top+5, Terrain.DOOR);
        Painter.set(this, startHallway.right-1, startHallway.top+5, Terrain.DOOR);
        Painter.set(this, startHallway.left, startHallway.top+11, Terrain.DOOR);
        Painter.set(this, startHallway.right-1, startHallway.top+11, Terrain.DOOR);

        Painter.fill(this, tenguCell, Terrain.WALL);
        Painter.fill(this, tenguCell, 1, Terrain.EMPTY);

        Painter.set(this, ENTRANCE_POS, Terrain.ENTRANCE);

        Painter.set(this, tenguCell.left+4, tenguCell.top, Terrain.DOOR);

        for (Point p : startTorches){
            Painter.set(this, p, Terrain.WALL_DECO);
        }

        addCagesToCells();

        //we set up the exit for consistently with other levels, even though it's in the walls
        LevelTransition exit = new LevelTransition(this, pointToCell(levelExit), LevelTransition.Type.REGULAR_EXIT);
        exit.right+=2;
        exit.bottom+=3;
        transitions.add(exit);

    }

    private void setMapArena(){

        Dungeon.hero.interrupt();

        clearEntities(pauseSafeArea);

        transitions.clear();

        Painter.fill(this, 0, 0, 32, 32, Terrain.WALL);

        Painter.fill(this, arena, Terrain.WALL);
        Painter.fillEllipse(this, arena, 1, Terrain.EMPTY);
        cleanMapState();

        rogue.pos = Dungeon.hero.pos - 2 * 32;
        rogue.state = rogue.WANDERING;
        GameScene.add(rogue);

        GameScene.flash(0x80FFFFFF);
        Sample.INSTANCE.play(Assets.Sounds.BLAST);


    }
    private void setMapEnd(){

        Painter.fill(this, 0, 0, 32, 32, Terrain.WALL);

        setMapStart();

        for (Heap h : heaps.valueList()){
            if (h.peek() instanceof IronKey){
                h.destroy();
            }
        }

        CustomTilemap vis = new PrisonBossLevel.ExitVisual();
        vis.pos(11, 10);
        customTiles.add(vis);
        GameScene.add(vis, false);

        vis = new PrisonBossLevel.ExitVisualWalls();
        vis.pos(11, 10);
        customWalls.add(vis);
        GameScene.add(vis, true);

        Painter.set(this, tenguCell.left+4, tenguCell.top, Terrain.DOOR);

        int cell = pointToCell(endStart);
        int i = 0;
        while (cell < length()){
            System.arraycopy(endMap, i, map, cell, 14);
            i += 14;
            cell += width();
        }

        //pre-2.5.1 saves, if exit wasn't already added
        if (exit() == entrance()) {
            LevelTransition exit = new LevelTransition(this, pointToCell(levelExit), LevelTransition.Type.REGULAR_EXIT);
            exit.right += 2;
            exit.bottom += 3;
            transitions.add(exit);
        }

        addCagesToCells();
    }

    public void onWon(){

        unseal();
        //Painter.set(this, startHallway.left+1, startHallway.top, Terrain.DOOR);
        Dungeon.hero.interrupt();
        Dungeon.hero.pos = tenguCell.left+4 + (tenguCell.top+2)*width();
        Dungeon.hero.sprite.interruptMotion();
        Dungeon.hero.sprite.place(Dungeon.hero.pos);
        Camera.main.snapTo(Dungeon.hero.sprite.center());

        rogue.pos = pointToCell(tenguCellCenter);
        if(rogue.sprite!=null){
            rogue.sprite.place(rogue.pos);
        }



        //remove all mobs, but preserve allies
        ArrayList<Mob> allies = new ArrayList<>();
        for(Mob m : mobs.toArray(new Mob[0])){
            if (m.alignment == Char.Alignment.ALLY && !m.properties().contains(Char.Property.IMMOVABLE)){
                allies.add(m);
                mobs.remove(m);
            }
        }

        setMapEnd();

        for (Mob m : allies){
            do{
                m.pos = randomTenguCellPos();
            } while (findMob(m.pos) != null || m.pos == Dungeon.hero.pos);
            if (m.sprite != null) m.sprite.place(m.pos);
            mobs.add(m);
        }
        state = PrisonNewBossLevel.State.WON;
        rogue.die(Dungeon.hero);

        clearEntities(tenguCell);
        cleanMapState();


        GameScene.flash(0x80FFFFFF);
        Sample.INSTANCE.play(Assets.Sounds.BLAST);


        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                Music.INSTANCE.fadeOut(5f, new Callback() {
                    @Override
                    public void call() {
                        Music.INSTANCE.end();
                    }
                });
            }
        });
    }

    //randomly places up to 5 cages on tiles that are aside walls (but not torches or doors!)
    public void addCagesToCells(){
        Random.pushGenerator(Dungeon.seedCurDepth());
        for (int i = 0; i < 5; i++){
            int cell = randomPrisonCellPos();
            boolean valid = false;
            for (int j : PathFinder.NEIGHBOURS4){
                if (map[cell+j] == Terrain.WALL){
                    valid = true;
                }
            }
            if (valid){
                Painter.set(this, cell, Terrain.REGION_DECO);
            }
        }

        Random.popGenerator();
    }
    private ArrayList<Item> storedItems = new ArrayList<>();
    private void clearEntities(Rect safeArea){
        for (Heap heap : heaps.valueList()){
            if (safeArea == null || !safeArea.inside(cellToPoint(heap.pos))){
                for (Item item : heap.items){
                    if (!(item instanceof Bomb) || ((Bomb)item).fuse == null){
                        storedItems.add(item);
                    }
                }
                heap.destroy();
            }
        }

        for (HeavyBoomerang.CircleBack b : Dungeon.hero.buffs(HeavyBoomerang.CircleBack.class)){
            if (b.activeDepth() == Dungeon.depth
                    && (safeArea == null || !safeArea.inside(cellToPoint(b.returnPos())))){
                storedItems.add(b.cancel());
            }
        }

        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
            if ((safeArea == null || !safeArea.inside(cellToPoint(mob.pos)))){
                mob.destroy();
                if (mob.sprite != null)
                    mob.sprite.killAndErase();
            }
        }
        for (Plant plant : plants.valueList()){
            if (safeArea == null || !safeArea.inside(cellToPoint(plant.pos))){
                plants.remove(plant.pos);
            }
        }
    }
    private static final Rect pauseSafeArea = new Rect(9, 2, 12, 12);

    @Override
    protected void createMobs() {
        rogue = new RogueBoss();

        DiedTengu tengu = new DiedTengu();
        tengu.pos = 10 + 4*32;
        mobs.add(tengu);
    }

    @Override
    protected void createItems() {
        int pos;
        do {
            pos = randomPrisonCellPos();
        } while (solid[pos]);
        drop(new IronKey(Dungeon.depth), pos);

        do {
            pos = randomPrisonCellPos();
        } while (solid[pos]);
        drop(new Shuriken().quantity(3), pos);
    }
    private int randomPrisonCellPos(){
        Rect room = startCells[Random.Int(startCells.length)];

        return Random.IntRange(room.left+1, room.right-2)
                + width()*Random.IntRange(room.top+1, room.bottom-2);
    }
    public int randomTenguCellPos(){
        return Random.IntRange(tenguCell.left+1, tenguCell.right-2)
                + width()*Random.IntRange(tenguCell.top+1, tenguCell.bottom-2);
    }

    private void cleanMapState(){
        buildFlagMaps();
        cleanWalls();

        BArray.setFalse(visited);
        BArray.setFalse(mapped);

        for (Blob blob: blobs.values()){
            blob.fullyClear();
        }
        addVisuals(); //this also resets existing visuals
        traps.clear();
        GameScene.resetMap();
        Dungeon.observe();
    }

    public static class DiedTengu extends NPC {

        {
            spriteClass = DiedTenguSprite.class;

            properties.add(Property.IMMOVABLE);
        }

        @Override
        protected boolean act() {
            if (Dungeon.hero.buff(AscensionChallenge.class) != null){
                die(null);
                return true;
            }
            return super.act();
        }

        @Override
        public int defenseSkill( Char enemy ) {
            return INFINITE_EVASION;
        }

        @Override
        public void damage( int dmg, Object src ) {
            //do nothing
        }

        @Override
        public boolean add( Buff buff ) {
            return false;
        }

        @Override
        public boolean reset() {
            return true;
        }

        @Override
        public boolean interact(Char c) {
            ((PrisonNewBossLevel)Dungeon.level).setMapArena();
            Buff.affect(c,RogueBoss.BLiness.class);
            die(null);
            return true;
        }
    }

}
