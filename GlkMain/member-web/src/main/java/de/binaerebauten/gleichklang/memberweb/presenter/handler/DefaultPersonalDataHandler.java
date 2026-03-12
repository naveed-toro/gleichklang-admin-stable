package de.binaerebauten.gleichklang.memberweb.presenter.handler;

import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent.PersonalDataHandler;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

public class DefaultPersonalDataHandler implements PersonalDataHandler
{
	private final AnswerService answerService;
	private final UserService userService;
	private final LocatableService locatableService;
	
	private final User user;
	
	public DefaultPersonalDataHandler(ApplicationContext ctx, User user)
	{
		userService = ctx.getBean(UserService.class);
		answerService = ctx.getBean(AnswerService.class);
		locatableService = ctx.getBean(LocatableService.class);
		
		this.user = user;
	}
	
	@Override
	public Address getNewAddress(Collection<Address> addresses)
	{
		if (addresses.size() >= 10)
		{
			Notification.show(I18N.ADDRESS_LIMIT_WARN.msg(), Notification.Type.WARNING_MESSAGE);
			return null;
		}
		
		final Address newAddress = new Address();
		newAddress.setUser(user);
		if (addresses.isEmpty()) newAddress.setPayment(true);
		newAddress.setChecked(true);
		return newAddress;
	}
	
	@Override
	public void save(User user, Answer sexAnswer) throws UniqueValidationException
	{
		userService.save(user);
		answerService.save(sexAnswer);
		locatableService.notifyMissingZips(user);
	}
	
	@Override
	public List<Continent> getContinents()
	{
		return locatableService.getContinents();
	}
	
	@Override
	public List<Country> getCountries(Continent continent)
	{
		return locatableService.getCountries(continent);
	}
	
	@Override
	public List<Zip> getZips(Region region)
	{
		return locatableService.getZips(region);
	}
	
	@Override
	public List<Region> getRegions(Country country)
	{
		return locatableService.getRegions(country);
	}
	
	@Override
	public List<Zip> getZips(@NotNull Country country)
	{
		return locatableService.getZips(country);
	}
	
	@Override
	public List<Continent> getContinentsWithZips()
	{
		return locatableService.getContinentsWithZips();
	}
	
	@Override
	public List<Country> getCountriesWithZips(@NotNull Continent continent)
	{
		return locatableService.getCountriesWithZips(continent);
	}
	
	@Override
	public User getUser()
	{
		return user;
	}
	
	@Override
	public Answer getSexAnswer()
	{
		return answerService.getAnswers(user, NaturalKey.SEX, true).iterator().next();
	}
}
