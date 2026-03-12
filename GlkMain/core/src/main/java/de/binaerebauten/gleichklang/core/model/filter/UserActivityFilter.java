package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.utils.XmlLocalDateAdapter;
import de.binaerebauten.gleichklang.core.utils.XmlLocalDateTimeAdapter;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class UserActivityFilter extends UserFilter
{
    @XmlAttribute
	@Column(name = "user_activity")
	@Enumerated(EnumType.STRING)
	@NotNull
	private UserActivity userActivity;

    @XmlAttribute
    @XmlJavaTypeAdapter(XmlLocalDateAdapter.class)
	@Column(name = "from_date")
	private LocalDate fromDate;

    @XmlAttribute
    @XmlJavaTypeAdapter(XmlLocalDateAdapter.class)
	@Column(name = "to_date")
	private LocalDate toDate;

    @XmlAttribute
	@Column(name = "activity_category")
	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public String getName()
	{
		final String period;
		final String category = this.category != null ? " (" + this.category.toString() + ")" : "";

		if (fromDate != null && toDate != null)
		{
			period = " " + fromDate.toString() + " - " + toDate.toString();
		}
		else
		{
			period = " " + (fromDate != null ? "ab " + fromDate.toString() : toDate != null ? "bis " + toDate.toString() : "");
		}

		return (userActivity != null ? userActivity.toString() : "") + period + category;
	}

	public UserActivity getUserActivity()
	{
		return userActivity;
	}

	public void setUserActivity(UserActivity userActivity)
	{
		this.userActivity = userActivity;
	}

	public LocalDate getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate)
	{
		this.fromDate = fromDate;
	}

	public LocalDate getToDate()
	{
		return toDate;
	}

	public void setToDate(LocalDate toDate)
	{
		this.toDate = toDate;
	}

	public RecommendationCategory getCategory()
	{
		return category;
	}

	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}
}
