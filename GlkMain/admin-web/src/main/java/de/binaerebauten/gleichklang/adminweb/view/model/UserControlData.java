package de.binaerebauten.gleichklang.adminweb.view.model;

import com.vaadin.server.StreamResource;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.RelationshipService.StatisticData;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.Directory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

public class UserControlData
{
	public static final SimpleAttribute user_ = new SimpleAttribute("user");
	public static final SimpleAttribute sex_ = new SimpleAttribute("sex");
	public static final SimpleAttribute age_ = new SimpleAttribute("age");
	public static final SimpleAttribute awareThrough_ = new SimpleAttribute("awareThrough");
	public static final SimpleAttribute freeText_ = new SimpleAttribute("freeText");
	public static final SimpleAttribute password_  = new SimpleAttribute("password");

	private final User user;
	private final String sex;
	private final int age;
	private final String awareThrough;
	private final String freeText;
	private String password;
	
	/* for statistics */
	private final Set<RecommendationCategory> availableCategories;
	private final SubscriptionState currentSubscriptionState;
	private final Map<RecommendationCategory, Long> noOfMatches;
	private final Map<RecommendationCategory, Long> noOfReceivedMessages;
	private final Map<RecommendationCategory, Long> noOfSendMessages;
	private final Map<RecommendationCategory, Double> missingRequiredAnswersRatio;
	private final Map<RecommendationCategory, Map<Strictness, Long>> matchCount;
	private final Map<RecommendationCategory, Map<Strictness, Long>> allocatableMatchCount;
	private final Map<RecommendationCategory, Boolean> suggestionInitial;
	private final Map<RecommendationCategory, Long> relationshipCountSinceCheckPeriod;
	private final Map<StatisticData, String> additionalStatistics;
	
	private final SortedMap<MessageTable.Directory, LazyBeanFilteredItemsHandler<Message>> messageHandlerMap;
	private final SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> adminMessageHandlerMap;
	private final Map<String, StreamResource> userReportsSourceList;
	private final LazyBeanFilteredItemsHandler<Relationship> relationshipHandler;
	private final FilterControlHandler relationshipFilterControlHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	
	private UserControlData(UserControlDataBuilder builder)
	{
		this.user = Objects.requireNonNull(builder.user);
		this.noOfMatches = Objects.requireNonNull(builder.noOfMatches);
		this.noOfReceivedMessages = Objects.requireNonNull(builder.noOfReceivedMessages);
		this.noOfSendMessages = Objects.requireNonNull(builder.noOfSendMessages);
		this.missingRequiredAnswersRatio = Objects.requireNonNull(builder.missingRequiredAnswersRatio);
		this.matchCount = Objects.requireNonNull(builder.matchCount);
		this.allocatableMatchCount = Objects.requireNonNull(builder.allocatableMatchCount);
		this.suggestionInitial = Objects.requireNonNull(builder.suggestionInitial);
		this.relationshipCountSinceCheckPeriod = Objects.requireNonNull(builder.relationshipCountSinceCheckPeriod);
		this.additionalStatistics = Objects.requireNonNull(builder.additionalStatistics);
		this.availableCategories = Objects.requireNonNull(builder.availableCategories);
		
		this.messageHandlerMap = Objects.requireNonNull(builder.messageHandlerMap);
		this.adminMessageHandlerMap = Objects.requireNonNull(builder.adminMessageHandlerMap);
		this.relationshipHandler = Objects.requireNonNull(builder.relationshipHandler);
		this.relationshipFilterControlHandler = Objects.requireNonNull(builder.relationshipFilterControlHandler);
		this.filterSpecificationBuilder = Objects.requireNonNull(builder.filterSpecificationBuilder);

		this.sex = builder.sex;
		this.age = builder.age;
		this.awareThrough = builder.awareThrough;
		this.freeText = builder.freeText;
		this.currentSubscriptionState = builder.currentSubscriptionState;
		this.userReportsSourceList = builder.reportsSourceList;
	}

	public User getUser()
	{
		return user;
	}

	/**
	 * Used via {@link #sex_}.
	 *
	 * @return the users sex
	 */
	public String getSex()
	{
		return sex;
	}

