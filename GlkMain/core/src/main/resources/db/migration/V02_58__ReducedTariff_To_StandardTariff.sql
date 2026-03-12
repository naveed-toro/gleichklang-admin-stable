UPDATE product AS reduced_offer
    SET reduced_offer.tariff = 'STANDARD'
    WHERE reduced_offer.tariff = 'REDUCED';


