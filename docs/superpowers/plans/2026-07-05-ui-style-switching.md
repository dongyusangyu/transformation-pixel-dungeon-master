# UI Style Switching Implementation Plan

> **For AI agent:** Execute this plan in the current workspace with TDD and verify each step before continuing.

**Goal:** Add a persistent Transformation/SPD UI style selector to the interface settings and apply the selected assets immediately without leaving the game.

**Architecture:** `SPDSettings` owns the persisted style enum. `Assets.Interfaces` resolves the nine themeable interface paths from that setting while all uncovered assets keep their existing paths. The cursor layer accepts a selectable asset directory, and the settings window applies the new selection before using the existing seamless scene reset so all visible UI objects are rebuilt.

**Tech stack:** Java, libGDX/Noosa, JUnit 4, Gradle.

---

### Task 1: Persist and resolve UI styles

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettings.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/UIStyleTest.java`

- [ ] Add failing tests for Transformation defaults, SPD paths, and invalid stored values.
- [ ] Run `core:test --tests com.shatteredpixel.shatteredpixeldungeon.UIStyleTest` and confirm the missing API fails compilation.
- [ ] Add `UIStyle`, persistent getter/setter, and dynamic interface path methods.
- [ ] Re-run the focused test and confirm it passes.

### Task 2: Replace cached interface paths

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Chrome.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/BossHealthBar.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/Icons.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/KeyDisplay.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/MenuPane.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/RadialMenu.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/StatusPane.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/TalentButton.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/Toolbar.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/v0_3_X_Changes.java`

- [ ] Replace the nine fixed constants with dynamic path method calls.
- [ ] Remove static path caching in status and boss UI components.
- [ ] Compile `core:compileJava` and fix any remaining fixed-path references.

### Task 3: Switch cursor assets and expose the setting

**Files:**
- Modify: `SPD-classes/src/main/java/com/watabou/noosa/ui/Cursor.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/PixelScene.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSettings.java`
- Modify: `core/src/main/assets/messages/windows/windows.properties`
- Modify: `core/src/main/assets/messages/windows/windows_zh.properties`

- [ ] Make cursor file resolution use the current style directory and reload when the style changes.
- [ ] Apply the cursor directory before each `PixelScene` cursor setup.
- [ ] Add the “UI Style Settings” button and Transformation/SPD selection window.
- [ ] Persist the selection, clear texture cache, and call `seamlessResetScene()`.
- [ ] Add English and Chinese labels.

### Task 4: Verify

**Files:**
- Verify all files above and the supplied `interfaces/SPD` and `gdx/SPD` assets.

- [ ] Run the focused `UIStyleTest`.
- [ ] Run `core:test`.
- [ ] Run `core:compileJava android:compileDebugJavaWithJavac` with the Gradle installation under `D:/AS`.
- [ ] Inspect `git diff` to ensure only intended source/message/plan changes were added and existing user changes were preserved.
