package com.shatteredpixel.shatteredpixeldungeon.desktop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Entry point for the standalone player-demonstration recorder jar. */
public final class RecordDesktopLauncher {

	private static final String RECORDING_ENABLED = "agentmin.record.enabled";
	private static final String RECORDING_DIRECTORY = "agentmin.record.dir";
	private static final String RECORDING_INTERVAL = "agentmin.record.reward_interval";

	private RecordDesktopLauncher() {
	}

	public static void main(String[] args) {
		Path datasetDirectory = datasetDirectory();
		try {
			Files.createDirectories(datasetDirectory);
		} catch (IOException e) {
			System.err.println("[AgentMin] Could not create dataset directory: " + datasetDirectory + " (" + e.getMessage() + ")");
		}

		System.setProperty(RECORDING_ENABLED, "true");
		System.setProperty(RECORDING_DIRECTORY, datasetDirectory.toString());
		System.setProperty(RECORDING_INTERVAL, "10");
		System.out.println("[AgentMin] Player dataset recording enabled: " + datasetDirectory);
		DesktopLauncher.main(args);
	}

	private static Path datasetDirectory() {
		String appData = System.getenv("APPDATA");
		Path roaming = appData == null || appData.trim().isEmpty()
				? Paths.get(System.getProperty("user.home"), "AppData", "Roaming")
				: Paths.get(appData);
		return roaming.resolve(".Transform").resolve("蜕变地牢").resolve("dataset");
	}
}
