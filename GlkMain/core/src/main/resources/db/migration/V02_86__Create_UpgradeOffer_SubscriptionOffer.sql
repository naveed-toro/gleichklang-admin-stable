CREATE TABLE IF NOT EXISTS upgrade_offer_subscription_offer
(
    upgrade_offer_id      BIGINT NOT NULL,
    subscription_offer_id BIGINT NOT NULL,

    FOREIGN KEY (upgrade_offer_id) REFERENCES product (id),
    FOREIGN KEY (subscription_offer_id) REFERENCES product (id)
);
