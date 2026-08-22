package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.StealingChains;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.ChainShadowThiefSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

public class ChainShadowThief extends Thief {

	private static final String CHAIN_USED = "chain_used";

	private boolean chainUsed;

	{
		spriteClass = ChainShadowThiefSprite.class;

		HP = HT = 100;
		defenseSkill = 20;
		EXP = 13;
		maxLvl = 30;
		loot = null;
		lootChance = 0f;
        loot = Random.oneOf(Generator.Category.RING, Generator.Category.ARTIFACT);
        lootChance = 0.03f;

		WANDERING = new ChainWandering();
		HUNTING = new ChainHunting();
	}

    @Override
    public float lootChance() {
        //each drop makes future drops 1/3 as likely
        // so loot chance looks like: 1/33, 1/100, 1/300, 1/900, etc.
        return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.THEIF_MISC.count);
    }

	@Override
	protected float carryingSpeedMultiplier() {
		return 2f;
	}

	@Override
	protected boolean canStealOnAttack() {
		return true;
	}

	@Override
	protected Class<?> theftMessageClass() {
		return ChainShadowThief.class;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 40);
	}


	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return 0;
	}

	@Override
	protected boolean steal(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null
				|| item != null) {
			return false;
		}

		// Match the ordinary thief's melee theft target selection, while taking
		// the whole stack once the target has been selected.
		Item toSteal = Random.element(hero.belongings.backpack.items);
		if (toSteal == null || toSteal.unique || toSteal.level() >= 1) {
			return false;
		}

		return stealItem(hero, toSteal, false);
	}

	private Item chooseStealableItem(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null
				|| item != null) {
			return null;
		}

		ArrayList<Item> candidates = new ArrayList<>();
		for (Item candidate : hero.belongings.backpack.items) {
			if (!candidate.unique && candidate.level() < 1) {
				candidates.add(candidate);
			}
		}
		return Random.element(candidates);
	}

	protected boolean stealByChain(Hero hero, Item toSteal) {
		if (chainUsed) {
			return false;
		}

		boolean stolen = stealItem(hero, toSteal, true);
		if (stolen) {
			chainUsed = true;
		}
		return stolen;
	}

	private boolean stealItem(Hero hero, Item toSteal, boolean byChain) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null
				|| toSteal == null || item != null
				|| !hero.belongings.backpack.contains(toSteal)) {
			return false;
		}

		Class<?> messageClass = byChain ? theftMessageClass() : Thief.class;
		GLog.w(Messages.get(messageClass, "stole", toSteal.name()));
		if (!toSteal.stackable) {
			Dungeon.quickslot.convertToPlaceholder(toSteal);
		}
		Item.updateQuickslot();

		item = toSteal.detachAll(hero.belongings.backpack);
        /*
		if (item instanceof Honeypot) {
			item = ((Honeypot)item).shatter(this, pos);
		} else if (item instanceof Honeypot.ShatteredPot) {
			((Honeypot.ShatteredPot)item).pickupPot(this);
		}

         */

		return item != null;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHAIN_USED, chainUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		chainUsed = bundle.getBoolean(CHAIN_USED);
	}

	private ChainResult tryChainSteal() {
		Hero hero = Dungeon.hero;
		if (chainUsed || alignment != Alignment.ENEMY || hero == null
				|| !hero.isAlive() || hero.invisible > 0 || isCharmedBy(hero)
				|| fieldOfView == null || hero.pos < 0 || hero.pos >= fieldOfView.length
				|| !fieldOfView[hero.pos] || Dungeon.level.distance(pos, hero.pos) >= 4) {
			return ChainResult.NONE;
		}

		Item toSteal = chooseStealableItem(hero);
		if (toSteal == null) {
			return ChainResult.NONE;
		}

		Ballistica chain = new Ballistica(pos, hero.pos, Ballistica.PROJECTILE);
		if (chain.collisionPos != hero.pos || chain.path.size() < 2) {
			return ChainResult.NONE;
		}
		if (Dungeon.level != null && Dungeon.level.pit[chain.path.get(1)]) {
			return ChainResult.NONE;
		}

		spend(TICK);
		boolean animate = sprite != null && sprite.parent != null
				&& (sprite.visible || hero.sprite.visible);
		if (!animate) {
			return stealByChain(hero, toSteal) ? ChainResult.COMPLETE : ChainResult.NONE;
		}

		Sample.INSTANCE.play(Assets.Sounds.CHAINS);
		sprite.parent.add(new StealingChains(sprite.center(), hero.sprite.destinationCenter(),
				Effects.Type.CHAIN, new StealingChains.ItemProvider() {
			@Override
			public Item provide() {
				return stealByChain(hero, toSteal) ? item : null;
			}
		}, new Callback() {
			@Override
			public void call() {
				if (chainUsed) {
					state = FLEEING;
				}
				next();
			}
		}));
		return ChainResult.WAITING;
	}

	private enum ChainResult {
		NONE,
		COMPLETE,
		WAITING
	}

	private class ChainWandering extends Wandering {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			ChainResult result = tryChainSteal();
			if (result != ChainResult.NONE) {
				if (result == ChainResult.COMPLETE) state = FLEEING;
				return result == ChainResult.COMPLETE;
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}

	private class ChainHunting extends Hunting {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			ChainResult result = tryChainSteal();
			if (result != ChainResult.NONE) {
				if (result == ChainResult.COMPLETE) state = FLEEING;
				return result == ChainResult.COMPLETE;
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}
}
