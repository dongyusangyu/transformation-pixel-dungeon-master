package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class AgentMinRuntimeLog {

	private static int stepCounter;

	private AgentMinRuntimeLog() {
	}

	public static void log(String message) {
		if (AgentMinBridgeConfig.LOGGING) {
			GLog.i("[AgentMin] " + message);
		}
	}

	public static void step(int actionId, AgentMinAction action, boolean executed, float sentReward) {
		if (!AgentMinBridgeConfig.LOGGING) {
			return;
		}
		stepCounter++;
		if (stepCounter % AgentMinBridgeConfig.GLOG_STEP_INTERVAL != 0) {
			return;
		}
		String label = action == null ? "null" : action.label;
		String kind = action == null || action.kind == null ? "UNKNOWN" : action.kind.name();
		int from = action == null ? -1 : action.fromCell;
		int target = action == null ? -1 : action.targetCell;
		int heroPos = Dungeon.hero == null ? -1 : Dungeon.hero.pos;
		String line = "[AgentMin] step=" + stepCounter
				+ " action=" + actionId
				+ " " + kind + "/" + label
				+ " executed=" + executed
				+ " from=" + from
				+ " target=" + target
				+ " heroPos=" + heroPos
				+ " reward=" + fmt(sentReward)
				+ " total=" + fmt(AgentMinRewardTracker.episodeReward())
				+ " events=" + rewardEvents();
		if (executed) {
			GLog.h(line);
		} else {
			GLog.w(line);
		}
        /*
		if (Dungeon.hero != null && Dungeon.hero.sprite != null) {
			Dungeon.hero.sprite.showStatus(executed ? CharSprite.POSITIVE : CharSprite.WARNING,
					"AI " + actionId + " r=" + fmt(sentReward));
		}

         */
	}

	private static String rewardEvents() {
		ArrayList<AgentMinRewardTracker.RewardEvent> events = AgentMinRewardTracker.recentEvents();
		if (events.isEmpty()) {
			return "none";
		}
		StringBuilder sb = new StringBuilder();
		int start = Math.max(0, events.size() - 3);
		for (int i = start; i < events.size(); i++) {
			AgentMinRewardTracker.RewardEvent event = events.get(i);
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(event.reason).append(":").append(fmt(event.reward));
		}
		return sb.toString();
	}

	private static String fmt(float value) {
		return String.format(java.util.Locale.ROOT, "%+.3f", value);
	}
}
