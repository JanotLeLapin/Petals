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
        public void delete();
    }

    public static interface World {
        public String name();
        public boolean exists();
        public Game game();
        public org.bukkit.World world();
        public void delete();
    }

    // Basic
    public UUID uniqueId();
    public boolean exists();
    public boolean running();
    public long ticks();
    public Petal plugin();
    public void delete();
    // Players
    public Player host();
    public Set<Player> players();
    // Worlds
    public World home();
    public Set<World> worlds();
}

