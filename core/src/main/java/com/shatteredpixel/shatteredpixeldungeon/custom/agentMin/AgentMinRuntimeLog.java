package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class AgentMinRuntimeLog {

	private static final int STYLE_INFO = 0;
	private static final int STYLE_HIGHLIGHT = 1;
	private static final int STYLE_WARNING = 2;

	private static int stepCounter;

	private AgentMinRuntimeLog() {
	}

	public static void log(String message) {
		if (AgentMinBridgeConfig.LOGGING) {
			gameLog("[AgentMin] " + message, STYLE_INFO);
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
		String skill = action == null || action.skill == null ? "UNKNOWN" : action.skill.name();
		int from = action == null ? -1 : action.fromCell;
		int target = action == null ? -1 : action.targetCell;
		int heroPos = Dungeon.hero == null ? -1 : Dungeon.hero.pos;
		String line = "[AgentMin] step=" + stepCounter
				+ " action=" + actionId
				+ " skill=" + skill
				+ " " + kind + "/" + label
				+ " executed=" + executed
				+ " from=" + from
				+ " target=" + target
				+ " heroPos=" + heroPos
				+ " reward=" + fmt(sentReward)
				+ " total=" + fmt(AgentMinRewardTracker.episodeReward())
				+ " events=" + rewardEvents();
		gameLog(line, executed ? STYLE_HIGHLIGHT : STYLE_WARNING);
        /*
		if (Dungeon.hero != null && Dungeon.hero.sprite != null) {
			Dungeon.hero.sprite.showStatus(executed ? CharSprite.POSITIVE : CharSprite.WARNING,
					"AI " + actionId + " r=" + fmt(sentReward));
		}

         */
	}

	private static void gameLog(final String line, final int style) {
		try {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					try {
						if (style == STYLE_HIGHLIGHT) {
							GLog.h(line);
						} else if (style == STYLE_WARNING) {
							GLog.w(line);
						} else {
							GLog.i(line);
						}
					} catch (Throwable ignored) {
						// Logging must never interrupt realtime training.
					}
				}
			});
		} catch (Throwable ignored) {
			// Logging must never interrupt realtime training.
		}
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
