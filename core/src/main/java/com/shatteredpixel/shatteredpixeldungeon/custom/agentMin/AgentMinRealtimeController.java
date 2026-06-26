package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Random;

public class AgentMinRealtimeController {

	private static final AgentMinPythonClient CLIENT = new AgentMinPythonClient();
	private static boolean running;
	private static boolean talentUpgradePending;

	private AgentMinRealtimeController() {
	}

	public static void enable() {
		AgentMinBridgeConfig.ENABLED = true;
		AgentMinRuntimeLog.log("realtime controller enabled");
	}

	public static void disable() {
		AgentMinBridgeConfig.ENABLED = false;
		talentUpgradePending = false;
		CLIENT.close();
		AgentMinRuntimeLog.log("realtime controller disabled");
	}

	public static void onHeroLevelUp() {
		if (!AgentMinBridgeConfig.ENABLED || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			return;
		}
		talentUpgradePending = true;
		AgentMinRuntimeLog.log("talent upgrade pending after level up");
		if (!running) {
			processPendingTalentUpgrades();
		}
	}

	public static void onHeroReady() {
		if (!AgentMinBridgeConfig.ENABLED || running || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			return;
		}
		running = true;
		try {
			if (hasAvailableTalentPoint()) {
				talentUpgradePending = true;
			}
			if (processTalentUpgradesLocked()) {
				return;
			}
			AgentMinState state = AgentMinStateBuilder.capture();
			AgentMinEncodedState encoded = AgentMinStateEncoder.encode(state);
			int actionId = CLIENT.requestAction(encoded);
			AgentMinAction action = wandProbeAction(encoded.actionSpace);
			if (action != null) {
				actionId = action.actionId;
				AgentMinRuntimeLog.log("wand probe selected action=" + actionId + " target=" + action.targetCell);
			} else {
				action = encoded.actionSpace == null ? null : encoded.actionSpace.get(actionId);
			}
			if (action == null) {
				int requestedAction = actionId;
				action = fallbackAction(encoded.actionSpace);
				actionId = action == null ? -1 : action.actionId;
				if (action != null) {
					AgentMinRuntimeLog.log("python bridge fallback requested=" + requestedAction
							+ " action=" + actionId + " " + action.label);
				}
			}
			float sentReward = AgentMinRewardTracker.consumePendingReward();
			if (action != null) {
				boolean executed = AgentMinActionExecutor.execute(action, state);
				if (!executed && Dungeon.hero != null) {
					AgentMinRewardTracker.onActionFailed();
					if (AgentMinActionExecutor.lastFailureConsumesTime()) {
						Dungeon.hero.rest(false);
					}
				}
				AgentMinRuntimeLog.step(actionId, action, executed, sentReward);
			} else if (Dungeon.hero != null) {
				AgentMinRewardTracker.onActionFailed();
				Dungeon.hero.rest(false);
			}
		} catch (Throwable t) {
			AgentMinRuntimeLog.log("realtime step failed: " + t.getMessage());
		} finally {
			running = false;
		}
	}

	private static void processPendingTalentUpgrades() {
		if (!AgentMinBridgeConfig.ENABLED || running || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) {
			return;
		}
		running = true;
		try {
			processTalentUpgradesLocked();
		} catch (Throwable t) {
			AgentMinRuntimeLog.log("forced talent upgrade failed: " + t.getMessage());
		} finally {
			running = false;
		}
	}

	private static boolean processTalentUpgradesLocked() {
		if (!talentUpgradePending || !hasAvailableTalentPoint()) {
			talentUpgradePending = false;
			return false;
		}
		boolean handled = false;
		int upgrades = 0;
		while (hasAvailableTalentPoint() && upgrades < 8) {
			AgentMinState state = AgentMinStateBuilder.capture();
			AgentMinActionSpace forcedSpace = AgentMinActionSpaceBuilder.buildTalentUpgrade(state);
			if (forcedSpace == null || forcedSpace.actionCount <= 0) {
				AgentMinRuntimeLog.log("forced talent upgrade skipped: no upgradeable talent action");
				talentUpgradePending = false;
				return handled;
			}
			AgentMinEncodedState encoded = AgentMinStateEncoder.encode(state);
			encoded.actionSpace = forcedSpace;
			encoded.forcedSkill = forcedSpace.forcedSkill;
			int actionId = CLIENT.requestAction(encoded);
			AgentMinAction action = forcedSpace.get(actionId);
			if (action == null) {
				int requestedAction = actionId;
				action = fallbackAction(forcedSpace);
				actionId = action == null ? -1 : action.actionId;
				if (action != null) {
					AgentMinRuntimeLog.log("forced talent fallback requested=" + requestedAction
							+ " action=" + actionId + " " + action.label);
				}
			}
			float sentReward = AgentMinRewardTracker.consumePendingReward();
			if (action == null) {
				AgentMinRewardTracker.onTalentUpgradeDecision(false);
				AgentMinRuntimeLog.log("forced talent upgrade failed: no selected action");
				talentUpgradePending = false;
				return true;
			}
			boolean executed = AgentMinActionExecutor.execute(action, state);
			if (!executed) {
				AgentMinRewardTracker.onActionFailed();
			}
			AgentMinRuntimeLog.step(actionId, action, executed, sentReward);
			handled = true;
			if (!executed) {
				talentUpgradePending = false;
				return true;
			}
			upgrades++;
		}
		talentUpgradePending = hasAvailableTalentPoint();
		return handled || talentUpgradePending;
	}

	private static boolean hasAvailableTalentPoint() {
		if (Dungeon.hero == null || Dungeon.hero.talents == null) {
			return false;
		}
		for (int tier = 1; tier <= Talent.MAX_TALENT_TIERS; tier++) {
			if (Dungeon.hero.talentPointsAvailable(tier) > 0) {
				return true;
			}
		}
		return false;
	}

	private static AgentMinAction fallbackAction(AgentMinActionSpace space) {
		if (space == null || space.actions.isEmpty()) {
			return null;
		}
		AgentMinAction best = null;
		AgentMinAction wait = null;
		for (AgentMinAction action : space.actions) {
			if (action == null || !action.valid) {
				continue;
			}
			if (action.kind == AgentMinAction.Kind.WAIT) {
				wait = action;
				continue;
			}
			if (best == null || action.priority > best.priority) {
				best = action;
			}
		}
		return best == null ? wait : best;
	}

	private static AgentMinAction wandProbeAction(AgentMinActionSpace space) {
		if (!AgentMinBridgeConfig.WAND_PROBE || space == null || space.actions.isEmpty()) {
			return null;
		}
		int count = 0;
		for (AgentMinAction action : space.actions) {
			if (action != null && action.valid && action.kind == AgentMinAction.Kind.ZAP_WAND) {
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		int selected = Random.Int(count);
		for (AgentMinAction action : space.actions) {
			if (action != null && action.valid && action.kind == AgentMinAction.Kind.ZAP_WAND && selected-- == 0) {
				return action;
			}
		}
		return null;
	}
}
