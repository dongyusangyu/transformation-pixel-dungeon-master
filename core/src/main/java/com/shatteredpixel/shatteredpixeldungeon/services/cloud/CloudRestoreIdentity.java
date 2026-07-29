package com.shatteredpixel.shatteredpixeldungeon.services.cloud;

import java.util.Locale;
import java.util.UUID;

public final class CloudRestoreIdentity {

    private CloudRestoreIdentity() {
    }

    public static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        try {
            return UUID.fromString(normalized).toString().equals(normalized) ? normalized : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String resolveRestoredUUID(String requestedUUID, String responseUUID) {
        String requested = normalize(requestedUUID);
        String restored = normalize(responseUUID);
        if (requested == null || restored == null || restored.isEmpty()) {
            return null;
        }
        return requested.isEmpty() || requested.equals(restored) ? restored : null;
    }
}
