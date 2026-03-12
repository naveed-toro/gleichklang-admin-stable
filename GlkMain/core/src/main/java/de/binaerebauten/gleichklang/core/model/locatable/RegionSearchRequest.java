package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionAnswer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "region_search_request")
public class RegionSearchRequest extends BaseEntity
{
	@ManyToOne(cascade = CascadeType.DETACH)
	@NotNull
	private Continent continent;
	
	@ManyToOne(cascade = CascadeType.DETACH)
	private Country country;
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinTable(name = "region_search_request_restriction",
			joinColumns = { @JoinColumn(name = "region_search_request_id") },
			inverseJoinColumns = { @JoinColumn(name = "locatable_id") })
	private Set<Region> restrictions = new HashSet<>();
	
	@ManyToOne
	@NotNull
	private RegionAnswer answer;
	
	public RegionAnswer getAnswer()
	{
		return answer;
	}
	
	public void setAnswer(RegionAnswer answer)
	{
		this.answer = answer;
	}
	
	public Continent getContinent()
	{
		return continent;
	}
	
	public void setContinent(Continent continent)
	{
		this.continent = continent;
	}
	
	public Country getCountry()
	{
		return country;
	}
	
	public void setCountry(Country country)
	{
		this.country = country;
	}
	
	public Set<Region> getRestrictions()
	{
		return restrictions;
	}
	
	public void setRestrictions(Set<Region> restrictions)
	{
		this.restrictions = restrictions;
	}
	
	public void addRestriction(Region restriction)
	{
		restrictions.add(restriction);
	}
	
	public String getName()
	{
		String name = continent.getName();
		if(country != null) name += " > " + country.getName();
		if(restrictions != null)
		{
			if(!restrictions.isEmpty()) name += " > ";
			name += restrictions.stream().map(Region::getName).collect(Collectors.joining(", "));
		}
		
		return name;
	}
}
