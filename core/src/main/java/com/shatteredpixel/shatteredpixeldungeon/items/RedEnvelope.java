package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring.getUnknown;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genHighValueConsumable;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genLowValueConsumable;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genMidValueConsumable;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public class RedEnvelope  extends Item {

    {
        image = ItemSpriteSheet.RED_ENVELOPE;
        //image = ItemSpriteSheet.GOLD;

        stackable = true;
        unique = true;

        defaultAction = AC_USE;

        bones = false;
    }
    private static final String AC_USE = "USE";
    private static final String AC_ALLUSE = "ALLUSE";
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        actions.add( AC_ALLUSE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals(AC_USE)) {

            curUser = hero;
            curItem.detach(curUser.belongings.backpack);

            Dungeon.level.drop(new Gold().quantity(Dungeon.depth*10+ Random.Int(Dungeon.depth*20)), hero.pos).sprite.drop();
            Item bonus=null;
            if(Random.Int(10)==1){
                int cnt=Random.Int(3);
                switch (cnt){
                    case 0:bonus =genLowValueConsumable();break;
                    case 1:bonus =genMidValueConsumable();break;
                    case 2:bonus =genHighValueConsumable();break;
                }
                Dungeon.level.drop(bonus,hero.pos).sprite.drop(hero.pos);
                GLog.p( Messages.get(this, "surprise"));
            }
            if(Random.Int(100)==0){
                if(Random.Int(2)==0){
                    Dungeon.level.drop(new ScrollOfMetamorphosis().quantity(10),hero.pos).sprite.drop(hero.pos);
                    new Flare(6, 32).color(0xFFAA00, true).show(hero.sprite, 4f);
                    GLog.p( Messages.get(this, "supersurprise"));
                }else{
                    Dungeon.level.drop(new ScrollOfUpgrade(),hero.pos).sprite.drop(hero.pos);
                    new Flare(6, 32).color(0xFFAA00, true).show(hero.sprite, 4f);
                    GLog.p( Messages.get(this, "supersurprise1"));

                }

            }

        }else if(action.equals(AC_ALLUSE)){

            int cnt=this.quantity;
            /*
            //HashMap<Item,Integer> items=new HashMap<Item,Integer>;
            ArrayList<Item>items=new ArrayList<Item>();
            for(int i=0;i<cnt;i++){
                curItem.detach(curUser.belongings.backpack);
                items.add(new Gold().quantity(Dungeon.depth*10+ Random.Int(Dungeon.depth*20)));
                if(Random.Int(10)==1){
                    switch (Random.Int(3)){
                        case 0:items.add(genLowValueConsumable());break;
                        case 1:items.add(genMidValueConsumable());break;
                        case 2:items.add(genHighValueConsumable());break;
                    }
                }
                if(Random.Int(100)==0){
                    if(Random.Int(2)==0){
                        items.add(new ScrollOfMetamorphosis().quantity(10));
                    }else{
                        items.add(new ScrollOfUpgrade());
                    }

                }
            }
            for(Item item:items){
                Dungeon.level.drop(item,hero.pos).sprite.drop(hero.pos);
            }

             */
            HashMap<Class<? extends Item>, Item> items=new HashMap<Class<? extends Item>, Item>();
            for(int i=0;i<cnt;i++){
                curItem.detach(curUser.belongings.backpack);
                int goldQuantity = Dungeon.depth * 10 + Random.Int(Dungeon.depth * 20);
                Item gold = new Gold().quantity(goldQuantity);
                if (items.containsKey(Gold.class)) {
                    Item existingGold = (Item) items.get(Gold.class);
                    existingGold.quantity(existingGold.quantity() + gold.quantity());
                } else {
                    items.put(Gold.class, gold);
                }

                if(Random.Int(10)==1){
                    Item bonus=null;
                    switch (Random.Int(3)){
                        case 0:bonus =genLowValueConsumable();break;
                        case 1:bonus =genMidValueConsumable();break;
                        case 2:bonus =genHighValueConsumable();break;
                    }
                    if (items.containsKey(bonus.getClass())) {
                        Item existingGold = (Item) items.get(bonus.getClass());
                        existingGold.quantity(existingGold.quantity() + bonus.quantity());
                    } else {
                        items.put(bonus.getClass(), bonus);
                    }
                }

                if(Random.Int(100)==0){
                    Item bonus1=null;
                    if(Random.Int(2)==0){
                        bonus1=new ScrollOfMetamorphosis().quantity(10);
                    }else{
                        bonus1=new ScrollOfUpgrade();
                    }
                    if (items.containsKey(bonus1.getClass())) {
                        Item existingGold = (Item) items.get(bonus1.getClass());
                        existingGold.quantity(existingGold.quantity() + bonus1.quantity());
                    } else {
                        items.put(bonus1.getClass(), bonus1);
                    }

                }
            }
            for (Item item : items.values()) {
                Dungeon.level.drop(item, hero.pos).sprite.drop(hero.pos);
            }


        }
    }
}
