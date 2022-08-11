package io.github.petals.event;

import org.bukkit.event.Listener;

/**
 * Special {@link Listener} variant that allows listening for events that fire during games
 * by adding a second {@link io.github.petals.Game} parameter to event handlers
 */
public interface GameListener extends Listener {}

