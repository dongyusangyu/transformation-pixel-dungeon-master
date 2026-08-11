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
		register(answers, "Q071", 0,
				"非Boss且处于主线分支的楼层创建时，会固定加入1张蜕变卷轴。");
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
				"不计相关天赋时，每块暗金矿值50人情，击杀任务强敌另得1000人情；达到3000人情上限共需40块暗金矿。");
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
				"血色哨卫的锁定逻辑不受玩家隐形状态影响。");
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
				"墓碑会尝试在玩家的四个正交相邻格各生成一只怨灵；被阻挡的位置不会生成。");
		register(answers, "Q105", 0,
				"墓碑房怨灵闪避高，但生命值只有1。");
		register(answers, "Q106", 3,
				"宝箱只开金币，一区前三层也可能是魔能触媒。");
		register(answers, "Q107", 2,
				"该房间固定生成并消耗1把水晶钥匙。");
		register(answers, "Q108", 1,
				"六选三房间固定消耗3把水晶钥匙。");
		register(answers, "Q109", 0,
				"无相关挑战影响时，治疗泉会直接回满生命与饱食度。");
		register(answers, "Q110", 3,
				"任意物品都可丢入鉴定泉进行鉴定。");
		register(answers, "Q111", 2,
				"玩家在花园中饥饿速度降低三分之一。");
		register(answers, "Q112", 1,
				"普通商店固定出现在6、11、16层。");
		register(answers, "Q113", 0,
				"普通商店的生成位置固定参考本层入口，也就是上楼楼梯口。");
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
				"随机模式的王冠界面会显示本局预先随机出的护甲技能候选。");
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
				"非随机模式下，极速房生成装备最高自带+2，再有三分之一概率额外升1级，因此最高为+3。");
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
				"石像武器必有附魔，护甲必有刻印；两件装备掉落时也都会被鉴定。");
		register(answers, "Q140", 1,
				"房间会分别按照药剂与卷轴的默认生成权重排序，再将其放入对应位置。");
		register(answers, "Q141", 0,
				"治疗泉会回满生命与饱食度，清除治疗药剂可治愈的状态，并解除已装备物品的诅咒。");
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
				"敌对蜜蜂只会索敌蜂罐周围三格内的目标，并会返回蜂罐附近。");
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
				"两同一种子加一异种时，75%按投入种子决定，25%从普通药剂池完全随机。");
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
				"蜕变结晶由8张蜕变卷轴加1点炼金能量制成。");
		register(answers, "Q177", 0,
				"随机模式把物品生成器中每个原本大于0的权重改为1。");
		register(answers, "Q178", 3,
				"随机器由地牢种子、常量盐和职业序号共同初始化。");
		register(answers, "Q179", 2,
				"法杖类型已知、完整鉴定，或成功施放过非诅咒效果后，才显示对应效果图标。");
		register(answers, "Q180", 1,
				"刻印列表使用地牢种子异或固定盐后洗牌。");
		register(answers, "Q181", 1,
				"一区固定有3张升级卷轴，但野生宝箱不会生成升级卷轴；同图案又代表同类型，因此这3张都不是升级卷轴。");
		register(answers, "Q182", 1,
				"火墙会在合规位置固定生成冰霜药剂，野生宝箱中的冰霜药剂只来自普通随机生成，二者恰好同类是巧合。");
		register(answers, "Q183", 1,
				"一区前三层的魔能触媒可能藏在毒气房的2个宝箱或1个遗骸中；其他位置已搜尽时，应回去检查这些容器。");
		register(answers, "Q184", 1,
				"无法确定冰霜药剂时，应在距火墙1格且不在水上的位置饮用；这样能让冰霜生效，也避免把力量药剂误掷出去。");
		register(answers, "Q185", 1,
				"先用炸弹或震爆符石远程炸开宝箱即可查看物品，再决定是否值得消耗浮空药剂前往拾取。");
		register(answers, "Q186", 1,
				"虚空锁链能把巨型食人鱼拉上岸解决威胁，因此可以保留本层固定生成的隐身药剂再取宝箱。");
		register(answers, "Q187", 2,
				"六选三属于可能错过物品的选择结构，不会放入固定的力量药剂和升级卷轴；相同外观对应的背包物品也可排除这两类。");
		register(answers, "Q188", 0,
				"极速房宝箱出现液火药剂，说明本层还有为隐藏附魔石房固定生成的液火药剂，应重点检查可烧开的书架。");
		register(answers, "Q189", 0,
				"种子袋初始计1分，另三种包裹初始为0分；种子符石有2件时种子袋得3分，因此法杖与投掷武器合计至少4件才能让魔法筒袋严格最高。");
		register(answers, "Q190", 0,
				"浮空药剂先花4点炼成暴风骤雨合剂，再花8点炼成水爆魔药，总能耗为12点。");
		register(answers, "Q191", 2,
				"横扫对其他可攻击敌人造成向上取整的100×(1+2)/(10+2)=25点物理伤害；主目标A不会重复受到横扫伤害。");
		register(answers, "Q192", 1,
				"武技精通的基础上限为min(2+(19-1)/3,8)=8点；勇士再增加2点，上限为10点，并使武技充能回复速度提升50%。");
		register(answers, "Q193", 3,
				"符石转换的基础消耗为8点，+2符石专家减少4点，因此每颗消耗4点；连续转换3颗共消耗12点炼金能量。");
		register(answers, "Q194", 0,
				"+1符文爆破按印记层数×0.25为所有法杖充能；8层使每根回复2点，3根未满充法杖合计回复6点。");
		register(answers, "Q195", 2,
				"极恶中队不限定召唤来源或怪物种类；只要怪物处于盟友阵营，+1同步大部分物攻特效，+2还会同步大部分物攻增伤。");
		register(answers, "Q196", 1,
				"背包没有手里剑箱时，+2忍者便当改为提供2回合神器充能；食物来自丰饶之角时，该充能不会作用于丰饶之角自身。");
		register(answers, "Q197", 3,
				"粘液温室通常让体内发芽不耗回合，但荒芜之地会改用挑战耗时；升级后耗时由20回合降至15回合，+2同时回复30点饱食度。");
		register(answers, "Q198", 0,
				"武技训练已经使非决斗家能够使用武技，因此+2专注一餐触发常规效果并获得1点武技充能，不再触发下次攻击增加英雄等级一半伤害的兼容效果。");
		register(answers, "Q199", 2,
				"+2炼金屏障让每次消耗炼金能量获得2层屏障，工具箱供能升级也会触发；两次本应获得4层但上限为3层，敌人造成正伤害后消耗1层，剩2层。");
		register(answers, "Q200", 1,
				"DM-400的物理攻击通常施加3回合指令标记；+2指令餐把进食后的前3次提高到6回合，效果耗尽后第4次恢复为3回合。");
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
