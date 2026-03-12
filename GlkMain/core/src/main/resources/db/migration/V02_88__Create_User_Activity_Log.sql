CREATE TABLE IF NOT EXISTS user_activity_log
(
    id                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id               VARCHAR(255),
    change_date             DATETIME,
    create_date             DATETIME,

    user_id                 BIGINT NOT NULL,
    user_activity           VARCHAR(255) NOT NULL,
    category                VARCHAR(255),
    FOREIGN KEY (user_id)   REFERENCES user_ (id)
);

DELETE FROM user_activity_log;

INSERT INTO user_activity_log (change_date, create_date, user_id, user_activity, category)
    SELECT
        ca.changedate,
        ca.createdate,
        u.id,
        CASE ca.activity
                WHEN 'login' THEN 'LOGIN'
                WHEN 'loginfailed' THEN 'LOGIN_FAILED'
                WHEN 'new_match_f' THEN 'NEW_MATCH'
                WHEN 'new_match_p' THEN 'NEW_MATCH'
                WHEN 'register' THEN 'REGISTERED'
                WHEN 'renewal_cancelled' THEN 'RENEWAL_CANCELLED'
                WHEN 'renewal_reactivated' THEN 'RENEWAL_REACTIVATED'
            END,
        CASE ca.activity
                WHEN 'new_match_p' THEN 'PARTNERSHIP'
                WHEN 'new_match_f' THEN 'FRIENDSHIP'
                ELSE NULL
        END
    FROM
        compuseractivity ca INNER JOIN user_ u ON u.legacy_id = ca.user
    WHERE
        ca.activity = 'login' OR
        ca.activity = 'loginfailed' OR
        ca.activity = 'new_match_f' OR
        ca.activity = 'new_match_p' OR
        ca.activity = 'register' OR
        ca.activity = 'renewal_cancelled' OR
        ca.activity = 'renewal_reactivated';