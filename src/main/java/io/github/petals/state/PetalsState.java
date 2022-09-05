package io.github.petals.state;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.github.petals.Base;
import io.github.petals.Metadata;

public class PetalsState implements InvocationHandler {
    private final Base owner;
    private final Metadata meta;

    public PetalsState(Base owner, Metadata meta) {
        this.owner = owner;
        this.meta = meta;
    }

    public static <T extends State<?>> T createProxy(Class<T> state, Base owner, Metadata meta) {
        return (T) Proxy.newProxyInstance(
            state.getClassLoader(),
            new Class[] { state },
            new PetalsState(owner, meta)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "owner":
                return this.owner;
            case "raw":
                return this.meta;
        }

        final Getter getter = method.getAnnotation(Getter.class);
        if (getter != null) {
            final String key = getter.value();
            final Class<?> rt = method.getReturnType();
            if (rt == boolean.class) return meta.containsKey(key);

            String value = this.meta.get(key);
            if (rt == String.class) return value;
            if (Enum.class.isAssignableFrom(rt)) return Enum.valueOf((Class<? extends Enum>) rt, value);

            value = value == null ? "0" : value;
            if (rt == byte.class) return Byte.valueOf(value);
            if (rt == short.class) return Short.valueOf(value);
            if (rt == int.class) return Integer.valueOf(value);
            if (rt == long.class) return Long.valueOf(value);
            if (rt == float.class) return Float.valueOf(value);
            if (rt == double.class) return Double.valueOf(value);

            throw new ClassCastException(String.format("Cannot deserialize value %s with type %s", key, rt.getName()));
        }

        final Setter setter = method.getAnnotation(Setter.class);
        if (setter != null) {
            final String key = setter.value();
            final Class<?> param = method.getParameters()[0].getType();

            if (Enum.class.isAssignableFrom(param)) {
                this.meta.put(key, ((Enum<?>) args[0]).name());
                return null;
            }

            final String value = String.valueOf(args[0]);
            if (param == boolean.class) {
                if (Boolean.parseBoolean(value)) this.meta.put(key, "1");
                else this.meta.remove(key);
            }
            else this.meta.put(key, value);
        }

        return null;
    }
}

