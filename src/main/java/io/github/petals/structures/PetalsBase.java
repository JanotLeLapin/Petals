package io.github.petals.structures;

import io.github.petals.Base;

public class PetalsBase {
    protected final String uniqueId;

    public PetalsBase(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String uniqueId() {
        return this.uniqueId;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getName(), this.uniqueId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Base ? ((Base) obj).uniqueId().equals(this.uniqueId()) : false;
    }
}

