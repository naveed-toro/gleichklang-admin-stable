package de.binaerebauten.gleichklang.core.utils.filter;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription_;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio_;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity_;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail_;
import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.Match_;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message_;
import de.binaerebauten.gleichklang.core.model.message.ReceiverEnvelope_;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.service.MatchingUtilService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.utils.MatchingUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;


import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.binaerebauten.gleichklang.core.utils.MatchingUtils.ALLOWED_MISSING_ANSWERS_RATIO;

@Component
public class FilterSpecificationBuilder
{

	private static final Logger log = LoggerFactory.getLogger(UndeliverableMailService.class);

	@Autowired
	@Lazy
	private EmailDomainMappingRepository emailDomainMappingRepository;
	private String exactCheckBox;
	private boolean audioFrndShipPrtnrFilterUnary=false;

	private class FilterSpecificationBuilderVisitor implements FilterVisitor<Specification<User>>
	{
		@Override
		public Specification<User> visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			final Specification<User> spec = unaryOperatorFilter.getFilter().accept(this);
			audioFrndShipPrtnrFilterUnary=true;
			switch (unaryOperatorFilter.getUnaryOperator())
			{
				case NOT:
					return Specifications.not(spec);
			}

			return null;
		}

		@Override
		public Specification<User> visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			final Specifications<User> specs = Specifications.where(binaryOperatorFilter.getLeftFilter().accept(this));
			final Specification<User> rightSpec = binaryOperatorFilter.getRightFilter().accept(this);

			switch (binaryOperatorFilter.getBinaryOperator())
			{
				case INTERSECTION:
					return specs.and(rightSpec);
				case UNION:
					return specs.or(rightSpec);
			}

