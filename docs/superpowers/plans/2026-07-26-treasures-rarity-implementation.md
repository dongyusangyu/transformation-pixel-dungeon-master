# Treasures Rarity Implementation Plan

**Goal:** Add persistent random treasure rarity and restrained rarity coloring in inventory slots.

**Architecture:** `Treasures` owns rarity generation and persistence. `InventorySlot` owns visual colors and animates legendary slots without per-frame texture allocation.

### Task 1: Define rarity behavior with tests

**Files:**
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasuresTest.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java`

1. Add failing probability-boundary tests.
2. Add failing item flag and bundle persistence tests.
3. Implement `Rarity`, single-roll generation, item flags, chest image, and bundle storage.
4. Run the focused test.

### Task 2: Lock down transmutation exclusion

**Files:**
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasuresTest.java`

1. Add a test exposing `ScrollOfTransmutation.usableOnItem`.
2. Confirm a `Treasures` instance is rejected.
3. Only modify transmutation production code if the existing positive whitelist does not already reject it.

### Task 3: Add inventory rarity coloring

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/InventorySlot.java`

1. Add common, rare, epic, and legendary background handling.
2. Keep ordinary item behavior unchanged.
3. Animate legendary rainbow-gold through color multiplication using `Game.timeTotal`.
4. Avoid texture creation in `update()`.

### Task 4: Localize and verify

**Files:**
- Modify: `core/src/main/assets/messages/items/items.properties`
- Modify: `core/src/main/assets/messages/items/items_zh.properties`

1. Add base item name, description, and rarity text.
2. Run the focused test, `:core:test`, and `:android:compileDebugJavaWithJavac`.
3. Run `git diff --check` on target files and review the final diff.
