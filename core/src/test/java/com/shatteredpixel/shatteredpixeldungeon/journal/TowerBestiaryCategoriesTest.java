package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Corpse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.PowerfulWraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TwistedMirror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.WildDread;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerBestiaryCategoriesTest {

	@Test
	public void towerCategoriesAreTheFinalBestiaryGroups() {
		Bestiary[] categories = Bestiary.values();

		assertSame(Bestiary.TOWER_MOBS, categories[categories.length - 2]);
		assertSame(Bestiary.TOWER_BOSSES, categories[categories.length - 1]);
	}

	@Test
	public void towerCreaturesAreIsolatedFromNormalRegionalEnemies() {
		assertEquals(Arrays.asList(
				CamouflageGnoll.class,
				CorrosiveSwarm.class,
				Corpse.class,
				EarthlySerpent.class,
				RoastLambWarlock.class,
				MechanicalFist.class,
				MimicCrocodile.class,
				Obscura.class,
				AlienatedPrismaticGuard.class,
				SoulCollector.class,
				HeavyCrabification.class
		), new ArrayList<>(Bestiary.TOWER_MOBS.entities()));
		assertTrue(!Bestiary.REGIONAL.entities().contains(CamouflageGnoll.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(CorrosiveSwarm.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(Corpse.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(EarthlySerpent.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(RoastLambWarlock.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(MechanicalFist.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(MimicCrocodile.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(Obscura.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(AlienatedPrismaticGuard.class));
		assertTrue(!Bestiary.TOWER_MOBS.entities().contains(WildDread.class));
		assertTrue(!Bestiary.TOWER_MOBS.entities().contains(TwistedMirror.class));
		assertTrue(!Bestiary.TOWER_MOBS.entities().contains(PowerfulWraith.class));
		assertEquals(Arrays.asList(PestilenceKnight.class),
				new ArrayList<>(Bestiary.TOWER_BOSSES.entities()));
	}

	@Test
	public void everyTowerCreatureUsesLevelThirtyExperienceCap() {
		assertEquals(30, new CamouflageGnoll().maxLvl);
		assertEquals(30, new CorrosiveSwarm().maxLvl);
		assertEquals(30, new Corpse().maxLvl);
		assertEquals(30, new EarthlySerpent().maxLvl);
		assertEquals(30, new RoastLambWarlock().maxLvl);
		assertEquals(30, new MechanicalFist().maxLvl);
		assertEquals(30, new MimicCrocodile().maxLvl);
		assertEquals(30, new Obscura().maxLvl);
		assertEquals(30, new AlienatedPrismaticGuard().maxLvl);
		assertEquals(30, new SoulCollector().maxLvl);
		assertEquals(13, new AlienatedPrismaticGuard().EXP);
		assertEquals(30, new WildDread().maxLvl);
		assertEquals(30, new PowerfulWraith().maxLvl);
		assertEquals(0, new PowerfulWraith().EXP);
	}

	@Test
	public void alienatedReplicaMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String guard = "actors.mobs.tmobs.alienatedprismaticguard.";
		String mirror = "actors.mobs.tmobs.twistedmirror.";

		assertEquals("alienated prismatic guard", defaults.getProperty(guard + "name"));
		assertTrue(defaults.getProperty(guard + "desc", "").contains("battle rings"));
		assertEquals("twisted mirror", defaults.getProperty(mirror + "name"));
		assertTrue(defaults.getProperty(mirror + "desc", "").contains("weapon"));
		assertEquals("异化虹卫", chinese.getProperty(guard + "name"));
		assertTrue(chinese.getProperty(guard + "desc", "").contains("战斗戒指"));
		assertEquals("扭曲镜像", chinese.getProperty(mirror + "name"));
		assertTrue(chinese.getProperty(mirror + "desc", "").contains("武器"));
	}

	@Test
	public void soulCollectorMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String collector = "actors.mobs.tmobs.soulcollector.";
		String wraith = "actors.mobs.tmobs.powerfulwraith.";

		assertEquals("soul collector", defaults.getProperty(collector + "name"));
		assertTrue(defaults.getProperty(collector + "desc", "").contains("three full turns"));
		assertEquals("powerful wraith", defaults.getProperty(wraith + "name"));
		assertTrue(defaults.getProperty(wraith + "desc", "").contains("10 health"));
		assertTrue(!chinese.getProperty(collector + "name", "").isEmpty());
		assertTrue(!chinese.getProperty(collector + "desc", "").isEmpty());
		assertTrue(!chinese.getProperty(wraith + "name", "").isEmpty());
		assertTrue(!chinese.getProperty(wraith + "desc", "").isEmpty());
	}

	@Test
	public void obscuraMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");

		assertEquals("the obscura",
				defaults.getProperty("actors.mobs.tmobs.obscura.name"));
		assertTrue(defaults.getProperty("actors.mobs.tmobs.obscura.desc", "")
				.contains("wild dread"));
		assertEquals("wild dread",
				defaults.getProperty("actors.mobs.tmobs.wilddread.name"));
		assertTrue(defaults.getProperty("actors.mobs.tmobs.wilddread.desc", "")
				.contains("revives"));

		assertEquals("胧光怪",
				chinese.getProperty("actors.mobs.tmobs.obscura.name"));
		assertTrue(chinese.getProperty("actors.mobs.tmobs.obscura.desc", "")
				.contains("野生惧魔"));
		assertEquals("野生惧魔",
				chinese.getProperty("actors.mobs.tmobs.wilddread.name"));
		assertTrue(chinese.getProperty("actors.mobs.tmobs.wilddread.desc", "")
				.contains("复活"));
	}

	@Test
	public void mimicCrocodileMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String prefix = "actors.mobs.tmobs.mimiccrocodile.";

		assertEquals("mimic crocodile", defaults.getProperty(prefix + "name"));
		assertTrue(defaults.getProperty(prefix + "desc", "").contains("lurking"));
		assertEquals("拟态鳄鱼", chinese.getProperty(prefix + "name"));
		assertTrue(chinese.getProperty(prefix + "desc", "").contains("潜伏"));
	}

	@Test
	public void mechanicalFistMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String prefix = "actors.mobs.tmobs.mechanicalfist.";

		assertEquals("mechanical fist", defaults.getProperty(prefix + "name"));
		assertTrue(defaults.getProperty(prefix + "desc", "").contains("three tiles"));
		assertEquals("机械拳头", chinese.getProperty(prefix + "name"));
		assertTrue(chinese.getProperty(prefix + "desc", "").contains("三格"));
		assertTrue(!defaults.getProperty(prefix + "discover_hint", "").isEmpty());
		assertTrue(!chinese.getProperty(prefix + "discover_hint", "").isEmpty());
	}

	@Test
	public void roastLambWarlockMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String prefix = "actors.mobs.tmobs.roastlambwarlock.";

		assertEquals("roast lamb warlock", defaults.getProperty(prefix + "name"));
		assertTrue(defaults.getProperty(prefix + "desc", "").contains("sheep"));
		assertEquals("烤全羊术士", chinese.getProperty(prefix + "name"));
		assertTrue(chinese.getProperty(prefix + "desc", "").contains("绵羊"));
	}

	@Test
	public void earthlySerpentMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String prefix = "actors.mobs.tmobs.earthlyserpent.";

		assertEquals("earthly serpent", defaults.getProperty(prefix + "name"));
		assertTrue(defaults.getProperty(prefix + "desc").contains("corrosive mist"));
		assertEquals("尘世巨蟒", chinese.getProperty(prefix + "name"));
		assertTrue(chinese.getProperty(prefix + "desc").contains("酸雾"));
	}

	@Test
	public void pestilenceKnightMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String prefix = "actors.mobs.tboss.pestilenceknight.";

		assertEquals("Pestilence Knight", defaults.getProperty(prefix + "name"));
		assertTrue(defaults.getProperty(prefix + "desc", "").contains("Braziers"));
		assertTrue(defaults.getProperty(prefix + "harvest", "").contains("Harvest"));
		assertTrue(!defaults.getProperty("actors.buffs.tboss.terminalhealingpenalty.name", "").isEmpty());
		assertEquals("瘟疫骑士", chinese.getProperty(prefix + "name"));
		assertTrue(chinese.getProperty(prefix + "desc", "").contains("香炉"));
		assertTrue(!chinese.getProperty(prefix + "pale_charge", "").isEmpty());
		assertTrue(!chinese.getProperty("actors.buffs.tboss.terminalhealingpenalty.desc", "").isEmpty());
	}

	@Test
	public void towerCategoryTitlesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadJournalMessages("journal.properties");
		Properties chinese = loadJournalMessages("journal_zh.properties");

		assertEquals("tower creatures",
				defaults.getProperty("journal.bestiary.tower_mobs.title"));
		assertEquals("tower bosses",
				defaults.getProperty("journal.bestiary.tower_bosses.title"));
		assertEquals("高塔生物",
				chinese.getProperty("journal.bestiary.tower_mobs.title"));
		assertEquals("高塔 Boss",
				chinese.getProperty("journal.bestiary.tower_bosses.title"));
	}

	private static Properties loadJournalMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/journal")
				.resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/actors")
				.resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
