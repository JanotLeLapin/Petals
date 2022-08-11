package io.github.petals;

import java.util.Set;

import org.bukkit.OfflinePlayer;

public interface Game {
    public static interface Player {
        public String uniqueId();
        /** Whether this handle references a valid player in the database */
        public boolean exists();
        public Game game();
        /** Bukkit representation of this player */
        public OfflinePlayer player();
        /** Removes the player from his game, then deletes him from the database */
        public void delete();
    }

    public static interface World {
        public String name();
        /** Whether this handle references a valid world in the database */
        public boolean exists();
        public Game game();
        /** Bukkit representation of this world */
        public org.bukkit.World world();
        /** Removes the world from its game, then deletes it from the database */
        public void delete();
    }

    // Basic
    public String uniqueId();
    /** Whether this handle references a valid game in the database */
    public boolean exists();
    /** Whether the game has started yet */
    public boolean running();
    /** The amount of ticks that elapsed since the game started */
    public long ticks();
    /** The {@link Petal} plugin managing this game */
    public Petal plugin();
    /** Deletes all players and worlds from the game then deletes the game */
    public void delete();
    // Players
    /** Player hosting this game */
    public Player host();
    /** Every player in this game */
    public Set<Player> players();
    public Player player(String uniqueId);
    public Player addPlayer(String uniqueId);
    // Worlds
    /** Main world for this game */
    public World home();
    /** Every world in this game */
    public Set<World> worlds();
    public World world(String name);
    public World addWorld(String name);
}

