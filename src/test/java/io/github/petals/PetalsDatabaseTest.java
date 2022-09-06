package io.github.petals;

import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.*;

import io.github.petals.state.State;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

@Testcontainers
public class PetalsDatabaseTest extends TestUtil {
    @Test
    public void games() {
        // No games
        assertEquals(0, db.games().size());

        // Created a game -> one game
        Game<State<?>> g1 = db.createGame("host1", plugin);
        assertEquals(1, db.games().size());
        assertEquals("host1", g1.host().uniqueId());

        // Created a game -> two games
        Game<State<?>> g2 = db.createGame("host2", plugin);
        assertEquals(2, db.games().size());
        assertEquals("host2", g2.host().uniqueId());
        assertEquals("host1", g1.host().uniqueId());

        // Deleted a game -> one game
        g1.delete();
        assertEquals(1, db.games().size());
        assertEquals(false, g1.exists());
        assertEquals(Optional.empty(), db.player("host1"));
        assertEquals("host2", g2.host().uniqueId());
    }

    @Test
    public void duplicates() {
        // Same host for two games
        Game<State<?>> g1 = db.createGame("host1", plugin);
        assertThrows(IllegalStateException.class, () -> db.createGame("host1", plugin));
        g1.delete();
        db.createGame("host1", plugin);

        // Add host to game
        Game<State<?>> g2 = db.createGame("host2", plugin);
        assertThrows(IllegalStateException.class, () -> g2.addPlayer("host2"));
        g2.delete();

        // Add same player twice to game
        Game<State<?>> g3 = db.createGame("host3", plugin);
        g3.addPlayer("player3");
        assertThrows(IllegalStateException.class, () -> g3.addPlayer("player3"));

        // Add same player to two games
        Game<State<?>> g4a = db.createGame("host4a", plugin);
        Game<State<?>> g4b = db.createGame("host4b", plugin);
        g4a.addPlayer("player4a");
        assertThrows(IllegalStateException.class, () -> g4b.addPlayer("player4a"));
    }

    @Test
    public void customImplementation() {
        // Add metadata property to game host when created
        doAnswer(i ->
            ((Game<State<?>>) i.getArgument(0, State.class).owner()).host().state().raw().put("foo", "bar")
        ).when(plugin).onCreateGame(any());
        Game<State<?>> g1 = petals.database().createGame("host1", plugin);
        assertEquals("bar", g1.host().state().raw().get("foo"));

        // Do nothing when game created
        doNothing().when(plugin).onCreateGame(any());
        Game<State<?>> g2 = petals.database().createGame("host2", plugin);
        assertNull(g2.host().state().raw().get("foo"));
    }
}

