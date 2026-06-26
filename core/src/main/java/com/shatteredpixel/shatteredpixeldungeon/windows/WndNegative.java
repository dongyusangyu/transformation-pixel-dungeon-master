package com.shatteredpixel.shatteredpixeldungeon.windows;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.LinkedHashMap;
import java.util.List;

public class WndNegative extends Window {
    public static WndNegative INSTANCE;

    private static final int WIDTH = 124;
    private static final int GAP = 3;
    private static final int TALENTS_PER_PICK = 2;
    private static final int NEGATIVE_CHALLENGE_ICON = 11;

    private static final int[] DEPTHS = {2, 6, 11, 21};
    private static final int[] OPTION_GROUPS = {0, 1, 2, 4};

    private final int stage;
    private final int talentTier;
    private boolean selectionComplete;

    public static void showIfNeeded() {
        if (!Dungeon.isChallenged(Challenges.NEGATIVE)
                || hero == null
                || !hero.isAlive()
                || INSTANCE != null) {
            return;
        }

        for (int i = 0; i < DEPTHS.length; i++) {
            if (Dungeon.depth == DEPTHS[i] && !Statistics.negativetalents[i]) {
                if (Dungeon.isChallenged(Challenges.EXTREME_ENVIRONMENT) && (i == 0 || i == 3)) {
                    grantExtremeTalents(i, OPTION_GROUPS[i]);
                } else {
                    GameScene.show(new WndNegative(i, OPTION_GROUPS[i]));
                }
                break;
            }
        }
    }

    private static void grantExtremeTalents(int stage, int optionGroup) {
        for (Talent talent : optionTalents(optionGroup)) {
            TalentButton.addTalent(talent, 0, stage + 1);
        }
        GLog.n(Messages.get(Challenges.class,"weakened_text"));
        Sample.INSTANCE.play(Assets.Sounds.CURSED);
    }

    private static List<Talent> optionTalents(int optionGroup) {
        List<Talent> availableTalents = Talent.getNegativeTalent();
        int start = optionGroup * TALENTS_PER_PICK;
        start = Math.min(start, Math.max(0, availableTalents.size() - TALENTS_PER_PICK));
        int end = Math.min(start + TALENTS_PER_PICK, availableTalents.size());
        return availableTalents.subList(start, end);
    }

    public WndNegative(int stage, int optionGroup) {
        super();

        INSTANCE = this;
        this.stage = stage;
        this.talentTier = stage + 1;

        LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
        StatusPane.talentBlink = 10f;
        WndHero.lastIdx = 1;

        for (Talent talent : optionTalents(optionGroup)) {
            options.put(talent, 0);
        }
        setup(options);
    }

    private void setup(LinkedHashMap<Talent, Integer> replaceOptions) {
        float top = 0;

        IconTitle title = new IconTitle(Challenges.icon(NEGATIVE_CHALLENGE_ICON),
                Messages.titleCase(Messages.get(this, "title")));
        title.color(TITLE_COLOR);
        title.setRect(0, 0, WIDTH, 0);
        add(title);

        top = title.bottom() + GAP;

        RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(this, "select"), 6);
        text.maxWidth(WIDTH);
        text.align(RenderedTextBlock.CENTER_ALIGN);
        text.setPos(0, top);
        add(text);

        top = text.bottom() + GAP;

        TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(replaceOptions, talentTier, TalentButton.Mode.NEGATIVE);

        add(optionsPane);
        optionsPane.title.text(" ");
        optionsPane.setPos(0, top);
        optionsPane.setSize(WIDTH, optionsPane.height());
        resize((int) optionsPane.width(), (int) optionsPane.bottom());

        resize(WIDTH, (int) optionsPane.bottom());
    }

    public void select(Talent talent) {
        if (selectionComplete) return;
        selectionComplete = true;
        TalentButton.addTalent(talent, 0, talentTier);
        hide();
    }

    @Override
    public void hide() {
        if (!selectionComplete
                && hero != null
                && hero.isAlive()
                && Dungeon.isChallenged(Challenges.NEGATIVE)
                && stage >= 0
                && stage < Statistics.negativetalents.length
                && !Statistics.negativetalents[stage]) {
            StatusPane.talentBlink = 10f;
            return;
        }
        super.hide();
        if (INSTANCE == this) {
            INSTANCE = null;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
