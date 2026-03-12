package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.locatable.ProximitySearchRequest;
import de.binaerebauten.gleichklang.core.model.locatable.RegionSearchRequest;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class RegionAnswer extends Answer
{
	@NotFound(action = NotFoundAction.IGNORE)
	@OneToMany(mappedBy = "answer", fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<RegionSearchRequest> regionSearchRequests = new ArrayList<>();
	
	@OneToMany(mappedBy = "answer", fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<ProximitySearchRequest> proximitySearchRequests = new ArrayList<>();
	
	@Column(name = "search_relocatable")
	private boolean searchRelocatable = false;
	
	@Column(name = "relocatable")
	private boolean relocatable = false;
	
	public List<RegionSearchRequest> getRegionSearchRequests()
	{
		return regionSearchRequests;
	}
	
	public void setRegionSearchRequests(List<RegionSearchRequest> regionSearchRequests)
	{
		this.regionSearchRequests = regionSearchRequests;
	}
	
	public void addRegionSearchRequest(RegionSearchRequest regionSearchRequest)
	{
		regionSearchRequests.add(regionSearchRequest);
		regionSearchRequest.setAnswer(this);
	}
	
	public List<ProximitySearchRequest> getProximitySearchRequests()
	{
		return proximitySearchRequests;
	}
	
	public void setProximitySearchRequests(List<ProximitySearchRequest> proximitySearchRequests)
	{
		this.proximitySearchRequests = proximitySearchRequests;
	}
	
	public void addProximitySearchRequest(ProximitySearchRequest proximitySearchRequest)
	{
		proximitySearchRequests.add(proximitySearchRequest);
		proximitySearchRequest.setAnswer(this);
	}
	
	public boolean isSearchRelocatable()
	{
		return searchRelocatable;
	}
	
	public void setSearchRelocatable(boolean searchRelocatable)
	{
		this.searchRelocatable = searchRelocatable;
	}
	
	public boolean isRelocatable()
	{
		return relocatable;
	}
	
	public void setRelocatable(boolean relocatable)
	{
		this.relocatable = relocatable;
	}

	@Override
	public String getValue()
	{
		final String region = regionSearchRequests.stream().map(RegionSearchRequest::getName).collect(Collectors.joining("\n"));
		final String proximity = proximitySearchRequests.stream().map(ProximitySearchRequest::getName).collect(Collectors.joining("\n"));
		
		return String.join("\n----\n", region, proximity);
	}

	@Override
	public boolean isAnswered()
	{
		return true;
	}
	
	@Override
	public RegionQuestion getQuestion()
	{
		return (RegionQuestion) super.getQuestion();
	}
}
