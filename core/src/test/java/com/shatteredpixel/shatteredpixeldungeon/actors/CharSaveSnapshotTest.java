package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

public class CharSaveSnapshotTest {

	@Test
	public void savingUsesStableBuffSnapshotWhenBuffSerializationMutatesOwner() {
		Gnoll owner = new Gnoll();
		MutatingBuff.owner = owner;
		MutatingBuff.serialized = false;
		owner.add(new MutatingBuff());
		owner.add(new Buff());

		try {
			owner.storeInBundle(new Bundle());
			assertTrue("test buff must be serialized", MutatingBuff.serialized);
		} catch (java.util.ConcurrentModificationException e) {
			fail("saving a character must iterate a stable buff snapshot");
		} finally {
			MutatingBuff.owner = null;
		}
	}

	public static class MutatingBuff extends Buff {
		static Char owner;
		static boolean serialized;

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			serialized = true;
			owner.add(new Buff());
		}
	}
}
