UPDATE product AS initial_offer
    SET initial_offer.DTYPE = 'InitialSubscriptionOffer'
    WHERE initial_offer.initial_offer = 1;

CALL DROP_COLUMN('product', 'initial_offer');

CALL ADD_COLUMN('product', 'upgrade_type', 'VARCHAR(255) NULL');


