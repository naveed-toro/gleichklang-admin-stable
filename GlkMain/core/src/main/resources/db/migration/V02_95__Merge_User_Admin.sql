CALL ADD_COLUMN('user_', 'DTYPE', 'varchar(31)');
CALL ADD_COLUMN('user_', 'deleted', 'BIT DEFAULT FALSE');

CALL DROP_FOREIGN_KEY('admin_roles', 'admin_roles_ibfk_1');
DELETE FROM user_
WHERE DTYPE = 'Admin';

DELETE FROM admin_roles;

UPDATE user_
SET DTYPE = 'User';

CALL CHANGE_COLUMN('user_', 'birth_date', 'birth_date', 'date DEFAULT NULL');
CALL CHANGE_COLUMN('user_', 'member_status', 'member_status',
                   'varchar(255) DEFAULT NULL');
CALL DROP_INDEX('user_alias', 'user_');
CALL DROP_INDEX('user_email', 'user_');

-- this table is necessary for idempotence otherwise next query will fail
CREATE TABLE IF NOT EXISTS admin
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255) ,
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  alias            VARCHAR(255) NOT NULL,
  email            VARCHAR(255) NOT NULL,
  first_name        VARCHAR(255) DEFAULT NULL,
  last_name         VARCHAR(255) DEFAULT NULL,
  password         VARCHAR(35)  NOT NULL,
  reset_password bit(1) DEFAULT b'0',
  email_confirmed bit(1) DEFAULT b'0'
);


INSERT INTO user_ (
  DTYPE, legacy_id, change_date, create_date, alias, email, first_name, last_name, password, reset_password, email_confirmed)
  SELECT
    'Admin',
    id,
    change_date,
    create_date,
    alias,
    email,
    first_name,
    last_name,
    password,
    reset_password,
    email_confirmed
  FROM admin;

UPDATE admin_roles ar, user_ u
SET ar.admin_id = u.id
WHERE ar.admin_id = u.legacy_id AND u.DTYPE = 'Admin';

CALL DROP_FOREIGN_KEY('admin_roles', 'admin_roles_ibfk_1');
CALL ADD_FOREIGN_KEY('admin_roles', 'admin_id', 'user_', 'id');
CALL CREATE_UNIQUE_INDEX('user_type_email_idx', 'user_', 'email, DTYPE');
CALL CREATE_UNIQUE_INDEX('user_type_alias_idx', 'user_', 'alias, DTYPE');
CALL CREATE_INDEX('user_dtype_idx', 'user_', 'DTYPE');
CALL DROP_FOREIGN_KEY('user_settings', 'user_settings_ibfk_2');

DROP TABLE admin;