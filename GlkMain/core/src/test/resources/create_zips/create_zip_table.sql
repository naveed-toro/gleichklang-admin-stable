DROP TABLE IF EXISTS zip_table;

CREATE TABLE `zip_table` (
  `country_code` CHAR(2)
                 COLLATE utf8_unicode_ci DEFAULT NULL,
  `postal_code`  VARCHAR(20)
                 COLLATE utf8_unicode_ci DEFAULT NULL,
  `place_name`   VARCHAR(100)
                 COLLATE utf8_unicode_ci DEFAULT NULL,
  `place_code`   VARCHAR(100)
                 COLLATE utf8_unicode_ci DEFAULT NULL,
  `latitude`     DOUBLE                  DEFAULT NULL,
  `longitude`    DOUBLE                  DEFAULT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

CREATE INDEX country_code_index ON zip_table (country_code);
CREATE INDEX postal_code_index ON zip_table (postal_code);
CREATE INDEX latitude_index ON zip_table (latitude);
CREATE INDEX longitude_index ON zip_table (longitude);

INSERT INTO zip_table
  SELECT
    country_code,
    postal_code,
    admin_name1              AS place_name,
    admin_code1              AS place_code,
    ROUND(AVG(latitude), 2)  AS latitude,
    ROUND(AVG(longitude), 2) AS longitude
  FROM all_countries
  GROUP BY postal_code, country_code
  ORDER BY country_code, place_code;

UPDATE zip_table as z JOIN regions AS r ON z.place_name = r.name SET z.place_code = r.region_code;