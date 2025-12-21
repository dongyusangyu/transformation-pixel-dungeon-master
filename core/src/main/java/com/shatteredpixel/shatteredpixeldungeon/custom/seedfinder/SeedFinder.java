package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.getNegativeTalent;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SeedFinder {
    enum Condition {ANY, ALL}
    enum FINDING {STOP,CONTINUE}

    public static FINDING findingStatus = FINDING.STOP;

    public static class Options {
        public static int floors;
        public static Condition condition;
        public static long seed;
    }

    static class HeapItem {
        public Item item;
        public Heap heap;

        public HeapItem(Item item, Heap heap) {
            this.item = item;
            this.heap = heap;
        }
    }

    List<Class<? extends Item>> blacklist;
    ArrayList<String> itemList;
    ArrayList<String> itemLists;
    ArrayList<String> talentLists;

    private void addTextItems(String caption, ArrayList<HeapItem> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");

            for (HeapItem item : items) {
                Item i = item.item;
                Heap h = item.heap;

                if (((i instanceof Armor && ((Armor) i).hasGoodGlyph()) ||
                        (i instanceof Weapon && ((Weapon) i).hasGoodEnchant()) ||
                        (i instanceof Ring) || (i instanceof Wand)) && i.cursed)
                    builder.append("- " + Messages.get(this, "cursed")).append(i.title().toLowerCase());

                else
                    builder.append("- ").append(i.title().toLowerCase());

                if (h.type != Type.HEAP)
                    builder.append(" (").append(h.title().toLowerCase()).append(")");

                builder.append("\n");
            }

            builder.append("\n");
        }
    }
    private void addTextTalent( ArrayList<Talent> talents, StringBuilder builder) {
        if (!talents.isEmpty()) {

            for (Talent talent : talents) {

                builder.append(talent.title());

                builder.append("\n");
            }

            builder.append("\n");
        }
    }

    private void addTextQuest(String caption, ArrayList<Item> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");

            for (Item i : items) {
                if (i.cursed)
                    builder.append("- " + Messages.get(this, "cursed")).append(i.title().toLowerCase()).append("\n");

                else
                    builder.append("- ").append(i.title().toLowerCase()).append("\n");
            }

            builder.append("\n");
        }
    }



    public void findSeed(boolean stop){
        if(!stop){
            findingStatus = FINDING.STOP;
        }
    }

    public String findSeed(String[] wanted, int floor) {
        itemLists = new ArrayList<>(Arrays.asList(wanted));
        itemList = new ArrayList<>();
        talentLists = new ArrayList<>();
        int cnt=0;
        for(String i:itemLists){
            if(cnt==0){
                if(i.contains("Negative Talent")){
                    cnt=1;
                    //continue;
                }else{itemList.add(i);}
            }else{
                talentLists.add(i);
            }
        }
        ArrayList<String> seedDigitsTable=new ArrayList<String>();
        findingStatus = FINDING.CONTINUE;
        Options.condition = Condition.ALL;
        String result="NONE";
        while(true){
            String seedDigits = Integer.toString(Random.Int(542950367));
            //String seedDigits = Integer.toString(54295036);

            if(!seedDigitsTable.contains(seedDigits)){
                seedDigitsTable.add(seedDigits);
                for (int i = Random.Int(8900); i < DungeonSeed.TOTAL_SEEDS && findingStatus == FINDING.CONTINUE ; i++) {
                    if (SeedFindScene.thread.isInterrupted()) {
                        return "";
                    }
                    final String i1 = seedDigits + i;
                    Gdx.app.postRunnable(() -> {
                        if (!SeedFindScene.thread.isInterrupted()) {
                            SeedFindScene.r.text("正在查找种子:\n" + i1);
                        }
                    });
                    if (testSeedALL(seedDigits + i, floor)) {
                        result = logSeedItems(seedDigits + Integer.toString(i), floor);
//                result = seedDigits + i;
                        break;
                    } else {
                        Gdx.app.log("SeedFinder", "Seed " + seedDigits + i + " not found");
                    }
                    if(Random.Int(30)==0){
                        break;
                    }
                }
            }
            if(result!="NONE"){
                break;
            }

        }
        /*

        String seedDigits = Integer.toString(Random.Int(500000));
        findingStatus = FINDING.CONTINUE;
        Options.condition = Condition.ALL;

        String result="NONE";
//        testSeedALL(seedDigits + 1, floor);
        for (int i = Random.Int(9999999); i < DungeonSeed.TOTAL_SEEDS && findingStatus == FINDING.CONTINUE ; i++) {
            if (SeedFindScene.thread.isInterrupted()) {
                return "";
            }

            final String i1 = seedDigits + i;
            Gdx.app.postRunnable(() -> {
                if (!SeedFindScene.thread.isInterrupted()) {
                    SeedFindScene.r.text("正在查找种子:" + i1);
                }
            });
            if (testSeedALL(seedDigits + i, floor)) {
                result = logSeedItems(seedDigits + Integer.toString(i), floor);
//                result = seedDigits + i;
                break;
            } else {
                Gdx.app.log("SeedFinder", "Seed " + seedDigits + i + " not found");
            }
        }

         */
        return result;
    }

    private ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();

        for (Mob m : l.mobs) {
            if (m instanceof Statue && !(m instanceof ArmoredStatue)) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((Statue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            }

            else if (m instanceof ArmoredStatue) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((ArmoredStatue) m).armor().identify());
                h.items.add(((ArmoredStatue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            }

            else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();

                for (Item item : ((Mimic) m).items)
                    h.items.add(item.identify());

                if (m instanceof GoldenMimic) h.type = Type.HEAP;
                else if (m instanceof CrystalMimic) h.type = Type.HEAP;
                else h.type = Type.HEAP;
                heaps.add(h);
            }
        }

        return heaps;
    }

    private boolean testSeed(String seed, int floors) {
        SPDSettings.customSeed(seed);
        GamesInProgress.selectedClass = HeroClass.WARRIOR;
        Dungeon.init();

        boolean[] itemsFound = new boolean[itemList.size()];

        for (int i = 0; i < floors; i++) {
            Level l = Dungeon.newLevel();

            ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
            heaps.addAll(getMobDrops(l));

            if(Ghost.Quest.armor != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Ghost.Quest.armor.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Wandmaker.Quest.wand1 != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Wandmaker.Quest.wand1.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ","")) || Wandmaker.Quest.wand2.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                    if(Wandmaker.Quest.type() == 1 && Messages.get(this, "corpsedust").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 2 && Messages.get(this, "embers").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 3 && Messages.get(this, "rotberry").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Imp.Quest.reward != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Imp.Quest.reward.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }

            for (Heap h : heaps) {
                for (Item item : h.items) {
                    item.identify();

                    for (int j = 0; j < itemList.size(); j++) {
                        if (item.title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                            if (itemsFound[j] == false) {
                                itemsFound[j] = true;
                                break;
                            }
                        }
                    }
                }
            }

            Dungeon.depth++;
        }

        if (Options.condition == Condition.ANY) {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemsFound[i] == true)
                    return true;
            }

            return false;
        }

        else {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemsFound[i] == false)
                    return false;
            }

            return true;
        }
    }

    private boolean testSeedALL(String seed, int floors) {
        SPDSettings.customSeed(seed);
        int a = SPDSettings.challenges();
        SPDSettings.challenges( Challenges.TEST_MODE);
        Dungeon.hero = null;
        Dungeon.daily = Dungeon.dailyReplay = false;
        Dungeon.initSeed();

        Boolean talentTrue =false;
        if(!talentLists.isEmpty()){
            ArrayList<Talent> getNegativeTalent=getNegativeTalent();
            ArrayList<String> getNegativeTalents=new ArrayList<String>();
            boolean[] talentFound = new boolean[talentLists.size()];
            Arrays.fill(talentFound, false);
            for(int cnt=0;cnt<8;cnt++){
                getNegativeTalents.add(getNegativeTalent.get(cnt).title().toLowerCase());
            }
            String passTalent="**";
            if(talentLists.size()==4){
                for(int cnt=0;cnt<4;cnt++){
                    String wantingTalent = talentLists.get(cnt);
                    if(wantingTalent.contains(passTalent)){
                        if(talentFound[cnt]==false){
                            talentFound[cnt]=true;
                        }
                    }else if(wantingTalent.contains(getNegativeTalents.get(cnt*2+1)) || wantingTalent.contains(getNegativeTalents.get(cnt*2))){
                        if(talentFound[cnt]==false){
                            talentFound[cnt]=true;
                        }
                    }
                }
            }else if(talentLists.size()==1){
                int cnt=0;
                String wantingTalent = talentLists.get(cnt);
                if(wantingTalent.contains(passTalent)){
                    if(talentFound[cnt]==false){
                        talentFound[cnt]=true;
                    }
                }else if(wantingTalent.contains(getNegativeTalents.get(cnt*2+1)) || wantingTalent.contains(getNegativeTalents.get(cnt*2))){
                    if(talentFound[cnt]==false){
                        talentFound[cnt]=true;
                    }
                }
            }else{
                for(int cnt=0;cnt<talentLists.size();cnt++){
                    String wantingTalent = talentLists.get(cnt);
                    if(getNegativeTalents.contains(wantingTalent)){
                        if(talentFound[cnt]==false){
                            talentFound[cnt]=true;
                        }
                    }
                }
            }
            if(areAllTrue(talentFound)){
                talentTrue =  true;}



        }else{talentTrue =  true;}
        if(itemList.isEmpty() && talentTrue){
            return true;
        }

        GamesInProgress.selectedClass = HeroClass.WARRIOR;

        Dungeon.init();

        boolean[] itemsFound = new boolean[itemList.size()];
        Arrays.fill(itemsFound, false);


        for (int i = 0; i < floors; i++) {
            Level l = Dungeon.newLevel();

            ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
            heaps.addAll(getMobDrops(l));

            if(Ghost.Quest.armor != null){
                for (int j = 0; j < itemList.size(); j++) {
                    String wantingItem = itemList.get(j);
                    boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                    if(precise){
                        wantingItem = wantingItem.replaceAll(" ", "");
                    }else{
                        wantingItem = wantingItem.replaceAll("\"","");
                    }
                    if (!precise&&Ghost.Quest.armor.identify().title().toLowerCase().replaceAll(" ","").contains(wantingItem) || precise&& Ghost.Quest.armor.identify().title().toLowerCase().equals(wantingItem)) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Wandmaker.Quest.wand1 != null){
                for (int j = 0; j < itemList.size(); j++) {
                    String wantingItem = itemList.get(j);
                    String wand1 = Wandmaker.Quest.wand1.identify().title().toLowerCase();
                    String wand2 = Wandmaker.Quest.wand2.identify().title().toLowerCase();
                    boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                    if(precise){
                        wantingItem = wantingItem.replaceAll("\"","");
                        if (wand1.equals(wantingItem) || wand2.equals(wantingItem)) {
                            if (itemsFound[j] == false) {
                                itemsFound[j] = true;
                                break;
                            }
                        }
                    }else{
                        wantingItem = wantingItem.replaceAll(" ", "");
                        wand1 = wand1.replaceAll(" ","");
                        wand2 = wand2.replaceAll(" ","");
                        if (wand1.contains(wantingItem) || wand2.contains(wantingItem)) {
                            if (itemsFound[j] == false) {
                                itemsFound[j] = true;
                                break;
                            }
                        }
                    }
                    if(Wandmaker.Quest.type() == 1 && Messages.get(this, "corpsedust").contains(wantingItem.replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 2 && Messages.get(this, "embers").contains(wantingItem.replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 3 && Messages.get(this, "rotberry").contains(wantingItem.replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Imp.Quest.reward != null){
                for (int j = 0; j < itemList.size(); j++) {
                    String wantingItem = itemList.get(j);
                    boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                    String ring = Imp.Quest.reward.identify().title().toLowerCase();
                    if (!precise&&ring.replaceAll(" ","").contains(wantingItem.replaceAll(" ",""))
                            ||
                            precise&& ring.equals(wantingItem)) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }

            for (Heap h : heaps) {
                for (Item item : h.items) {
                    item.identify();
                    String itemName = item.title().toLowerCase();

                    for (int j = 0; j < itemList.size(); j++) {
                        String wantingItem = itemList.get(j);
                        boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                        if (!precise&&itemName.replaceAll(" ","").contains(wantingItem.replaceAll(" ",""))
                                || precise&& itemName.equals(wantingItem.replaceAll("\"", ""))) {
                            if (itemsFound[j] == false) {
                                itemsFound[j] = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(areAllTrue(itemsFound) && talentTrue==true){


                return true;
            }
            Dungeon.depth++;
        }
        SPDSettings.challenges(a);
        return false;
    }

    private static boolean areAllTrue(boolean[] array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    public String logSeedItems(String seed, int floors) {
        int a = SPDSettings.challenges();
        SPDSettings.challenges( Challenges.TEST_MODE);
        SPDSettings.customSeed(seed);
        Dungeon.initSeed();
        GamesInProgress.selectedClass = HeroClass.WARRIOR;
        Dungeon.init();
        StringBuilder result = new StringBuilder(Messages.get(this, "seed") + DungeonSeed.convertToCode(Dungeon.seed) + " \n(" + Dungeon.seed + ") " + Messages.get(this, "items") + ":\n\n");

        blacklist = Arrays.asList(Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class, EnergyCrystal.class,
                CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class);
        result.append("\n_----- ").append(Messages.get(this, "talent") + " -----_\n\n");

        ArrayList<Talent> getNegativeTalent=getNegativeTalent();
        ArrayList<Talent> getNegativeTalents=new ArrayList<Talent>();
        for(int i=0;i<8;i++){
            getNegativeTalents.add(getNegativeTalent.get(i));
        }
        StringBuilder talentBuilder = new StringBuilder();
        addTextTalent(getNegativeTalents, talentBuilder);
        result.append("\n").append(talentBuilder);

        for (int i = 0; i < floors; i++) {
            result.append("\n_----- ").append(Long.toString(Dungeon.depth)).append(" ").append(Messages.get(this, "floor") + " -----_\n\n");

            Level l = Dungeon.newLevel();
            ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
            StringBuilder builder = new StringBuilder();
            ArrayList<HeapItem> scrolls = new ArrayList<>();
            ArrayList<HeapItem> potions = new ArrayList<>();
            ArrayList<HeapItem> equipment = new ArrayList<>();
            ArrayList<HeapItem> rings = new ArrayList<>();
            ArrayList<HeapItem> artifacts = new ArrayList<>();
            ArrayList<HeapItem> wands = new ArrayList<>();
            ArrayList<HeapItem> others = new ArrayList<>();
            ArrayList<HeapItem> forSales = new ArrayList<>();

            // list quest rewards
            if (Ghost.Quest.armor != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Ghost.Quest.armor.identify());
                rewards.add(Ghost.Quest.weapon.identify());
                Ghost.Quest.complete();

                addTextQuest("[ " + Messages.get(this, "sad_ghost_reward") + " ]", rewards, builder);
            }

            if (Wandmaker.Quest.wand1 != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Wandmaker.Quest.wand1.identify());
                rewards.add(Wandmaker.Quest.wand2.identify());
                Wandmaker.Quest.complete();

                builder.append("[ " + Messages.get(this, "wandmaker_need") +" ]:\n ");


                switch (Wandmaker.Quest.type()) {
                    case 1: default:
                        builder.append(Messages.get(this, "corpsedust") + "\n\n");
                        break;
                    case 2:
                        builder.append(Messages.get(this, "embers") + "\n\n");
                        break;
                    case 3:
                        builder.append(Messages.get(this, "rotberry") + "\n\n");
                }

                addTextQuest("[ "+ Messages.get(this, "wandmaker_reward") +" ]", rewards, builder);
            }

            if (Imp.Quest.reward != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Imp.Quest.reward.identify());
                Imp.Quest.complete();

                addTextQuest("[ "+ Messages.get(this, "imp_reward") +" ]", rewards, builder);
            }

            heaps.addAll(getMobDrops(l));

            // list items
            for (Heap h : heaps) {
                for (Item item : h.items) {
                    item.identify();

                    if (h.type == Type.FOR_SALE) forSales.add(new HeapItem(item, h));
                    else if (blacklist.contains(item.getClass())) continue;
                    else if (item instanceof Scroll) scrolls.add(new HeapItem(item, h));
                    else if (item instanceof Potion) potions.add(new HeapItem(item, h));
                    else if (item instanceof MeleeWeapon || item instanceof Armor) equipment.add(new HeapItem(item, h));
                    else if (item instanceof Ring) rings.add(new HeapItem(item, h));
                    else if (item instanceof Artifact) artifacts.add(new HeapItem(item, h));
                    else if (item instanceof Wand) wands.add(new HeapItem(item, h));
                    else others.add(new HeapItem(item, h));
                }
            }

            addTextItems("[ "+ Messages.get(this, "scrolls") +" ]", scrolls, builder);
            addTextItems("[ "+ Messages.get(this, "potions") +" ]", potions, builder);
            addTextItems("[ "+ Messages.get(this, "equipment") +" ]", equipment, builder);
            addTextItems("[ "+ Messages.get(this, "rings") +" ]", rings, builder);
            addTextItems("[ "+ Messages.get(this, "artifacts") +" ]", artifacts, builder);
            addTextItems("[ "+ Messages.get(this, "wands") +" ]", wands, builder);
            addTextItems("[ "+ Messages.get(this, "for_sales") +" ]", forSales, builder);
            addTextItems("[ "+ Messages.get(this, "others") +" ]", others, builder);

            result.append("\n").append(builder);

            Dungeon.depth++;
        }
        SPDSettings.challenges(a);
        return result.toString();
    }

}

