package io.github.petals;

import org.bukkit.plugin.Plugin;

import io.github.petals.Game.Player;

public interface Petal extends Plugin {
    /** Executed when a new game is created */
    public void onCreateGame(Game game);
    /** Executed when a game is started */
    public void onStartGame(Game game);
    /** Executed when a game is stopped */
    public void onStopGame(Game game);

    /** Executed when a player is added to a game */
    public void onAddPlayer(Player player);
    /** Executed when a player is removed from a game */
    public void onRemovePlayer(Player player);
}

