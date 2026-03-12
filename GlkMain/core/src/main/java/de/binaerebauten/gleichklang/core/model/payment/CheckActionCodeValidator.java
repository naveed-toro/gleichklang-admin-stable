package de.binaerebauten.gleichklang.core.model.payment;

import com.google.common.base.Strings;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckActionCodeValidator
		implements ConstraintValidator<CheckActionCode, InitialSubscriptionOffer>
{
	@Override
	public void initialize(CheckActionCode constraintAnnotation)
	{
	}

	@Override
	public boolean isValid(InitialSubscriptionOffer subscriptionOffer, ConstraintValidatorContext context)
	{
		boolean isValid = true;

		if (subscriptionOffer == null)
		{
			isValid = true;
		}
		else if (Tariff.SOCIAL.equals(subscriptionOffer.getTariff()))
		{
			isValid = !Strings.isNullOrEmpty(subscriptionOffer.getActionCode());
			if (!isValid)
			{
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(I18N.CHECKACTIONCODE_CONSTRAINT_VIOLATION.msg())
						.addPropertyNode(I18N.CHECKACTIONCODE_CONSTRAINT_PROPERTY.msg()).addConstraintViolation();
			}

		}
		return isValid;
	}
}
