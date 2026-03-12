SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS proximity_search_locatable, proximity_search, answer_locatable;

DROP TABLE IF EXISTS locatable_search_request;
CREATE TABLE locatable_search_request
(
  `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  DTYPE           VARCHAR(31) NOT NULL,
  `change_date`   DATETIME             DEFAULT NULL,
  `create_date`   DATETIME             DEFAULT NULL,
  `legacy_id`     VARCHAR(255)         DEFAULT NULL,
  `answer_id`     BIGINT               DEFAULT NULL,
  `center_zip_id` BIGINT(20)           DEFAULT NULL,
  `distance`      INT(11)              DEFAULT NULL,
  CONSTRAINT `locatable_search_request_ibfk_1` FOREIGN KEY (`center_zip_id`) REFERENCES `locatable` (`id`),
  CONSTRAINT `locatable_search_request_ibfk_2` FOREIGN KEY (`answer_id`) REFERENCES `answer` (`id`)
);


DROP TABLE IF EXISTS locatable_search_request_restriction;
CREATE TABLE locatable_search_request_restriction (
  `locatable_search_request_id` BIGINT(20) NOT NULL DEFAULT '0',
  `locatable_id`                BIGINT(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`locatable_search_request_id`, `locatable_id`),
  CONSTRAINT `locatable_search_request_restriction_ibfk_1` FOREIGN KEY (`locatable_search_request_id`) REFERENCES `locatable_search_request` (`id`),
  CONSTRAINT `locatable_search_request_restriction_ibfk_2` FOREIGN KEY (`locatable_id`) REFERENCES `locatable` (`id`)
);


SET FOREIGN_KEY_CHECKS = 1;