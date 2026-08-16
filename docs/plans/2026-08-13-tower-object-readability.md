# Tower Object Readability Implementation Plan

**Goal:** Make all six tower themes' statues, solid obstacles, and bookshelves clearly readable at native pixel scale while preserving terrain semantics and theme identity.

**Architecture:** Extend the shared tower tileset normalizer with bookshelf geometry transfer and object readability passes. Redraw the greenhouse and frost statue families in their generators, then regenerate every tower atlas and validate both numeric contrast and layer stitching.

**Tech stack:** Python, Pillow, `unittest`, Gradle asset integration.

## Tasks

- [ ] Add failing semantic tests for bookshelf geometry, object contrast, and the two redesigned statue families.
- [ ] Normalize bookshelf masks across flat, raised, internal, and overhang layers using the halls atlas as geometry reference.
- [ ] Add theme-aware outlines, contact shadows, and accents to solid tower objects without changing wells, alchemy pots, or vegetation.
- [ ] Redraw greenhouse sun-prism statues and frost preserved statues across flat, raised, and overhang layers.
- [ ] Regenerate all six tower atlases and render a focused object audit board.
- [ ] Run Python semantic tests, generator tests, and the core asset build checks.
