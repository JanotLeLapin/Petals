package io.github.petals;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.scheduler.BukkitTask;

import io.github.petals.role.Role;

/** Reference to a game stored in the database */
public interface Game {
    /** Reference to a player stored in the database */
    public static interface Player<T extends Role> {
        /** @return the universally unique identifier of this player */
        public String uniqueId();
        /** @return whether this handle references a valid player in the database */
        public boolean exists();
        /** @return the game linked to this player */
        public Game game();
        /** @return the Bukkit representation of this player */
        public Optional<org.bukkit.entity.Player> player();
        /** @return the custom metadata of this player */
        public Map<String, String> meta();
        /** @return the role for this player. If your games doesn't depend on roles this might be null */
        public T role();
        /**
         * Updates the role for this player
         *
         * @param role The role class to link to this player
         */
        public void role(Class<? extends Role> role);
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

    /** Task scheduler for a game */
    public static interface Scheduler {
        /**
         * Schedules a given runnable
         *
         * @param delay The amount of ticks to wait before running
         * @param runnable The runnable
         * @return A Bukkit task
         */
        public BukkitTask runTaskLater(long delay, Runnable runnable);
        /**
         * Schedules a given runnable
         *
         * @param delay The amount of ticks to wait before running
         * @param period The amount of ticks between each run
         * @param runnable The runnable
         * @return A Bukkit task
         */
        public BukkitTask runTaskTimer(long delay, long period, Runnable runnable);
        /**
         * Removes a task associated with the given task ID
         *
         * @param taskId The task ID
         */
        public void cancel(int taskId);
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
    /** @return the amount of ticks that elapsed since the game started */
    public long ticks();
    /** @return the {@link Petal} plugin managing this game */
    public Petal plugin();
    /** @return the {@link Scheduler} for this game */
    public Scheduler scheduler();
    /** Deletes all players and worlds from the game then deletes the game */
    public void delete();
    // Players
    /** @return the player hosting this game */
    public Player<Role> host();
    /** @return every player in this game */
    public Set<Player<Role>> players();
    /**
     * Finds a player stored in the database from its ID
     *
     * @param uniqueId The player ID to lookup
     * @return The player handle
     */
    public Optional<Player<Role>> player(String uniqueId);
    /**
     * Finds each player stored in the database with given role
     *
     * @param <T> The role type
     * @param role The role class to match
     * @return A set of player handles
     */
    public <T extends Role> Set<Player<T>> players(Class<T> role);
    /**
     * Finds a player stored in the database from ID and role
     *
     * @param <T> The role type
     * @param uniqueId The player ID to lookup
     * @param role The role class to match
     * @return The player handle
     */
    public <T extends Role> Optional<Player<T>> player(String uniqueId, Class<T> role);
    /**
     * Creates a player on the database and links it to this game
     *
     * @param uniqueId The player ID
     * @return The player handle
     */
    public Player<Role> addPlayer(String uniqueId) throws IllegalStateException;
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
}

