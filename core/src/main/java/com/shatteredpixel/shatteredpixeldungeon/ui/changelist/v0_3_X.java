package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class v0_3_X {

    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v0_3_0Changes(changeInfos);
    }

    public static void add_v0_3_0Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.3.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        addButton(changes, Icons.STAIRS, "v0_3_0.button_1.title", "v0_3_0.button_1.text");
        addButton(changes, Icons.TALENT, "v0_3_0.button_2.title", "v0_3_0.button_2.text");
        addButton(changes, Icons.PREFS, "v0_3_0.button_3.title", "v0_3_0.button_3.text");
        addButton(changes, Icons.DISPLAY, "v0_3_0.button_4.title", "v0_3_0.button_4.text");
        addButton(changes, Icons.WARNING, "v0_3_0.button_5.title", "v0_3_0.button_5.text");
    }

    private static void addButton(ChangeInfo changes, Icons icon, String titleKey, String textKey) {
        changes.addButton(new ChangeButton(
                Icons.get(icon),
                Messages.get(v0_3_X.class, titleKey),
                Messages.get(v0_3_X.class, textKey)));
    }
}
