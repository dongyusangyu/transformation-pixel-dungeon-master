package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Corpse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TwistedMirror;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerLevelCamouflageGnollTest {

	@Test
	public void allTowerMonstersOwnEqualInteriorBuckets() {
		TowerMobRules.Selection[] selections = TowerMobRules.Selection.values();
		assertEquals(11, selections.length);
		for (int i = 0; i < selections.length; i++) {
			assertSame(selections[i], TowerMobRules.select((i + 0.25f) / selections.length));
			assertSame(selections[i], TowerMobRules.select((i + 0.75f) / selections.length));
		}
		assertSame(TowerMobRules.Selection.CAMOUFLAGE_GNOLL, selections[0]);
		assertSame(TowerMobRules.Selection.HEAVY_CRABIFICATION, selections[10]);
		assertTrue(Arrays.stream(TowerMobRules.Selection.values())
				.noneMatch(selection -> selection.name().contains("TWISTED_MIRROR")));
	}

	@Test
	public void naturalTowerSpawnsAlwaysStartWandering() {
		for (Mob mob : Arrays.asList(
				new CamouflageGnoll(),
				new CorrosiveSwarm(),
				new Corpse(),
				new EarthlySerpent(),
				new RoastLambWarlock(),
				new MechanicalFist(),
				new MimicCrocodile(),
				new Obscura(),
				new AlienatedPrismaticGuard(),
				new SoulCollector(),
				new HeavyCrabification())) {
			mob.state = mob.SLEEPING;
			TowerMobRules.prepareNaturalSpawn(mob);
			assertSame(mob.WANDERING, mob.state);
		}
		assertTrue(!(TowerMobRules.prepareNaturalSpawn(new AlienatedPrismaticGuard())
				instanceof TwistedMirror));
	}
}
