# EX Item Sprite SEAL Implementation Plan

> **For AI agent workers:** Follow the checked steps in order and use TDD.

**Goal:** Add `EXItemSpriteSheet.SEAL` as a minimal example that renders the
matching frame from `ex_items.png`.

**Architecture:** `EXItemSpriteSheet` encodes a sheet marker into the integer
image id. `ItemSprite` decodes the id, selects the proper texture, and uses the
existing `ItemSpriteSheet.film` frame geometry.

**Tech Stack:** Java, JUnit 4, existing Noosa `ItemSprite` rendering APIs.

---

### Task 1: Define and test EX image routing

**Files:**
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`

- [ ] Write a test asserting EX `SEAL` resolves to `ex_items.png` and the normal
  `SEAL` resolves to `items.png`.
- [ ] Run the test and confirm it fails because the EX API does not exist.
- [ ] Add the EX asset constant, encoded `SEAL`, and routing helpers.
- [ ] Run the test and confirm it passes.

### Task 2: Integrate ItemSprite

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java`

- [ ] Select the resolved texture in `frame(int image)`.
- [ ] Decode the frame before using `ItemSpriteSheet.film`.
- [ ] Apply the same routing to `pick(int, int, int)`.
- [ ] Run the focused test and force a core compile.
- [ ] Run `git diff --check` on the touched files.
