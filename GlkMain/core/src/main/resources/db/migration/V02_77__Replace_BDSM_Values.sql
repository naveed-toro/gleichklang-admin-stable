UPDATE i18n AS i
SET i.i18n_value = CASE i.i18n_key
                   WHEN 'partner/in_soll...rolle_einnehmen'
                     THEN 'Partner/in sollte devote Rolle einnehmen'
                   WHEN 'partner/in_soll...rolle_einnehmen$1'
                     THEN 'Partner/in sollte dominante Rolle einnehmen'
                   WHEN 'partner/in_soll...rolle_einnehmen$2'
                     THEN 'Partner/in sollte devote oder dominante Rolle einnehmen'
                   WHEN 'partner/in_soll...chsel_einnehmen'
                     THEN 'Partner/in sollte devote oder dominante Rolle im Wechsel einnehmen'
                   WHEN 'ja,_auch__partn...rolle_einnehmen$1'
                     THEN 'Ja, auch - Partner/in sollte dominante Rolle einnehmen'
                   WHEN 'ja,_nur__partne...rolle_einnehmen$1'
                     THEN 'Ja, nur - Partner/in sollte dominante Rolle einnehmen'
                   WHEN 'ja,_auch__freun...rolle_einnehmen$1'
                     THEN 'Ja, auch - Freund/in sollte dominante Rolle einnehmen'
                   WHEN 'ja,_nur__freund...rolle_einnehmen$1'
                     THEN 'Ja, nur - Freund/in sollte dominante Rolle einnehmen'
                   ELSE i.i18n_value END
WHERE i.language = 'DE';

UPDATE choice_group SET name = 'BDSM-Initial(Freund)' WHERE legacy_id = 'freund.f_bdsm_suche';
UPDATE choice_group SET name = 'BDSM-Initial(Partner)' WHERE legacy_id = 'partner.p_bdsm_suche';
