create table `activator` (
`id` bigint not null auto_increment,
`dtype` varchar(31) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`activating_question_id` bigint not null,
`enables_questionnaire_id` bigint default null,
`enables_question_id` bigint default null,
`enables_question_group_id` bigint default null,
`natural_key` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `activator_natural_key` (`natural_key`));

create table `activator_choice` (
`activator_id` bigint not null,
`choice_id` bigint not null);

create table `active_inactive` (
`id` bigint not null auto_increment,
`subscription_id` bigint default null,
`type` varchar(255) default null,
`user_id` bigint default null,
`alias` varchar(255) default null,
`user_type` varchar(255) default null,
`create_date` datetime default null,
`change_date` datetime default null,
primary key (`id`));

create table `address` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`city` varchar(255) character set utf8mb3 default null,
`streetwithnumber` varchar(255) character set utf8mb3 default null,
`user_id` bigint not null,
`continent_id` bigint not null,
`country_id` bigint not null,
`zip_id` bigint default null,
`region_id` bigint default null,
`tmp_zip` varchar(255) character set utf8mb3 default null,
`payment` bit(1) default null,
`checked` bit(1) default null,
primary key (`id`),
unique key `address_payment_idx` (`user_id`,`payment`));

create table `admin_reminder` (
`id` bigint not null auto_increment,
`due_date` datetime default null,
`create_date` datetime default null,
`change_date` datetime default null,
`admin_id` bigint default null,
`reminder_title` varchar(255) character set utf8mb3 not null,
`reminder_text` varchar(255) character set utf8mb3 not null,
`reminder_status` varchar(50) character set utf8mb3 default 'new',
`reminder_recurrence` varchar(50) character set utf8mb3 default 'everyone',
`user_id` bigint default null,
primary key (`id`));

create table `admin_roles` (
`admin_id` bigint not null,
`roles` varchar(225) character set utf8mb3 default null);

create table `admin_user_login` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`admin_id` bigint not null,
`password` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `user_id` (`user_id`),
unique key `admin_id` (`admin_id`));

create table `admin_work_item` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`admin_id` bigint default null,
`message_id` bigint not null,
`work_item_status` varchar(255) character set utf8mb3 not null,
primary key (`id`));

create table `affiliate_payment_state` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`affiliate_partner` varchar(255) character set utf8mb3 not null,
`external_id` varchar(255) character set utf8mb3 not null,
`paid_to_partner` bit(1) not null default 0,
`payment_id` bigint not null,
primary key (`id`));

create table `affinity_question` (
`questions_mapping_id` bigint not null,
`question_id` bigint not null,
`affinity_mapping_id` bigint not null,
primary key (`questions_mapping_id`,`question_id`));

create table `after_cancel` (
`aftercancel` varchar(255) character set utf8mb3 default null,
`user_id` bigint not null);

create table `answer` (
`id` bigint not null auto_increment,
`dtype` varchar(31) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`number_value` int default null,
`text_value` clob character set utf8mb4,
`question_id` bigint not null,
`user_id` bigint not null,
`search_relocatable` bit(1) default 0,
`relocatable` bit(1) default 0,
`relationship_visible` bit(1) default 1,
primary key (`id`),
unique key `answer_question_user_idx` (`question_id`,`user_id`));

create table `area_match_statistic_entry` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`log_match_area` varchar(255) character set utf8mb3 not null,
`duration` bigint not null,
`number` bigint not null,
`match_statistic_id` bigint not null,
primary key (`id`));

create table `audio` (
`id` bigint not null auto_increment,
`create_date` datetime default null,
`change_date` datetime default current_timestamp on update current_timestamp,
`deleted` bit(1) default null,
`author_id` bigint default null,
`name` varchar(255) character set utf8mb3 not null,
`path` varchar(500) character set utf8mb3 default null,
`for_partnership` tinyint(1) default '0',
`for_friendship` tinyint(1) default '0',
primary key (`id`));

create table `avatar` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`file_id` bigint not null,
`category` varchar(255) character set utf8mb3 not null,
`thumbnail_id` bigint default null,
`mediafile_id` bigint default null,
primary key (`id`),
unique key `avatar_user_id_category` (`user_id`,`category`));

create table `bank_account` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`holder` varchar(255) character set utf8mb3 not null,
`bank_name` varchar(255) character set utf8mb3 not null,
`account_number` varchar(255) character set utf8mb3 not null,
`bank_number` varchar(255) character set utf8mb3 not null,
`iban` varchar(255) character set utf8mb3 not null,
`bic` varchar(255) character set utf8mb3 not null,
`country_id` bigint not null,
`active` bit(1) default null,
primary key (`id`),
unique key `bank_account_country_id` (`active`,`country_id`));

create table `blog` (
`id` bigint not null auto_increment,
`title` varchar(80) default null,
`body` clob,
`link` varchar(80) default null,
`imageurl` varchar(150) default null,
`userid` bigint default null,
`language` varchar(20) default null,
`image_url` varchar(255) default null,
`user_id` bigint default null,
primary key (`id`));

create table `cache` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`dtype` varchar(255) character set utf8mb3 default null,
`cache_key` varchar(255) character set utf8mb3 not null,
`cache_group` varchar(255) character set utf8mb3 default null,
`number_value` double default null,
`text_value` varchar(255) character set utf8mb3 default null,
primary key (`id`),
unique key `cache_key_group` (`cache_key`,`cache_group`));

create table `cancel_reason` (
`cancelreasons` varchar(255) character set utf8mb3 default null,
`user_id` bigint not null,
`cancel_reasons` varchar(255) character set utf8mb3 default null);

create table `chat_attachment` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`file_id` bigint not null,
`message_id` bigint not null,
primary key (`id`));

create table `chat_messages` (
`id` bigint not null auto_increment,
`create_date` datetime not null,
`change_date` datetime default current_timestamp on update current_timestamp,
`conversation_id` bigint default null,
`message_body` longtext character set utf8mb4,
`sent` bit(1) not null default 0,
`read_` bit(1) not null default 0,
`deleted` bit(1) not null default 0,
`source` varchar(35) character set utf8mb4 default null,
`sender_id` bigint not null,
`receiver_id` bigint not null,
`is_draft` tinyint(1) not null default '0',
`send_date` datetime default null,
`sender_hidden` tinyint(1) not null default '0',
`receiver_hidden` tinyint(1) not null default '0',
`sender_deleted` tinyint(1) not null default '0',
`receiver_deleted` tinyint(1) not null default '0',
primary key (`id`));

