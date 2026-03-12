CALL DROP_COLUMN('newsletter', 'register_ip');
CALL DROP_COLUMN('newsletter', 'register_date');

UPDATE user_ u JOIN user_settings us ON us.user_id = u.id SET us.disable_news_notifications = false WHERE u.member_status = 'CANCELED';