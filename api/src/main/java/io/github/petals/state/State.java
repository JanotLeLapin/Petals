package io.github.petals.state;

import java.util.Map;

import io.github.petals.Base;

public interface State<T extends Base> {
    /** @return The Petal object bound to this state */
    public T owner();

    /** @return An object you may use to manipulate this state as a Map */
    public Map<String, String> raw();
}

