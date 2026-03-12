package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.BlockedStatus;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.SignableUserRepository;
import de.binaerebauten.gleichklang.core.repository.SpareEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends SignableUserRepository<User>, SpareEntityRepository<User, Long>
{
	@Query("SELECT birthDate FROM User WHERE id = ?1")
	LocalDate findBirthDateById(Long id);

	@Transactional
	@Modifying
	@Query("UPDATE User u SET "
			+ "u.email = NULL, "
			+ "u.birthDate = NULL, "
			+ "u.lastName = NULL, "
			+ "u.firstName = NULL, "
			+ "u.password = NULL, "
			+ "u.registerIp = NULL, "
			+ "u.confirmationIp = NULL, "
			+ "u.confirmationDate = NULL, "
			+ "u.memberStatus = ?1 "
			+ "WHERE u = ?2")
	void deleteUser(MemberStatus memberStatus ,User user);// Adding member status for admin_deleted or deleted(user). it is making easier to find out the who's deleted the user.
	
	@Query("SELECT alias FROM User WHERE id = ?1")
	String findAliasById(Long id);

	User findById(Long id);
	
	@Query("SELECT u FROM User u JOIN u.userSettings us WHERE u.email IN (?1) AND u.memberStatus = ?2 AND us.disableNewsNotifications = false")
	List<User> findUsersWithUserNewsActivated(Collection<String> mails, MemberStatus canceled);

	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.isBlocked = true, u.blockedStatus = ?2,u.blockedDate=current_date WHERE u = ?1")
	public void updateUser(User user,BlockedStatus blockedStatus);


	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.isBlocked = false WHERE u = ?1")
	public void unblockUser(User user);

	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.isBlocked = false, u.memberStatus = ?1, u.blockedStatus = ?3 , u.blockedDate=?4 , u.adminBlockedDate = ?4 WHERE u = ?2")
	public void unblockUserAdmin(MemberStatus memberStatus, User user, BlockedStatus blockedStatus, LocalDateTime localDateTime);


	@Query("FROM User u WHERE u.isBlocked = true")
	public Page<User> findByBlockedTrue(Specification<User> var1, Pageable var2);

	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.isBlocked = true,u.blockedStatus = ?2 ,u.blockedDate= ?3 WHERE u.id = ?1")
	public void updateUserBlocked(long userId, BlockedStatus blockedStatus, LocalDateTime localDateTime);

	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.blockedStatus = ?1 ,u.adminBlockedDate=current_date WHERE u = ?2")
	public void blockedByAdmin(BlockedStatus blockedStatus, User user);


	@Transactional
	@Modifying
	@Query("UPDATE User u SET u.memberStatus = ?1 WHERE u = ?2")
	public void updateUser(MemberStatus memberStatus ,User user);

	@Query("FROM User u WHERE u.alias = ?1 or u.email = ?1")
	User findByEmailOrAlias(String emailOrAlias);

	@Query(value = "call generate_member_stat(?1)", nativeQuery = true)
	Long generateUserStatistics(Integer limit);
}
