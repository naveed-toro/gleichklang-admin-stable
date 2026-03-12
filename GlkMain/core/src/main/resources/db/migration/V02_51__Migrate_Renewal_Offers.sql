UPDATE product AS renewal_offer, product as offer
    SET renewal_offer.DTYPE = 'RenewalOffer'
    WHERE renewal_offer.id = offer.auto_renewal_offer_id;


