package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.watabou.noosa.Game;

public class AgentMinTrainingBootstrap {

	public static final boolean AUTO_START = Boolean.parseBoolean(value("agentmin.auto_start", "AGENTMIN_AUTO_START", "false"));

	private static boolean started;

	private AgentMinTrainingBootstrap() {
	}

	public static void autoStartWarriorRun() {
		if (!AgentMinBridgeConfig.ENABLED || !AUTO_START || started) {
			return;
		}

		startNewWarriorRun("auto start", false);
	}

	public static boolean startNewWarriorRun(String reason) {
		return startNewWarriorRun(reason, true);
	}

	private static boolean startNewWarriorRun(String reason, boolean reuseCurrentSlot) {
		if (!AgentMinBridgeConfig.ENABLED || !AUTO_START) {
			return false;
		}

		int slot = reuseCurrentSlot && GamesInProgress.curSlot > 0 ? GamesInProgress.curSlot : GamesInProgress.firstEmpty();
		if (slot == -1) {
			AgentMinRuntimeLog.log("training run start skipped: no empty save slot");
			return false;
		}

		if (reuseCurrentSlot && GamesInProgress.gameExists(slot)) {
			Dungeon.deleteGame(slot, true);
		}

		started = true;
		AgentMinRuntimeLog.log("starting warrior training run in slot " + slot + " (" + reason + ")");

		GamesInProgress.curSlot = slot;
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		GamesInProgress.skin = 0;
		SPDSettings.Skin(0);

		Dungeon.hero = null;
		Dungeon.daily = false;
		Dungeon.dailyReplay = false;
		Dungeon.initSeed();
		ActionIndicator.clearAction();
		ActionIndicator1.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

		Game.switchScene(InterlevelScene.class);
		return true;
	}

	private static String value(String property, String env, String fallback) {
		String result = System.getProperty(property);
		if (result != null && result.length() > 0) {
			return result;
		}
		result = System.getenv(env);
		if (result != null && result.length() > 0) {
			return result;
		}
		return fallback;
	}
}
