package com.shatteredpixel.shatteredpixeldungeon.journal;


import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final LinkedHashMap<Class<?>, Boolean> seen = new LinkedHashMap<>();
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

        /*
        T1.addEntities(Talent.HEARTY_MEAL,Talent.VETERANS_INTUITION,Talent.EMPOWERING_MEAL,Talent.SCHOLARS_INTUITION,Talent.CACHED_RATIONS,
                Talent.THIEFS_INTUITION,Talent.NATURES_BOUNTY,Talent.SURVIVALISTS_INTUITION,Talent.STRENGTHENING_MEAL,Talent.ADVENTURERS_INTUITION,
                Talent.PROVOKED_ANGER, Talent.IRON_WILL,Talent.LINGERING_MAGIC, Talent.BACKUP_BARRIER, Talent.SUCKER_PUNCH,
                Talent.PROTECTIVE_SHADOWS, Talent.FOLLOWUP_STRIKE, Talent.NATURES_AID, Talent.PATIENT_STRIKE, Talent.AGGRESSIVE_BARRIER,
                Talent.STRONG_ATTACK,Talent.FEAR_INCARNATION,Talent.DISTURB_ATTACK,Talent.THRID_HAND,Talent.WATER_ATTACK,Talent.BOMB_MANIAC,
                Talent.THICKENED_ARMOR,Talent.POWERFUL_CALCULATIONS,Talent.INSERT_BID,Talent.MEAL_SHIELD, Talent.STRENGTH_TRAIN,
                Talent.TREAT_MEAL,Talent.COVER_SCAR,Talent.NURTRITIOUS_MEAL,Talent.SAVAGE_PHYSIQUE,Talent.ICE_BREAKING,
                Talent.STRENGTH_GREATEST,Talent.MORE_TALENT,Talent.NOVICE_BENEFITS,Talent.FISHING_TIME,Talent.POSION_DAGGER,
                Talent.SHOCK_BOMB,Talent.ILLUSION_FEED,Talent.ATTACK_DOOR,Talent.ZHUOJUN_BUTCHER,Talent.AID_STOMACH,
                Talent.EATEN_SLOWLY,Talent.INVINCIBLE,Talent.THORNY_ROSE,Talent.GOLD_MEAL,Talent.WATER_GHOST,Talent.ASH_LEDGER,
                Talent.SECRET_LIGHTING,Talent.DAMAGED_CORE,Talent.ANESTHESIA,Talent.EXPERIENCE_MEAL,
                Talent.MILITARY_WATERSKIN,Talent.JASMINE_TEA,Talent.GOLDOFBOOK,Talent.SATIATED_SPELLS, Talent.HOLY_INTUITION,
                Talent.SEARING_LIGHT, Talent.SHIELD_OF_LIGHT,Talent.CHOCOLATE_COINS,Talent.GHOST_GIFT,Talent.PERSONAL_ATTACK,
                Talent.HONEY_FISH,Talent.ASCENSION_CURSE,Talent.SHEPHERD_INTENTION,Talent.SILVER_LANGUAGE,Talent.INSINUATION,
                Talent.AUTO_PICK);



        T2.addEntities(Talent.IRON_STOMACH, Talent.LIQUID_WILLPOWER, Talent.RUNIC_TRANSFERENCE,Talent.ENERGIZING_MEAL, Talent.INSCRIBED_POWER,
                Talent.WAND_PRESERVATION, Talent.MYSTICAL_MEAL,Talent.INSCRIBED_STEALTH, Talent.WIDE_SEARCH,
                Talent.INVIGORATING_MEAL, Talent.LIQUID_NATURE, Talent.HEIGHTENED_SENSES,
                Talent.FOCUSED_MEAL, Talent.LIQUID_AGILITY, Talent.WEAPON_RECHARGING,Talent.LETHAL_MOMENTUM,
                Talent.IMPROVISED_PROJECTILES, Talent.ARCANE_VISION, Talent.SHIELD_BATTERY,
                Talent.SILENT_STEPS, Talent.ROGUES_FORESIGHT, Talent.DURABLE_PROJECTILES,
                Talent.LETHAL_HASTE, Talent.SWIFT_EQUIP, Talent.REJUVENATING_STEPS,
                Talent.SURVIVAL_VOLITION,Talent.STRONG_THROW,Talent.BURNING_CURSE,
                Talent.INVISIBILITY_SHADOWS,Talent.JUSTICE_PUNISH,Talent.PRECIOUS_EXPERIENCE,
                Talent.GHOLL_WITCHCRAFT,Talent.WEIRD_THROW,Talent.MORE_CHANCE,
                Talent.AMAZING_EYESIGHT,Talent.LIGHT_APPLICATION,Talent.HEAVY_APPLICATION,
                Talent.BLESS_MEAL,Talent.WAKE_SNAKE,Talent.COLLECTION_GOLD, Talent.DROP_RESISTANT,Talent.GOD_LEFTHAND,Talent.GOD_RIGHTHAND,
                Talent.STRENGTHEN_CHAIN,Talent.STRENGTHEN_CHALICE,Talent.BURNING_BLOOD,Talent.GET_UP,
                Talent.GIANT_KILLER,Talent.WANT_ALL,Talent.VEGETARIANISM,Talent.WORD_STUN,
                Talent.HEAVY_BURDEN,Talent.ARROW_PENETRATION,Talent.DELICIOUS_FLYING,Talent.FRENZIED_ATTACK,
                Talent.WULEI_ZHENGFA,Talent.MAGIC_GIRL,Talent.ABYSSAL_GAZE,Talent.JOURNEY_NATURE,Talent.STRENGTH_BOOK,
                Talent.EXPLORATION_INTUITION,Talent.FUDI_CHOUXIN,Talent.POISON_INBODY,Talent.SEED_RECYCLING,
                Talent.HEDONISM,Talent.BACKFIRED,Talent.ENLIGHTENING_MEAL, Talent.RECALL_INSCRIPTION, Talent.SUNRAY,
                Talent.DIVINE_SENSE, Talent.BLESS,Talent.QUANTUM_HACKING,Talent.SHOOT_SATELLITE,Talent.INSTANT_REFINING,
                Talent.DIVINE_PROTECTION,Talent.CONVERSION_HOLY,Talent.GENESIS,Talent.INDULGENCE,Talent.PROTECT_CURSE,
                Talent.POTENTIAL_ENERGY,Talent.NIRVANA);



        T3.addEntities(Talent.HOLD_FAST, Talent.DESPERATE_POWER, Talent.LIGHT_CLOAK, Talent.SEER_SHOT,
                Talent.DEADLY_FOLLOWUP, Talent.STRONGMAN, Talent.ALLY_WARP, Talent.ENHANCED_RINGS,
                Talent.POINT_BLANK, Talent.PRECISE_ASSAULT, Talent.TRAP_MASTER,Talent.COUNTER_ATTACK,
                Talent.BEHEST,Talent.OVERWHELMING,Talent.MAGIC_RECYCLING,Talent.ENGINEER_REFIT,Talent.PHANTOM_SHOOTER,
                Talent.MARTIAL_TRAIN,Talent.RAGE_ATTACK,Talent.GOLD_FORMATION,Talent.ACCUMULATE_STEADILY,
                Talent.DOUBLE_TRINKETS,Talent.INVINCIBLE_MEAL,Talent.WELLFED_MEAL,Talent.DETOX_DAMAGE,
                Talent.RETURNING_HONOR,Talent.WEAPON_MAKE,Talent.SECRET_STASH,Talent.EARTH_MEAL,Talent.AFRAID_DEATH,
                Talent.PYROMANIAC,Talent.REVERSE_POLARITY,Talent.HERO_NAME,Talent.SKY_EARTH,Talent.WATER_ISFOOD,
                Talent.ANGEL_STANCE,Talent.HOMETOWN_CLOUD,Talent.DEEP_FREEZE,Talent.WIDE_KNOWLEDGE,Talent.NO_VIEWRAPE,
                Talent.MORONITY,Talent.LOVE_BACKSTAB,Talent.CONCEPT_GRID,Talent.ABACUS,Talent.TIME_SAND,
                Talent.READ_PROFITABLE,Talent.STRENGTH_CLOAK,Talent.WANLING_POTION,Talent.ACTIVE_MUSCLES,
                Talent.SEA_WIND,Talent.ENDLESS_MEAL,Talent.BIRTHDAY_GIFT,Talent.COLLECT_PLANTS,Talent.SHARP_HEAD,
                Talent.STRENGTH_ARMBAND,Talent.EXTREME_CASTING,Talent.PRECISE_SHOT,Talent.TREASURE_SENSE,
                Talent.BEYOND_LIMIT,Talent.HEALTHY_FOOD,Talent.EXTREME_REACTION,Talent.CLEANSE, Talent.LIGHT_READING,
                Talent.CICADA_DANCE,Talent.HOLY_FAITH,Talent.CHANGQI_BOOKSTORE,Talent.PURIFYING_EVIL,Talent.DIVINE_STORM,
                Talent.RESURRECTION,Talent.ASHES_BOW,Talent.SWIFT_CHURCH);
        NEGATIVE.addEntities(Talent.ETERNAL_CURSE,Talent.EATER,Talent.FATE_DECISION,Talent.ENDLESS_MALICE,Talent.FEEBLE,
                Talent.MALNUTRITION,Talent.SHORTSIGHTED,Talent.BAT_SERUM,Talent.MYOPIA,Talent.LAND_SWIMMING,
                Talent.JIULONGLA_COFFIN,Talent.COWBOY,Talent.MAMBA_OUT,Talent.EXPLOSION_MEAL,Talent.HANDON_GROUND,
                Talent.CHILL_WATER,Talent.LIFE_SPORT,Talent.WEAKEN_CHALICE,Talent.MENTAL_COLLAPSE,Talent.UNAVOIDABLE,
                Talent.PARASITISM,Talent.DUMP_TRUCK,Talent.BURNOUT_CHAMPION,Talent.PHOTOPHOBY,Talent.OUTCONTROL_MAGIC,
                Talent.WINTER_SWIMMING,Talent.FIRE_WOOD,Talent.BE_INCONSTANT,Talent.UNBEAR_HUNGER,Talent.VIP_MEAL,
                Talent.FAST_DIE,Talent.WASH_HAND,Talent.THORNS_SPRANG,Talent.FULLPASSION,Talent.FULLFIGHTING,Talent.PHASECLAW,
                Talent.DEEP_FEAR,Talent.NO_DOOR,Talent.UPDRAFT,Talent.BONE_FIRE,Talent.JUMP_FACE_SILICONE,Talent.GRASS_MOB,
                Talent.DISASTER_CURSE);

         */

    }



}
