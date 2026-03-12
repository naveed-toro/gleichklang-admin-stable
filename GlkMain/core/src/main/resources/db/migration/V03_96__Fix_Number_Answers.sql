UPDATE answer a
  LEFT JOIN question q ON q.id = a.question_id
SET a.number_value = NULL
WHERE a.number_value = 0 AND
      q.i18n_key IN ('partner.p_minalter', 'partner.p_maxalter',
                     'partner.p_mingroesse', 'partner.p_maxgroesse',
                     'freund.f_minalter', 'freund.f_maxalter',
                     'aussehen.gewicht');
