package de.binaerebauten.gleichklang.core.model.user;

/**
 * There are different steps by the registration of new users. User fills
 * questionnaires and after questionnaires are finished the registration state is updated
 * {@link UserRegistrationState} describes which states the user has already fulfilled
 *
 * INITIATED - user completed preregistration process
 * CATEGORY - user fills some CATEGORY questionnaire (a reference to a questionnaire is referenced in {@link UserRegistrationState}
 * PERSONAL - user fills some PERSONAL questionnaire
 * MASTER - user fills his master data
 * PAYMENT - user fills payment information
 */
public enum RegistrationState
{
	INITIATED,
	CATEGORY,
	PERSONAL,
	PAYMENT,
	PARTNERSHIPPOST,
	FRIENDSHIPPOST,
	PERSONALPOST,
	MEMBERADDRESS,
	SUBSCRIPTIONOFFER,
	PAYMENTRESULT,
	CONFIGNOTIFICATION,
	LASTSTEP
}
