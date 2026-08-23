package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class v0_3_X {

    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v0_3_0AlphaChanges(changeInfos);
    }

    public static void add_v0_3_0AlphaChanges(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.3.0Alpha", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo("v0.3.0Alpha2-Alpha3", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        addButton(changes, Icons.STAIRS, "v0_3_0alpha.button_1.title", "v0_3_0alpha3.button_1.text");
        addButton(changes, Icons.DISPLAY, "v0_3_0alpha.button_6.title", "v0_3_0alpha3.button_2.text");




        addButton(changes, Icons.PREFS, "v0_3_0alpha.button_3.title", "v0_3_0alpha3.button_3.text");
        addButton(changes, Icons.WARNING, "v0_3_0alpha.button_4.title", "v0_3_0alpha3.button_4.text");
        changes = new ChangeInfo("v0.3.0Alpha1", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        addButton(changes, Icons.STAIRS, "v0_3_0alpha.button_1.title", "v0_3_0alpha.button_1.text");
        addButton(changes, Icons.TALENT, "v0_3_0alpha.button_2.title", "v0_3_0alpha.button_2.text");

        addButton(changes, Icons.PREFS, "v0_3_0alpha.button_3.title", "v0_3_0alpha.button_3.text");
        addButton(changes, new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "v0_3_0alpha.button_4.title", "v0_3_0alpha.button_4.text");
        addButton(changes, Icons.WARNING, "v0_3_0alpha.button_5.title", "v0_3_0alpha.button_5.text");


    }

    private static void addButton(ChangeInfo changes, Icons icon, String titleKey, String textKey) {
        changes.addButton(new ChangeButton(
                Icons.get(icon),
                Messages.get(v0_3_X.class, titleKey),
                Messages.get(v0_3_X.class, textKey)));
    }
    private static void addButton(ChangeInfo changes, Image icon, String titleKey, String textKey) {
        changes.addButton(new ChangeButton(
                icon,
                Messages.get(v0_3_X.class, titleKey),
                Messages.get(v0_3_X.class, textKey)));
    }
    private static void addButton(ChangeInfo changes, Icons icon, String titleKey, String textKey, String textKey1) {
        changes.addButton(new ChangeButton(
                Icons.get(icon),
                Messages.get(v0_3_X.class, titleKey),
                Messages.get(v0_3_X.class, textKey),
                Messages.get(v0_3_X.class, textKey)));
    }
}
