package de.binaerebauten.gleichklang.core.security;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks string fields of an entity for sanitizing.
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface SanitizeContent
{
}
