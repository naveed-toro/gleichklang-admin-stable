UPDATE i18n
SET i18n_value = '<b>Test Produkt</b>, Laufzeit 1 Stunde für 25.00 EUR'
WHERE i18n_key = 'Test_Test P+F';

UPDATE product SET duration_unit ='HOURS'
WHERE name = 'Test P+F';
