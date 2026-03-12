CALL ADD_COLUMN('payment', 'refunded', 'BIT NOT NULL DEFAULT 0');
CALL ADD_COLUMN('payment', 'payment_to_refund_id', 'bigint(20) DEFAULT NULL');

CALL ADD_FOREIGN_KEY('payment', 'payment_to_refund_id', 'payment', 'id');