/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
public class GreatImp extends NPC {
    {
        spriteClass = ImpSprite.class;

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
                        GameScene.show( new WndOptions(new ItemSprite(new Amulet()),
                                Messages.titleCase(new GreatImp().name()),
                                Messages.get(GreatImp.class, "warning"),
                                Messages.get(GreatImp.class, "yes"),
                                Messages.get(GreatImp.class, "no") ) {
                            @Override
                            protected void onSelect( int index ) {
                                switch (index) {
                                    case 0:
                                        Dungeon.level.reseal();
                                        ScrollOfTeleportation.appear(hero,Dungeon.level.entrance());
                                        Dungeon.level.occupyCell( hero );
                                        Dungeon.observe();
                                        GameScene.updateFog();
                                        yell(Messages.get(GreatImp.class, "time", Messages.titleCase(hero.name())));
                                        Game.runOnRenderThread(new Callback() {
                                            @Override
                                            public void call() {
                                                Music.INSTANCE.play(Assets.Music.BACKS1_1, true);
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
            } else{
                boolean sencodPhase=true;
                for ( Char a : Actor.chars() ){
                    if (a instanceof ChaosDisciples){
                        sencodPhase=false;
                        break;
                    }
                }
                if(sencodPhase){
                    Game.runOnRenderThread(new Callback() {
                        @Override
                        public void call() {
                            Music.INSTANCE.play(Assets.Music.BACKS1_2, true);
                        }
                    });
                    yell(Messages.get(this, "die", Messages.titleCase(hero.name())));
                    die(null);
                    GreatDemon lastBoss =new  GreatDemon();
                    lastBoss.pos=pos;
                    Buff.affect(lastBoss, GreatDemon.MagicCooldown.class,6);
                    Buff.affect(lastBoss, GreatDemon.MissilesCooldown.class,3);
                    Buff.affect(lastBoss, GreatDemon.SummonCooldown.class,30);
                    Buff.affect(lastBoss, GreatDemon.ThrowCooldown.class,30);
                    ScrollOfTeleportation.appear(hero,Dungeon.level.entrance());
                    Dungeon.level.occupyCell( hero );
                    Dungeon.observe();
                    GameScene.updateFog();
                    GameScene.add( lastBoss );
                    BossHealthBar.assignBoss(lastBoss);
                }else{
                    yell(Messages.get(this, "nothing", Messages.titleCase(hero.name())));
                }

            }


        }
        return true;
    }

}
