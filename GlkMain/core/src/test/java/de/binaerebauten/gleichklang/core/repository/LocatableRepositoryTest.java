package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by michael on 04/09/15.
 */
public class LocatableRepositoryTest
		extends AbstractRepositoryTest<LocatableEntity>
{
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private LocatableRepository locatableRepository;

	private Continent defaultContinent;
	private Country defaultCountry;
	private Region defaultRegion;
	private Zip defaultZip;

	@Test
	public void testFindByType() throws Exception
	{
		assertThat(locatableRepository.findByType(Continent.class), Matchers.notNullValue());
		assertThat(locatableRepository.findByType(Country.class), Matchers.notNullValue());
		assertThat(locatableRepository.findByType(Region.class), Matchers.notNullValue());
		assertThat(locatableRepository.findByType(Zip.class), Matchers.notNullValue());
	}

	@Test
	public void testFindByParentAndType() throws Exception
	{
		List<Continent> continents = locatableRepository.findByType(Continent.class);
		assertThat(continents, Matchers.notNullValue());

		List<Country> countries = locatableRepository.findByParentAndType(continents.get(0), Country.class);
		assertThat(countries, Matchers.notNullValue());
		assertThat(locatableRepository.findByParentAndType(countries.get(0), Region.class), Matchers.notNullValue());
		assertThat(locatableRepository.findByParentAndType(countries.get(0), Zip.class), Matchers.notNullValue());
	}

	@Test
	public void testFindCountriesWithZips() throws Exception
	{
		Continent continentWithoutZips = defaultEntityFactory.persistDefaultContinent();
		assertThat(locatableRepository.findCountriesWithZips(defaultContinent).get(0), equalTo(defaultCountry));
		assertThat(locatableRepository.findCountriesWithZips(continentWithoutZips).isEmpty(), is(true));
	}

	private Zip createZip(Country country, String zip)
	{
		final Zip entity = defaultEntityFactory.persistDefaultZip(country);
		entity.setZip(zip);
		locatableRepository.save(entity);
		return entity;
	}

	@Test
	public void findZips()
	{
		final Country country = defaultEntityFactory.persistDefaultCountry();
		createZip(country, "zip1");
		createZip(country, "zip2");
		createZip(country, "zip3");
		createZip(country, "zip4");

		final List<Zip> zips = locatableRepository.findOrderedZips(country);
		Zip lastZip = null;
		for(Zip zip : zips){
			if(lastZip != null){
				assertThat(lastZip.getZip().compareTo(zip.getZip()) <= 0, CoreMatchers.is(true));
			}
			lastZip = zip;
		}
	}

	@Test
	public void testFindByCountryCode()
	{
		Country country = locatableRepository.findByCountryCode("");
		assertThat(country, nullValue());

		country = locatableRepository.findByCountryCode(defaultCountry.getCountryCode());
		assertThat(country, is (defaultCountry));
	}

	@Override
	protected Collection<LocatableEntity> getPersistedEntities()
	{
		List<LocatableEntity> locatableEntityList = new ArrayList<>();

		defaultContinent = defaultEntityFactory.persistDefaultContinent();
		defaultCountry = defaultEntityFactory.persistDefaultCountry(defaultContinent);
		defaultRegion = defaultEntityFactory.persistDefaultRegion(defaultCountry);
		defaultZip = defaultEntityFactory.persistDefaultZip(defaultRegion);

		locatableEntityList.add(defaultContinent);
		locatableEntityList.add(defaultCountry);
		locatableEntityList.add(defaultRegion);
		locatableEntityList.add(defaultZip);

		return locatableEntityList;
	}

	@Test
	public void testFindContinentWithZips() throws Exception
	{
		Continent continentWithoutZips = defaultEntityFactory.persistDefaultContinent();
		assertThat(locatableRepository.findContinentWithZips().get(0), equalTo(defaultContinent));
		assertThat(locatableRepository.findContinentWithZips().contains(continentWithoutZips), is(false));
	}

	@Override protected JpaRepository<LocatableEntity, Long> getRepository()
	{
		return locatableRepository;
	}

	@Override public void testDelete() throws Exception
	{
		locatableRepository.delete(defaultRegion);
		locatableRepository.delete(defaultZip);
		locatableRepository.delete(defaultCountry);
		locatableRepository.delete(defaultContinent);

		assertThat(locatableRepository.findAll().isEmpty(), is(true));
	}
}