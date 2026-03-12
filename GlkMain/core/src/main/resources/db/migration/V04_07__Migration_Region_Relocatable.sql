SET @question_id=(SELECT id FROM question WHERE i18n_key = 'partner.region_question');

INSERT INTO answer (DTYPE, legacy_id, create_date, change_date, question_id, user_id, search_relocatable, relocatable)
    SELECT
        'RegionAnswer',
        cp.no,
        NOW(),
        NOW(),
        @question_id,
        u.id,
        cp.p_region_umzug IS NOT NULL AND cp.p_region_umzug > 0,
        CASE WHEN cp.umzug IN ('004', '005') THEN TRUE ELSE FALSE END
    FROM comppartner cp JOIN user_ u on u.legacy_id = cp.owner
ON DUPLICATE KEY UPDATE
    search_relocatable = cp.p_region_umzug IS NOT NULL AND cp.p_region_umzug > 0,
    relocatable = CASE WHEN cp.umzug IN ('004', '005') THEN TRUE ELSE FALSE END;