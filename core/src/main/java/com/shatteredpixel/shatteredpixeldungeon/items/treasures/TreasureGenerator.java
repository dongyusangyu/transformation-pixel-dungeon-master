package com.shatteredpixel.shatteredpixeldungeon.items.treasures;

import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

@SuppressWarnings("unchecked")
public final class TreasureGenerator {

	private static final Class<? extends Treasures>[] TOP = new Class[]{
			GoryeoMaebyeong.class, JavaneseGoldCup.class,
			EthiopianProcessionalCross.class, GreatZimbabweBird.class, DjenneTerracottaFigure.class
	};

	private static final Class<? extends Treasures>[] RARE = new Class[]{
			LewisChessQueen.class, HarbavilleTriptych.class,
			AlMughiraPyxis.class, BlacasEwer.class, GreatKhanPaiza.class
	};

	private static final Class<? extends Treasures>[] UNCOMMON = new Class[]{
			CholaNataraja.class,
			DojigiriYasutsuna.class,
			TurquoiseSerpent.class,
			RuWareBowl.class,
			IncaGoldenLlama.class
	};

	private static final Class<? extends Treasures>[] COMMON = new Class[]{
			MuiscaGoldenRaft.class, ImperialCrown.class, PakalJadeMask.class,
			SuttonHooHelmet.class, BookOfKells.class
	};

	private TreasureGenerator() {
	}

	public static Treasures random() {
		Class<? extends Treasures>[] classes =
				classesForInternal(collectionRarityForRoll(Random.Float()));
		return Reflection.newInstance(classes[Random.Int(classes.length)]);
	}

	static Treasures.CollectionRarity collectionRarityForRoll(float roll) {
		// With mean base values 930/2000/4000/5000 and existing quality rolls,
		// 3.5 collectibles per raid retain an expected total value of 5001.06075.
		if (roll < 0.01f) {
			return Treasures.CollectionRarity.TOP;
		} else if (roll < 0.05f) {
			return Treasures.CollectionRarity.RARE;
		} else if (roll < 0.15f) {
			return Treasures.CollectionRarity.UNCOMMON;
		} else {
			return Treasures.CollectionRarity.COMMON;
		}
	}

	static Class<? extends Treasures>[] classesFor(
			Treasures.CollectionRarity collectionRarity) {
		return classesForInternal(collectionRarity).clone();
	}

	private static Class<? extends Treasures>[] classesForInternal(
			Treasures.CollectionRarity collectionRarity) {
		switch (collectionRarity) {
			case TOP:
				return TOP;
			case RARE:
				return RARE;
			case UNCOMMON:
				return UNCOMMON;
			case COMMON:
			default:
				return COMMON;
		}
	}
}
