package io.github.petals;

import io.github.petals.role.Role;
import redis.clients.jedis.JedisPooled;

public class Util {
    public static boolean isRoleAssignable(String playerId, Class<? extends Role> role, JedisPooled pooled) {
        String roleName = pooled.hget(playerId, "role");
        try {
            Class<?> roleClass = Class.forName(roleName);
            return role.isAssignableFrom(roleClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

