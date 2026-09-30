package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class v0_3_X {

    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v0_3_0fixChanges(changeInfos);
        add_v0_3_0Changes(changeInfos);

    }
    public static void add_v0_3_0fixChanges(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.3.0fix", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes = new ChangeInfo("fix2", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        addButton(changes, Icons.get(Icons.CATALOG), "v0_3_0.button_1.title", "v0_3_0fix2.button_1.text");
        addButton(changes, Icons.PREFS, "v0_3_0.button_3.title", "v0_3_0fix2.button_2.text");
        addButton(changes, Icons.WARNING, "v0_3_0.button_5.title", "v0_3_0fix2.button_3.text");
        changes = new ChangeInfo("fix1", false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        addButton(changes, new ItemSprite(EXItemSpriteSheet.NECRONOMICON, null), "v0_3_0.button_1.title", "v0_3_0fix1.button_1.text");
        addButton(changes, Icons.PREFS, "v0_3_0.button_3.title", "v0_3_0fix1.button_2.text");
        addButton(changes, Icons.WARNING, "v0_3_0.button_5.title", "v0_3_0.button_5.text");
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
    private static void addButton(ChangeInfo changes, Image icon, String titleKey, String textKey) {
        changes.addButton(new ChangeButton(
                icon,
                Messages.get(v0_3_X.class, titleKey),
                Messages.get(v0_3_X.class, textKey)));
    }
}
