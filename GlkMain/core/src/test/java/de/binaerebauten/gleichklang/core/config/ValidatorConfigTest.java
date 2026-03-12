package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.model.filter.AgeFilter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ValidatorConfig.class)
public class ValidatorConfigTest
{
	@Autowired
	private LocalValidatorFactoryBean factory;
	
	@Ignore
	@Test
	public void test()
	{
		final Validator validator = factory.getValidator();
		final AgeFilter ageFilter = new AgeFilter();
		final Set<ConstraintViolation<AgeFilter>> violations = validator.validate(ageFilter);
		for (ConstraintViolation<AgeFilter> violation : violations)
		{
			System.out.println(violation.getMessage());
		}
	}
}
