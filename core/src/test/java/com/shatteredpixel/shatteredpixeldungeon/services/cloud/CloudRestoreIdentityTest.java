package com.shatteredpixel.shatteredpixeldungeon.services.cloud;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CloudRestoreIdentityTest {

    @Test
    public void normalizesEnteredUuidForRestore() {
        assertEquals(
                "22222222-2222-4222-8222-222222222222",
                CloudRestoreIdentity.normalize(
                        "  22222222-2222-4222-8222-222222222222  "
                )
        );
    }

    @Test
    public void emptyInputUsesDeviceFingerprintFallback() {
        assertEquals("", CloudRestoreIdentity.normalize("  "));
        assertEquals("", CloudRestoreIdentity.normalize(null));
    }

    @Test
    public void rejectsNonCanonicalOrInvalidUuid() {
        assertNull(CloudRestoreIdentity.normalize("not-a-uuid"));
        assertNull(CloudRestoreIdentity.normalize("1-1-1-1-1"));
    }

    @Test
    public void acceptsOnlyTheRequestedUuidFromRestoreResponse() {
        String uuid = "22222222-2222-4222-8222-222222222222";
        assertEquals(uuid, CloudRestoreIdentity.resolveRestoredUUID(uuid, uuid));
        assertEquals(uuid, CloudRestoreIdentity.resolveRestoredUUID("", uuid));
        assertNull(CloudRestoreIdentity.resolveRestoredUUID(
                uuid,
                "33333333-3333-4333-8333-333333333333"
        ));
        assertNull(CloudRestoreIdentity.resolveRestoredUUID(uuid, "invalid"));
        assertNull(CloudRestoreIdentity.resolveRestoredUUID("", ""));
    }
}
