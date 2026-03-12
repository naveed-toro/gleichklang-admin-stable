UPDATE user_ SET member_status = 'DELETED' WHERE email IS NULL;
UPDATE user_ JOIN subscription ON user_id = user_.id SET member_status = 'CANCELED' WHERE member_status = 'REGISTRATION';