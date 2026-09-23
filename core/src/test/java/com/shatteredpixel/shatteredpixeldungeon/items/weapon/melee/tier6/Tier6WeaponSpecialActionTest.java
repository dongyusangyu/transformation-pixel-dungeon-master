package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WeaponSpecialAction;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.PortableBlackHole;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Visual;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Tier6WeaponSpecialActionTest {

	@After
	public void clearStatics() {
		Dungeon.hero = null;
		ActionIndicator1.clearAction();
	}

	@Test
	public void onlyFourRecurringTierSixActionsUseTheIndicator() {
		assertTrue(WeaponSpecialAction.class.isAssignableFrom(ChainMace.class));
		assertTrue(WeaponSpecialAction.class.isAssignableFrom(LakeSword.class));
		assertTrue(WeaponSpecialAction.class.isAssignableFrom(MercuryBlade.class));
		assertTrue(WeaponSpecialAction.class.isAssignableFrom(MountainGuard.class));

		assertFalse(WeaponSpecialAction.class.isAssignableFrom(AuxiliaryCore.class));
		assertFalse(WeaponSpecialAction.class.isAssignableFrom(SakuraBlossomBlade.class));
		assertFalse(WeaponSpecialAction.class.isAssignableFrom(VenomousSickle.class));
		assertFalse(WeaponSpecialAction.class.isAssignableFrom(PalermoSword.class));
		assertFalse(WeaponSpecialAction.class.isAssignableFrom(Gungnir.class));
		assertFalse(WeaponSpecialAction.class.isAssignableFrom(PortableBlackHole.class));
	}

	@Test
	public void specialActionBelongsOnlyToTheEffectiveMainHand() {
		Hero hero = hero();
		ChainMace mace = new ChainMace();

		hero.belongings.secondWep = mace;
		assertFalse(((WeaponSpecialAction) mace).usable());

		hero.belongings.secondWep = null;
		hero.belongings.weapon = mace;
		assertTrue(((WeaponSpecialAction) mace).usable());

		hero.belongings.lostInventory(true);
		assertFalse(((WeaponSpecialAction) mace).usable());
	}

	@Test
	public void specialActionRequiresEnoughStrengthForCurrentWeaponRequirement() {
		Hero hero = hero();
		ChainMace mace = new ChainMace();
		hero.belongings.weapon = mace;
		int requirement = mace.STRReq();

		hero.STR = requirement - 1;
		assertFalse(mace.usable());

		hero.STR = requirement;
		assertTrue(mace.usable());

		hero.STR = requirement + 1;
		assertTrue(mace.usable());
	}

	@Test
	public void mountainReleaseAppearsOnlyAtFullEnergyInMainHand() {
		Hero hero = hero();
		MountainGuard guard = TestHeroFactory.allocateItem(MountainGuard.class);
		hero.belongings.weapon = guard;

		assertFalse(((WeaponSpecialAction) guard).usable());
		guard.addEnergy(99);
		assertFalse(((WeaponSpecialAction) guard).usable());
		guard.addEnergy(1);
		assertTrue(((WeaponSpecialAction) guard).usable());

		hero.belongings.weapon = null;
		hero.belongings.secondWep = guard;
		assertFalse(((WeaponSpecialAction) guard).usable());
	}

	@Test
	public void migratedWeaponsDefaultToAbilityForEligibleHeroes() {
		Hero hero = hero();
		hero.heroClass = HeroClass.DUELIST;
		hero.subClass = HeroSubClass.NONE;

		assertAbilityDefault(hero, new ChainMace());
		assertAbilityDefault(hero, new LakeSword());
		assertAbilityDefault(hero, new MercuryBlade());

		MountainGuard guard = TestHeroFactory.allocateItem(MountainGuard.class);
		guard.addEnergy(100);
		assertAbilityDefault(hero, guard);
	}

	@Test
	public void chainMaceCommandDoesNotRemainAsMainHandQuickslotDefault() {
		Hero hero = hero();
		hero.heroClass = HeroClass.WARRIOR;
		ChainMace mace = new ChainMace();
		hero.belongings.weapon = mace;

		assertFalse(ChainMace.AC_THROW.equals(mace.defaultAction()));
		assertFalse(ChainMace.AC_THROW.equals(mace.specialActionId()));
		assertEquals(ChainMace.AC_COMMAND_THROW, mace.specialActionId());
	}

	@Test
	public void specialActionsAreNotDuplicatedInMainHandItemMenus() {
		Hero hero = hero();

		ChainMace mace = new ChainMace();
		hero.belongings.weapon = mace;
		assertFalse(mace.actions(hero).contains(ChainMace.AC_THROW));

		LakeSword lakeSword = new LakeSword();
		hero.belongings.weapon = lakeSword;
		assertFalse(lakeSword.actions(hero).contains(LakeSword.AC_DRAW));

		MercuryBlade mercuryBlade = new MercuryBlade();
		hero.belongings.weapon = mercuryBlade;
		assertFalse(mercuryBlade.actions(hero).contains(MercuryBlade.AC_SHOOT));

		MountainGuard mountainGuard = TestHeroFactory.allocateItem(MountainGuard.class);
		mountainGuard.addEnergy(100);
		hero.belongings.weapon = mountainGuard;
		assertFalse(mountainGuard.actions(hero).contains(MountainGuard.AC_RELEASE));
	}

	@Test
	public void oneTimeItemActionsRemainInTheirItemMenus() throws IOException {
		String auxiliary = readTier6Source("AuxiliaryCore.java");
		String sakura = readTier6Source("SakuraBlossomBlade.java");

		assertTrue(auxiliary.contains("actions.add(AC_INFUSE)"));
		assertTrue(sakura.contains("if (canEvolve()) actions.add(AC_EVOLVE)"));
	}

	private static String readTier6Source(String fileName) throws IOException {
		String relative = "src/main/java/com/shatteredpixel/shatteredpixeldungeon/"
				+ "items/weapon/melee/tier6/" + fileName;
		java.nio.file.Path path = Paths.get(relative);
		if (!Files.exists(path)) path = Paths.get("core").resolve(relative);
		return Files.readString(path);
	}

	@Test
	public void reconciliationPrioritizesMainHandThenRestoresRegisteredAction() {
		Hero hero = hero();
		TestAction fallback = new TestAction();
		assertTrue(ActionIndicator1.setAction(fallback));

		ChainMace mace = new ChainMace();
		hero.belongings.weapon = mace;
		ActionIndicator1.reconcileActionState();
		assertSame(mace, ActionIndicator1.action);

		hero.belongings.weapon = null;
		hero.belongings.secondWep = mace;
		ActionIndicator1.reconcileActionState();
		assertSame(fallback, ActionIndicator1.action);

		fallback.usable = false;
		ActionIndicator1.reconcileActionState();
		assertNull(ActionIndicator1.action);
	}

	private static Hero hero() {
		Hero hero = TestHeroFactory.create();
		hero.subClass = HeroSubClass.NONE;
		hero.STR = 100;
		Dungeon.hero = hero;
		return hero;
	}

	private static void assertAbilityDefault(Hero hero, MeleeWeapon weapon) {
		hero.belongings.weapon = weapon;
		assertEquals(MeleeWeapon.AC_ABILITY, weapon.defaultAction());
	}

	private static class TestAction implements ActionIndicator1.Action {
		boolean usable = true;

		@Override
		public String actionName() {
			return "fallback";
		}

		@Override
		public int actionIcon() {
			return HeroIcon.NONE;
		}

		@Override
		public Visual primaryVisual() {
			return null;
		}

		@Override
		public int indicatorColor() {
			return 0;
		}

		@Override
		public void doAction() {
		}

		@Override
		public boolean usable() {
			return usable;
		}
	}
}
