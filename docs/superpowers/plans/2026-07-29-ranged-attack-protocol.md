# Monster Ranged Attack Protocol Implementation Plan

**Goal:** Centralize ranged physical and ranged magical mob attacks behind one protocol, including drones, so shared effects such as `NO_VIEWRAPE` run from one ranged-hit hook.

**Architecture:** `RangedAttack` defines targeting, dispatch, attack type, and the shared hit hook. `PhysicalRangedAttack` and `MagicalRangedAttack` provide distinct defaults while `Mob` owns common dispatch and the pending physical-animation context. Individual mobs retain only damage rolls, debuffs, special timing, and visuals.

**Tech stack:** Java, JUnit 4, Gradle.

---

### Task 1: Lock the protocol contract

**Files:**
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RangedAttackTest.java`

- [ ] Assert physical projectile mobs implement `PhysicalRangedAttack`.
- [ ] Assert spell projectile mobs implement `MagicalRangedAttack`.
- [ ] Assert the drone hierarchy participates in the physical protocol.
- [ ] Run the focused test and confirm it fails because the protocol does not exist.

### Task 2: Add protocol and central dispatch

**Files:**
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RangedAttack.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/PhysicalRangedAttack.java`
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/MagicalRangedAttack.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java`

- [ ] Dispatch ranged reach and execution from `Mob.canAttack` and `Mob.doAttack`.
- [ ] Preserve asynchronous physical projectile callbacks with an explicit pending attack type.
- [ ] Move `NO_VIEWRAPE` into the shared ranged-hit hook.
- [ ] Run focused tests.

### Task 3: Migrate regular and special mobs

**Files:**
- Modify: `GnollTrickster.java`, `Scorpio.java`, `DM100.java`, `Shaman.java`, `Warlock.java`, `CrystalWisp.java`, `Eye.java`
- Modify: ranged custom mobs that duplicate `NO_VIEWRAPE`, including `ChaosDisciples.java` and `GoldBoss.java`

- [ ] Remove duplicated line-of-fire and attack-selection code where the protocol default is sufficient.
- [ ] Keep per-mob damage, debuffs, resistance source classes, charge states, and death messages unchanged.
- [ ] Route every successful ranged hit through `onRangedAttackHit`.

### Task 4: Migrate drones

**Files:**
- Modify: `items/artifacts/InstructionTool.java`
- Modify: `actors/hero/ally/AttackDrone.java`
- Modify: `actors/hero/ally/AuxiliaryDrone.java`
- Modify: `sprites/DronesSprite.java`

- [ ] Make drone beams and projectiles use one physical hit roll and the physical ranged pipeline.
- [ ] Give attack drones functional line-of-fire targeting, retaining existing short-range limits.
- [ ] Keep support, bomb, and chaos drones unable to perform direct ranged attacks.
- [ ] Ensure animation-disabled and invisible combat complete exactly once.

### Task 5: Verify

- [ ] Run `gradlew core:test --tests ...RangedAttackTest`.
- [ ] Run existing mob and drone-related tests.
- [ ] Run `gradlew core:compileJava`.
- [ ] Inspect the final diff for duplicated `NO_VIEWRAPE` logic and accidental changes to unrelated dirty files.
