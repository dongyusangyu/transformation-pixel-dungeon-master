# Treasures Rarity Design

## Goal

Add a reusable `Treasures` item base class under `items.treasures` with persistent random rarity and rarity-colored inventory slots.

## Item Model

- `Treasures` extends `Item`.
- It uses `ItemSpriteSheet.CHEST` by default.
- It is non-stackable, non-upgradable, identified, and outside every transmutation whitelist.
- It exposes four rarity levels: `LEGENDARY`, `EPIC`, `RARE`, and `COMMON`.
- A single random roll assigns rarity:
  - Legendary: `[0, 0.001)`, 0.1%.
  - Epic: `[0.001, 0.011)`, 1%.
  - Rare: `[0.011, 0.111)`, 10%.
  - Common: `[0.111, 1)`, 88.9%.
- Rarity is stored in the item bundle and restored without rerolling.

## Inventory Presentation

`InventorySlot` detects `Treasures` and applies the approved restrained palette:

- Common: existing slot background, `0x9953564D`.
- Rare: dark green, `0x99426D3B`.
- Epic: muted gold, `0x999E762C`.
- Legendary: a low-brightness rainbow-gold tint animated from `Game.timeTotal`.

The legendary animation reuses one solid background texture and changes only its color multiplier. It must not allocate textures every frame.

## Compatibility

Normal cursed/unidentified slot tinting does not override treasure rarity because `Treasures` is always identified and is not cursed by default. Ordinary items retain the current `InventorySlot` behavior.

`ScrollOfTransmutation` selects items through a positive type whitelist. Since `Treasures` directly extends `Item` and cannot also be a whitelisted ring, weapon, wand, potion, scroll, artifact, trinket, seed, or runestone type, it is not transmutable.

## Verification

- Unit tests cover every probability boundary.
- Unit tests verify rarity bundle persistence and item flags.
- A transmutation-selection regression test confirms `Treasures` is rejected.
- Core tests and Android compilation verify the inventory rendering integration.
