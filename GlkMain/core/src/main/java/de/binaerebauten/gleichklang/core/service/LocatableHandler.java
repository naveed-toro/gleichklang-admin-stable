package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface LocatableHandler
{
	List<Continent> getContinents();
	
	List<Country> getCountries(@NotNull Continent continent);
	
	List<Zip> getZips(@NotNull Country country);
	
	List<Zip> getZips(@NotNull Region region);
	
	List<Region> getRegions(@NotNull Country country);
	
	List<Continent> getContinentsWithZips();
	
	List<Country> getCountriesWithZips(@NotNull Continent continent);
}
