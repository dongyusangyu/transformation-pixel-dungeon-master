package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.PackageTrie;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgentMinAndroidCompatibilityTest {

	private static final Pattern FILE_TIMESTAMP =
			Pattern.compile("[0-9]{8}_[0-9]{6}_[0-9]{3}");
	private static final AtomicBoolean PROBE_INITIALIZED = new AtomicBoolean();

	@Test
	public void fileTimestampUsesPortableFormat() {
		String timestamp = AgentMinDatasetRecorder.formatFileTimestamp();

		assertTrue(FILE_TIMESTAMP.matcher(timestamp).matches());
	}

	@Test
	public void classDiscoveryDoesNotRunStaticInitializers() throws Exception {
		assertFalse(PROBE_INITIALIZED.get());

		Class<?> loaded = PackageTrie.loadClassWithoutInitialization(
				StaticInitializationProbe.class.getName(),
				getClass().getClassLoader());

		assertNotNull(loaded);
		assertFalse(PROBE_INITIALIZED.get());
	}

	private static class StaticInitializationProbe {
		static {
			PROBE_INITIALIZED.set(true);
		}
	}
}