			return null;
		}

		@Override
		public Specification<User> visit(TemplateFilter templateFilter)
		{
			return templateFilter.getFilter().accept(this);
		}

		@Override
		public Specification<User> visit(RegionFilter regionFilter)
		{
			return (root, query, cb) ->
			{
				final ListJoin<User, Address> address = root.join(User_.addresses);

				/*
				  Zip was removed from filterComponent, but was left as specification,
				  because of simple reimplement the zip and also for existing templates
				 */

				final Predicate continent = cb.equal(address.get(Address_.continent).get(LocatableEntity_.id), regionFilter.getLocatableEntity().getId());
				final Predicate zip = cb.equal(address.get(Address_.zip).get(LocatableEntity_.id), regionFilter.getLocatableEntity().getId());
				final Predicate country = cb.equal(address.get(Address_.country).get(LocatableEntity_.id), regionFilter.getLocatableEntity().getId());
				final Predicate region = cb.equal(address.get(Address_.region).get(LocatableEntity_.id), regionFilter.getLocatableEntity().getId());

				return cb.or(continent, zip, country, region);
			};
		}

		@Override
		public Specification<User> visit(ProximityFilter proximityFilter)
		{

			return null;
		}

		@Override
		public Specification<User> visit(RecommendationCategoryFilter recommendationCategoryFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				final Predicate categoryPredicate = cb.equal(subscriptionRoot.join(Subscription_.offer).join(SubscriptionOffer_.categories).get(SubscriptionOfferCategory_.category), recommendationCategoryFilter.getEnumValue());
				final Predicate activePredicate = cb.equal(subscriptionRoot.get(Subscription_.current), Boolean.TRUE);

				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(cb.and(categoryPredicate, activePredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(ChoiceQuestionFilter choiceQuestionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ChoiceAnswer> answerRoot = subQuery.from(ChoiceAnswer.class);

				final Predicate questionPredicate = cb.equal(answerRoot.get(ChoiceAnswer_.question), choiceQuestionFilter.getChoiceQuestion());
				final Predicate choicePredicate = cb.isMember(choiceQuestionFilter.getChoice(), answerRoot.get(ChoiceAnswer_.choices));

				subQuery.select(answerRoot.join(ChoiceAnswer_.user)).where(cb.and(questionPredicate, choicePredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(NumberQuestionFilter numberQuestionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<NumberAnswer> answerRoot = subQuery.from(NumberAnswer.class);

				final Predicate questionPredicate = cb.equal(answerRoot.get(NumberAnswer_.question), numberQuestionFilter.getNumberQuestion());
				final Predicate valuePredicate = cb.between(answerRoot.get(NumberAnswer_.numberValue), numberQuestionFilter.getMin(), numberQuestionFilter.getMax());

				subQuery.select(answerRoot.join(ChoiceAnswer_.user)).where(cb.and(questionPredicate, valuePredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(AgeFilter ageFilter)
		{
			return (root, query, cb) ->
			{
				final int max = Math.max(ageFilter.getMaxAge(), ageFilter.getMinAge());
				final int min = Math.min(ageFilter.getMaxAge(), ageFilter.getMinAge());

				final LocalDate today = LocalDate.now();
				return cb.between(root.get(User_.birthDate), today.minusYears(max + 1), today.minusYears(min));
			};
		}

		@Override
		public Specification<User> visit(TextQuestionFilter textQuestionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<TextAnswer> answerRoot = subQuery.from(TextAnswer.class);

				final Predicate questionPredicate = cb.equal(answerRoot.get(TextAnswer_.question), textQuestionFilter.getTextQuestion());
				final Predicate textPredicate = cb.like(answerRoot.get(TextAnswer_.textValue), "%" + textQuestionFilter.getText() + "%");

				subQuery.select(answerRoot.join(TextAnswer_.user)).where(cb.and(questionPredicate, textPredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(MemberStatusFilter memberStatusFilter)
		{
			return (root, query, cb) ->
					cb.equal(root.get(User_.memberStatus), memberStatusFilter.getEnumValue());
		}

		@Override
		public Specification<User> visit(LastNameFilter lastNameFilter)
		{
			return (root, query, cb) ->
			{
				Predicate lastNameFilterPredicate=null;
				if(exactCheckBox.equals("false") || exactCheckBox.equals(""))
				{
					lastNameFilterPredicate= cb.like(root.get(User_.lastName), "%" + lastNameFilter.getValue() + "%");
				}
				else
				{
					lastNameFilterPredicate= cb.equal(root.get(User_.lastName), lastNameFilter.getValue());
				}
				return lastNameFilterPredicate;
			};
		}

		@Override
		public Specification<User> visit(FirstNameFilter firstNameFilter)
		{
			return (root, query, cb) ->
			{
				Predicate firstNameFilterPredicate=null;
				if(exactCheckBox.equals("false") || exactCheckBox.equals(""))
				{
					firstNameFilterPredicate=cb.like(root.get(User_.firstName), "%" + firstNameFilter.getValue() + "%");
				}
				else
				{
					firstNameFilterPredicate=cb.equal(root.get(User_.firstName), firstNameFilter.getValue() );
				}
				return firstNameFilterPredicate;
			};
		}

		@Override
		public Specification<User> visit(AliasFilter aliasFilter)
		{
			return (root, query, cb) ->
			{
				Predicate aliasFilterPredicate=null;
				if(exactCheckBox.equals("false") || exactCheckBox.equals(""))
				{
					aliasFilterPredicate=cb.like(root.get(User_.alias), "%" + aliasFilter.getValue() + "%");
				}
				else
				{
					aliasFilterPredicate=cb.equal(root.get(User_.alias), aliasFilter.getValue() );
				}
				return aliasFilterPredicate;
			};
		}

		@Override
		public Specification<User> visit(MailFilter mailFilter)
		{
			// TODO: make changes to also search

			return (root, query, cb) ->
			{
				String emailAddress = mailFilter.getValue(), domain;
				Predicate emailFilter = cb.like(root.get(User_.email), "%" + mailFilter.getValue() + "%");

				if(exactCheckBox.equals("false") || exactCheckBox.equals(""))
				{
					emailFilter=cb.like(root.get(User_.email), "%" + mailFilter.getValue() + "%");
				}
				else
				{
					emailFilter = cb.equal(root.get(User_.email), mailFilter.getValue());
				}

				// try to generate for sibling email Ids
				try
				{
					if(emailAddress !=null && !emailAddress.isEmpty() && emailAddress.indexOf("@") != -1)
					{

						System.out.println("***** in here ");
						domain = emailAddress.substring(emailAddress.indexOf("@")+1);
						emailAddress = emailAddress.trim().substring(0, emailAddress.indexOf("@"));
						if(emailAddress != null && !emailAddress.isEmpty())
						{
						String siblingDomains[] = getActiveEmailMappings(domain);
						if(siblingDomains != null && siblingDomains.length >0)
						{
							for(String s:siblingDomains)
							{
								emailFilter = cb.or(emailFilter, cb.like(root.get(User_.email), "%" +emailAddress+"@"+s.trim() + "%"));
								System.out.println("*****"+emailAddress);

							}
		}
						}
					}
				}
				catch (Exception ex)
				{
					System.out.println(ex);
					return cb.like(root.get(User_.email), "%" + mailFilter.getValue() + "%");
				}
				return emailFilter;
			};
		}

		private String[] getActiveEmailMappings(String userEnteredDomain)
		{

			String siblingDomains[] = null;
			if(userEnteredDomain != null && emailDomainMappingRepository != null)
			{
				List<EmailDomainMapping> siblingMappings;
				siblingMappings =  emailDomainMappingRepository.findByActiveTrueAndMappingValueContaining(userEnteredDomain);
				if(siblingMappings != null && siblingMappings.size() >0)
				{
					for(EmailDomainMapping mapping: siblingMappings)
					{
						siblingDomains = mapping.getMappingValue().split(",");
						}
			}}
			return siblingDomains;
		}

		@Override
		public Specification<User> visit(ActionCodeFilter actionCodeFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<UserPaymentSettings> paymentSettingsRoot = subQuery.from(UserPaymentSettings.class);

				final Predicate stringFilter = cb.like(paymentSettingsRoot.get(UserPaymentSettings_.actionCode), "%" + actionCodeFilter.getValue() + "%");

				subQuery.select(paymentSettingsRoot.join(UserPaymentSettings_.user)).where(stringFilter);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(UserActivityFilter userActivityFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<UserActivityLog> userActivityLogRoot = subQuery.from(UserActivityLog.class);

				Predicate filter = cb.equal(userActivityLogRoot.get(UserActivityLog_.userActivity), userActivityFilter.getUserActivity());

				if (userActivityFilter.getFromDate() != null)
				{
					final LocalDateTime fromDate = LocalDateTime.of(userActivityFilter.getFromDate(), LocalTime.MIN);
					filter = cb.and(filter, cb.greaterThanOrEqualTo(userActivityLogRoot.get(UserActivityLog_.createDate), fromDate));
				}

				if (userActivityFilter.getToDate() != null)
				{
					final LocalDateTime toDate = LocalDateTime.of(userActivityFilter.getToDate(), LocalTime.MAX);
					filter = cb.and(filter, cb.lessThanOrEqualTo(userActivityLogRoot.get(UserActivityLog_.createDate), toDate));
				}

				if (userActivityFilter.getCategory() != null)
				{
					filter = cb.and(filter, cb.or(cb.isNull(userActivityLogRoot.get(UserActivityLog_.category)), cb.equal(userActivityLogRoot.get(UserActivityLog_.category), userActivityFilter.getCategory())));
				}

				subQuery.select(userActivityLogRoot.join(UserActivityLog_.user)).where(filter);

				return cb.in(root).value(subQuery);
			};
		}


		@Override
		public Specification<User> visit(SubscriptionStateFilter subscriptionStateFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				final LocalDateTime now = LocalDateTime.now();

				final SubscriptionStateCriteriaBuilder subscriptionStateCriteriaBuilder = new SubscriptionStateCriteriaBuilder(cb);

				final Subscription.SubscriptionState subscriptionState = subscriptionStateFilter.getEnumValue();

				if(!subscriptionStateFilter.getEnumValue().equals(Subscription.SubscriptionState.PENDING)){

				final Predicate subscriptionStatePredicate = subscriptionStateCriteriaBuilder.build(subscriptionRoot, subscriptionState, now);

				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(subscriptionStatePredicate);

				return cb.in(root).value(subQuery);}

				else {
					subQuery.select(subscriptionRoot.join(Subscription_.user));
					final In<User> inClause = cb.in(root).value(subQuery);
					return cb.not(inClause);
				}
			};
		}

		@Override
		public Specification<User> visit(CancelReasonFilter cancelReasonFilter)
		{
			return (root, query, cb) -> cb.isMember(cancelReasonFilter.getEnumValue(), root.get(User_.cancelReasons));
		}

		@Override
		public Specification<User> visit(UserPrepaymentUsageFilter userPrepaymentUsageFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Invoice> invoiceRoot = subQuery.from(Invoice.class);

				SetJoin<Invoice, AbstractPayment> join = invoiceRoot.join(Invoice_.payments);
				Path<String> usagePath = cb.treat(join, Prepayment.class).get(Prepayment_.externalReferenceId);

				String pattern = "%" + Strings.nullToEmpty(userPrepaymentUsageFilter.getValue()) + "%";

				Predicate usageLikePredicate = cb.like(usagePath, pattern);

				subQuery.select(invoiceRoot.join(Invoice_.user)).where(usageLikePredicate);

				return cb.in(root).value(subQuery);
			};
		}

		/*
		 * This method filters users only, so if one user has several payments and one of them has the
		 * requested external reference id, all these payments will be found.
		 */
		@Override
		public Specification<User> visit(ExternalReferenceIdFilter externalReferenceIdFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<AbstractPayment> paymentRoot = subQuery.from(AbstractPayment.class);
				Predicate externalReferenceIdFilterPredicate=null;
				if(exactCheckBox.equals("false") || exactCheckBox.equals(""))
				{
					externalReferenceIdFilterPredicate=cb.like(paymentRoot.get(AbstractPayment_.externalReferenceId), "%" + externalReferenceIdFilter.getValue() + "%");
				}
				else
				{
					externalReferenceIdFilterPredicate=cb.like(paymentRoot.get(AbstractPayment_.externalReferenceId), externalReferenceIdFilter.getValue());
				}

				subQuery.select(paymentRoot.join(AbstractPayment_.user)).where(externalReferenceIdFilterPredicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(ConfirmationFilter confirmationFilter)
		{
			return (root, query, cb) -> cb.equal(root.get(User_.emailConfirmed), confirmationFilter.isValue());
		}

		@Override
		public Specification<User> visit(CityFilter cityFilter)
		{
			return (root, query, cb) -> cb.like(root.join(User_.addresses).get(Address_.city), "%" + cityFilter.getValue() + "%");
		}

		@Override
		public Specification<User> visit(SexChoiceQuestionFilter sexChoiceQuestionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ChoiceAnswer> answerRoot = subQuery.from(ChoiceAnswer.class);

				final Predicate questionPredicate = cb.equal(answerRoot.join(ChoiceAnswer_.question).get(Question_.i18nKey), NaturalKey.SEX.naturalKey);
				final Predicate choicePredicate = cb.isMember(sexChoiceQuestionFilter.getChoice(), answerRoot.get(ChoiceAnswer_.choices));

				subQuery.select(answerRoot.join(ChoiceAnswer_.user)).where(cb.and(questionPredicate, choicePredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(MatchFilter matchFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<Long> sourceUserSubQuery = query.subquery(Long.class);
				final Root<Match> sourceUserMatchRoot = sourceUserSubQuery.from(Match.class);

				final Subquery<Long> targetUserSubQuery = query.subquery(Long.class);
				final Root<Match> targetUserMatchRoot = targetUserSubQuery.from(Match.class);

				sourceUserSubQuery.select(cb.count(sourceUserMatchRoot.get(Match_.sourceUser)))
						.where(cb.equal(sourceUserMatchRoot.get(Match_.sourceUser), root));
				targetUserSubQuery.select(cb.count(targetUserMatchRoot.get(Match_.targetUser)))
						.where(cb.equal(targetUserMatchRoot.get(Match_.targetUser), root));

				final Expression<Long> matches = cb.sum(sourceUserSubQuery, targetUserSubQuery);

				return cb.lessThanOrEqualTo(matches, 0L);
			};
		}

		@Override
		public Specification<User> visit(RelationshipFilter relationshipFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<Long> subQuery = query.subquery(Long.class);
				final Root<Relationship> relationshipRoot = subQuery.from(Relationship.class);

				Predicate wherePredicates = cb.equal(relationshipRoot.get(Relationship_.sourceUser), root);

				if (relationshipFilter.getFrom() != null)
				{
					wherePredicates = cb.and(wherePredicates, cb.greaterThanOrEqualTo(relationshipRoot.get(Relationship_.createDate), relationshipFilter.getFrom()));
				}
				if (relationshipFilter.getTo() != null)
				{
					wherePredicates = cb.and(wherePredicates, cb.lessThanOrEqualTo(relationshipRoot.get(Relationship_.createDate), relationshipFilter.getTo()));
				}

				subQuery.select(cb.count(relationshipRoot.get(Relationship_.sourceUser))).where(wherePredicates);

				switch (relationshipFilter.getDirection())
				{
					case GREATER:
						return cb.greaterThan(subQuery, (long) relationshipFilter.getThreshold());
					default:
						return cb.lessThanOrEqualTo(subQuery, (long) relationshipFilter.getThreshold());
				}
			};
		}

		@Override
		public Specification<User> visit(MessageFilter messageFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<Long> subQuery = query.subquery(Long.class);
				final Root<Message> messageRoot = subQuery.from(Message.class);

				final Path<User> selectUser;
				switch (messageFilter.getDirectory())
				{
					case INCOMING:
						selectUser = messageRoot.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user);
						break;
					default:
						selectUser = messageRoot.join(Message_.senderEnvelope).get(ReceiverEnvelope_.user);
						break;
				}

				final Predicate equalUser = cb.equal(selectUser, root);
				final Predicate sentPredicate = cb.equal(messageRoot.get(Message_.sent), true);

				subQuery.select(cb.count(selectUser)).where(equalUser, sentPredicate);

				switch (messageFilter.getDirection())
				{
					case GREATER:
						return cb.greaterThan(subQuery, (long) messageFilter.getThreshold());
					default:
						return cb.lessThanOrEqualTo(subQuery, (long) messageFilter.getThreshold());
				}
			};
		}

		@Override
		public Specification<User> visit(MissingAnswersRatioFilter missingAnswersRatioFilter)
		{
			final RecommendationCategory category = missingAnswersRatioFilter.getCategory();
			final Collection<Question> questions = matchingUtilService.getRequiredQuestions(category);
			final Collection<Question> requiredQuestions = MatchingUtils.getRequiredCount(questions, category);

			return (root, query, cb) ->
			{
				if (requiredQuestions.isEmpty()) return null;

				final Subquery<Long> subQuery = query.subquery(Long.class);
				final Root<? extends Answer> answerRoot = subQuery.from(Answer.class);

				final Predicate questionPredicate = answerRoot.get(Answer_.question).in(requiredQuestions);
				final Predicate userPredicate = cb.equal(answerRoot.get(Answer_.user), root);

				final Predicate numberAnswerPredicate = cb.and(
						cb.equal(answerRoot.type(), NumberAnswer.class),
						((Root<NumberAnswer>) answerRoot).get(NumberAnswer_.numberValue).isNotNull());

				final Predicate choiceAnswerPredicate = cb.and(
						cb.equal(answerRoot.type(), ChoiceAnswer.class),
						cb.greaterThan(cb.size(((Root<ChoiceAnswer>) answerRoot).get(ChoiceAnswer_.choices)), 0));

				final Predicate completePredicate = cb.or(numberAnswerPredicate, choiceAnswerPredicate);

				subQuery.select(cb.count(answerRoot)).where(userPredicate, questionPredicate, completePredicate);

				return cb.lessThanOrEqualTo(cb.quot(subQuery, (float) requiredQuestions.size()).as(Float.class), (float) ALLOWED_MISSING_ANSWERS_RATIO);
			};
		}

		@Override
		public Specification<User> visit(FreeTextFilter freeTextFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<TextAnswer> answerRoot = subQuery.from(TextAnswer.class);

				final Path<String> x = answerRoot.join(TextAnswer_.question).join(Question_.questionGroup).join(QuestionGroup_.questionnaire).get(Question_.i18nKey);

				final Predicate friendshipQuestionPredicate = cb.equal(x, NaturalKey.FREE_TEXT_FRIENDSHIP.naturalKey);
				final Predicate partnerQuestionPredicate = cb.equal(x, NaturalKey.FREE_TEXT_PARTNER.naturalKey);
				final Predicate questionPredicate = cb.or(friendshipQuestionPredicate, partnerQuestionPredicate);
				final Predicate valuePredicate = cb.like(answerRoot.get(TextAnswer_.textValue), "%" + freeTextFilter.getValue() + "%");

				subQuery.select(answerRoot.join(ChoiceAnswer_.user)).where(cb.and(questionPredicate, valuePredicate));

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(DisableFootprintNotificationsFilter disableFootprintNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.disableFootprintNotifications), disableFootprintNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(DisableNewsNotificationsFilter disableNewsNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.disableNewsNotifications), disableNewsNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.disableCipherMessageNotifications), disableCipherMessageNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.disablePositiveRankingNotifications), disablePositiveRankingNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.disableRecommendationNotifications), disableRecommendationNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(EnableMarketingNotificationsFilter enableMarketingNotificationsFilter)
		{
			return (root, query, cb) -> cb.equal(root.join(User_.userSettings).get(UserSettings_.enableMarketingNotifications), enableMarketingNotificationsFilter.isValue());
		}

		@Override
		public Specification<User> visit(MailBlockedFilter mailBlockedFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<String> subQuery = query.subquery(String.class);
				final Root<UndeliverableMail> undeliverableMailRoot = subQuery.from(UndeliverableMail.class);

				final List<Predicate> undeliverablePredicates = new ArrayList<>();
				for (UndeliverableMailReason undeliverableMailReason : UndeliverableMailReason.values())
				{
					final Predicate reasonPredicate = cb.equal(undeliverableMailRoot.get(UndeliverableMail_.undeliverableMailReason), undeliverableMailReason);
					final Predicate incidentsPredicate = cb.greaterThan(undeliverableMailRoot.get(UndeliverableMail_.incidents), undeliverableMailReason.getMaxIncidents());
					undeliverablePredicates.add(cb.and(reasonPredicate, incidentsPredicate));
				}

				final Predicate orPredicate = undeliverablePredicates.stream().reduce(cb::or).orElse(cb.conjunction());

				subQuery.select(undeliverableMailRoot.get(UndeliverableMail_.recipientEmail)).where(orPredicate);

				final In<String> inClause = cb.in(root.get(User_.email)).value(subQuery);
				return mailBlockedFilter.isValue() ? inClause : cb.not(inClause);
			};
		}

		@Override
		public Specification<User> visit(BlockedFilter blockedFilter)
		{
			return (root, query, cb) ->
			{
				Predicate userBlockedFilter = null;

				if (blockedFilter.isValue()) {
					userBlockedFilter = cb.equal(root.get(User_.blockedStatus), BlockedStatus.BLOCKED);
				} else {
					userBlockedFilter = cb.notEqual(root.get(User_.blockedStatus), BlockedStatus.BLOCKED);
				}
				return userBlockedFilter;
			};

		}

		@Override
		public Specification<User> visit(AdminBlockedFilter adminBlockedFilter)
		{
			return (root, query, cb) ->
			{
				Predicate adminBLockedFilter = null;

				if (adminBlockedFilter.isValue()) {
					adminBLockedFilter = cb.equal(root.get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED);
				} else {
					adminBLockedFilter = cb.notEqual(root.get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED);
				}
				return adminBLockedFilter;
			};
		}

		@Override
		public Specification<User> visit(AudioFriendshipFilter audioFriendshipFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<UserAudio> audioRoot = subQuery.from(UserAudio.class);
				Predicate audioPredicate =null;
				log.info("audioFriendshipFilter.isValue()==="+audioFriendshipFilter.isValue());
				if (audioFriendshipFilter.isValue()) {
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.friendship),Boolean.TRUE);
				}
				else if(audioFrndShipPrtnrFilterUnary)
				{
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.friendship),Boolean.TRUE);
				}
				else
				{
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.friendship),Boolean.FALSE);
				}

				subQuery.select(audioRoot.join(UserAudio_.author)).where(audioPredicate);

				return cb.in(root).value(subQuery);
			};

		}

		@Override
		public Specification<User> visit(AudioPartnershipFilter audioPartnershipFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<UserAudio> audioRoot = subQuery.from(UserAudio.class);
				Predicate audioPredicate =null;
				log.info("audioPartnershipFilter.isValue()==="+audioPartnershipFilter.isValue());
				if (audioPartnershipFilter.isValue()) {
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.partnership),Boolean.TRUE);
				}
				else if(audioFrndShipPrtnrFilterUnary)
				{
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.partnership),Boolean.TRUE);
				}
				else
				{
					audioPredicate =  cb.equal(audioRoot.get(UserAudio_.partnership),Boolean.FALSE);
				}

				subQuery.select(audioRoot.join(UserAudio_.author)).where(audioPredicate);

				return cb.in(root).value(subQuery);
			};
		}
		@Override
		public Specification<User> visit(PaymentTypeFilter paymentTypeFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<AbstractPayment> paymentRoot = subQuery.from(AbstractPayment.class);

				final Predicate typeFilter = cb.equal(paymentRoot.type(), paymentTypeFilter.getEnumValue().getPaymentClass());
				final Predicate currentPredicate = cb.equal(paymentRoot.get(AbstractPayment_.current), Boolean.TRUE);
				final Predicate filter = cb.and(typeFilter, currentPredicate);

				subQuery.select(paymentRoot.join(AbstractPayment_.user)).where(filter);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(RecommendationBreakFilter recommendationBreakFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<RecommendationBreak> recommendationBreakRoot = subQuery.from(RecommendationBreak.class);

				final Predicate categoryPredicate = cb.equal(recommendationBreakRoot.get(RecommendationBreak_.category), recommendationBreakFilter.getEnumValue());
				final Predicate activePredicate = cb.or(cb.isNull(recommendationBreakRoot.get(RecommendationBreak_.endDate)), cb.greaterThanOrEqualTo(recommendationBreakRoot.get(RecommendationBreak_.endDate), LocalDate.now()));
				final Predicate filter = cb.and(activePredicate, categoryPredicate);

				subQuery.select(recommendationBreakRoot.join(RecommendationBreak_.user)).where(filter);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(UserRegistrationFilter userRegistrationFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<UserRegistrationState> userRegistrationStateRoot = subQuery.from(UserRegistrationState.class);

				final Predicate statePredicate = cb.equal(userRegistrationStateRoot.get(UserRegistrationState_.registrationState), userRegistrationFilter.getEnumValue());

				subQuery.select(userRegistrationStateRoot.join(UserRegistrationState_.user)).where(statePredicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(HasSubscriptionFilter hasSubscriptionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				subQuery.select(subscriptionRoot.join(Subscription_.user));

				final In<User> inClause = cb.in(root).value(subQuery);
				return hasSubscriptionFilter.isValue() ? inClause : cb.not(inClause);
			};
		}

		@Override
		public Specification<User> visit(AutoRenewalFilter autoRenewalFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				final Predicate predicate = cb.and(
						cb.equal(subscriptionRoot.get(Subscription_.automaticRenewal), autoRenewalFilter.isValue()),
						cb.equal(subscriptionRoot.get(Subscription_.current), true));

				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(predicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(InvoicesProductFilter invoicesProductFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Invoice> invoiceRoot = subQuery.from(Invoice.class);

				final Predicate predicate = cb.like(invoiceRoot.join(Invoice_.items).join(InvoiceItem_.product).get(Product_.name), "%" + invoicesProductFilter.getValue() + "%");

				subQuery.select(invoiceRoot.join(Invoice_.user)).where(predicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(SubscriptionsProductFilter subscriptionsProductFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				final Predicate predicate = cb.like(subscriptionRoot.join(Subscription_.offer).get(Product_.name), "%" + subscriptionsProductFilter.getValue() + "%");

				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(predicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(PaymentStateFilter paymentStateFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<AbstractPayment> paymentRoot = subQuery.from(AbstractPayment.class);

				final Predicate stateFilter = cb.equal(paymentRoot.get(AbstractPayment_.state), paymentStateFilter.getEnumValue());
				final Predicate currentPredicate = cb.equal(paymentRoot.get(AbstractPayment_.current), Boolean.TRUE);
				final Predicate filter = cb.and(stateFilter, currentPredicate);

				subQuery.select(paymentRoot.join(AbstractPayment_.user)).where(filter);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(DateFilter dateFilter)
		{
			return (root, query, cb) ->
			{

				final Subquery<User> subQuery = query.subquery(User.class);

				final Root<Invoice> invoiceRoot = subQuery.from(Invoice.class);

				final Predicate predicate = cb.between(invoiceRoot.join(Invoice_.items).get(InvoiceItem_.createDate),dateFilter.getStartDate(),dateFilter.getEndDate());

				final Predicate predicate1 = cb.equal(invoiceRoot.join(Invoice_.items).join(InvoiceItem_.product).get(Product_.dtypeValue),DateFilter.convertToTitleCaseIteratingChars(dateFilter.getProductType().toString()).replaceAll(" ", ""));

				final Predicate stateFilter = cb.equal(invoiceRoot.join(Invoice_.payments).get(AbstractPayment_.state), dateFilter.getPaymentState());

				final Predicate amountGreaterFilter = cb.gt(invoiceRoot.join(Invoice_.items).get(InvoiceItem_.amount).get(MonetaryAmount_.amount),dateFilter.getValue());

				final Predicate amountLessFilter = cb.le(invoiceRoot.join(Invoice_.items).get(InvoiceItem_.amount).get(MonetaryAmount_.amount),dateFilter.getValue1());

				if(dateFilter.getValue()!=null && dateFilter.getValue1()!=null && dateFilter.getPaymentState()!=null) {
					//subQuery.select(paymentRoot.join(AbstractPayment_.user)).where(stateFilter);
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(stateFilter, predicate, predicate1, amountGreaterFilter, amountLessFilter);
				}
				else if(dateFilter.getValue()!=null && dateFilter.getPaymentState()!=null){
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(stateFilter, predicate, predicate1, amountGreaterFilter);
				}
				else if(dateFilter.getValue1()!=null && dateFilter.getPaymentState()!=null){
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(stateFilter, predicate, predicate1, amountLessFilter);
				}
				else if(dateFilter.getValue()!=null && dateFilter.getValue1()!=null){
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(predicate, predicate1, amountLessFilter, amountGreaterFilter);
				}
				else if(dateFilter.getPaymentState()!=null && dateFilter.getValue()==null && dateFilter.getValue1()==null){
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(stateFilter,predicate, predicate1);
				}
				else{
					subQuery.select(invoiceRoot.join(Invoice_.user)).where(predicate, predicate1);
				}

				return cb.in(root).value(subQuery);
			};
		}


		@Override
		public Specification<User> visit(SubscriptionEndFilter subscriptionEndFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);
				final Predicate predicate = cb.greaterThanOrEqualTo(subscriptionRoot.get(Subscription_.end),subscriptionEndFilter.getStartDate());
				final Predicate predicate1 = cb.lessThanOrEqualTo(subscriptionRoot.get(Subscription_.end),subscriptionEndFilter.getEndDate());
				//final Predicate predicate2 = cb.equal(subscriptionRoot.get(Subscription_.current),true);
				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(predicate, predicate1);
				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(ActiveSubscriptionFilter activeSubscriptionFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);
				final Predicate predicate = cb.greaterThanOrEqualTo(subscriptionRoot.get(Subscription_.end),activeSubscriptionFilter.getStartDate());
				final Predicate predicate1 = cb.lessThanOrEqualTo(subscriptionRoot.get(Subscription_.begin),activeSubscriptionFilter.getEndDate());

				final Predicate filter = cb.and(predicate,predicate1);
				//final Predicate predicate2 = cb.equal(subscriptionRoot.get(Subscription_.current),true);
				//final Predicate predicate3 = cb.equal(subscriptionRoot.get(Subscription_.state), Subscription.SubscriptionState.ACTIVE);
				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(filter);
				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(DeactivatedProlongationsFilter deactivatedProlongationsFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ActiveDeactiveSubscription> activeDeactiveSubscriptionRoot = subQuery.from(ActiveDeactiveSubscription.class);
				final Predicate predicate = cb.greaterThanOrEqualTo(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.createDate),deactivatedProlongationsFilter.getStartDate());
				final Predicate predicate1 = cb.lessThanOrEqualTo(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.createDate),deactivatedProlongationsFilter.getEndDate());
				final Predicate predicate2 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.type),"Deactivated");

				Predicate filter=null;

				final Predicate predicate3 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.userType), "Member");
				filter = cb.and(predicate,predicate1,predicate2,predicate3);

				/*if(deactivatedProlongationsFilter.isValue()) {
					final Predicate predicate3 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.userType), "Member");
					filter = cb.and(predicate,predicate1,predicate2,predicate3);
				}
				else
				{
					filter = cb.and(predicate,predicate1,predicate2);
				}*/

				subQuery.select(activeDeactiveSubscriptionRoot.join(ActiveDeactiveSubscription_.user)).where(filter);
				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(ActivatedProlongationsFilter activatedProlongationsFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ActiveDeactiveSubscription> activeDeactiveSubscriptionRoot = subQuery.from(ActiveDeactiveSubscription.class);
				final Predicate predicate = cb.greaterThanOrEqualTo(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.createDate),activatedProlongationsFilter.getStartDate());
				final Predicate predicate1 = cb.lessThanOrEqualTo(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.createDate),activatedProlongationsFilter.getEndDate());

				final Predicate predicate2 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.type),"Activated");

				Predicate filter=null;

				final Predicate predicate3 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.userType), "Member");
				filter = cb.and(predicate,predicate1,predicate2,predicate3);

				/*if(activatedProlongationsFilter.isValue()) {
					final Predicate predicate3 = cb.equal(activeDeactiveSubscriptionRoot.get(ActiveDeactiveSubscription_.userType), "Member");
					filter = cb.and(predicate,predicate1,predicate2,predicate3);
				}
				else
				{
					filter = cb.and(predicate,predicate1,predicate2);
				}*/

				subQuery.select(activeDeactiveSubscriptionRoot.join(ActiveDeactiveSubscription_.user)).where(filter);
				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(LastLoginFilter lastLoginFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ClientInformation> invoiceRoot = subQuery.from(ClientInformation.class);

				final Predicate predicate1 = cb.greaterThanOrEqualTo(invoiceRoot.get(ClientInformation_.createDate),lastLoginFilter.getStartDate());
				final Predicate predicate2 = cb.lessThanOrEqualTo(invoiceRoot.get(ClientInformation_.createDate),lastLoginFilter.getEndDate());

				final Predicate predicate3 = cb.greaterThanOrEqualTo(invoiceRoot.get(ClientInformation_.changeDate),lastLoginFilter.getStartDate());
				final Predicate predicate4 = cb.lessThanOrEqualTo(invoiceRoot.get(ClientInformation_.changeDate),lastLoginFilter.getEndDate());

				final Predicate filter1 = cb.and(predicate1, predicate2);
				final Predicate filter2 = cb.and(predicate3, predicate4);

				final Predicate filter = cb.or(filter1, filter2);

				final Predicate predicate5 = cb.equal(invoiceRoot.get(ClientInformation_.lastSeen),true);
				subQuery.select(invoiceRoot.join(ClientInformation_.user)).where(predicate5, filter);
				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(LastSeenBrowserFilter lastSeenBrowserFilter)
		{
			return (root, query, cb) ->
			{
				final Predicate predicate;
				final Predicate predicate1;
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<ClientInformation> invoiceRoot = subQuery.from(ClientInformation.class);
				final Predicate predicate2 = cb.equal(invoiceRoot.get(ClientInformation_.lastSeen),true);
				if(lastSeenBrowserFilter.getCheckBoxvalue()) {
					predicate = cb.equal(invoiceRoot.get(ClientInformation_.browser.getName()), lastSeenBrowserFilter.getEnumValue());
					predicate1 =cb.equal(invoiceRoot.get(ClientInformation_.layout), ClientInformation.Device.MOBILE);
					subQuery.select(invoiceRoot.join(ClientInformation_.user)).where(cb.and(predicate,predicate1, predicate2));
				}
				else{
					predicate = cb.equal(invoiceRoot.get(ClientInformation_.browser.getName()), lastSeenBrowserFilter.getEnumValue());
					subQuery.select(invoiceRoot.join(ClientInformation_.user)).where(predicate,predicate2);
				}

				return cb.in(root).value(subQuery);
			};
		}
//		where(cb.and(categoryPredicate, activePredicate));


		@Override
		public Specification<User> visit(CurrentProductFilter currentProductFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<Subscription> subscriptionRoot = subQuery.from(Subscription.class);

				final Predicate predicate = cb.and(
						cb.like(subscriptionRoot.join(Subscription_.offer).get(Product_.name), "%" + currentProductFilter.getValue() + "%"),
						cb.equal(subscriptionRoot.get(Subscription_.current), true));

				subQuery.select(subscriptionRoot.join(Subscription_.user)).where(predicate);

				return cb.in(root).value(subQuery);
			};
		}

		@Override
		public Specification<User> visit(UserIdFilter userIdFilter)
		{
			return (root, query, cb) -> cb.equal(root.get(User_.id), userIdFilter.getValue());
		}

		@Override
		public Specification<User> visit(PaymentIdFilter paymentIdFilter)
		{
			return (root, query, cb) ->
			{
				final Subquery<User> subQuery = query.subquery(User.class);
				final Root<AbstractPayment> paymentRoot = subQuery.from(AbstractPayment.class);
				final Predicate filter = cb.equal(paymentRoot.get(AbstractPayment_.id), paymentIdFilter.getValue());

				subQuery.select(paymentRoot.join(AbstractPayment_.user)).where(filter);

				return cb.in(root).value(subQuery);
			};
		}
	}

	private final MatchingUtilService matchingUtilService;

	@Autowired
	public FilterSpecificationBuilder(MatchingUtilService matchingUtilService)
	{
		this.matchingUtilService = matchingUtilService;
	}

	public Specification<User> build(AbstractFilter filter,String exactCheckBox)
	{
		this.exactCheckBox=exactCheckBox;
		if (filter == null) return null;
		return filter.accept(new FilterSpecificationBuilderVisitor());
	}
}
