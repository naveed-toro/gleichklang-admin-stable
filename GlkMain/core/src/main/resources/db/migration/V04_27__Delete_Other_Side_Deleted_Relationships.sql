UPDATE
    relationship r1 JOIN relationship r2 ON r1.source_user_id = r2.target_user_id AND r1.target_user_id = r2.source_user_id
SET
    r2.deleted = true
WHERE
    r1.deleted = true AND r2.deleted = false;