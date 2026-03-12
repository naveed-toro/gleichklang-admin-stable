package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for accessing the {@link Subscription} entities.
 */
@Repository
public interface SubscriptionRepository
		extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription>
{
	/**
	 * Finds all subscriptions of given user.
	 *
	 * @param user the user
	 *
	 * @return the list of subscriptions
	 */
	List<Subscription> findAllByUser(@Param("user") User user);
	
	/**
	 * Finds the current user subscription.
	 *
	 * @param user the user
	 *
	 * @return the current subscription or null if the user hasn't payed the initial subscription yet
	 */
	@Query("SELECT s FROM Subscription s JOIN FETCH s.offer JOIN FETCH s.user WHERE s.current = true AND s.user = :user")
	Optional<Subscription> findCurrentSubscription(@Param("user") User user);
	
	/**
	 * Finds the current user subscription.
	 *
	 * @param userId the user
	 *
	 * @return the current subscription or null if the user hasn't payed the initial subscription yet
	 */
	@Query("SELECT s FROM Subscription s WHERE s.current = true AND s.user.id = :user")
	Optional<Subscription> findCurrentSubscription(@Param("user") Long userId);

	/**
	 * Returns the recommendation categories of the given users current subscription.
	 *
	 * @param user the user
	 * @return the recommendation categories of the subscription offer
	 * {@link de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer#categories}
	 */
	@Query("SELECT DISTINCT c.category FROM SubscriptionOfferCategory c, Subscription s  WHERE"
			+ " s.offer = c.subscriptionOffer AND"
			+ " s.current = true AND s.user = :user")
	Set<RecommendationCategory> findCurrentSubscriptionOfferCategories(@Param("user") User user);

	/**
	 * Returns the recommendation categories of the given users current subscription
	 *
	 * @param userId the userId
	 * @return the recommendation categories of the subscription offer
	 * {@link de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer#categories}
	 */
	@Query("SELECT DISTINCT c.category FROM SubscriptionOfferCategory c, Subscription s  WHERE"
			+ " s.offer = c.subscriptionOffer AND"
			+ " s.current = true AND s.user.id = :user AND s.begin <= :date AND s.end >= :date")
	Set<RecommendationCategory> findCurrentSubscriptionOfferCategories(@Param("user") Long userId, @Param("date") LocalDateTime date);

	/**
	 * Returns the recommendation categories of the given users last subscription.
	 *
	 * @param userId the userId
	 * @return the recommendation categories of the subscription offer
	 * {@link de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer#categories}
	 */
	@Query(
			value = "SELECT DISTINCT c.category FROM subscription_offer_category c " +
					"WHERE c.subscription_offer_id = " +
					"(SELECT s.offer_id FROM subscription s WHERE s.user_id = ?1 ORDER BY s.create_date DESC LIMIT 1)",
			nativeQuery = true)
	List<String> findLastSubscriptionOfferCategories(Long userId);

	/**
	 * Finds the current active subscriptions that are expiring at the given date.
	 *
	 * @param date the expiration date
	 * @param pageable the pageable
	 * @return the page with the current active subscriptions
	 */
	@Query("SELECT s FROM Subscription s WHERE s.state = 'ACTIVE' AND s.end < :date")
	Page<Subscription> findSubscriptionStateTransitionToExpiring(@Param("date") LocalDateTime date, Pageable pageable);
	
	/**
	 * Finds expired subscriptions at the given date.
	 *
	 * @param date the expiration date
	 * @param pageable the pageable
	 * @return the page with the current active subscriptions
	 */
	@Query("SELECT s FROM Subscription s WHERE (s.state = 'EXPIRING' OR s.state = 'EXPIRED') AND s.expirationDate < :date")
	Page<Subscription> findSubscriptionStateTransitionToExpired(@Param("date") LocalDateTime date, Pageable pageable);

	@Transactional
	@Modifying
	@Query(
			value = "update subscription s inner join user_ u on u.id=s.user_id set " +
					"s.state = 'CANCELED'" +
					"where u.id=?1 and s.current = 1 AND (s.state = 'ACTIVE' OR s.state = 'EXPIRED')",
			nativeQuery = true)
	public void cancelSubscription(Long userId);
}