create table `choice` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`sort_order` int not null,
`i18n_key` varchar(255) character set utf8mb3 default null,
`choice_group_id` bigint not null,
primary key (`id`),
unique key `choice_sort_order_choice_group` (`sort_order`,`choice_group_id`));

create table `choice_answer` (
`answer_id` bigint not null,
`choice_id` bigint not null,
primary key (`answer_id`,`choice_id`));

create table `choice_group` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`i18n_key` varchar(255) character set utf8mb3 not null,
`deleted` bit(1) not null default 0,
primary key (`id`),
unique key `choice_group_name_idx` (`i18n_key`));

create table `client_information` (
`id` bigint not null auto_increment,
`change_date` datetime default current_timestamp on update current_timestamp,
`create_date` datetime default null,
`user_id` bigint not null,
`browser` varchar(55) character set utf8mb3 not null,
`browser_major_version` int not null,
`browser_minor_version` int not null,
`browser_outdated` bit(1) default null,
`browser_height` int default null,
`browser_width` int default null,
`country` varchar(255) character set utf8mb3 default null,
`touch_device` bit(1) default null,
`language` varchar(255) character set utf8mb3 default null,
`layout` varchar(255) character set utf8mb3 default null,
`screen_height` int default null,
`screen_width` int default null,
`timezone_offset` int default null,
`os` varchar(55) character set utf8mb3 not null,
`last_login` bit(1) default 0,
`usr_login_data` varchar(250) character set utf8mb3 default null,
primary key (`id`));

create table `conversations` (
`id` bigint not null auto_increment,
`create_date` datetime not null default current_timestamp,
`change_date` datetime default current_timestamp on update current_timestamp,
`initiator` bigint not null,
`responder` bigint not null,
`latest_message_date` datetime default null,
`last_notification_initiator` datetime default null,
`last_notification_responder` datetime default null,
primary key (`id`));

create table `conversations_contact_lists` (
`id` bigint not null auto_increment,
`create_date` datetime not null default current_timestamp,
`change_date` timestamp null default current_timestamp on update current_timestamp,
`conversation_id` bigint not null,
`nature_initiated` bit(1) default 0,
`project_initiated` bit(1) default 0,
`beruf_initiated` bit(1) default 0,
`treffen_initiated` bit(1) default 0,
primary key (`id`));

create table `email_domain_mapping` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` timestamp not null default current_timestamp,
`mapping_name` varchar(100) character set utf8mb3 default null,
`deleted` bit(1) default 0,
`active` bit(1) default 1,
`mapping_value` varchar(500) character set utf8mb3 default null,
primary key (`id`));

create table `email_link` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`unique_token` varchar(255) character set utf8mb3 not null,
`context` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `unique_token` (`unique_token`));

create table `email_link_parameter` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`email_link_id` bigint not null,
`parameter_type` varchar(255) character set utf8mb3 not null,
`parameter_value` varchar(255) character set utf8mb3 not null,
primary key (`id`));

create table `email_template_mapping` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` timestamp not null default current_timestamp,
`template_name` varchar(100) character set utf8mb3 default null,
`template_description` varchar(100) character set utf8mb3 default null,
`template_language` varchar(50) character set utf8mb3 default null,
`template_footer` bigint default null,
`deleted` bit(1) default 0,
`active` bit(1) default 1,
`template_text` clob character set utf8mb3,
primary key (`id`));

create table `envelope` (
`id` bigint not null auto_increment,
`dtype` varchar(31) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint default null,
`hidden` bit(1) not null,
`read_` bit(1) default null,
`message_id` bigint not null,
`deleted` bit(1) not null default 0,
primary key (`id`));

create table `external_payment_registration` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`version` bigint not null default '0',
`last_used_date` datetime not null,
`registration_id` varchar(255) character set utf8mb3 default null,
`user_id` bigint not null,
`external_reference_id` varchar(255) character set utf8mb3 default null,
`payment_type_id` varchar(255) character set utf8mb3 default null,
`active` tinyint default null,
primary key (`id`),
unique key `external_payment_registration_external_reference_id_idx` (`external_reference_id`));

create table `file` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`name` varchar(255) character set utf8mb3 default null,
`relationshipcategory` varchar(255) character set utf8mb3 default null,
`type` varchar(5) character set utf8mb3 default null,
`description` varchar(255) character set utf8mb3 default null,
primary key (`id`));

create table `filter` (
`id` bigint not null auto_increment,
`dtype` varchar(255) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`left_filter_id` bigint default null,
`right_filter_id` bigint default null,
`binary_operator` varchar(255) character set utf8mb3 default null,
`single_filter_id` bigint default null,
`unary_operator` varchar(255) character set utf8mb3 default null,
`template_filter_id` bigint default null,
`template_name` varchar(255) character set utf8mb3 default null,
`enum_value` varchar(255) character set utf8mb3 default null,
`choice_question_id` bigint default null,
`choice_id` bigint default null,
`radius` int default null,
`zip_id` bigint default null,
`locatable_id` bigint default null,
`deleted` bit(1) default null,
`string_value` varchar(255) character set utf8mb3 default null,
`boolean_value` bit(1) default null,
`user_activity` varchar(255) character set utf8mb3 default null,
`from_date` date default null,
`to_date` date default null,
`activity_category` varchar(255) character set utf8mb3 default null,
`text_value` varchar(255) character set utf8mb3 default null,
`text_question_id` bigint default null,
`number_question_id` bigint default null,
`min_value` int default null,
`max_value` int default null,
`min_age_value` int default null,
`max_age_value` int default null,
`relationship_from` datetime default null,
`relationship_to` datetime default null,
`threshold` int default null,
`direction` varchar(255) character set utf8mb3 default null,
`directory` varchar(255) character set utf8mb3 default null,
`category` varchar(255) character set utf8mb3 default null,
`number_value` bigint default null,
`number_value1` bigint default null,
primary key (`id`));

