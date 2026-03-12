-- Duration may not be less than 1
UPDATE product
SET duration = 1
WHERE (DTYPE = "RenewalOffer" OR DTYPE = "InitialSubscriptionOffer" OR
       DTYPE = "UpgradeOffer") AND duration < 1;
