package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class TestTengu extends Mob {

    private static Char throwingChar;

    private int phase = 1;
    private int abilityCooldown = 2;
    private int lastAbility = -1;

    private static final String PHASE = "phase";
    private static final String ABILITY_COOLDOWN = "ability_cooldown";
    private static final String LAST_ABILITY = "last_ability";

    {
        spriteClass = TenguSprite.class;

        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200;
        EXP = 20;
        defenseSkill = 15;

        viewDistance = 12;
        state = WANDERING;
        properties.add(Property.BOSS);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(6, 12);
    }

    @Override
    public int attackSkill(Char target) {
        return Dungeon.level.adjacent(pos, target.pos) ? 10 : 20;
    }

    @Override
    public float attackDelay() {
        return Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
                && Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT)
                ? super.attackDelay() * 2 / 3
                : super.attackDelay();
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 5);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return super.canAttack(enemy)
                || new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    @Override
    protected boolean act() {
        if (!BossHealthBar.isAssigned()) {
            notice();
        }
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
            return super.act();
        }
        if (enemy != null && enemy.isAlive() && state == HUNTING) {
            abilityCooldown--;
            if (abilityCooldown <= 0) {
                useTestAbility(enemy);
                abilityCooldown = phase == 1 ? 4 : 3;
                spend(TICK);
                return true;
            }
        }
        return super.act();
    }

    private void useTestAbility(Char target) {
        int ability = Random.Int(4);
        if (ability == lastAbility) {
            ability = (ability + 1) % 4;
        }
        lastAbility = ability;
        switch (ability) {
            case 0:
                throwBomb(this, target);
                break;
            case 1:
                throwFire(this, target);
                break;
            case 2:
                throwShocker(this, target);
                break;
            default:
                placeVisiblePoisonTrap(target);
                break;
        }
    }

    private void placeVisiblePoisonTrap(Char target) {
        int trapPos = -1;
        for (int i = 0; i < 40; i++) {
            int cell = target.pos + PathFinder.NEIGHBOURS8[Random.Int(PathFinder.NEIGHBOURS8.length)];
            if (TestBossUtil.canUseCell(cell)) {
                trapPos = cell;
                break;
            }
        }
        if (trapPos == -1) {
            trapPos = target.pos;
        }
        Level.set(trapPos, Terrain.TRAP);
        Dungeon.level.setTrap(new TestPoisonDartTrap().reveal(), trapPos);
        CellEmitter.get(trapPos).burst(Speck.factory(Speck.STEAM), 4);
    }

    @Override
    public void damage(int dmg, Object src, DamageTag... damageTags) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        if (!BossHealthBar.isAssigned()) {
            notice();
        }
        int preHP = HP;
        super.damage(dmg, src, damageTags);
        int dmgTaken = preHP - HP;
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmgTaken);
        }
        if (phase == 1 && HP <= HT / 2 && isAlive()) {
            HP = Math.max(HP, HT / 2);
            phase = 2;
            yell(Messages.get(this, "phase"));
            jumpNearTarget();
            BossHealthBar.bleed(true);
        }
    }

    private void jumpNearTarget() {
        Char target = TestBossUtil.firstEnemy(this);
        int center = target == null ? pos : target.pos;
        int newPos = TestBossUtil.randomSpawnCellNear(center, 2, 6);
        if (newPos != -1) {
            if (sprite != null) {
                sprite.move(pos, newPos);
            }
            move(newPos);
            Dungeon.level.occupyCell(this);
        }
    }

    @Override
    public void die(Object cause) {
        for (Blob blob : Dungeon.level.blobs.values()) {
            if (blob instanceof Fire) {
                blob.clear(0);
            }
        }
        ArrayList<Item> items = Dungeon.level.getItemsToPreserveFromSealedResurrect();

        for (Item i : items.toArray(new Item[0])){
            if (i instanceof TestBombAbility.TestBombItem || i instanceof TestShockerAbility.TestShockerItem){
                items.remove(i);
            }
        }
        TestBossUtil.bossSlain(this);
        super.die(cause);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
        yell(Messages.get(this, "notice"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase);
        bundle.put(ABILITY_COOLDOWN, abilityCooldown);
        bundle.put(LAST_ABILITY, lastAbility);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
        lastAbility = bundle.getInt(LAST_ABILITY);
        BossHealthBar.assignBoss(this);
        if (phase >= 2) {
            BossHealthBar.bleed(true);
        }
    }

    // ======================== Ability: Bomb ========================

    public static boolean throwBomb(final Char thrower, final Char target) {
        int targetCell = -1;

        for (int i : PathFinder.NEIGHBOURS8){
            int cell = target.pos + i;
            boolean bombHere = false;
            for (TestBombAbility b : thrower.buffs(TestBombAbility.class)){
                if (b.bombPos == cell){
                    bombHere = true;
                }
            }
            if (!bombHere && !Dungeon.level.solid[cell] &&
                    (targetCell == -1 || Dungeon.level.trueDistance(cell, thrower.pos) < Dungeon.level.trueDistance(targetCell, thrower.pos))){
                targetCell = cell;
            }
        }

        if (targetCell == -1){
            return false;
        }

        final int finalTargetCell = targetCell;
        throwingChar = thrower;
        final TestBombAbility.TestBombItem item = new TestBombAbility.TestBombItem();
        thrower.sprite.zap(finalTargetCell);
        ((MissileSprite) thrower.sprite.parent.recycle(MissileSprite.class)).
                reset(thrower.sprite,
                        finalTargetCell,
                        item,
                        new Callback() {
                            @Override
                            public void call() {
                                item.onThrow(finalTargetCell);
                                thrower.next();
                            }
                        });
        return true;
    }

    public static class TestBombAbility extends Buff {

        public int bombPos = -1;
        private int timer = 3;

        private ArrayList<Emitter> smokeEmitters = new ArrayList<>();

        @Override
        public boolean act() {

            if (smokeEmitters.isEmpty()){
                fx(true);
            }

            PointF p = DungeonTilemap.raisedTileCenterToWorld(bombPos);
            if (timer == 3) {
                FloatingText.show(p.x, p.y, bombPos, "3...", CharSprite.WARNING);
            } else if (timer == 2){
                FloatingText.show(p.x, p.y, bombPos, "2...", CharSprite.WARNING);
            } else if (timer == 1){
                FloatingText.show(p.x, p.y, bombPos, "1...", CharSprite.WARNING);
            } else {
                PathFinder.buildDistanceMap( bombPos, BArray.not( Dungeon.level.solid, null ), 2 );
                for (int cell = 0; cell < PathFinder.distance.length; cell++) {

                    if (PathFinder.distance[cell] < Integer.MAX_VALUE) {
                        Char ch = Actor.findChar(cell);
                        if (ch != null && !(ch instanceof TestTengu)) {
                            int dmg = Random.NormalIntRange(5 + Dungeon.scalingDepth(), 10 + Dungeon.scalingDepth() * 2);
                            dmg -= ch.drRoll();

                            if (dmg > 0) {
                                ch.damage(dmg, Bomb.class, DamageTag.PHYSICAL);
                            }

                            if (ch == Dungeon.hero){
                                Statistics.qualifiedForBossChallengeBadge = false;
                                Statistics.bossScores[1] -= 100;

                                if (!ch.isAlive()) {
                                    Dungeon.fail(TestTengu.class);
                                }
                            }
                        }
                    }

                }

                Heap h = Dungeon.level.heaps.get(bombPos);
                if (h != null) {
                    for (Item i : h.items.toArray(new Item[0])) {
                        if (i instanceof TestBombItem) {
                            h.remove(i);
                        }
                    }
                }
                Sample.INSTANCE.play(Assets.Sounds.BLAST);
                detach();
                return true;
            }

            timer--;
            spend(TICK);
            return true;
        }

        @Override
        public void fx(boolean on) {
            if (on && bombPos != -1){
                PathFinder.buildDistanceMap( bombPos, BArray.not( Dungeon.level.solid, null ), 2 );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                        Emitter e = CellEmitter.get(i);
                        e.pour( SmokeParticle.FACTORY, 0.25f );
                        smokeEmitters.add(e);
                    }
                }
            } else if (!on) {
                for (Emitter e : smokeEmitters){
                    e.burst(BlastParticle.FACTORY, 2);
                }
            }
        }

        private static final String BOMB_POS = "bomb_pos";
        private static final String TIMER = "timer";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( BOMB_POS, bombPos );
            bundle.put( TIMER, timer );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            bombPos = bundle.getInt( BOMB_POS );
            timer = bundle.getInt( TIMER );
        }

        public static class TestBombItem extends Item {

            {
                dropsDownHeap = true;
                unique = true;

                image = ItemSpriteSheet.TENGU_BOMB;
            }

            @Override
            public boolean doPickUp(Hero hero, int pos) {
                GLog.w( Messages.get(this, "cant_pickup") );
                return false;
            }

            @Override
            protected void onThrow(int cell) {
                super.onThrow(cell);
                if (throwingChar != null){
                    Buff.append(throwingChar, TestBombAbility.class).bombPos = cell;
                    throwingChar = null;
                } else {
                    Buff.append(curUser, TestBombAbility.class).bombPos = cell;
                }
            }

        }

    }

    // ======================== Ability: Fire ========================

    public static boolean throwFire(final Char thrower, final Char target) {
        Ballistica aim = new Ballistica(thrower.pos, target.pos, Ballistica.WONT_STOP);

        for (int i = 0; i < PathFinder.CIRCLE8.length; i++){
            if (aim.sourcePos+PathFinder.CIRCLE8[i] == aim.path.get(1)){
                thrower.sprite.zap(target.pos);
                Buff.append(thrower, TestFireAbility.class).direction = i;

                thrower.sprite.emitter().start(Speck.factory(Speck.STEAM), .03f, 10);
                return true;
            }
        }

        return false;
    }

    public static class TestFireAbility extends Buff {

        public int direction;
        private int[] curCells;

        HashSet<Integer> toCells = new HashSet<>();

        @Override
        public boolean act() {

            toCells.clear();

            if (curCells == null){
                curCells = new int[1];
                curCells[0] = target.pos;
                spreadFromCell( curCells[0] );

            } else {
                for (Integer c : curCells) {
                    if (TestFireBlob.volumeAt(c, TestFireBlob.class) > 0) spreadFromCell(c);
                }
            }

            for (Integer c : curCells){
                toCells.remove(c);
            }

            if (toCells.isEmpty()){
                detach();
            } else {
                curCells = new int[toCells.size()];
                int i = 0;
                for (Integer c : toCells){
                    GameScene.add(Blob.seed(c, 2, TestFireBlob.class));
                    curCells[i] = c;
                    i++;
                }
            }

            spend(TICK);
            return true;
        }

        private void spreadFromCell( int cell ){
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[left(direction)]]){
                toCells.add(cell + PathFinder.CIRCLE8[left(direction)]);
            }
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[direction]]){
                toCells.add(cell + PathFinder.CIRCLE8[direction]);
            }
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[right(direction)]]){
                toCells.add(cell + PathFinder.CIRCLE8[right(direction)]);
            }
        }

        private int left(int direction){
            return direction == 0 ? 7 : direction-1;
        }

        private int right(int direction){
            return direction == 7 ? 0 : direction+1;
        }

        private static final String DIRECTION = "direction";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( DIRECTION, direction );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            direction = bundle.getInt( DIRECTION );
        }

        public static class TestFireBlob extends Blob {

            {
                actPriority = BUFF_PRIO - 1;
                alwaysVisible = true;
            }

            @Override
            protected void evolve() {

                boolean observe = false;
                boolean burned = false;

                int cell;
                for (int i = area.left; i < area.right; i++){
                    for (int j = area.top; j < area.bottom; j++){
                        cell = i + j* Dungeon.level.width();
                        off[cell] = (int)GameMath.gate(0, cur[cell] - 1, 1);

                        if (off[cell] > 0) {
                            volume += off[cell];
                        }

                        if (cur[cell] > 0 && off[cell] == 0){

                            //similar to fire.burn(), but TestTengu is immune, and hero loses score
                            Char ch = Actor.findChar( cell );
                            if (ch != null && !ch.isImmune(Fire.class) && !(ch instanceof TestTengu)) {
                                Buff.affect( ch, Burning.class ).reignite( ch );
                            }
                            if (ch == Dungeon.hero){
                                Statistics.qualifiedForBossChallengeBadge = false;
                                Statistics.bossScores[1] -= 100;
                            }

                            Heap heap = Dungeon.level.heaps.get( cell );
                            if (heap != null) {
                                heap.burn();
                            }

                            Plant plant = Dungeon.level.plants.get( cell );
                            if (plant != null){
                                plant.wither();
                            }

                            if (Dungeon.level.flamable[cell]){
                                Dungeon.level.destroy( cell );

                                observe = true;
                                GameScene.updateMap( cell );
                            }

                            burned = true;
                            CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.03f, 10);
                        }
                    }
                }

                if (observe) {
                    Dungeon.observe();
                }

                if (burned){
                    Sample.INSTANCE.play(Assets.Sounds.BURNING);
                }
            }

            @Override
            public void use(BlobEmitter emitter) {
                super.use(emitter);
                emitter.pour( FlameParticle.FACTORY, 0.15f );
            }

            @Override
            public String tileDesc() {
                return Messages.get(this, "desc");
            }
        }

    }

    // ======================== Ability: Shocker ========================

    public static boolean throwShocker(final Char thrower, final Char target) {
        int targetCell = -1;

        for (int i : PathFinder.NEIGHBOURS8){
            int cell = target.pos + i;
            if (Dungeon.level.distance(cell, thrower.pos) >= 2 && !Dungeon.level.solid[cell]){
                boolean validTarget = true;
                for (TestShockerAbility s : thrower.buffs(TestShockerAbility.class)){
                    if (Dungeon.level.distance(cell, s.shockerPos) < 2){
                        validTarget = false;
                        break;
                    }
                }
                if (validTarget && Dungeon.level.trueDistance(cell, thrower.pos) < Dungeon.level.trueDistance(targetCell, thrower.pos)){
                    targetCell = cell;
                }
            }
        }

        if (targetCell == -1){
            return false;
        }

        final int finalTargetCell = targetCell;
        throwingChar = thrower;
        final TestShockerAbility.TestShockerItem item = new TestShockerAbility.TestShockerItem();
        thrower.sprite.zap(finalTargetCell);
        ((MissileSprite) thrower.sprite.parent.recycle(MissileSprite.class)).
                reset(thrower.sprite,
                        finalTargetCell,
                        item,
                        new Callback() {
                            @Override
                            public void call() {
                                item.onThrow(finalTargetCell);
                                thrower.next();
                            }
                        });
        return true;
    }

    public static class TestShockerAbility extends Buff {

        public int shockerPos;
        private Boolean shockingOrdinals = null;

        @Override
        public boolean act() {

            if (shockingOrdinals == null){
                shockingOrdinals = Random.Int(2) == 1;

                spreadblob();
            } else if (shockingOrdinals){

                target.sprite.parent.add(new Lightning(shockerPos - 1 - Dungeon.level.width(), shockerPos + 1 + Dungeon.level.width(), null));
                target.sprite.parent.add(new Lightning(shockerPos - 1 + Dungeon.level.width(), shockerPos + 1 - Dungeon.level.width(), null));

                if (Dungeon.level.distance(Dungeon.hero.pos, shockerPos) <= 1){
                    Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
                }

                shockingOrdinals = false;
                spreadblob();
            } else {

                target.sprite.parent.add(new Lightning(shockerPos - Dungeon.level.width(), shockerPos + Dungeon.level.width(), null));
                target.sprite.parent.add(new Lightning(shockerPos - 1, shockerPos + 1, null));

                if (Dungeon.level.distance(Dungeon.hero.pos, shockerPos) <= 1){
                    Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
                }

                shockingOrdinals = true;
                spreadblob();
            }

            spend(TICK);
            return true;
        }

        private void spreadblob(){
            GameScene.add(Blob.seed(shockerPos, 1, TestShockerBlob.class));
            for (int i = shockingOrdinals ? 0 : 1; i < PathFinder.CIRCLE8.length; i += 2){
                if (!Dungeon.level.solid[shockerPos+PathFinder.CIRCLE8[i]]) {
                    GameScene.add(Blob.seed(shockerPos + PathFinder.CIRCLE8[i], 2, TestShockerBlob.class));
                }
            }
        }

        private static final String SHOCKER_POS = "shocker_pos";
        private static final String SHOCKING_ORDINALS = "shocking_ordinals";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( SHOCKER_POS, shockerPos );
            if (shockingOrdinals != null) bundle.put( SHOCKING_ORDINALS, shockingOrdinals );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            shockerPos = bundle.getInt( SHOCKER_POS );
            if (bundle.contains(SHOCKING_ORDINALS)) shockingOrdinals = bundle.getBoolean( SHOCKING_ORDINALS );
        }

        public static class TestShockerBlob extends Blob {

            {
                actPriority = BUFF_PRIO - 1;
                alwaysVisible = true;
            }

            @Override
            protected void evolve() {

                boolean shocked = false;

                int cell;
                for (int i = area.left; i < area.right; i++){
                    for (int j = area.top; j < area.bottom; j++){
                        cell = i + j* Dungeon.level.width();
                        off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;

                        if (off[cell] > 0) {
                            volume += off[cell];
                        }

                        if (cur[cell] > 0 && off[cell] == 0){

                            shocked = true;

                            Char ch = Actor.findChar(cell);
                            if (ch != null && !(ch instanceof TestTengu)){
                                ch.damage(2 + Dungeon.scalingDepth(), new Electricity(), DamageTag.PHYSICAL, DamageTag.ELECTRIC);

                                if (ch == Dungeon.hero){
                                    Statistics.qualifiedForBossChallengeBadge = false;
                                    Statistics.bossScores[1] -= 100;
                                    if (!ch.isAlive()) {
                                        Dungeon.fail(TestTengu.class);
                                        GLog.n(Messages.get(Electricity.class, "ondeath"));
                                    }
                                }
                            }

                        }
                    }
                }

                if (shocked) Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );

            }

            @Override
            public void use(BlobEmitter emitter) {
                super.use(emitter);

                emitter.pour( SparkParticle.STATIC, 0.10f );
            }

            @Override
            public String tileDesc() {
                return Messages.get(this, "desc");
            }
        }

        public static class TestShockerItem extends Item {

            {
                dropsDownHeap = true;
                unique = true;

                image = ItemSpriteSheet.TENGU_SHOCKER;
            }

            @Override
            public boolean doPickUp(Hero hero, int pos) {
                GLog.w( Messages.get(this, "cant_pickup") );
                return false;
            }

            @Override
            protected void onThrow(int cell) {
                super.onThrow(cell);
                if (throwingChar != null){
                    Buff.append(throwingChar, TestShockerAbility.class).shockerPos = cell;
                    throwingChar = null;
                } else {
                    Buff.append(curUser, TestShockerAbility.class).shockerPos = cell;
                }
            }

        }

    }
}
