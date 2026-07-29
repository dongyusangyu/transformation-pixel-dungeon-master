# Metamorphosis Prism Implementation Plan

**Goal:** Add an alchemical prism that replaces an acquired boss talent with up to four randomly selected boss talents from the same boss-talent slot.

**Architecture:** Keep boss-slot filtering and point-preserving replacement in `MetamorphosisPrism` as testable static methods. Use two talent windows for source selection and replacement selection, then consume the prism only after a replacement is confirmed.

**Tech stack:** Java, existing `Spell`, `TalentsPane`, `TalentButton`, `Recipe.SimpleRecipe`, JUnit 4.

---

### Task 1: Boss talent candidate and replacement rules

**Files:**
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/spells/MetamorphosisPrism.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/spells/MetamorphosisPrismTest.java`

- [x] Add failing tests proving candidates share the source slot, exclude the source, and preserve invested points during replacement.
- [x] Run the focused test and confirm it fails because `MetamorphosisPrism` does not exist.
- [x] Implement the minimal candidate and replacement helpers.
- [x] Run the focused test and confirm it passes.

### Task 2: Two-stage talent selection UI

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/spells/MetamorphosisPrism.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/TalentButton.java`

- [x] Add source and replacement modes to `TalentButton`.
- [x] Show only acquired, non-placeholder boss talents in the first window.
- [x] Randomly choose at most four eligible same-slot replacements in the second window.
- [x] Consume and animate the prism only after the player confirms the replacement.

### Task 3: Recipe, catalog, and localization

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Recipe.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/QuickRecipe.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java`
- Modify: `core/src/main/assets/messages/items/items.properties`
- Modify: `core/src/main/assets/messages/items/items_zh.properties`

- [x] Register `3x ScrollOfMetamorphosis + 3 energy -> 1x MetamorphosisPrism`.
- [x] Add the prism to quick recipes and the spell catalog.
- [x] Add English fallback and Chinese user-facing text.
- [x] Compile, run focused tests, and check changed files for whitespace errors.
