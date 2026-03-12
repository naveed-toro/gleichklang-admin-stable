package de.binaerebauten.gleichklang.core.model.news;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.Filterable;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rgoerner on 10.12.15.
 */
@Entity
@Table(name = "news")
public class News extends BaseEntity implements Filterable
{
	@NotNull
	@Column(name = "title")
	private String title;

	@NotNull
	@Column(name = "teaser_text")
	private String teaserText;

	@NotNull
	@Column(name = "text")
	private String text;

	@NotNull
	@Column(name = "active")
	private boolean active = false;

	@NotNull
	@Column(name = "valid_from")
	private LocalDateTime validFrom;

	@NotNull
	@Column(name = "valid_to")
	private LocalDateTime validTo;

	@NotNull
	@Column(name = "language")
	private String language;

	@NotNull
	@Column(name = "email_notification")
	private boolean emailNotification = false;

	@Column(name = "email_send_date")
	private LocalDateTime emailSendDate;

	//the admin who sent the news via email
	@ManyToOne
	@JoinColumn(name = "admin_id")
	private Admin admin;

	@Formula("(SELECT COUNT(u.id) FROM user_news u WHERE u.news_id = id AND u.viewed)")
	private Long sumNewsVisits;

	@ManyToOne
	private AbstractFilter filter;
	
	@OneToMany(mappedBy = "news", cascade = CascadeType.REMOVE)
	private Set<UserNews> userNews = new HashSet<>();

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTeaserText()
	{
		return teaserText;
	}

	public void setTeaserText(String teaserText)
	{
		this.teaserText = teaserText;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public LocalDateTime getValidFrom()
	{
		return validFrom;
	}

	public void setValidFrom(LocalDateTime validFrom)
	{
		this.validFrom = validFrom;
	}

	public LocalDateTime getValidTo()
	{
		return validTo;
	}

	public void setValidTo(LocalDateTime validTo)
	{
		this.validTo = validTo;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public Long getSumNewsVisits() { return this.sumNewsVisits; }

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}

	@Override
	public void setFilter(AbstractFilter filter)
	{
		this.filter = filter;
	}

	public void setEmailNotification(boolean notification) {this.emailNotification = notification; }

	public boolean isEmailNotification() { return emailNotification; }

	public void setEmailSendDate(LocalDateTime sendDate) {this.emailSendDate = sendDate;}

	public LocalDateTime getEmailSendDate() { return this.emailSendDate;}

	public Admin getAdmin()
	{
		return admin;
	}

	public void setAdmin(Admin admin)
	{
		this.admin = admin;
	}
	
	private Set<UserNews> getUserNews()
	{
		return userNews;
	}
	
	private void setUserNews(Set<UserNews> userNews)
	{
		this.userNews = userNews;
	}
}