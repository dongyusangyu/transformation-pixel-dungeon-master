package com.shatteredpixel.shatteredpixeldungeon.journal;


import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public enum  TalentCatalog {
    T1,
    T2,
    T3,
    BOSS,
    NEGATIVE;
    public LinkedHashMap<Talent, Integer> entities(){
        return talents;
    }
    public ArrayList<Talent> entities2(){
        return tierTalents;
    }

    public String title(){
        return Messages.get(this, name() + ".title");
    }

    public int totalEntities(){
        return seen.size();
    }



    public int totalSeen(){
        int seenTotal = 0;
        for (boolean entitySeen : seen.values()){
            if (entitySeen) seenTotal++;
        }
        return seenTotal;
    }
    private final LinkedHashMap<Talent, Boolean> seen = new LinkedHashMap<>();
    private final ArrayList<Talent> tierTalents = new ArrayList<>();
    private final LinkedHashMap<Talent, Integer> talents = new LinkedHashMap<Talent, Integer>();

    private void addEntities(Talent... classes ){
        for (Talent cls : classes){
            talents.put(cls, 0);
        }
        for (Talent cls : classes){
            tierTalents.add(cls);
        }
    }
    private void addEntities(int tier){
        for(ArrayList<Talent> talents1: Talent.typeTalent.get(tier)){
            if(!talents1.isEmpty()){
                for(Talent t:talents1){
                    talents.put(t, 0);
                    tierTalents.add(t);
                }
            }
        }
    }
    private void addNegative(){
        for(ArrayList<Talent> talents1: Talent.negativeTalent){
            if(!talents1.isEmpty()){
                for(Talent t:talents1){
                    talents.put(t, 0);
                    tierTalents.add(t);
                }
            }
        }
    }


    static {
        T1.addEntities(0);
        T2.addEntities(1);
        T3.addEntities(2);
        BOSS.addEntities(Talent.AQUATIC_RECOVER,Talent.PUMP_ATTACK,Talent.OOZE_ATTACK, Talent.STRONGEST_SHIELD,Talent.COMBO_PACKAGE,Talent.BREAK_ENEMY_RANKS,
                Talent.SURPRISE_THROW, Talent.SMOKE_MASK,Talent.RUSH, Talent.OVERLOAD_CHARGE,Talent.GAS_SPURT,Talent.FASTING,
                Talent.KING_PROTECT,Talent.SUMMON_FOLLOWER,Talent.WOLFISH_GAZE,Talent.ENERGY_CONVERSION,
                Talent.YOG_LARVA,Talent.YOG_FIST,Talent.YOG_RAY);
        NEGATIVE.addNegative();
    }

    public static int useCount(Talent cls){
        for (TalentCatalog cat : values()) {
            if (cat.talents.containsKey(cls)) {
                return cat.talents.get(cls);
            }
        }
        return 0;
    }
    public static void countUse(Talent cls){
        countUses(cls, 1);
    }
    public static void countUses(Talent cls, int uses){
        for (TalentCatalog cat : values()) {
            if (cat.talents.containsKey(cls) && cat.talents.get(cls) != Integer.MAX_VALUE && !Dungeon.isChallenged(Challenges.TEST_MODE)) {
                cat.talents.put(cls, cat.talents.get(cls)+uses);
                if (cat.talents.get(cls) < -1_000_000_000){ //to catch cases of overflow
                    cat.talents.put(cls, Integer.MAX_VALUE);
                }
                Journal.saveNeeded = true;
            }
        }
    }

    private static final String TALENT_COUNTS = "talent_counts";
    private static final String TALENT_CLASSES = "talent_class";
    public static void store( Bundle bundle ){
        //ArrayList<Integer> talentCounts = new ArrayList<>();
        //ArrayList<Enum<?>> classes = new ArrayList<>();
        Bundle talentCounts = new Bundle();
        for (TalentCatalog cat : values()) {
            for (Talent entity : cat.entities().keySet()) {
                if (cat.talents.get(entity) > 0){
                    talentCounts.put(entity.name(),cat.talents.get(entity));
                }
            }
        }



        bundle.put( TALENT_COUNTS, talentCounts);
    }

    public static void restore( Bundle bundle ){

        if (bundle.contains(TALENT_COUNTS)){
            Bundle talentCounts = bundle.getBundle(TALENT_COUNTS);
            for (String key : talentCounts.getKeys()){
                int value = talentCounts.getInt(key);
                for (TalentCatalog cat : values()){
                    if (cat.talents.containsKey(Talent.valueOf(key))){
                        cat.talents.put(Talent.valueOf(key), value);
                    }
                }

            }
        }

    }











}
