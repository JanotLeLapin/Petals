package io.github.petals.structures;

import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.petals.TestUtil;
import io.github.petals.Game.Player;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Testcontainers
public class PetalsBaseTest extends TestUtil {
    @Test
    public void equality() {
        String game = db.createGame("foo", plugin).uniqueId();

        Player<?> p1 = db.player("foo").get();
        assertTrue(p1.equals(p1));

        Player<?> p2 = db.player("foo").get();
        assertEquals(p1, p2);

        Player<?> p3 = db.createPlayer("bar", game);
        assertNotEquals(p1, p3);
    }
}

