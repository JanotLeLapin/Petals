package io.github.petals;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

public interface Game {
    public static interface Player {
        public UUID uniqueId();
        public boolean exists();
        public Game game();
        public OfflinePlayer player();
    }

    // Basic
    public UUID uniqueId();
    public boolean exists();
    public boolean running();
    public long ticks();
    // Players
    public Player host();
    public Set<Player> players();
}

