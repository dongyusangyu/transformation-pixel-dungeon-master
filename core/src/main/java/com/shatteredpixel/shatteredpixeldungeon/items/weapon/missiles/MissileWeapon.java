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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightEquipmentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.AgentMinDatasetRecorder;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Explosive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.TwoHandedGreatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

abstract public class MissileWeapon extends Weapon {

	{
		stackable = true;
		quantity = defaultQuantity();
		
		bones = true;

		defaultAction = AC_THROW;
		usesTargeting = true;
	}

	public static final long UNASSIGNED_SET_ID = Long.MIN_VALUE;
	private static final SecureRandom SET_ID_RANDOM = new SecureRandom();

	public long setID = newSetID();

	/*
	 * The bow's shared enchantment is resolved once for each physical throw.
	 * It is deliberately transient: it describes an attack in progress, not
	 * an item property that should be saved or shown in inventory information.
	 */
	private SharedEnchantmentSnapshot sharedEnchantmentSnapshot;

	static final class SharedEnchantmentSnapshot {
		private final Weapon.Enchantment enchantment;

		private SharedEnchantmentSnapshot(Weapon.Enchantment enchantment) {
			this.enchantment = enchantment;
		}

		boolean matches(Class<? extends Weapon.Enchantment> type) {
			return enchantment != null && enchantment.getClass() == type;
		}
	}

	static boolean sharedEnchantmentRollSucceeds(int talentPoints, int roll) {
		return talentPoints > 0 && roll >= 0 && roll < 3 && roll < talentPoints;
	}

	static SharedEnchantmentSnapshot createSharedEnchantmentSnapshot(
			Weapon.Enchantment enchantment, int talentPoints, int roll) {
		return enchantment != null && sharedEnchantmentRollSucceeds(talentPoints, roll)
				? new SharedEnchantmentSnapshot(enchantment) : null;
	}

	private void prepareSharedEnchantment(Hero user) {
		sharedEnchantmentSnapshot = null;
		if (user == null || user != Dungeon.hero || this instanceof SpiritBow.SpiritArrow
				|| user.buff(MagicImmune.class) != null
				|| HungerKnightEquipmentSeal.isActive(user)) {
			return;
		}

		SpiritBow bow = user.belongings.getItem(SpiritBow.class);
		int talentPoints = user.pointsInTalent(Talent.SHARED_ENCHANTMENT);
		if (bow != null && bow.enchantment != null && talentPoints > 0) {
			sharedEnchantmentSnapshot = createSharedEnchantmentSnapshot(
					bow.enchantment, talentPoints, Random.Int(3));
		}
	}

	private void clearSharedEnchantment() {
		sharedEnchantmentSnapshot = null;
	}

	private boolean hasSharedEnchantment(Class<? extends Weapon.Enchantment> type,
			Char owner) {
		return owner == Dungeon.hero && owner != null
				&& owner.buff(MagicImmune.class) == null
				&& !HungerKnightEquipmentSeal.isActive(owner)
				&& sharedEnchantmentSnapshot != null
				&& sharedEnchantmentSnapshot.matches(type);
	}

	//whether or not this instance of the item exists purely to trigger its effect. i.e. no dropping
	public boolean spawnedForEffect = false;
	//A phantom projectile repeats the attack pipeline without recursively spawning another phantom.
	protected boolean phantomProjectile = false;

	public enum QianfaVolleyMode {
		STACK,
		REPEAT_THREE
	}

	public interface QianfaRepeatProjectile {
	}

	public QianfaVolleyMode qianfaVolleyMode() {
		return this instanceof QianfaRepeatProjectile
				? QianfaVolleyMode.REPEAT_THREE
				: QianfaVolleyMode.STACK;
	}

	public static int qianfaVolleyCount(MissileWeapon weapon, int talentPoints,
			int roll, boolean hasCharTarget) {
		if (weapon == null || !hasCharTarget || talentPoints <= 0 || roll >= talentPoints) {
			return 1;
		}
		if (weapon.qianfaVolleyMode() == QianfaVolleyMode.REPEAT_THREE) {
			return 3;
		}
		return Math.max(1, weapon.quantity());
	}

	public static int phantomShooterChance(int talentPoints) {
		return Math.min(3, Math.max(0, talentPoints)) * 10;
	}

	public static boolean shouldTriggerPhantomShooter(int talentPoints, int roll) {
		return roll >= 0 && roll < 10
				&& roll < phantomShooterChance(talentPoints) / 10;
	}

	protected final MissileWeapon markAsPhantom(MissileWeapon projectile) {
		if (projectile != null) {
			projectile.phantomProjectile = true;
			projectile.spawnedForEffect = true;
			projectile.parent = null;
		}
		return projectile;
	}

	protected MissileWeapon createPhantomProjectile() {
		Item item = duplicate();
		if (!(item instanceof MissileWeapon)) {
			return null;
		}
		item.quantity(0);
		return markAsPhantom((MissileWeapon) item);
	}

	protected boolean sticky = true;
	
	public static final float MAX_DURABILITY = 100;
	protected float durability = MAX_DURABILITY;
	protected float baseUses = 10;
	
	public boolean holster;
	
	//used to reduce durability from the source weapon stack, rather than the one being thrown.
	public MissileWeapon parent;
	
	public int tier;

	protected int usesToID(){
		return 10; //half of a melee weapon
	}

	@Override
	public int min() {
		if (hero != null){
			int ringBonus = benefitsFromSharpshooting()
					? RingOfSharpshooting.levelDamageBonus(hero) : 0;
			return Math.max(0, min(buffedLvl() + ringBonus));
		} else {
			return Math.max(0 , min( buffedLvl() ));
		}
	}
	
	@Override
	public int min(int lvl) {
		return  2 * tier +                      //base
				(tier == 1 ? lvl : 2*lvl);      //level scaling
	}
	
	@Override
	public int max() {
		if (hero != null){
			int ringBonus = benefitsFromSharpshooting()
					? RingOfSharpshooting.levelDamageBonus(hero) : 0;
			return Math.max(0, max(buffedLvl() + ringBonus));
		} else {
			return Math.max(0 , max( buffedLvl() ));
		}
	}

	public boolean benefitsFromSharpshooting() {
		return true;
	}
	
	@Override
	public int max(int lvl) {
		return  5 * tier +                      //base
				tier*lvl;                       //level scaling
	}
	
	public int STRReq(int lvl){
		int req = STRReq(tier, lvl) - 1; //1 less str than normal for their tier
		if (masteryPotionBonus){
			req -= 2;
		}
		return req;
	}

	//use the parent item if this has been thrown from a parent
	public int buffedLvl(){

		if (parent != null) {
			if(hero!=null && hero.hasTalent(Talent.LIGHT_BOX) && hero.buff(Talent.LightBox.class)!=null){
				return parent.buffedLvl()+hero.pointsInTalent(Talent.LIGHT_BOX);
			}
			return parent.buffedLvl();
		} else {
			if(hero!=null && hero.hasTalent(Talent.LIGHT_BOX) && hero.buff(Talent.LightBox.class)!=null){
				return super.buffedLvl()+hero.pointsInTalent(Talent.LIGHT_BOX);
			}
			return super.buffedLvl();
		}
	}

