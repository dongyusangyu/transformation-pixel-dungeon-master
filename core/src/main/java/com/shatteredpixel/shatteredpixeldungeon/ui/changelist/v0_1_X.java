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

        ChangeInfo changes = new ChangeInfo(Messages.get(v0_1_X.class, "v0_1_0.title"), true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TPX), Messages.get(v0_1_X.class, "v0_1_0.button_1.title"), Messages.get(v0_1_X.class, "v0_1_0.button_1.text")));
    }
    public static void add_v0_1_1Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_1.button_1.title"), Messages.get(v0_1_X.class, "v0_1_1.button_1.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_1.button_2.title"), Messages.get(v0_1_X.class, "v0_1_1.button_2.text")));
    }
    public static void add_v0_1_2Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_2.button_1.title"), Messages.get(v0_1_X.class, "v0_1_2.button_1.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_1_X.class, "v0_1_2.button_2.title"), Messages.get(v0_1_X.class, "v0_1_2.button_2.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_2.button_3.title"), Messages.get(v0_1_X.class, "v0_1_2.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_2.button_4.title"), Messages.get(v0_1_X.class, "v0_1_2.button_4.text")));
    }

    public static void add_v0_1_3Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_3.button_1.title"), Messages.get(v0_1_X.class, "v0_1_3.button_1.text")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_1_X.class, "v0_1_3.button_2.title"), Messages.get(v0_1_X.class, "v0_1_3.button_2.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_3.button_3.title"), Messages.get(v0_1_X.class, "v0_1_3.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_3.button_4.title"), Messages.get(v0_1_X.class, "v0_1_3.button_4.text")));
    }
    public static void add_v0_1_4Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_4.button_1.title"), Messages.get(v0_1_X.class, "v0_1_4.button_1.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_1_X.class, "v0_1_4.button_2.title"), Messages.get(v0_1_X.class, "v0_1_4.button_2.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_4.button_3.title"), Messages.get(v0_1_X.class, "v0_1_4.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_4.button_4.title"), Messages.get(v0_1_X.class, "v0_1_4.button_4.text")));
    }
    public static void add_v0_1_5Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), Messages.get(v0_1_X.class, "v0_1_5.button_1.title"), Messages.get(v0_1_X.class, "v0_1_5.button_1.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), Messages.get(v0_1_X.class, "v0_1_5.button_2.title"), Messages.get(v0_1_X.class, "v0_1_5.button_2.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_5.button_3.title"), Messages.get(v0_1_X.class, "v0_1_5.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_5.button_4.title"), Messages.get(v0_1_X.class, "v0_1_5.button_4.text")));
    }

    public static void add_v0_1_6Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_6.button_1.title"), Messages.get(v0_1_X.class, "v0_1_6.button_1.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_6.button_2.title"), Messages.get(v0_1_X.class, "v0_1_6.button_2.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_6.button_3.title"), Messages.get(v0_1_X.class, "v0_1_6.button_3.text")));
    }
    public static void add_v0_1_7Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_SUBLITION), Messages.get(v0_1_X.class, "v0_1_7.button_1.title"), Messages.get(v0_1_X.class, "v0_1_7.button_1.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_7.button_2.title"), Messages.get(v0_1_X.class, "v0_1_7.button_2.text")));
    }

    public static void add_v0_1_8Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_8.button_1.title"), Messages.get(v0_1_X.class, "v0_1_8.button_1.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.MAGICGIRL, 0, 90, 12, 15), Messages.get(v0_1_X.class, "v0_1_8.button_2.title"), Messages.get(v0_1_X.class, "v0_1_8.button_2.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_8.button_3.title"), Messages.get(v0_1_X.class, "v0_1_8.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_8.button_4.title"), Messages.get(v0_1_X.class, "v0_1_8.button_4.text")));
    }
    public static void add_v0_1_9Changes( ArrayList<ChangeInfo> changeInfos ) {

        ChangeInfo changes = new ChangeInfo("v0.1.9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo( Messages.get( ChangesScene.class, "new"), false, null);
        changes.hardlight( Window.TITLE_COLOR );
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), Messages.get(v0_1_X.class, "v0_1_9.button_1.title"), Messages.get(v0_1_X.class, "v0_1_9.button_1.text")));
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.WARNING );
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), Messages.get(v0_1_X.class, "v0_1_9.button_2.title"), Messages.get(v0_1_X.class, "v0_1_9.button_2.text")));
        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(v0_1_X.class, "v0_1_9.button_3.title"), Messages.get(v0_1_X.class, "v0_1_9.button_3.text")));
        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(v0_1_X.class, "v0_1_9.button_4.title"), Messages.get(v0_1_X.class, "v0_1_9.button_4.text")));
    }

}
