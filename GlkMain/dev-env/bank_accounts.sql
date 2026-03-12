-- MySQL dump 10.14  Distrib 5.5.56-MariaDB, for Linux (x86_64)
--
-- Host: 192.168.35.20    Database: gleichklang
-- ------------------------------------------------------
-- Server version	5.6.39-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bank_account`
--

DROP TABLE IF EXISTS `bank_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bank_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `legacy_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `change_date` datetime DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `holder` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `bank_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `account_number` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `bank_number` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `iban` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `bic` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `country_id` bigint(20) NOT NULL,
  `active` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bank_account_country_id` (`active`,`country_id`),
  KEY `country_id` (`country_id`),
  CONSTRAINT `bank_account_ibfk_1` FOREIGN KEY (`country_id`) REFERENCES `locatable` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bank_account`
--

LOCK TABLES `bank_account` WRITE;
/*!40000 ALTER TABLE `bank_account` DISABLE KEYS */;
INSERT INTO `bank_account` VALUES (8,NULL,NULL,'2017-10-10 00:00:00','Gleichklang limited','Deutsche Bank','3682606','10070024','DE34100700240368260600','DEUTDEDBBER',53,NULL),(9,NULL,NULL,'2017-10-10 00:00:00','Gleichklang Limited','Deutsche Bank, Berlin','368260601','10070024','DE07100700240368260601','DEUTDEDBBER',53,NULL),(10,NULL,NULL,'2017-10-10 00:00:00','Heidelpay GmbH','Unknown','110097255','14000','AT941400000110097255','BAWAATWW',172,NULL),(11,NULL,NULL,'2017-10-10 00:00:00','Heidelberger Payment','WestLB Düsseldorf','1675214','30050000','DE19300500000001675214','WELADEDDXXX',53,NULL),(12,NULL,NULL,'2017-10-10 00:00:00','Gleichklang limited','GLS Bank','4025656101','43060967','DE87430609674025656101','GENODEM1GLS',53,''),(13,NULL,NULL,'2017-10-10 00:00:00','heidelpay gmbh','Unknown','666875923','70020270','DE54700202700666875923','HYVEDEMM',53,NULL),(15,NULL,NULL,'2017-10-10 00:00:00','Unknown','Unknown','Unknown','Unknown','Unknown','Unknown',53,NULL);
/*!40000 ALTER TABLE `bank_account` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-03-14  9:57:41
