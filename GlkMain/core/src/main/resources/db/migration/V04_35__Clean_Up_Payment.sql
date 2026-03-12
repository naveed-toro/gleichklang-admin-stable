CALL ADD_COLUMN('subscription', 'state', 'VARCHAR(255)');

UPDATE
    subscription s
    JOIN user_ u ON s.user_id = u.id
    LEFT JOIN payment p ON p.user_id = u.id AND p.current = true AND p.state = 'PENDING'
SET
    s.state = CASE
        WHEN s.current = true AND u.member_status = 'REGISTERED' AND s.expiration_date < NOW() THEN 'EXPIRING'
        WHEN s.current = true AND u.member_status = 'REGISTERED' AND s.end < NOW() AND p.id IS NOT NULL THEN 'EXPIRING'
        WHEN s.current = true AND u.member_status = 'REGISTERED' THEN 'ACTIVE'
        WHEN s.end = s.expiration_date THEN 'CANCELED'
        ELSE 'EXPIRED'
        END
WHERE
    s.state IS NULL;

ALTER TABLE subscription MODIFY state VARCHAR(255) NOT NULL;