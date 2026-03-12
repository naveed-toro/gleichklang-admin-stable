CREATE TABLE IF NOT EXISTS avatar
(
    id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id                VARCHAR(255),
    change_date              DATETIME,
    create_date              DATETIME,
    user_id                  BIGINT NOT NULL,
    file_id                  BIGINT NOT NULL,
    category                 VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id)    REFERENCES user_ (id),
    FOREIGN KEY (file_id)    REFERENCES file (id),

    UNIQUE INDEX avatar_user_id_category (user_id, category)
);

UPDATE user_ SET birthDate = '1970-01-01' WHERE birthDate IS NULL;

CALL CHANGE_COLUMN('user_', 'birthDate', 'birth_date', 'DATE NOT NULL');
CALL CHANGE_COLUMN('user_', 'confirmationCode', 'confirmation_code', 'VARCHAR(255)');
CALL CHANGE_COLUMN('user_', 'firstName', 'first_name', 'VARCHAR(255)');
CALL CHANGE_COLUMN('user_', 'lastName', 'last_name', 'VARCHAR(255)');
CALL CHANGE_COLUMN('user_', 'memberStatus', 'member_status', 'VARCHAR(255) NOT NULL');
CALL CHANGE_COLUMN('user_', 'newMail', 'new_mail', 'VARCHAR(255)');
CALL CHANGE_COLUMN('user_', 'statusMessage', 'status_message', 'VARCHAR(255)');

CALL DROP_COLUMN('user_', 'sex');