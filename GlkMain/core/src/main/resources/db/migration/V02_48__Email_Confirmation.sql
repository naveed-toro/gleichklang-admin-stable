CALL DROP_COLUMN('user_', 'confirmation_code');
CALL DROP_COLUMN('user_', 'new_mail');
CALL ADD_COLUMN('user_', 'email_confirmed', 'BIT(1) DEFAULT FALSE');

CALL CHANGE_COLUMN('user_', 'memberStatus', 'member_status', 'VARCHAR(255) NOT NULL');

CALL CHANGE_COLUMN('user_settings', 'cancellationPolicyAccepted', 'cancellation_policy_accepted','BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'communityRulesAccepted', 'community_rules_accepted', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disableAds', 'disable_ads', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disableFurtherInfo', 'disable_further_info', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'generalTermsAccepted', 'general_terms_accepted', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'mailBlocked', 'mail_blocked', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'matchmakingSuccess', 'matchmaking_success', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'privacyPolicyAccepted', 'privacy_policy_accepted', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'satisfactionGleichklang', 'satisfaction_gleichklang', 'VARCHAR(25)');
CALL CHANGE_COLUMN('user_settings', 'satisfactionOther', 'satisfaction_other', 'VARCHAR(25)');

CALL CHANGE_COLUMN('user_settings', 'disableRecommendationNotifications',
                   'disable_recommendation_notifications', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disableCipherMessageNotifications',
                   'disable_cipher_message_notifications', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disablePositiveRankingNotifications',
                   'disable_positive_ranking_notifications', 'BIT(1) DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disableInfoMails', 'disable_info_mails', 'BIT(1) DEFAULT FALSE');

CALL CHANGE_COLUMN('user_settings', 'sourcePage', 'source_page', 'VARCHAR(500)');
CALL CHANGE_COLUMN('user_settings', 'sourceText', 'source_text', 'VARCHAR(255)');
CALL CHANGE_COLUMN('user_settings', 'talkToPress', 'talk_to_press', 'BIT(1) DEFAULT FALSE');


UPDATE user_ AS u
  JOIN compuser AS cu
    ON u.legacy_id = cu.no
SET u.email_confirmed = cu.confirmed;
