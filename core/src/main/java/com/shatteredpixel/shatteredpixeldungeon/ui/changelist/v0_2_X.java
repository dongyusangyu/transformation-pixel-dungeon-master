package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.SpecialPackage;
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
        add_v0_2_1Changes(changeInfos);
    }
    public static void add_v0_2_1Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo("0.2.5fix3",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 添加新的冒险指南2篇、史莱姆故事一则。\n"+
                        "_-_ 修复战士?和精英强敌3的一些问题。\n"+
                        "_-_ 挑战图标和文本修订。\n"
        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.SLIMEGIRL, 1), "史莱姆娘：平衡性调整",
                "_-_增强：狂史日记现在可以让被吞噬的怪物正常掉落掉落物\n" +
                        "_-_削弱：粘液护甲的自动升级所需史莱姆娘等级：3/6/9/12/15→3/9/15/21/27。\n"+
                        "_-_削弱：暗液缠身+3恢复的饱食度：90→45。\n"+
                        "_-_削弱：狂史日记所需耗能：75→90。\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋改动",
                        "_-_ 新增5个正面天赋:液体感知，水波荡漾，自然亲和，优质吸收，盈能生命。\n"+
                                "_-_ 对物理增伤类天赋进行翻新，当前天赋增伤=伤害*累计天赋增伤倍率+天赋固定增伤值。\n"+
                                "_-_ 累计天赋增伤倍率为所有物理增伤天赋倍率乘积，天赋固定增伤值为固定增伤天赋乘积。\n"+
                                "_-_ 累计天赋增伤倍率为所有物理增伤天赋倍率乘积，天赋固定增伤值为固定增伤天赋之和。\n"+
                                "_-_ 武僧宗师不再触发绝大多数的玩家天赋增伤效果。\n"


        ));


        changes = new ChangeInfo("0.2.5fix2",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 粘液护甲无法使用诅咒棱晶升级,使用邪能碎片不能完全诅咒的问题,移除粘液护甲转移功能\n"+
                        "_-_ 修复暗液钩爪释放时天赋异常触发问题，如果遇到暗液钩爪功能消失，可通过进食恢复功能\n"+
                        "_-_ 修复天赋_暗气释放_异常问题\n"+
                        "_-_ 修复水灵史莱姆生成粘液消耗异常的问题\n"+
                        "_-_ 暗液钩爪与法术荆棘之鞭设置12格的最大距离限制\n"+
                        "_-_ 豺狼流寇不再因为负面效果敌对，而是因为生命值低于生命上限敌对\n"+
                        "_-_ 削弱天赋_水灵强愈_。\n"+
                        "_-_ 修复护甲技能三位一体的界面排版问题\n"+
                        "_-_ 微调史莱姆娘立绘位置\n"+
                        "_-_ 试修复国王和战士？战斗中问题\n"
        ));


        changes = new ChangeInfo("0.2.5fix1",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 修复天赋_极限施法_伤害异常、_暗气释放_、_陷阵之志_生效异常的问题\n"+
                        "_-_ 调整部分文本。\n"+
                        "_-_ 完善关于界面\n"+
                        "_-_ 无敌一餐，金币一餐，吃出经验、生日礼物设置100次触发上限\n"+
                        "_-_ 修复圣杯等治疗溢出问题\n"+
                        "_-_ 调整战士？跳跃cd\n"+
                        "_-_ 修复狂史日记贴图异常问题\n"
        ));
        changes.addButton(new ChangeButton(new ItemSprite(new SlimeBall()), "粘液球",
                "新增新投掷武器：粘液球，由史莱姆娘初始持有\n" +
                        "_-_ 这是从史莱姆娘身上不断脱落的粘液组成的球体，现在摸起来已经有些发硬了。也许丢出去还能对敌人的行动产生一些影响。\n" +
                        "_-_ 本饰品由_DM216_制作\n"

        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.SLIMEGIRL, 1), "史莱姆娘：平衡性调整",
                "_-_暗夜史莱姆近战攻击附着淤泥概率从30%上调至50%，暗夜钩爪cd从100下调至50，加强天赋_暗液缠身_、_强效淤泥_。\n" +
                        "_-_史莱姆初始持有2个粘液球。\n"+
                        "_-_粘液护甲加强，从原来的每6级一次提升加强到每3级一次提升，最多+5。\n"+
                        "_-_讨伐者的长矛初始伤害从1-12上调至1-14。\n"
        ));
        changes = new ChangeInfo("0.2.5",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 修复天赋_极限施法_和_目光短浅_生效异常的问题\n"+
                        "_-_ 修复精英强敌3、觉察之泉导致游戏崩溃的问题。\n"+
                        "_-_ 本版本限定负面天赋组为：_限定2_\n"+
                        "_-_ 绅士初始投武从投石调整为幸运银币。\n"

        ));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.SLIMEGIRL, 1), "新增英雄：史莱姆娘",
                "_-_史莱姆娘初始穿戴无法脱下的_粘液护甲_，会随史莱姆娘等级提升而提高品阶和获得额外的等级。史莱姆娘可以花费一回合让种子在体内发芽，并立刻触发种子对应的植被效果。\n" +
                        "史莱姆娘初始还携带有一把_讨伐者的长矛_、水袋以及绒布袋。\n" +
                        "史莱姆娘作为魔物，没有任何开局鉴定的物品。\n"+
                        "本英雄由_叶·凌卡缇娜·霜麟_设计，_可则_和_DM216_绘制美术素材，_Secher-Nbiw_制作"
                       ));
        changes.addButton(new ChangeButton(new Image(new WarriorBossSprite()), "新增5层boss：战士?",
                "新增5层boss：战士?\n" +
                        "_-_ 战士?和粘咕依照种子有1/2概率生成\n" +
                        "_-_ 击败战士?可掉落纹章残蜡、口粮和战士的升华秘卷\n" +
                        "_-_ 战士?拥有狂战和角斗的部分特性，以及能够使用护甲技能：跳跃\n"+
                        "_-_ 战士?会掉落新类型的升华秘卷，并有全新的boss天赋可供选择\n"

        ));
        changes.addButton(new ChangeButton(Icons.JOURNAL.get(), "新增高亮字体类型",
                "新增￥红色￥、￡橙色￡、€绿色€高亮字体\n" +
                        "_-_ 本字体效果由群友_咕_(QQ1323933183)指导完成，感谢群友_咕_\n"

        ));

        changes = new ChangeInfo("0.2.4fix1",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 力量药水不会因坠落深渊而碎裂，宝箱怪变羊时会掉落物品\n"+
                        "_-_ 修复天赋_溃决之手_触发异常问题。\n"+
                        "_-_ 部分天赋文本修改。\n"

        ));


        changes = new ChangeInfo("0.2.4",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 修复天赋_我全都要_、_营养不良_失效的问题，修复天赋_超越极限_生效异常的问题。\n"+
                        "_-_ 修复徽章_光荣凯旋_获取异常问题。\n"+
                        "_-_ 同步破碎对_臃肿诅咒_和_狱火刻印_的改动。\n"

        ));
        changes.addButton(new ChangeButton(Icons.CHALLENGE_COLOR.get(), "新增挑战",
                "_-_ 挑战_精英强敌_、_绝命头目_添加新的挑战效果\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋改动",
                "_-_ 可蜕变的正面天赋划分为物攻，法伤，特效，资源，法术，辅助，其他共7类，在使用蜕变秘卷蜕变天赋时可选择类型，该类型天赋将以3倍概率出现在选项中。具体天赋分类请到对应天赋详情页查看。\n"+
                        "_-_ 负面天赋划分为12个通用负面天赋，和4组每组8个版本限定负面天赋，每次更新时改变限定天赋组。具体负面天赋划分到天赋详情页查看。\n"+
                        "_-_ 本版本限定负面天赋组为：_限定1_\n"+
                        "_-_ 新增5个正面天赋:溃绝之手，预知卷轴爱好者，火球术，灵光闪现，女巫媚药。\n"+
                        "_-_ 新增5个法术天赋:奸奇咒法，神行术，聚焦光线，转愈圣术，荆棘之鞭。\n"+
                        "_-_ 新增1个负面天赋：手滑点错。\n"

        ));
        changes.addButton(new ChangeButton(Icons.DISPLAY_LAND.get(), "界面改动",
                "_-_ 标题界面、Boss斩杀等美术风格跟进破碎3.0的美术风格。本次更新的美术素材由_DM216_绘制\n"

        ));


        changes = new ChangeInfo("0.2.3",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ _牧羊原旨_削弱，+1释放的羊群范围缩小，+2能耗从2点上调至3点。\n"+
                        "_-_ _赎罪券_加强，金币消耗从500/300下调至200/150。\n"+
                        "_-_ _手杖_加强，初始伤害从1-6上调至1-7。\n"+
                        "_-_ 同步破碎3.1的楼层改动，木桶及后续障碍物可用镐或炸弹清除,且炸弹只能清除3*3范围障碍。\n"+
                        "_-_ 同步破碎3.1的牧师增强与削弱。\n"+
                        "_-_ 同步破碎3.1的新徽章。\n"+
                        "_-_ 同步破碎3.1的探索分计算方式。\n"+
                        "_-_ 同步破碎3.1的新怪物和新饰品。\n"+
                        "_-_ 同步破碎3.1战士天赋_丰盛一餐_、_受衅怒火_、_液蕴意志_的改动，关于纹章部分只修改贴图，不改变其机制及相关天赋。\n"+
                        "_-_ 移除_升华秘卷_的警告界面\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增7个正面天赋:灰烬之弓，速行藓神教，诅咒护体，势能转换，涅槃，含沙射影，自动拾取。\n"+
                        "_-_ 新增4个负面天赋：蚀骨之火，跳脸硅胶，草木皆兵，灾厄诅咒。\n"

        ));


        changes.addButton(new ChangeButton(new Image(new GreatShoperSprite()), "返程BOSS战2",
                "新增20层返程boss战\n" +
                        "_-_ 取得护符后对话20层店主即可触发\n" +
                        "_-_ 击败boss可获得1万得分和徽章\n" +
                        "_-_ 背景故事中新增返程见闻一则\n" +
                        "_-_ BOSS战难度较高，需玩家谨慎对待\n" +
                        "_-_ BOSS战怪物画师：_可则_、_幻影の人食い雑魚_、_DM216_、_放縦鵺_\n" +
                        "_-_ 为BOSS战新增一首BGM《On-Boss》\n"

        ));
        changes.addButton(new ChangeButton(Icons.CHALLENGE_COLOR.get(), "新增挑战",
                "_-_ 增加新挑战_恶劣环境_和_极端环境_，并给挑战_信念护体_、_恐药异症_、_集群智能_、_没入黑暗_、_禁忌咒文_、_荒芜之地_添加新的挑战效果\n"+
                        "_-_ 如果玩家勾选_恶劣环境_且没勾选任意基础挑战进入游戏，会随机增加一项基础挑战\n"+
                        "_-_ 如果玩家勾选_极端环境_且没勾选_恶劣环境_进入游戏，会自动增加_恶劣环境_\n"


        ));

        changes = new ChangeInfo("0.2.2fix10",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 增加资源类天赋升级的道具数量限制，一般职业的上限为16次，绅士为29次。\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增10个正面天赋:量子骇入，空蝉幻舞，金币巧克力，白幽馈赠，人身攻击，击落卫星，圣光信仰，蜂蜜鱼缸，顷刻炼化，常启书室。\n"+
                        "_-_ 新增10个法术天赋和对应的法术:升天咒，神明庇佑，牧羊原旨，圣法皈依，净化邪恶，神力领域风暴，创世纪，赎罪券，银光锐语，复活术。\n"+
                        "_-_ 新增3个负面天赋：深层恐惧，门都没有，上升气流。\n"

        ));

        changes = new ChangeInfo("0.2.2fix9",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 同步破碎3.0.1对_神圣武器、护甲_的改动\n"+
                        "_-_ 同步破碎3.0.1对_苔藓丛簇_的改动\n"+
                        "_-_ _圣典拓本_充能上限上调至8点，转化能量从3*剩余充能数降低至2*剩余充能数，最大转化为15点能量\n"+
                        "_-_ 实装附魔_横扫_的元素打击效果，本附魔由_Secher-Nbiw_制作\n"+
                        "_-_ 实装饥饿条的ui，由_DM216_小姐绘制美术素材\n"

        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_ 修复_每日挑战_地牢生成问题\n"+
                        "_-_ 修复_老杖匠_和_巨魔_的对话异常问题\n"+
                        "_-_ 修复_陷阱晶柱_存储陷阱问题\n"+
                        "_-_ 修复__无味果投掷异常触发天赋长期素食问题\n"+
                        "_-_ 修复护甲技能_超凡升天_的攻击距离不生效问题\n"+
                        "_-_ 修复天赋_回味无穷_吞_启蒙圣餐_的充能问题\n"+
                        "_-_ 将_魔能触媒_设置为不可堆叠，修复持有多个触媒炼金时，饰品窗口异常出现问题\n"+
                        "_-_ 修复法术_符文复制_对符石的异常问题\n"+
                        "_-_ 修复向_生命之泉_投掷装备的异常闪退问题\n"));

        changes = new ChangeInfo("0.2.2fix8",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 同步3.0对财富戒指的改动\n"+
                        "_-_ 修复绅士三层天赋异常问题\n"+
                        "_-_ 修复凝血魔瓶和天赋寄生文本问题\n"+
                        "_-_ 圣典拓本转化能量从8提升至3*剩余充能数\n"
        ));


        changes = new ChangeInfo("0.2.2fix7",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        ItemSprite sprite = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD,new Sweeping().glowing());
        sprite.lightness(0f);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 同步3.0.0的连击和十字弩射击的武技效果\n"+
                        "_-_ 同步3.0的苔藓、牙齿、十三叶草、香炉、遗忘碎片、陷阱元件效果\n"+
                        "_-_ 同步3.0的天赋联动升级效果\n"+
                        "_-_ 同步3.0的开始界面Ui改动\n"+
                        "_-_ 同步3.0的斗篷、圣典不能嬗变的改动\n"+
                        "_-_ 同步3.0的补充疯狂强盗的描述\n"+
                        "_-_ 同步3.0对饥饿扣除生命值和生命值自然回复的改动\n"+
                        "_-_ 淬毒匕首、涿郡屠夫、正义惩戒等天赋描述修改\n"+
                        "_-_ 感谢_委员会全员_和_墨意-天志_为本次更新进行大量测试\n"+
                        "_-_ 补充部分成就徽章图标和重绘绅士动画效果，以及大量物品图标重绘，由_DM216_小姐重绘\n"
                ));
        changes.addButton(new ChangeButton(sprite, "新增附魔：横扫",
                "_-_ 横扫附魔会打击大量敌人\n"+
                        "_-_ 决斗家元素打击效果未定\n"+
                        "_-_ 本附魔由_Secher-Nbiw_制作\n"));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.CLERIC, 1), "牧师实装",
                "_-_ 实装牧师，并调整牧师初始天赋\n"+
                           "_-_ 牧师的天赋除启蒙圣餐外，蜕变效果均不生效，启蒙圣餐效果也进行改动\n"+
                           "_-_ 新增圣典拓本，使其他英雄也能像牧师一样释放法术，圣典拓本可以通过炼金获取(需要刻笔但不消耗刻笔)\n"+
                           "_-_ 绅士公文包的可选物品新增圣典\n"+
                           "_-_ 蜕变天赋时额外增加一个牧师天赋选项，便于玩家快速获取法术天赋，天赋分池后，将会进行调整\n"+
                           "_-_ 天赋符文复制改动：无法复制蜕变秘卷\n"));
        changes.addButton( new ChangeButton(Icons.DISPLAY.get(), "新ui实装",
                "_-_ 蜕变地牢的紫色ui实装\n"+
                           "_-_ 本次ui的美术素材由_DM216_小姐倾情绘制，希望大家喜欢\n"));


        changes = new ChangeInfo("0.2.2fix6",false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增正面天赋3个，负面天赋3个。"));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 全面调整各天赋描述中强调的内容。\n"+
                        "_-_ 暂时关闭进入0层的通道。"));
        changes.addButton(new ChangeButton(new ItemSprite( ItemSpriteSheet.MAGES_STAFF), "魔杖初始伤害调整",
                "_-_ 魔杖初始伤害从_1-6_提高至_1-7_。"));
        changes.addButton(new ChangeButton(new ItemSprite( ItemSpriteSheet.BROKEN_PACKAGE), "新增绅士遗物",
                "_-_ 当绅士死亡后，玩家发现其遗骸，会生成破损皮包，使用后获得少量金币。"));



        changes = new ChangeInfo("2月16日生日纪念版", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋强度调整",
                "_-_ 无尽恶意+0时生效概率上调至40%，生命扣除从当前生命值的百分比调整为生命上限的百分比"));
        changes.addButton(new ChangeButton(new ItemSprite( ItemSpriteSheet.SHATTERED_CAKE), "生日快乐",
                "_-_ 祝_DM216_小姐2月16日生日快乐，永远年轻漂亮，每天生活开心!\n"+
                        "_-_ DM216小姐为蜕变地牢绘制了很多精美的图标，还提出不少有意思的天赋创意并实装到蜕变地牢内，为蜕变地牢发展做出卓越贡献。\n相应的，如果大家也对蜕变地牢做出卓越贡献，可以反馈给制作组，制作组也会制作纪念版本分享给大家，并表彰其贡献。"));
        changes = new ChangeInfo("0.2.2fix5", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton( Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 修复第一批天赋描述丢失的问题\n" +
                        "_-_ 添加种子查找功能，除常规功能外，可以查找负面天赋\n"+
                        "_-_ 限制存档数量，当玩家现有存档数超过5个时，将无法进行新游戏\n"+
                        "_-_ 自该版本后的通关记录将保留存档内所有道具，而不是只保留装备和快捷栏道具"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋bug修复",
                "_-_ 泥头车现在对全部拥有巨型词条的怪物生效\n"+
                        "_-_ 细嚼慢咽的饱腹效果将维持在450/150回合"));
        changes.addButton(new ChangeButton(new Image(new StatueSprite()), "第0层施工中",
                "_-_ 现在第0层调用通用楼层，并放置一个测试石像供玩家测试伤害\n"+
                        "_-_ 玩家将可以使用过去通关记录的角色进入第0层，未来将会围绕这一部分制作实验性玩法\n"+
                        "_-_ 进入第0层方法：在有空余存档位的前提下，进入排行榜界面，选择一个通关记录，进入挑战信息界面，点击新的蓝色按钮即可进入\n"+
                        "_-_ _注意：在本版本之前的通关记录只保留装备和快捷栏物品，而在本版本及之后的版本才能保留玩家存档的全部道具。由于未知原因，初次进入第0层后玩家的武器、护甲、神器不会自动充能，需要扔出或者退出重进存档才能正常充能_"));

        changes = new ChangeInfo("0.2.2fix4", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton( Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 修复绅士的介绍界面和开始界面的显示问题\n" +
                        "_-_ 添加第二批天赋描述,修复天赋描述问题"
                       ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋强度改动",
                "_-_ 茉莉花茶和军用水袋调整为一层天赋\n"+
                        "_-_ 马猴烧酒的变身效果在阅读魔典时也会触发\n"+
                        "_-_ 涿郡屠夫增加掉落的等级限制\n"+
                        "_-_ 摸鱼时间+2会在水池房额外增加1条鱼的生成\n"+
                        "_-_ 享乐主义的伤害增幅从3/6下调至2/4\n"+
                        "_-_ 天地之力每级增伤从12%上调至15%，地缚根护甲可以触发，但守望的地缚根的树肤不会触发"));


        changes = new ChangeInfo("0.2.2fix3", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton( Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 修复大恶魔buff图标显示问题\n" +
                        "_-_ 批量升级投掷武器时将只起到一张升级的效果\n"+
                        "_-_ 返程动画和标题界面重置，由_DM216_绘制\n"+
                        "_-_ 添加第一批天赋描述,修复天赋描述问题\n"+"" +
                        "_-_ 给奸奇信徒的黑魔法加入移除全面净化的效果"));
        changes.addButton(new ChangeButton(new Image(new PhantomLandPiranhaSprite()), "新增幻影陆地食人鱼",
                "_-_ 生成陆地食人鱼时概率生成幻影陆地食人鱼\n"+
                        "_-_ 幻影陆地食人鱼属性与幻影食人鱼一致，可掉落幻影鱼肉\n"+
                        "_-_ 幻影陆地食人鱼形象由_DM216_绘制"));
        changes.addButton(new ChangeButton(new ItemSprite(new SpecialPackage()), "公文包变更",
                "_-_ 绅士初始的特殊宝箱更改为公文包，由_DM216_绘制\n"+
                        "_-_ 公文包与原特殊宝箱功能一致"));

        ItemSprite sprite1 = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD,new Heavy().glowing());
        sprite.lightness(0f);
        changes = new ChangeInfo("0.2.2fix2", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton( Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 修复畏光等天赋生效异常问题\n" +
                        "_-_ 修复手杖未加入目录的问题\n"+
                        "_-_ 修复绅士介绍界面图标异常问题\n"+
                        "_-_ 削弱正义惩戒的击杀祝福效果\n"+
                        "_-_ 绅士小人形象重置，本次更新的形象由_DM216_绘制"));
        changes.addButton(new ChangeButton(sprite1, "新增诅咒：沉重",
                "_-_ 沉重诅咒会使武器力量需求增加\n"+
                        "_-_ 当决斗家武器有沉重诅咒时，元素打击效果为对范围内的怪物概率施加虚弱效果"));

        changes = new ChangeInfo("0.2.2fix1", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton( Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 修复反戈一击的文本描述错误\n" +
                        "_-_ 修复天赋弱化问题\n"+
                        "_-_ 修复机械技师等生效问题\n"+
                        "_-_ 实装当玩家处于马猴烧酒状态下，悲伤幽灵的特殊对话3则"));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋实装",
                "_-_ 实装负面天赋陆游\n" +
                        "_-_新增怪物陆地食人鱼\n"));
        changes.addButton(new ChangeButton(new ItemSprite(new WalkStick()), "绅士初始武器修改",
                "_-_ 手杖是一把可以攻击产生护盾，提高防御力的武器\n" +
                        "_-_初始属性1-6，攻击概率叠加护盾\n"+
                        "_-_ 本武器图标由_DM216_绘制"));
        changes.addButton(new ChangeButton(new ItemSprite(new SpecialPackage()), "特别宝箱调整",
                "_-_ 为减少玩家游玩绅士时SL频率，故设置特别宝箱的物品可以自选" ));

        changes = new ChangeInfo("0.2.2", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new ItemSprite(new GoldIngot()), "黄金元宝",
                "黄金元宝完全实装\n" +
                        "_-_ 通过减少金钱掉落量，获得额外伤害倍率\n" +
                        "_-_ 本饰品由_Secher-Nbiw_制作\n"+
                        "_-_ 本饰品由_DM216_重新绘制"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增正面天赋_10_个\n" +
                        "_-_新增负面天赋_15_个，具体请到天赋图鉴查阅\n"+
                        "_-_本次更新的天赋图标由_DM216_绘制"));
        changes.addButton(new ChangeButton(new Image(new DM300Sprite()), "DM300新形象",
                "_-_ 实装DM300的新形象\n" +
                        "_-_本次更新的形象由_Lefted_绘制"));



        changes = new ChangeInfo("v0.2.1", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 调整部分天赋强度：\n" +
                        "加强万灵药水、借痛排毒、求生意志、工程技师、反戈一击、盛食厉兵、算盘\n"+
                        "_-_ 修复各类bug\n"+
                        "_-_ 重修挑战界面"));

        changes.addButton(new ChangeButton(new Image(new GreatDemonSprite()), "返程BOSS战1",
                "新增25层返程boss战\n" +
                        "_-_ 取得护符后对话25层小恶魔即可触发\n" +
                        "_-_ 击败boss可获得1万得分和徽章\n" +
                        "_-_ 背景故事中新增返程见闻一则\n" +
                        "_-_ BOSS战难度较高，需玩家谨慎对待\n" +
                        "_-_ BOSS战怪物画师：_可则_、_幻影の人食い雑魚_\n" +
                        "_-_ 为BOSS战新增两首BGM《Combat Area》和《Chu-Boss》\n"

        ));
        changes.addButton(new ChangeButton(new ItemSprite(new GoldIngot()), "黄金元宝",
                "新增新饰品：黄金元宝\n" +
                        "_-_ 通过消耗金钱获得额外的伤害倍率\n" +
                        "_-_ 本饰品由_Secher-Nbiw_制作\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增正面天赋：强化魔典，由_Secher-Nbiw_制作\n" +
                        "_-_新增负面天赋18个，具体请到天赋图鉴查阅\n"+
                        "_-_新增挑战：负面天赋\n"+
                        "_-_本次更新的天赋图标由_DM216_绘制"));






    }
    public static void add_v0_2_2Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo("v0.2.1", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "_-_ 调整部分天赋强度：\n" +
                        "加强万灵药水、借痛排毒、求生意志、工程技师、反戈一击、盛食厉兵、算盘、豺狼巫术\n"+
                        "_-_ 修复各类bug\n"+
                        "_-_ 重修挑战界面"));

        changes.addButton(new ChangeButton(new Image(new GreatDemonSprite()), "返程BOSS战1",
                "新增25层返程boss战\n" +
                        "_-_ 取得护符后对话25层小恶魔即可触发\n" +
                        "_-_ 击败boss可获得1万得分和徽章\n" +
                        "_-_ 背景故事中新增返程见闻一则\n" +
                        "_-_ BOSS战难度较高，需玩家谨慎对待\n" +
                        "_-_ BOSS战怪物画师：_可则_、_幻影の人食い雑魚_\n" +
                        "_-_ 为BOSS战新增两首BGM《Combat Area》和《Chu-Boss》\n"

        ));
        changes.addButton(new ChangeButton(new ItemSprite(new GoldIngot()), "黄金元宝",
                "新增新饰品：黄金元宝\n" +
                        "_-_ 通过消耗金钱获得额外的伤害倍率\n" +
                        "_-_ 本饰品由_Secher-Nbiw_制作\n"

        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "新增天赋",
                "_-_ 新增正面天赋：强化魔典，由_Secher-Nbiw_制作\n" +
                        "_-_新增负面天赋18个，具体请到天赋图鉴查阅\n"+
                        "_-_新增挑战：负面天赋\n"+
                        "_-_本次更新的天赋图标由_DM216_绘制"));

    }
}
