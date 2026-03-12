package de.binaerebauten.gleichklang.core.service.mail;

import de.binaerebauten.gleichklang.core.config.VelocityServiceTestConfig;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VelocityServiceTestConfig.class })
public class MailTemplateInstanceTest
{
	@Autowired
	private TemplateEngineService templateEngineService;

	@Test
	public void testCreateInstance()
	{
		for (UserMailTemplate userMailTemplate : EnumSet.of(UserMailTemplate.SOCIAL_REGISTERED_USER))
		{
			for (Language language : EnumSet.of(Language.EN, Language.DE))
			{
				User user = new User();

				user.setEmail("test@example.com");
				user.setAlias("Hey Joe");

				MailTemplateInstance mailTemplateInstance = MailTemplateInstance.create(templateEngineService, user, userMailTemplate, language);

				assertThat(mailTemplateInstance, notNullValue());

				assertThat(mailTemplateInstance.getSubject(), not(emptyOrNullString()));
				assertThat(mailTemplateInstance.getContent(), not(emptyOrNullString()));
			}
		}
	}
}
