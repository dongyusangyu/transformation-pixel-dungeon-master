package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.watabou.utils.Bundle;

final class TowerBossHitLimit {

    private static final String HITS = "tower_direct_hits";
    private static final String REMAINING = "tower_hit_window_remaining";
    private static final int MAX_HITS = 3;

    private int hits;
    private float windowEnd = Float.NEGATIVE_INFINITY;
    private float restoredRemaining = -1f;

    boolean allows(float time, boolean periodic) {
        if (periodic) return true;
        updateWindow(time);
        return hits < MAX_HITS;
    }

    void record(float time) {
        updateWindow(time);
        hits++;
    }

    void store(Bundle bundle, float time) {
        if (restoredRemaining >= 0f) {
            bundle.put(REMAINING, restoredRemaining);
        } else {
            bundle.put(REMAINING, Math.max(0f, windowEnd - time));
        }
        bundle.put(HITS, hits);
    }

    void restore(Bundle bundle) {
        hits = Math.max(0, Math.min(MAX_HITS, bundle.getInt(HITS)));
        restoredRemaining = bundle.contains(REMAINING)
                ? Math.max(0f, Math.min(1f, bundle.getFloat(REMAINING))) : 0f;
        windowEnd = Float.NEGATIVE_INFINITY;
    }

    private void updateWindow(float time) {
        if (restoredRemaining >= 0f) {
            windowEnd = time + restoredRemaining;
            restoredRemaining = -1f;
        }
        if (time >= windowEnd) {
            hits = 0;
            windowEnd = (float) Math.floor(time) + 1f;
        }
    }
}
