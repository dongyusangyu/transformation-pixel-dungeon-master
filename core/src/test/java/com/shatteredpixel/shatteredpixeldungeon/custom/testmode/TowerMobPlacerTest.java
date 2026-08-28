package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DarkMechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MyriadBlackShadow;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TapirCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RuneSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TwistedMirror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.PowerfulWraith;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerMobPlacerTest {

	@Test
	public void towerMobCatalogMatchesBestiaryRegistry() {
		assertEquals(15, TowerMobPlacer.MOBS_PER_PAGE);
		assertEquals(5, TowerMobPlacer.GRID_COLUMNS);
		assertEquals(3, TowerMobPlacer.gridRowsForCount(15));

		List<Class<?>> registeredMobs = new ArrayList<>();
		for (Class<?> entity : Bestiary.TOWER_MOBS.entities()) {
			if (Mob.class.isAssignableFrom(entity)) {
				registeredMobs.add(entity);
			}
		}

		List<Class<?>> placerMobs = new ArrayList<>();
		for (int page = 0; page < TowerMobPlacer.pageCount(); page++) {
			placerMobs.addAll(TowerMobPlacer.mobsOnPage(page));
		}

		assertEquals(registeredMobs, placerMobs);
		assertEquals(registeredMobs.size(), TowerMobPlacer.mobCount());
		assertTrue(placerMobs.contains(EarthlySerpent.class));
		assertTrue(placerMobs.contains(RoastLambWarlock.class));
		assertTrue(placerMobs.contains(MechanicalFist.class));
		assertTrue(placerMobs.contains(MimicCrocodile.class));
		assertTrue(placerMobs.contains(Obscura.class));
		assertTrue(placerMobs.contains(AlienatedPrismaticGuard.class));
		assertTrue(placerMobs.contains(SoulCollector.class));
		assertTrue(placerMobs.contains(HeavyCrabification.class));
		assertTrue(placerMobs.contains(RuneSpinner.class));
		assertTrue(placerMobs.contains(ChainShadowThief.class));
		assertTrue(placerMobs.contains(DeathButterfly.class));
		assertTrue(placerMobs.contains(DarkMechanicalFist.class));
		assertTrue(placerMobs.contains(MyriadBlackShadow.class));
		assertTrue(placerMobs.contains(TapirCrocodile.class));
		assertTrue(!placerMobs.contains(TwistedMirror.class));
		assertTrue(!placerMobs.contains(PowerfulWraith.class));
	}

	@Test
	public void toolSleepsOrdinaryTowerMobsButNotMimicCrocodile() {
		Mob ordinary = new CamouflageGnoll();
		ordinary.state = ordinary.WANDERING;
		TowerMobPlacer.applyInitialState(ordinary);
		assertTrue(ordinary.state == ordinary.SLEEPING);

		Mob obscura = new Obscura();
		obscura.state = obscura.WANDERING;
		TowerMobPlacer.applyInitialState(obscura);
		assertTrue(obscura.state == obscura.SLEEPING);

		Mob alienatedGuard = new AlienatedPrismaticGuard();
		alienatedGuard.state = alienatedGuard.WANDERING;
		TowerMobPlacer.applyInitialState(alienatedGuard);
		assertTrue(alienatedGuard.state == alienatedGuard.SLEEPING);

		Mob collector = new SoulCollector();
		collector.state = collector.WANDERING;
		TowerMobPlacer.applyInitialState(collector);
		assertTrue(collector.state == collector.SLEEPING);

		Mob heavyCrab = new HeavyCrabification();
		heavyCrab.state = heavyCrab.WANDERING;
		TowerMobPlacer.applyInitialState(heavyCrab);
		assertTrue(heavyCrab.state == heavyCrab.SLEEPING);

		Mob runeSpinner = new RuneSpinner();
		runeSpinner.state = runeSpinner.WANDERING;
		TowerMobPlacer.applyInitialState(runeSpinner);
		assertTrue(runeSpinner.state == runeSpinner.SLEEPING);

		Mob chainShadowThief = new ChainShadowThief();
		chainShadowThief.state = chainShadowThief.WANDERING;
		TowerMobPlacer.applyInitialState(chainShadowThief);
		assertTrue(chainShadowThief.state == chainShadowThief.SLEEPING);

		Mob deathButterfly = new DeathButterfly();
		deathButterfly.state = deathButterfly.WANDERING;
		TowerMobPlacer.applyInitialState(deathButterfly);
		assertTrue(deathButterfly.state == deathButterfly.SLEEPING);

		Mob darkMechanicalFist = new DarkMechanicalFist();
		darkMechanicalFist.state = darkMechanicalFist.WANDERING;
		TowerMobPlacer.applyInitialState(darkMechanicalFist);
		assertTrue(darkMechanicalFist.state == darkMechanicalFist.SLEEPING);

		Mob myriad = new MyriadBlackShadow();
		myriad.state = myriad.WANDERING;
		TowerMobPlacer.applyInitialState(myriad);
		assertTrue(myriad.state == myriad.SLEEPING);

		Mob crocodile = new MimicCrocodile();
		crocodile.state = crocodile.SLEEPING;
		TowerMobPlacer.applyInitialState(crocodile);
		assertTrue(crocodile.state == crocodile.WANDERING);

		Mob tapir = new TapirCrocodile();
		tapir.state = tapir.SLEEPING;
		TowerMobPlacer.applyInitialState(tapir);
		assertTrue(tapir.state == tapir.WANDERING);
	}

	@Test
	public void pageOutsideTowerMobCatalogIsEmpty() {
		assertTrue(TowerMobPlacer.mobsOnPage(-1).isEmpty());
		assertEquals(java.util.Arrays.asList(DarkMechanicalFist.class, MyriadBlackShadow.class,
				TapirCrocodile.class),
				TowerMobPlacer.mobsOnPage(1));
		assertTrue(TowerMobPlacer.mobsOnPage(2).isEmpty());
	}
}
