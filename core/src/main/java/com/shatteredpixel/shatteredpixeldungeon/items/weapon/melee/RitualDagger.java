package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Suffering;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class RitualDagger extends MeleeWeapon {

	public static final String AC_ATTACK = "ATTACK";
	public static final String AC_STAB = "STAB";

	private static final int KILLS_TO_RITUAL = 13;
	private static final String KILLS = "kills";

	private int kills = 0;
	private boolean ritualStab = false;
    @Override
    public boolean isUpgradable() {
        return false;
    }



    {
		image = ItemSpriteSheet.RITUAL_DAGGER;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;
		tier = 1;
		defaultAction = AC_ATTACK;
		usesTargeting = true;
		unique = true;
		bones = false;
		levelKnown = true;
        DLY = 0.5f;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);
		actions.add(AC_ATTACK);
		if (hero.subClass.is(HeroSubClass.PIOUS)) {
			actions.add(AC_STAB);
		}
		return actions;
	}

	@Override
	public String defaultAction() {
		return AC_ATTACK;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_ATTACK)) {
			return Messages.upperCase(Messages.get(this, "ac_attack"));
		} else if (action.equals(AC_STAB)) {
			return Messages.upperCase(Messages.get(this, "ac_stab"));
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		curUser = hero;
		curItem = this;
		if (action.equals(AC_ATTACK)) {
			GameScene.selectCell(attacker);
		} else if (action.equals(AC_STAB)) {
			if (hero.buff(Reason.class) == null) {
				GLog.w(Messages.get(this, "no_reason_buff"));
				return;
			}
			if (!ritualReady()) {
				GLog.w(Messages.get(this, "not_ready"));
				return;
			}
			GameScene.selectCell(stabber);
		}
	}

	@Override
	public boolean doEquip(Hero hero) {
		return false;
	}

	@Override
	public int min(int lvl) {
		return 1 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 10 + 3 * lvl;
	}





	@Override
	public int level() {
		return baseLevel();
	}

	@Override
	public int buffedLvl() {
		return super.buffedLvl();
	}

	private int baseLevel() {
		return hero == null ? 0 : hero.lvl / 5;
	}
    @Override
    public int STRReq(int lvl) {
        return STRReq(1, lvl); //tier 1
    }



	@Override
	public String info() {
        String info = Messages.get(this, ritualReady() ? "desc_ready" : "desc");

        if(hero==null) return info;
		info += "\n\n" + Messages.get(this, "stats", augment.damageFactor(min()), augment.damageFactor(max()), STRReq());


        if (STRReq() > hero.STR()) {
            info += " " + Messages.get(Weapon.class, "too_heavy");
        } else if (hero.STR() > STRReq()){
            info += " " + Messages.get(Weapon.class, "excess_str", hero.STR() - STRReq());
        }


        info += "\n\n" + Messages.get(this, "fast");
		info += Messages.get(this, "no_equip");
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

        if (isEquipped(Dungeon.hero) && !hasCurseEnchant() && Dungeon.hero.buff(HolyWeapon.HolyWepBuff.class) != null
                && (Dungeon.hero.subClass != HeroSubClass.PALADIN || enchantment == null)){
            info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", Messages.get(HolyWeapon.class, "ench_name", Messages.get(Enchantment.class, "enchant"))));
            info += " " + Messages.get(HolyWeapon.class, "ench_desc");
        } else if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
            if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
            info += " " + enchantment.desc();
        } else if (enchantHardened){
            info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
        }
		return info;
	}

	public boolean ritualReady() {
		return kills >= KILLS_TO_RITUAL;
	}

	public int killsToRitual() {
		return Math.max(0, KILLS_TO_RITUAL - kills);
	}

	public void onKill(Hero hero) {
		if (!hero.subClass.is(HeroSubClass.PIOUS) || ritualReady()) {
			return;
		}
		kills++;
		if (ritualReady()) {
			Buff.detach(hero, RitualTracker.class);
			GLog.p(Messages.get(this, "ready"));
			ActionIndicator.refresh();
			updateQuickslot();
		}
	}

	public void resetRitual(Hero hero) {
		kills = 0;
		if (hero != null && hero.subClass.is(HeroSubClass.PIOUS)) {
			Buff.affect(hero, RitualTracker.class);
			ActionIndicator.refresh();
		}
		updateQuickslot();
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		boolean picked = super.doPickUp(hero, pos);
		if (picked) {
			resetRitual(hero);
		}
		return picked;
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		if (hero != null) {
			Buff.detach(hero, RitualTracker.class);
			ActionIndicator.refresh();
		}
	}

	@Override
	public void onThrow(int cell) {
		if (hero != null) {
			resetRitual(null);
			Buff.detach(hero, RitualTracker.class);
		}
		super.onThrow(cell);
	}

	private void attackTarget(Hero hero, Char target, boolean special) {
		ritualStab = special;
		hero.sprite.attack(target.pos, () -> {
			hero.belongings.abilityWeapon = this;
			try {
				float delay = hero.attackDelay();

				boolean hadBloodGift = target.buff(BloodGift.class) != null;
				int preHP = target.HP + target.shielding();
				if (special) {
					boolean attacked = hero.attack(target, 1, 0, Char.INFINITE_ACCURACY);
					if(attacked){
						int damage = Math.max(0, preHP - (target.HP + target.shielding()));
						if (hero.subClass.is(HeroSubClass.PIOUS)) {
							BloodGift.applyTo(target);
							if (!hadBloodGift) {
                                if(target.isAlive()){
                                    BloodGift.restoreReason(hero, target, damage);
                                }else{
                                    BloodGift.restoreReason(hero, target, preHP);
                                }

							}
						}
						Reason.gainReason(hero, 50);
						GLog.w(Messages.get(RitualDagger.this, "stab_enemy"));
					}
					resetRitual(hero);
					hero.next();
				} else {
					boolean attacked = hero.attack(target);
					if (attacked && hero.subClass.is(HeroSubClass.PIOUS)) {
						int damage = Math.max(0, preHP - (target.HP + target.shielding()));
						BloodGift.applyTo(target);
						if (!hadBloodGift) {
                            if(target.isAlive()){
                                BloodGift.restoreReason(hero, target, damage);
                            }else{
                                BloodGift.restoreReason(hero, target, preHP);
                            }
						}
					}
                    if (hero.buff(Talent.LethalMomentumTracker.class) != null){
                        hero.buff(Talent.LethalMomentumTracker.class).detach();
                        hero.next();
                    }else{
                        hero.spendAndNext(delay);
                    }

				}
			} finally {
				ritualStab = false;
				hero.belongings.abilityWeapon = null;
			}

		});
		hero.busy();
        Invisibility.dispel();
	}


	private final CellSelector.Listener attacker = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || !(curUser instanceof Hero)) return;
			Hero hero = (Hero)curUser;
			Char ch = Actor.findChar(target);
			if (ch == null || ch == hero || ch.alignment != Char.Alignment.ENEMY || hero.isCharmedBy( ch )) {
				GLog.w(Messages.get(RitualDagger.this, "no_target"));
				return;
			}
			if (!canReach(hero, target)) {
				GLog.w(Messages.get(RitualDagger.this, "too_far"));
				return;
			}
			attackTarget(hero, ch, false);
		}

		@Override
		public String prompt() {
			return Messages.get(RitualDagger.this, "prompt_attack");
		}
	};
    @Override
    public float accuracyFactor(Char owner, Char target) {

        int encumbrance = 0;

        if( owner instanceof Hero ){
            encumbrance = STRReq() - ((Hero)owner).STR();
            if(hero.hasTalent(Talent.FALSEHOOD_POWER)){
                encumbrance = Math.max(0, encumbrance - hero.pointsInTalent(Talent.FALSEHOOD_POWER)-2);
            }
        }

        float ACC = this.ACC;

        if (owner.buff(Wayward.WaywardBuff.class) != null && enchantment instanceof Wayward){
            ACC /= 5;
        }


        return encumbrance > 0 ? (float)(ACC / Math.pow( 1.5, encumbrance )) : ACC;
    }

    @Override
    public float delayFactor( Char owner ) {
        return baseDelay(owner) * (1f/speedMultiplier(owner));
    }

	private final CellSelector.Listener stabber = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || !(curUser instanceof Hero) ) return;
			Hero hero = (Hero)curUser;
			if (target == hero.pos) {
				stabSelf(hero);
				return;
			}
			Char ch = Actor.findChar(target);
			if (ch == null || ch == hero || ch.alignment != Char.Alignment.ENEMY || !canReach(hero, target) ) {
				GLog.w(Messages.get(RitualDagger.this, "bad_stab_target"));
				return;
			}

			attackTarget(hero, ch, true);

		}

		@Override
		public String prompt() {
			return Messages.get(RitualDagger.this, "prompt_stab");
		}
	};

	public void stabSelf(Hero hero) {
		if (!ritualReady()) {
			GLog.w(Messages.get(this, "not_ready"));
			return;
		}
		hero.sprite.operate(hero.pos);
		hero.sprite.emitter().burst(ShadowParticle.CURSE, 6);
		Sample.INSTANCE.play(Assets.Sounds.CURSED);
		GLog.w(Messages.get(this, "stab_self"));
		Reason.loseReason(hero, 100);
		hero.damage(1, this, DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
		if (!hero.isAlive()) {
			Dungeon.fail(this);
			GLog.n(Messages.get(this, "ondeath"));
		}
		resetRitual(hero);
		hero.next();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KILLS, kills);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		kills = bundle.getInt(KILLS);
	}
    @Override
    public int damageRoll(Char owner) {
        int damage = super.damageRoll(owner);

        if (owner instanceof Hero) {
            int exStr = ((Hero)owner).STR() - STRReq();
            if (exStr > 0) {
                damage += Hero.heroDamageIntRange( 0, exStr );
            }
            if(((Hero)owner).hasTalent(Talent.STRENGTH_GREATEST)){
                damage += Hero.heroDamageIntRange( 0, ((Hero)owner).pointsInTalent(Talent.STRENGTH_GREATEST) );
            }
        }

        return ritualStab ? damage * 4 : damage;
    }

	public static class RitualTracker extends Buff {
		{
			type = buffType.POSITIVE;
            revivePersists = true;
		}

		@Override
		public int icon() {
			return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.RITUAL;
		}

		@Override
		public String desc() {
			RitualDagger dagger = hero == null ? null : hero.belongings.getItem(RitualDagger.class);
			return Messages.get(this, "desc", dagger == null ? KILLS_TO_RITUAL : dagger.killsToRitual());
		}
	}

	public static class BloodGift extends Buff {
		{
			type = buffType.NEGATIVE;
		}

		public static void applyTo(Char target) {
			if (target == null || target.buff(BloodGift.class) != null) {
				return;
			}
			Buff.affect(target, BloodGift.class);
			if (target.sprite != null) {
				target.sprite.showStatus(CharSprite.ORANGE, Messages.get(BloodGift.class, "name"));
			}
		}

		public static void onPiousAttackDamage(Char attacker, Char target, int damage) {
			if (attacker instanceof Hero
					&& ((Hero) attacker).subClass.is(HeroSubClass.PIOUS)
					&& target != null
					&& target.buff(BloodGift.class) != null) {
				restoreReason((Hero) attacker, target, Math.max(1, damage));
			}
		}

		public static void restoreReason(Hero hero, Char target, int damage) {
			if (hero == null || target == null || damage <= 0 || target.buff(BloodGift.class) == null) {
				return;
			}
			Reason.gainReason(hero, Math.max(1, damage / 5));
		}

		@Override
		public int icon() {
			return BuffIndicator.RITUAL;
		}

		@Override
		public boolean act() {
			diactivate();
			return true;
		}
	}
}
