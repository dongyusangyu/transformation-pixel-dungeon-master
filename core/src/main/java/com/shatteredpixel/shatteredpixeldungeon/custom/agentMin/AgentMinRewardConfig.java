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
	public static float NEW_VISITED_CELL_REWARD = 0.025f;
	// 智能体首次走到该格子的奖励。推荐区间：0.01 ~ 0.04；鼓励离开已走小区域。
	public static float AGENT_NEW_CELL_REWARD = 0.015f;
	// 重复走到已走过格子的惩罚。推荐区间：-0.04 ~ -0.008；当前偏强以压制四点循环。
	public static float AGENT_REVISIT_CELL_PENALTY = -0.008f;

	// 直接回头 AB/A 惩罚。推荐区间：-0.18 ~ -0.04；轻罚，避免阻止必要撤退。
	public static float IMMEDIATE_BACKTRACK_PENALTY = -0.04f;
	// 短环路惩罚，例如 ABCDA 或 ABCA。推荐区间：-1.20 ~ -0.30；rollout=1 时建议偏强，反馈更直接。
	public static float SHORT_CYCLE_PENALTY = -0.25f;
	// 小区域循环惩罚。推荐区间：-1.80 ~ -0.50；用于压制房间角落和门口打转。
	public static float SMALL_AREA_LOOP_PENALTY = -0.35f;
	// 前沿附近无进展循环惩罚。推荐区间：-1.40 ~ -0.40；逼迫穿门/换路线。
	public static float FRONTIER_LOOP_PENALTY = -0.25f;
	// 循环检测窗口。推荐区间：6 ~ 12；8 可以覆盖四点循环和短期打转。
	public static int LOOP_MEMORY_WINDOW = 8;
	// 检测窗口内唯一格子阈值。推荐区间：3 ~ 5；4 专门针对 ABCD 类循环。
	public static int LOOP_UNIQUE_CELL_THRESHOLD = 4;
	// 小区域循环触发前的连续判定次数。推荐区间：2 ~ 6。
	public static int SMALL_AREA_LOOP_STREAK_THRESHOLD = 2;
	// 循环惩罚重复间隔。推荐区间：2 ~ 6；避免每步都产生过密惩罚。
	public static int LOOP_PENALTY_REPEAT_INTERVAL = 3;
	// 离前沿多少格以内算前沿附近。推荐区间：1 ~ 3。
	public static int FRONTIER_LOOP_DISTANCE = 3;
	// 前沿附近无进展多少步后开始惩罚。推荐区间：3 ~ 8。
	public static int FRONTIER_LOOP_STREAK_THRESHOLD = 3;
	// 前沿循环惩罚重复间隔。推荐区间：2 ~ 6。
	public static int FRONTIER_LOOP_REPEAT_INTERVAL = 2;

	// 每点生命损失惩罚。推荐区间：-0.12 ~ -0.02；过大可能让智能体过度避战。
	public static float HP_LOSS_PENALTY_PER_POINT = -0.006f;
	// 命中敌人的基础奖励。推荐区间：0.03 ~ 0.20。
	public static float ATTACK_HIT_REWARD = 0.12f;
	// 对敌造成每点伤害的奖励。推荐区间：0.01 ~ 0.08；需和击杀奖励共同调节。
	public static float DAMAGE_ENEMY_REWARD_PER_POINT = 0.04f;
	// 贴脸近战命中奖励。推荐区间：0.03 ~ 0.15；鼓励怪物贴脸时优先近战。
	public static float MELEE_ADJACENT_HIT_REWARD = 0.10f;
	// 远程命中奖励。推荐区间：0.02 ~ 0.12；用于学习投掷/法杖的有效使用。
	public static float RANGED_HIT_REWARD = 0.12f;
	// 接近斩杀的一击奖励。推荐区间：0.20 ~ 0.80。
	public static float FINISHING_HIT_REWARD = 0.35f;
	// 低血量下成功输出的微奖励。推荐区间：0.02 ~ 0.12；用于学习风险收益。
	public static float LOW_HP_COMBAT_BONUS = 0.05f;
	// 贴脸使用投掷/法杖资源的惩罚。推荐区间：-0.30 ~ -0.05。
	public static float ADJACENT_RANGED_WASTE_PENALTY = -0.30f;
	// 投掷后没有造成伤害或正向效果时的微惩罚。推荐区间：-0.18 ~ -0.03。
	public static float THROWN_NO_EFFECT_PENALTY = -0.1f;
	// 无效投掷装备/饰品/神器的额外惩罚。推荐区间：-0.50 ~ -0.10。
	public static float THROWN_EQUIPMENT_NO_EFFECT_EXTRA_PENALTY = -0.6f;
	// 投掷动作被视为有效所需的最小正向奖励变化。推荐区间：0.005 ~ 0.05。
	public static float THROWN_EFFECT_REWARD_EPSILON = 0.01f;
	// 可安全战斗时忽略贴脸敌人的惩罚。推荐区间：-0.80 ~ -0.20。
	public static float IGNORE_ADJACENT_ENEMY_PENALTY = -0.25f;
	// 可见弱敌时持续远离的惩罚。推荐区间：-0.50 ~ -0.10。
	public static float SAFE_COMBAT_AVOIDANCE_PENALTY = -0.12f;
	// 可见弱敌时主动靠近的微奖励。推荐区间：0.04 ~ 0.20。
	public static float APPROACH_VISIBLE_ENEMY_REWARD = 0.08f;
	// 击杀普通敌人的奖励。推荐区间：0.80 ~ 2.50。
	public static float KILL_ENEMY_REWARD = 2.25f;
	// 击杀小 Boss 倍率。推荐区间：1.5 ~ 3.0。
	public static float KILL_MINIBOSS_MULTIPLIER = 2.0f;
	// 击杀 Boss 倍率。推荐区间：3.0 ~ 6.0。
	public static float KILL_BOSS_MULTIPLIER = 4.0f;

	// 正常下楼奖励。推荐区间：3.0 ~ 8.0；它应明显高于普通探索事件。
	public static float DESCEND_REWARD = 3.50f;
	// 下楼前允许保留的未探索比例。推荐区间：0.15 ~ 0.45；越低越鼓励扫清楼层。
	public static float DESCEND_UNEXPLORED_GRACE_RATIO = 0.25f;
	// 按未探索比例扣除的动态下楼惩罚。推荐区间：-4.0 ~ -0.8；只用于压低过早下楼收益。
	public static float DESCEND_UNEXPLORED_RATIO_PENALTY = -1.6f;
	// 已发现但未拾取物品的动态下楼惩罚。推荐区间：-0.12 ~ -0.01；保持很小，避免刷物品优先级过高。
	public static float DESCEND_UNPICKED_KNOWN_ITEM_PENALTY = -0.04f;
	// 动态下楼惩罚总下限。推荐区间：-3.0 ~ -0.5；避免完全压过正常下楼奖励。
	public static float DESCEND_EXPLORATION_MAX_PENALTY = -1.2f;
	// 上楼惩罚。推荐区间：-6.0 ~ -1.0；避免非必要回退。
	public static float ASCEND_PENALTY = -3.0f;
	// 跳深渊下楼惩罚。推荐区间：-10.0 ~ -1.0；不鼓励跳楼推进。
	public static float FALL_DESCEND_PENALTY = -5.0f;
	// Penalizes immediate down/up or up/down stair reversals. Recommended range: -12.0 ~ -3.0.
	public static float STAIR_REVERSAL_PENALTY = -2.0f;
	// Penalizes repeated stair interaction from the same depth. Recommended range: -5.0 ~ -1.0.
	public static float STAIR_REPEAT_PENALTY = -1.0f;
	// How long stair anti-loop memory is kept, in actor time. Recommended range: 40 ~ 160.
	public static float STAIR_LOOP_MEMORY_TURNS = 80f;
	// First-floor entrance ascend is blocked for AgentMin and must not spend time. Recommended range: -3.0 ~ -0.5.
	public static float FLOOR_ONE_ASCEND_BLOCKED_PENALTY = -0.20f;

	// 动作失败惩罚。推荐区间：-0.30 ~ -0.05。
	public static float ACTION_FAILED_PENALTY = -0.08f;
	// 等待惩罚。推荐区间：-0.12 ~ -0.02；当前偏强以压制原地静止。
	public static float WAIT_PENALTY = -0.06f;
	// 死亡惩罚。推荐区间：-12.0 ~ -4.0。
	public static float HERO_DEATH_PENALTY = -4.0f;

	// 单个奖励事件裁剪下限。推荐区间：-20.0 ~ -5.0；PPO 训练建议默认 -10。
	public static float MIN_EVENT_REWARD = -6.0f;
	// 单个奖励事件裁剪上限。推荐区间：5.0 ~ 20.0；PPO 训练建议默认 10。
	public static float MAX_EVENT_REWARD = 10.0f;

	// 穿过门的奖励。推荐区间：2.0 ~ 5.0；解决初始房间随机游走的关键奖励。
	public static float DOOR_PASS_REWARD = 2.25f;
	// 穿门奖励冷却。推荐区间：15 ~ 40 回合；20 回合内不重复奖励。
	public static float DOOR_PASS_COOLDOWN_TURNS = 20f;
	// 成功开锁门奖励。推荐区间：0.40 ~ 1.20。
	public static float LOCKED_DOOR_OPEN_REWARD = 0.80f;
	// 无法开锁门惩罚。推荐区间：-0.60 ~ -0.10。
	public static float LOCKED_DOOR_FAIL_PENALTY = -0.25f;
	// 没有对应钥匙却尝试开锁门的额外惩罚。推荐区间：-2.00 ~ -0.60。
	public static float LOCKED_DOOR_NO_KEY_PENALTY = -0.50f;
	// 短时间反复尝试同一扇失败锁门的额外惩罚。推荐区间：-1.50 ~ -0.30。
	public static float LOCKED_DOOR_REPEAT_FAIL_PENALTY = -0.40f;
	// 锁门失败记忆回合数，记忆期内动作空间会过滤同一格开锁动作。推荐区间：30 ~ 150。
	public static int LOCKED_DOOR_FAIL_MEMORY_TURNS = 80;
	// 发现新门奖励。推荐区间：0.20 ~ 0.80。
	public static float DISCOVER_NEW_DOOR_REWARD = 0.25f;
	// 到达新门附近奖励。推荐区间：0.50 ~ 1.50。
	public static float REACH_NEW_DOOR_REWARD = 0.35f;
	// 首次穿过新门奖励。推荐区间：1.50 ~ 4.00。
	public static float PASS_NEW_DOOR_REWARD = 2.0f;
	// 穿门后观察新区域的有效回合窗口。推荐区间：3 ~ 8。
	public static int POST_DOOR_REVEAL_STEPS = 8;
	// 穿门后新视野格子阈值。推荐区间：8 ~ 20。
	public static int POST_DOOR_REVEAL_CELL_THRESHOLD = 12;
	// 穿门后打开新视野的奖励。推荐区间：0.60 ~ 2.00。
	public static float POST_DOOR_REVEAL_REWARD = 0.40f;

	// 成功打开上锁宝箱奖励。推荐区间：0.30 ~ 1.20。
	public static float LOCKED_CHEST_OPEN_REWARD = 1.00f;
	// 打不开上锁宝箱惩罚。推荐区间：-0.50 ~ -0.10。
	public static float LOCKED_CHEST_FAIL_PENALTY = -0.45f;
	// 没有对应钥匙却尝试开锁箱的额外惩罚。推荐区间：-1.80 ~ -0.50。
	public static float LOCKED_CHEST_NO_KEY_PENALTY = -1.10f;
	// 短时间反复尝试同一失败锁箱的额外惩罚。推荐区间：-1.20 ~ -0.25。
	public static float LOCKED_CHEST_REPEAT_FAIL_PENALTY = -0.75f;
	// 锁箱失败记忆回合数，记忆期内动作空间会过滤同一格开箱动作。推荐区间：30 ~ 150。
	public static int LOCKED_CHEST_FAIL_MEMORY_TURNS = 80;

	// 强制使用蜕变/升华类资源的小奖励，核心收益来自后续天赋成功选择。推荐区间：0.05 ~ 0.50。
	public static float TALENT_RESOURCE_USE_REWARD = 0.25f;
	// 天赋升级/蜕变成功奖励。推荐区间：0.60 ~ 2.00。
	public static float TALENT_UPGRADE_SUCCESS_REWARD = 1.20f;
	// 天赋升级/蜕变失败或取消惩罚。推荐区间：-1.50 ~ -0.30。
	public static float TALENT_UPGRADE_FAIL_PENALTY = -0.80f;

	// 物品监视器成功选中当前selector可生效物品的奖励。推荐区间：0.05 ~ 0.30。
	public static float MONITOR_ITEM_VALID_SELECTION_REWARD = 0.16f;
	// 物品监视器选中空槽、越界槽或当前selector不可生效物品的惩罚。推荐区间：-0.80 ~ -0.15。
	public static float MONITOR_ITEM_INVALID_SELECTION_PENALTY = -0.35f;
	// Penalizes item actions that try to open text-measuring UI from the actor thread.
	public static float ITEM_UI_BLOCKED_PENALTY = -0.40f;
	// CellSelector 监视器选择有效格子的奖励。推荐区间：0.04 ~ 0.25。
	public static float MONITOR_CELL_VALID_SELECTION_REWARD = 0.12f;
	// CellSelector 监视器选择越界、不可见或无效格子的惩罚。推荐区间：-1.20 ~ -0.20。
	public static float MONITOR_CELL_INVALID_SELECTION_PENALTY = -0.35f;
	// 特殊选项窗口选择有效项目的奖励。推荐区间：0.04 ~ 0.25。
	public static float MONITOR_OPTION_VALID_SELECTION_REWARD = 0.12f;
	// 特殊选项窗口选择空项目或越界项目的惩罚。推荐区间：-1.20 ~ -0.20。
	public static float MONITOR_OPTION_INVALID_SELECTION_PENALTY = -0.35f;

	// 穿上空装备位的新装备基础奖励。推荐区间：0.10 ~ 0.70。
	public static float EQUIP_NEW_BASE_REWARD = 0.35f;
	// 更换装备的基础奖励，主要收益来自装备评分差。推荐区间：0.00 ~ 0.40。
	public static float EQUIP_REPLACE_BASE_REWARD = 0.15f;
	// 装备评分差奖励倍率，新装备评分减旧装备评分后乘此值。推荐区间：0.04 ~ 0.18。
	public static float EQUIP_VALUE_DIFF_SCALE = 0.10f;
	// 装备满足力量需求的基础奖励。推荐区间：0.10 ~ 0.60。
	public static float EQUIP_STRENGTH_FIT_REWARD = 0.30f;
	// 每点多余力量带来的小奖励。推荐区间：0.01 ~ 0.06。
	public static float EQUIP_STRENGTH_MARGIN_REWARD_PER_POINT = 0.03f;
	// 每点力量不足带来的惩罚。推荐区间：-0.25 ~ -0.06。
	public static float EQUIP_STRENGTH_DEFICIT_PENALTY_PER_POINT = -0.16f;
	// 相对旧装备改善力量负担的奖励倍率。推荐区间：0.03 ~ 0.12。
	public static float EQUIP_STRENGTH_DIFF_SCALE = 0.06f;
	// 穿上实际有诅咒装备的基础惩罚。推荐区间：-2.50 ~ -0.80。
	public static float EQUIP_CURSED_PENALTY = -1.40f;
	// 诅咒惩罚随装备评分增加的倍率。推荐区间：-0.12 ~ -0.03。
	public static float EQUIP_CURSED_VALUE_SCALE = -0.06f;
	// 成功脱下装备的基础惩罚。推荐区间：-0.80 ~ -0.20。
	public static float UNEQUIP_BASE_PENALTY = -1f;
	// 脱下高价值装备的额外惩罚倍率。推荐区间：-0.12 ~ -0.03。
	public static float UNEQUIP_VALUE_SCALE = -0.07f;
	// 脱下力量不足装备时抵消部分惩罚的倍率。推荐区间：0.02 ~ 0.10。
	public static float UNEQUIP_OVERSTR_RELIEF_PER_POINT = 0.05f;
	// 尝试穿脱装备但装备状态未改变的惩罚。推荐区间：-0.60 ~ -0.10。
	public static float EQUIP_ACTION_NO_CHANGE_PENALTY = -0.20f;
	// 尝试卸下或替换已装备诅咒装备的基础惩罚。推荐区间：-1.50 ~ -0.40。
	public static float CURSED_EQUIPPED_ACTION_PENALTY = -0.85f;
	// 已装备诅咒装备价值越高，尝试动它的额外惩罚越高。推荐区间：-0.12 ~ -0.03。
	public static float CURSED_EQUIPPED_VALUE_SCALE = -0.05f;
	// 已装备诅咒装备满足力量需求且越契合，尝试动它的额外惩罚越高。推荐区间：-0.08 ~ -0.01。
	public static float CURSED_EQUIPPED_STRENGTH_MARGIN_SCALE = -0.03f;
	// 已装备诅咒装备力量不足时，适当减轻“想换掉它”的惩罚，但不会转为奖励。推荐区间：0.00 ~ 0.08。
	public static float CURSED_EQUIPPED_OVERSTR_RELIEF_PER_POINT = 0.03f;

	// AgentMin 是否在动作空间内暴露主动卸下装备。推荐区间：false/true；基础探索训练建议 false，避免穿脱循环。
	public static boolean ALLOW_UNEQUIP_ACTIONS = false;
	// 替换装备至少需要的评分提升。推荐区间：1.0 ~ 5.0；越高越保守，能抑制初始装备反复更换。
	public static float EQUIP_MIN_SCORE_IMPROVEMENT = 2.25f;
	// 装备动作冷却回合数。推荐区间：12 ~ 40；冷却内不再暴露同类装备动作。
	public static float EQUIP_ACTION_COOLDOWN_TURNS = 24f;
	// 冷却内再次尝试装备/卸装的惩罚。推荐区间：-2.50 ~ -0.50。
	public static float EQUIP_REPEAT_COOLDOWN_PENALTY = -1.35f;
	// 替换没有明显变好的装备时的惩罚。推荐区间：-1.50 ~ -0.30。
	public static float EQUIP_LOW_VALUE_CHANGE_PENALTY = -0.90f;
	// 已经装备同类或同评分装备时的额外惩罚。推荐区间：-1.20 ~ -0.20。
	public static float EQUIP_SIDEGRADE_PENALTY = -0.65f;

	// 靠近探索前沿的奖励。推荐区间：0.02 ~ 0.08；正向引导走向门/未知边界。
	public static float FRONTIER_STEP_CLOSER_REWARD = 0.07f;
	// 远离探索前沿的惩罚。推荐区间：-0.06 ~ -0.01。
	public static float FRONTIER_STEP_AWAY_PENALTY = -0.06f;
	// 进入新房间奖励。推荐区间：2.0 ~ 5.0；这是“走出房间”的主要终点奖励。
	public static float ENTER_NEW_ROOM_REWARD = 2.75f;
	// 房间内短期停滞惩罚。推荐区间：-1.00 ~ -0.20。
	public static float ROOM_STALL_PENALTY = -0.60f;
	// 长期停滞惩罚。推荐区间：-1.50 ~ -0.40。
	public static float LONG_STALL_PENALTY = -1.00f;
	// 门附近等待惩罚。推荐区间：-1.20 ~ -0.30；用于打掉“门口角落等待”的局部最优。
	public static float DOOR_NEAR_WAIT_PENALTY = -0.35f;
	// 门附近徘徊但没有穿门的惩罚。推荐区间：-1.80 ~ -0.40。
	public static float DOOR_NEAR_LOITER_PENALTY = -0.45f;
	// 门附近发生短循环/回头时的额外惩罚。推荐区间：-2.50 ~ -0.60；专门压制门口来回两格走。
	public static float DOOR_NEAR_CYCLE_PENALTY = -0.55f;
	// 离门多少格以内算门附近。推荐区间：1 ~ 3。
	public static int DOOR_NEAR_DISTANCE = 2;
	// 门附近徘徊多少步后开始惩罚。推荐区间：2 ~ 5。
	public static int DOOR_LOITER_STREAK_THRESHOLD = 4;
	// 门附近徘徊惩罚重复间隔。推荐区间：1 ~ 4。
	public static int DOOR_LOITER_REPEAT_INTERVAL = 3;
	// 房间停滞统计窗口。推荐区间：24 ~ 60 回合。
	public static int ROOM_STALL_WINDOW = 32;
	// 统计窗口内重复/无进展阈值。推荐区间：14 ~ 35；当前偏低以更快发现初始房间循环。
	public static int ROOM_STALL_STREAK_THRESHOLD = 18;
	// 房间停滞重复惩罚间隔。推荐区间：5 ~ 15 回合。
	public static int ROOM_STALL_REPEAT_INTERVAL = 10;
	// 长期停滞阈值。推荐区间：35 ~ 90 回合。
	public static int LONG_STALL_THRESHOLD = 48;
	// 长期停滞重复惩罚间隔。推荐区间：6 ~ 20 回合。
	public static int LONG_STALL_REPEAT_INTERVAL = 12;

	// 半血以下进食奖励。推荐区间：0.20 ~ 0.80。
	public static float FOOD_EAT_LOW_HP_REWARD = 0.35f;
	// 饥饿/饥荒进食奖励。推荐区间：0.30 ~ 1.00。
	public static float FOOD_EAT_HUNGRY_REWARD = 0.50f;
	// 不饿且满血时浪费食物惩罚。推荐区间：-0.80 ~ -0.20。
	public static float FOOD_EAT_WASTEFUL_PENALTY = -0.55f;

	private AgentMinRewardConfig() {
	}
}
