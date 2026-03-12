CALL ADD_COLUMN('heidelpay_descheduler', 'request_count', 'INT NOT NULL DEFAULT 1');

TRUNCATE heidelpay_descheduler;
CALL CREATE_UNIQUE_INDEX('heidelpay_descheduler_ref_unique_id_idx', 'heidelpay_descheduler', 'ref_unique_id');