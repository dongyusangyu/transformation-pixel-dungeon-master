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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.eat_item;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.energy;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScrollEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TimeStasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.Decoy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.OneSword;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.DivineSense;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.RecallInscription;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
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
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfDivination;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TelekineticGrab;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dirk;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Katana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Wakizashi;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
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
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Image;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public enum Talent {

	//Warrior T1
	HEARTY_MEAL(0), VETERANS_INTUITION(1), PROVOKED_ANGER(2), IRON_WILL(3),
	//Warrior T2
	IRON_STOMACH(4), LIQUID_WILLPOWER(5), RUNIC_TRANSFERENCE(6), LETHAL_MOMENTUM(7), IMPROVISED_PROJECTILES(8),
	//Warrior T3
	HOLD_FAST(9, 3), STRONGMAN(10, 3),
	//Berserker T3
	ENDLESS_RAGE(11, 3), DEATHLESS_FURY(12, 3), ENRAGED_CATALYST(13, 3),
	//Gladiator T3
	CLEAVE(14, 3), LETHAL_DEFENSE(15, 3), ENHANCED_COMBO(16, 3),
	//Heroic Leap T4
	BODY_SLAM(17, 4), IMPACT_WAVE(18, 4), DOUBLE_JUMP(19, 4),
	//Shockwave T4
	EXPANDING_WAVE(20, 4), STRIKING_WAVE(21, 4), SHOCK_FORCE(22, 4),
	//Endure T4
	SUSTAINED_RETRIBUTION(23, 4), SHRUG_IT_OFF(24, 4), EVEN_THE_ODDS(25, 4),

	//Mage T1
	EMPOWERING_MEAL(32), SCHOLARS_INTUITION(33), LINGERING_MAGIC(34), BACKUP_BARRIER(35),
	//Mage T2
	ENERGIZING_MEAL(36), INSCRIBED_POWER(37), WAND_PRESERVATION(38), ARCANE_VISION(39), SHIELD_BATTERY(40),
	//Mage T3
	DESPERATE_POWER(41, 3), ALLY_WARP(42, 3),
	//Battlemage T3
	EMPOWERED_STRIKE(43, 3), MYSTICAL_CHARGE(44, 3), EXCESS_CHARGE(45, 3),
	//Warlock T3
	SOUL_EATER(46, 3), SOUL_SIPHON(47, 3), NECROMANCERS_MINIONS(48, 3),
	//Elemental Blast T4
	BLAST_RADIUS(49, 4), ELEMENTAL_POWER(50, 4), REACTIVE_BARRIER(51, 4),
	//Wild Magic T4
	WILD_POWER(52, 4), FIRE_EVERYTHING(53, 4), CONSERVED_MAGIC(54, 4),
	//Warp Beacon T4
	TELEFRAG(55, 4), REMOTE_BEACON(56, 4), LONGRANGE_WARP(57, 4),

	//Rogue T1
	CACHED_RATIONS(64), THIEFS_INTUITION(65), SUCKER_PUNCH(66), PROTECTIVE_SHADOWS(67),
	//Rogue T2
	MYSTICAL_MEAL(68), INSCRIBED_STEALTH(69), WIDE_SEARCH(70), SILENT_STEPS(71), ROGUES_FORESIGHT(72),
	//Rogue T3
	ENHANCED_RINGS(73, 3), LIGHT_CLOAK(74, 3),
	//Assassin T3
	ENHANCED_LETHALITY(75, 3), ASSASSINS_REACH(76, 3), BOUNTY_HUNTER(77, 3),
	//Freerunner T3
	EVASIVE_ARMOR(78, 3), PROJECTILE_MOMENTUM(79, 3), SPEEDY_STEALTH(80, 3),
	//Smoke Bomb T4
	HASTY_RETREAT(81, 4), BODY_REPLACEMENT(82, 4), SHADOW_STEP(83, 4),
	//Death Mark T4
	FEAR_THE_REAPER(84, 4), DEATHLY_DURABILITY(85, 4), DOUBLE_MARK(86, 4),
	//Shadow Clone T4
	SHADOW_BLADE(87, 4), CLONED_ARMOR(88, 4), PERFECT_COPY(89, 4),

	//Huntress T1
	NATURES_BOUNTY(96), SURVIVALISTS_INTUITION(97), FOLLOWUP_STRIKE(98), NATURES_AID(99),
	//Huntress T2
	INVIGORATING_MEAL(100), LIQUID_NATURE(101), REJUVENATING_STEPS(102), HEIGHTENED_SENSES(103), DURABLE_PROJECTILES(104),
	//Huntress T3
	POINT_BLANK(105, 3), SEER_SHOT(106, 3),
	//Sniper T3
	FARSIGHT(107, 3), SHARED_ENCHANTMENT(108, 3), SHARED_UPGRADES(109, 3),
	//Warden T3
	DURABLE_TIPS(110, 3), BARKSKIN(111, 3), SHIELDING_DEW(112, 3),
	//Spectral Blades T4
	FAN_OF_BLADES(113, 4), PROJECTING_BLADES(114, 4), SPIRIT_BLADES(115, 4),
	//Natures Power T4
	GROWING_POWER(116, 4), NATURES_WRATH(117, 4), WILD_MOMENTUM(118, 4),
	//Spirit Hawk T4
	EAGLE_EYE(119, 4), GO_FOR_THE_EYES(120, 4), SWIFT_SPIRIT(121, 4),

	//Duelist T1
	STRENGTHENING_MEAL(128), ADVENTURERS_INTUITION(129), PATIENT_STRIKE(130), AGGRESSIVE_BARRIER(131),
	//Duelist T2
	FOCUSED_MEAL(132), LIQUID_AGILITY(133), WEAPON_RECHARGING(134), LETHAL_HASTE(135), SWIFT_EQUIP(136),
	//Duelist T3
	PRECISE_ASSAULT(137, 3), DEADLY_FOLLOWUP(138, 3),
	//Champion T3
	VARIED_CHARGE(139, 3), TWIN_UPGRADES(140, 3), COMBINED_LETHALITY(141, 3),
	//Monk T3
	UNENCUMBERED_SPIRIT(142, 3), MONASTIC_VIGOR(143, 3), COMBINED_ENERGY(144, 3),
	//Challenge T4
	CLOSE_THE_GAP(145, 4), INVIGORATING_VICTORY(146, 4), ELIMINATION_MATCH(147, 4),
	//Elemental Strike T4
	ELEMENTAL_REACH(148, 4), STRIKING_FORCE(149, 4), DIRECTED_POWER(150, 4),
	//Duelist A3 T4
	FEIGNED_RETREAT(151, 4), EXPOSE_WEAKNESS(152, 4), COUNTER_ABILITY(153, 4),

	//universal T4
	HEROIC_ENERGY(26, 4), //See icon() and title() for special logic for this one
	//Ratmogrify T4
	RATSISTANCE(215, 4), RATLOMACY(216, 4), RATFORCEMENTS(217, 4),

	//SlimeGril T1
	RESILIENT_MEAL(416),LIQUID_PERCEPTION(417), WATER_WAVE(418),LIQUID_ARMOR(419),
	//SlimeGirl T2
	TOUGH_MEAL(420), SLIME_GREENHOUSE(421), ENERGY_ABSORPTION(422),NATURAL_AFFINITY(423),QUALITY_ABSORPTION(424),
	//SlimeGirl T3
	ORIGINAL_MONSTER(425, 3),EMPOWERING_LIFE(426,3),
	//WaterSlime T3
	WATER_BODY(427, 3), WATER_REVIVAL(428, 3), WATER_REGENERATION(429, 3),
	//DarkSlime T3
	POTENT_OOZE(430, 3), DARK_GAS(431, 3), DARK_LIQUID(432, 3),
	//SpringSpell T4
	POTENT_HEALING(433, 4), EFFICIENT_HEALING(434, 4), MASS_HEALING(435, 4),
	//RapidWaterfall T4
	VIOLENT_STORM(436, 4), NEW_TRAP(437, 4), HOLY_BATH(438, 4),
	//MadSlimeT4
	NO_PICK(439, 4), FOOD_BONUS(440, 4), DELICIOUS_DIGESTION(441, 4),
	//Ninja
	NINJA_MEAL(448),HUNTING_INTUITION(449),AGILE_ATTACK(450),YOU_SCARED_ME(451),
	YUNYING_MEAL(452),XIA(453),QUICK_SEARCH(454),NINJA_SOCIAL(455),FEINT(456),
	QIANFA_THROWING(457,3),LIGHT_BOX(458,3),
	//Tatteki_ninja
	SOKO(459,3),KONO_FUKUSA(460,3),KUNIKUCHI(461,3),
	SOUL_HUNTING(462,3),USE_ENVIRONMENT(463,3),MIND_WATER(464,3),

	DEVERSION(465,4),SHINKAGE(466,4),ALLHUNTING(467,4),
	POWER_GUNPOWDER(468,4),TEA_STAINS(469,4),FIREWORK(470,4),

	//OneSword
	OFFENSIVE(471,4),GLIMPSE(472,4),KILL_CONTINUE(473,4),

	//GOO
	AQUATIC_RECOVER(160,2),PUMP_ATTACK(161,2),OOZE_ATTACK(162,2),
	//Tengu
	SURPRISE_THROW(163,2),SMOKE_MASK(164,2),RUSH(165,2),
	//DM300
	OVERLOAD_CHARGE(166,2),GAS_SPURT(167,2),FASTING(168,2),
	//DwaefKing
	KING_PROTECT(169,2),SUMMON_FOLLOWER(170,2),WOLFISH_GAZE(171,2),ENERGY_CONVERSION(183,2),
	STRONG_ATTACK(172,2),SURVIVAL_VOLITION(173,2),TRAP_MASTER(174,3),
	STRONG_THROW(175,2),COUNTER_ATTACK(176,3),FEAR_INCARNATION(177,2),
	BEHEST(178,3),BURNING_CURSE(179,2),INVISIBILITY_SHADOWS(180,2),
	JUSTICE_PUNISH(181,2),OVERWHELMING(182,3),MAGIC_RECYCLING(184,3),
	PRECIOUS_EXPERIENCE(185,2),DISTURB_ATTACK(186,2),GHOLL_WITCHCRAFT(187,2),
	ENGINEER_REFIT(188,3),WEIRD_THROW(189,2),THRID_HAND(190,2),WATER_ATTACK(191,2),
	MORE_CHANCE(192,2),AMAZING_EYESIGHT(193,2),LIGHT_APPLICATION(194,2),
	HEAVY_APPLICATION(195,2),BLESS_MEAL(196,2),BOMB_MANIAC(197,2),
	PHANTOM_SHOOTER(198,3),WAKE_SNAKE(199,2),COLLECTION_GOLD(200,2),
	MARTIAL_TRAIN(201,3),THICKENED_ARMOR(202,2),POWERFUL_CALCULATIONS(203,2),
	INSERT_BID(204,2),MEAL_SHIELD(205,2),DROP_RESISTANT(206,2),
	STRENGTH_TRAIN(207,2),TREAT_MEAL(208,2),COVER_SCAR(209,2),
	NURTRITIOUS_MEAL(210,2),RAGE_ATTACK(211,3),MILITARY_WATERSKIN(212,2),
	JASMINE_TEA(213,2),GOD_LEFTHAND(214,2),GOD_RIGHTHAND(219,2),
	GOLD_FORMATION(220,3),STRENGTHEN_CHAIN(221,2),STRENGTHEN_CHALICE(222,2),
	SAVAGE_PHYSIQUE(223,2),ICE_BREAKING(27,2),BURNING_BLOOD(28,2),
	STRENGTH_GREATEST(29,2),ACCUMULATE_STEADILY(30,3),GET_UP(31,2),
	MORE_TALENT(59,2),GIANT_KILLER(60,2),WANT_ALL(61,2),VEGETARIANISM(62,2),
	WORD_STUN(63,2),NOVICE_BENEFITS(91,2),DOUBLE_TRINKETS(92,3),FISHING_TIME(93,2),
	POSION_DAGGER(94,2),INVINCIBLE_MEAL(95,3),WELLFED_MEAL(123,3),
	SHOCK_BOMB(124,2),ILLUSION_FEED(125,2),HEAVY_BURDEN(126,2),
	ATTACK_DOOR(127,2),ARROW_PENETRATION(155,2),DETOX_DAMAGE(156,3),
	RETURNING_HONOR(157,3),ZHUOJUN_BUTCHER(158,2),AID_STOMACH(159,2),
	WEAPON_MAKE(224,3),SECRET_STASH(225,3),EARTH_MEAL(226,3),
	EATEN_SLOWLY(227,2),INVINCIBLE(228,2),YOG_LARVA(229,3),
	YOG_FIST(230,3),YOG_RAY(231,3),AFRAID_DEATH(232,3),
	PYROMANIAC(233,3),REVERSE_POLARITY(234,3),HERO_NAME(235,3),
	SKY_EARTH(236,3),WATER_ISFOOD(237,3),DELICIOUS_FLYING(238,2),
	ANGEL_STANCE(239,3),THORNY_ROSE(240,2),HOMETOWN_CLOUD(241,3),
	DEEP_FREEZE(242,3),FRENZIED_ATTACK(243,2),WULEI_ZHENGFA(244,2),
	MAGIC_GIRL(245,2),WIDE_KNOWLEDGE(246,3),GOLD_MEAL(247,2),
	NO_VIEWRAPE(248,3),WATER_GHOST(249,2),ASH_LEDGER(250,2),
	SECRET_LIGHTING(251,2),POTENTIAL_1(273,2),POTENTIAL_2(273,2),
	POTENTIAL_3(273,2),POTENTIAL_4(273,2),POTENTIAL_5(273,2),
	POTENTIAL_6(274,3),POTENTIAL_7(274,3),POTENTIAL_8(274,3),
	POTENTIAL_9(274,3),ABYSSAL_GAZE(252,2),MORONITY(253,3),
	JOURNEY_NATURE(254,2),LOVE_BACKSTAB(255,3),CONCEPT_GRID(256,3),
	ABACUS(257,3),TIME_SAND(258,3),DAMAGED_CORE(259,2),
	READ_PROFITABLE(260,3),STRENGTH_CLOAK(261,3),ANESTHESIA(262,2),
	WANLING_POTION(263,3),ACTIVE_MUSCLES(264,3),SEA_WIND(265,3),
	ENDLESS_MEAL(266,3),EXPERIENCE_MEAL(267,2),BIRTHDAY_GIFT(268,3),
	COLLECT_PLANTS(269,3),SHARP_HEAD(270,3),STRENGTH_ARMBAND(271,3),
	STRENGTH_BOOK(272,2),ETERNAL_CURSE(275,2),EATER(276,2),
	FATE_DECISION(277,2),ENDLESS_MALICE(278,2),FEEBLE(279,2),
	MALNUTRITION(280,2),SHORTSIGHTED(281,2),BAT_SERUM(282,2),
	MYOPIA(283,2),LAND_SWIMMING(284,2),JIULONGLA_COFFIN(285,2),
	COWBOY(286,2),MAMBA_OUT(287,2),EXPLOSION_MEAL(288,2),
	HANDON_GROUND(289,2),CHILL_WATER(290,2),LIFE_SPORT(291,2),
	WEAKEN_CHALICE(292,2),EXTREME_CASTING(293,3),PRECISE_SHOT(294,3),
	EXPLORATION_INTUITION(295,2),FUDI_CHOUXIN(296,2),POISON_INBODY(297,2),
	TREASURE_SENSE(298,3),SEED_RECYCLING(299,2),HEDONISM(300,2),
	BEYOND_LIMIT(301,3),HEALTHY_FOOD(302,3),MENTAL_COLLAPSE(303,2),
	UNAVOIDABLE(304,2),PARASITISM(305,2),DUMP_TRUCK(306,2),
	BURNOUT_CHAMPION(307,2),PHOTOPHOBY(308,2),OUTCONTROL_MAGIC(309,2),
	WINTER_SWIMMING(310,2),FIRE_WOOD(311,2),BE_INCONSTANT(312,2),
	UNBEAR_HUNGER(313,2),VIP_MEAL(314,2),FAST_DIE(315,2),
	WASH_HAND(316,2),THORNS_SPRANG(317,2),FULLPASSION(318,2),
	FULLFIGHTING(319,2),PHASECLAW(320,2),BACKFIRED(321,2),
	GOLDOFBOOK(322,2),EXTREME_REACTION(323,3),
	//Cleric T1
	SATIATED_SPELLS(352), HOLY_INTUITION(353), SEARING_LIGHT(354), SHIELD_OF_LIGHT(355),
	//Cleric T2
	ENLIGHTENING_MEAL(356), RECALL_INSCRIPTION(357), SUNRAY(358), DIVINE_SENSE(359), BLESS(360),
	//Cleric T3
	CLEANSE(361, 3), LIGHT_READING(362, 3),
	//Priest T3
	HOLY_LANCE(363, 3), HALLOWED_GROUND(364, 3), MNEMONIC_PRAYER(365, 3),
	//Paladin T3
	LAY_ON_HANDS(366, 3), AURA_OF_PROTECTION(367, 3), WALL_OF_LIGHT(368, 3),
	//Ascended Form T4
	DIVINE_INTERVENTION(369, 4), JUDGEMENT(370, 4), FLASH(371, 4),
	//Trinity T4
	BODY_FORM(372, 4), MIND_FORM(373, 4), SPIRIT_FORM(374, 4),
	//Power of Many T4
	BEAMING_RAY(375, 4), LIFE_LINK(376, 4), STASIS(377, 4),DEEP_FEAR(324),
	NO_DOOR(325),UPDRAFT(326),QUANTUM_HACKING(327),CICADA_DANCE(328,3),CHOCOLATE_COINS(329),
	GHOST_GIFT(330),PERSONAL_ATTACK(331),SHOOT_SATELLITE(332),HOLY_FAITH(333,3),
	HONEY_FISH(334),INSTANT_REFINING(335),CHANGQI_BOOKSTORE(336,3),ASCENSION_CURSE(337),
	DIVINE_PROTECTION(338),SHEPHERD_INTENTION(339),CONVERSION_HOLY(340),PURIFYING_EVIL(341,3),
	DIVINE_STORM(342,3),GENESIS(343),INDULGENCE(344),SILVER_LANGUAGE(345),RESURRECTION(346,3),
	BONE_FIRE(347),JUMP_FACE_SILICONE(348),GRASS_MOB(349),DISASTER_CURSE(350),ASHES_BOW(351,3),
	SWIFT_CHURCH(379,3),PROTECT_CURSE(380),POTENTIAL_ENERGY(381),NIRVANA(382),INSINUATION(383),
	AUTO_PICK(384),HAND_SLIP(385),HAND_DESTRUCTION(386,3),PREDICTIVE_LOVER(387),FIRE_BALL(388),
	FLASH_GENIUS(389),WITCH_POTION(390),TRAITOROUS_SPELL(391),SPRINT_SPELL(392),FOCUS_LIGHT(393),
	ZHUANYU_SPELL(394,3),THORN_WHIP(395),STRONGEST_SHIELD(396),COMBO_PACKAGE(397),BREAK_ENEMY_RANKS(398),
	REVELATION(399),HOLY_GRENADE(400,3),BLADE_STAR(401,3),SACRED_BLADE(402,3),
	EQUIPMENT_BLESS(403,2),HOTLIGHT(404),HEALATTACK(405),EAT_MIND(406),DISABLIITY_POSION(407,3),
	LIGHT_CROP(408),DEVIL_FLAME(409,3),FAST_BREAK(410),CRAZY_DANCER(411),THROWING_RECYCLING(412,3);

	public static final int ATTACK = 0;
	public static final int MAGIC = 1;
	public static final int EFFECT = 2;
	public static final int RESOURCE = 3;
	public static final int SPELL = 4;
	public static final int ASSIST = 5;
	public static final int OTHER = 6;


	public static class LifeBarrior extends Barrier{

		@Override
		public boolean act() {
			incShield(0);
			return super.act();
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
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
			return BuffIndicator.MARK;
		}
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.5f, 0.5f, 1);
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
	public static class AgileAttack extends Buff {

		{
			type = buffType.POSITIVE;
		}
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}
	//史莱姆
	public static class SlimeMucusCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 30); }
		public void Reduce(){
			spend(-1);
			if(visualcooldown()<=0){
				detach();
			}
		}
	};
	public static class LightBox extends FlavourBuff{
		public int icon() { return BuffIndicator.NONE; }
	};
	public static class FeintCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.5f, 1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	//史莱姆娘
	public static class DarkHookCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.1f, 0.1f, 0.1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 100); }
		@Override
		public void detach(){
			Buff.affect(Dungeon.hero, DarkHook.class);
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
		public void tintIcon(Image icon) { icon.hardlight(0f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
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
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class YogRayCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class SmokeCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class InvShaCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 40); }
	};
	public static class WordStunCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.COMBO; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};

	public static class StrAtkCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.COMBO; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
	};

	public static class SurVolCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.COMBO; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 80); }
	};
		public static class SurResistance extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}
	public static class GasCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.COMBO; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 15); }
	};
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
	public static class LiquidAgilEVATracker extends FlavourBuff{};
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

	int icon;
	int maxPoints;

	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21, 31};

	Talent( int icon ){
		this(icon, 2);
	}

	Talent( int icon, int maxPoints ){
		this.icon = icon;
		this.maxPoints = maxPoints;
	}

	public int icon(){
		if (this == HEROIC_ENERGY){
			if (Ratmogrify.useRatroicEnergy){
				return 218;
			}
			HeroClass cls = Dungeon.hero != null ? Dungeon.hero.heroClass : GamesInProgress.selectedClass;
			switch (cls){
				case WARRIOR: default:
					return 26;
				case MAGE:case FREEMAN:
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
			}
		} else {
			return icon;
		}
	}

	public int maxPoints(){
		return maxPoints;
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
		if (metamorphed){
			String metaDesc = Messages.get(this, name() + ".meta_desc");
			if (!metaDesc.equals(Messages.NO_TEXT_FOUND)){
				return Messages.get(this, name() + ".desc") + "\n\n" + metaDesc;
			}
		}
		return Messages.get(this, name() + ".desc");
	}
	public static void onTalentUpgradedItem( Hero hero, Talent talent ){
		int max_item=25;
		if(hero.heroClass==HeroClass.FREEMAN){
			max_item+=13;
		}
		if(Dungeon.talent_item>max_item){
			GLog.w("超出资源限制，无法通过升级天赋获取资源");
			return ;
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
		if(hero.pointsInTalent(WEAPON_MAKE)==3 && talent==WEAPON_MAKE){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(Generator.randomWeapon(5).identify(),hero.pos).sprite.drop(hero.pos);
		}else if(hero.pointsInTalent(WEAPON_MAKE)==2 && talent==WEAPON_MAKE){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(Generator.randomWeapon(4).identify(),hero.pos).sprite.drop(hero.pos);
		}else if(hero.pointsInTalent(WEAPON_MAKE)==1 && talent==WEAPON_MAKE){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(Generator.randomWeapon(3).identify(),hero.pos).sprite.drop(hero.pos);
		}
		if(talent==NOVICE_BENEFITS){
			Dungeon.talent_item+=1;
			Dungeon.level.drop(new ScrollOfMetamorphosis(),hero.pos).sprite.drop();
		}
		if(talent==DOUBLE_TRINKETS && hero.pointsInTalent(DOUBLE_TRINKETS)==3){
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
			Dungeon.level.drop(new WandOfLightning(),hero.pos).sprite.drop(hero.pos);
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
	}



	public static void onTalentUpgraded( Hero hero, Talent talent ){
		int max_item=15;
		if(hero.heroClass==HeroClass.FREEMAN){
			max_item+=13;
		}
		onTalentUpgradedItem(hero,talent );
		hero.updateHT(true);
		updateQuickslot();
		//Dungeon.quickslot.reset();
		//for metamorphosis
		if (talent == IRON_WILL && hero.heroClass != HeroClass.WARRIOR){
			Buff.affect(hero, BrokenSeal.WarriorShield.class);
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
		if (talent == TRAP_MASTER){
			new ReclaimTrap().collect();
		}
		if (talent == HEIGHTENED_SENSES || talent == FARSIGHT || talent==SHORTSIGHTED){
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
			if (!toGive.collect()){
				Dungeon.level.drop(toGive, hero.pos).sprite.drop();
			}
		}

	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}};
	public static class NatureBerriesDropped extends CounterBuff{{revivePersists = true;}};

	public static void onFoodEaten( Hero hero, float foodVal, Item foodSource ){
		int maxeat = 100;
		int oldeat = Dungeon.eat_item;
		if (hero.hasTalent(HEARTY_MEAL)){
			//3/5 HP healed, when hero is below 30% health
			if (hero.HP/(float)hero.HT <= 0.33f) {
				int healing = 2 + 2 * hero.pointsInTalent(HEARTY_MEAL);
				hero.HP = Math.min(hero.HP + healing, hero.HT);
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);

			}
		}
		if(hero.hasTalent(NINJA_MEAL)){
			Shuriken_Box box = hero.belongings.getItem(Shuriken_Box.class);
			if(box != null){
				box.directCharge(hero.pointsInTalent(NINJA_MEAL));
				ScrollOfRecharging.charge(hero);
			}else{
				Buff.affect( hero, ArtifactRecharge.class).set(hero.pointsInTalent(NINJA_MEAL));
			}
		}
		if(hero.hasTalent(YUNYING_MEAL)){
			Buff.affect(hero,Invisibility.class,hero.pointsInTalent(YUNYING_MEAL)*2);
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
			Buff.prolong( hero, Recharging.class, 2 + 3*(hero.pointsInTalent(ENERGIZING_MEAL)) );
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
			if (hero.heroClass == HeroClass.DUELIST){
				//0.67/1 charge for the duelist
				Buff.affect( hero, MeleeWeapon.Charger.class ).gainCharge((hero.pointsInTalent(FOCUSED_MEAL)+1)/3f);
				ScrollOfRecharging.charge( hero );
			} else {
				// lvl/3 / lvl/2 bonus dmg on next hit for other classes
				Buff.affect( hero, PhysicalEmpower.class).set(Math.round(hero.lvl / (4f - hero.pointsInTalent(FOCUSED_MEAL))), 1);
			}
		}
        if (hero.hasTalent(BLESS_MEAL)){
            Buff.affect( hero, Bless.class,2+hero.pointsInTalent(BLESS_MEAL)*3);
        }
		if (hero.hasTalent(MEAL_SHIELD)){
			Buff.affect( hero, Barrier.class).setShield(hero.pointsInTalent(MEAL_SHIELD)*4);
		}
		if(hero.hasTalent(NURTRITIOUS_MEAL)){
			hero.HP = Math.min(hero.HP + hero.pointsInTalent(NURTRITIOUS_MEAL), hero.HT);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(hero.pointsInTalent(NURTRITIOUS_MEAL)), FloatingText.HEALING);
			Buff.affect( hero, Barrier.class).setShield(hero.pointsInTalent(NURTRITIOUS_MEAL));
		}
		if(hero.hasTalent(INVINCIBLE_MEAL) && Dungeon.eat_item<maxeat){
			Buff.affect(hero, Invulnerability.class,hero.pointsInTalent(INVINCIBLE_MEAL));
			Dungeon.eat_item++;
		}
		if(hero.hasTalent(EARTH_MEAL)){
			Barkskin.conditionallyAppend(hero, (hero.lvl*hero.pointsInTalent(Talent.EARTH_MEAL))/3, 1 );
		}
		if(hero.hasTalent(GOLD_MEAL) && Dungeon.eat_item<maxeat){
			Dungeon.level.drop(new Gold().quantity(5+10*hero.pointsInTalent(GOLD_MEAL)), hero.pos).sprite.drop();
			Dungeon.eat_item++;
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
			Buff.affect(hero, PotionOfCleansing.Cleanse.class,hero.pointsInTalent(HEALTHY_FOOD));
		}
		if(hero.pointsNegative(VIP_MEAL)>0){
			Dungeon.gold-=(int)((0.1f*hero.pointsNegative(VIP_MEAL))*Dungeon.gold);
		}
		if (hero.hasTalent(SATIATED_SPELLS)){
			if (hero.heroClass == HeroClass.CLERIC) {
				Buff.affect(hero, SatiatedSpellsTracker.class);
			} else {
				//3/5 shielding, delayed up to 10 turns
				int amount = 1 + 2*hero.pointsInTalent(SATIATED_SPELLS);
				Barrier b = Buff.affect(hero, Barrier.class);
				if (b.shielding() <= amount){
					b.setShield(amount);
					b.delay(Math.max(10-b.cooldown(), 0));
				}
			}
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

			if (hero.subClass == HeroSubClass.WARDEN) {
				Buff.affect(hero, Healing.class).setHeal(Healing, 0, 1);
			} else {
				Buff.affect(hero, Sungrass.Health.class).boost(Healing);
			}
		}
		if(Dungeon.eat_item>=maxeat && eat_item>oldeat){
			GLog.i("无敌一餐，饭里藏金，吃出经验、生日礼物等天赋此后不再触发。");
		}
		if(hero.subClass==HeroSubClass.DARKSLIME && hero.buffs(DarkHookCooldown.class).isEmpty() && hero.buffs(DarkHook.class).isEmpty()){
			Buff.affect(hero, DarkHook.class);
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
		return factor;
	}

	public static void onPotionUsed( Hero hero, int cell, float factor ){
		if(hero.hasTalent(WITCH_POTION)){
			Char mob = Actor.findChar(cell);
			if(mob !=null && !(mob instanceof  Hero)){
				Buff.affect( mob, Charm.class, 5 ).object = hero.id();
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
			Buff.affect(hero, Barrier.class).setShield(1+2*hero.pointsInTalent(ANESTHESIA));
		}
		if(hero.hasTalent(WANLING_POTION)){
			Buff.affect(hero, PotionOfCleansing.Cleanse.class,1*hero.pointsInTalent(WANLING_POTION));
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
			GameScene.show(new WndDivination(IDed));
		}
	}

	public static void onScrollUsed( Hero hero, int pos, float factor, Class<?extends Item> cls ){
		int max_item=15;
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
			switch(Random.Int(hero.pointsInTalent(READ_PROFITABLE)+3)){
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
					hero.HP=Math.min(hero.HT,(int)(hero.HP+hero.HT/8));
					hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(hero.HT / 8), FloatingText.HEALING );
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

	public static int onAttackProcMult( Hero hero, Char enemy, int dmg ){
		if(hero.hasTalent(HEDONISM) && hero.buff(Hunger.class).level<300){
			dmg+=2*hero.pointsInTalent(HEDONISM);
		}
		if(hero.pointsInTalent(PUMP_ATTACK)>0 &&  Random.Int( 5 )==1 && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			dmg = Math.round(dmg * (1.0f + 1.0f*hero.pointsInTalent(PUMP_ATTACK)));
			GLog.i("此一击积蓄了很强的力量，造成多倍伤害");
		}
		if (hero.pointsInTalent(Talent.OVERLOAD_CHARGE)==1 && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null) && (hero.buff(Recharging.class)!=null || hero.buff(ArtifactRecharge.class)!=null)){
			dmg*=1.3;
		}else if(hero.pointsInTalent(Talent.OVERLOAD_CHARGE)==2 && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null) && (hero.buff(Recharging.class)!=null || hero.buff(ArtifactRecharge.class)!=null)){
			dmg*=1.5;
		}
		if(hero.attackDelay()>1 && hero.pointsInTalent(Talent.OVERWHELMING)>=1){
			dmg*=1+(hero.attackDelay()-1)*0.33*hero.pointsInTalent(Talent.OVERWHELMING);
		}
		if( hero.pointsInTalent(JUSTICE_PUNISH)>=1 && (enemy.properties().contains(Char.Property.UNDEAD)|| enemy.properties().contains(Char.Property.DEMONIC))){
			dmg*=1.1+0.1*hero.pointsInTalent(JUSTICE_PUNISH);
		}
		if(hero.pointsInTalent(AMAZING_EYESIGHT)>0 && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null) && Dungeon.level.distance(hero.pos,enemy.pos)>=4-hero.pointsInTalent(AMAZING_EYESIGHT)){
			dmg*=1.2;
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
			dmg*=(8/10-hero.pointsInTalent(AFRAID_DEATH)/10);
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
		if(hero.pointsInTalent(PRECISE_SHOT)>0 && !(hero.belongings.attackingWeapon() instanceof MeleeWeapon) && Dungeon.level.distance(hero.pos,enemy.pos)>=3){
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
			dmg*=1+0.05*hero.pointsInTalent(POISON_INBODY)*cnt;
		}
		if(hero.pointsNegative(PHOTOPHOBY)>0 && !hero.buffs(Light.class).isEmpty()){
			dmg*=1-0.2*hero.pointsNegative(PHOTOPHOBY);
		}
		if(hero.hasTalent(SHOOT_SATELLITE) && enemy.flying==true){
			dmg*=1+0.1+0.1*hero.pointsInTalent(SHOOT_SATELLITE);
			if(hero.belongings.attackingWeapon() instanceof ThrowingStone){
				dmg*=2;
			}
		}
		if(hero.hasTalent(KONO_FUKUSA) && (((Mob) enemy).surprisedBy(hero) || ((Mob) enemy).state==((Mob) enemy).HUNTING)){
			dmg*=1+0.2*hero.pointsInTalent(KONO_FUKUSA);
		}
		if(hero.buff(OneSword.OKU_OneSword.class)!=null && hero.belongings.attackingWeapon() instanceof MeleeWeapon){
			Buff.affect(enemy,OneSword.Kill.class);
			float onesword = 1.3f;
			if((hero.belongings.attackingWeapon() instanceof Katana) || (hero.belongings.attackingWeapon() instanceof Wakizashi)){
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
		if(hero.hasTalent(QIANFA_THROWING) && hero.pointsInTalent(QIANFA_THROWING)>Random.Int(10) && hero.belongings.attackingWeapon() instanceof MissileWeapon){
			dmg *=3;
		}
		return dmg;
	}
	public static int onAttackProcBonus( Hero hero, Char enemy){
		int dmg =0;
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
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon) {
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
					&& !(hero.belongings.attackingWeapon() instanceof MissileWeapon)){
				hero.buff(PatientStrikeTracker.class).detach();
				dmg += Random.IntRange(hero.pointsInTalent(Talent.PATIENT_STRIKE), 2);
			}
		}
		if(hero.subClass == HeroSubClass.DARKSLIME && Random.Int(10)<5){
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
		return dmg;
	}

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		dmg = onAttackProcMult(hero,enemy,dmg)+onAttackProcBonus(hero,enemy);
		if (hero.hasTalent(DEADLY_FOLLOWUP) && enemy.alignment == Char.Alignment.ENEMY) {
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon) {
				if (!(hero.belongings.attackingWeapon() instanceof SpiritBow.SpiritArrow)) {
					Buff.prolong(hero, DeadlyFollowupTracker.class, 5f).object = enemy.id();
				}
			} else if (hero.buff(DeadlyFollowupTracker.class) != null
					&& hero.buff(DeadlyFollowupTracker.class).object == enemy.id()){
				dmg = Math.round(dmg * (1.0f + .1f*hero.pointsInTalent(DEADLY_FOLLOWUP)));
			}
		}
		if(hero.hasTalent(OOZE_ATTACK)&& Random.Int( 4 )<=hero.pointsInTalent(OOZE_ATTACK) && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			Buff.affect( enemy, Ooze.class ).set(15);
			Viscosity.DeferedDamage deferred=Buff.affect( enemy, Viscosity.DeferedDamage.class );
			deferred.prolong( 10 );
			enemy.sprite.burst( 0x000000, 5 );
		}
		if( hero.pointsInTalent(STRONG_ATTACK)>=1 && hero.buffs(Talent.StrAtkCooldown.class).isEmpty() && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			Buff.affect( enemy, Vulnerable.class ,1+hero.pointsInTalent(STRONG_ATTACK)*2);
			Buff.affect(hero,StrAtkCooldown.class,15);

		}
		if(hero.pointsInTalent(Talent.DISTURB_ATTACK)>Random.Int(10)){
			Buff.affect(enemy, Vertigo.class,3);
		}
		if(hero.pointsInTalent(Talent.GHOLL_WITCHCRAFT)>0 && !enemy.buffs(PinCushion.class).isEmpty()){
			Buff.affect(enemy, Hex.class,hero.pointsInTalent(Talent.GHOLL_WITCHCRAFT));
		}
		if (hero.hasTalent(COVER_SCAR) && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			Buff.affect( enemy, Bleeding.class).set(hero.pointsInTalent(COVER_SCAR));
		}
		if(hero.hasTalent(ICE_BREAKING) && !enemy.buffs(Chill.class).isEmpty()){
			enemy.damage(hero.pointsInTalent(ICE_BREAKING), new WandOfMagicMissile());
		}
		if(hero.hasTalent(POSION_DAGGER) && (hero.belongings.attackingWeapon() instanceof Dagger ||
				hero.belongings.attackingWeapon() instanceof Dirk  || hero.belongings.attackingWeapon() instanceof  AssassinsBlade)){
			Buff.affect(enemy,Poison.class).set(hero.pointsInTalent(POSION_DAGGER)*2);
		}
		if(!hero.buffs(YogFistPower.class).isEmpty()){
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
			Buff.affect(hero, Barrier.class).incShield(2);
		}
		if(hero.hasTalent(DAMAGED_CORE)){
			//enemy.damage(hero.pointsInTalent(DAMAGED_CORE), new WandOfMagicMissile());
			enemy.damage(1+ Random.Int(hero.pointsInTalent(DAMAGED_CORE)-1), new WandOfMagicMissile());
		}
		if(hero.pointsInTalent(FUDI_CHOUXIN)> Random.Int(4)){
			Buff.affect(enemy, Weakness.class,2);
		}
		if(hero.pointsNegative(MENTAL_COLLAPSE)> Random.Int(10)){
			hero.damage(dmg/2,new WandOfMagicMissile());
		}
		if(hero.pointsNegative(Talent.FIRE_WOOD)>Random.Int(20)){
			Buff.affect(hero, Burning.class).reignite(hero,3);
			Buff.affect(enemy, Burning.class).reignite(enemy,3);
			Buff.affect(hero, Charm.class,3);
			Buff.affect(enemy, Charm.class,3);
		}
		if(hero.hasTalent(ASHES_BOW) && hero.buffs(AshesBowCooldown.class).isEmpty() &&
				(hero.belongings.attackingWeapon() instanceof MissileWeapon
						|| hero.belongings.attackingWeapon() instanceof SpiritBow)){
			Buff.affect(enemy, Burning.class).reignite(enemy,hero.pointsInTalent(ASHES_BOW));
			Buff.affect(hero,AshesBowCooldown.class,15);
		}
		if(hero.pointsInTalent(NIRVANA)==2 && !hero.buffs(Burning.class).isEmpty()){
			Buff.affect(enemy, Burning.class).extend(3);
		}
		if(hero.pointsInTalent(HAND_DESTRUCTION) * 0.05f > ((float)(enemy.HP)/(float)(enemy.HT)) && !enemy.isImmune(Grim.class) && enemy.resist(Grim.class)==1f){
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
		if(hero.hasTalent(COMBO_PACKAGE) && c!=null && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			c.left++;
			if(c.left>=8-2*hero.pointsInTalent(COMBO_PACKAGE)){
				onFoodEaten(hero,0,null);
				c.left-=8-2*hero.pointsInTalent(COMBO_PACKAGE);
				if(c.left<=0){
					c.detach();
				}
			}
		}else if(hero.hasTalent(COMBO_PACKAGE) && c==null && ((hero.belongings.attackingWeapon() instanceof MeleeWeapon) || hero.belongings.attackingWeapon()==null)){
			Buff.affect(hero,ComboPackage.class).left=1;
		}

		if(hero.pointsInTalent(THROWING_RECYCLING)>Random.Int(4) && enemy.buff(PinCushion.class) != null ){
			while (enemy.buff(PinCushion.class) != null) {
				Item item = enemy.buff(PinCushion.class).grabOne();
				if (item.doPickUp(hero, enemy.pos)) {
					hero.spend(-item.picktime(hero, enemy.pos)); //casting the spell already takes a turn
					GLog.i( Messages.capitalize(Messages.get(hero, "you_now_have", item.name())) );

				} else {
					GLog.w(Messages.get(TelekineticGrab.class, "cant_grab"));
					Dungeon.level.drop(item, enemy.pos).sprite.drop();
				}
			}
		}
		if(hero.subClass==HeroSubClass.TATTEKI_NINJA && (hero.belongings.attackingWeapon() instanceof MissileWeapon)){
			Buff.affect(enemy, Tatteki.Fix.class);
		}
		if(hero.buff(Ninja_Energy.Throw_Skill.class)!=null && (hero.belongings.attackingWeapon() instanceof MissileWeapon) && enemy.isAlive()){
			Ninja_Energy.Throw_Skill b = hero.buff(Ninja_Energy.Throw_Skill.class);
			b.detach();
			if(hero.buff(Ninja_Energy.Gas_Storage.class)!=null){
				Ninja_Energy.Gas_Storage gas_storage=hero.buff(Ninja_Energy.Gas_Storage.class);
				for(Blob blob:gas_storage.blobs.values()){
					GameScene.add(Blob.seed(enemy.pos,10,blob.getClass()));
				}
				gas_storage.detach();
			}
			if(Dungeon.level.map[hero.pos] == Terrain.GRASS || Dungeon.level.map[hero.pos] == Terrain.EMBERS
					|| Dungeon.level.map[hero.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[hero.pos] == Terrain.FURROWED_GRASS){
				Plant plant = (Plant) Reflection.newInstance(Random.element(SpiritBow.harmfulPlants));
				plant.pos = enemy.pos;
				plant.activate( enemy.isAlive() ? enemy : null );
			}else if(Dungeon.level.water[hero.pos]){
				Ninja_Energy.NinjaAbility.Throw_Water(hero.pos,enemy.pos);
			}else{
				Buff.affect(enemy, Cripple.class,5);
				Buff.affect(enemy,Bleeding.class).set(dmg*0.5f);
			}
		}
		if(hero.buff(NinjaSocial.class)!=null){
			hero.buff(NinjaSocial.class).left--;
			if(hero.buff(NinjaSocial.class).left<0){
				hero.buff(NinjaSocial.class).detach();
			}
		}

		return dmg;
	}

	public static int onDefenseProc( Char enemy, int damage ) {
		//史莱姆
		//此处应写在蜕变Talent.java中onDefenseProc
		if(hero.pointsInTalent(Talent.DARK_GAS)>0){
			boolean darkGasTrue =false;
			switch (hero.pointsInTalent(Talent.DARK_GAS)){
				case 3:
					if(hero.subClass == HeroSubClass.DARKSLIME && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, StenchGas.class));
						darkGasTrue =true;
					}
				case 2:
					if(hero.subClass == HeroSubClass.DARKSLIME && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, CorrosiveGas.class));
						darkGasTrue =true;
					}
				case 1:
					if(hero.subClass == HeroSubClass.DARKSLIME && Random.Int( 5 ) == 0){
						GameScene.add(Blob.seed(hero.pos, 20, ToxicGas.class));
						darkGasTrue =true;

					}
					break;
			}
			if(darkGasTrue){
				Buff.affect(hero, BlobImmunity.class,3);
			}

		}

		if(hero.hasTalent(Talent.RAGE_ATTACK)){
			damage*=1+hero.pointsInTalent(Talent.RAGE_ATTACK)*0.1;
		}

		if(hero.pointsInTalent(SMOKE_MASK)==2 && hero.buffs(SmokeCooldown.class).isEmpty()){
			GameScene.add( Blob.seed( hero.pos, 100, SmokeScreen.class ) );
			Buff.affect(hero, SmokeCooldown.class, 20f);
		}else if (hero.pointsInTalent(SMOKE_MASK)==1 && hero.buffs(SmokeCooldown.class).isEmpty()){
			Buff.affect(hero, SmokeCooldown.class, 20f);
			GameScene.add( Blob.seed( hero.pos, 75, SmokeScreen.class ) );
		}
		if (hero.pointsInTalent(Talent.SURVIVAL_VOLITION)>=1 && hero.HP<hero.HT*0.3 && hero.buffs(SurVolCooldown.class).isEmpty()){
			int duration=1+hero.pointsInTalent(Talent.SURVIVAL_VOLITION);
			Buff.affect(hero, Haste.class, duration);
			Buff.affect(hero,Talent.SurVolCooldown.class,80);
			Buff.affect(hero, PotionOfCleansing.Cleanse.class, duration);
			Buff.affect(hero,Talent.SurResistance.class,duration);
		}

		if(hero.pointsInTalent(COUNTER_ATTACK)>0 && Dungeon.level.distance(hero.pos,enemy.pos)<2 &&
				Random.Int(100)<5+hero.pointsInTalent(COUNTER_ATTACK)*15){
			enemy.damage((int)(hero.lvl*(hero.pointsInTalent(COUNTER_ATTACK)+1)*0.1),new WandOfMagicMissile());

		}

		if(!hero.buffs(Talent.SurResistance.class).isEmpty()){
			damage*=0.5;
		}
		if(!hero.buffs(Light.class).isEmpty() && hero.hasTalent(ANGEL_STANCE)){
			enemy.damage((int)(damage*(0.2+0.1*hero.pointsInTalent(ANGEL_STANCE))), new WandOfMagicMissile());
		}
		if(hero.hasTalent(THORNY_ROSE)){
			Buff.affect(enemy, Bleeding.class).set(1);
			if(hero.pointsInTalent(THORNY_ROSE)==2){
				enemy.damage(1,new WandOfMagicMissile());
			}
		}
		if(hero.hasTalent(NO_VIEWRAPE)){
			if(hero.distance(enemy)>1){
				Buff.affect(enemy, Blindness.class,hero.pointsInTalent(NO_VIEWRAPE));
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
		if(hero.subClass == HeroSubClass.DARKSLIME && Random.Int(20)<3){
			Buff.affect(enemy,Ooze.class).set(20);
		}
		//史莱姆娘7.22
		if (hero.subClass == HeroSubClass.WATERSLIME && hero.buff(Talent.SlimeMucusCooldown.class) == null){
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
		return (int)damage;
	}



	public static int onDamage(  int dmg, Object src  ){
		if(hero.hasTalent(Talent.WELLFED_MEAL) && !hero.buffs(WellFed.class).isEmpty()){
			dmg*=1-hero.pointsInTalent(Talent.WELLFED_MEAL)*0.05f;
		}
		if(!hero.buffs(Talent.NoSleep.class).isEmpty() &&  hero.hasTalent(Talent.GET_UP)){
			Buff.affect(hero, Adrenaline.class,0.5f+0.5f*hero.pointsInTalent(Talent.GET_UP));
			hero.buff(Talent.NoSleep.class).detach();
		}
		if (hero.subClass == HeroSubClass.WATERSLIME){
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
				AllyBuff.affectAndLoot(mob, hero, ScrollOfSirensSong.Enthralled.class);
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
			if(Random.Int(4)==0){
				for (Buff b :hero.buffs()){
					if (b.type == Buff.buffType.NEGATIVE
							&& !(b instanceof AllyBuff)
							&& !(b instanceof LostInventory)){
						b.detach();
					}
				}
			}
		}
		if(hero.hasTalent(Talent.CONCEPT_GRID) && !(src instanceof Viscosity.DeferedDamage) && !(src instanceof Hunger)){
			dmg-=hero.pointsInTalent(Talent.CONCEPT_GRID);
		}

		if(hero.pointsNegative(Talent.UNAVOIDABLE)>0 && !(src instanceof Electricity || src instanceof Bleeding
				|| src instanceof Hunger || src instanceof Viscosity.DeferedDamage)){
			dmg=Math.max((int)(hero.HT*0.05*hero.pointsNegative(Talent.UNAVOIDABLE)),dmg);
		}
		if (hero.buff(Talent.WarriorFoodImmunity.class) != null){
			if (hero.pointsInTalent(Talent.IRON_STOMACH) == 1)       dmg = Math.round(dmg*0.25f);
			else if (hero.pointsInTalent(Talent.IRON_STOMACH) == 2)  dmg = Math.round(dmg*0.00f);
		}
		if(hero.pointsNegative(HAND_SLIP)>0 && hero.lastAction instanceof HeroAction.Move){
			dmg *= 1 + 0.25f * hero.pointsNegative(HAND_SLIP);
		}
		if (AntiMagic.RESISTS.contains(src.getClass()) && hero.belongings.armor() != null){
			int armDr = Random.NormalIntRange( hero.belongings.armor().DRMin(), hero.belongings.armor().DRMax());
			if (hero.STR() < hero.belongings.armor().STRReq()){
				armDr -= 2*(hero.belongings.armor().STRReq() - hero.STR());
			}
			if(hero.hasTalent(Talent.STRONGEST_SHIELD) && Random.Int(2)==1 && armDr>0){
				dmg -= armDr;
			}
		}
		//史莱姆
		if(hero.hasTalent(Talent.ENERGY_ABSORPTION)){
			if(hero.heroClass == HeroClass.SLIMEGIRL){
				dmg = (int)(Math.min(dmg,hero.HT/(4+2*hero.pointsInTalent(ENERGY_ABSORPTION))));
			}else{
				dmg = (int)(Math.min(dmg,hero.HT*(0.4f-0.1f*hero.pointsInTalent(ENERGY_ABSORPTION))));
			}

		}
		if(hero.armorAbility!=null && hero.armorAbility instanceof Decoy && dmg>hero.HP){
			ArrayList<Decoy.Decoyman> decoymen = Decoy.getDecoymanAlly();
			if(decoymen!=null){
				decoymen.get(0).damage(114514,src);
				return 0;
			}
		}

		return Math.round(dmg);
	}

	public static void onWandProc( Char target, int wandLevel, int chargesUsed){
		if(hero != null && hero.hasTalent(Talent.INSINUATION) && target instanceof Mob && ((Mob) target).state== ((Mob) target).SLEEPING){
			Buff.affect(target, Poison.class).set(1+2*hero.pointsInTalent(Talent.INSINUATION));
		}
		if (hero.hasTalent(Talent.ARCANE_VISION)) {
			int dur = 5 + 5* hero.pointsInTalent(Talent.ARCANE_VISION);
			Buff.append(hero, TalismanOfForesight.CharAwareness.class, dur).charID = target.id();
		}
		if (hero.hasTalent(Talent.BURNING_CURSE) && !target.buffs(Burning.class).isEmpty()){
			if(hero.pointsInTalent(Talent.BURNING_CURSE)==1){
				Buff.affect(target, Weakness.class,5);
			}else if(hero.pointsInTalent(Talent.BURNING_CURSE)==2){
				Buff.affect(target, Weakness.class,5);
				Buff.affect(target, Terror.class,3);
			}
		}
		if(hero.hasTalent(Talent.MORONITY) && target!=hero && Random.Int(2)==1){
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
					if (ch != null && ch!=hero && ch.alignment != Char.Alignment.ALLY) {
						ch.damage(Random.NormalIntRange(40, 50+hero.pointsInTalent(Talent.YOG_RAY)*10), new Eye.DeathGaze());
					}
				}
			}
		}
		if(hero.hasTalent(Talent.QUANTUM_HACKING) && !(target instanceof NPC) && !(target instanceof Hero)){
			Buff.affect(target, Vertigo.class,1+hero.pointsInTalent(Talent.QUANTUM_HACKING));
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
					AllyBuff.affectAndLoot(mob, hero, ScrollOfSirensSong.Enthralled.class);
					mob.state= mob.HUNTING;
					GameScene.add( mob );
					ScrollOfTeleportation.appear( mob, respawnPoints.get( index ) );

				}else {
					Warlock mob = new Warlock();
					AllyBuff.affectAndLoot(mob, hero, ScrollOfSirensSong.Enthralled.class);
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
					&& hero.buff(Talent.LethalHasteCooldown.class) == null){
				Buff.affect(hero, Talent.LethalHasteCooldown.class, 100f);
				Buff.affect(hero, GreaterHaste.class).set(2 + 2* hero.pointsInTalent(Talent.LETHAL_HASTE));
			}
			if(hero.hasTalent(Talent.CICADA_DANCE)){
				Buff.affect(hero, Invisibility.class,1+hero.pointsInTalent(Talent.CICADA_DANCE));
			}
		}
		if(hero.hasTalent(Talent.INVINCIBLE)){
			Buff.affect(hero,Adrenaline.class,hero.pointsInTalent(Talent.INVINCIBLE)*2+1);
		}
		if( hero.pointsInTalent(Talent.JUSTICE_PUNISH)>=1 && (emeny.properties().contains(Char.Property.UNDEAD)|| emeny.properties().contains(Char.Property.DEMONIC))) {
			Buff.affect(hero, Bless.class, hero.pointsInTalent(Talent.JUSTICE_PUNISH));
		}
		if(hero.hasTalent(Talent.INSTANT_REFINING) && hero.pointsInTalent(Talent.INSTANT_REFINING)+1>Random.Int(20)){
			Dungeon.energy+=1;
			hero.sprite.showStatusWithIcon( 0x44CCFF, Integer.toString(1), FloatingText.ENERGY );
		}
		if(hero != null && hero.subClass==HeroSubClass.DARKSLIME && hero.pointsInTalent(Talent.DARK_LIQUID)==3 && emeny.buff(Roots.class)!=null){
			Buff.affect(hero, Hunger.class).satisfy(45);
			Talent.onFoodEaten(hero,90,null);
			SpellSprite.show(hero, SpellSprite.FOOD);
			Sample.INSTANCE.play(Assets.Sounds.EAT);
			Talent.DarkHookCooldown b = hero.buff(Talent.DarkHookCooldown.class);
			if(b!=null && cause != Chasm.class){
				b.detach();
			}
		}
	}

	public static void onMobDie(Mob mob,  Object cause ){
		if(hero.hasTalent(Talent.KILL_CONTINUE) && hero.buff(OneSword.OKU_OneSword.class)!=null
				&& cause==hero  && hero.belongings.attackingWeapon() instanceof MeleeWeapon){
			Buff.affect(hero, OneSword.OKU_OneSword.class, hero.pointsInTalent(Talent.KILL_CONTINUE));
		}
		if(hero.hasTalent(Talent.BURNING_BLOOD)){
			hero.HP=Math.min(hero.pointsInTalent(Talent.BURNING_BLOOD)+hero.HP,hero.HT);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(hero.pointsInTalent(Talent.BURNING_BLOOD)), FloatingText.HEALING);
		}

		if(mob.properties().contains(INORGANIC) &&  hero.pointsInTalent(Talent.ENGINEER_REFIT)> Random.Int(8) && cause == hero
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

		if(hero.hasTalent(Talent.ASH_LEDGER)){
			Buff.affect(hero, Barrier.class).setShield(hero.pointsInTalent(Talent.ASH_LEDGER)*2);
		}
		if(hero.pointsNegative(Talent.FULLFIGHTING)*3>Random.Int(10) && hero.pos != -1){
			for (Mob m : Dungeon.level.mobs) {
				m.beckon( hero.pos );
			}
			if (Dungeon.level.heroFOV[mob.pos]) {
				CellEmitter.center( mob.pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
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
	}

	public static class AquaticRecover extends Buff {
		private int AquaticRecover_cnt=0;

		@Override
		public  boolean act() {
			//在水面上每10/5回合恢复一点生命值
			if (((Hero) target).hasTalent(Talent.AQUATIC_RECOVER) && !((Hero) target).flying && Dungeon.level.water[((Hero) target).pos]) {
				//if (Dungeon.level.water[((Hero)target).pos]){
				if (((Hero) target).pointsInTalent(Talent.AQUATIC_RECOVER) >= 2) {
					AquaticRecover_cnt += 2;
				} else {
					AquaticRecover_cnt += 1;

				}
				if (AquaticRecover_cnt >= 10 && ((Hero) target).HP < ((Hero) target).HT) {
					((Hero) target).HP += 1;
					((Hero) target).sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(1), FloatingText.HEALING);
					AquaticRecover_cnt = 0;
				}

			}


			spend(TICK);
			return true;
		}
		
	}
	public static class YogFistPower extends FlavourBuff {
		{ type = Buff.buffType.POSITIVE; }
		//public static final float DURATION	= 5f;
		//public float left=DURATION;
		public int icon() { return BuffIndicator.UPGRADE; }
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.6f, 0f, 0.8f);
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

	public static final int MAX_TALENT_TIERS = 4;

	public static void initClassTalents( Hero hero ){
		initClassTalents( hero.heroClass, hero.talents, hero.metamorphedTalents ,hero.sublimationTalents);
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
		initClassTalents( cls, talents, new LinkedHashMap<>(), new LinkedHashMap<>());
	}

	public static void initNegativeTalent(HeroClass cls,ArrayList<LinkedHashMap<Talent, Integer>> talents, ArrayList<Talent> negativeTalents){
		if(negativeTalents.isEmpty()){
			return;
		}
		for(int i=0;i<negativeTalents.size();i++){
			talents.get(i).put(negativeTalents.get(i), 0);
		}

	}

	public static ArrayList<Talent> getNegativeTalent(){
		//版本限定
		int xianding = 3;

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
		for(int i = 0;i < 5;i++){
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

	}


	public static ArrayList< ArrayList< ArrayList<Talent>>> typeTalent = new ArrayList<>();
	static {
		for(int i = 0;i < 3;i++){
			ArrayList<ArrayList<Talent>> typetalent = new ArrayList<>();
			for(int j=0;j<7;j++){
				typetalent.add(new ArrayList<>());
			}
			typeTalent.add(typetalent);
		}
		Collections.addAll(typeTalent.get(0).get(ATTACK),PROVOKED_ANGER,LINGERING_MAGIC,SUCKER_PUNCH,FOLLOWUP_STRIKE,STRENGTHENING_MEAL,
				PATIENT_STRIKE,STRONG_ATTACK,FEAR_INCARNATION,DISTURB_ATTACK,WATER_ATTACK,COVER_SCAR,STRENGTH_GREATEST,POSION_DAGGER,
				ATTACK_DOOR,WATER_WAVE,AGILE_ATTACK);
		Collections.addAll(typeTalent.get(0).get(MAGIC),EMPOWERING_MEAL,ICE_BREAKING,DAMAGED_CORE,INSINUATION,LIGHT_CROP);
		Collections.addAll(typeTalent.get(0).get(EFFECT),HEARTY_MEAL,BACKUP_BARRIER,PROTECTIVE_SHADOWS,NATURES_AID,AGGRESSIVE_BARRIER,
				POWERFUL_CALCULATIONS,INSERT_BID,MEAL_SHIELD,TREAT_MEAL,NURTRITIOUS_MEAL,SHOCK_BOMB,AID_STOMACH,EATEN_SLOWLY,INVINCIBLE,
				THORNY_ROSE,ASH_LEDGER,SECRET_LIGHTING,ANESTHESIA,JASMINE_TEA,PERSONAL_ATTACK,FLASH_GENIUS,RESILIENT_MEAL,NINJA_MEAL);
		Collections.addAll(typeTalent.get(0).get(RESOURCE),CACHED_RATIONS,NATURES_BOUNTY,THRID_HAND,MORE_TALENT,NOVICE_BENEFITS,ILLUSION_FEED,
				ZHUOJUN_BUTCHER,GOLD_MEAL,EXPERIENCE_MEAL,MILITARY_WATERSKIN,GOLDOFBOOK,GHOST_GIFT,PREDICTIVE_LOVER,LIQUID_PERCEPTION);
		Collections.addAll(typeTalent.get(0).get(SPELL),SATIATED_SPELLS, HOLY_INTUITION, SEARING_LIGHT, SHIELD_OF_LIGHT,
				ASCENSION_CURSE,SHEPHERD_INTENTION,SILVER_LANGUAGE,TRAITOROUS_SPELL,FOCUS_LIGHT,HEALATTACK);
		Collections.addAll(typeTalent.get(0).get(ASSIST),VETERANS_INTUITION,IRON_WILL,SCHOLARS_INTUITION,THIEFS_INTUITION,
				SURVIVALISTS_INTUITION,ADVENTURERS_INTUITION,BOMB_MANIAC,THICKENED_ARMOR,STRENGTH_TRAIN,SAVAGE_PHYSIQUE,
				AUTO_PICK,LIQUID_ARMOR,HUNTING_INTUITION,CRAZY_DANCER,YOU_SCARED_ME);
		Collections.addAll(typeTalent.get(0).get(OTHER),FISHING_TIME,WATER_GHOST,HONEY_FISH,CHOCOLATE_COINS);

		//tier=2
		Collections.addAll(typeTalent.get(1).get(ATTACK),LETHAL_MOMENTUM,IMPROVISED_PROJECTILES,STRONG_THROW,JUSTICE_PUNISH,GHOLL_WITCHCRAFT,
				WEIRD_THROW,AMAZING_EYESIGHT,GIANT_KILLER,ARROW_PENETRATION,FRENZIED_ATTACK,FUDI_CHOUXIN,POISON_INBODY,HEDONISM,
				SHOOT_SATELLITE,LETHAL_HASTE,NINJA_SOCIAL);
		Collections.addAll(typeTalent.get(1).get(MAGIC),ENERGIZING_MEAL,INSCRIBED_POWER,ARCANE_VISION,SHIELD_BATTERY,BURNING_CURSE,
				WULEI_ZHENGFA,MAGIC_GIRL,ABYSSAL_GAZE,QUANTUM_HACKING,FIRE_BALL,EAT_MIND);
		Collections.addAll(typeTalent.get(1).get(EFFECT),IRON_STOMACH,LIQUID_WILLPOWER,MYSTICAL_MEAL,INSCRIBED_STEALTH,INVIGORATING_MEAL,
				LIQUID_NATURE,FOCUSED_MEAL,LIQUID_AGILITY,SURVIVAL_VOLITION,INVISIBILITY_SHADOWS,BLESS_MEAL,COLLECTION_GOLD,GET_UP,
				VEGETARIANISM,WORD_STUN,DELICIOUS_FLYING,BACKFIRED,WITCH_POTION,TOUGH_MEAL,SLIME_GREENHOUSE,YUNYING_MEAL,XIA,FEINT);
		Collections.addAll(typeTalent.get(1).get(RESOURCE),WAND_PRESERVATION,ROGUES_FORESIGHT,PRECIOUS_EXPERIENCE,MORE_CHANCE,WANT_ALL,
				SEED_RECYCLING,INSTANT_REFINING,FAST_BREAK);
		Collections.addAll(typeTalent.get(1).get(SPELL),STRENGTHEN_CHAIN,STRENGTHEN_CHALICE,JOURNEY_NATURE,STRENGTH_BOOK,RECALL_INSCRIPTION,
				SUNRAY, DIVINE_SENSE, BLESS, DIVINE_PROTECTION,CONVERSION_HOLY,GENESIS,INDULGENCE,SPRINT_SPELL,THORN_WHIP,ENLIGHTENING_MEAL,
				REVELATION,EQUIPMENT_BLESS,HOTLIGHT);
		Collections.addAll(typeTalent.get(1).get(ASSIST),WIDE_SEARCH,SILENT_STEPS,REJUVENATING_STEPS,HEIGHTENED_SENSES,DURABLE_PROJECTILES,
				WEAPON_RECHARGING,SWIFT_EQUIP,LIGHT_APPLICATION,HEAVY_APPLICATION,DROP_RESISTANT,GOD_LEFTHAND,GOD_RIGHTHAND,BURNING_BLOOD,
				HEAVY_BURDEN,EXPLORATION_INTUITION,PROTECT_CURSE,POTENTIAL_ENERGY,NIRVANA,RUNIC_TRANSFERENCE,ENERGY_ABSORPTION,
				NATURAL_AFFINITY,QUALITY_ABSORPTION,QUICK_SEARCH);
		Collections.addAll(typeTalent.get(1).get(OTHER),WAKE_SNAKE);

		//tier=3
		Collections.addAll(typeTalent.get(2).get(ATTACK),POINT_BLANK,PRECISE_ASSAULT,DEADLY_FOLLOWUP,COUNTER_ATTACK,OVERWHELMING,PHANTOM_SHOOTER,
				MARTIAL_TRAIN,RAGE_ATTACK,ACCUMULATE_STEADILY,WELLFED_MEAL,SKY_EARTH,DEEP_FREEZE,LOVE_BACKSTAB,ABACUS,PRECISE_SHOT,CICADA_DANCE,
				ASHES_BOW,HAND_DESTRUCTION,THROWING_RECYCLING,QIANFA_THROWING);
		Collections.addAll(typeTalent.get(2).get(MAGIC),DESPERATE_POWER,MAGIC_RECYCLING,ANGEL_STANCE,MORONITY,EXTREME_CASTING,SWIFT_CHURCH,
				EMPOWERING_LIFE,DISABLIITY_POSION,DEVIL_FLAME);
		Collections.addAll(typeTalent.get(2).get(EFFECT),ENHANCED_RINGS,SEER_SHOT,INVINCIBLE_MEAL,DETOX_DAMAGE,EARTH_MEAL,REVERSE_POLARITY,
				WATER_ISFOOD,NO_VIEWRAPE,READ_PROFITABLE,WANLING_POTION,ENDLESS_MEAL,HEALTHY_FOOD);
		Collections.addAll(typeTalent.get(2).get(RESOURCE),TRAP_MASTER,GOLD_FORMATION,DOUBLE_TRINKETS,RETURNING_HONOR,WEAPON_MAKE,SECRET_STASH,
				PYROMANIAC,BIRTHDAY_GIFT,COLLECT_PLANTS,TREASURE_SENSE,CHANGQI_BOOKSTORE);
		Collections.addAll(typeTalent.get(2).get(SPELL),ALLY_WARP,TIME_SAND,STRENGTH_CLOAK,STRENGTH_ARMBAND,CLEANSE, LIGHT_READING,PURIFYING_EVIL,
				DIVINE_STORM,RESURRECTION,ZHUANYU_SPELL,HOLY_GRENADE,BLADE_STAR,SACRED_BLADE);
		Collections.addAll(typeTalent.get(2).get(ASSIST),HOLD_FAST,STRONGMAN,LIGHT_CLOAK,BEHEST,AFRAID_DEATH,HERO_NAME,HOMETOWN_CLOUD,WIDE_KNOWLEDGE,
				CONCEPT_GRID,ACTIVE_MUSCLES,SEA_WIND,BEYOND_LIMIT,EXTREME_REACTION,HOLY_FAITH,ORIGINAL_MONSTER,LIGHT_BOX);
		Collections.addAll(typeTalent.get(2).get(OTHER),ENGINEER_REFIT,SHARP_HEAD);
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
				Collections.addAll(tierTalents, CACHED_RATIONS, THIEFS_INTUITION);
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
		tierTalents.clear();
		Collections.addAll(tierTalents, AQUATIC_RECOVER,PUMP_ATTACK,OOZE_ATTACK,STRONGEST_SHIELD,COMBO_PACKAGE,BREAK_ENEMY_RANKS,
				SURPRISE_THROW,SMOKE_MASK,RUSH);
		for (Talent t : sublimation.keySet()){
			if(tierTalents.contains(t)){
				talents.get(0).put(t, 0);}
		}
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
				/*
			case COMMON:
				Collections.addAll(tierTalents, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES, ARCANE_VISION, SHIELD_BATTERY,
						SILENT_STEPS, ROGUES_FORESIGHT, DURABLE_PROJECTILES, LETHAL_HASTE, SWIFT_EQUIP, REJUVENATING_STEPS,
						SURVIVAL_VOLITION,STRONG_THROW,BURNING_CURSE,INVISIBILITY_SHADOWS,JUSTICE_PUNISH,PRECIOUS_EXPERIENCE,
						GHOLL_WITCHCRAFT,WEIRD_THROW,MORE_CHANCE,AMAZING_EYESIGHT,LIGHT_APPLICATION,HEAVY_APPLICATION,
                        BLESS_MEAL,WAKE_SNAKE,COLLECTION_GOLD,DROP_RESISTANT,GOD_LEFTHAND,
						GOD_RIGHTHAND,STRENGTHEN_CHAIN,STRENGTHEN_CHALICE,BURNING_BLOOD,GET_UP,GIANT_KILLER,WANT_ALL,
						VEGETARIANISM,WORD_STUN,HEAVY_BURDEN,ARROW_PENETRATION,DELICIOUS_FLYING,FRENZIED_ATTACK,
						WULEI_ZHENGFA,MAGIC_GIRL,ABYSSAL_GAZE,JOURNEY_NATURE,STRENGTH_BOOK,EXPLORATION_INTUITION,
						FUDI_CHOUXIN,POISON_INBODY,SEED_RECYCLING,HEDONISM,BACKFIRED,QUANTUM_HACKING,SHOOT_SATELLITE,
						INSTANT_REFINING,DIVINE_PROTECTION,CONVERSION_HOLY,GENESIS,INDULGENCE,PROTECT_CURSE,
						POTENTIAL_ENERGY,NIRVANA);
				break;
			case ADVANCED:
				Collections.addAll(tierTalents, OVERLOAD_CHARGE,GAS_SPURT,FASTING,
						KING_PROTECT,SUMMON_FOLLOWER,WOLFISH_GAZE,ENERGY_CONVERSION);
				break;

				 */

		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();
		Collections.addAll(tierTalents, OVERLOAD_CHARGE,GAS_SPURT,FASTING,
				KING_PROTECT,SUMMON_FOLLOWER,WOLFISH_GAZE,ENERGY_CONVERSION);
		for (Talent t : sublimation.keySet()){
			if(tierTalents.contains(t)){
				talents.get(1).put(t, 0);}
		}
		tierTalents.clear();

		//tier 3
		switch (cls){
			case WARRIOR: default:
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
			case SLIMEGIRL:
				Collections.addAll(tierTalents, ORIGINAL_MONSTER);
				break;
			case NINJA:
				Collections.addAll(tierTalents, LIGHT_BOX);
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
		tierTalents.clear();
		Collections.addAll(tierTalents, YOG_LARVA,YOG_FIST,YOG_RAY);
		for (Talent t : sublimation.keySet()){
			if(tierTalents.contains(t)){
				talents.get(2).put(t, 0);}
		}
		tierTalents.clear();

		//tier4
		//TBD
	}

	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 3
		switch (cls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, ENDLESS_RAGE, DEATHLESS_FURY, ENRAGED_CATALYST);
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, CLEAVE, LETHAL_DEFENSE, ENHANCED_COMBO);
				break;
			case BATTLEMAGE:
				Collections.addAll(tierTalents, EMPOWERED_STRIKE, MYSTICAL_CHARGE, EXCESS_CHARGE);
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS);
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, ENHANCED_LETHALITY, ASSASSINS_REACH, BOUNTY_HUNTER);
				break;
			case FREERUNNER:
				Collections.addAll(tierTalents, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH);
				break;
			case SNIPER:
				Collections.addAll(tierTalents, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES);
				break;
			case WARDEN:
				Collections.addAll(tierTalents, DURABLE_TIPS, BARKSKIN, SHIELDING_DEW);
				break;
			case CHAMPION:
				Collections.addAll(tierTalents, VARIED_CHARGE, TWIN_UPGRADES, COMBINED_LETHALITY);
				break;
			case MONK:
				Collections.addAll(tierTalents, UNENCUMBERED_SPIRIT, MONASTIC_VIGOR, COMBINED_ENERGY);
				break;
			case PRIEST:
				Collections.addAll(tierTalents, HOLY_LANCE, HALLOWED_GROUND, MNEMONIC_PRAYER);
				break;
			case PALADIN:
				Collections.addAll(tierTalents, LAY_ON_HANDS, AURA_OF_PROTECTION, WALL_OF_LIGHT);
				break;
			case WATERSLIME:
				Collections.addAll(tierTalents, WATER_BODY, WATER_REVIVAL, WATER_REGENERATION);
				break;
			case DARKSLIME:
				Collections.addAll(tierTalents, POTENT_OOZE, DARK_GAS, DARK_LIQUID);
				break;
			case TATTEKI_NINJA:
				Collections.addAll(tierTalents, SOKO, KONO_FUKUSA, KUNIKUCHI);
				break;
			case NINJA_MASTER:
				Collections.addAll(tierTalents, SOUL_HUNTING, USE_ENVIRONMENT, MIND_WATER);
				break;
			case FREEMAN:
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

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

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				if (tier.get(talent) > 0){
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
		for (Talent t : hero.negativeTalents){
			negativeBundle.put(t.name(), t);
		}

		bundle.put("replacements", replacementsBundle);
		bundle.put("sublimation", sublimationBundle);
		bundle.put("negative", negativeBundle);
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
				//String value = negativeBundle.getString(key);

				try {
					hero.negativeTalents.add(Talent.valueOf(key));
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}


			}
		}
		if (hero.heroClass != null)     initClassTalents(hero);
		if (hero.subClass != null)      initSubclassTalents(hero);
		if (hero.armorAbility != null)  initArmorTalents(hero);
		if(Dungeon.isChallenged(Challenges.NEGATIVE)){
			Talent.initNegativeTalent(hero.heroClass,hero.talents,hero.negativeTalents);
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

							if (tier.containsKey(talent)) {
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

		WndDivination(ArrayList<Item> IDed ){
			/*
			IconTitle cur = new IconTitle(null,
					Messages.titleCase(Messages.get(LIQUID_PERCEPTION, "title")));
			cur.setRect(0, 0, WIDTH, 0);
			add(cur);

			 */
			/*
			RenderedTextBlock msg = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
			msg.maxWidth(120);
			msg.setPos(0, cur.bottom() + 2);
			add(msg);

			 */
			IconTitle cur = null;
			float pos = 10;

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