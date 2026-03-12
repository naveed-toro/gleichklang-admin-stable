CALL DROP_COLUMN('user_settings', 'disable_further_info');

UPDATE user_settings us JOIN user_ u ON u.id = us.user_id JOIN compuser cu ON cu.no = u.legacy_id SET us.disable_news_notifications = true WHERE
    cu.no_further_infos = '1' OR
    cu.supress_infomails = '1';

UPDATE user_settings SET disable_footprint_notifications = true WHERE
    disable_recommendation_notifications AND
    disable_cipher_message_notifications AND
    disable_positive_ranking_notifications AND
    disable_news_notifications;