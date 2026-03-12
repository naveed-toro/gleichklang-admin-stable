package de.binaerebauten.gleichklang.core.utils;

import javax.transaction.Transactional;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is our customized @Transactional annotation for methods that may throw
 * a checked exception.
 *
 * The default of spring is to not rollback a transaction that throws a checked exception,
 * but we decided to throw checked exceptions for validation
 * {@link de.binaerebauten.gleichklang.core.service.validator.ValidationException}
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD})
@Transactional(rollbackOn = Exception.class)
public @interface CheckedTransactional
{
}
