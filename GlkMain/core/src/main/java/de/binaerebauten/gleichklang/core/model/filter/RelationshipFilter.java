package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.XmlLocalDateTimeAdapter;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.RELATIONSHIP_FILTER;

/**
 * Filter for the count in the RelationshipTable {@link de.binaerebauten.gleichklang.core.model.matching.Relationship}
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class RelationshipFilter extends UserFilter
{
	@XmlType(name ="relationshipFilterDirection")
	public enum Direction
	{
		LESS_OR_EQUAL,
		GREATER
	}
	
	@XmlAttribute
	@XmlJavaTypeAdapter(XmlLocalDateTimeAdapter.class)
	@Column(name = "relationship_from")
	private LocalDateTime from;
	
	@XmlAttribute
	@XmlJavaTypeAdapter(XmlLocalDateTimeAdapter.class)
	@Column(name = "relationship_to")
	private LocalDateTime to;
	
	@XmlAttribute
	@NotNull
	private int threshold = 0;
	
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@NotNull
	private Direction direction = Direction.LESS_OR_EQUAL;
	
	public LocalDateTime getFrom()
	{
		return from;
	}
	
	public void setFrom(LocalDateTime from)
	{
		this.from = from;
	}
	
	public LocalDateTime getTo()
	{
		return to;
	}
	
	public void setTo(LocalDateTime to)
	{
		this.to = to;
	}
	
	public int getThreshold()
	{
		return threshold;
	}
	
	public void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}
	
	public Direction getDirection()
	{
		return direction;
	}
	
	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
	
	@Override
	public String getName()
	{
		return RELATIONSHIP_FILTER.toString();
	}
}