create table `heidelpay_registration` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`unique_id` varchar(255) character set utf8mb3 not null,
`transaction_id` varchar(255) character set utf8mb3 default null,
`active` bit(1) not null,
`email` varchar(255) character set utf8mb3 default null,
`first_name` varchar(255) character set utf8mb3 default null,
`last_name` varchar(255) character set utf8mb3 default null,
`heidelpay_date` datetime default null,
primary key (`id`));

create table `heidelpay_transaction` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`payment_id` bigint not null,
`return_code` varchar(255) character set utf8mb3 not null,
`return_message` varchar(255) character set utf8mb3 not null,
`result` varchar(255) character set utf8mb3 not null,
primary key (`id`));

create table `hibernate_sequence` (
`next_val` bigint default null);

create table `i18n` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`language` varchar(255) character set utf8mb3 default null,
`i18n_key` varchar(255) character set utf8mb3 not null,
`i18n_value` clob character set utf8mb3 not null,
`base_name` varchar(255) character set utf8mb3 not null default 'none',
primary key (`id`),
unique key `i18n_language_i18n_key_base_name` (`language`,`i18n_key`,`base_name`));

create table `invoice` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`version` bigint not null default '0',
primary key (`id`));

create table `invoice_item` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`invoice_id` bigint not null,
`product_id` bigint not null,
`subscription_id` bigint default null,
`amount` decimal(8,2) not null,
`currency` varchar(3) character set utf8mb3 not null,
`version` bigint not null default '0',
`chargeback_reason` varchar(255) character set utf8mb3 default null,
primary key (`id`));

create table `js_include_postfix` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`key_name` varchar(12) character set utf8mb3 not null,
primary key (`id`));

create table `jwt_token` (
`id` bigint not null auto_increment,
`create_date` datetime default null,
`change_date` datetime default null,
`user_id` bigint default null,
`token` varchar(500) default null,
`login_name` varchar(255) default null,
primary key (`id`));

create table `locatable` (
`id` bigint not null auto_increment,
`dtype` varchar(255) character set utf8mb3 default null,
`change_date` datetime default null,
`create_date` datetime default null,
`i18n_key` varchar(255) character set utf8mb3 default null,
`zip` varchar(25) character set utf8mb3 default null,
`latitude` double default null,
`longitude` double default null,
`parent_id` bigint default null,
`region_name` varchar(255) character set utf8mb3 default null,
`region_id` bigint default null,
`sort_order` int default null,
primary key (`id`),
unique key `locatable_sort_order_dtype` (`dtype`,`sort_order`));

create table `mail_queue_entry` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`recipient_email` varchar(255) character set utf8mb3 default null,
`mail_template` varchar(255) character set utf8mb3 not null,
`attempts` int not null,
`delivery_status` varchar(255) character set utf8mb3 not null,
`undeliverable_mail_reason` varchar(255) character set utf8mb3 not null,
`next_reminder_date` datetime default null,
`dtype` varchar(31) character set utf8mb3 not null default 'unregisteredusermailqueueentry',
`recipient_id` bigint default null,
`reminder_count` int default '0',
`next_retry_date` datetime default null,
`sender_id` bigint default null,
primary key (`id`));

create table `match_` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`source_user_id` bigint not null,
`target_user_id` bigint not null,
`count` int not null,
`category` varchar(255) character set utf8mb3 not null,
`strictness` int not null,
primary key (`id`),
unique key `source_user_id_target_user_id_category` (`source_user_id`,`target_user_id`,`category`));

create table `match_statistic` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`matching_scope` varchar(255) character set utf8mb3 not null,
`start_date` datetime not null,
`end_date` datetime default null,
primary key (`id`));

create table `match_statistic_entry` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`log_area` varchar(255) character set utf8mb3 not null,
`duration` bigint not null,
`number` bigint not null,
`match_statistic_id` bigint not null,
primary key (`id`));

create table `matching_matrix` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`name` varchar(31) character set utf8mb3 not null,
`source_choice_group_id` bigint not null,
`target_choice_group_id` bigint not null,
primary key (`id`),
unique key `matching_matrix_name` (`name`));

create table `matching_matrix_value` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`matrix_id` bigint not null,
`source_choice_id` bigint not null,
`target_choice_id` bigint not null,
`strictness` int not null,
primary key (`id`),
unique key `unique_index` (`matrix_id`,`source_choice_id`,`target_choice_id`));

create table `media` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`file_id` bigint not null,
`media_gallery_id` bigint not null,
`deleted` bit(1) not null,
primary key (`id`));

create table `media_gallery` (
`id` bigint not null auto_increment,
`change_date` datetime default current_timestamp on update current_timestamp,
`create_date` datetime default null,
`name` varchar(255) character set utf8mb3 default null,
`author_id` bigint not null,
`visible_category` varchar(255) character set utf8mb3 default null,
`visible_affiliation` varchar(255) character set utf8mb3 default null,
`deleted` bit(1) not null,
`secret` bit(1) not null,
`avatar_gallery` bit(1) default null,
primary key (`id`));

create table `media_gallery_invisible_relationship` (
`media_gallery_id` bigint not null,
`relationship_id` bigint not null);

create table `media_gallery_visible_relationship` (
`media_gallery_id` bigint not null,
`relationship_id` bigint not null);

create table `mediagallery_category` (
`id` bigint not null auto_increment,
`mediagallery_id` bigint not null,
`partnership_category` bit(1) not null default 0,
`friendship_category` bit(1) not null default 0,
`partnership_affiliation` varchar(45) character set utf8mb3 default null,
`friendship_affiliation` varchar(45) character set utf8mb3 default null,
`suggestions` bit(1) default 0,
`create_date` datetime default null,
`change_date` datetime default null,
primary key (`id`));

create table `member_statistics` (
`id` bigint not null auto_increment,
`create_date` timestamp not null default current_timestamp,
`change_date` datetime default null,
`user_id` bigint not null,
`total_relationships` mediumint default null,
`viewed_relationships` mediumint default null,
`received_suggestion_f` mediumint default null,
`received_suggestion_p` mediumint default null,
`received_message_f` mediumint default null,
`received_message_p` mediumint default null,
`received_message_cl` mediumint default null,
`sent_message_f` mediumint default null,
`sent_message_p` mediumint default null,
`sent_message_cl` mediumint default null,
`total_conversations` mediumint default null,
`convo_first_wrote` mediumint default null,
`convo_first_answered` mediumint default null comment 'p - partnership\nf - friendship\ncl - contact list',
primary key (`id`),
unique key `user_id_unique` (`user_id`));

