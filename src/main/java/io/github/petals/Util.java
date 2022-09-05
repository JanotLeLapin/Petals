package io.github.petals;

import io.github.petals.state.PetalsState;
import io.github.petals.state.State;
import redis.clients.jedis.JedisPooled;

public class Util {
    public static boolean isStateAssignable(String key, Class<? extends State> state, JedisPooled pooled) {
        String roleName = pooled.hget(key, "state");
        try {
            Class<?> sc = Class.forName(roleName);
            return state.isAssignableFrom(sc);
        } catch (ClassNotFoundException e) {}
        return false;
    }

    public static Object createState(Base base, JedisPooled pooled) {
        Class<? extends State<?>> clazz;
        try {
            clazz = (Class<? extends State<?>>) Class.forName(pooled.hget(base.uniqueId(), "state"));
        } catch (NullPointerException | ClassNotFoundException | ClassCastException e) {
            clazz = (Class<? extends State<?>>) (Class<?>) State.class;
        }

        return PetalsState.createProxy(clazz, base, new Metadata(base.uniqueId() + ":state", pooled));
    }
}

