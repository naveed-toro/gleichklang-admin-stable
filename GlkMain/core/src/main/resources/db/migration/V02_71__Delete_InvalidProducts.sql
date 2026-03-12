-- mark invalid products as invalid by setting the end to a date in the past
UPDATE product p
    SET p.DTYPE = 'RenewalOffer', p.end = '2016-01-01'
WHERE p.DTYPE = 'SubscriptionOffer';