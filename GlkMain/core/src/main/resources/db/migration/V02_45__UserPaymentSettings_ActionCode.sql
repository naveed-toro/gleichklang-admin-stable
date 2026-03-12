CALL ADD_COLUMN('user_payment_settings', 'action_code', 'VARCHAR(255) NULL');

UPDATE user_payment_settings ups
    JOIN user_ u ON ups.user_id = u.id
    JOIN compuser cu ON cu.no = u.legacy_id
SET ups.action_code = cu.aktionsnr;