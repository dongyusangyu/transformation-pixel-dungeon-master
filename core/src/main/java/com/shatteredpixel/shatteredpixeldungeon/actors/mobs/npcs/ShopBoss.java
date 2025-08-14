package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatDemon;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Callback;

public class ShopBoss extends NPC {
    {
        spriteClass = ShopkeeperSprite.class;

        properties.add(Property.IMMOVABLE);
    }
    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }

    @Override
    public void damage( int dmg, Object src ) {
        //do nothing
    }
    @Override
    public boolean add( Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }
    private void confirm( Window chooseWindow ) {

    }

    @Override
    public boolean interact(Char c) {

        sprite.turnTo( pos, hero.pos );

        if (c != hero){
            return true;
        }
        Amulet amulet = hero.belongings.getItem( Amulet.class );
        if(amulet==null){
            yell(Messages.get(this, "hello", Messages.titleCase(hero.name())));
        }else{
            if(!Dungeon.level.locked){
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        GameScene.show(new WndOptions(new ItemSprite(new Amulet()),
                                Messages.titleCase(new ShopBoss().name()),
                                Messages.get(ShopBoss.class, "warning"),
                                Messages.get(ShopBoss.class, "yes"),
                                Messages.get(ShopBoss.class, "no") ) {
                            @Override
                            protected void onSelect( int index ) {
                                switch (index) {
                                    case 0:
                                        die(null);
                                        Dungeon.level.reseal();
                                        ScrollOfTeleportation.appear(hero, CityBossLevel.throne + 3 * Dungeon.level.width());
                                        Dungeon.level.occupyCell( hero );
                                        Dungeon.observe();
                                        GameScene.updateFog();
                                        Game.runOnRenderThread(new Callback() {
                                            @Override
                                            public void call() {
                                                Music.INSTANCE.play(Assets.Music.BACKS2, true);
                                            }
                                        });
                                        break;
                                    case 1:
                                        //do nothing
                                        break;
                                }
                            }
                            public void onBackPressed() {}
                        } );
                    }
                });
            }


        }
        return true;
    }
}
