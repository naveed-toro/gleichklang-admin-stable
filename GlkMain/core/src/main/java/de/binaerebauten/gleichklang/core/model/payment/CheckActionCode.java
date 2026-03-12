package de.binaerebauten.gleichklang.core.model.payment;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckActionCodeValidator.class)
@Documented
public @interface CheckActionCode
{
	String message() default "Aktionscode darf für Sozialangebote nicht leer sein";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
