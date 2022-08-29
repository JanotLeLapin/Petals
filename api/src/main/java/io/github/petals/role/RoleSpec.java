package io.github.petals.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that describes the default properties for a role
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RoleSpec {
    /**
     * @return The name of the role.
     */
    public String name();
    /**
     * @return The description of the role.
     */
    public String description();
}

