package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LandPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PhantomLandPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PhantomPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class PhantomLandPiranha extends LandPiranha{
    {
        spriteClass = PhantomLandPiranhaSprite.class;

        loot = PhantomMeat.class;
        lootChance = 1f;

    }
    @Override
    public void damage(int dmg, Object src) {
        Char dmgSource = null;
        if (src instanceof Char) dmgSource = (Char)src;
        if (src instanceof Wand) dmgSource = Dungeon.hero;

        if (dmgSource == null || !Dungeon.level.adjacent(pos, dmgSource.pos)){
            dmg = Math.round(dmg/2f); //halve damage taken if we are going to teleport
        }
        if(hero.hasTalent(Talent.FISHING_TIME)){
            dmg*=1.3;
        }
        super.damage(dmg, src);
        if (hero.pointsInTalent(Talent.FISHING_TIME)==2){
            return;
        }
        if (isAlive() && !(src instanceof Corruption)) {
            if (dmgSource != null) {
                if (!Dungeon.level.adjacent(pos, dmgSource.pos)) {
                    ArrayList<Integer> candidates = new ArrayList<>();
                    for (int i : PathFinder.NEIGHBOURS8) {
                        if (Actor.findChar( dmgSource.pos + i ) == null && Dungeon.level.passable[dmgSource.pos + i]) {
                            candidates.add(dmgSource.pos + i);
                        }

                    }
                    if (!candidates.isEmpty()) {
                        if (hero.pointsInTalent(Talent.FISHING_TIME)==2){
                            return;
                        }
                        ScrollOfTeleportation.appear(this, Random.element(candidates));
                        aggro(dmgSource);
                    } else {

                        teleportAway();
                    }
                }
            } else {
                teleportAway();
            }
        }
    }
    private boolean teleportAway(){

        if (flying){
            return false;
        }

        ArrayList<Integer> inFOVCandidates = new ArrayList<>();
        ArrayList<Integer> outFOVCandidates = new ArrayList<>();
        for (int i = 0; i < Dungeon.level.length(); i++){
            if (Actor.findChar( i ) == null && Dungeon.level.passable[i]){
                if (Dungeon.level.heroFOV[i]){
                    inFOVCandidates.add(i);
                } else {
                    outFOVCandidates.add(i);
                }
            }
        }

        if (!outFOVCandidates.isEmpty()){

            ScrollOfTeleportation.appear(this, Random.element(outFOVCandidates));
            return true;
        } else if (!inFOVCandidates.isEmpty()){
            ScrollOfTeleportation.appear(this, Random.element(inFOVCandidates));
            return true;
        }

        return false;

    }
}
