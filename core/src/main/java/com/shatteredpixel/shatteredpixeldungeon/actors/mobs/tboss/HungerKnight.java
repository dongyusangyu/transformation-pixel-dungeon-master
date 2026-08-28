package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightEquipmentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightMagicLease;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightOverburden;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightTalentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossRewardGenerator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.HungerKnightSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The tower's famine rider. Its combat panel is driven by a private random
 * tier-six weapon and a private suit of armor; neither item is dropped.
 */
public class HungerKnight extends TowerBoss {

    public enum Phase { AWAKENING, DEPLETION, EXHAUSTION }
    public enum Adaptation { NONE, ATTACK, MAGIC, EFFECT, RESOURCE, SPELL, ASSIST, OTHER }
    enum Skill { NONE, HEAVY, THRUST, QUAKE, COMBO }
    private enum Transition { NONE, ARMED }

    public static final int FINAL_DAMAGE_CAP = 150;
    public static final int FIRST_LOCK_HP = 1000;
    public static final int SECOND_LOCK_HP = 500;
    private static final int[] EQUIPMENT_LEVEL_WEIGHTS = {5, 10, 15, 20, 25, 25};

    private static final long EQUIPMENT_SALT = 0x48554E4745524B4EL;
    private static final String WEAPON = "weapon";
    private static final String ARMOR = "armor";
    private static final String PHASE = "phase";
    private static final String PHASE_LOCKS = "phase_locks";
    private static final String TRANSITION = "transition";
    private static final String WEAPON_CLASS = "weapon_class";
    private static final String WEAPON_LEVEL = "weapon_level";
    private static final String WEAPON_ENCHANT = "weapon_enchant";
    private static final String ARMOR_CLASS = "armor_class";
    private static final String ARMOR_LEVEL = "armor_level";
    private static final String ARMOR_GLYPH = "armor_glyph";
    private static final String ENCOUNTER_INITIALIZED = "encounter_initialized";
    private static final String TALENT_PHASE_BITS = "talent_phase_bits";
    private static final String SEALED_TALENTS = "sealed_talents";
    private static final String ADAPTATION = "adaptation";
    private static final String BOSS_ACTIONS = "boss_actions";
    private static final String ADAPTATION_ACTIONS = "adaptation_actions";
    private static final String REWARD_DROPPED = "reward_dropped";
    private static final String NOTICE_ANNOUNCED = "notice_announced";
    private static final String PENDING_SKILL = "pending_skill";
    private static final String PENDING_CELLS = "pending_cells";
    private static final String PENDING_TARGET_ID = "pending_target_id";
    private static final String PENDING_TARGET_CELL = "pending_target_cell";
    private static final String PENDING_TURNS = "pending_turns";
    private static final String PENDING_PAUSED = "pending_paused";
    private static final String HEAVY_COOLDOWN = "heavy_cooldown";
    private static final String THRUST_COOLDOWN = "thrust_cooldown";
    private static final String QUAKE_COOLDOWN = "quake_cooldown";
    private static final String COMBO_TRIGGER_BITS = "combo_trigger_bits";
    private static final String COMBO_STRIKE_INDEX = "combo_strike_index";
    private static final String TALENT_ANNOUNCEMENT_PENDING = "talent_announcement_pending";
    private static final String PENDING_ORDINARY_TALENT = "pending_ordinary_talent";
    private static final String PENDING_BOSS_TALENT = "pending_boss_talent";

    private Weapon weapon;
    private Armor armor;
    private Class<? extends MeleeWeapon> weaponClass;
    private int weaponLevel;
    private Class<? extends Weapon.Enchantment> weaponEnchantClass;
    private Class<? extends Armor> armorClass;
    private int armorLevel;
    private Class<? extends Armor.Glyph> armorGlyphClass;
    private Phase phase = Phase.AWAKENING;
    private int phaseLocks;
    private Transition transition = Transition.NONE;
    private boolean encounterInitialized;
    private int talentPhaseBits;
    private final ArrayList<String> sealedTalentNames = new ArrayList<>();
    private Adaptation adaptation = Adaptation.NONE;
    private int bossActions;
    private int adaptationActions;
    private boolean rewardDropped;
    private boolean noticeAnnounced;
    private Skill pendingSkill = Skill.NONE;
    private int[] pendingCells = new int[0];
    private int pendingTargetId = -1;
    private int pendingTargetCell = -1;
    private int pendingTurns;
    private boolean pendingPaused;
    private int heavyCooldown;
    private int thrustCooldown;
    private int quakeCooldown;
    private int comboTriggerBits;
    private int comboStrikeIndex;
    private boolean talentAnnouncementPending;
    private String pendingOrdinaryTalent = "";
    private String pendingBossTalent = "";
    private transient boolean restoreGrace;
    private transient boolean waitingForAttackCompletion;
    private transient boolean thrustResolving;
    private transient boolean comboResolving;
    private transient boolean comboCompletionHandled;
    private transient Skill resolvingSkill = Skill.NONE;

    public HungerKnight() {
        HT = HP = 1500;
        viewDistance = 8;
        defenseSkill = 25;
        EXP = 0;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
        spriteClass = HungerKnightSprite.class;
        properties.add(Property.BOSS);
        properties.add(Property.UNSLEEP);
        rollEquipmentBlueprint();
    }

    @Override
    public String towerBossId() {
        return TowerBossGenerator.HUNGER_KNIGHT_ID;
    }

    @Override
    public boolean prepareArena(TowerBossLevel level, int spawnCell) {
        ensureEquipment();
        initializeEncounter();
        return true;
    }

    private void initializeEncounter() {
        if (Dungeon.hero == null) return;
        if (!encounterInitialized) {
            encounterInitialized = true;
            enterPhase(phase);
        } else {
            reconcileEncounterEffects();
        }
    }

    private void enterPhase(Phase entered) {
        Hero hero = Dungeon.hero;
        if (hero == null) return;
        if (entered == Phase.AWAKENING) {
            HungerKnightEquipmentSeal.attach(hero, id());
        } else {
            HungerKnightEquipmentSeal.release(hero, id());
            HungerKnightOverburden.attach(hero, id());
        }
        if (entered == Phase.EXHAUSTION) HungerKnightMagicLease.acquire(hero, id());
        selectPhaseTalents(entered.ordinal() + 1);
        adaptationActions = 0;
        applyAdaptationOpening();
    }

