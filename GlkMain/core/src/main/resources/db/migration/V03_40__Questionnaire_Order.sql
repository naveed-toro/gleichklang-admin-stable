
SET FOREIGN_KEY_CHECKS = 0;

UPDATE questionnaire SET sort_order = sort_order + 100;

# UPDATE questionnaire SET sort_order = NULL;
UPDATE questionnaire SET sort_order = 1 WHERE i18n_key = 'partner';
UPDATE questionnaire SET sort_order = 2 WHERE i18n_key = 'partner.region_question';
UPDATE questionnaire SET sort_order = 3 WHERE i18n_key = 'partnerschaft';
UPDATE questionnaire SET sort_order = 4 WHERE i18n_key = 'ptext';
UPDATE questionnaire SET sort_order = 5 WHERE i18n_key = 'freund';
UPDATE questionnaire SET sort_order = 6 WHERE i18n_key = 'friend.region_question';
UPDATE questionnaire SET sort_order = 7 WHERE i18n_key = 'freundschaft';
UPDATE questionnaire SET sort_order = 8 WHERE i18n_key = 'ftext';
UPDATE questionnaire SET sort_order = 9 WHERE i18n_key = 'person';
UPDATE questionnaire SET sort_order = 10 WHERE i18n_key = 'aussehen';
UPDATE questionnaire SET sort_order = 11 WHERE i18n_key = 'hobby_v';
UPDATE questionnaire SET sort_order = 12 WHERE i18n_key = 'persoenlichkeit';
UPDATE questionnaire SET sort_order = 13 WHERE i18n_key = 'gesellschaft';
UPDATE questionnaire SET sort_order = 14 WHERE i18n_key = 'registration';
UPDATE questionnaire SET sort_order = 15 WHERE i18n_key = 'stammdaten';

SELECT max(sort_order) INTO @sort_order from questionnaire where sort_order < 100;
SELECT @sort_order;
UPDATE questionnaire SET sort_order = @sort_order:= @sort_order + 1 WHERE sort_order > 100;

SET FOREIGN_KEY_CHECKS = 1;

UPDATE questionnaire SET visible = FALSE WHERE i18n_key = 'stammdaten';