	/**
	 * Used via {@link #age_}.
	 *
	 * @return the users age
	 */
	public int getAge()
	{
		return age;
	}
	
	/**
	 * Used via {@link #awareThrough_}.
	 *
	 * @return the users aware through
	 */

	public String getAwareThrough()
	{
		return awareThrough;
	}

	/**
	 * Used via {@link #freeText_}
	 * @return the users free text
	 */
	public String getFreeText()
	{
		return freeText;
	}
	
	/**
	 * Used via {@link #password_}
	 * @return the users new password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Used via {@link #password_}
	 * @return the users new password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public Map<RecommendationCategory, Long> getNoOfMatches()
	{
		return noOfMatches;
	}

	public Map<RecommendationCategory, Long> getNoOfReceivedMessages()
	{
		return noOfReceivedMessages;
	}

	public Map<RecommendationCategory, Long> getNoOfSendMessages()
	{
		return noOfSendMessages;
	}

	public Map<MessageTable.Directory, LazyBeanFilteredItemsHandler<Message>> getMessageHandlerMap()
	{
		return messageHandlerMap;
	}
	
	public SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> getAdminMessageHandlerMap()
	{
		return adminMessageHandlerMap;
	}
	
	public Map<String, StreamResource> getUserReportsSourceList() {
		return userReportsSourceList;
	}
	
	public SubscriptionState getCurrentSubscriptionState()
	{
		return currentSubscriptionState;
	}
	
	public Map<RecommendationCategory, Double> getMissingRequiredAnswersRatio()
	{
		return missingRequiredAnswersRatio;
	}
	
	public Map<RecommendationCategory, Map<Strictness, Long>> getMatchCount()
	{
		return matchCount;
	}
	
	public Map<RecommendationCategory, Map<Strictness, Long>> getAllocatableMatchCount()
	{
		return allocatableMatchCount;
	}

	public Map<RecommendationCategory, Boolean> getSuggestionInitial()
	{
		return suggestionInitial;
	}
	
	public Map<RecommendationCategory, Long> getRelationshipCountSinceCheckPeriod()
	{
		return relationshipCountSinceCheckPeriod;
	}
	
	public Map<StatisticData, String> getAdditionalStatistics()
	{
		return additionalStatistics;
	}
	
	public LazyBeanFilteredItemsHandler<Relationship> getRelationshipHandler()
	{
		return relationshipHandler;
	}
	
	public FilterControlHandler getRelationshipFilterControlHandler()
	{
		return relationshipFilterControlHandler;
	}
	
	public FilterSpecificationBuilder getFilterSpecificationBuilder()
	{
		return filterSpecificationBuilder;
	}
	
	public Set<RecommendationCategory> getAvailableCategories()
	{
		return availableCategories;
	}
	
	public static class UserControlDataBuilder
	{
		private User user;
		private String sex;
		private int age;
		private String awareThrough;
		private String freeText;
		private SubscriptionState currentSubscriptionState;
		private Map<RecommendationCategory, Long> noOfMatches;
		private Map<RecommendationCategory, Long> noOfReceivedMessages;
		private Map<RecommendationCategory, Long> noOfSendMessages;
		private Map<RecommendationCategory, Double> missingRequiredAnswersRatio;
		private Map<RecommendationCategory, Map<Strictness, Long>> matchCount;
		private Map<RecommendationCategory, Map<Strictness, Long>> allocatableMatchCount;
		private Map<RecommendationCategory, Boolean> suggestionInitial;
		private Map<RecommendationCategory, Long> relationshipCountSinceCheckPeriod;
		private SortedMap<Directory, LazyBeanFilteredItemsHandler<Message>> messageHandlerMap;
		private SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> adminMessageHandlerMap;
		private Map<String, StreamResource> reportsSourceList;
		private LazyBeanFilteredItemsHandler<Relationship> relationshipHandler;
		private FilterControlHandler relationshipFilterControlHandler;
		private FilterSpecificationBuilder filterSpecificationBuilder;
		private Map<StatisticData, String> additionalStatistics;
		private Set<RecommendationCategory> availableCategories;
		
		public UserControlDataBuilder setUser(User user)
		{
			this.user = user;
			return this;
		}
		
		public UserControlDataBuilder setSex(String sex)
		{
			this.sex = sex;
			return this;
		}
		
		public UserControlDataBuilder setAge(int age)
		{
			this.age = age;
			return this;
		}
		
		public UserControlDataBuilder setAwareThrough(String awareThrough)
		{
			this.awareThrough = awareThrough;
			return this;
		}
		
		public UserControlDataBuilder setFreeText(String freeText)
		{
			this.freeText = freeText;
			return this;
		}
		
		public UserControlDataBuilder setCurrentSubscriptionState(SubscriptionState currentSubscriptionState)
		{
			this.currentSubscriptionState = currentSubscriptionState;
			return this;
		}
		
		public UserControlDataBuilder setNoOfMatches(Map<RecommendationCategory, Long> noOfMatches)
		{
			this.noOfMatches = noOfMatches;
			return this;
		}
		
		public UserControlDataBuilder setNoOfReceivedMessages(Map<RecommendationCategory, Long> noOfReceivedMessages)
		{
			this.noOfReceivedMessages = noOfReceivedMessages;
			return this;
		}
		
		public UserControlDataBuilder setNoOfSendMessages(Map<RecommendationCategory, Long> noOfSendMessages)
		{
			this.noOfSendMessages = noOfSendMessages;
			return this;
		}
		
		public UserControlDataBuilder setMissingRequiredAnswersRatio(Map<RecommendationCategory, Double> missingRequiredAnswersRatio)
		{
			this.missingRequiredAnswersRatio = missingRequiredAnswersRatio;
			return this;
		}
		
		public UserControlDataBuilder setMatchCount(Map<RecommendationCategory, Map<Strictness, Long>> matchCount)
		{
			this.matchCount = matchCount;
			return this;
		}

		public UserControlDataBuilder setAllocatableMatchCount(Map<RecommendationCategory, Map<Strictness, Long>> allocatableMatchCount)
		{
			this.allocatableMatchCount = allocatableMatchCount;
			return this;
		}

		public UserControlDataBuilder setSuggestionInitial(Map<RecommendationCategory, Boolean> suggestionInitial)
		{
			this.suggestionInitial = suggestionInitial;
			return this;
		}
		
		public UserControlDataBuilder setRelationshipCountSinceCheckPeriod(Map<RecommendationCategory, Long> relationshipCountSinceCheckPeriod)
		{
			this.relationshipCountSinceCheckPeriod = relationshipCountSinceCheckPeriod;
			return this;
		}
		
		public UserControlDataBuilder setMessageHandlerMap(SortedMap<Directory, LazyBeanFilteredItemsHandler<Message>> messageHandlerMap)
		{
			this.messageHandlerMap = messageHandlerMap;
			return this;
		}
		
		public UserControlDataBuilder setAdminMessageHandlerMap(SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> adminMessageHandlerMap)
		{
			this.adminMessageHandlerMap = adminMessageHandlerMap;
			return this;
		}
		
		public UserControlDataBuilder setReportsSourceList(Map<String, StreamResource> reportsSourceList)
		{
			this.reportsSourceList = reportsSourceList;
			return this;
		}
		
		public UserControlDataBuilder setRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> relationshipHandler)
		{
			this.relationshipHandler = relationshipHandler;
			return this;
		}
		
		public UserControlDataBuilder setRelationshipFilterControlHandler(FilterControlHandler relationshipFilterControlHandler)
		{
			this.relationshipFilterControlHandler = relationshipFilterControlHandler;
			return this;
		}
		
		public UserControlDataBuilder setFilterSpecificationBuilder(FilterSpecificationBuilder filterSpecificationBuilder)
		{
			this.filterSpecificationBuilder = filterSpecificationBuilder;
			return this;
		}
		
		public UserControlDataBuilder setAdditionalStatistics(Map<StatisticData, String> additionalStatistics)
		{
			this.additionalStatistics = additionalStatistics;
			return this;
		}
		
		public UserControlDataBuilder setAvailableCategories(Set<RecommendationCategory> availableCategories)
		{
			this.availableCategories = availableCategories;
			return this;
		}
		
		public UserControlData createUserControlData()
		{
			return new UserControlData(this);
		}
	}
}
