package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class ReasonDamageTagsTest {

	@Test
	public void reasonCollapseDamageIsUnavoidableAndUsesReasonCategory() {
		EnumSet<DamageTag> tags = DamageTag.of(Reason.deathDamageTags());

		assertTrue(tags.contains(DamageTag.REASON));
		assertTrue(tags.contains(DamageTag.UNAVOIDABLE));
		assertTrue(DamageTag.primaryIconTag(tags) == DamageTag.REASON);
	}
}
