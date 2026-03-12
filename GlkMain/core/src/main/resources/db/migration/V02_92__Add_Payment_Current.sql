CALL ADD_COLUMN('payment', 'current', 'BIT');

-- create a combined unique index to avoid creation of multiple current payments per user
CALL CREATE_UNIQUE_INDEX('payment_user_id_current', 'payment', 'user_id, current');

UPDATE payment p1
	JOIN (SELECT max(create_date), id FROM payment GROUP BY user_id) p2
	ON p1.id = p2.id
SET p1.current = 1;