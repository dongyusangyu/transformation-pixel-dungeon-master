package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DuelistSubclassEnhancementTest {

	@Test
	public void comboRestoreDoesNotRequireAnAttachedHero() {
		Bundle bundle = new Bundle();
		bundle.put("count", 6);
		bundle.put("combotime", 5f);
		bundle.put("initialComboTime", 5f);

		Combo restored = new Combo();
		restored.restoreFromBundle(bundle);

		assertEquals(6, restored.getComboCount());
		assertEquals(2, restored.requirement(Combo.ComboMove.CLOBBER));
	}

	@Test
	public void championChargeCalculationsMatchDesign() {
		assertEquals(2, MeleeWeapon.Charger.championExtraCap(0));
		assertEquals(3, MeleeWeapon.Charger.championExtraCap(1));
		assertEquals(4, MeleeWeapon.Charger.championExtraCap(2));
		assertEquals(5, MeleeWeapon.Charger.championExtraCap(3));

		assertEquals(1.5f, MeleeWeapon.Charger.championRechargeMultiplier(0), 0.0001f);
		assertEquals(5f / 3f, MeleeWeapon.Charger.championRechargeMultiplier(1), 0.0001f);
		assertEquals(11f / 6f, MeleeWeapon.Charger.championRechargeMultiplier(2), 0.0001f);
		assertEquals(2f, MeleeWeapon.Charger.championRechargeMultiplier(3), 0.0001f);
	}

	@Test
	public void skilledParryCalculationsMatchDesign() {
		assertEquals(2f, SkilledParry.duration(1), 0.0001f);
		assertEquals(3f, SkilledParry.duration(2), 0.0001f);
		assertEquals(4f, SkilledParry.duration(3), 0.0001f);
		assertEquals(30f, SkilledParryCooldown.DURATION, 0.0001f);
		assertTrue(SkilledParry.canTrigger(null));
		SkilledParryCooldown cooldown = new SkilledParryCooldown();
		cooldown.ability = Object.class;
		assertFalse(SkilledParry.canTrigger(cooldown));
		assertTrue(SkilledParryCooldown.isDifferentAbility(null, Object.class));
		assertFalse(SkilledParryCooldown.isDifferentAbility(Object.class, Object.class));
		assertTrue(SkilledParryCooldown.isDifferentAbility(Object.class, String.class));
	}

	@Test
	public void sharedWeaponAbilitiesUseTheSameAbilityType() {
		assertAbilityType(Sword.class,
				WornShortsword.class, Shortsword.class, Sword.class, Longsword.class, Greatsword.class);
		assertAbilityType(Dagger.class, Dagger.class, Dirk.class, AssassinsBlade.class);
		assertAbilityType(Sai.class, Gloves.class, Sai.class, Gauntlet.class);
		assertAbilityType(Rapier.class, Rapier.class, Katana.class);
		assertAbilityType(Mace.class,
				Cudgel.class, HandAxe.class, Mace.class, BattleAxe.class, WarHammer.class);
		assertAbilityType(Quarterstaff.class, WalkStick.class, Quarterstaff.class);
		assertAbilityType(Spear.class, SpearOfConqueror.class, Spear.class, Glaive.class);
		assertAbilityType(Scimitar.class, Wakizashi.class, Scimitar.class);
		assertAbilityType(RoundShield.class, Bracer.class, RoundShield.class, Greatshield.class);
		assertAbilityType(Sickle.class, Sickle.class, WarScythe.class);
		assertAbilityType(Whip.class, LatherWhip.class, Whip.class, SteelWhip.class);
		assertAbilityType(Crossbow.class, Blowpipe.class, Crossbow.class);

		assertSame(Flail.class, MeleeWeapon.abilityType(Flail.class));
		assertFalse(SkilledParryCooldown.isDifferentAbility(Greatsword.class, Shortsword.class));
		assertTrue(SkilledParryCooldown.isDifferentAbility(Greatsword.class, Dagger.class));
	}

	@Test
	public void weaponTraitsCoverTalentWeaponFamilies() {
		assertTrue(MeleeWeapon.hasTrait(Dagger.class, MeleeWeapon.WeaponTrait.DAGGER));
		assertTrue(MeleeWeapon.hasTrait(RitualDagger.class, MeleeWeapon.WeaponTrait.DAGGER));
		assertFalse(MeleeWeapon.hasTrait(Sword.class, MeleeWeapon.WeaponTrait.DAGGER));

		assertTrue(new BlockingTestWeapon().hasTrait(MeleeWeapon.WeaponTrait.BLOCKING, null));
		assertFalse(new NonBlockingTestWeapon().hasTrait(MeleeWeapon.WeaponTrait.BLOCKING, null));
	}

	private static void assertAbilityType(Class<?> expected, Class<?>... weaponClasses) {
		for (Class<?> weaponClass : weaponClasses) {
			assertSame(weaponClass.getSimpleName(), expected, MeleeWeapon.abilityType(weaponClass));
		}
	}

	private static class BlockingTestWeapon extends MeleeWeapon {
		@Override
		public int defenseFactor(Char owner) {
			return 1;
		}
	}

	private static class NonBlockingTestWeapon extends MeleeWeapon {
	}

	@Test
	public void alternatingWeaponsCalculationsMatchDesign() {
		assertEquals(0.1f, AlternatingWeapons.initialBonus(), 0.0001f);
		assertEquals(0.2f, AlternatingWeapons.nextBonus(0.1f, 1), 0.0001f);
		assertEquals(0.3f, AlternatingWeapons.nextBonus(0.2f, 1), 0.0001f);
		assertEquals(0.3f, AlternatingWeapons.nextBonus(0.3f, 1), 0.0001f);
		assertEquals(0.5f, AlternatingWeapons.nextBonus(0.3f, 2), 0.0001f);
		assertEquals(0.7f, AlternatingWeapons.nextBonus(0.4f, 3), 0.0001f);
		assertEquals(1.7f, AlternatingWeapons.damageMultiplier(0.7f), 0.0001f);
	}

	@Test
	public void monkIntegerEnergyCalculationsMatchDesign() {
		assertEquals(1f, MonkEnergy.naturalWayBonus(20, 1), 0.0001f);
		assertEquals(1.5f, MonkEnergy.naturalWayBonus(30, 2), 0.0001f);
		assertEquals(2f, MonkEnergy.naturalWayBonus(30, 3), 0.0001f);

		assertEquals(3, MonkEnergy.displayedEnergy(3.99f));
		assertEquals(1.45f, MonkEnergy.innerPeaceEvasionMultiplier(3.99f, 1), 0.0001f);
		assertEquals(1.525f, MonkEnergy.innerPeaceEvasionMultiplier(3.99f, 2), 0.0001f);
		assertEquals(1.6f, MonkEnergy.innerPeaceEvasionMultiplier(3.99f, 3), 0.0001f);
	}

	@Test
	public void yinYangUsesVisibleWholeResourcesAndRoundsShortageUp() {
		assertEquals(1, MonkEnergy.missingWeaponCharges(1, 0.99f, 2f));
		assertEquals(1, MonkEnergy.missingWeaponCharges(0, 0.99f, 1f));
		assertEquals(0, MonkEnergy.missingWeaponCharges(2, 0f, 2f));

		assertEquals(3, MonkEnergy.yinYangEnergyCost(1, 1));
		assertEquals(2, MonkEnergy.yinYangEnergyCost(1, 2));
		assertEquals(1, MonkEnergy.yinYangEnergyCost(1, 3));
		assertTrue(MonkEnergy.canCoverWeaponChargeShortage(3.99f, 1, 1));
		assertFalse(MonkEnergy.canCoverWeaponChargeShortage(2.99f, 1, 1));

		MonkEnergy energy = new MonkEnergy();
		energy.energy = 3.99f;
		energy.spendForYinYang(1, 1);
		assertEquals(0.99f, energy.energy, 0.0001f);
	}

	@Test
	public void duelistSubclassPoolsContainNewCandidatesButDefaultsStayUnchanged() {
		assertEquals(696, Talent.WEAPON_ABILITY_MASTER.icon());
		assertEquals(697, Talent.SKILLED_PARRY.icon());
		assertEquals(698, Talent.ALTERNATING_WEAPONS.icon());
		assertEquals(699, Talent.NATURAL_WAY.icon());
		assertEquals(700, Talent.INNER_PEACE.icon());
		assertEquals(701, Talent.YIN_YANG_BALANCE.icon());

		List<Talent> championPool = Talent.subclassTalentPool(HeroSubClass.CHAMPION);
		assertEquals(6, championPool.size());
		assertTrue(championPool.contains(Talent.WEAPON_ABILITY_MASTER));
		assertTrue(championPool.contains(Talent.SKILLED_PARRY));
		assertTrue(championPool.contains(Talent.ALTERNATING_WEAPONS));

		List<Talent> monkPool = Talent.subclassTalentPool(HeroSubClass.MONK);
		assertEquals(6, monkPool.size());
		assertTrue(monkPool.contains(Talent.NATURAL_WAY));
		assertTrue(monkPool.contains(Talent.INNER_PEACE));
		assertTrue(monkPool.contains(Talent.YIN_YANG_BALANCE));

		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		Talent.initSubclassTalents(HeroSubClass.CHAMPION, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.WEAPON_ABILITY_MASTER));

		talents.clear();
		Talent.initSubclassTalents(HeroSubClass.MONK, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.NATURAL_WAY));
	}
}
