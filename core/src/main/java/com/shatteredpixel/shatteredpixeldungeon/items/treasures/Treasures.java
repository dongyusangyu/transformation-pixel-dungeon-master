package com.shatteredpixel.shatteredpixeldungeon.items.treasures;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.Locale;

public class Treasures extends Item {

	private static final String RARITY = "rarity";
	private static final int TOP_LEGENDARY_PREMIUM = 4_950_000;

	private Rarity rarity;
	private final CollectionRarity collectionRarity;
	private final int baseValue;

	{
		stackable = false;
	}

	public Treasures() {
		this(ItemSpriteSheet.CHEST, CollectionRarity.COMMON, 0,
				rarityForRoll(Random.Float()));
	}

	protected Treasures(int image, CollectionRarity collectionRarity, int baseValue) {
		this(image, collectionRarity, baseValue, rarityForRoll(Random.Float()));
	}

	Treasures(int image, CollectionRarity collectionRarity,
			  int baseValue, Rarity rarity) {
		this.image = image;
		this.collectionRarity = collectionRarity;
		this.baseValue = baseValue;
		this.rarity = rarity;
	}

	public Rarity rarity() {
		return rarity;
	}

	public CollectionRarity collectionRarity() {
		return collectionRarity;
	}

	public Treasures setRarity(Rarity rarity) {
		if (rarity == null) {
			throw new IllegalArgumentException("rarity cannot be null");
		}
		this.rarity = rarity;
		return this;
	}

	static Rarity rarityForRoll(float roll) {
		if (roll < 0.001f) {
			return Rarity.LEGENDARY;
		} else if (roll < 0.011f) {
			return Rarity.EPIC;
		} else if (roll < 0.111f) {
			return Rarity.RARE;
		} else {
			return Rarity.COMMON;
		}
	}

	static Rarity rarityFromStoredName(String name) {
		if (name != null) {
			try {
				return Rarity.valueOf(name);
			} catch (IllegalArgumentException ignored) {
				// Old or damaged saves should still load the item safely.
			}
		}
		return Rarity.COMMON;
	}

	static int qualityMultiplier(Rarity rarity) {
		switch (rarity) {
			case LEGENDARY:
				return 10;
			case EPIC:
				return 5;
			case RARE:
				return 2;
			case COMMON:
			default:
				return 1;
		}
	}

	public String rarityName() {
		return Messages.get(Treasures.class,
				"quality_" + rarity.name().toLowerCase(Locale.ENGLISH));
	}

	public String collectionRarityName() {
		return Messages.get(Treasures.class, "collection_rarity_"
				+ collectionRarity.name().toLowerCase(Locale.ENGLISH));
	}

	public String journalDesc() {
		return super.desc()
				+ "\n\n" + Messages.get(Treasures.class,
						"collection_rarity", collectionRarityName());
	}

	@Override
	public String desc() {
		return journalDesc()
				+ "\n" + Messages.get(Treasures.class, "quality", rarityName())
				+ "\n" + Messages.get(Treasures.class, "value", value());
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		int singleUnitValue = baseValue * qualityMultiplier(rarity);
		if (collectionRarity == CollectionRarity.TOP && rarity == Rarity.LEGENDARY) {
			singleUnitValue += TOP_LEGENDARY_PREMIUM;
		}
		long totalValue = (long) singleUnitValue * Math.max(0, quantity);
		return totalValue >= Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: (int) totalValue;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(RARITY, rarity.name());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		rarity = rarityFromStoredName(bundle.contains(RARITY)
				? bundle.getString(RARITY)
				: null);
	}

	public enum Rarity {
		LEGENDARY,
		EPIC,
		RARE,
		COMMON
	}

	public enum CollectionRarity {
		TOP,
		RARE,
		UNCOMMON,
		COMMON
	}
}
