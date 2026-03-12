package de.binaerebauten.gleichklang.core.model.media;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "media_gallery")
public class MediaGallery extends BaseEntity implements DeletableEntity<Long>
{
	@SanitizeContent
	@NotEmpty
	private String name;

	@NotNull
	@ManyToOne
	private User author;

	@Column(name = "visible_category")
	@Enumerated(EnumType.STRING)
	private RecommendationCategory visibleCategory;

	@Column(name = "visible_affiliation")
	@Enumerated(EnumType.STRING)
	private Affiliation visibleAffiliation;

	private boolean secret;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable
			(
					name = "media_gallery_visible_relationship",
					joinColumns = { @JoinColumn(name = "media_gallery_id") },
					inverseJoinColumns = { @JoinColumn(name = "relationship_id") }
			)
	private Set<Relationship> visibleRelationships = new HashSet<>();

	private boolean deleted;

	@Column(name = "avatar_gallery")
	private boolean avatarGallery = false;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public User getAuthor()
	{
		return author;
	}

	public void setAuthor(User author)
	{
		this.author = author;
	}

	public RecommendationCategory getVisibleCategory()
	{
		return visibleCategory;
	}

	public void setVisibleCategory(RecommendationCategory visibleCategory)
	{
		this.visibleCategory = visibleCategory;
	}

	public Affiliation getVisibleAffiliation()
	{
		return visibleAffiliation;
	}

	public void setVisibleAffiliation(Affiliation visibleAffiliation)
	{
		this.visibleAffiliation = visibleAffiliation;
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public boolean isSecret()
	{
		return secret;
	}

	public void setSecret(boolean secret)
	{
		this.secret = secret;
	}

	public Set<Relationship> getVisibleRelationships()
	{
		return visibleRelationships;
	}

	public void setVisibleRelationships(Set<Relationship> visibleRelationships)
	{
		this.visibleRelationships = visibleRelationships;
	}

	public void setAsAvatarGallery()
	{
		this.avatarGallery = true;
	}

	public boolean isAvatarGallery()
	{
		return this.avatarGallery;
	}
}
