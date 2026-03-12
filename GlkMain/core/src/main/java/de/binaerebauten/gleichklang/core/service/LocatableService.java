package de.binaerebauten.gleichklang.core.service;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocatableService implements LocatableHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(LocatableService.class);
	
	private final LocatableRepository locatableRepository;
	private final MessageService messageService;
	
	@Autowired
	public LocatableService(LocatableRepository locatableRepository, MessageService messageService)
	{
		this.locatableRepository = Objects.requireNonNull(locatableRepository);
		this.messageService = Objects.requireNonNull(messageService);
	}
	
	@Override
	public List<Continent> getContinentsWithZips()
	{
		return this.locatableRepository.findContinentWithZips();
	}
	
	@Override
	public List<Continent> getContinents()
	{
		return locatableRepository.findByType(Continent.class);
	}
	
	@Override
	public List<Country> getCountries(Continent continent)
	{
		final List<Country> countryList = locatableRepository.findByParentAndType(continent, Country.class);
		countryList.sort(Comparator.comparing(Country::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Country::getName));
		
		return countryList;
	}
	
	@Override
	public List<Country> getCountriesWithZips(@NotNull Continent continent)
	{
		final List<Country> countryList = locatableRepository.findCountriesWithZips(continent);
		countryList.sort(Comparator.comparing(Country::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Country::getName));
		
		return countryList;
	}
	
	@Override
	public List<Region> getRegions(Country country)
	{
		return locatableRepository.findByParentAndType(country, Region.class);
	}
	
	@Override
	public List<Zip> getZips(Country country)
	{
		return getUniqueZips(locatableRepository.findOrderedZips(country));
	}
	
	@Override
	public List<Zip> getZips(Region region)
	{
		return getUniqueZips(locatableRepository.findOrderedZips(region));
	}


	public List<Zip> getUniqueZips(List<Zip> orderedZips)
	{
		List<Zip> orderedZipsFinal=new ArrayList<>();

		Map<String,Zip> zipMap= new LinkedHashMap<>();

		for(Zip zip:orderedZips)
		{
			if(!zipMap.containsKey(zip.getZip()))
			{
				zipMap.put(zip.getZip(),zip);
			}
		}


		for(Zip zip:zipMap.values())
		{
			orderedZipsFinal.add(zip);
		}
		return orderedZipsFinal;
	}
	public void notifyMissingZips(User user)
	{
		final List<Address> addressesWithTmpZip = user.getAddresses().stream().filter(address -> !Strings.isNullOrEmpty(address.getTmpZip())).collect(Collectors.toList());
		
		if (addressesWithTmpZip.isEmpty())
			return;
		
		final StringJoiner missingZips = new StringJoiner("\n");
		for (Address address : addressesWithTmpZip)
		{
			missingZips.add(String.format("%s (%s, %s, %s)",
					address.getTmpZip(),
					address.getContinent() != null ? address.getContinent().getName() : "-no continent selected-",
					address.getRegion() != null ? address.getRegion().getName() : "-no region selected-",
					address.getCountry() != null ? address.getCountry().getName() : "-no country selected-"));
		}
		
		final Message message = messageService.createNewMessage(user);
		message.setMessageType(MessageType.NEW_ZIP);
		message.setSubject(I18N.LOCATABLESERVICE_MESSAGE_NEWZIPTITLE.msg());
		message.setBody(I18N.LOCATABLESERVICE_MESSAGE_NEWZIPCONTENT.msg(user.getAlias(), user.getEmail(), missingZips.toString()));
		
		try
		{
			messageService.sendMessageToAdmin(message, null);
		}
		catch (ValidationException e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	public String[] getRegions()
	{
		return locatableRepository.getRegions().toArray(new String[0]);
	}
}
