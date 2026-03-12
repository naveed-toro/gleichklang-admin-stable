CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  `change_date` datetime default null,
  `create_date` datetime default null,
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user_` (`id`)
);

Delete from roles;

insert into roles (name,change_date,create_date) values
('ROLE_USER',NULL,NULL),
('ROLE_ADMIN',NULL,NULL);