create table `message` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`subject` varchar(255) character set utf8mb4 not null,
`body` longtext character set utf8mb4,
`sent` bit(1) not null,
`send_date` datetime default null,
`reply_to_message_id` bigint default null,
`deleted` bit(1) not null default 0,
`message_type` varchar(255) character set utf8mb4 not null,
`status` varchar(50) character set utf8mb4 default null,
`reply_userid` bigint default null,
`from_contact_list` varchar(35) character set utf8mb4 default null,
primary key (`id`));

create table `message_attachment` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`file_id` bigint not null,
`message_id` bigint not null,
primary key (`id`));

create table `news` (
`id` bigint not null auto_increment,
`title` varchar(255) character set utf8mb3 not null,
`teaser_text` clob character set utf8mb3 not null,
`text` clob character set utf8mb3 not null,
`valid_from` datetime not null,
`valid_to` datetime not null,
`active` bit(1) not null,
`change_date` datetime default null,
`create_date` datetime default null,
`language` varchar(255) character set utf8mb3 not null,
`filter_id` bigint default null,
`email_notification` bit(1) default 0,
`email_send_date` datetime default null,
`admin_id` bigint default null,
primary key (`id`));

create table `newsletter` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`email` varchar(255) character set utf8mb3 not null,
`confirmation_ip` varchar(255) character set utf8mb3 default null,
`confirmation_date` datetime default null,
primary key (`id`),
unique key `email` (`email`));

create table `notifications` (
`id` bigint not null auto_increment,
`payload` varchar(2000) not null,
`headers` varchar(2000) default null,
`received_at` varchar(45) default 'now()',
`ip_address` varchar(45) default null,
`remote_host_name` varchar(45) default null,
`processed` tinyint default '0',
`endpoint` varchar(200) not null,
primary key (`id`));

create table `payment` (
`id` bigint not null auto_increment,
`change_date` datetime default current_timestamp on update current_timestamp,
`create_date` datetime default null,
`external_id` varchar(255) character set utf8mb3 default null,
`method` varchar(255) character set utf8mb3 not null,
`amount` decimal(10,2) not null,
`currency` varchar(255) character set utf8mb3 not null,
`dtype` varchar(31) character set utf8mb3 not null,
`state` varchar(31) character set utf8mb3 not null,
`user_id` bigint not null,
`bank_account_id` bigint default null,
`invoice_id` bigint not null,
`version` bigint not null default '0',
`synchronization_count` int not null default '0',
`reminder_count` int default '0',
`next_reminder_date` datetime default null,
`current` bit(1) default null,
`external_reference_id` varchar(255) character set utf8mb3 default null,
`refunded` bit(1) not null default 0,
`payment_to_refund_id` bigint default null,
`comment` varchar(255) character set utf8mb3 default null,
`revocation_date` date default null,
`revocation_amount` decimal(10,2) default null,
`charge_id` varchar(255) character set utf8mb3 default null,
`payment_id` varchar(255) character set utf8mb3 default null,
`auth_id` varchar(255) character set utf8mb3 default null,
primary key (`id`),
unique key `payment_user_id_current` (`user_id`,`current`),
unique key `payment_external_reference_id_idx` (`external_reference_id`),
unique key `payment_external_id` (`external_id`));

create table `paypal_token` (
`id` bigint not null auto_increment,
`create_date` datetime default null,
`change_date` datetime default null,
`token` varchar(500) default null,
`user_id` bigint default null,
primary key (`id`),
unique key `user_id_unique` (`user_id`));

create table `product` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`dtype` varchar(31) character set utf8mb3 not null,
`name` varchar(255) character set utf8mb3 not null,
`i18n_key` varchar(255) character set utf8mb3 default null,
`amount` decimal(5,2) not null,
`currency` varchar(3) character set utf8mb3 not null,
`begin` datetime not null,
`end` datetime default null,
`for_method` varchar(255) character set utf8mb3 default null,
`action_code` varchar(255) character set utf8mb3 default null,
`duration` int default null,
`duration_unit` varchar(255) character set utf8mb3 default null,
`tariff` varchar(255) character set utf8mb3 default null,
`auto_renewal_offer_id` bigint default null,
`upgrade_type` varchar(255) character set utf8mb3 default null,
`additional` bit(1) default 0,
primary key (`id`));

create table `proximity_search_request` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`answer_id` bigint not null,
`center_zip_id` bigint not null,
`distance` int not null,
`restrict_country` bit(1) not null,
primary key (`id`));

create table `query_performance` (
`version` varchar(255) character set utf8mb3 default null,
`type` varchar(255) character set utf8mb3 default null,
`rows` int default null,
`message` varchar(255) character set utf8mb3 default null,
`change_date` datetime default null,
`time_diff` int default null,
`object_id` int default null);

create table `question` (
`id` bigint not null auto_increment,
`dtype` varchar(31) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`label` varchar(255) character set utf8mb3 default null,
`selection_type` varchar(25) character set utf8mb3 default null,
`max_length` int default null,
`number_of_lines` int default null,
`max_value` int default null,
`min_value` int default null,
`i18n_key` varchar(255) character set utf8mb3 not null,
`choice_group_id` bigint default null,
`question_group_id` bigint default null,
`deleted` bit(1) not null default 0,
`sort_order` int not null,
`representation_type` varchar(25) character set utf8mb3 not null default 'default',
`adjustable_relationship_visibility` bit(1) not null,
`only_admin_visible` bit(1) not null,
`requirement` varchar(255) character set utf8mb3 not null,
`with_relocation` bit(1) default null,
`default_choice_id` bigint default null,
primary key (`id`),
unique key `question_i18n_key` (`i18n_key`),
unique key `question_sort_order_question_group` (`sort_order`,`question_group_id`));

create table `question_group` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`i18n_key` varchar(255) character set utf8mb3 not null,
`questionnaire_id` bigint default null,
`deleted` bit(1) not null default 0,
`sort_order` int not null,
primary key (`id`),
unique key `question_group_i18n_key_questionnaire` (`i18n_key`,`questionnaire_id`),
unique key `question_group_sort_order_questionnaire` (`sort_order`,`questionnaire_id`));

