package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class TestAlignment extends TestGenerator {

    {
        image = ItemSpriteSheet.SCROLL_GYFU;
    }

    private static int selected = 0;

    private static final Char.Alignment[] ALIGNMENTS = new Char.Alignment[]{
            Char.Alignment.ENEMY,
            Char.Alignment.ENEMY1,
            Char.Alignment.ENEMY2,
            Char.Alignment.ENEMY3,
            Char.Alignment.ENEMY4,
            Char.Alignment.NEUTRAL,
            Char.Alignment.ALLY
    };

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        }
    }

    private Char.Alignment selectedAlignment() {
        return ALIGNMENTS[selected];
    }

    private String alignmentName(Char.Alignment alignment) {
        if (alignment == null) {
            return Messages.get(TestAlignment.class, "null_name");
        }
        return Messages.get(TestAlignment.class, alignment.name().toLowerCase() + "_name");
    }

    private String alignmentDesc(Char.Alignment alignment) {
        return Messages.get(TestAlignment.class, alignment.name().toLowerCase() + "_desc");
    }

    private void selectTarget() {
        GameScene.selectCell(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) {
                    return;
                }

                Char ch = Actor.findChar(cell);
                if (ch == null) {
                    GLog.w(Messages.get(TestAlignment.this, "no_char"));
                    return;
                }

                Char.Alignment alignment = selectedAlignment();
                Char.Alignment oldAlignment = ch.alignment;

                ch.alignment = alignment;
                ch.updateSpriteState();
                GLog.i(Messages.get(TestAlignment.this, "set_success",
                        ch.name(),
                        alignmentName(oldAlignment),
                        alignmentName(alignment)));
            }

            @Override
            public String prompt() {
                return Messages.get(TestAlignment.this, "prompt");
            }
        });
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int GAP = 2;

        private OptionSlider o_alignment;
        private RenderedTextBlock t_alignmentInfo;
        private RedButton b_apply;

        public SettingsWindow() {
            super();

            o_alignment = new OptionSlider(Messages.get(this, "alignment"),
                    "0",
                    String.valueOf(ALIGNMENTS.length - 1),
                    0,
                    ALIGNMENTS.length - 1) {
                @Override
                protected void onChange() {
                    selected = getSelectedValue();
                    updateText();
                }
            };
            o_alignment.setSelectedValue(selected);
            add(o_alignment);

            t_alignmentInfo = PixelScene.renderTextBlock("", 6);
            t_alignmentInfo.maxWidth(WIDTH);
            add(t_alignmentInfo);

            b_apply = new RedButton(Messages.get(this, "apply_button")) {
                @Override
                protected void onClick() {
                    SettingsWindow.this.hide();
                    selectTarget();
                }
            };
            add(b_apply);

            updateText();
        }

        private void layout() {
            o_alignment.setRect(0, GAP, WIDTH, 24);
            t_alignmentInfo.setPos(0, GAP + o_alignment.bottom());
            b_apply.setRect(0, t_alignmentInfo.bottom() + GAP, WIDTH, 16);
            resize(WIDTH, (int) b_apply.bottom());
        }

        private String infoBuilder() {
            Char.Alignment alignment = selectedAlignment();
            return Messages.get(this, "selected", selected, alignmentName(alignment))
                    + "\n\n" + alignmentDesc(alignment);
        }

        private void updateText() {
            t_alignmentInfo.text(infoBuilder());
            layout();
        }
    }
}
