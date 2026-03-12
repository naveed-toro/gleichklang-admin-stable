package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.service.mail.MailTemplate;


/**
 * Enumerates the user mail templates.
 */
public enum UserMailTemplate implements MailTemplate
{
	PASSWORD_RESET("password_reminder_teilnehmer.html.vm"),
	PASSWORD_RESET_NEW("password_reminder_teilnehmer.html.vm"),
	// Payments
	ADMITTANCE_1("admittance1.html.vm"),
	ADMITTANCE_2("admittance2.html.vm"),
	
	CB_ACCOUNTERROR_FIRST("cb_accounterror_first.html.vm"),
	CB_ACCOUNTERROR_RENEWAL_FIRST("cb_accounterror_renewal_first.html.vm"),
	CB_INSUFFICIENT_FIRST("cb_insufficient_first.html.vm"),
	CB_INSUFFICIENT_RENEWAL_FIRST("cb_insufficient_renewal_first.html.vm"),
	CB_INSUFFICIENT_REMINDER("cb_insufficient_reminder.html.vm"),
	CB_OTHER_FIRST("cb_other_first.html.vm"),
	CB_OTHER_RENEWAL_FIRST("cb_other_renewal_first.html.vm"),
	CB_REFUND_FIRST("cb_refund_first.html.vm"),
	CB_REFUND_NEXT("cb_refund_next.html.vm"),
	
	CB_REVOCATION_FIRST("cb_revocation_first.html.vm"),
	CB_REVOCATION_REMINDER("cb_revocation_reminder.html.vm"),
	CB_REVOCATION_RENEWAL_FIRST("cb_revocation_renewal_first.html.vm"),
	
	DONATION("donation.html.vm"),
	EXTENSION("extension.html.vm"),
	EXTENSION_PP("extension_pp.html.vm"),
	EXTENSION_DDCC("extension_ddcc.html.vm"),

	NEWS_FROM_GLEICHKLANG("news_from_gleichklang.html.vm"),
	
	MISSING_PAYMENT_REMINDER("missing_payment_reminder.html.vm"),
	MISSING_QUESTIONAIRE_REMINDER("missing_questionaire_reminder.html.vm"),
	
	OPTIMIZATION_PP("optimization_pp.html.vm"),
	OPTIMIZATION_DDCC("optimization_ddcc.html.vm"),
	OPTIMIZATION_DONE("optimization_done.html.vm"),
	OPTIMIZATION_REMINDER("optimization_reminder.html.vm"),
	
	PP_PAID_CB_NOTIF("pp_paid_cb_notif.html.vm"),
	PP_PAID_DONATION_NOTIF("pp_paid_donation_notif.html.vm"),
	PP_PAID_OPTIMIZATION_NOTIF("pp_paid_optimization_notif.html.vm"),

	PREPAYMENT_PAID_NOTFICATION("prepayment_paid_notification.html.vm"),
	PREPAYMENT_REMINDER_NEW("prepayment_reminder_new.html.vm"),
	PREPAYMENT_REMINDER_NEXT("prepayment_reminder_next.html.vm"),
	PREPAYMENT_REMINDER_RENEWAL_NEW("prepayment_reminder_renewal.html.vm"),
	PREPAYMENT_REMINDER_RENEWAL_NEXT("prepayment_reminder_renewal_next.html.vm"),
	PREPAYMENT_REMINDER_UPGRADE_NEW("prepayment_reminder_upgrade.html.vm"),
	PREPAYMENT_REMINDER_UPGRADE_NEXT("prepayment_reminder_upgrade_next.html.vm"),

	PREPAYMENT_REFUND_NOTFICATION("prepayment_refund_notification.html.vm"),
	PREPAYMENT_REFUND_REQUEST("prepayment_refund_request.html.vm"),

	REGISTRATION_USER("registration_user.html.vm"),
	
	RENEWAL_CHOSEN_REMINDER("renewal_chosen_reminder.html.vm"),
	RENEWAL_DISABLED_REMINDER("renewal_disabled_reminder.html.vm"),
	RENEWAL_DISABLED("renewal_disabled.html.vm"),
	RENEWAL_FAILED_ACTUAL("renewal_failed_actual.html.vm"),
	RENEWAL_NOTIF_DDCC("renewal_notif_ddcc.html.vm"),
	
	SIGNOFF_ACK_MSG("signoff_ack_msg.html.vm"),
	
	SOCIAL_PP_USER("social_pp_user.html.vm"),
	SOCIAL_REGISTERED_USER("social_registered_user.html.vm"),
	SOCIAL_UNREGISTERED_USER("social_unregistered_user.html.vm"), // this template isn't bound to an user!

	MAIL_CHANGE_VERIFICATION("mailchange_verification_teilnehmer.html.vm"),
	
	// User e-mails
	NEW_BOXNUMBER_CONTACT("new_boxnumber_contact.html.vm"),
	NEW_MATCH("new_match.html.vm"),
	NEW_MATCH_POSITIVE("new_match_positive.html.vm"),
	NO_MATCH_MESSAGE("no_match_message.html.vm"),
	NEW_FOOTPRINT("new_footprint.html.vm"),

	// Admin templates
	ADMIN_PASSWORD_RESET("password_reminder_admin.html.vm"),
	ADMIN_PASSWORD_RESET_REQUEST("password_reset_request.html.vm"),
	ADMIN_UNKNOWN_EXTERNAL_PAYMENT("unknown_external_payment.html.vm"),
	
	NEW_PASSWORD("new_password.html.vm"),
	USER_DELETED("user_deleted.html.vm"),
	USER_DELETED_WITH_SATISFACTION("user_deleted_satisfaction.html.vm"),
	SUBSCRIPTION_CANCELLED("subscription_cancelled.html.vm"),
	SUBSCRIPTION_CANCELLED_WITH_SATISFACTION("subscription_cancelled_satisfaction.html.vm"),
	SUBSCRIPTION_CANCELLED_USER_DELETED("subscription_cancelled_user_deleted.html.vm"),
	SUBSCRIPTION_CANCELLED_USER_DELETED_WITH_SATISFACTION("subscription_cancelled_user_deleted_satisfaction.html.vm"),
	ADMIN_MESSAGE("admin_message.html.vm"),
	ADMIN_FIRST_MESSAGE("admin_first_message.html.vm"),
	CANCELED_USER_MAIL("canceled_user_mail.html.vm"),
	SUBSCRIPTION_END_AUTORENEWAL_OFF_USER_SATISFIED("subscription_end_autorenewal_off_user_satisfied.html.vm"),
	SUBSCRIPTION_END_AUTORENEWAL_OFF_USER_UNSATISFIED("subscription_end_autorenewal_off_user_unsatisfied.html.vm");

	private final String templateName;

	UserMailTemplate(String templateName)
	{
		this.templateName = templateName;
	}




	@Override
	public String getTemplateName()
	{
		return templateName;
	}
}
