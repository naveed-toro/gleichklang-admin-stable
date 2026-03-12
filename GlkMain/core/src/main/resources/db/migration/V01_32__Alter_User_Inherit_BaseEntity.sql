
ALTER TABLE after_cancel
	DROP FOREIGN KEY after_cancel_ibfk_1,
	CHANGE user_id user_legacy_id VARCHAR(255) NOT NULL,
	ADD user_id BIGINT;

ALTER TABLE cancel_reason
	DROP FOREIGN KEY cancel_reason_ibfk_1,
	CHANGE user_id user_legacy_id VARCHAR(255) NOT NULL,
	ADD user_id BIGINT;

ALTER TABLE subscription
	DROP FOREIGN KEY subscription_ibfk_1,
	CHANGE user_id user_legacy_id VARCHAR(255) NOT NULL,
	ADD user_id BIGINT;


UPDATE after_cancel dest INNER JOIN user_ src ON src.legacy_id = dest.user_legacy_id SET dest.user_id = src.id;
UPDATE cancel_reason dest INNER JOIN user_ src ON src.legacy_id = dest.user_legacy_id SET dest.user_id = src.id;

ALTER TABLE after_cancel
	ADD FOREIGN KEY (user_id) REFERENCES user_ (id),
	DROP COLUMN user_legacy_id,
	MODIFY user_id BIGINT NOT NULL;

ALTER TABLE cancel_reason
	ADD FOREIGN KEY (user_id) REFERENCES user_ (id),
	DROP COLUMN user_legacy_id,
	MODIFY user_id BIGINT NOT NULL;

ALTER TABLE subscription
	ADD FOREIGN KEY (user_id) REFERENCES user_ (id),
	DROP COLUMN user_legacy_id,
	MODIFY user_id BIGINT NOT NULL;

ALTER TABLE user_roles
CHANGE user_id user_settings_legacy_id VARCHAR(255) NOT NULL,
ADD user_settings_id BIGINT;

UPDATE user_roles dest INNER JOIN user_settings src ON src.legacy_id = dest.user_settings_legacy_id SET dest.user_settings_id = src.id;

ALTER TABLE user_roles
ADD FOREIGN KEY (user_settings_id) REFERENCES user_settings (id),
DROP COLUMN user_settings_legacy_id,
MODIFY user_settings_id BIGINT NOT NULL;