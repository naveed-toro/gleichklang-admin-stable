CALL ADD_COLUMN('subscription', 'active', 'BIT');

-- create a combined unique index to avoid creation of multiple subscriptions per user
CALL CREATE_UNIQUE_INDEX('subscription_user_id_active', 'subscription', 'user_id, active');
