CREATE TABLE IF NOT EXISTS email_domain_mapping (
  id                  BIGINT(20)   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date         TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date         TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
  mapping_name        VARCHAR(100),
  deleted             BIT(1) DEFAULT false,
  active              BIT(1) DEFAULT true,
  mapping_value      VARCHAR(500)
  );


CREATE TEMPORARY TABLE IF NOT EXISTS tmp_roles (role VARCHAR(255));

DELETE FROM tmp_roles;

INSERT INTO tmp_roles VALUES
    ('SYSTEM_CONFIGURATION');

INSERT INTO admin_roles(admin_id, roles)
    SELECT distinct u.id, tr.role FROM user_ u, tmp_roles tr WHERE u.DTYPE = 'Admin';

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

  delete from email_template_mapping;
