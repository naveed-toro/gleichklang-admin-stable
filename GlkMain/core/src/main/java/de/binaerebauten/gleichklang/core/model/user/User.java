package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity
@NamedEntityGraphs({
		@NamedEntityGraph(name = "User.lazy"),
		@NamedEntityGraph(name = "User.complete", attributeNodes = { @NamedAttributeNode("addresses"), @NamedAttributeNode("cancelReasons") })
})
public class User extends SignableUser {
	@NotNull
	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@SanitizeContent
	@Column(name = "status_message")
	private String statusMessage;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_status", nullable = false)
	private MemberStatus memberStatus;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("id asc")
	private final List<Address> addresses = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
	private UserSettings userSettings;

	@ElementCollection(targetClass = CancelReason.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "cancel_reason", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	private Set<CancelReason> cancelReasons = new HashSet<>();

	@ElementCollection(targetClass = RecommendationCategory.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "user_recommendation_category", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	private Set<RecommendationCategory> categories = new HashSet<>();

	@OneToMany(mappedBy = "user")
	private Set<Avatar> avatar;

	@Column(name = "register_ip")
	private String registerIp;

	@Column(name = "confirmation_ip")
	private String confirmationIp;

	@Column(name = "confirmation_date")
	private LocalDateTime confirmationDate;

	@Column(name = "blockedDate")
	private LocalDateTime blockedDate;

	@Column(name = "adminBlockedDate")
	private LocalDateTime adminBlockedDate;

	@OneToMany(mappedBy = "user")
	private List<Subscription> subscriptions;

	@Enumerated(EnumType.STRING)
	@Column(name = "Blocked_State", nullable = false)
	private BlockedStatus blockedStatus =  BlockedStatus.NOT_BLOCKED;

	@Column(name = "cust_id")
	private String custId;

	@Column(name = "card_id")
	private String cardId;

	//TODO RG: check why this leads to failing tests (maybe syntax-problem)
//	@Formula("(SELECT GROUP_CONCAT( CONCAT(r.category,': ', ( CASE WHEN (r.end_date IS NULL) THEN 'unlimited' ELSE r.end_date END)  )) FROM recommendation_break r WHERE r.user_id = id GROUP BY r.user_id)")
//	private String recommendationBreaks;

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public MemberStatus getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(MemberStatus memberStatus) {
		this.memberStatus = memberStatus;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	/**
	 * Primary address is the address with the lowest sort order
	 *
	 * @return the primary address used for payment purposes
	 * @throws AddressNotFoundException
	 * @deprecated Why does this method throw an exception?
	 */
	public Address getPaymentAddress() throws AddressNotFoundException {
		return getOptionalPaymentAddress().orElseThrow(() -> new AddressNotFoundException(I18N.USER_WARNING_ADDRESS_NOT_EXISTS.msg()));
	}

	/**
	 * Returns the optional payment address.
	 *
	 * @return optional payment address
	 */
	public Optional<Address> getOptionalPaymentAddress() {
		return addresses.stream().filter(Address::isPayment).findFirst();
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses.clear();
		addresses.forEach(this::addAddress);
	}

	public void addAddress(Address address) {
		address.setUser(this);
		this.addresses.add(address);
	}

	public UserSettings getUserSettings() {
		return userSettings;
	}

	public void setUserSettings(UserSettings userSettings) {
		this.userSettings = userSettings;
	}

	public Set<CancelReason> getCancelReasons() {
		return cancelReasons;
	}

	public void setCancelReasons(Set<CancelReason> cancelReasons) {
		this.cancelReasons = cancelReasons;
	}

	public int getAge() {
		if (getBirthDate() == null) return 0;
		return (int) getBirthDate().until(LocalDate.now(), ChronoUnit.YEARS);
	}

	public Set<RecommendationCategory> getCategories() {
		return categories;
	}

	public void setCategories(Set<RecommendationCategory> categories) {
		this.categories = categories;
	}

	/**
	 * Returns an ordered copy of the categories with the
	 * default category (represented by an null entry).
	 *
	 * @return ordered set of categories
	 */
	public Set<RecommendationCategory> getOrderedCategoriesWithDefault() {
		Set<RecommendationCategory> categoriesWithNull = new LinkedHashSet<>();
		categoriesWithNull.add(null);
		categoriesWithNull.addAll(getOrderedCategories());

		return categoriesWithNull;
	}

	/**
	 * This returns an ordered copy of the categories.
	 * Changing of the returned set doesn't affect this entity!
	 *
	 * @return the ordered set of categories
	 */
	public Set<RecommendationCategory> getOrderedCategories() {
		final EnumSet<RecommendationCategory> orderedCategories = categories.isEmpty() ?
				EnumSet.noneOf(RecommendationCategory.class) : EnumSet.copyOf(categories);
		return orderedCategories;
	}

	public Set<Avatar> getAvatar() {
		return avatar;
	}

	public void setAvatar(Set<Avatar> avatar) {
		this.avatar = avatar;
	}

	public String getRegisterIp() {
		return registerIp;
	}

	public void setRegisterIp(String registerIp) {
		this.registerIp = registerIp;
	}

	public String getConfirmationIp() {
		return confirmationIp;
	}

	public void setConfirmationIp(String confirmationIp) {
		this.confirmationIp = confirmationIp;
	}

	public LocalDateTime getConfirmationDate() {
		return confirmationDate;
	}

	public void setConfirmationDate(LocalDateTime confirmationDate) {
		this.confirmationDate = confirmationDate;
	}

	public LocalDateTime getBlockedDate() {return blockedDate=blockedDate; }

	public void setBlockedDate(LocalDateTime blockedDate) {this.blockedDate = blockedDate; }

	public LocalDateTime getAdminBlockedDate() {return adminBlockedDate; }

	public void setAdminBlockedDate(LocalDateTime adminBlockedDate) {this.adminBlockedDate = adminBlockedDate; }

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public boolean isDataDeleted() {
		return getEmail() == null || MemberStatus.DELETED.equals(getMemberStatus()) || MemberStatus.ADMIN_DELETED.equals(getMemberStatus());
	}

	public boolean isCanceled() {
		return MemberStatus.CANCELED.equals(getMemberStatus()) || MemberStatus.REGISTRATION.equals(getMemberStatus());
	}

	public boolean isAdminCanceled() {
		return MemberStatus.ADMIN_CANCELED.equals(getMemberStatus());
	}

//	public String getRecommendationBreaks() {return this.recommendationBreaks;}


	public BlockedStatus getBlockedStatus() {
		return blockedStatus;
	}

	public void setBlockedStatus(BlockedStatus blockedStatus) {
		this.blockedStatus = blockedStatus;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
}
