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
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfStormClouds;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SprayGun extends Weapon {

	public static final String AC_SPRAY = "SPRAY";
	public static final String AC_LOAD_ENERGY = "LOAD_ENERGY";
	public static final String AC_LOAD_POTION = "LOAD_POTION";

	private static final int BASE_CHARGES = 6;
	private static final int MAX_CHARGES = 12;
	private static final int HASTE_COOLDOWN = 5;
	private static final int RANDOM_HASTE_COOLDOWN = 30;
	private static final ItemSprite.Glowing CONCENTRATED_GLOW = new ItemSprite.Glowing(0xFFFFFF, 0.35f);

	private static final String CHARGES = "charges";
	private static final String LOADED = "loaded";
	private static final String CONCENTRATED = "concentrated_potion";

	private int charges = BASE_CHARGES;
	private Loaded loaded = Loaded.NONE;
	private boolean concentratedPotion = false;

	{
		image = ItemSpriteSheet.SPRAYGUN_BASE;
		defaultAction = AC_SPRAY;
		usesTargeting = true;
		unique = true;
		bones = false;
		levelKnown = true;
		cursedKnown = true;
	}

	private enum Loaded {
		NONE,
		STRENGTH,
		LIQUID_FLAME,
		HASTE,
		HEALING,
		FROST,
		LEVITATION,
		TOXIC_GAS,
		PURITY,
		PARALYTIC_GAS,
		EXPERIENCE,
		MIND_VISION,
		INVISIBILITY
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);
		if (charges > 0) {
			actions.add(AC_SPRAY);
		}
		actions.add(AC_LOAD_ENERGY);
		actions.add(AC_LOAD_POTION);
		return actions;
	}
    @Override
    public int STRReq(int lvl) {
        if(hero!=null){
            return hero.STR;
        }else{
            return 10;
        }
    }

	@Override
	public String defaultAction() {
		return charges > 0 ? AC_SPRAY : AC_LOAD_ENERGY;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		curUser = hero;
		curItem = this;

		if (action.equals(AC_SPRAY)) {
			if (charges <= 0) {
				GLog.w(Messages.get(this, "no_charges"));
			} else {
				GameScene.selectCell(sprayer);
			}
		} else if (action.equals(AC_LOAD_ENERGY)) {
			loadEnergy(hero);
		} else if (action.equals(AC_LOAD_POTION)) {
			GameScene.selectItem(potionSelector);
		}
	}

	@Override
	public int image() {
		return imageFor(loaded);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public int level() {
		return baseSprayLevel();
	}

	@Override
	public int buffedLvl() {
		return sprayLevel();
	}

	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		updateQuickslot();
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public Weapon enchant(Enchantment ench) {
		enchantment = null;
		return this;
	}

	@Override
	public int min(int lvl) {
		return isAlchemist() ? 4 + sprayLevel() : 0;
	}

	@Override
	public int max(int lvl) {
		return isAlchemist() ? 10 + 5 * sprayLevel() : 0;
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return sprayTargeting(user, dst).collisionPos;
	}

	@Override
	public String status() {
		return charges + "/" + maxCharges();
	}

	@Override
	public String info() {
		int lvl = sprayLevel();
		int knockback = knockback();
		String info = Messages.get(this, loaded == Loaded.NONE ? "desc_unloaded" : "desc_loaded");
        if(hero==null) return info;
		if (isAlchemist()) {
			info += "\n\n" + Messages.get(this, "stats_alchemist", min(0), max(0), knockback);
		} else {
			info += "\n\n" + Messages.get(this, "stats", knockback);
		}
		info += "\n\n" + Messages.get(this, "charges", charges, maxCharges());

        if(hero.subClass==HeroSubClass.ALCHEMIST) info += "\n\n" + Messages.get(this, "effect_" + loaded.name().toLowerCase()+"_alchemist",lvl);
        else info += "\n\n" + Messages.get(this, "effect_" + loaded.name().toLowerCase(), lvl);
		if (concentratedPotion) {
			info += "\n\n" + Messages.get(this, "concentrated");
		}
		return info;
	}


	@Override
	public ItemSprite.Glowing glowing() {
		return concentratedPotion ? CONCENTRATED_GLOW : super.glowing();
	}

	public int charges() {
		return charges;
	}

	public void loadConcentratedPotion(Hero hero) {
		charges = Math.min(maxCharges(), charges + BASE_CHARGES);
		concentratedPotion = true;
		GLog.p(Messages.get(this, "load_concentrated"));
		updateQuickslot();
	}

	private int baseSprayLevel() {
		return Math.min(6, Math.max(0, Dungeon.hero == null ? 0 : Dungeon.hero.lvl / 5));
	}

	private int sprayLevel() {
		return baseSprayLevel() + concentratedBonus();
	}

	private int concentratedBonus() {
		if (!concentratedPotion || Dungeon.hero == null) {
			return 0;
		}
		int talentBonus = Dungeon.hero.pointsInTalent(Talent.REAGENT_ENHANCEMENT);
		return talentBonus > 0 ? 2 + talentBonus : 2;
	}

	private int maxCharges() {
		return Math.min(MAX_CHARGES, BASE_CHARGES + baseSprayLevel());
	}

	private int knockback() {
		int kb = 1 + sprayLevel() / 2;
		if (loaded == Loaded.STRENGTH) {
			kb += isAlchemist() ? 2 : 1;
		}
		return kb;
	}

	private boolean isAlchemist() {
		return Dungeon.hero != null && Dungeon.hero.subClass.is(HeroSubClass.ALCHEMIST);
	}
    float ins=0;

	private int range(Hero hero) {
		return 5;
	}

	private float degrees(Hero hero) {
		return 75f;
	}
    private Ballistica bolt;
    private ConeAOE cone;

	private Ballistica sprayTargeting(Hero hero, int target) {
		bolt = new Ballistica(hero.pos, target, Ballistica.MAGIC_BOLT);

		if (hero.subClass.is(HeroSubClass.ALCHEMIST)) {
			return bolt=new Ballistica(hero.pos, target, Ballistica.WONT_STOP);
		}


		return bolt;
	}

	private void loadEnergy(Hero hero) {
		int need = maxCharges() - charges;
		if (need <= 0) {
			GLog.i(Messages.get(this, "full"));
			return;
		}

		int fromEnergy = Math.min(Dungeon.energy, need);
		int missing = need - fromEnergy;
		Reason reason = hero.buff(Reason.class);
		int fromReason = reason == null ? 0 : Math.min(missing, reason.reason / 5);
		int total = fromEnergy + fromReason;

		if (total <= 0) {
			GLog.w(Messages.get(this, "no_energy"));
			return;
		}

		Dungeon.energy -= fromEnergy;
		if (fromReason > 0) {
			Reason.loseReason(hero, fromReason * 5);
		}
		charges += total;
		if (fromEnergy > 0) {
			Talent.onAlchemyEnergyConsumed(hero);
		}
		Talent.onSprayGunLoaded(hero);
		GLog.i(Messages.get(this, fromReason > 0 ? "load_reason" : "load_energy"));
		hero.spendAndNext(Talent.sprayGunLoadTime(hero));
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
		updateQuickslot();
	}

	private void loadPotion(Hero hero, Potion potion) {
		Loaded next = loadedFor(potion);
		if (next == null) {
			return;
		}

		loaded = next;
		image = imageFor(loaded);
		if (next == Loaded.STRENGTH) {
			GLog.p(Messages.get(this, "load_strength"));
		} else {
			if (!Talent.alchemistPotionLoadPreserved(hero)) {
				potion.detach(hero.belongings.backpack);
			}
			charges = Math.min(maxCharges(), charges + (next == Loaded.EXPERIENCE ? 10 : BASE_CHARGES));
			GLog.i(Messages.get(this, "load_potion", potion.name()));
		}
		hero.spendAndNext(1f);
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.DRINK);
		updateQuickslot();
	}

	private void spray(Hero hero, int target) {
		Ballistica aim = sprayTargeting(hero, target);
		int dist = aim.dist;
		ArrayList<Loaded> effects = activeLoadedEffects(hero);
        ins=0;
        if (target == hero.pos) {
            GLog.i(Messages.get(this,"no_target"));
            return;
        }
		if (hero.subClass.is(HeroSubClass.ALCHEMIST)) {
            dist = 5;
			cone = new ConeAOE(aim, dist, degrees(hero), Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);

            MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
            ConeAOE cone1 = new ConeAOE(aim, dist, degrees(hero), Ballistica.WONT_STOP);
            if (eternalFire != null && eternalFire.volume > 0 && loaded!=Loaded.LIQUID_FLAME) {
                for(int cell:cone1.cells){
                    if(Dungeon.level.distance(hero.pos,cell)<dist){
                        eternalFire.clear( cell );
                    }
                }
            }

			for (Ballistica ray : cone.outerRays) {
				((MagicMissile)hero.sprite.parent.recycle(MagicMissile.class)).reset(coneMissileType(), hero.sprite, ray.path.get(ray.dist), null);
			}

			hero.sprite.attack(target, new Callback() {
				@Override
				public void call() {
					perCellEffect(new ArrayList<>(cone.cells), true, effects);
					ArrayList<Char> chars = new ArrayList<>();
					for (Char ch : Actor.chars()) {
						if (ch != hero && cone.cells.contains(ch.pos)) {
							chars.add(ch);
						}
					}
					for (Char ch : chars) {
						affectChar(hero, ch, effects);
					}
					finishSpray(hero, effects);
				}
			});


		} else {
            Ballistica route = sprayTargeting(hero, target);
            MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
            if (eternalFire != null && eternalFire.volume > 0 && loaded!=Loaded.LIQUID_FLAME) {
                eternalFire.clear( route.collisionPos );
                //bolt ends 1 tile short of fire, so check next tile too
                if (route.path.size() > route.dist+1){
                    eternalFire.clear( route.path.get(route.dist+1) );
                }

            }
            int cell = route.collisionPos;

			hero.sprite.zap(cell);
			MagicMissile.boltFromChar(hero.sprite.parent, singleMissileType(), hero.sprite, cell, new Callback() {
				@Override
				public void call() {
					ArrayList<Integer> cells = new ArrayList<>();
					cells.add(cell);
					perCellEffect(cells, false, effects);
					Char ch = Actor.findChar(cell);
					if (ch != null && ch != hero) {
						affectChar(hero, ch, effects);
					}else {
                        Dungeon.level.pressCell(bolt.collisionPos);
                    }
					finishSpray(hero, effects);
				}
			});
            Sample.INSTANCE.play(Assets.Sounds.ZAP);
		}

		hero.busy();
	}

	private void finishSpray(Hero hero, ArrayList<Loaded> effects) {
		if (!Talent.alchemistSprayChargePreserved(hero)) {
			charges--;
		}
		if (effects.contains(Loaded.HASTE) && hero.buff(HasteSprayCooldown.class) == null) {
			Buff.affect(hero, HasteSprayCooldown.class, loaded == Loaded.HASTE ? HASTE_COOLDOWN-1 : RANDOM_HASTE_COOLDOWN-1);
			hero.next();
		} else {
			hero.spendAndNext(1f);
		}
        if(effects.contains(Loaded.INVISIBILITY) && hero.subClass.is(HeroSubClass.ALCHEMIST) && ins>0) Buff.affect(hero, Invisibility.class,ins);
        else Invisibility.dispel();
		concentratedPotion = false;
        Sample.INSTANCE.play( Assets.Sounds.SQUIRT, 1, 1, Random.Float( 0.9f, 1.1f ) );
		updateQuickslot();
	}

	private void perCellEffect(ArrayList<Integer> cells, boolean alchemist, ArrayList<Loaded> effects) {
		if (!effects.contains(Loaded.LIQUID_FLAME)) {

			Fire fire = (Fire)Dungeon.level.blobs.get(Fire.class);
			if (fire != null) {
				for (int cell : cells) {
					fire.clear(cell);
					Char ch = Actor.findChar(cell);
					if (ch != null) Buff.detach(ch, Burning.class);

				}
			}

		}

		for (int cell : cells) {
            if(Dungeon.level.map[cell]==Terrain.DOOR)  Dungeon.level.pressCell(cell);
			for (Loaded effect : effects) {
				switch (effect) {
					case FROST:
						GameScene.add(Blob.seed(cell, 8, Freezing.class));
						break;
					case LIQUID_FLAME:
                        ArrayList<Integer> adjacentCells = new ArrayList<>();
                        if(Dungeon.level.adjacent(bolt.sourcePos, cell)
                                && !(Dungeon.level.flamable[cell] || Dungeon.level.solid[cell])){
                            adjacentCells.add(cell);
                            if (Dungeon.level.heaps.get(cell) != null){
                                Dungeon.level.heaps.get(cell).burn();
                            }
                        }else{
                            GameScene.add(Blob.seed(cell, 2, Fire.class));
                            Dungeon.level.pressCell(cell);
                            Fire.burn(cell);
                        }
                        //if wand was shot right at a wall
                        if (cone==null || cone.cells.isEmpty()){
                            adjacentCells.add(bolt.sourcePos);
                        }

                        //ignite cells that share a side with an adjacent cell, are flammable, and are closer to the collision pos
                        //This prevents short-range casts not igniting barricades or bookshelves
                        for (int c : adjacentCells){
                            for (int i : PathFinder.NEIGHBOURS8){
                                if (Dungeon.level.trueDistance(cell+i, bolt.collisionPos) < Dungeon.level.trueDistance(cell, bolt.collisionPos)
                                        && Dungeon.level.flamable[cell+i]
                                        && Fire.volumeAt(cell+i, Fire.class) == 0){
                                    Dungeon.level.pressCell(cell);
                                    GameScene.add( Blob.seed( c+i, 2, Fire.class ) );
                                }
                            }
                        }

						break;
					case PARALYTIC_GAS:
						GameScene.add(Blob.seed(cell, alchemist ? 8 : 4, Electricity.class));
						break;
					case LEVITATION:
						if (alchemist && !Dungeon.level.solid[cell] && Dungeon.level.map[cell]!=Terrain.CHASM && Dungeon.level.map[cell]!=Terrain.EXIT && Dungeon.level.map[cell]!=Terrain.ENTRANCE) {
                            Dungeon.level.setCellToWater(true, cell);
						}
						break;
					default:
				}
			}
		}
	}


	private void affectChar(Hero hero, Char ch, ArrayList<Loaded> effects) {
		boolean ally = ch.alignment == hero.alignment;
		boolean alchemist = hero.subClass.is(HeroSubClass.ALCHEMIST);
		int lvl = sprayLevel();

		if (alchemist && dealsAlchemistDamage(ally, loaded)) {
			int damage = Hero.heroDamageIntRange(min(lvl), max(lvl));
			damage = Math.round(damage * Talent.alchemistCloseBlastMultiplier(hero, Dungeon.level.distance(hero.pos, ch.pos), range(hero)));
			ch.damage(damage, new WandOfMagicMissile());
		}
		if (!ally && loaded != Loaded.FROST && loaded != Loaded.MIND_VISION && knockback() > 0) {
            Ballistica a = new Ballistica(hero.pos, ch.pos, Ballistica.WONT_STOP);
			WandOfBlastWave.throwChar(ch, new Ballistica(ch.pos, a.collisionPos, Ballistica.MAGIC_BOLT), knockback(), true, true, SprayGun.this);
		}

		for (Loaded effect : effects) {
			applyLoadedEffect(hero, ch, effect, ally, alchemist, lvl);
		}
        Buff.detach(ch, Ooze.class);
	}


	private void applyLoadedEffect(Hero hero, Char ch, Loaded effect, boolean ally, boolean alchemist, int lvl) {
		switch (effect) {
			case NONE:
				break;
			case STRENGTH:
				if (!ally) Buff.affect(ch, Weakness.class, alchemist ? 8 + 2 * lvl : 4 + lvl);
				break;
			case HEALING:
				if (ally) {
					int heal = Math.round(ch.HT * (alchemist ? 0.5f : 0.3f)) + 5 * lvl;
					int trueHeal = Math.min(heal, ch.HT - ch.HP);
					if (trueHeal > 0) ch.heal(trueHeal);
					if (alchemist && heal > trueHeal) {
						Buff.affect(ch, Barrier.class).setShield(Math.min(ch.HT / 2, heal - trueHeal));
					}
					PotionOfHealing.cure(ch);
				}
				break;
			case MIND_VISION:
				if (!ally) {

					if (alchemist){
                        Buff.append(hero, TalismanOfForesight.CharAwareness.class, 4 + lvl).charID = ch.id();
                    }
                    teleportAway(ch, hero);
				}
				break;
			case FROST:
				if (!Char.hasProp(ch, Char.Property.ICY)) {
					if (alchemist) {
						Buff.prolong(ch, Frost.class, 4 + lvl);
						Buff.prolong(ch, Roots.class, 4 + lvl);
					} else if (Random.Float() < 0.30f + lvl * 0.05f) {
						Buff.prolong(ch, Frost.class, 4 + lvl);
					}
				}
				break;
			case LIQUID_FLAME:
				if (!Char.hasProp(ch, Char.Property.FIERY)) {
					Buff.affect(ch, Burning.class).reignite(ch);
					ch.damage(Hero.heroDamageIntRange(alchemist ? 4 + lvl : 1 + lvl, alchemist ? 10 + 5 * lvl : 3 + 2 * lvl), new Burning());
				}
				break;
			case TOXIC_GAS:
				if (!ally) {
					if (alchemist) {
						Buff.affect(ch, Corrosion.class).set(3 + 2 * lvl, 2, Corrosion.class);
					} else {
						Buff.affect(ch, Poison.class).set(6 + 2 * lvl);
					}
				}
				break;
			case HASTE:
				if (ally && alchemist) Buff.prolong(ch, Adrenaline.class, 10f);
                if (!ally) Buff.prolong(ch, Cripple.class, 2 + lvl / 2f);
				break;
			case INVISIBILITY:
                if (!ally) Buff.prolong(ch, Blindness.class, 4 + lvl);
				if (alchemist && ally) ins+=2 + lvl / 2f;
				break;
			case LEVITATION:
				if (!ally) Buff.prolong(ch, Vertigo.class, 2 + lvl);
				if (alchemist && Char.hasProp(ch, Char.Property.FIERY)) {
					ch.damage(Hero.heroDamageIntRange(5 + Dungeon.depth, 10 + 2 * Dungeon.depth), new GeyserTrap());
				}
				break;
			case PARALYTIC_GAS:
				//Buff.prolong(ch, Paralysis.class, alchemist ? 3f : 1.5f);
				break;
			case PURITY:
				if (ally && alchemist) {
					PotionOfCleansing.cleanse(ch, 10f);
				}
                //if (!ally) extendNegativeBuffs(ch, alchemist ? 4 + lvl : 2 + lvl);
                if (!ally) extendNegativeBuffs(ch, alchemist ? 4  : 2 );

				break;
			case EXPERIENCE:
				if (ally) {
					Buff.prolong(ch, Bless.class, 30f);
				} else if (Char.hasProp(ch, Char.Property.UNDEAD) || Char.hasProp(ch, Char.Property.DEMONIC)) {
					ch.damage(7 + lvl, new WandOfMagicMissile());
				}
                /*
				if (alchemist && !ally) {
					ch.damage(7 + lvl, new WandOfMagicMissile());
				}

                 */
				break;
		}


	}

	private boolean dealsAlchemistDamage(boolean ally, Loaded effect) {
		if (effect == Loaded.FROST || effect == Loaded.LIQUID_FLAME || effect == Loaded.MIND_VISION) return false;
		if (!ally) return true;
		return effect == Loaded.NONE
				|| effect == Loaded.STRENGTH
				|| effect == Loaded.MIND_VISION
				|| effect == Loaded.TOXIC_GAS
				|| effect == Loaded.INVISIBILITY
				|| effect == Loaded.LEVITATION
				|| effect == Loaded.PARALYTIC_GAS;
	}

	private ArrayList<Loaded> activeLoadedEffects(Hero hero) {
		ArrayList<Loaded> effects = new ArrayList<>();
		effects.add(loaded);

		int extraEffects = concentratedPotion ? hero.pointsInTalent(Talent.CONCENTRATED_ESSENCE) : 0;
		if (extraEffects <= 0) {
			return effects;
		}

		ArrayList<Loaded> candidates = new ArrayList<>();
		for (Loaded candidate : Loaded.values()) {
			if (candidate != Loaded.NONE && candidate != loaded && compatibleWithEffects(candidate, effects)) {
				candidates.add(candidate);
			}
		}
		Random.shuffle(candidates);

		for (Loaded candidate : candidates) {
			if (effects.size() >= extraEffects + 1) {
				break;
			}
			if (compatibleWithEffects(candidate, effects)) {
				effects.add(candidate);
			}
		}
		sortEffects(effects);
		return effects;
	}

	private boolean compatibleWithEffects(Loaded candidate, ArrayList<Loaded> effects) {
		if (candidate == Loaded.FROST && effects.contains(Loaded.LIQUID_FLAME)) {
			return false;
		}
		if (candidate == Loaded.LIQUID_FLAME && effects.contains(Loaded.FROST)) {
			return false;
		}
		return !effects.contains(candidate);
	}

	private void sortEffects(ArrayList<Loaded> effects) {
		ArrayList<Loaded> ordered = new ArrayList<>();
		addEffectIfPresent(ordered, effects, Loaded.STRENGTH);
		for (Loaded effect : effects) {
			if (effect != Loaded.STRENGTH && effect != Loaded.PURITY && effect != Loaded.MIND_VISION) {
				ordered.add(effect);
			}
		}
		addEffectIfPresent(ordered, effects, Loaded.PURITY);
		addEffectIfPresent(ordered, effects, Loaded.MIND_VISION);
		effects.clear();
		effects.addAll(ordered);
	}

	private void addEffectIfPresent(ArrayList<Loaded> ordered, ArrayList<Loaded> effects, Loaded effect) {
		if (effects.contains(effect)) {
			ordered.add(effect);
		}
	}

	private void teleportAway(Char ch, Hero hero) {
		if (Char.hasProp(ch, Char.Property.IMMOVABLE) || ch.isImmune(ScrollOfTeleportation.class)) {
			return;
		}

		ArrayList<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++) {
			int dist = Dungeon.level.distance(hero.pos, i);
			if (dist >= 8 && dist <= 10
					&& (Dungeon.level.passable[i] || Dungeon.level.avoid[i])
					&& !Dungeon.level.solid[i]
					&& Actor.findChar(i) == null) {
				candidates.add(i);
			}
		}
		Random.shuffle(candidates);
		for (int cell : candidates) {
			if (ScrollOfTeleportation.teleportToLocation(ch, cell)) {
				return;
			}
		}
		ScrollOfTeleportation.teleportChar(ch);
	}

	private void extendNegativeBuffs(Char ch, float duration) {
		if (ch.buff(Poison.class) != null) ch.buff(Poison.class).extend(duration);
		if (ch.buff(Corrosion.class) != null) ch.buff(Corrosion.class).extend(duration);
		if (ch.buff(Bleeding.class) != null) ch.buff(Bleeding.class).extend(duration);
		if (ch.buff(Burning.class) != null) ch.buff(Burning.class).extend(duration);
		if (ch.buff(Ooze.class) != null) ch.buff(Ooze.class).extend(duration);
		if (ch.buff(Dread.class) != null) ch.buff(Dread.class).extend(duration);
		if (ch.buff(Chill.class) != null) Buff.prolong(ch, Chill.class, duration);
		if (ch.buff(Weakness.class) != null) Buff.prolong(ch, Weakness.class, duration);
		if (ch.buff(Vulnerable.class) != null) Buff.prolong(ch, Vulnerable.class, duration);
		if (ch.buff(Hex.class) != null) Buff.prolong(ch, Hex.class, duration);
		if (ch.buff(Degrade.class) != null) Buff.prolong(ch, Degrade.class, duration);
		if (ch.buff(Cripple.class) != null) Buff.prolong(ch, Cripple.class, duration);
		if (ch.buff(Slow.class) != null) Buff.prolong(ch, Slow.class, duration);
		if (ch.buff(Blindness.class) != null) Buff.prolong(ch, Blindness.class, duration);
		if (ch.buff(Terror.class) != null) Buff.prolong(ch, Terror.class, duration);
		if (ch.buff(Vertigo.class) != null) Buff.prolong(ch, Vertigo.class, duration);
		if (ch.buff(Roots.class) != null) Buff.prolong(ch, Roots.class, duration);
		if (ch.buff(Paralysis.class) != null) Buff.prolong(ch, Paralysis.class, duration);
	}

	private int coneMissileType() {
		switch (loaded) {
			case STRENGTH:
				return MagicMissile.SPRAY_STRENGTH_CONE;
			case FROST:
				return MagicMissile.FROST_CONE;
			case LIQUID_FLAME:
				return MagicMissile.FIRE_CONE;
			case TOXIC_GAS:
				return MagicMissile.CORROSION_CONE;
			case HASTE:
				return MagicMissile.SPRAY_HASTE_CONE;
			case HEALING:
				return MagicMissile.HEAL_CONE;
			case LEVITATION:
				return MagicMissile.SPRAY_LEVITATION_CONE;
			case PARALYTIC_GAS:
				return MagicMissile.SPARK_CONE;
			case PURITY:
				return MagicMissile.SPRAY_PURITY_CONE;
			case EXPERIENCE:
				return MagicMissile.SPRAY_EXP_CONE;
			case MIND_VISION:
				return MagicMissile.SPRAY_MIND_CONE;
			case INVISIBILITY:
				return MagicMissile.SPRAY_INVIS_CONE;
			case NONE:
			default:
				return MagicMissile.SPRAY_BASE_CONE;
		}
	}

	private int singleMissileType() {
		switch (loaded) {
			case STRENGTH:
				return MagicMissile.SPRAY_STRENGTH;
			case FROST:
				return MagicMissile.FROST;
			case LIQUID_FLAME:
				return MagicMissile.FIRE;
			case TOXIC_GAS:
				return MagicMissile.SPRAY_TOXIC;
			case HASTE:
				return MagicMissile.SPRAY_HASTE;
			case HEALING:
				return MagicMissile.HEAL_MISSILE;
			case LEVITATION:
				return MagicMissile.SPRAY_LEVITATION;
			case PARALYTIC_GAS:
				return MagicMissile.SHAMAN_BLUE;
			case PURITY:
				return MagicMissile.SPRAY_PURITY;
			case EXPERIENCE:
				return MagicMissile.SPRAY_EXP;
			case MIND_VISION:
				return MagicMissile.SPRAY_MIND;
			case INVISIBILITY:
				return MagicMissile.SPRAY_INVIS;
			case NONE:
			default:
				return MagicMissile.SPRAY_BASE;
		}
	}

	private static int imageFor(Loaded loaded) {
		switch (loaded) {
			case STRENGTH: return ItemSpriteSheet.SPRAYGUN_STRENGTH;
			case LIQUID_FLAME: return ItemSpriteSheet.SPRAYGUN_LIQUID_FLAME;
			case HASTE: return ItemSpriteSheet.SPRAYGUN_HASTE;
			case HEALING: return ItemSpriteSheet.SPRAYGUN_HEALING;
			case FROST: return ItemSpriteSheet.SPRAYGUN_FROST;
			case LEVITATION: return ItemSpriteSheet.SPRAYGUN_LEVITATION;
			case TOXIC_GAS: return ItemSpriteSheet.SPRAYGUN_TOXIC_GAS;
			case PURITY: return ItemSpriteSheet.SPRAYGUN_PURITY;
			case PARALYTIC_GAS: return ItemSpriteSheet.SPRAYGUN_PARALYTIC_GAS;
			case EXPERIENCE: return ItemSpriteSheet.SPRAYGUN_EXPERIENCE;
			case MIND_VISION: return ItemSpriteSheet.SPRAYGUN_MIND_VISION;
			case INVISIBILITY: return ItemSpriteSheet.SPRAYGUN_INVISIBILITY;
			case NONE:
			default: return ItemSpriteSheet.SPRAYGUN_BASE;
		}
	}

	private Loaded loadedFor(Potion potion) {
		if (potion instanceof PotionOfStrength) return Loaded.STRENGTH;
		if (potion instanceof PotionOfLiquidFlame) return Loaded.LIQUID_FLAME;
		if (potion instanceof PotionOfHaste) return Loaded.HASTE;
		if (potion instanceof PotionOfHealing) return Loaded.HEALING;
		if (potion instanceof PotionOfFrost) return Loaded.FROST;
		if (potion instanceof PotionOfLevitation) return Loaded.LEVITATION;
		if (potion instanceof PotionOfToxicGas) return Loaded.TOXIC_GAS;
		if (potion instanceof PotionOfPurity) return Loaded.PURITY;
		if (potion instanceof PotionOfParalyticGas) return Loaded.PARALYTIC_GAS;
		if (potion instanceof PotionOfExperience) return Loaded.EXPERIENCE;
		if (potion instanceof PotionOfMindVision) return Loaded.MIND_VISION;
		if (potion instanceof PotionOfInvisibility) return Loaded.INVISIBILITY;
		return null;
	}

	private final WndBag.ItemSelector potionSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(SprayGun.this, "prompt_load");
		}

		@Override
		public Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag> preferredBag() {
			return PotionBandolier.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Potion && ((Potion)item).isKnown() && loadedFor((Potion)item) != null;
		}

		@Override
		public void onSelect(Item item) {
			if (item instanceof Potion) {
				loadPotion(curUser, (Potion)item);
			}
		}
	};

	private final CellSelector.Listener sprayer = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null && curUser instanceof Hero) {
				spray((Hero)curUser, target);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(SprayGun.this, "prompt");
		}
	};

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHARGES, charges);
		bundle.put(LOADED, loaded);
		bundle.put(CONCENTRATED, concentratedPotion);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		charges = bundle.contains(CHARGES) ? bundle.getInt(CHARGES) : BASE_CHARGES;
		if (bundle.contains(LOADED)) {
			loaded = bundle.getEnum(LOADED, Loaded.class);
		}
		concentratedPotion = bundle.getBoolean(CONCENTRATED);
		image = imageFor(loaded);
	}

	public static class HasteSprayCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1.0f, 0.8f, 0.0f);
        }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 5); }

    }
}
