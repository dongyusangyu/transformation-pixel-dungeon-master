package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class TestDM300 extends DM300 {
    private final int MIN_COOLDOWN = 5;
    private final int MAX_COOLDOWN = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 7 : 9;

    private int turnsSinceLastAbility = -1;
    private int abilityCooldown = Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN);

    private int lastAbility = 0;
    private static final int NONE = 0;
    private static final int GAS = 1;
    private static final int ROCKS = 2;

    private int superchargeTurns = 0;
    private static final String SUPERCHARGE_TURNS = "supercharge_turns";

    {
        state = WANDERING;
    }

    @Override
    protected Char chooseEnemy() {
        if (supercharged && Dungeon.hero != null && Dungeon.hero.isAlive()) {
            return Dungeon.hero;
        }
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }
    @Override
    protected boolean act() {

        if (paralysed > 0){
            return super.act();
        }

        //ability logic only triggers if DM is not supercharged
        if (!supercharged){
            if (turnsSinceLastAbility >= 0) turnsSinceLastAbility++;

            //in case DM-300 hasn't been able to act yet
            if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
                fieldOfView = new boolean[Dungeon.level.length()];
            }
            Dungeon.level.updateFieldOfView( this, fieldOfView );
            boolean heroVisible = Dungeon.hero.invisible <= 0
                    && Dungeon.level.insideMap(Dungeon.hero.pos)
                    && fieldOfView[Dungeon.hero.pos];

            //determine if DM can reach its enemy
            boolean canReach;
            if (enemy == null || !enemy.isAlive()){
                if (Dungeon.level.adjacent(pos, Dungeon.hero.pos)){
                    canReach = true;
                } else {
                    canReach = (Dungeon.findStep(this, Dungeon.hero.pos, Dungeon.level.openSpace, fieldOfView, true) != -1);
                }
            } else {
                if (Dungeon.level.adjacent(pos, enemy.pos)){
                    canReach = true;
                } else {
                    canReach = (Dungeon.findStep(this, enemy.pos, Dungeon.level.openSpace, fieldOfView, true) != -1);
                }
            }

            if (state != HUNTING){
                if (heroVisible && canReach){
                    beckon(Dungeon.hero.pos);
                }
            } else {

                if ((enemy == null || !enemy.isAlive()) && heroVisible) {
                    enemy = Dungeon.hero;
                }

                //more aggressive ability usage when DM can't reach its target
                if (enemy != null && enemy.isAlive() && !canReach){

                    //try to fire gas at an enemy we can't reach
                    if (turnsSinceLastAbility >= MIN_COOLDOWN){
                        //use a coneAOE to try and account for trickshotting angles
                        ConeAOE aim = new ConeAOE(new Ballistica(pos, enemy.pos, Ballistica.WONT_STOP), Float.POSITIVE_INFINITY, 30, Ballistica.STOP_SOLID);
                        if (aim.cells.contains(enemy.pos) && !Char.hasProp(enemy, Property.INORGANIC)) {
                            lastAbility = GAS;
                            turnsSinceLastAbility = 0;

                            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                                sprite.zap(enemy.pos);
                                return false;
                            } else {
                                ventGas(enemy);
                                Sample.INSTANCE.play(Assets.Sounds.GAS);
                                return true;
                            }
                            //if we can't gas, or if target is inorganic then drop rocks
                            //unless enemy is already stunned, we don't want to stunlock them
                        } else if (enemy.paralysed <= 0) {
                            lastAbility = ROCKS;
                            turnsSinceLastAbility = 0;
                            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                                ((DM300Sprite)sprite).slam(enemy.pos);
                                return false;
                            } else {
                                dropRocks(enemy);
                                Sample.INSTANCE.play(Assets.Sounds.ROCKS);
                                return true;
                            }
                        }

                    }

                } else if (enemy != null && enemy.isAlive() && fieldOfView[enemy.pos]) {
                    if (turnsSinceLastAbility > abilityCooldown) {

                        if (lastAbility == NONE) {
                            //50/50 either ability
                            lastAbility = Random.Int(2) == 0 ? GAS : ROCKS;
                        } else if (lastAbility == GAS) {
                            //more likely to use rocks
                            lastAbility = Random.Int(4) == 0 ? GAS : ROCKS;
                        } else {
                            //more likely to use gas
                            lastAbility = Random.Int(4) != 0 ? GAS : ROCKS;
                        }

                        if (Char.hasProp(enemy, Property.INORGANIC)){
                            lastAbility = ROCKS;
                        }

                        //doesn't spend a turn if enemy is at a distance
                        if (Dungeon.level.adjacent(pos, enemy.pos)){
                            spend(TICK);
                        }

                        turnsSinceLastAbility = 0;
                        abilityCooldown = Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN);

                        if (lastAbility == GAS) {
                            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                                sprite.zap(enemy.pos);
                                return false;
                            } else {
                                ventGas(enemy);
                                Sample.INSTANCE.play(Assets.Sounds.GAS);
                                return true;
                            }
                        } else {
                            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                                ((DM300Sprite)sprite).slam(enemy.pos);
                                return false;
                            } else {
                                dropRocks(enemy);
                                Sample.INSTANCE.play(Assets.Sounds.ROCKS);
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            superchargeTurns--;
            if (superchargeTurns <= 0) {
                loseSupercharge();
            }

            if (!chargeAnnounced){
                yell(Messages.get(this, "supercharged"));
                chargeAnnounced = true;
            }

            enemy = Dungeon.hero;
            target = Dungeon.hero.pos;
            state = HUNTING;

            if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
                fieldOfView = new boolean[Dungeon.level.length()];
            }
            Dungeon.level.updateFieldOfView( this, fieldOfView );
            boolean heroVisible = Dungeon.hero.invisible <= 0
                    && Dungeon.level.insideMap(Dungeon.hero.pos)
                    && fieldOfView[Dungeon.hero.pos];

            if (heroVisible){
                beckon(Dungeon.hero.pos);
            }

        }
        boolean hasVisibleEnemy = TestBossUtil.hasVisibleAttackableEnemy(this);
        if (shouldClearEnemy(supercharged, hasVisibleEnemy)) {
            clearEnemy();
        }


        return super.act();
    }

    static boolean shouldClearEnemy(boolean supercharged, boolean hasVisibleEnemy) {
        return !supercharged && !hasVisibleEnemy;
    }



    @Override
    public void supercharge() {
        supercharged = true;
        pylonsActivated++;
        superchargeTurns = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 12 : 8;

        spend(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 2f : 3f);
        yell(Messages.get(this, "charging"));
        if (sprite != null) {
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
            ((DM300Sprite)sprite).updateChargeState(true);
            ((DM300Sprite)sprite).charge();
        }
        chargeAnnounced = false;
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        super.damage(dmg, src);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SUPERCHARGE_TURNS, superchargeTurns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        superchargeTurns = bundle.getInt(SUPERCHARGE_TURNS);
    }
}
