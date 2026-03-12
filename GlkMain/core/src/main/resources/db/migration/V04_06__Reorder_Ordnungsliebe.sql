SET @nein=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_nein');
SET @eher_nein=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_eher_nein');
SET @eher_ja=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_eher_ja');
SET @ja=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_ja');

UPDATE answer a LEFT JOIN question q ON a.question_id = q.id JOIN choice_answer ca ON ca.answer_id = a.id
SET ca.choice_id = CASE ca.choice_id
     WHEN @eher_ja
       THEN @eher_nein
     WHEN @eher_nein
        THEN @eher_ja
     WHEN @ja
        THEN @nein
     WHEN @nein
        THEN @ja
     ELSE ca.choice_id END
WHERE q.i18n_key = 'person.ordnungsliebe';