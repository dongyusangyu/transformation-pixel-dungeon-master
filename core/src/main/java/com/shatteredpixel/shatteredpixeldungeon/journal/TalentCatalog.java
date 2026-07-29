package com.shatteredpixel.shatteredpixeldungeon.journal;


import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public enum  TalentCatalog {
    T1,
    T2,
    T3,
    BOSS,
    NEGATIVE,
    SUBCLASS,
    ARMOR;
    public LinkedHashMap<Talent, Integer> entities(){
        return talents;
    }
    public ArrayList<Talent> entities2(){
        return tierTalents;
    }

    public ArrayList<Talent> entities(SortMode sortMode){
        ArrayList<Talent> sorted = new ArrayList<>(tierTalents);
        if (sortMode == SortMode.DEFAULT){
            return sorted;
        }
        Collections.sort(sorted, sortMode.comparator(this));
        return sorted;
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
    private final LinkedHashMap<Talent, Integer> metamorphAppearances = new LinkedHashMap<Talent, Integer>();
    private final LinkedHashMap<Talent, Integer> transformSpellTalents = new LinkedHashMap<Talent, Integer>();
    private final LinkedHashMap<Talent, Integer> serverTalents = new LinkedHashMap<Talent, Integer>();
    private final LinkedHashMap<Talent, Integer> serverMetamorphAppearances = new LinkedHashMap<Talent, Integer>();
    private final LinkedHashMap<Talent, Integer> serverTransformSpellTalents = new LinkedHashMap<Talent, Integer>();

    private static final LinkedHashMap<Talent, TalentCatalog> CATALOG_BY_TALENT = new LinkedHashMap<>();

    public enum SortMode {
        DEFAULT("default"),
        LOCAL_SELECTED("local_selected"),
        LOCAL_RATE("local_rate"),
        LOCAL_TARGETED("local_targeted"),
        SERVER_SELECTED("server_selected"),
        SERVER_RATE("server_rate"),
        SERVER_TARGETED("server_targeted");

        private final String messageKey;

        SortMode(String messageKey){
            this.messageKey = messageKey;
        }

        public String title(){
            return Messages.get(TalentCatalog.class, "sort_" + messageKey);
        }

        public SortMode next(){
            SortMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        private Comparator<Talent> comparator(TalentCatalog catalog){
            return new Comparator<Talent>() {
                @Override
                public int compare(Talent lhs, Talent rhs) {
                    int result;
                    switch (SortMode.this){
                        case LOCAL_SELECTED:
                            result = Integer.compare(catalog.talents.get(rhs), catalog.talents.get(lhs));
                            break;
                        case LOCAL_RATE:
                            result = Float.compare(localRate(catalog, rhs), localRate(catalog, lhs));
                            break;
                        case LOCAL_TARGETED:
                            result = Integer.compare(catalog.transformSpellTalents.get(rhs), catalog.transformSpellTalents.get(lhs));
                            break;
                        case SERVER_SELECTED:
                            result = Integer.compare(catalog.serverTalents.get(rhs), catalog.serverTalents.get(lhs));
                            break;
                        case SERVER_RATE:
                            result = Float.compare(serverRate(catalog, rhs), serverRate(catalog, lhs));
                            break;
                        case SERVER_TARGETED:
                            result = Integer.compare(catalog.serverTransformSpellTalents.get(rhs), catalog.serverTransformSpellTalents.get(lhs));
                            break;
                        case DEFAULT:
                        default:
                            result = 0;
                            break;
                    }
                    return result == 0 ? Integer.compare(catalog.tierTalents.indexOf(lhs), catalog.tierTalents.indexOf(rhs)) : result;
                }
            };
        }

        private static float localRate(TalentCatalog catalog, Talent talent){
            int appearances = Math.max(catalog.metamorphAppearances.get(talent), catalog.talents.get(talent));
            return appearances <= 0 ? 0f : (float) catalog.talents.get(talent) / appearances;
        }

        private static float serverRate(TalentCatalog catalog, Talent talent){
            int appearances = Math.max(catalog.serverMetamorphAppearances.get(talent), catalog.serverTalents.get(talent));
            return appearances <= 0 ? 0f : (float) catalog.serverTalents.get(talent) / appearances;
        }
    }

    private void registerTalent(Talent talent) {
        if (Talent.forbiddenInCatalogOrMetamorphosis(talent) || talents.containsKey(talent)) {
            return;
        }
        talents.put(talent, 0);
        metamorphAppearances.put(talent, 0);
        transformSpellTalents.put(talent, 0);
        serverTalents.put(talent, 0);
        serverMetamorphAppearances.put(talent, 0);
        serverTransformSpellTalents.put(talent, 0);
        tierTalents.add(talent);
        CATALOG_BY_TALENT.put(talent, this);
    }

    private void addEntities(Talent... classes ){
        for (Talent cls : classes){
            registerTalent(cls);
        }
    }
    private void addCommonTierEntities(int tier){
        for (Talent.TalentType type : Talent.COMMON_TYPES) {
            for (Talent talent : Talent.talentsByTierAndType(tier + 1, type)) {
                if (!Talent.isBossTalentPlaceholder(talent)) {
                    registerTalent(talent);
                }
            }
        }
    }

    private void addSubclassEntities() {
        for (HeroClass heroClass : catalogHeroClasses()) {
            for (HeroSubClass subClass : heroClass.subClasses()) {
                for (Talent talent : Talent.subclassTalentPool(subClass)) {
                    registerTalent(talent);
                }
            }
        }
    }

    private void addArmorEntities() {
        for (HeroClass heroClass : catalogHeroClasses()) {
            for (ArmorAbility ability : heroClass.armorAbilities()) {
                if(ability instanceof Ratmogrify) continue;
                ArrayList<LinkedHashMap<Talent, Integer>> armorTalents = new ArrayList<>();
                Talent.initArmorTalents(ability, armorTalents);
                if (armorTalents.size() <= 3) {
                    continue;
                }
                for (Talent talent : armorTalents.get(3).keySet()) {
                    if(talent!=Talent.HEROIC_ENERGY)registerTalent(talent);
                }
            }
        }
        ArmorAbility ability=new Ratmogrify();
        ArrayList<LinkedHashMap<Talent, Integer>> armorTalents = new ArrayList<>();
        Talent.initArmorTalents(ability, armorTalents);
        for (Talent talent : armorTalents.get(3).keySet()) {
            if(talent!=Talent.HEROIC_ENERGY)registerTalent(talent);
        }
        registerTalent(Talent.HEROIC_ENERGY);
    }

    private void addNegative(){
        for(ArrayList<Talent> talents1: Talent.negativeTalent){
            if(!talents1.isEmpty()){
                for(Talent t:talents1){
                    registerTalent(t);
                }
            }
        }
    }

    private static HeroClass[] catalogHeroClasses() {
        ArrayList<HeroClass> classes = new ArrayList<>();
        for (HeroClass heroClass : HeroClass.values()) {
            if (heroClass != HeroClass.RATKING) {
                classes.add(heroClass);
            }
        }
        return classes.toArray(new HeroClass[0]);
    }


    static {
        T1.addCommonTierEntities(0);
        T2.addCommonTierEntities(1);
        T3.addCommonTierEntities(2);
        BOSS.addEntities(Talent.AQUATIC_RECOVER,Talent.PUMP_ATTACK,Talent.OOZE_ATTACK, Talent.STRONGEST_SHIELD,Talent.COMBO_PACKAGE,Talent.BREAK_ENEMY_RANKS,
                Talent.SURPRISE_THROW, Talent.SMOKE_MASK,Talent.RUSH,Talent.SHADOW_KILLER,Talent.KILL_SPREE,Talent.SEAOFPEOPLE,Talent.PHANTOM_STEP,
                Talent.FASTING,Talent.THUNDER_STRIKE,Talent.DIRECTIONAL_COLLAPSE,
                Talent.KING_PROTECT,Talent.SUMMON_FOLLOWER,Talent.WOLFISH_GAZE,Talent.ENERGY_CONVERSION,
                Talent.YOG_LARVA,Talent.YOG_FIST,Talent.YOG_RAY);
        NEGATIVE.addNegative();
        SUBCLASS.addSubclassEntities();
        ARMOR.addArmorEntities();
    }

    private static TalentCatalog catalogFor(Talent talent) {
        return CATALOG_BY_TALENT.get(talent);
    }

    public static int useCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : cat.talents.get(cls);
    }
    public static int appearanceCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : Math.max(cat.metamorphAppearances.get(cls), cat.talents.get(cls));
    }
    public static float useRate(Talent cls){
        int appearances = appearanceCount(cls);
        if (appearances <= 0){
            return 0f;
        }
        return (float) useCount(cls) / appearances;
    }
    public static int transformSpellCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : cat.transformSpellTalents.get(cls);
    }
    public static int serverUseCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : cat.serverTalents.get(cls);
    }
    public static int serverAppearanceCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : Math.max(cat.serverMetamorphAppearances.get(cls), cat.serverTalents.get(cls));
    }
    public static float serverUseRate(Talent cls){
        int appearances = serverAppearanceCount(cls);
        if (appearances <= 0){
            return 0f;
        }
        return (float) serverUseCount(cls) / appearances;
    }
    public static int serverTransformSpellCount(Talent cls){
        TalentCatalog cat = catalogFor(cls);
        return cat == null ? 0 : cat.serverTransformSpellTalents.get(cls);
    }
    public static void countUse(Talent cls){
        countUses(cls, 1);
    }
    public static void countUses(Talent cls, int uses){
        if (!canRecordMetamorphStats()){
            return;
        }
        countTrackedUses(cls, uses);
    }

    static void countTrackedUses(Talent cls, int uses){
        TalentCatalog cat = catalogFor(cls);
        if (cat != null && cat.talents.get(cls) != Integer.MAX_VALUE) {
            cat.talents.put(cls, cat.talents.get(cls)+uses);
            if (cat.talents.get(cls) < -1_000_000_000){ //to catch cases of overflow
                cat.talents.put(cls, Integer.MAX_VALUE);
            }
            Journal.saveNeeded = true;
        }
    }
    public static void countAppearance(Talent cls){
        countAppearances(cls, 1);
    }
    public static void countAppearances(Talent cls, int appearances){
        if (!canRecordMetamorphStats()){
            return;
        }
        countTrackedAppearances(cls, appearances);
    }

    static void countTrackedAppearances(Talent cls, int appearances){
        TalentCatalog cat = catalogFor(cls);
        if (cat != null && cat.metamorphAppearances.get(cls) != Integer.MAX_VALUE) {
            cat.metamorphAppearances.put(cls, cat.metamorphAppearances.get(cls)+appearances);
            if (cat.metamorphAppearances.get(cls) < -1_000_000_000){
                cat.metamorphAppearances.put(cls, Integer.MAX_VALUE);
            }
            Journal.saveNeeded = true;
        }
    }
    public static void countTransformSpellUse(Talent cls){
        countTransformSpellUses(cls, 1);
    }
    public static void countTransformSpellUses(Talent cls, int uses){
        if (!canRecordMetamorphStats()){
            return;
        }
        TalentCatalog cat = catalogFor(cls);
        if (cat != null && cat.transformSpellTalents.get(cls) != Integer.MAX_VALUE) {
            cat.transformSpellTalents.put(cls, cat.transformSpellTalents.get(cls)+uses);
            if (cat.transformSpellTalents.get(cls) < -1_000_000_000){
                cat.transformSpellTalents.put(cls, Integer.MAX_VALUE);
            }
            Journal.saveNeeded = true;
        }
    }

    private static boolean canRecordMetamorphStats(){
        if (Dungeon.isChallenged(Challenges.TEST_MODE)){
            return false;
        }
        if (Dungeon.customSeedText != null && !Dungeon.customSeedText.isEmpty()){
            return false;
        }
        String selectedSeed = SPDSettings.customSeed();
        return selectedSeed == null || selectedSeed.isEmpty();
    }

    private static final String TALENT_COUNTS = "talent_counts";
    private static final String TALENT_APPEARANCES = "talent_metamorph_appearances";
    private static final String TRANSFORM_SPELL_COUNTS = "transform_spell_talent_counts";
    private static final String SERVER_TALENT_STATS = "server_talent_stats";
    private static final String SELECTED = "selected";
    private static final String APPEARED = "appeared";
    private static final String TARGETED = "targeted";

    public static Bundle localStatsBundle(){
        Bundle stats = new Bundle();
        for (TalentCatalog cat : values()) {
            for (Talent entity : cat.entities().keySet()) {
                Bundle talentStats = new Bundle();
                talentStats.put(SELECTED, cat.talents.get(entity));
                talentStats.put(APPEARED, cat.metamorphAppearances.get(entity));
                talentStats.put(TARGETED, cat.transformSpellTalents.get(entity));
                stats.put(entity.name(), talentStats);
            }
        }
        return stats;
    }

    public static void restoreLocalStats(Bundle stats){
        clearStats(talentsMapSelector.LOCAL);
        applyStats(stats, talentsMapSelector.LOCAL);
        Journal.saveNeeded = true;
    }

    public static void restoreServerStats(Bundle stats){
        clearStats(talentsMapSelector.SERVER);
        applyStats(stats, talentsMapSelector.SERVER);
        Journal.saveNeeded = true;
    }

    private enum talentsMapSelector {
        LOCAL,
        SERVER
    }

    private static void clearStats(talentsMapSelector selector){
        for (TalentCatalog cat : values()) {
            for (Talent talent : cat.entities().keySet()) {
                if (selector == talentsMapSelector.LOCAL){
                    cat.talents.put(talent, 0);
                    cat.metamorphAppearances.put(talent, 0);
                    cat.transformSpellTalents.put(talent, 0);
                } else {
                    cat.serverTalents.put(talent, 0);
                    cat.serverMetamorphAppearances.put(talent, 0);
                    cat.serverTransformSpellTalents.put(talent, 0);
                }
            }
        }
    }

    private static void applyStats(Bundle stats, talentsMapSelector selector){
        for (String key : stats.getKeys()){
            Talent talent;
            try {
                talent = Talent.valueOf(key);
            } catch (Exception e){
                continue;
            }
            Bundle talentStats = stats.getBundle(key);
            for (TalentCatalog cat : values()){
                if (cat.talents.containsKey(talent)){
                    if (selector == talentsMapSelector.LOCAL){
                        cat.talents.put(talent, talentStats.getInt(SELECTED));
                        cat.metamorphAppearances.put(talent, talentStats.getInt(APPEARED));
                        cat.transformSpellTalents.put(talent, talentStats.getInt(TARGETED));
                    } else {
                        cat.serverTalents.put(talent, talentStats.getInt(SELECTED));
                        cat.serverMetamorphAppearances.put(talent, talentStats.getInt(APPEARED));
                        cat.serverTransformSpellTalents.put(talent, talentStats.getInt(TARGETED));
                    }
                }
            }
        }
    }

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

        Bundle talentAppearances = new Bundle();
        for (TalentCatalog cat : values()) {
            for (Talent entity : cat.entities().keySet()) {
                if (cat.metamorphAppearances.get(entity) > 0){
                    talentAppearances.put(entity.name(), cat.metamorphAppearances.get(entity));
                }
            }
        }

        bundle.put(TALENT_APPEARANCES, talentAppearances);

        Bundle transformSpellCounts = new Bundle();
        for (TalentCatalog cat : values()) {
            for (Talent entity : cat.entities().keySet()) {
                if (cat.transformSpellTalents.get(entity) > 0){
                    transformSpellCounts.put(entity.name(), cat.transformSpellTalents.get(entity));
                }
            }
        }

        bundle.put(TRANSFORM_SPELL_COUNTS, transformSpellCounts);

        Bundle serverStats = new Bundle();
        for (TalentCatalog cat : values()) {
            for (Talent entity : cat.entities().keySet()) {
                int selected = cat.serverTalents.get(entity);
                int appeared = cat.serverMetamorphAppearances.get(entity);
                int targeted = cat.serverTransformSpellTalents.get(entity);
                if (selected > 0 || appeared > 0 || targeted > 0){
                    Bundle talentStats = new Bundle();
                    talentStats.put(SELECTED, selected);
                    talentStats.put(APPEARED, appeared);
                    talentStats.put(TARGETED, targeted);
                    serverStats.put(entity.name(), talentStats);
                }
            }
        }

        bundle.put(SERVER_TALENT_STATS, serverStats);
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

        if (bundle.contains(TALENT_APPEARANCES)){
            Bundle talentAppearances = bundle.getBundle(TALENT_APPEARANCES);
            for (String key : talentAppearances.getKeys()){
                int value = talentAppearances.getInt(key);
                for (TalentCatalog cat : values()){
                    if (cat.metamorphAppearances.containsKey(Talent.valueOf(key))){
                        cat.metamorphAppearances.put(Talent.valueOf(key), value);
                    }
                }
            }
        }

        if (bundle.contains(TRANSFORM_SPELL_COUNTS)){
            Bundle transformSpellCounts = bundle.getBundle(TRANSFORM_SPELL_COUNTS);
            for (String key : transformSpellCounts.getKeys()){
                int value = transformSpellCounts.getInt(key);
                for (TalentCatalog cat : values()){
                    if (cat.transformSpellTalents.containsKey(Talent.valueOf(key))){
                        cat.transformSpellTalents.put(Talent.valueOf(key), value);
                    }
                }
            }
        }

        if (bundle.contains(SERVER_TALENT_STATS)){
            restoreServerStats(bundle.getBundle(SERVER_TALENT_STATS));
        }

    }











}