create table `questionnaire` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`i18n_key` varchar(255) character set utf8mb3 not null,
`recommendation_category` varchar(255) character set utf8mb3 default null,
`deleted` bit(1) not null default 0,
`sort_order` int not null,
`icon` varchar(255) character set utf8mb3 default null,
primary key (`id`),
unique key `questionnaire_i18n_key` (`i18n_key`),
unique key `questionnaire_sort_order` (`sort_order`));

create table `questions_mapping` (
`id` bigint not null auto_increment,
`dtype` varchar(31) character set utf8mb3 not null,
`change_date` datetime default null,
`create_date` datetime default null,
`source_question_id` bigint default null,
`target_question_id` bigint default null,
`fact_question_id` bigint default null,
`max_question_id` bigint default null,
`min_question_id` bigint default null,
`max_distance` int default null,
`matrix_id` bigint default null,
`max_age_question_id` bigint default null,
`min_age_question_id` bigint default null,
`avatar_question_id` bigint default null,
`true_choice_id` bigint default null,
`natural_key` varchar(255) character set utf8mb3 not null,
`default_empty_strictness` int not null,
primary key (`id`),
unique key `questions_mapping_natural_key` (`natural_key`));

create table `recommendation_break` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`category` varchar(255) character set utf8mb3 not null,
`end_date` date default null,
primary key (`id`),
unique key `user_id_category` (`user_id`,`category`));

create table `region_search_request` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`answer_id` bigint not null,
`continent_id` bigint default null,
`country_id` bigint default null,
primary key (`id`));

create table `region_search_request_restriction` (
`region_search_request_id` bigint not null,
`locatable_id` bigint not null,
primary key (`region_search_request_id`,`locatable_id`));

create table `relationship` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`source_user_id` bigint not null,
`target_user_id` bigint not null,
`affiliation` varchar(255) character set utf8mb3 not null,
`viewed` bit(1) not null,
`deleted` bit(1) not null,
`memo` clob character set utf8mb3,
`footprint` varchar(255) character set utf8mb3 default null,
`footprint_viewed` bit(1) default null,
`footprint_date` datetime default null,
`notified` bit(1) default null,
`footprint_notified` bit(1) default 1,
`last_viewed_date` datetime default null,
`first_viewed` datetime default null,
`delete_date` datetime default null,
`deleted_by` varchar(255) character set utf8mb3 default null,
`is_direct_contact` bit(1) default 0,
`memocorona` varchar(100) character set utf8mb3 default null,
`memonature` varchar(100) character set utf8mb3 default null,
`memoproject` varchar(100) character set utf8mb3 default null,
`is_target_temp_blocked` bit(1) not null default 0,
`kontakt_list` varchar(35) character set utf8mb3 default null,
`memo_nature` clob character set utf8mb3,
`is_active` tinyint not null default '1',
primary key (`id`),
unique key `source_user_id_target_user_id` (`source_user_id`,`target_user_id`));

create table `relationship_category` (
`category` varchar(255) character set utf8mb3 not null,
`relationship_id` bigint not null,
primary key (`category`,`relationship_id`));

create table `roles` (
`id` bigint not null auto_increment,
`name` varchar(20) character set utf8mb3 default null,
`change_date` datetime default null,
`create_date` datetime default null,
primary key (`id`),
unique key `uk_nb4h0p6txrmfc0xbrd1kglp9t` (`name`));

create table `scamming` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
primary key (`id`),
unique key `scamming_user_id` (`user_id`));

create table `schema_version` (
`installed_rank` int not null,
`version` varchar(50) default null,
`description` varchar(200) not null,
`type` varchar(20) not null,
`script` varchar(1000) not null,
`checksum` int default null,
`installed_by` varchar(100) not null,
`installed_on` timestamp not null default current_timestamp,
`execution_time` int not null,
`success` tinyint(1) not null,
primary key (`installed_rank`));

create table `service_offer_category` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`service_offer_id` bigint not null,
`category` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `service_offer_category_service_offer_id_category` (`service_offer_id`,`category`));

create table `stat_history` (
`table_name` varchar(255) default null,
`table_rows` int default null,
`change_date` datetime default null);

create table `subscription` (
`id` bigint not null auto_increment,
`change_date` datetime default current_timestamp on update current_timestamp,
`create_date` datetime default null,
`user_id` bigint not null,
`begin` datetime default null,
`end` datetime default null,
`offer_id` bigint not null,
`automatic_renewal` bit(1) not null,
`version` bigint not null default '0',
`current` bit(1) default null,
`expiration_date` datetime default null,
`state` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `subscription_user_id_active` (`user_id`,`current`));

create table `subscription_offer_category` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`subscription_offer_id` bigint not null,
`category` varchar(255) character set utf8mb3 not null,
primary key (`id`),
unique key `subscription_offer_category_subscription_offer_id_category` (`subscription_offer_id`,`category`));

create table `template_context` (
`template_context` varchar(255) character set utf8mb3 not null,
`template_filter_id` bigint not null,
primary key (`template_context`,`template_filter_id`));

create table `undeliverable_mail` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`undeliverable_mail_reason` varchar(255) character set utf8mb3 not null,
`recipient_email` varchar(255) character set utf8mb3 not null,
`incidents` int not null default '1',
primary key (`id`),
unique key `undeliverable_mail_recipient_email_undeliverable_mail_reason` (`recipient_email`,`undeliverable_mail_reason`));

create table `unzer_charge` (
`id` bigint not null auto_increment,
`create_date` datetime not null,
`change_date` datetime not null default current_timestamp,
`charge_id` varchar(45) not null,
`amount` decimal(10,2) default null,
`currency` varchar(10) default null,
`type_id` varchar(45) not null,
`customer_id` varchar(45) default null,
`unzer_payment_id` varchar(45) not null,
`unzer_trace_id` varchar(45) not null,
`unzer_message_code` varchar(45) not null,
`payment_id` bigint not null,
`unzer_unique_id` varchar(255) default null,
`unzer_short_id` varchar(255) default null,
primary key (`id`));

