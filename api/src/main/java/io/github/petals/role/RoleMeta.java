package io.github.petals.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Utility annotation that maps a method to a Metadata key in the Redis store
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RoleMeta {
    /**
     * @return the Metadata key name. Defaults to the name of the method.
     */
    public String value() default "";
}

