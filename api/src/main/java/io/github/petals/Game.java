package io.github.petals;

import java.util.Set;

import org.bukkit.OfflinePlayer;

/** Reference to a game stored in the database */
public interface Game {
    /** Reference to a player stored in the database */
    public static interface Player {
        /** @return the universally unique identifier of this player */
        public String uniqueId();
        /** @return whether this handle references a valid player in the database */
        public boolean exists();
        /** @return the game linked to this player */
        public Game game();
        /** @return the Bukkit representation of this player */
        public OfflinePlayer player();
        /** Removes the player from his game, then deletes him from the database */
        public void delete();
    }

    /** Reference to a world stored in the database */
    public static interface World {
        /** @return the name of this world */
        public String name();
        /** @return whether this handle references a valid world in the database */
        public boolean exists();
        /** @return the game linked to this world */
        public Game game();
        /** @return the Bukkit representation of this world */
        public org.bukkit.World world();
        /** Removes the world from its game, then deletes it from the database */
        public void delete();
    }

    // Basic
    /** @return the universally unique identifier of this game */
    public String uniqueId();
    /** @return whether this handle references a valid game in the database */
    public boolean exists();
    /** @return whether the game has started yet */
    public boolean running();
    /** @return the amount of ticks that elapsed since the game started */
    public long ticks();
    /** @return the {@link Petal} plugin managing this game */
    public Petal plugin();
    /** Deletes all players and worlds from the game then deletes the game */
    public void delete();
    // Players
    /** @return the player hosting this game */
    public Player host();
    /** @return every player in this game */
    public Set<Player> players();
    /**
     * Finds a player stored in the database from its ID. Although never null, it may not exist
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Player player(String uniqueId);
    /**
     * Creates a player on the database and links it to this game
     *
     * @param uniqueId The player ID
     * @return The player handle
     */
    public Player addPlayer(String uniqueId);
    // Worlds
    /** @return main world for this game */
    public World home();
    /** @return every world in this game */
    public Set<World> worlds();
    /**
     * Finds a world stored in the database from its name. Although never null, it may not exist
     *
     * @param name The world name to lookup
     * @return The world handle
     */
    public World world(String name);
    /**
     * Creates a world on the database and links it to this game
     *
     * @param name The world name
     * @return The world handle
     */
    public World addWorld(String name);
}

