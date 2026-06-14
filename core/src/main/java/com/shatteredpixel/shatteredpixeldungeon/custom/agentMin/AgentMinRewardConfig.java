package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinRewardConfig {

	// 是否启用基础智能体奖励记录。正式游玩不需要训练时，可以把这里改成 false。
	public static boolean ENABLED = true;

	// 每走一步的小惩罚，防止智能体原地绕圈或无意义移动。
	public static float MOVE_PENALTY = -0.02f;

	// 每次拾取脚下物品的奖励，引导基础模型学会收集资源。
	public static float PICKUP_REWARD = 0.35f;

	// 尝试拾取但失败的惩罚，例如背包已满或物品无法收纳时触发。
	public static float PICKUP_FAILED_PENALTY = -0.12f;

	// 主动丢弃物品的惩罚，避免智能体用“丢下-捡起”制造无意义循环。
	public static float DROP_ITEM_PENALTY = -0.25f;

	// 如果拾取的是最近由玩家自己丢下的物品，不给拾取奖励，而是额外惩罚。
	public static float RECENT_DROP_PICKUP_PENALTY = -0.45f;

	// 记录最近多少次主动丢弃事件，用于识别刷拾取奖励的行为。
	public static int RECENT_DROP_MEMORY_SIZE = 64;

	// 记录最近多少次失败拾取事件，用于让动作空间暂时屏蔽同一物品的拾取候选。
	public static int RECENT_PICKUP_FAILURE_MEMORY_SIZE = 32;

	// 每发现一个此前没有 visited 的格子奖励。数值不宜太大，否则智能体会过度冒险探图。
	public static float NEW_VISITED_CELL_REWARD = 0.035f;

	// 每点实际失去生命的惩罚。这里按 HP 逐点扣分，让模型学习避免无谓受伤。
	public static float HP_LOSS_PENALTY_PER_POINT = -0.08f;

	// 攻击命中敌人的固定奖励，用来鼓励主动清理威胁。
	public static float ATTACK_HIT_REWARD = 0.12f;

	// 每对敌人造成 1 点实际伤害的奖励，补充固定命中奖励，使高质量攻击更有价值。
	public static float DAMAGE_ENEMY_REWARD_PER_POINT = 0.025f;

	// 击杀普通敌人的基础奖励。
	public static float KILL_ENEMY_REWARD = 1.8f;

	// 击杀精英、迷你 Boss、Boss 的额外奖励倍率，用于区分更关键的战斗成果。
	public static float KILL_MINIBOSS_MULTIPLIER = 2.0f;
	public static float KILL_BOSS_MULTIPLIER = 4.0f;

	// 下楼奖励是基础目标之一，数值高于普通击杀，推动智能体完成楼层推进。
	public static float DESCEND_REWARD = 4.0f;

	// 上楼通常意味着回退或逃避，基础模型先给予惩罚；后续可按场景改成条件奖励。
	public static float ASCEND_PENALTY = -1.5f;

	// 死亡是一个 episode 的强负反馈，避免模型用牺牲换短期收益。
	public static float HERO_DEATH_PENALTY = -10.0f;

	// 单次事件奖励裁剪范围，防止异常数值破坏训练稳定性。
	public static float MIN_EVENT_REWARD = -20.0f;
	public static float MAX_EVENT_REWARD = 20.0f;

	private AgentMinRewardConfig() {
	}
}
