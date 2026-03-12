-- phpMyAdmin SQL Dump
-- version 4.0.6deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 26, 2014 at 02:19 PM
-- Server version: 5.5.37-0ubuntu0.13.10.1
-- PHP Version: 5.5.3-1ubuntu2.5

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `gleichklang`
--

-- --------------------------------------------------------

--
-- Table structure for table `chat_sessions`
--

CREATE TABLE IF NOT EXISTS `chat_sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id1` varchar(75) NOT NULL,
  `id2` varchar(75) NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `compactiveuser`
--

CREATE TABLE IF NOT EXISTS `compactiveuser` (
  `user`         VARCHAR(75)          DEFAULT NULL,
  `usertype`     VARCHAR(30)          DEFAULT NULL,
  `lastlogin`    DATETIME             DEFAULT NULL,
  `sessioncount` MEDIUMINT(9)         DEFAULT NULL,
  `no`           VARCHAR(75) NOT NULL DEFAULT '',
  `type`         VARCHAR(30)          DEFAULT NULL,
  `createdate`   DATETIME             DEFAULT NULL,
  `changedate`   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compactiveuser_user` (`user`),
  KEY `compactiveuser_createdate` (`createdate`),
  KEY `compactiveuser_changedate` (`changedate`),
  KEY `compactiveuser_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compadjektive` (
  `as_1e`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_5r`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_6v`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_7e`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_9n`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_11r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_12v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_14g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_15n`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_16o`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_17r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_19e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_20g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_21n`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_23r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_24v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_25e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_26g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_27n`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_28o`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_29r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_30v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_31e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_32g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_34o`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_35r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_36v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_37e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_38g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_39n`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_40o`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_42v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_44g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_45n`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_46o`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compadjektive_uniowner` (`owner`),
  KEY `compadjektive_changedate` (`changedate`),
  KEY `compadjektive_createdate` (`createdate`),
  KEY `compadjektive_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compadportalrecipient` (
  `portalid`              VARCHAR(127)         DEFAULT NULL,
  `portal_mail`           VARCHAR(127)         DEFAULT NULL,
  `portal_username`       VARCHAR(127)         DEFAULT NULL,
  `portal_firstname`      VARCHAR(127)         DEFAULT NULL,
  `portal_lastname`       VARCHAR(127)         DEFAULT NULL,
  `portal_salutation`     MEDIUMINT(9)         DEFAULT NULL,
  `portal_messagestosend` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `no`                    VARCHAR(75) NOT NULL DEFAULT '',
  `type`                  VARCHAR(30)          DEFAULT NULL,
  `createdate`            DATETIME             DEFAULT NULL,
  `changedate`            DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compadportalrecipient_mail` (`portal_mail`),
  KEY `compadportalrecipient_createdate` (`createdate`),
  KEY `compadportalrecipient_componenttype` (`type`),
  KEY `compadportalrecipient_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compaffiliate_promotion` (
  `owner`            VARCHAR(75)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `promotion_key`    VARCHAR(20)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `promotion_name`   VARCHAR(40)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`               VARCHAR(75)
                     COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`             VARCHAR(30)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`       DATETIME                           DEFAULT NULL,
  `changedate`       DATETIME                           DEFAULT NULL,
  `promotion_active` TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compaffiliate_promotion_promotion_key` (`promotion_key`),
  KEY `compaffiliate_promotion_owner` (`owner`),
  KEY `compaffiliate_promotion_createdate` (`createdate`),
  KEY `compaffiliate_promotion_changedate` (`changedate`),
  KEY `compaffiliate_promotion_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compaffiliate_promotion_refund` (
  `owner`                VARCHAR(75)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `promotion`            VARCHAR(75)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `promotion_key`        VARCHAR(20)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `promotion_target`     VARCHAR(255)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `promoted_participant` VARCHAR(75)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `promoted_affiliate`   VARCHAR(75)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `recursion_level`      MEDIUMINT(9)                       DEFAULT NULL,
  `promotion_state`      VARCHAR(255)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                   VARCHAR(75)
                         COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                 VARCHAR(30)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`           DATETIME                           DEFAULT NULL,
  `changedate`           DATETIME                           DEFAULT NULL,
  `refund_amount`        INT(11)                            DEFAULT NULL,
  `promoted_date`        DATE                               DEFAULT NULL,
  `promoted_transaction` VARCHAR(75)
                         COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compaffiliate_promotion_refund_promotion_key` (`promotion_key`),
  KEY `compaffiliate_promotion_refund_promotion_state` (`promotion_state`),
  KEY `compaffiliate_promotion_refund_promoted_affiliate` (`promoted_affiliate`),
  KEY `compaffiliate_promotion_refund_createdate` (`createdate`),
  KEY `compaffiliate_promotion_refund_owner` (`owner`),
  KEY `compaffiliate_promotion_refund_recursion_level` (`recursion_level`),
  KEY `compaffiliate_promotion_refund_promoted_participant` (`promoted_participant`),
  KEY `compaffiliate_promotion_refund_promotion_target` (`promotion_target`),
  KEY `compaffiliate_promotion_refund_changedate` (`changedate`),
  KEY `compaffiliate_promotion_refund_promotion` (`promotion`),
  KEY `compaffiliate_promotion_refund_componenttype` (`type`),
  KEY `compaffiliate_promotion_refund_promoted_transaction` (`promoted_transaction`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compaussehen` (
  `groesse`          MEDIUMINT(9)                       DEFAULT NULL,
  `gewicht`          MEDIUMINT(9)                       DEFAULT NULL,
  `figur`            VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `koerpertyp`       VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `haarfarbe`        VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `haarlaenge`       VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `glatze`           VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `haartyp`          VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `augenfarbe`       VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `brille`           VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `bart`             VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `brustbehaarung`   VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `koerperbehaarung` VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`            VARCHAR(75)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`               VARCHAR(75)
                     COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`             VARCHAR(30)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`       DATETIME                           DEFAULT NULL,
  `changedate`       DATETIME                           DEFAULT NULL,
  `fem_mask`         VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compaussehen_uniowner` (`owner`),
  KEY `compaussehen_changedate` (`changedate`),
  KEY `compaussehen_createdate` (`createdate`),
  KEY `compaussehen_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compcircularletter` (
  `title`                 VARCHAR(255)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `subject`               VARCHAR(70)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `body`                  TEXT
                          COLLATE latin1_german2_ci,
  `template`              VARCHAR(50)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `recipientqueryfinder`  VARCHAR(70)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `state`                 VARCHAR(30)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `scheduledate`          DATETIME                           DEFAULT NULL,
  `sentdate`              DATETIME                           DEFAULT NULL,
  `recipientsize`         MEDIUMINT(9)                       DEFAULT NULL,
  `no`                    VARCHAR(75)
                          COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                  VARCHAR(30)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`            DATETIME                           DEFAULT NULL,
  `changedate`            DATETIME                           DEFAULT NULL,
  `recipientqueryfilter`  VARCHAR(120)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `announcedatefrom`      DATE                               DEFAULT NULL,
  `announcedateto`        DATE                               DEFAULT NULL,
  `actionnumber`          VARCHAR(255)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `participant_states`    TEXT
                          COLLATE latin1_german2_ci,
  `pressinfo_option`      VARCHAR(50)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `donation_paid`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `land`                  VARCHAR(50)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz`                   VARCHAR(10)
                          COLLATE latin1_german2_ci          DEFAULT NULL,
  `radius`                MEDIUMINT(9)                       DEFAULT NULL,
  `region_reverse`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `circular_satisfaction` TEXT
                          COLLATE latin1_german2_ci,
  `extension_flag`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `adsportal_filter`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compcircularletter_scheduledate_state` (`scheduledate`, `state`),
  KEY `compcircularletter_changedate` (`changedate`),
  KEY `compcircularletter_sentdate` (`sentdate`),
  KEY `compcircularletter_createdate` (`createdate`),
  KEY `compcircularletter_sentdate_state` (`sentdate`, `state`),
  KEY `compcircularletter_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_adjektive` (
  `as_1e`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_2g`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_3n`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_4o`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_5r`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_6v`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_7e`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_8g`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_9n`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_10o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_11r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_12v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_13e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_14g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_15n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_16o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_17r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_18v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_19e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_20g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_21n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_22o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_23r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_24v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_25e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_26g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_27n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_28o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_29r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_30v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_31e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_32g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_33n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_34o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_35r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_36v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_37e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_38g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_39n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_40o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_41r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_42v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_43e`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_44g`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_45n`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_46o`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_47r`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `as_48v`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_adjektive_mail` (`mail`),
  UNIQUE KEY `compext_adjektive_uniowner` (`owner`),
  KEY `compext_adjektive_changedate` (`changedate`),
  KEY `compext_adjektive_createdate` (`createdate`),
  KEY `compext_adjektive_pseudonym` (`pseudonym`),
  KEY `compext_adjektive_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_freundschaft` (
  `fd_if14`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if18`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if24`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if7`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if17`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if1`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if19`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if15`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if21`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if2`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if20`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if12`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if5`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if6`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if8`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if9`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if22`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if16`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if10`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if13`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if4`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if23`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if11`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_if3`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf70`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp109`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u32`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf83`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf87`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u28`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u33`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps51`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp106`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp102`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf71`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps56`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u27`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag41`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp112`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf82`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps48`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd119`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag42`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp110`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag43`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps53`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf68`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag40`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf76`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf81`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp105`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd117`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp126`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u25`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd125`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps50`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps46`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf89`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u29`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag37`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp108`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf64`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf95`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps59`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf90`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd124`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf72`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf91`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf88`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf77`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf98`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps57`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp129`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf96`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd122`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag35`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf80`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps52`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag44`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag45`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp100`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp113`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf86`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp111`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps61`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf85`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf62`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf69`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps54`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps55`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps58`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf99`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf73`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf74`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp104`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp107`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u26`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u31`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag34`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf75`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf79`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd115`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag36`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf65`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp127`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd120`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag39`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf78`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp128`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf84`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp130`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf93`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf66`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd116`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf97`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf92`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp101`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd123`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag38`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd118`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps60`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf63`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd121`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u30`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps49`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd114`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps47`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf67`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_freundschaft_mail` (`mail`),
  UNIQUE KEY `compext_freundschaft_uniowner` (`owner`),
  KEY `compext_freundschaft_createdate` (`createdate`),
  KEY `compext_freundschaft_changedate` (`changedate`),
  KEY `compext_freundschaft_pseudonym` (`pseudonym`),
  KEY `compext_freundschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_gesellschaft` (
  `g1_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_11`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_12`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_11`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_12`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_13`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_12`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_14`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_15`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_16`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_gesellschaft_mail` (`mail`),
  UNIQUE KEY `compext_gesellschaft_uniowner` (`owner`),
  KEY `compext_gesellschaft_createdate` (`createdate`),
  KEY `compext_gesellschaft_changedate` (`changedate`),
  KEY `compext_gesellschaft_pseudonym` (`pseudonym`),
  KEY `compext_gesellschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_hobby_e` (
  `h_m2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_t5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo10`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci11`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci12`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk13`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et14`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn15`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se16`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b17`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e19`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li20`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li21`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk22`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st23`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk24`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se25`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_k26`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn28`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci29`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et30`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe31`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg32`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn33`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo34`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag35`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e36`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn37`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s38`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et39`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_t40`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag41`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r42`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et43`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b44`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk46`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et47`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht48`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo49`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li50`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn51`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s52`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te53`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m54`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b55`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci56`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te57`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe59`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe60`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn61`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv62`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci63`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et64`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e65`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk66`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_t67`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe68`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk69`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag70`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd71`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw72`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv73`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b74`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s75`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r76`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff77`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st78`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se79`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv80`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m81`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r82`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv83`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht84`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd85`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st86`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk87`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw88`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk89`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te90`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m91`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd92`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s93`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m94`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st95`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et96`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo97`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd98`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag99`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gs100`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et101`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e102`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e103`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et104`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li105`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo106`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st107`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b108`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg109`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw110`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e111`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo112`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r113`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b114`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw115`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk116`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m119`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw120`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li121`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m122`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff123`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff124`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li125`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li126`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st127`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_t128`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci129`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_k130`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et131`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag132`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et133`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff134`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se135`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht136`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg137`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et138`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk139`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk140`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te141`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s142`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st143`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag144`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci145`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag146`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e147`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe148`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et149`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht150`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te151`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag152`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn153`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m154`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r155`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo156`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn157`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv158`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg159`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk160`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg161`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r162`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd163`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r164`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv165`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li166`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s167`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk168`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se169`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et170`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s171`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw172`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk173`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s174`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r175`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et176`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg178`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn179`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk180`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `h_sonst`                       TEXT
                                  COLLATE latin1_german2_ci,
  `h_ge200`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge201`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge202`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge203`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge204`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_hobby_e_mail` (`mail`),
  UNIQUE KEY `compext_hobby_e_uniowner` (`owner`),
  KEY `compext_hobby_e_createdate` (`createdate`),
  KEY `compext_hobby_e_changedate` (`changedate`),
  KEY `compext_hobby_e_pseudonym` (`pseudonym`),
  KEY `compext_hobby_e_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_kritische_lebensereignisse` (
  `v1`                            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v3`                            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v7`                            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v10`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v12`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v13`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v15`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v18`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v20`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v21`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v23`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v24`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v27`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v28`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v29`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v32`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v34`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v35`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v36`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v37`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v40`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v44`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v45`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v46`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v48`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v50`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v55`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v58`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v59`                           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_kritische_lebensereignisse_mail` (`mail`),
  UNIQUE KEY `compext_kritische_lebensereignisse_uniowner` (`owner`),
  KEY `compext_kritische_lebensereignisse_createdate` (`createdate`),
  KEY `compext_kritische_lebensereignisse_changedate` (`changedate`),
  KEY `compext_kritische_lebensereignisse_pseudonym` (`pseudonym`),
  KEY `compext_kritische_lebensereignisse_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_partnerschaft` (
  `p1_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_7`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p3_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p4_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_8`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_9`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `p32_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_1`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_2`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_3`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_4`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_5`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_10`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_10`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_6`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_11`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_11`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_12`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_partnerschaft_mail` (`mail`),
  UNIQUE KEY `compext_partnerschaft_uniowner` (`owner`),
  KEY `compext_partnerschaft_changedate` (`changedate`),
  KEY `compext_partnerschaft_createdate` (`createdate`),
  KEY `compext_partnerschaft_pseudonym` (`pseudonym`),
  KEY `compext_partnerschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_persoenlichkeit` (
  `pk_10_a`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_17_i`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_19_v`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_20_f`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_22_e`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_23_b`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_24_g`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_27_p`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_28_l`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_29_s`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_30_be`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_33se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_44p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_31_ae`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_32_r`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_33_d`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_34_sb`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_35_im`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_36_ve`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_37_h`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_38_ge`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_39_du`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_41_er`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_43_fa`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_44_aes`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_45_gf`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_46_ha`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_47_i`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_48_wn`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_51_al`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_52_e`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_53_b`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_54_g`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_55_k`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_57_p`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_58_l`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_59_s`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_60_be`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_1se_se`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_3ge_ga`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_10_ma_md`                   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_15na_na`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_20ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_34se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_35p_fi`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_45p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_61_ae`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_62_r`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_64_sb`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_65_im`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_66_ve`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_67_h`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_68_ge`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_69_du`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_71_er`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_72_pe`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_73_fa`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_75_gf`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_76_hg`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_77_i`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_79_v`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_81_al`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_82_e`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_85_k`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_86_ol`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_87_p`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_88_l`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_90_be`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_2se_se`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_4ge_ga`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_11ma_md`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_16na_na`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk21_ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_36p_fk`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_46p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_91_ae`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_92_r`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_94_sb`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_95_im`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_96_ve`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_98_ge`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_99_du`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_100_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_101_er`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_104_aes`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_105_gf`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_107_i`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_108_wn`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_110_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_111_al`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_112_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_113_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_115_k`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_117_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_119_s`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_120_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_12ma_md`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_17na_na`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_22ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_26se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_37p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_121_ae`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_123_d`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_124_sb`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_125_im`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_130_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_132_pe`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_133_fa`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_134_aes`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_136_hg`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_137_i`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_139_v`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_140_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_141_al`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_142_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_145_k`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_146_ol`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_147_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_148_l`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_150_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_6_ge_ga`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_13ma_md`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_18na_na`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_23ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_27se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_151_ae`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_152_r`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_153_d`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_156_ve`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_157_h`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_158_ge`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_159_du`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_160_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_161_er`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_162_pe`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_163_fa`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_164_aes`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_166_hg`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_168_wn`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_169_v`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_172_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_173_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_174_g`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_175_k`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_177_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_178_l`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_180_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_7_ge_ga`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_14_ma_md`                   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_24ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_28se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_182_r`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_183_d`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_184_sb`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_185_im`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_186_ve`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_187_h`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_189_du`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_190_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_191_er`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_192_pe`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_194_aes`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_195_gf`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_196_hg`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_200_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_202_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_203_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_204_g`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_205_k`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_206_ol`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_207_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_209_s`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_210_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_8_ge_ga`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_25ri_ri`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_29se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_40p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_211_ae`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_212_r`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_215_im`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_216_ve`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_217_h`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_218_ge`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_219_du`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_220_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_223_fa`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_224_aes`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_227_i`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_228_wn`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_229_v`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_230_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_231_al`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_232_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_233_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_234_g`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_236_ol`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_237_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_240_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_9_ge_ga`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_30se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_243_d`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_244_sb`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_245_im`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_246_ve`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_247_h`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_250_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_251_er`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_252_pe`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_255_gf`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_256_hg`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_257_i`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_258_wn`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_260_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_261_al`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_262_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_263_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_264_g`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_266_ol`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_267_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_269_s`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_270_be`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_31se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_273_d`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_276_ve`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_278_ge`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_280_a`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_282_pe`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_285_gf`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_288_wn`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_289_v`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_290_f`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_292_e`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_293_b`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_294_g`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_297_p`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_298_l`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_299_s`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_32se_se`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_43p_ps`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_persoenlichkeit_mail` (`mail`),
  UNIQUE KEY `compext_persoenlichkeit_uniowner` (`owner`),
  KEY `compext_persoenlichkeit_changedate` (`changedate`),
  KEY `compext_persoenlichkeit_createdate` (`createdate`),
  KEY `compext_persoenlichkeit_pseudonym` (`pseudonym`),
  KEY `compext_persoenlichkeit_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_stoerbar` (
  `st_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_11`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_12`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_13`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_14`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_15`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_16`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_17`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_18`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_19`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_20`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_21`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_22`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_23`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_24`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_25`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_26`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_27`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_28`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_29`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_30`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_31`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_32`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_33`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_34`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_35`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_36`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_37`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_38`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_39`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_40`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_41`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_42`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_43`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_44`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_45`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_46`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_47`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_48`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_49`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_50`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_51`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_52`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_53`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_54`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_55`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_56`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_57`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_58`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_59`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_60`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_61`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_62`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_63`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_64`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_65`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_66`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_67`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_68`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_69`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_70`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_71`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_72`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_73`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_74`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_75`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_76`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_77`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_78`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_79`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_80`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_81`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_82`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_83`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_84`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_85`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_86`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_87`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_88`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_89`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_90`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_91`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_92`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_93`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_94`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_95`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_96`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_97`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_98`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_99`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_100`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_101`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_102`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_103`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_104`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_105`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_106`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_107`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_108`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_109`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_110`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_111`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_112`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_stoerbar_uniowner` (`owner`),
  KEY `compext_stoerbar_changedate` (`changedate`),
  KEY `compext_stoerbar_componenttype` (`type`),
  KEY `compext_stoerbar_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compext_stoerbarkeit` (
  `st_1`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_2`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_3`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_4`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_5`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_6`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_7`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_8`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_9`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_10`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_11`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_12`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_13`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_14`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_15`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_16`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_17`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_18`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_19`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_20`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_21`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_22`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_23`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_24`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_25`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_26`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_27`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_28`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_29`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_30`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_31`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_32`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_33`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_34`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_35`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_36`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_37`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_38`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_39`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_40`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_41`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_42`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_43`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_44`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_45`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_46`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_47`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_48`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_49`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_50`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_51`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_52`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_53`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_54`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_55`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_56`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_57`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_58`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_59`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_60`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_61`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_62`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_63`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_64`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_65`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_66`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_67`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_68`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_69`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_70`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_71`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_72`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_73`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_74`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_75`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_76`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_77`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_78`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_79`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_80`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_81`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_82`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_83`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_84`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_85`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_86`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_87`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_88`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_89`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_90`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_91`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_92`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_93`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_94`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_95`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_96`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_97`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_98`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_99`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_100`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_101`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_102`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_103`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_104`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_105`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_106`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_107`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_108`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_109`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_110`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_111`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_112`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `nachfrage`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compext_stoerbarkeit_uniowner` (`owner`),
  KEY `compext_stoerbarkeit_changedate` (`changedate`),
  KEY `compext_stoerbarkeit_componenttype` (`type`),
  KEY `compext_stoerbarkeit_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compffoto` (
  `foto1`      MEDIUMBLOB,
  `foto2`      MEDIUMBLOB,
  `foto3`      MEDIUMBLOB,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  `hide_fotos` TINYINT(3) UNSIGNED                DEFAULT '0',
  PRIMARY KEY (`no`),
  UNIQUE KEY `compffoto_uniowner` (`owner`),
  KEY `compffoto_changedate` (`changedate`),
  KEY `compffoto_createdate` (`createdate`),
  KEY `compffoto_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compfreund` (
  `owner`                    VARCHAR(75)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_minalter`               MEDIUMINT(9)                       DEFAULT NULL,
  `f_maxalter`               MEDIUMINT(9)                       DEFAULT NULL,
  `f_region`                 TEXT
                             COLLATE latin1_german2_ci,
  `f_plz_1`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_radius_1`               MEDIUMINT(9)                       DEFAULT NULL,
  `f_plz_2`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_radius_2`               MEDIUMINT(9)                       DEFAULT NULL,
  `f_plz_3`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_radius_3`               MEDIUMINT(9)                       DEFAULT NULL,
  `f_reise_partner`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_raucher_akzeptieren`    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_hiv`                    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_behinderung`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erkrankung_koer`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erkrankung_psy`         VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_vege_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_vegan_suche`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_hiv_select`             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_behinderten_select`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_koerperkrank_select`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_psychischkrank_select`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_religion_select`        MEDIUMINT(9)                       DEFAULT NULL,
  `f_gay_select`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                       VARCHAR(75)
                             COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                     VARCHAR(30)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`               DATETIME                           DEFAULT NULL,
  `changedate`               DATETIME                           DEFAULT NULL,
  `f_gay_mitteil_auch`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_gay_mitteil_nur`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_prefalter`              MEDIUMINT(9)                       DEFAULT NULL,
  `f_land_1`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_land_2`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_land_3`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_hu`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_ka`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_na`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_pf`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_vo`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_fi`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_re`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierakz_in`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_ka`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_ch`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_mu`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_ju`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_bu`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_hi`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_sh`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_na`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relakz_at`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_akad_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_oeko_praef`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_eso_praef`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fwunsch_fussball`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fwunsch_motorsport`     VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fwunsch_auto`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fwunsch_motorrad`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fwunsch_computer`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fakz_fussball`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fakz_motorsport`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fakz_auto`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fakz_motorrad`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fakz_computer`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_gay_mitteil`            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_nur_foto`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_kuenstler_suche`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_behinderung_suche`      VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erkrankung_koer_suche`  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erkrankung_psy_suche`   VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_hiv_suche`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_ch`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_is`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_jud`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_bud`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_hin`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_schin`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_eso`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_off`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_relsearch_agn`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_tierrechte_praef`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_bdsm_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_alleinerziehend_select` VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_bdsm_mitteil`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_relsearch_bahai`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_fkk_praef`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_bdsm_nur`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_hochsensibel`           VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erotik`                 VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_erotik_mitteil`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `f_bdsm_suche_allg`        VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `f_bdsm_suche_spezial`     VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compfreund_uniowner` (`owner`),
  KEY `compfreund_changedate` (`changedate`),
  KEY `compfreund_createdate` (`createdate`),
  KEY `compfreund_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compfreundschaft` (
  `fd_sf70`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u32`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u33`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps51`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp106`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp102`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf71`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps56`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp112`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps48`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd119`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag42`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag43`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps53`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf68`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf76`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp105`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd117`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u25`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd125`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps50`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps46`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf89`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u29`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf95`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf72`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf77`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps57`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp129`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag35`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps52`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp100`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_pp111`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf62`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf69`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps54`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ps55`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf74`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u26`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag34`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf75`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf79`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd115`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag36`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp127`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_ag39`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf78`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp128`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sp130`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_sf92`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_u30`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `fd_nd114`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compfreundschaft_uniowner` (`owner`),
  KEY `compfreundschaft_createdate` (`createdate`),
  KEY `compfreundschaft_changedate` (`changedate`),
  KEY `compfreundschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compftext` (
  `freiertext` TEXT
               COLLATE latin1_german2_ci,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  `frage1`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort1`   TEXT
               COLLATE latin1_german2_ci,
  `frage2`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort2`   TEXT
               COLLATE latin1_german2_ci,
  `frage3`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort3`   TEXT
               COLLATE latin1_german2_ci,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compftext_uniowner` (`owner`),
  KEY `compftext_changedate` (`changedate`),
  KEY `compftext_createdate` (`createdate`),
  KEY `compftext_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compgesellschaft` (
  `g1_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g1_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g2_9`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g3_10`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_11`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g5_12`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `g4_12`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compgesellschaft_uniowner` (`owner`),
  KEY `compgesellschaft_changedate` (`changedate`),
  KEY `compgesellschaft_createdate` (`createdate`),
  KEY `compgesellschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comphobby_e` (
  `h_m2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo9`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge200`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo10`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci11`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et14`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se16`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e19`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li20`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk22`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_k26`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn28`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et30`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg32`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn33`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo34`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge201`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag35`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e36`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn37`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_t40`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag41`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r42`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b44`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk46`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht48`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_s52`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m54`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci56`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te57`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_pe60`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn61`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv62`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci63`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et64`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge202`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e65`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag70`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd71`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw72`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b74`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r76`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st78`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se79`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv80`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m81`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r82`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_kv83`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw88`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m91`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m94`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et96`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo97`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag99`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge203`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gs100`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et101`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e102`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_e103`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et104`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st107`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg109`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw110`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_mo112`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r113`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_b114`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw115`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk116`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m119`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw120`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li121`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_m122`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff123`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_li126`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st127`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_k130`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et131`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et133`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ff134`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se135`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht136`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg137`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk139`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ge204`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te141`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_st143`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ci145`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et149`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ht150`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_te151`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_ag152`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn153`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r155`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_tn157`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk160`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_eg161`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r162`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gd163`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r164`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_fk168`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_se169`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_hw172`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gk173`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_r175`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_et176`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_gn179`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `h_dk180`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `comphobby_e_uniowner` (`owner`),
  KEY `comphobby_e_createdate` (`createdate`),
  KEY `comphobby_e_changedate` (`changedate`),
  KEY `comphobby_e_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comphobby_v` (
  `fernsehen`              TEXT
                           COLLATE latin1_german2_ci,
  `musik_passiv`           TEXT
                           COLLATE latin1_german2_ci,
  `musik_aktiv`            TEXT
                           COLLATE latin1_german2_ci,
  `bildung`                TEXT
                           COLLATE latin1_german2_ci,
  `computer`               TEXT
                           COLLATE latin1_german2_ci,
  `essen_kueche`           TEXT
                           COLLATE latin1_german2_ci,
  `essen_kost`             TEXT
                           COLLATE latin1_german2_ci,
  `geselligkeit`           TEXT
                           COLLATE latin1_german2_ci,
  `heimwerken`             TEXT
                           COLLATE latin1_german2_ci,
  `gestaltende_kunst`      TEXT
                           COLLATE latin1_german2_ci,
  `darstellende_kunst`     TEXT
                           COLLATE latin1_german2_ci,
  `foto_film`              TEXT
                           COLLATE latin1_german2_ci,
  `handarbeit`             TEXT
                           COLLATE latin1_german2_ci,
  `mode`                   TEXT
                           COLLATE latin1_german2_ci,
  `kulturveranstaltungen`  TEXT
                           COLLATE latin1_german2_ci,
  `spiele`                 TEXT
                           COLLATE latin1_german2_ci,
  `literatur_gattung`      TEXT
                           COLLATE latin1_german2_ci,
  `literatur_themen`       TEXT
                           COLLATE latin1_german2_ci,
  `tanzen`                 TEXT
                           COLLATE latin1_german2_ci,
  `gesundheit`             TEXT
                           COLLATE latin1_german2_ci,
  `sport_aktiv`            TEXT
                           COLLATE latin1_german2_ci,
  `sport_passiv`           TEXT
                           COLLATE latin1_german2_ci,
  `technik`                TEXT
                           COLLATE latin1_german2_ci,
  `tiere_natur`            TEXT
                           COLLATE latin1_german2_ci,
  `extremsport`            TEXT
                           COLLATE latin1_german2_ci,
  `soziales_engagement`    TEXT
                           COLLATE latin1_german2_ci,
  `politisches_engagement` TEXT
                           COLLATE latin1_german2_ci,
  `kommunikation`          TEXT
                           COLLATE latin1_german2_ci,
  `sammeln`                TEXT
                           COLLATE latin1_german2_ci,
  `geld`                   TEXT
                           COLLATE latin1_german2_ci,
  `esoterik`               TEXT
                           COLLATE latin1_german2_ci,
  `reisen`                 TEXT
                           COLLATE latin1_german2_ci,
  `sonstiges`              TEXT
                           COLLATE latin1_german2_ci,
  `owner`                  VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                     VARCHAR(75)
                           COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                   VARCHAR(30)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`             DATETIME                           DEFAULT NULL,
  `changedate`             DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `comphobby_v_uniowner` (`owner`),
  KEY `comphobby_v_createdate` (`createdate`),
  KEY `comphobby_v_changedate` (`changedate`),
  KEY `comphobby_v_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compkritische_lebensereignisse` (
  `v1`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v3`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v7`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v12`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v13`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v15`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v20`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v21`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v23`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v24`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v27`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v28`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v29`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v32`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v34`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v35`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v40`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v44`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v45`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v46`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v48`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v50`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `v58`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compkritische_lebensereignisse_uniowner` (`owner`),
  KEY `compkritische_lebensereignisse_changedate` (`changedate`),
  KEY `compkritische_lebensereignisse_createdate` (`createdate`),
  KEY `compkritische_lebensereignisse_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `complinkentry` (
  `title`       VARCHAR(255)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `description` TEXT
                COLLATE latin1_german2_ci,
  `linkurl`     VARCHAR(255)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `imageurl`    VARCHAR(255)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `mail`        VARCHAR(255)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `category`    VARCHAR(255)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`          VARCHAR(75)
                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`        VARCHAR(30)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`  DATETIME                           DEFAULT NULL,
  `changedate`  DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `complinkentry_mail` (`mail`),
  KEY `complinkentry_componenttype` (`type`),
  KEY `complinkentry_category` (`category`),
  KEY `complinkentry_createdate` (`createdate`),
  KEY `complinkentry_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmail_additionaldata` (
  `adddataname`    VARCHAR(80)          DEFAULT NULL,
  `adddatapattern` VARCHAR(80)          DEFAULT NULL,
  `atomcondition`  VARCHAR(75)          DEFAULT NULL,
  `no`             VARCHAR(75) NOT NULL DEFAULT '',
  `type`           VARCHAR(30)          DEFAULT NULL,
  `createdate`     DATETIME             DEFAULT NULL,
  `changedate`     DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_additionaldata_changedate` (`changedate`),
  KEY `compmail_additionaldata_componenttype` (`type`),
  KEY `compmail_additionaldata_atomcondition` (`atomcondition`),
  KEY `compmail_additionaldata_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_atomcondition` (
  `condname`   VARCHAR(80)          DEFAULT NULL,
  `sqltxt`     TEXT,
  `no`         VARCHAR(75) NOT NULL DEFAULT '',
  `type`       VARCHAR(30)          DEFAULT NULL,
  `createdate` DATETIME             DEFAULT NULL,
  `changedate` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_atomcondition_componenttype` (`type`),
  KEY `compmail_atomcondition_createdate` (`createdate`),
  KEY `compmail_atomcondition_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_campaign` (
  `campaignname` VARCHAR(80)          DEFAULT NULL,
  `description`  TEXT,
  `dirname`      VARCHAR(80)          DEFAULT NULL,
  `scheduletime` DATETIME             DEFAULT NULL,
  `finished`     TINYINT(3) UNSIGNED  DEFAULT NULL,
  `no`           VARCHAR(75) NOT NULL DEFAULT '',
  `type`         VARCHAR(30)          DEFAULT NULL,
  `createdate`   DATETIME             DEFAULT NULL,
  `changedate`   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_campaign_createdate` (`createdate`),
  KEY `compmail_campaign_changedate` (`changedate`),
  KEY `compmail_campaign_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_campaign_category` (
  `htmlname`   VARCHAR(80)          DEFAULT NULL,
  `picturedir` VARCHAR(80)          DEFAULT NULL,
  `campaign`   VARCHAR(75)          DEFAULT NULL,
  `category`   VARCHAR(75)          DEFAULT NULL,
  `no`         VARCHAR(75) NOT NULL DEFAULT '',
  `type`       VARCHAR(30)          DEFAULT NULL,
  `createdate` DATETIME             DEFAULT NULL,
  `changedate` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_campaign_category_changedate` (`changedate`),
  KEY `compmail_campaign_category_category` (`category`),
  KEY `compmail_campaign_category_createdate` (`createdate`),
  KEY `compmail_campaign_category_campaign` (`campaign`),
  KEY `compmail_campaign_category_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_category` (
  `categoryname` VARCHAR(80)          DEFAULT NULL,
  `description`  TEXT,
  `no`           VARCHAR(75) NOT NULL DEFAULT '',
  `type`         VARCHAR(30)          DEFAULT NULL,
  `createdate`   DATETIME             DEFAULT NULL,
  `changedate`   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_category_changedate` (`changedate`),
  KEY `compmail_category_createdate` (`createdate`),
  KEY `compmail_category_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_category_condlist` (
  `condlist`   VARCHAR(75)          DEFAULT NULL,
  `category`   VARCHAR(75)          DEFAULT NULL,
  `no`         VARCHAR(75) NOT NULL DEFAULT '',
  `type`       VARCHAR(30)          DEFAULT NULL,
  `createdate` DATETIME             DEFAULT NULL,
  `changedate` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_category_condlist_createdate` (`createdate`),
  KEY `compmail_category_condlist_componenttype` (`type`),
  KEY `compmail_category_condlist_changedate` (`changedate`),
  KEY `compmail_category_condlist_condlist` (`condlist`),
  KEY `compmail_category_condlist_category` (`category`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_conditionlist` (
  `condlistname` VARCHAR(80)          DEFAULT NULL,
  `description`  TEXT,
  `no`           VARCHAR(75) NOT NULL DEFAULT '',
  `type`         VARCHAR(30)          DEFAULT NULL,
  `createdate`   DATETIME             DEFAULT NULL,
  `changedate`   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_conditionlist_changedate` (`changedate`),
  KEY `compmail_conditionlist_createdate` (`createdate`),
  KEY `compmail_conditionlist_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_condlist_atomcond` (
  `condlist`      VARCHAR(75)          DEFAULT NULL,
  `atomcondition` VARCHAR(75)          DEFAULT NULL,
  `dataentry`     VARCHAR(75)          DEFAULT NULL,
  `no`            VARCHAR(75) NOT NULL DEFAULT '',
  `type`          VARCHAR(30)          DEFAULT NULL,
  `createdate`    DATETIME             DEFAULT NULL,
  `changedate`    DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_condlist_atomcond_condlist` (`condlist`),
  KEY `compmail_condlist_atomcond_componenttype` (`type`),
  KEY `compmail_condlist_atomcond_dataentry` (`dataentry`),
  KEY `compmail_condlist_atomcond_atomcondition` (`atomcondition`),
  KEY `compmail_condlist_atomcond_createdate` (`createdate`),
  KEY `compmail_condlist_atomcond_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmail_dataentry` (
  `additionaldata` VARCHAR(75)          DEFAULT NULL,
  `datastring`     VARCHAR(80)          DEFAULT NULL,
  `datatype`       VARCHAR(80)          DEFAULT NULL,
  `no`             VARCHAR(75) NOT NULL DEFAULT '',
  `type`           VARCHAR(30)          DEFAULT NULL,
  `createdate`     DATETIME             DEFAULT NULL,
  `changedate`     DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmail_dataentry_createdate` (`createdate`),
  KEY `compmail_dataentry_changedate` (`changedate`),
  KEY `compmail_dataentry_additionaldata` (`additionaldata`),
  KEY `compmail_dataentry_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmatch_pair` (
  `matching_search_domain_info` VARCHAR(75)          DEFAULT NULL,
  `part_1`                      VARCHAR(75)          DEFAULT NULL,
  `part_2`                      VARCHAR(75)          DEFAULT NULL,
  `match_searchdomain`          VARCHAR(50)          DEFAULT NULL,
  `already_proposed`            TINYINT(3) UNSIGNED  DEFAULT NULL,
  `matching_value`              MEDIUMINT(9)         DEFAULT NULL,
  `no`                          VARCHAR(75) NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)          DEFAULT NULL,
  `createdate`                  DATETIME             DEFAULT NULL,
  `changedate`                  DATETIME             DEFAULT NULL,
  `matching_value_sel`          MEDIUMINT(9)         DEFAULT NULL,
  `matching_value_general`      MEDIUMINT(9)         DEFAULT NULL,
  `matching_value_select`       MEDIUMINT(9)         DEFAULT NULL,
  `matching_value_match`        MEDIUMINT(9)         DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmatch_pair_matching_search_domain_info` (`matching_search_domain_info`),
  KEY `compmatch_pair_part_2` (`part_2`),
  KEY `compmatch_pair_part_1` (`part_1`),
  KEY `compmatch_pair_createdate` (`createdate`),
  KEY `compmatch_pair_changedate` (`changedate`),
  KEY `compmatch_pair_componenttype` (`type`),
  KEY `compmatch_pair_match_searchdomain_searchdomain_info` (`match_searchdomain`),
  KEY `compmatch_pair_match_searchdomain_part2` (`match_searchdomain`, `part_2`),
  KEY `compmatch_pair_match_searchdomain_part1` (`match_searchdomain`, `part_1`),
  KEY `compmatch_pair_match_searchdomain` (`match_searchdomain`),
  KEY `compmatch_pair_match_searchdomain_part1_part2` (`match_searchdomain`, `part_1`, `part_2`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmatch_pair_info` (
  `matching_search_domain_info` VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `part_info_1`                 VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `part_info_2`                 VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `part_1`                      VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `part_2`                      VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `match_searchdomain`          VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `already_proposed`            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pair_category`               VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `selected`                    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `unselect_criterias_1`        TEXT
                                COLLATE latin1_german2_ci,
  `unselect_criterias_2`        TEXT
                                COLLATE latin1_german2_ci,
  `form_distances`              TEXT
                                COLLATE latin1_german2_ci,
  `no`                          VARCHAR(75)
                                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                  DATETIME                           DEFAULT NULL,
  `changedate`                  DATETIME                           DEFAULT NULL,
  `preferencevalue`             VARCHAR(40)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `preference_values_1`         TEXT
                                COLLATE latin1_german2_ci,
  `preference_values_2`         TEXT
                                COLLATE latin1_german2_ci,
  `forced_from`                 MEDIUMINT(9)                       DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmatch_pair_info_part_1` (`part_1`),
  KEY `compmatch_pair_info_changedate` (`changedate`),
  KEY `compmatch_pair_info_part_info_2` (`part_info_2`),
  KEY `compmatch_pair_info_createdate` (`createdate`),
  KEY `compmatch_pair_info_matching_search_domain_info` (`matching_search_domain_info`),
  KEY `compmatch_pair_info_part_2` (`part_2`),
  KEY `compmatch_pair_info_part_info_1` (`part_info_1`),
  KEY `compmatch_pair_info_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmatch_participant_info` (
  `matching_search_domain_info` VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `participant`                 VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `match_searchdomain`          VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `pairs_total_count`           MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_select_count`          MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_matched_count`         MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_proposed_count`        MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_single_proposed_count` MEDIUMINT(9)                       DEFAULT NULL,
  `selection_info_xml`          TEXT
                                COLLATE latin1_german2_ci,
  `matching_info_raw_xml`       TEXT
                                COLLATE latin1_german2_ci,
  `matching_info_norm_xml`      TEXT
                                COLLATE latin1_german2_ci,
  `no`                          VARCHAR(75)
                                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                  DATETIME                           DEFAULT NULL,
  `changedate`                  DATETIME                           DEFAULT NULL,
  `selection_info_user_xml`     TEXT
                                COLLATE latin1_german2_ci,
  PRIMARY KEY (`no`),
  KEY `compmatch_participant_info_participant` (`participant`),
  KEY `compmatch_participant_info_createdate` (`createdate`),
  KEY `compmatch_participant_info_changedate` (`changedate`),
  KEY `compmatch_participant_info_matching_search_domain_info` (`matching_search_domain_info`),
  KEY `compmatch_participant_info_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmatch_searchdomain_info` (
  `match_searchdomain`          VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `begin_matching`              DATETIME                           DEFAULT NULL,
  `end_matching`                DATETIME                           DEFAULT NULL,
  `matching_status`             VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `participants_total_count`    MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_total_count`           MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_select_count`          MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_matched_count`         MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_proposed_count`        MEDIUMINT(9)                       DEFAULT NULL,
  `pairs_single_proposed_count` MEDIUMINT(9)                       DEFAULT NULL,
  `subformnames`                TEXT
                                COLLATE latin1_german2_ci,
  `defaultweights`              TEXT
                                COLLATE latin1_german2_ci,
  `parameter_config`            TEXT
                                COLLATE latin1_german2_ci,
  `no`                          VARCHAR(75)
                                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                  DATETIME                           DEFAULT NULL,
  `changedate`                  DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmatch_searchdomain_info_changedate` (`changedate`),
  KEY `compmatch_searchdomain_info_createdate` (`createdate`),
  KEY `compmatch_searchdomain_info_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmatch_select_info` (
  `matching_search_domain_info` VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `match_searchdomain`          VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `sample_total_count`          MEDIUMINT(9)                       DEFAULT NULL,
  `count_geschlecht`            MEDIUMINT(9)                       DEFAULT NULL,
  `count_alter`                 MEDIUMINT(9)                       DEFAULT NULL,
  `count_religion`              MEDIUMINT(9)                       DEFAULT NULL,
  `count_behinderung`           MEDIUMINT(9)                       DEFAULT NULL,
  `count_behinderung_auswahl`   MEDIUMINT(9)                       DEFAULT NULL,
  `count_psychisch`             MEDIUMINT(9)                       DEFAULT NULL,
  `count_psychisch_auswahl`     MEDIUMINT(9)                       DEFAULT NULL,
  `count_physisch`              MEDIUMINT(9)                       DEFAULT NULL,
  `count_physisch_auswahl`      MEDIUMINT(9)                       DEFAULT NULL,
  `count_hiv`                   MEDIUMINT(9)                       DEFAULT NULL,
  `count_hiv_auswahl`           MEDIUMINT(9)                       DEFAULT NULL,
  `count_vegan`                 MEDIUMINT(9)                       DEFAULT NULL,
  `count_vegetarisch`           MEDIUMINT(9)                       DEFAULT NULL,
  `count_region`                MEDIUMINT(9)                       DEFAULT NULL,
  `count_uebergewicht`          MEDIUMINT(9)                       DEFAULT NULL,
  `count_alleinerziehend`       MEDIUMINT(9)                       DEFAULT NULL,
  `count_platonisch`            MEDIUMINT(9)                       DEFAULT NULL,
  `count_groesse`               MEDIUMINT(9)                       DEFAULT NULL,
  `count_heirat`                MEDIUMINT(9)                       DEFAULT NULL,
  `count_kinderwunsch`          MEDIUMINT(9)                       DEFAULT NULL,
  `count_wochenende`            MEDIUMINT(9)                       DEFAULT NULL,
  `count_umzug`                 MEDIUMINT(9)                       DEFAULT NULL,
  `count_bisex`                 MEDIUMINT(9)                       DEFAULT NULL,
  `count_reise`                 MEDIUMINT(9)                       DEFAULT NULL,
  `count_gay_suche`             MEDIUMINT(9)                       DEFAULT NULL,
  `count_other`                 MEDIUMINT(9)                       DEFAULT NULL,
  `no`                          VARCHAR(75)
                                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                  DATETIME                           DEFAULT NULL,
  `changedate`                  DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmatch_select_info_matching_search_domain_info` (`matching_search_domain_info`),
  KEY `compmatch_select_info_createdate` (`createdate`),
  KEY `compmatch_select_info_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmatch_statistic_info` (
  `matching_search_domain_info` VARCHAR(75)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `statistic_group`             VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `form_name`                   VARCHAR(255)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `form_index`                  MEDIUMINT(9)                       DEFAULT NULL,
  `stat_count`                  INT(11)                            DEFAULT NULL,
  `stat_mean`                   VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_stddev`                 VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_variance`               VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_max`                    VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_min`                    VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_skew`                   VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_kurtosis`               VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `stat_median`                 VARCHAR(50)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `statistic_object`            LONGBLOB,
  `no`                          VARCHAR(75)
                                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                        VARCHAR(30)
                                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                  DATETIME                           DEFAULT NULL,
  `changedate`                  DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmatch_statistic_info_createdate` (`createdate`),
  KEY `compmatch_statistic_info_matching_search_domain_info` (`matching_search_domain_info`),
  KEY `compmatch_statistic_info_changedate` (`changedate`),
  KEY `compmatch_statistic_info_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compmedium` (
  `medium_type`   VARCHAR(50)          DEFAULT NULL,
  `owner`         VARCHAR(75)          DEFAULT NULL,
  `categories`    TEXT,
  `file_url`      VARCHAR(127)         DEFAULT NULL,
  `name`          VARCHAR(127)         DEFAULT NULL,
  `no`            VARCHAR(75) NOT NULL DEFAULT '',
  `type`          VARCHAR(30)          DEFAULT NULL,
  `createdate`    DATETIME             DEFAULT NULL,
  `changedate`    DATETIME             DEFAULT NULL,
  `searchdomain`  VARCHAR(10)          DEFAULT NULL,
  `filename`      VARCHAR(127)         DEFAULT NULL,
  `profile_image` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `extension`     VARCHAR(20)          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmedium_owner` (`owner`),
  KEY `compmedium_changedate` (`changedate`),
  KEY `compmedium_componenttype` (`type`),
  KEY `compmedium_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compmessage` (
  `sender`                        VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `recipient`                     VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sendermail`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sendername`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `recipientmail`                 VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `recipientname`                 VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sentdate`                      DATETIME                           DEFAULT NULL,
  `deliverydate`                  DATETIME                           DEFAULT NULL,
  `subject`                       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `body`                          TEXT
                                  COLLATE latin1_german2_ci,
  `priority`                      MEDIUMINT(9)                       DEFAULT NULL,
  `state`                         VARCHAR(20)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `activitystate`                 VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `hasattachments`                TINYINT(3) UNSIGNED                DEFAULT NULL,
  `errorcounter`                  MEDIUMINT(9)                       DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `writedate`                     DATETIME                           DEFAULT NULL,
  `rubrik`                        CHAR(1)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `teilnehmer`                    VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `teilnehmervorschlag`           VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `recipientvorschlag`            VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachrichtentext`               TEXT
                                  COLLATE latin1_german2_ci,
  `person_responsible`            VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `direction`                     VARCHAR(5)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `reply_message`                 VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `replied_message`               VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `messagetype`                   VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `visiblestaterecipient`         VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT 'visible',
  `visiblestatesender`            VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT 'visible',
  `kontakt_activitystate`         VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `kontakt_visiblestaterecipient` VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `kontakt_visiblestatesender`    VARCHAR(10)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compmessage_recipientvorschlag` (`recipientvorschlag`),
  KEY `compmessage_recipient` (`recipient`),
  KEY `compmessage_teilnehmervorschlag` (`teilnehmervorschlag`),
  KEY `compmessage_priority_state` (`state`, `priority`),
  KEY `compmessage_sender` (`sender`),
  KEY `compmessage_teilnehmer` (`teilnehmer`),
  KEY `compmessage_sentdate` (`sentdate`),
  KEY `compmessage_person_responsible` (`person_responsible`),
  KEY `compmessage_sentdate_direction_state` (`direction`, `sentdate`, `state`),
  KEY `compmessage_reply_message` (`reply_message`),
  KEY `compmessage_sentdate_direction` (`direction`, `sentdate`),
  KEY `compmessage_replied_message` (`replied_message`),
  KEY `compmessage_sendermail` (`sendermail`),
  KEY `compmessage_recipientmail` (`recipientmail`),
  KEY `compmessage_createdate` (`createdate`),
  KEY `compmessage_changedate` (`changedate`),
  KEY `compmessage_componenttype` (`type`),
  KEY `compmessage_messagetype` (`messagetype`),
  KEY `compmessage_deliverydate_direction` (`direction`, `deliverydate`),
  KEY `compmessage_deliverydate` (`deliverydate`),
  KEY `compmessage_deliverydate_direction_state` (`direction`, `state`, `deliverydate`),
  KEY `compmessage_deliverydate_direction_state_sendername` (`sendername`, `direction`, `state`, `deliverydate`, `sentdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compnews` (
  `news_title`    VARCHAR(100)         DEFAULT NULL,
  `news_from`     DATETIME             DEFAULT NULL,
  `news_to`       DATETIME             DEFAULT NULL,
  `news_valid`    DATETIME             DEFAULT NULL,
  `news_text`     TEXT,
  `news_locale`   VARCHAR(50)          DEFAULT NULL,
  `news_inactive` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `no`            VARCHAR(75) NOT NULL DEFAULT '',
  `type`          VARCHAR(30)          DEFAULT NULL,
  `createdate`    DATETIME             DEFAULT NULL,
  `changedate`    DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compnews_changedate` (`changedate`),
  KEY `compnews_createdate` (`createdate`),
  KEY `compnews_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compnewsletterrecipient` (
  `mail`       VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compnewsletterrecipient_mail` (`mail`),
  KEY `compnewsletterrecipient_changedate` (`changedate`),
  KEY `compnewsletterrecipient_createdate` (`createdate`),
  KEY `compnewsletterrecipient_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compnewsread` (
  `owner`       VARCHAR(75)          DEFAULT NULL,
  `news_number` VARCHAR(75)          DEFAULT NULL,
  `no`          VARCHAR(75) NOT NULL DEFAULT '',
  `type`        VARCHAR(30)          DEFAULT NULL,
  `createdate`  DATETIME             DEFAULT NULL,
  `changedate`  DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compnewsread_news_number` (`news_number`),
  KEY `compnewsread_changedate` (`changedate`),
  KEY `compnewsread_owner` (`owner`),
  KEY `compnewsread_componenttype` (`type`),
  KEY `compnewsread_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `comppartner` (
  `owner`                    VARCHAR(75)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_hiv_select`             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_behinderten_select`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_koerperkrank_select`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_psychischkrank_select`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_religion_select`        MEDIUMINT(9)                       DEFAULT NULL,
  `p_minalter`               MEDIUMINT(9)                       DEFAULT NULL,
  `p_maxalter`               MEDIUMINT(9)                       DEFAULT NULL,
  `p_region`                 TEXT
                             COLLATE latin1_german2_ci,
  `p_plz_1`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_radius_1`               MEDIUMINT(9)                       DEFAULT NULL,
  `p_plz_2`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_radius_2`               MEDIUMINT(9)                       DEFAULT NULL,
  `p_plz_3`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_radius_3`               MEDIUMINT(9)                       DEFAULT NULL,
  `heiraten`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `kinderwunsch`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `umzug`                    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `wochenende`               VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_raucher_akzeptieren`    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_gewicht_akzeptieren`    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bisex`                  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_hiv`                    VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_behinderung`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_erkrankung_koer`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_erkrankung_psy`         VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bisex_suche`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_vege_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_vegan_suche`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                       VARCHAR(75)
                             COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                     VARCHAR(30)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`               DATETIME                           DEFAULT NULL,
  `changedate`               DATETIME                           DEFAULT NULL,
  `p_mingroesse`             MEDIUMINT(9)                       DEFAULT NULL,
  `p_maxgroesse`             MEDIUMINT(9)                       DEFAULT NULL,
  `p_platonisch_suche`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_platonisch_mitteil`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_alleinerziehend_select` VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_akz_fem_mask`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_prefalter`              MEDIUMINT(9)                       DEFAULT NULL,
  `p_prefgroesse`            MEDIUMINT(9)                       DEFAULT NULL,
  `p_land_1`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_land_2`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_land_3`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_hu`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_ka`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_na`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_pf`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_vo`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_fi`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_re`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierakz_in`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_ka`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_ch`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_mu`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_ju`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_bu`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_hi`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_sh`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_na`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relakz_at`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_akad_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_oeko_praef`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_eso_praef`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fwunsch_fussball`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fwunsch_motorsport`     VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fwunsch_auto`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fwunsch_motorrad`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fwunsch_computer`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fakz_fussball`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fakz_motorsport`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fakz_auto`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fakz_motorrad`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fakz_computer`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_nur_foto`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_kuenstler_suche`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_transsex`               VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_intersex`               VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_transsex_suche`         VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_intersex_suche`         VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_behinderung_suche`      VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_erkrankung_koer_suche`  VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_erkrankung_psy_suche`   VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_transsex_only`          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_bdsm_suche`             VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bdsm_mitteil`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_hiv_suche`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_ch`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_is`           VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_jud`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_bud`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_hin`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_schin`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_eso`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_off`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_relsearch_agn`          VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_sexdev`                 VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_tierrechte_praef`       VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_dreierbez`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_transvestit`            VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_gewicht_ausschluss`     TEXT
                             COLLATE latin1_german2_ci,
  `p_relsearch_bahai`        VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_fkk_praef`              VARCHAR(255)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_gewicht_wunsch`         TEXT
                             COLLATE latin1_german2_ci,
  `p_sexfunc`                VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_sexfunc_suche`          VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bdsm_nur`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_hochsensibel`           VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_erotik_mitteil`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p_erotik`                 VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bdsm_suche_allg`        VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_bdsm_suche_spezial`     VARCHAR(50)
                             COLLATE latin1_german2_ci          DEFAULT NULL,
  `p_region_umzug`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `comppartner_uniowner` (`owner`),
  KEY `comppartner_createdate` (`createdate`),
  KEY `comppartner_changedate` (`changedate`),
  KEY `comppartner_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppartnerschaft` (
  `p1_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p19_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p18_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_6`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p1_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_1`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p11_6`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_6`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_2`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p7_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_6`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_7`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p15_7`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_7`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p30_3`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_7`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p20_7`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p2_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p5_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p6_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_4`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_8`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_8`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p16_8`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p17_8`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p8_9`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p31_5`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p9_9`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p13_9`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p14_9`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_10`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_10`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p32_6`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_11`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p10_11`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `p12_12`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `comppartnerschaft_uniowner` (`owner`),
  KEY `comppartnerschaft_changedate` (`changedate`),
  KEY `comppartnerschaft_createdate` (`createdate`),
  KEY `comppartnerschaft_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_bill` (
  `participant_id` VARCHAR(75)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `transaction_id` VARCHAR(75)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `bill`           BLOB,
  `no`             VARCHAR(75)
                   COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`           VARCHAR(30)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`     DATETIME                           DEFAULT NULL,
  `changedate`     DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_bill_transaction_id` (`transaction_id`),
  KEY `comppayment_bill_participant_id` (`participant_id`),
  KEY `comppayment_bill_createdate` (`createdate`),
  KEY `comppayment_bill_changedate` (`changedate`),
  KEY `comppayment_bill_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_break` (
  `break_owner`    VARCHAR(75)          DEFAULT NULL,
  `break_category` VARCHAR(50)          DEFAULT NULL,
  `break_start`    DATETIME             DEFAULT NULL,
  `break_end`      DATETIME             DEFAULT NULL,
  `break_until`    DATETIME             DEFAULT NULL,
  `break_active`   TINYINT(3) UNSIGNED  DEFAULT NULL,
  `no`             VARCHAR(75) NOT NULL DEFAULT '',
  `type`           VARCHAR(30)          DEFAULT NULL,
  `createdate`     DATETIME             DEFAULT NULL,
  `changedate`     DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_break_createdate` (`createdate`),
  KEY `comppayment_break_break_owner` (`break_owner`),
  KEY `comppayment_break_changedate` (`changedate`),
  KEY `comppayment_break_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `comppayment_event` (
  `transaction_id` VARCHAR(75)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `eventmethod`    CHAR(2)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `eventtype`      CHAR(2)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `eventdate`      DATETIME                           DEFAULT NULL,
  `uniqueid`       VARCHAR(32)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `referenceid`    VARCHAR(32)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `result_ok`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `return_code`    VARCHAR(64)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `return_text`    VARCHAR(255)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `event_xml`      TEXT
                   COLLATE latin1_german2_ci,
  `no`             VARCHAR(75)
                   COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`           VARCHAR(30)
                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`     DATETIME                           DEFAULT NULL,
  `changedate`     DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_event_transaction_id` (`transaction_id`),
  KEY `comppayment_event_changedate` (`changedate`),
  KEY `comppayment_event_createdate` (`createdate`),
  KEY `comppayment_event_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_offer` (
  `offer_name`                      VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_amount`                    VARCHAR(30)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_currency`                  CHAR(3)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_description`               TEXT
                                    COLLATE latin1_german2_ci,
  `offer_duration`                  MEDIUMINT(9)                       DEFAULT NULL,
  `offer_duration_unit`             VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_installment`               MEDIUMINT(9)                       DEFAULT NULL,
  `offer_searchdomains`             TEXT
                                    COLLATE latin1_german2_ci,
  `offer_payment_types`             TEXT
                                    COLLATE latin1_german2_ci,
  `offer_valid_begin`               DATETIME                           DEFAULT NULL,
  `offer_valid_end`                 DATETIME                           DEFAULT NULL,
  `assumed_searchdomains`           TEXT
                                    COLLATE latin1_german2_ci,
  `excluded_searchdomains`          TEXT
                                    COLLATE latin1_german2_ci,
  `assumed_forms`                   TEXT
                                    COLLATE latin1_german2_ci,
  `excluded_forms`                  TEXT
                                    COLLATE latin1_german2_ci,
  `offer_automatic_renewal`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `renewal_offer_name`              VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `actionnumber`                    VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `counselling`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `no`                              VARCHAR(75)
                                    COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                            VARCHAR(30)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                      DATETIME                           DEFAULT NULL,
  `changedate`                      DATETIME                           DEFAULT NULL,
  `is_renewal_offer`                TINYINT(3) UNSIGNED                DEFAULT '0',
  `offer_installment_duration`      MEDIUMINT(9)                       DEFAULT NULL,
  `offer_installment_duration_unit` VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `is_sozialtarif`                  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `offergroup`                      VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `is_ermaessigungstarif`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `offer_prolongation_amount`       MEDIUMINT(9)                       DEFAULT NULL,
  `offer_prolongation_unit`         VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_renewalcode_source`        VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_renewalcode_destination`   VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_isvaramount`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `offer_minamount`                 VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_maxamount`                 VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_diffamount`                VARCHAR(255)
                                    COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_offer_createdate` (`createdate`),
  KEY `comppayment_offer_changedate` (`changedate`),
  KEY `comppayment_offer_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_rate` (
  `transaction_id`  VARCHAR(75)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `sequenceno`      MEDIUMINT(9)                       DEFAULT NULL,
  `amount`          VARCHAR(32)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `eventmethod`     CHAR(2)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `paymentdate`     DATETIME                           DEFAULT NULL,
  `raten_state`     VARCHAR(255)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `payevent`        VARCHAR(75)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`              VARCHAR(75)
                    COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`            VARCHAR(30)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`      DATETIME                           DEFAULT NULL,
  `changedate`      DATETIME                           DEFAULT NULL,
  `reference_id`    VARCHAR(255)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `planneddate`     DATETIME                           DEFAULT NULL,
  `lasteventdate`   DATETIME                           DEFAULT NULL,
  `chargebackevent` VARCHAR(75)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_rate_payevent` (`payevent`),
  KEY `comppayment_rate_transaction_id` (`transaction_id`),
  KEY `comppayment_rate_createdate` (`createdate`),
  KEY `comppayment_rate_changedate` (`changedate`),
  KEY `comppayment_rate_componenttype` (`type`),
  KEY `comppayment_rate_chargebackevent` (`chargebackevent`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_transaction` (
  `participant_id`                          VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `startdate`                               DATETIME                           DEFAULT NULL,
  `lasteventdate`                           DATETIME                           DEFAULT NULL,
  `protocolstate`                           VARCHAR(50)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `method`                                  CHAR(2)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `subscription_begin`                      DATETIME                           DEFAULT NULL,
  `subscription_end`                        DATETIME                           DEFAULT NULL,
  `automatic_renewal`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `registrationid`                          VARCHAR(32)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `amount`                                  VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `paid_searchdomains`                      TEXT
                                            COLLATE latin1_german2_ci,
  `customer_ip`                             VARCHAR(15)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `identification_transactionid`            VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `identification_referenceid`              VARCHAR(32)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `identification_shortid`                  VARCHAR(32)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_holder`                VARCHAR(128)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_number`                VARCHAR(64)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_bank`                  VARCHAR(12)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_iban`                  VARCHAR(28)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_bic`                   VARCHAR(11)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_country`               CHAR(2)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `offer_name`                              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `renewal_transaction`                     VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `bill_sent`                               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `renewal_notification_sent`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `chargeback_reminder_sent`                TINYINT(3) UNSIGNED                DEFAULT NULL,
  `prepayment_reminder_count`               MEDIUMINT(9)                       DEFAULT NULL,
  `actionnumber`                            VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `counselling`                             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `counselling_mail`                        VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `counselling_customer_firstname`          VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `counselling_customer_lastname`           VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                                      VARCHAR(75)
                                            COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                                    VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                              DATETIME                           DEFAULT NULL,
  `changedate`                              DATETIME                           DEFAULT NULL,
  `superclix_id`                            VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `do_refund`                               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_installment`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `count_installments`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `paid_installments`                       MEDIUMINT(9)                       DEFAULT NULL,
  `installment_time`                        DATETIME                           DEFAULT NULL,
  `installment_first_reference_id`          VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `connector_account_bankname`              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `renewal_notification_time`               DATETIME                           DEFAULT NULL,
  `chargeback_reminder_time`                DATETIME                           DEFAULT NULL,
  `prepayment_reminder_time`                DATETIME                           DEFAULT NULL,
  `user_blacklisted`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `installment_duration`                    MEDIUMINT(9)                       DEFAULT NULL,
  `installment_duration_unit`               VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `userinfo_id`                             VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `disabled_renewal_notification_sent`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `disabled_renewal_notification_time`      DATETIME                           DEFAULT NULL,
  `chargeback_reminder_count`               MEDIUMINT(9)                       DEFAULT NULL,
  `disabled_renewal_notification_timestamp` VARCHAR(40)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `disable_unpaid_pp`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `social_rate_text`                        TEXT
                                            COLLATE latin1_german2_ci,
  `is_affiliate`                            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pay_duration`                            MEDIUMINT(9)                       DEFAULT NULL,
  `pay_duration_unit`                       VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `superclix_provision_paid`                TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_renewal`                              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `origin_amount`                           VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `chargeback_reason`                       VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `fixed_startdate`                         DATETIME                           DEFAULT NULL,
  `registration_available`                  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `registration_refresh_date`               DATETIME                           DEFAULT NULL,
  `tarif_type`                              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `disable_cb_handling`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `amount_currency`                         VARCHAR(8)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `reminder_mode`                           VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `reminder_count`                          MEDIUMINT(9)                       DEFAULT NULL,
  `reminder_last_time`                      DATETIME                           DEFAULT NULL,
  `reminder_next_time`                      DATETIME                           DEFAULT NULL,
  `cb_orig_transaction`                     VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `refunded`                                TINYINT(3) UNSIGNED                DEFAULT NULL,
  `channelid`                               VARCHAR(32)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `donation_considered`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `adcell_id`                               VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `adcell_provision_paid`                   TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_transaction_renewal_transaction` (`renewal_transaction`),
  KEY `comppayment_transaction_participant_id` (`participant_id`),
  KEY `comppayment_transaction_createdate` (`createdate`),
  KEY `comppayment_transaction_changedate` (`changedate`),
  KEY `comppayment_transaction_userinfo_id` (`userinfo_id`),
  KEY `comppayment_transaction_componenttype` (`type`),
  KEY `comppayment_transaction_cb_orig_transaction` (`cb_orig_transaction`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppayment_userinfo` (
  `participant_id`   VARCHAR(75)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `is_registered`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `eventmethod`      CHAR(2)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `registrationdate` DATETIME                           DEFAULT NULL,
  `registrationid`   VARCHAR(32)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `blacklisted`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `payevent`         VARCHAR(75)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`               VARCHAR(75)
                     COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`             VARCHAR(30)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`       DATETIME                           DEFAULT NULL,
  `changedate`       DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `comppayment_userinfo_participant_id` (`participant_id`),
  KEY `comppayment_userinfo_changedate` (`changedate`),
  KEY `comppayment_userinfo_payevent` (`payevent`),
  KEY `comppayment_userinfo_createdate` (`createdate`),
  KEY `comppayment_userinfo_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppersoenlichkeit` (
  `pk_10_a`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_17_i`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_19_v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_20_f`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_22_e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_23_b`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_24_g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_27_p`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_28_l`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_29_s`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_30_be`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_33se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_31_ae`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_32_r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_33_d`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_34_sb`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_35_im`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_36_ve`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_37_h`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_38_ge`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_39_du`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_41_er`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_43_fa`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_44_aes`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_45_gf`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_46_ha`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_47_i`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_48_wn`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_51_al`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_52_e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_53_b`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_54_g`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_55_k`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_57_p`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_58_l`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_59_s`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_60_be`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_1se_se`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_3ge_ga`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_10_ma_md` TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_15na_na`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_20ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_34se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_61_ae`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_62_r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_64_sb`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_65_im`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_66_ve`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_67_h`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_68_ge`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_69_du`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_71_er`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_72_pe`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_73_fa`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_75_gf`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_76_hg`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_77_i`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_79_v`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_81_al`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_82_e`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_85_k`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_86_ol`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_87_p`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_88_l`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_90_be`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_2se_se`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_4ge_ga`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_11ma_md`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_16na_na`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk21_ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_91_ae`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_92_r`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_94_sb`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_95_im`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_96_ve`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_98_ge`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_99_du`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_100_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_101_er`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_104_aes`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_105_gf`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_107_i`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_108_wn`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_110_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_111_al`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_112_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_113_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_115_k`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_117_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_119_s`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_120_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_12ma_md`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_17na_na`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_22ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_26se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_121_ae`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_123_d`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_124_sb`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_125_im`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_130_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_132_pe`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_133_fa`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_134_aes`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_136_hg`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_137_i`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_139_v`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_140_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_141_al`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_142_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_145_k`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_146_ol`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_147_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_148_l`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_150_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_6_ge_ga`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_13ma_md`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_18na_na`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_23ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_27se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_151_ae`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_152_r`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_153_d`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_156_ve`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_157_h`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_158_ge`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_159_du`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_160_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_161_er`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_162_pe`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_163_fa`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_164_aes`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_166_hg`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_168_wn`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_169_v`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_172_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_173_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_174_g`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_175_k`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_177_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_178_l`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_180_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_7_ge_ga`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_14_ma_md` TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_24ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_28se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_182_r`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_183_d`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_184_sb`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_185_im`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_186_ve`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_187_h`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_189_du`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_190_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_191_er`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_192_pe`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_194_aes`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_195_gf`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_196_hg`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_200_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_202_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_203_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_204_g`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_205_k`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_206_ol`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_207_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_209_s`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_210_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_8_ge_ga`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_25ri_ri`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_29se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_211_ae`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_212_r`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_215_im`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_216_ve`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_217_h`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_218_ge`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_219_du`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_220_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_223_fa`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_224_aes`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_227_i`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_228_wn`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_229_v`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_230_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_231_al`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_232_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_233_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_234_g`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_236_ol`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_237_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_240_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_9_ge_ga`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_30se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_243_d`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_244_sb`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_245_im`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_246_ve`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_247_h`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_250_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_251_er`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_252_pe`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_255_gf`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_256_hg`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_257_i`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_258_wn`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_260_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_261_al`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_262_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_263_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_264_g`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_266_ol`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_267_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_269_s`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_270_be`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_31se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_273_d`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_276_ve`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_278_ge`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_280_a`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_282_pe`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_285_gf`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_288_wn`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_289_v`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_290_f`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_292_e`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_293_b`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_294_g`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_297_p`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_298_l`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_299_s`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `pk_32se_se`  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`       VARCHAR(75)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`          VARCHAR(75)
                COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`        VARCHAR(30)
                COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`  DATETIME                           DEFAULT NULL,
  `changedate`  DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `comppersoenlichkeit_uniowner` (`owner`),
  KEY `comppersoenlichkeit_changedate` (`changedate`),
  KEY `comppersoenlichkeit_createdate` (`createdate`),
  KEY `comppersoenlichkeit_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compperson` (
  `schulabschluss`         VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `berufsausbildung`       TEXT
                           COLLATE latin1_german2_ci,
  `beruflicher_status`     TEXT
                           COLLATE latin1_german2_ci,
  `berufsfeld`             TEXT
                           COLLATE latin1_german2_ci,
  `berufsfeld_sonst`       VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `ausbildung_bezeichnung` VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `studium_bezeichnung`    VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `berufsbezeichnung`      VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `kinder`                 MEDIUMINT(9)                       DEFAULT NULL,
  `tiere`                  TEXT
                           COLLATE latin1_german2_ci,
  `rauchen`                VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `alkoholkonsum`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `schlafrhythmus`         VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `ordnungsliebe`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `vegetar_ernaehrung`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `vegane_ernaehrung`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `hiv_info`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `behinderten_info`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `koerperkrank_info`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `psychisch_krank_info`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`                  VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                     VARCHAR(75)
                           COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                   VARCHAR(30)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`             DATETIME                           DEFAULT NULL,
  `changedate`             DATETIME                           DEFAULT NULL,
  `alleinerziehend`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `in_partnerschaft`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `kinder_details`         VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `kinder_haushalt`        VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `religion_info`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `kuenstler`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `relprev_ch`             VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_is`             VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_jud`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_bud`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_hin`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_schin`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_eso`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_off`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_agn`            VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `rel_mitteil`            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `transvestit`            TINYINT(3) UNSIGNED                DEFAULT NULL,
  `vege_info`              VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `vegan_info`             VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `relprev_bahai`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `familienstand`          VARCHAR(255)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `hochsensibel_info`      VARCHAR(50)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `hochsensibel_mitteil`   TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compperson_uniowner` (`owner`),
  KEY `compperson_changedate` (`changedate`),
  KEY `compperson_createdate` (`createdate`),
  KEY `compperson_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `comppfoto` (
  `foto1`      MEDIUMBLOB,
  `foto2`      MEDIUMBLOB,
  `foto3`      MEDIUMBLOB,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  `hide_fotos` TINYINT(3) UNSIGNED                DEFAULT '0',
  PRIMARY KEY (`no`),
  UNIQUE KEY `comppfoto_uniowner` (`owner`),
  KEY `comppfoto_changedate` (`changedate`),
  KEY `comppfoto_createdate` (`createdate`),
  KEY `comppfoto_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compphonenumber` (
  `phonenumber_provider`         VARCHAR(128)         DEFAULT NULL,
  `phonenumber_delete_allowed`   TINYINT(3) UNSIGNED  DEFAULT NULL,
  `phonenumber_change_allowed`   TINYINT(3) UNSIGNED  DEFAULT NULL,
  `phonenumber_begin`            DATETIME             DEFAULT NULL,
  `phonenumber_end`              DATETIME             DEFAULT NULL,
  `phonenumber_dest_number`      VARCHAR(255)         DEFAULT NULL,
  `phonenumber_dest_countrycode` VARCHAR(255)         DEFAULT NULL,
  `phonenumber_duration_min`     INT(11)              DEFAULT NULL,
  `phonenumber_duration_code`    VARCHAR(255)         DEFAULT NULL,
  `phonenumber_active_state`     TINYINT(3) UNSIGNED  DEFAULT NULL,
  `phonenumber_costs_cent`       MEDIUMINT(9)         DEFAULT NULL,
  `phonenumber_full_num_nat`     VARCHAR(50)          DEFAULT NULL,
  `phonenumber_full_num_int`     VARCHAR(50)          DEFAULT NULL,
  `phonenumber_gen_countrycode`  MEDIUMINT(9)         DEFAULT NULL,
  `phonenumber_gen_ndc`          MEDIUMINT(9)         DEFAULT NULL,
  `phonenumber_gen_sn`           MEDIUMINT(9)         DEFAULT NULL,
  `phonenumber_gen_ddi`          MEDIUMINT(9)         DEFAULT NULL,
  `phonenumber_gen_iptn`         VARCHAR(128)         DEFAULT NULL,
  `phonenumber_owner`            VARCHAR(75)          DEFAULT NULL,
  `phonenumber_match`            VARCHAR(75)          DEFAULT NULL,
  `no`                           VARCHAR(75) NOT NULL DEFAULT '',
  `type`                         VARCHAR(30)          DEFAULT NULL,
  `createdate`                   DATETIME             DEFAULT NULL,
  `changedate`                   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compphonenumber_createdate` (`createdate`),
  KEY `compphonenumber_componenttype` (`type`),
  KEY `compphonenumber_changedate` (`changedate`),
  KEY `compphonenumber_phonenumber_match` (`phonenumber_match`),
  KEY `compphonenumber_phonenumber_owner` (`phonenumber_owner`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compplzlist` (
  `plz_staat`                      VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_bundesland`                 VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_regierungsbezirk`           VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_landkreis`                  VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_verwaltungszusammenschluss` VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_ort`                        VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_lon`                        VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_lat`                        VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz_plz`                        VARCHAR(255)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                             VARCHAR(75)
                                   COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                           VARCHAR(30)
                                   COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                     DATETIME                           DEFAULT NULL,
  `changedate`                     DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compplzlist_createdate` (`createdate`),
  KEY `compplzlist_changedate` (`changedate`),
  KEY `compplzlist_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compproband_socio_economics` (
  `schulabschluss`                VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `in_partnerschaft`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `partnerschaft_dauer`           MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_anzahl`        MEDIUMINT(9)                       DEFAULT NULL,
  `partnerschaften_zufriedenheit` VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenwaertige_beziehung`       VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `sexuelle_orientierung`         VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `freunde_anzahl`                MEDIUMINT(9)                       DEFAULT NULL,
  `freundschaften_zufriedenheit`  VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `owner`                         VARCHAR(75)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `mail`                          VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                     VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                    DATE                               DEFAULT NULL,
  `geschlecht`                    VARCHAR(255)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                            VARCHAR(75)
                                  COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                          VARCHAR(30)
                                  COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                    DATETIME                           DEFAULT NULL,
  `changedate`                    DATETIME                           DEFAULT NULL,
  `fertig`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compproband_socio_economics_uniowner` (`owner`),
  KEY `compproband_socio_economics_changedate` (`changedate`),
  KEY `compproband_socio_economics_componenttype` (`type`),
  KEY `compproband_socio_economics_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compptext` (
  `freiertext` TEXT
               COLLATE latin1_german2_ci,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  `frage1`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort1`   TEXT
               COLLATE latin1_german2_ci,
  `frage2`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort2`   TEXT
               COLLATE latin1_german2_ci,
  `frage3`     VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `antwort3`   TEXT
               COLLATE latin1_german2_ci,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compptext_uniowner` (`owner`),
  KEY `compptext_createdate` (`createdate`),
  KEY `compptext_changedate` (`changedate`),
  KEY `compptext_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compregion_addresslocation` (
  `adrloc_street`                VARCHAR(128)         DEFAULT NULL,
  `adrloc_hnumber`               VARCHAR(10)          DEFAULT NULL,
  `adrloc_city`                  VARCHAR(128)         DEFAULT NULL,
  `adrloc_zip`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_continent`             VARCHAR(20)          DEFAULT NULL,
  `adrloc_country`               VARCHAR(20)          DEFAULT NULL,
  `adrloc_state`                 VARCHAR(20)          DEFAULT NULL,
  `adrloc_nearpublicloc`         VARCHAR(128)         DEFAULT NULL,
  `adrloc_nearwkloc`             VARCHAR(128)         DEFAULT NULL,
  `adrloc_lon`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_lat`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_geovalid`              TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_geoqualtity`           TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_geotimestamp`          DATETIME             DEFAULT NULL,
  `adrloc_geoprovider`           VARCHAR(40)          DEFAULT NULL,
  `adrloc_isparticipantlocation` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_isinternational`       TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_usecontinent`          TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_zipcountry`            TINYINT(3) UNSIGNED  DEFAULT NULL,
  `owner`                        VARCHAR(75)          DEFAULT NULL,
  `no`                           VARCHAR(75) NOT NULL DEFAULT '',
  `type`                         VARCHAR(30)          DEFAULT NULL,
  `createdate`                   DATETIME             DEFAULT NULL,
  `changedate`                   DATETIME             DEFAULT NULL,
  `adrloc_ismain`                TINYINT(3) UNSIGNED  DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_addresslocation_owner` (`owner`),
  KEY `compregion_addresslocation_componenttype` (`type`),
  KEY `compregion_addresslocation_changedate` (`changedate`),
  KEY `compregion_addresslocation_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compregion_adresslocation` (
  `adrloc_street`                VARCHAR(128)         DEFAULT NULL,
  `adrloc_hnumber`               VARCHAR(10)          DEFAULT NULL,
  `adrloc_city`                  VARCHAR(128)         DEFAULT NULL,
  `adrloc_zip`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_continent`             VARCHAR(20)          DEFAULT NULL,
  `adrloc_country`               VARCHAR(20)          DEFAULT NULL,
  `adrloc_state`                 VARCHAR(20)          DEFAULT NULL,
  `adrloc_nearpublicloc`         VARCHAR(128)         DEFAULT NULL,
  `adrloc_nearwkloc`             VARCHAR(128)         DEFAULT NULL,
  `adrloc_lon`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_lat`                   VARCHAR(20)          DEFAULT NULL,
  `adrloc_geovalid`              TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_geoqualtity`           TINYINT(3) UNSIGNED  DEFAULT NULL,
  `adrloc_geotimestamp`          DATETIME             DEFAULT NULL,
  `adrloc_isparticipantlocation` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `owner`                        VARCHAR(75)          DEFAULT NULL,
  `no`                           VARCHAR(75) NOT NULL DEFAULT '',
  `type`                         VARCHAR(30)          DEFAULT NULL,
  `createdate`                   DATETIME             DEFAULT NULL,
  `changedate`                   DATETIME             DEFAULT NULL,
  `adrloc_isinternational`       TINYINT(3) UNSIGNED  DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_adresslocation_changedate` (`changedate`),
  KEY `compregion_adresslocation_componenttype` (`type`),
  KEY `compregion_adresslocation_createdate` (`createdate`),
  KEY `compregion_adresslocation_owner` (`owner`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compregion_assignment` (
  `reg_active`  TINYINT(3) UNSIGNED  DEFAULT NULL,
  `searchquest` VARCHAR(75)          DEFAULT NULL,
  `searchrestr` VARCHAR(75)          DEFAULT NULL,
  `owner`       VARCHAR(75)          DEFAULT NULL,
  `no`          VARCHAR(75) NOT NULL DEFAULT '',
  `type`        VARCHAR(30)          DEFAULT NULL,
  `createdate`  DATETIME             DEFAULT NULL,
  `changedate`  DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_assignment_searchrestr` (`searchrestr`),
  KEY `compregion_assignment_owner` (`owner`),
  KEY `compregion_assignment_createdate` (`createdate`),
  KEY `compregion_assignment_searchquest` (`searchquest`),
  KEY `compregion_assignment_componenttype` (`type`),
  KEY `compregion_assignment_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compregion_basesearch` (
  `reg_active`          TINYINT(3) UNSIGNED  DEFAULT NULL,
  `reg_paripheryactive` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `reg_searchdomain`    VARCHAR(50)          DEFAULT NULL,
  `owner`               VARCHAR(75)          DEFAULT NULL,
  `no`                  VARCHAR(75) NOT NULL DEFAULT '',
  `type`                VARCHAR(30)          DEFAULT NULL,
  `createdate`          DATETIME             DEFAULT NULL,
  `changedate`          DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_basesearch_componenttype` (`type`),
  KEY `compregion_basesearch_changedate` (`changedate`),
  KEY `compregion_basesearch_owner` (`owner`),
  KEY `compregion_basesearch_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compregion_geonameszip` (
  `geonames_country`   VARCHAR(4)           DEFAULT NULL,
  `geonames_zip`       VARCHAR(20)          DEFAULT NULL,
  `geonames_latitude`  VARCHAR(20)          DEFAULT NULL,
  `geonames_longitude` VARCHAR(20)          DEFAULT NULL,
  `geonames_accuracy`  TINYINT(3) UNSIGNED  DEFAULT NULL,
  `no`                 VARCHAR(75) NOT NULL DEFAULT '',
  `type`               VARCHAR(30)          DEFAULT NULL,
  `createdate`         DATETIME             DEFAULT NULL,
  `changedate`         DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_geonameszip_createdate` (`createdate`),
  KEY `compregion_geonameszip_componenttype` (`type`),
  KEY `compregion_geonameszip_country_zip` (`geonames_zip`, `geonames_country`),
  KEY `compregion_geonameszip_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compregion_searchrestr` (
  `reg_notice`         TEXT,
  `owner`              VARCHAR(75)          DEFAULT NULL,
  `no`                 VARCHAR(75) NOT NULL DEFAULT '',
  `type`               VARCHAR(30)          DEFAULT NULL,
  `createdate`         DATETIME             DEFAULT NULL,
  `changedate`         DATETIME             DEFAULT NULL,
  `reg_continent`      VARCHAR(20)          DEFAULT NULL,
  `reg_countrylist`    TEXT,
  `reg_country`        VARCHAR(20)          DEFAULT NULL,
  `reg_statelist`      TEXT,
  `reg_addrloc`        VARCHAR(75)          DEFAULT NULL,
  `reg_radius`         MEDIUMINT(9)         DEFAULT NULL,
  `reg_base`           VARCHAR(75)          DEFAULT NULL,
  `reg_code`           VARCHAR(20)          DEFAULT NULL,
  `parent`             VARCHAR(75)          DEFAULT NULL,
  `reg_over_frontiers` TINYINT(3) UNSIGNED  DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compregion_searchrestr_owner` (`owner`),
  KEY `compregion_searchrestr_createdate` (`createdate`),
  KEY `compregion_searchrestr_reg_addrloc` (`reg_addrloc`),
  KEY `compregion_searchrestr_changedate` (`changedate`),
  KEY `compregion_searchrestr_componenttype` (`type`),
  KEY `compregion_searchrestr_reg_base` (`reg_base`),
  KEY `compregion_searchrestr_parent` (`parent`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compstoerbar` (
  `st_1`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_2`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_3`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_4`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_5`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_6`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_7`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_8`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_9`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_10`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_11`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_12`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_13`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_14`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_15`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_16`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_17`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_18`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_19`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_20`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_21`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_22`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_23`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_24`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_25`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_26`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_27`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_28`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_29`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_30`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_31`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_32`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_33`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_34`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_35`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_36`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_37`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_38`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_39`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_40`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_41`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_42`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_43`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_44`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_45`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_46`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_47`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_48`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_49`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_50`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_51`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_52`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_53`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_54`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_55`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_56`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_57`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_58`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_59`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_60`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_61`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_62`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_63`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_64`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_65`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_66`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_67`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_68`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_69`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_70`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_71`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_72`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_73`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_74`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_75`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_76`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_77`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_78`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_79`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_80`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_81`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_82`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_83`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_84`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_85`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_86`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_87`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_88`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_89`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_90`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_91`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_92`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_93`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_94`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_95`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_96`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_97`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_98`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_99`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_100`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_101`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_102`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_103`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_104`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_105`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_106`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_107`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_108`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_109`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_110`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_111`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `st_112`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`      VARCHAR(75)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compstoerbar_uniowner` (`owner`),
  KEY `compstoerbar_createdate` (`createdate`),
  KEY `compstoerbar_componenttype` (`type`),
  KEY `compstoerbar_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compsymbol` (
  `symbol_url`  VARCHAR(127)         DEFAULT NULL,
  `symbol_name` VARCHAR(127)         DEFAULT NULL,
  `symbol_seen` TINYINT(3) UNSIGNED  DEFAULT NULL,
  `sender_id`   VARCHAR(75)          DEFAULT NULL,
  `receiver_id` VARCHAR(75)          DEFAULT NULL,
  `no`          VARCHAR(75) NOT NULL DEFAULT '',
  `type`        VARCHAR(30)          DEFAULT NULL,
  `createdate`  DATETIME             DEFAULT NULL,
  `changedate`  DATETIME             DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compsymbol_receiver_id` (`receiver_id`),
  KEY `compsymbol_changedate` (`changedate`),
  KEY `compsymbol_sender_id` (`sender_id`),
  KEY `compsymbol_createdate` (`createdate`),
  KEY `compsymbol_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `compteilnehmerfilterinfo` (
  `is_bisexuell`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_asexuell`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_uebergewicht`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_alleinerziehend` TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_kuenstler`       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_eso_partner`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_eso_freund`      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_oeko_partner`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_oeko_freund`     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_vegetar`         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_vegan`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `owner`              VARCHAR(75)
                       COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                 VARCHAR(75)
                       COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`               VARCHAR(30)
                       COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`         DATETIME                           DEFAULT NULL,
  `changedate`         DATETIME                           DEFAULT NULL,
  `is_transsex`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_intersex`        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `is_bdsm`            TINYINT(3) UNSIGNED                DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compteilnehmerfilterinfo_uniowner` (`owner`),
  KEY `compteilnehmerfilterinfo_changedate` (`changedate`),
  KEY `compteilnehmerfilterinfo_createdate` (`createdate`),
  KEY `compteilnehmerfilterinfo_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compundeliveredmail` (
  `mail`             VARCHAR(255)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`               VARCHAR(75)
                     COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`             VARCHAR(30)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`       DATETIME                           DEFAULT NULL,
  `changedate`       DATETIME                           DEFAULT NULL,
  `lastreturndate`   DATETIME                           DEFAULT NULL,
  `undeliveryreason` VARCHAR(50)
                     COLLATE latin1_german2_ci          DEFAULT NULL,
  `msgbody`          TEXT
                     COLLATE latin1_german2_ci,
  PRIMARY KEY (`no`),
  KEY `compundeliveredmail_mail` (`mail`),
  KEY `compundeliveredmail_createdate` (`createdate`),
  KEY `compundeliveredmail_changedate` (`changedate`),
  KEY `compundeliveredmail_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compunsolicitedmail` (
  `mail`       VARCHAR(255)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`         VARCHAR(75)
               COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`       VARCHAR(30)
               COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate` DATETIME                           DEFAULT NULL,
  `changedate` DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compunsolicitedmail_mail` (`mail`),
  KEY `compunsolicitedmail_createdate` (`createdate`),
  KEY `compunsolicitedmail_changedate` (`changedate`),
  KEY `compunsolicitedmail_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compuser` (
  `roles`                                   TEXT
                                            COLLATE latin1_german2_ci,
  `login`                                   VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `password`                                VARCHAR(35)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `confirmationcode`                        VARCHAR(20)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `mail`                                    VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `new_mail`                                VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `new_mail_verifyer`                       VARCHAR(15)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                                      VARCHAR(75)
                                            COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                                    VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                              DATETIME                           DEFAULT NULL,
  `changedate`                              DATETIME                           DEFAULT NULL,
  `completed_adjektive`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_adjektive`                  VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_aussehen`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_aussehen`                   VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_freund`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_freund`                     VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_freundschaft`                  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_freundschaft`               VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_ftext`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_ftext`                      VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_gesellschaft`                  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_gesellschaft`               VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_hobby_e`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_hobby_e`                    VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_hobby_v`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_hobby_v`                    VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_kritische_lebensereignisse`    TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_kritische_lebensereignisse` VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_partner`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_partner`                    VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_partnerschaft`                 TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_partnerschaft`              VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_persoenlichkeit`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_persoenlichkeit`            VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_person`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_person`                     VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `completed_ptext`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_ptext`                      VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `pseudonym`                               VARCHAR(127)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `vorname`                                 VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `nachname`                                VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `telefon`                                 VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `strasse`                                 VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `hausnr`                                  VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `plz`                                     VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `ort`                                     VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `land`                                    VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `bundesland`                              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `geburtstag`                              DATE                               DEFAULT NULL,
  `geschlecht`                              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `suche`                                   TEXT
                                            COLLATE latin1_german2_ci,
  `teilnahmeart`                            TEXT
                                            COLLATE latin1_german2_ci,
  `completed_pfoto`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `completed_ffoto`                         TINYINT(3) UNSIGNED                DEFAULT NULL,
  `questionaire_pfoto`                      VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `questionaire_ffoto`                      VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `admitted_partnersuche`                   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `admitted_freundschaftssuche`             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `disable_vermittlung`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `begin_disable_vermittlung`               DATETIME                           DEFAULT NULL,
  `letzte_benachrichtigung`                 DATETIME                           DEFAULT NULL,
  `agb_akzeptiert`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `datenschutz_akzeptiert`                  TINYINT(3) UNSIGNED                DEFAULT NULL,
  `widerrufsbelehrung_akzeptiert`           TINYINT(3) UNSIGNED                DEFAULT NULL,
  `online_fb_vorhanden`                     TINYINT(3) UNSIGNED                DEFAULT NULL,
  `online_fb_kopieren`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `payment_chosen`                          TINYINT(3) UNSIGNED                DEFAULT '0',
  `letzte_erinnerung`                       DATE                               DEFAULT NULL,
  `anzahl_erinnerungen`                     MEDIUMINT(9)                       DEFAULT '0',
  `quelle`                                  VARCHAR(35)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `quellefrei`                              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `aktionsnr`                               VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `disable_vermittlung_partner`             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `begin_disable_vermittlung_partner`       DATETIME                           DEFAULT NULL,
  `disable_vermittlung_freund`              TINYINT(3) UNSIGNED                DEFAULT NULL,
  `begin_disable_vermittlung_freund`        DATETIME                           DEFAULT NULL,
  `end_disable_vermittlung`                 DATETIME                           DEFAULT NULL,
  `disabled_time`                           MEDIUMINT(9)                       DEFAULT '0',
  `end_disable_vermittlung_partner`         DATETIME                           DEFAULT NULL,
  `disabled_time_partner`                   MEDIUMINT(9)                       DEFAULT '0',
  `end_disable_vermittlung_freund`          DATETIME                           DEFAULT NULL,
  `disabled_time_freund`                    MEDIUMINT(9)                       DEFAULT '0',
  `keine_werbung`                           TINYINT(3) UNSIGNED                DEFAULT '0',
  `signedoff_date`                          DATE                               DEFAULT NULL,
  `confirmed`                               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `aktionsnummer`                           VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `payment_registrierung`                   TINYINT(3) UNSIGNED                DEFAULT NULL,
  `infomail_allowed`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `first_visit_referer`                     TEXT
                                            COLLATE latin1_german2_ci,
  `first_visit_referer_domain`              VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `first_visit_referer_keywords`            VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `filterinfo`                              VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `supress_notif_newmatch`                  TINYINT(3) UNSIGNED                DEFAULT '0',
  `supress_notif_contactmsg`                TINYINT(3) UNSIGNED                DEFAULT '0',
  `supress_notif_matchpos`                  TINYINT(3) UNSIGNED                DEFAULT '0',
  `no_further_infos`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `participant_state`                       VARCHAR(255)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `participant_state_changed`               DATETIME                           DEFAULT NULL,
  `report_begin`                            DATE                               DEFAULT NULL,
  `report_end`                              DATE                               DEFAULT NULL,
  `previous_participant_state`              VARCHAR(50)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `previous_active_state`                   VARCHAR(50)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `supress_infomails`                       TINYINT(3) UNSIGNED                DEFAULT NULL,
  `force_match_p`                           VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `force_match_f`                           VARCHAR(30)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `trace_match`                             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `chat_invisible`                          TINYINT(3) UNSIGNED                DEFAULT NULL,
  `mailblocked`                             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `address_info`                            VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `quellen`                                 TEXT
                                            COLLATE latin1_german2_ci,
  `pressinfo`                               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `comunityregeln_akzeptiert`               TINYINT(3) UNSIGNED                DEFAULT NULL,
  `unchanged_p`                             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `unchanged_f`                             TINYINT(3) UNSIGNED                DEFAULT NULL,
  `user_satisfaction`                       VARCHAR(10)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `matching_success`                        TINYINT(3) UNSIGNED                DEFAULT NULL,
  `user_satisfaction_other`                 VARCHAR(10)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `delrec_p`                                MEDIUMINT(9)                       DEFAULT '0',
  `delrec_f`                                MEDIUMINT(9)                       DEFAULT '0',
  `paymode`                                 VARCHAR(10)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `extmode`                                 TINYINT(3) UNSIGNED                DEFAULT NULL,
  `payamount`                               VARCHAR(10)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `extm_user_disabled`                      TINYINT(3) UNSIGNED                DEFAULT NULL,
  `registration_ip`                         VARCHAR(100)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `registration_mailno`                     VARCHAR(75)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  `mailunblocked_time`                      DATETIME                           DEFAULT NULL,
  `mailunblocked_ip`                        VARCHAR(100)
                                            COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compuser_pseudonym` (`pseudonym`),
  UNIQUE KEY `compuser_type_login` (`login`, `type`),
  KEY `compuser_questionaire_gesellschaft` (`questionaire_gesellschaft`),
  KEY `compuser_questionaire_partner` (`questionaire_partner`),
  KEY `compuser_questionaire_ffoto` (`questionaire_ffoto`),
  KEY `compuser_questionaire_freundschaft` (`questionaire_freundschaft`),
  KEY `compuser_questionaire_ftext` (`questionaire_ftext`),
  KEY `compuser_questionaire_persoenlichkeit` (`questionaire_persoenlichkeit`),
  KEY `compuser_questionaire_adjektive` (`questionaire_adjektive`),
  KEY `compuser_questionaire_freund` (`questionaire_freund`),
  KEY `compuser_questionaire_kritische_lebensereignisse` (`questionaire_kritische_lebensereignisse`),
  KEY `compuser_questionaire_hobby_e` (`questionaire_hobby_e`),
  KEY `compuser_questionaire_aussehen` (`questionaire_aussehen`),
  KEY `compuser_questionaire_ptext` (`questionaire_ptext`),
  KEY `compuser_questionaire_pfoto` (`questionaire_pfoto`),
  KEY `compuser_questionaire_person` (`questionaire_person`),
  KEY `compuser_questionaire_hobby_v` (`questionaire_hobby_v`),
  KEY `compuser_questionaire_partnerschaft` (`questionaire_partnerschaft`),
  KEY `compuser_mail` (`mail`),
  KEY `compuser_createdate` (`createdate`),
  KEY `compuser_changedate` (`changedate`),
  KEY `compuser_componenttype` (`type`),
  KEY `compuser_type` (`type`),
  KEY `compuser_filterinfo` (`filterinfo`),
  KEY `compuser_address_info` (`address_info`),
  KEY `compuser_registration_mailno` (`registration_mailno`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compuseractivity` (
  `user`              VARCHAR(75)
                      COLLATE latin1_german2_ci          DEFAULT NULL,
  `usertype`          VARCHAR(30)
                      COLLATE latin1_german2_ci          DEFAULT NULL,
  `timestamp`         DATETIME                           DEFAULT NULL,
  `activity`          VARCHAR(35)
                      COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                VARCHAR(75)
                      COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`              VARCHAR(30)
                      COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`        DATETIME                           DEFAULT NULL,
  `changedate`        DATETIME                           DEFAULT NULL,
  `signoff_reason`    TEXT
                      COLLATE latin1_german2_ci,
  `signoff_followups` TEXT
                      COLLATE latin1_german2_ci,
  `signoff_comment`   TEXT
                      COLLATE latin1_german2_ci,
  PRIMARY KEY (`no`),
  KEY `compuseractivity_user` (`user`),
  KEY `compuseractivity_changedate` (`changedate`),
  KEY `compuseractivity_createdate` (`createdate`),
  KEY `compuseractivity_componenttype` (`type`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compvorschlag` (
  `rubrik`                 CHAR(1)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `suchender`              VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `vorschlag`              VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `gegenvorschlag`         VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `vorschlagsdatum`        DATETIME                           DEFAULT NULL,
  `matchval`               VARCHAR(40)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `activitystate`          VARCHAR(10)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `ablage`                 VARCHAR(10)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`                     VARCHAR(75)
                           COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                   VARCHAR(30)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`             DATETIME                           DEFAULT NULL,
  `changedate`             DATETIME                           DEFAULT NULL,
  `singlesided`            VARCHAR(10)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `selstate`               VARCHAR(12)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `match_participant_info` TEXT
                           COLLATE latin1_german2_ci,
  `match_admin_info`       TEXT
                           COLLATE latin1_german2_ci,
  `visibility_fotos`       VARCHAR(5)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  `current_phonenumber`    VARCHAR(75)
                           COLLATE latin1_german2_ci          DEFAULT NULL,
  PRIMARY KEY (`no`),
  KEY `compvorschlag_gegenvorschlag` (`gegenvorschlag`),
  KEY `compvorschlag_vorschlag` (`vorschlag`),
  KEY `compvorschlag_suchender` (`suchender`),
  KEY `compvorschlag_changedate` (`changedate`),
  KEY `compvorschlag_createdate` (`createdate`),
  KEY `compvorschlag_componenttype` (`type`),
  KEY `compvorschlag_current_phonenumber` (`current_phonenumber`),
  KEY `compvorschlag_ablage` (`ablage`),
  KEY `compvorschlag_rubrik_suchender` (`rubrik`, `suchender`),
  KEY `compvorschlag_rubrik` (`rubrik`),
  KEY `compvorschlag_activitystate` (`activitystate`),
  KEY `compvorschlag_rubrik_vorschlagsdatum` (`vorschlagsdatum`, `rubrik`),
  KEY `compvorschlag_rubrik_suchender_vorschlag` (`rubrik`, `suchender`, `vorschlag`),
  KEY `compvorschlag_vorschlagsdatum` (`vorschlagsdatum`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compziparea` (
  `ziparea_country`           VARCHAR(6)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `ziparea_zipcode`           VARCHAR(10)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `ziparea_count`             MEDIUMINT(9)                       DEFAULT NULL,
  `ziparea_countunique`       MEDIUMINT(9)                       DEFAULT NULL,
  `ziparea_lon`               VARCHAR(40)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `ziparea_lat`               VARCHAR(40)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `ziparea_meanradius`        VARCHAR(40)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `ziparea_convexhull_count`  MEDIUMINT(9)                       DEFAULT NULL,
  `ziparea_convexhull`        TEXT
                              COLLATE latin1_german2_ci,
  `ziparea_convexhull_coords` TEXT
                              COLLATE latin1_german2_ci,
  `no`                        VARCHAR(75)
                              COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`                      VARCHAR(30)
                              COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`                DATETIME                           DEFAULT NULL,
  `changedate`                DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compziparea_country_ziparea` (`ziparea_country`, `ziparea_zipcode`),
  KEY `compziparea_componenttype` (`type`),
  KEY `compziparea_changedate` (`changedate`),
  KEY `compziparea_createdate` (`createdate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

CREATE TABLE IF NOT EXISTS `compzipcity` (
  `zipcity_country` VARCHAR(6)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `zipcity_zip`     VARCHAR(10)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `zipcity_state`   VARCHAR(8)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `zipcity_cities`  TEXT
                    COLLATE latin1_german2_ci,
  `zipcity_lon`     VARCHAR(40)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `zipcity_lat`     VARCHAR(40)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `no`              VARCHAR(75)
                    COLLATE latin1_german2_ci NOT NULL DEFAULT '',
  `type`            VARCHAR(30)
                    COLLATE latin1_german2_ci          DEFAULT NULL,
  `createdate`      DATETIME                           DEFAULT NULL,
  `changedate`      DATETIME                           DEFAULT NULL,
  PRIMARY KEY (`no`),
  UNIQUE KEY `compzipcity_country_zip` (`zipcity_country`, `zipcity_zip`),
  KEY `compzipcity_componenttype` (`type`),
  KEY `compzipcity_createdate` (`createdate`),
  KEY `compzipcity_changedate` (`changedate`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_german2_ci;

--
-- Dumping data for table `compzipcity`
--


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
