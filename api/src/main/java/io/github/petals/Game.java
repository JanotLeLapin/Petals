package io.github.petals;

import java.util.Optional;
import java.util.Set;

import org.bukkit.plugin.Plugin;

import io.github.petals.state.State;

/** Reference to a game stored in the database */
public interface Game<T extends State<?>> extends Base {
    /** Reference to a player stored in the database */
    public static interface Player<U extends State<?>> extends Base {
        /** @return whether this handle references a valid player in the database */
        public boolean exists();
        /** @return the game linked to this player */
        public Game<? extends State<?>> game();
        /** @return the Bukkit representation of this player */
        public Optional<org.bukkit.entity.Player> player();
        /** @return a state object, allows you to interact with this player's state */
        public U state();
        /**
         * Updates the state type for this player
         *
         * @param <S> The new state type
         * @param state The state class to link to this player
         * @return An instance of the given state class
         */
        public <S extends State<?>> S state(Class<S> state);
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
        public Game<?> game();
        /** @return the Bukkit representation of this world */
        public org.bukkit.World world();
        /** Removes the world from its game, then deletes it from the database */
        public void delete();
    }

    /** Task scheduler for a game */
    public static interface Scheduler {
        /**
         * Schedules a given runnable
         *
         * @param plugin The plugin
         * @param delay The amount of millis to wait before running
         * @param runnable The runnable
         * @return A Petals Task
         */
        public long runTaskLater(Plugin plugin, long delay, Runnable runnable);
        /**
         * Schedules a given runnable
         *
         * @param plugin The plugin
         * @param delay The amount of millis to wait before running
         * @param period The amount of millis between each run
         * @param runnable The runnable
         * @return A Petals Task
         */
        public long runTaskTimer(Plugin plugin, long delay, long period, Runnable runnable);
        /**
         * Kills and removes a task associated with the given task ID
         *
         * @param taskId The task ID
         */
        public void cancel(long taskId);
        /** Removes each task associated with this scheduler */
        public void clear();
    }

    // Basic
    /** @return the universally unique identifier of this game */
    public String uniqueId();
    /** @return whether this handle references a valid game in the database */
    public boolean exists();
    /** @return whether the game has started yet */
    public boolean running();
    /** @return the amount of millis that elapsed since the game started */
    public long time();
    /** @return the {@link Petal} plugin managing this game */
    public Petal plugin();
    /** @return the {@link Scheduler} for this game */
    public Scheduler scheduler();
    /** @return the {@link State} object for this game */
    public T state();
    /**
     * Updates the {@link State} object for this game
     *
     * @param <U> The type of the new state
     * @param state The new state class
     * @return The new state
     */
    public <U extends State<?>> U state(Class<U> state);
    /** Deletes all players and worlds from the game then deletes the game */
    public void delete();
    // Players
    /** @return the player hosting this game */
    public Player<? extends State<?>> host();
    /** @return every player in this game */
    public Set<Player<? extends State<?>>> players();
    /**
     * Finds each player stored in the database with given state type
     *
     * @param <U> The state type
     * @param state The state class to match
     * @return A set of player handles
     */
    public <U extends State<?>> Set<Player<U>> players(Class<U> state);
    /**
     * Finds a player in this game from its ID
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Optional<Player<State<?>>> player(String uniqueId);
    /**
     * Finds a player in this game from a Bukkit Player object
     *
     * @param player The Bukkit Player object
     * @return The player handle
     */
    public Optional<Player<State<?>>> player(org.bukkit.entity.Player player);
    /**
     * Finds a player in this game from ID and state type
     *
     * @param <U> The state type
     * @param uniqueId The player ID to lookup
     * @param state The state class to match
     * @return The player handle
     */
    public <U extends State<?>> Optional<Player<U>> player(String uniqueId, Class<U> state);
    /**
     * Finds a player in this game from a Bukkit Player object and a state type
     *
     * @param <U> The state type
     * @param player The Bukkit Player object
     * @param state The state class to match
     * @return The player handle
     */
    public <U extends State<?>> Optional<Player<U>> player(org.bukkit.entity.Player player, Class<U> state);
    /**
     * Creates a player on the database and links it to this game
     *
     * @param uniqueId The player ID
     * @return The player handle
     */
    public Player<State<?>> addPlayer(String uniqueId) throws IllegalStateException;
    /**
     * Creates a player on the database and links it to this game
     *
     * @param player The Bukkit Player object
     * @return The player handle
     */
    public Player<State<?>> addPlayer(org.bukkit.entity.Player player) throws IllegalStateException;
    // Worlds
    /** @return every world in this game */
    public Set<World> worlds();
    /**
     * Finds a world stored in the database from its name
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
    public World addWorld(String name) throws IllegalStateException;
    /** Resets the ticks for this game */
    public void start();
}

