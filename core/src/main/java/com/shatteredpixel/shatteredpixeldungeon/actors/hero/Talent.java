/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.eat_item;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.energy;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.INORGANIC;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genLowValueConsumable;
import static com.watabou.utils.PathFinder.buildDistanceMap;
import static com.watabou.utils.PathFinder.distance;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StenchGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ErodingSoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeroDisguise;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Panic;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScrollEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Suffering;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TimeStasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Virtue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.dm400.Routine;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.Decoy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.OneSword;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess.KingBlade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess.MarchForward;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.DivineSense;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.RecallInscription;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatShoper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SlimeMucus;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Affection;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Entanglement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfDivination;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MetamorphosisPrism;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TelekineticGrab;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Katana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RitualDagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Wakizashi;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SakuraBlossomBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum Talent {

	//Warrior T1
	HEARTY_MEAL(0, 2, 1, TalentType.EFFECT), VETERANS_INTUITION(1, 2, 1, TalentType.ASSIST), PROVOKED_ANGER(2, 2, 1, TalentType.ATTACK), IRON_WILL(3, 2, 1, TalentType.ASSIST),
	//Warrior T2
	IRON_STOMACH(4, 2, 2, TalentType.EFFECT), LIQUID_WILLPOWER(5, 2, 2, TalentType.EFFECT), RUNIC_TRANSFERENCE(6, 2, 2, TalentType.ASSIST), LETHAL_MOMENTUM(7, 2, 2, TalentType.ATTACK), IMPROVISED_PROJECTILES(8, 2, 2, TalentType.ATTACK),
	//Warrior T3
	HOLD_FAST(9, 3, 3, TalentType.ASSIST), STRONGMAN(10, 3, 3, TalentType.ASSIST),
	//Berserker T3
	ENDLESS_RAGE(11, 3, 3, TalentType.SUBCLASS), DEATHLESS_FURY(12, 3, 3, TalentType.SUBCLASS), ENRAGED_CATALYST(13, 3, 3, TalentType.SUBCLASS),
	CEASELESS_RAGE(672, 3, 3, TalentType.SUBCLASS), MIRRORED_REVENGE(673, 3, 3, TalentType.SUBCLASS), BLOODTHIRSTY_BERSERK(674, 3, 3, TalentType.SUBCLASS),
	//Gladiator T3
	CLEAVE(14, 3, 3, TalentType.SUBCLASS), LETHAL_DEFENSE(15, 3, 3, TalentType.SUBCLASS), ENHANCED_COMBO(16, 3, 3, TalentType.SUBCLASS),
	COMBO_FOCUS(675, 3, 3, TalentType.SUBCLASS), RELENTLESS_COMBAT(676, 3, 3, TalentType.SUBCLASS), COMBO_MASTERY(677, 3, 3, TalentType.SUBCLASS),
	//Heroic Leap T4
	BODY_SLAM(17, 4, 4, TalentType.ARMOR), IMPACT_WAVE(18, 4, 4, TalentType.ARMOR), DOUBLE_JUMP(19, 4, 4, TalentType.ARMOR),
	//Shockwave T4
	EXPANDING_WAVE(20, 4, 4, TalentType.ARMOR), STRIKING_WAVE(21, 4, 4, TalentType.ARMOR), SHOCK_FORCE(22, 4, 4, TalentType.ARMOR),
	//Endure T4
	SUSTAINED_RETRIBUTION(23, 4, 4, TalentType.ARMOR), SHRUG_IT_OFF(24, 4, 4, TalentType.ARMOR), EVEN_THE_ODDS(25, 4, 4, TalentType.ARMOR),

	//Mage T1
	EMPOWERING_MEAL(32, 2, 1, TalentType.MAGIC), SCHOLARS_INTUITION(33, 2, 1, TalentType.ASSIST), LINGERING_MAGIC(34, 2, 1, TalentType.ATTACK), BACKUP_BARRIER(35, 2, 1, TalentType.EFFECT),
	//Mage T2
	ENERGIZING_MEAL(36, 2, 2, TalentType.MAGIC), INSCRIBED_POWER(37, 2, 2, TalentType.MAGIC), WAND_PRESERVATION(38, 2, 2, TalentType.RESOURCE), ARCANE_VISION(39, 2, 2, TalentType.MAGIC), SHIELD_BATTERY(40, 2, 2, TalentType.MAGIC),
	//Mage T3
	DESPERATE_POWER(41, 3, 3, TalentType.MAGIC), ALLY_WARP(42, 3, 3, TalentType.SPELL),
	//Battlemage T3
	EMPOWERED_STRIKE(43, 3, 3, TalentType.SUBCLASS), MYSTICAL_CHARGE(44, 3, 3, TalentType.SUBCLASS), EXCESS_CHARGE(45, 3, 3, TalentType.SUBCLASS),
	LONG_ARM(678, 3, 3, TalentType.SUBCLASS), ARCANE_CONFLUENCE(679, 3, 3, TalentType.SUBCLASS), FOCUSED_CASTING(680, 3, 3, TalentType.SUBCLASS),
	//Warlock T3
	SOUL_EATER(46, 3, 3, TalentType.SUBCLASS), SOUL_SIPHON(47, 3, 3, TalentType.SUBCLASS), NECROMANCERS_MINIONS(48, 3, 3, TalentType.SUBCLASS),
	FINE_TASTING(681, 3, 3, TalentType.SUBCLASS), BONE_DEEP(682, 3, 3, TalentType.SUBCLASS), MIND_IMPRISONMENT(683, 3, 3, TalentType.SUBCLASS),
	//Elemental Blast T4
	BLAST_RADIUS(49, 4, 4, TalentType.ARMOR), ELEMENTAL_POWER(50, 4, 4, TalentType.ARMOR), REACTIVE_BARRIER(51, 4, 4, TalentType.ARMOR),
	//Wild Magic T4
	WILD_POWER(52, 4, 4, TalentType.ARMOR), FIRE_EVERYTHING(53, 4, 4, TalentType.ARMOR), CONSERVED_MAGIC(54, 4, 4, TalentType.ARMOR),
	//Warp Beacon T4
	TELEFRAG(55, 4, 4, TalentType.ARMOR), REMOTE_BEACON(56, 4, 4, TalentType.ARMOR), LONGRANGE_WARP(57, 4, 4, TalentType.ARMOR),

	//Rogue T1
	CACHED_RATIONS(64, 2, 1, TalentType.RESOURCE), THIEFS_INTUITION(65, 2, 1, TalentType.ASSIST), SUCKER_PUNCH(66, 2, 1, TalentType.ATTACK), PROTECTIVE_SHADOWS(67, 2, 1, TalentType.EFFECT),
	//Rogue T2
	MYSTICAL_MEAL(68, 2, 2, TalentType.EFFECT), INSCRIBED_STEALTH(69, 2, 2, TalentType.EFFECT), WIDE_SEARCH(70, 2, 2, TalentType.ASSIST), SILENT_STEPS(71, 2, 2, TalentType.ASSIST), ROGUES_FORESIGHT(72, 2, 2, TalentType.RESOURCE),
	//Rogue T3
	ENHANCED_RINGS(73, 3, 3, TalentType.EFFECT), LIGHT_CLOAK(74, 3, 3, TalentType.ASSIST),
	//Assassin T3
	ENHANCED_LETHALITY(75, 3, 3, TalentType.SUBCLASS), ASSASSINS_REACH(76, 3, 3, TalentType.SUBCLASS), BOUNTY_HUNTER(77, 3, 3, TalentType.SUBCLASS),
	UNEXPECTED_STRIKE(684, 3, 3, TalentType.SUBCLASS), CLOSING_STAGE(685, 3, 3, TalentType.SUBCLASS), PERFECT_FINALE(686, 3, 3, TalentType.SUBCLASS),
	//Freerunner T3
	EVASIVE_ARMOR(78, 3, 3, TalentType.SUBCLASS), PROJECTILE_MOMENTUM(79, 3, 3, TalentType.SUBCLASS), SPEEDY_STEALTH(80, 3, 3, TalentType.SUBCLASS),
	FREERUNNER_AFTERIMAGE(687, 3, 3, TalentType.SUBCLASS), MOMENTUM_RESERVE(688, 3, 3, TalentType.SUBCLASS), WARMUP_PREPARATION(689, 3, 3, TalentType.SUBCLASS),
	//Smoke Bomb T4
	HASTY_RETREAT(81, 4, 4, TalentType.ARMOR), BODY_REPLACEMENT(82, 4, 4, TalentType.ARMOR), SHADOW_STEP(83, 4, 4, TalentType.ARMOR),
	//Death Mark T4
	FEAR_THE_REAPER(84, 4, 4, TalentType.ARMOR), DEATHLY_DURABILITY(85, 4, 4, TalentType.ARMOR), DOUBLE_MARK(86, 4, 4, TalentType.ARMOR),
	//Shadow Clone T4
	SHADOW_BLADE(87, 4, 4, TalentType.ARMOR), CLONED_ARMOR(88, 4, 4, TalentType.ARMOR), PERFECT_COPY(89, 4, 4, TalentType.ARMOR),

	//Huntress T1
	NATURES_BOUNTY(96, 2, 1, TalentType.RESOURCE), SURVIVALISTS_INTUITION(97, 2, 1, TalentType.ASSIST), FOLLOWUP_STRIKE(98, 2, 1, TalentType.ATTACK), NATURES_AID(99, 2, 1, TalentType.EFFECT),
	//Huntress T2
	INVIGORATING_MEAL(100, 2, 2, TalentType.EFFECT), LIQUID_NATURE(101, 2, 2, TalentType.EFFECT), REJUVENATING_STEPS(102, 2, 2, TalentType.ASSIST), HEIGHTENED_SENSES(103, 2, 2, TalentType.ASSIST), DURABLE_PROJECTILES(104, 2, 2, TalentType.ASSIST),
	//Huntress T3
	POINT_BLANK(105, 3, 3, TalentType.ATTACK), SEER_SHOT(106, 3, 3, TalentType.EFFECT),
	//Sniper T3
	FARSIGHT(107, 3, 3, TalentType.SUBCLASS), SHARED_ENCHANTMENT(108, 3, 3, TalentType.SUBCLASS), SHARED_UPGRADES(109, 3, 3, TalentType.SUBCLASS),
	//Warden T3
	DURABLE_TIPS(110, 3, 3, TalentType.SUBCLASS), BARKSKIN(111, 3, 3, TalentType.SUBCLASS), SHIELDING_DEW(112, 3, 3, TalentType.SUBCLASS),
	//Spectral Blades T4
	FAN_OF_BLADES(113, 4, 4, TalentType.ARMOR), PROJECTING_BLADES(114, 4, 4, TalentType.ARMOR), SPIRIT_BLADES(115, 4, 4, TalentType.ARMOR),
	//Natures Power T4
	GROWING_POWER(116, 4, 4, TalentType.ARMOR), NATURES_WRATH(117, 4, 4, TalentType.ARMOR), WILD_MOMENTUM(118, 4, 4, TalentType.ARMOR),
	//Spirit Hawk T4
	EAGLE_EYE(119, 4, 4, TalentType.ARMOR), GO_FOR_THE_EYES(120, 4, 4, TalentType.ARMOR), SWIFT_SPIRIT(121, 4, 4, TalentType.ARMOR),

	//Duelist T1
	STRENGTHENING_MEAL(128, 2, 1, TalentType.ATTACK), ADVENTURERS_INTUITION(129, 2, 1, TalentType.ASSIST), PATIENT_STRIKE(130, 2, 1, TalentType.ATTACK), AGGRESSIVE_BARRIER(131, 2, 1, TalentType.EFFECT),
	//Duelist T2
	FOCUSED_MEAL(132, 2, 2, TalentType.EFFECT), LIQUID_AGILITY(133, 2, 2, TalentType.EFFECT), WEAPON_RECHARGING(134, 2, 2, TalentType.ASSIST), LETHAL_HASTE(135, 2, 2, TalentType.ATTACK), SWIFT_EQUIP(136, 2, 2, TalentType.ASSIST),
	//Duelist T3
	PRECISE_ASSAULT(137, 3, 3, TalentType.ATTACK), DEADLY_FOLLOWUP(138, 3, 3, TalentType.ATTACK),
	//Champion T3
	VARIED_CHARGE(139, 3, 3, TalentType.SUBCLASS), TWIN_UPGRADES(140, 3, 3, TalentType.SUBCLASS), COMBINED_LETHALITY(141, 3, 3, TalentType.SUBCLASS),
	WEAPON_ABILITY_MASTER(696, 3, 3, TalentType.SUBCLASS), SKILLED_PARRY(697, 3, 3, TalentType.SUBCLASS), ALTERNATING_WEAPONS(698, 3, 3, TalentType.SUBCLASS),
	//Monk T3
	UNENCUMBERED_SPIRIT(142, 3, 3, TalentType.SUBCLASS), MONASTIC_VIGOR(143, 3, 3, TalentType.SUBCLASS), COMBINED_ENERGY(144, 3, 3, TalentType.SUBCLASS),
	NATURAL_WAY(699, 3, 3, TalentType.SUBCLASS), INNER_PEACE(700, 3, 3, TalentType.SUBCLASS), YIN_YANG_BALANCE(701, 3, 3, TalentType.SUBCLASS),
	//Challenge T4
	CLOSE_THE_GAP(145, 4, 4, TalentType.ARMOR), INVIGORATING_VICTORY(146, 4, 4, TalentType.ARMOR), ELIMINATION_MATCH(147, 4, 4, TalentType.ARMOR),
	//Elemental Strike T4
	ELEMENTAL_REACH(148, 4, 4, TalentType.ARMOR), STRIKING_FORCE(149, 4, 4, TalentType.ARMOR), DIRECTED_POWER(150, 4, 4, TalentType.ARMOR),
	//Duelist A3 T4
	FEIGNED_RETREAT(151, 4, 4, TalentType.ARMOR), EXPOSE_WEAKNESS(152, 4, 4, TalentType.ARMOR), COUNTER_ABILITY(153, 4, 4, TalentType.ARMOR),

	//universal T4
	HEROIC_ENERGY(26, 4, 4, TalentType.ARMOR), //See icon() and title() for special logic for this one
	//Ratmogrify T4
	RATSISTANCE(215, 4, 4, TalentType.ARMOR), RATLOMACY(216, 4, 4, TalentType.ARMOR), RATFORCEMENTS(217, 4, 4, TalentType.ARMOR),

	//SlimeGril T1
	RESILIENT_MEAL(416, 2, 1, TalentType.EFFECT),LIQUID_PERCEPTION(417, 2, 1, TalentType.RESOURCE), WATER_WAVE(418, 2, 1, TalentType.ATTACK),LIQUID_ARMOR(419, 2, 1, TalentType.ASSIST),
	//SlimeGirl T2
	TOUGH_MEAL(420, 2, 2, TalentType.EFFECT), SLIME_GREENHOUSE(421, 2, 2, TalentType.EFFECT), ENERGY_ABSORPTION(422, 2, 2, TalentType.ASSIST),NATURAL_AFFINITY(423, 2, 2, TalentType.ASSIST),QUALITY_ABSORPTION(424, 2, 2, TalentType.ASSIST),
	//SlimeGirl T3
	ORIGINAL_MONSTER(425, 3, 3, TalentType.ASSIST),EMPOWERING_LIFE(426, 3, 3, TalentType.MAGIC),
	//WaterSlime T3
	WATER_BODY(427, 3, 3, TalentType.SUBCLASS), WATER_REVIVAL(428, 3, 3, TalentType.SUBCLASS), WATER_REGENERATION(429, 3, 3, TalentType.SUBCLASS),
	//DarkSlime T3
	POTENT_OOZE(430, 3, 3, TalentType.SUBCLASS), DARK_GAS(431, 3, 3, TalentType.SUBCLASS), DARK_LIQUID(432, 3, 3, TalentType.SUBCLASS),
	//SpringSpell T4
	POTENT_HEALING(433, 4, 4, TalentType.ARMOR), EFFICIENT_HEALING(434, 4, 4, TalentType.ARMOR), MASS_HEALING(435, 4, 4, TalentType.ARMOR),
	//RapidWaterfall T4
	VIOLENT_STORM(436, 4, 4, TalentType.ARMOR), NEW_TRAP(437, 4, 4, TalentType.ARMOR), HOLY_BATH(438, 4, 4, TalentType.ARMOR),
	//MadSlimeT4
	NO_PICK(439, 4, 4, TalentType.ARMOR), FOOD_BONUS(440, 4, 4, TalentType.ARMOR), DELICIOUS_DIGESTION(441, 4, 4, TalentType.ARMOR),
	//Ninja
	NINJA_MEAL(448, 2, 1, TalentType.EFFECT),HUNTING_INTUITION(449, 2, 1, TalentType.ASSIST),AGILE_ATTACK(450, 2, 1, TalentType.ATTACK),YOU_SCARED_ME(451, 2, 1, TalentType.ASSIST),
	YUNYING_MEAL(452, 2, 2, TalentType.EFFECT),XIA(453, 2, 2, TalentType.EFFECT),QUICK_SEARCH(454, 2, 2, TalentType.ASSIST),NINJA_SOCIAL(455, 2, 2, TalentType.ATTACK),FEINT(456, 2, 2, TalentType.EFFECT),
	QIANFA_THROWING(457, 3, 3, TalentType.ATTACK),LIGHT_BOX(458, 3, 3, TalentType.ASSIST),
	//Tatteki_ninja
	SOKO(459, 3, 3, TalentType.SUBCLASS),KONO_FUKUSA(460, 3, 3, TalentType.SUBCLASS),KUNIKUCHI(461, 3, 3, TalentType.SUBCLASS),
	SOUL_HUNTING(462, 3, 3, TalentType.SUBCLASS),USE_ENVIRONMENT(463, 3, 3, TalentType.SUBCLASS),MIND_WATER(464, 3, 3, TalentType.SUBCLASS),

	DEVERSION(465, 4, 4, TalentType.ARMOR),SHINKAGE(466, 4, 4, TalentType.ARMOR),ALLHUNTING(467, 4, 4, TalentType.ARMOR),
	POWER_GUNPOWDER(468, 4, 4, TalentType.ARMOR),TEA_STAINS(469, 4, 4, TalentType.ARMOR),FIREWORK(470, 4, 4, TalentType.ARMOR),

	//OneSword
	OFFENSIVE(471, 4, 4, TalentType.ARMOR),GLIMPSE(472, 4, 4, TalentType.ARMOR),KILL_CONTINUE(473, 4, 4, TalentType.ARMOR),
	//DM400
	MARK_MEAL(480, 2, 1, TalentType.EFFECT),OVER_CODE(481, 2, 1, TalentType.RESOURCE),TARGET_TARGETING(482, 2, 1, TalentType.ATTACK),TBM(483, 2, 1, TalentType.ASSIST),
	OVER_MEAL(484, 2, 2, TalentType.EFFECT),RECOVER_CHARGE(485, 2, 2, TalentType.EFFECT),BODY_REINFORCE(486, 2, 2, TalentType.ASSIST),EFFICIENT_ORDER(487, 2, 2, TalentType.ASSIST),ROCKET_FIST(488, 2, 2, TalentType.ATTACK),
	BIG_FIST(489, 3, 3, TalentType.ATTACK),QUICK_TOOL(490, 3, 3, TalentType.ASSIST),

	//AT400
	SUSTAIN_MARK(491, 3, 3, TalentType.SUBCLASS),BATTLE_UPGRADE(492, 3, 3, TalentType.SUBCLASS),FLY_DRONE(493, 3, 3, TalentType.SUBCLASS),
	//AU400
	SPECIAL_MARK(494, 3, 3, TalentType.SUBCLASS),ASSIST_UPGRADE(495, 3, 3, TalentType.SUBCLASS),FAST_CRUISE(496, 3, 3, TalentType.SUBCLASS),
	OVER_EXTEND(497, 4, 4, TalentType.ARMOR),TERROR_MACH(498, 4, 4, TalentType.ARMOR),STRONG_PERSERVE(499, 4, 4, TalentType.ARMOR),
	OVER_TIME(500, 4, 4, TalentType.ARMOR),DEADLY_GAS(501, 4, 4, TalentType.ARMOR),DISCHARGE_HAPPY(502, 4, 4, TalentType.ARMOR),
	PROCESS_EXTEND(503, 4, 4, TalentType.ARMOR),GLORIOUS_DEAD(504, 4, 4, TalentType.ARMOR),UPON_WAVE(505, 4, 4, TalentType.ARMOR),
    //PRINCESS
    ENCHANT_MEAL(544, 2, 1, TalentType.EFFECT),MANA_SENSE(545, 2, 1, TalentType.ASSIST),RUNE_BLADE(546, 2, 1, TalentType.MAGIC),ARCANE_SHIELD(547, 2, 1, TalentType.ASSIST),
    ROYAL_MEAL(548, 2, 2, TalentType.EFFECT),RUNE_EXPERT(549, 2, 2, TalentType.RESOURCE),MORE_RING(550, 2, 2, TalentType.ASSIST),MANA_WREATH(551, 2, 2, TalentType.ASSIST),MAGIC_ARROW(552, 2, 2, TalentType.ATTACK),
    STR_RUNE(553, 3, 3, TalentType.RESOURCE),RING_BOND(554, 3, 3, TalentType.ASSIST),
    DARKMARK(555, 3, 3, TalentType.SUBCLASS),RUNE_BLAST(556, 3, 3, TalentType.SUBCLASS),RUNE_SURGE(557, 3, 3, TalentType.SUBCLASS),
    FLUENT(558, 3, 3, TalentType.SUBCLASS),STANCE_MASTERY(559, 3, 3, TalentType.SUBCLASS),INELEMENT(560, 3, 3, TalentType.SUBCLASS),
    SPEEDUP(561, 4, 4, TalentType.ARMOR),RUN_ATTACK(562, 4, 4, TalentType.ARMOR),DELAY_TACTIC(563, 4, 4, TalentType.ARMOR),
    ELEMENT_CURSE(564, 4, 4, TalentType.ARMOR),AFFLICTED_ILLNESS(565, 4, 4, TalentType.ARMOR),SPREAD_PAIN(566, 4, 4, TalentType.ARMOR),
    KING_GAZE(567, 4, 4, TalentType.ARMOR),KING_POWER(568, 4, 4, TalentType.ARMOR),KING_MIGHT(569, 4, 4, TalentType.ARMOR),


	//GOO
	AQUATIC_RECOVER(160, 2, 2, TalentType.BOSS),PUMP_ATTACK(161, 2, 2, TalentType.BOSS),OOZE_ATTACK(162, 2, 2, TalentType.BOSS),
	//Tengu
	SURPRISE_THROW(163, 2, 1, TalentType.BOSS),SMOKE_MASK(164, 2, 1, TalentType.BOSS),RUSH(165, 2, 1, TalentType.BOSS),
	//DM300
    THUNDER_STRIKE(166, 2, 2, TalentType.BOSS),DIRECTIONAL_COLLAPSE(167, 2, 2, TalentType.BOSS),FASTING(168, 2, 2, TalentType.BOSS),
	//DwaefKing
	KING_PROTECT(169, 2, 2, TalentType.BOSS),SUMMON_FOLLOWER(170, 2, 2, TalentType.BOSS),WOLFISH_GAZE(171, 2, 2, TalentType.BOSS),ENERGY_CONVERSION(183, 2, 2, TalentType.BOSS),
	STRONG_ATTACK(172, 2, 1, TalentType.ATTACK),SURVIVAL_VOLITION(173, 2, 2, TalentType.EFFECT),TRAP_MASTER(174, 3, 3, TalentType.RESOURCE),
	STRONG_THROW(175, 2, 2, TalentType.ATTACK),COUNTER_ATTACK(176, 3, 3, TalentType.ATTACK),FEAR_INCARNATION(177, 2, 1, TalentType.ATTACK),
	BEHEST(178, 3, 3, TalentType.ASSIST),BURNING_CURSE(179, 2, 2, TalentType.MAGIC),INVISIBILITY_SHADOWS(180, 2, 1, TalentType.RESOURCE),
	JUSTICE_PUNISH(181, 2, 2, TalentType.ATTACK),OVERWHELMING(182, 3, 3, TalentType.ATTACK),MAGIC_RECYCLING(184, 3, 3, TalentType.MAGIC),
	PRECIOUS_EXPERIENCE(185, 2, 2, TalentType.RESOURCE),DISTURB_ATTACK(186, 2, 1, TalentType.ATTACK),GHOLL_WITCHCRAFT(187, 2, 2, TalentType.ATTACK),
	ENGINEER_REFIT(188, 2, 2, TalentType.OTHER),WEIRD_THROW(189, 2, 2, TalentType.ATTACK),THRID_HAND(190, 2, 1, TalentType.RESOURCE),WATER_ATTACK(191, 2, 1, TalentType.ATTACK),
	MORE_CHANCE(192, 2, 2, TalentType.RESOURCE),AMAZING_EYESIGHT(193, 2, 2, TalentType.ATTACK),LIGHT_APPLICATION(194, 2, 2, TalentType.ASSIST),
	HEAVY_APPLICATION(195, 2, 2, TalentType.ASSIST),BLESS_MEAL(196, 2, 2, TalentType.EFFECT),BOMB_MANIAC(197, 2, 1, TalentType.ASSIST),
	PHANTOM_SHOOTER(198, 3, 3, TalentType.ATTACK),WAKE_SNAKE(199, 2, 2, TalentType.OTHER),COLLECTION_GOLD(200, 2, 1, TalentType.EFFECT),
	MARTIAL_TRAIN(201, 3, 3, TalentType.ATTACK),THICKENED_ARMOR(202, 2, 1, TalentType.ASSIST),POWERFUL_CALCULATIONS(203, 2, 1, TalentType.EFFECT),
	INSERT_BID(204, 2, 1, TalentType.EFFECT),MEAL_SHIELD(205, 2, 1, TalentType.EFFECT),DROP_RESISTANT(206, 2, 1, TalentType.ASSIST),
	STRENGTH_TRAIN(207, 2, 1, TalentType.ASSIST),TREAT_MEAL(208, 2, 1, TalentType.EFFECT),COVER_SCAR(209, 2, 1, TalentType.ATTACK),
	NURTRITIOUS_MEAL(210, 2, 1, TalentType.EFFECT),RAGE_ATTACK(211, 3, 3, TalentType.ATTACK),MILITARY_WATERSKIN(212, 2, 1, TalentType.RESOURCE),
	JASMINE_TEA(213, 2, 1, TalentType.EFFECT),GOD_LEFTHAND(214, 2, 2, TalentType.ASSIST),GOD_RIGHTHAND(219, 2, 2, TalentType.ASSIST),
	GOLD_FORMATION(220, 3, 3, TalentType.RESOURCE),STRENGTHEN_CHAIN(221, 2, 2, TalentType.SPELL),STRENGTHEN_CHALICE(222, 2, 2, TalentType.SPELL),
	SAVAGE_PHYSIQUE(223, 2, 1, TalentType.ASSIST),ICE_BREAKING(27, 2, 1, TalentType.MAGIC),BURNING_BLOOD(28, 2, 2, TalentType.ASSIST),
	STRENGTH_GREATEST(29, 2, 1, TalentType.ATTACK),ACCUMULATE_STEADILY(30, 3, 3, TalentType.ATTACK),GET_UP(31, 2, 2, TalentType.EFFECT),
	MORE_TALENT(59, 2, 1, TalentType.RESOURCE),GIANT_KILLER(60, 2, 2, TalentType.ATTACK),WANT_ALL(61, 2, 2, TalentType.RESOURCE),VEGETARIANISM(62, 2, 2, TalentType.EFFECT),
	WORD_STUN(63, 2, 2, TalentType.EFFECT),NOVICE_BENEFITS(91, 2, 1, TalentType.RESOURCE),DOUBLE_TRINKETS(92, 3, 3, TalentType.RESOURCE),FISHING_TIME(93, 2, 1, TalentType.OTHER),
	POSION_DAGGER(94, 2, 1, TalentType.ATTACK),INVINCIBLE_MEAL(95, 3, 3, TalentType.EFFECT),WELLFED_MEAL(123, 3, 3, TalentType.ATTACK),
	SHOCK_BOMB(124, 2, 1, TalentType.EFFECT),ILLUSION_FEED(125, 2, 1, TalentType.RESOURCE),HEAVY_BURDEN(126, 2, 2, TalentType.ASSIST),
	ATTACK_DOOR(127, 2, 1, TalentType.ATTACK),ARROW_PENETRATION(155, 2, 2, TalentType.ATTACK),DETOX_DAMAGE(156, 3, 3, TalentType.EFFECT),
	RETURNING_HONOR(157, 3, 3, TalentType.RESOURCE),ZHUOJUN_BUTCHER(158, 2, 1, TalentType.RESOURCE),AID_STOMACH(159, 2, 1, TalentType.EFFECT),
	WEAPON_MAKE(224, 3, 3, TalentType.RESOURCE),SECRET_STASH(225, 3, 3, TalentType.RESOURCE),EARTH_MEAL(226, 3, 3, TalentType.EFFECT),
	EATEN_SLOWLY(227, 2, 1, TalentType.EFFECT),INVINCIBLE(228, 2, 2, TalentType.EFFECT),YOG_LARVA(229, 3, 3, TalentType.BOSS),
	YOG_FIST(230, 3, 3, TalentType.BOSS),YOG_RAY(231, 3, 3, TalentType.BOSS),AFRAID_DEATH(232, 3, 3, TalentType.ASSIST),
	PYROMANIAC(233, 3, 3, TalentType.RESOURCE),REVERSE_POLARITY(234, 3, 3, TalentType.EFFECT),HERO_NAME(235, 3, 3, TalentType.ASSIST),
	SKY_EARTH(236, 3, 3, TalentType.ATTACK),WATER_ISFOOD(237, 3, 3, TalentType.EFFECT),DELICIOUS_FLYING(238, 2, 2, TalentType.EFFECT),
	ANGEL_STANCE(239, 3, 3, TalentType.MAGIC),THORNY_ROSE(240, 2, 1, TalentType.EFFECT),HOMETOWN_CLOUD(241, 3, 3, TalentType.ASSIST),
	DEEP_FREEZE(242, 3, 3, TalentType.ATTACK),FRENZIED_ATTACK(243, 2, 2, TalentType.ATTACK),WULEI_ZHENGFA(244, 2, 2, TalentType.MAGIC),
	MAGIC_GIRL(245, 2, 2, TalentType.MAGIC),WIDE_KNOWLEDGE(246, 3, 3, TalentType.ASSIST),GOLD_MEAL(247, 2, 1, TalentType.RESOURCE),
	NO_VIEWRAPE(248, 3, 3, TalentType.EFFECT),WATER_GHOST(249, 2, 1, TalentType.OTHER),ASH_LEDGER(250, 2, 1, TalentType.EFFECT),
	SECRET_LIGHTING(251, 2, 1, TalentType.EFFECT),POTENTIAL_1(273, 2, 1, TalentType.OTHER),POTENTIAL_2(273, 2, 1, TalentType.OTHER),
	POTENTIAL_3(273, 2, 2, TalentType.OTHER),POTENTIAL_4(273, 2, 2, TalentType.OTHER),POTENTIAL_5(273, 2, 2, TalentType.OTHER),
	POTENTIAL_6(274, 3, 3, TalentType.OTHER),POTENTIAL_7(274, 3, 3, TalentType.OTHER),POTENTIAL_8(274, 3, 3, TalentType.OTHER),
	POTENTIAL_9(274, 3, 3, TalentType.OTHER),ABYSSAL_GAZE(252, 2, 2, TalentType.MAGIC),MORONITY(253, 3, 3, TalentType.MAGIC),
	JOURNEY_NATURE(254, 2, 2, TalentType.SPELL),LOVE_BACKSTAB(255, 3, 3, TalentType.ATTACK),CONCEPT_GRID(256, 3, 3, TalentType.ASSIST),
	ABACUS(257, 3, 3, TalentType.ATTACK),TIME_SAND(258, 3, 3, TalentType.SPELL),DAMAGED_CORE(259, 2, 1, TalentType.MAGIC),
	READ_PROFITABLE(260, 3, 3, TalentType.EFFECT),STRENGTH_CLOAK(261, 3, 3, TalentType.SPELL),ANESTHESIA(262, 2, 1, TalentType.EFFECT),
	WANLING_POTION(263, 3, 3, TalentType.EFFECT),ACTIVE_MUSCLES(264, 3, 3, TalentType.ASSIST),SEA_WIND(265, 3, 3, TalentType.ASSIST),
	ENDLESS_MEAL(266, 3, 3, TalentType.EFFECT),EXPERIENCE_MEAL(267, 2, 1, TalentType.RESOURCE),BIRTHDAY_GIFT(268, 3, 3, TalentType.RESOURCE),
	COLLECT_PLANTS(269, 3, 3, TalentType.RESOURCE),SHARP_HEAD(270, 3, 3, TalentType.OTHER),STRENGTH_ARMBAND(271, 3, 3, TalentType.SPELL),
	STRENGTH_BOOK(272, 2, 2, TalentType.SPELL),ETERNAL_CURSE(275, 2, 0, TalentType.NEGATIVE),EATER(276, 2, 0, TalentType.NEGATIVE),
	FATE_DECISION(277, 2, 0, TalentType.NEGATIVE),ENDLESS_MALICE(278, 2, 0, TalentType.NEGATIVE),FEEBLE(279, 2, 0, TalentType.NEGATIVE),
	MALNUTRITION(280, 2, 0, TalentType.NEGATIVE),SHORTSIGHTED(281, 2, 0, TalentType.NEGATIVE),BAT_SERUM(282, 2, 0, TalentType.NEGATIVE),
	MYOPIA(283, 2, 0, TalentType.NEGATIVE),LAND_SWIMMING(284, 2, 0, TalentType.NEGATIVE),JIULONGLA_COFFIN(285, 2, 0, TalentType.NEGATIVE),
	COWBOY(286, 2, 0, TalentType.NEGATIVE),MAMBA_OUT(287, 2, 0, TalentType.NEGATIVE),EXPLOSION_MEAL(288, 2, 0, TalentType.NEGATIVE),
	HANDON_GROUND(289, 2, 0, TalentType.NEGATIVE),CHILL_WATER(290, 2, 0, TalentType.NEGATIVE),LIFE_SPORT(291, 2, 0, TalentType.NEGATIVE),
	WEAKEN_CHALICE(292, 2, 0, TalentType.NEGATIVE),EXTREME_CASTING(293, 3, 3, TalentType.MAGIC),PRECISE_SHOT(294, 3, 3, TalentType.ATTACK),
	EXPLORATION_INTUITION(295, 2, 2, TalentType.ASSIST),FUDI_CHOUXIN(296, 2, 2, TalentType.ATTACK),POISON_INBODY(297, 2, 2, TalentType.ATTACK),
	TREASURE_SENSE(298, 3, 3, TalentType.RESOURCE),SEED_RECYCLING(299, 2, 2, TalentType.RESOURCE),HEDONISM(300, 2, 2, TalentType.ATTACK),
	BEYOND_LIMIT(301, 3, 3, TalentType.ASSIST),HEALTHY_FOOD(302, 3, 3, TalentType.EFFECT),MENTAL_COLLAPSE(303, 2, 0, TalentType.NEGATIVE),
	UNAVOIDABLE(304, 2, 0, TalentType.NEGATIVE),PARASITISM(305, 2, 0, TalentType.NEGATIVE),DUMP_TRUCK(306, 2, 0, TalentType.NEGATIVE),
	BURNOUT_CHAMPION(307, 2, 0, TalentType.NEGATIVE),PHOTOPHOBY(308, 2, 0, TalentType.NEGATIVE),OUTCONTROL_MAGIC(309, 2, 0, TalentType.NEGATIVE),
	WINTER_SWIMMING(310, 2, 0, TalentType.NEGATIVE),FIRE_WOOD(311, 2, 0, TalentType.NEGATIVE),BE_INCONSTANT(312, 2, 0, TalentType.NEGATIVE),
	UNBEAR_HUNGER(313, 2, 0, TalentType.NEGATIVE),VIP_MEAL(314, 2, 0, TalentType.NEGATIVE),FAST_DIE(315, 2, 0, TalentType.NEGATIVE),
	WASH_HAND(316, 2, 0, TalentType.NEGATIVE),THORNS_SPRANG(317, 2, 0, TalentType.NEGATIVE),FULLPASSION(318, 2, 0, TalentType.NEGATIVE),
	FULLFIGHTING(319, 2, 0, TalentType.NEGATIVE),PHASECLAW(320, 2, 0, TalentType.NEGATIVE),BACKFIRED(321, 2, 2, TalentType.EFFECT),
	GOLDOFBOOK(322, 2, 1, TalentType.RESOURCE),EXTREME_REACTION(323, 3, 3, TalentType.ASSIST),
	//Cleric T1
	SATIATED_SPELLS(352, 2, 1, TalentType.SPELL), HOLY_INTUITION(353, 2, 1, TalentType.SPELL), SEARING_LIGHT(354, 2, 1, TalentType.SPELL), SHIELD_OF_LIGHT(355, 2, 1, TalentType.SPELL),
	//Cleric T2
	ENLIGHTENING_MEAL(356, 2, 2, TalentType.SPELL), RECALL_INSCRIPTION(357, 2, 2, TalentType.SPELL), SUNRAY(358, 2, 2, TalentType.SPELL), DIVINE_SENSE(359, 2, 2, TalentType.SPELL), BLESS(360, 2, 2, TalentType.SPELL),
	//Cleric T3
	CLEANSE(361, 3, 3, TalentType.SPELL), LIGHT_READING(362, 3, 3, TalentType.SPELL),
	//Priest T3
	HOLY_LANCE(363, 3, 3, TalentType.SUBCLASS), HALLOWED_GROUND(364, 3, 3, TalentType.SUBCLASS), MNEMONIC_PRAYER(365, 3, 3, TalentType.SUBCLASS),
	//Paladin T3
	LAY_ON_HANDS(366, 3, 3, TalentType.SUBCLASS), AURA_OF_PROTECTION(367, 3, 3, TalentType.SUBCLASS), WALL_OF_LIGHT(368, 3, 3, TalentType.SUBCLASS),
	//Ascended Form T4
	DIVINE_INTERVENTION(369, 4, 4, TalentType.ARMOR), JUDGEMENT(370, 4, 4, TalentType.ARMOR), FLASH(371, 4, 4, TalentType.ARMOR),
	//Trinity T4
	BODY_FORM(372, 4, 4, TalentType.ARMOR), MIND_FORM(373, 4, 4, TalentType.ARMOR), SPIRIT_FORM(374, 4, 4, TalentType.ARMOR),
	//Power of Many T4
	BEAMING_RAY(375, 4, 4, TalentType.ARMOR), LIFE_LINK(376, 4, 4, TalentType.ARMOR), STASIS(377, 4, 4, TalentType.ARMOR),DEEP_FEAR(324, 2, 0, TalentType.NEGATIVE),
	NO_DOOR(325, 2, 0, TalentType.NEGATIVE),UPDRAFT(326, 2, 0, TalentType.NEGATIVE),QUANTUM_HACKING(327, 2, 2, TalentType.MAGIC),CICADA_DANCE(328, 3, 3, TalentType.ATTACK),CHOCOLATE_COINS(329, 2, 1, TalentType.OTHER),
	GHOST_GIFT(330, 2, 1, TalentType.RESOURCE),PERSONAL_ATTACK(331, 2, 1, TalentType.EFFECT),SHOOT_SATELLITE(332, 2, 2, TalentType.OTHER),HOLY_FAITH(333, 3, 3, TalentType.ASSIST),
	HONEY_FISH(334, 2, 1, TalentType.OTHER),INSTANT_REFINING(335, 2, 2, TalentType.RESOURCE),CHANGQI_BOOKSTORE(336, 3, 3, TalentType.RESOURCE),ASCENSION_CURSE(337, 2, 1, TalentType.SPELL),
	DIVINE_PROTECTION(338, 2, 2, TalentType.SPELL),SHEPHERD_INTENTION(339, 2, 1, TalentType.SPELL),CONVERSION_HOLY(340, 2, 2, TalentType.SPELL),PURIFYING_EVIL(341, 3, 3, TalentType.SPELL),
	DIVINE_STORM(342, 3, 3, TalentType.SPELL),GENESIS(343, 2, 2, TalentType.SPELL),INDULGENCE(344, 2, 2, TalentType.SPELL),SILVER_LANGUAGE(345, 2, 1, TalentType.SPELL),RESURRECTION(346, 3, 3, TalentType.SPELL),
	BONE_FIRE(347, 2, 0, TalentType.NEGATIVE),JUMP_FACE_SILICONE(348, 2, 0, TalentType.NEGATIVE),GRASS_MOB(349, 2, 0, TalentType.NEGATIVE),DISASTER_CURSE(350, 2, 0, TalentType.NEGATIVE),ASHES_BOW(351, 3, 3, TalentType.ATTACK),
	SWIFT_CHURCH(379, 3, 3, TalentType.MAGIC),PROTECT_CURSE(380, 2, 2, TalentType.ASSIST),POTENTIAL_ENERGY(381, 2, 1, TalentType.ASSIST),NIRVANA(382, 2, 2, TalentType.ASSIST),INSINUATION(383, 2, 1, TalentType.MAGIC),
	AUTO_PICK(384, 2, 1, TalentType.ASSIST),HAND_SLIP(385, 2, 0, TalentType.NEGATIVE),HAND_DESTRUCTION(386, 3, 3, TalentType.ATTACK),PREDICTIVE_LOVER(387, 2, 1, TalentType.RESOURCE),FIRE_BALL(388, 2, 1, TalentType.MAGIC),
	FLASH_GENIUS(389, 2, 1, TalentType.EFFECT),WITCH_POTION(390, 2, 2, TalentType.EFFECT),TRAITOROUS_SPELL(391, 2, 1, TalentType.SPELL),SPRINT_SPELL(392, 2, 2, TalentType.SPELL),FOCUS_LIGHT(393, 2, 1, TalentType.SPELL),
	ZHUANYU_SPELL(394, 3, 3, TalentType.SPELL),THORN_WHIP(395, 2, 2, TalentType.SPELL),STRONGEST_SHIELD(396, 2, 1, TalentType.BOSS),COMBO_PACKAGE(397, 2, 1, TalentType.BOSS),BREAK_ENEMY_RANKS(398, 2, 1, TalentType.BOSS),
	REVELATION(399, 2, 2, TalentType.SPELL),HOLY_GRENADE(400, 3, 3, TalentType.SPELL),BLADE_STAR(401, 3, 3, TalentType.SPELL),SACRED_BLADE(402, 3, 3, TalentType.SPELL),
	EQUIPMENT_BLESS(403, 2, 2, TalentType.SPELL),HOTLIGHT(404, 2, 2, TalentType.SPELL),HEALATTACK(405, 2, 1, TalentType.SPELL),EAT_MIND(406, 2, 2, TalentType.MAGIC),DISABLIITY_POSION(407, 3, 3, TalentType.MAGIC),
	LIGHT_CROP(408, 2, 1, TalentType.MAGIC),DEVIL_FLAME(409, 3, 3, TalentType.MAGIC),FAST_BREAK(410, 2, 2, TalentType.RESOURCE),CRAZY_DANCER(411, 2, 1, TalentType.ASSIST),THROWING_RECYCLING(412, 3, 3, TalentType.ATTACK),
    FALSEHOOD_POWER(413, 2, 1, TalentType.ASSIST),FLAME_INCARNATION(414, 3, 3, TalentType.EFFECT),MARKSMAN(415, 2, 1, TalentType.MAGIC),SPIDER_SENSE(443, 2, 1, TalentType.ASSIST),NO_MORE_MOB(444, 2, 1, TalentType.OTHER),
	KEBI(445, 2, 2, TalentType.RESOURCE),ICE_MEAL(446, 3, 3, TalentType.EFFECT),MORE_FAVORS(447, 2, 1, TalentType.RESOURCE),HARDWARE_ELEMENTS(475, 3, 3, TalentType.RESOURCE),STATIC_LIGHT(476, 2, 1, TalentType.MAGIC),
	MIRACLE_ALCHEMY(477, 2, 2, TalentType.RESOURCE),HUNGRY_GHOST(478, 2, 0, TalentType.NEGATIVE),UNABLE_REST(479, 2, 0, TalentType.NEGATIVE),ELITE_RECRUIT(507, 4, 4, TalentType.ARMOR),PRIM_ACCU(508, 4, 4, TalentType.ARMOR),
	SOUL_CONTRACT(509, 4, 4, TalentType.ARMOR),CURSEDMAN(511, 2, 0, TalentType.NEGATIVE),SHADOW_KILLER(512, 2, 1, TalentType.BOSS),KILL_SPREE(513, 2, 1, TalentType.BOSS),SEAOFPEOPLE(514, 2, 1, TalentType.BOSS),PHANTOM_STEP(515, 2, 1, TalentType.BOSS),
	USURY(516, 2, 0, TalentType.NEGATIVE),ARMED_UPRISING(517, 2, 1, TalentType.ATTACK),DUNGEON_HERO(518, 2, 1, TalentType.RESOURCE),EXP_SOLID(519, 2, 2, TalentType.RESOURCE), WEALTH_STATUE(520, 3, 3, TalentType.RESOURCE),
	SWEET_SLEEP(521, 2, 2, TalentType.ATTACK),LIGHT_SHOCK(522, 2, 2, TalentType.MAGIC),BOOZY(523, 2, 1, TalentType.EFFECT),STEAM_BEAN(524, 2, 0, TalentType.NEGATIVE),ALCHEMY_ACCIDENT(525, 2, 0, TalentType.NEGATIVE),SHELL_CRY(526, 2, 0, TalentType.NEGATIVE),
	MAL_CURSE(527, 2, 0, TalentType.NEGATIVE),OVERLOAD_CHARGE(528, 2, 2, TalentType.EFFECT),GAS_SPURT(529, 2, 2, TalentType.MAGIC),
	BOSS_TALENT_SLOT_1(530, 0, 1, TalentType.BOSS), BOSS_TALENT_SLOT_2(530, 0, 1, TalentType.BOSS), BOSS_TALENT_SLOT_3(530, 0, 2, TalentType.BOSS), BOSS_TALENT_SLOT_4(530, 0, 2, TalentType.BOSS), BOSS_TALENT_SLOT_5(530, 0, 3, TalentType.BOSS),
	ERODING_SOUL(531, 2, 1, TalentType.MAGIC), CREDULOUS(532, 2, 1, TalentType.SPELL), PENETRATING_CAST(533, 2, 2, TalentType.MAGIC), BOUNTIFUL_ENHANCEMENT(534, 3, 3, TalentType.SPELL), ICE_HELL(535, 3, 3, TalentType.MAGIC),
	//Huntress boss talent
	HUNTING_TECHNIQUE(602, 2, 2, TalentType.BOSS), NATURAL_CHILD(603, 2, 2, TalentType.BOSS), FALCON_EYE(604, 2, 2, TalentType.BOSS),
	//Friar T1
	TRANQUIL_TINCTURE(576, 2, 1, TalentType.RESOURCE), RAVENS_EYE(577, 2, 1, TalentType.ASSIST), CRYSTAL_GUNPOWDER(578, 2, 1, TalentType.ATTACK), BULWARK_GREATSHIELD(579, 2, 1, TalentType.ASSIST),
	//Friar T2
	WHISPERING_MEAL(580, 2, 2, TalentType.EFFECT), LIQUID_BARRIER(581, 2, 2, TalentType.EFFECT), FAST_RELOAD(582, 2, 2, TalentType.ASSIST), OTHERWORLD_BANE(583, 2, 2, TalentType.EFFECT), BULLSEYE(584, 2, 2, TalentType.ATTACK),
	//Friar T3
	MIDAS_TOUCH(585, 3, 3, TalentType.RESOURCE), HUMAN_GLORY(586, 3, 3, TalentType.ASSIST),
	//Alchemist T3
	ALCHEMY_SHIELD(587, 3, 3, TalentType.SUBCLASS), CLOSE_BLAST(588, 3, 3, TalentType.SUBCLASS), CONSERVATION(589, 3, 3, TalentType.SUBCLASS),
	//Reason Dose armor ability
	HEART_DOSE(593, 4, 4, TalentType.ARMOR), SHIELD_DOSE(594, 4, 4, TalentType.ARMOR), CALM_DOSE(595, 4, 4, TalentType.ARMOR),
	//Friar armor ability
	CONCENTRATED_ESSENCE(596, 4, 4, TalentType.ARMOR), REAGENT_ENHANCEMENT(597, 4, 4, TalentType.ARMOR), REUSE_REAGENT(598, 4, 4, TalentType.ARMOR),
	PIOUS_FAITH(599, 4, 4, TalentType.ARMOR), SUPREME_BLESSING(600, 4, 4, TalentType.ARMOR), HATRED_OF_EVIL(601, 4, 4, TalentType.ARMOR),
	//Pious T3
	NEVER_COMPROMISE(590, 3, 3, TalentType.SUBCLASS), REWIND_TIME(591, 3, 3, TalentType.SUBCLASS), BRIGHT_WARRIOR(592, 3, 3, TalentType.SUBCLASS),
	// Ratking T1
	ROYAL_PRIVILEGE(608, 2, 1, TalentType.OTHER), ROYAL_INTUITION(609, 2, 1, TalentType.OTHER), KINGS_WISDOM(610, 2, 1, TalentType.OTHER), NOBLE_CAUSE(611, 2, 1, TalentType.OTHER),
	// Ratking T2
	RK_ROYAL_MEAL(612, 2, 2, TalentType.OTHER), RESTORATION(613, 2, 2, TalentType.OTHER), POWER_WITHIN(614, 2, 2, TalentType.OTHER), KINGS_VISION(615, 2, 2, TalentType.OTHER), PURSUIT(616, 2, 2, TalentType.OTHER),
	// RatKing T3
	RK_BERSERKER(617, 3, 3, TalentType.SUBCLASS), RK_GLADIATOR(618, 3, 3, TalentType.SUBCLASS), RK_BATTLEMAGE(619, 3, 3, TalentType.SUBCLASS), RK_WARLOCK(620, 3, 3, TalentType.SUBCLASS),
	RK_ASSASSIN(621, 3, 3, TalentType.SUBCLASS), RK_FREERUNNER(622, 3, 3, TalentType.SUBCLASS), RK_SNIPER(623, 3, 3, TalentType.SUBCLASS), RK_WARDEN(624, 3, 3, TalentType.SUBCLASS),
	RK_CHAMPION(625, 3, 3, TalentType.SUBCLASS), RK_MONK(626, 3, 3, TalentType.SUBCLASS), RK_PRIEST(627, 3, 3, TalentType.SUBCLASS), RK_PALADIN(628, 3, 3, TalentType.SUBCLASS),
	RK_WATERSLIME(651, 3, 3, TalentType.SUBCLASS), RK_DARKSLIME(652, 3, 3, TalentType.SUBCLASS),
	RK_TATTEKI_NINJA(653, 3, 3, TalentType.SUBCLASS), RK_NINJA_MASTER(654, 3, 3, TalentType.SUBCLASS), RK_AT400(655, 3, 3, TalentType.SUBCLASS), RK_AU400(656, 3, 3, TalentType.SUBCLASS),
	RK_RUNEMAGE(657, 3, 3, TalentType.SUBCLASS), RK_COMBATMASTER(658, 3, 3, TalentType.SUBCLASS),
    ;

	public enum TalentType {
		ATTACK("attack"),
		MAGIC("magic"),
		EFFECT("effect"),
		RESOURCE("resource"),
		SPELL("spell"),
		ASSIST("assist"),
		OTHER("other"),
		BOSS("boss"),
		NEGATIVE("negative"),
		SUBCLASS("subclass"),
		ARMOR("armor");

		private final String messageKey;

		TalentType(String messageKey) {
			this.messageKey = messageKey;
		}

		public String messageKey() {
			return messageKey;
		}

		public boolean isCommon() {
			switch (this) {
				case ATTACK:
				case MAGIC:
				case EFFECT:
				case RESOURCE:
				case SPELL:
				case ASSIST:
				case OTHER:
					return true;
				default:
					return false;
			}
		}
	}

	public static final TalentType[] COMMON_TYPES = new TalentType[]{
			TalentType.ATTACK,
			TalentType.MAGIC,
			TalentType.EFFECT,
			TalentType.RESOURCE,
			TalentType.SPELL,
			TalentType.ASSIST,
			TalentType.OTHER
	};


	public static class LifeBarrior extends Barrier{

		@Override
		public boolean act() {
			incShield(0);
			spend( TICK );
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}

    public static class DirectionalCollapse extends Buff {

        {
            type = buffType.POSITIVE;

        }
        @Override
        public int icon() {
            return BuffIndicator.WAND;
        }
        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.6196f, 0.6196f, 0.6196f);
        }
        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc",hero!=null ? 5-hero.pointsInTalent(DIRECTIONAL_COLLAPSE):4, left);
        }
        public int left=0;
        public DirectionalCollapse set(int shots){

            left = Math.max(left, shots);
            return this;
        }
        private static final String LEFT 	= "left";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle(bundle);
            bundle.put( LEFT, left );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getInt( LEFT );
        }
    }

	public static class AlchemyShield extends Buff {

		private static final String LAYERS = "layers";
		private static final int MAX_LAYERS = 3;

		{
			type = buffType.POSITIVE;
		}

		private int layers = 0;

		public void gain( int amount ){
			if (amount <= 0) return;
			layers = Math.min(MAX_LAYERS, layers + amount);
			BuffIndicator.refreshHero();
		}

		public int absorb( int damage, Object src ){
			if (damage <= 0 || layers <= 0 || !(src instanceof Char)){
				return damage;
			}

			Char source = (Char)src;
			if (source.alignment == hero.alignment){
				return damage;
			}

			int reduction = currentReduction();
			layers--;
			if (layers <= 0){
				detach();
			} else {
				BuffIndicator.refreshHero();
			}
			return Math.max(0, damage - reduction);
		}

		private int currentReduction(){
			return Math.min(20, 5 + Dungeon.energy);
		}

		@Override
		public int icon() {
			return BuffIndicator.ENERGY_SHIELD;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(layers);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", currentReduction(), layers);
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put(LAYERS, layers);
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle(bundle);
			layers = bundle.getInt(LAYERS);
		}
	}

	public static class DyingWill extends Buff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.CORRUPT;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0xFF2222);
		}

		@Override
		public boolean act() {
			if (!hasSufferingOrVirtue(target)){
				detach();
				Buff.affect(target, HeavyWound.class, 100f);
				if (target.sprite != null) {
					target.sprite.showStatus(CharSprite.WARNING, Messages.get(this, "heavy_wound"));
				}
				GLog.w(Messages.get(this, "heavy_wound_log"));
			}
			spend(TICK);
			return true;
		}
	}

	public static class HeavyWound extends FlavourBuff {
		{
			type = buffType.NEGATIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.CORRUPT;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0x555555);
		}
	}

	public static class ResilientArmor extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1, 1, 0);
		}
		@Override
		public float iconFadePercent() {
			return Math.max(0, (3-left) / 3f);
		}
		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}
		public int left;
		public void set(int shots){
			left = Math.max(left, shots);
		}
		private static final String LEFT 	= "left";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getInt( LEFT );
		}
	}
	public static class NinjaSocial extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.INVERT_MARK;
		}
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1.0f, 0f, 0f);
		}
		@Override
		public float iconFadePercent() {
			return Math.max(0, (5-left) / 5f);
		}
		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}
		public int left;
		public void set(int shots){
			left = Math.max(left, shots);
		}
		private static final String LEFT 	= "left";
		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getInt( LEFT );
		}
	}
    public static class ManaWreath extends Buff {

        {
            type = buffType.NEGATIVE;
        }
        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }
        @Override
        public boolean act() {
            detach();
            return super.act();
        }
    }
    public static class EnchantMeal extends Buff {

        {
            type = buffType.POSITIVE;
        }
        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }
        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.537f, 0.0f, 1.0f);
        }
        @Override
        public float iconFadePercent() {
            return Math.max(0, (5-left) / 5f);
        }
        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc", left);
        }
        public int left;
        public void set(int shots){
            left = Math.max(left, shots);
        }
        private static final String LEFT 	= "left";
        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle(bundle);
            bundle.put( LEFT, left );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getInt( LEFT );
        }
    }
	public static class MarkMeal extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			if(hero!=null && hero.heroClass==HeroClass.DM400){
				return BuffIndicator.DM400MARK;
			}else{
				return BuffIndicator.INVERT_MARK;
			}

		}
		@Override
		public void tintIcon(Image icon) {
			if(hero!=null && hero.heroClass==HeroClass.DM400){
				super.tintIcon(icon);
			}else{
				icon.hardlight(0, 1, 0);
			}

		}
		@Override
		public float iconFadePercent() {
			return Math.max(0, (3-left) / 3f);
		}
		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}
		public int left=3;
		public void set(int shots){
			left = Math.max(left, shots);
		}
		private static final String LEFT 	= "left";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getInt( LEFT );
		}
	}
	public static class ComboPackage extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.COMBO_PACKAGE;
		}
		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}
		public int left;
		public void set(int shots){
			left = Math.max(left, shots);
		}
	}
	public static class EnemyDies extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}
	public static class AgileAttack extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.WEAPON;
		}
		public void tintIcon(Image icon) { icon.hardlight(1f, 0.8f, 0.85f); }

	}
	//史莱姆
	public static class SlimeMucusCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.SLIMEMUCUS; }

		public float iconFadePercent() { return Math.max(0, visualcooldown() / 30); }
		public void Reduce(){
			spend(-1);
			if(visualcooldown()<=0){
				detach();
			}
		}
	};
    public static class RoyalMeal extends FlavourBuff{
        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }
        public void tintIcon(Image icon) { icon.hardlight(1.0f, 0.604f, 0.012f); }
    };
    public static class RoyalMeal2 extends RoyalMeal{
    };
	public static class TBMCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.NONE; }
		@Override
		public void detach(){
			if(hero.hasTalent(TBM)){
				Buff.affect(hero, Barrier.class).incShield(1+2*hero.pointsInTalent(TBM));
				Buff.affect(hero,TBMCooldown.class,60);
			}else{
				super.detach();
			}


		}
	};
	public static class LightBox extends FlavourBuff{
		public int icon() { return BuffIndicator.NONE; }
	};
	public static class FeintCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.6f, 0.6f, 0.6f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	//史莱姆娘
	public static class DarkHookCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.DHCOOL; }

		public float iconFadePercent() { return Math.max(0, visualcooldown() / 100); }
		@Override
		public void detach(){
			if(hero.subClass.is(HeroSubClass.DARKSLIME)){
				ActionIndicator.setAction(hero.buff(DarkHook.class));
				BuffIndicator.refreshHero();
			}
			if (target.remove( this ) && target.sprite != null) fx( false );
		}
	};
	public static class XiaDef extends FlavourBuff{
		public int icon() { return BuffIndicator.XIA; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 0.75f, 0.79f); }
		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
	};
	public static class PhantomStep extends FlavourBuff{
		public int icon() { return BuffIndicator.XIA; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0f); }
		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
	};
	public static class DarkGasCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0.5f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 75); }
	};




	//used for metamorphed searing light
	public static class AshesBowCooldown extends FlavourBuff{
		@Override
		public int icon() {
			return BuffIndicator.TIME;
		}
		public void tintIcon(Image icon) { icon.hardlight(1.0f, 0.667f, 0.2f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
	}

	public static class UableRest extends Buff{
		{
			revivePersists=true;
		}
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}
	public static class SteamBean extends Buff{
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}



	public static class ImprovisedProjectileCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};
	public static class NoSleep extends Buff {
		public int icon() { return BuffIndicator.MAGIC_SLEEP; }
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.25f, 1.5f, 1f);
		}

	};
	public static class YogFistCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
            icon.hardlight(0.341f, 0.267f, 0.380f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class YogRayCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
            icon.hardlight(0.208f, 0.149f, 0.239f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class SmokeCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
            icon.hardlight(0.149f, 0.137f, 0.133f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
		public void detach(){
			if(hero.hasTalent(SMOKE_MASK)) {
				//Buff.affect(Dungeon.hero, SmokeMask.class);
				ActionIndicator1.setAction( hero.buff(SmokeMask.class) );
			}
			BuffIndicator.refreshHero();
			super.detach();
		}
	};
	public static class InvShaCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.071f, 0.063f, 0.424f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 40); }
	};
	public static class WordStunCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(1.0f, 0.859f, 0.396f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};

	public static class StrAtkCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.6f, 0.6f, 0.478f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
	};

	public static class SurVolCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.247f, 0.278f, 0.302f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 80); }
	};
		public static class SurResistance extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}
	public static class GasCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
            icon.hardlight(0.314f, 1.0f, 0.376f);
        }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
	};
	public static class HandSlipVulnerability extends Buff {};
	public static class LethalMomentumTracker extends FlavourBuff{};
	public static class StrikingWaveTracker extends FlavourBuff{};
	public static class WandPreservationCounter extends CounterBuff{{revivePersists = true;}};
	public static class EmpoweredStrikeTracker extends FlavourBuff{
		//blast wave on-hit doesn't resolve instantly, so we delay detaching for it
		public boolean delayedDetach = false;
	};
	public static class ProtectiveShadowsTracker extends Buff {
		float barrierInc = 0.5f;

		@Override
		public boolean act() {
			//barrier every 2/1 turns, to a max of 3/5
			if (((Hero)target).hasTalent(Talent.PROTECTIVE_SHADOWS) && target.invisible > 0){
				Barrier barrier = Buff.affect(target, Barrier.class);
				if (barrier.shielding() < 1 + 2*((Hero)target).pointsInTalent(Talent.PROTECTIVE_SHADOWS)) {
					barrierInc += 0.5f * ((Hero) target).pointsInTalent(Talent.PROTECTIVE_SHADOWS);
				}
				if (barrierInc >= 1){
					barrierInc = 0;
					barrier.incShield(1);
				} else {
					barrier.incShield(0); //resets barrier decay
				}
			} else {
				detach();
			}
			spend( TICK );
			return true;
		}

		private static final String BARRIER_INC = "barrier_inc";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( BARRIER_INC, barrierInc);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			barrierInc = bundle.getFloat( BARRIER_INC );
		}
	}
	public static class BountyHunterTracker extends FlavourBuff{};
	public static class RejuvenatingStepsCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.35f, 0.15f); }
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / (15 - 5*Dungeon.hero.pointsInTalent(REJUVENATING_STEPS)), 1); }
	};
	public static class RejuvenatingStepsFurrow extends CounterBuff{{revivePersists = true;}};
	public static class NaturalChildFurrowCounter extends CounterBuff {
		{
			revivePersists = true;
		}

		public static int maxUses(int talentPoints) {
			return 100 * Math.min(2, Math.max(0, talentPoints));
		}

		public boolean canUse(int talentPoints) {
			return maxUses(talentPoints) > 0 && count() < maxUses(talentPoints);
		}

		public void consumeUse(int talentPoints) {
			if (canUse(talentPoints)) {
				countUp(1);
			}
		}

		public void recoverFromExperience(float percent, int talentPoints) {
			if (percent > 0 && maxUses(talentPoints) > 0) {
				countDown(percent * maxUses(talentPoints));
			}
		}
	};
	public static class SeerShotCooldown extends FlavourBuff{
		public int icon() { return target.buff(RevealedArea.class) != null ? BuffIndicator.NONE : BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.7f, 0.4f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class SpiritBladesTracker extends FlavourBuff{};
	public static class PatientStrikeTracker extends Buff {
		public int pos;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		@Override
		public boolean act() {
			if (pos != target.pos) {
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}
		private static final String POS = "pos";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
		}
	};
	public static class AggressiveBarrierCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};
	public static class LiquidAgilEVATracker extends FlavourBuff{
		{
			//detaches after hero acts, not after mobs act
			actPriority = HERO_PRIO+1;
		}
	};
	public static class LiquidAgilACCTracker extends FlavourBuff{
		public int uses;

		{ type = buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }

		private static final String USES = "uses";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(USES, uses);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			uses = bundle.getInt(USES);
		}
	};
	public static class LethalHasteCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 100); }
	};
    public static class ArcaneShieldCooldown extends FlavourBuff{
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0.537f, 0.0f, 1.0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
    };
	public static class SwiftEquipCooldown extends FlavourBuff{
		public boolean secondUse;
		public boolean hasSecondUse(){
			return secondUse;
		}

		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
			if (hasSecondUse()) icon.hardlight(0.85f, 0f, 1.0f);
			else                icon.hardlight(0.35f, 0f, 0.7f);
		}
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / 20f, 1); }

		private static final String SECOND_USE = "second_use";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(SECOND_USE, secondUse);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			secondUse = bundle.getBoolean(SECOND_USE);
		}
	};
	public static class DeadlyFollowupTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	}
	public static class PreciseAssaultTracker extends FlavourBuff{
		{ type = buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0.0f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	};
	public static class VariedChargeTracker extends Buff{
		public Class weapon;

		private static final String WEAPON    = "weapon";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WEAPON, weapon);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			weapon = bundle.getClass(WEAPON);
		}
	}
	public static class CombinedLethalityAbilityTracker extends FlavourBuff{
		public MeleeWeapon weapon;
	};
	public static class CombinedEnergyAbilityTracker extends FlavourBuff{
		public boolean monkAbilused = false;
		public boolean wepAbilUsed = false;

		private static final String MONK_ABIL_USED  = "monk_abil_used";
		private static final String WEP_ABIL_USED   = "wep_abil_used";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MONK_ABIL_USED, monkAbilused);
			bundle.put(WEP_ABIL_USED, wepAbilUsed);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			monkAbilused = bundle.getBoolean(MONK_ABIL_USED);
			wepAbilUsed = bundle.getBoolean(WEP_ABIL_USED);
		}
	}
	public static class CounterAbilityTacker extends FlavourBuff{}
	public static class SatiatedSpellsTracker extends Buff{
		@Override
		public int icon() {
			return BuffIndicator.SPELL_FOOD;
		}
	}
	//used for metamorphed searing light
	public static class SearingLightCooldown extends FlavourBuff{
		@Override
		public int icon() {
			return BuffIndicator.TIME;
		}
		public void tintIcon(Image icon) { icon.hardlight(0f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	}

	final int icon;
	final int maxPoints;
	final int tier;
	final TalentType type;

	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21, 31};

	Talent(int icon, int maxPoints, int tier, TalentType type){
		this.icon = icon;
		this.maxPoints = maxPoints;
		this.tier = tier;
		this.type = type;
	}

	public int icon(){
		if (this == HEROIC_ENERGY){
			if (Ratmogrify.useRatroicEnergy){
				return 218;
			}
			HeroClass cls = Dungeon.hero != null ? Dungeon.hero.heroClass : GamesInProgress.selectedClass;
			if (cls == null) {
				cls = HeroClass.WARRIOR;
			}
			switch (cls){
				case WARRIOR: default:
					return 26;
				case MAGE:
					return 58;
				case ROGUE:
					return 90;
				case HUNTRESS:
					return 122;
				case DUELIST:
					return 154;
				case CLERIC:
					return 378;
				case SLIMEGIRL:
					return 442;
				case NINJA:
					return 474;
				case DM400:
					return 506;
				case FREEMAN:
					return 510;
				case PRINCESS:
					return 570;
                case FRIAR:
                    return 602;
			}
		} else {
			return icon;
		}
	}

	public int maxPoints(){
		return maxPoints;
	}

	public int tier() {
		return tier;
	}

	public TalentType type() {
		return type;
	}

	public static int falconEyePiercingDistance(int points, int adjustedViewDistance, boolean sighted) {
		if (!sighted || points <= 0 || adjustedViewDistance <= 0) {
			return 0;
		}
		if (points >= 2) {
			return Math.min(adjustedViewDistance,12);
		}
		return Math.min(Math.max(1, Math.round(adjustedViewDistance * 0.75f)),12);
	}

	public boolean isCommonTalentType() {
		return type.isCommon();
	}

	public String typeLabel() {
		if (type == TalentType.NEGATIVE) {
			return Messages.get(Talent.class, "type",
					Messages.get(ScrollOfMetamorphosis.class, negativeTypeMessageKey()));
		}
		return Messages.get(Talent.class, "type", Messages.get(ScrollOfMetamorphosis.class, type.messageKey()));
	}

	public int negativeTalentGroup() {
		for (int i = 0; i < negativeTalent.size(); i++) {
			if (negativeTalent.get(i).contains(this)) {
				return i;
			}
		}
		return -1;
	}

	private String negativeTypeMessageKey() {
		int group = negativeTalentGroup();
		if (group <= 0) {
			return "negative_common";
		}
		return "negative_limited_" + group;
	}

	public static boolean isBossTalentPlaceholder(Talent talent){
		return talent == BOSS_TALENT_SLOT_1
				|| talent == BOSS_TALENT_SLOT_2
				|| talent == BOSS_TALENT_SLOT_3
				|| talent == BOSS_TALENT_SLOT_4
				|| talent == BOSS_TALENT_SLOT_5;
	}

	public static boolean isBossTalent(Talent talent){
		switch (talent){
			case AQUATIC_RECOVER: case PUMP_ATTACK: case OOZE_ATTACK:
			case STRONGEST_SHIELD: case COMBO_PACKAGE: case BREAK_ENEMY_RANKS:
			case SURPRISE_THROW: case SMOKE_MASK: case RUSH:
			case SHADOW_KILLER: case KILL_SPREE: case SEAOFPEOPLE: case PHANTOM_STEP:
			case FASTING: case THUNDER_STRIKE: case DIRECTIONAL_COLLAPSE: case HUNTING_TECHNIQUE: case NATURAL_CHILD: case FALCON_EYE:
			case KING_PROTECT: case SUMMON_FOLLOWER: case WOLFISH_GAZE: case ENERGY_CONVERSION:
			case YOG_LARVA: case YOG_FIST: case YOG_RAY:
				return true;
			default:
				return false;
		}
	}

	public static boolean excludedFromMetamorphosis(Talent talent){
		return talent == null
				|| forbiddenInCatalogOrMetamorphosis(talent)
				|| talent.type == TalentType.BOSS
				|| talent.type == TalentType.NEGATIVE
				|| talent.type == TalentType.SUBCLASS
				|| talent.type == TalentType.ARMOR;
	}

	public static boolean excludedAsMetamorphSource(Talent talent){
		return talent == null
				|| talent.type == TalentType.BOSS
				|| talent.type == TalentType.NEGATIVE
				|| talent.type == TalentType.SUBCLASS
				|| talent.type == TalentType.ARMOR;
	}

	/**
	 * Returns the common talents the hero can currently replace. This must use the
	 * stored layout rather than reconstructing the hero's default class tree, as a
	 * new-cycle hero can inherit off-class talents.
	 */
	public static ArrayList<LinkedHashMap<Talent, Integer>> metamorphSources(Hero hero) {
		ArrayList<LinkedHashMap<Talent, Integer>> sources = new ArrayList<>();
		for (int i = 0; i < MAX_TALENT_TIERS; i++) {
			LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
			if (hero != null && hero.talents != null && i < hero.talents.size()) {
				for (Talent talent : hero.talents.get(i).keySet()) {
					if (!excludedAsMetamorphSource(talent)) {
						tier.put(talent, hero.pointsInTalent(talent));
					}
				}
			}
			sources.add(tier);
		}
		return sources;
	}

	public static boolean forbiddenInCatalogOrMetamorphosis(Talent talent) {
		switch (talent) {
			case POTENTIAL_1:
			case POTENTIAL_2:
			case POTENTIAL_3:
			case POTENTIAL_4:
			case POTENTIAL_5:
			case POTENTIAL_6:
			case POTENTIAL_7:
			case POTENTIAL_8:
			case POTENTIAL_9:
			case ROYAL_PRIVILEGE:
			case ROYAL_INTUITION:
			case KINGS_WISDOM:
			case NOBLE_CAUSE:
			case RK_ROYAL_MEAL:
			case RESTORATION:
			case POWER_WITHIN:
			case KINGS_VISION:
			case PURSUIT:
			case RK_BERSERKER:
			case RK_GLADIATOR:
			case RK_BATTLEMAGE:
			case RK_WARLOCK:
			case RK_ASSASSIN:
			case RK_FREERUNNER:
			case RK_SNIPER:
			case RK_WARDEN:
			case RK_CHAMPION:
			case RK_MONK:
			case RK_PRIEST:
			case RK_PALADIN:
			case RK_WATERSLIME:
			case RK_DARKSLIME:
			case RK_TATTEKI_NINJA:
			case RK_NINJA_MASTER:
			case RK_AT400:
			case RK_AU400:
			case RK_RUNEMAGE:
			case RK_COMBATMASTER:
				return true;
			default:
				return false;
		}
	}

	public static Talent bossTalentSlot(String type){
		switch (type){
			case "GOO": case "WARRIOR":
				return BOSS_TALENT_SLOT_1;
			case "TENGU": case "ROGUE":
				return BOSS_TALENT_SLOT_2;
			case "DM300": case "HUNTRESS":
				return BOSS_TALENT_SLOT_3;
			case "DWARFKING":
				return BOSS_TALENT_SLOT_4;
			case "YOG":
				return BOSS_TALENT_SLOT_5;
			default:
				return BOSS_TALENT_SLOT_1;
		}
	}

	public static Talent bossTalentSlot(Talent talent){
		switch (talent){
			case AQUATIC_RECOVER: case PUMP_ATTACK: case OOZE_ATTACK:
			case STRONGEST_SHIELD: case COMBO_PACKAGE: case BREAK_ENEMY_RANKS:
				return BOSS_TALENT_SLOT_1;
			case SURPRISE_THROW: case SMOKE_MASK: case RUSH:
			case SHADOW_KILLER: case KILL_SPREE: case SEAOFPEOPLE: case PHANTOM_STEP:
				return BOSS_TALENT_SLOT_2;
			case FASTING: case THUNDER_STRIKE: case DIRECTIONAL_COLLAPSE: case HUNTING_TECHNIQUE: case NATURAL_CHILD: case FALCON_EYE:
				return BOSS_TALENT_SLOT_3;
			case KING_PROTECT: case SUMMON_FOLLOWER: case WOLFISH_GAZE: case ENERGY_CONVERSION:
				return BOSS_TALENT_SLOT_4;
			case YOG_LARVA: case YOG_FIST: case YOG_RAY:
				return BOSS_TALENT_SLOT_5;
			default:
				return null;
		}
	}

	public static Talent bossTalentForSlot(Talent slot, LinkedHashMap<Talent, String> sublimation){
		if (sublimation.containsKey(slot)){
			try {
				return Talent.valueOf(sublimation.get(slot));
			} catch (Exception ignored) {
				return slot;
			}
		}
		for (Talent talent : sublimation.keySet()){
			if (isBossTalent(talent) && bossTalentSlot(sublimation.get(talent)) == slot){
				return talent;
			}
		}
		return slot;
	}

	public String title(){
		if (this == HEROIC_ENERGY && Ratmogrify.useRatroicEnergy){
			return Messages.get(this, name() + ".rat_title");
		}
		return Messages.get(this, name() + ".title");
	}

	public final String desc(){
		return desc(false);
	}

	public String desc(boolean metamorphed){
		String desc = Messages.get(this, name() + ".desc");
		if (metamorphed){
			String metaDesc = Messages.get(this, name() + ".meta_desc");
			if (Messages.canget(this.getClass(), name() + ".meta_desc")){
				desc += "\n\n" + metaDesc;
			}
		}
		desc += "\n\n" + typeLabel();
		String comment = Messages.get(this, name() + ".comment");
		return !Messages.canget(this.getClass(), name() + ".comment") || comment.isEmpty() ? desc : desc + "\n\n" + comment;
	}


	public static List<Talent> talentsByTierAndType(int tier, TalentType type) {
		ArrayList<Talent> talents = new ArrayList<>();
		for (Talent talent : values()) {
			if (talent.tier == tier && talent.type == type) {
				talents.add(talent);
			}
		}
		return talents;
	}

	public static List<Talent> talentsByTier(int tier, EnumSet<TalentType> allowedTypes) {
		ArrayList<Talent> talents = new ArrayList<>();
		for (Talent talent : values()) {
			if (talent.tier == tier && allowedTypes.contains(talent.type)) {
				talents.add(talent);
			}
		}
		return talents;
	}

	public static List<Talent> commonTalentsByTier(int tier) {
		return talentsByTier(tier, EnumSet.of(
				TalentType.ATTACK,
				TalentType.MAGIC,
				TalentType.EFFECT,
				TalentType.RESOURCE,
				TalentType.SPELL,
				TalentType.ASSIST,
				TalentType.OTHER
		));
	}

	private static void addMetamorphCandidates(ArrayList<Talent> pool, int tier, TalentType type, HashSet<Talent> excludedTalents) {
		for (Talent talent : talentsByTierAndType(tier, type)) {
			if (!excludedTalents.contains(talent) && !excludedFromMetamorphosis(talent)) {
				pool.add(talent);
			}
		}
	}

	public static ArrayList<Talent> metamorphCandidatePool(int tier, TalentType preferredType, HashSet<Talent> excludedTalents, int preferredWeight) {
		ArrayList<Talent> pool = new ArrayList<>();
		boolean strictPreferred = preferredType == TalentType.SPELL;
		if (!strictPreferred) {
			for (TalentType type : COMMON_TYPES) {
				addMetamorphCandidates(pool, tier, type, excludedTalents);
			}
		}
		int extraCopies = strictPreferred ? Math.max(1, preferredWeight) : Math.max(0, preferredWeight - 1);
		for (int i = 0; i < extraCopies; i++) {
			addMetamorphCandidates(pool, tier, preferredType, excludedTalents);
		}
		return pool;
	}
	public static void onTalentUpgradedItem( Hero hero, Talent talent ){
		int max_item=26;
		if(hero.heroClass==HeroClass.FREEMAN){
			max_item+=13;
		}
		if(talent==SHOCK_BOMB && hero.hasTalent(SHOCK_BOMB)) {
			Dungeon.level.drop(new Bomb(), hero.pos).sprite.drop();
		}

		if(talent==BOMB_MANIAC && hero.hasTalent(BOMB_MANIAC)) {
			Dungeon.level.drop(new Bomb(), hero.pos).sprite.drop();
		}
        if(talent==EXP_SOLID && hero.hasTalent(EXP_SOLID)) {

            hero.upLevel(-hero.pointsInTalent(EXP_SOLID));

        }
		if(Dungeon.talent_item==max_item){
			Dungeon.talent_item+=1;
			GLog.w("超出资源限制，无法通过升级天赋获取资源。");
			if(talent == USURY && !hero.hasTalent(USURY)){
				GLog.w(Messages.get(USURY,USURY.name()+".nomoney"));
			}
			return ;
		}else if(Dungeon.talent_item>max_item){
			return ;
		}
        if(talent==EXP_SOLID && hero.hasTalent(EXP_SOLID)) {
            Dungeon.talent_item+=1;
            Dungeon.level.drop(new PotionOfExperience().quantity(hero.pointsInTalent(EXP_SOLID)), hero.pos).sprite.drop();
        }
		if(talent == USURY && !hero.hasTalent(USURY)){
			GLog.p(Messages.get(USURY,USURY.name()+".principal"));
			Dungeon.level.drop(new Gold().quantity(1000), hero.pos).sprite.drop();
		}

		if(talent==POTENTIAL_1 && hero.pointsInTalent(POTENTIAL_1)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_2 && hero.pointsInTalent(POTENTIAL_2)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_3 && hero.pointsInTalent(POTENTIAL_3)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_4 && hero.pointsInTalent(POTENTIAL_4)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_5 && hero.pointsInTalent(POTENTIAL_5)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_6 && (hero.pointsInTalent(POTENTIAL_6)==1 || hero.pointsInTalent(POTENTIAL_6)==2)){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_7 && (hero.pointsInTalent(POTENTIAL_7)==1 || hero.pointsInTalent(POTENTIAL_7)==2)){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_8 && (hero.pointsInTalent(POTENTIAL_8)==1 || hero.pointsInTalent(POTENTIAL_8)==2)){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==POTENTIAL_9 && (hero.pointsInTalent(POTENTIAL_9)==1 || hero.pointsInTalent(POTENTIAL_9)==2)){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==SECRET_STASH){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new Gold().quantity(1000),hero.pos).sprite.drop(hero.pos);
		}
		if(hero.hasTalent(WEAPON_MAKE) && talent==WEAPON_MAKE){
			Dungeon.talent_item+=1;
			Weapon w = Generator.randomWeapon(2+hero.pointsInTalent(WEAPON_MAKE));
			w.identify();
			w.masteryPotionBonus = true;
			Dungeon.level.drop(w,hero.pos).sprite.drop(hero.pos);
		}
		if(talent==NOVICE_BENEFITS && hero.pointsInTalent(NOVICE_BENEFITS)>0){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==DOUBLE_TRINKETS && hero.pointsInTalent(DOUBLE_TRINKETS)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new TrinketCatalyst(),hero.pos).sprite.drop();
		}
		if(hero.hasTalent(ILLUSION_FEED)&& talent==ILLUSION_FEED){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new Pasty(), hero.pos).sprite.drop();
		}
		if(talent==PYROMANIAC){
			Dungeon.talent_item+=1;
			if(hero.pointsInTalent(PYROMANIAC)==3){
				Dungeon.level.drop(new ElixirOfDragonsBlood(), hero.pos).sprite.drop();
			}else if(hero.pointsInTalent(PYROMANIAC)==2){
				Dungeon.level.drop(new InfernalBrew().quantity(2), hero.pos).sprite.drop();
			}else if(hero.pointsInTalent(PYROMANIAC)==1){
				Dungeon.level.drop(new PotionOfDragonsBreath().quantity(3), hero.pos).sprite.drop();
			}
		}
		if(talent==WULEI_ZHENGFA && hero.pointsInTalent(WULEI_ZHENGFA)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new WandOfLightning().identify(),hero.pos).sprite.drop(hero.pos);
		}
		if(talent==ERODING_SOUL && hero.pointsInTalent(ERODING_SOUL)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new WandOfCorruption().identify(),hero.pos).sprite.drop(hero.pos);
		}
		if(talent==COLLECT_PLANTS){
			Dungeon.talent_item+=1;
			for(int i=0;i<hero.pointsInTalent(COLLECT_PLANTS)+2;i++){
				Dungeon.level.drop(Generator.random(Generator.Category.SEED),hero.pos).sprite.drop(hero.pos);
			}
			if(hero.pointsInTalent(COLLECT_PLANTS)==3){
				Dungeon.level.drop(new Blandfruit(),hero.pos).sprite.drop(hero.pos);
			}
		}
		if(talent==PERSONAL_ATTACK && hero.hasTalent(PERSONAL_ATTACK)) {
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfRage(), hero.pos).sprite.drop();
		}

        if(talent==FLASH_GENIUS && hero.hasTalent(FLASH_GENIUS)) {
            Dungeon.talent_item+=1;
            Dungeon.level.drop(new StoneOfIntuition(), hero.pos).sprite.drop();
        }

		if(talent==LIGHT_SHOCK && hero.pointsInTalent(LIGHT_SHOCK)==1){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new WandOfPrismaticLight().identify(),hero.pos).sprite.drop(hero.pos);
		}
		if(talent==MIDAS_TOUCH && hero.hasTalent(MIDAS_TOUCH)){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new LiquidMetal().quantity(20 * hero.pointsInTalent(MIDAS_TOUCH)), hero.pos).sprite.drop(hero.pos);
		}
	}

	public static void onTalentUpgraded( Hero hero, Talent talent ){

		int max_item=15;
		if(hero.heroClass==HeroClass.FREEMAN){
			max_item+=13;
		}
		if (talent != null) {
			onTalentUpgradedItem(hero, talent);
		}

		hero.updateHT(true);
		MeleeWeapon.syncCharger(hero);
		updateQuickslot();
		//Dungeon.quickslot.reset();
		//for metamorphosis
		if (talent == IRON_WILL && hero.heroClass != HeroClass.WARRIOR){
			Buff.affect(hero, BrokenSeal.WarriorShield.class);
		}
		if (talent == SMOKE_MASK && hero.buff(SmokeCooldown.class)==null && hero.pointsInTalent(SMOKE_MASK) >0){

			Buff.affect(hero, SmokeMask.class);
			ActionIndicator1.setAction( hero.buff(SmokeMask.class) );
			BuffIndicator.refreshHero();
		}
		if (talent == NATURAL_CHILD && hero.buff(NaturalChildCooldown.class) == null
				&& hero.pointsInTalent(NATURAL_CHILD) > 0) {
			Buff.affect(hero, NaturalChildAction.class);
			ActionIndicator.setAction(hero.buff(NaturalChildAction.class));
			BuffIndicator.refreshHero();
		}
		if (!hero.hasTalent(NATURAL_CHILD)) {
			NaturalChildAction action = hero.buff(NaturalChildAction.class);
			if (action != null) action.detach();
			NaturalChildBarkskin barkskin = hero.buff(NaturalChildBarkskin.class);
			if (barkskin != null) barkskin.detach();
			NaturalChildCooldown cooldown = hero.buff(NaturalChildCooldown.class);
			if (cooldown != null) cooldown.detach();
			NaturalChildFurrowCounter counter = hero.buff(NaturalChildFurrowCounter.class);
			if (counter != null) counter.detach();
		}
		if (!hero.hasTalent(SMOKE_MASK) && hero.buff(SmokeMask.class) != null) {
			hero.buff(SmokeMask.class).detach();
		}
		if (talent == null) {
			BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
			if (shield != null && shield.maxShield() <= 0) {
				shield.detach();
			}
			if (!hero.hasTalent(AQUATIC_RECOVER) && hero.buff(AquaticRecover.class) != null) {
				hero.buff(AquaticRecover.class).detach();
			}
			BuffIndicator.refreshHero();
		}

		if (talent == VETERANS_INTUITION && hero.pointsInTalent(VETERANS_INTUITION) == 2){
			if (hero.belongings.armor() != null && !ShardOfOblivion.passiveIDDisabled())  {
				hero.belongings.armor.identify();
			}
		}
		if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (hero.belongings.ring instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) {
				hero.belongings.ring.identify();
			}
			if (hero.belongings.misc instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) {
				hero.belongings.misc.identify();
			}
			for (Item item : Dungeon.hero.belongings){
				if (item instanceof Ring){
					((Ring) item).setKnown();
				}
			}
		}
		if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 1){
			if (hero.belongings.ring instanceof Ring) hero.belongings.ring.setKnown();
			if (hero.belongings.misc instanceof Ring) ((Ring) hero.belongings.misc).setKnown();
		}
		if (talent == ADVENTURERS_INTUITION && hero.pointsInTalent(ADVENTURERS_INTUITION) == 2){
			if (hero.belongings.weapon() != null && !ShardOfOblivion.passiveIDDisabled()){
				hero.belongings.weapon().identify();
			}
		}

		if (talent == PROTECTIVE_SHADOWS)
			if (hero.invisible > 0) {
				Buff.affect(hero, ProtectiveShadowsTracker.class);
			}
		if (talent == AQUATIC_RECOVER && Dungeon.level.water[hero.pos]){
			Buff.affect(hero, Talent.AquaticRecover.class);
		}

		if (talent == LIGHT_CLOAK && hero.heroClass == HeroClass.ROGUE){
			for (Item item : Dungeon.hero.belongings.backpack){
				if (item instanceof CloakOfShadows){
					if (!hero.belongings.lostInventory() || item.keptThroughLostInventory()) {
						((CloakOfShadows) item).activate(Dungeon.hero);
					}
				}
			}
		}
		if (talent == LIGHT_BOX && hero.heroClass == HeroClass.NINJA){
			for (Item item : Dungeon.hero.belongings.backpack){
				if (item instanceof Shuriken_Box){
					if (!hero.belongings.lostInventory() || item.keptThroughLostInventory()) {
						((Shuriken_Box) item).activate(Dungeon.hero);
					}
				}
			}
		}
		if (talent == QUICK_TOOL && hero.heroClass == HeroClass.DM400){
			for (Item item : Dungeon.hero.belongings.backpack){
				if (item instanceof InstructionTool){
					if (!hero.belongings.lostInventory() || item.keptThroughLostInventory()) {
						((InstructionTool) item).activate(Dungeon.hero);
					}
				}
			}
		}
		if (talent == TRAP_MASTER){
			new ReclaimTrap().collect();
		}
		if ((talent == HEIGHTENED_SENSES || talent == FARSIGHT || talent==SHORTSIGHTED
				|| talent == null) && Dungeon.level != null){
			Dungeon.observe();
		}


		if (talent == TWIN_UPGRADES || talent == DESPERATE_POWER || talent == STRONGMAN){
			updateQuickslot();
		}

		if (talent == UNENCUMBERED_SPIRIT && hero.pointsInTalent(talent) == 3){
			Item toGive = new ClothArmor().identify();
			if (!toGive.collect()){
				Dungeon.level.drop(toGive, hero.pos).sprite.drop();
			}
			toGive = new Gloves().identify();
			if (!toGive.collect()){
				Dungeon.level.drop(toGive, hero.pos).sprite.drop();
			}
		}
		if(talent==REVERSE_POLARITY){
			if(hero.pointsInTalent(REVERSE_POLARITY)==3){
				hero.HP=hero.HT;
			}else if(hero.pointsInTalent(REVERSE_POLARITY)==2){
				hero.HP=hero.HT/2;
			}else if(hero.pointsInTalent(REVERSE_POLARITY)==1){
				hero.HP=1;
			}
		}

		if (talent == LIGHT_READING){
			for (Item item : Dungeon.hero.belongings.backpack){
				if (item instanceof HolyTome){
					if (!hero.belongings.lostInventory() || item.keptThroughLostInventory()) {
						((HolyTome) item).activate(Dungeon.hero);
					}
				}
			}
		}

		if(talent == SWIFT_CHURCH && hero.pointsInTalent(SWIFT_CHURCH)==1){
			Item toGive = new WandOfRegrowth().identify();
            Dungeon.level.drop(toGive, hero.pos).sprite.drop();
		}
		if(talent ==TBM && hero.buff(TBMCooldown.class)==null){
			Buff.affect(hero,Barrier.class).incShield(1+2*hero.pointsInTalent(TBM));
			Buff.affect(hero,TBMCooldown.class,60);
		}
        if(talent == MORE_RING){
            Ring ring = hero.belongings.getItem(RingOfKing.class);
            if(ring!=null && ring.isEquipped(hero)){
                if(hero.belongings.artifact==null){
                    hero.belongings.artifact = ring;
                    if(hero.belongings.ring==ring){
                        hero.belongings.ring = null;
                    }else if(hero.belongings.misc==ring){
                        hero.belongings.misc = null;
                    }
                }
            }
        }

		if(hero.pointsInTalent(MORE_RING)<1 && hero.belongings.artifact!=null && hero.belongings.artifact instanceof RingOfKing){
            hero.belongings.artifact.doUnequip(hero,true,true);
        }else if(hero.pointsInTalent(MORE_RING)<2 && hero.belongings.artifact!=null && hero.belongings.artifact instanceof Ring && !(hero.belongings.artifact instanceof RingOfKing)){
			hero.belongings.artifact.doUnequip(hero,true,true);
		}



		if(hero.hasTalent(MARTIAL_TRAIN) && hero.belongings.weapon!=null){
			MeleeWeapon.syncCharger(hero);
			hero.belongings.weapon.activate(hero);
		}
		Item.updateQuickslot();



	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}};
	public static class NatureBerriesDropped extends CounterBuff{{revivePersists = true;}};

	private static boolean resolvingDelayedSatisfaction;

	public static class DelayedSatisfaction extends FlavourBuff {

		private static final float DURATION = 5f;

		private float foodVal;
		public boolean hornOfPlenty;

		{
			type = buffType.POSITIVE;
		}



		@Override
		public int icon() {
			return BuffIndicator.DELAYEDSATISFACTION;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, visualcooldown() / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)Math.ceil(visualcooldown()));
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(visualcooldown()));
		}

		@Override
		public void detach() {

            if(hornOfPlenty){
                onFoodEaten((Hero)target, foodVal, new HornOfPlenty());
            }else{
                onFoodEaten((Hero)target, foodVal, null);
            }

            super.detach();

		}

		private static final String FOOD_VAL = "food_val";
		private static final String HORN_OF_PLENTY = "horn_of_plenty";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put( FOOD_VAL, foodVal );
			bundle.put( HORN_OF_PLENTY, hornOfPlenty );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle(bundle);
			foodVal = bundle.getFloat( FOOD_VAL );
			hornOfPlenty = bundle.getBoolean( HORN_OF_PLENTY );
		}
	}

	public static void onFoodEaten( Hero hero, float foodVal, Item foodSource ){
		if (hero.buff(DelayedSatisfaction.class)==null
				&& foodSource instanceof HornOfPlenty
				&& hero.hasTalent(BOUNTIFUL_ENHANCEMENT)) {
            Buff.affect(hero, DelayedSatisfaction.class,5).hornOfPlenty=true;
		}
		int maxeat = 100;
		int oldeat = Dungeon.eat_item;
		if (hero.HP / (float) hero.HT <= 0.33f && hero.pointsInTalent(HEARTY_MEAL)>0) {
			int healing = 2 + 2 * (hero.pointsInTalent(HEARTY_MEAL));
			hero.heal(healing);
		}

		if(hero.hasTalent(NINJA_MEAL)){
			Shuriken_Box box = hero.belongings.getItem(Shuriken_Box.class);
			if(box != null){
				box.directCharge(hero.pointsInTalent(NINJA_MEAL));
				ScrollOfRecharging.charge(hero);
			}else{
				Buff.affect( hero, ArtifactRecharge.class).set(hero.pointsInTalent(NINJA_MEAL)).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
			}
		}

		if (hero.hasTalent(ENLIGHTENING_MEAL)){
			HolyTome tome = hero.belongings.getItem(HolyTome.class);
			if (tome != null) {
				tome.directCharge( 0.34f + 0.33f * (hero.pointsInTalent(ENLIGHTENING_MEAL)));
				ScrollOfRecharging.charge(hero);
			}else {
				//2/3 turns of recharging
				ArtifactRecharge buff = Buff.affect( hero, ArtifactRecharge.class);
				if (buff.left() < 1 + (hero.pointsInTalent(ENLIGHTENING_MEAL))){
					Buff.affect( hero, ArtifactRecharge.class).set(1 + (hero.pointsInTalent(ENLIGHTENING_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
				}
				Buff.prolong( hero, Recharging.class, 1 + (hero.pointsInTalent(ENLIGHTENING_MEAL)) );
				ScrollOfRecharging.charge( hero );
				SpellSprite.show(hero, SpellSprite.CHARGE);
			}
		}
		if(hero.hasTalent(DELICIOUS_FLYING)){
			Buff.affect(hero, Levitation.class,5+10*hero.pointsInTalent(DELICIOUS_FLYING));
		}
		if (hero.hasTalent(IRON_STOMACH)){
			if (hero.cooldown() > 0) {
				Buff.affect(hero, WarriorFoodImmunity.class, hero.cooldown());
			}
		}
		if(hero.hasTalent(TREAT_MEAL)){
			Buff.affect(hero, Healing.class).setHeal(1+2*hero.pointsInTalent(TREAT_MEAL),0.25f,0);
		}
		if (hero.hasTalent(EMPOWERING_MEAL)){
			//2/3 bonus wand damage for next 3 zaps
			Buff.affect( hero, WandEmpower.class).set(1 + hero.pointsInTalent(EMPOWERING_MEAL), 3);
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(ENERGIZING_MEAL)){
			//5/8 turns of recharging
			int buffed_times = 2 + 3*(hero.pointsInTalent(ENERGIZING_MEAL));
			Buff.prolong( hero, Recharging.class, buffed_times );
			ScrollOfRecharging.charge( hero );
			SpellSprite.show(hero, SpellSprite.CHARGE);
		}
		if (hero.hasTalent(MYSTICAL_MEAL)){
			//3/5 turns of recharging
			ArtifactRecharge buff = Buff.affect( hero, ArtifactRecharge.class);
			if (buff.left() < 1 + 2*(hero.pointsInTalent(MYSTICAL_MEAL))){
				Buff.affect( hero, ArtifactRecharge.class).set(1 + 2*(hero.pointsInTalent(MYSTICAL_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
			}
			ScrollOfRecharging.charge( hero );
			SpellSprite.show(hero, SpellSprite.CHARGE, 0, 1, 1);
		}
		if (hero.hasTalent(INVIGORATING_MEAL)){
			//effectively 1/2 turns of haste
			Buff.prolong( hero, Haste.class, 0.67f+hero.pointsInTalent(INVIGORATING_MEAL));
		}
		if (hero.hasTalent(STRENGTHENING_MEAL)){
			//3 bonus physical damage for next 2/3 attacks
			Buff.affect( hero, PhysicalEmpower.class).set(3, 1 + hero.pointsInTalent(STRENGTHENING_MEAL));
		}
		if (hero.hasTalent(FOCUSED_MEAL)){
			if (MeleeWeapon.canUseWeaponAbility(hero)){
				//0.67/1 charge for heroes who can use weapon abilities
				Buff.affect( hero, MeleeWeapon.Charger.class ).gainCharge((hero.pointsInTalent(FOCUSED_MEAL)+1)/3f);
				ScrollOfRecharging.charge( hero );
			} else {
				// lvl/3 / lvl/2 bonus dmg on next hit for other classes
				Buff.affect( hero, PhysicalEmpower.class).set(Math.round(hero.lvl / (4f - hero.pointsInTalent(FOCUSED_MEAL))), 1);
			}
		}
        if (hero.hasTalent(BLESS_MEAL)){
            Buff.affect( hero, Bless.class,hero.pointsInTalent(BLESS_MEAL)*10+15);
        }
		if (hero.hasTalent(MEAL_SHIELD)){
			Buff.affect( hero, Barrier.class).setShield(hero.pointsInTalent(MEAL_SHIELD)*4);
		}
		if(hero.hasTalent(NURTRITIOUS_MEAL)){
            hero.heal(hero.pointsInTalent(NURTRITIOUS_MEAL)*2);
			Buff.affect( hero, Barrier.class).setShield(hero.pointsInTalent(NURTRITIOUS_MEAL)*2);
		}
		if(hero.hasTalent(INVINCIBLE_MEAL) && Dungeon.eat_item<maxeat){
            if(hero.buff(Invulnerability.class)!=null){
                Buff.affect(hero, Invulnerability.class,hero.pointsInTalent(INVINCIBLE_MEAL));
            }else{
                Buff.affect(hero, Invulnerability.class,hero.pointsInTalent(INVINCIBLE_MEAL)-1);
            }
			Dungeon.eat_item++;
		}
        if(hero.hasTalent(YUNYING_MEAL) && Dungeon.eat_item<maxeat){
            Buff.affect(hero,Invisibility.class,hero.pointsInTalent(YUNYING_MEAL)*2);
            Dungeon.eat_item++;
        }
		if(hero.hasTalent(EARTH_MEAL)){
			Barkskin.conditionallyAppend(hero, (hero.lvl*hero.pointsInTalent(Talent.EARTH_MEAL))/3*2, 1 );
		}
		if(hero.hasTalent(GOLD_MEAL)){
			Dungeon.level.drop(new Gold().quantity(15+10*hero.pointsInTalent(GOLD_MEAL)), hero.pos).sprite.drop();

		}
		if(hero.hasTalent(ENDLESS_MEAL)){
			Buff.affect(hero, Swiftthistle.TimeBubble.class).reset1(2*hero.pointsInTalent(ENDLESS_MEAL));
		}
		if(hero.hasTalent(EXPERIENCE_MEAL) && Dungeon.eat_item<maxeat){
			int exp=1+hero.pointsInTalent(EXPERIENCE_MEAL);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(exp), FloatingText.EXPERIENCE);
			hero.earnExp(exp, null);
			Dungeon.eat_item++;
		}
		if(hero.pointsInTalent(BIRTHDAY_GIFT)>Random.Int(6) && Dungeon.eat_item<maxeat){
			Dungeon.level.drop(genLowValueConsumable(), hero.pos).sprite.drop();
			Dungeon.eat_item++;
		}
		if(hero.pointsNegative(EXPLOSION_MEAL)>Random.Int(10)){
			int explosionPos = hero.pos;
			new Bomb.ConjuredBomb().explode(explosionPos);
		}
		if(hero.hasTalent(HEALTHY_FOOD)){
			Buff.affect(hero, PotionOfCleansing.Cleanse.class,hero.pointsInTalent(HEALTHY_FOOD)+1);
		}
		if(hero.pointsNegative(VIP_MEAL)>0){
			Dungeon.gold-=(int)((0.1f*hero.pointsNegative(VIP_MEAL))*Dungeon.gold);
		}
		if (hero.hasTalent(SATIATED_SPELLS)){
			Buff.affect(hero, SatiatedSpellsTracker.class);
		}
		//史莱姆娘
		if (hero.hasTalent(RESILIENT_MEAL)){
			Buff.affect( hero, ResilientArmor.class).set(1 + hero.pointsInTalent(RESILIENT_MEAL)*2);
		}
		if (hero.hasTalent(TOUGH_MEAL)){
			int Healing = 0;
			if(hero.pointsInTalent(TOUGH_MEAL) == 1){
				Healing = (int)(0.1 * (hero.HT - hero.HP));
				Healing = Math.max(Healing, 3);
				Healing = Math.min(Healing, 15);
			}else{
				Healing = (int)(0.15 * (hero.HT - hero.HP));
				Healing = Math.max(Healing, 5);
				Healing = Math.min(Healing, 25);
			}

			if(hero.hasTalent(ORIGINAL_MONSTER)){
				Healing = (int)(Healing * 1.5);
			}

			if (hero.subClass.is(HeroSubClass.WARDEN)) {
				Buff.affect(hero, Healing.class).setHeal(Healing, 0, 1);
			} else {
				Buff.affect(hero, Sungrass.Health.class).boost(Healing);
			}
		}
		if(Dungeon.eat_item>=maxeat && eat_item>oldeat){
			GLog.i("受限进食天赋此后不再触发。");
		}
		/*
		if(hero.hasTalent(SMOKE_MASK) && hero.buffs(SmokeCooldown.class).isEmpty() && hero.buffs(SmokeMask.class).isEmpty()){
			Buff.affect(hero, SmokeMask.class);
			ActionIndicator1.setAction( hero.buff(SmokeMask.class) );
			BuffIndicator.refreshHero();
		}else if(hero.hasTalent(SMOKE_MASK) && hero.buff(SmokeMask.class)!=null){
			ActionIndicator1.setAction( hero.buff(SmokeMask.class) );
		}

		 */
		if(hero.hasTalent(Talent.ICE_MEAL)){
			Buff.affect(hero, FrostImbue.class,Math.min(4,2*hero.pointsInTalent(Talent.ICE_MEAL)));
			if(hero.pointsInTalent(Talent.ICE_MEAL)>2){
				for (int i : PathFinder.NEIGHBOURS8) {
					int c = hero.pos + i;
					Char ch = Actor.findChar(c);
					if(ch!=null){
						Buff.affect(ch, Frost.class, Frost.DURATION);
					}

				}
			}
		}
		if (hero.hasTalent(MARK_MEAL)){
			//2/3 bonus wand damage for next 3 zaps
			Buff.affect( hero, MarkMeal.class).left=3;

		}
        if (hero.hasTalent(ENCHANT_MEAL)){
            Buff.affect( hero, EnchantMeal.class).left=1+2*hero.pointsInTalent(ENCHANT_MEAL);
        }
        if(hero.hasTalent(ROYAL_MEAL)){
            Ring ring = hero.belongings.getItem(RingOfKing.class);
            if(ring!=null){
                Buff.affect( hero, RoyalMeal.class,10);
            }else{
                Buff.affect( hero, RoyalMeal2.class,5*hero.pointsInTalent(ROYAL_MEAL));
            }
        }
		if (hero.hasTalent(WHISPERING_MEAL)){
			float duration = hero.pointsInTalent(WHISPERING_MEAL) == 1 ? 2f : 4f;
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]){
					Buff.affect(mob, Terror.class, duration).object = hero.id();
				}
			}
		}

	}

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}


	public static float itemIDSpeedFactor( Hero hero, Item item ){
		// 1.75x/2.5x speed with Huntress talent
		float factor = 1f + 0.75f*hero.pointsInTalent(SURVIVALISTS_INTUITION);

		// Affected by both Warrior(1.75x/2.5x) and Duelist(2.5x/inst.) talents
		if (item instanceof MeleeWeapon){
			factor *= 1f + 1.5f*hero.pointsInTalent(ADVENTURERS_INTUITION); //instant at +2 (see onItemEquipped)
			factor *= 1f + 0.75f*hero.pointsInTalent(VETERANS_INTUITION);
		}
		// Affected by both Warrior(2.5x/inst.) and Duelist(1.75x/2.5x) talents
		if (item instanceof Armor){
			factor *= 1f + 0.75f*hero.pointsInTalent(ADVENTURERS_INTUITION);
			factor *= 1f + hero.pointsInTalent(VETERANS_INTUITION); //instant at +2 (see onItemEquipped)
		}
		// 3x/instant for Mage (see Wand.wandUsed())
		if (item instanceof Wand){
			factor *= 1f + 2.0f*hero.pointsInTalent(SCHOLARS_INTUITION);
		}
		// 3x/instant speed with Huntress talent (see MissileWeapon.proc)
		if (item instanceof MissileWeapon){
			factor *= 1f + 2.0f*hero.pointsInTalent(SURVIVALISTS_INTUITION);
		}
		// 2x/instant for Rogue (see onItemEqupped), also id's type on equip/on pickup
		if (item instanceof Ring){
			factor *= 1f + hero.pointsInTalent(THIEFS_INTUITION);
		}
		if (item instanceof MissileWeapon && !((item instanceof Shuriken_Box.SmallShuriken)|| (item instanceof Tatteki.Tamaru) || (item instanceof SpiritBow.SpiritArrow))){
			factor *= 1f + 2.0f*hero.pointsInTalent(HUNTING_INTUITION);
		}
        if (hero.pointsInTalent(MANA_SENSE)>0){
            if(item instanceof Weapon && ((Weapon) item).enchantment!=null){
                factor *=1.0f + 4.0f*hero.pointsInTalent(MANA_SENSE);
            }
            if(item instanceof Armor && ((Armor) item).glyph!=null){
                factor *=1.0f + 4.0f*hero.pointsInTalent(MANA_SENSE);
            }
        }
		return factor;
	}

	public static void onPotionUsed( Hero hero, int cell, float factor ){
		if(hero.hasTalent(WITCH_POTION)){
			Char mob = Actor.findChar(cell);
			if(mob !=null && !(mob instanceof  Hero)){
				Buff.affect( mob, Charm.class, 15 ).object = hero.id();
				if(hero.pointsInTalent(WITCH_POTION)==2){
					Buff.affect( mob, Poison.class).set(5);
				}
			}
		}
		if (hero.hasTalent(LIQUID_WILLPOWER)){
			int shieldToGive = Math.round( factor * hero.HT * (0.035f * (hero.pointsInTalent(LIQUID_WILLPOWER)) + 0.03f));
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
				Buff.affect(hero, Barrier.class).setShield(shieldToGive);
}
		if (hero.hasTalent(LIQUID_NATURE)){
			ArrayList<Integer> grassCells = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS9){
				grassCells.add(cell+i);
			}
			Random.shuffle(grassCells);
			for (int grassCell : grassCells){
				Char ch = Actor.findChar(grassCell);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY){
					//1/2 turns of roots
					Buff.affect(ch, Roots.class, factor * hero.pointsInTalent(LIQUID_NATURE));
				}
				if (Dungeon.level.map[grassCell] == Terrain.EMPTY ||
						Dungeon.level.map[grassCell] == Terrain.EMBERS ||
						Dungeon.level.map[grassCell] == Terrain.EMPTY_DECO){
					Level.set(grassCell, Terrain.GRASS);
					GameScene.updateMap(grassCell);
				}
				CellEmitter.get(grassCell).burst(LeafParticle.LEVEL_SPECIFIC, 4);
			}
			// 4/6 cells total
			int totalGrassCells = (int) (factor * (2 + 2 * hero.pointsInTalent(LIQUID_NATURE)));
			while (grassCells.size() > totalGrassCells){
				grassCells.remove(0);
			}
			for (int grassCell : grassCells){
				int t = Dungeon.level.map[grassCell];
				if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
						|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
						&& Dungeon.level.plants.get(grassCell) == null){
					Level.set(grassCell, Terrain.HIGH_GRASS);
					GameScene.updateMap(grassCell);
				}
			}
			Dungeon.observe();
		}
		if (hero.hasTalent(LIQUID_AGILITY)){
			Buff.prolong(hero, LiquidAgilEVATracker.class, hero.cooldown() + Math.max(0, factor-1));
			if (factor >= 0.5f){
				Buff.prolong(hero, LiquidAgilACCTracker.class, 5f).uses = Math.round(factor);
			}
		}

		if(hero.hasTalent(ANESTHESIA)){
			Buff.affect(hero, Barrier.class).setShield(2+3*hero.pointsInTalent(ANESTHESIA));
		}
		if(hero.hasTalent(WANLING_POTION)){
			Buff.affect(hero, PotionOfCleansing.Cleanse.class,hero.pointsInTalent(WANLING_POTION)+1.0f);
			if(hero.pointsInTalent(WANLING_POTION)==3){
				for (Buff b : hero.buffs()){
					if (b.type == Buff.buffType.NEGATIVE
							&& !(b instanceof AllyBuff)
							&& !(b instanceof LostInventory)){
						b.detach();
					}
				}
			}
		}
		if(hero.hasTalent(LIQUID_PERCEPTION) && factor==2){
			HashSet<Class<? extends Potion>> potions = Potion.getUnknown();
			HashSet<Class<? extends Scroll>> scrolls = Scroll.getUnknown();
			ArrayList<Item> IDed = new ArrayList<>();
			Potion p = Reflection.newInstance(Random.element(potions));
			if(hero.pointsInTalent(LIQUID_PERCEPTION)==2 && Random.Int(2)==1){
				Scroll s = Reflection.newInstance(Random.element(scrolls));
				if(s!=null){
					s.identify();
					IDed.add(s);
				}
				
			}else{
				if(p!=null){
					p.identify();
					IDed.add(p);
				}
				
			}
			GameScene.show(new WndDivination(IDed,LIQUID_PERCEPTION));
		}
	}

	public static void onScrollUsed( Hero hero, int pos, float factor, Class<?extends Item> cls ){
		int max_item=26;
		if(hero.heroClass==HeroClass.FREEMAN){
			max_item+=13;
		}
		if (hero.hasTalent(INSCRIBED_POWER)){
			// 2/3 empowered wand zaps
			Buff.affect(hero, ScrollEmpower.class).reset((int) (factor * (1 + hero.pointsInTalent(INSCRIBED_POWER))));
		}
		if (hero.hasTalent(INSCRIBED_STEALTH)){
			// 3/5 turns of stealth
			Buff.affect(hero, Invisibility.class, factor * (1 + 2*hero.pointsInTalent(INSCRIBED_STEALTH)));
			Sample.INSTANCE.play( Assets.Sounds.MELD );
		}
		if (hero.hasTalent(RECALL_INSCRIPTION) && cls !=null && Scroll.class.isAssignableFrom(cls) && cls != ScrollOfUpgrade.class && cls!= ScrollOfMetamorphosis.class){

			Buff.prolong(hero, RecallInscription.UsedItemTracker.class, hero.pointsInTalent(RECALL_INSCRIPTION) == 2 ? 300 : 10).item = cls;

		}
		if(hero.hasTalent(MAGIC_GIRL)){
			Buff.affect(hero, HeroDisguise.class,15+15*hero.pointsInTalent(MAGIC_GIRL));
			if(hero.pointsInTalent(MAGIC_GIRL)==2){
				Buff.affect(hero, Light.class,10);
			}
		}
		if(hero.hasTalent(SECRET_LIGHTING)){
			Buff.affect(hero, Light.class,hero.pointsInTalent(SECRET_LIGHTING)*20);
		}
		if(hero.hasTalent(READ_PROFITABLE)){
			switch(Random.Int(hero.pointsInTalent(READ_PROFITABLE)+2)){
				case 0:
					Buff.affect(hero, Levitation.class,5);
					break;
				case 1:
					Buff.affect(hero, PotionOfCleansing.Cleanse.class,5);
					break;
				case 2:
					Buff.affect(hero, Haste.class,5);
					break;
				case 3:
					Buff.affect(hero, Invisibility.class,5);
					break;
				case 4:
                    hero.heal(hero.HT/8);
					break;
			}
		}
		if(hero.hasTalent(GOLDOFBOOK) ){
			Dungeon.level.drop(new Gold().quantity(50*hero.pointsInTalent(GOLDOFBOOK)), hero.pos).sprite.drop();
		}
		if(hero.hasTalent(CHANGQI_BOOKSTORE) && hero.pointsInTalent(CHANGQI_BOOKSTORE)*3+3>Random.Int(20)&& Dungeon.talent_item<max_item){
			Dungeon.talent_item++;
			Dungeon.level.drop(Generator.randomUsingDefaults( Generator.Category.STONE ), hero.pos).sprite.drop();
		}
		if(hero.hasTalent(XIA)){
			Buff.affect(hero,XiaDef.class,(1+2*hero.pointsInTalent(XIA))*factor);
		}
		if(hero.hasTalent(RECOVER_CHARGE) && hero.heroClass!=HeroClass.DM400){
			Buff.affect(hero, ArtifactRecharge.class).extend(2*hero.pointsInTalent(RECOVER_CHARGE));
		}
		if(hero.hasTalent(OVER_CODE) && factor==2){
			HashSet<Class<? extends Potion>> potions = Potion.getUnknown();
			HashSet<Class<? extends Scroll>> scrolls = Scroll.getUnknown();
			ArrayList<Item> IDed = new ArrayList<>();
			Scroll s = Reflection.newInstance(Random.element(scrolls));
			if(hero.pointsInTalent(OVER_CODE)==2 && Random.Int(2)==1){
				Potion p = Reflection.newInstance(Random.element(potions));
				if(p!=null){
					p.identify();
					IDed.add(p);
				}
			}else{
				if(s!=null){
					s.identify();
					IDed.add(s);
				}

			}
			GameScene.show(new WndDivination(IDed,OVER_CODE));
		}
	}
	public static void onRunestoneUsed( Hero hero, int pos, Class<?extends Item> cls ){
		if (hero.hasTalent(RECALL_INSCRIPTION) && Runestone.class.isAssignableFrom(cls) ){
			Buff.prolong(hero, RecallInscription.UsedItemTracker.class, hero.pointsInTalent(RECALL_INSCRIPTION) == 2 ? 300 : 10).item = cls;
			/*
			if (hero.heroClass == HeroClass.CLERIC){
				Buff.prolong(hero, RecallInscription.UsedItemTracker.class, hero.pointsInTalent(RECALL_INSCRIPTION) == 2 ? 300 : 10).item = cls;
			} else {

				//don't trigger on 1st intuition use
				if (cls.equals(StoneOfIntuition.class) && hero.buff(StoneOfIntuition.IntuitionUseTracker.class) != null){
					return;
				}
				// 10/15%
				if (Random.Int(20) < 1 + hero.pointsInTalent(RECALL_INSCRIPTION)){
					Reflection.newInstance(cls).collect();
					GLog.p("refunded!");
				}
			}

			 */
		}
	}

	public static void onUpgradeScrollUsed( Hero hero ){
		if (hero.hasTalent(INSCRIBED_POWER)){
			if (hero.heroClass == HeroClass.MAGE) {
				MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
				if (staff != null) {
					staff.gainCharge(2 + 2 * hero.pointsInTalent(INSCRIBED_POWER), true);
					ScrollOfRecharging.charge(Dungeon.hero);
					SpellSprite.show(hero, SpellSprite.CHARGE);
				}
			} else {
				Buff.affect(hero, Recharging.class, 8 + 8 * hero.pointsInTalent(INSCRIBED_POWER));
			}
		}
	}

	public static void onArtifactUsed( Hero hero ){
		if (hero.hasTalent(ENHANCED_RINGS)){
			Buff.prolong(hero, EnhancedRings.class, 3f*hero.pointsInTalent(ENHANCED_RINGS));
		}
		if(hero.hasTalent(YOG_FIST) && hero.buffs(YogFistCooldown.class).isEmpty()){
			Buff.affect(hero,YogFistCooldown.class,20);
			Buff.affect(hero,YogFistPower.class,5);
			if(hero.pointsInTalent(YOG_FIST)>=1){
				Buff.affect(hero, Light.class,40);
			}
			if(hero.pointsInTalent(YOG_FIST)>=2){
				Buff.affect(hero, FireImbue.class).set(10);

			}
			if(hero.pointsInTalent(YOG_FIST)==3){
				Barkskin.conditionallyAppend(hero, hero.lvl/2, 1 );
			}
		}
		if(hero.hasTalent(Talent.LIGHT_BOX) && hero.belongings.getItem(Shuriken_Box.class)==null){
			Buff.affect(hero,LightBox.class,3);
		}
		/*
		if (Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.DIVINE_SENSE)){
			Buff.prolong(Dungeon.hero, DivineSense.DivineSenseTracker.class, Dungeon.hero.cooldown()+1);
		}

		// 10/20/30%
		if (Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.CLEANSE)
				&& Random.Int(10) < Dungeon.hero.pointsInTalent(Talent.CLEANSE)){
			boolean removed = false;
			for (Buff b : Dungeon.hero.buffs()) {
				if (b.type == Buff.buffType.NEGATIVE) {
					b.detach();
					removed = true;
				}
			}
			if (removed) new Flare( 6, 32 ).color(0xFF4CD2, true).show( Dungeon.hero.sprite, 2f );
		}

		 */
	}

	public static void onItemEquipped( Hero hero, Item item ){
        hero.updateHT(false);
		boolean identify = false;
		if (hero.pointsInTalent(VETERANS_INTUITION) == 2 && item instanceof Armor){
			identify = true;
		}
		if (hero.hasTalent(THIEFS_INTUITION) && item instanceof Ring){
			if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
				identify = true;
			}
			((Ring) item).setKnown();
		}
        if (hero.pointsInTalent(MANA_SENSE)>1){
            if(item instanceof Weapon && ((Weapon) item).enchantment!=null){
                identify = true;
            }
            if(item instanceof Armor && ((Armor) item).glyph!=null){
                identify = true;
            }
        }
		if (hero.pointsInTalent(ADVENTURERS_INTUITION) == 2 && item instanceof Weapon){
			identify = true;
		}

		if (identify && !ShardOfOblivion.passiveIDDisabled()){
			item.identify();
		}
	}

	public static void onItemCollected( Hero hero, Item item ){
		if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (item instanceof Ring) ((Ring) item).setKnown();
		}
		if(hero.hasTalent(WIDE_KNOWLEDGE)){
			item.cursedKnown = true;
		}
		if(hero.pointsInTalent(WIDE_KNOWLEDGE)>=2 && !item.isIdentified()){
			item.identify();
			if(hero.pointsInTalent(WIDE_KNOWLEDGE)==3){
				Buff.affect(hero, Recharging.class,3);
			}
		}

	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	public static void onItemIdentified( Hero hero, Item item ){
		//currently no talents that trigger here, it wasn't a very popular trigger =(
	}

	public static boolean canRavensEyeInspect( Hero hero, Item item ){
		return hero != null
				&& item != null
				&& hero.hasTalent(RAVENS_EYE)
				&& isRavensEyeTarget(item)
				&& !item.isIdentified()
				&& !item.cursedKnown;
	}

	private static boolean isRavensEyeTarget( Item item ){
		return item instanceof Weapon
				|| item instanceof Armor
				|| item instanceof Artifact
				|| item instanceof Ring
				|| item instanceof Wand;
	}

	public static void doRavensEyeInspect( final Hero hero, final Item item ){
		if (!canRavensEyeInspect(hero, item)){
			return;
		}

		GameScene.show(new WndOptions(new ItemSprite(item), RAVENS_EYE.title(),
				Messages.get(Item.class, "eye_prompt"),
				Messages.get(Item.class, "eye_yes"),
				Messages.get(Item.class, "eye_no")){
			@Override
			protected void onSelect(int index) {
				if (index == 0){
					inspectWithRavensEye(hero, item);
				}
			}
		});
	}

	private static void inspectWithRavensEye( Hero hero, Item item ){
		if (!canRavensEyeInspect(hero, item)){
			return;
		}

		int points = hero.pointsInTalent(RAVENS_EYE);
		if (hero.heroClass == HeroClass.FRIAR){
			int cost = points == 1 ? 30 : 20;
			Reason reason = hero.buff(Reason.class);
			if (reason == null || reason.reason < cost){
				GLog.w(Messages.get(Item.class, "no_reason"));
				return;
			}
			Reason.loseReason(hero, cost);
		} else {
			Buff.affect(hero, Bleeding.class).set(points == 1 ? 6 : 4);
		}

		item.cursedKnown = true;
		updateQuickslot();

		if (item.cursed){
			GLog.w(Messages.get(Item.class, "cursed"));
		} else {
			GLog.i(Messages.get(Item.class, "uncursed"));
		}

		hero.busy();
		hero.sprite.operate(hero.pos);
		hero.sprite.parent.add( new Identification( hero.sprite.center().offset( 0, -16 ) ) );
		Sample.INSTANCE.play(Assets.Sounds.READ);
		hero.spendAndNext(1f);
	}

	public static boolean hasBlockingWeapon( Hero hero ){
		if (hero == null){
			return false;
		}
		return hasBlockingWeapon(hero, hero.belongings.weapon())
				|| hasBlockingWeapon(hero, hero.belongings.secondWep());
	}

	private static boolean hasBlockingWeapon( Hero hero, KindOfWeapon weapon ){
		return MeleeWeapon.hasTrait(weapon, MeleeWeapon.WeaponTrait.BLOCKING, hero);
	}
	public static void onPotionUsed( Hero hero, int cell, float factor, Potion potion ){
		onPotionUsed(hero, cell, factor);
		if (hero != null && hero.hasTalent(LIQUID_BARRIER)){
			float duration = hero.pointsInTalent(LIQUID_BARRIER) == 1 ? 5f : 10f;
			Buff.affect(hero, BlobImmunity.class, duration * factor);
		}
	}





	public static int alchemyEnergyCost( Hero hero, int cost ){
		if (hero == null || cost <= 0 || !hero.subClass.is(HeroSubClass.ALCHEMIST)){
			return cost;
		}
		return Math.max(1, cost - (3 + hero.pointsInTalent(ALCHEMY_SHIELD)));
	}

	public static int miracleAlchemyBonus( Hero hero, Item result ){
		if (hero == null){
			return 0;
		}
		// Keep the roll before product filtering to preserve the legacy RNG sequence.
		return miracleAlchemyBonus(hero.pointsInTalent(MIRACLE_ALCHEMY), Random.Int(5),
				result == null ? null : result.getClass());
	}

	static int miracleAlchemyBonus( int points, int roll, Class<? extends Item> productType ){
		if (points <= roll || productType == null){
			return 0;
		}
		return isMiracleAlchemyProduct(productType) ? 1 : 0;
	}

	private static boolean isMiracleAlchemyProduct( Class<? extends Item> productType ){
		return !Trinket.class.isAssignableFrom(productType)
				&& !StewedMeat.class.isAssignableFrom(productType)
				&& !Blandfruit.class.isAssignableFrom(productType)
				&& !Bomb.class.isAssignableFrom(productType)
				&& !LiquidMetal.class.isAssignableFrom(productType)
				&& !ScrollOfEnchantment.class.isAssignableFrom(productType)
				&& !RubbingsTome.class.isAssignableFrom(productType)
				&& !MagicalInfusion.class.isAssignableFrom(productType)
				&& !ElixirOfMight.class.isAssignableFrom(productType)
				&& !TransformSpell.class.isAssignableFrom(productType)
				&& !MetamorphosisPrism.class.isAssignableFrom(productType)
				&& !PotionOfMastery.class.isAssignableFrom(productType);
	}

	public static void onAlchemyEnergyConsumed( Hero hero ){
		if (hero != null && hero.subClass.is(HeroSubClass.ALCHEMIST) && hero.hasTalent(ALCHEMY_SHIELD)){
			Buff.affect(hero, AlchemyShield.class).gain(hero.pointsInTalent(ALCHEMY_SHIELD));
		}
	}

	public static float alchemistCloseBlastMultiplier( Hero hero, int distance, int maxRange ){
		if (hero == null || !hero.subClass.is(HeroSubClass.ALCHEMIST) || !hero.hasTalent(CLOSE_BLAST)){
			return 1f;
		}
		int closerSteps = Math.min(4, Math.max(0, maxRange - distance));
		float bonusPerStep = 0.05f + 0.05f * hero.pointsInTalent(CLOSE_BLAST);
		return 1f + closerSteps * bonusPerStep;
	}

	public static boolean alchemistSprayChargePreserved( Hero hero ){
		if (hero == null || !hero.subClass.is(HeroSubClass.ALCHEMIST) || !hero.hasTalent(CONSERVATION)){
			return false;
		}
		return Random.Float() < 0.1f * hero.pointsInTalent(CONSERVATION);
	}

	public static boolean alchemistPotionLoadPreserved( Hero hero ){
		if (hero == null || !hero.subClass.is(HeroSubClass.ALCHEMIST) || !hero.hasTalent(CONSERVATION)){
			return false;
		}
		return Random.Float() < 0.15f * (hero.pointsInTalent(CONSERVATION) + 1);
	}



	public static float bullseyeAccuracyFactor( Hero hero ){
		if (hero != null && hero.hasTalent(BULLSEYE)){
			return hero.pointsInTalent(BULLSEYE) == 1 ? 1.5f : 1.75f;
		}
		return 1f;
	}

	public static boolean isArmedUprisingAlly(Char attacker) {
		return attacker instanceof Mob && attacker.alignment == Char.Alignment.ALLY;
	}

	public enum AttackProcChannel {
		MELEE_DAMAGE,
		MELEE_SPECIAL
	}

	/**
	 * Immutable, attack-scoped facts used by talent procs. Inherited channels
	 * never alter the real weapon or delivery type, so weapon-specific effects
	 * can continue to distinguish an actual melee hit from an inherited one.
	 */
	public static final class AttackProcContext {

		private final KindOfWeapon weapon;
		private final DamageTag.Delivery delivery;
		private final boolean inheritedMeleeDamage;
		private final boolean inheritedMeleeSpecial;

		private AttackProcContext(KindOfWeapon weapon, DamageTag.Delivery delivery,
				boolean inheritedMeleeDamage, boolean inheritedMeleeSpecial) {
			this.weapon = weapon;
			this.delivery = delivery == null ? DamageTag.Delivery.NONE : delivery;
			this.inheritedMeleeSpecial = inheritedMeleeSpecial;
			this.inheritedMeleeDamage = inheritedMeleeDamage || inheritedMeleeSpecial;
		}

		public static AttackProcContext create(KindOfWeapon weapon, DamageTag.Delivery delivery,
				boolean inheritedMeleeDamage, boolean inheritedMeleeSpecial) {
			return new AttackProcContext(weapon, delivery,
					inheritedMeleeDamage, inheritedMeleeSpecial);
		}

		public static AttackProcContext forAttack(Hero hero, boolean inheritedMeleeDamage,
				boolean inheritedMeleeSpecial, DamageTag... damageTags) {
			KindOfWeapon weapon = hero == null ? null : hero.belongings.attackingWeapon();
			EnumSet<DamageTag> tags = DamageTag.of(damageTags);
			DamageTag.Delivery delivery = DamageTag.physicalDelivery(tags);
			if (delivery == DamageTag.Delivery.NONE && !tags.contains(DamageTag.MAGICAL)) {
				if (weapon instanceof MissileWeapon) {
					delivery = DamageTag.Delivery.RANGED;
				} else if (weapon instanceof MeleeWeapon || weapon == null) {
					delivery = DamageTag.Delivery.MELEE;
				}
			}
			return create(weapon, delivery, inheritedMeleeDamage, inheritedMeleeSpecial);
		}

		/**
		 * Builds the context for a direct hero attack and rolls Hunting Technique
		 * once, so its damage and special channels always share the same result.
		 */
		public static AttackProcContext forHeroAttack(Hero hero, DamageTag... damageTags) {
			AttackProcContext context = forAttack(hero, false, false, damageTags);
			int points = hero == null ? 0 : hero.pointsInTalent(HUNTING_TECHNIQUE);
			if (points > 0 && context.isActualPhysicalRangedWeaponAttack()) {
				return applyHuntingTechnique(context, points, Random.Int(3));
			}
			return context;
		}

		static AttackProcContext applyHuntingTechnique(AttackProcContext context,
				int talentPoints, int roll) {
			if (context == null || talentPoints <= 0 || roll < 0 || roll >= 3
					|| !context.isActualPhysicalRangedWeaponAttack()) {
				return context;
			}
			return create(context.weapon, context.delivery, true, talentPoints >= 2);
		}

		public boolean allows(AttackProcChannel channel) {
			return isActualMeleeAttack() || inherits(channel);
		}

		public boolean inherits(AttackProcChannel channel) {
			return channel == AttackProcChannel.MELEE_DAMAGE
					? inheritedMeleeDamage
					: inheritedMeleeSpecial;
		}

		public boolean isActualMeleeAttack() {
			return delivery == DamageTag.Delivery.MELEE
					&& (weapon instanceof MeleeWeapon || weapon == null);
		}

		public boolean isActualPhysicalRangedWeaponAttack() {
			return delivery == DamageTag.Delivery.RANGED && weapon instanceof MissileWeapon;
		}

		public boolean isActualMeleeWeapon() {
			return weapon instanceof MeleeWeapon;
		}

		public boolean isActualUnarmedAttack() {
			return isActualMeleeAttack() && weapon == null;
		}

		public KindOfWeapon weapon() {
			return weapon;
		}

		public DamageTag.Delivery delivery() {
			return delivery;
		}
	}

	public static int onAttackProcMult( Hero hero, Char enemy, int dmg ){
		return onAttackProcMult(hero, enemy, dmg,
				AttackProcContext.forAttack(hero, false, false));
	}

	public static int onAttackProcMult(Hero hero, Char enemy, int dmg, AttackProcContext context) {
		if(hero.pointsInTalent(PUMP_ATTACK)>0 && Random.Int(5)==1 && context.allows(AttackProcChannel.MELEE_DAMAGE)){
			dmg = Math.round(dmg * (1.0f + 1.0f*hero.pointsInTalent(PUMP_ATTACK)));
			GLog.i("此一击积蓄了很强的力量，造成多倍伤害");
		}
		if (hero.pointsInTalent(Talent.OVERLOAD_CHARGE)==1 && context.allows(AttackProcChannel.MELEE_DAMAGE) && (hero.buff(Recharging.class)!=null || hero.buff(ArtifactRecharge.class)!=null)){
			dmg*=1.3;
		}else if(hero.pointsInTalent(Talent.OVERLOAD_CHARGE)==2 && context.allows(AttackProcChannel.MELEE_DAMAGE) && (hero.buff(Recharging.class)!=null || hero.buff(ArtifactRecharge.class)!=null)){
			dmg*=1.5;
		}
		if(hero.attackDelay()>1 && hero.pointsInTalent(Talent.OVERWHELMING)>=1){
			dmg*=1+(hero.attackDelay()-1)*0.33*hero.pointsInTalent(Talent.OVERWHELMING);
		}
		if( hero.pointsInTalent(JUSTICE_PUNISH)>=1 && (enemy.properties().contains(Char.Property.UNDEAD)|| enemy.properties().contains(Char.Property.DEMONIC))){
			dmg*=1.1+0.1*hero.pointsInTalent(JUSTICE_PUNISH);
		}
		if(hero.pointsInTalent(AMAZING_EYESIGHT)>0 && context.allows(AttackProcChannel.MELEE_DAMAGE) && Dungeon.level.distance(hero.pos,enemy.pos)>=4-hero.pointsInTalent(AMAZING_EYESIGHT)){
			dmg*=1.35;
		}
		if(hero.hasTalent(Talent.RAGE_ATTACK)){
			dmg*=1+hero.pointsInTalent(Talent.RAGE_ATTACK)*0.1;
		}
		if(hero.pointsInTalent(GOD_LEFTHAND)==2 && hero.pointsInTalent(GOD_RIGHTHAND)==2){
			dmg*=1.6;
		}else if(hero.pointsInTalent(GOD_LEFTHAND)>=1 && hero.pointsInTalent(GOD_RIGHTHAND)>=1){
			dmg*=1.3;
		}
		if(hero.hasTalent(GIANT_KILLER) && enemy.properties().contains(Char.Property.LARGE)){
			dmg*=1+0.35*hero.pointsInTalent(GIANT_KILLER);
		}
		if(hero.HP<hero.HT*0.3 && hero.hasTalent(AFRAID_DEATH)){
			dmg*=afraidDeathDamageMultiplier(hero.pointsInTalent(AFRAID_DEATH));
		}
		if(hero.hasTalent(SKY_EARTH) && (!hero.buffs(Roots.class).isEmpty() || !hero.buffs(Levitation.class).isEmpty() || !hero.buffs(Earthroot.Armor.class).isEmpty())){
			dmg*=1+0.15*hero.pointsInTalent(SKY_EARTH);
		}
		if(hero.pointsInTalent(Talent.DEEP_FREEZE)==3 && (!enemy.buffs(Chill.class).isEmpty() || !enemy.buffs(Frost.class).isEmpty())){
			dmg*=1.2;
			if(hero.hasTalent(ICE_BREAKING)){
				dmg*=1.5;
			}
		}
		if(hero.hasTalent(LOVE_BACKSTAB) && !enemy.buffs(Charm.class).isEmpty()){
			dmg*=1+0.15*hero.pointsInTalent(LOVE_BACKSTAB);
		}
		if(hero.pointsNegative(FEEBLE)>0){
			dmg*=1-0.1f*hero.pointsNegative(FEEBLE);
		}
		if(hero.pointsInTalent(PRECISE_SHOT)>0 && !context.isActualMeleeWeapon() && Dungeon.level.distance(hero.pos,enemy.pos)>=3){
			dmg*=1+0.1f*hero.pointsInTalent(PRECISE_SHOT);
		}
		if(hero.hasTalent(POISON_INBODY)){
			int cnt=0;
			for (Buff b : hero.buffs()){
				if (b.type == Buff.buffType.NEGATIVE
						&& !(b instanceof AllyBuff)
						&& !(b instanceof LostInventory)){
					cnt++;
				}
			}
			dmg*=1+0.1f*hero.pointsInTalent(POISON_INBODY)*cnt;
		}
		if(hero.pointsNegative(PHOTOPHOBY)>0 && !hero.buffs(Light.class).isEmpty()){
			dmg*=1-0.2*hero.pointsNegative(PHOTOPHOBY);
		}
		if(hero.hasTalent(SHOOT_SATELLITE) && enemy.flying==true){
			dmg*=1+0.1+0.1*hero.pointsInTalent(SHOOT_SATELLITE);
			if(context.weapon() instanceof ThrowingStone){
				dmg*=2;
			}
		}
		if(hero.hasTalent(KONO_FUKUSA) && enemy instanceof Mob &&(((Mob) enemy).surprisedBy(hero) || ((Mob) enemy).state==((Mob) enemy).HUNTING)){
			dmg*=1+0.2*hero.pointsInTalent(KONO_FUKUSA);
		}
		if(hero.hasTalent(KONO_FUKUSA) && enemy instanceof Hero){
			dmg*=1+0.2*hero.pointsInTalent(KONO_FUKUSA);
		}
		if(hero.buff(OneSword.OKU_OneSword.class)!=null && context.isActualMeleeWeapon()){
			float onesword = 1.3f;
			if((context.weapon() instanceof Katana) || (context.weapon() instanceof Wakizashi)){
				onesword+=0.2f;
			}
			if(hero.hasTalent(OFFENSIVE)){
				onesword+=0.15f*hero.pointsInTalent(OFFENSIVE);
			}
			dmg*=onesword;
		}
		if(enemy.buff(Decoy.ShadowMark.class)!=null && hero.hasTalent(Talent.ALLHUNTING)){
			dmg*=1+0.15f*hero.pointsInTalent(Talent.ALLHUNTING);
		}
		if(hero.subClass.is(HeroSubClass.AT400) && enemy.buff(InstructionTool.InstructionMark.class)!=null){
			dmg *=1.3f;
		}
		if(hero.hasTalent(SWEET_SLEEP) && enemy instanceof Mob
				&& ((Mob) enemy).surprisedBy(hero)){
			dmg*=1.2f+0.1f*hero.pointsInTalent(SWEET_SLEEP);
		}
        if(hero.glyphLevel(Obfuscation.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER) && enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)){
            dmg*=1.0f + 0.05f * hero.stealth();
        }
        FightStance fightStance = hero.buff(FightStance.class);
        if(fightStance!=null){
            if(fightStance.stance==fightStance.balance){
                dmg*=1.1f+Math.min(1,hero.pointsInTalent(Talent.STANCE_MASTERY))*0.05f;
            }else if(fightStance.stance==fightStance.invasion){
                dmg*=1.2f+hero.pointsInTalent(Talent.STANCE_MASTERY)/2*0.1f;
            }

        }
		if(hero.hasTalent(MAGIC_ARROW)){
			KindOfWeapon weapon = context.weapon();
            RingOfKing ring = hero.belongings.getItem(RingOfKing.class);
            if(((weapon instanceof Weapon) && !(weapon instanceof MeleeWeapon) && ((Weapon)weapon).getEnchant()!=null) ||
                    (ring != null && ring.enchantment != null && !ring.cursed && ring.isEquipped(hero))){
                dmg*=1.0f+0.15f*hero.pointsInTalent(MAGIC_ARROW);
            }
        }
		if (hero.hasTalent(DEADLY_FOLLOWUP) && enemy.alignment == Char.Alignment.ENEMY
				&& !(context.weapon() instanceof MissileWeapon)
				&& hero.buff(DeadlyFollowupTracker.class) != null
				&& hero.buff(DeadlyFollowupTracker.class).object == enemy.id()){
			dmg = Math.round(dmg * (1.0f + .1f*hero.pointsInTalent(DEADLY_FOLLOWUP)));
		}
		return dmg;
	}
	public static int onAttackProcBonus( Hero hero, Char enemy){
		return onAttackProcBonus(hero, enemy,
				AttackProcContext.forAttack(hero, false, false));
	}

	public static int onAttackProcBonus(Hero hero, Char enemy, AttackProcContext context) {
		int dmg =0;
		if(hero.hasTalent(HEDONISM) && hero.buff(Hunger.class).level<300){
			dmg += 2*hero.pointsInTalent(HEDONISM);
		}
		if (hero.hasTalent(CRYSTAL_GUNPOWDER)){
			dmg += Math.min(2 * hero.pointsInTalent(CRYSTAL_GUNPOWDER), Dungeon.energy * hero.pointsInTalent(CRYSTAL_GUNPOWDER));
		}
		if (context.weapon() instanceof Dart && hero.hasTalent(BULLSEYE) && enemy.alignment != hero.alignment){
			dmg += hero.pointsInTalent(BULLSEYE) == 1 ? 3 : 5;
		}
		if( hero.pointsInTalent(FEAR_INCARNATION)>=1 && !enemy.buffs(Terror.class).isEmpty()){
			dmg+=1+2*hero.pointsInTalent(FEAR_INCARNATION);}
		if(hero.hasTalent(ATTACK_DOOR) && Dungeon.level.map[enemy.pos] ==Terrain.OPEN_DOOR){
			dmg+=hero.pointsInTalent(ATTACK_DOOR);
		}
		if (hero.hasTalent(Talent.PROVOKED_ANGER)
				&& hero.buff(ProvokedAngerTracker.class) != null){
			dmg += 1 + hero.pointsInTalent(Talent.PROVOKED_ANGER) * 2;
			hero.buff(ProvokedAngerTracker.class).detach();
		}

		if (hero.hasTalent(Talent.LINGERING_MAGIC)
				&& hero.buff(LingeringMagicTracker.class) != null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.LINGERING_MAGIC) , 2);
			hero.buff(LingeringMagicTracker.class).detach();
		}

		if (hero.hasTalent(Talent.SUCKER_PUNCH)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(SuckerPunchTracker.class) == null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.SUCKER_PUNCH) , 2);
			Buff.affect(enemy, SuckerPunchTracker.class);
		}

		if (hero.hasTalent(Talent.FOLLOWUP_STRIKE) && enemy.isAlive() && enemy.alignment == Char.Alignment.ENEMY) {
			if (context.weapon() instanceof MissileWeapon) {
				Buff.prolong(hero, FollowupStrikeTracker.class, 5f).object = enemy.id();
			} else if (hero.buff(FollowupStrikeTracker.class) != null
					&& hero.buff(FollowupStrikeTracker.class).object == enemy.id()){
				dmg += 1 + hero.pointsInTalent(FOLLOWUP_STRIKE);
				hero.buff(FollowupStrikeTracker.class).detach();
			}
		}

		if (hero.buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*hero.pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
			if (bow != null) dmg = bow.proc( hero, enemy, dmg );
			hero.buff(Talent.SpiritBladesTracker.class).detach();
		}

		if (hero.hasTalent(PATIENT_STRIKE)){
			if (hero.buff(PatientStrikeTracker.class) != null
					&& (!(context.weapon() instanceof MissileWeapon)
					|| context.inherits(AttackProcChannel.MELEE_DAMAGE))){
				hero.buff(PatientStrikeTracker.class).detach();
				dmg += Random.IntRange(hero.pointsInTalent(Talent.PATIENT_STRIKE), 2);
			}
		}
		if(hero.subClass.is(HeroSubClass.DARKSLIME) && Random.Int(10)<5){
			Buff.affect(enemy,Ooze.class).set(20);
		}

		if(hero.pointsInTalent(Talent.WATER_ATTACK)>0 && Dungeon.level.water[hero.pos]){
			dmg+=hero.pointsInTalent(Talent.WATER_ATTACK);
		}
		if(hero.pointsInTalent(Talent.WATER_WAVE)>0 && Dungeon.level.water[enemy.pos]){
			dmg+=hero.pointsInTalent(Talent.WATER_WAVE);
		}
		if(hero.hasTalent(AGILE_ATTACK) && hero.buff(AgileAttack.class)!=null){
			hero.buff(AgileAttack.class).detach();
			dmg+=1+2*hero.pointsInTalent(AGILE_ATTACK);
		}
		if(hero.hasTalent(TARGET_TARGETING)){
			if(enemy.buff(InstructionTool.InstructionMark.class)!=null){
				dmg+=hero.pointsInTalent(TARGET_TARGETING);
			}else if(hero.heroClass!=HeroClass.DM400 && enemy.buff(Vertigo.class)!=null){
				dmg+=hero.pointsInTalent(TARGET_TARGETING);
			}
		}
        if(hero.glyphLevel(Viscosity.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER) && hero.buff(Viscosity.DeferedDamage.class)!=null){
            int d=hero.buff(Viscosity.DeferedDamage.class).getDamage();
            dmg+=d/2;
            hero.buff(Viscosity.DeferedDamage.class).prolong(-d/2);
        }

		return dmg;
	}

	public static int onAttackProc(Hero hero, Char attacker, Char enemy, int dmg,
			DamageTag... damageTags) {
		AttackProcContext context = attacker == hero
				? AttackProcContext.forHeroAttack(hero, damageTags)
				: AttackProcContext.forAttack(hero, false, false);
		return onAttackProc(hero, attacker, enemy, dmg, context, damageTags);
	}

	public static int onAttackProc(Hero hero, Char attacker, Char enemy, int dmg,
			AttackProcContext context, DamageTag... damageTags) {
		if(attacker==hero){
			dmg = onAttackProcMult(hero, enemy, dmg, context)
					+ onAttackProcBonus(hero, enemy, context);
		}
		if(hero.buff(OneSword.OKU_OneSword.class)!=null && context.isActualMeleeWeapon()){
			Buff.affect(enemy,OneSword.Kill.class);
		}
		if (hero.hasTalent(DEADLY_FOLLOWUP) && enemy.alignment == Char.Alignment.ENEMY
				&& context.weapon() instanceof MissileWeapon
				&& !(context.weapon() instanceof SpiritBow.SpiritArrow)) {
			Buff.prolong(hero, DeadlyFollowupTracker.class, 5f).object = enemy.id();
		}
		if(hero.hasTalent(OOZE_ATTACK)&& Random.Int(4)<=hero.pointsInTalent(OOZE_ATTACK) && context.allows(AttackProcChannel.MELEE_SPECIAL)){
			Buff.affect( enemy, Ooze.class ).set(15);
			Viscosity.DeferedDamage deferred=Buff.affect( enemy, Viscosity.DeferedDamage.class );
			deferred.prolong( 10 );
			showOozeAttackEffect(enemy);
		}
		if( hero.pointsInTalent(STRONG_ATTACK)>=1 && attacker.buffs(Talent.StrAtkCooldown.class).isEmpty() && context.allows(AttackProcChannel.MELEE_SPECIAL)){
			Buff.affect( enemy, Vulnerable.class ,1+hero.pointsInTalent(STRONG_ATTACK)*2);
			Buff.affect(attacker,StrAtkCooldown.class,15);

		}
		if(hero.pointsInTalent(Talent.DISTURB_ATTACK)>Random.Int(10)){
			Buff.affect(enemy, Vertigo.class,3);
		}
		if(hero.pointsInTalent(Talent.GHOLL_WITCHCRAFT)>0 && !enemy.buffs(PinCushion.class).isEmpty()){
			Buff.affect(enemy, Hex.class,hero.pointsInTalent(Talent.GHOLL_WITCHCRAFT)+1);
		}
		if (hero.hasTalent(COVER_SCAR) && context.allows(AttackProcChannel.MELEE_SPECIAL)){
			Buff.affect( enemy, Bleeding.class).set(hero.pointsInTalent(COVER_SCAR));
		}
		if(hero.hasTalent(ICE_BREAKING) && !enemy.buffs(Chill.class).isEmpty()  && enemy.isAlive()){
			enemy.damage(hero.pointsInTalent(ICE_BREAKING), new WandOfMagicMissile(), DamageTag.MAGICAL);
		}
		if (hero.hasTalent(POSION_DAGGER) && MeleeWeapon.hasTrait(
				context.weapon(), MeleeWeapon.WeaponTrait.DAGGER, hero)) {
			Buff.affect(enemy,Poison.class).set(hero.pointsInTalent(POSION_DAGGER)*2);
		}
		if(!attacker.buffs(YogFistPower.class).isEmpty()){
			if(hero.pointsInTalent(YOG_FIST)>=1){
				Buff.affect(enemy, Blindness.class,5);
			}
			if(hero.pointsInTalent(YOG_FIST)==3){
				Buff.affect(enemy, Roots.class,5);
			}
		}
		if(hero.pointsInTalent(Talent.DEEP_FREEZE)>=2){
			Buff.affect(enemy,Chill.class,3);
		}
		if(hero.pointsInTalent(FRENZIED_ATTACK)>Random.Int(4)){
			Buff.affect(enemy, Amok.class,1);
		}
		if(hero.hasTalent(ABACUS) && hero.pointsInTalent(ABACUS)>= Random.Int(4)){
			Buff.affect(attacker, Barrier.class).incShield(2);
		}
		if(hero.hasTalent(DAMAGED_CORE)  && enemy.isAlive()){
			//enemy.damage(hero.pointsInTalent(DAMAGED_CORE), new WandOfMagicMissile());
			enemy.damage(hero.pointsInTalent(DAMAGED_CORE), new WandOfMagicMissile(), DamageTag.MAGICAL);
		}
		if(hero.pointsInTalent(FUDI_CHOUXIN)> Random.Int(4)){
			Buff.affect(enemy, Weakness.class,2);
		}
		if(hero.pointsNegative(MENTAL_COLLAPSE)> Random.Int(10)  && enemy.isAlive()){
			hero.damage(dmg/2,new WandOfMagicMissile(), DamageTag.MAGICAL);
		}
		if(hero.pointsNegative(Talent.FIRE_WOOD)>Random.Int(20)){
			Buff.affect(attacker, Burning.class).reignite(hero,3);
			Buff.affect(enemy, Burning.class).reignite(enemy,3);
			Buff.affect(attacker, Charm.class,10).object=enemy.id();
			Buff.affect(enemy, Charm.class,10).object=hero.id();
		}
		if(hero.hasTalent(ASHES_BOW) && attacker.buffs(AshesBowCooldown.class).isEmpty() &&
				(context.weapon() instanceof MissileWeapon
						|| context.weapon() instanceof SpiritBow)){
			Buff.affect(enemy, Burning.class).reignite(enemy,hero.pointsInTalent(ASHES_BOW));
			Buff.affect(attacker,AshesBowCooldown.class,15);
		}
		if(hero.pointsInTalent(NIRVANA)==2 && !attacker.buffs(Burning.class).isEmpty()){
			Buff.affect(enemy, Burning.class).extend(3);
		}
		if(hero.pointsInTalent(HAND_DESTRUCTION) * 0.05f > ((float)(enemy.HP)/(float)(enemy.HT)) && !enemy.isImmune(Grim.class) && enemy.resist(Grim.class)==1f && enemy!=hero){
			enemy.sprite.emitter().burst( ShadowParticle.UP, 5 );
			enemy.die(hero);
		}
		if (hero.buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*hero.pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
			if (bow != null) dmg = bow.proc( hero, enemy, dmg );
			hero.buff(Talent.SpiritBladesTracker.class).detach();
		}
		ComboPackage c = hero.buff(ComboPackage.class);
		if(hero.hasTalent(COMBO_PACKAGE) && c!=null && attacker==hero && context.allows(AttackProcChannel.MELEE_SPECIAL)){
			c.left++;
			if(c.left>=8-2*hero.pointsInTalent(COMBO_PACKAGE) ){
				onFoodEaten(hero,0,new HornOfPlenty());
				c.left-=8-2*hero.pointsInTalent(COMBO_PACKAGE);
				if(c.left<=0){
					c.detach();
				}
			}
		}else if(hero.hasTalent(COMBO_PACKAGE) && c==null && attacker==hero && context.allows(AttackProcChannel.MELEE_SPECIAL)){
			Buff.affect(attacker,ComboPackage.class).left=1;
		}

		if(hero.pointsInTalent(THROWING_RECYCLING)>Random.Int(4) && enemy.buff(PinCushion.class) != null ){
			while (enemy.buff(PinCushion.class) != null) {
				Item item = enemy.buff(PinCushion.class).grabOne();
				if (item.doPickUp(hero, enemy.pos)) {
					hero.spend(-item.pickupDelay()); //casting the spell already takes a turn
					GLog.i( Messages.capitalize(Messages.get(hero, "you_now_have", item.name())) );

				} else {
					GLog.w(Messages.get(TelekineticGrab.class, "cant_grab"));
					Dungeon.level.drop(item, enemy.pos).sprite.drop();
				}
			}
		}
		if(hero.subClass.is(HeroSubClass.TATTEKI_NINJA) && (context.weapon() instanceof MissileWeapon)
				&& !(context.weapon() instanceof Tatteki) && !(context.weapon() instanceof Tatteki.Tamaru)){
			Buff.affect(enemy, Tatteki.Fix.class);
		}
		if(attacker==hero && hero.buff(Ninja_Energy.Throw_Skill.class)!=null && (context.weapon() instanceof MissileWeapon) && enemy.isAlive()){
			Ninja_Energy.Throw_Skill b = hero.buff(Ninja_Energy.Throw_Skill.class);
			b.detach();
			if(hero.buff(Ninja_Energy.Gas_Storage.class)!=null){
				Ninja_Energy.Gas_Storage gas_storage=hero.buff(Ninja_Energy.Gas_Storage.class);
				for(Blob blob:gas_storage.blobs.values()){
					GameScene.add(Blob.seed(enemy.pos,30,blob.getClass()));
				}
				gas_storage.detach();
			}
			if(Dungeon.level.map[hero.pos] == Terrain.GRASS || Dungeon.level.map[hero.pos] == Terrain.EMBERS
					|| Dungeon.level.map[hero.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[hero.pos] == Terrain.FURROWED_GRASS){
				Plant plant = (Plant) Reflection.newInstance(Random.element(SpiritBow.harmfulPlantPool()));
				plant.pos = enemy.pos;
				plant.activate( enemy.isAlive() ? enemy : null );
			}else if(Dungeon.level.water[hero.pos]){
				Ninja_Energy.NinjaAbility.Throw_Water(hero.pos,enemy.pos);
			}else{
				Buff.affect(enemy, Cripple.class,5);
				Buff.affect(enemy,Bleeding.class).set(dmg*0.5f);
			}
		}
		if(attacker.buff(NinjaSocial.class)!=null){
			attacker.buff(NinjaSocial.class).left--;
			if(attacker.buff(NinjaSocial.class).left<0){
				attacker.buff(NinjaSocial.class).detach();
			}
		}
		MarkMeal b = attacker.buff(MarkMeal.class);
		if(hero.heroClass==HeroClass.DM400){
			int turn =3;
			if(b!=null){
				turn +=1+hero.pointsInTalent(MARK_MEAL);
				b.left-=1;
				if(b.left<=0){
					b.detach();
				}
			}
			if(enemy.alignment != Char.Alignment.ALLY){
				Buff.affect(enemy, InstructionTool.InstructionMark.class).reset(turn);
			}

		}else{
			if(b!=null){
				Buff.affect(enemy, Vertigo.class,1+hero.pointsInTalent(MARK_MEAL));
				b.left-=1;
				if(b.left<=0){
					b.detach();
				}
			}
		}
		if(enemy.buff(InstructionTool.InstructionMark.class)!=null && hero.pointsInTalent(SPECIAL_MARK)>Random.Int(6)){
			Buff.affect(enemy, StoneOfAggression.Aggression.class,3);
		}
		if(hero.hasTalent(ROCKET_FIST)){
			if(Random.Int(10)<6 && context.isActualUnarmedAttack()){
				Ballistica trajectory = new Ballistica(attacker.pos, enemy.pos, Ballistica.STOP_TARGET);
				//trim it to just be the part that goes past them
				trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
				//knock them back along that ballistica
				WandOfBlastWave.throwChar(enemy,
						trajectory,
						hero.pointsInTalent(ROCKET_FIST),
						false,
						true,
						attacker);

			}else if(Random.Int(10)<3 && !context.isActualUnarmedAttack()
					&& context.allows(AttackProcChannel.MELEE_SPECIAL)){
				Ballistica trajectory = new Ballistica(attacker.pos, enemy.pos, Ballistica.STOP_TARGET);
				//trim it to just be the part that goes past them
				trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
				//knock them back along that ballistica
				WandOfBlastWave.throwChar(enemy,
						trajectory,
						hero.pointsInTalent(ROCKET_FIST),
						false,
						true,
						attacker);
			}

		}
		if(hero.pointsNegative(Talent.STEAM_BEAN)>0 && enemy.buff(SteamBean.class)==null){
			Buff.affect(hero, Daze.class, 4*hero.pointsNegative(Talent.STEAM_BEAN));
			Buff.affect(enemy,SteamBean.class);
		}
		if(hero.hasTalent(RUNE_BLADE) && enemy.isAlive()){
			KindOfWeapon weapon = context.weapon();
            RingOfKing ring = hero.belongings.getItem(RingOfKing.class);
            if((weapon instanceof Weapon && ((Weapon)weapon).getEnchant()!=null) ||
                (ring != null && ring.enchantment != null && !ring.cursed && ring.isEquipped(hero))){
                enemy.damage(hero.pointsInTalent(RUNE_BLADE), new WandOfMagicMissile(), DamageTag.MAGICAL);
            }
        }
		if(hero.hasTalent(MANA_WREATH)){
			KindOfWeapon weapon = context.weapon();
            RingOfKing ring = hero.belongings.getItem(RingOfKing.class);
            if((weapon instanceof Weapon && ((Weapon)weapon).getEnchant()!=null) ||
                    (ring != null && ring.enchantment != null && !ring.cursed && ring.isEquipped(hero))){
                Buff.affect(enemy, ManaWreath.class);
            }
        }
        if(enemy.buff(Charm.class)!=null && hero.glyphLevel(Affection.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER)){
            hero.heal((int)(dmg*0.2f));
        }
        if(hero.glyphLevel(AntiMagic.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER)  && enemy.isAlive()){
            enemy.damage(AntiMagic.drRoll(hero, hero.glyphLevel(AntiMagic.class)), new WandOfMagicMissile(), DamageTag.MAGICAL);
        }
        if(hero.glyphLevel(Entanglement.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER) && hero.buff(Earthroot.Armor.class)!=null){
            int shield = hero.buff(Earthroot.Armor.class).getLevel();
            if(shield>Random.Int(20)){
                hero.buff(Earthroot.Armor.class).changeLevel(-(5+Dungeon.scalingDepth())/2);
				Plant plant = (Plant) Reflection.newInstance(Random.element(SpiritBow.harmfulPlantPool()));
                plant.pos = enemy.pos;
				if(plant instanceof Icecap){
					Buff.affect(hero, FrostImbue.class, 1f);
				}
                plant.activate( enemy.isAlive() ? enemy : null );

            }
        }
		if(hero.glyphLevel(Potential.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER) && context.isActualMeleeWeapon()){
            int wands = hero.belongings.charge( 0.5f );
            if (wands > 0) {
                hero.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 10);
            }
        }
        if(hero.glyphLevel(Thorns.class)>=0 && hero.subClass.is(HeroSubClass.COMBATMASTER)){
            Buff.affect(enemy, Bleeding.class).set((int)(dmg*0.3f));
        }

        if(hero.hasTalent(THUNDER_STRIKE)){
            int dst = 1;
            if((hero.buff(Recharging.class)!=null || hero.buff(ArtifactRecharge.class)!=null)){
                dst += 1;
            }
            Shocking shocking = new Shocking();
            shocking.affected.clear();
            shocking.arcs.clear();

            shocking.arc1(attacker, enemy, dst, shocking.affected, shocking.arcs);
            if(hero.pointsInTalent(THUNDER_STRIKE)<2){
                shocking.affected.remove(enemy);
            }
            for (Char ch : shocking.affected) {
                if (ch.alignment != attacker.alignment  && ch.isAlive()) {
                    ch.damage(Math.round(dmg * 0.5f), new Electricity(), DamageTag.PHYSICAL, DamageTag.ELECTRIC);
                }
            }
            attacker.sprite.parent.addToFront( new Lightning( shocking.arcs, null ) );
            Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
        }

		return dmg;
	}

	static void showOozeAttackEffect(Char enemy) {
		if (enemy.sprite != null) {
			enemy.sprite.burst(0x000000, 5);
		}
	}

	public static void onRangedAttackHit(Char attacker, Char target) {
		if (hero != null && target == hero && attacker != hero
				&& hero.hasTalent(NO_VIEWRAPE) && hero.distance(attacker) > 1) {
			Buff.affect(attacker, Blindness.class, hero.pointsInTalent(NO_VIEWRAPE));
		}
	}

	public static int onDefenseProc(Char enemy, int damage, DamageTag... damageTags) {
        if(hero.hasTalent(ARCANE_SHIELD)){
            Armor a = hero.belongings.armor();
            RingOfKing ring = hero.belongings.getItem(RingOfKing.class);
            if(((a!=null && a.glyph!=null) ||
                    (ring != null && ring.glyph != null && !ring.cursed && ring.isEquipped(hero)))
            && hero.buff(ArcaneShieldCooldown.class)==null){
                Buff.affect(hero,ArcaneShieldCooldown.class,20);
                Buff.affect(hero,Barrier.class).incShield(1+hero.pointsInTalent(ARCANE_SHIELD));
            }
        }
		//史莱姆
		//此处应写在蜕变Talent.java中onDefenseProc
		if(hero.pointsInTalent(Talent.DARK_GAS)>0){
			boolean darkGasTrue =false;
			switch (hero.pointsInTalent(Talent.DARK_GAS)){
				case 3:
					if(hero.subClass.is(HeroSubClass.DARKSLIME) && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, StenchGas.class));
						darkGasTrue =true;
					}
				case 2:
					if(hero.subClass.is(HeroSubClass.DARKSLIME) && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, CorrosiveGas.class));
						darkGasTrue =true;
					}
				case 1:
					if(hero.subClass.is(HeroSubClass.DARKSLIME) && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, ToxicGas.class));
						darkGasTrue =true;

					}
					break;
			}
			if(darkGasTrue){
				Buff.affect(hero, BlobImmunity.class,4);
			}

		}

		if(hero.hasTalent(Talent.RAGE_ATTACK)){
			damage*=1+hero.pointsInTalent(Talent.RAGE_ATTACK)*0.1;
		}


		if (hero.pointsInTalent(Talent.SURVIVAL_VOLITION)>=1 && hero.HP<hero.HT*0.3 && hero.buffs(SurVolCooldown.class).isEmpty()){
			int duration=1+hero.pointsInTalent(Talent.SURVIVAL_VOLITION);
			Buff.affect(hero, Haste.class, duration);
			Buff.affect(hero,Talent.SurVolCooldown.class,80);
			Buff.affect(hero, PotionOfCleansing.Cleanse.class, duration);
			Buff.affect(hero,Talent.SurResistance.class,duration);
		}

		if(hero.pointsInTalent(COUNTER_ATTACK)>0 && Dungeon.level.distance(hero.pos,enemy.pos)<2 &&
				Random.Int(100)<5+hero.pointsInTalent(COUNTER_ATTACK)*15  && enemy.isAlive()){
			enemy.damage((int)(hero.lvl*(0.5)),new LifeLink(), DamageTag.PHYSICAL, DamageTag.NO_ARMOR);

		}

		if(!hero.buffs(Talent.SurResistance.class).isEmpty()){
			damage*=0.5;
		}
		if(!hero.buffs(Light.class).isEmpty() && hero.hasTalent(ANGEL_STANCE)  && enemy.isAlive()){
			enemy.damage((int)(damage*(0.2+0.1*hero.pointsInTalent(ANGEL_STANCE))), new WandOfMagicMissile(), DamageTag.MAGICAL);
		}
		if(hero.hasTalent(THORNY_ROSE)  && enemy.isAlive()){
			Buff.affect(enemy, Bleeding.class).set(1);
			if(hero.pointsInTalent(THORNY_ROSE)==2){
				enemy.damage(1,new LifeLink(), DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
			}
		}
		//史莱姆娘
		if(hero.buff(ResilientArmor.class) != null){
			/*
			int ShiledtoGive = Random.Int(1,3);
			Buff.affect(this, Barrier.class).setShield( ShiledtoGive );
			GLog.p(String.valueOf(ShiledtoGive));

			 */
			hero.buff(ResilientArmor.class).left--;
			if(hero.buff(ResilientArmor.class).left <= 0){
				hero.buff(ResilientArmor.class).detach();
			}
		}
		if(hero.subClass.is(HeroSubClass.DARKSLIME) && Random.Int(20)<3){
			Buff.affect(enemy,Ooze.class).set(20);
		}
		//史莱姆娘7.22
		if (hero.subClass.is(HeroSubClass.WATERSLIME) && hero.buff(Talent.SlimeMucusCooldown.class) == null){
			ArrayList<Integer> pos = new ArrayList<>();
			PathFinder.buildDistanceMap( hero.pos, BArray.not( Dungeon.level.solid, null ), 4 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					if (Dungeon.level.traps.get(i) == null	//无陷阱
							&& Actor.findChar( i) == null	//无单位
							&& Dungeon.level.passable[i]		//可通过
							&& Dungeon.level.map[i] != Terrain.EMPTY_SP){	//不为悬崖
						pos.add(i);
					}
				}
			}
			/*
			for(int i : PathFinder.NEIGHBOURS8){//循环判定角色周围八格
				if (Dungeon.level.traps.get(hero.pos + i) == null	//无陷阱
						&& Actor.findChar(hero.pos + i) == null	//无单位
						&& Dungeon.level.passable[hero.pos + i]		//？
						&& Dungeon.level.map[hero.pos + i] != Terrain.EMPTY_SP){	//不为悬崖
					pos.add(i);
				}
			}

			 */

			if(!pos.isEmpty()){
				int HPtoReduce = Math.max((int)(0.1 * hero.HP),1);
				if (hero.pointsInTalent(Talent.WATER_REVIVAL) == 3) {
					HPtoReduce = Math.max((int)(0.05 * hero.HP),1);
				}
				if (hero.HP > HPtoReduce){
					Buff.affect(hero, Talent.SlimeMucusCooldown.class, 30f);
					hero.HP -= HPtoReduce;
					hero.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(HPtoReduce), FloatingText.PHYS_DMG);
					if(hero.HP<0){
						hero.die(hero);
					}
				}
				SlimeMucus slime = new SlimeMucus();
				Collections.shuffle(pos);	//打乱pos
				slime.pos = pos.get(0);
				GameScene.add(slime, 1f);
				Dungeon.level.occupyCell(slime);
				Dungeon.level.pressCell(slime.pos);
				//Buff.affect(slime, Bleeding.class).set(6);
				//Buff.affect(slime, Barrier.class).setShield( hero.HT/2 );
			}
		}
		if(hero.hasTalent(SPIDER_SENSE) && !Dungeon.level.heroFOV[enemy.pos]){
			Buff.affect(hero,Swiftthistle.TimeBubble.class).reset1(hero.pointsInTalent(SPIDER_SENSE));
		}
		if(hero.subClass.is(HeroSubClass.AU400) && enemy.buff(InstructionTool.InstructionMark.class)!=null){
			damage*=0.8f;
		}
		if(hero.hasTalent(BULWARK_GREATSHIELD) && hasBlockingWeapon(hero)){
			damage = Math.round(damage * (hero.pointsInTalent(BULWARK_GREATSHIELD) == 1 ? 0.90f : 0.85f));
		}
		return (int)damage;
	}



	public static int onDamage(int dmg, Object src, DamageTag... damageTags) {
		EnumSet<DamageTag> tags = DamageTag.of(damageTags);
		boolean unavoidable = tags.contains(DamageTag.UNAVOIDABLE);
		if (unavoidable) {
			return dmg;
		}
		if (hero != null){
			AlchemyShield shield = hero.buff(AlchemyShield.class);
			if (shield != null){
				dmg = shield.absorb(dmg, src);
			}
			if (dmg >= hero.HP && triggerDyingWill(dmg, src, unavoidable)){
				dmg = Math.max(0, hero.HP - 1);
                Sample.INSTANCE.play( Assets.Sounds.DEATHSDOOR, 1, 1, Random.Float( 0.9f, 1.1f ) );
			}
		}
		if(hero.hasTalent(TBM) && hero.buff(TBMCooldown.class)==null){
			Buff.affect(hero,Barrier.class).incShield(1+2*hero.pointsInTalent(TBM));
			Buff.affect(hero,TBMCooldown.class,60);
		}
		if(hero.hasTalent(Talent.WELLFED_MEAL) && !hero.buffs(WellFed.class).isEmpty()){
			dmg*=1-hero.pointsInTalent(Talent.WELLFED_MEAL)*0.10f;
		}
		if(hero.hasTalent(BODY_REINFORCE) && hero.heroClass!=HeroClass.DM400
				&& bodyReinforceApplies(hero.pointsInTalent(BODY_REINFORCE), dmg)){
			dmg*=1f-0.05f*hero.pointsInTalent(BODY_REINFORCE);
		}
		if(!hero.buffs(Talent.NoSleep.class).isEmpty() &&  hero.hasTalent(Talent.GET_UP)){
			Buff.affect(hero, Adrenaline.class,2*hero.pointsInTalent(Talent.GET_UP)+1);
			hero.buff(Talent.NoSleep.class).detach();
		}
		if (hero.subClass.is(HeroSubClass.WATERSLIME)){
			Talent.SlimeMucusCooldown b = hero.buff(Talent.SlimeMucusCooldown.class);
			if(b !=null){
				b.Reduce();
			}
		}
		if(dmg>4 && hero.hasTalent(Talent.YOG_LARVA)){
			int maxcnt=Math.min(2,hero.pointsInTalent(Talent.YOG_LARVA));
			for(int cnt=0;cnt<maxcnt;cnt++){
				YogDzewa.Larva mob=new YogDzewa.Larva();
				//YogFist.BurningFist mob=new YogFist.BurningFist();
				//AllyBuff.affectAndLoot(mob, hero, ScrollOfSirensSong.Enthralled.class);
				Buff.affect(mob,ScrollOfSirensSong.Enthralled.class);
				GameScene.add( mob );
				ScrollOfTeleportation.appear( mob, hero.pos );
				if(hero.pointsInTalent(Talent.YOG_LARVA)==3 && Random.Int(2)==1){
					Class<?extends ChampionEnemy> buffCls;
					int random = 6;
					if(Dungeon.isChallenged(Challenges.EXTREME_ENVIRONMENT) && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)){
						random = 10;
					} else if(Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT) && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)){
						random = 8;
					}
					switch (Random.Int(random)){
						case 0: default:    buffCls = ChampionEnemy.Blazing.class;      break;
						case 1:             buffCls = ChampionEnemy.Projecting.class;   break;
						case 2:             buffCls = ChampionEnemy.AntiMagic.class;    break;
						case 3:             buffCls = ChampionEnemy.Giant.class;        break;
						case 4:             buffCls = ChampionEnemy.Blessed.class;      break;
						case 5:             buffCls = ChampionEnemy.Growing.class;      break;
						case 6:				buffCls = ChampionEnemy.Corrosion.class;      break;
						case 7:				buffCls = ChampionEnemy.Haste.class;      break;
						case 8:				buffCls = ChampionEnemy.Holy.class;      break;
						case 9:				buffCls = ChampionEnemy.Transform.class;      break;
					}
					Buff.affect(mob, buffCls);
				}
			}
		}
		if(src instanceof  Char && hero.hasTalent(Talent.DETOX_DAMAGE) && hero.pointsInTalent(Talent.DETOX_DAMAGE)*5>Random.Int(20)){
			Buff.affect(hero, PotionOfCleansing.Cleanse.class, 1);
			if(Random.Int(20)<3){
				for (Buff b :hero.buffs()){
					if (b.type == Buff.buffType.NEGATIVE
							&& !(b instanceof AllyBuff)
							&& !(b instanceof LostInventory)){
						b.detach();
					}
				}
			}
		}
		if (hero.hasTalent(Talent.CONCEPT_GRID)
				&& !tags.contains(DamageTag.DEFERRED)
				&& !tags.contains(DamageTag.HUNGER)) {
			dmg-=hero.pointsInTalent(Talent.CONCEPT_GRID);
		}

		if (hero.pointsNegative(Talent.UNAVOIDABLE) > 0
				&& !tags.contains(DamageTag.ELECTRIC)
				&& !tags.contains(DamageTag.BLEEDING)
				&& !tags.contains(DamageTag.HUNGER)
				&& !tags.contains(DamageTag.DEFERRED)) {
			dmg=Math.max((int)(hero.HT*0.05*hero.pointsNegative(Talent.UNAVOIDABLE)),dmg);
		}
		if (hero.buff(Talent.WarriorFoodImmunity.class) != null){
			if (hero.pointsInTalent(Talent.IRON_STOMACH) == 1)       dmg = Math.round(dmg*0.25f);
			else if (hero.pointsInTalent(Talent.IRON_STOMACH) == 2)  dmg = Math.round(dmg*0.00f);
		}
		if(hero.pointsNegative(HAND_SLIP)>0 && hero.buff(HandSlipVulnerability.class) != null){
			dmg *= 1 + 0.25f * hero.pointsNegative(HAND_SLIP);
		}
		if(hero.buff(Routine.preOverLoad.class)!=null){
			dmg*=0.5f;
		}
		if(hero.buff(Routine.OverLoad.class)!=null){
			if(hero.hasTalent(STRONG_PERSERVE)){
				dmg*=1-0.05f*hero.pointsInTalent(STRONG_PERSERVE);
			}else if(hero.buff(Routine.preOverLoad.class)==null){
				dmg*=1.5f;
			}
		}

		if (tags.contains(DamageTag.MAGICAL) && hero.belongings.armor() != null){
			int armDr = Random.NormalIntRange( hero.belongings.armor().DRMin(), hero.belongings.armor().DRMax());
			if (hero.STR() < hero.belongings.armor().STRReq()){
				armDr -= 2*(hero.belongings.armor().STRReq() - hero.STR());
			}
			if(hero.hasTalent(Talent.STRONGEST_SHIELD) && Random.Int(2)==1 && armDr>0){
				dmg -= armDr;
			}
		}

		if(hero.armorAbility!=null && hero.armorAbility instanceof Decoy && dmg>=hero.HP){
			ArrayList<Decoy.Decoyman> decoymen = Decoy.getDecoymanAlly();
			if(decoymen!=null  && decoymen.get(0).isAlive()){
				decoymen.get(0).damage(114514,src, DamageTag.PHYSICAL);
				return 0;
			}
		}
        if(hero.buff(MarchForward.Forward.class)!=null){
            MarchForward.Forward f=hero.buff(MarchForward.Forward.class);
            f.left-=1;
            if(f.left<=0){
                f.detach();
            }
            if(hero.hasTalent(DELAY_TACTIC) && src instanceof Char){
				GLog.w(Messages.get(MarchForward.class,"say"));
                Buff.affect((Char)src, Slow.class,2*hero.pointsInTalent(DELAY_TACTIC));
            }
        }

		return Math.round(dmg);
	}

	static boolean bodyReinforceApplies(int points, int dmg) {
		return dmg >= (points >= 2 ? 10 : 20);
	}

	public static boolean isUnavoidableDamage(Object src){
		return src instanceof Reason;
	}

	private static boolean triggerDyingWill(int dmg, Object src, boolean unavoidable){

		if (hero == null || !hero.hasTalent(NEVER_COMPROMISE)
				|| !hasSufferingOrVirtue(hero) || unavoidable) {
			return false;
		}
		int shift = 60 - 10 * hero.pointsInTalent(NEVER_COMPROMISE);
		if (hero.buff(Suffering.Fear.class) != null) Buff.extend(hero, Suffering.Fear.class, shift);
		else if (hero.buff(Suffering.Despair.class) != null) Buff.extend(hero, Suffering.Despair.class, shift);
		else if (hero.buff(Suffering.Paranoia.class) != null) Buff.extend(hero, Suffering.Paranoia.class, shift);
		else if (hero.buff(Suffering.Ecstasy.class) != null) Buff.extend(hero, Suffering.Ecstasy.class, shift);
		else if (hero.buff(Virtue.Firm.class) != null) Buff.extend(hero, Virtue.Firm.class, -shift);
		else if (hero.buff(Virtue.Fearless.class) != null) Buff.extend(hero, Virtue.Fearless.class, -shift);
		else if (hero.buff(Virtue.Inspire.class) != null) Buff.extend(hero, Virtue.Inspire.class, -shift);

		Buff.affect(hero, DyingWill.class);

		if (hero.sprite != null) {
			hero.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(DyingWill.class, "name"));
		}
		GLog.n(Messages.get(DyingWill.class, "get"));
		return true;
	}

	private static boolean hasSufferingOrVirtue(Char ch){
		return hasSuffering(ch)
				|| ch.buff(Virtue.Firm.class) != null
				|| ch.buff(Virtue.Fearless.class) != null
				|| ch.buff(Virtue.Inspire.class) != null;
	}

	private static boolean hasSuffering(Char ch){
		return ch.buff(Suffering.Fear.class) != null
				|| ch.buff(Suffering.Despair.class) != null
				|| ch.buff(Suffering.Paranoia.class) != null
				|| ch.buff(Suffering.Ecstasy.class) != null;
	}

	public static int beforeSufferingReasonLoss(Hero hero, int lose){
		Reason r = hero == null ? null : hero.buff(Reason.class);
		if (r == null || !hero.hasTalent(REWIND_TIME) || hero.buff(RewindTimeUsed.class) != null){
			return lose;
		}
		if (hasSuffering(hero) && r.reason >= 20 && r.reason - lose < 20){
			Reason.gainReason(hero, 20 + 10 * hero.pointsInTalent(REWIND_TIME));
			Buff.detach(hero, Panic.class);
			Buff.affect(hero, RewindTimeUsed.class,50f);
		}
		return lose;
	}

	public static class RewindTimeUsed extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}


		@Override
		public int icon() {
			return BuffIndicator.TIME;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.64f, 0.53f, 0.42f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, visualcooldown() / 50);
		}
	}

	public static void onWandProc( Char target, int wandLevel, int chargesUsed,int dmg){
		if(hero != null && hero.hasTalent(Talent.INSINUATION) && target instanceof Mob && ((Mob) target).state== ((Mob) target).SLEEPING){
			Buff.affect(target, Poison.class).set(1+2*hero.pointsInTalent(Talent.INSINUATION));
		}
		if (hero.hasTalent(Talent.ARCANE_VISION)) {
			int dur = 5 + 5* hero.pointsInTalent(Talent.ARCANE_VISION);
			Buff.append(hero, TalismanOfForesight.CharAwareness.class, dur).charID = target.id();
		}
		if (hero.hasTalent(Talent.BURNING_CURSE) && !target.buffs(Burning.class).isEmpty()){
			if(hero.pointsInTalent(Talent.BURNING_CURSE)==1){
				Buff.affect(target, Cripple.class,2);
			}else if(hero.pointsInTalent(Talent.BURNING_CURSE)==2){
				Buff.affect(target, Cripple.class,2);
				Buff.affect(target, Blindness.class,2);
			}
		}
		if(hero.hasTalent(Talent.MORONITY) && target!=hero ){
			switch (Random.Int(3)){
				case 0:
					Buff.affect(target,Weakness.class,hero.pointsInTalent(Talent.MORONITY));
					break;
				case 1:
					Buff.affect(target,Vulnerable.class,hero.pointsInTalent(Talent.MORONITY));
					break;
				case 2:
					Buff.affect(target, Hex.class,hero.pointsInTalent(Talent.MORONITY));
					break;
			}
		}

		if(hero.hasTalent(Talent.YOG_FIST) && !hero.buffs(Talent.YogFistPower.class).isEmpty() && target != hero){
			Buff.affect(target, Blindness.class,5);
			if(hero.pointsInTalent(Talent.YOG_FIST)==3){
				Buff.affect(target, Roots.class,5);
			}
		}
		if(hero.hasTalent(Talent.YOG_RAY) && hero.buffs(Talent.YogRayCooldown.class).isEmpty() && target != hero){
			Buff.affect(hero, Talent.YogRayCooldown.class,20);
			ArrayList<Integer> targetedCells = new ArrayList<>();
			HashSet<Integer> affectedCells = new HashSet<>();
			HashSet<Char> affected = new HashSet<>();
			for (int i = 0; i < hero.pointsInTalent(Talent.YOG_RAY)*2-1; i++){
				int targetPos = target.pos;
				if (i != 0){
					do {
						targetPos = target.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
					} while (Dungeon.level.trueDistance(hero.pos, target.pos)
							> Dungeon.level.trueDistance(hero.pos, target.pos));
				}
				targetedCells.add(targetPos);
				Ballistica b = new Ballistica(hero.pos, targetPos, Ballistica.WONT_STOP);
				affectedCells.addAll(b.path);
			}
			for (int i : targetedCells) {
				Ballistica b = new Ballistica(hero.pos, i, Ballistica.WONT_STOP);
				//shoot beams
				hero.sprite.parent.add(new Beam.DeathRay(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
				for (int p : b.path) {
					Char ch = Actor.findChar(p);
					if (ch != null && ch!=hero && ch.alignment != Char.Alignment.ALLY  && ch.isAlive()) {
						ch.damage(Random.NormalIntRange(40, 50+hero.pointsInTalent(Talent.YOG_RAY)*10), new Eye.DeathGaze(), DamageTag.MAGICAL);
					}
				}
			}
		}
		if(hero.hasTalent(Talent.QUANTUM_HACKING) && !(target instanceof NPC) && !(target instanceof Hero)){
			Buff.affect(target, Vertigo.class,hero.pointsInTalent(Talent.QUANTUM_HACKING));
		}
        if(hero.hasTalent(Talent.EAT_MIND) && dmg>0){
            Buff.affect(hero, Hunger.class).satisfy((0.1f + 0.1f*hero.pointsInTalent(Talent.EAT_MIND))*dmg);
        }



	}

	public static void ontryToZap( Hero owner, int target ){
		if (hero.hasTalent(Talent.GAS_SPURT) && hero.buff(Talent.GasCooldown.class)==null && Dungeon.level.distance(hero.pos,target)>2){
			if(hero.pointsInTalent(Talent.GAS_SPURT)==2){
				GameScene.add( Blob.seed( target, 100, CorrosiveGas.class ).setStrength( 2 + Dungeon.scalingDepth()/5));
				Buff.affect(hero, Talent.GasCooldown.class,15);
			}else if(hero.pointsInTalent(Talent.GAS_SPURT)==1){
				GameScene.add( Blob.seed( target, 100, ToxicGas.class ) );
				Buff.affect(hero, Talent.GasCooldown.class,15);
			}
		}

		if(hero.hasTalent(FIRE_BALL) && Dungeon.level.flamable[target]){
			GameScene.add(Blob.seed(target, hero.pointsInTalent(FIRE_BALL), Fire.class));
		}
	}

    public static void onToZap( Hero owner, int cell ){
        if(owner.hasTalent(DIRECTIONAL_COLLAPSE)){
            DirectionalCollapse dc = owner.buff(DirectionalCollapse.class);
            if(dc==null){
                dc = Buff.affect(owner, DirectionalCollapse.class).set(1);
            }else{
                dc.left+=1;
            }
            if(dc.left >= 5-owner.pointsInTalent(DIRECTIONAL_COLLAPSE)){
                dc.left=0;
                dropStone(cell);
            }
        }
    }
	public static int onWandDamage(Wand wand, Char target, int dmg){
		if (hero == null){
			return dmg;
		}
		boolean frostTarget = target != null && target.buff(Frost.class) != null;
		Chill chill = target == null ? null : target.buff(Chill.class);
		if (hero.hasTalent(PENETRATING_CAST) && target != null && target.alignment == Char.Alignment.ENEMY){
			int removed = 0;
			for (Buff buff : new ArrayList<>(target.buffs())){
				if (buff.type == Buff.buffType.NEGATIVE && !(buff instanceof Doom)){
					buff.detach();
					removed++;
				}
			}
			if (removed > 0){
				dmg = Math.round(dmg * (1f + removed * 0.1f * hero.pointsInTalent(PENETRATING_CAST)));
			}
		}
		if (wand instanceof WandOfFrost){
			if (frostTarget && hero.pointsInTalent(ICE_HELL) >= 2){
				dmg += Math.max(0, wand.buffedLvl()) * 4;
			} else if (chill != null && hero.pointsInTalent(ICE_HELL) < 1){
				float chillturns = Math.min(10, chill.cooldown());
				dmg = (int)Math.round(dmg * Math.pow(0.9333f, chillturns));
			}
		}
		dmg = magicGirlWandDamage(dmg, hero.pointsInTalent(Talent.MAGIC_GIRL),
				hero.hasTalent(Talent.MAGIC_GIRL) && !hero.buffs(HeroDisguise.class).isEmpty());
        if (hero.hasTalent(Talent.EXTREME_CASTING)){
            dmg=(int)(dmg*(1f+0.2f*hero.pointsInTalent(Talent.EXTREME_CASTING)));
        }
        if(hero.hasTalent(Talent.EMPOWERING_LIFE) && hero.shielding()>0){
            dmg=(int)(dmg*(1f+0.1f*hero.pointsInTalent(Talent.EMPOWERING_LIFE)));;
        }

        if(hero.hasTalent(Talent.STATIC_LIGHT)){
            dmg+=hero.pointsInTalent(Talent.STATIC_LIGHT);
        }
        if(hero.pointsInTalent(Talent.MARKSMAN)>1 && wand instanceof WandOfMagicMissile){
            dmg+=3;
        }
		return dmg;
	}

	static float afraidDeathDamageMultiplier(int points) {
		return 0.8f - 0.1f * points;
	}

	static int magicGirlWandDamage(int damage, int points, boolean disguised) {
		return disguised ? (int) (damage * (1f + 0.2f * points)) : damage;
	}

	public static void onArmorAbility( Hero hero, float chargeUse ){
		if(hero.pointsInTalent(Talent.SUMMON_FOLLOWER)>=1){
			ArrayList<Integer> respawnPoints = new ArrayList<>();

			for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
				int p = hero.pos + PathFinder.NEIGHBOURS9[i];
				if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
					respawnPoints.add( p );
				}
			}

			int spawned = 0;
			while (spawned < hero.pointsInTalent(Talent.SUMMON_FOLLOWER) && respawnPoints.size() > 0) {
				int index = Random.index( respawnPoints );
				if (Random.Int(2)==0){
					Monk mob = new Monk();
					Buff.affect( mob, ScrollOfSirensSong.Enthralled.class);
					mob.state= mob.HUNTING;
					GameScene.add( mob );
					ScrollOfTeleportation.appear( mob, respawnPoints.get( index ) );

				}else {
					Warlock mob = new Warlock();
					Buff.affect( mob, ScrollOfSirensSong.Enthralled.class);
					mob.state= mob.HUNTING;
					GameScene.add( mob );
					ScrollOfTeleportation.appear( mob, respawnPoints.get( index ) );
				}
				respawnPoints.remove( index );

				spawned++;
			}
		}
	}
	public static void onEmenyDie(Char emeny,  Object cause ){
		if (cause == hero || cause instanceof Weapon || cause instanceof Weapon.Enchantment){
			if (hero.hasTalent(Talent.LETHAL_MOMENTUM)
					&& Random.Float() < 0.34f + 0.33f* hero.pointsInTalent(Talent.LETHAL_MOMENTUM)){
				Buff.affect(hero, Talent.LethalMomentumTracker.class, 0f);
			}
			if (hero.heroClass != HeroClass.DUELIST
					&& hero.hasTalent(Talent.LETHAL_HASTE)
					&& hero.buff(Talent.LethalHasteCooldown.class) == null
					&& !canTriggerLethalHasteWithWeaponAbility(hero)){
				Buff.affect(hero, Talent.LethalHasteCooldown.class, 100f);
				Buff.affect(hero, GreaterHaste.class).set(2 + 2* hero.pointsInTalent(Talent.LETHAL_HASTE));
			}
			if(hero.hasTalent(Talent.CICADA_DANCE)){
				Buff.affect(hero, Invisibility.class,1+hero.pointsInTalent(Talent.CICADA_DANCE));
				Buff.affect(hero,EnemyDies.class);
			}
		}
		if(hero.hasTalent(Talent.INVINCIBLE)){
			Buff.affect(hero,Adrenaline.class,hero.pointsInTalent(Talent.INVINCIBLE)*2+1);
		}
		if( hero.pointsInTalent(Talent.JUSTICE_PUNISH)>=1 && (emeny.properties().contains(Char.Property.UNDEAD)|| emeny.properties().contains(Char.Property.DEMONIC))) {
			Buff.affect(hero, Bless.class, hero.pointsInTalent(Talent.JUSTICE_PUNISH));
		}

		if(hero != null && hero.subClass.is(HeroSubClass.DARKSLIME) && hero.pointsInTalent(Talent.DARK_LIQUID)==3 && emeny.buff(Roots.class)!=null){
			Buff.affect(hero, Recharging.class,8);
			Buff.affect(hero, ArtifactRecharge.class).extend(5);
			Talent.DarkHookCooldown b = hero.buff(Talent.DarkHookCooldown.class);
			if(b!=null && cause != Chasm.class){
				b.detach();
			}
		}
        if(emeny.buff(Burning.class)!=null && hero.pointsInTalent(FLAME_INCARNATION)>Random.Int(10)){
			Elemental elemental = new Elemental.FireElemental();
			GameScene.add( elemental );
			Buff.affect(elemental, SummonElemental.InvisAlly.class);
			elemental.setSummonedALly();
			elemental.HP = elemental.HT;
			ScrollOfTeleportation.appear( elemental, emeny.pos );
		}
		if(hero.hasTalent(Talent.KILL_CONTINUE) && hero.buff(OneSword.OKU_OneSword.class)!=null
				&& cause==hero  && hero.belongings.attackingWeapon() instanceof MeleeWeapon){
			Buff.affect(hero, OneSword.OKU_OneSword.class, hero.pointsInTalent(Talent.KILL_CONTINUE));
		}
		if(hero.hasTalent(Talent.BURNING_BLOOD)){
            hero.heal(hero.pointsInTalent(Talent.BURNING_BLOOD));

		}
		if(hero.pointsNegative(Talent.FULLFIGHTING)*3>Random.Int(10) && hero.pos != -1 ){
			for (Mob m : Dungeon.level.mobs) {
				m.beckon( hero.pos );
			}
			if (Dungeon.level.heroFOV[emeny.pos]) {
				CellEmitter.center( emeny.pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
			}
			Sample.INSTANCE.play( Assets.Sounds.ALERT );
			switch (Random.Int(3)){
				case 0:
					GLog.n("战斗，爽！");
					break;
				case 1:
					GLog.n("我的武器已经饥渴难耐。");
					break;
				case 2:
					GLog.n("我要打十个！");
					break;
			}
		}
		if(cause instanceof Hero && hero.hasTalent(NINJA_SOCIAL)){
			Buff.affect(hero, NinjaSocial.class).set(1+2*hero.pointsInTalent(NINJA_SOCIAL));
		}
		if(cause instanceof Hero && hero.hasTalent(KILL_SPREE)){
			Buff.affect(hero, Healing.class).setHeal((int)((0.025f+0.025f*hero.pointsInTalent(KILL_SPREE))*emeny.HT),0.25f, 0);
			Buff.affect(hero,ArtifactRecharge.class).extend(1+1*hero.pointsInTalent(KILL_SPREE));
		}
		if(hero.subClass.is(HeroSubClass.PIOUS)){
			RitualDagger ritualDagger = hero.belongings.getItem(RitualDagger.class);
			if (ritualDagger != null){
				ritualDagger.onKill(hero);
			}
		}
		if (cause == hero && emeny.alignment == Char.Alignment.ENEMY
				&& hero.belongings.attackingWeapon() instanceof SakuraBlossomBlade) {
			((SakuraBlossomBlade) hero.belongings.attackingWeapon()).onDirectKill();
		}
		if (cause == hero && emeny.alignment == Char.Alignment.ENEMY
				&& hero.belongings.attackingWeapon() instanceof Gungnir) {
			((Gungnir) hero.belongings.attackingWeapon()).onDirectKill(hero, emeny);
		}
        if(hero.heroClass==HeroClass.FRIAR){
			int virtueKillExtend = hero.subClass.is(HeroSubClass.PIOUS) && hero.hasTalent(BRIGHT_WARRIOR) ? 3 + hero.pointsInTalent(BRIGHT_WARRIOR)*3 : 3;
            if(emeny instanceof YogDzewa){
                Reason.gainReason(hero,100);
                if(hero.buff(Panic.class)!=null){
                    hero.buff(Panic.class).detach();
                }
            }else if(emeny.properties().contains(Char.Property.BOSS)){
                Reason.gainReason(hero,50);
                if(hero.buff(Panic.class)!=null){
                    hero.buff(Panic.class).detach();
                }
            }else if(emeny.properties().contains(Char.Property.MINIBOSS)){
                Reason.gainReason(hero,10);
            }else if(emeny instanceof Wraith){
                Reason.gainReason(hero,2);
                if(hero.buff(Panic.class)!=null){
                    hero.buff(Panic.class).detach();
                }
            }else if(emeny.properties().contains(Char.Property.UNDEAD) || emeny.properties().contains(Char.Property.DEMONIC)){
                Reason.gainReason(hero,Random.Int(2,6));
            }else{
                Reason.gainReason(hero,Random.Int(1,3));
            }
            if(hero.buff(Suffering.Fear.class)!=null){
                if(emeny instanceof YogDzewa){
                    hero.buff(Suffering.Fear.class).detach();
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    if (hero.buff(Suffering.Fear.class).cooldown() <= 100) {
                        hero.buff(Suffering.Fear.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Fear.class,-100);
                    }
                }else{
                    if (hero.buff(Suffering.Fear.class).cooldown() < 6) {
                        hero.buff(Suffering.Fear.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Fear.class,-6);
                    }
                }
            }else if(hero.buff(Suffering.Despair.class)!=null){
                if(emeny instanceof YogDzewa){
                    hero.buff(Suffering.Despair.class).detach();
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    if (hero.buff(Suffering.Despair.class).cooldown() <= 100) {
                        hero.buff(Suffering.Despair.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Despair.class,-100);
                    }
                }else{
                    if (hero.buff(Suffering.Despair.class).cooldown() < 6) {
                        hero.buff(Suffering.Despair.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Despair.class,-6);
                    }
                }
            }else if(hero.buff(Suffering.Paranoia.class)!=null){
                if(emeny instanceof YogDzewa){
                    hero.buff(Suffering.Paranoia.class).detach();
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    if (hero.buff(Suffering.Paranoia.class).cooldown() <= 100) {
                        hero.buff(Suffering.Paranoia.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Paranoia.class,-100);
                    }
                }else{
                    if (hero.buff(Suffering.Paranoia.class).cooldown() < 6) {
                        hero.buff(Suffering.Paranoia.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Paranoia.class,-6);
                    }
                }
            }else if(hero.buff(Suffering.Ecstasy.class)!=null){
                if(emeny instanceof YogDzewa){
                    hero.buff(Suffering.Ecstasy.class).detach();
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    if (hero.buff(Suffering.Ecstasy.class).cooldown() <= 100) {
                        hero.buff(Suffering.Ecstasy.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Ecstasy.class,-100);
                    }
                }else{
                    if (hero.buff(Suffering.Ecstasy.class).cooldown() < 6) {
                        hero.buff(Suffering.Ecstasy.class).detach();
                    }else{
                        Buff.extend(hero,Suffering.Ecstasy.class,-6);
                    }
                }
            }

            if(hero.buff(Virtue.Firm.class)!=null){
                if(emeny instanceof YogDzewa){
                    Buff.extend(hero,Virtue.Firm.class,150);
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    Buff.extend(hero,Virtue.Firm.class,50);
                }else{
                    Buff.extend(hero,Virtue.Firm.class,virtueKillExtend);
                }
                if(Random.Int(4)==0){
                    Reason.VirtueReason(hero,6);
                }
            }else if(hero.buff(Virtue.Fearless.class)!=null){
                if(emeny instanceof YogDzewa){
                    Buff.extend(hero,Virtue.Fearless.class,150);
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    Buff.extend(hero,Virtue.Fearless.class,50);
                }else{
                    Buff.extend(hero,Virtue.Fearless.class,virtueKillExtend);
                }

            }else if(hero.buff(Virtue.Inspire.class)!=null){
                if(emeny instanceof YogDzewa){
                    Buff.extend(hero,Virtue.Inspire.class,150);
                }else if(emeny.properties().contains(Char.Property.BOSS)){
                    Buff.extend(hero,Virtue.Inspire.class,50);
                }else{
                    Buff.extend(hero,Virtue.Inspire.class,virtueKillExtend);
                }
                if(Random.Int(4)==0){
                    hero.heal(hero.HT/10);
                }
            }
        }
        if((emeny.properties().contains(Char.Property.UNDEAD) || emeny.properties().contains(Char.Property.DEMONIC)) && hero.hasTalent(OTHERWORLD_BANE)){
            Buff.affect( hero, MindVision.class, 1+hero.pointsInTalent(OTHERWORLD_BANE) );
            SpellSprite.show(hero, SpellSprite.VISION, 1, 0.77f, 0.9f);
            Dungeon.observe();
        }
	}

	public static void onMobDie(Mob mob,  Object cause ){

		if(mob.properties().contains(INORGANIC) &&  hero.pointsInTalent(Talent.ENGINEER_REFIT)> Random.Int(8)
				&& !((mob instanceof Wraith) || (mob instanceof Skeleton)) ){
			DM100 m= new DM100();
			Buff.affect(m, Corruption.class);
			GameScene.add( m );
			ScrollOfTeleportation.appear( m, mob.pos );
		}
		if(hero.pointsNegative(Talent.PARASITISM)>Random.Int(10) && !(mob instanceof NPC)){
			YogDzewa.Larva m = new YogDzewa.Larva();
			GameScene.add( m );
			ScrollOfTeleportation.appear( m, mob.pos );
		}
        if(hero.hasTalent(Talent.MANA_WREATH) && mob.buff(ManaWreath.class)!=null){
            Buff.affect(hero, ArcaneArmor.class).set(10, 5*hero.pointsInTalent(Talent.MANA_WREATH));
        }

		if(hero.hasTalent(Talent.ASH_LEDGER)){
			Buff.affect(hero, Barrier.class).setShield(hero.pointsInTalent(Talent.ASH_LEDGER)*2);
		}

	}

    public static void dropStone( int cell ) {

        Dungeon.hero.interrupt();
        final int rockCenter;
        rockCenter = cell;
        /*
        Char enemy = Dungeon.level.findMob(cell);
        //knock back 2 tiles if adjacent
        if(enemy!=null && Dungeon.level.adjacent(hero.pos, cell) ){
            int oppositeAdjacent = cell + (cell - hero.pos);
            Ballistica trajectory = new Ballistica(cell, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(enemy, trajectory, 2, false, false, hero);
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 2));

            //knock back 1 tile if there's 1 tile of space
        } else if (enemy!=null && Dungeon.level.distance(hero.pos, cell) == 2) {
            int oppositeAdjacent = cell + (cell - hero.pos);
            Ballistica trajectory = new Ballistica(cell, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(enemy, trajectory, 1, false, false, hero);

            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 1));

            //otherwise no knockback
        } else {
            rockCenter = cell;
        }

         */

        int safeCell;
        do {
            safeCell = rockCenter + PathFinder.NEIGHBOURS8[Random.Int(8)];
        } while (safeCell == hero.pos
                || (Dungeon.level.solid[safeCell] && Random.Int(2) == 0)
                && Random.Int(2) == 0);

        ArrayList<Integer> rockCells = new ArrayList<>();

        int start = rockCenter - Dungeon.level.width() * 3 - 3;
        int pos;
        for (int y = 0; y < 7; y++) {
            pos = start + Dungeon.level.width() * y;
            for (int x = 0; x < 7; x++) {
                if (!Dungeon.level.insideMap(pos)) {
                    pos++;
                    continue;
                }
                //add rock cell to pos, if it is not solid, and isn't the safecell
                if (!Dungeon.level.solid[pos] && pos != safeCell && Random.Int(Dungeon.level.distance(rockCenter, pos)) == 0) {
                    rockCells.add(pos);
                }
                pos++;
            }
        }
        if (!Dungeon.level.solid[rockCenter] && rockCenter != safeCell) {
            rockCells.add(rockCenter);
        }
        for (int i : rockCells){
            hero.sprite.parent.add(new TargetedCell(i, 0x00FF00));
        }
        Buff.append(hero, FallingStoneBuff.class, 1 * Actor.TICK).setRockPositions(rockCells);

    }
    public static class FallingStoneBuff extends DelayedRockFall {

        @Override
        public void affectChar(Char ch) {
            if (ch.alignment != Char.Alignment.ALLY){
                ch.damage(Random.NormalIntRange(10, 20), this,
						DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
                Buff.prolong(ch, Paralysis.class, 2);
            }
        }

    }

	public static class AquaticRecover extends Buff {
		private float AquaticRecover_cnt=0;
		{
			revivePersists = true;
		}
		private float AquaticRecover_total=0;

		@Override
		public  boolean act() {
			//在水面上每10/5回合恢复一点生命值
			if (((Hero) target).hasTalent(Talent.AQUATIC_RECOVER) && !((Hero) target).flying && Dungeon.level.water[((Hero) target).pos]) {
				//if (Dungeon.level.water[((Hero)target).pos]){
				float partAquaticRecover = hero.pointsInTalent(Talent.AQUATIC_RECOVER);
				/*
				if(((Hero)target).isStarving()){
					partAquaticRecover*=0.5f;
				}

				 */
				int lvl=((Hero) target).lvl;
				if(((Hero) target).hasTalent(Talent.AQUATIC_RECOVER) && ((Hero) target).HP < ((Hero) target).HT && AquaticRecover_total<(lvl*5+15)*lvl){
					AquaticRecover_cnt+=partAquaticRecover;
				}
				if (AquaticRecover_cnt >= 10 && ((Hero) target).HP < ((Hero) target).HT && AquaticRecover_total<(lvl*5+15)*lvl) {
					((Hero) target).heal(1);
					AquaticRecover_total += 1;
					AquaticRecover_cnt = 0;
				}

			}
			spend(TICK);
			return true;
		}
		private static final String TOTAL    = "total";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TOTAL, AquaticRecover_total);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			AquaticRecover_total = bundle.getInt(TOTAL);
		}
		
	}
	public static class YogFistPower extends FlavourBuff {
		{ type = Buff.buffType.POSITIVE; }
		//public static final float DURATION	= 5f;
		//public float left=DURATION;
		public int icon() { return BuffIndicator.UPGRADE; }
		@Override
		public void tintIcon(Image icon) {

			icon.hardlight(0.341f, 0.267f, 0.380f);
		}
		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
		//@Override
		public void detach() {
			super.detach();
		}
		/*
		@Override
		public  boolean act() {

			//Potion.splash( hero.pos );

			if(hero.pointsInTalent(Talent.YOG_FIST)>1){
				for (int i : PathFinder.NEIGHBOURS9) {
					//int vol = Fire.volumeAt(hero.pos+i, Fire.class);
					if (!Dungeon.level.solid[hero.pos + i] ){
						GameScene.add( Blob.seed( hero.pos + i, 2, Fire.class ) );
					}
				}
			}
			super.act();
			spend(TICK);
			return true;
		}

		 */

	}
	public static class ProvokedAngerTracker extends FlavourBuff{
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.WEAPON; }
		public void tintIcon(Image icon) { icon.hardlight(1.43f, 1.43f, 1.43f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	}
	public static class LingeringMagicTracker extends FlavourBuff{
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.WEAPON; }
		public void tintIcon(Image icon) { icon.hardlight(1.43f, 1.43f, 0f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	}
	public static class SuckerPunchTracker extends Buff{};
	public static class FollowupStrikeTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.75f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	};

	public static class NaturalChildBarkskin extends Barkskin {
		{
			type = buffType.POSITIVE;
		}

		@Override
		protected int decayAmount() {
			return 1;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.25f, 1.0f, 0.25f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (20 - level()) / 20f);
		}
	}

	public static class NaturalChildCooldown extends FlavourBuff {
		@Override
		public int icon() {
			return BuffIndicator.TIME;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.25f, 1.0f, 0.25f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, visualcooldown() / 25f);
		}

		@Override
		public void detach() {
			if (target == Dungeon.hero && Dungeon.hero.hasTalent(NATURAL_CHILD)) {
				ActionIndicator.setAction(Dungeon.hero.buff(NaturalChildAction.class));
				BuffIndicator.refreshHero();
			}
			super.detach();
		}
	}

	public static class NaturalChildAction extends Buff implements ActionIndicator.Action {
		{
			actPriority = BUFF_PRIO - 1;
			revivePersists = true;
		}

		@Override
		public void detach() {
			super.detach();
			ActionIndicator.clearAction(this);
		}

		@Override
		public int icon() {
			return BuffIndicator.BARKSKIN;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.25f, 1.0f, 0.25f);
		}

		@Override
		public boolean act() {
			if (target == Dungeon.hero && Dungeon.hero.hasTalent(NATURAL_CHILD)
					&& target.buff(NaturalChildCooldown.class) == null) {
				ActionIndicator.setAction(this);
			} else {
				ActionIndicator.clearAction(this);
			}
			spend(TICK);
			return true;
		}

		@Override
		public String actionName() {
			return Messages.get(this, "action_name");
		}

		@Override
		public int indicatorColor() {
			return 0x36B83F;
		}

		@Override
		public int actionIcon() {
			return HeroIcon.WARDEN;
		}

		@Override
		public void doAction() {
			if (!usable()) return;

			Hero hero = Dungeon.hero;
			int points = hero.pointsInTalent(NATURAL_CHILD);
			int barkskin = points >= 2 ? 20 : 15;
			NaturalChildFurrowCounter counter = Buff.affect(hero, NaturalChildFurrowCounter.class);
			boolean canGrowHighGrass = Regeneration.regenOn() && counter.canUse(points);
			NaturalChildBarkskin armor = Buff.affect(hero, NaturalChildBarkskin.class);
			armor.set(barkskin, 1);

			if (Dungeon.level != null) {
				for (int offset : PathFinder.NEIGHBOURS9) {
					int cell = hero.pos + offset;
					if (!Dungeon.level.insideMap(cell)) continue;

					Char ch = Actor.findChar(cell);
					if (points >= 2 && ch != null && ch.alignment == Char.Alignment.ENEMY) {
						Buff.affect(ch, Roots.class, 2f);
					}

					int terrain = Dungeon.level.map[cell];
					boolean canGrow = terrain == Terrain.EMPTY || terrain == Terrain.EMBERS
							|| terrain == Terrain.EMPTY_DECO || terrain == Terrain.GRASS
							|| terrain == Terrain.FURROWED_GRASS;
					if (canGrow && Dungeon.level.plants.get(cell) == null
							&& terrain != Terrain.HIGH_GRASS) {
						Level.set(cell, canGrowHighGrass ? Terrain.HIGH_GRASS : Terrain.FURROWED_GRASS);
						GameScene.updateMap(cell);
						if (canGrowHighGrass) {
							CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 4);
						}
					}
				}
				Dungeon.observe();
			}
			counter.consumeUse(points);

			Buff.affect(hero, NaturalChildCooldown.class, 25f);
			ActionIndicator.clearAction(this);
			BuffIndicator.refreshHero();
		}

		@Override
		public boolean usable() {
			return target == Dungeon.hero && Dungeon.hero != null
					&& Dungeon.hero.hasTalent(NATURAL_CHILD)
					&& Dungeon.hero.buff(NaturalChildCooldown.class) == null;
		}
	}
	public static class SmokeMask extends Buff implements ActionIndicator1.Action {
		{
			//always acts after other buffs, so invisibility effects can process first
			actPriority = BUFF_PRIO - 1;
			revivePersists = true;
		}
		@Override
		public void detach() {
			super.detach();
			ActionIndicator1.clearAction(this);
		}
		@Override
		public int icon() {
			return BuffIndicator.SMOKEMASK;
		}
		@Override
		public boolean act() {
			if(target.buff(SmokeCooldown.class)!=null){
				ActionIndicator1.clearAction(this);
			}else if(hero.hasTalent(SMOKE_MASK)){
				ActionIndicator1.setAction(this);
				BuffIndicator.refreshHero();
			}

			spend(TICK);
			return true;
		}


		@Override
		public String actionName() {
			return Messages.get(this, "action_name");
		}
		@Override
		public int indicatorColor() {
			return 0x262322;
		}
		@Override
		public int actionIcon() {
			return HeroIcon.SMOKEMASK;
		}
		@Override
		public void doAction() {
			Buff.affect(hero, SmokeCooldown.class, 24f);
			GameScene.add( Blob.seed( hero.pos, 50+50*hero.pointsInTalent(SMOKE_MASK), SmokeScreen.class ) );
			ActionIndicator1.clearAction(this);
			BuffIndicator.refreshHero();
		}
		@Override
		public boolean usable() {
			return target == Dungeon.hero
					&& Dungeon.hero != null
					&& Dungeon.hero.hasTalent(SMOKE_MASK)
					&& target.buff(SmokeCooldown.class) == null;
		}
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);

		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
		}
	}

	private static boolean canTriggerLethalHasteWithWeaponAbility(Hero hero) {
		if (hero == null) {
			return false;
		}
		if ((hero.belongings.weapon() instanceof MeleeWeapon
				&& ((MeleeWeapon) hero.belongings.weapon()).canUseWeaponAbilityAction(hero))
				|| (hero.belongings.secondWep() instanceof MeleeWeapon
				&& ((MeleeWeapon) hero.belongings.secondWep()).canUseWeaponAbilityAction(hero))) {
			return true;
		}
		RingOfForce force = hero.belongings.getItem(RingOfForce.class);
		return force != null
				&& force.isEquipped(hero)
				&& MeleeWeapon.canUseWeaponAbility(hero);
	}

	public static final int MAX_TALENT_TIERS = 4;

	public static void initClassTalents( Hero hero ){
		initClassTalents( hero, hero.talents, hero.metamorphedTalents ,hero.sublimationTalents);
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
		initClassTalents( cls, talents, new LinkedHashMap<>(), new LinkedHashMap<>());
	}

	public static void initClassTalents( Hero hero, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements ,LinkedHashMap<Talent, String> sublimation){
		if (HeroRandomizer.hasRandomClassTalents(hero)){
			initRandomClassTalents(hero, talents, replacements, sublimation);
		} else {
			initClassTalents(HeroRandomizer.talentClass(hero), talents, replacements, sublimation);
		}
	}

	public static void initNegativeTalent(HeroClass cls,ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Integer> negativeTalents){
		if(negativeTalents.isEmpty()){
			return;
		}
		for(Talent t:negativeTalents.keySet()){
			talents.get(negativeTalents.get(t)).put(t, 0);
		}

	}





	public static ArrayList<Talent> getNegativeTalent(){
		//版本限定
		int xianding = 1;

		ArrayList<Talent> Negatives=new ArrayList<>();
		for(Talent t:negativeTalent.get(0)){
			Negatives.add(t);
		}
		for(Talent t:negativeTalent.get(xianding)){
			Negatives.add(t);
		}
		long seed=Dungeon.seed;
		Random random = new Random();
		random.pushGenerator(seed);

		random.shuffle(Negatives);
		return Negatives;
		/*
		ArrayList<Talent> Negatives=new ArrayList<>();
		Collections.addAll(Negatives,ETERNAL_CURSE,EATER, FATE_DECISION,ENDLESS_MALICE,FEEBLE, MALNUTRITION,
				SHORTSIGHTED,BAT_SERUM, MYOPIA,JIULONGLA_COFFIN, COWBOY,MAMBA_OUT,EXPLOSION_MEAL,
				HANDON_GROUND,CHILL_WATER,LIFE_SPORT, WEAKEN_CHALICE,MENTAL_COLLAPSE,UNAVOIDABLE,
				PARASITISM,DUMP_TRUCK,BURNOUT_CHAMPION,PHOTOPHOBY,OUTCONTROL_MAGIC,WINTER_SWIMMING,
				FIRE_WOOD,BE_INCONSTANT,UNBEAR_HUNGER,VIP_MEAL,FAST_DIE,WASH_HAND,THORNS_SPRANG,LAND_SWIMMING,
				FULLPASSION,FULLFIGHTING,PHASECLAW,DEEP_FEAR,NO_DOOR,UPDRAFT,BONE_FIRE,JUMP_FACE_SILICONE,GRASS_MOB
				,DISASTER_CURSE);
		long seed=Dungeon.seed;
		Random random = new Random();
		random.pushGenerator(seed);

		random.shuffle(Negatives);
		return Negatives;

		 */
	}

	public static  ArrayList< ArrayList<Talent>> negativeTalent = new ArrayList<>();
	static {
		for(int i = 0;i < 6;i++){
			ArrayList<Talent> negativetalent = new ArrayList<>();
			negativeTalent.add(negativetalent);
		}
		//通用负面天赋
		Collections.addAll(negativeTalent.get(0),ENDLESS_MALICE,LAND_SWIMMING,EXPLOSION_MEAL,COWBOY,SHORTSIGHTED,PARASITISM,OUTCONTROL_MAGIC,
				FULLFIGHTING,NO_DOOR,JUMP_FACE_SILICONE,DEEP_FEAR,BE_INCONSTANT);
		//版本限定负面天赋
		Collections.addAll(negativeTalent.get(1),ETERNAL_CURSE,FATE_DECISION,MAMBA_OUT,HANDON_GROUND,BURNOUT_CHAMPION, FULLPASSION,
				UPDRAFT,HAND_SLIP);
		Collections.addAll(negativeTalent.get(2),EATER,MALNUTRITION,CHILL_WATER,UNBEAR_HUNGER,VIP_MEAL,WASH_HAND,THORNS_SPRANG,GRASS_MOB);
		Collections.addAll(negativeTalent.get(3),DISASTER_CURSE,PHASECLAW,FIRE_WOOD,UNAVOIDABLE,MENTAL_COLLAPSE,WEAKEN_CHALICE,BAT_SERUM,
				FEEBLE);
		Collections.addAll(negativeTalent.get(4),MYOPIA,LIFE_SPORT,DUMP_TRUCK,PHOTOPHOBY,WINTER_SWIMMING,FAST_DIE,BONE_FIRE,JIULONGLA_COFFIN);
		Collections.addAll(negativeTalent.get(5),HUNGRY_GHOST,UNABLE_REST,CURSEDMAN,USURY,STEAM_BEAN,ALCHEMY_ACCIDENT,SHELL_CRY,MAL_CURSE);

	}


	public static ArrayList<Talent> getClericTalent(int tier){
		ArrayList<Talent> ClericTalent=new ArrayList<>();
		switch (tier){
			case 1: default:
				Collections.addAll(ClericTalent, SATIATED_SPELLS, HOLY_INTUITION, SEARING_LIGHT, SHIELD_OF_LIGHT,
						ASCENSION_CURSE,SHEPHERD_INTENTION,SILVER_LANGUAGE);
				break;
			case 2:
				Collections.addAll(ClericTalent,  RECALL_INSCRIPTION, SUNRAY, DIVINE_SENSE, BLESS,
						DIVINE_PROTECTION,CONVERSION_HOLY,GENESIS,INDULGENCE);
				break;
			case 3:
				Collections.addAll(ClericTalent, CLEANSE, LIGHT_READING,PURIFYING_EVIL,DIVINE_STORM,RESURRECTION);
				break;
		}


		Random.shuffle(ClericTalent);
		return ClericTalent;
	}

	private static void initRandomClassTalents( Hero hero, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements ,LinkedHashMap<Talent, String> sublimation){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		addRandomClassTalents(HeroRandomizer.classTalents(hero, 1), talents.get(0), replacements);
		talents.get(0).put(bossTalentForSlot(BOSS_TALENT_SLOT_1, sublimation), 0);
		talents.get(0).put(bossTalentForSlot(BOSS_TALENT_SLOT_2, sublimation), 0);

		addRandomClassTalents(HeroRandomizer.classTalents(hero, 2), talents.get(1), replacements);
		talents.get(1).put(bossTalentForSlot(BOSS_TALENT_SLOT_3, sublimation), 0);
		talents.get(1).put(bossTalentForSlot(BOSS_TALENT_SLOT_4, sublimation), 0);

		addRandomClassTalents(HeroRandomizer.classTalents(hero, 3), talents.get(2), replacements);
		talents.get(2).put(bossTalentForSlot(BOSS_TALENT_SLOT_5, sublimation), 0);
	}

	private static void addRandomClassTalents(List<Talent> tierTalents, LinkedHashMap<Talent, Integer> tier, LinkedHashMap<Talent, Talent> replacements){
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			tier.put(talent, 0);
		}
	}


	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements ,LinkedHashMap<Talent, String> sublimation){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HEARTY_MEAL, VETERANS_INTUITION);
				//Collections.addAll(tierTalents,SACRED_BLADE,BLADE_STAR);
				break;
			case MAGE:
				//Collections.addAll(tierTalents, GAS_SPURT, SCHOLARS_INTUITION);
				Collections.addAll(tierTalents, EMPOWERING_MEAL,BACKUP_BARRIER );
				break;
			case ROGUE:
				Collections.addAll(tierTalents, CACHED_RATIONS, PROTECTIVE_SHADOWS);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, NATURES_BOUNTY, FOLLOWUP_STRIKE);
				break;
			case DUELIST:
				Collections.addAll(tierTalents, STRENGTHENING_MEAL, ADVENTURERS_INTUITION);
				break;
			case CLERIC:
				Collections.addAll(tierTalents, SATIATED_SPELLS, SHIELD_OF_LIGHT);
				break;
			case FREEMAN:
				Collections.addAll(tierTalents, POTENTIAL_1, POTENTIAL_2);
				break;
			case SLIMEGIRL:
				Collections.addAll(tierTalents, RESILIENT_MEAL, LIQUID_ARMOR);
				break;
			case NINJA:
				Collections.addAll(tierTalents, NINJA_MEAL, HUNTING_INTUITION);
				break;
			case DM400:
				Collections.addAll(tierTalents, MARK_MEAL, TARGET_TARGETING);
				break;
            case PRINCESS:
				Collections.addAll(tierTalents, ENCHANT_MEAL,RUNE_BLADE);
				break;
			case FRIAR:
				Collections.addAll(tierTalents, TRANQUIL_TINCTURE, RAVENS_EYE);
				break;
			case RATKING:
				Collections.addAll(tierTalents, ROYAL_PRIVILEGE, ROYAL_INTUITION, KINGS_WISDOM, NOBLE_CAUSE);
				break;
				/*
			case COMMON:
				Collections.addAll(tierTalents, PROVOKED_ANGER, IRON_WILL,LINGERING_MAGIC, SUCKER_PUNCH,
						PROTECTIVE_SHADOWS, SCHOLARS_INTUITION,SURVIVALISTS_INTUITION, NATURES_AID, PATIENT_STRIKE, AGGRESSIVE_BARRIER,
						STRONG_ATTACK,FEAR_INCARNATION,DISTURB_ATTACK,THRID_HAND,WATER_ATTACK,BOMB_MANIAC,THICKENED_ARMOR,
						POWERFUL_CALCULATIONS,INSERT_BID,MEAL_SHIELD,STRENGTH_TRAIN,TREAT_MEAL,COVER_SCAR,NURTRITIOUS_MEAL,
						SAVAGE_PHYSIQUE,ICE_BREAKING,STRENGTH_GREATEST,MORE_TALENT,NOVICE_BENEFITS,FISHING_TIME,
						POSION_DAGGER,SHOCK_BOMB,ILLUSION_FEED,ATTACK_DOOR,ZHUOJUN_BUTCHER,AID_STOMACH,EATEN_SLOWLY,
						INVINCIBLE,THORNY_ROSE,GOLD_MEAL,WATER_GHOST,ASH_LEDGER,SECRET_LIGHTING,DAMAGED_CORE,ANESTHESIA,
						EXPERIENCE_MEAL,MILITARY_WATERSKIN,JASMINE_TEA,GOLDOFBOOK,CHOCOLATE_COINS,GHOST_GIFT,PERSONAL_ATTACK,
						HONEY_FISH,ASCENSION_CURSE,SHEPHERD_INTENTION,INSINUATION,AUTO_PICK);
				break;
			case ADVANCED:
				Collections.addAll(tierTalents, AQUATIC_RECOVER,PUMP_ATTACK,OOZE_ATTACK,
						SURPRISE_THROW,SMOKE_MASK,RUSH);
				break;
				 */

		}
		for (Talent talent : tierTalents){

			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(0).put(talent, 0);


		}
		talents.get(0).put(bossTalentForSlot(BOSS_TALENT_SLOT_1, sublimation), 0);
		talents.get(0).put(bossTalentForSlot(BOSS_TALENT_SLOT_2, sublimation), 0);
		tierTalents.clear();

		//tier 2
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, IRON_STOMACH, LIQUID_WILLPOWER, RUNIC_TRANSFERENCE);
				break;
			case MAGE:
				Collections.addAll(tierTalents, ENERGIZING_MEAL, INSCRIBED_POWER, WAND_PRESERVATION);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, MYSTICAL_MEAL, INSCRIBED_STEALTH, WIDE_SEARCH);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, INVIGORATING_MEAL, LIQUID_NATURE, HEIGHTENED_SENSES);
				break;
			case DUELIST:
				Collections.addAll(tierTalents, FOCUSED_MEAL, LIQUID_AGILITY, WEAPON_RECHARGING);
				break;
			case CLERIC:
				Collections.addAll(tierTalents, ENLIGHTENING_MEAL, DIVINE_SENSE, BLESS);
				break;
			case FREEMAN:
				Collections.addAll(tierTalents, POTENTIAL_3, POTENTIAL_4,POTENTIAL_5);
				break;
			case SLIMEGIRL:
				Collections.addAll(tierTalents, TOUGH_MEAL, SLIME_GREENHOUSE, ENERGY_ABSORPTION);
				break;
			case NINJA:
				Collections.addAll(tierTalents, YUNYING_MEAL, XIA,FEINT);
				break;
			case DM400:
				Collections.addAll(tierTalents, OVER_MEAL, RECOVER_CHARGE,BODY_REINFORCE);
				break;
            case PRINCESS:
				Collections.addAll(tierTalents, ROYAL_MEAL,RUNE_EXPERT,MORE_RING);
				break;
			case RATKING:
				Collections.addAll(tierTalents, RK_ROYAL_MEAL,RESTORATION,POWER_WITHIN,KINGS_VISION,PURSUIT);
            case FRIAR:
                Collections.addAll(tierTalents, WHISPERING_MEAL,LIQUID_BARRIER,FAST_RELOAD);
                break;

		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		talents.get(1).put(bossTalentForSlot(BOSS_TALENT_SLOT_3, sublimation), 0);
		talents.get(1).put(bossTalentForSlot(BOSS_TALENT_SLOT_4, sublimation), 0);
		tierTalents.clear();

		//tier 3
		switch (cls){
			case WARRIOR:
				Collections.addAll(tierTalents, HOLD_FAST);
				break;
			case MAGE:
				Collections.addAll(tierTalents, DESPERATE_POWER);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, LIGHT_CLOAK);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, SEER_SHOT);
				break;
			case DUELIST:
				Collections.addAll(tierTalents, DEADLY_FOLLOWUP);
				break;
			case FREEMAN:
				Collections.addAll(tierTalents, POTENTIAL_6, POTENTIAL_7,POTENTIAL_8,POTENTIAL_9);
				break;
			case CLERIC:
				Collections.addAll(tierTalents, LIGHT_READING);
				break;
			case FRIAR:
				Collections.addAll(tierTalents, HUMAN_GLORY);
				break;
			case SLIMEGIRL:
				Collections.addAll(tierTalents, ORIGINAL_MONSTER);
				break;
			case NINJA:
				Collections.addAll(tierTalents, LIGHT_BOX);
				break;
			case DM400:
				Collections.addAll(tierTalents, QUICK_TOOL);
				break;
			case PRINCESS:
				Collections.addAll(tierTalents, RING_BOND);
				break;
			case RATKING: default:
				break;
				/*
			case COMMON:
				Collections.addAll(tierTalents, STRONGMAN, ALLY_WARP, ENHANCED_RINGS, POINT_BLANK, PRECISE_ASSAULT,
						TRAP_MASTER,COUNTER_ATTACK,BEHEST,OVERWHELMING,MAGIC_RECYCLING,ENGINEER_REFIT,PHANTOM_SHOOTER,
						MARTIAL_TRAIN,RAGE_ATTACK,GOLD_FORMATION,ACCUMULATE_STEADILY,DOUBLE_TRINKETS,INVINCIBLE_MEAL,
						DETOX_DAMAGE,RETURNING_HONOR,WEAPON_MAKE,SECRET_STASH,EARTH_MEAL,AFRAID_DEATH,PYROMANIAC,
						REVERSE_POLARITY,HERO_NAME,SKY_EARTH,WATER_ISFOOD,ANGEL_STANCE,HOMETOWN_CLOUD,DEEP_FREEZE,
						WIDE_KNOWLEDGE,NO_VIEWRAPE,MORONITY,LOVE_BACKSTAB,CONCEPT_GRID,ABACUS,TIME_SAND,READ_PROFITABLE,
						STRENGTH_CLOAK,WANLING_POTION,ACTIVE_MUSCLES,SEA_WIND,ENDLESS_MEAL,BIRTHDAY_GIFT,COLLECT_PLANTS,
						SHARP_HEAD,STRENGTH_ARMBAND,EXTREME_CASTING,PRECISE_SHOT,TREASURE_SENSE,BEYOND_LIMIT,
						HEALTHY_FOOD,EXTREME_REACTION,CICADA_DANCE,HOLY_FAITH,PURIFYING_EVIL,DIVINE_STORM,RESURRECTION,
						ASHES_BOW,SWIFT_CHURCH);
				break;
			case ADVANCED:
					Collections.addAll(tierTalents, YOG_LARVA,YOG_FIST,Talent.YOG_RAY);

				 */
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(2).put(talent, 0);
		}
		talents.get(2).put(bossTalentForSlot(BOSS_TALENT_SLOT_5, sublimation), 0);
		tierTalents.clear();
		/*
		if(hero.negativeTalents!=null){
			for(Talent t:hero.negativeTalents.keySet()){
				if(!talents.get(hero.negativeTalents.get(t)).containsKey(t)){
					talents.get(hero.negativeTalents.get(t)).put(t, 0);
				}
			}
		}

		 */



		//tier4
		//TBD
	}

	private static final EnumMap<HeroSubClass, List<Talent>> nativeSubclassTalents =
			new EnumMap<>(HeroSubClass.class);
	public static final Map<HeroSubClass, List<Talent>> subclassTalentPools;

	static {
		registerSubclassTalents(HeroSubClass.BERSERKER, ENDLESS_RAGE, DEATHLESS_FURY, ENRAGED_CATALYST);
		registerSubclassTalents(HeroSubClass.GLADIATOR, CLEAVE, LETHAL_DEFENSE, ENHANCED_COMBO);
		registerSubclassTalents(HeroSubClass.BATTLEMAGE, EMPOWERED_STRIKE, MYSTICAL_CHARGE, EXCESS_CHARGE);
		registerSubclassTalents(HeroSubClass.WARLOCK, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS);
		registerSubclassTalents(HeroSubClass.ASSASSIN, ENHANCED_LETHALITY, ASSASSINS_REACH, BOUNTY_HUNTER);
		registerSubclassTalents(HeroSubClass.FREERUNNER, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH);
		registerSubclassTalents(HeroSubClass.SNIPER, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES);
		registerSubclassTalents(HeroSubClass.WARDEN, DURABLE_TIPS, BARKSKIN, SHIELDING_DEW);
		registerSubclassTalents(HeroSubClass.CHAMPION, VARIED_CHARGE, TWIN_UPGRADES, COMBINED_LETHALITY);
		registerSubclassTalents(HeroSubClass.MONK, UNENCUMBERED_SPIRIT, MONASTIC_VIGOR, COMBINED_ENERGY);
		registerSubclassTalents(HeroSubClass.PRIEST, HOLY_LANCE, HALLOWED_GROUND, MNEMONIC_PRAYER);
		registerSubclassTalents(HeroSubClass.PALADIN, LAY_ON_HANDS, AURA_OF_PROTECTION, WALL_OF_LIGHT);
		registerSubclassTalents(HeroSubClass.WATERSLIME, WATER_BODY, WATER_REVIVAL, WATER_REGENERATION);
		registerSubclassTalents(HeroSubClass.DARKSLIME, POTENT_OOZE, DARK_GAS, DARK_LIQUID);
		registerSubclassTalents(HeroSubClass.TATTEKI_NINJA, SOKO, KONO_FUKUSA, KUNIKUCHI);
		registerSubclassTalents(HeroSubClass.NINJA_MASTER, SOUL_HUNTING, USE_ENVIRONMENT, MIND_WATER);
		registerSubclassTalents(HeroSubClass.AT400, SUSTAIN_MARK, BATTLE_UPGRADE, FLY_DRONE);
		registerSubclassTalents(HeroSubClass.AU400, SPECIAL_MARK, ASSIST_UPGRADE, FAST_CRUISE);
		registerSubclassTalents(HeroSubClass.RUNEMAGE, DARKMARK, RUNE_BLAST, RUNE_SURGE);
		registerSubclassTalents(HeroSubClass.COMBATMASTER, FLUENT, STANCE_MASTERY, INELEMENT);
		registerSubclassTalents(HeroSubClass.ALCHEMIST, ALCHEMY_SHIELD, CLOSE_BLAST, CONSERVATION);
		registerSubclassTalents(HeroSubClass.PIOUS, NEVER_COMPROMISE, REWIND_TIME, BRIGHT_WARRIOR);

		EnumMap<HeroSubClass, List<Talent>> pools = new EnumMap<>(HeroSubClass.class);
		registerSubclassPool(pools, HeroSubClass.BERSERKER,
				CEASELESS_RAGE, MIRRORED_REVENGE, BLOODTHIRSTY_BERSERK);
		registerSubclassPool(pools, HeroSubClass.GLADIATOR,
				COMBO_FOCUS, RELENTLESS_COMBAT, COMBO_MASTERY);
		registerSubclassPool(pools, HeroSubClass.BATTLEMAGE,
				LONG_ARM, ARCANE_CONFLUENCE, FOCUSED_CASTING);
		registerSubclassPool(pools, HeroSubClass.WARLOCK,
				FINE_TASTING, BONE_DEEP, MIND_IMPRISONMENT);
		registerSubclassPool(pools, HeroSubClass.ASSASSIN,
				UNEXPECTED_STRIKE, CLOSING_STAGE, PERFECT_FINALE);
		registerSubclassPool(pools, HeroSubClass.FREERUNNER,
				FREERUNNER_AFTERIMAGE, MOMENTUM_RESERVE, WARMUP_PREPARATION);
		registerSubclassPool(pools, HeroSubClass.SNIPER);
		registerSubclassPool(pools, HeroSubClass.WARDEN);
		registerSubclassPool(pools, HeroSubClass.CHAMPION,
				WEAPON_ABILITY_MASTER, SKILLED_PARRY, ALTERNATING_WEAPONS);
		registerSubclassPool(pools, HeroSubClass.MONK,
				NATURAL_WAY, INNER_PEACE, YIN_YANG_BALANCE);
		registerSubclassPool(pools, HeroSubClass.PRIEST);
		registerSubclassPool(pools, HeroSubClass.PALADIN);
		registerSubclassPool(pools, HeroSubClass.WATERSLIME);
		registerSubclassPool(pools, HeroSubClass.DARKSLIME);
		registerSubclassPool(pools, HeroSubClass.TATTEKI_NINJA);
		registerSubclassPool(pools, HeroSubClass.NINJA_MASTER);
		registerSubclassPool(pools, HeroSubClass.AT400);
		registerSubclassPool(pools, HeroSubClass.AU400);
		registerSubclassPool(pools, HeroSubClass.RUNEMAGE);
		registerSubclassPool(pools, HeroSubClass.COMBATMASTER);
		registerSubclassPool(pools, HeroSubClass.ALCHEMIST);
		registerSubclassPool(pools, HeroSubClass.PIOUS);
		subclassTalentPools = Collections.unmodifiableMap(pools);
	}

	private static void registerSubclassTalents(HeroSubClass subClass, Talent... talents) {
		ArrayList<Talent> registered = new ArrayList<>();
		Collections.addAll(registered, talents);
		nativeSubclassTalents.put(subClass, Collections.unmodifiableList(registered));
	}

	private static void registerSubclassPool(
			EnumMap<HeroSubClass, List<Talent>> pools,
			HeroSubClass subClass, Talent... additionalTalents) {
		ArrayList<Talent> pool = new ArrayList<>(nativeSubclassTalents.get(subClass));
		Collections.addAll(pool, additionalTalents);
		pools.put(subClass, Collections.unmodifiableList(pool));
	}

	public static List<Talent> subclassTalentPool(HeroSubClass subClass) {
		List<Talent> pool = subclassTalentPools.get(subClass);
		return pool == null ? Collections.emptyList() : pool;
	}

	public static List<Talent> subclassTalentPool(Talent talent) {
		for (Map.Entry<HeroSubClass, List<Talent>> entry : subclassTalentPools.entrySet()) {
			if (entry.getValue().contains(talent)) {
				return entry.getValue();
			}
		}
		return Collections.emptyList();
	}

	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents(hero.subClass, hero.talents, hero.metamorphedTalents);
	}

	public static void initSubclassTalents(
			HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		initSubclassTalents(cls, talents, new LinkedHashMap<>());
	}

	public static void initSubclassTalents(
			HeroSubClass cls,
			ArrayList<LinkedHashMap<Talent, Integer>> talents,
			LinkedHashMap<Talent, Talent> replacements ){
		if (cls == null || cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		List<Talent> tierTalents = nativeSubclassTalents.get(cls);
		if (tierTalents != null) {
			for (Talent talent : tierTalents){
				talents.get(2).put(replacements.getOrDefault(talent, talent), 0);
			}
		} else if (cls == HeroSubClass.KING) {
			Talent[] kingTalents = {RK_BERSERKER, RK_GLADIATOR, RK_BATTLEMAGE, RK_WARLOCK,
					RK_ASSASSIN, RK_FREERUNNER, RK_SNIPER, RK_WARDEN,
					RK_CHAMPION, RK_MONK, RK_PRIEST, RK_PALADIN,
					RK_WATERSLIME, RK_DARKSLIME, RK_TATTEKI_NINJA, RK_NINJA_MASTER,
					RK_AT400, RK_AU400, RK_RUNEMAGE, RK_COMBATMASTER};
			for (Talent talent : kingTalents) {
				talents.get(2).put(talent, 0);
			}
		}
	}

	public static void initArmorTalents( Hero hero ){
		initArmorTalents( hero.armorAbility, hero.talents);
	}

	public static void initArmorTalents(ArmorAbility abil, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (abil == null) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		for (Talent t : abil.talents()){
			talents.get(3).put(t, 0);
		}
	}

	private static final String TALENT_TIER = "talents_tier_";
	private static final String COMPLETE_TALENT_LAYOUT = "complete_talent_layout";

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		if (Dungeon.newCycle) {
			bundle.put(COMPLETE_TALENT_LAYOUT, true);
		}
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				if (tier.get(talent) > 0 || Dungeon.newCycle){
					tierBundle.put(talent.name(), tier.get(talent));
				}
				if (tierBundle.contains(talent.name())){
					tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
				}
			}
			bundle.put(TALENT_TIER+(i+1), tierBundle);
		}

		Bundle replacementsBundle = new Bundle();
		for (Talent t : hero.metamorphedTalents.keySet()){
			replacementsBundle.put(t.name(), hero.metamorphedTalents.get(t));
		}
		Bundle sublimationBundle = new Bundle();
		for (Talent t : hero.sublimationTalents.keySet()){
			sublimationBundle.put(t.name(), hero.sublimationTalents.get(t));
		}
		Bundle negativeBundle = new Bundle();
		for (Talent t : hero.negativeTalents.keySet()){
			negativeBundle.put(t.name(), hero.negativeTalents.get(t));
		}

		bundle.put("replacements", replacementsBundle);
		bundle.put("sublimation", sublimationBundle);
		bundle.put("negative", negativeBundle);
		if (hero.testModeNegativeTalent != null){
			bundle.put("test_mode_negative", hero.testModeNegativeTalent.name());
		}
	}

	private static final HashSet<String> removedTalents = new HashSet<>();
	static{
		//v2.4.0
		removedTalents.add("TEST_SUBJECT");
		removedTalents.add("TESTED_HYPOTHESIS");
		//v2.2.0
		removedTalents.add("EMPOWERING_SCROLLS");
	}

	private static final HashMap<String, String> renamedTalents = new HashMap<>();
	static{
		//v2.4.0
		renamedTalents.put("SECONDARY_CHARGE",          "VARIED_CHARGE");

		//v2.2.0
		renamedTalents.put("RESTORED_WILLPOWER",        "LIQUID_WILLPOWER");
		renamedTalents.put("ENERGIZING_UPGRADE",        "INSCRIBED_POWER");
		renamedTalents.put("MYSTICAL_UPGRADE",          "INSCRIBED_STEALTH");
		renamedTalents.put("RESTORED_NATURE",           "LIQUID_NATURE");
		renamedTalents.put("RESTORED_AGILITY",          "LIQUID_AGILITY");
		//v2.1.0
		renamedTalents.put("LIGHTWEIGHT_CHARGE",        "PRECISE_ASSAULT");
		//v2.0.0 BETA
		renamedTalents.put("LIGHTLY_ARMED",             "UNENCUMBERED_SPIRIT");
		//v2.0.0
		renamedTalents.put("ARMSMASTERS_INTUITION",     "VETERANS_INTUITION");
	}

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero ){

		if (bundle.contains("replacements")){
			Bundle replacements = bundle.getBundle("replacements");
			for (String key : replacements.getKeys()){
				String value = replacements.getString(key);
				if (renamedTalents.containsKey(key)) key = renamedTalents.get(key);
				if (renamedTalents.containsKey(value)) value = renamedTalents.get(value);
				if (!removedTalents.contains(key) && !removedTalents.contains(value)){
					try {
						hero.metamorphedTalents.put(Talent.valueOf(key), Talent.valueOf(value));
					} catch (Exception e) {
						ShatteredPixelDungeon.reportException(e);
					}
				}

			}
		}
		if (bundle.contains("sublimation")){
			Bundle sublimationBundle = bundle.getBundle("sublimation");
			for (String key : sublimationBundle.getKeys()){
				String value = sublimationBundle.getString(key);

					try {
						hero.sublimationTalents.put(Talent.valueOf(key), value);
					} catch (Exception e) {
						ShatteredPixelDungeon.reportException(e);
					}


			}
		}
		if (bundle.contains("negative")){
			Bundle negativeBundle = bundle.getBundle("negative");
			for (String key : negativeBundle.getKeys()){
				int cnt =0;
				int value;
				try {
					value = negativeBundle.getInt(key);
				} catch (Exception e) {
					value = cnt;
					cnt++;
				}
				try {
					hero.negativeTalents.put(Talent.valueOf(key),value);
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}


			}
		}
		if (bundle.contains("test_mode_negative")){
			String value = bundle.getString("test_mode_negative");
			if (renamedTalents.containsKey(value)) value = renamedTalents.get(value);
			if (!removedTalents.contains(value)){
				try {
					hero.testModeNegativeTalent = Talent.valueOf(value);
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		}
		boolean restoreCompleteLayout = bundle.contains(COMPLETE_TALENT_LAYOUT)
				&& bundle.getBoolean(COMPLETE_TALENT_LAYOUT);
		if (!restoreCompleteLayout) {
			if (hero.heroClass != null)     initClassTalents(hero);
			if (hero.subClass != null)      initSubclassTalents(hero);
			if (hero.armorAbility != null)  initArmorTalents(hero);

			if(Dungeon.isChallenged(Challenges.NEGATIVE) || !hero.negativeTalents.isEmpty()){
				Talent.initNegativeTalent(hero.heroClass,hero.talents,hero.negativeTalents);
			}
		}
		if (restoreCompleteLayout) {
			while (hero.talents.size() < MAX_TALENT_TIERS) {
				hero.talents.add(new LinkedHashMap<>());
			}
			for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
				tier.clear();
			}
		}






		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			//LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = bundle.contains(TALENT_TIER+(i+1)) ? bundle.getBundle(TALENT_TIER+(i+1)) : null;

			if (tierBundle != null){
				for (String tName : tierBundle.getKeys()){
					int points = tierBundle.getInt(tName);
					if (renamedTalents.containsKey(tName)) tName = renamedTalents.get(tName);
					if (!removedTalents.contains(tName)) {
						try {
							Talent talent = Talent.valueOf(tName);

							if (tier.containsKey(talent) || restoreCompleteLayout || Dungeon.newCycle) {
								tier.put(talent, Math.min(points, talent.maxPoints()));
							}

						} catch (Exception e) {
							ShatteredPixelDungeon.reportException(e);
						}

					}

				}
			}
			//Dungeon.hero.talents.set(i, tier);

		}
	}


	private static class WndDivination extends Window {

		private static final int WIDTH = 120;

		WndDivination(ArrayList<Item> IDed,Talent talent ){
			IconTitle tfTitle = new IconTitle(new TalentIcon( talent ), talent.title());
			tfTitle.setRect(0, 0, WIDTH, 0);
			add(tfTitle);
			IconTitle cur = null;
			float pos = tfTitle.bottom()+4;

			for (Item i : IDed){

				cur = new IconTitle(i);
				cur.setRect(0, pos, WIDTH, 0);
				add(cur);
				pos = cur.bottom() + 2;

			}

			resize(WIDTH, (int)pos);
		}

	}


}
