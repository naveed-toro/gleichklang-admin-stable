CALL ADD_COLUMN('user_settings', 'source_text', 'VARCHAR(255)');

INSERT IGNORE INTO answer (DTYPE, create_date, question_id, user_id, text_value)
  SELECT
    'TextAnswer',
    NOW(),
    q.id,
    us.user_id,
    us.source_text
  FROM
    user_settings us LEFT JOIN question q ON q.i18n_key = 'source_text';

CALL DROP_COLUMN('user_settings', 'source_text');