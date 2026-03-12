CALL DROP_INDEX('address_payment_idx', 'address');
CALL DROP_COLUMN('address', 'payment');
CALL ADD_COLUMN('address', 'payment', 'BIT(1) DEFAULT NULL');

UPDATE address as a JOIN (SELECT id FROM address group by user_id) as t1 ON a.id = t1.id
SET a.payment = true;

CALL CREATE_UNIQUE_INDEX('address_payment_idx', 'address', 'user_id, payment');
