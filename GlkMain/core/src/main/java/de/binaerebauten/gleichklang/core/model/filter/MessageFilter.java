package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.MESSAGE_FILTER;

/**
 * Filter for the count in the MessageTable {@link de.binaerebauten.gleichklang.core.model.message.Message}
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class MessageFilter extends UserFilter
{
	@XmlType(name = "messageFilterDirection")
	public enum Direction
	{
		LESS_OR_EQUAL,
		GREATER
	}
	
	public enum Directory
	{
		INCOMING,
		OUTGOING
	}
	
	@XmlValue
	@NotNull
	private int threshold = 0;
	
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@NotNull
	private Direction direction = Direction.LESS_OR_EQUAL;
	
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@NotNull
	private Directory directory;
	
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
	
	public Directory getDirectory()
	{
		return directory;
	}
	
	public void setDirectory(Directory directory)
	{
		this.directory = directory;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
	
	@Override
	public String getName()
	{
		return MESSAGE_FILTER.toString();
	}
}
