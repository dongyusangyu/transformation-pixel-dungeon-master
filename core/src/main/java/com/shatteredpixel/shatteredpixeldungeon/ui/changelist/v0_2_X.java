package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.SpecialPackage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
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
import com.shatteredpixel.shatteredpixeldungeon.sprites.RogueBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarriorBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class v0_2_X {
    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
        add_v0_2_9fixChanges( changeInfos);
        add_v0_2_9Changes( changeInfos);
        add_v0_2_8Changes( changeInfos);
        add_v0_2_7Changes( changeInfos);
        add_v0_2_6Changes(changeInfos);
        add_v0_2_5Changes(changeInfos);
        add_v0_2_4Changes(changeInfos);
        add_v0_2_3Changes(changeInfos);
        add_v0_2_2Changes(changeInfos);
        add_v0_2_1Changes(changeInfos);
        add_v0_2_0Changes(changeInfos);
    }
    public static void add_v0_2_9fixChanges( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.9fix", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo("fix4", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG),
                Messages.get(v0_2_X.class, "v0_2_9fix4.button_1.title"), Messages.get(v0_2_X.class, "v0_2_9fix4.button_1.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS),
                Messages.get(v0_2_X.class, "v0_2_9fix4.button_2.title"), Messages.get(v0_2_X.class, "v0_2_9fix4.button_2.text_1"), Messages.get(v0_2_X.class, "v0_2_9fix4.button_2.text_2")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(v0_2_X.class, "v0_2_9fix4.button_3.title"), Messages.get(v0_2_X.class, "v0_2_9fix4.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY),
                Messages.get(v0_2_X.class, "v0_2_9fix4.button_4.title"), Messages.get(v0_2_X.class, "v0_2_9fix4.button_4.text")));

        changes = new ChangeInfo("fix3", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR),
                Messages.get(v0_2_X.class, "v0_2_9fix3.button_1.title"), Messages.get(v0_2_X.class, "v0_2_9fix3.button_1.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG),
                Messages.get(v0_2_X.class, "v0_2_9fix3.button_2.title"), Messages.get(v0_2_X.class, "v0_2_9fix3.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS),
                Messages.get(v0_2_X.class, "v0_2_9fix3.button_3.title"), Messages.get(v0_2_X.class, "v0_2_9fix3.button_3.text")));

        changes = new ChangeInfo("fix2", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH, new ItemSprite.Glowing(0x000000)),
                Messages.get(v0_2_X.class, "v0_2_9fix2.button_1.title"), Messages.get(v0_2_X.class, "v0_2_9fix2.button_1.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS),
                Messages.get(v0_2_X.class, "v0_2_9fix2.button_2.title"), Messages.get(v0_2_X.class, "v0_2_9fix2.button_2.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(v0_2_X.class, "v0_2_9fix2.button_3.title"), Messages.get(v0_2_X.class, "v0_2_9fix2.button_3.text_1"), Messages.get(v0_2_X.class, "v0_2_9fix2.button_3.text_2")));

        changes = new ChangeInfo("fix1", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT),
                Messages.get(v0_2_X.class, "v0_2_9fix.button_1.title"), Messages.get(v0_2_X.class, "v0_2_9fix.button_1.text")));
        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RITUAL_DAGGER),
                Messages.get(v0_2_X.class, "v0_2_9fix.button_2.title"), Messages.get(v0_2_X.class, "v0_2_9fix.button_2.text")));
        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BRONZE_WATCH),
                Messages.get(v0_2_X.class, "v0_2_9fix.button_3.title"), Messages.get(v0_2_X.class, "v0_2_9fix.button_3.text")));
        changes.addButton(new ChangeButton(new Image(new TenguSprite()),
                Messages.get(v0_2_X.class, "v0_2_9fix.button_4.title"), Messages.get(v0_2_X.class, "v0_2_9fix.button_4.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(v0_2_X.class, "v0_2_9fix.button_5.title"), Messages.get(v0_2_X.class, "v0_2_9fix.button_5.text_1"), Messages.get(v0_2_X.class, "v0_2_9fix.button_5.text_2")));

    }
    public static void add_v0_2_9Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_9.button_1.title"), Messages.get(v0_2_X.class, "v0_2_9.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.FRIAR, 6), Messages.get(v0_2_X.class, "v0_2_9.button_2.title"), Messages.get(v0_2_X.class, "v0_2_9.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_9.button_3.title"), Messages.get(v0_2_X.class, "v0_2_9.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_2_X.class, "v0_2_9.button_4.title"), Messages.get(v0_2_X.class, "v0_2_9.button_4.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_9.button_5.title"), Messages.get(v0_2_X.class, "v0_2_9.button_5.text")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_9.button_6.title"), Messages.get(v0_2_X.class, "v0_2_9.button_6.text_1"), Messages.get(v0_2_X.class, "v0_2_9.button_6.text_2")));


    }
    public static void add_v0_2_0Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.FREEMAN, 1), Messages.get(v0_2_X.class, "v0_2_0.button_1.title"), Messages.get(v0_2_X.class, "v0_2_0.button_1.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_0.button_2.title"), Messages.get(v0_2_X.class, "v0_2_0.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_0.button_3.title"), Messages.get(v0_2_X.class, "v0_2_0.button_3.text")));
    }
    public static void add_v0_2_1Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_1.button_1.title"), Messages.get(v0_2_X.class, "v0_2_1.button_1.text")));
        changes.addButton(new ChangeButton(new ItemSprite(new GoldIngot()), Messages.get(v0_2_X.class, "v0_2_1.button_2.title"), Messages.get(v0_2_X.class, "v0_2_1.button_2.text")));
        changes.addButton( new ChangeButton(new Image(new GreatDemonSprite()), Messages.get(v0_2_X.class, "v0_2_1.button_3.title"), Messages.get(v0_2_X.class, "v0_2_1.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_2_X.class, "v0_2_1.button_4.title"), Messages.get(v0_2_X.class, "v0_2_1.button_4.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_1.button_5.title"), Messages.get(v0_2_X.class, "v0_2_1.button_5.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_1.button_6.title"), Messages.get(v0_2_X.class, "v0_2_1.button_6.text")));


    }
    public static void add_v0_2_2Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_2.button_1.title"), Messages.get(v0_2_X.class, "v0_2_2.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.FREEMAN, 6), Messages.get(v0_2_X.class, "v0_2_2.button_2.title"), Messages.get(v0_2_X.class, "v0_2_2.button_2.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.CLERIC, 6), Messages.get(v0_2_X.class, "v0_2_2.button_3.title"), Messages.get(v0_2_X.class, "v0_2_2.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_2.button_4.title"), Messages.get(v0_2_X.class, "v0_2_2.button_4.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_2.button_5.title"), Messages.get(v0_2_X.class, "v0_2_2.button_5.text")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_2.button_6.title"), Messages.get(v0_2_X.class, "v0_2_2.button_6.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_2.button_7.title"), Messages.get(v0_2_X.class, "v0_2_2.button_7.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_2.button_8.title"), Messages.get(v0_2_X.class, "v0_2_2.button_8.text")));

    }

    public static void add_v0_2_3Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_3.button_1.title"), Messages.get(v0_2_X.class, "v0_2_3.button_1.text")));
        changes.addButton( new ChangeButton(new Image(new GreatShoperSprite()), Messages.get(v0_2_X.class, "v0_2_3.button_2.title"), Messages.get(v0_2_X.class, "v0_2_3.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_2_X.class, "v0_2_3.button_3.title"), Messages.get(v0_2_X.class, "v0_2_3.button_3.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.SHPX), Messages.get(v0_2_X.class, "v0_2_3.button_4.title"), Messages.get(v0_2_X.class, "v0_2_3.button_4.text")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_3.button_5.title"), Messages.get(v0_2_X.class, "v0_2_3.button_5.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_3.button_6.title"), Messages.get(v0_2_X.class, "v0_2_3.button_6.text")));


    }
    public static void add_v0_2_4Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_4.button_1.title"), Messages.get(v0_2_X.class, "v0_2_4.button_1.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_4.button_2.title"), Messages.get(v0_2_X.class, "v0_2_4.button_2.text")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_4.button_3.title"), Messages.get(v0_2_X.class, "v0_2_4.button_3.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_4.button_4.title"), Messages.get(v0_2_X.class, "v0_2_4.button_4.text")));
    }

    public static void add_v0_2_5Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_5.button_1.title"), Messages.get(v0_2_X.class, "v0_2_5.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.SLIMEGIRL, 6), Messages.get(v0_2_X.class, "v0_2_5.button_2.title"), Messages.get(v0_2_X.class, "v0_2_5.button_2.text_1"), Messages.get(v0_2_X.class, "v0_2_5.button_2.text_2")));
        changes.addButton( new ChangeButton(new Image(new WarriorBossSprite()), Messages.get(v0_2_X.class, "v0_2_5.button_3.title"), Messages.get(v0_2_X.class, "v0_2_5.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_2_X.class, "v0_2_5.button_4.title"), Messages.get(v0_2_X.class, "v0_2_5.button_4.text")));
        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SHURIKEN), Messages.get(v0_2_X.class, "v0_2_5.button_5.title"), Messages.get(v0_2_X.class, "v0_2_5.button_5.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_5.button_6.title"), Messages.get(v0_2_X.class, "v0_2_5.button_6.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_5.button_7.title"), Messages.get(v0_2_X.class, "v0_2_5.button_7.text")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_5.button_8.title"), Messages.get(v0_2_X.class, "v0_2_5.button_8.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_5.button_9.title"), Messages.get(v0_2_X.class, "v0_2_5.button_9.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_5.button_10.title"), Messages.get(v0_2_X.class, "v0_2_5.button_10.text_1"), Messages.get(v0_2_X.class, "v0_2_5.button_10.text_2")));

    }
    public static void add_v0_2_6Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_6.button_1.title"), Messages.get(v0_2_X.class, "v0_2_6.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.NINJA, 6), Messages.get(v0_2_X.class, "v0_2_6.button_2.title"), Messages.get(v0_2_X.class, "v0_2_6.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_6.button_3.title"), Messages.get(v0_2_X.class, "v0_2_6.button_3.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_6.button_4.title"), Messages.get(v0_2_X.class, "v0_2_6.button_4.text")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_6.button_5.title"), Messages.get(v0_2_X.class, "v0_2_6.button_5.text_1"), Messages.get(v0_2_X.class, "v0_2_6.button_5.text_2"), Messages.get(v0_2_X.class, "v0_2_6.button_5.text_3"), Messages.get(v0_2_X.class, "v0_2_6.button_5.text_4"), Messages.get(v0_2_X.class, "v0_2_6.button_5.text_5")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_6.button_6.title"), Messages.get(v0_2_X.class, "v0_2_6.button_6.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_6.button_7.title"), Messages.get(v0_2_X.class, "v0_2_6.button_7.text")));

    }

    public static void add_v0_2_7Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_7.button_1.title"), Messages.get(v0_2_X.class, "v0_2_7.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.DM400, 6), Messages.get(v0_2_X.class, "v0_2_7.button_2.title"), Messages.get(v0_2_X.class, "v0_2_7.button_2.text_1"), Messages.get(v0_2_X.class, "v0_2_7.button_2.text_2")));
        changes.addButton( new ChangeButton(new Image(new RogueBossSprite()), Messages.get(v0_2_X.class, "v0_2_7.button_3.title"), Messages.get(v0_2_X.class, "v0_2_7.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_2_X.class, "v0_2_7.button_4.title"), Messages.get(v0_2_X.class, "v0_2_7.button_4.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_7.button_5.title"), Messages.get(v0_2_X.class, "v0_2_7.button_5.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_7.button_6.title"), Messages.get(v0_2_X.class, "v0_2_7.button_6.text")));



        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);



        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_2_X.class, "v0_2_7.button_7.title"), Messages.get(v0_2_X.class, "v0_2_7.button_7.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_7.button_8.title"), Messages.get(v0_2_X.class, "v0_2_7.button_8.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_7.button_9.title"), Messages.get(v0_2_X.class, "v0_2_7.button_9.text_1"), Messages.get(v0_2_X.class, "v0_2_7.button_9.text_2")));

    }


    public static void add_v0_2_8Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.2.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_2_X.class, "v0_2_8.button_1.title"), Messages.get(v0_2_X.class, "v0_2_8.button_1.text")));
        changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.PRINCESS, 6), Messages.get(v0_2_X.class, "v0_2_8.button_2.title"), Messages.get(v0_2_X.class, "v0_2_8.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), Messages.get(v0_2_X.class, "v0_2_8.button_3.title"), Messages.get(v0_2_X.class, "v0_2_8.button_3.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.SHPX), Messages.get(v0_2_X.class, "v0_2_8.button_4.title"), Messages.get(v0_2_X.class, "v0_2_8.button_4.text")));
        changes.addButton( new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_2_X.class, "v0_2_8.button_5.title"), Messages.get(v0_2_X.class, "v0_2_8.button_5.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);


        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_2_X.class, "v0_2_8.button_6.title"), Messages.get(v0_2_X.class, "v0_2_8.button_6.text_1"), Messages.get(v0_2_X.class, "v0_2_8.button_6.text_2")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_2_X.class, "v0_2_8.button_7.title"), Messages.get(v0_2_X.class, "v0_2_8.button_7.text_1"), Messages.get(v0_2_X.class, "v0_2_8.button_7.text_2")));

    }


}
