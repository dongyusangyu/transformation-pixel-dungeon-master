package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.DeathKnightExecutionMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightTalentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightEquipmentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightMagicLease;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.HundredTonHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhip;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessGameMessages;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Signal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TowerBossPresentationTest {
    private HeadlessGameMessages messages;
    private HeadlessItemSprites sprites;
    private Signal<String> oldLog;
    private Level oldLevel;
    private Hero oldHero;
    private Object oldScene;
    private final ArrayList<String> log = new ArrayList<>();

    @Before public void setUp() throws Exception {
        messages = new HeadlessGameMessages();
        sprites = new HeadlessItemSprites();
        sprites.addSheet(Assets.Effects.DEATH_KNIGHT_SLASH, 64, 16);
        oldLog = GLog.update;
        GLog.update = new Signal<>();
        GLog.update.add(text -> { log.add(text); return false; });
        oldLevel = Dungeon.level;
        oldHero = Dungeon.hero;
        oldScene = field(GameScene.class, "scene").get(null);
        field(GameScene.class, "scene").set(null, null);
    }

    @After public void tearDown() throws Exception {
        field(GameScene.class, "scene").set(null, oldScene);
        Dungeon.level = oldLevel;
        Dungeon.hero = oldHero;
        GLog.update = oldLog;
        sprites.close();
        messages.close();
    }

    @Test public void encounterBuffsHaveDistinctInspectableIconsAndTints() {
        Buff[] buffs = {new DeathKnightExecutionMark(), new HungerKnightTalentSeal(),
                new HungerKnightEquipmentSeal(), new HungerKnightMagicLease()};
        int[] icons = {BuffIndicator.MARK, BuffIndicator.DEGRADE, BuffIndicator.GLYPH_RECALL, BuffIndicator.WAND};
        java.util.HashSet<Integer> seen = new java.util.HashSet<>();
        for (int i = 0; i < buffs.length; i++) {
            assertEquals(icons[i], buffs[i].icon());
            assertTrue(seen.add(buffs[i].icon()));
            assertNotEquals(BuffIndicator.NONE, buffs[i].icon());
            assertFalse(buffs[i].name().startsWith("java.lang.object"));
            assertFalse(buffs[i].desc().startsWith("java.lang.object"));
            assertEquals(0f, buffs[i].iconFadePercent(), 0f);
            Image icon = new Image();
            buffs[i].tintIcon(icon);
            assertTrue(icon.rm != 1f || icon.gm != 1f || icon.bm != 1f);
        }
        assertEquals(Buff.buffType.NEUTRAL, buffs[3].type);
        assertFalse(buffs[3].announced);
    }

    @Test public void deathMarkDescriptionFormatsCurrentStacksInBothLanguages() {
        Gnoll target = new Gnoll();
        DeathKnightExecutionMark mark = DeathKnightExecutionMark.addHit(target, 6001);
        DeathKnightExecutionMark.addHit(target, 6001);
        for (Languages language : new Languages[]{Languages.CHI_SMPL, Languages.ENGLISH}) {
            Messages.setup(language);
            assertEquals("2", mark.iconTextDisplay());
            assertTrue(mark.desc().contains("2"));
            assertFalse(mark.desc().contains("%d"));
            assertFalse(mark.desc().contains("%%"));
        }
        mark.detach();
    }

    @Test public void independentDeathKnightsDoNotCombineTheirDisplayedExecutionThresholds() {
        Gnoll target = new Gnoll();
        target.HT = 100;
        target.HP = 11;
        DeathKnightExecutionMark mark = DeathKnightExecutionMark.addHit(target, 6001);
        DeathKnightExecutionMark.addHit(target, 6001);
        DeathKnightExecutionMark.addHit(target, 6002);
        assertEquals(3, mark.stacks());
        assertEquals("2", mark.iconTextDisplay());
        assertTrue(mark.desc().contains(Messages.get(mark, "desc_multiple")));
        assertFalse(mark.shouldExecute());
        Bundle saved = new Bundle();
        mark.storeInBundle(saved);
        mark.detach();
        DeathKnightExecutionMark restored = new DeathKnightExecutionMark();
        restored.restoreFromBundle(saved);
        assertTrue(restored.attachTo(target));
        assertEquals("2", restored.iconTextDisplay());
        DeathKnightExecutionMark.clear(target, 6001);
        assertEquals("1", restored.iconTextDisplay());
        DeathKnightExecutionMark.clear(target, 6002);
        assertNull(target.buff(DeathKnightExecutionMark.class));
    }

    @Test public void talentLossNoticesAreNegativeAndAreOnlyDeliveredOnceWithoutASprite() throws Exception {
        HungerKnight boss = new HungerKnight();
        invoke(boss, "announceTalentSelection", new Class<?>[]{Talent.class, Talent.class},
                Talent.HEARTY_MEAL, Talent.IRON_STOMACH);
        invoke(boss, "flushPendingTalentAnnouncement", new Class<?>[0]);
        invoke(boss, "flushPendingTalentAnnouncement", new Class<?>[0]);
        assertEquals(Arrays.asList(GLog.NEGATIVE + Messages.get(boss, "swallow_pair",
                Talent.HEARTY_MEAL.title(), Talent.IRON_STOMACH.title())), log);
    }

    @Test public void noTalentNoticeUsesTheSinglePositiveSentenceNotThePairTemplate() throws Exception {
        HungerKnight boss = new HungerKnight();
        invoke(boss, "announceTalentSelection", new Class<?>[]{Talent.class, Talent.class}, null, null);
        invoke(boss, "flushPendingTalentAnnouncement", new Class<?>[0]);
        assertEquals(Arrays.asList(GLog.POSITIVE + Messages.get(boss, "swallow_none")), log);
    }

    @Test public void singleTalentLossAndRearmedRitualHaveTheirOwnLogColors() throws Exception {
        HungerKnight boss = new HungerKnight();
        invoke(boss, "announceTalentSelection", new Class<?>[]{Talent.class, Talent.class}, Talent.HEARTY_MEAL, null);
        invoke(boss, "flushPendingTalentAnnouncement", new Class<?>[0]);
        Dungeon.level = null;
        invoke(boss, "rearmPending", new Class<?>[0]);
        assertEquals(Arrays.asList(GLog.NEGATIVE + Messages.get(boss, "swallow_single", Talent.HEARTY_MEAL.title()),
                GLog.WARNING + Messages.get(boss, "rearm")), log);
    }

    @Test public void toastWarningsAreNarrativeButRealBossDialogueKeepsYellFormatting() throws Exception {
        GentlemanElf boss = new GentlemanElf();
        invoke(boss, "announce", new Class<?>[]{String.class}, "toast");
        assertEquals(Arrays.asList(GLog.WARNING + Messages.get(boss, "toast")), log);
        log.clear();
        boss.sprite = new CharSprite();
        invoke(boss, "announce", new Class<?>[]{String.class}, "devour");
        assertEquals(2, log.size());
        assertTrue(log.get(1).startsWith(GLog.NEGATIVE));
        assertTrue(log.get(1).contains("\""));
    }

    @Test public void pestilencePhaseNotificationsAreUnprefixedNarrativeLogsWithoutASprite() throws Exception {
        PestilenceKnight boss = new PestilenceKnight(5);
        for (String key : new String[]{"phase_outbreak", "phase_terminal"}) {
            invoke(boss, "announceSkill", new Class<?>[]{String.class, Object[].class}, key, new Object[]{40});
            assertEquals(GLog.NEGATIVE + Messages.get(boss, key, 40), log.get(log.size() - 1));
            assertFalse(log.get(log.size() - 1).contains("%d"));
        }
        assertEquals(2, log.size());
    }

    @Test public void cupRespawnNoticeDoesNotDependOnBossSpriteAndDoesNotRepeat() throws Exception {
        TowerBossLevel level = new TowerBossLevel();
        level.setSize(29, 35);
        level.mobs = new java.util.HashSet<>();
        level.transitions = new ArrayList<>();
        Arrays.fill(level.passable, true);
        Dungeon.level = level;
        GentlemanElf boss = new GentlemanElf();
        boss.pos = 14 + 15 * 29;
        level.prepareGentlemanElfArena(boss);
        GentlemanElfArena.Host host = (GentlemanElfArena.Host) invoke(level, "gentlemanElfHost",
                new Class<?>[]{GentlemanElf.class}, boss);
        assertTrue(host.respawnCup());
        assertEquals(Arrays.asList(GLog.NEGATIVE + Messages.get(boss, "cup_spawn")), log);
        assertFalse(host.respawnCup());
        assertEquals(1, log.size());
    }

    @Test public void swordWaveIsInsertedIntoEffectsRatherThanTheMonsterGroup() throws Exception {
        Group effects = new Group();
        Group mobs = new Group();
        GameScene scene = allocate(GameScene.class);
        field(GameScene.class, "effects").set(scene, effects);
        field(GameScene.class, "scene").set(null, scene);
        TowerBossLevel level = new TowerBossLevel();
        level.setSize(29, 35);
        Dungeon.level = level;
        DeathKnight boss = new DeathKnight();
        boss.pos = 14 + 15 * 29;
        boss.sprite = new CharSprite();
        mobs.add(boss.sprite);
        field(DeathKnight.class, "pendingCells").set(boss, new int[]{boss.pos + 1});
        field(DeathKnight.class, "pendingBands").set(boss, new DeathKnightBombardment.Band[]{DeathKnightBombardment.Band.CORE});
        SPDSettings.charAnimations(true);
        invoke(boss, "showBombardmentFx", new Class<?>[0]);
        assertEquals(1, effects.length);
        assertEquals(1, mobs.length);
        SPDSettings.charAnimations(false);
        invoke(boss, "showBombardmentFx", new Class<?>[0]);
        assertEquals(1, effects.length);
    }

    @Test public void upgradePreviewRowLabelsExistInBothLanguages() {
        for (Languages language : new Languages[]{Languages.CHI_SMPL, Languages.ENGLISH}) {
            Messages.setup(language);
            assertFalse(Messages.get(WndUpgrade.class, "ability_ambush_damage").startsWith("java.lang.object"));
            assertFalse(Messages.get(WndUpgrade.class, "feature_attack_range").startsWith("java.lang.object"));
        }
    }

    @Test public void hammerAbilityPreviewAcceptsEverySupportedSourceOfWeaponAbilities() throws Exception {
        Hero hero = TestHeroFactory.create();
        hero.heroClass = HeroClass.WARRIOR;
        hero.subClass = HeroSubClass.NONE;
        Dungeon.hero = hero;
        WndUpgrade window = allocate(WndUpgrade.class);
        HundredTonHammer hammer = new HundredTonHammer();
        Class<?>[] signature = {Item.class, int.class};
        assertEquals(false, invoke(window, "canViewWeaponAbilityUpgrade", signature, hammer, 0));
        hero.heroClass = HeroClass.DUELIST;
        assertEquals(true, invoke(window, "canViewWeaponAbilityUpgrade", signature, hammer, 0));
        hero.heroClass = HeroClass.WARRIOR;
        hero.subClass = HeroSubClass.CHAMPION;
        assertEquals(true, invoke(window, "canViewWeaponAbilityUpgrade", signature, hammer, 0));
        hero.subClass = HeroSubClass.NONE;
        java.util.LinkedHashMap<Talent, Integer> talents = new java.util.LinkedHashMap<>();
        talents.put(Talent.MARTIAL_TRAIN, 1);
        hero.talents.add(talents);
        assertEquals(true, invoke(window, "canViewWeaponAbilityUpgrade", signature, hammer, 0));
        hero.talents.clear();
        Buff.affect(hero, MeleeWeapon.MartialMastery.class);
        assertEquals(true, invoke(window, "canViewWeaponAbilityUpgrade", signature, hammer, 0));
        Buff.detach(hero, MeleeWeapon.MartialMastery.class);
    }

    @Test public void ordinaryHeroesStillGetTailWhipAttackRangeWithoutAnAbilityPreview() throws Exception {
        Hero hero = TestHeroFactory.create();
        hero.heroClass = HeroClass.WARRIOR;
        hero.subClass = HeroSubClass.NONE;
        Dungeon.hero = hero;
        DemonTailWhip whip = new DemonTailWhip();
        assertEquals(false, invoke(allocate(WndUpgrade.class), "canViewWeaponAbilityUpgrade",
                new Class<?>[]{Item.class, int.class}, whip, 6));
        assertEquals("3", whip.upgradeFeatureStats(6).get(0).value);
        assertEquals("4", whip.upgradeFeatureStats(7).get(0).value);
        assertEquals("feature_attack_range", whip.upgradeFeatureStats(7).get(0).type.messageKey());
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        return type.cast(((Unsafe) field(Unsafe.class, "theUnsafe").get(null)).allocateInstance(type));
    }
}
