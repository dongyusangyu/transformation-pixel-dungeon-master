package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestSpecialization;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestSpecializationState;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

public final class WndTestSpecialization {

	private static final int WIDTH = 120;
	private static final int PANE_HEIGHT = 120;
	private static final int BUTTON_HEIGHT = 16;
	private static final int INFO_WIDTH = 16;
	private static final int GAP = 2;

	private WndTestSpecialization() {
	}

	private abstract static class ScrollableSelection extends Window {

		protected final Hero hero;
		protected final Component content;
		private final ScrollPane pane;
		private final float paneTop;

		protected ScrollableSelection(Hero hero, String title, String message) {
			this.hero = hero;

			IconTitle titlebar = new IconTitle(
					new ItemSprite(ItemSpriteSheet.MASK, null), title);
			titlebar.setRect(0, 0, WIDTH, 0);
			add(titlebar);

			RenderedTextBlock body = PixelScene.renderTextBlock(message, 6);
			body.maxWidth(WIDTH);
			body.setPos(0, titlebar.bottom() + GAP);
			add(body);

			paneTop = body.bottom() + GAP;
			content = new Component();
			pane = new ScrollPane(content);
			add(pane);
		}

		protected void finish(float contentHeight) {
			content.setRect(0, 0, WIDTH, contentHeight);
			float paneHeight = Math.min(PANE_HEIGHT, Math.max(BUTTON_HEIGHT, contentHeight));
			resize(WIDTH, (int) Math.ceil(paneTop + paneHeight));
			pane.setRect(0, paneTop, WIDTH, paneHeight);
			pane.scrollTo(0, 0);
			pane.update();
		}

		@Override
		public void offset(int xOffset, int yOffset) {
			super.offset(xOffset, yOffset);
			pane.setPos(pane.left(), pane.top());
		}
	}

	private abstract static class ScrollableRedButton extends RedButton {

		private ScrollableRedButton(String label) {
            super(label, 8);
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
		}
	}
    private abstract static class ScrollclassRedButton extends RedButton {

        private ScrollclassRedButton(String label) {
            super(label, 6);
            hotArea.blockLevel = PointerArea.NEVER_BLOCK;
        }
    }

	private abstract static class ScrollableInfoButton extends IconButton {

		private ScrollableInfoButton() {
			super(Icons.get(Icons.INFO));
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
		}
	}

	public static class ClassSelection extends ScrollableSelection {

		public ClassSelection(Hero hero) {
			super(hero,
					Messages.get(TestSpecialization.class, "subclass_title"),
					Messages.get(TestSpecialization.class, "subclass_message"));

			float pos = GAP;
			for (HeroClass heroClass : TestSpecializationState.selectableClasses()) {
				RedButton button = new ScrollableRedButton(heroClass.title()) {
					@Override
					protected void onClick() {
						hide();
						GameScene.show(new SubclassSelection(hero, heroClass));
					}
				};
				button.setRect(0, pos, WIDTH - INFO_WIDTH, BUTTON_HEIGHT);
				content.add(button);

				IconButton info = new ScrollableInfoButton() {
					@Override
					protected void onClick() {
						GameScene.show(new WndHeroInfo(heroClass));
					}
				};
				info.setRect(WIDTH - INFO_WIDTH, pos, INFO_WIDTH, BUTTON_HEIGHT);
				content.add(info);
				pos = button.bottom() + GAP;
			}
			finish(pos);
		}
	}

	public static class SubclassSelection extends ScrollableSelection {

		public SubclassSelection(Hero hero, HeroClass heroClass) {
			super(hero,
					Messages.get(TestSpecialization.class, "choose_subclass", heroClass.title()),
					Messages.get(TestSpecialization.class, "choose_subclass_message"));

			float pos = GAP;
			for (HeroSubClass subClass : heroClass.subClasses()) {
				RedButton button = new ScrollclassRedButton(subClass.shortDesc()) {
					@Override
					protected void onClick() {
						confirmSubclass(subClass);
					}
				};
				button.leftJustify = true;
				button.multiline = true;
				button.setSize(WIDTH - INFO_WIDTH, BUTTON_HEIGHT);
				float height = Math.max(BUTTON_HEIGHT, button.reqHeight() + 2);
				button.setRect(0, pos, WIDTH - INFO_WIDTH, height);
				content.add(button);

				IconButton info = new ScrollableInfoButton() {
					@Override
					protected void onClick() {
						GameScene.show(new WndInfoSubclass(heroClass, subClass));
					}
				};
				info.setRect(WIDTH - INFO_WIDTH,
						button.top() + (button.height() - INFO_WIDTH) / 2,
						INFO_WIDTH, INFO_WIDTH);
				content.add(info);
				pos = button.bottom() + GAP;
			}
			finish(pos);
		}

