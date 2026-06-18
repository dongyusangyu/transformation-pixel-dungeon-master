package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

public class AgentMinRealtimeController {

	private static final AgentMinPythonClient CLIENT = new AgentMinPythonClient();
	private static boolean running;

	private AgentMinRealtimeController() {
	}

	public static void enable() {
		AgentMinBridgeConfig.ENABLED = true;
		AgentMinRuntimeLog.log("realtime controller enabled");
	}

	public static void disable() {
		AgentMinBridgeConfig.ENABLED = false;
		CLIENT.close();
		AgentMinRuntimeLog.log("realtime controller disabled");
	}

	public static void onHeroReady() {
		if (!AgentMinBridgeConfig.ENABLED || running || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			return;
		}
		running = true;
		try {
			AgentMinState state = AgentMinStateBuilder.capture();
			AgentMinEncodedState encoded = AgentMinStateEncoder.encode(state);
			int actionId = CLIENT.requestAction(encoded);
			float sentReward = AgentMinRewardTracker.consumePendingReward();
			if (actionId >= 0) {
				AgentMinAction action = encoded.actionSpace == null ? null : encoded.actionSpace.get(actionId);
				boolean executed = AgentMinActionExecutor.execute(action, state);
				if (!executed && Dungeon.hero != null) {
					AgentMinRewardTracker.onActionFailed();
					Dungeon.hero.rest(false);
				}
				AgentMinRuntimeLog.step(actionId, action, executed, sentReward);
			}
		} catch (Throwable t) {
			AgentMinRuntimeLog.log("realtime step failed: " + t.getMessage());
		} finally {
			running = false;
		}
	}
}
