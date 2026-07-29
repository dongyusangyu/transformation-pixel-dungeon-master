# EX Item Sprite SEAL Design

## Goal

Provide one reference constant, `EXItemSpriteSheet.SEAL`, which uses the same
frame coordinates and dimensions as `ItemSpriteSheet.SEAL` but renders from
`sprites/ex_items.png`.

## Design

- Keep every existing `ItemSpriteSheet` value and call site unchanged.
- Encode EX sheet indexes in a separate integer range so they remain compatible
  with the existing `Item.image` integer field.
- Add `EXItemSpriteSheet.SEAL` as the only example constant.
- Make `ItemSprite` resolve the encoded value into:
  - texture: `Assets.Sprites.EX_ITEMS`
  - frame: `ItemSpriteSheet.SEAL`
- Restore the normal items texture whenever a regular image index is viewed,
  so pooled `ItemSprite` instances can switch safely between sheets.

## Verification

Unit tests verify that normal and EX indexes resolve to different textures while
sharing the same base frame. A core compile verifies the rendering integration.
