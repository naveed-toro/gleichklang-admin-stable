CALL DROP_COLUMN('user_settings', 'mail_blocked');
UPDATE user_ u JOIN compuser cu ON cu.no = u.legacy_id SET u.email_confirmed = (cu.mailblocked <> '1' OR cu.mailblocked IS NULL);

CALL CHANGE_COLUMN('user_settings', 'disable_info_mails', 'disable_news_notifications', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disable_footprint_notifications', 'disable_footprint_notifications', 'BIT NOT NULL  DEFAULT FALSE AFTER disable_news_notifications');
CALL CHANGE_COLUMN('user_settings', 'disable_recommendation_notifications', 'disable_recommendation_notifications', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disable_cipher_message_notifications', 'disable_cipher_message_notifications', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disable_positive_ranking_notifications', 'disable_positive_ranking_notifications', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'privacy_policy_accepted', 'privacy_policy_accepted', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'general_terms_accepted', 'general_terms_accepted', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'community_rules_accepted', 'community_rules_accepted', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'cancellation_policy_accepted', 'cancellation_policy_accepted', 'BIT NOT NULL DEFAULT FALSE');
CALL CHANGE_COLUMN('user_settings', 'disable_ads', 'disable_ads', 'BIT NOT NULL DEFAULT FALSE');

UPDATE user_settings SET disable_footprint_notifications = true WHERE
    disable_recommendation_notifications AND
    disable_cipher_message_notifications AND
    disable_positive_ranking_notifications AND
    disable_news_notifications;