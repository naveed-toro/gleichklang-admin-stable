UPDATE product s, comppayment_offer o

SET s.initial_offer = 1

WHERE s.DTYPE = 'SubscriptionOffer'
AND s.legacy_id = o.no
AND o.is_renewal_offer = 0;