package de.binaerebauten.gleichklang.core.service;

import com.google.common.collect.Table;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class RelationshipService
{
	public enum StatisticData implements DefaultEnumI18N
	{
		FIRST_WROTE,
		FIRST_WROTE_ANSWERED,
		FIRST_RECEIVED,
		FIRST_RECEIVED_ANSWERED,
		MINIMAL_SUBSTANTIAL_COMMUNICATION,
		SUBSTANTIAL_COMMUNICATION,
		SUBSTANTIAL_COMMUNICATION_TO_COMMUNICATION,
		VIEWED_RELATIONSHIP;
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	public enum GlobalStatisticData implements DefaultEnumI18N
	{
		RELATIONSHIP_COUNT,
		COMMUNICATION,
		SUBSTANTIAL_COMMUNICATION_1,
		SUBSTANTIAL_COMMUNICATION_5;
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(RelationshipService.class);
	
	@Autowired
	private RelationshipRepository relationshipRepository;
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private MailQueueService mailQueueService;
	
	@Autowired
	private MailSendService mailSendService;
	
	@Autowired
	private UserMailTemplateService userMailTemplateService;
	
	public Relationship getInverseRelationship(Relationship relationship)
	{
		final Relationship inverseRelationship = relationshipRepository.findRelationshipBySourceUserAndTargetUser(relationship.getTargetUser(), relationship.getSourceUser());
		if (inverseRelationship == null)
		{
			LOG.error("No inverse relationship for {} - {}", relationship.getTargetUser(), relationship.getSourceUser());
			return null;
		}
		return inverseRelationship;
	}

// These mails are now directly sent
//	@Scheduled(fixedRate = 120000)
//	public void addNewFootprintMails()
//	{
//		final List<User> users = relationshipRepository.findAllUserWithNotNotifiedNewFootprints();
//		LOG.info("{} User werden über neue Footprints informiert", users.size());
//		try {
//			for (User user : users)
//			{
//				mailQueueService.enqueue(user, UserMailTemplate.NEW_FOOTPRINT);
//			}
//		}catch (Exception e) {
//			LOG.error("addNewFootprintMails", e);
//		}
//	}

	@Transactional
	public void saveViewedRelationship(Relationship relationship, Affiliation oldAffiliation)
	{
		final Relationship inverseRelationship = getInverseRelationship(relationship);
		if (inverseRelationship != null)
		{
			if (inverseRelationship.isDeleted()) relationship.setDeleted(true);
			if (relationship.isDeleted()) inverseRelationship.setDeleted(true);

			inverseRelationship.setFootprintViewed(true);
			relationshipRepository.save(inverseRelationship);
		}
		if(!relationship.isViewed()) {
			relationship.setFirstViewed(LocalDateTime.now());
		}
		relationship.setViewed(true);
		relationship.setLastViewedDate(LocalDateTime.now());
		relationshipRepository.saveAndFlush(relationship);

		if (oldAffiliation == Affiliation.NEUTRAL || oldAffiliation == Affiliation.NEGATIVE)
		{
			if (relationship.getAffiliation() == Affiliation.POSITIVE && !relationship.getSourceUser().isBlocked())
			{
				mailSendService.sendEmail(relationship.getTargetUser(), userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_MATCH_POSITIVE, relationship.getSourceUser(), relationship.getTargetUser(), relationship.getMainCategory()));
			}
		}
	}

    public LazyBeanFilteredItemsHandler<Relationship> createRelationshipHandler(User currentUser)
    {
        final Specifications<Relationship> specs = Specifications.where((root, query, cb) ->

                cb.and(

                        cb.equal(root.get(Relationship_.sourceUser), currentUser),
                        cb.equal(root.get(Relationship_.deleted), false),
                        cb.notEqual(root.get(Relationship_.targetUser).get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED)
                ));

        return (specification, pageable) -> relationshipRepository.findAll(specs.and(specification), pageable);
    }
	
	public LazyBeanFilteredItemsHandler<Relationship> createLastRelationshipHandler(User currentUser)
	{
		
		final Specifications<Relationship> specs = Specifications.where((root, query, cb) ->
			cb.and(
					currentUser.getCategories().stream().map(c -> cb.isMember(c, root.get(Relationship_.categories))).reduce(cb::or).orElse(cb.disjunction()),
					cb.equal(root.get(Relationship_.sourceUser), currentUser),
					cb.equal(root.get(Relationship_.deleted), false),
					cb.isNotNull(root.get(Relationship_.lastViewedDate))
			)
		);
		
		return (specification, pageable) -> relationshipRepository.findAll(specs.and(specification), pageable);
	}
	
	public LazyBeanFilteredItemsHandler<Relationship> createAdminRelationshipHandler(User user)
	{
		final Specifications<Relationship> specs = Specifications.where((root, query, cb) ->
				
				cb.or(
						cb.equal(root.get(Relationship_.sourceUser), user),
						cb.equal(root.get(Relationship_.targetUser), user)
				));
		
		return (specification, pageable) -> relationshipRepository.findAll(specs.and(specification), pageable);
	}

//	@Transactional
//	public void deleteRelationship(Relationship relationship)
//	{
//		final Relationship inverseRelationship = getInverseRelationship(relationship);
//
//		relationshipRepository.markAsDeleted(relationship);
//		LocalDateTime localDateTime = LocalDateTime.now();
//		relationshipRepository.deletedDate(localDateTime, relationship);
//		if (inverseRelationship != null)
//			relationshipRepository.markAsDeleted(inverseRelationship);
//		relationshipRepository.deletedDate(localDateTime, inverseRelationship);
//	}


	@Transactional
	public void deleteRelationship(Relationship relationship)
	{
		final Relationship inverseRelationship = getInverseRelationship(relationship);

		relationshipRepository.markAsDeleted(relationship);
		if (inverseRelationship != null)
			relationshipRepository.markAsDeleted(inverseRelationship);
	}


	@Transactional
	public void restoreRelationship(Relationship relationship)
	{
		final Relationship inverseRelationship = getInverseRelationship(relationship);
		
		relationshipRepository.restoreDeleted(relationship);
		if (inverseRelationship != null)
			relationshipRepository.restoreDeleted(inverseRelationship);
	}
	
	@Transactional(readOnly = true)
	public Long getRelationshipCountForUser(User user, RecommendationCategory category)
	{
		if(category == null) return relationshipRepository.countBySourceUser(user);
		
		return relationshipRepository.countBySourceUserAndCategory(user, category);
	}
	
	@Transactional(readOnly = true)
	public Map<GlobalStatisticData, String> getStatistics()
	{
		final EnumMap<GlobalStatisticData, String> result = new EnumMap<>(GlobalStatisticData.class);
		
		final Long relationshipCount = relationshipRepository.count();
		final Table<Long, Long, Long> messageStatisticTable = messageService.getMessageStatistics();
		final Map<Long, Long> substantialMessageStatistics = messageService.getCommunicationStatistics(messageStatisticTable);
		final String separator = " - ";
		
		for (GlobalStatisticData globalStatisticData : GlobalStatisticData.values())
		{
			String resultValue = "";
			
			switch (globalStatisticData)
			{
				case RELATIONSHIP_COUNT:
					resultValue = Long.toString(relationshipCount);
					break;
				case COMMUNICATION:
					final long communications = messageStatisticTable.size();
					resultValue = communications + separator + StringUtils.getPercentageString(communications, relationshipCount);
					break;
				case SUBSTANTIAL_COMMUNICATION_1:
					final Long substantialCommunication1 = messageService.countSubstantialCommunications(substantialMessageStatistics, 1L);
					resultValue = substantialCommunication1 + separator + StringUtils.getPercentageString(substantialCommunication1, relationshipCount / 2);
					break;
				case SUBSTANTIAL_COMMUNICATION_5:
					final Long substantialCommunication5 = messageService.countSubstantialCommunications(substantialMessageStatistics, 5L);
					resultValue = substantialCommunication5 + separator + StringUtils.getPercentageString(substantialCommunication5, relationshipCount / 2);
					break;
			}
			
			result.put(globalStatisticData, resultValue);
		}
		
		return result;
	}
	
	@Transactional(readOnly = true)
	public Map<StatisticData, String> getStatistics(User user,boolean isAdmin)
	{
		LOG.info("start getStatistics");
		final EnumMap<StatisticData, String> result = new EnumMap<>(StatisticData.class);

		
		LOG.info("start getMessageStatistics");
		final Table<Long, Long, TreeSet<LocalDateTime>> messageStatisticTable = messageService.getMessageStatistics(user);
		LOG.info("end getMessageStatistics");
		
		final Long relationships = relationshipRepository.countBySourceUser(user);
		final Long firstWrote = messageService.countFirstWrote(messageStatisticTable, user.getId(), false);
		final Long firstReceived = messageService.countFirstReceived(messageStatisticTable, user.getId(), false);
		final Long firstReceivedAnswered = messageService.countFirstReceived(messageStatisticTable, user.getId(), true);
		final Long firstWroteAnswered = messageService.countFirstWrote(messageStatisticTable, user.getId(), true);
		final Long substantialCommunication = messageService.countSubstantialCommunications(messageService.convertMessageStatistics(messageStatisticTable), 2L);
		
		for (StatisticData statisticData : StatisticData.values())
		{
			String resultValue = "";
			
			switch (statisticData)
			{
				case FIRST_WROTE:
					resultValue = isAdmin?StringUtils.getPercentageString(firstWrote, relationships):StringUtils.getPercentageString(firstWrote, relationships)+" "+firstWrote;
					break;
				case FIRST_WROTE_ANSWERED:
					resultValue = isAdmin?StringUtils.getPercentageString(firstWroteAnswered, firstWrote):StringUtils.getPercentageString(firstWroteAnswered, firstWrote)+" "+firstWroteAnswered;
					break;
				case FIRST_RECEIVED:
					resultValue = StringUtils.getPercentageString(firstReceived, relationships);
					break;
				case FIRST_RECEIVED_ANSWERED:
					resultValue = StringUtils.getPercentageString(firstReceivedAnswered, firstReceived);
					break;
				case MINIMAL_SUBSTANTIAL_COMMUNICATION:
					final Long minimalSubstantialCommunication = firstWroteAnswered + firstReceivedAnswered;
					resultValue = StringUtils.getPercentageString(minimalSubstantialCommunication, relationships);
					break;
				case SUBSTANTIAL_COMMUNICATION:
					resultValue = StringUtils.getPercentageString(substantialCommunication, relationships);
					break;
				case SUBSTANTIAL_COMMUNICATION_TO_COMMUNICATION:
					final Long communications = firstReceived + firstWrote;
					resultValue = StringUtils.getPercentageString(substantialCommunication, communications);
					break;
				case VIEWED_RELATIONSHIP:
					final Long viewedRelationship = relationshipRepository.countBySourceUserAndViewedTrue(user);
					resultValue = isAdmin?StringUtils.getPercentageString(viewedRelationship, relationships):StringUtils.getPercentageString(viewedRelationship, relationships)+" "+viewedRelationship;
					break;
			}
			
			result.put(statisticData, resultValue);
		}
		
		LOG.info("end getStatistics");
		return result;
	}

	@Transactional(readOnly = true)
	public Map<StatisticData, String> getNumberOfFirstWroteStatisticsByCategory(User user,RecommendationCategory recommendationCategory) {
		final EnumMap<StatisticData, String> result = new EnumMap<>(StatisticData.class);


		final Table<Long, Long, TreeSet<LocalDateTime>> messageStatisticTable = messageService.getMessageStatisticsByCategory(user, recommendationCategory);

		final Long firstWrote = messageService.countFirstWrote(messageStatisticTable, user.getId(), false);
		for (StatisticData statisticData : StatisticData.values()) {
			String resultValue = "";

			switch (statisticData) {
				case FIRST_WROTE:
					resultValue = " " + firstWrote;
					break;
			}

			result.put(statisticData, resultValue);
		}

		return result;
	}

	public Long numberOfViewedSuggestions(User user, Date date){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return relationshipRepository.findNumberOfViewedSuggestions(user,localDateTime);
	}
	
}
