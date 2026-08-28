package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PrecognitiveEye;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Objects;

public class TestArtifact extends TestGenerator {
    {
        image = ItemSpriteSheet.ARTIFACT_HOLDER;
    }
    private int level;
    private int selected;
    private boolean cursed;
    @Override
    public ArrayList<String> actions(Hero hero) {
        return super.actions(hero);
    }
    @Override
    public void execute(Hero hero, String action ) {
        super.execute( hero, action );
        if(action.equals(AC_GIVE)){
            GameScene.show(new SettingsWindow());
        }
    }

    private void modifyArtifact(Artifact a){
        int max = Math.min(level, a.levelCap());
        for(int i=0;i<max; ++i){
            a.upgrade();
        }
        a.cursed = cursed;
    }
    private void createArtifact(){
        Artifact a = Reflection.newInstance(selectedArtifactType());
        if(a != null){
            modifyArtifact(a);
            if(Challenges.isItemBlocked(a)) return;
            a.identify();
            if(a.collect()){
                GLog.i(Messages.get(this, "collect_success", a.name()));
            }else{
                a.doDrop(curUser);
            }
        }
    }
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("selected", selected);
        bundle.put("is_cursed", cursed);
        bundle.put("level", level);
    }
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        selected = bundle.getInt("selected");
        cursed = bundle.getBoolean("is_cursed");
        level = bundle.getInt("level");
    }

    private static final Class<?>[] LEGACY_ARTIFACT_ORDER = new Class[]{
            AlchemistsToolkit.class,
            CapeOfThorns.class,
            ChaliceOfBlood.class,
            CloakOfShadows.class,
            DriedRose.class,
            EtherealChains.class,
            HornOfPlenty.class,
            LloydsBeacon.class,
            MasterThievesArmband.class,
            SandalsOfNature.class,
            TalismanOfForesight.class,
            TimekeepersHourglass.class,
            UnstableSpellbook.class,
            Shuriken_Box.class,
            HolyTome.class,
            InstructionTool.class,
            SkeletonKey.class,
            PrecognitiveEye.class
    };

    private static final ArrayList<Class<? extends Artifact>> artifactList = new ArrayList<>();

    static int artifactLevelCap(Class<? extends Artifact> type) {
        Artifact artifact = Reflection.newInstance(type);
        return artifact == null ? 0 : artifact.levelCap();
    }

    static int clampLevel(Class<? extends Artifact> type, int requestedLevel) {
        return Math.max(0, Math.min(requestedLevel, artifactLevelCap(type)));
    }

    static ArrayList<Class<? extends Artifact>> registeredArtifactTypes() {
        ArrayList<Class<? extends Artifact>> types = new ArrayList<>();
        for (Class<?> type : Generator.Category.ARTIFACT.classes) {
            if (Artifact.class.isAssignableFrom(type)) {
                types.add(type.asSubclass(Artifact.class));
            }
        }
        return types;
    }

    private void buildArtifactArray(){
        if(!artifactList.isEmpty()) return;

        ArrayList<Class<? extends Artifact>> registered = registeredArtifactTypes();
        for (Class<?> type : LEGACY_ARTIFACT_ORDER) {
            if (registered.remove(type)) {
                artifactList.add(type.asSubclass(Artifact.class));
            }
        }
        artifactList.addAll(registered);
    }

    private Class<? extends Artifact> selectedArtifactType() {
        buildArtifactArray();
        selected = Math.max(0, Math.min(selected, artifactList.size() - 1));
        return artifactList.get(selected);
    }



    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 17;
        private static final int GAP = 2;
        private RenderedTextBlock t_selected;
        private OptionSlider o_level;
        private CheckBox c_curse;
        private RedButton b_create;
        private ArrayList<IconButton> artifactSprites = new ArrayList<>();

        public SettingsWindow(){
            buildArtifactArray();
            createArtifactImage();
            t_selected = PixelScene.renderTextBlock("", 6);
            t_selected.text();
            add((t_selected));

            createLevelSlider();

            c_curse = new CheckBox(Messages.get(this, "curse")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    cursed = checked();
                }
            };
            c_curse.checked(cursed);
            add(c_curse);

            b_create = new RedButton(Messages.get(this, "create_button")) {
                @Override
                protected void onClick() {
                    createArtifact();
                }
            };
            add(b_create);

            updateText();
        }

        private void layout(){
            t_selected.setPos(0, 3*GAP + BTN_SIZE *4);
            o_level.setRect(0, t_selected.bottom() + GAP, WIDTH, 24);
            c_curse.setRect(0, o_level.bottom() + GAP, WIDTH, 18);
            b_create.setRect(0, c_curse.bottom() + GAP, WIDTH, 16);
            resize(WIDTH, (int)b_create.bottom() + GAP);
        }

        private void createArtifactImage(){
            float left;
            float top = GAP;
            int placed = 0;
            int length = artifactList.size();
            for (int i = 0; i < length; i++) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        selected = j;
                        rebuildLevelSlider();
                        updateText();
                        super.onClick();
                    }
                };
                Image im = new ItemSprite(Objects.requireNonNull(Reflection.newInstance(artifactList.get(i))));
                btn.icon(im);
                left=0f;
                btn.setRect(left + Math.floorMod(i,7) *  BTN_SIZE, top+(int)(i/7)*BTN_SIZE, BTN_SIZE, BTN_SIZE);
                /*
                if(i<7) {
                    left = (WIDTH - BTN_SIZE * 7) / 2f;
                    btn.setRect(left + placed * BTN_SIZE, top, BTN_SIZE, BTN_SIZE);
                }
                else {
                    left = (WIDTH - BTN_SIZE * 8) / 2f;
                    btn.setRect(left + (placed-7) * BTN_SIZE, top + GAP + BTN_SIZE, BTN_SIZE, BTN_SIZE);
                }

                 */
                add(btn);
                placed++;
                artifactSprites.add(btn);
            }
        }

        private void updateText(){
            t_selected.text(Messages.get(TestArtifact.class, "selected", Messages.get(selectedArtifactType(), "name")));
            layout();
        }

        private void createLevelSlider() {
            Class<? extends Artifact> type = selectedArtifactType();
            int max = artifactLevelCap(type);
            level = clampLevel(type, level);
            o_level = new OptionSlider(Messages.get(this, "level"), "0", Integer.toString(max), 0, max) {
                @Override
                protected void onChange() {
                    level = getSelectedValue();
                }
            };
            o_level.setSelectedValue(level);
            add(o_level);
        }

        private void rebuildLevelSlider() {
            if (o_level != null) {
                o_level.killAndErase();
            }
            createLevelSlider();
        }
    }
}
