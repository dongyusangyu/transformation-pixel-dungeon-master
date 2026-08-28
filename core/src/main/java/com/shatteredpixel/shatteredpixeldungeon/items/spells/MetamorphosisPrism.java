package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetamorphosisPrism extends Spell {

	private static final int WINDOW_WIDTH = 120;
	private static final int MAX_BOSS_CANDIDATES = 4;
	private static final int MAX_SUBCLASS_CANDIDATES = 3;

	{
		image = EXItemSpriteSheet.META_INFUSE;
	}

	@Override
	protected void onCast(Hero hero) {
		ArrayList<LinkedHashMap<Talent, Integer>> talents = ownedMetamorphosisTalents(hero);
		if (!hasTalents(talents)) {
			GLog.w(Messages.get(this, "no_talents"));
			return;
		}
		GameScene.show(new WndBossTalentChoose(this, talents));
	}

	static List<Talent> eligibleBossTalents(Talent replacing) {
		ArrayList<Talent> candidates = new ArrayList<>();
		Talent slot = Talent.bossTalentSlot(replacing);
		if (slot == null) {
			return candidates;
		}
		for (Talent talent : Talent.values()) {
			if (talent != replacing
					&& Talent.isBossTalent(talent)
					&& Talent.bossTalentSlot(talent) == slot
					&& !Talent.forbiddenInCatalogOrMetamorphosis(talent)) {
				candidates.add(talent);
			}
		}
		return candidates;
	}

	static List<Talent> chooseBossCandidates(Talent replacing, int limit) {
		List<Talent> candidates = eligibleBossTalents(replacing);
		Random.shuffle(candidates);
		return new ArrayList<>(candidates.subList(0, Math.min(limit, candidates.size())));
	}

	static List<Talent> eligibleSubclassTalents(
			Talent replacing, Collection<Talent> ownedTalents) {
		ArrayList<Talent> candidates = new ArrayList<>();
		List<Talent> pool = Talent.subclassTalentPool(replacing);
		if (!subclassPoolSupportsMetamorphosis(pool)) {
			return candidates;
		}
		for (Talent talent : pool) {
			if (!ownedTalents.contains(talent)
					&& !Talent.forbiddenInCatalogOrMetamorphosis(talent)) {
				candidates.add(talent);
			}
		}
		return candidates;
	}

	static boolean subclassPoolSupportsMetamorphosis(Collection<Talent> pool) {
		return pool != null && pool.size() > 3;
	}

	static List<Talent> chooseSubclassCandidates(
			Talent replacing, Collection<Talent> ownedTalents, int limit) {
		List<Talent> candidates = eligibleSubclassTalents(replacing, ownedTalents);
		Random.shuffle(candidates);
		return new ArrayList<>(candidates.subList(0, Math.min(limit, candidates.size())));
	}

	static LinkedHashMap<Talent, Integer> replaceInTier(
			LinkedHashMap<Talent, Integer> tier, Talent replacing, Talent replacement) {
		LinkedHashMap<Talent, Integer> result = new LinkedHashMap<>();
		for (Talent talent : tier.keySet()) {
			if (talent == replacing) {
				result.put(replacement, tier.get(talent));
			} else {
				result.put(talent, tier.get(talent));
			}
		}
		return result;
	}

	private static Set<Talent> ownedTalents(Hero hero) {
		HashSet<Talent> owned = new HashSet<>();
		for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
			owned.addAll(tier.keySet());
		}
		return owned;
	}

	private static ArrayList<LinkedHashMap<Talent, Integer>> ownedMetamorphosisTalents(Hero hero) {
		ArrayList<LinkedHashMap<Talent, Integer>> result = new ArrayList<>();
		Set<Talent> owned = ownedTalents(hero);
		for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
			LinkedHashMap<Talent, Integer> selectableTalents = new LinkedHashMap<>();
			for (Talent talent : tier.keySet()) {
				if (Talent.isBossTalent(talent) && !Talent.isBossTalentPlaceholder(talent)) {
					selectableTalents.put(talent, tier.get(talent));
				} else if (talent.tier() == 3
						&& talent.type() == Talent.TalentType.SUBCLASS
						&& !eligibleSubclassTalents(talent, owned).isEmpty()) {
					selectableTalents.put(talent, tier.get(talent));
				}
			}
			result.add(selectableTalents);
		}
		return result;
	}

	private static boolean hasTalents(ArrayList<LinkedHashMap<Talent, Integer>> talents) {
		for (LinkedHashMap<Talent, Integer> tier : talents) {
			if (!tier.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static int talentTier(Hero hero, Talent talent) {
		for (int i = 0; i < hero.talents.size(); i++) {
			if (hero.talents.get(i).containsKey(talent)) {
				return i + 1;
			}
		}
		return -1;
	}

	public static boolean replaceBossTalent(Hero hero, Talent replacing, Talent replacement) {
		int tier = talentTier(hero, replacing);
		Talent slot = Talent.bossTalentSlot(replacing);
		if (tier < 1 || slot == null || Talent.bossTalentSlot(replacement) != slot) {
			return false;
		}

		if (replacing == Talent.SMOKE_MASK) {
			Talent.SmokeMask smokeMask = hero.buff(Talent.SmokeMask.class);
			if (smokeMask != null) {
				smokeMask.detach();
			}
		}
		if (replacing == Talent.NATURAL_CHILD) {
			Talent.NaturalChildAction action = hero.buff(Talent.NaturalChildAction.class);
			if (action != null) action.detach();
			Talent.NaturalChildBarkskin barkskin = hero.buff(Talent.NaturalChildBarkskin.class);
			if (barkskin != null) barkskin.detach();
			Talent.NaturalChildCooldown cooldown = hero.buff(Talent.NaturalChildCooldown.class);
			if (cooldown != null) cooldown.detach();
		}

		LinkedHashMap<Talent, Integer> currentTier = hero.talents.get(tier - 1);
		hero.talents.set(tier - 1, replaceInTier(currentTier, replacing, replacement));
		hero.sublimationTalents.remove(replacing);
		hero.sublimationTalents.put(slot, replacement.name());
		WndHero.lastIdx = 1;
		return true;
	}

	public static boolean replaceSubclassTalent(Hero hero, Talent replacing, Talent replacement) {
		int tier = talentTier(hero, replacing);
		List<Talent> pool = Talent.subclassTalentPool(replacing);
		if (tier != 3 || !subclassPoolSupportsMetamorphosis(pool) || !pool.contains(replacement)
				|| ownedTalents(hero).contains(replacement)
				|| Talent.forbiddenInCatalogOrMetamorphosis(replacement)) {
			return false;
		}

		LinkedHashMap<Talent, Integer> currentTier = hero.talents.get(tier - 1);
		hero.talents.set(tier - 1, replaceInTier(currentTier, replacing, replacement));
		recordMetamorphosis(hero.metamorphedTalents, replacing, replacement);
		WndHero.lastIdx = 1;
		return true;
	}

	static void recordMetamorphosis(
			LinkedHashMap<Talent, Talent> replacements,
			Talent replacing, Talent replacement) {
		Talent original = replacing;
		for (Map.Entry<Talent, Talent> entry : replacements.entrySet()) {
			if (entry.getValue() == replacing) {
				original = entry.getKey();
				break;
			}
		}
		if (original == replacement) {
			replacements.remove(original);
		} else {
			replacements.put(original, replacement);
		}
	}

	private static boolean replaceTalent(Hero hero, Talent replacing, Talent replacement) {
		if (Talent.isBossTalent(replacing)) {
			return replaceBossTalent(hero, replacing, replacement);
		}
		return replaceSubclassTalent(hero, replacing, replacement);
	}

	public static void chooseReplacement(MetamorphosisPrism prism, Talent replacing, int tier) {
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) {
			return;
		}
		List<Talent> candidates;
		if (Talent.isBossTalent(replacing)) {
			candidates = chooseBossCandidates(replacing, MAX_BOSS_CANDIDATES);
		} else {
			candidates = chooseSubclassCandidates(
					replacing, ownedTalents(hero), MAX_SUBCLASS_CANDIDATES);
		}
		if (candidates.isEmpty()) {
			GLog.w(Messages.get(prism, "no_candidates"));
			return;
		}
		if (WndBossTalentChoose.INSTANCE != null) {
			WndBossTalentChoose.INSTANCE.hide();
		}
		GameScene.show(new WndBossTalentReplace(prism, replacing, tier, candidates));
	}

	public static void completeReplacement(
			MetamorphosisPrism prism, Talent replacing, Talent replacement) {
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()
				|| prism.quantity() <= 0
				|| !hero.belongings.backpack.contains(prism)
				|| !replaceTalent(hero, replacing, replacement)) {
			return;
		}

		if (WndBossTalentReplace.INSTANCE != null) {
			WndBossTalentReplace.INSTANCE.hide();
		}
		prism.detach(hero.belongings.backpack);
		hero.spend(1f);
		hero.busy();
		hero.sprite.operate(hero.pos);
		hero.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
		Sample.INSTANCE.play(Assets.Sounds.READ);
		Invisibility.dispel();
		Transmuting.show(hero, replacing, replacement);

		Catalog.countUse(MetamorphosisPrism.class);
		TalentCatalog.countUse(replacement);
		Statistics.metamorphosis++;
		Badges.validateFreemanUnlock();
		Badges.validateTalent(replacement);
		Talent.onTalentUpgraded(hero, replacement);
		Talent.onScrollUsed(hero, hero.pos, prism.talentFactor, MetamorphosisPrism.class);
		updateQuickslot();
	}

	private void confirmCancelation(Window chooseWindow) {
		GameScene.show(new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(MetamorphosisPrism.class, "cancel_warn"),
				Messages.get(InventoryScroll.class, "yes"),
				Messages.get(InventoryScroll.class, "no")) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					Hero hero = Dungeon.hero;
					if (hero != null && hero.belongings.backpack.contains(MetamorphosisPrism.this)) {
						MetamorphosisPrism.this.detach(hero.belongings.backpack);
						hero.spendAndNext(1f);
						Catalog.countUse(MetamorphosisPrism.class);
						updateQuickslot();
					}
					chooseWindow.hide();
				}
			}

			@Override
			public void onBackPressed() {
			}
		});
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public int energyVal() {
		return 0;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs = new Class[]{ScrollOfMetamorphosis.class};
			inQuantity = new int[]{3};
			cost = 3;
			output = MetamorphosisPrism.class;
			outQuantity = 1;
		}
	}

	public static class WndBossTalentChoose extends Window {

		public static WndBossTalentChoose INSTANCE;
		public final MetamorphosisPrism prism;
		private final TalentsPane pane;

		public WndBossTalentChoose(
				MetamorphosisPrism prism,
				ArrayList<LinkedHashMap<Talent, Integer>> talents) {
			INSTANCE = this;
			this.prism = prism;

			IconTitle title = new IconTitle(prism);
			title.color(TITLE_COLOR);
			title.setRect(0, 0, WINDOW_WIDTH, 0);
			add(title);

			RenderedTextBlock text = PixelScene.renderTextBlock(
					Messages.get(MetamorphosisPrism.class, "choose_desc"), 6);
			text.maxWidth(WINDOW_WIDTH);
			text.setPos(0, title.bottom() + 2);
			add(text);

			pane = new TalentsPane(TalentButton.Mode.BOSS_METAMORPH_CHOOSE, talents);
			add(pane);
			pane.setPos(0, text.bottom() + 2);
			pane.setSize(WINDOW_WIDTH, pane.content().height());
			resize(WINDOW_WIDTH, (int) pane.bottom());
			pane.setPos(0, text.bottom() + 2);
		}

		@Override
		public void hide() {
			super.hide();
			if (INSTANCE == this) {
				INSTANCE = null;
			}
		}

		@Override
		public void offset(int xOffset, int yOffset) {
			super.offset(xOffset, yOffset);
			pane.setPos(pane.left(), pane.top());
		}
	}

	public static class WndBossTalentReplace extends Window {

		public static WndBossTalentReplace INSTANCE;
		public final MetamorphosisPrism prism;
		public final Talent replacing;

		public WndBossTalentReplace(
				MetamorphosisPrism prism,
				Talent replacing,
				int tier,
				List<Talent> candidates) {
			INSTANCE = this;
			this.prism = prism;
			this.replacing = replacing;

			IconTitle title = new IconTitle(prism);
			title.color(TITLE_COLOR);
			title.setRect(0, 0, WINDOW_WIDTH, 0);
			add(title);

			RenderedTextBlock text = PixelScene.renderTextBlock(
					Messages.get(MetamorphosisPrism.class,
							Talent.isBossTalent(replacing)
									? "replace_boss_desc"
									: "replace_subclass_desc"), 6);
			text.maxWidth(WINDOW_WIDTH);
			text.setPos(0, title.bottom() + 2);
			add(text);

			LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
			int points = Dungeon.hero.pointsInTalent(replacing);
			for (Talent candidate : candidates) {
				options.put(candidate, points);
				TalentCatalog.countAppearance(candidate);
			}

			TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(
					options, tier, TalentButton.Mode.BOSS_METAMORPH_REPLACE);
			add(optionsPane);
			optionsPane.title.text(" ");
			optionsPane.setPos(0, text.bottom() + 2);
			optionsPane.setSize(WINDOW_WIDTH, optionsPane.height());
			resize(WINDOW_WIDTH, (int) optionsPane.bottom());
		}

		@Override
		public void hide() {
			super.hide();
			if (INSTANCE == this) {
				INSTANCE = null;
			}
		}

		@Override
		public void onBackPressed() {
			prism.confirmCancelation(this);
		}
	}
}
