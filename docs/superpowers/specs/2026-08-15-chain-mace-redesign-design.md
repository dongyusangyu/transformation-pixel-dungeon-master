# Chain Mace Redesign

## Scope

Redesign the tier-six `ChainMace` around weak melee attacks and a high-reward area throw while preserving its current Duelist ability and its persistent iron-ball follower. Update the item and custom dictionary text so both interfaces describe the implemented rules.

## Weapon Stats

- The weapon remains tier 6 with its current strength requirement, reach, attack delay, sprite, sounds, and upgrade behavior.
- Melee damage is `6 + level` to `20 + 7 * level`.
- A normal melee hit keeps the current weapon attack pipeline, including accuracy, armor reduction, enchantments, augments, strength bonuses, talents, sounds, and `0.25f` hit shake.

## Equipped Throw Targeting

- Throwing an unequipped chain mace retains ordinary item-throw behavior.
- Throwing an equipped chain mace commands its linked iron ball and does not unequip or drop the weapon.
- The selected destination may be an ordinary empty floor cell or a cell occupied by a character.
- The destination must be visible, inside the map, non-solid, non-pit, and reachable by an unobstructed projectile trajectory.
- The iron ball must be adjacent to the hero when the command is issued.
- The iron ball, hero, and destination must be collinear, with the hero strictly between the ball and destination. The hero cannot be either endpoint. This is the required inertial wind-up: the ball is thrown from one side of the hero to the other.
- Invalid commands do not spend time and display a localized reason. A busy or returning ball cannot receive another command.

## Throw Damage And Friendly Fire

- A successful throw rolls one base weapon damage value from the chain mace's current augmented damage range, then doubles that single result. At level 0 with no augment this is `12-40`; each weapon level adds `2-14` to the possible result.
- Excess-strength bonuses, Ring of Force bonuses, attack-only talents, accuracy checks, armor rolls, and weapon enchantment procs are not added to the area damage. This keeps the specified `6-20+7/level`, doubled, as the authoritative range.
- The doubled result is shared by every full-damage target in that impact; it is not rerolled per character.
- Every living character in the destination's 3x3 area is affected regardless of alignment, including enemies, allies, neutral characters, and the hero.
- The hero receives one quarter of the rolled area damage, rounded down. All other affected characters receive the full value.
- The iron-ball follower remains immune and cannot be damaged by its own impact.
- Area damage is direct impact damage and does not trigger weapon enchantments separately for each target.
- The impact causes no knockback or displacement.

## Throw Presentation And Return

- The launch retains the chain sound and the current rapid iron-ball jump animation.
- Landing plays a heavy crushing or rock impact sound, emits dust and rock particles, shows a visual-only impact wave, and applies the existing `0.33f` hit shake once per impact.
- The impact wave must not call blast-wave displacement helpers or otherwise move characters.
- After impact, the ball enters the existing returning command state and the hero spends the normal attack delay.
- While returning after a throw or interrupted command, the ball moves at a fixed `3x` speed.
- During ordinary following, the ball dynamically uses the hero's current movement speed, including temporary acceleration and slowing effects.
- When the ball reaches an adjacent cell, the command state becomes idle and throwing is available again.

## Duelist Ability

- Preserve the existing `iron ball barrage` / `横扫千军` behavior, targeting rules, guaranteed hits, command sequence, charge cost, interruption recovery, sound, animation, and return behavior.
- The Duelist ability remains a sequence of weapon attacks and does not use the new 3x3 impact damage or friendly-fire rules.
- The follower returns at `3x` speed after the ability, as it does after a normal throw.

## Iron-Ball Follower

- The follower remains nested in `ChainMace` and remains a neutral `DirectableAlly` that cannot attack, select enemies, be targeted as a hostile character, or block ballistics.
- It rejects every buff and remains invulnerable to all ordinary damage.
- Add explicit protection against direct `die` calls so traps, scripted instant-death effects, boss transitions, and similar effects cannot kill it.
- Controlled removal still occurs when the chain mace is unequipped, the owner dies, the weapon relationship is lost, or stale follower state is reconciled across floors.
- Existing save/restore, floor-transition recovery, crowded-cell handling, and command interruption handling remain intact.

## Text Changes

- Update English and Chinese item descriptions in `items.properties` and `items_zh.properties`.
- The item text must explain the inertial through-hero trajectory, 3x3 damage, universal friendly fire, quarter self-damage, and absence of knockback without obscuring the separate Duelist ability description.
- Update the Chinese custom dictionary entry in `custom_zh.properties` from the old `6-50` damage curve and single-target throw to the new melee curve, doubled area roll, targeting restriction, friendly-fire rule, return speed, and preserved Duelist ability.
- Update an English custom entry only if a matching ChainMace entry exists when implementation begins.
- Update existing localization tests that intentionally assert exact ChainMace text.

## Validation

- Unit tests cover the `6-20+7/level` melee curve and doubled throw range.
- Pure targeting tests cover straight and diagonal valid throws, empty destinations, hero-not-between rejection, non-collinear rejection, blocked trajectories, invalid terrain, and a non-adjacent ball.
- Damage distribution tests cover one shared roll, full damage to enemy/ally/neutral targets, quarter damage to the hero, follower immunity, and no knockback call.
- Follower tests cover neutral alignment, rejection of buffs, ballistic transparency, explicit instant-death immunity, ordinary speed matching the hero, and fixed 3x return speed.
- Existing ChainMace command-state, save/restore, floor-transition, ability, integration, sprite, and localization tests continue to pass.

## Out Of Scope

- No changes to the weapon's generation pool, tier, strength requirement, sprite assets, Duelist ability balance, enchantment definitions, general throwing framework, or other followers.
