CREATE TABLE IF NOT EXISTS `user_statistic` (
 `id` bigint(20) not null auto_increment,
 `change_date` datetime default null,
 `create_date` datetime default null,
 `start_date` datetime not null,
 `end_date` datetime default null,
 primary key (`id`)
 );