    private void reconcileEncounterEffects() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;
        HungerKnightTalentSeal seal = ownedTalentSeal(hero, true);
        if (seal == null) return;
        for (String name : sealedTalentNames) {
            try { seal.add(Talent.valueOf(name)); } catch (IllegalArgumentException ignored) {}
        }
        if (phase == Phase.AWAKENING) {
            HungerKnightEquipmentSeal.attach(hero, id());
        } else {
            HungerKnightEquipmentSeal.release(hero, id());
            HungerKnightOverburden.attach(hero, id());
        }
        if (phase == Phase.EXHAUSTION) HungerKnightMagicLease.acquire(hero, id());
    }

    private void selectPhaseTalents(int tier) {
        int bit = 1 << (tier - 1);
        if ((talentPhaseBits & bit) != 0 || Dungeon.hero == null) return;
        talentPhaseBits |= bit;
        List<Talent> ordinary = new ArrayList<>();
        List<Talent> boss = new ArrayList<>();
        for (LinkedHashMap<Talent, Integer> map : Dungeon.hero.talents) {
            for (Talent talent : map.keySet()) {
                int points = Dungeon.hero.rawTalentPoints(talent);
                if (points <= 0 || talent.tier() != tier
                        || sealedTalentNames.contains(talent.name())
                        || Talent.isBossTalentPlaceholder(talent)) continue;
                if (Talent.isBossTalent(talent)) boss.add(talent);
                else if (talent.type().isCommon()) ordinary.add(talent);
            }
        }
        Comparator<Talent> byName = Comparator.comparing(Enum::name);
        ordinary.sort(byName);
        boss.sort(byName);
        Talent normal = deterministicTalent(ordinary, tier, 0x4F52444EL);
        Talent bossTalent = deterministicTalent(boss, tier, 0x424F5353L);
        HungerKnightTalentSeal seal = ownedTalentSeal(Dungeon.hero, true);
        if (seal == null) return;
        if (normal != null) {
            seal.add(normal);
            sealedTalentNames.add(normal.name());
            adaptation = adaptationFor(normal.type());
        } else {
            adaptation = Adaptation.NONE;
        }
        if (bossTalent != null) {
            seal.add(bossTalent);
            sealedTalentNames.add(bossTalent.name());
        }
        refreshHeroDerivedState(Dungeon.hero);
        announceTalentSelection(normal, bossTalent);
    }

    private void announceTalentSelection(Talent normal, Talent bossTalent) {
        talentAnnouncementPending = true;
        pendingOrdinaryTalent = normal == null ? "" : normal.name();
        pendingBossTalent = bossTalent == null ? "" : bossTalent.name();
    }

    private String talentSelectionMessage(Talent normal, Talent bossTalent) {
        String none = Messages.get(this, "swallow_none");
        if (normal != null && bossTalent != null) {
            return Messages.get(this, "swallow_pair", normal.title(), bossTalent.title());
        } else if (normal != null || bossTalent != null) {
            Talent only = normal != null ? normal : bossTalent;
            return Messages.get(this, "swallow_single", only.title());
        } else {
            return Messages.get(this, "swallow_pair", none, none);
        }
    }

    private void clearPendingTalentAnnouncement() {
        talentAnnouncementPending = false;
        pendingOrdinaryTalent = "";
        pendingBossTalent = "";
    }

    private void flushPendingTalentAnnouncement() {
        if (!talentAnnouncementPending || sprite == null) return;
        Talent normal = talentByName(pendingOrdinaryTalent);
        Talent bossTalent = talentByName(pendingBossTalent);
        yell(talentSelectionMessage(normal, bossTalent));
        clearPendingTalentAnnouncement();
    }

    private Talent deterministicTalent(List<Talent> candidates, int tier, long salt) {
        if (candidates.isEmpty()) return null;
        long value = mix64(Dungeon.seed ^ ((long) Dungeon.depth << 32)
                ^ id() ^ ((long) tier << 48) ^ salt);
        return candidates.get(new java.util.Random(value).nextInt(candidates.size()));
    }

    private HungerKnightTalentSeal ownedTalentSeal(Hero hero, boolean create) {
        for (HungerKnightTalentSeal seal : hero.buffs(HungerKnightTalentSeal.class)) {
            if (seal.ownerId() == id()) return seal;
        }
        if (!create) return null;
        HungerKnightTalentSeal seal = new HungerKnightTalentSeal();
        seal.setOwnerId(id());
        return seal.attachTo(hero) ? seal : null;
    }

    private static Adaptation adaptationFor(Talent.TalentType type) {
        if (type == null) return Adaptation.NONE;
        try { return Adaptation.valueOf(type.name()); }
        catch (IllegalArgumentException ignored) { return Adaptation.OTHER; }
    }

    private void applyAdaptationOpening() {
        if (adaptation == Adaptation.EFFECT) {
            Buff.affect(this, Barrier.class).setShield(30);
        } else if (adaptation == Adaptation.OTHER) {
            Buff.affect(this, Bless.class, 5f);
        }
    }

    private void finishBossAction() {
        bossActions++;
        adaptationActions++;
        if (heavyCooldown > 0) heavyCooldown--;
        if (thrustCooldown > 0) thrustCooldown--;
        if (quakeCooldown > 0) quakeCooldown--;
        if (adaptation == Adaptation.EFFECT && adaptationActions % 30 == 0) {
            Buff.affect(this, Barrier.class).setShield(30);
        } else if (adaptation == Adaptation.SPELL && adaptationActions % 20 == 0) {
            Buff.affect(this, Adrenaline.class, 5f);
        } else if (adaptation == Adaptation.ASSIST && adaptationActions % 40 == 0) {
            int ceiling = phase == Phase.AWAKENING ? HT
                    : phase == Phase.DEPLETION ? FIRST_LOCK_HP : SECOND_LOCK_HP;
            int missing = Math.max(0, ceiling - HP);
            HP = Math.min(ceiling, HP + Math.min(100, Math.max(1, Math.round(missing * 0.1f))));
        }
    }

    @SuppressWarnings("unchecked")
    private void rollEquipmentBlueprint() {
        Random.pushGenerator(equipmentSeed());
        try {
            Class<?>[] choices = Generator.Category.WEP_T6.classes;
            if (choices == null || choices.length == 0) {
                throw new IllegalStateException("No tier-six weapons are registered");
            }
            weaponClass = (Class<? extends MeleeWeapon>) choices[Random.Int(choices.length)];
            weaponLevel = weightedEquipmentLevel();
            weaponEnchantClass = Weapon.Enchantment.random().getClass();

            int armorRoll = Random.Int(100);
            armorClass = armorRoll < 10 ? MailArmor.class
                    : armorRoll < 70 ? ScaleArmor.class : PlateArmor.class;
            armorLevel = weightedEquipmentLevel();
            armorGlyphClass = Armor.Glyph.random().getClass();
        } finally {
            Random.popGenerator();
        }
    }

    private void ensureEquipment() {
        if (weapon != null && armor != null) return;
        if (weaponClass == null || armorClass == null
                || weaponEnchantClass == null || armorGlyphClass == null) {
            rollEquipmentBlueprint();
        }
        weapon = Reflection.newInstance(weaponClass);
        weapon.level(weaponLevel);
        weapon.cursed = false;
        weapon.enchant(Reflection.newInstance(weaponEnchantClass));

        armor = Reflection.newInstance(armorClass);
        armor.level(armorLevel);
        armor.cursed = false;
        armor.inscribe(Reflection.newInstance(armorGlyphClass));
    }

    private static int weightedEquipmentLevel() {
        return equipmentLevelForRoll(Random.Int(100));
    }

    private static int equipmentLevelForRoll(int roll) {
        int cumulative = 0;
        for (int level = 0; level < EQUIPMENT_LEVEL_WEIGHTS.length; level++) {
            cumulative += EQUIPMENT_LEVEL_WEIGHTS[level];
            if (roll < cumulative) return level;
        }
        return EQUIPMENT_LEVEL_WEIGHTS.length - 1;
    }

    private static long equipmentSeed() {
        long seed = Dungeon.seed ^ ((long) Dungeon.depth << 32)
                ^ TowerBossGenerator.HUNGER_KNIGHT_ID.hashCode() ^ EQUIPMENT_SALT;
        return mix64(seed);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    @Override
    public int damageRoll() {
        ensureEquipment();
        return weapon.damageRoll(this);
    }

    @Override
    public int attackSkill(Char target) {
        ensureEquipment();
        return Math.round(50f * weapon.accuracyFactor(this, target));
    }

    @Override
    public float attackDelay() {
        ensureEquipment();
        float delay = super.attackDelay() * weapon.delayFactor(this);
        return phase == Phase.EXHAUSTION ? delay * 0.75f : delay;
    }

    @Override
    protected boolean canAttack(Char enemy) {
        ensureEquipment();
        return super.canAttack(enemy)
                || weapon.canReach(this, enemy.pos, phaseClawReachBonus());
    }

    @Override
    public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
        ensureEquipment();
        damage = super.attackProc(enemy, damage, damageTags);
        damage = weapon.proc(this, enemy, damage);
        if (adaptation == Adaptation.ATTACK && containsTag(damageTags, DamageTag.PHYSICAL)) {
            damage = Math.round(damage * 1.1f);
        }
        return damage;
    }

    @Override
    protected void onAttackResolved(Char target, boolean hit, int damageDealt,
                                    DamageTag... damageTags) {
        super.onAttackResolved(target, hit, damageDealt, damageTags);
        if (hit && resolvingSkill == Skill.HEAVY) {
            Buff.prolong(target, Cripple.class, 3f);
            Buff.prolong(target, Degrade.class, 6f);
        }
        if (hit && target == Dungeon.hero && containsTag(damageTags, DamageTag.MELEE)) {
            addHeroHunger(meleeHungerFor(resolvingSkill));
        }
    }

    private int meleeHungerFor(Skill skill) {
        int hunger = 0;
        if (skill == Skill.THRUST) hunger = 30;
        else if (skill == Skill.HEAVY) hunger = 55;
        else if (skill == Skill.NONE) hunger = 15;
        if (adaptation == Adaptation.RESOURCE) hunger += 20;
        return hunger;
    }

    private void addHeroHunger(int amount) {
        if (Dungeon.hero == null || amount <= 0) return;
        Hunger hunger = Dungeon.hero.buff(Hunger.class);
        if (hunger == null) hunger = Buff.affect(Dungeon.hero, Hunger.class);
        if (hunger != null) hunger.affectHunger(-amount);
    }

    @Override
    public int drRoll() {
        ensureEquipment();
        return Random.NormalIntRange(armor.DRMin(), armor.DRMax());
    }

    @Override
    public int defenseProc(Char enemy, int damage, DamageTag... damageTags) {
        ensureEquipment();
        damage = armor.proc(enemy, this, damage);
        return super.defenseProc(enemy, damage, damageTags);
    }

    @Override
    public int glyphLevel(Class<? extends Armor.Glyph> glyphClass) {
        if (armor != null && armor.hasGlyph(glyphClass, this)) {
            return Math.max(super.glyphLevel(glyphClass), armor.buffedLvl());
        }
        return super.glyphLevel(glyphClass);
    }

    @Override
    public boolean isImmune(Class effect) {
        // Buff attachment asks this before prepareArena has necessarily materialized
        // the private equipment.  Do not force texture-backed Item initialization here.
        if (armor != null
                && effect == com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class
                && armor.hasGlyph(Brimstone.class, this)) return true;
        return super.isImmune(effect);
    }

    @Override
    public float speed() {
        ensureEquipment();
        float result = armor.speedFactor(this, super.speed());
        return phase == Phase.EXHAUSTION ? result * 1.5f : result;
    }

    @Override
    public float stealth() {
        ensureEquipment();
        return armor.stealthFactor(this, super.stealth());
    }

    @Override
    public int defenseSkill(Char enemy) {
        ensureEquipment();
        return Math.round(armor.evasionFactor(this, 25));
    }

    @Override
    protected int modifyFinalDamage(int damage, Object source, DamageTag... tags) {
        if (transition != Transition.NONE) return 0;
        int result = Math.max(0, super.modifyFinalDamage(damage, source, tags));
        if (adaptation == Adaptation.MAGIC && containsTag(tags, DamageTag.MAGICAL)) {
            result = Math.round(result * 0.8f);
        }
        int capped = Math.min(FINAL_DAMAGE_CAP, result);
        int lock = nextUnfinishedLockHP();
        if (lock < 0) return capped;
        int untilLock = Math.max(0, HP - lock);
        if (capped >= untilLock) {
            transition = Transition.ARMED;
            return untilLock;
        }
        return capped;
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return transition != Transition.NONE || super.isInvulnerable(effect);
    }

    private int nextUnfinishedLockHP() {
        if ((phaseLocks & 1) == 0) return FIRST_LOCK_HP;
        if ((phaseLocks & 2) == 0) return SECOND_LOCK_HP;
        return -1;
    }

    private boolean advanceTransition() {
        if (transition == Transition.NONE) return false;
        clearPendingSkill();
        if ((phaseLocks & 1) == 0) {
            phaseLocks |= 1;
            phase = Phase.DEPLETION;
        } else if ((phaseLocks & 2) == 0) {
            phaseLocks |= 2;
            phase = Phase.EXHAUSTION;
        }
        transition = Transition.NONE;
        enterPhase(phase);
        finishBossAction();
        if (sprite instanceof HungerKnightSprite) {
            ((HungerKnightSprite) sprite).phaseTransition();
        }
        if (sprite != null) {
            String message = Messages.get(this,
                    phase == Phase.DEPLETION ? "phase_depletion" : "phase_exhaustion");
            if (talentAnnouncementPending) {
                message += "\n" + talentSelectionMessage(
                        talentByName(pendingOrdinaryTalent), talentByName(pendingBossTalent));
                clearPendingTalentAnnouncement();
            }
            yell(message);
        }
        spend(TICK);
        return true;
    }

    @Override
    protected boolean act() {
        initializeEncounter();
        if (pendingSkill != Skill.NONE && (paralysed > 0 || state == SLEEPING)) {
            pendingPaused = true;
            showPendingTelegraph();
            return super.act();
        }
        if (transition != Transition.NONE && paralysed <= 0 && state != SLEEPING) {
            return advanceTransition();
        }
        if (restoreGrace) return consumeRestoreGrace();
        if (pendingPaused) return rearmPending();
        if (pendingSkill != Skill.NONE) return advancePending();

        Char target = specialTarget();
        int comboTrigger = nextComboTrigger();
        if (comboTrigger != 0 && validTarget(target)) {
            comboTriggerBits |= comboTrigger;
            return telegraph(Skill.COMBO, target, warningLine(target, 3));
        }
        if (quakeCooldown <= 0 && canQuake()) return performQuake();
        if (heavyCooldown <= 0 && validTarget(target) && canAttack(target)) {
            return telegraph(Skill.HEAVY, target, new int[]{target.pos});
        }
        if (thrustCooldown <= 0 && canThrust(target)) {
            return telegraph(Skill.THRUST, target, thrustLine(target));
        }
        boolean completed = super.act();
        if (completed) finishBossAction();
        else waitingForAttackCompletion = true;
        return completed;
    }

    private Char specialTarget() {
        if (validTarget(enemy)) return enemy;
        return validTarget(Dungeon.hero) ? Dungeon.hero : null;
    }

    private boolean validTarget(Char target) {
        return target != null && target != this && target.isAlive()
                && target.alignment != alignment && Dungeon.level != null
                && target.pos >= 0 && target.pos < Dungeon.level.length();
    }

    private int nextComboTrigger() {
        if (phase != Phase.EXHAUSTION) return 0;
        if ((comboTriggerBits & 1) == 0) return 1;
        if (HP <= 200 && (comboTriggerBits & 2) == 0) return 2;
        if (HP <= 100 && (comboTriggerBits & 4) == 0) return 4;
        return 0;
    }

    private boolean telegraph(Skill skill, Char target, int[] cells) {
        if (skill == null || skill == Skill.NONE || !validTarget(target)
                || cells == null || cells.length == 0) return false;
        pendingSkill = skill;
        pendingCells = cells.clone();
        pendingTargetId = target.id();
        pendingTargetCell = target.pos;
        pendingTurns = 1;
        pendingPaused = false;
        comboStrikeIndex = 0;
        announceSkill(skill);
        showPendingTelegraph();
        finishBossAction();
        spend(TICK);
        return true;
    }

    private boolean advancePending() {
        if (pendingSkill == Skill.NONE) return false;
        if (pendingTurns > 1) {
            pendingTurns--;
            showPendingTelegraph();
            finishBossAction();
            spend(TICK);
            return true;
        }
        Skill skill = pendingSkill;
        if (skill == Skill.THRUST && beginThrustResolution()) return false;
        if (skill == Skill.COMBO && beginComboResolution()) return false;
        switch (skill) {
            case HEAVY:
                resolveHeavy();
                break;
            case COMBO:
                resolveCombo();
                break;
            default:
                break;
        }
        finishPendingSkill(skill);
        return true;
    }

    private void finishPendingSkill(Skill skill) {
        clearPendingSkill();
        finishBossAction();
        if (skill == Skill.HEAVY) heavyCooldown = heavyCooldownLength();
        else if (skill == Skill.THRUST) thrustCooldown = thrustCooldownLength();
        spend(TICK);
    }

    private void resolveHeavy() {
        Char target = Actor.findCharById(pendingTargetId);
        if (!validTarget(target) || !canAttack(target)) return;
        if (sprite instanceof HungerKnightSprite) ((HungerKnightSprite) sprite).charge();
        skillAttack(target, 1.5f, Skill.HEAVY);
    }

    private boolean canThrust(Char target) {
        if (!validTarget(target)) return false;
        int distance = Dungeon.level.distance(pos, target.pos);
        if (distance < 2 || distance > 6) return false;
        Ballistica shot = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
        return shot.collisionPos == target.pos;
    }

    private int[] thrustLine(Char target) {
        if (!validTarget(target)) return new int[0];
        Ballistica shot = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
        int size = Math.min(shot.dist, shot.path.size() - 1);
        int[] cells = new int[Math.max(0, size)];
        for (int i = 0; i < cells.length; i++) cells[i] = shot.path.get(i + 1);
        return cells;
    }

    private boolean beginThrustResolution() {
        if (Dungeon.level == null || pendingCells.length == 0) return false;
        Char mainTarget = Actor.findCharById(pendingTargetId);
        ArrayList<Char> victims = new ArrayList<>();
        int destination = pos;
        int steps = 0;
        for (int cell : pendingCells) {
            if (steps >= 3 || cell < 0 || cell >= Dungeon.level.length()
                    || Dungeon.level.solid[cell] || !Dungeon.level.passable[cell]) break;
            Char occupant = Actor.findChar(cell);
            if (occupant != null) {
                if (validTarget(occupant) && !victims.contains(occupant)) victims.add(occupant);
                break;
            }
            destination = cell;
            steps++;
        }
        for (Char target : victims) {
            skillAttack(target, 1f, Skill.THRUST);
        }
        boolean moving = false;
        if (destination != pos) {
            int oldPos = pos;
            move(destination, false);
            moving = safeMoveSprite(oldPos, destination);
        }
        // Start the attack after moveSprite selects its run animation so the
        // visible thrust remains active during the position tween.
        if (sprite instanceof HungerKnightSprite) ((HungerKnightSprite) sprite).thrust();
        if (validTarget(mainTarget) && mainTarget.pos == pendingTargetCell
                && Dungeon.level.adjacent(pos, pendingTargetCell)) {
            skillAttack(mainTarget, 0.5f, Skill.THRUST);
        }
        if (moving) {
            thrustResolving = true;
            return true;
        }
        return false;
    }

    private boolean canQuake() {
        if (Dungeon.level == null) return false;
        int count = 0;
        for (Char target : Actor.chars()) {
            if (validQuakeTarget(target) && Dungeon.level.distance(pos, target.pos) <= 1) count++;
        }
        return count >= 3;
    }

    private boolean performQuake() {
        ArrayList<Char> victims = new ArrayList<>();
        for (Char target : Actor.chars()) {
            if (validQuakeTarget(target) && Dungeon.level.distance(pos, target.pos) <= 1) {
                victims.add(target);
            }
        }
        if (sprite instanceof HungerKnightSprite) ((HungerKnightSprite) sprite).charge();
        if (sprite != null) yell(Messages.get(this, "quake"));
        for (Char target : victims) {
            skillAttack(target, 0.75f, Skill.QUAKE);
            Buff.prolong(target, Vertigo.class, 3f);
            int aim = cellBeyond(target.pos, 3);
            if (aim != target.pos) {
                WandOfBlastWave.throwCharImmediately(target,
                        new Ballistica(target.pos, aim, Ballistica.MAGIC_BOLT),
                        2, false, false, this, null);
            }
        }
        finishBossAction();
        quakeCooldown = 6;
        spend(TICK);
        return true;
    }

    private boolean validQuakeTarget(Char target) {
        return target != null && target != this && target.isAlive() && Dungeon.level != null
                && target.pos >= 0 && target.pos < Dungeon.level.length();
    }

    private int cellBeyond(int from, int steps) {
        if (Dungeon.level == null) return from;
        int width = Dungeon.level.width();
        int dx = Integer.compare(from % width, pos % width);
        int dy = Integer.compare(from / width, pos / width);
        int x = Math.max(0, Math.min(width - 1, from % width + dx * steps));
        int y = Math.max(0, Math.min(Dungeon.level.height() - 1, from / width + dy * steps));
        return x + y * width;
    }

    private void resolveCombo() {
        Char target = Actor.findCharById(pendingTargetId);
        if (!validTarget(target) || target.pos != pendingTargetCell) return;
        approachForCombo(target, 3);
        if (!canAttack(target)) return;
        if (sprite instanceof HungerKnightSprite) ((HungerKnightSprite) sprite).thrust();
        for (; comboStrikeIndex < 3 && validTarget(target) && canAttack(target);
             comboStrikeIndex++) {
            skillAttack(target, 0.5f, Skill.COMBO);
        }
    }

    /**
     * Starts the animated combo and returns true only when Actor processing must
     * wait for its callback chain. Headless and detached-sprite paths resolve
     * synchronously through {@link #resolveCombo()}.
     */
    private boolean beginComboResolution() {
        Char target = comboTarget();
        if (target == null) return false;
        approachForCombo(target, 3);
        if (!canAttack(target) || !comboSpriteReady()) return false;
        comboResolving = true;
        comboCompletionHandled = false;
        playNextComboStrike();
        return true;
    }

    private Char comboTarget() {
        Char target = Actor.findCharById(pendingTargetId);
        return validTarget(target) && target.pos == pendingTargetCell ? target : null;
    }

    private boolean comboSpriteReady() {
        return sprite instanceof HungerKnightSprite && sprite.visible && sprite.parent != null;
    }

    private void playNextComboStrike() {
        if (!comboResolving || comboCompletionHandled) return;
        Char target = comboTarget();
        if (comboStrikeIndex >= 3 || target == null || !canAttack(target)) {
            finishComboResolution();
            return;
        }
        if (!comboSpriteReady()) {
            resolveRemainingComboWithoutAnimation(target);
            finishComboResolution();
            return;
        }
        final int expectedStrike = comboStrikeIndex;
        ((HungerKnightSprite) sprite).thrust(new Callback() {
            @Override
            public void call() {
                completeComboStrike(expectedStrike);
            }
        });
    }

    private void completeComboStrike(int expectedStrike) {
        if (!comboResolving || comboCompletionHandled || comboStrikeIndex != expectedStrike) return;
        Char target = comboTarget();
        if (target != null && canAttack(target)) {
            skillAttack(target, 0.5f, Skill.COMBO);
            comboStrikeIndex++;
        }
        playNextComboStrike();
    }

    private void resolveRemainingComboWithoutAnimation(Char target) {
        while (comboStrikeIndex < 3 && validTarget(target)
                && target.pos == pendingTargetCell && canAttack(target)) {
            skillAttack(target, 0.5f, Skill.COMBO);
            comboStrikeIndex++;
        }
    }

    private void finishComboResolution() {
        if (!comboResolving || comboCompletionHandled) return;
        comboCompletionHandled = true;
        comboResolving = false;
        clearPendingSkill();
        finishBossAction();
        spend(TICK);
        next();
    }

    private void skillAttack(Char target, float multiplier, Skill skill) {
        Skill previous = resolvingSkill;
        resolvingSkill = skill;
        try {
            attack(target, multiplier, 0f, 1f, DamageTag.PHYSICAL, DamageTag.MELEE);
        } finally {
            resolvingSkill = previous;
        }
    }

    private void approachForCombo(Char target, int maximumSteps) {
        int oldPos = pos;
        for (int i = 0; i < maximumSteps && validTarget(target) && !canAttack(target); i++) {
            int step = Dungeon.findStep(this, target.pos, Dungeon.level.passable,
                    fieldOfView, true);
            if (step == -1 || step == pos || Actor.findChar(step) != null) break;
            move(step, false);
        }
        if (pos != oldPos) safeMoveSprite(oldPos, pos);
    }

    private boolean safeMoveSprite(int from, int to) {
        if (sprite == null || from == to) return false;
        if (Dungeon.level != null && Dungeon.level.heroFOV != null) {
            moveSprite(from, to);
            return sprite.isMoving;
        } else {
            sprite.place(to);
            return false;
        }
    }

    private int[] warningLine(Char target, int maximumSteps) {
        if (!validTarget(target)) return new int[0];
        Ballistica line = new Ballistica(pos, target.pos, Ballistica.STOP_SOLID);
        int length = Math.min(maximumSteps, Math.min(line.dist, line.path.size() - 1));
        int[] cells = new int[Math.max(1, length)];
        if (length == 0) cells[0] = target.pos;
        else for (int i = 0; i < length; i++) cells[i] = line.path.get(i + 1);
        return cells;
    }

    private void clearPendingSkill() {
        pendingSkill = Skill.NONE;
        pendingCells = new int[0];
        pendingTargetId = -1;
        pendingTargetCell = -1;
        pendingTurns = 0;
        pendingPaused = false;
        restoreGrace = false;
        comboStrikeIndex = 0;
    }

    private boolean rearmPending() {
        pendingPaused = false;
        showPendingTelegraph();
        if (sprite != null) yell(Messages.get(this, "rearm"));
        finishBossAction();
        spend(TICK);
        return true;
    }

    private boolean consumeRestoreGrace() {
        restoreGrace = false;
        pendingPaused = false;
        showPendingTelegraph();
        finishBossAction();
        spend(TICK);
        return true;
    }

    private void announceSkill(Skill skill) {
        if (sprite instanceof HungerKnightSprite) {
            HungerKnightSprite hungerSprite = (HungerKnightSprite) sprite;
            if (skill == Skill.THRUST || skill == Skill.COMBO) hungerSprite.thrust();
            else hungerSprite.charge();
        }
        if (sprite != null) yell(Messages.get(this, skill.name().toLowerCase()));
    }

    private void showPendingTelegraph() {
        if (sprite == null || sprite.parent == null || Dungeon.level == null) return;
        int color = pendingSkill == Skill.COMBO ? 0xE8C84A
                : pendingSkill == Skill.THRUST ? 0xD98232 : 0xD34B3F;
        for (int cell : pendingCells) {
            if (cell >= 0 && cell < Dungeon.level.length()) {
                sprite.parent.addToBack(new TargetedCell(cell, color));
            }
        }
    }

    private int heavyCooldownLength() { return phase == Phase.AWAKENING ? 8 : 7; }
    private int thrustCooldownLength() { return phase == Phase.AWAKENING ? 6 : 5; }

    @Override
    public void onMotionComplete() {
        super.onMotionComplete();
        if (thrustResolving) {
            thrustResolving = false;
            finishPendingSkill(Skill.THRUST);
            next();
        }
    }

    @Override
    public void onAttackComplete() {
        super.onAttackComplete();
        if (waitingForAttackCompletion) {
            waitingForAttackCompletion = false;
            finishBossAction();
        }
    }

    @Override
    public void notice() {
        super.notice();
        initializeEncounter();
        if (!noticeAnnounced) {
            noticeAnnounced = true;
            if (sprite != null) {
                String notice = Messages.get(this, "notice");
                if (talentAnnouncementPending) {
                    notice += "\n" + talentSelectionMessage(
                            talentByName(pendingOrdinaryTalent),
                            talentByName(pendingBossTalent));
                    clearPendingTalentAnnouncement();
                }
                yell(notice);
            }
        } else {
            flushPendingTalentAnnouncement();
        }
    }

    @Override
    public void cleanupArena(TowerBossLevel level) {
        cleanupEncounterEffects();
    }

    private void cleanupEncounterEffects() {
        comboResolving = false;
        comboCompletionHandled = true;
        if (sprite instanceof HungerKnightSprite) {
            ((HungerKnightSprite) sprite).cancelSpecialCallback();
        }
        Hero hero = Dungeon.hero;
        if (hero == null) return;
        HungerKnightEquipmentSeal.release(hero, id());
        HungerKnightOverburden.release(hero, id());
        if (!HungerKnightMagicLease.release(hero, id())) {
            HungerKnightMagicLease.releaseAll(hero);
        }
        if (phase == Phase.EXHAUSTION) {
            HungerKnightMagicLease.cleanupLegacyOrphanedImmunity(hero);
        }
        HungerKnightTalentSeal.release(hero, id());
        refreshHeroDerivedState(hero);
    }

    private static void refreshHeroDerivedState(Hero hero) {
        if (hero == null) return;
        hero.updateHT(false);
        if (Dungeon.level != null && Dungeon.level.heroFOV != null) Dungeon.observe();
    }

    /** Removes every orphaned Hunger-Knight source after recovery finds no live owner. */
    public static void cleanupOrphanedEffects(Hero hero) {
        if (hero == null) return;
        LinkedHashSet<Integer> owners = new LinkedHashSet<>();
        for (HungerKnightTalentSeal buff : hero.buffs(HungerKnightTalentSeal.class)) {
            owners.add(buff.ownerId());
        }
        for (HungerKnightEquipmentSeal buff : hero.buffs(HungerKnightEquipmentSeal.class)) {
            owners.add(buff.ownerId());
        }
        for (HungerKnightOverburden buff : hero.buffs(HungerKnightOverburden.class)) {
            owners.add(buff.ownerId());
        }
        for (HungerKnightMagicLease buff : hero.buffs(HungerKnightMagicLease.class)) {
            owners.add(buff.ownerId());
        }
        for (int owner : owners) {
            HungerKnightEquipmentSeal.release(hero, owner);
            HungerKnightOverburden.release(hero, owner);
            HungerKnightMagicLease.release(hero, owner);
            HungerKnightTalentSeal.release(hero, owner);
        }
        HungerKnightMagicLease.cleanupLegacyOrphanedImmunity(hero);
        refreshHeroDerivedState(hero);
    }

    @Override
    public void die(Object cause) {
        if (!rewardDropped) {
            rewardDropped = true;
            if (Dungeon.level != null) {
                Dungeon.level.drop(TowerBossRewardGenerator.createReward(
                        Dungeon.seed, Dungeon.depth, towerBossId()), pos).sprite.drop(pos);
            }
            if (sprite != null) yell(Messages.get(this, "defeated"));
        }
        cleanupEncounterEffects();
        super.die(cause);
    }

    public Phase phase() {
        return phase;
    }

    @Override
    public String description() {
        ensureEquipment();
        return Messages.get(this, "desc") + "\n\n"
                + Messages.get(this, "equipment", weapon.name(), armor.name());
    }

    Weapon weaponForTest() {
        ensureEquipment();
        return weapon;
    }

    Armor armorForTest() {
        ensureEquipment();
        return armor;
    }

    int finalDamageForTest(int damage) {
        return modifyFinalDamage(damage, null);
    }

    boolean transitionArmedForTest() {
        return transition == Transition.ARMED;
    }

    void completeTransitionForTest() {
        if (transition == Transition.NONE) transition = Transition.ARMED;
        advanceTransition();
    }

    void initializeEncounterForTest() { initializeEncounter(); }
    void cleanupEncounterForTest() { cleanupEncounterEffects(); }
    int heavyCooldownLengthForTest() { return heavyCooldownLength(); }
    int thrustCooldownLengthForTest() { return thrustCooldownLength(); }
    float phaseAttackDelayMultiplierForTest() { return phase == Phase.EXHAUSTION ? 0.75f : 1f; }
    void setPendingForTest(Skill skill, int[] cells, int targetCell, int turns) {
        pendingSkill = skill;
        pendingCells = cells == null ? new int[0] : cells.clone();
        pendingTargetCell = targetCell;
        pendingTurns = turns;
    }
    void setPendingPausedForTest(boolean value) { pendingPaused = value; }
    boolean pendingPausedForTest() { return pendingPaused; }
    Skill pendingSkillForTest() { return pendingSkill; }
    int[] pendingCellsForTest() { return pendingCells.clone(); }
    int pendingTargetCellForTest() { return pendingTargetCell; }
    int pendingTurnsForTest() { return pendingTurns; }
    boolean restoreGraceForTest() { return restoreGrace; }
    boolean consumeRestoreGraceForTest() { return consumeRestoreGrace(); }
    void forcePhaseForTest(Phase value, int locks) { phase = value; phaseLocks = locks; }
    int nextComboTriggerForTest() { return nextComboTrigger(); }
    void consumeComboTriggerForTest(int trigger) { comboTriggerBits |= trigger; }
    void setCooldownsForTest(int heavy, int thrust, int quake) {
        heavyCooldown = heavy;
        thrustCooldown = thrust;
        quakeCooldown = quake;
    }
    void finishBossActionForTest() { finishBossAction(); }
    int[] cooldownsForTest() { return new int[]{heavyCooldown, thrustCooldown, quakeCooldown}; }
    void forceAdaptationForTest(Adaptation value) { adaptation = value; }
    int meleeHungerForTest(Skill skill) { return meleeHungerFor(skill); }
    void setComboStrikeIndexForTest(int value) { comboStrikeIndex = value; }
    int comboStrikeIndexForTest() { return comboStrikeIndex; }
    int adaptationActionsForTest() { return adaptationActions; }

    static int equipmentLevelForRollForTest(int roll) {
        return equipmentLevelForRoll(Math.max(0, Math.min(99, roll)));
    }
    void setEquipmentLevelsForTest(int weapon, int armor) {
        weaponLevel = weapon;
        armorLevel = armor;
    }
    void setThrustResolvingForTest(boolean value) { thrustResolving = value; }
    boolean thrustResolvingForTest() { return thrustResolving; }

    int baseAccuracyForTest() { return 50; }
    int baseEvasionForTest() { return 25; }
    Class<? extends MeleeWeapon> weaponClassForTest() { return weaponClass; }
    int weaponLevelForTest() { return weaponLevel; }
    Class<? extends Weapon.Enchantment> weaponEnchantClassForTest() { return weaponEnchantClass; }
    Class<? extends Armor> armorClassForTest() { return armorClass; }
    int armorLevelForTest() { return armorLevel; }
    Class<? extends Armor.Glyph> armorGlyphClassForTest() { return armorGlyphClass; }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        if (weapon != null) bundle.put(WEAPON, weapon);
        if (armor != null) bundle.put(ARMOR, armor);
        bundle.put(WEAPON_CLASS, weaponClass.getName());
        bundle.put(WEAPON_LEVEL, weaponLevel);
        bundle.put(WEAPON_ENCHANT, weaponEnchantClass.getName());
        bundle.put(ARMOR_CLASS, armorClass.getName());
        bundle.put(ARMOR_LEVEL, armorLevel);
        bundle.put(ARMOR_GLYPH, armorGlyphClass.getName());
        bundle.put(PHASE, phase.ordinal());
        bundle.put(PHASE_LOCKS, phaseLocks);
        bundle.put(TRANSITION, transition.ordinal());
        bundle.put(ENCOUNTER_INITIALIZED, encounterInitialized);
        bundle.put(TALENT_PHASE_BITS, talentPhaseBits);
        bundle.put(SEALED_TALENTS, sealedTalentNames.toArray(new String[0]));
        bundle.put(ADAPTATION, adaptation.ordinal());
        bundle.put(BOSS_ACTIONS, bossActions);
        bundle.put(ADAPTATION_ACTIONS, adaptationActions);
        bundle.put(REWARD_DROPPED, rewardDropped);
        bundle.put(NOTICE_ANNOUNCED, noticeAnnounced);
        bundle.put(PENDING_SKILL, pendingSkill.ordinal());
        bundle.put(PENDING_CELLS, pendingCells);
        bundle.put(PENDING_TARGET_ID, pendingTargetId);
        bundle.put(PENDING_TARGET_CELL, pendingTargetCell);
        bundle.put(PENDING_TURNS, pendingTurns);
        bundle.put(PENDING_PAUSED, pendingPaused);
        bundle.put(HEAVY_COOLDOWN, heavyCooldown);
        bundle.put(THRUST_COOLDOWN, thrustCooldown);
        bundle.put(QUAKE_COOLDOWN, quakeCooldown);
        bundle.put(COMBO_TRIGGER_BITS, comboTriggerBits);
        bundle.put(COMBO_STRIKE_INDEX, comboStrikeIndex);
        bundle.put(TALENT_ANNOUNCEMENT_PENDING, talentAnnouncementPending);
        bundle.put(PENDING_ORDINARY_TALENT, pendingOrdinaryTalent);
        bundle.put(PENDING_BOSS_TALENT, pendingBossTalent);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        Weapon restoredWeapon = bundle.contains(WEAPON) ? (Weapon) bundle.get(WEAPON) : null;
        Armor restoredArmor = bundle.contains(ARMOR) ? (Armor) bundle.get(ARMOR) : null;
        if (restoredWeapon != null) weapon = restoredWeapon;
        if (restoredArmor != null) armor = restoredArmor;
        weaponClass = classAt(bundle.getString(WEAPON_CLASS), MeleeWeapon.class, weaponClass);
        weaponLevel = clampLevel(bundle.getInt(WEAPON_LEVEL));
        weaponEnchantClass = classAt(bundle.getString(WEAPON_ENCHANT),
                Weapon.Enchantment.class, weaponEnchantClass);
        armorClass = classAt(bundle.getString(ARMOR_CLASS), Armor.class, armorClass);
        armorLevel = clampLevel(bundle.getInt(ARMOR_LEVEL));
        armorGlyphClass = classAt(bundle.getString(ARMOR_GLYPH),
                Armor.Glyph.class, armorGlyphClass);
        phase = enumAt(Phase.values(), bundle.getInt(PHASE), Phase.AWAKENING);
        phaseLocks = Math.max(0, Math.min(3, bundle.getInt(PHASE_LOCKS)));
        transition = enumAt(Transition.values(), bundle.getInt(TRANSITION), Transition.NONE);
        encounterInitialized = bundle.getBoolean(ENCOUNTER_INITIALIZED);
        talentPhaseBits = Math.max(0, Math.min(7, bundle.getInt(TALENT_PHASE_BITS)));
        sealedTalentNames.clear();
        String[] names = bundle.getStringArray(SEALED_TALENTS);
        if (names != null) {
            for (String name : names) {
                try {
                    Talent.valueOf(name);
                    if (!sealedTalentNames.contains(name)) sealedTalentNames.add(name);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        adaptation = enumAt(Adaptation.values(), bundle.getInt(ADAPTATION), Adaptation.NONE);
        bossActions = Math.max(0, bundle.getInt(BOSS_ACTIONS));
        adaptationActions = Math.max(0, bundle.getInt(ADAPTATION_ACTIONS));
        rewardDropped = bundle.getBoolean(REWARD_DROPPED);
        noticeAnnounced = bundle.getBoolean(NOTICE_ANNOUNCED);
        pendingSkill = enumAt(Skill.values(), bundle.getInt(PENDING_SKILL), Skill.NONE);
        pendingCells = bundle.getIntArray(PENDING_CELLS);
        if (pendingCells == null) pendingCells = new int[0];
        pendingTargetId = bundle.contains(PENDING_TARGET_ID)
                ? bundle.getInt(PENDING_TARGET_ID) : -1;
        pendingTargetCell = bundle.contains(PENDING_TARGET_CELL)
                ? bundle.getInt(PENDING_TARGET_CELL) : -1;
        pendingTurns = pendingSkill == Skill.NONE ? 0
                : Math.max(1, bundle.getInt(PENDING_TURNS));
        pendingPaused = bundle.getBoolean(PENDING_PAUSED);
        heavyCooldown = Math.max(0, bundle.getInt(HEAVY_COOLDOWN));
        thrustCooldown = Math.max(0, bundle.getInt(THRUST_COOLDOWN));
        quakeCooldown = Math.max(0, bundle.getInt(QUAKE_COOLDOWN));
        comboTriggerBits = Math.max(0, Math.min(7, bundle.getInt(COMBO_TRIGGER_BITS)));
        comboStrikeIndex = Math.max(0, Math.min(3, bundle.getInt(COMBO_STRIKE_INDEX)));
        talentAnnouncementPending = bundle.getBoolean(TALENT_ANNOUNCEMENT_PENDING);
        pendingOrdinaryTalent = bundle.getString(PENDING_ORDINARY_TALENT);
        pendingBossTalent = bundle.getString(PENDING_BOSS_TALENT);
        if (pendingOrdinaryTalent == null) pendingOrdinaryTalent = "";
        if (pendingBossTalent == null) pendingBossTalent = "";
        if (pendingSkill == Skill.NONE || pendingCells.length == 0) clearPendingSkill();
        else restoreGrace = true;
    }

    private static <T> T enumAt(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    private static int clampLevel(int level) {
        return Math.max(0, Math.min(EQUIPMENT_LEVEL_WEIGHTS.length - 1, level));
    }

    private static boolean containsTag(DamageTag[] tags, DamageTag wanted) {
        if (tags == null) return false;
        for (DamageTag tag : tags) if (tag == wanted) return true;
        return false;
    }

    private static Talent talentByName(String name) {
        if (name == null || name.isEmpty()) return null;
        try { return Talent.valueOf(name); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> classAt(String name, Class<T> base,
                                                  Class<? extends T> fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            Class<?> resolved = Class.forName(name);
            return base.isAssignableFrom(resolved) ? (Class<? extends T>) resolved : fallback;
        } catch (ClassNotFoundException ignored) {
            return fallback;
        }
    }
}
