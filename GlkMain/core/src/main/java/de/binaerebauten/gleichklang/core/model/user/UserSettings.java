package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_settings")
public class UserSettings extends BaseEntity
{
	@OneToOne(fetch = FetchType.EAGER)
	private User user;
	
	@Column(name = "general_terms_accepted")
	private boolean generalTermsAccepted;
	
	@Column(name = "privacy_policy_accepted")
	private boolean privacyPolicyAccepted;
	
	@Column(name = "cancellation_policy_accepted")
	private boolean cancellationPolicyAccepted;
	
	@Column(name = "disable_ads")
	private boolean disableAds;
	
	@Column(name = "disable_news_notifications")
	private boolean disableNewsNotifications;
	
	@Column(name = "disable_recommendation_notifications")
	private boolean disableRecommendationNotifications;
	
	@Column(name = "disable_cipher_message_notifications")
	private boolean disableCipherMessageNotifications;
	
	@Column(name = "disable_positive_ranking_notifications")
	private boolean disablePositiveRankingNotifications;
	
	@Column(name = "disable_footprint_notifications")
	private boolean disableFootprintNotifications;
	
	@Column(name = "enable_marketing_notifications")
	private boolean enableMarketingNotifications;
	
	@Column(name = "community_rules_accepted")
	private boolean communityRulesAccepted;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private Language language = Language.DE;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public boolean isGeneralTermsAccepted()
	{
		return generalTermsAccepted;
	}
	
	public void setGeneralTermsAccepted(boolean generalTermsAccepted)
	{
		this.generalTermsAccepted = generalTermsAccepted;
	}
	
	public boolean isPrivacyPolicyAccepted()
	{
		return privacyPolicyAccepted;
	}
	
	public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted)
	{
		this.privacyPolicyAccepted = privacyPolicyAccepted;
	}
	
	public boolean isCancellationPolicyAccepted()
	{
		return cancellationPolicyAccepted;
	}
	
	public void setCancellationPolicyAccepted(boolean cancellationPolicyAccepted)
	{
		this.cancellationPolicyAccepted = cancellationPolicyAccepted;
	}
	
	public boolean isDisableAds()
	{
		return disableAds;
	}
	
	public void setDisableAds(boolean disableAds)
	{
		this.disableAds = disableAds;
	}
	
	public boolean isDisableNewsNotifications()
	{
		return disableNewsNotifications;
	}
	
	public void setDisableNewsNotifications(boolean disableNewsNotifications)
	{
		this.disableNewsNotifications = disableNewsNotifications;
	}
	
	public boolean isDisableRecommendationNotifications()
	{
		return disableRecommendationNotifications;
	}
	
	public void setDisableRecommendationNotifications(boolean disableRecommendationNotifications)
	{
		this.disableRecommendationNotifications = disableRecommendationNotifications;
	}
	
	public boolean isDisableCipherMessageNotifications()
	{
		return disableCipherMessageNotifications;
	}
	
	public void setDisableCipherMessageNotifications(boolean disableCipherMessageNotifications)
	{
		this.disableCipherMessageNotifications = disableCipherMessageNotifications;
	}
	
	public boolean isDisablePositiveRankingNotifications()
	{
		return disablePositiveRankingNotifications;
	}
	
	public void setDisablePositiveRankingNotifications(boolean disablePositiveRankingNotifications)
	{
		this.disablePositiveRankingNotifications = disablePositiveRankingNotifications;
	}
	
	public boolean isDisableFootprintNotifications()
	{
		return disableFootprintNotifications;
	}
	
	public void setDisableFootprintNotifications(boolean disableFootprintNotifications)
	{
		this.disableFootprintNotifications = disableFootprintNotifications;
	}
	
	public boolean isEnableMarketingNotifications()
	{
		return enableMarketingNotifications;
	}
	
	public void setEnableMarketingNotifications(boolean enableMarketingNotifications)
	{
		this.enableMarketingNotifications = enableMarketingNotifications;
	}
	
	public boolean isCommunityRulesAccepted()
	{
		return communityRulesAccepted;
	}
	
	public void setCommunityRulesAccepted(boolean communityRulesAccepted)
	{
		this.communityRulesAccepted = communityRulesAccepted;
	}
	
	public Language getLanguage()
	{
		return language;
	}
	
	public void setLanguage(Language language)
	{
		this.language = language;
	}
}