create table `unzer_customer` (
`id` bigint not null auto_increment,
`create_date` datetime not null,
`change_date` datetime not null default current_timestamp,
`user_id` bigint not null,
`customer_id` varchar(20) not null,
`first_name` varchar(50) default null,
`last_name` varchar(50) default null,
`active` tinyint default null,
primary key (`id`));

create table `upgrade_offer_subscription_offer` (
`upgrade_offer_id` bigint not null,
`subscription_offer_id` bigint not null);

create table `user_` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`first_name` varchar(255) character set utf8mb3 default null,
`last_name` varchar(255) character set utf8mb3 default null,
`alias` varchar(255) character set utf8mb3 not null,
`old_alias` varchar(255) character set utf8mb3 default null,
`alias_marked_old` bit(1) not null default 0,
`email` varchar(255) character set utf8mb3 default null,
`birth_date` date default null,
`member_status` varchar(255) character set utf8mb3 default null,
`password_old` varchar(35) character set utf8mb3 default null,
`status_message` varchar(255) character set utf8mb3 default null,
`password` varchar(255) character set utf8mb3 default null,
`reset_password` bit(1) default 0,
`email_confirmed` bit(1) default 0,
`register_ip` varchar(255) character set utf8mb3 default null,
`dtype` varchar(31) character set utf8mb3 default null,
`deleted` bit(1) default 0,
`new_email` varchar(255) character set utf8mb3 default null,
`confirmation_ip` varchar(255) character set utf8mb3 default null,
`confirmation_date` datetime default null,
`admin_notes` varchar(2000) character set utf8mb3 default null,
`isblocked` bit(1) default 0,
`blockeddate` datetime default null,
`adminblockeddate` datetime default null,
`blocked_state` varchar(200) character set utf8mb3 not null default 'not_blocked',
`cust_id` varchar(255) character set utf8mb3 default null,
`card_id` varchar(255) character set utf8mb3 default null,
`is_blocked` bit(1) default null,
`admin_blocked_date` datetime(6) default null,
`status_message_friendship` varchar(255) character set utf8mb3 default null,
primary key (`id`),
unique key `user_type_email_idx` (`email`,`dtype`),
unique key `user_type_alias_idx` (`alias`,`dtype`));

create table `user_activity_log` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`user_activity` varchar(255) character set utf8mb3 not null,
`category` varchar(255) character set utf8mb3 default null,
primary key (`id`));

create table `user_news` (
`id` bigint not null auto_increment,
`hide` bit(1) not null,
`user_id` bigint not null,
`news_id` bigint not null,
`change_date` datetime default null,
`create_date` datetime default null,
`viewed` bit(1) not null,
`notified` bit(1) default 0,
primary key (`id`),
unique key `user_id_news_id` (`user_id`,`news_id`));

create table `user_payment_settings` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`payment_method` varchar(255) character set utf8mb3 not null,
`action_code` varchar(255) character set utf8mb3 default null,
primary key (`id`),
unique key `user_payment_settings_user_id` (`user_id`));

create table `user_pin` (
`id` bigint not null auto_increment,
`create_date` datetime not null,
`change_date` timestamp null default current_timestamp on update current_timestamp,
`user_id` bigint not null,
`pin` bigint not null,
primary key (`id`));

create table `user_recommendation_category` (
`user_id` bigint not null,
`categories` varchar(255) character set utf8mb3 not null,
primary key (`user_id`,`categories`));

create table `user_registration_state` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`user_id` bigint not null,
`registration_state` varchar(255) character set utf8mb3 not null,
`recommendation_category` varchar(255) character set utf8mb3 default null,
`questionnaire_id` bigint default null,
primary key (`id`),
unique key `user_registration_user` (`user_id`));

create table `user_roles` (
`user_id` bigint not null,
`role_id` bigint not null,
primary key (`user_id`,`role_id`));

create table `user_settings` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`cancellation_policy_accepted` bit(1) not null default 0,
`community_rules_accepted` bit(1) not null default 0,
`disable_ads` bit(1) not null default 0,
`general_terms_accepted` bit(1) not null default 0,
`privacy_policy_accepted` bit(1) not null default 0,
`disable_recommendation_notifications` bit(1) not null default 0,
`disable_cipher_message_notifications` bit(1) not null default 0,
`disable_positive_ranking_notifications` bit(1) not null default 0,
`disable_news_notifications` bit(1) not null default 0,
`disable_footprint_notifications` bit(1) not null default 0,
`enable_marketing_notifications` bit(1) not null default 1,
`user_id` bigint not null,
`language` varchar(255) character set utf8mb3 not null default 'de',
primary key (`id`));

create table `user_statistic` (
`id` bigint not null auto_increment,
`change_date` datetime default null,
`create_date` datetime default null,
`start_date` datetime not null,
`end_date` datetime default null,
`status` varchar(255) default null,
primary key (`id`));

null$);

