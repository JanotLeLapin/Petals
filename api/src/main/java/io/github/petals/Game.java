package io.github.petals;

import java.util.UUID;

public interface Game {
    public UUID uniqueId();
    public boolean exists();
    public boolean running();
    public long ticks();
}