		private void confirmSubclass(HeroSubClass subClass) {
			GameScene.show(new WndOptions(
					new HeroIcon(subClass),
					Messages.titleCase(subClass.title()),
					Messages.get(TestSpecialization.class, "confirm_subclass"),
					Messages.get(TestSpecialization.class, "yes"),
					Messages.get(TestSpecialization.class, "no")) {
				@Override
				protected void onSelect(int index) {
					if (index == 0 && TestSpecializationState.chooseSubclass(hero, subClass)) {
						SubclassSelection.this.hide();
						GLog.p(Messages.get(TestSpecialization.class,
								"subclass_changed", subClass.title()));
					}
				}
			});
		}
	}

	public static class ArmorAbilitySelection extends ScrollableSelection {

		public ArmorAbilitySelection(Hero hero, HeroClass heroClass) {
			super(hero,
					Messages.get(TestSpecialization.class,
							"choose_armor", heroClass.title()),
					Messages.get(TestSpecialization.class, "choose_armor_message"));

			float pos = GAP;
			for (ArmorAbility ability
					: TestSpecializationState.armorAbilitiesForClass(heroClass)) {
				RedButton button = new ScrollclassRedButton(ability.shortDesc()) {
					@Override
					protected void onClick() {
						confirmArmorAbility(heroClass, ability);
					}
				};
				button.leftJustify = true;
				button.multiline = true;
				button.setSize(WIDTH - INFO_WIDTH, BUTTON_HEIGHT);
				float height = Math.max(BUTTON_HEIGHT, button.reqHeight() + 2);
				button.setRect(0, pos, WIDTH - INFO_WIDTH, height);
				content.add(button);

				IconButton info = new ScrollableInfoButton() {
					@Override
					protected void onClick() {
						GameScene.show(new WndInfoArmorAbility(heroClass, ability));
					}
				};
				info.setRect(WIDTH - INFO_WIDTH,
						button.top() + (button.height() - INFO_WIDTH) / 2,
						INFO_WIDTH, INFO_WIDTH);
				content.add(info);
				pos = button.bottom() + GAP;
			}
			finish(pos);
		}

		private void confirmArmorAbility(HeroClass sourceClass, ArmorAbility ability) {
			GameScene.show(new WndOptions(
					new HeroIcon(ability),
					Messages.titleCase(ability.name()),
					Messages.get(TestSpecialization.class,
							"confirm_armor", sourceClass.title()),
					Messages.get(TestSpecialization.class, "yes"),
					Messages.get(TestSpecialization.class, "no")) {
				@Override
				protected void onSelect(int index) {
					if (index != 0) {
						return;
					}
					if (hero.belongings.armor() == null) {
						GLog.n(Messages.get(TestSpecialization.class, "no_armor"));
					} else if (TestSpecializationState.chooseArmorAbility(hero, ability)) {
						ArmorAbilitySelection.this.hide();
						GLog.p(Messages.get(TestSpecialization.class,
								"armor_changed", ability.name()));
					}
				}
			});
		}
	}

	public static class ArmorClassSelection extends ScrollableSelection {

		public ArmorClassSelection(Hero hero) {
			super(hero,
					Messages.get(TestSpecialization.class, "armor_title"),
					Messages.get(TestSpecialization.class, "armor_message"));

			float pos = GAP;
			for (HeroClass heroClass : TestSpecializationState.selectableClasses()) {
				RedButton button = new ScrollableRedButton(heroClass.title()) {
					@Override
					protected void onClick() {
						hide();
						GameScene.show(new ArmorAbilitySelection(hero, heroClass));
					}
				};
				button.setRect(0, pos, WIDTH - INFO_WIDTH, BUTTON_HEIGHT);
				content.add(button);

				IconButton info = new ScrollableInfoButton() {
					@Override
					protected void onClick() {
						GameScene.show(new WndHeroInfo(heroClass));
					}
				};
				info.setRect(WIDTH - INFO_WIDTH, pos, INFO_WIDTH, BUTTON_HEIGHT);
				content.add(info);
				pos = button.bottom() + GAP;
			}
			finish(pos);
		}
	}
}