alter table activator add constraint `activator_ibfk_1` foreign key (`activating_question_id`) references `question` (`id`);
alter table activator add constraint `activator_ibfk_2` foreign key (`enables_questionnaire_id`) references `questionnaire` (`id`);
alter table activator add constraint `activator_ibfk_3` foreign key (`enables_question_id`) references `question` (`id`);
alter table activator add constraint `activator_ibfk_4` foreign key (`enables_question_group_id`) references `question_group` (`id`);
alter table activator_choice add constraint `activator_choice_ibfk_1` foreign key (`activator_id`) references `activator` (`id`);
alter table activator_choice add constraint `activator_choice_ibfk_2` foreign key (`choice_id`) references `choice` (`id`);
alter table active_inactive add constraint `active_inactive_ibfk_1` foreign key (`subscription_id`) references `subscription` (`id`);
alter table active_inactive add constraint `fkhn8ucdblpcorvvv9orkqvebl7` foreign key (`user_id`) references `user_` (`id`);
alter table address add constraint `address_ibfk_1` foreign key (`user_id`) references `user_` (`id`) on delete cascade on update cascade;
alter table address add constraint `address_ibfk_2` foreign key (`continent_id`) references `locatable` (`id`);
alter table address add constraint `address_ibfk_3` foreign key (`country_id`) references `locatable` (`id`);
alter table address add constraint `address_ibfk_4` foreign key (`zip_id`) references `locatable` (`id`);
alter table address add constraint `address_ibfk_5` foreign key (`region_id`) references `locatable` (`id`);
alter table admin_reminder add constraint `admin_reminder_ibfk_1` foreign key (`admin_id`) references `user_` (`id`);
alter table admin_roles add constraint `admin_roles_ibfk_1` foreign key (`admin_id`) references `user_` (`id`);
alter table admin_user_login add constraint `admin_user_login_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table admin_user_login add constraint `admin_user_login_ibfk_2` foreign key (`admin_id`) references `user_` (`id`);
alter table admin_work_item add constraint `admin_work_item_ibfk_1` foreign key (`message_id`) references `message` (`id`);
alter table admin_work_item add constraint `admin_work_item_ibfk_2` foreign key (`admin_id`) references `user_` (`id`);
alter table affiliate_payment_state add constraint `affiliate_payment_state_ibfk_1` foreign key (`payment_id`) references `payment` (`id`);
alter table affinity_question add constraint `affinity_question_ibfk_1` foreign key (`questions_mapping_id`) references `questions_mapping` (`id`);
alter table affinity_question add constraint `affinity_question_ibfk_2` foreign key (`question_id`) references `question` (`id`);
alter table after_cancel add constraint `after_cancel_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table answer add constraint `answer_ibfk_1` foreign key (`question_id`) references `question` (`id`);
alter table answer add constraint `fkd9btbpysag892wvmcbi04csfc` foreign key (`user_id`) references `user_` (`id`);
alter table area_match_statistic_entry add constraint `area_match_statistic_entry_ibfk_1` foreign key (`match_statistic_id`) references `match_statistic` (`id`);
alter table audio add constraint `audio_ibfk_1` foreign key (`author_id`) references `user_` (`id`);
alter table avatar add constraint `avatar_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table avatar add constraint `avatar_ibfk_2` foreign key (`file_id`) references `file` (`id`);
alter table avatar add constraint `avatar_ibfk_3` foreign key (`thumbnail_id`) references `file` (`id`);
alter table avatar add constraint `avatar_ibfk_4` foreign key (`mediafile_id`) references `file` (`id`);
alter table bank_account add constraint `bank_account_ibfk_1` foreign key (`country_id`) references `locatable` (`id`);
alter table cancel_reason add constraint `cancel_reason_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table chat_attachment add constraint `file_key` foreign key (`file_id`) references `file` (`id`);
alter table chat_attachment add constraint `fk_chat_attachment_1` foreign key (`message_id`) references `chat_messages` (`id`);
alter table choice add constraint `choice_ibfk_1` foreign key (`choice_group_id`) references `choice_group` (`id`);
alter table choice_answer add constraint `choice_answer_ibfk_1` foreign key (`answer_id`) references `answer` (`id`) on delete cascade;
alter table choice_answer add constraint `choice_answer_ibfk_2` foreign key (`choice_id`) references `choice` (`id`);
alter table client_information add constraint `fk_client_information_1` foreign key (`user_id`) references `user_` (`id`);
alter table conversations_contact_lists add constraint `fk_conversations` foreign key (`conversation_id`) references `conversations` (`id`);
alter table email_link add constraint `email_link_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table email_link_parameter add constraint `email_link_parameter_ibfk_1` foreign key (`email_link_id`) references `email_link` (`id`);
alter table envelope add constraint `envelope_ibfk_3` foreign key (`user_id`) references `user_` (`id`);
alter table envelope add constraint `envelope_ibfk_4` foreign key (`message_id`) references `message` (`id`);
alter table external_payment_registration add constraint `external_payment_registration_ibfk_2` foreign key (`user_id`) references `user_` (`id`);
alter table filter add constraint `filter_ibfk_10` foreign key (`number_question_id`) references `question` (`id`);
alter table filter add constraint `filter_ibfk_1` foreign key (`left_filter_id`) references `filter` (`id`);
alter table filter add constraint `filter_ibfk_2` foreign key (`right_filter_id`) references `filter` (`id`);
alter table filter add constraint `filter_ibfk_3` foreign key (`single_filter_id`) references `filter` (`id`);
alter table filter add constraint `filter_ibfk_4` foreign key (`template_filter_id`) references `filter` (`id`);
alter table filter add constraint `filter_ibfk_5` foreign key (`choice_question_id`) references `question` (`id`);
alter table filter add constraint `filter_ibfk_6` foreign key (`choice_id`) references `choice` (`id`);
alter table filter add constraint `filter_ibfk_7` foreign key (`zip_id`) references `locatable` (`id`);
alter table filter add constraint `filter_ibfk_8` foreign key (`locatable_id`) references `locatable` (`id`);
alter table filter add constraint `filter_ibfk_9` foreign key (`text_question_id`) references `question` (`id`);
alter table heidelpay_transaction add constraint `heidelpay_transaction_ibfk_1` foreign key (`payment_id`) references `payment` (`id`);
alter table invoice add constraint `invoice_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table invoice_item add constraint `invoice_item_ibfk_1` foreign key (`invoice_id`) references `invoice` (`id`);
alter table invoice_item add constraint `invoice_item_ibfk_2` foreign key (`subscription_id`) references `subscription` (`id`);
alter table invoice_item add constraint `invoice_item_ibfk_3` foreign key (`product_id`) references `product` (`id`);
alter table jwt_token add constraint `jwt_token_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table locatable add constraint `locatable_ibfk_1` foreign key (`parent_id`) references `locatable` (`id`);
alter table match_ add constraint `match__ibfk_1` foreign key (`source_user_id`) references `user_` (`id`);
alter table match_ add constraint `match__ibfk_2` foreign key (`target_user_id`) references `user_` (`id`);
alter table match_statistic_entry add constraint `match_statistic_entry_ibfk_1` foreign key (`match_statistic_id`) references `match_statistic` (`id`);
alter table matching_matrix add constraint `matching_matrix_ibfk_6` foreign key (`source_choice_group_id`) references `choice_group` (`id`);
alter table matching_matrix add constraint `matching_matrix_ibfk_7` foreign key (`target_choice_group_id`) references `choice_group` (`id`);
alter table matching_matrix_value add constraint `matching_matrix_value_ibfk_1` foreign key (`matrix_id`) references `matching_matrix` (`id`);
alter table matching_matrix_value add constraint `matching_matrix_value_ibfk_8` foreign key (`source_choice_id`) references `choice` (`id`);
alter table matching_matrix_value add constraint `matching_matrix_value_ibfk_9` foreign key (`target_choice_id`) references `choice` (`id`);
alter table media add constraint `media_ibfk_1` foreign key (`file_id`) references `file` (`id`);
alter table media add constraint `media_ibfk_2` foreign key (`media_gallery_id`) references `media_gallery` (`id`);
alter table media_gallery add constraint `media_gallery_ibfk_1` foreign key (`author_id`) references `user_` (`id`);
alter table media_gallery_visible_relationship add constraint `media_gallery_visible_relationship_ibfk_1` foreign key (`media_gallery_id`) references `media_gallery` (`id`);
alter table media_gallery_visible_relationship add constraint `media_gallery_visible_relationship_ibfk_2` foreign key (`relationship_id`) references `relationship` (`id`);
alter table mediagallery_category add constraint `mediagallery_category_ibfk_1` foreign key (`mediagallery_id`) references `media_gallery` (`id`);
alter table message add constraint `message_ibfk_1` foreign key (`reply_to_message_id`) references `message` (`id`);
alter table message_attachment add constraint `message_attachment_ibfk_1` foreign key (`file_id`) references `file` (`id`);
alter table news add constraint `news_ibfk_1` foreign key (`filter_id`) references `filter` (`id`);
alter table payment add constraint `payment_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table payment add constraint `payment_ibfk_2` foreign key (`bank_account_id`) references `bank_account` (`id`);
alter table payment add constraint `payment_ibfk_3` foreign key (`payment_to_refund_id`) references `payment` (`id`);
alter table paypal_token add constraint `user_id` foreign key (`user_id`) references `user_` (`id`);
alter table product add constraint `product_ibfk_1` foreign key (`auto_renewal_offer_id`) references `product` (`id`);
alter table proximity_search_request add constraint `proximity_search_request_ibfk_1` foreign key (`answer_id`) references `answer` (`id`);
alter table proximity_search_request add constraint `proximity_search_request_ibfk_2` foreign key (`center_zip_id`) references `locatable` (`id`);
alter table question add constraint `question_ibfk_1` foreign key (`question_group_id`) references `question_group` (`id`);
alter table question add constraint `question_ibfk_2` foreign key (`choice_group_id`) references `choice_group` (`id`);
alter table question_group add constraint `question_group_ibfk_1` foreign key (`questionnaire_id`) references `questionnaire` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_10` foreign key (`max_age_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_11` foreign key (`min_age_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_12` foreign key (`avatar_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_13` foreign key (`true_choice_id`) references `choice` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_14` foreign key (`max_age_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_15` foreign key (`min_age_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_16` foreign key (`avatar_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_17` foreign key (`true_choice_id`) references `choice` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_4` foreign key (`fact_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_5` foreign key (`max_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_6` foreign key (`min_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_7` foreign key (`matrix_id`) references `matching_matrix` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_8` foreign key (`source_question_id`) references `question` (`id`);
alter table questions_mapping add constraint `questions_mapping_ibfk_9` foreign key (`target_question_id`) references `question` (`id`);
alter table recommendation_break add constraint `recommendation_break_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table region_search_request add constraint `region_search_request_ibfk_1` foreign key (`answer_id`) references `answer` (`id`);
alter table region_search_request add constraint `region_search_request_ibfk_2` foreign key (`continent_id`) references `locatable` (`id`);
alter table region_search_request add constraint `region_search_request_ibfk_3` foreign key (`country_id`) references `locatable` (`id`);
alter table region_search_request_restriction add constraint `region_search_request_restriction_ibfk_1` foreign key (`region_search_request_id`) references `region_search_request` (`id`);
alter table region_search_request_restriction add constraint `region_search_request_restriction_ibfk_2` foreign key (`locatable_id`) references `locatable` (`id`);
alter table relationship add constraint `relationship_ibfk_1` foreign key (`source_user_id`) references `user_` (`id`);
alter table relationship add constraint `relationship_ibfk_2` foreign key (`target_user_id`) references `user_` (`id`);
alter table relationship_category add constraint `relationship_category_ibfk_1` foreign key (`relationship_id`) references `relationship` (`id`);
alter table scamming add constraint `scamming_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table service_offer_category add constraint `service_offer_category_ibfk_1` foreign key (`service_offer_id`) references `product` (`id`);
alter table subscription add constraint `subscription_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table subscription add constraint `subscription_ibfk_2` foreign key (`offer_id`) references `product` (`id`);
alter table subscription_offer_category add constraint `subscription_offer_category_ibfk_1` foreign key (`subscription_offer_id`) references `product` (`id`);
alter table template_context add constraint `template_context_ibfk_1` foreign key (`template_filter_id`) references `filter` (`id`);
alter table unzer_charge add constraint `payment_id_charge_id` foreign key (`payment_id`) references `payment` (`id`);
alter table unzer_customer add constraint `user_id_id_user_` foreign key (`user_id`) references `user_` (`id`);
alter table upgrade_offer_subscription_offer add constraint `upgrade_offer_subscription_offer_ibfk_1` foreign key (`upgrade_offer_id`) references `product` (`id`);
alter table upgrade_offer_subscription_offer add constraint `upgrade_offer_subscription_offer_ibfk_2` foreign key (`subscription_offer_id`) references `product` (`id`);
alter table user_activity_log add constraint `user_activity_log_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table user_news add constraint `user_news_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table user_news add constraint `user_news_ibfk_2` foreign key (`news_id`) references `news` (`id`);
alter table user_payment_settings add constraint `user_payment_settings_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table user_recommendation_category add constraint `user_recommendation_category_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
alter table user_registration_state add constraint `user_registration_state_user` foreign key (`user_id`) references `user_` (`id`);
alter table user_roles add constraint `user_roles_ibfk_1` foreign key (`role_id`) references `roles` (`id`);
alter table user_roles add constraint `user_roles_ibfk_2` foreign key (`user_id`) references `user_` (`id`);
alter table user_settings add constraint `user_settings_ibfk_1` foreign key (`user_id`) references `user_` (`id`);
