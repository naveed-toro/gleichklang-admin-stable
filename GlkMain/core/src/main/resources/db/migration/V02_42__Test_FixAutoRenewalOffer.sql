-- the test auto renewal offer must renew itself
UPDATE product p, product a
SET p. auto_renewal_offer_id = a.id
WHERE p.name = 'Test P+F Auto' AND a.name = 'Test P+F Auto';
