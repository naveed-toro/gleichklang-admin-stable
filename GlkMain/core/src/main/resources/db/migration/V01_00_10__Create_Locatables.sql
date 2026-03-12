DROP TABLE IF EXISTS locatable;

CREATE TABLE IF NOT EXISTS `locatable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `DTYPE` varchar(255) DEFAULT NULL,
  change_date        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date        DATETIME             DEFAULT NULL,
  `legacy_id` varchar(255) DEFAULT NULL,
  `i18n_key` varchar(255) DEFAULT NULL,
  `zip` varchar(25) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `region_name` varchar(255) DEFAULT NULL,
  `region_id` bigint(20) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `locatable_sort_order_Dtype` (`DTYPE`,`sort_order`),
  KEY `locatable_i18n_key_idx` (`i18n_key`),
  KEY `i18n_index` (`i18n_key`),
  KEY `parent_index` (`parent_id`),
  KEY `DTYPE_index` (`DTYPE`),
  KEY `locatable_legacy_idx` (`legacy_id`),
  CONSTRAINT `locatable_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `locatable` (`id`)
);