	public Item upgrade( boolean enchant ) {
		if (!bundleRestoring) {
			ensureSetIDAssigned();
			durability = MAX_DURABILITY;
			extraThrownLeft = false;
			quantity = defaultQuantity();
			UpgradedSetTracker.setCanonicalLevel(Dungeon.hero, this, trueLevel()+1);
		}
		//thrown weapons don't get curse weakened
		boolean wasCursed = cursed;
		super.upgrade( enchant );
		if (wasCursed && hasCurseEnchant()){
			cursed = wasCursed;
		}
		sanitizeAfterUpgrade();
		return this;
	}

	@Override
	//FIXME some logic here assumes the items are in the player's inventory. Might need to adjust
	public Item upgrade() {
		if (!bundleRestoring) {
			ensureSetIDAssigned();
			durability = MAX_DURABILITY;
			extraThrownLeft = false;
			quantity = defaultQuantity();
			UpgradedSetTracker.setCanonicalLevel(Dungeon.hero, this, trueLevel()+1);
		}
		return super.upgrade();
	}

	@Override
	public void recordUpgradeScrollUse() {
		if (Dungeon.hero != null && hasAssignedSetID()) {
			UpgradedSetTracker.recordUpgradeScrollUse(Dungeon.hero, this);
		} else {
			super.recordUpgradeScrollUse();
		}
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_EQUIP );
		return actions;
	}
	
	@Override
	public boolean collect(Bag container) {
		if (container != null && container.owner instanceof Hero){
			ensureSetIDAssigned();
			if (!UpgradedSetTracker.pickupValid((Hero) container.owner, this)) {
				logDust(this);
				quantity(0);
				return true;
			}
		}
		if (container instanceof MagicalHolster) holster = true;
		boolean collected = super.collect(container);
		if (collected && container != null && container.owner instanceof Hero) {
			sanitizeInventorySets((Hero) container.owner, this);
		}
		return collected;
	}

	@Override
	public boolean isSimilar( Item item ) {
		if (!hasSameExtractionRaidOrigin(item) || !(item instanceof MissileWeapon)) return false;
		MissileWeapon other = (MissileWeapon) item;
		if (!usesIndependentSetID() || !other.usesIndependentSetID()){
			return getClass() == other.getClass() && trueLevel() == other.trueLevel();
		}

		boolean thisAssigned = hasAssignedSetID();
		boolean otherAssigned = other.hasAssignedSetID();
		return thisAssigned && otherAssigned && setID == other.setID;
	}

	@Override
	protected void onCollectedByMerge(Item destination, int sourceQuantityBefore,
			int destinationQuantityBefore) {
		if (!bundleRestoring && collectionLoss(sourceQuantityBefore, destinationQuantityBefore,
				quantity, destination.quantity()) > 0) {
			logDuplicateDust(this);
		}
	}

	static int collectionLoss(int sourceQuantityBefore, int destinationQuantityBefore,
			int sourceQuantityAfter, int destinationQuantityAfter) {
		return Math.max(0, sourceQuantityBefore + destinationQuantityBefore
				- sourceQuantityAfter - destinationQuantityAfter);
	}
	
	@Override
	public int throwPos(Hero user, int dst) {

		int projecting = 0;
		if (hasEnchant(Projecting.class, user)){
			projecting += 4;
		}
		if(user.pointsInTalent(Talent.ARROW_PENETRATION)==2 && (Dungeon.level.passable[dst] || Dungeon.level.avoid[dst] || Actor.findChar(dst) != null)
				&& Dungeon.level.distance(user.pos, dst) <=12){
			return dst;
		}else if(user.pointsInTalent(Talent.ARROW_PENETRATION)==1 && (Dungeon.level.passable[dst] || Dungeon.level.avoid[dst] || Actor.findChar(dst) != null)
				&& Dungeon.level.distance(user.pos, dst) <=8){
			return dst;
		}

		if (projecting > 0
				&& (Dungeon.level.passable[dst] || Dungeon.level.avoid[dst] || Actor.findChar(dst) != null)
				&& Dungeon.level.distance(user.pos, dst) <= Math.round(projecting * Enchantment.genericProcChanceMultiplier(user))){
			return dst;
		} else {
			return super.throwPos(user, dst);
		}
	}

	@Override
	public void cast(final Hero user, final int dst) {
		clearSharedEnchantment();
		prepareSharedEnchantment(user);
		final int cell = throwPos(user, dst);
		final Char enemy = Actor.findChar(cell);
		final boolean hasCharTarget = enemy != null && enemy != user;
		final int talentPoints = user.pointsInTalent(Talent.QIANFA_THROWING);
		final int roll = talentPoints > 0 && hasCharTarget ? Random.Int(10) : 10;
		final int volleyCount = qianfaVolleyCount(this, talentPoints, roll, hasCharTarget);

		if (volleyCount <= 1) {
			super.cast(user, dst);
			return;
		}

		AgentMinDatasetRecorder.onItemCast(this, user, dst);
		QuickSlotButton.target(enemy);
		float delay = castDelay(user, cell) + TwoHandedGreatsword.extraActionDelay(user);
		castQianfaVolley(user, enemy, cell, volleyCount, delay);
	}

	private void castQianfaVolley(Hero user, Char enemy, int firstCell,
			int volleyCount, float delay) {
		user.busy();
		new QianfaVolley(user, enemy, firstCell, volleyCount, delay).launchNext();
	}

	private final class QianfaVolley {

		private final Hero user;
		private final Char enemy;
		private final int firstCell;
		private final float delay;
		private int remaining;
		private boolean firstShot = true;
		private Actor continuationActor;

		private QianfaVolley(Hero user, Char enemy, int firstCell,
				int volleyCount, float delay) {
			this.user = user;
			this.enemy = enemy;
			this.firstCell = firstCell;
			this.remaining = volleyCount;
			this.delay = delay;
		}

		private void launchNext() {
			if (remaining <= 0 || enemy == null || !enemy.isAlive()) {
				MissileWeapon.this.clearSharedEnchantment();
				finishQianfaVolley(user, delay);
				releaseContinuationActor();
				return;
			}

			if (!firstShot) {
				MissileWeapon.this.prepareSharedEnchantment(user);
			}
			final int shotCell = firstShot ? firstCell : throwPos(user, enemy.pos);
			firstShot = false;
			if (Actor.findChar(shotCell) != enemy) {
				MissileWeapon.this.clearSharedEnchantment();
				finishQianfaVolley(user, delay);
				releaseContinuationActor();
				return;
			}

			user.sprite.zap(shotCell);
			throwSound();
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).reset(
					user.sprite, enemy.sprite, MissileWeapon.this, new Callback() {
						@Override
						public void call() {
							curUser = user;
							MissileWeapon projectile = qianfaVolleyMode() == QianfaVolleyMode.STACK
									? detachVolleyProjectile(user)
									: MissileWeapon.this;
							if (projectile != null) {
								projectile.onThrow(shotCell);
								MissileWeapon.this.clearSharedEnchantment();
								remaining--;
							} else {
								MissileWeapon.this.clearSharedEnchantment();
								remaining = 0;
							}

							Actor actorToRelease = continuationActor;
							continuationActor = null;
							if (remaining > 0 && enemy.isAlive()) {
								scheduleNext();
							} else {
								finishQianfaVolley(user, delay);
							}
							if (actorToRelease != null) actorToRelease.next();
						}
					});
		}

		private MissileWeapon detachVolleyProjectile(Hero user) {
			Item detached = MissileWeapon.this.detach(user.belongings.backpack);
			return detached instanceof MissileWeapon ? (MissileWeapon) detached : null;
		}

		private void scheduleNext() {
			Actor.add(new Actor() {
				{
					actPriority = VFX_PRIO - 1;
				}

				@Override
				protected boolean act() {
					continuationActor = this;
					launchNext();
					Actor.remove(this);
					return false;
				}
			});
			user.next();
		}

		private void releaseContinuationActor() {
			if (continuationActor != null) {
				Actor actor = continuationActor;
				continuationActor = null;
				actor.next();
			}
		}
	}

	private void finishQianfaVolley(Hero user, float delay) {
		if (user.buff(Talent.LethalMomentumTracker.class) != null) {
			user.buff(Talent.LethalMomentumTracker.class).detach();
			user.next();
		} else {
			user.spendAndNext(delay);
		}
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float accFactor = super.accuracyFactor(owner, target);

		accFactor *= adjacentAccFactor(owner, target);

		return accFactor;
	}

	protected float adjacentAccFactor(Char owner, Char target){
		if (Dungeon.level.adjacent( owner.pos, target.pos )) {
			if (owner instanceof Hero){
				return (0.5f + 0.25f*((Hero) owner).pointsInTalent(Talent.POINT_BLANK));
			} else {
				return 0.5f;
			}
		} else {
			return 1.5f;
		}
	}

	@Override
	public void doThrow(Hero hero) {
		parent = null; //reset parent before throwing, just in case
		if (((levelKnown && level() > 0) || hasGoodEnchant() || masteryPotionBonus || enchantHardened)
				&& !extraThrownLeft && quantity() == 1 && durabilityLeft() <= durabilityPerUse()){
			GameScene.show(new WndOptions(new ItemSprite(this), Messages.titleCase(title()),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_desc"),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_yes"),
					Messages.get(MissileWeapon.class, "break_upgraded_warn_no")){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						MissileWeapon.super.doThrow(hero);
					} else {
						QuickSlotButton.cancel();
						InventoryPane.cancelTargeting();
					}
				}

				@Override
				public void onBackPressed() {
					super.onBackPressed();
					QuickSlotButton.cancel();
					InventoryPane.cancelTargeting();
				}
			});

		} else {
			super.doThrow(hero);
		}
	}

	@Override
	protected void onThrow( int cell ) {
		MissileWeapon source = parent;
		try {
		Char enemy = Actor.findChar( cell );
		if (enemy == null || enemy == curUser) {
			parent = null;

			triggerSeerShot(cell);

			if (!spawnedForEffect) super.onThrow( cell );
		} else {
			if (!curUser.shoot( enemy, this )) {
				rangedMiss( cell );
			} else {
				
				rangedHit( enemy, cell );
				onSuccessfulThrow(enemy);

			}
		}
		} finally {
			clearSharedEnchantment();
			if (source != null) source.clearSharedEnchantment();
		}
	}

	protected void onSuccessfulThrow(final Char enemy) {
		final Hero user = curUser;
		if (phantomProjectile || user == null || user != Dungeon.hero
				|| enemy == null || !enemy.isAlive()) {
			return;
		}

		int talentPoints = user.pointsInTalent(Talent.PHANTOM_SHOOTER);
		if (talentPoints <= 0
				|| !shouldTriggerPhantomShooter(talentPoints, Random.Int(10))) {
			return;
		}

		MissileWeapon phantom = createPhantomProjectile();
		if (phantom == null) {
			return;
		}
		schedulePhantomThrow(user, enemy, phantom);
	}

	private void schedulePhantomThrow(final Hero user, final Char enemy,
			final MissileWeapon phantom) {
		if (userSpriteUnavailable(user, enemy)) {
			curUser = user;
			phantom.prepareSharedEnchantment(user);
			phantom.onThrow(enemy.pos);
			return;
		}

		Actor.add(new Actor() {
			{
				actPriority = VFX_PRIO;
			}

			@Override
			protected boolean act() {
				if (!enemy.isAlive() || Actor.findChar(enemy.pos) != enemy) {
					Actor.remove(this);
					return true;
				}
				if (userSpriteUnavailable(user, enemy)) {
					curUser = user;
					phantom.prepareSharedEnchantment(user);
					phantom.onThrow(enemy.pos);
					Actor.remove(this);
					return true;
				}

				final Actor continuation = this;
				phantom.throwSound();
				((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).reset(
						user.sprite, enemy.sprite, phantom, new Callback() {
							@Override
							public void call() {
								curUser = user;
								if (enemy.isAlive() && Actor.findChar(enemy.pos) == enemy) {
									phantom.prepareSharedEnchantment(user);
									phantom.onThrow(enemy.pos);
								}
								continuation.next();
							}
						});
				Actor.remove(this);
				return false;
			}
		});
	}

	private boolean userSpriteUnavailable(Hero user, Char enemy) {
		return user.sprite == null || user.sprite.parent == null || enemy.sprite == null;
	}

	protected void triggerSeerShot(int cell) {
		if (curUser.hasTalent(Talent.SEER_SHOT)
				&& curUser.heroClass != HeroClass.HUNTRESS
				&& curUser.buff(Talent.SeerShotCooldown.class) == null
				&& Actor.findChar(cell) == null) {
			RevealedArea a = Buff.affect(curUser, RevealedArea.class, 5 * curUser.pointsInTalent(Talent.SEER_SHOT));
			a.depth = Dungeon.depth;
			a.pos = cell;
			Buff.affect(curUser, Talent.SeerShotCooldown.class, 20f);
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attacker == Dungeon.hero && sharedEnchantmentSnapshot != null
				&& attacker.buff(MagicImmune.class) == null
				&& !HungerKnightEquipmentSeal.isActive(attacker)) {
			damage = sharedEnchantmentSnapshot.enchantment.proc(this, attacker, defender, damage);
		}

		if ((cursed || hasCurseEnchant()) && !cursedKnown){
			GLog.n(Messages.get(this, "curse_discover"));
		}
		cursedKnown = true;
		if (parent != null) parent.cursedKnown = true;

		//instant ID with the right talent
		if (attacker == Dungeon.hero && Dungeon.hero.pointsInTalent(Talent.HUNTING_INTUITION) == 2 && !((this instanceof Shuriken_Box.SmallShuriken)|| (this instanceof Tatteki.Tamaru) || (this instanceof SpiritBow.SpiritArrow))){
			usesLeftToID = Math.min(usesLeftToID, 0);
			availableUsesToID =  Math.max(usesLeftToID, 0);
		}

		int result = super.proc(attacker, defender, damage);

		//handle ID progress over parent/child
		if (parent != null && parent.usesLeftToID > usesLeftToID){
			float diff = parent.usesLeftToID - usesLeftToID;
			parent.usesLeftToID -= diff;
			parent.availableUsesToID -= diff;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					parent.setIDReady();
				} else {
					parent.identify();
				}
			}
		}

		if (!isIdentified() && ShardOfOblivion.passiveIDDisabled()){
			Buff.prolong(curUser, ShardOfOblivion.ThrownUseTracker.class, 50f);
		}
		if(hero.pointsInTalent(Talent.SURPRISE_THROW)>0 && ((Hero) attacker).justMoved){
			result *=1.0f+0.2f* hero.pointsInTalent(Talent.SURPRISE_THROW);
		}

		if(hero.hasTalent(Talent.STRENGTH_GREATEST)){
			result+=hero.pointsInTalent(Talent.STRENGTH_GREATEST);
		}


		return result;
	}

	@Override
	public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
		return hasSharedEnchantment(type, owner) || super.hasEnchant(type, owner);
	}

	@Override
	public Item virtual() {
		Item item = super.virtual();

		((MissileWeapon)item).setID = setID;

		return item;
	}

	public int defaultQuantity(){
		return 3;
	}

	//mainly used to track warnings relating to throwing the last upgraded one, not super accurate
	public boolean extraThrownLeft = false;

	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);
		if (Dungeon.hero != null && Dungeon.hero.randomMode) {
			switch (Random.chances(new float[]{5, 20, 75})) {
				case 0:
					quantity(1);
					break;
				case 1:
					quantity(2);
					break;
				case 2: default:
					quantity(3);
					break;
			}
		}

		//we use a separate RNG here so that variance due to things like parchment scrap
		//does not affect levelgen
		Random.pushGenerator(Random.Long());

			//30% chance to be cursed
			//10% chance to be enchanted
			float effectRoll = Random.Float();
			if (effectRoll < 0.3f * ParchmentScrap.curseChanceMultiplier()) {
				enchant(Enchantment.randomCurse());
				cursed = true;
			} else if (effectRoll >= 1f - (0.1f * ParchmentScrap.enchantChanceMultiplier())){
				enchant();
			}

		Random.popGenerator();

		return this;
	}

	public String status() {
		//show quantity even when it is 1
		return Integer.toString( quantity );
	}
	
	@Override
	public float castDelay(Char user, int cell) {
		if (Actor.findChar(cell) != null && Actor.findChar(cell) != user){
			if (user instanceof Hero && ((Hero) user).justMoved && ((Hero) user).hasTalent(Talent.SURPRISE_THROW))  return 0;
			else                                                    return delayFactor( user );
		} else {
			//忍者技能
			if(user instanceof Hero && hero.buff(Ninja_Energy.Throw_Skill.class)!=null && Dungeon.level.water[user.pos]){
				if(hero.buff(Ninja_Energy.Gas_Storage.class)!=null){
					Ninja_Energy.Gas_Storage gas_storage=hero.buff(Ninja_Energy.Gas_Storage.class);
					for(Blob blob:gas_storage.blobs.values()){
						GameScene.add(Blob.seed(cell,10,blob.getClass()));
					}
					gas_storage.detach();
				}
				Ninja_Energy.Throw_Skill b = hero.buff(Ninja_Energy.Throw_Skill.class);
				b.detach();
				Ninja_Energy.NinjaAbility.Throw_Water(user.pos,cell);
			}
			return super.castDelay(user, cell);
		}
	}

	protected void rangedHit( Char enemy, int cell ){
		decrementDurability();
		if (durability > 0 && !spawnedForEffect){
			//attempt to stick the missile weapon to the enemy, just drop it if we can't.
			if (sticky && enemy != null && enemy.isActive() && enemy.alignment != Char.Alignment.ALLY){
				PinCushion p = Buff.affect(enemy, PinCushion.class);
				if (p.target == enemy){
					p.stick(this);
					return;
				}
			}
			Heap dropped = Dungeon.level.drop( this, cell );
			if (dropped.sprite != null) {
				dropped.sprite.drop();
			}
		}
	}

	protected void rangedMiss( int cell ) {
		parent = null;
		if (!spawnedForEffect) super.onThrow(cell);
	}

	public float durabilityLeft(){
		return durability;
	}

	public void repair( float amount ){
		durability += amount;
		durability = Math.min(durability, MAX_DURABILITY);
	}

	public void damage( float amount ){
		durability -= amount;
		durability = Math.max(durability, 1); //cannot break from doing this
	}

	public final float durabilityPerUse(){
		return durabilityPerUse(level());
	}

	//classes that add steps onto durabilityPerUse can turn rounding off, to do their own rounding after more logic
	protected boolean useRoundingInDurabilityCalc = true;

	public float durabilityPerUse( int level ){
		float usages = baseUses * (float)(Math.pow(2f, level));

		//+50%/75% durability
		if (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.DURABLE_PROJECTILES)){
			usages *= 1f + (1+Dungeon.hero.pointsInTalent(Talent.DURABLE_PROJECTILES))/4f;
		}
		if (holster) {
			usages *= MagicalHolster.HOLSTER_DURABILITY_FACTOR;
		}

		//+50% durability on speed aug, -33% durability on damage aug
		usages /= augment.delayFactor(1f);

		if (Dungeon.hero != null && benefitsFromSharpshooting()) {
			usages *= RingOfSharpshooting.durabilityMultiplier(Dungeon.hero);
		}

		//at 100 uses, items just last forever.
		if (usages >= 100f) return 0;

		if (useRoundingInDurabilityCalc){
			usages = Math.round(usages);
			//add a tiny amount to account for rounding error for calculations like 1/3
			return (MAX_DURABILITY/usages) + 0.001f;
		} else {
			//rounding can be disabled for classes that override durability per use
			return MAX_DURABILITY/usages;
		}
	}
	
	protected void decrementDurability(){
		//Virtual projectiles (for example Phantom Shooter's extra throw) must not
		//consume or break the real weapon stack that they mirror.
		if (spawnedForEffect) return;

		//if this weapon was thrown from a source stack, degrade that stack.
		//unless a weapon is about to break, then break the one being thrown
		if (parent != null){
			if (parent.durability <= parent.durabilityPerUse()){
				durability = 0;
				parent.durability = MAX_DURABILITY;
				parent.extraThrownLeft = false;
				if (parent.durabilityPerUse() < 100f) {
					GLog.n(Messages.get(this, "has_broken"));
				}
			} else {
				parent.durability -= parent.durabilityPerUse();
				if (parent.durability > 0 && parent.durability <= parent.durabilityPerUse()){
					GLog.w(Messages.get(this, "about_to_break"));
				}
			}
			parent = null;
		} else {
			durability -= durabilityPerUse();
			if (durability > 0 && durability <= durabilityPerUse()){
				GLog.w(Messages.get(this, "about_to_break"));
			} else if (durabilityPerUse() < 100f && durability <= 0){
				GLog.n(Messages.get(this, "has_broken"));
			}
		}
	}
	
	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll( owner ));
		
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Hero.heroDamageIntRange( 0, exStr );
			}
			if (owner.buff(Momentum.class) != null && owner.buff(Momentum.class).freerunning()) {
				damage = Math.round(damage * (1f + 0.15f * ((Hero) owner).pointsInTalent(Talent.PROJECTILE_MOMENTUM)));
			}
		}
		
		return damage;
	}

	/**
	 * Rolls missile damage with an explicit effective-level bonus. This avoids
	 * consulting the global hero when another actor is temporarily using a
	 * read-only reference to the hero's missile weapon.
	 */
	public int damageRollWithLevelBonus(Char owner, int levelBonus) {
		int level = buffedLvl() + levelBonus;
		return augment.damageFactor(Random.NormalIntRange(
				Math.max(0, min(level)), Math.max(0, max(level))));
	}
	
	@Override
	public void reset() {
		super.reset();
		durability = MAX_DURABILITY;
	}
	
	@Override
	public Item merge(Item other) {
		super.merge(other);
		if (hasSameExtractionRaidOrigin(other) && isSimilar(other)) {
			extraThrownLeft = false;

			durability += ((MissileWeapon)other).durability;
			durability -= MAX_DURABILITY;
			while (durability <= 0){
				quantity -= 1;
				durability += MAX_DURABILITY;
			}

			if (quantity > defaultQuantity() && hasAssignedSetID()){
				quantity = defaultQuantity();
				durability = MAX_DURABILITY;
			}

			masteryPotionBonus = masteryPotionBonus || ((MissileWeapon) other).masteryPotionBonus;
			levelKnown = levelKnown || other.levelKnown;
			cursedKnown = cursedKnown || other.cursedKnown;
			enchantHardened = enchantHardened || ((MissileWeapon) other).enchantHardened;

			//if other has a curse/enchant status that's a higher priority, copy it. in the following order:
			//curse infused
			if (!curseInfusionBonus && ((MissileWeapon) other).curseInfusionBonus && ((MissileWeapon) other).hasCurseEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				curseInfusionBonus = true;
				cursed = cursed || other.cursed;
			//enchanted
			} else if (!curseInfusionBonus && !hasGoodEnchant() && ((MissileWeapon) other).hasGoodEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				cursed = other.cursed;
			//nothing
			} else if (!curseInfusionBonus && hasCurseEnchant() && !((MissileWeapon) other).hasCurseEnchant()){
				enchantment = ((MissileWeapon) other).enchantment;
				cursed = other.cursed;
			}
			//cursed (no copy as other cannot have a higher priority status)

			//special case for explosive, as it tracks a variable
			if (((MissileWeapon) other).enchantment instanceof Explosive
				&& enchantment instanceof Explosive){
				((Explosive) enchantment).merge((Explosive) ((MissileWeapon) other).enchantment);
			}

			//merge level to the lower level (backpack's level standard)
			if (other.level() < level()){
				level(other.level());
			}
		} else if (hasAssignedSetID() && other instanceof MissileWeapon
				&& ((MissileWeapon) other).hasAssignedSetID()
				&& setID == ((MissileWeapon) other).setID){
			//different class but same setID: merge quantities and use backpack level
			extraThrownLeft = false;
			durability += ((MissileWeapon)other).durability;
			durability -= MAX_DURABILITY;
			while (durability <= 0){
				quantity -= 1;
				durability += MAX_DURABILITY;
			}
			if (quantity > defaultQuantity() && hasAssignedSetID()){
				quantity = defaultQuantity();
				durability = MAX_DURABILITY;
			}
			levelKnown = levelKnown || other.levelKnown;
			cursedKnown = cursedKnown || other.cursedKnown;
			if (other.level() < level()){
				level(other.level());
			}
			enchantHardened = enchantHardened || ((MissileWeapon) other).enchantHardened;
			masteryPotionBonus = masteryPotionBonus || ((MissileWeapon) other).masteryPotionBonus;
		}
		return this;
	}
	
	@Override
	public Item split(int amount) {
		bundleRestoring = true;
		Item split = super.split(amount);
		bundleRestoring = false;

		//unless the thrown weapon will break, split off a max durability item and
		//have it reduce the durability of the main stack. Cleaner to the player this way
		if (split != null){
			MissileWeapon m = (MissileWeapon)split;
			m.sharedEnchantmentSnapshot = sharedEnchantmentSnapshot;
			m.durability = MAX_DURABILITY;
			m.parent = this;
			extraThrownLeft = m.extraThrownLeft = true;
		}
		
		return split;
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		parent = null;
		ensureSetIDAssigned();
		if (!UpgradedSetTracker.pickupValid(hero, this)){
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext(pickupDelay());
			logDust(this);
			quantity(0);
			return true;
		} else {
			extraThrownLeft = false;
			boolean pickedUp = super.doPickUp(hero, pos);
			if (pickedUp){
				sanitizeInventorySets(hero, this);
			}
			return pickedUp;
		}
	}
	
	@Override
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	@Override
	public String info() {

		String info = super.info();

		if (levelKnown) {
			info += "\n\n" + Messages.get(MissileWeapon.class, "stats_known", tier, augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
			if (Dungeon.hero != null) {
				if (STRReq() > Dungeon.hero.STR()) {
					info += " " + Messages.get(Weapon.class, "too_heavy");
				} else if (Dungeon.hero.STR() > STRReq()) {
					info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
				}
			}
		} else {
			info += "\n\n" + Messages.get(MissileWeapon.class, "stats_unknown", tier, min(0), max(0), STRReq(0));
			if (Dungeon.hero != null && STRReq(0) > Dungeon.hero.STR()) {
				info += " " + Messages.get(MissileWeapon.class, "probably_too_heavy");
			}
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
			if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
			info += " " + enchantment.desc();
		} else if (enchantHardened){
			info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
		}

		if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}

		info += "\n\n";
		String statsInfo = Messages.get(this, "stats_desc");
		if (!statsInfo.equals("")) info += statsInfo + " ";
		info += Messages.get(MissileWeapon.class, "distance");

		switch (augment) {
			case SPEED:
				info += " " + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += " " + Messages.get(Weapon.class, "stronger");
				break;
			case MAGIC:
				info += " " + Messages.get(Weapon.class, "magical", weaponTier());
				break;
			case NONE:
		}

		if (levelKnown) {
			if (durabilityPerUse() > 0) {
				info += "\n\n" + Messages.get(this, "uses_left",
						(int) Math.ceil(durability / durabilityPerUse()),
						(int) Math.ceil(MAX_DURABILITY / durabilityPerUse()));
			} else {
				info += "\n\n" + Messages.get(this, "unlimited_uses");
			}
		}  else {
			if (durabilityPerUse(0) > 0) {
				info += "\n\n" + Messages.get(this, "unknown_uses", (int) Math.ceil(MAX_DURABILITY / durabilityPerUse(0)));
			} else {
				info += "\n\n" + Messages.get(this, "unlimited_uses");
			}
		}
		
		
		return info;
	}
	
	@Override
	public int value() {
		int price = 5 * tier * quantity;
		if (hasGoodEnchant()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseEnchant())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	private static final String SET_ID = "set_id";

	private static final String SPAWNED = "spawned";
	private static final String DURABILITY = "durability";
	private static final String EXTRA_LEFT = "extra_left";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SET_ID, setID);
		bundle.put(SPAWNED, spawnedForEffect);
		bundle.put(DURABILITY, durability);
		bundle.put(EXTRA_LEFT, extraThrownLeft);
	}
	
	private static boolean bundleRestoring = false;
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		bundleRestoring = true;
		super.restoreFromBundle(bundle);
		bundleRestoring = false;

		if (bundle.contains(SET_ID)){
			setID = bundle.getLong(SET_ID);
		//pre v3.2.0 logic
		} else {
			if (level() > 0){
				quantity = defaultQuantity();
			}
			if (usesIndependentSetID()){
				setID = UNASSIGNED_SET_ID;
			}
		}

		spawnedForEffect = bundle.getBoolean(SPAWNED);
		durability = bundle.getFloat(DURABILITY);
		extraThrownLeft = bundle.getBoolean(EXTRA_LEFT);
		ensureSetIDAssigned();
	}

	public static class PlaceHolder extends MissileWeapon {

		{
			image = ItemSpriteSheet.MISSILE_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			//yes, even though it uses a dart outline
			return hasSameExtractionRaidOrigin(item)
					&& item instanceof MissileWeapon
					&& !(item instanceof Dart);
		}

		@Override
		public String status() {
			return null;
		}

		@Override
		public String info() {
			return "";
		}
	}

	//also used by liquid metal crafting to track when a set is consumed
	public static class UpgradedSetTracker extends Buff {

		public static final int ACTIVE = 0;
		public static final int MERCHANT_HELD = 1;
		public static final int CONSUMED = 2;

		public HashMap<Long, Integer> levelThresholds = new HashMap<>();
		public HashMap<Long, Integer> upgradeScrollCredits = new HashMap<>();
		private HashMap<Long, Integer> setStates = new HashMap<>();
		private HashSet<Long> unresolvedLegacyCredits = new HashSet<>();
		private boolean legacyCreditLedger;
		private boolean rejectUntrackedLocalCredits;

		public static boolean pickupValid(Hero h, MissileWeapon w){
			if (h == null || w == null) return true;
			UpgradedSetTracker tracker = h.buff(UpgradedSetTracker.class);
			return tracker == null || tracker.synchronizeMember(w);
		}

		public static void markSold(Hero h, MissileWeapon w, boolean buybackable) {
			if (h == null || w == null || !w.hasAssignedSetID()) return;
			UpgradedSetTracker tracker = Buff.affect(h, UpgradedSetTracker.class);
			if (tracker != null) tracker.markSold(w, buybackable);
		}

		public static void restoreFromMerchant(Hero h, MissileWeapon w) {
			if (h == null || w == null || !w.hasAssignedSetID()) return;
			UpgradedSetTracker tracker = h.buff(UpgradedSetTracker.class);
			if (tracker != null) tracker.restoreFromMerchant(w);
		}

		public static int availableUpgradeScrollUses(Hero h, MissileWeapon w) {
			if (h == null || w == null || !w.hasAssignedSetID()) {
				return w == null ? 0 : Math.max(0, w.upgradeScrollUses);
			}
			UpgradedSetTracker tracker = h.buff(UpgradedSetTracker.class);
			return tracker == null
					? Math.max(0, w.upgradeScrollUses)
					: tracker.availableUpgradeScrollUses(w);
		}

		public static int consumeUpgradeScrollUses(Hero h, MissileWeapon w) {
			if (w == null) return 0;
			if (h == null || !w.hasAssignedSetID()) {
				int credits = Math.max(0, w.upgradeScrollUses);
				w.upgradeScrollUses = 0;
				return credits;
			}
			UpgradedSetTracker tracker = Buff.affect(h, UpgradedSetTracker.class);
			if (tracker == null) {
				int credits = Math.max(0, w.upgradeScrollUses);
				w.upgradeScrollUses = 0;
				return credits;
			}
			return tracker.consumeUpgradeScrollUses(w);
		}

		public static void recordUpgradeScrollUse(Hero h, MissileWeapon w) {
			if (w == null) return;
			if (h == null || !w.hasAssignedSetID()) {
				w.upgradeScrollUses++;
				return;
			}
			UpgradedSetTracker tracker = Buff.affect(h, UpgradedSetTracker.class);
			if (tracker == null) {
				w.upgradeScrollUses++;
				return;
			}
			tracker.recordUpgradeScrollUse(w);
		}

		public static void setCanonicalLevel(Hero h, MissileWeapon w, int level) {
			if (h == null || w == null || !w.hasAssignedSetID()) return;
			UpgradedSetTracker tracker = Buff.affect(h, UpgradedSetTracker.class);
			if (tracker != null) tracker.setCanonicalLevel(w, level);
		}

		public static void invalidateSet(Hero h, MissileWeapon w) {
			if (h == null || w == null || !w.hasAssignedSetID()) return;
			UpgradedSetTracker tracker = Buff.affect(h, UpgradedSetTracker.class);
			if (tracker != null) tracker.invalidateSet(w);
		}

		public static void resetForNewCycle(Hero h) {
			if (h == null) return;
			UpgradedSetTracker tracker = h.buff(UpgradedSetTracker.class);
			if (tracker != null) {
				tracker.resetLedger(true);
			}
		}

		public static void resetLegacyForNewCycle(Hero h) {
			if (h == null) return;
			UpgradedSetTracker tracker = h.buff(UpgradedSetTracker.class);
			if (tracker != null) tracker.resetLegacyForNewCycle();
		}

		public void resetLegacyForNewCycle() {
			if (legacyCreditLedger) resetLedger(true);
		}

		private void resetLedger(boolean rejectUntrackedLocalCredits) {
			levelThresholds.clear();
			upgradeScrollCredits.clear();
			setStates.clear();
			unresolvedLegacyCredits.clear();
			legacyCreditLedger = false;
			this.rejectUntrackedLocalCredits = rejectUntrackedLocalCredits;
		}

		public int availableUpgradeScrollUses(MissileWeapon w) {
			return w == null ? 0 : resolveUpgradeScrollCredits(w);
		}

		public int consumeUpgradeScrollUses(MissileWeapon w) {
			if (w == null) return 0;
			if (stateFor(w.setID) != ACTIVE) {
				w.upgradeScrollUses = 0;
				return 0;
			}
			int credits = resolveUpgradeScrollCredits(w);
			upgradeScrollCredits.put(w.setID, 0);
			unresolvedLegacyCredits.remove(w.setID);
			w.upgradeScrollUses = 0;
			return credits;
		}

		public void recordUpgradeScrollUse(MissileWeapon w) {
			if (w == null) return;
			if (stateFor(w.setID) != ACTIVE) {
				w.upgradeScrollUses = 0;
				return;
			}
			int credits = resolveUpgradeScrollCredits(w) + 1;
			upgradeScrollCredits.put(w.setID, credits);
			w.upgradeScrollUses = credits;
		}

		public void setCanonicalLevel(MissileWeapon w, int level) {
			Integer currentLevel = w == null ? null : levelThresholds.get(w.setID);
			if (w != null && stateFor(w.setID) != CONSUMED
					&& (currentLevel == null || currentLevel != Integer.MAX_VALUE)) {
				levelThresholds.put(w.setID, level);
			}
		}

		public void invalidateSet(MissileWeapon w) {
			if (w == null || !w.hasAssignedSetID()) return;
			setStates.put(w.setID, CONSUMED);
			levelThresholds.put(w.setID, Integer.MAX_VALUE);
			upgradeScrollCredits.put(w.setID, 0);
			unresolvedLegacyCredits.remove(w.setID);
			w.upgradeScrollUses = 0;
		}

		public void markSold(MissileWeapon w) {
			markSold(w, true);
		}

		public void markSold(MissileWeapon w, boolean buybackable) {
			if (w == null || !w.hasAssignedSetID()) return;
			if (stateFor(w.setID) == CONSUMED) return;
			if (!levelThresholds.containsKey(w.setID)) {
				levelThresholds.put(w.setID, w.trueLevel());
			}
			resolveUpgradeScrollCredits(w);
			if (buybackable) {
				setStates.put(w.setID, MERCHANT_HELD);
			} else {
				invalidateSet(w);
			}
		}

		public boolean restoreFromMerchant(MissileWeapon w) {
			if (w == null || !w.hasAssignedSetID()
					|| stateFor(w.setID) != MERCHANT_HELD) return false;
			setStates.put(w.setID, ACTIVE);
			return synchronizeMember(w);
		}

		private int stateFor(long setID) {
			Integer state = setStates.get(setID);
			if (state != null) return state;
			Integer level = levelThresholds.get(setID);
			return level != null && level == Integer.MAX_VALUE ? CONSUMED : ACTIVE;
		}

		private int resolveUpgradeScrollCredits(MissileWeapon w) {
			if (stateFor(w.setID) != ACTIVE) {
				return 0;
			}
			Integer canonicalLevel = levelThresholds.get(w.setID);
			if (canonicalLevel != null && canonicalLevel == Integer.MAX_VALUE) {
				upgradeScrollCredits.put(w.setID, 0);
				unresolvedLegacyCredits.remove(w.setID);
				return 0;
			}
			if (unresolvedLegacyCredits.remove(w.setID)) {
				int credits = canonicalLevel != null
						&& canonicalLevel != Integer.MAX_VALUE
						&& w.trueLevel() == canonicalLevel
						? Math.max(0, w.upgradeScrollUses)
						: 0;
				upgradeScrollCredits.put(w.setID, credits);
			} else if (!upgradeScrollCredits.containsKey(w.setID)) {
				upgradeScrollCredits.put(w.setID, rejectUntrackedLocalCredits
						? 0 : Math.max(0, w.upgradeScrollUses));
			}
			return upgradeScrollCredits.get(w.setID);
		}

		public boolean synchronizeMember(MissileWeapon w) {
			if (w == null) return false;
			if (stateFor(w.setID) != ACTIVE) {
				w.upgradeScrollUses = 0;
				return false;
			}
			Integer canonicalLevel = levelThresholds.get(w.setID);
			if (canonicalLevel != null) {
				if (w.trueLevel() < canonicalLevel) {
					w.upgradeScrollUses = 0;
					return false;
				}
				int credits = resolveUpgradeScrollCredits(w);
				if (w.trueLevel() != canonicalLevel) {
					w.level(canonicalLevel);
				}
				w.upgradeScrollUses = credits;
			} else {
				w.upgradeScrollUses = resolveUpgradeScrollCredits(w);
			}
			return true;
		}

		public static final String SET_IDS = "set_ids";
		public static final String SET_LEVELS = "set_levels";
		private static final String CREDIT_SET_IDS = "credit_set_ids";
		private static final String CREDIT_USES = "credit_uses";
		private static final String UNRESOLVED_CREDIT_SET_IDS = "unresolved_credit_set_ids";
		private static final String REJECT_UNTRACKED_LOCAL_CREDITS =
				"reject_untracked_local_credits";
		private static final String SET_STATES = "set_states";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			long[] IDs = new long[levelThresholds.size()];
			int[] levels = new int[levelThresholds.size()];
			int i = 0;
			for (Long ID : levelThresholds.keySet()){
				IDs[i] = ID;
				levels[i] = levelThresholds.get(ID);
				i++;
			}
			bundle.put(SET_IDS, IDs);
			bundle.put(SET_LEVELS, levels);

			long[] creditIDs = new long[upgradeScrollCredits.size()];
			int[] credits = new int[upgradeScrollCredits.size()];
			i = 0;
			for (Long ID : upgradeScrollCredits.keySet()) {
				creditIDs[i] = ID;
				credits[i] = upgradeScrollCredits.get(ID);
				i++;
			}
			bundle.put(CREDIT_SET_IDS, creditIDs);
			bundle.put(CREDIT_USES, credits);

			long[] unresolvedIDs = new long[unresolvedLegacyCredits.size()];
			i = 0;
			for (Long ID : unresolvedLegacyCredits) unresolvedIDs[i++] = ID;
			bundle.put(UNRESOLVED_CREDIT_SET_IDS, unresolvedIDs);
			bundle.put(REJECT_UNTRACKED_LOCAL_CREDITS, rejectUntrackedLocalCredits);

			long[] stateIDs = new long[setStates.size()];
			int[] states = new int[setStates.size()];
			i = 0;
			for (Long ID : setStates.keySet()) {
				stateIDs[i] = ID;
				states[i] = setStates.get(ID);
				i++;
			}
			bundle.put(SET_STATES + "_ids", stateIDs);
			bundle.put(SET_STATES, states);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			long[] IDs = bundle.getLongArray(SET_IDS);
			int[] levels = bundle.getIntArray(SET_LEVELS);
			levelThresholds.clear();
			for (int i = 0; IDs != null && levels != null
					&& i < Math.min(IDs.length, levels.length); i++){
				levelThresholds.put(IDs[i], levels[i]);
			}

			upgradeScrollCredits.clear();
			setStates.clear();
			if (bundle.contains(SET_STATES + "_ids") && bundle.contains(SET_STATES)) {
				long[] stateIDs = bundle.getLongArray(SET_STATES + "_ids");
				int[] states = bundle.getIntArray(SET_STATES);
				for (int i = 0; stateIDs != null && states != null
						&& i < Math.min(stateIDs.length, states.length); i++) {
					int state = states[i];
					if (state >= ACTIVE && state <= CONSUMED) {
						setStates.put(stateIDs[i], state);
					}
				}
			}
			unresolvedLegacyCredits.clear();
			if (bundle.contains(CREDIT_SET_IDS) && bundle.contains(CREDIT_USES)) {
				legacyCreditLedger = false;
				rejectUntrackedLocalCredits = bundle.getBoolean(
						REJECT_UNTRACKED_LOCAL_CREDITS);
				long[] creditIDs = bundle.getLongArray(CREDIT_SET_IDS);
				int[] credits = bundle.getIntArray(CREDIT_USES);
				for (int i = 0; creditIDs != null && credits != null
						&& i < Math.min(creditIDs.length, credits.length); i++) {
					upgradeScrollCredits.put(creditIDs[i], Math.max(0, credits[i]));
				}
				if (bundle.contains(UNRESOLVED_CREDIT_SET_IDS)) {
					long[] unresolvedIDs = bundle.getLongArray(UNRESOLVED_CREDIT_SET_IDS);
					if (unresolvedIDs != null) {
						for (long ID : unresolvedIDs) unresolvedLegacyCredits.add(ID);
					}
				}
			} else {
				legacyCreditLedger = true;
				rejectUntrackedLocalCredits = false;
				unresolvedLegacyCredits.addAll(levelThresholds.keySet());
			}
			for (Long ID : levelThresholds.keySet()) {
				if (levelThresholds.get(ID) == Integer.MAX_VALUE) {
					setStates.put(ID, CONSUMED);
				} else if (!setStates.containsKey(ID)) {
					setStates.put(ID, ACTIVE);
				}
			}
		}
	}

	private boolean usesIndependentSetID(){
		return isUpgradable() && defaultQuantity() > 1;
	}

	private boolean hasAssignedSetID(){
		return usesIndependentSetID() && setID != UNASSIGNED_SET_ID;
	}

	public void ensureSetIDAssigned(){
		if (usesIndependentSetID() && setID == UNASSIGNED_SET_ID){
			setID = newSetID();
		}
	}

	private static long newSetID(){
		long id;
		do {
			id = SET_ID_RANDOM.nextLong();
		} while (id == UNASSIGNED_SET_ID);
		return id;
	}

	public static void sanitizeInventorySets(Hero hero, Item preferredSource){
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null){
			return;
		}
		UpgradedSetTracker tracker = hero.buff(UpgradedSetTracker.class);
		if (tracker != null) {
			reconcileInventoryMembers(hero.belongings.backpack, tracker, true, new boolean[]{false});
		}
		sanitizeInventorySets(hero.belongings.backpack, preferredSource, true);
	}

	public static void prepareIncomingBag(Hero hero, Bag bag) {
		if (hero == null || bag == null) return;
		UpgradedSetTracker tracker = hero.buff(UpgradedSetTracker.class);
		if (tracker == null) return;
		reconcileInventoryMembers(bag, tracker, true, new boolean[]{false});
		sanitizeInventorySets(bag, null, true);
	}

	private static void reconcileInventoryMembers(Bag bag, UpgradedSetTracker tracker,
			boolean showWarning, boolean[] warned) {
		for (Item item : bag.items.toArray(new Item[0])) {
			if (item instanceof MissileWeapon) {
				MissileWeapon missile = (MissileWeapon) item;
				missile.ensureSetIDAssigned();
				if (!tracker.synchronizeMember(missile)) {
					if (showWarning && !warned[0]) {
						logDust(missile);
						warned[0] = true;
					}
					missile.detachAll(bag);
					missile.quantity(0);
				}
			} else if (item instanceof Bag) {
				reconcileInventoryMembers((Bag) item, tracker, showWarning, warned);
			}
		}
	}

	static void sanitizeInventorySets(Bag inventory, Item preferredSource, boolean showWarning){
		HashMap<Long, ArrayList<MissileSetEntry>> grouped = new HashMap<>();
		collectMissileEntries(inventory, preferredSource, grouped);

		for (ArrayList<MissileSetEntry> entries : grouped.values()){
			if (entries.isEmpty()) continue;

			MissileWeapon sample = entries.get(0).weapon;
			if (!sample.hasAssignedSetID()) continue;

			int totalQuantity = 0;
			for (MissileSetEntry entry : entries){
				totalQuantity += entry.weapon.quantity();
			}

			if (totalQuantity <= sample.defaultQuantity()) continue;

			MissileSetEntry survivor = selectSurvivor(entries);
			boolean warned = false;

			for (MissileSetEntry entry : entries){
				if (entry == survivor) continue;

				MissileWeapon weapon = entry.weapon;
				if (weapon.quantity() <= 0) continue;

				if (showWarning && !warned){
					logDuplicateDust(weapon);
					warned = true;
				}

				survivor.weapon.merge(weapon);
				weapon.detachAll(entry.container);
				weapon.quantity(0);
			}
		}
	}

	private static void logDust(MissileWeapon weapon) {
		if (Gdx.app != null && Gdx.files != null) {
			GLog.w(Messages.get(weapon, "dust"));
		}
	}

	private static void logDuplicateDust(MissileWeapon weapon) {
		if (Gdx.app != null && Gdx.files != null) {
			GLog.w(Messages.get(weapon, "duplicate_dust"));
		}
	}

	private void sanitizeAfterUpgrade(){
		Hero hero = Dungeon.hero;
		if (!bundleRestoring && hero != null && hero.belongings != null
				&& hero.belongings.backpack != null && hero.belongings.backpack.contains(this)){
			sanitizeAfterUpgrade(hero.belongings.backpack, true);
		}
	}

	void sanitizeAfterUpgrade(Bag inventory, boolean showWarning){
		sanitizeInventorySets(inventory, this, showWarning);
	}

	private static void collectMissileEntries(Bag bag, Item preferredSource,
			HashMap<Long, ArrayList<MissileSetEntry>> grouped){
		for (Item item : bag.items.toArray(new Item[0])){
			if (item instanceof MissileWeapon){
				MissileWeapon weapon = (MissileWeapon) item;
				if (!weapon.hasAssignedSetID()) continue;
				grouped.computeIfAbsent(weapon.setID, ignored -> new ArrayList<>())
						.add(new MissileSetEntry(weapon, bag, isPreferredSource(item, preferredSource)));
			} else if (item instanceof Bag){
				collectMissileEntries((Bag) item, preferredSource, grouped);
			}
		}
	}

	private static boolean isPreferredSource(Item item, Item preferredSource){
		if (preferredSource == null) return false;
		if (item == preferredSource) return true;
		return preferredSource instanceof Bag && ((Bag) preferredSource).contains(item);
	}

	private static MissileSetEntry selectSurvivor(ArrayList<MissileSetEntry> entries){
		MissileSetEntry survivor = entries.get(0);
		for (int i = 1; i < entries.size(); i++){
			MissileSetEntry candidate = entries.get(i);
			if (candidate.weapon.quantity() > survivor.weapon.quantity()){
				survivor = candidate;
			} else if (candidate.weapon.quantity() == survivor.weapon.quantity()){
				if (candidate.weapon.trueLevel() > survivor.weapon.trueLevel()){
					survivor = candidate;
				} else if (candidate.weapon.trueLevel() == survivor.weapon.trueLevel()
						&& survivor.preferred && !candidate.preferred){
					survivor = candidate;
				}
			}
		}
		return survivor;
	}

	private static class MissileSetEntry {
		private final MissileWeapon weapon;
		private final Bag container;
		private final boolean preferred;

		private MissileSetEntry(MissileWeapon weapon, Bag container, boolean preferred) {
			this.weapon = weapon;
			this.container = container;
			this.preferred = preferred;
		}
	}
}
