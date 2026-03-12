UPDATE
    payment p
    JOIN user_ u ON p.user_id = u.id
SET
    p.state = 'CANCELED'
WHERE
    p.state = 'PENDING' AND
    u.member_status = 'DELETED';