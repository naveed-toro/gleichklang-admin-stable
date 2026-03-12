UPDATE user_ SET member_status = 'REGISTERED' WHERE member_status = 'ACTIVE';
UPDATE user_ SET member_status = 'REGISTRATION' WHERE member_status IN ('INITIATED', 'PENDING');
UPDATE user_ SET member_status = 'CANCELED' WHERE member_status = 'EXMEMBER';

DROP TABLE IF EXISTS user_registration_state;
CREATE TABLE user_registration_state
(
  `id` bigint(20) AUTO_INCREMENT PRIMARY KEY,
  `legacy_id` varchar(255) DEFAULT NULL,
  `change_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_date` datetime DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  `registration_state` varchar(255) NOT NULL,
  `recommendation_category` varchar(255) DEFAULT NULL,
  CONSTRAINT `user_registration_state_user` FOREIGN KEY (`user_id`) REFERENCES `user_` (`id`)
);

CALL CREATE_UNIQUE_INDEX('user_registration_user', 'user_registration_state', 'user_id');
