package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.I18N;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionAnswer;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.NoSuchElementException;

@Entity
@Table(name = "proximity_search_request")
public class ProximitySearchRequest extends BaseEntity
{
	public enum ProximityDistance implements DefaultEnumI18N
	{
		TINY(50),
		SMALL(100),
		MEDIUM(200),
		LARGE(300);
		
		private final int distance;
		
		ProximityDistance(int distance)
		{
			this.distance = distance;
		}
		
		public static ProximityDistance parse(int distance)
		{
			if (distance == 0)
			{
				return null;
			}
			
			for (ProximityDistance proximityDistance : values())
			{
				if (proximityDistance.getDistance() == distance)
				{
					return proximityDistance;
				}
			}
			
			throw new NoSuchElementException(String.format("No Proximity distance with distance %d exists", distance));
		}
		
		public int getDistance()
		{
			return distance;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "center_zip_id")
	@NotNull
	private Zip center;
	
	@Column
	@NotNull
	private Integer distance = null;
	
	@Column(name = "restrict_country")
	@NotNull
	private boolean restrictCountry = false;
	
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
	
	public Zip getCenter()
	{
		return center;
	}
	
	public void setCenter(Zip center)
	{
		this.center = center;
	}
	
	public ProximityDistance getDistance()
	{
		return distance != null ? ProximityDistance.parse(distance) : null;
	}
	
	public void setDistance(@NotNull ProximityDistance proximityDistance)
	{
		this.distance = proximityDistance.getDistance();
	}
	
	public boolean isRestrictCountry()
	{
		return restrictCountry;
	}
	
	public void setRestrictCountry(boolean restrictCountry)
	{
		this.restrictCountry = restrictCountry;
	}
	
	public String getName()
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (center == null || distance == null)
		{
			stringBuilder.append(I18N.LOCATABLE_PROXIMITY_NOT_ENOUGH_VALUES.msg());
			return stringBuilder.toString();
		}
		
		String restrictionString = "";
		if (isRestrictCountry())
		{
			restrictionString = "(" + center.getParent().getName() + ")";
		}
		
		stringBuilder.append(I18N.LOCATABLE_PROXIMITY.msg(center.getName(), distance, restrictionString));
		return stringBuilder.toString();
	}
}
