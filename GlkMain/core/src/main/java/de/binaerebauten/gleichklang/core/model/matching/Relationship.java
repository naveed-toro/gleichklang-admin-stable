package de.binaerebauten.gleichklang.core.model.matching;

import com.vaadin.server.ThemeResource;
import de.binaerebauten.gleichklang.core.model.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.media.Footprint;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;
import de.binaerebauten.gleichklang.core.utils.DatabaseResourceBundleUtil;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.FunctionalUtils;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "relationship")
@NamedEntityGraph(name = "RelationshipLazy")
public class Relationship extends BaseEntity implements DeletableEntity<Long>,SourceUserEntity<Long>
{
	public enum Affiliation implements DefaultEnumI18N
	{
		NEUTRAL,
		POSITIVE,
		NEGATIVE;
		
		public ThemeResource getIcon()
		{
			switch (this)
			{
				case NEUTRAL:
					return new ThemeResource("img/icon_unentschieden.png");
				case POSITIVE:
					return new ThemeResource("img/icon_angenommen.png");
				case NEGATIVE:
					return new ThemeResource("img/icon_abgelehnt.png");
			}
			
			return null;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	public static final String NAME = "name";
	@ManyToOne
	@JoinColumn(name = "source_user_id", updatable = false, insertable = false)
	private User sourceUser;
	
	@ManyToOne
	@JoinColumn(name = "target_user_id", updatable = false, insertable = false)
	private User targetUser;
	
	@NotNull
	@Column(name = "source_user_id")
	private Long sourceUserId;
	
	@NotNull
	@Column(name = "target_user_id")
	private Long targetUserId;

	@Column(name = "deleted_by")
	private String deletedBy;
	
	@Enumerated(EnumType.STRING)
	private Affiliation affiliation = Affiliation.NEUTRAL;
	
	@ElementCollection(targetClass = RecommendationCategory.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "relationship_category", joinColumns = @JoinColumn(name = "relationship_id"))
	@Column(name = "category")
	@Enumerated(EnumType.STRING)
	private Set<RecommendationCategory> categories = new HashSet<>();
	
	@SanitizeContent
	@Size(max = 65535)
	private String memo;
	
	// TODO should be separated in a Footprint Class via OneToOne
	
	@Enumerated(EnumType.STRING)
	private Footprint footprint;
	
	@Column(name = "footprint_viewed")
	private Boolean footprintViewed;
	
	@Column(name = "footprint_date")
	private LocalDateTime footprintDate;
	
	@Column(name = "footprint_notified")
	private boolean footprintNotified;
	
	private boolean viewed;
	
	@Column(name = "last_viewed_date")
	private LocalDateTime lastViewedDate;
	
	private boolean deleted;

	@Column(name = "delete_date")
	private LocalDateTime deleteDate;

	@Column(name = "first_viewed")
	private LocalDateTime firstViewed;
	
	/**
	 * This flag marks if the user was notified about this "new" relationship.
	 */
	private boolean notified;
	
	@Formula("(SELECT c.i18n_key FROM question q JOIN answer a ON q.id = a.question_id JOIN choice_answer ca ON a.id = ca.answer_id JOIN choice c ON c.id = ca.choice_id WHERE q.i18n_key = '" + NaturalKeyEntity.SEX_QUESTION + "' AND a.user_id = target_user_id)")
	private String targetUserSex;
	
	@Formula("(SELECT r.viewed FROM relationship r WHERE r.source_user_id = target_user_id AND r.target_user_id = source_user_id)")
	private Boolean targetRelationshipViewed;
	
	@Formula("(SELECT r.footprint FROM relationship r WHERE r.source_user_id = target_user_id AND r.target_user_id = source_user_id)")
	@Enumerated(EnumType.STRING)
	private Footprint targetUserFootprint;

	@Formula("(SELECT r.footprint_date FROM relationship r WHERE r.source_user_id = target_user_id AND r.target_user_id = source_user_id)")
	private LocalDateTime targetUserFootprintDate;
	
	public User getSourceUser()
	{
		return sourceUser;
	}
	
	public void setSourceUser(User sourceUser)
	{
		this.sourceUser = sourceUser;
	}
	
	public User getTargetUser()
	{
		return targetUser;
	}
	
	public void setTargetUser(User targetUser)
	{
		this.targetUser = targetUser;
	}
	
	public Affiliation getAffiliation()
	{
		return affiliation;
	}
	
	public void setAffiliation(Affiliation affiliation)
	{
		this.affiliation = affiliation;
	}
	
	public Set<RecommendationCategory> getCategories()
	{
		return categories;
	}
	
	public void setCategories(Set<RecommendationCategory> categories)
	{
		this.categories = categories;
	}
	
	public RecommendationCategory getMainCategory()
	{
		return categories.stream().sorted().findFirst().orElse(null);
	}
	
	public String getMemo()
	{
		return memo;
	}
	
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	
	public Footprint getFootprint()
	{
		return footprint;
	}
	
	public void setFootprint(Footprint footprint)
	{
		this.footprint = footprint;
	}
	
	public Boolean getFootprintViewed()
	{
		return footprintViewed;
	}
	
	public void setFootprintViewed(Boolean footprintViewed)
	{
		this.footprintViewed = footprintViewed;
	}
	
	public boolean isFootprintNotified()
	{
		return footprintNotified;
	}
	
	public void setFootprintNotified(boolean footprintNotified)
	{
		this.footprintNotified = footprintNotified;
	}
	
	public boolean isViewed()
	{
		return viewed;
	}
	
	public void setViewed(boolean viewed)
	{
		this.viewed = viewed;
	}
	
	public LocalDateTime getLastViewedDate()
	{
		return lastViewedDate;
	}
	
	public void setLastViewedDate(LocalDateTime lastViewedDate)
	{
		this.lastViewedDate = lastViewedDate;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public boolean isNotified()
	{
		return notified;
	}
	
	public void setNotified(boolean notified)
	{
		this.notified = notified;
	}
	
	public LocalDateTime getFootprintDate()
	{
		return footprintDate;
	}
	
	public void setFootprintDate(LocalDateTime footprintDate)
	{
		this.footprintDate = footprintDate;
	}
	
	public String getName()
	{
		return targetUser.getAlias();
	}
	
	public Long getSourceUserId()
	{
		return sourceUserId;
	}
	
	public void setSourceUserId(Long sourceUserId)
	{
		this.sourceUserId = sourceUserId;
	}
	
	public Long getTargetUserId()
	{
		return targetUserId;
	}
	
	public void setTargetUserId(Long targetUserId)
	{
		this.targetUserId = targetUserId;
	}
	
	public String getTargetUserSex()
	{
		return DatabaseResourceBundleUtil.msg(this.targetUserSex, BaseName.CHOICE_VALUE);
	}
	
	public Optional<NaturalKey> getTargetUserSexKey()
	{
		return FunctionalUtils.nullSafe(() -> NaturalKey.getNaturalKey(this.targetUserSex, Choice.class));
	}
	
	public Boolean isTargetRelationshipViewed()
	{
		return this.targetRelationshipViewed;
	}
	
	public Footprint getTargetUserFootprint()
	{
		return this.targetUserFootprint;
	}

	
	public LocalDateTime getTargetUserFootprintDate()
	{
		return this.targetUserFootprintDate;
	}

	public LocalDateTime getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(LocalDateTime deleteDate) {
		this.deleteDate = deleteDate;
	}

	public LocalDateTime getFirstViewed() {
		return firstViewed;
	}

	public void setFirstViewed(LocalDateTime firstViewed) {
		this.firstViewed = firstViewed;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}
}
