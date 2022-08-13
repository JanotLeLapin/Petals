package io.github.petals.role;

import io.github.petals.Game.Player;

/**
 * Representation of a role for strategic UHC games
 */
public interface Role {
    /** @return The player associated with this role */
    public Player<Role> player();

    /** @return The name of this role */
    public String name();
    /** @return The description of this role */
    public String description();
}

