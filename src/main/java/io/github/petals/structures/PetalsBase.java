package io.github.petals.structures;

public class PetalsBase {
    protected final String uniqueId;

    public PetalsBase(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getName(), this.uniqueId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof PetalsBase ? ((PetalsBase) obj).uniqueId == this.uniqueId : false;
    }
}

