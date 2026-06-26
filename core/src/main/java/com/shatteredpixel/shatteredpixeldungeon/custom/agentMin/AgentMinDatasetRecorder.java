package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AgentMinDatasetRecorder {

	private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
	private static final String ENABLED_PROPERTY = "agentmin.record.enabled";
	private static final String ENABLED_ENV = "AGENTMIN_RECORD_ENABLED";
	private static final String DIR_PROPERTY = "agentmin.record.dir";
	private static final String DIR_ENV = "AGENTMIN_RECORD_DIR";
	private static final String REPORT_INTERVAL_PROPERTY = "agentmin.record.reward_interval";
	private static final String REPORT_INTERVAL_ENV = "AGENTMIN_RECORD_REWARD_INTERVAL";

	private static BufferedWriter writer;
	private static BufferedWriter progressWriter;
	private static String runId;
	private static long sampleCount;
	private static AgentMinEncodedState pendingEncoded;
	private static boolean shutdownHookInstalled;

	private AgentMinDatasetRecorder() {
	}

	public static boolean enabled() {
		String enabled = value(ENABLED_PROPERTY, ENABLED_ENV, "false");
		String dir = recordDirectory();
		return enabled.equalsIgnoreCase("true")
				&& dir != null
				&& !dir.trim().isEmpty()
				&& !AgentMinBridgeConfig.ENABLED;
	}

	public static synchronized void onHeroReady() {
		if (!enabled() || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			pendingEncoded = null;
			return;
		}
		pendingEncoded = AgentMinStateEncoder.captureEncoded();
	}

	public static synchronized void onHeroActing(HeroAction action) {
		if (!enabled() || action == null || pendingEncoded != null || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			return;
		}
		pendingEncoded = AgentMinStateEncoder.captureEncoded();
	}

	public static synchronized void onHeroActionSelected(HeroAction action) {
		if (!enabled() || action == null) {
			return;
		}
		record(findForHeroAction(action));
	}

	public static synchronized void onHeroStep(int step) {
		if (!enabled() || step < 0) {
			return;
		}
		record(findByKindAndTarget(AgentMinAction.Kind.MOVE, step));
	}

	public static synchronized void onWait(boolean fullRest) {
		if (!enabled() || fullRest) {
			return;
		}
		record(findByKind(AgentMinAction.Kind.WAIT));
	}

	public static synchronized void onFoodEaten(Item item) {
		if (!enabled()) {
			return;
		}
		record(findItemAction(AgentMinAction.Kind.EAT_FOOD, item, "EAT"));
	}

	public static synchronized void onPotionDrank(Item item) {
		if (!enabled()) {
			return;
		}
		record(findItemAction(AgentMinAction.Kind.DRINK_HEALING, item, "DRINK"));
	}

	public static synchronized void onItemCast(Item item, Hero user, int dst) {
		if (!enabled() || item == null || user == null) {
			return;
		}
		int targetCell = dst;
		try {
			targetCell = item.targetingPos(user, dst);
		} catch (Throwable ignored) {
			// fall back to raw dst
		}
		if (item instanceof Wand) {
			record(findTargetedItemAction(AgentMinAction.Kind.ZAP_WAND, item, "ZAP", targetCell, dst));
		} else {
			record(findTargetedItemAction(AgentMinAction.Kind.THROW_WEAPON, item, "THROW", targetCell, dst));
		}
	}

	public static synchronized void close() {
		pendingEncoded = null;
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException ignored) {
			}
			writer = null;
		}
		if (progressWriter != null) {
			try {
				progressWriter.flush();
				progressWriter.close();
			} catch (IOException ignored) {
			}
			progressWriter = null;
		}
	}

	private static AgentMinAction findForHeroAction(HeroAction action) {
		if (action instanceof HeroAction.Attack) {
			int target = ((HeroAction.Attack) action).target == null ? -1 : ((HeroAction.Attack) action).target.pos;
			return findByKindAndTarget(AgentMinAction.Kind.ATTACK, target);
		}
		if (action instanceof HeroAction.Move) {
			return findByKindAndTarget(AgentMinAction.Kind.MOVE, action.dst);
		}
		if (action instanceof HeroAction.PickUp) {
			return findByKindAndTarget(AgentMinAction.Kind.PICK_UP, action.dst);
		}
		if (action instanceof HeroAction.LvlTransition) {
			return findByKindAndTarget(AgentMinAction.Kind.USE_STAIRS, action.dst);
		}
		return null;
	}

	private static AgentMinAction findByKind(AgentMinAction.Kind kind) {
		if (pendingEncoded == null || pendingEncoded.actionSpace == null) {
			return null;
		}
		for (AgentMinAction action : pendingEncoded.actionSpace.actions) {
			if (action != null && action.valid && action.kind == kind) {
				return action;
			}
		}
		return null;
	}

	private static AgentMinAction findByKindAndTarget(AgentMinAction.Kind kind, int targetCell) {
		if (pendingEncoded == null || pendingEncoded.actionSpace == null) {
			return null;
		}
		for (AgentMinAction action : pendingEncoded.actionSpace.actions) {
			if (action != null && action.valid && action.kind == kind && action.targetCell == targetCell) {
				return action;
			}
		}
		return null;
	}

	private static AgentMinAction findItemAction(AgentMinAction.Kind kind, Item item, String itemAction) {
		if (pendingEncoded == null || pendingEncoded.actionSpace == null || item == null) {
			return null;
		}
		String className = item.getClass().getName();
		for (AgentMinAction action : pendingEncoded.actionSpace.actions) {
			if (action != null
					&& action.valid
					&& action.kind == kind
					&& itemAction.equals(action.itemAction)
					&& className.equals(action.itemClassName)) {
				return action;
			}
		}
		return null;
	}

	private static AgentMinAction findTargetedItemAction(AgentMinAction.Kind kind, Item item, String itemAction, int targetCell, int fallbackCell) {
		if (pendingEncoded == null || pendingEncoded.actionSpace == null || item == null) {
			return null;
		}
		String className = item.getClass().getName();
		AgentMinAction fallback = null;
		for (AgentMinAction action : pendingEncoded.actionSpace.actions) {
			if (action == null
					|| !action.valid
					|| action.kind != kind
					|| !itemAction.equals(action.itemAction)
					|| !className.equals(action.itemClassName)) {
				continue;
			}
			if (action.targetCell == targetCell || action.targetCell == fallbackCell) {
				return action;
			}
			if (fallback == null) {
				fallback = action;
			}
		}
		return fallback;
	}

	private static void record(AgentMinAction action) {
		if (action == null || pendingEncoded == null) {
			return;
		}
		try {
			ensureWriter();
			long currentIndex = sampleCount;
			writer.write(AgentMinJsonBridge.sampleJson(pendingEncoded, action, runId, currentIndex));
			writer.newLine();
			writer.flush();
			sampleCount = currentIndex + 1;
			reportRewardProgressIfNeeded(action, currentIndex + 1, pendingEncoded);
		} catch (IOException e) {
			AgentMinRuntimeLog.log("dataset record failed: " + e.getMessage());
		} finally {
			pendingEncoded = null;
		}
	}

	private static void ensureWriter() throws IOException {
		if (writer != null) {
			return;
		}
		Path dir = Paths.get(recordDirectory());
		Files.createDirectories(dir);
		runId = "agentmin_demo_" + LocalDateTime.now().format(FILE_TIME);
		Path file = dir.resolve(runId + ".jsonl");
		writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		Path progressFile = dir.resolve(runId + "_progress.log");
		progressWriter = Files.newBufferedWriter(progressFile, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		sampleCount = 0;
		if (!shutdownHookInstalled) {
			Runtime.getRuntime().addShutdownHook(new Thread(AgentMinDatasetRecorder::close, "AgentMinDatasetRecorderShutdown"));
			shutdownHookInstalled = true;
		}
		AgentMinRuntimeLog.log("dataset recorder writing to " + file);
		AgentMinRuntimeLog.log("dataset recorder progress log: " + progressFile);
	}

	private static void reportRewardProgressIfNeeded(AgentMinAction action, long recordedTurns, AgentMinEncodedState encoded) throws IOException {
		int interval = rewardReportInterval();
		if (interval <= 0 || recordedTurns % interval != 0) {
			return;
		}
		String actionKind = action.kind == null ? "UNKNOWN" : action.kind.name();
		String actionLabel = action.label == null ? "null" : action.label;
		String message = String.format(Locale.ROOT,
				"[AgentMin][Record] turn=%d action=%s/%s total_reward=%+.3f pending_reward=%+.3f",
				recordedTurns,
				actionKind,
				actionLabel,
				encoded.episodeReward,
				encoded.pendingReward);
		AgentMinRuntimeLog.log(message);
		if (progressWriter != null) {
			progressWriter.write(message);
			progressWriter.newLine();
			progressWriter.flush();
		}
	}

	private static int rewardReportInterval() {
		String raw = value(REPORT_INTERVAL_PROPERTY, REPORT_INTERVAL_ENV, "10");
		if (raw == null || raw.trim().isEmpty()) {
			return 10;
		}
		try {
			return Math.max(1, Integer.parseInt(raw.trim()));
		} catch (NumberFormatException ignored) {
			return 10;
		}
	}

	private static String recordDirectory() {
		return value(DIR_PROPERTY, DIR_ENV, "");
	}

	private static String value(String property, String environment, String fallback) {
		String configured = System.getProperty(property);
		if (configured != null && !configured.trim().isEmpty()) {
			return configured;
		}
		configured = System.getenv(environment);
		if (configured != null && !configured.trim().isEmpty()) {
			return configured;
		}
		return fallback;
	}
}
