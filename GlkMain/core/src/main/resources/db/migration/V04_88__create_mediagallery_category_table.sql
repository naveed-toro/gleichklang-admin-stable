CREATE TABLE IF NOT EXISTS `mediagallery_category` (
  `category` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `mediagallery_id` bigint NOT NULL,
  PRIMARY KEY (`category`,`mediagallery_id`),
  KEY `mediagallery_id` (`mediagallery_id`),
  CONSTRAINT `mediagallery_category_ibfk_1` FOREIGN KEY (`mediagallery_id`) REFERENCES `media_gallery` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci