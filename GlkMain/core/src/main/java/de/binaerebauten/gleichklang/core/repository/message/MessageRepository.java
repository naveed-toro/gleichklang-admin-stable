package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message>
{



	@Query("SELECT COUNT(m) FROM Message m, Relationship r WHERE m.deleted = false AND m.sent = true AND m.senderEnvelope.user = ?1 "
			+ "AND m.senderEnvelope.user = r.sourceUser AND m.receiverEnvelope.user = r.targetUser AND ?2 MEMBER OF r.categories "
			+ "AND m.messageType <> 'CANCEL_MESSAGE'")
	Long countOutgoingsByUser(User user, RecommendationCategory category);

	@Query("SELECT COUNT(m) FROM Message m, Relationship r WHERE m.deleted = false AND m.sent = true AND m.receiverEnvelope.user = ?1 "
			+ "AND m.senderEnvelope.user = r.sourceUser AND m.receiverEnvelope.user = r.targetUser AND ?2 MEMBER OF r.categories "
			+ "AND m.messageType <> 'CANCEL_MESSAGE'")
	Long countIncomingsByUser(User user, RecommendationCategory recommendationCategory);

	/*@Query("SELECT COUNT(m) FROM Message m, Relationship r WHERE m.deleted = false AND m.sent = true AND m.receiverEnvelope.user = ?1 AND (m.subject!='Contact deleted' OR m.subject!='Kontakt entfernt')"
			+ "AND m.senderEnvelope.user = r.sourceUser AND m.receiverEnvelope.user = r.targetUser AND m.senderEnvelope.user.memberStatus<> 'BLOCKED' AND m.senderEnvelope.user.memberStatus<> 'ADMIN_BLOCKED' AND ?2 MEMBER OF r.categories "
			+ "AND m.receiverEnvelope.read = false AND m.receiverEnvelope.hidden = false AND m.receiverEnvelope.deleted = false "
			+ "AND m.messageType <> 'CANCEL_MESSAGE'")*/
	@Query("select count(m) from Message as m "
				  + "inner join m.receiverEnvelope as rec1 "
				  + "inner join m.senderEnvelope as rec2 "
				  + "inner join m.senderEnvelope as send1 "
				  + "inner join m.senderEnvelope as send2 "
				  + "where ( ( ( m.sent=true ) and ( rec1.user=?1 ) and ( send1.user is not null ) and rec1.read=false) "
				  + "and ( ( m.deleted=false ) and ( rec1.hidden=false ) and ( rec1.deleted=false) ) "
			      + "and ( ( m.senderEnvelope.user.blockedStatus<>'ADMIN_BLOCKED'  ) and ( m.senderEnvelope.user.memberStatus<>'CANCELED'  ) and ( m.senderEnvelope.user.memberStatus<>'ADMIN_CANCELED'  ) and ( m.senderEnvelope.user.blockedStatus<>'ADMIN_BLOCKED' ) ) ) "
				  + "and ( ( m.messageType<>'CANCEL_MESSAGE'  ) "
				  + "and ( m in (select m from Message as m1, Relationship as r1 inner join m1.senderEnvelope as send3 "
				  + "inner join m1.receiverEnvelope as rec3 where ( send3.user=r1.sourceUser ) and ( rec3.user=r1.targetUser ) "
				  + "and ( ?2 member of r1.categories ) AND ?2 MEMBER OF rec1.user.categories) ) )")
	Long countNewIncomingMessagesByUser(User user, RecommendationCategory recommendationCategory);

	@Query(value = "select count(distinct message0_.id)  from message message0_ "
			+ "cross join relationship relationsh1_ "
			+ "cross join envelope receiveren2_ "
			+ "cross join envelope senderenve3_ "
			+ "cross join user_ user6_ "
			+ "cross join user_ user7_ "
			+ "inner join user_recommendation_category urct ON relationsh1_.target_user_id = urct.user_id "
			+ "inner join user_recommendation_category urcs ON relationsh1_.source_user_id = urcs.user_id "
			+ "inner join relationship_category rc on rc.relationship_id=relationsh1_.id  AND relationsh1_.source_user_id=urcs.user_id "
			+ "AND urct.categories = rc.category "
			+ "AND urcs.categories = rc.category "
			+ "where message0_.id=receiveren2_.message_id and receiveren2_.DTYPE='ReceiverEnvelope' and message0_.id=senderenve3_.message_id "
			+ "and receiveren2_.user_id=user7_.id "
			+ "and user6_.Blocked_State<>'ADMIN_BLOCKED' "
			+ "and senderenve3_.DTYPE='SenderEnvelope' and senderenve3_.user_id=user6_.id and message0_.deleted=0 and message0_.sent=1 and receiveren2_.user_id= ?1 "
			+ "and ( message0_.subject<>'Contact deleted'  or message0_.subject<>'Kontakt entfernt' ) and senderenve3_.user_id=relationsh1_.source_user_id "
			+ "and receiveren2_.user_id=relationsh1_.target_user_id "
			+ "and receiveren2_.read_=0 and receiveren2_.hidden=0 and receiveren2_.deleted=0 and message0_.message_type<>'CANCEL_MESSAGE' and (DATE_SUB(user6_.blockedDate, INTERVAL 3 MINUTE)>senderenve3_.create_date or user6_.isBlocked = 0) and (DATE_SUB(user7_.blockedDate, INTERVAL 3 MINUTE) > receiveren2_.create_date or user7_.isBlocked = 0)",
			nativeQuery = true)
	Long countNewIncomingMessagesByUser(User user);

	@Query("SELECT COUNT(m) FROM Message m WHERE m.deleted = false AND m.sent = true AND m.receiverEnvelope.user = ?1 "
			+ "AND m.senderEnvelope.user is null AND m.receiverEnvelope.hidden = false AND m.receiverEnvelope.read = false")
	Long countIncomingAdminMessagesByUser(User user);

	@Query("SELECT m FROM Message m WHERE m in (SELECT n FROM Message n WHERE n.deleted = false AND n.sent = true AND n.receiverEnvelope.hidden = false AND n.receiverEnvelope.deleted = false AND n.receiverEnvelope.user = ?1 AND n.senderEnvelope.user = ?2 AND (n.receiverEnvelope.createDate< ?3 OR n.receiverEnvelope.read=true)) OR m IN "
			+ "(SELECT o FROM Message o WHERE o.deleted = false AND o.sent = true AND o.senderEnvelope.hidden = false AND o.senderEnvelope.deleted = false AND o.receiverEnvelope.user = ?2 AND o.senderEnvelope.user = ?1) order by m.createDate desc")
	Page<Message> findIncomingsAndOutgoingsByUserAndTargetUser2(User currentUser, User targetUser,LocalDateTime blockedDateminus3Minutes, Pageable pageable);


	@Query("SELECT m FROM Message m WHERE m in (SELECT n FROM Message n WHERE n.deleted = false AND n.sent = true AND n.receiverEnvelope.hidden = false AND n.receiverEnvelope.deleted = false AND n.receiverEnvelope.user = ?1 AND n.senderEnvelope.user = ?2 AND (n.senderEnvelope.createDate< ?3 OR n.receiverEnvelope.read=true)) OR m IN "
			+ "(SELECT o FROM Message o WHERE o.deleted = false AND o.sent = true AND o.senderEnvelope.hidden = false AND o.senderEnvelope.deleted = false AND o.receiverEnvelope.user = ?2 AND o.senderEnvelope.user = ?1) order by m.createDate desc")
	Page<Message> findIncomingsAndOutgoingsByUserAndTargetUser1(User currentUser, User targetUser,LocalDateTime blockedDateminus3Minutes, Pageable pageable);


	@Query("SELECT m FROM Message m WHERE m in (SELECT n FROM Message n WHERE n.deleted = false AND n.sent = true AND n.receiverEnvelope.hidden = false AND n.receiverEnvelope.deleted = false AND n.receiverEnvelope.user = ?1 AND n.senderEnvelope.user = ?2) OR m IN "
			+ "(SELECT o FROM Message o WHERE o.deleted = false AND o.sent = true AND o.senderEnvelope.hidden = false AND o.senderEnvelope.deleted = false AND o.receiverEnvelope.user = ?2 AND o.senderEnvelope.user = ?1) order by m.createDate desc")
	Page<Message> findIncomingsAndOutgoingsByUserAndTargetUser(User currentUser, User targetUser, Pageable pageable);


	@Query("SELECT m FROM Message m WHERE m in (SELECT n FROM Message n WHERE n.deleted = false AND n.sent = true AND n.receiverEnvelope.deleted = false AND n.receiverEnvelope.user = ?1 AND n.senderEnvelope.user = ?2) OR m IN "
			+ "(SELECT o FROM Message o WHERE o.deleted = false AND o.sent = true  AND o.senderEnvelope.deleted = false AND o.receiverEnvelope.user = ?2 AND o.senderEnvelope.user = ?1)")
	Page<Message> findAllByUserAndTargetUser(User currentUser, User targetUser, Pageable pageable);

	@Query("SELECT COUNT(m) FROM Message m WHERE m in (SELECT n FROM Message n WHERE n.deleted = false AND n.sent = true AND n.receiverEnvelope.hidden = false AND n.receiverEnvelope.deleted = false AND n.receiverEnvelope.user = ?1 AND n.senderEnvelope.user = ?2) OR m IN "
			+ "(SELECT o FROM Message o WHERE o.deleted = false AND o.sent = true AND o.senderEnvelope.hidden = false AND o.senderEnvelope.deleted = false AND o.receiverEnvelope.user = ?2 AND o.senderEnvelope.user = ?1)")
	Long countIncomingsAndOutgoingsByUserAndTargetUser(User currentUser, User targetUser);

	@Query("SELECT se.user.id, re.user.id, COUNT(1) FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND se.user IS NOT NULL AND re.user IS NOT NULL AND m.messageType <> 'CANCEL_MESSAGE' GROUP BY se.user.id, re.user.id")
	Set<Object[]> getMessageStatistics();

	/** separated in two queries for massive performance thrust instead of using disjunction **/
	@Query("SELECT se.user.id, re.user.id, m.sendDate FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND re.user IS NOT NULL AND se.user = ?1 AND m.messageType <> 'CANCEL_MESSAGE'")
	Set<Object[]> getSendMessageStatistics(User user);

	@Query("SELECT se.user.id, re.user.id, m.sendDate FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND se.user IS NOT NULL AND re.user = ?1 AND m.messageType <> 'CANCEL_MESSAGE'")
	Set<Object[]> getReceiveMessageStatistics(User user);

	/** separated in two queries for massive performance thrust instead of using disjunction **/
	@Query("SELECT se.user.id, re.user.id, m.sendDate FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND re.user IS NOT NULL AND se.user = ?1 AND ?2 MEMBER OF se.user.categories AND m.messageType <> 'CANCEL_MESSAGE'")
	Set<Object[]> getSendMessageStatistics(User user, RecommendationCategory recommendationCategory);

	@Query("SELECT se.user.id, re.user.id, m.sendDate FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND se.user IS NOT NULL AND re.user = ?1 AND ?2 MEMBER OF re.user.categories AND m.messageType <> 'CANCEL_MESSAGE'")
	Set<Object[]> getReceiveMessageStatistics(User user, RecommendationCategory recommendationCategory);

	@Query("SELECT se.user.id, re.user.id, m.sendDate FROM Message m LEFT JOIN m.receiverEnvelope re LEFT JOIN m.senderEnvelope se "
			+ "WHERE m.sent = true AND se.user IS NOT NULL AND re.user = ?1 AND m.messageType <> 'CANCEL_MESSAGE'")
	Set<Object[]> getRepliedMessageStatistics(User user);

	//TODO Changes for Task 3

	@Query("SELECT COUNT(*) FROM Message m , Relationship r WHERE m.deleted = false AND m.sent = true AND m.replyToMessage IS NULL AND m.senderEnvelope.user = ?1  AND ?2 MEMBER OF r.categories)")
	Long countUniqueMessagesSentByUser(User currentUser , RecommendationCategory recommendationCategory);

	@Query("SELECT COUNT(*) FROM Message m , Relationship r WHERE m.deleted = false AND m.receiverEnvelope.user = ?1 AND m.replyToMessage IS NULL AND ?2 MEMBER OF r.categories")
	Long countUniqueMessagesReceivedByUser(User currentUser , RecommendationCategory recommendationCategory);


	@Query("SELECT COUNT(*) FROM Message n WHERE n.deleted = false AND n.sent = true AND n.senderEnvelope.user = ?1  AND n.replyToMessage IN (SELECT DISTINCT  m.replyToMessage FROM Message m , Relationship r WHERE m.deleted = false AND m.sent = true AND m.senderEnvelope.user = ?1  AND ?2 MEMBER OF r.categories )")
	Long countUniqueRepliesSentByUser(User currentUser , RecommendationCategory recommendationCategory);


	@Query("SELECT COUNT(*) FROM Message m WHERE m.deleted = false AND m.sent = true AND m.receiverEnvelope.user = ?1 AND m.replyToMessage IN (SELECT DISTINCT n.replyToMessage FROM Message n , Relationship r WHERE n.deleted = false AND n.sent = true AND n.replyToMessage IS NULL AND n.senderEnvelope.user = ?1 AND ?2 MEMBER OF r.categories ))")
	Long countUniqueRepliesReceivedByUser(User currentUser , RecommendationCategory recommendationCategory);



	@Query(value = "select count(*) from message m inner join envelope e on m.id=e.message_id inner join user_ u on u.id=e.user_id where ( u.email=?1 and e.DTYPE ='SenderEnvelope') or  (u.email=?2 and e.DTYPE ='ReceiverEnvelope')", nativeQuery = true)
	Long getCountOfMessage(String senderEmail,String receiverEmail);


	//TODO Changes for Task 3

	@Query("SELECT COUNT(m) > 0 FROM Message m LEFT JOIN m.senderEnvelope sender LEFT JOIN m.receiverEnvelope receiver WHERE sender.user = ?1 AND m.messageType = ?2 AND receiver.user IS NULL AND m.sendDate > ?3 AND m.deleted = false")
	boolean isSentToAdmin(User user, MessageType messageType, LocalDateTime startDate);

	@Transactional
	@Modifying
	@Query("UPDATE Message n SET " + DeletableEntity.DELETED + " = true where n.id = ?1)")
	public void deleteScamAdminMessageOnUnblock(Long id);


	@Query("SELECT  m.id FROM Message m WHERE m.deleted = false AND m.sent = true AND m.senderEnvelope.user = ?1")
	public Set<Object[]> selectScamAdminMessageOnUnblock( User user);

	@Query("from Message m WHERE m.deleted = false AND m.sent = true AND m.senderEnvelope.user = ?1 AND  m.messageType = 'LOVE_SCAMMER'")
	public List<Message> findByUserScam(User user);


}