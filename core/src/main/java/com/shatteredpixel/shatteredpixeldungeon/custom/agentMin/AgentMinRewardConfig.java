package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinRewardConfig {

	// 是否启用 AgentMin 奖励记录。推荐区间：true/false；训练时保持 true。
	public static boolean ENABLED = true;

	// 移动基础奖励。推荐区间：-0.003 ~ 0.003；新格奖励已足够，基础移动保持中性。
	public static float MOVE_PENALTY = 0.0f;

	// 拾取成功奖励。推荐区间：0.20 ~ 0.80；过高会诱导绕物品刷分。
	public static float PICKUP_REWARD = 0.45f;
	// 拾取失败惩罚。推荐区间：-0.30 ~ -0.05；用于避免反复拾取不可拾物。
	public static float PICKUP_FAILED_PENALTY = -0.15f;
	// 丢弃物品惩罚。推荐区间：-0.60 ~ -0.10；用于抑制丢下再捡刷分。
	public static float DROP_ITEM_PENALTY = -0.30f;
	// 刚丢弃物品又拾取的额外惩罚。推荐区间：-1.00 ~ -0.30。
	public static float RECENT_DROP_PICKUP_PENALTY = -0.70f;
	// 近期丢弃记录长度。推荐区间：32 ~ 128。
	public static int RECENT_DROP_MEMORY_SIZE = 64;
	// 近期拾取失败记录长度。推荐区间：16 ~ 64。
	public static int RECENT_PICKUP_FAILURE_MEMORY_SIZE = 32;

	// 新增可见/已探索格子的单格奖励。推荐区间：0.02 ~ 0.08；主要鼓励打开新视野。
	public static float NEW_VISITED_CELL_REWARD = 0.06f;
	// 智能体首次走到该格子的奖励。推荐区间：0.01 ~ 0.04；鼓励离开已走小区域。
	public static float AGENT_NEW_CELL_REWARD = 0.035f;
	// 重复走到已走过格子的惩罚。推荐区间：-0.04 ~ -0.008；当前偏强以压制四点循环。
	public static float AGENT_REVISIT_CELL_PENALTY = -0.025f;

	// 直接回头 AB/A 惩罚。推荐区间：-0.18 ~ -0.04；轻罚，避免阻止必要撤退。
	public static float IMMEDIATE_BACKTRACK_PENALTY = -0.08f;
	// 短环路惩罚，例如 ABCDA 或 ABCA。推荐区间：-0.50 ~ -0.15。
	public static float SHORT_CYCLE_PENALTY = -0.30f;
	// 小区域循环惩罚。推荐区间：-0.80 ~ -0.25；用于压制房间角落打转。
	public static float SMALL_AREA_LOOP_PENALTY = -0.45f;
	// 前沿附近无进展循环惩罚。推荐区间：-1.00 ~ -0.30；逼迫穿门/换路线。
	public static float FRONTIER_LOOP_PENALTY = -0.60f;
	// 循环检测窗口。推荐区间：6 ~ 12；8 可以覆盖四点循环和短期打转。
	public static int LOOP_MEMORY_WINDOW = 8;
	// 检测窗口内唯一格子阈值。推荐区间：3 ~ 5；4 专门针对 ABCD 类循环。
	public static int LOOP_UNIQUE_CELL_THRESHOLD = 4;
	// 小区域循环触发前的连续判定次数。推荐区间：2 ~ 6。
	public static int SMALL_AREA_LOOP_STREAK_THRESHOLD = 3;
	// 循环惩罚重复间隔。推荐区间：2 ~ 6；避免每步都产生过密惩罚。
	public static int LOOP_PENALTY_REPEAT_INTERVAL = 3;
	// 离前沿多少格以内算前沿附近。推荐区间：1 ~ 3。
	public static int FRONTIER_LOOP_DISTANCE = 2;
	// 前沿附近无进展多少步后开始惩罚。推荐区间：3 ~ 8。
	public static int FRONTIER_LOOP_STREAK_THRESHOLD = 4;
	// 前沿循环惩罚重复间隔。推荐区间：2 ~ 6。
	public static int FRONTIER_LOOP_REPEAT_INTERVAL = 3;

	// 每点生命损失惩罚。推荐区间：-0.12 ~ -0.02；过大可能让智能体过度避战。
	public static float HP_LOSS_PENALTY_PER_POINT = -0.03f;
	// 命中敌人的基础奖励。推荐区间：0.03 ~ 0.20。
	public static float ATTACK_HIT_REWARD = 0.08f;
	// 对敌造成每点伤害的奖励。推荐区间：0.01 ~ 0.08；需和击杀奖励共同调节。
	public static float DAMAGE_ENEMY_REWARD_PER_POINT = 0.04f;
	// 贴脸近战命中奖励。推荐区间：0.03 ~ 0.15；鼓励怪物贴脸时优先近战。
	public static float MELEE_ADJACENT_HIT_REWARD = 0.06f;
	// 远程命中奖励。推荐区间：0.02 ~ 0.12；用于学习投掷/法杖的有效使用。
	public static float RANGED_HIT_REWARD = 0.05f;
	// 接近斩杀的一击奖励。推荐区间：0.20 ~ 0.80。
	public static float FINISHING_HIT_REWARD = 0.35f;
	// 低血量下成功输出的微奖励。推荐区间：0.02 ~ 0.12；用于学习风险收益。
	public static float LOW_HP_COMBAT_BONUS = 0.06f;
	// 贴脸使用投掷/法杖资源的惩罚。推荐区间：-0.30 ~ -0.05。
	public static float ADJACENT_RANGED_WASTE_PENALTY = -0.15f;
	// 击杀普通敌人的奖励。推荐区间：0.80 ~ 2.50。
	public static float KILL_ENEMY_REWARD = 1.80f;
	// 击杀小 Boss 倍率。推荐区间：1.5 ~ 3.0。
	public static float KILL_MINIBOSS_MULTIPLIER = 2.0f;
	// 击杀 Boss 倍率。推荐区间：3.0 ~ 6.0。
	public static float KILL_BOSS_MULTIPLIER = 4.0f;

	// 正常下楼奖励。推荐区间：3.0 ~ 8.0；它应明显高于普通探索事件。
	public static float DESCEND_REWARD = 5.0f;
	// 上楼惩罚。推荐区间：-6.0 ~ -1.0；避免非必要回退。
	public static float ASCEND_PENALTY = -4.0f;
	// 跳深渊下楼惩罚。推荐区间：-10.0 ~ -1.0；不鼓励跳楼推进。
	public static float FALL_DESCEND_PENALTY = -6.0f;

	// 动作失败惩罚。推荐区间：-0.30 ~ -0.05。
	public static float ACTION_FAILED_PENALTY = -0.14f;
	// 等待惩罚。推荐区间：-0.12 ~ -0.02；当前偏强以压制原地静止。
	public static float WAIT_PENALTY = -0.08f;
	// 死亡惩罚。推荐区间：-12.0 ~ -4.0。
	public static float HERO_DEATH_PENALTY = -6.0f;

	// 单个奖励事件裁剪下限。推荐区间：-20.0 ~ -5.0；PPO 训练建议默认 -10。
	public static float MIN_EVENT_REWARD = -10.0f;
	// 单个奖励事件裁剪上限。推荐区间：5.0 ~ 20.0；PPO 训练建议默认 10。
	public static float MAX_EVENT_REWARD = 10.0f;

	// 穿过门的奖励。推荐区间：2.0 ~ 5.0；解决初始房间随机游走的关键奖励。
	public static float DOOR_PASS_REWARD = 4.0f;
	// 穿门奖励冷却。推荐区间：15 ~ 40 回合；20 回合内不重复奖励。
	public static float DOOR_PASS_COOLDOWN_TURNS = 20f;
	// 成功开锁门奖励。推荐区间：0.40 ~ 1.20。
	public static float LOCKED_DOOR_OPEN_REWARD = 0.80f;
	// 无法开锁门惩罚。推荐区间：-0.60 ~ -0.10。
	public static float LOCKED_DOOR_FAIL_PENALTY = -0.30f;
	// 发现新门奖励。推荐区间：0.20 ~ 0.80。
	public static float DISCOVER_NEW_DOOR_REWARD = 0.45f;
	// 到达新门附近奖励。推荐区间：0.50 ~ 1.50。
	public static float REACH_NEW_DOOR_REWARD = 1.10f;
	// 首次穿过新门奖励。推荐区间：1.50 ~ 4.00。
	public static float PASS_NEW_DOOR_REWARD = 3.0f;
	// 穿门后观察新区域的有效回合窗口。推荐区间：3 ~ 8。
	public static int POST_DOOR_REVEAL_STEPS = 5;
	// 穿门后新视野格子阈值。推荐区间：8 ~ 20。
	public static int POST_DOOR_REVEAL_CELL_THRESHOLD = 12;
	// 穿门后打开新视野的奖励。推荐区间：0.60 ~ 2.00。
	public static float POST_DOOR_REVEAL_REWARD = 1.30f;

	// 成功打开上锁宝箱奖励。推荐区间：0.30 ~ 1.20。
	public static float LOCKED_CHEST_OPEN_REWARD = 0.65f;
	// 打不开上锁宝箱惩罚。推荐区间：-0.50 ~ -0.10。
	public static float LOCKED_CHEST_FAIL_PENALTY = -0.25f;

	// 靠近探索前沿的奖励。推荐区间：0.02 ~ 0.08；正向引导走向门/未知边界。
	public static float FRONTIER_STEP_CLOSER_REWARD = 0.045f;
	// 远离探索前沿的惩罚。推荐区间：-0.06 ~ -0.01。
	public static float FRONTIER_STEP_AWAY_PENALTY = -0.025f;
	// 进入新房间奖励。推荐区间：2.0 ~ 5.0；这是“走出房间”的主要终点奖励。
	public static float ENTER_NEW_ROOM_REWARD = 3.50f;
	// 房间内短期停滞惩罚。推荐区间：-1.00 ~ -0.20。
	public static float ROOM_STALL_PENALTY = -0.65f;
	// 长期停滞惩罚。推荐区间：-1.50 ~ -0.40。
	public static float LONG_STALL_PENALTY = -1.10f;
	// 房间停滞统计窗口。推荐区间：24 ~ 60 回合。
	public static int ROOM_STALL_WINDOW = 32;
	// 统计窗口内重复/无进展阈值。推荐区间：14 ~ 35；当前偏低以更快发现初始房间循环。
	public static int ROOM_STALL_STREAK_THRESHOLD = 18;
	// 房间停滞重复惩罚间隔。推荐区间：5 ~ 15 回合。
	public static int ROOM_STALL_REPEAT_INTERVAL = 6;
	// 长期停滞阈值。推荐区间：35 ~ 90 回合。
	public static int LONG_STALL_THRESHOLD = 45;
	// 长期停滞重复惩罚间隔。推荐区间：6 ~ 20 回合。
	public static int LONG_STALL_REPEAT_INTERVAL = 8;

	// 半血以下进食奖励。推荐区间：0.20 ~ 0.80。
	public static float FOOD_EAT_LOW_HP_REWARD = 0.35f;
	// 饥饿/饥荒进食奖励。推荐区间：0.30 ~ 1.00。
	public static float FOOD_EAT_HUNGRY_REWARD = 0.50f;
	// 不饿且满血时浪费食物惩罚。推荐区间：-0.80 ~ -0.20。
	public static float FOOD_EAT_WASTEFUL_PENALTY = -0.55f;

	private AgentMinRewardConfig() {
	}
}
