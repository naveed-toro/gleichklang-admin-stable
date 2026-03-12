package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.launcher.H2SchemaGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by michael on 20/05/15.
 */
public class DatabaseConverterTest
{


	@Test
	public void testLongtextConvertion() throws Exception
	{
		List<String> createTableStatements = new ArrayList<>();
		String testScript = "CREATE TABLE i18n (\n"
				+ "    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,\n"
				+ "    change_date DATETIME,\n"
				+ "    create_date DATETIME,\n"
				+ "    language VARCHAR(255) collate utf8_unicode_ci,\n"
				+ "    i18n_key VARCHAR(255) collate utf8_unicode_ci,\n"
				+ "    i18n_value TEXT,\n"
				+ "    base_name VARCHAR(255) collate utf8_unicode_ci);\n";
		createTableStatements.add(testScript);
		String createScript = new H2SchemaGenerator.MySqlH2Converter().convert(createTableStatements);

		String expectedScript = "create table i18n (\n"
				+ "id bigint primary key not null auto_increment,\n"
				+ "change_date datetime,\n"
				+ "create_date datetime,\n"
				+ "language varchar(255),\n"
				+ "i18n_key varchar(255),\n"
				+ "i18n_value clob,\n"
				+ "base_name varchar(255));\n";

		assertThat(createScript, equalTo(expectedScript));
	}

	@Test
	public void testForeignKeys() throws Exception
	{
		List<String> createTableStatements = new ArrayList<>();
		String testScript = "CREATE TABLE `proximity_search_locatable` (\n"
				+ "  `proximity_search_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  `restriction_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  PRIMARY KEY (`proximity_search_id`,`restriction_id`),\n"
				+ "  KEY `restriction_id` (`restriction_id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_2` FOREIGN KEY (`restriction_id`) REFERENCES `locatable` (`id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_1` FOREIGN KEY (`proximity_search_id`) REFERENCES `proximity_search` (`id`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
		createTableStatements.add(testScript);
		String createScript = new H2SchemaGenerator.MySqlH2Converter().convert(createTableStatements);
		String expectedScript = "create table `proximity_search_locatable` (\n"
				+ "`proximity_search_id` bigint(20) not null default '0',\n"
				+ "`restriction_id` bigint(20) not null default '0',\n"
				+ "primary key (`proximity_search_id`,`restriction_id`));\n\n"
				+ "alter table proximity_search_locatable add constraint `proximity_search_locatable_ibfk_1` foreign key (`proximity_search_id`) references `proximity_search` (`id`);\n"
				+ "alter table proximity_search_locatable add constraint `proximity_search_locatable_ibfk_2` foreign key (`restriction_id`) references `locatable` (`id`);\n";

		assertThat(createScript, equalTo(expectedScript));
	}

	@Test
	public void testMultiple() throws Exception
	{
		List<String> createTableStatements = new ArrayList<>();
		createTableStatements.add("CREATE TABLE `proximity_search_locatable` (\n"
				+ "  `proximity_search_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  `restriction_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  PRIMARY KEY (`proximity_search_id`,`restriction_id`),\n"
				+ "  KEY `restriction_id` (`restriction_id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_2` FOREIGN KEY (`restriction_id`) REFERENCES `locatable` (`id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_1` FOREIGN KEY (`proximity_search_id`) REFERENCES `proximity_search` (`id`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");

		createTableStatements.add("CREATE TABLE `proximity_search_locatable2` (\n"
				+ "  `proximity_search_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  `restriction_id` bigint(20) NOT NULL DEFAULT '0',\n"
				+ "  PRIMARY KEY (`proximity_search_id`,`restriction_id`),\n"
				+ "  KEY `restriction_id` (`restriction_id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_2` FOREIGN KEY (`restriction_id`) REFERENCES `locatable` (`id`),\n"
				+ "  CONSTRAINT `proximity_search_locatable_ibfk_1` FOREIGN KEY (`proximity_search_id`) REFERENCES `proximity_search` (`id`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");

		String createScript = new H2SchemaGenerator.MySqlH2Converter().convert(createTableStatements);
		String expectedScript = "create table `proximity_search_locatable` (\n"
				+ "`proximity_search_id` bigint(20) not null default '0',\n"
				+ "`restriction_id` bigint(20) not null default '0',\n"
				+ "primary key (`proximity_search_id`,`restriction_id`));\n\n"
				+ "create table `proximity_search_locatable2` (\n"
				+ "`proximity_search_id` bigint(20) not null default '0',\n"
				+ "`restriction_id` bigint(20) not null default '0',\n"
				+ "primary key (`proximity_search_id`,`restriction_id`));\n\n"
				+ "alter table proximity_search_locatable add constraint `proximity_search_locatable_ibfk_1` foreign key (`proximity_search_id`) references `proximity_search` (`id`);\n"
				+ "alter table proximity_search_locatable add constraint `proximity_search_locatable_ibfk_2` foreign key (`restriction_id`) references `locatable` (`id`);\n"
				+ "alter table proximity_search_locatable2 add constraint `proximity_search_locatable_ibfk_1` foreign key (`proximity_search_id`) references `proximity_search` (`id`);\n"
				+ "alter table proximity_search_locatable2 add constraint `proximity_search_locatable_ibfk_2` foreign key (`restriction_id`) references `locatable` (`id`);\n";

		assertThat(createScript, equalTo(expectedScript));
	}
}
