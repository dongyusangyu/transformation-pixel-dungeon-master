package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.SpecialPackage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldIngot;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Heavy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Sweeping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WalkStick;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.SlimeBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatDemonSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatShoperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PhantomLandPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarriorBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class v0_2_X {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
        add_v0_2_7fixChanges(changeInfos);
        add_v0_2_7Changes( changeInfos);
        add_v0_2_6Changes(changeInfos);
        add_v0_2_5Changes(changeInfos);
        add_v0_2_4Changes(changeInfos);
        add_v0_2_3Changes(changeInfos);
        add_v0_2_2Changes(changeInfos);
        add_v0_2_1Changes(changeInfos);
        add_v0_2_0Changes(changeInfos);
    }
    public static void add_v0_2_0Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.FREEMAN, 1), "新增英雄：绅士",
                "_-_ 绅士缺乏战斗能力，没有专精和护甲技能，不过好在其有无限的潜力激发，能够从冒险新手蜕变成一方强者\n" +
                        "_-_ 该职业拥有一个特别宝箱，可以随机开出职业道具，3层天赋有4个可蜕变天赋槽\n" +
                        "_-_ 使用一张蜕变秘卷即可解锁绅士及成就\n"
        ));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 天赋_活动肌肉_加强，持续时间从100回合提高至300回合\n" +
                        "_-_ 改动_破冰行动_、_破损核心_等天赋的伤害类型\n" +
                        "_-_ 天赋_借痛排毒_削弱，获得全面净化效果的概率降低\n" +
                        "_-_ 矮人国王天赋_王之加护_不再有额外的免伤效果\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月28日\n"+
                        "_-_ 击败古神后_小恶魔_会在25层祝玩家新年快乐。\n"+
                        "_-_ 新增_批量升级_按钮，需要鉴定升级道具才能使用，一次能够使用10张升级卷轴\n"+
                        "_-_ _升华秘卷_现在可被收纳进卷轴筒，物品图鉴增加升华秘卷、红包以及特别宝箱\n"

        ));
    }
    public static void add_v0_2_1Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_1_个正面天赋：强化魔典\n"+
                        "_-_ 以上新增内容均由_Secher-Nbiw_开发完成\n"
        ));
        changes.addButton(new ChangeButton(new ItemSprite(new GoldIngot()), "黄金元宝",
                "新增新饰品：黄金元宝\n" +
                        "_-_ 通过消耗金钱获得额外的伤害倍率\n" +
                        "_-_ 本饰品由_Secher-Nbiw_制作\n"

        ));
        changes.addButton( new ChangeButton(new Image(new GreatDemonSprite()), "新增返程Boss战：大恶魔",
                "_-_ _开启条件：_携带护符与25层小恶魔对话触发\n" +
                        "_-_ 本场Boss战是一个可选择的挑战，并非玩家通关阻碍，故数值极高，要谨慎对待。如果玩家搞清楚该场Boss战机制，会变得相当简单\n" +
                        "_-_ 本场Boss战会给玩家Boss得分增加10000分，击败Boss可以获得新徽章，在游戏背景故事新增一则返程见闻\n"+
                        "_-_ 本场Boss战敌人贴图均由_可则_和_幻影の人食い雑魚_绘制\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "挑战更新",
                "_-_ 新增_1_个挑战\n"+
                        "_-_ _负面天赋：_玩家到达2，6，11，21层会强制从2个负面天赋中二选一，目前新增17个负面天赋可供玩家游玩\n"+
                        "_-_ _负面：_永恒诅咒，饭桶，命运抉择，无尽恶意，有气无力，营养不良，目光短浅，蝙蝠血清，近视眼，陆游(未实装)，九龙拉馆，奸商，致敬牢大，好吃到爆，脑袋着地，喝水塞牙，生命在于运动，圣杯弱化\n"
        ));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 天赋_万灵药水_、_借痛排毒_、_求生意志_、_工程技师_、_反戈一击_、_盛食厉兵_和_算盘_加强\n" +
                        "_-_ 此外还有各类杂项Bug修复以及挑战界面调整。\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年2月1日\n"

        ));


    }
    public static void add_v0_2_2Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_23_个正面天赋，_10_个法术天赋，_21_个负面天赋\n"+
                        "_-_ _1层：_书中黄金，白幽馈赠，人身攻击，蜂蜜鱼缸，金币巧克力\n"+
                        "_-_ _2层：_探险直觉，釜底抽薪，万毒入体，种子回收，享乐主义，引火上身，量子骇入，击落卫星，顷刻炼化\n" +
                        "_-_ _3层：_极限施法，精准射击，宝藏嗅觉，超越极限，健康食物，极限反应，空蝉幻舞，圣光信仰，常启书室\n"+
                        "_-_ _1层法术：_牧羊原旨，银光锐语，升天咒\n"+
                        "_-_ _2层法术：_神明庇佑，圣法皈依，赎罪券，创世纪\n" +
                        "_-_ _3层法术：_净化邪恶，神力领域风暴，复活术\n"+
                        "_-_ _负面：_心智崩塌，无可回避，寄生，泥头车，糊人总冠军，畏光，失控魔法，冬泳怪鸽，干柴烈火，见异思迁，饥饿难耐，会员制餐厅，长启速逝，饭前洗手，荆棘丛生，热情满满，战意盈然，相位利爪，深层恐惧，门都没有，上升气流\n"+
                        "_-_ 于fix1完全实装负面天赋_陆游_及其衍生敌人_巨型陆地食人鱼_\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.FREEMAN, 6), "绅士完善",
                "_-_ 于_fix1_将初始武器变更为_手杖_，初始伤害1~6点，攻击概率叠加护盾\n" +
                        "_-_ 于_fix2_将_手杖_加入图鉴，并由_DM-216_对小人进行重绘\n" +
                        "_-_ 于_fix3_将返程结算立绘重绘，同时还将特别宝箱改为_公文包_并能够自选职业道具，均由_DM-216_进行绘制\n"+
                        "_-_ 于_fix6_新增遗物破损皮包，能够开出一些金币\n"+
                        "_-_ 于_fix7_为公文包增加了神圣法典选项\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.CLERIC, 6), "内容同步",
                "_-_ 于_fix7_进行_破碎v3.0_的内容同步(感谢_委员会全员_以及_墨意-天志_为内容同步更新进行的大量测试)\n" +
                        "_-_ _牧师实装：_牧师实装并对牧师初始天赋进行调整。牧师天赋除启蒙圣餐外，其余天赋被其他英雄蜕变出来均无效果变更，同时启蒙圣餐被其他英雄蜕变出来的效果也进行了改动，而符文复制新增无法复制蜕变秘卷的改动\n" +
                        "_-_ _杂项内容：_决斗家的镶钉手套、双钗、魔岩拳套和十字弩的武技加强；对饰物苔藓丛簇、拟箱利齿、十三叶草、混沌香炉、遗忘碎片和陷阱元件的效果改动；狙击手天赋联动升级的效果改动；游戏存档界面的ui改动；暗影斗篷和神圣法典无法嬗变的改动；对疯狂强盗的描述补充\n"+
                        "_-_ 于_fix8_同步_破碎v3.0_对财富戒指的改动(装卸戒指会继承保底次数而非直接重置)\n"+
                        "_-_ 于_fix9_同步_破碎v3.0.1_苔藓丛簇和神圣武器、神圣护甲的效果"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), "内容更新",
                "_-_ 完全实装饰物_黄金元宝_\n"+
                        "_-_ 于_fix1_加入了幽妹的特殊对话内容，当玩家变身美少女“战士”后在每次下楼时概率触发(不与幽妹原对话内容同时触发)\n"+
                        "_-_ 于_fix2_实装沉重诅咒，使武器力量需求+2\n"+
                        "_-_ 于_fix5_通关排行榜的挑战页面加入一个奇妙的入口，随后于_fix6_移除\n"+
                        "_-_ 于_fix7_实装_横扫附魔_，攻击一个目标时有几率对使用者周围所有目标造成一定打击(决斗家护甲技能元素打击效果未定)，由_Secher-Nbiw_制作\n"+
                        "_-_ 同时还实装了_圣典拓本_，使其他英雄也能像牧师一样释放法术，不过由于其不稳定性，充能无法以任何形式回复，同时有些需要选取目标释放的法术在不选取目标尝试取消释放后依旧会消耗充能，需谨慎使用。拓本可通过炼金获取(具体可查看炼金图鉴-强化武器部分，材料中的奥术刻笔仅为需要放入，不会消耗)\n"+
                        "_-_ 于_fix9_补充_横扫附魔_的元素打击效果\n\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), "视觉更新",
                "_-_ _DM-300_贴图改动，由_Lefted_绘制\n" +
                        "_-_ 于_fix3_进行主界面标题重绘，由_DM-216_绘制\n" +
                        "_-_ 在_fix3~7_更新期间为大量天赋进行了描述补充、注释补充以及高亮内容调整\n"+
                        "_-_ 于_fix7_重绘了绅士小人、绅士的英雄护甲、升华秘卷、符石、大恶魔徽章、天狗天赋等内容，同时还重绘了所有ui界面，顺便将DM-300天赋辟谷之术更名为_电子养胃_\n"+
                        "_-_ 于_fix9_更新饱食度ui，类似mc的饱食度设计\n"
        ));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 于_fix2_削弱天赋_正义惩戒_给予的祝福效果\n" +
                        "_-_ 于_fix3_加强_奸奇信徒_，使其远程攻击能够消除全面净化效果\n" +
                        "_-_ 同时加强负面天赋_陆游_，现在还能够生成幻影陆地食人鱼，贴图由_可则_和_DM-216_绘制\n" +
                        "_-_ 于_fix4_将天赋_茉莉花茶_和_军用水袋_从2层天赋池移动至1层天赋池\n" +
                        "_-_ 天赋_摸鱼时间+2_效果新增：鱼房额外生成1条巨型食人鱼\n" +
                        "_-_ 加强天赋_马猴烧酒_，现在可以通过阅读神器魔典触发\n" +
                        "_-_ 加强天赋_天地之力_，每级增伤从12%上调至15%，地缚根护甲能够触发(守望者踩地缚根给的树肤不触发)\n" +
                        "_-_ 削弱天赋_涿郡屠户_，增加掉落的等级限制\n" +
                        "_-_ 削弱天赋_享乐主义_，伤害由3/6下调至2/4\n" +
                        "_-_ 于2月16日——_DM-216_的生日加强负面天赋_无尽恶意_，+0时的生效概率上调至40%，扣除的生命值由“当前生命值的10%”变成“生命值上限的10%”\n" +
                        "_-_ 于_fix6_将法师魔杖的初始伤害由1~6上调至1~7\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年2月2日-4月12日\n"+
                        "_-_  于_fix3_对批量升级功能进行改动：对投掷武器进行批量升级只能起到一张升级的效果。\n"+
                        "_-_  于_fix5_限制存档数量。\n"+
                        "_-_  于_fix8_将圣典拓本转化的能量从8提升至3*剩余充能数。\n"+
                        "_-_  于_fix10_新增资源类天赋的物品获得上限，通过升级及蜕变获得资源的效果至多触发16次(绅士为29次)。\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 于_fix2_修复负面天赋_畏光_常态生效的问题，同时还修复了人物选择界面绅士介绍图标的显示问题\n"+
                        "_-_ 于_fix3_修复大恶魔Buff图标显示异常的问题\n"+
                        "_-_ 于_fix4_修复绅士介绍和开始界面的显示问题，同时还修复了部分天赋的描述问题\n"+
                        "_-_ 于_fix5_修复负面天赋_泥头车_只对巨型精英生效的问题，同时将天赋_细嚼慢咽_的饱腹效果维持在450/150回合(后者为启用挑战缩餐节食的情况\n"+
                        "_-_ 于_fix8_修复_绅士_三层天赋生效异常问题，同时修复饰物凝血之瓶和负面天赋寄生的文本显示问题\n"
        ));

    }

    public static void add_v0_2_3Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_7_个正面天赋，_4_个负面天赋\n"+
                        "_-_ _1层：_含沙射影，自动拾取\n"+
                        "_-_ _2层：_诅咒护体，势能转换，涅槃\n" +
                        "_-_ _3层：_灰烬之弓，速行藓神教\n"+
                        "_-_ _负面：_蚀骨之火，跳脸硅胶，草木皆兵，灾厄诅咒\n"
        ));
        changes.addButton( new ChangeButton(new Image(new GreatShoperSprite()), "新增返程Boss战：店主",
                "_-_ _开启条件：_携带护符与20层店主对话触发\n" +
                        "_-_ 本场Boss战同样会给玩家Boss得分增加10000分，击败Boss可以获得新徽章，在游戏背景故事新增一则返程见闻\n" +
                        "_-_ 本场Boss战敌人贴图由_可则_、_幻影の人食い雑魚_、_DM-216_和_放縦鵺_绘制\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "挑战更新",
                "_-_ 新增_2_个挑战\n"+
                        "_-_ _恶劣环境：_使破碎原有挑战提升至等级2，未勾选任何其他挑战则随机启用一项挑战\n"+
                        "_-_ _极端环境：_使破碎原有挑战提升至等级3，自动启用恶劣环境挑战\n"+
                        "_-_ 仅为信念护体、恐药异症、集群智能、没入黑暗、禁忌咒文和荒芜之地添加新的挑战效果\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.SHPX), "内容同步",
                "_-_ 对_破碎v3.1_的内容进行同步：楼层改动，木桶及后续障碍物可用镐子或炸弹清除，且炸弹只能清除3x3范围障碍；牧师增强与削弱；新成就；探索分计算方式；新敌人(豺狼流寇和寄居蟹)和新饰物(雪貂绒束)；战士天赋丰盛一餐、受衅怒火和液蕴意志改动，关于纹章部分只修改贴图，不改变其机制及相关天赋\n"
        ));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 削弱法术天赋_牧羊原旨_，+1释放的羊群范围缩小，+2能耗从2点上调至3点\n" +
                        "_-_ 加强法术天赋_赎罪券_，金币消耗从500/300下调至200/15\n" +
                        "_-_ 加强1阶武器_手杖_，初始伤害从1~6上调至1~7\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年7月23日\n"+
                        "_-_  移除了_升华秘卷_的警告界面并调整其逻辑。\n"
        ));


    }
    public static void add_v0_2_4Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 可蜕变的正面天赋划分为_物攻_，_法伤_，_特效_，_资源_，_法术_，_辅助_，_其他_共7类，在使用蜕变秘卷蜕变天赋时可选择类型，该类型天赋将以3倍概率出现在选项中。具体天赋分类请到对应天赋详情页查看\n"+
                        "_-_ 负面天赋划分为_12个通用_负面天赋，和_4组每组8个版本限定_负面天赋，每次大版本更新时改变限定天赋组。具体负面天赋划分到天赋详情页查看\n"+
                        "_-_ 本版本限定负面天赋组为：_限定1_\n"+
                        "_-_ 新增共计_5_个正面天赋，_5_个法术天赋，_1_个负面天赋\n"+
                        "_-_ _1层：_灵光闪现，预知秘卷爱好者\n"+
                        "_-_ _2层：_火球术，女巫媚药\n" +
                        "_-_ _3层：_溃决之手\n"+
                        "_-_ _1层法术：_奸奇咒法，聚焦光线\n"+
                        "_-_ _2层法术：_神行术，荆棘之鞭\n" +
                        "_-_ _3层法术：_转愈圣术\n"+
                        "_-_ _负面：_手滑点错\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), "视觉更新",
                "_-_ 标题界面、Boss击杀、失败等同步_破碎v3.0_的美术风格，由_DM-216_绘制\n"
        ));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "内容改动",
                "_-_  更新日期：2025年7月30日-7月31日\n"+
                        "_-_  为_精英强敌_和_绝命头目_挑战新增等级2和等级3的内容\n"+
                        "_-_  同步了破碎对于_臃肿诅咒_和_狱火刻印_的改动\n"+
                        "_-_  于_fix1_使得力量药剂不再因为坠入深渊而碎裂，同时使得被变成魔法绵羊的宝箱怪能够掉落物品"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复天赋_我全都要_和_营养不良_的失效问题，同时修复天赋_超越极限_生效异常的问题\n"+
                        "_-_ 修复成就_光荣凯旋_获取异常问题\n"+
                        "_-_ 于_fix1_修复神圣精英敌人坠落导致游戏卡死的问题，同时修复天赋_溃决之手_触发异常问题\n"
        ));
    }

    public static void add_v0_2_5Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 本版本限定负面天赋组为：_限定2_\n"+
                        "_-_ 新增共计_13_个正面天赋，_7_个法术天赋\n"+
                        "_-_ _1层：_液体感知，水波荡漾，狩猎直觉，雷雨发庄稼，疯狂舞者\n"+
                        "_-_ _2层：_自然亲和，优质吸收，食人心智，高速突破\n" +
                        "_-_ _3层：_盈能生命，致残毒云，恶魔之焰，投掷回收\n"+
                        "_-_ _1层法术：_圣愈一击\n"+
                        "_-_ _2层法术：_天启，装备赐福，灼热强光\n" +
                        "_-_ _3层法术：_神圣手雷，刀刃阶梯，黄金回旋\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.SLIMEGIRL, 6), "史莱姆娘",
                "_-_ 史莱姆娘只有9点力量，但拥有更高的初始生命值和生命成长。同时初始穿戴无法脱下的粘液护甲，会随史莱姆娘等级提升而提高等级和等阶。史莱姆娘还可以花费一回合让种子在体内发芽，并立刻触发种子对应的植被效果。击败粘咕即可解锁史莱姆娘及成就\n" +
                        "_-_ _两个转职：_水灵史莱姆和暗液史莱姆\n" +
                        "_-_ _三个护甲技能：_阳春法阵、飞湍瀑流和狂史日记\n"+
                        "_-_ 角色由_叶·凌卡缇娜·霜麟_设计，_可则_和_DM-216_绘制美术素材，_Secher-Nbiw_制作\n",
                        "_-_ 于_fix1_新增1阶投掷武器_粘液球_，由史莱姆娘初始携带2个，击中敌人时给予3回合黏糊糊效果，使敌人移速减半，由_DM-216_提议制作\n"+
                        "_-_ 加强_粘液护甲_，从原来的每6级一次升级加强到每3级一次升级，最多+5\n"+
                        "_-_ 加强_征讨者的长矛_，初始伤害从1~12上调至1~14\n"+
                        "_-_ 加强_暗液史莱姆_，攻击附带淤泥的概率上调至50%，暗液钩爪冷却下降至50，天赋_暗液缠身_和_强效淤泥_得到加强\n"+
                        "_-_ 于_fix2_改动_粘液护甲_，现在无法使用诅咒棱晶升级，但依然和_fix1_版本一样可以通过食用邪能碎片达到相同的效果\n"+
                        "_-_ 移除史莱姆娘_英雄液铠_的转移功能\n"+
                        "_-_ 削弱_水灵史莱姆_的天赋_水灵强愈_\n"+
                        "_-_ 削弱_暗液史莱姆_的_暗液钩爪_，设置12格的最大距离限制\n"+
                        "_-_ 于_fix3_调整护甲技能_狂史日记_，击杀敌人会有掉落物产生，但充能消耗由75提升至90\n"+
                        "_-_ 削弱_粘液护甲_，自动升级所需史莱姆娘等级从3/6/9/12/15调整为3/9/15/21/27\n"+
                        "_-_ 削弱天赋_暗液缠身+3_，恢复的饱食度从90调整为45\n"+
                        "_-_ 于_fix4_调整天赋_暗液缠身+3_，使用暗液钩爪将敌人拉下悬崖后不再能重置cd\n"

        ));
        changes.addButton( new ChangeButton(new Image(new WarriorBossSprite()), "新增5层Boss：战士？",
                "_-_ _战士？_和_粘咕_根据种子有1/2概率生成\n" +
                        "_-_ _战士？_拥有狂战士和角斗士的部分特性，以及能够使用护甲技能。击败战士？可掉落纹章残蜡、1~3个口粮和专属_升华秘卷_并能获得_饭桶终结者_成就\n" +
                        "_-_ 新增_3_个战士？天赋：最强之盾，连击套餐，陷阵之志\n"+
                        "_-_ 于_fix1_调整战士？的技能cd，现在每10回合能够进行一次英勇之跃\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "挑战更新",
                "_-_ 于fix4新增_1_个挑战\n"+
                        "_-_ _测试时间：_与破碎注解版一样的测试时间挑战，便于玩家进行游戏测试，目前仅为_v2.4_\n"
        ));
        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SHURIKEN), "内容同步",
                "_-_ 于_fix4_进行_破碎v3.2_的内容同步\n" +
                        "_-_ _投武改动：_主要同步关于投武重做的内容，大部分削弱内容不作保留。投武耐久维持原状，原本是10和15耐久的投武不进行耐久削弱，当然飞刺也继续保持10耐久而不进行增强；投武升级的耐久提升仅从x3降低为x2；2阶及以上的投武伤害下限成长依旧为2，1阶投石和飞刺的成长从1~1改回1~2，均不进行削弱；不改动手里剑，保持为移动后投掷不消耗回合并且无cd限制；不削弱飞斧的流血；不削弱回旋镖的耐久，依然为10；不削弱苦无和方石的基础伤害上限；不削弱流星索和飞斧的伤害上限成长；保留投武空射固定消耗1回合的特性，相当于正常的丢出物品；保留投武被炸减少耐久的特性(这包括易爆诅咒的触发)\n" +
                        "_-_ _杂项内容：_女猎手原来的生存直觉保留，同时加入投武鉴定版的生存直觉在蜕变池子中，名字变更为狩猎直觉；不保留针对矢石保养和联动升级的削弱；保留友善诅咒触发后使对应武器伤害降低至0的特性；针对狂战士的削弱不保留；其余有关牧师、战斗法师等增强保留；加入快捷备注功能；所有蜕变新增物品、单位图鉴都同步了破碎3.2图鉴中的未发现时的提示文本\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), "内容更新",
                "_-_ 加入_幸运银币_作为绅士的初始投掷武器，用以替换投石\n"+
                        "_-_ 于_fix1_加入了幽妹的特殊对话内容，当玩家变身美少女“战士”后在每次下楼时概率触发(不与幽妹原对话内容同时触发)\n"+
                        "_-_ 于_fix3_添加新的_探索指南2篇_、_史莱姆娘英雄故事1则_，同时新增_1_个成就\n"+
                        "_-_ _死境斗士_：在开启包括“恶劣环境”和“极端环境”八个挑战的情况下通关\n"+
                        "_-_ 于_fix4_加入绅士的转职详情，仅用于查看潜力无限天赋，整体和原先一样无变化，同时为万事通成就加了绅士，并在“关于蜕变地牢”界面_内置创意投稿链接_\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), "视觉更新",
                "_-_ 添加￥红色￥、￡橙色￡以及€绿色€字体，由咕(Q1323933183)指导完成\n" +
                        "_-_ 于_fix1_完善主界面的“关于蜕变地牢”界面，同时调整部分文本\n" +
                        "_-_ 于_fix2_微调史莱姆娘的立绘位置\n"+
                        "_-_ 于_fix3_加入红杯图标，在开启挑战极端环境后替换游戏内原先的金杯，同时对部分文本进行修订\n"+
                        "_-_ 于_fix4_加入红包作战图标，在游戏内会在挑战图标旁边显示\n"+
                        "_-_ 于_fix5_加入测试时间图标，在游戏内会替换原先的金杯或红杯\n"
        ));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 于_fix1_为天赋_无敌一餐_、_饭里藏金_、_吃出经验_和_生日礼物_设置100次触发上限\n" +
                        "_-_ 于_fix2_为法术天赋_荆棘之鞭_设置12格的最大距离限制\n" +
                        "_-_ 于_fix3_削弱武僧宗师，使其不再能触发绝大多数的玩家天赋特殊效果\n" +
                        "_-_ 于_fix5_加强法术天赋_荆棘之鞭_和_神力领域风暴_，充能消耗均向下调整1点，同时调整天狗天赋_奇袭投掷_，现在需要瞄准目标投掷才能生效不耗时特性但灵能弓也能够享受此加成\n" +
                        "_-_ 于_fix6_令所有角色开局鉴定蜕变秘卷，同时加强天赋_即兴投掷_，冷却从50下调到20\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年8月8日~8月18日\n"+
                        "_-_  这一版本的更新还包括了6次fix更新，此处将所有fix更新内容合并\n"+
                        "_-_  于_fix2_为所有法术天赋添加了是否消耗回合的描述，可通过圣典查看法术详情得知，同时还为部分法术增加了快捷瞄准功能(例如聚焦光线)。\n"+
                        "_-_  于_fix3_对物理增伤类天赋进行翻新，当前天赋增伤=伤害x累计天赋增伤倍率+天赋固定增伤值。(累计天赋增伤倍率为所有物理增伤天赋倍率乘积，天赋固定增伤值为固定增伤天赋之和)。\n"+
                        "_-_  于_fix6_使得无序魔典能够触发除符文复制外的其他使用卷轴的天赋(实验性改动)，同时允许空手触发需要近战触发的天赋。\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复天赋_极限施法_和_目光短浅_生效异常的问题，同时修复精英强敌3级、知识之泉导致游戏崩溃的问题\n"+
                        "_-_ 于_fix1_修复天赋_极限施法_伤害异常以及_暗气释放_和_陷阵之志_生效异常的问题，同时修复_蓄血圣杯_等治疗溢出问题以及护甲技能_狂史日记_导致的贴图异常问题\n"+
                        "_-_ 于_fix2_试修复豺狼流寇的敌对问题\n"+
                        "_-_ 修复史莱姆娘食用_邪能碎片_不能完全诅咒粘液护甲的问题\n\n"+
                        "_-_ 修复暗液钩爪释放时天赋异常触发问题，如果遇到暗液钩爪功能消失，可通过进食恢复功能\n"+
                        "_-_ 修复天赋_暗气释放_异常问题\n"+
                        "_-_ 修复_水灵史莱姆_生成粘液消耗异常的问题\n"+
                        "_-_ 修复护甲技能_三位一体_的界面排版问题\n"+
                        "_-_ 尝试修复战士？贴图位置异常问题，不确定是否会再次出现\n"+
                        "_-_ 尝试修复_矮人国王二阶段_卡死的情况，经测试后暂无问题\n"+
                        "_-_ 尝试修复一部分返程报错的情况\n",
                        "_-_ 于_fix3_修复_战士？_和挑战_精英强敌3级_的一些问题，同时将遗漏的天赋_夺命余势_和_启蒙圣餐_加回2层天赋池\n"+
                        "_-_ 于_fix4_修复了_千面手_成就和_万事通_成就中的_史莱姆_、_水灵史莱姆_和_暗液史莱姆_不会亮的问题，同时修复了幽妹可以装备并复制出粘液护甲、战士？被击杀后不会重复获得_饭桶终结者_成就(不在局内显示)、负面天赋_营养不良_的生效异常和天赋_空蝉幻舞_的触发异常问题\n"+ "_-_ 于_fix5_修复被护甲技能_鼠化术_鼠化的敌人在恢复正常后能够发动绯红之王进行时删连续殴打的问题，同时修复武技等必中效果被闪避、轻弓和重弓平A耗时异常(修复后轻弓和重弓空射都是1回合，击中敌人时则为正常)、_狙击手_追击耗时异常、_索敌附魔_失效、部分文本的错误以及表述不当等问题\n"+
                                "_-_ 于_fix6_修复部分pc端界面错误的问题\n"

        ));

    }
    public static void add_v0_2_6Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 根据投票，自本版本开始，天赋池up倍率从3倍上调至_6倍_\n"+
                        "_-_ 本版本限定负面天赋组为：_限定3_\n"+
                        "_-_ 新增共计_5_个正面天赋\n"+
                        "_-_ _1层：_敏捷反击，吓我一跳\n"+
                        "_-_ _2层：_快速搜索，忍者应酬\n" +
                        "_-_ _3层：_千发投掷\n"+
                        "于_fix3_新增共计_10_个正面天赋\n"+
                        "_-_ _1层：_虚假之力，魔弹射手，蜘蛛感应，NO集群，更多人情，静滞电流\n"+
                        "_-_ _2层：_刻笔·布莱恩特，奇迹炼金\n" +
                        "_-_ _3层：_炎烬化身，冰能一餐，五金元素\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.NINJA, 6), "忍者",
                "_-_ 忍者初始携带一件_独特的手里剑箱_，可从箱中快速取出并投射小小手里剑以打击甚至是干扰敌人。忍者能从更远的距离_探测秘密与陷阱_，并且行动_更难被敌人察觉_。击败天狗以解锁忍者\n" +
                        "_-_ _两个转职：_铁炮忍者和忍术大师\n" +
                        "_-_ _三个护甲技能：_影武者、平蜘蛛釜和一之太刀\n"+
                        "_-_ 角色由_东榆桑榆_和_幻影食人杂鱼_设计，_DM-216_绘制美术素材，_东榆桑榆_制作\n"+
                        "_-_ 追加一则关于忍者的英雄故事并新增相关成就\n"+
                        "_-_ 于_fix1_将_手里剑箱_初始伤害由4~8下调至2~4，并且重新调整了瞄准判定\n"+
                        "_-_ _铁炮忍者的铁炮_现在伤害下限恒定为0，而恐惧效果调整为只对敌人生效\n"+
                        "_-_ _忍术大师_通过_遁术-草木复苏_召唤的沉沦触手现在可以通过交互清除\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), "内容更新",
                "_-_ 于_fix4_新增_2_个物品\n"+
                        "_-_ _蜕变结晶：_它会允许使用者对任一通用天赋进行定向蜕变。可以通过使用蜕变秘卷合成获取\n"+
                        "_-_ _蜕变晶柱：_可以无限蜕变指定天赋，不会消耗自身。测试道具，只能在测试时间使用\n"
        ));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), "视觉更新",
                "_-_ 于_fix2_调整紫色ui的饱和度，使按键颜色不会过于明亮\n" +
                        "_-_ 于_fix3_为挑战界面增加图标，由_咕(Q1323933183)_协助完成\n" +
                        "_-_ 挑战图标的美术素材，由_DM-216_绘制\n"
        ));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 获得道具的最大次数从16次上调至26次，绅士仍保留额外的13次上限\n" +
                        "_-_ _长启书室_共用获得道具的最大次数\n"+
                        "_-_  fix2改动：\n"+
                        "_-_  天赋_女巫媚药_给予的魅惑效果由5回合提高至15回合\n"+
                        "_-_  天赋_燃咒+2_给予的恐惧效果改为眩晕\n"+
                        "_-_  天赋_量子骇入_给予的眩晕效果由2/3回合下调至1/2回合\n"+
                        "_-_  负面天赋_干柴烈火_给予的魅惑效果由3回合提高至10回合\n"+
                        "_-_  暗液史莱姆天赋_暗气释放_不再有cd限制，且给予的净化屏障持续时间上调至4回合\n"+
                        "_-_  调整护甲技能_狂史日记_造成的伤害\n",
                "_-_ fix3加强天赋：\n"+
                        "_-_ _脑袋尖尖_：力量提升3/6/9\n"+
                        "_-_ _陷阱专家_：回收概率提升至66%，75%，80%\n"+
                        "_-_ _引火上身_：提供的烈焰之力回合数提升至5/8\n"+
                        "_-_ _顷刻炼化_：获得炼金能量的概率从10%/15%上调至40%/60%\n"+
                        "_-_ _金币巧克力_：金币消耗减半\n"+
                        "_-_ _军用水袋_：水袋上限增加提升至15/30，生命之泉可以装满水袋，而非装至20点\n"+
                        "_-_ _武器制造_：获得的武器力量需求-2\n"+
                        "_-_ _眼力惊人_：增伤提高至35%，精准提升降低至35%\n"+
                        "_-_ _赐福圣餐_：祝福延长至25/35回合\n"+
                        "_-_ _宝贵经验_：神意启发合剂也能触发效果，并且+2时饮用经验及其合剂给予祝福回合上调至50\n"+
                        "_-_ _诅咒护体_：额外等级对武器也生效\n"+
                        "_-_ _长期素食_：肉食给予的饱食度降低从50%下调到30%\n"+
                        "_-_ _打草惊蛇_：巨蛇从狂乱改变为腐化\n"+
                        "_-_ _卧榻之侧_：激素涌动回合提升至3/5\n",
                "_-_ fix3加强天赋：\n"+
                        "_-_ _爆破狂魔_：升级该天赋可额外获得一个炸弹\n"+
                        "_-_ _饭里藏金_：升级该天赋可获得量提升至25/35\n"+
                        "_-_ _麻醉酊剂_：护盾量上调至5/8\n"+
                        "_-_ _反戈一击_：固定对敌人造成0.5倍等级的反伤\n"+
                        "_-_ _活动肌肉_：升级获得的力量翻倍，持续时间延长\n"+
                        "_-_ _盛食厉兵_：增伤和伤害减免上调至15%/30%/45%和10%/20%/30%\n",
                "_-_ fix3调整和削弱天赋：\n"+
                        "_-_ _暗液缠身_：8回合法杖充能+5回合神器充能\n"+
                        "_-_ _工程技师_：下放到2层\n"+
                        "_-_ _所向披靡_：上调到2层\n"+
                        "_-_ _火球术_，_金币护盾_，_势能转换_，_掠阵之影_：下放到1层\n"+
                        "_-_ _击落卫星_：调整类型为其他类\n"+
                        "_-_ _燃咒_：+1给予残废，+2给予失明\n"+
                        "_-_ _极限施法_：极限施法的降低充能速度从原来的10%/20%/30%上调至13%/26%/39%\n"+
                        "_-_ _借痛排毒_：获得全面净化效果概率从25%下调至15%\n"+
                        "_-_ _黄金回旋_：+2增伤取消，+3对恶魔亡灵增伤降低至30%\n",
                "_-_ fix4加强和削弱天赋：\n"+
                        "_-_ 加强天赋：\n"+
                        "_-_ _十全大补_：效果翻倍\n"+
                        "_-_ _震撼爆炸，爆破狂魔_：升级立即获取1个炸弹，且不占用上限\n"+
                        "_-_ _人身攻击_：升级此天赋额外获得一张盛怒卷轴\n"+
                        "_-_ _水鬼索命_：+1时的怨灵给予5回合狂乱效果，+2给予腐化效果\n"+
                        "_-_ _健康食物，万灵药水_：在原效果基础上增加1回合持续时长n"+
                        "_-_ 调整和削弱天赋：\n"+
                        "_-_ _烟雾掩护_：cd从20回合上调至25回合，可以自由释放，烟雾量上调至100/150\n"+
                        "_-_ _吓我一跳_：闪避给予的护盾量上升至+2/+3，叠加上限分别为4/6点\n"+
                        "_-_ _水灵回复_：如果玩家处于极度饥饿状态，该天赋回复效率减半\n"+
                        "_-_ _NO集群_：移入其他类\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年8月23日-9月29日\n"+
                        "_-_  于_fix4_为天赋图鉴的天赋背景根据类型设置了不同的颜色区分\n"+
                        "_-_  为不同Boss掉落的_升华秘卷_在文本上添加区分\n\n"+
                        "_-_  更新绝大多数的_天赋描述_\n"+
                        "_-_  _战士_初始力量上调至11点\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复天赋_自然亲和_和_盈能生命_生效异常的问题\n"+
                        "_-_ 于_fix1_修复天赋_目光短浅_和觉察之泉的视野问题\n"+
                        "_-_ 修复天赋_云隐一餐_对号角的减回合不生效的问题\n"+
                        "_-_ 修复护甲技能平蜘蛛釜不触发其他天赋的问题\n"+
                        "_-_ 修复腐化触手异常掉落问题\n"+
                        "_-_ 于_fix2_试修复忍术大师释放遁术导致的游戏崩溃\n"+
                        "_-_ 修复击杀英雄boss无法获得徽章问题\n"+
                        "_-_ 于_fix3_修复盗贼的检测范围缩小的问题\n"+
                        "_-_ 修复天赋_怨灵爪牙_生效异常问题\n"+
                        "_-_ 修复天赋_马猴烧酒_变身时长异常的问题\n"+
                        "_-_ 修复天赋_原生魔物_生效异常的问题\n"+
                        "_-_ 修复魔杖灌注和文章贴附异常问题\n"+
                        "_-_ 于_fix4_实装多数测试工具\n"+
                        "_-_ 修复天赋_强化袖章_生效异常问题\n"+
                        "_-_ 修复天赋_虚假之力_生效异常问题\n"

        ));

    }

    public static void add_v0_2_7Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                        "_-_ 本版本限定负面天赋组为：_限定4_\n"+
                        "_-_ 新增共计_5_个正面天赋\n"+
                        "_-_ _1层：_超算代码，定期维护\n"+
                        "_-_ _2层：_高效指令，火箭拳头\n" +
                        "_-_ _3层：_重拳出击\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.DM400, 6), "DM-400",
                "_-_ DM-400可以借助_指令集合工具_生产无人机协助作战，同时它的物理攻击还会_强化_战斗型无人机对敌人造成的效果。它还拥有_非生物_属性。击败DM300以解锁DM-400\n" +
                        "_-_ _两个转职：_AT400和AU400\n" +
                        "_-_ _三个护甲技能：_例行维护、废气排出和蜂巢集群\n"+
                        "_-_ 角色由_DM216_设计，_DM-216_绘制美术素材，_东榆桑榆_制作\n"+
                        "_-_ 追加一则关于DM-400的英雄故事并新增相关成就\n"
        ));



        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡性调整",
                "_-_ 加强天赋_优质吸收_：露珠回复从+1/+2提升至+2/+3\n" +
                        "_-_ 加强天赋_极限施法_：每级降低的法杖充能速度从13%下调至10%\n"+
                        "_-_ 加强天赋_破损核心_：+2时伤害从1-2点上调至2点\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年10月13日\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复天赋_烟雾掩护_生效异常的问题\n"+
                        "_-_ 修复物品_蜕变结晶_导致天赋丢失的问题\n"

        ));

    }
    public static void add_v0_2_7fixChanges( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.7fix", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( "v0.2.7fix1", false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.DM400, 6), "DM-400改动",
                "_-_ 僚机无人机增加1~2点初始护甲\n"+
                        "_-_ 激光无人机增加2倍命中，标记点燃概率上调至50%\n"+
                        "_-_ 迷乱无人机的血量上调至_3+玩家33%_\n"+
                        "_-_ 袭扰和震慑无人机的伤害统一上调至_神器效果等级~5+神器效果等级_\n"+
                        "_-_ 混沌无人机的变化所需时间减少10回合\n"+
                        "_-_ 所有辅助型无人机每架可为玩家提供0.3潜行点（护航无人机为0.8）\n"+
                        "_-_ 增加游荡和回收功能，回收时若无人机血量≥80%则返还1点工具充能\n"+
                        "_-_ 增加卷轴记录功能，使AT-400/AU-400可以自由制造已录入卷轴对应的无人机\n"+
                        "_-_ 自爆无人机自动掉血距离缩短\n"
        ));


        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_  更新日期：2025年10月15日\n"+
                        "_-_  调整无人机群体指挥\n"+
                        "_-_  调整辅助无人机的行动逻辑\n"+
                        "_-_  允许蓝底法杖拆成树脂\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复天赋_快捷工具_生效异常问题\n"+
                        "_-_ 修复天赋_法杖回收_生效异常问题\n"+
                        "_-_ 修复护甲刻印_涌流_、_迅捷_、_臃肿_生效异常问题\n"+
                        "_-_ 修复神器_无序魔典_阅读异常问题\n"

        ));
    }

}
