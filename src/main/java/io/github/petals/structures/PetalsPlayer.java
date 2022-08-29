package io.github.petals.structures;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.petals.Game;
import io.github.petals.Metadata;
import io.github.petals.role.Role;
import io.github.petals.role.RoleMeta;
import io.github.petals.role.RoleSpec;
import redis.clients.jedis.JedisPooled;

public class PetalsPlayer<T extends Role> implements Game.Player<T> {
    private String uniqueId;
    private T role = null;
    private JedisPooled pooled;

    public PetalsPlayer(String uniqueId, JedisPooled pooled) {
        this.uniqueId = uniqueId;
        this.pooled = pooled;
    }

    @Override
    public String uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean exists() {
        return this.uniqueId == null ? false : this.pooled.sismember("players", this.uniqueId);
    }

    @Override
    public Game game() {
        return new PetalsGame(this.pooled.hget(this.uniqueId, "game"), pooled);
    }

    @Override
    public Optional<Player> player() {
        Player p = Bukkit.getPlayer(UUID.fromString(this.uniqueId));
        return p == null ? Optional.empty() : Optional.of(p);
    }

    @Override
    public Map<String, String> meta() {
        return new Metadata(this.uniqueId + ":meta", this.pooled);
    }

    @Override
    public T role() {
        if (this.role != null) return this.role;

        // Cache role
        String roleClass = pooled.hget(uniqueId, "role");
        if (roleClass != null) {
            try {
                final Class<?> role = Class.forName(pooled.hget(uniqueId, "role"));
                if (!Role.class.isAssignableFrom(role)) throw new ClassCastException(String.format("%s does not implement Role", role.getName()));
                if (!role.isInterface()) throw new ClassCastException(String.format("%s is not an interface", role.getName()));

                final RoleSpec spec = role.getDeclaredAnnotation(RoleSpec.class);
                if (spec == null) throw new ClassCastException(String.format("%s is not annotated with io.github.petals.role.RoleSpec", role.getName()));

                T instance = (T) Proxy.newProxyInstance(
                    role.getClassLoader(),
                    new Class[] { role },
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "player":
                                return this;
                            case "name":
                                return spec.name();
                            case "description":
                                return spec.description();
                        }

                        final RoleMeta meta = method.getDeclaredAnnotation(RoleMeta.class);
                        final String key = meta.key().isEmpty() ? method.getName() : meta.key();
                        if (meta != null) {
                            if (method.getParameterCount() == 0) {
                                final Class<?> returnType = method.getReturnType();
                                if (returnType == boolean.class) return this.meta().containsKey(key);
                                else {
                                    String value = this.meta().get(key);
                                    if (returnType == String.class) return value;
                                    if (Enum.class.isAssignableFrom(returnType)) return Enum.valueOf((Class<? extends Enum>) returnType, value);

                                    value = value == null ? "0" : value;
                                    if (returnType == byte.class) return Byte.valueOf(value);
                                    if (returnType == short.class) return Short.valueOf(value);
                                    if (returnType == int.class) return Integer.valueOf(value);
                                    if (returnType == long.class) return Long.valueOf(value);
                                    if (returnType == float.class) return Float.valueOf(value);
                                    if (returnType == double.class) return Double.valueOf(value);
                                }

                                throw new ClassCastException(String.format("Cannot deserialize value %s with type %s", key, returnType.getName()));
                            } else {
                                final Class<?> param = method.getParameters()[0].getType();
                                final String value = String.valueOf(args[0]);

                                if (param == boolean.class) {
                                    if (Boolean.parseBoolean(value)) this.meta().put(key, "1");
                                    else this.meta().remove(key);
                                } else if (param == Enum.class) this.meta().put(key, ((Enum<?>) args[0]).name());
                                else this.meta().put(key, value);
                            }
                        }

                        return null;
                    }
                );

                this.role = instance;
            } catch (ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
            }
        }

        return this.role;
    }

    @Override
    public void role(Class<? extends Role> role) {
        this.pooled.hset(this.uniqueId, "role", role.getName());
    }

    @Override
    public void delete() {
        this.game().plugin().onRemovePlayer((Game.Player<Role>) this);

        this.meta().clear();

        this.pooled.srem("players", this.uniqueId);
        this.pooled.del(this.uniqueId);
    }
}

