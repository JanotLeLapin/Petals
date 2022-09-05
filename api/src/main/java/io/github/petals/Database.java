package io.github.petals;

import java.util.Optional;
import java.util.Set;

import io.github.petals.Game.Player;
import io.github.petals.Game.World;
import io.github.petals.state.State;

/** Abstraction around the Redis database storing games, players and worlds */
public interface Database {
    /** @return whether the database is accessible */
    public boolean ping();

    /** @return a set containing every currently stored games */
    public Set<Game<State<?>>> games();

    /**
     * Finds a player stored in the database from its ID
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Optional<Player<State<?>>> player(String uniqueId);
    /**
     * Finds a player stored in the database from a Bukkit Player instance
     *
     * @param player The Bukkit Player object
     * @return The player handle
     */
    public Optional<Player<State<?>>> player(org.bukkit.entity.Player player);
    /**
     * Finds a player stored in the database from its ID and state type
     *
     * @param <T> The state type
     * @param uniqueId The player ID to lookup
     * @param state The state class to match
     * @return The player handle
     */
    public <T extends State<?>> Optional<Player<T>> player(String uniqueId, Class<T> state);
    /**
     * Finds a player stored in the database from a Bukkit Player instance and a state type
     *
     * @param <T> The state type
     * @param player The Bukkit Player object
     * @param state The state class to match
     * @return The player handle
     */
    public <T extends State<?>> Optional<Player<T>> player(org.bukkit.entity.Player player, Class<T> state);
    /**
     * Finds a world stored in the database from its name
     *
     * @param name The world name to lookup
     * @return The world handle
     */
    public Optional<World> world(String name);
    /**
     * Finds a world stored in the database from a Bukkit World instance
     *
     * @param world The Bukkit World object
     * @return The world handle
     */
    public Optional<World> world(org.bukkit.World world);

    /**
     * Creates a new game
     *
     * @param host The host player ID for this game
     * @param plugin The plugin responsible for this game
     * @return The game
     */
    public Game<State<?>> createGame(String host, Petal plugin) throws IllegalStateException;
}

