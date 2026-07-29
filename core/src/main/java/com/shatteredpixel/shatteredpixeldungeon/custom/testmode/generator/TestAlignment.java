package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.levels.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class TestAlignment extends TestGenerator {

    private static final String AC_ARENA = "arena";

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
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ARENA);
        return actions;
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (action.equals(AC_ARENA)) {
            return Messages.get(this,
                    TestArenaLevel.isCurrentLevel() ? "ac_leave_arena" : "ac_enter_arena");
        }
        return super.actionName(action, hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        } else if (action.equals(AC_ARENA)) {
            useTestArena(hero);
        }
    }

    private void useTestArena(Hero hero) {
        if (TestArenaLevel.isCurrentLevel()) {
            ArenaReturnTracker tracker = hero.buff(ArenaReturnTracker.class);
            if (tracker == null || !tracker.hasReturnPoint()) {
                GLog.w(Messages.get(this, "arena_no_return"));
                return;
            }
            switchLevel(tracker.returnDepth(), tracker.returnBranch(), tracker.returnPos());
        } else {
            if (!Dungeon.isChallenged(Challenges.TEST_MODE)
                    || Dungeon.level.locked
                    || hero.buff(LockedFloor.class) != null) {
                GLog.w(Messages.get(this, "arena_locked"));
                return;
            }
            Buff.affect(hero, ArenaReturnTracker.class)
                    .setReturnPoint(Dungeon.depth, Dungeon.branch, hero.pos);
            switchLevel(TestArenaLevel.DEPTH, TestArenaLevel.BRANCH,
                    TestArenaLevel.centerCell());
        }
    }

    private void switchLevel(int depth, int branch, int pos) {
        TimekeepersHourglass.timeFreeze timeFreeze =
                Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
        if (timeFreeze != null) {
            timeFreeze.disarmPresses();
        }
        Swiftthistle.TimeBubble timeBubble =
                Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
        if (timeBubble != null) {
            timeBubble.disarmPresses();
        }
        Level.beforeTransition();
        InterlevelScene.mode = InterlevelScene.Mode.TEST_ARENA;
        InterlevelScene.returnDepth = depth;
        InterlevelScene.returnBranch = branch;
        InterlevelScene.returnPos = pos;
        Game.switchScene(InterlevelScene.class);
    }

    public static class ArenaReturnTracker extends Buff {

        private static final String RETURN_DEPTH = "return_depth";
        private static final String RETURN_BRANCH = "return_branch";
        private static final String RETURN_POS = "return_pos";

        {
            revivePersists = true;
        }

        private int returnDepth = -1;
        private int returnBranch = -1;
        private int returnPos = -1;

        public void setReturnPoint(int depth, int branch, int pos) {
            returnDepth = depth;
            returnBranch = branch;
            returnPos = pos;
        }

        public boolean hasReturnPoint() {
            return returnDepth > 0 && returnBranch >= 0 && returnPos >= 0;
        }

        public int returnDepth() {
            return returnDepth;
        }

        public int returnBranch() {
            return returnBranch;
        }

        public int returnPos() {
            return returnPos;
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(RETURN_DEPTH, returnDepth);
            bundle.put(RETURN_BRANCH, returnBranch);
            bundle.put(RETURN_POS, returnPos);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            returnDepth = bundle.contains(RETURN_DEPTH) ? bundle.getInt(RETURN_DEPTH) : -1;
            returnBranch = bundle.contains(RETURN_BRANCH) ? bundle.getInt(RETURN_BRANCH) : -1;
            returnPos = bundle.contains(RETURN_POS) ? bundle.getInt(RETURN_POS) : -1;
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
