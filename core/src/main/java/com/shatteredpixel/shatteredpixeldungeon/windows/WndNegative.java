package com.shatteredpixel.shatteredpixeldungeon.windows;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class WndNegative extends Window {
    public static WndNegative INSTANCE;

    //public Talent replacing;
    //public String type;
    //LinkedHashMap<Talent, Integer> replaceOptions;

    //for window restoring
        /*
        public WndSublimation(){
            super();

            if (INSTANCE != null){
                replacing = INSTANCE.replacing;
                tier = INSTANCE.tier;
                replaceOptions = INSTANCE.replaceOptions;
                INSTANCE = this;
                setup(replacing, tier, replaceOptions);
            } else {
                hide();
            }
        }

         */
    public WndNegative() {
        super();

        INSTANCE = this;
        //this.replacing = replacing;
        // this.type = type;
        int tier = hero.negativeTalents.size();
        int index = 2;


        LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
        //Set<Talent> curTalentsAtTier = Dungeon.hero.talents.get(tier-1).keySet();

        List<Talent> availableTalents = Talent.getNegativeTalent();
        StatusPane.talentBlink = 10f;
        WndHero.lastIdx = 1;
        List<Talent> selectedTalents = availableTalents.subList((tier)*index,(tier+1)*index);
        /*
        if (!hero.negativeTalents.isEmpty()) {
            for (Talent talent : hero.negativeTalents) {
                if (availableTalents.contains(talent)) {
                    availableTalents.remove(talent);
                }
            }
        }

        List<Talent> selectedTalents = new ArrayList<>();
        while (selectedTalents.size() < 3 && !availableTalents.isEmpty()) {
            Talent randomTalent = Random.element(availableTalents);
            selectedTalents.add(randomTalent);
            availableTalents.remove(randomTalent);
        }

         */
        //List<Talent> selectedTalents = availableTalents;
        for (Talent talent : selectedTalents) {
            options.put(talent, 0);
        }
        setup(tier, options);
    }

    private void setup(int tier, LinkedHashMap<Talent, Integer> replaceOptions) {
        float top = 0;

        IconTitle title = new IconTitle();
        title.color(TITLE_COLOR);
        title.setRect(0, 0, 120, 0);
        add(title);

        top = title.bottom() + 2;

        RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(this, "select"), 6);
        text.maxWidth(120);
        text.setPos(0, top);
        add(text);

        top = text.bottom() + 2;

        TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(replaceOptions, tier, TalentButton.Mode.NEGATIVE);

        add(optionsPane);
        optionsPane.title.text(" ");
        optionsPane.setPos(0, top);
        optionsPane.setSize(120, optionsPane.height());
        resize((int) optionsPane.width(), (int) optionsPane.bottom());

        resize(120, (int) optionsPane.bottom());
    }

    @Override
    public void hide() {
        super.hide();
        if (INSTANCE == this) {
            INSTANCE = null;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
