# TestSpecialization Implementation Plan

**Goal:** Add a reusable test-mode item that independently changes the hero's subclass or armor ability through scrollable selection windows.

**Architecture:** `TestSpecialization` owns the item actions, `TestSpecializationState` owns selectable pools and safe state transitions, and `WndTestSpecialization` owns the scrollable UI. State transitions replace only the old subclass/armor talent set, synchronize dedicated buffs, and preserve the hero's base class and unrelated talents.

**Tech Stack:** Java, libGDX/Noosa UI, JUnit 4, Gradle.

---

### Task 1: Lock down selectable pools and talent replacement

**Files:**
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestSpecializationStateTest.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestSpecializationState.java`

1. Add failing tests asserting that the class pool contains exactly the first 12 `HeroClass` values and excludes `RATKING`.
2. Add failing tests asserting that the armor-ability pool is built from those 12 classes only.
3. Add failing tests for replacing old subclass and armor talent keys without removing unrelated talents.
4. Implement the smallest pool and talent-replacement helpers needed to pass these tests.
5. Run the focused test class.

### Task 2: Implement safe subclass and armor-ability transitions

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestSpecializationState.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestSpecializationStateTest.java`

1. Add tests for switching repeatedly between subclass/armor selections.
2. Implement subclass cleanup and setup for `Preparation`, `DarkHook`, `Ninja_Energy`, `FightStance`, `Reason`, and `MeleeWeapon.Charger`.
3. Grant compatibility equipment used by subclass mechanics when it is absent, without removing granted equipment on later switches.
4. Convert equipped ordinary armor to `ClassArmor` only once, preserve its properties, and reuse it for later armor-ability changes.
5. Refresh hero stats, sprite state, quickslot, and talent/action indicators after confirmed changes.

### Task 3: Add the standalone item and scrollable windows

**Files:**
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestSpecialization.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTestSpecialization.java`

1. Add a unique, non-consumable `TestItem` using `ItemSpriteSheet.MASK`.
2. Add independent `TEST_SUBCLASS` and `TEST_ARMOR_ABILITY` actions.
3. Build a scrollable class window followed by a scrollable subclass window, with descriptions and confirmation.
4. Build a scrollable armor-ability window grouped by source class, with descriptions and confirmation.
5. Reject armor-ability use when no armor is equipped and log the localized warning with `GLog.n`.

### Task 4: Register and localize the item

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroClass.java`
- Modify: `core/src/main/assets/messages/custom/custom.properties`
- Modify: `core/src/main/assets/messages/custom/custom_zh.properties`

1. Add `TestSpecialization` to test-mode starting items.
2. Add English and Chinese item, action, window, confirmation, success, and no-armor messages.
3. Verify message keys match the standalone package/class names.

### Task 5: Verify integration

**Files:**
- Inspect: all files changed above

1. Run focused unit tests.
2. Run `:core:compileJava` and `:core:test` with the repository Gradle wrapper; use the Gradle/JDK tools under `D:\AS` only if the wrapper environment requires them.
3. Review the diff for accidental changes and confirm no `TengusMask` or `KingsCrown` item-consumption flow is called.
4. Report implemented behavior and any remaining manual in-game checks.
