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

package com.shatteredpixel.shatteredpixeldungeon.actors;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Panic;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SkilledParry;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Suffering;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Virtue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.friar.HolyPrayer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AuxiliaryDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.BeamingRay;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Blade_Star;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LifeLinkSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.PhantomPiranha;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SlimeMucus;
import com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.AgentMinRewardTracker;
import com.shatteredpixel.shatteredpixeldungeon.custom.buffs.IgnoreArmor;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.ImmortalShieldAffecter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SpearShield;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RitualDagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.LuckyCoin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.SlimeBall;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ChronoSuccubus;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.BeamingRay;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LifeLinkSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;

public abstract class Char extends Actor {

	public interface HealingModifier {
		float incomingHealingReduction();
		default void afterIncomingHealing(int requested, int actual) {}
	}
	
	public int pos = 0;
	
	public CharSprite sprite;
	
	public int HT;
	public int HP;
	
	protected float baseSpeed	= 1;
	protected PathFinder.Path path;

	public int paralysed	    = 0;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;

	//these are relative to the hero
	public enum Alignment{
		ENEMY,
		ENEMY1,
		ENEMY2,
		ENEMY3,
		ENEMY4,
		NEUTRAL,
		ALLY
	}
	public Alignment alignment;
	
	public int viewDistance	= 8;
	
	public boolean[] fieldOfView = null;
	
	private LinkedHashSet<Buff> buffs = new LinkedHashSet<>();
	
	@Override
	protected boolean act() {
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		//throw any items that are on top of an immovable char
		if (properties().contains(Property.IMMOVABLE)){
			throwItems();
		}
		return false;
	}

