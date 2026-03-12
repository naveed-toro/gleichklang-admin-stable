-- Set status CANCELED for all users without E-Mail
UPDATE user_ u SET u.member_status = 'CANCELED' WHERE u.email IS NULL;
