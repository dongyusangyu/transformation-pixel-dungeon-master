package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TowerMobPlacer extends TestItem {

	static final int MOBS_PER_PAGE = 15;
	static final int GRID_COLUMNS = 5;

	private static final String AC_PLACE = "place";
	private static final String AC_SET = "set";
	private static final String PAGE = "page";
	private static final String MOB_INDEX = "mob_index";
	private static final String ELITE_OPTIONS = "elite_options";

	private static final List<Class<? extends Mob>> TOWER_MOBS = towerMobsFromBestiary();

	private static final List<Class<? extends ChampionEnemy>> ELITE_BUFFS =
			Collections.unmodifiableList(Arrays.asList(
					ChampionEnemy.Blazing.class,
					ChampionEnemy.AntiMagic.class,
					ChampionEnemy.Blessed.class,
					ChampionEnemy.Giant.class,
					ChampionEnemy.Growing.class,
					ChampionEnemy.Projecting.class,
					ChampionEnemy.Corrosion.class,
					ChampionEnemy.Haste.class,
					ChampionEnemy.Holy.class,
					ChampionEnemy.Transform.class
			));

	private int page;
	private int mobIndex;
	private int eliteOptions;

	{
		image = ItemSpriteSheet.MOB_HOLDER;
		defaultAction = AC_PLACE;
	}

	private static List<Class<? extends Mob>> towerMobsFromBestiary() {
		ArrayList<Class<? extends Mob>> mobs = new ArrayList<>();
		for (Class<?> entity : Bestiary.TOWER_MOBS.entities()) {
			if (Mob.class.isAssignableFrom(entity)) {
				mobs.add(entity.asSubclass(Mob.class));
			}
		}
		return Collections.unmodifiableList(mobs);
	}

	static int mobCount() {
		return TOWER_MOBS.size();
	}

	static int pageCount() {
		return Math.max(1, (mobCount() + MOBS_PER_PAGE - 1) / MOBS_PER_PAGE);
	}

	static int gridRowsForCount(int count) {
		return Math.max(1, (count + GRID_COLUMNS - 1) / GRID_COLUMNS);
	}

	static List<Class<? extends Mob>> mobsOnPage(int page) {
		if (page < 0 || page >= pageCount()) {
			return Collections.emptyList();
		}
		int from = page * MOBS_PER_PAGE;
		if (from >= mobCount()) {
			return Collections.emptyList();
		}
		int to = Math.min(from + MOBS_PER_PAGE, mobCount());
		return Collections.unmodifiableList(TOWER_MOBS.subList(from, to));
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_PLACE);
		actions.add(AC_SET);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_PLACE.equals(action)) {
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					if (cell != null) {
						if (canPlaceMob(cell)) {
							placeMob(cell);
						} else {
							GLog.w(M.L(TowerMobPlacer.class, "forbidden"));
						}
					}
					curUser.next();
				}

				@Override
				public String prompt() {
					return M.L(TowerMobPlacer.class, "prompt");
				}
			});
		} else if (AC_SET.equals(action)) {
			GameScene.show(new WndSetMob());
		}
	}

	private void placeMob(int cell) {
		try {
			Mob mob = Reflection.newInstance(selectedMobClass());
			applyInitialState(mob);
			mob.pos = cell;
			GameScene.add(mob);
			for (int i = 0; i < ELITE_BUFFS.size(); i++) {
				if ((eliteOptions & (1 << i)) != 0) {
					Buff.affect(mob, ELITE_BUFFS.get(i));
				}
			}
			com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.appear(mob, cell);
			Dungeon.level.occupyCell(mob);
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	static void applyInitialState(Mob mob) {
		mob.state = mob instanceof MimicCrocodile ? mob.WANDERING : mob.SLEEPING;
	}

	private Class<? extends Mob> selectedMobClass() {
		normalizeSelection();
		return TOWER_MOBS.get(mobIndex);
	}

	private boolean canPlaceMob(int cell) {
		return Actor.findChar(cell) == null
				&& (!Dungeon.level.solid[cell]
				|| Dungeon.level.map[cell] == Terrain.DOOR
				|| Dungeon.level.map[cell] == Terrain.OPEN_DOOR);
	}

	private void normalizeSelection() {
		page = Math.max(0, Math.min(page, pageCount() - 1));
		int firstOnPage = page * MOBS_PER_PAGE;
		int lastOnPage = Math.min(firstOnPage + MOBS_PER_PAGE, mobCount()) - 1;
		if (mobIndex < firstOnPage || mobIndex > lastOnPage) {
			mobIndex = firstOnPage;
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PAGE, page);
		bundle.put(MOB_INDEX, mobIndex);
		bundle.put(ELITE_OPTIONS, eliteOptions);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		page = bundle.getInt(PAGE);
		mobIndex = bundle.getInt(MOB_INDEX);
		eliteOptions = bundle.getInt(ELITE_OPTIONS);
		normalizeSelection();
	}

	private class WndSetMob extends Window {

		private static final int WIDTH = 140;
		private static final int INITIAL_HEIGHT = 118;
		private static final int BUTTON_SIZE = 16;
		private static final int GAP = 1;
		private static final int GRID_TOP = 30;
		private static final int GRID_ROW_GAP = 2;

		private final ArrayList<IconButton> mobButtons = new ArrayList<>();
		private final ArrayList<CheckBox> eliteChecks = new ArrayList<>(ELITE_BUFFS.size());
		private final RenderedTextBlock selectedPage;
		private final RenderedTextBlock selectedMob;

		WndSetMob() {
			resize(WIDTH, INITIAL_HEIGHT);

			RedButton previous = new RedButton("<<<", 8) {
				@Override
				public void onClick() {
					page = (page + pageCount() - 1) % pageCount();
					mobIndex = page * MOBS_PER_PAGE;
					refreshMobButtons();
				}
			};
			previous.setRect(GAP, GAP, 24, 18);
			add(previous);

			RedButton next = new RedButton(">>>", 8) {
				@Override
				public void onClick() {
					page = (page + 1) % pageCount();
					mobIndex = page * MOBS_PER_PAGE;
					refreshMobButtons();
				}
			};
			next.setRect(WIDTH - 24 - GAP, GAP, 24, 18);
			add(next);

			selectedPage = PixelScene.renderTextBlock("", 9);
			PixelScene.align(selectedPage);
			add(selectedPage);

			selectedMob = PixelScene.renderTextBlock("", 9);
			selectedMob.hardlight(0xFFFF44);
			PixelScene.align(selectedMob);
			add(selectedMob);

			for (int i = 0; i < ELITE_BUFFS.size(); i++) {
				CheckBox check = new CheckBox(M.L(MobPlacer.class, "elite_name" + i));
				check.checked((eliteOptions & (1 << i)) != 0);
				add(check);
				eliteChecks.add(check);
			}

			createMobButtons();
			updateTexts();
			layoutWindow();
		}

		private void createMobButtons() {
			List<Class<? extends Mob>> pageMobs = mobsOnPage(page);
			for (int i = 0; i < pageMobs.size(); i++) {
				final int selectedIndex = page * MOBS_PER_PAGE + i;
				IconButton button = new IconButton() {
					@Override
					public void onClick() {
						super.onClick();
						mobIndex = selectedIndex;
						updateTexts();
					}
				};
				CharSprite sprite = Reflection.newInstance(pageMobs.get(i)).sprite();
				button.icon(sprite);
				float maxDimension = Math.max(sprite.width(), sprite.height());
				if (maxDimension > 0) {
					sprite.scale = new PointF(BUTTON_SIZE / maxDimension, BUTTON_SIZE / maxDimension);
				}

				int row = i / GRID_COLUMNS;
				int column = i % GRID_COLUMNS;
				int rowCount = Math.min(GRID_COLUMNS, pageMobs.size() - row * GRID_COLUMNS);
				float left = (WIDTH - ((BUTTON_SIZE + GAP) * rowCount - GAP)) / 2f;
				button.setRect(
						left + column * (BUTTON_SIZE + GAP),
						GRID_TOP + row * (BUTTON_SIZE + GRID_ROW_GAP),
						BUTTON_SIZE,
						BUTTON_SIZE
				);
				add(button);
				mobButtons.add(button);
			}
		}

		private void clearMobButtons() {
			for (IconButton button : mobButtons) {
				button.destroy();
			}
			mobButtons.clear();
		}

		private void refreshMobButtons() {
			clearMobButtons();
			createMobButtons();
			updateTexts();
			layoutWindow();
		}

		private void updateTexts() {
			normalizeSelection();
			selectedPage.text((page + 1) + "/" + pageCount());
			selectedPage.maxWidth(WIDTH / 2);
			selectedPage.setPos((WIDTH - selectedPage.width()) / 2f, 5);

			selectedMob.text(M.L(selectedMobClass(), "name"));
			selectedMob.maxWidth(WIDTH);
		}

		private int gridRows() {
			return gridRowsForCount(mobsOnPage(page).size());
		}

		private int gridBottom() {
			return GRID_TOP + gridRows() * BUTTON_SIZE + (gridRows() - 1) * GRID_ROW_GAP;
		}

		private void layoutWindow() {
			selectedMob.setPos((WIDTH - selectedMob.width()) / 2f, gridBottom() + 8);
			float y = selectedMob.bottom() + 4;
			for (int i = 0; i < eliteChecks.size(); i++) {
				CheckBox check = eliteChecks.get(i);
				if ((i & 1) == 0) {
					check.setRect(0, y, WIDTH / 2f - GAP / 2f, 16);
				} else {
					check.setRect(WIDTH / 2f + GAP / 2f, y, WIDTH / 2f - GAP / 2f, 16);
					y += 16 + GAP;
				}
			}
			resize(WIDTH, (int) eliteChecks.get(eliteChecks.size() - 1).bottom() + 1);
		}

		private void updateEliteOptions() {
			int options = 0;
			for (int i = 0; i < eliteChecks.size(); i++) {
				if (eliteChecks.get(i).checked()) {
					options |= 1 << i;
				}
			}
			eliteOptions = options;
		}

		@Override
		public void onBackPressed() {
			updateEliteOptions();
			super.onBackPressed();
		}
	}
}