	protected void throwItems(){
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null && heap.type == Heap.Type.HEAP
				&& !(heap.peek() instanceof Tengu.BombAbility.BombItem)
				&& !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+n]){
					candidates.add(pos+n);
				}
			}
			if (!candidates.isEmpty()){
				Dungeon.level.drop( heap.pickUp(), Random.element(candidates) ).sprite.drop( pos );
			}
		}
	}

	public String name(){
		return Messages.get(this, "name");
	}

	public boolean canInteract(Char c){
		if (Dungeon.level.adjacent( pos, c.pos )){
			return true;
		} else if (c instanceof Hero
				&& alignment == Alignment.ALLY
				&& !hasProp(this, Property.IMMOVABLE)
				&& Dungeon.level.distance(pos, c.pos) <= 2* hero.pointsInTalent(Talent.ALLY_WARP)){
			return true;
		} else {
			return false;
		}
	}
	
	//swaps places by default
	public boolean interact(Char c){

		//don't allow char to swap onto hazard unless they're flying
		//you can swap onto a hazard though, as you're not the one instigating the swap
		if (!Dungeon.level.passable[pos] && !c.flying){
			return true;
		}

		//can't swap into a space without room
		if (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.pos]
			|| c.properties().contains(Property.LARGE) && !Dungeon.level.openSpace[pos]){
			return true;
		}

		//we do a little raw position shuffling here so that the characters are never
		// on the same cell when logic such as occupyCell() is triggered
		int oldPos = pos;
		int newPos = c.pos;

		//can't swap or ally warp if either char is immovable
		if (hasProp(this, Property.IMMOVABLE) || hasProp(c, Property.IMMOVABLE)){
			return true;
		}

		//warp instantly with allies in this case
		if (c == hero && hero.hasTalent(Talent.ALLY_WARP)){
			PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[pos] == Integer.MAX_VALUE){
				return true;
			}
			pos = newPos;
			c.pos = oldPos;
			ScrollOfTeleportation.appear(this, newPos);
			ScrollOfTeleportation.appear(c, oldPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		//can't swap places if one char has restricted movement
		if (paralysed > 0 || c.paralysed > 0 || rooted || c.rooted
				|| buff(Vertigo.class) != null || c.buff(Vertigo.class) != null){
			return true;
		}

		c.pos = oldPos;
		moveSprite( oldPos, newPos );
		move( newPos );

		c.pos = newPos;
		c.sprite.move( newPos, oldPos );
		c.move( oldPos );
		
		c.spend( 1 / c.speed() );

		if (c == hero){
			if (hero.subClass.is(HeroSubClass.FREERUNNER)){
				Buff.affect(hero, Momentum.class).gainStack();
			}

			hero.busy();
		}
		
		return true;
	}
	
	protected boolean moveSprite( int from, int to ) {
		
		if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.turnTo(from, to);
			sprite.place( to );
			return true;
		}
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
	}

	public boolean blockSound( float pitch ) {
		return false;
	}
	
	protected static final String POS       = "pos";
	protected static final String TAG_HP    = "HP";
	protected static final String TAG_HT    = "HT";
	protected static final String TAG_SHLD  = "SHLD";
	protected static final String TAG_ALIGNMENT = "alignment";
	protected static final String BUFFS	    = "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, HP );
		bundle.put( TAG_HT, HT );
		if (alignment != null) {
			bundle.put( TAG_ALIGNMENT, alignment );
		}
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		pos = bundle.getInt( POS );
		HP = bundle.getInt( TAG_HP );
		HT = bundle.getInt( TAG_HT );
		if (bundle.contains( TAG_ALIGNMENT )) {
			alignment = bundle.getEnum( TAG_ALIGNMENT, Alignment.class );
		}
		if (alignment == null) {
			alignment = Alignment.ENEMY;
		}
		
		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
				if(b instanceof DarkHook){
					ActionIndicator.clearAction();
				}else if(b instanceof Talent.SmokeMask){
					ActionIndicator1.clearAction();
				}
			}
		}
	}

	final public boolean attack( Char enemy ){
		return attack(enemy, 1f, 0f, 1f, DamageTag.PHYSICAL);
	}
	
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
		return attack(enemy, dmgMulti, dmgBonus, accMulti, DamageTag.PHYSICAL);
	}

	public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
			DamageTag... damageTags) {

		if (enemy == null) return false;

		EnumSet<DamageTag> attackTags = DamageTag.of(damageTags);
		if (!attackTags.contains(DamageTag.PHYSICAL)
				&& !attackTags.contains(DamageTag.MAGICAL)) {
			attackTags.add(DamageTag.PHYSICAL);
		}
		if (attackTags.contains(DamageTag.PHYSICAL)
				&& !attackTags.contains(DamageTag.MELEE)
				&& !attackTags.contains(DamageTag.RANGED)) {
			attackTags.add(physicalAttackDeliveryTag());
		}
		
		boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

		if (enemy.isInvulnerable(getClass())) {

			if (visibleFight) {
				enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
			}

			return false;

		} else if (hit(this, enemy, accMulti, attackTags.toArray(new DamageTag[0]))) {
			
			int dr = Math.round(enemy.drRoll() * AscensionChallenge.statModifier(enemy));
			dr = modifyEnemyArmor(enemy, dr);

			if (attackIgnoresArmor(enemy)) {
				dr = 0;
				attackTags.add(DamageTag.NO_ARMOR);
			}

			//we use a float here briefly so that we don't have to constantly round while
			// potentially applying various multiplier effects
			float dmg;
			Preparation prep = buff(Preparation.class);
			if (prep != null){
				dmg = prep.damageRoll(this);
				if (this == hero && hero.hasTalent(Talent.BOUNTY_HUNTER)) {
					Buff.affect(hero, Talent.BountyHunterTracker.class, 0.0f);
				}
			} else {
				dmg = damageRoll();
			}


			dmg = dmg*dmgMulti;

			//flat damage bonus is affected by multipliers
			dmg += dmgBonus;

			if (enemy.buff(GuidingLight.Illuminated.class) != null){
				enemy.buff(GuidingLight.Illuminated.class).detach();
				if (this == Dungeon.hero && Dungeon.hero.hasTalent(Talent.SEARING_LIGHT)){
					dmg += 1 + 2*Dungeon.hero.pointsInTalent(Talent.SEARING_LIGHT);
				}
				if (this != Dungeon.hero && Dungeon.hero.subClass.is(HeroSubClass.PRIEST)){
					enemy.damage(5+Dungeon.hero.lvl, GuidingLight.INSTANCE, DamageTag.MAGICAL);
				}
				if(hero.hasTalent(Talent.HEALATTACK) && this == hero){
					int heal = 3;
					if(hero.pointsInTalent(Talent.HEALATTACK)>1){
						heal =(int)(hero.lvl * 0.3);
					}

					hero.heal(heal);

				}
			}

			Berserk berserk = buff(Berserk.class);
			if (berserk != null) dmg = berserk.damageFactor(dmg);

			if (buff( Fury.class ) != null) {
				dmg *= 1.5f;
			}

			if (buff( PowerOfMany.PowerBuff.class) != null){
				if (buff( BeamingRay.BeamingRayBoost.class) != null
						&& buff( BeamingRay.BeamingRayBoost.class).object == enemy.id()){
					dmg *= 1.3f + 0.05f*Dungeon.hero.pointsInTalent(Talent.BEAMING_RAY);
				} else {
					dmg *= 1.25f;
				}
			}


			for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
				dmg *= buff.meleeDamageFactor();
			}

			dmg *= AscensionChallenge.statModifier(this);

			//friendly endure
			Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
			if (endure != null) dmg = endure.damageFactor(dmg);

			//enemy endure
			endure = enemy.buff(Endure.EndureTracker.class);
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}

			if (enemy.buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}

			if (Dungeon.hero.alignment == enemy.alignment
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(enemy.pos, Dungeon.hero.pos) <= 2 || enemy.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)){
				dmg *= 0.9f - 0.1f*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
			}

			if (enemy.buff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
				dmg *= 0.2f;
			}

			if ( buff(Weakness.class) != null ){
				dmg *= 0.67f;
			}

			//characters influenced by aggression deal 1/2 damage to bosses
			if ( enemy.buff(StoneOfAggression.Aggression.class) != null
					&& enemy.alignment == alignment
					&& (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
				dmg *= 0.5f;
				//yog-dzewa specifically takes 1/4 damage
				if (enemy instanceof YogDzewa){
					dmg *= 0.5f;
				}
			}
            if ( buff( Suffering.Fear.class ) != null) dmg*=0.75f;
			if ( buff( Suffering.Ecstasy.class ) != null) dmg*=1.3f;
			if ( buff( Talent.HeavyWound.class ) != null) dmg*=0.5f;
			if (buff(HolyPrayer.HolyPrayerBlessing.class) != null
					&& (Char.hasProp(enemy, Property.UNDEAD) || Char.hasProp(enemy, Property.DEMONIC))
            && hero!=null && hero.hasTalent(Talent.HATRED_OF_EVIL)) {
				dmg *= 1f+0.2f+0.1f*hero.pointsInTalent(Talent.HATRED_OF_EVIL);
			}
			
			DamageTag[] resolvedAttackTags = attackTags.toArray(new DamageTag[0]);
			int effectiveDamage = enemy.defenseProc(this, Math.round(dmg), resolvedAttackTags);
			boolean enemyAliveAfterDefenseProc = enemy.isAlive();
			int attackHealthBefore = enemy.HP + enemy.shielding();
			//do not trigger on-hit logic if defenseProc returned a negative value
			if (effectiveDamage >= 0) {
				effectiveDamage = Math.max(effectiveDamage - dr, 0);

				if (enemy.buff(Viscosity.ViscosityTracker.class) != null) {
					effectiveDamage = enemy.buff(Viscosity.ViscosityTracker.class).deferDamage(effectiveDamage);
					enemy.buff(Viscosity.ViscosityTracker.class).detach();
				}

				//vulnerable specifically applies after armor reductions
				if (enemy.buff(Vulnerable.class) != null) {
					effectiveDamage *= 1.33f;
				}

				effectiveDamage = attackProc(enemy, effectiveDamage, resolvedAttackTags);
				if (hero != null && hero.hasTalent(Talent.ARMED_UPRISING)
						&& Talent.isArmedUprisingAlly(this)) {
					Talent.onAttackProc(hero, this, enemy, effectiveDamage, resolvedAttackTags);
					if (hero.pointsInTalent(Talent.ARMED_UPRISING) >= 2) {
						effectiveDamage = Talent.onAttackProcMult(hero, enemy, effectiveDamage)+Talent.onAttackProcBonus(hero, enemy);
					}
				}
				if (this instanceof Hero) {
					Combo combo = buff(Combo.class);
					if (combo != null) {
						effectiveDamage += Combo.focusDamageBonus(combo.combatCount(),
								((Hero) this).pointsInTalent(Talent.COMBO_FOCUS));
					}
				}
			}
			if (visibleFight) {
				if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
					hitSound(Random.Float(0.87f, 1.15f));
				}
			}

			// If the enemy is already dead, interrupt the attack.
			// This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
			if (!enemy.isAlive()){
				//attackProc may deal damage directly. Include that damage in the completed
				//attack result, but preserve the old no-on-hit behavior for defenseProc kills.
				if (enemyAliveAfterDefenseProc) {
					finishAttackResolution(enemy, attackHealthBefore, resolvedAttackTags);
				}
				return true;
			}

			boolean hostilePhysicalAttack = this instanceof Hero && enemy.alignment == Alignment.ENEMY;
			int primaryHealthBefore = enemy.HP + enemy.shielding();
			enemy.damage(effectiveDamage, this, resolvedAttackTags);
			int damageDealt = resolvedAttackDamage(enemy, primaryHealthBefore);
			if (hostilePhysicalAttack) {
				Berserk attackBerserk = buff(Berserk.class);
				if (attackBerserk != null) {
					boolean melee = !(((Hero) this).belongings.attackingWeapon() instanceof MissileWeapon);
					attackBerserk.onPhysicalDamageDealt(damageDealt, melee);
				}
			}
			finishAttackResolution(enemy, attackHealthBefore, resolvedAttackTags);
			if (this == Dungeon.hero) {
				AgentMinRewardTracker.onHeroAttackEnemy(enemy, effectiveDamage);
			}



			if (buff(FireImbue.class) != null)  buff(FireImbue.class).proc(enemy);
			if (buff(FrostImbue.class) != null) buff(FrostImbue.class).proc(enemy);

			if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
				enemy.HP = 0;
				if (enemy.buff(Brute.BruteRage.class) != null){
					enemy.buff(Brute.BruteRage.class).detach();
				}
				if (!enemy.isAlive()) {
					enemy.die(this);
				} else {
					//helps with triggering any on-damage effects that need to activate
					enemy.damage(-1, this, resolvedAttackTags);
					DeathMark.processFearTheReaper(enemy);
				}
				if (enemy.sprite != null) {
					enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
				}
			}
			if (!enemy.isAlive() && enemy.alignment != alignment && prep != null) {
				prep.onAssassinationKill();
			}

			Talent.CombinedLethalityAbilityTracker combinedLethality = buff(Talent.CombinedLethalityAbilityTracker.class);
			if (combinedLethality != null && this instanceof Hero && ((Hero) this).belongings.attackingWeapon() instanceof MeleeWeapon && combinedLethality.weapon != ((Hero) this).belongings.attackingWeapon()){
				if ( enemy.isAlive() && enemy.alignment != alignment && !Char.hasProp(enemy, Property.BOSS)
						&& !Char.hasProp(enemy, Property.MINIBOSS) &&
						(enemy.HP/(float)enemy.HT) <= 0.4f*((Hero)this).pointsInTalent(Talent.COMBINED_LETHALITY)/3f) {
					enemy.HP = 0;
					if (enemy.buff(Brute.BruteRage.class) != null){
						enemy.buff(Brute.BruteRage.class).detach();
					}
					if (!enemy.isAlive()) {
						enemy.die(this);
					} else {
						//helps with triggering any on-damage effects that need to activate
						enemy.damage(-1, this, resolvedAttackTags);
						DeathMark.processFearTheReaper(enemy);
					}
					if (enemy.sprite != null) {
						enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Talent.CombinedLethalityAbilityTracker.class, "executed"));
					}
				}
				combinedLethality.detach();
			}

			if (enemy.sprite != null) {
				enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage);
				enemy.sprite.flash();
			}

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == hero) {
					
					if (this == hero) {
						return true;
					}

					if (this instanceof WandOfLivingEarth.EarthGuardian
							|| this instanceof MirrorImage || this instanceof PrismaticImage){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					if (this instanceof RogueBoss){
						if (Dungeon.hero.heroClass == HeroClass.NINJA){
							GLog.n( Messages.capitalize(Messages.get(RogueBoss.class, "executeninja")) );
						}else{
							GLog.n( Messages.capitalize(Messages.get(RogueBoss.class, "kill")) );
						}
					}else{
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
					}
					//GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
					
				} else if (this == hero) {
					GLog.i( Messages.capitalize(Messages.get(Char.class, "defeat", enemy.name())) );
				}
			}
			
			return true;
			
		} else {
			if((enemy instanceof Hero) && hero.hasTalent(Talent.EXTREME_REACTION)){
                    FlavourBuff buff=hero.buff(Adrenaline.class);
				if(buff==null){
					Buff.affect(hero, Adrenaline.class,hero.pointsInTalent(Talent.EXTREME_REACTION));
				}else if( buff.getturns()<=5){
					Buff.affect(hero, Adrenaline.class,hero.pointsInTalent(Talent.EXTREME_REACTION));
				}
			}
			if((enemy instanceof Hero)&& hero.hasTalent(Talent.CRAZY_DANCER)){
				Buff.affect(this,Amok.class,1+hero.pointsInTalent(Talent.CRAZY_DANCER));
			}
			if((enemy instanceof Hero)&& hero.hasTalent(Talent.AGILE_ATTACK)){
				Buff.affect(enemy,Talent.AgileAttack.class);
			}
			if((enemy instanceof Hero)&& hero.hasTalent(Talent.YOU_SCARED_ME) && enemy.shielding()<(hero.pointsInTalent(Talent.YOU_SCARED_ME)+1)*2){
				Buff.affect(enemy, Barrier.class).incShield(Math.min(1+hero.pointsInTalent(Talent.YOU_SCARED_ME),(hero.pointsInTalent(Talent.YOU_SCARED_ME)+1)*2-enemy.shielding()));
			}
			boolean skilledParry = enemy instanceof Hero
					&& SkilledParry.blocks((Hero) enemy, this);
			if (enemy.sprite != null){
				if (skilledParry) {
					enemy.sprite.showStatus(CharSprite.POSITIVE,
							Messages.get(SkilledParry.class, "parried"));
					hitMissIcon = -1;
				} else if (hitMissIcon != -1){
					//dooking is a playful sound Ferrets can make, like low pitched chirping
					// I doubt this will translate, so it's only in English
					if (hitMissIcon == FloatingText.MISS_TUFT && Messages.lang() == Languages.ENGLISH && Random.Int(10) == 0) {
						enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, "dooked", hitMissIcon);
					} else {
						enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, enemy.defenseVerb(), hitMissIcon);
					}
					hitMissIcon = -1;
				} else {
					enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
				}
			}
			if (visibleFight) {
				Sample.INSTANCE.play(skilledParry ? Assets.Sounds.HIT_PARRY : Assets.Sounds.MISS);
			}
            if(this==hero && hero.heroClass==HeroClass.FRIAR && this.buff(Suffering.Paranoia.class)!=null){
                if(this.buff(Reason.class)!=null && Random.Int(10)<3){
                    Reason.sufferingReason(this,5);
                }
            }
			
			return false;
			
		}

	}

    public int heal(int h){

        return heal(h,true);
    }
    public int heal(int h,boolean visual){
        float reduction = 0f;
        for (Buff buff : buffs()) {
            if (buff instanceof HealingModifier) {
                reduction += ((HealingModifier) buff).incomingHealingReduction();
            }
        }
        int modifiedHeal = Math.max(0, (int)(h * (1f - Math.min(0.95f, reduction))));
        int trueHeal = Math.min(modifiedHeal,HT-HP);
        int outHeal = modifiedHeal-trueHeal;
        if(this.buff(Virtue.Firm.class)!=null && outHeal>0){
            Buff.affect(this, Virtue.VirtueBarrier.class).incShield(outHeal);
        }
        //GLog.i(""+outHeal);
        HP+=trueHeal;
        if(visual && trueHeal>0 && sprite!=null) sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(trueHeal), FloatingText.HEALING);
        for (Buff buff : buffs()) {
            if (buff instanceof HealingModifier) {
                ((HealingModifier) buff).afterIncomingHealing(h, trueHeal);
            }
        }
        return trueHeal;
    }

	public static int INFINITE_ACCURACY = 1_000_000;
	public static int INFINITE_EVASION = 1_000_000;

	final public static boolean hit( Char attacker, Char defender, boolean magic ) {
		return hit(attacker, defender, magic ? DamageTag.MAGICAL : DamageTag.PHYSICAL);
	}

	public static boolean hit( Char attacker, Char defender, float accMulti, boolean magic ) {
		return hit(attacker, defender, accMulti,
				magic ? DamageTag.MAGICAL : DamageTag.PHYSICAL);
	}

	final public static boolean hit(Char attacker, Char defender, DamageTag... damageTags) {
		EnumSet<DamageTag> tags = DamageTag.of(damageTags);
		float accMulti = tags.contains(DamageTag.MAGICAL) ? 2f : 1f;
		return hit(attacker, defender, accMulti, damageTags);
	}

	public static boolean hit(Char attacker, Char defender, float accMulti,
			DamageTag... damageTags) {
		float acuStat = attacker.attackSkill( defender );
		float defStat = defender.defenseSkill( attacker );
		if (!(attacker instanceof Hero) && attacker.buff(HolyPrayer.HolyPrayerBlessing.class) != null) {
			acuStat *= attacker.buff(HolyPrayer.HolyPrayerBlessing.class).accuracyAndEvasionFactor();
		}
		if (!(defender instanceof Hero) && defender.buff(HolyPrayer.HolyPrayerBlessing.class) != null) {
			defStat *= defender.buff(HolyPrayer.HolyPrayerBlessing.class).accuracyAndEvasionFactor();
		}
		if (attacker.buff(LuckyCoin.LuckyStrikeTracker.class)!=null){
			acuStat = INFINITE_ACCURACY;
			attacker.buff(LuckyCoin.LuckyStrikeTracker.class).detach();
		}
		if (defender instanceof Hero && ((Hero) defender).damageInterrupt){
			((Hero) defender).interrupt();
		}
		if(attacker instanceof  Hero && hero.hasTalent(Talent.SPECIAL_MARK) && defender.buff(InstructionTool.InstructionMark.class)!=null){
			acuStat*=1.25f+0.25f*hero.pointsInTalent(Talent.SPECIAL_MARK);
		}

		//invisible chars always hit (for the hero this is surprise attacking)
		if (attacker.invisible > 0 && attacker.canSurpriseAttack()){
			acuStat = INFINITE_ACCURACY;
		}

		if (defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null){
			defStat = INFINITE_EVASION;
		}else if (defender.buff(FightStance.Focus.class) != null){
			defStat = INFINITE_EVASION;
		}

		//if accuracy or evasion are large enough, treat them as infinite.
		//note that infinite evasion beats infinite accuracy
		if (defStat >= INFINITE_EVASION){
			hitMissIcon = FloatingText.getMissReasonIcon(attacker, acuStat, defender, INFINITE_EVASION);
			return false;
		} else if (acuStat >= INFINITE_ACCURACY){
			hitMissIcon = FloatingText.getHitReasonIcon(attacker, INFINITE_ACCURACY, defender, defStat);
			return true;
		}

		float acuRoll = Random.Float( acuStat );
		if (attacker.buff(Bless.class) != null) acuRoll *= 1.25f;
		if (attacker.buff(  Hex.class) != null) acuRoll *= 0.8f;
		if (attacker.buff( Daze.class) != null) acuRoll *= 0.5f;
		if(attacker.buff( Talent.NinjaSocial.class) != null) {acuRoll *= 1.5f;}
        if(attacker.buff(  Suffering.Paranoia.class) != null) acuRoll *= 0.8f;
		for (ChampionEnemy buff : attacker.buffs(ChampionEnemy.class)){
			acuRoll *= buff.evasionAndAccuracyFactor();
		}
		acuRoll *= AscensionChallenge.statModifier(attacker);
		acuRoll *= accMulti;
		
		float defRoll = Random.Float( defStat );
		if (defender.buff(Bless.class) != null) defRoll *= 1.25f;
		if (defender.buff(  Hex.class) != null) defRoll *= 0.8f;
		if (defender.buff( Daze.class) != null) defRoll *= 0.5f;
		if (defender.buff( Talent.XiaDef.class) != null) defRoll *= 1.5f;
        if (defender.buff( Suffering.Fear.class) != null) defRoll *= 1.5f;
        if (defender.buff( Suffering.Despair.class) != null) defRoll =0f;
		for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)){
			defRoll *= buff.evasionAndAccuracyFactor();
		}
		defRoll *= AscensionChallenge.statModifier(defender);
		/*
		if (defRoll < acuRoll && (defRoll* FerretTuft.evasionMultiplier()) >= acuRoll){
			tuftDodged = true;
		}

		 */
		defRoll *= FerretTuft.evasionMultiplier();

		if (acuRoll >= defRoll){
			hitMissIcon = FloatingText.getHitReasonIcon(attacker, acuRoll, defender, defRoll);
			return true;
		} else {
			hitMissIcon = FloatingText.getMissReasonIcon(attacker, acuRoll, defender, defRoll);
			return false;
		}
	}

	private static int hitMissIcon = -1;

	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}
	
	public String defenseVerb() {
		return Messages.get(this, "def_verb");
	}
	
	public int drRoll() {
		int dr = 0;

		//Spsh5
		SpearShield spsh = Dungeon.hero.belongings.getItem(SpearShield.class);
		if (spsh != null) {
			//GLog.i("树肤护甲("+0+"-"+Barkskin.currentLevel(this)+")-->("+spsh.changeDrMin(0, Barkskin.currentLevel(this))+"-"+spsh.changeDrMax(0, Barkskin.currentLevel(this))+")");
			dr += Random.NormalIntRange(spsh.changeDrMin(0, Barkskin.currentLevel(this)),spsh.changeDrMax(0, Barkskin.currentLevel(this)));
		}else{
			dr += Random.NormalIntRange( 0 , Barkskin.currentLevel(this) );
		}

		return dr;
	}
	
	public int damageRoll() {
		return 1;
	}

	/**
	 * Allows an attack to alter the physical armor value while retaining the
	 * normal hit, defence-proc, viscosity and shielding pipeline.
	 */
	protected int modifyEnemyArmor(Char enemy, int armor) {
		return armor;
	}

	protected DamageTag physicalAttackDeliveryTag() {
		return DamageTag.MELEE;
	}

	/**
	 * Keeps armor bypass and its damage presentation on the same explicit path.
	 */
	protected boolean attackIgnoresArmor(Char enemy) {
		if (buff(IgnoreArmor.class) != null) {
			return true;
		}
		if (this instanceof Hero) {
			Hero h = (Hero) this;
			return h.hasTalent(Talent.BIG_FIST) && h.belongings.attackingWeapon() == null
					|| h.belongings.attackingWeapon() instanceof MissileWeapon
					&& h.subClass.is(HeroSubClass.SNIPER)
					&& !Dungeon.level.adjacent(h.pos, enemy.pos)
					|| h.belongings.attackingWeapon() instanceof Shuriken_Box.SmallShuriken
					|| h.buff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null;
		}
		return this instanceof InstructionTool.Drone
				&& enemy.buff(InstructionTool.InstructionMark.class) != null;
	}
	
	//TODO it would be nice to have a pre-armor and post-armor proc.
	// atm attack is always post-armor and defence is already pre-armor
	
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			buff.onAttackProc( enemy );
		}
		return damage;
	}

	public int attackProc(Char enemy, int damage) {
		return attackProc(enemy, damage, DamageTag.PHYSICAL);
	}

	/**
	 * Called after a landed attack has passed through the target's complete damage pipeline.
	 * The supplied damage is the final loss of HP and shielding, rather than attackProc's
	 * pre-damage value.
	 */
	protected void onAttackResolved(
			Char enemy, boolean hit, int damageDealt, DamageTag... damageTags) {
	}

	protected int resolvedAttackDamage(Char enemy, int healthBefore) {
		return Math.max(0, healthBefore - enemy.HP - enemy.shielding());
	}

	protected int finishAttackResolution(
			Char enemy, int healthBefore, DamageTag... damageTags) {
		int damageDealt = resolvedAttackDamage(enemy, healthBefore);
		onAttackResolved(enemy, true, damageDealt, damageTags);
		return damageDealt;
	}

	
	public int defenseProc(Char enemy, int damage, DamageTag... damageTags) {

		Earthroot.Armor armor = buff( Earthroot.Armor.class );
		if (armor != null) {
			damage = armor.absorb( damage );
		}

		ShieldOfLight.ShieldOfLightTracker shield = buff( ShieldOfLight.ShieldOfLightTracker.class);
		if (shield != null && shield.object == enemy.id()){
			int min = 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT);
			damage -= Random.NormalIntRange(min, 2*min);
			damage = Math.max(damage, 0);
		} else if (this == Dungeon.hero
				&& Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.SHIELD_OF_LIGHT)
				&& TargetHealthIndicator.instance.target() == enemy){
			//33/50%
			if (Random.Int(6) < 1+Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT)){
				damage -= 1;
			}
		}

		// hero and pris images skip this as they already benefit from hero's armor glyph proc
		if (!(this instanceof Hero || this instanceof PrismaticImage)) {
			if (Dungeon.hero.alignment == alignment && Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {
				damage = Dungeon.hero.belongings.armor().proc( enemy, this, damage );
			}
		}

		return damage;
	}

	public int defenseProc(Char enemy, int damage) {
		return defenseProc(enemy, damage, DamageTag.PHYSICAL);
	}

	//Returns the level a glyph is at for a char, or -1 if they are not benefitting from that glyph
	//This function is needed as (unlike enchantments) many glyphs trigger in a variety of cases
	public int glyphLevel(Class<? extends Armor.Glyph> cls){

		if (Dungeon.hero != null && Dungeon.level != null
				&& this != Dungeon.hero && Dungeon.hero.alignment == alignment
				&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
				&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {

			return Dungeon.hero.glyphLevel(cls);
		} else {
			return -1;
		}
	}

	public float speed() {
		float speed = baseSpeed;
		if ( buff( Cripple.class ) != null ) speed /= 2f;
		if ( buff( Stamina.class ) != null) speed *= 1.5f;
		if ( buff( Adrenaline.class ) != null) speed *= 2f;
		if ( buff( Haste.class ) != null) speed *= 3f;
		if ( buff( Dread.class ) != null) speed *= 2f;
        if ( buff( Suffering.Despair.class ) != null) speed *= 1.2f;
        if(buff(Virtue.Fearless.class)!=null) speed *= 1.5f;
		if(buff(SlimeBall.SlimeOoze.class)!=null){
			speed *= 0.5f;
		}
		if(buff(Ninja_Energy.WaterSmooth.class)!=null && Dungeon.level.water[this.pos]){
			speed *= 2f;
		}
		if(this!=hero){
			speed *= Swiftness.speedBoost(this, glyphLevel(Swiftness.class));
			speed *= Flow.speedBoost(this, glyphLevel(Flow.class));
			speed *= Bulk.speedBoost(this, glyphLevel(Bulk.class));
		}


		return speed;
	}

	//currently only used by invisible chars, or by the hero
	public boolean canSurpriseAttack(){
		return true;
	}
	
	//used so that buffs(Shieldbuff.class) isn't called every time unnecessarily
	private int cachedShield = 0;
	public boolean needsShieldUpdate = true;
	
	public int shielding(){
		if (!needsShieldUpdate){
			return cachedShield;
		}
		
		cachedShield = 0;
		for (ShieldBuff s : buffs(ShieldBuff.class)){
			cachedShield += s.shielding();
		}
		needsShieldUpdate = false;
		return cachedShield;
	}

	static boolean isFriarReasonAttack(HeroClass heroClass, Object src, boolean unavoidable) {
		return !unavoidable && heroClass == HeroClass.FRIAR && src instanceof Mob;
	}

	protected void processFriarReasonLoss(int dmg, Object src, boolean unavoidable) {
		if (this != hero || !isFriarReasonAttack(hero.heroClass, src, unavoidable)) {
			return;
		}
		if(src instanceof Wraith){
			Reason.loseReason(hero,5);
			if(Random.Int(3)==0){
				Buff.extend(this, Panic.class,10);
				Sample.INSTANCE.play( Assets.Sounds.PANIC, 1, 1, Random.Float( 0.9f, 1.1f ) );
			}
		}else if(((Mob) src).properties().contains(Property.UNDEAD) || ((Mob) src).properties().contains(Property.DEMONIC)){
			if(hero.HP<hero.HT/2){
				Reason.loseReason(hero,Math.min(dmg+Random.Int(2,6),30));
				if((src instanceof YogDzewa || src instanceof YogFist) && Random.Int(2)==0){
					Buff.extend(this, Panic.class,10);
					Sample.INSTANCE.play( Assets.Sounds.PANIC, 1, 1, Random.Float( 0.9f, 1.1f ) );
				}else if(Random.Int(5)==0){
					Buff.extend(this, Panic.class,10);
					Sample.INSTANCE.play( Assets.Sounds.PANIC, 1, 1, Random.Float( 0.9f, 1.1f ) );
				}
			}else{
				Reason.loseReason(hero,Math.min(dmg+Random.Int(1,3),30));
			}
		}else if(hero.HP<hero.HT/2){
			Reason.loseReason(hero,Math.min(dmg+Random.Int(2,6),30));
		}
		if(this.buff(Suffering.Fear.class)!=null && Random.Int(10)<3){
			Reason.sufferingReason(hero,5);
		}
	}

	private static Class<?> sourceClass(Object source) {
		if (source instanceof Class<?>) {
			return (Class<?>) source;
		}
		return source == null ? Object.class : source.getClass();
	}

	/**
	 * Compatibility for saved content and external extensions that still call
	 * the two-argument damage method. New in-project calls pass tags explicitly.
	 */
	protected DamageTag[] legacyDamageTags(Object source) {
		EnumSet<DamageTag> tags = EnumSet.noneOf(DamageTag.class);
		Class<?> sourceClass = sourceClass(source);

		if (AntiMagic.RESISTS.contains(sourceClass)) {
			tags.add(DamageTag.MAGICAL);
		} else {
			tags.add(DamageTag.PHYSICAL);
		}
		if (NO_ARMOR_PHYSICAL_SOURCES.contains(sourceClass)) tags.add(DamageTag.NO_ARMOR);
		if (Talent.isUnavoidableDamage(source))              tags.add(DamageTag.UNAVOIDABLE);
		if (source instanceof Pickaxe)                       tags.add(DamageTag.PICKAXE);
		if (source instanceof Hunger)                        tags.add(DamageTag.HUNGER);
		if (source instanceof Burning)                       tags.add(DamageTag.FIRE);
		if (source instanceof Chill || source instanceof Frost) tags.add(DamageTag.FROST);
		if (source instanceof GeyserTrap || source instanceof StormCloud) tags.add(DamageTag.WATER);
		if (source instanceof Electricity)                   tags.add(DamageTag.ELECTRIC);
		if (source instanceof Bleeding)                      tags.add(DamageTag.BLEEDING);
		if (source instanceof ToxicGas)                      tags.add(DamageTag.TOXIC);
		if (source instanceof Corrosion)                     tags.add(DamageTag.CORROSION);
		if (source instanceof Poison)                        tags.add(DamageTag.POISON);
		if (source instanceof Ooze)                          tags.add(DamageTag.OOZE);
		if (source instanceof SlimeMucus)                    tags.add(DamageTag.NO_ARMOR);
		if (source instanceof Viscosity.DeferedDamage)       tags.add(DamageTag.DEFERRED);
		if (source instanceof Corruption)                    tags.add(DamageTag.CORRUPTION);
		if (source instanceof AscensionChallenge)            tags.add(DamageTag.AMULET);
		if (source instanceof Reason)                        tags.add(DamageTag.REASON);
		if (source == Talent.ENDLESS_MALICE)                 tags.add(DamageTag.ENDLESS_MALICE);
		if (source == Talent.LIFE_SPORT)                     tags.add(DamageTag.LIFE_SPORT);

		return tags.toArray(new DamageTag[0]);
	}

	public void damage(int dmg, Object src, DamageTag... damageTags) {

		if (!isAlive() || dmg < 0) {
			return;
		}

		EnumSet<DamageTag> tags = DamageTag.of(damageTags);
		boolean unavoidable = tags.contains(DamageTag.UNAVOIDABLE);
		Class<?> srcClass = sourceClass(src);

		if(!unavoidable && isInvulnerable(srcClass)){
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			return;
		}

		if (!unavoidable && !(src instanceof LifeLink || src instanceof Hunger) && buff(LifeLink.class) != null){
			HashSet<LifeLink> links = buffs(LifeLink.class);
			for (LifeLink link : links.toArray(new LifeLink[0])){
				if (Actor.findById(link.object) == null){
					links.remove(link);
					link.detach();
				}
			}
			dmg = (int)Math.ceil(dmg / (float)(links.size()+1));
			for (LifeLink link : links){
				Char ch = (Char)Actor.findById(link.object);
				if (ch != null) {
					ch.damage(dmg, link, DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
					if (!ch.isAlive()) {
						link.detach();
						if (ch == Dungeon.hero){
							Badges.validateDeathFromFriendlyMagic();
							Dungeon.fail(src);
							GLog.n( Messages.get(LifeLink.class, "ondeath") );
						}
					}
				}
			}
		}

		//temporarily assign to a float to avoid rounding a bunch
		float damage = dmg;

		//if dmg is from a character we already reduced it in Char.attack
		if (!unavoidable && !(src instanceof Char)) {
			if (Dungeon.hero.alignment == alignment
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {
				damage *= 0.9f - 0.1f*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
			}
		}

		if (!unavoidable && buff(PowerOfMany.PowerBuff.class) != null){
			if (buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
				damage *= 0.70f - 0.05f*Dungeon.hero.pointsInTalent(Talent.LIFE_LINK);
			} else {
				damage *= 0.75f;
			}
		}

		Terror t = buff(Terror.class);
		if (t != null){
			t.recover();
		}
		Dread d = buff(Dread.class);
		if (d != null){
			d.recover();
		}
		Charm c = buff(Charm.class);
		if (c != null){
			c.recover(src);
		}
		if (this.buff(Frost.class) != null){
			Buff.detach( this, Frost.class );
		}
		if (this.buff(MagicalSleep.class) != null){
			Buff.detach(this, MagicalSleep.class);
		}
		if (this.buff(Doom.class) != null && !isImmune(Doom.class)){
			damage *= 1.67f;
		}
		if (alignment != Alignment.ALLY && this.buff(DeathMark.DeathMarkTracker.class) != null){
			damage *= 1.25f;
		}
		if(this.buff(AuxiliaryDrone.WeaknessMark.class)!=null){
			damage *= 1.3f;
		}

		if (buff(Sickle.HarvestBleedTracker.class) != null){
			buff(Sickle.HarvestBleedTracker.class).detach();

			if (!isImmune(Bleeding.class)){
				Bleeding b = buff(Bleeding.class);
				if (b == null){
					b = new Bleeding();
				}
				b.announced = false;
				b.set(dmg, Sickle.HarvestBleedTracker.class);
				b.attachTo(this);
				sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
				return;
			}
		}

		if (!unavoidable && isImmune( srcClass )) {
			damage = 0;
		} else if (!unavoidable) {
			damage *= resist( srcClass );
		}
        if(isAlive()){
            RitualDagger.BloodGift.onPiousAttackDamage(hero, this, (int)damage);
        }else{
            RitualDagger.BloodGift.onPiousAttackDamage(hero, this, (int)damage);
        }


		dmg = Math.round(damage);

		//we ceil these specifically to favor the player vs. champ dmg reduction
		// most important vs. giant champions in the earlygame
		if (!unavoidable) {
			for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
				dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
			}
		}

		// Magical mitigation is driven by explicit combat tags, not source-class lists.
		if (!unavoidable && tags.contains(DamageTag.MAGICAL)){
			dmg -= AntiMagic.drRoll(this, glyphLevel(AntiMagic.class));
			if (buff(ArcaneArmor.class) != null) {
				dmg -= Random.NormalIntRange(0, buff(ArcaneArmor.class).level());
			}
			if (dmg < 0) dmg = 0;
		}
        if(buff(MagicImmune.class)!=null && tags.contains(DamageTag.MAGICAL)) {
            dmg = 0;
        }
        //史莱姆
        if(this==hero && !unavoidable){
            if(hero.hasTalent(Talent.ENERGY_ABSORPTION)){
                float ddmg=dmg;
                if(hero.heroClass == HeroClass.SLIMEGIRL){
                    dmg = (int)(Math.min(dmg,hero.HT/(4+2*hero.pointsInTalent(Talent.ENERGY_ABSORPTION))));
                    if(ddmg>=1){
                        dmg = Math.max(dmg,1);
                    }
                }else{
                    dmg = (int)(Math.min(dmg,hero.HT*(0.4f-0.1f*hero.pointsInTalent(Talent.ENERGY_ABSORPTION))));
                    if(ddmg>=1){
                        dmg = Math.max(dmg,1);
                    }
                }
            }
            processFriarReasonLoss(dmg, src, unavoidable);
        }

		if (!unavoidable && buff( Paralysis.class ) != null && !(src instanceof Hero && hero.belongings.attackingWeapon() instanceof Shuriken_Box.SmallShuriken)) {
			buff( Paralysis.class ).processDamage(dmg);
		}

		int shielded = dmg;
		BrokenSeal.WarriorShield warriorShield = buff(BrokenSeal.WarriorShield.class);
		int warriorShieldBefore = warriorShield == null ? 0 : warriorShield.shielding();
		//FIXME: when I add proper damage properties, should add an IGNORES_SHIELDS property to use here.
		if (!unavoidable && !(src instanceof Hunger)){
			for (ShieldBuff s : buffs(ShieldBuff.class)){
				dmg = s.absorbDamage(dmg);
				if (dmg == 0) break;
			}
		}
		int absorbed = shielded - dmg;
		if (this instanceof Hero && absorbed > 0) {
			Hero shieldedHero = (Hero) this;
			int sealAbsorbed = warriorShield == null ? 0
					: Math.max(0, warriorShieldBefore - warriorShield.shielding());
			if (sealAbsorbed > 0 && shieldedHero.subClass.is(HeroSubClass.BERSERKER)) {
				Buff.affect(shieldedHero, Berserk.class).addRage(sealAbsorbed * 0.05f);
			}
			if (src instanceof Char
					&& ((Char) src).alignment == Alignment.ENEMY
					&& shieldedHero.hasTalent(Talent.MIRRORED_REVENGE)) {
				int reflected = Math.round(absorbed
						* (0.5f + 0.5f * shieldedHero.pointsInTalent(Talent.MIRRORED_REVENGE)));
				if (reflected > 0) {
					((Char) src).damage(reflected, Talent.MIRRORED_REVENGE,
							DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
				}
			}
		}
		if(hero!=null && hero.hasTalent(Talent.STATIC_LIGHT) && (src instanceof Wand || src instanceof WandOfWarding.Ward) && dmg>0){
			int lightturns = (int)(dmg*(0.025f+0.025f*hero.pointsInTalent(Talent.STATIC_LIGHT)));
			if(lightturns>0){
				Buff.affect(this, Paralysis.class,lightturns);
			}

		}
		dmg = Math.max(0, modifyFinalDamage(dmg, src, damageTags));
		shielded = absorbed;
		HP -= dmg;

		if (HP > 0 && shielded > 0 && shielding() == 0){
			if (this instanceof Hero && ((Hero) this).hasTalent(Talent.PROVOKED_ANGER)){
				Buff.affect(this, Talent.ProvokedAngerTracker.class, 5f);
			}
		}

		if (HP > 0 && buff(Grim.GrimTracker.class) != null){

			float finalChance = buff(Grim.GrimTracker.class).maxChance;
			finalChance *= (float)Math.pow( ((HT - HP) / (float)HT), 2);

			if (Random.Float() < finalChance) {
				int extraDmg = Math.round(HP*resist(Grim.class));
				dmg += extraDmg;
				HP -= extraDmg;

				sprite.emitter().burst( ShadowParticle.UP, 5 );
				if (!isAlive() && buff(Grim.GrimTracker.class).qualifiesForBadge){
					Badges.validateGrimWeapon();
				}
			}
		}

		if (HP < 0 && src instanceof Char && alignment == Alignment.ENEMY){
			if (((Char) src).buff(Kinetic.KineticTracker.class) != null){
				int dmgToAdd = -HP;
				dmgToAdd -= ((Char) src).buff(Kinetic.KineticTracker.class).conservedDamage;
				dmgToAdd = Math.round(dmgToAdd * Weapon.Enchantment.genericProcChanceMultiplier((Char) src));
				if (dmgToAdd > 0) {
					Buff.affect((Char) src, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
				}
				((Char) src).buff(Kinetic.KineticTracker.class).detach();
			}
		}

		if (sprite != null) {
			int icon = DamageIconResolver.resolve(tags);

			if ((icon == FloatingText.PHYS_DMG || icon == FloatingText.PHYS_DMG_NO_BLOCK) && hitMissIcon != -1){
				if (icon == FloatingText.PHYS_DMG_NO_BLOCK) hitMissIcon += 18; //extra row
				icon = hitMissIcon;
			}
			hitMissIcon = -1;
			if(dmg+shielded>=0){
				sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
			}

		}

		if (HP < 0) HP = 0;

		if (!isAlive()) {
			die( src );
		} else if (HP == 0 && buff(DeathMark.DeathMarkTracker.class) != null){
			DeathMark.processFearTheReaper(this);
		}
	}

	public void damage(int dmg, Object src) {
		damage(dmg, src, legacyDamageTags(src));
	}

	/** Last damage hook after mitigation and shield absorption, before HP is changed. */
	protected int modifyFinalDamage(int damage, Object source, DamageTag... damageTags) {
		return damage;
	}

	//these are misc. sources of physical damage which do not apply armor, they get a different icon
	private static HashSet<Class> NO_ARMOR_PHYSICAL_SOURCES = new HashSet<>();
	{
		NO_ARMOR_PHYSICAL_SOURCES.add(CrystalSpire.SpireSpike.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.Boulder.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.GnollRockFall.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollRockfallTrap.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.KingDamager.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.Summoning.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(LifeLink.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Chasm.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(WandOfBlastWave.Knockback.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Heap.class); //damage from wraiths attempting to spawn from heaps
		NO_ARMOR_PHYSICAL_SOURCES.add(Necromancer.SummoningBlockDamage.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DriedRose.GhostHero.NoRoseDamage.class);
        NO_ARMOR_PHYSICAL_SOURCES.add(Talent.FallingStoneBuff.class);
        NO_ARMOR_PHYSICAL_SOURCES.add(DM300.FallingRockBuff.class);
        NO_ARMOR_PHYSICAL_SOURCES.add(Blade_Star.class);
        NO_ARMOR_PHYSICAL_SOURCES.add(Blade_Star.Blade_Stair.class);
        NO_ARMOR_PHYSICAL_SOURCES.add(Shuriken_Box.SmallShuriken.class);
	}
	
	public void destroy() {
		HP = 0;
		Actor.remove( this );

		for (Char ch : Actor.chars().toArray(new Char[0])){
			if (ch.buff(Charm.class) != null && ch.buff(Charm.class).object == id()){
				ch.buff(Charm.class).detach();
			}
			if (ch.buff(Dread.class) != null && ch.buff(Dread.class).object == id()){
				ch.buff(Dread.class).detach();
			}
			if (ch.buff(Terror.class) != null && ch.buff(Terror.class).object == id()){
				ch.buff(Terror.class).detach();
			}
			if (ch.buff(SnipersMark.class) != null && ch.buff(SnipersMark.class).object == id()){
				ch.buff(SnipersMark.class).detach();
			}
			if (ch.buff(Talent.FollowupStrikeTracker.class) != null
					&& ch.buff(Talent.FollowupStrikeTracker.class).object == id()){
				ch.buff(Talent.FollowupStrikeTracker.class).detach();
			}
			if (ch.buff(Talent.DeadlyFollowupTracker.class) != null
					&& ch.buff(Talent.DeadlyFollowupTracker.class).object == id()){
				ch.buff(Talent.DeadlyFollowupTracker.class).detach();
			}
		}
	}

	/** Receives notification only after a character has reached its final death path. */
	public interface DeathListener {
		void onCharDied(Char deceased);
	}

	private void notifyDeathListeners() {
		// A second die() call on an already removed character must not create another soul.
		if (Actor.findById(id()) != this) {
			return;
		}
		for (Char character : Actor.chars().toArray(new Char[0])) {
			if (character != this && character instanceof DeathListener) {
				((DeathListener) character).onCharDied(this);
			}
		}
	}
	
	public void die( Object src ) {
		notifyDeathListeners();
		destroy();
		if (src != Chasm.class && sprite!=null) {
			sprite.die();
			if (!flying && Dungeon.level != null && sprite instanceof MobSprite && Dungeon.level.map[pos] == Terrain.CHASM){
				((MobSprite) sprite).fall();
			}
		}
	}

	//we cache this info to prevent having to call buff(...) in isAlive.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications
	public boolean deathMarked = false;
	
	public boolean isAlive() {
		return HP > 0 || deathMarked;
	}

	public boolean isActive() {
		return isAlive();
	}

	@Override
	protected void spendConstant(float time) {
		TimekeepersHourglass.timeFreeze freeze = buff(TimekeepersHourglass.timeFreeze.class);
		if (freeze != null) {
			if (ChronoSuccubus.tryBreakTimeStop(this, freeze.remainingTurns())) {
				freeze.detach();
				return;
			}
			freeze.processTime(time);
			return;
		}

		Swiftthistle.TimeBubble bubble = buff(Swiftthistle.TimeBubble.class);
		if (bubble != null){
			if (ChronoSuccubus.tryBreakTimeStop(this, bubble.remainingTurns())) {
				bubble.detach();
				return;
			}
			bubble.processTime(time);
			return;

		}

		super.spendConstant(time);
	}

	@Override
	protected void spend( float time ) {

		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
			//slowed and chilled do not stack
		} else if (buff( Chill.class ) != null) {
			timeScale *= buff( Chill.class ).speedFactor();
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spend( time / timeScale );
	}
	
	public synchronized LinkedHashSet<Buff> buffs() {
		return new LinkedHashSet<>(buffs);
	}
	
	@SuppressWarnings("unchecked")
	//returns all buffs assignable from the given buff class
	public synchronized <T extends Buff> HashSet<T> buffs( Class<T> c ) {
		HashSet<T> filtered = new HashSet<>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}

	@SuppressWarnings("unchecked")
	//returns an instance of the specific buff class, if it exists. Not just assignable
	public synchronized  <T extends Buff> T buff( Class<T> c ) {
		for (Buff b : buffs) {
			if (b.getClass() == c) {
				return (T)b;
			}
		}
		return null;
	}

	public synchronized boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean add( Buff buff ) {

		if (buff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof AllyBuff)
					&& !(buff instanceof LostInventory)){
				return false;
			}
		}

		if (sprite != null && buff(Challenge.SpectatorFreeze.class) != null){
			return false; //can't add buffs while frozen and game is loaded
		}

		buffs.add( buff );
		if (Actor.chars().contains(this)) Actor.add( buff );

		if (sprite != null && buff.announced) {
			switch (buff.type) {
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
					break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
					break;
				case NEUTRAL:
				default:
					sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
					break;
			}
		}

		return true;

	}
	
	public synchronized boolean remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );

		return true;
	}
	
	public synchronized void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	@Override
	protected synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}
	
	public synchronized void updateSpriteState() {

		if (sprite != null) {
            for (Buff buff:buffs) {
                buff.fx( true );
            }
			sprite.remove(CharSprite.State.ENEMY_RED);
			sprite.remove(CharSprite.State.ENEMY_YELLOW);
			sprite.remove(CharSprite.State.ENEMY_BLUE);
			sprite.remove(CharSprite.State.ENEMY_GREEN);
			switch (alignment) {
				case ENEMY1:
					sprite.add(CharSprite.State.ENEMY_RED);
					break;
				case ENEMY2:
					sprite.add(CharSprite.State.ENEMY_YELLOW);
					break;
				case ENEMY3:
					sprite.add(CharSprite.State.ENEMY_BLUE);
					break;
				case ENEMY4:
					sprite.add(CharSprite.State.ENEMY_GREEN);
					break;
			}
		}
	}
	
	public float stealth() {
		float stealth = 0;

		stealth += Obfuscation.stealthBoost(this, glyphLevel(Obfuscation.class));
		if(this instanceof Hero && ((Hero)this).heroClass==HeroClass.NINJA){
			stealth += 2;
		}
		float bonusDis=0;
		for (Char ch : Actor.chars()){
			if(ch instanceof AuxiliaryDrone.EscortDrone){
				bonusDis+=0.8f;
			}else if(ch instanceof AuxiliaryDrone){
				bonusDis+=0.3f;
			}
		}
		stealth+=(int)bonusDis;
        //GLog.i(String.valueOf(stealth));
		return stealth;
	}

	public final void move( int step ) {
		move( step, true );
	}

	//travelling may be false when a character is moving instantaneously, such as via teleportation
	public void move( int step, boolean travelling ) {

		if (travelling && Dungeon.level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			sprite.interruptMotion();
			int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
					|| (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[newPos])
					|| Actor.findChar( newPos ) != null)
				return;
			else {
				sprite.move(pos, newPos);
				step = newPos;
			}
		}

		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos );
		}

		pos = step;
		
		if (this != hero) {
			sprite.visible = Dungeon.level.heroFOV[pos];
		}
		
		Dungeon.level.occupyCell(this );
	}
	
	public int distance( Char other ) {
		return Dungeon.level.distance( pos, other.pos );
	}

	public boolean[] modifyPassable( boolean[] passable){
		//do nothing by default, but some chars can pass over terrain that others can't
		return passable;
	}
	
	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	protected final HashSet<Class> resistances = new HashSet<>();
	
	//returns percent effectiveness after resistances
	//TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
	public float resist( Class effect ){
		HashSet<Class> resists = new HashSet<>(resistances);
		for (Property p : properties()){
			resists.addAll(p.resistances());
		}
		for (Buff b : buffs()){
			resists.addAll(b.resistances());
		}
		
		float result = 1f;
		for (Class c : resists){
			if (c.isAssignableFrom(effect)){
				result *= 0.5f;
			}
		}
		return result * RingOfElements.resist(this, effect);
	}
	
	protected final HashSet<Class> immunities = new HashSet<>();
	
	public boolean isImmune(Class effect ){
		HashSet<Class> immunes = new HashSet<>(immunities);
		for (Property p : properties()){
			immunes.addAll(p.immunities());
		}
		for (Buff b : buffs()){
			immunes.addAll(b.immunities());
		}
		if (glyphLevel(Brimstone.class) >= 0){
			immunes.add(Burning.class);
		}

		for (Class c : immunes){
			if (c.isAssignableFrom(effect)){
				return true;
			}
		}
		return false;
	}

	//similar to isImmune, but only factors in damage.
	//Is used in AI decision-making
	public boolean isInvulnerable( Class effect ){
		return buff(Challenge.SpectatorFreeze.class) != null || buff(Invulnerability.class) != null || buff(ImmortalShieldAffecter.ImmortalShield.class) != null;
	}

	public boolean blocksBallistica() {
		return true;
	}

	protected HashSet<Property> properties = new HashSet<>();

	public void addProperties(Property p){
		properties.add(p);
	}

	public HashSet<Property> properties() {
		HashSet<Property> props = new HashSet<>(properties);
		//TODO any more of these and we should make it a property of the buff, like with resistances/immunities
		if (buff(ChampionEnemy.Giant.class) != null) {
			props.add(Property.LARGE);
		}
		if(buff(ChampionEnemy.Corrosion.class) != null){
			props.add(Property.ACIDIC);
		}
		if(buff(ChampionEnemy.RandomMiniBoss.class) != null){
			props.add(Property.MINIBOSS);
		}

		return props;
	}

	public enum Property{
		BOSS ( new HashSet<Class>( Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class)),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		MINIBOSS ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
        UNSLEEP(new HashSet<Class>(Arrays.asList(Sleep.class,MagicalSleep.class, Drowsy.class)),
                new HashSet<Class>( Arrays.asList(Sleep.class,MagicalSleep.class, Drowsy.class) )),
		BOSS_MINION,
		UNDEAD,
		DEMONIC,
		INORGANIC ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class) )),
		FIERY ( new HashSet<Class>( Arrays.asList(WandOfFireblast.class, Elemental.FireElemental.class)),
				new HashSet<Class>( Arrays.asList(Burning.class, Blazing.class))),
		ICY ( new HashSet<Class>( Arrays.asList(WandOfFrost.class, Elemental.FrostElemental.class)),
				new HashSet<Class>( Arrays.asList(Frost.class, Chill.class))),
		ACIDIC ( new HashSet<Class>( Arrays.asList(Corrosion.class)),
				new HashSet<Class>( Arrays.asList(Ooze.class))),
		ELECTRIC ( new HashSet<Class>( Arrays.asList(WandOfLightning.class, Shocking.class, Potential.class,
										Electricity.class, ShockingDart.class, Elemental.ShockElemental.class )),
				new HashSet<Class>()),
		LARGE,
		IMMOVABLE ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Vertigo.class) )),
		//A character that acts in an unchanging manner. immune to AI state debuffs or stuns/slows
		STATIC( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class, Terror.class, Amok.class, Charm.class, Sleep.class,
									Paralysis.class, Frost.class, Chill.class, Slow.class, Speed.class) )),
		EVIL(new HashSet<Class>(Arrays.asList(StoneOfAggression.Aggression.class, Amok.class, Shocking.class,
				Electricity.class, ShockingDart.class,Grim.class,Bleeding.class) ),
				new HashSet<Class>( Arrays.asList( ToxicGas.class, ParalyticGas.class, CorrosiveGas.class) )),

		GOLD(new HashSet<Class>(Arrays.asList(StoneOfAggression.Aggression.class, Amok.class,Shocking.class,
				ShockingDart.class,Grim.class,Bleeding.class)),
				AntiMagic.RESISTS),
		//史莱姆
		DARKSLIME(new HashSet<Class>(),new HashSet<Class>( Arrays.asList(Ooze.class)));

		private HashSet<Class> resistances;
		private HashSet<Class> immunities;
		
		Property(){
			this(new HashSet<Class>(), new HashSet<Class>());
		}
		
		Property( HashSet<Class> resistances, HashSet<Class> immunities){
			this.resistances = resistances;
			this.immunities = immunities;
		}
		
		public HashSet<Class> resistances(){
			return new HashSet<>(resistances);
		}
		
		public HashSet<Class> immunities(){
			return new HashSet<>(immunities);
		}

	}

	public static boolean hasProp( Char ch, Property p){
		return (ch != null && ch.properties().contains(p));
	}
}
