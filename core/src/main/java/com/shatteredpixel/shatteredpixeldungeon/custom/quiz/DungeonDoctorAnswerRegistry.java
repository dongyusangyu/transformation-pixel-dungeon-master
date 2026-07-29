/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DungeonDoctorAnswerRegistry {

	private static final Map<String, Answer> ANSWERS = createAnswers();

	private DungeonDoctorAnswerRegistry() {
	}

	public static Map<String, Answer> answers() {
		return ANSWERS;
	}

	private static Map<String, Answer> createAnswers() {
		LinkedHashMap<String, Answer> answers = new LinkedHashMap<>();
		register(answers, "Q001", 0,
				"药剂颜色与效果的映射会在每局重新随机。");
		register(answers, "Q002", 3,
				"卷轴图案与效果的映射会在每局重新随机。");
		register(answers, "Q003", 2,
				"自定义标签适合记录已推断但尚未鉴定的药剂或卷轴。");
		register(answers, "Q004", 1,
				"五个大区各固定生成3张升级卷轴，共15张。");
		register(answers, "Q005", 0,
				"禁忌咒文下升级卷轴总数降为8张。");
		register(answers, "Q006", 3,
				"五区的升级卷轴数量按2、1、2、1、2分布。");
		register(answers, "Q007", 2,
				"五个大区各固定生成2瓶力量药剂，共10瓶。");
		register(answers, "Q008", 1,
				"每个五层大区固定生成2瓶力量药剂。");
		register(answers, "Q009", 0,
				"每区一瓶力量药剂位于第一或第二层。");
		register(answers, "Q010", 3,
				"每区另一瓶力量药剂位于第三或第四层。");
		register(answers, "Q011", 2,
				"新游戏开始时会随机选择两套卷轴权重之一。");
		register(answers, "Q012", 1,
				"其中一套卷轴权重把嬗变卷轴的权重设为0。");
		register(answers, "Q013", 0,
				"魅魔的卷轴掉落不采用普通卷轴生成权重。");
		register(answers, "Q014", 3,
				"新游戏开始时会随机选择两套药剂权重之一。");
		register(answers, "Q015", 2,
				"其中一套药剂权重把经验药剂的权重设为0。");
		register(answers, "Q016", 1,
				"苍蝇的药剂掉落可用于判断治疗药剂。");
		register(answers, "Q017", 0,
				"死灵法师的药剂掉落可用于判断治疗药剂。");
		register(answers, "Q018", 3,
				"吸血蝙蝠的药剂掉落可用于判断治疗药剂。");
		register(answers, "Q019", 2,
				"非基座野生宝箱不承载流程固定生成的力量药剂。");
		register(answers, "Q020", 1,
				"非基座野生宝箱不承载流程固定生成的升级卷轴。");
		register(answers, "Q021", 0,
				"毒气房会保证本层生成1瓶净化药剂。");
		register(answers, "Q022", 3,
				"极速房会保证本层生成1瓶极速药剂。");
		register(answers, "Q023", 2,
				"火墙房会保证本层生成1瓶冰霜药剂。");
		register(answers, "Q024", 1,
				"浮空房会保证本层生成1瓶浮空药剂。");
		register(answers, "Q025", 0,
				"鱼房会保证本层生成1瓶隐形药剂。");
		register(answers, "Q026", 3,
				"木板房会保证本层生成1瓶液火药剂。");
		register(answers, "Q027", 2,
				"附魔石房会保证本层生成1瓶液火药剂。");
		register(answers, "Q028", 1,
				"三枚相同的腐梅之种固定对应力量药剂。");
		register(answers, "Q029", 0,
				"三枚相同的阳春草之种固定对应治疗药剂。");
		register(answers, "Q030", 3,
				"三枚相同的消逝草之种固定对应灵视药剂。");
		register(answers, "Q031", 2,
				"三枚相同的冰冠花之种固定对应冰霜药剂。");
		register(answers, "Q032", 1,
				"三枚相同的烈焰花之种固定对应液火药剂。");
		register(answers, "Q033", 0,
				"三枚相同的断肠苔之种固定对应毒气药剂。");
		register(answers, "Q034", 3,
				"三枚相同的速行蓟之种固定对应极速药剂。");
		register(answers, "Q035", 2,
				"三枚相同的致盲草之种固定对应隐形药剂。");
		register(answers, "Q036", 1,
				"三枚相同的风暴藤之种固定对应浮空药剂。");
		register(answers, "Q037", 0,
				"三枚相同的地缚根之种固定对应麻痹药剂。");
		register(answers, "Q038", 3,
				"三枚相同的魔皇草之种固定对应净化药剂。");
		register(answers, "Q039", 2,
				"三枚相同的星陨花之种固定对应经验药剂。");
		register(answers, "Q040", 1,
				"升级卷轴拆解后得到附魔符石。");
		register(answers, "Q041", 0,
				"鉴定卷轴拆解后得到感知符石。");
		register(answers, "Q042", 3,
				"祛邪卷轴拆解后得到探魔符石。");
		register(answers, "Q043", 2,
				"镜像卷轴拆解后得到羊群符石。");
		register(answers, "Q044", 1,
				"充能卷轴拆解后得到电击符石。");
		register(answers, "Q045", 0,
				"传送卷轴拆解后得到闪现符石。");
		register(answers, "Q046", 3,
				"催眠卷轴拆解后得到沉睡符石。");
		register(answers, "Q047", 2,
				"探地卷轴拆解后得到明示符石。");
		register(answers, "Q048", 1,
				"盛怒卷轴拆解后得到敌意符石。");
		register(answers, "Q049", 0,
				"复仇卷轴拆解后得到震爆符石。");
		register(answers, "Q050", 3,
				"恐惧卷轴拆解后得到恐惧符石。");
		register(answers, "Q051", 2,
				"嬗变卷轴拆解后得到强化符石。");
		register(answers, "Q052", 1,
				"配方力量药剂+4能量对应肌肉记忆合剂。");
		register(answers, "Q053", 0,
				"配方灵视药剂+4能量对应魔能透视合剂。");
		register(answers, "Q054", 3,
				"配方冰霜药剂+4能量对应极速冰冻合剂。");
		register(answers, "Q055", 2,
				"配方毒气药剂+4能量对应腐蚀酸雾合剂。");
		register(answers, "Q056", 1,
				"配方极速药剂+4能量对应精力回复合剂。");
		register(answers, "Q057", 0,
				"配方隐形药剂+4能量对应暗夜迷雾合剂。");
		register(answers, "Q058", 3,
				"配方浮空药剂+4能量对应暴风骤雨合剂。");
		register(answers, "Q059", 2,
				"配方净化药剂+4能量对应全面净化合剂。");
		register(answers, "Q060", 1,
				"配方经验药剂+4能量对应神意启发合剂。");
		register(answers, "Q061", 0,
				"配方麻痹药剂+10能量对应雷鸣魔药。");
		register(answers, "Q062", 3,
				"升级卷轴加6点炼金能量可制成注魔秘卷。");
		register(answers, "Q063", 2,
				"鉴定卷轴加6点炼金能量可制成预知秘卷。");
		register(answers, "Q064", 1,
				"镜像卷轴加6点炼金能量可制成虹卫秘卷。");
		register(answers, "Q065", 0,
				"充能卷轴加6点炼金能量可制成魔能秘卷。");
		register(answers, "Q066", 3,
				"传送卷轴加6点炼金能量可制成归返秘卷。");
		register(answers, "Q067", 2,
				"催眠卷轴加6点炼金能量可制成魅音秘卷。");
		register(answers, "Q068", 1,
				"探地卷轴加6点炼金能量可制成先见秘卷。");
		register(answers, "Q069", 0,
				"复仇卷轴加6点炼金能量可制成灵爆秘卷。");
		register(answers, "Q070", 3,
				"恐惧卷轴加6点炼金能量可制成梦魇秘卷。");
		register(answers, "Q071", 2,
				"嬗变卷轴加6点炼金能量可制成蜕变秘卷。");
		register(answers, "Q072", 1,
				"悲伤幽灵任务NPC会在2至4层出现。");
		register(answers, "Q073", 0,
				"2层对应腐臭老鼠任务。");
		register(answers, "Q074", 3,
				"3层对应豺狼诡术师任务。");
		register(answers, "Q075", 2,
				"4层对应巨钳螃蟹任务。");
		register(answers, "Q076", 1,
				"任务完成后从武器与护甲中二选一。");
		register(answers, "Q077", 0,
				"选中的幽灵任务奖励会立即鉴定。");
		register(answers, "Q078", 3,
				"老制杖任务NPC会在7至9层出现。");
		register(answers, "Q079", 2,
				"任务可能是腐梅之种、元素余烬或尸尘。");
		register(answers, "Q080", 1,
				"任务奖励是两根法杖中选择一根。");
		register(answers, "Q081", 0,
				"击杀腐莓核心后获得腐莓之种。");
		register(answers, "Q082", 3,
				"四根蜡烛需按十字形摆放。");
		register(answers, "Q083", 2,
				"尸尘位于木板房内的一个遗骸中。");
		register(answers, "Q084", 1,
				"巨魔铁匠会在12至14层出现。");
		register(answers, "Q085", 0,
				"没有巨魔给予的镐子就不能进入任务层。");
		register(answers, "Q086", 3,
				"每个暗金矿折算50人情。");
		register(answers, "Q087", 2,
				"暗金矿最多40个。");
		register(answers, "Q088", 1,
				"豺狼地卜师或巨型水晶各值1000人情。");
		register(answers, "Q089", 0,
				"小恶魔任务NPC会在17至19层出现。");
		register(answers, "Q090", 3,
				"任务奖励是一枚立即鉴定的诅咒戒指。");
		register(answers, "Q091", 2,
				"完成任务后小恶魔会在20层摆摊。");
		register(answers, "Q092", 1,
				"毒气房内有2个宝箱和1个遗骸。");
		register(answers, "Q093", 0,
				"血色哨卫能瞄准隐形玩家。");
		register(answers, "Q094", 3,
				"浮空房由悬崖或陷阱隔开，对面是一个宝箱。");
		register(answers, "Q095", 2,
				"鱼房中有3只巨型食人鱼。");
		register(answers, "Q096", 1,
				"木板房入口被木制路障封住。");
		register(answers, "Q097", 0,
				"献祭范围包含基座与灰烬的3乘3区域。");
		register(answers, "Q098", 3,
				"献祭完成会产生一件诅咒武器。");
		register(answers, "Q099", 2,
				"卷轴房至少包含鉴定卷轴或祛邪卷轴。");
		register(answers, "Q100", 1,
				"普通炼金房固定生成5点炼金能量。");
		register(answers, "Q101", 0,
				"普通炼金房不会在任一大区前两层生成。");
		register(answers, "Q102", 3,
				"石像武器必定带附魔。");
		register(answers, "Q103", 2,
				"石像护甲必定带刻印。");
		register(answers, "Q104", 1,
				"玩家上下左右各生成1只怨灵。");
		register(answers, "Q105", 0,
				"墓碑房怨灵闪避高，但生命值只有1。");
		register(answers, "Q106", 3,
				"宝箱只开金币，一区前三层也可能是魔能触媒。");
		register(answers, "Q107", 2,
				"该房间固定生成并消耗1把水晶钥匙。");
		register(answers, "Q108", 1,
				"六选三房间固定消耗3把水晶钥匙。");
		register(answers, "Q109", 0,
				"治疗泉会回满生命与饱食度。");
		register(answers, "Q110", 3,
				"任意物品都可丢入鉴定泉进行鉴定。");
		register(answers, "Q111", 2,
				"玩家在花园中饥饿速度降低三分之一。");
		register(answers, "Q112", 1,
				"普通商店固定出现在6、11、16层。");
		register(answers, "Q113", 0,
				"商店必定生成在上楼楼梯口附近。");
		register(answers, "Q114", 3,
				"6、11、16层不会生成任务NPC。");
		register(answers, "Q115", 2,
				"每局第1层绝对不会生成隐藏房间。");
		register(answers, "Q116", 1,
				"鼠王藏宝库是5层固定生成的隐藏房间。");
		register(answers, "Q117", 0,
				"代码明确排除鼠王英雄。");
		register(answers, "Q118", 3,
				"随机装备池跳过索引0的一阶装备。");
		register(answers, "Q119", 2,
				"随机模式通关会验证RANDOM_HERO徽章“鸿运当头”。");
		register(answers, "Q120", 1,
				"随机模式下鼠王从护甲技能池随机选择奖励。");
		register(answers, "Q121", 0,
				"两套卷轴权重中有一套把嬗变卷轴权重设为0。");
		register(answers, "Q122", 3,
				"魅魔的卷轴掉落独立于普通卷轴权重。");
		register(answers, "Q123", 2,
				"每区固定3张，可在过区后用数量推断。");
		register(answers, "Q124", 1,
				"记录每层拾取的颜色，遇到解密房时可交叉排除。");
		register(answers, "Q125", 0,
				"流程固定的力量药剂不会放在玩家可能错过的选择结构中。");
		register(answers, "Q126", 3,
				"非基座宝箱不承载解密房固定药剂，命中只是随机结果。");
		register(answers, "Q127", 2,
				"一区前三层的毒气房容器可能装有魔能触媒。");
		register(answers, "Q128", 1,
				"极速房的武器或护甲自带等级范围为+0至+3。");
		register(answers, "Q129", 0,
				"水爆魔药能将特殊地块变成水，从而绕过哨卫机制。");
		register(answers, "Q130", 3,
				"有多个未知药剂时，建议在距火墙一格且不在水上饮用。");
		register(answers, "Q131", 2,
				"可先查看是否为升级、力量或触媒，再决定是否消耗浮空。");
		register(answers, "Q132", 1,
				"把巨型食人鱼拉上岸可令其失去水中优势。");
		register(answers, "Q133", 0,
				"正面附魔分支会追加诅咒并升1级，因此至少+1。");
		register(answers, "Q134", 3,
				"怨灵会在四个正交方向生成，墙或雕像可占据位置。");
		register(answers, "Q135", 2,
				"开出怨灵则装备必定诅咒，否则必定无诅咒。");
		register(answers, "Q136", 1,
				"3.3版本后可消耗5点充能直接开门。");
		register(answers, "Q137", 0,
				"升级卷轴可能出现，但最低保证是鉴定或祛邪之一。");
		register(answers, "Q138", 3,
				"普通炼金房不会生成在任何大区的前两层。");
		register(answers, "Q139", 2,
				"石像武器必有附魔，护甲必有刻印。");
		register(answers, "Q140", 1,
				"房间按权重排位，经验和嬗变属于最低权重，不能出现在最外侧。");
		register(answers, "Q141", 0,
				"饮用会回满生命和饱食度，并移除负面状态及装备诅咒。");
		register(answers, "Q142", 3,
				"会获悉本层物品位置并发现隐藏门和陷阱。");
		register(answers, "Q143", 2,
				"花园中饥饿速度降低三分之一，并获得隐形。");
		register(answers, "Q144", 1,
				"进商店层前调整药剂、卷轴、投武和法杖数量可影响包裹。");
		register(answers, "Q145", 0,
				"一区固定2个隐藏房，五区固定3个。");
		register(answers, "Q146", 3,
				"隐藏卷轴房的独立权重中嬗变为6，最高。");
		register(answers, "Q147", 2,
				"隐藏炼金房的独立权重中经验为6，最高。");
		register(answers, "Q148", 1,
				"食物房固定生成一株无味果。");
		register(answers, "Q149", 0,
				"蜜蜂仅有罐子周围三格视野与索敌范围。");
		register(answers, "Q150", 3,
				"同种子的武器与护甲奖励自带等级相同。");
		register(answers, "Q151", 2,
				"持有尸尘后，玩家附近每隔一定回合生成怨灵。");
		register(answers, "Q152", 1,
				"同等级时默认给左侧装备+1。");
		register(answers, "Q153", 0,
				"若判定消除，实际保留附魔或刻印，但硬化效果消失。");
		register(answers, "Q154", 3,
				"五区越深入视野越小，火把用于维持探索视野。");
		register(answers, "Q155", 2,
				"三个不同种子时，50%完全随机，50%由种子决定。");
		register(answers, "Q156", 1,
				"此配方约67%按种子决定，约33%完全随机。");
		register(answers, "Q157", 0,
				"肌肉记忆合剂的对应配方是力量药剂+4能量。");
		register(answers, "Q158", 3,
				"魔能透视合剂的对应配方是灵视药剂+4能量。");
		register(answers, "Q159", 2,
				"极速冰冻合剂的对应配方是冰霜药剂+4能量。");
		register(answers, "Q160", 1,
				"腐蚀酸雾合剂的对应配方是毒气药剂+4能量。");
		register(answers, "Q161", 0,
				"精力回复合剂的对应配方是极速药剂+4能量。");
		register(answers, "Q162", 3,
				"暗夜迷雾合剂的对应配方是隐形药剂+4能量。");
		register(answers, "Q163", 2,
				"暴风骤雨合剂的对应配方是浮空药剂+4能量。");
		register(answers, "Q164", 1,
				"全面净化合剂的对应配方是净化药剂+4能量。");
		register(answers, "Q165", 0,
				"神意启发合剂的对应配方是经验药剂+4能量。");
		register(answers, "Q166", 3,
				"雷鸣魔药的对应配方是麻痹药剂+10能量。");
		register(answers, "Q167", 2,
				"注魔秘卷由升级卷轴加6点炼金能量制成。");
		register(answers, "Q168", 1,
				"预知秘卷由鉴定卷轴加6点炼金能量制成。");
		register(answers, "Q169", 0,
				"虹卫秘卷由镜像卷轴加6点炼金能量制成。");
		register(answers, "Q170", 3,
				"魔能秘卷由充能卷轴加6点炼金能量制成。");
		register(answers, "Q171", 2,
				"归返秘卷由传送卷轴加6点炼金能量制成。");
		register(answers, "Q172", 1,
				"魅音秘卷由催眠卷轴加6点炼金能量制成。");
		register(answers, "Q173", 0,
				"先见秘卷由探地卷轴加6点炼金能量制成。");
		register(answers, "Q174", 3,
				"灵爆秘卷由复仇卷轴加6点炼金能量制成。");
		register(answers, "Q175", 2,
				"梦魇秘卷由恐惧卷轴加6点炼金能量制成。");
		register(answers, "Q176", 1,
				"蜕变秘卷由嬗变卷轴加6点炼金能量制成。");
		register(answers, "Q177", 0,
				"随机模式把每个原本大于0的概率项改为1。");
		register(answers, "Q178", 3,
				"随机器由地牢种子、常量盐和职业序号共同初始化。");
		register(answers, "Q179", 2,
				"类型已知、完整鉴定或无诅咒施法后才应用效果图标。");
		register(answers, "Q180", 1,
				"刻印列表使用地牢种子异或固定盐后洗牌。");
		register(answers, "Q181", 0,
				"鉴定卷轴在两套卷轴生成权重中均为3。");
		register(answers, "Q182", 3,
				"祛邪卷轴在两套卷轴生成权重中均为2。");
		register(answers, "Q183", 2,
				"镜像卷轴的两套权重为1和2。");
		register(answers, "Q184", 1,
				"充能卷轴的两套权重为2和1。");
		register(answers, "Q185", 0,
				"传送卷轴的两套权重为1和2。");
		register(answers, "Q186", 3,
				"治疗药剂在两套药剂生成权重中均为3。");
		register(answers, "Q187", 2,
				"灵视药剂在两套药剂生成权重中均为2。");
		register(answers, "Q188", 1,
				"冰霜药剂的两套权重为1和2。");
		register(answers, "Q189", 0,
				"液火药剂的两套权重为2和1。");
		register(answers, "Q190", 3,
				"对应+0至+3的概率依次为50%、38.3%、10%、1.7%。");
		register(answers, "Q191", 2,
				"石像装备+0至+2的概率依次为75%、20%、5%。");
		register(answers, "Q192", 1,
				"幽灵奖励+0至+3的概率依次为50%、30%、15%、5%。");
		register(answers, "Q193", 0,
				"法杖+1至+3的概率依次为2/3、4/15、1/15。");
		register(answers, "Q194", 3,
				"戒指+2至+4的概率依次为2/3、4/15、1/15。");
		register(answers, "Q195", 2,
				"锻造装备+0至+3的概率依次为30%、45%、20%、5%。");
		register(answers, "Q196", 1,
				"3.2版本后的物品权重中，神器和戒指各占三分之一。");
		register(answers, "Q197", 0,
				"二区有75%概率生成2个，25%概率生成3个隐藏房。");
		register(answers, "Q198", 3,
				"其他食物降低三分之二；大饼计入饱腹持续后只降低二分之一。");
		register(answers, "Q199", 2,
				"随机模式跳过一阶，并把所有可生成的二至五阶等阶权重设为1。");
		register(answers, "Q200", 1,
				"附魔池内容用地牢种子与固定盐初始化后洗牌，同种子结果一致。");
		DungeonDoctorQuestionBankValidator.validateIds(answers.keySet());
		return Collections.unmodifiableMap(answers);
	}

	private static void register(Map<String, Answer> answers, String id,
			int correctIndex, String explanation) {
		if (id == null || id.trim().isEmpty()
				|| correctIndex < 0 || correctIndex >= QuizQuestion.OPTION_COUNT
				|| explanation == null || explanation.trim().isEmpty()
				|| answers.containsKey(id)) {
			throw new IllegalArgumentException("invalid dungeon doctor answer");
		}
		answers.put(id, new Answer(correctIndex, explanation));
	}

	public static final class Answer {

		private final int correctIndex;
		private final String explanation;

		Answer(int correctIndex, String explanation) {
			if (correctIndex < 0 || correctIndex >= QuizQuestion.OPTION_COUNT
					|| explanation == null || explanation.trim().isEmpty()) {
				throw new IllegalArgumentException("invalid dungeon doctor answer");
			}
			this.correctIndex = correctIndex;
			this.explanation = explanation;
		}

		public int correctIndex() {
			return correctIndex;
		}

		public String explanation() {
			return explanation;
		}
	}
}
