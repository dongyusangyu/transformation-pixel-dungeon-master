package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DamageTagTest {

	@Test
	public void damageCanCarryOrthogonalTags() {
		EnumSet<DamageTag> tags = DamageTag.of(
				DamageTag.MAGICAL,
				DamageTag.FIRE,
				DamageTag.NO_ARMOR);

		assertTrue(DamageTag.has(tags, DamageTag.MAGICAL));
		assertTrue(DamageTag.has(tags, DamageTag.FIRE));
		assertTrue(DamageTag.has(tags, DamageTag.NO_ARMOR));
		assertFalse(DamageTag.has(tags, DamageTag.PHYSICAL));
	}

	@Test
	public void elementalIconTakesPriorityOverBroadDamageNature() {
		assertEquals(DamageTag.FIRE,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.MAGICAL, DamageTag.FIRE)));
		assertEquals(DamageTag.MAGICAL,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.MAGICAL)));
		assertEquals(DamageTag.NO_ARMOR,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.PHYSICAL, DamageTag.NO_ARMOR)));
	}

	@Test
	public void attackDeliveryDoesNotChangeDamageIcon() {
		assertEquals(DamageTag.PHYSICAL,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.PHYSICAL, DamageTag.MELEE)));
		assertEquals(DamageTag.PHYSICAL,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.PHYSICAL, DamageTag.RANGED)));
		assertEquals(DamageTag.MAGICAL,
				DamageTag.primaryIconTag(DamageTag.of(DamageTag.MAGICAL, DamageTag.RANGED)));
	}

	@Test
	public void physicalDeliverySeparatesMeleeAndRangedAttacks() {
		assertEquals(DamageTag.Delivery.MELEE,
				DamageTag.physicalDelivery(
						DamageTag.of(DamageTag.PHYSICAL, DamageTag.MELEE)));
		assertEquals(DamageTag.Delivery.RANGED,
				DamageTag.physicalDelivery(
						DamageTag.of(DamageTag.PHYSICAL, DamageTag.RANGED)));
	}

	@Test
	public void magicAndUnclassifiedDamageHaveNoPhysicalDelivery() {
		assertEquals(DamageTag.Delivery.NONE,
				DamageTag.physicalDelivery(
						DamageTag.of(DamageTag.MAGICAL, DamageTag.RANGED)));
		assertEquals(DamageTag.Delivery.NONE,
				DamageTag.physicalDelivery(DamageTag.of(DamageTag.PHYSICAL)));
		assertEquals(DamageTag.Delivery.NONE,
				DamageTag.physicalDelivery(DamageTag.of(
						DamageTag.PHYSICAL, DamageTag.MAGICAL, DamageTag.MELEE)));
	}

	@Test
	public void combatPipelineAcceptsDamageTags() throws Exception {
		assertEquals(int.class, Char.class.getMethod(
				"attackProc", Char.class, int.class, DamageTag[].class).getReturnType());
		assertEquals(int.class, Char.class.getMethod(
				"defenseProc", Char.class, int.class, DamageTag[].class).getReturnType());
		assertEquals(void.class, Char.class.getMethod(
				"damage", int.class, Object.class, DamageTag[].class).getReturnType());
		assertEquals(boolean.class, Char.class.getMethod(
				"hit", Char.class, Char.class, DamageTag[].class).getReturnType());
		assertEquals(boolean.class, Char.class.getMethod(
				"hit", Char.class, Char.class, float.class, DamageTag[].class).getReturnType());
	}
}
