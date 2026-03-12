CALL ADD_COLUMN('user_', 'register_ip', 'VARCHAR(255)');

UPDATE user_ u1 JOIN compuser u2 ON u1.legacy_id = u2.no SET u1.register_ip = u2.registration_ip;