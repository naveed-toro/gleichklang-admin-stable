INSERT INTO recommendation_break (create_date, change_date, user_id, category, end_date)
    SELECT
        NOW(),
        NOW(),
        u.id,
        'PARTNERSHIP',
        cu.end_disable_vermittlung_partner
    FROM user_ u JOIN compuser cu ON u.legacy_id = cu.no
    WHERE
        cu.end_disable_vermittlung_partner > NOW() AND (cu.begin_disable_vermittlung_partner IS NULL OR cu.begin_disable_vermittlung_partner < NOW()) OR
        cu.end_disable_vermittlung_partner IS NULL AND cu.begin_disable_vermittlung_partner < NOW() IS NOT NULL
ON DUPLICATE KEY UPDATE
    end_date = cu.end_disable_vermittlung_partner;

INSERT INTO recommendation_break (create_date, change_date, user_id, category, end_date)
    SELECT
        NOW(),
        NOW(),
        u.id,
        'FRIENDSHIP',
        cu.end_disable_vermittlung_freund
    FROM user_ u JOIN compuser cu ON u.legacy_id = cu.no
    WHERE
        cu.end_disable_vermittlung_freund > NOW() AND (cu.begin_disable_vermittlung_freund IS NULL OR cu.begin_disable_vermittlung_freund < NOW()) OR
        cu.end_disable_vermittlung_freund IS NULL AND cu.begin_disable_vermittlung_freund < NOW() IS NOT NULL
ON DUPLICATE KEY UPDATE
    end_date = cu.end_disable_vermittlung_freund;