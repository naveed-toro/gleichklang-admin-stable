ALTER TABLE offer_search_domain
ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
ADD COLUMN legacy_id VARCHAR(255),
ADD COLUMN change_date DATETIME,
ADD COLUMN create_date DATETIME;

SET @rownum := 0;

INSERT INTO offer_search_domain
	(id, change_date, create_date, offer_id, search_domain)
SELECT
	@rownum := @rownum + 1 AS id,
	changedate AS change_date,
    createdate AS create_date,
	id AS offer_id,
	'FRIENDSHIP' AS search_domain
FROM comppayment_offer, offer
WHERE assumed_forms LIKE '%f%' AND comppayment_offer.no = offer.legacy_id;

INSERT INTO offer_search_domain
	(id, change_date, create_date, offer_id, search_domain)
SELECT
	@rownum := @rownum + 1 AS id,
	changedate AS change_date,
    createdate AS create_date,
	id AS offer_id,
	'PARTNERSHIP' AS search_domain
FROM comppayment_offer, offer
WHERE assumed_forms LIKE '%p%' AND comppayment_offer.no = offer.legacy_id;


CALL ADD_COLUMN('offer', 'DTYPE', 'VARCHAR(31) NOT NULL');

CALL ADD_COLUMN('offer', 'renews_offer_id', 'BIGINT');

CALL ADD_COLUMN('offer', 'renewal_offer_id', 'BIGINT');

UPDATE offer
SET DTYPE = CASE type
	WHEN 'INITIAL'
		THEN 'InitialOffer'
    WHEN 'RENEWAL'
        THEN 'RenewalOffer'
    WHEN 'CHARGE_BACK'
        THEN 'ChargeBackOffer'
    WHEN 'DONATION'
        THEN 'DonationOffer'
    WHEN 'EXTENSION'
        THEN 'ExtensionOffer'
END;


CALL DROP_COLUMN('offer', 'type');
