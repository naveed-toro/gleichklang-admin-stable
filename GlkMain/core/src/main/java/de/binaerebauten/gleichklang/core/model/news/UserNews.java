package de.binaerebauten.gleichklang.core.model.news;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Indicates if a user has read a news by clicking on [read more] in the
 * news-preview ({@link UserNews#viewed}. If the user views the fulltext of the news, he can decide to
 * hide the news via ({@link UserNews#hide}). Also used for tracking the number of visits per
 * news. Created by rgoerner on 10.12.15.
 */
@Entity
@Table(name = "user_news")
public class UserNews extends BaseEntity
{
	@ManyToOne
	private User user;

	@ManyToOne
	@JoinColumn(name = "news_id")
	private News news;

	@NotNull
	private boolean hide = false;

	@NotNull
	private boolean viewed = false;

	@NotNull
	private boolean notified = false;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public News getNews()
	{
		return news;
	}

	public void setNews(News news)
	{
		this.news = news;
	}

	public boolean isHide()
	{
		return hide;
	}

	public void setHide(boolean hide)
	{
		this.hide = hide;
	}

	public boolean isViewed()
	{
		return viewed;
	}

	public void setViewed(boolean viewed)
	{
		this.viewed = viewed;
	}

	public void setNotified(boolean notified) { this.notified = notified; }

	public boolean getNofified() {return notified; }
}
