CREATE TABLE IF NOT EXISTS user_payment_settings
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,

  user_id             BIGINT       NOT NULL,
  payment_method      VARCHAR(255) NOT NULL,

  FOREIGN KEY (user_id) REFERENCES user_ (id),

  -- make sure that exactly one row exists for each user
  UNIQUE INDEX user_payment_settings_user_id (user_id)
);

-- deleting all rows is safe here and makes this migration idempotent
DELETE FROM user_payment_settings;

INSERT INTO user_payment_settings
(create_date, user_id, payment_method)
  SELECT
    t.createdate AS create_date,
    u.id         AS user_id,
    CASE t.method
    WHEN 'CC'
      THEN 'CREDIT_CARD'
    WHEN 'DD'
      THEN 'DIRECT_DEBIT'
    WHEN 'PP'
      THEN 'PREPAYMENT'
    ELSE 'PREPAYMENT'
    END          AS payment_method
  FROM (SELECT *
        FROM comppayment_transaction t
        WHERE t.protocolstate = 'PAID'
        ORDER BY t.subscription_end DESC) AS t
    JOIN user_ u ON u.legacy_id = t.participant_id
  GROUP BY t.participant_id;