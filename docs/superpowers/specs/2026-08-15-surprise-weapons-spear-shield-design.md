# Surprise Weapon Spear-Shield Integration

## Goal

Make every weapon with an inherent surprise-attack damage range respect the
`SpearShield` trinket's minimum and maximum weapon-damage adjustments. Preserve
the weapons' current surprise multipliers, random-roll behavior, augment
behavior, excess-strength rolls, and normal-versus-surprise maximum fallback.

## Scope

The directly affected weapon classes are:

- `Dagger`, with a surprise minimum 75% of the way from minimum to maximum.
- `Dirk`, with a surprise minimum 67% of the way from minimum to maximum.
- `AssassinsBlade`, with a surprise minimum 50% of the way from minimum to maximum.
- `ThrowingKnife`, with a surprise minimum 75% of the way from minimum to maximum.
- `Kunai`, with a surprise minimum 60% of the way from minimum to maximum.

`SakuraBlossomBlade` inherits `AssassinsBlade.damageRoll` and is therefore
covered without a class-specific branch.

This change does not include `SHADOW_KILLER`, Ring of Force surprise logic,
Duelist heavy-blow abilities, `RitualDagger`, or weapons which merely benefit
from the normal guaranteed-hit property of a surprise attack.

## Shared Range Roll

Extract the hero-owned physical weapon range roll currently embedded in
`KindOfWeapon.damageRoll` into one protected helper. The helper accepts an
explicit minimum and maximum and performs the existing processing:

1. If the hero carries `SpearShield`, use `changeDmgMin(min, max)` and
   `changeDmgMax(min, max)` to derive the effective range.
2. Roll that effective range through `Hero.heroDamageIntRange`, preserving
   Thirteen-Leaf Clover behavior.
3. If no `SpearShield` is present, roll the supplied range unchanged.

`KindOfWeapon.damageRoll` calls this helper with the weapon's ordinary
`min()` and `max()`. Surprise weapons call the same helper with their computed
surprise minimum and ordinary maximum. This makes `SpearShield` the final
range adjustment before the hero damage roll in both paths.

The helper applies only to hero-owned weapon rolls. Non-hero weapon users keep
the existing `Random.NormalIntRange` path and are not affected by a trinket in
the hero's backpack.

## Normal-Roll Maximum Fallback

The existing surprise implementations contain this behavior:

```java
damage = Math.max(super.damageRoll(owner), damage);
```

Keep this behavior. It independently rolls the ordinary weapon candidate and
the surprise candidate, then uses the higher value. After this change:

- `super.damageRoll(owner)` obtains its ordinary candidate through the shared
  range helper, so `SpearShield` affects the ordinary range.
- The surprise candidate also uses the shared range helper, so `SpearShield`
  affects the surprise range.
- `Math.max` runs after both candidates have been adjusted and rolled.
- The current ordering of augment application, the maximum fallback, and the
  extra excess-strength roll remains unchanged.

Do not remove the fallback or replace the two candidates with a single roll.
Doing so would change the current surprise damage distribution beyond the
requested trinket integration.

## Calculation Order

For an inherent surprise attack, the resulting order is:

1. Read the weapon's current `min()` and `max()` values.
2. Compute the weapon-specific surprise minimum from that base range.
3. Apply `SpearShield` to the completed surprise range.
4. Roll through `Hero.heroDamageIntRange`, including Clover behavior.
5. Apply the weapon augment to the surprise candidate.
6. Compare it with the independently rolled ordinary candidate via `Math.max`.
7. Add the existing excess-strength roll.

This intentionally allows spear mode to replace a weapon's inherent surprise
minimum with a lower value when the trinket percentage is lower. That matches
the trinket's documented rule that its minimum correction may be below the
original minimum. Shield mode continues to clamp its reduced maximum so it
cannot fall below the supplied surprise minimum.

## Compatibility

- No item text needs to change because both the surprise weapons and
  `SpearShield` already describe their individual rules.
- Existing save data is unaffected; no new persistent fields are introduced.
- Future inherent-surprise weapons should use the shared range helper rather
  than calling `Hero.heroDamageIntRange` directly.
- Existing user changes in the affected weapon files, including the current
  `Math.max` fallback lines, must be preserved.

## Verification

Add focused tests that verify:

- The shared range helper leaves supplied bounds unchanged without
  `SpearShield`.
- Spear mode changes the final surprise minimum for each supported percentage.
- Shield mode changes the final surprise maximum and never lowers it below the
  surprise minimum.
- Cooldown behavior remains delegated to `SpearShield.changeDmgMin`.
- Each of the five direct weapon classes sends its inherent surprise range
  through the shared helper.
- `SakuraBlossomBlade` remains covered through `AssassinsBlade` inheritance.
- The ordinary candidate and surprise candidate both use the trinket-adjusted
  range before `Math.max` selects the higher roll.
- Non-surprise attacks and non-hero weapon rolls retain their current behavior.
