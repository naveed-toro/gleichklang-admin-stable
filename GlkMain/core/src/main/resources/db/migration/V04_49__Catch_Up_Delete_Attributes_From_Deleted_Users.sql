UPDATE user_ SET
    email = NULL,
    birth_date = NULL,
    last_name = NULL,
    first_name = NULL,
    password = NULL,
    register_ip = NULL,
    confirmation_ip = NULL,
    confirmation_date = NULL
WHERE member_status = 'DELETED';

DELETE a FROM address a JOIN user_ u ON u.id = a.user_id WHERE u.member_status = 'DELETED';

UPDATE payment p JOIN user_ u ON u.id = p.user_id SET
    p.state = 'CANCELED'
WHERE u.member_status = 'DELETED' AND p.state = 'PENDING';

SET FOREIGN_KEY_CHECKS = 0;

DELETE f, a
      FROM user_ u
      JOIN avatar a ON u.id = a.user_id
      JOIN file f ON f.id = a.file_id
WHERE u.member_status = 'DELETED';

DELETE f, m, g
      FROM user_ u
      JOIN media_gallery g ON u.id = g.author_id
      JOIN media m ON m.media_gallery_id = g.id
      JOIN file f ON f.id = m.file_id
WHERE u.member_status = 'DELETED';

SET FOREIGN_KEY_CHECKS = 1;

DELETE epr FROM external_payment_registration epr JOIN user_ u ON u.id = epr.user_id WHERE u.member_status = 'DELETED';