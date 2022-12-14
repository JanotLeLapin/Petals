package io.github.petals;

import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;
import io.github.petals.state.State;

/** A plugin depending on Petals */
public interface Petal extends Plugin {
    /**
     * Executed when a game is created
     *
     * @param game The game
     */
    public void onCreateGame(State<Game<?>> game);
    /**
     * Executed when a game is started
     *
     * @param game The game
     */
    public void onStartGame(State<Game<?>> game);
    /**
     * Executed when a game is being deleted
     *
     * @param game The game
     */
    public void onDeleteGame(State<Game<?>> game);

    /**
     * Executed when a player is added to a game
     *
     * @param player The player
     */
    public void onAddPlayer(State<Player<?>> player);
    /**
     * Executed when a player is removed from a
     *
     * @param player The player
     */
    public void onRemovePlayer(State<Player<?>> player);
}

