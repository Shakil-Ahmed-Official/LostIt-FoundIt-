package app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void samePasswordGivesDifferentHashesBecauseOfTheSalt() {
        assertNotEquals(PasswordHasher.hash("secret1"),
                PasswordHasher.hash("secret1"));
    }

    @Test
    void storedValueHasThreeParts() {
        assertEquals(3, PasswordHasher.hash("secret1").split(":").length);
    }

    @Test
    void acceptsTheCorrectPassword() {
        String stored = PasswordHasher.hash("secret1");
        assertTrue(PasswordHasher.verify("secret1", stored));
    }

    @Test
    void rejectsAWrongPassword() {
        String stored = PasswordHasher.hash("secret1");
        assertFalse(PasswordHasher.verify("secret2", stored));
    }

    @Test
    void rejectsAMalformedStoredValue() {
        assertFalse(PasswordHasher.verify("secret1", "not-a-hash"));
    }
}
