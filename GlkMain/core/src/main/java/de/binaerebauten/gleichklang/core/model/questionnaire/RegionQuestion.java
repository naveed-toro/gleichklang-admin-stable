package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class RegionQuestion extends Question
{
	@Column(name = "with_relocation")
	@NotNull
	private boolean withRelocation;
	
	public boolean isWithRelocation()
	{
		return withRelocation;
	}
	
	public void setWithRelocation(boolean withRelocation)
	{
		this.withRelocation = withRelocation;
	}
}
