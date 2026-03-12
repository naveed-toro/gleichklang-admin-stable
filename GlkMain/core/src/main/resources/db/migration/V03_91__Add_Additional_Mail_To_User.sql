UPDATE IGNORE user_ u
    JOIN compuser legacy_u ON u.legacy_id = legacy_u.no
SET u.email = legacy_u.mail WHERE u.email IS NULL;