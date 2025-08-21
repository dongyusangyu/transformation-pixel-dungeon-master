package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class v0_1_X {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
        add_v0_1_9Changes(changeInfos);
        add_v0_1_8Changes(changeInfos);
        add_v0_1_7Changes(changeInfos);
        add_v0_1_6Changes(changeInfos);
        add_v0_1_5Changes(changeInfos);
        add_v0_1_4Changes(changeInfos);
        add_v0_1_3Changes(changeInfos);
        add_v0_1_2Changes(changeInfos);
        add_v0_1_1Changes(changeInfos);
        add_v0_1_0Changes(changeInfos);

    }
    public static void add_v0_1_0Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1：蜕变地牢诞生", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TPX), "蜕变地牢诞生",
                "_-_ 诞生日期：2025年1月3日\n"+
                        "_-_ 蜕变地牢横空出世！以天赋蜕变为主要玩法，由_东隅桑榆_制作\n"+
                        "_-_本地牢还为所有固定流程的Boss增加了升华秘卷的掉落，同时还调整了各个角色的天赋及天赋数量，并且除Boss层外每层还会固定生成一张蜕变秘卷供玩家进行天赋蜕变\n" +
                        "希望各位玩得开心！\n"
        ));
    }
    public static void add_v0_1_1Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_11_个正面天赋和_1_个联动的Boss天赋\n"+
                        "_-_ _1层：_强力进攻，恐惧化身\n"+
                        "_-_ _2层：_求生意志，强力投掷，燃咒，正义惩戒，掠阵之影\n" +
                        "_-_ _3层：_陷阱专家，反戈一击，继承遗志，排山倒海(原为_萝卜地牢_天赋)\n"+
                        "_-_ _Boss天赋：_动能转换(击败矮人国王可选取，原为_萝卜地牢_天赋)\n"
        ));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月14日\n"+
                "_-_ 涉及近战攻击的本地牢天赋改为需装备近战武器触发\n"+
                        "_-_ _升华秘卷_取消释放后不会被消耗\n"
        ));
    }
    public static void add_v0_1_2Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_18_个正面天赋\n"+
                        "_-_ _1层：_扰乱攻击，第三只手，水性攻击，爆破狂魔\n"+
                        "_-_ _2层：_打草惊蛇，宝贵经验，诡异投掷，豺狼巫术，更多机会，金币护盾，眼力惊人，重甲运用，轻甲运用，赐福圣餐\n" +
                        "_-_ _3层：_武技训练，魔力回收，工程技师，幻影射手\n"
        ));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), "平衡性调整",
                "_-_ 粘咕天赋_淤泥附着_增加10点延迟伤害效果\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复部分_v0.1.1_的天赋触发条件异常及伤害倍率异常问题\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月16日\n"
        ));
    }

    public static void add_v0_1_3Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增_天赋图鉴_，可在日志中查看\n\n"+
                        "_-_ 新增共计_18_个正面天赋\n"+
                        "_-_ _1层：_加厚护甲，算无遗策，插标卖首，护盾一餐，力量锻炼，治疗一餐，遍体鳞伤，十全大补，野蛮体魄\n"+
                        "_-_ _2层：_耐摔王，军用水袋，茉莉花茶，神之左手，神之右手，强化锁链，强化圣杯\n" +
                        "_-_ _3层：_暴怒攻击，金币法阵\n"
        ));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), "平衡性调整",
                "_-_ 天狗天赋_烟雾掩护_冷却时间由50回合下调至20回合\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复显示、文本错误、闪退问题等\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月18日\n"
        ));
    }
    public static void add_v0_1_4Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                        "_-_ 新增共计_20_个正面天赋\n"+
                        "_-_ _1层：_破冰行动，劲冠三军，更多天赋，新手福利，摸鱼时间，淬毒匕首，震撼爆炸，画饼充饥，班门弄斧\n"+
                        "_-_ _2层：_燃烧之血，卧榻之侧，巨物杀手，我全都要，长期素食，律令震慑，负重前行\n" +
                        "_-_ _3层：_厚积薄发，双持饰品，无敌一餐，盛食厉兵\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "挑战更新",
                "_-_ 新增_1_个挑战\n"+
                        "_-_ _天赋弱化：_天赋+1不生效，+2、+3只生效+1，+4只生效+2\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复_王之加护_、_军用水袋_、_动能转换_等部分天赋不生效的问题\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月20日\n"
        ));
    }
    public static void add_v0_1_5Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "挑战更新",
                "_-_ 新增_2_个挑战\n"+
                        "_-_ _红包作战：_所有敌人的常规掉落物被替换为红包，红包必定能开出压岁钱，有小概率开出特别惊喜\n"+
                        "_-_ _最大麦穗：_每个天赋槽被蜕变过一次后不能够再次蜕变\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY),"视觉更新",
                "_-_ 对大量天赋图标进行重绘，使美术风格更加贴近破碎，由_DM-216_进行重绘\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复各类贴图错误\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月21日\n"+
                        "_-_ 调整_红包作战_挑战：红包可以开出另一种特殊物品，启用此挑战会使游戏得分清零，同时解决宝箱怪、小偷、任务敌人等敌人的掉落异常问题\n"+
                        "_-_ 对_女猎手_的初始天赋进行调整\n"+
                        "_-_ _DM-300_掉落的升华秘卷给予的天赋点移到2层，使用升华秘卷还会有金光特效\n"
        ));
    }

    public static void add_v0_1_6Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_10_个正面天赋\n"+
                        "_-_ _1层：_涿郡屠夫，胃有强酸，细嚼慢咽，所向披靡\n"+
                        "_-_ _2层：_矢贯坚石\n" +
                        "_-_ _3层：_借痛排毒，载誉而归，武器制造，私房钱，大地一餐\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复_幻影射手_吞投掷物的Bug，顺便调整_算无遗策_效果以及排行榜处挑战栏目大小等\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月23日\n"
        ));
    }
    public static void add_v0_1_7Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_SUBLITION), "Boss天赋更新\n",
                "_-_ 新增_3_个BOSS天赋：幼虫呼唤，巨拳之力，死亡射线\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月23日\n"+
                        "_-_ _升华秘卷_在描述中说明各Boss天赋位置和天赋点分配，以及其他改动\n"
        ));
    }

    public static void add_v0_1_8Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_20_个正面天赋\n"+
                        "_-_ _1层：_带刺玫瑰，饭里藏金，水鬼索命，灰烬账簿，照明之秘\n"+
                        "_-_ _2层：_好吃到飞，道友且慢，五雷正法，马猴烧酒\n" +
                        "_-_ _3层：_脚底抹油，纵火狂，天有撅人之好，英雄之名，天地之力，以水代食，天使姿态，故乡的云，极度深寒，知识渊博，不要视奸\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.MAGICGIRL, 0, 90, 12, 15),"美少女“战士”！",
                "_-_ 天赋马猴烧酒新增变身贴图，由_DM-216_绘制\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复各类贴图错误\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月24日\n"+
                        "_-_ _蜕变秘卷_选项+1\n"+
                        "_-_ _新春红包_一键打开代码优化\n"+
                        "_-_ 部分天赋强度调整\n"

        ));
    }
    public static void add_v0_1_9Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "天赋更新",
                "_-_ 新增共计_20_个正面天赋\n"+
                        "_-_ _1层：_破损核心，麻醉酊剂，吃出经验\n"+
                        "_-_ _2层：_深渊凝视，自然之行\n" +
                        "_-_ _3层：_痴愚，爱情背刺，概念格挡，算盘，时之沙，开卷有益，强化斗篷，万灵药水，活动肌肉，四海乘风，回味无穷，生日礼物，收集种子，脑袋尖尖，强化袖章\n" +
                        "\n"
        ));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),"平衡调整\n",
                "_-_ 天赋巨物杀手增伤提高，并更名为_巨人杀手_\n"+
                        "_-_ DM-300天赋_毒气喷射_加强，在两格距离内不会释放气体并可以在一般位置上释放气体\n"
        ));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),"Bug修复",
                "_-_ 修复各类贴图错误\n"
        ));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "改动",
                "_-_ 更新日期：2025年1月26日\n"+
                        "_-_ 预加载_v0.2_的更新\n"

        ));
    }

}
