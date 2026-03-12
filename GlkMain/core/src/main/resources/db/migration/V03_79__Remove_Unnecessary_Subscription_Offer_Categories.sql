-- Remove subscription offer categories from products which are not subscription offers
DELETE cat FROM subscription_offer_category cat
WHERE EXISTS(
    SELECT 1
    FROM product p
    WHERE p.DTYPE NOT IN
          ('InitialSubscriptionOffer', 'RenewalOffer', 'UpgradeOffer')
          AND cat.subscription_offer_id = p.id);
