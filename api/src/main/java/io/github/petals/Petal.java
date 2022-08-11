package io.github.petals;

import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;

/** A plugin depending on Petals */
public interface Petal extends Plugin {
    /**
     * Executed when a game is created
     *
     * @param game The game
     */
    public void onCreateGame(Game game);
    /**
     * Executed when a game is started
     *
     * @param game The game
     */
    public void onStartGame(Game game);
    /**
     * Executed when a game is stopped
     *
     * @param game The game
     */
    public void onStopGame(Game game);

    /**
     * Executed when a player is added to a game
     *
     * @param player The player
     */
    public void onAddPlayer(Player player);
    /**
     * Executed when a player is removed from a
     *
     * @param player The player
     */
    public void onRemovePlayer(Player player);
}

