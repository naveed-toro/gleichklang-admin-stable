ALTER Table user_ add column admin_notes VARCHAR(500);

CREATE TABLE IF NOT EXISTS email_domain_mapping (
  id                  BIGINT(20)   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date         TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date         TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
  mapping_name        VARCHAR(100),
  deleted             BIT(1),
  active              BIT(1),
  mapping_value      VARCHAR(500)
  );

insert into admin_roles values (1, 'SYSTEM_CONFIGURATION');

DROP TABLE email_template_mapping;

CREATE TABLE IF NOT EXISTS email_template_mapping (
  id                  BIGINT(20)   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date         TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date         TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
  template_name        VARCHAR(100),
  template_description VARCHAR(100),
  template_language     VARCHAR(50),
  template_footer      BIGINT(20) ,
  deleted             BIT(1) DEFAULT false,
  active              BIT(1) DEFAULT true,
  template_text      TEXT(5000) COLLATE utf8_unicode_ci
  );

  commit;
