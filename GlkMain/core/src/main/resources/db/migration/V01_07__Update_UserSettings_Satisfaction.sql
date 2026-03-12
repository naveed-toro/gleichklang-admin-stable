UPDATE user_settings
SET
  satisfactionGleichklang = CASE satisfactionGleichklang
                            WHEN '004'
                              THEN 'SATISFIED'
                            WHEN '003'
                              THEN 'RATHER_SATISFIED'
                            WHEN '002'
                              THEN 'RATHER_UNSATISFIED'
                            WHEN '001'
                              THEN 'UNSATISFIED'
                            ELSE
                              'NO_JUDGE'
                            END,
  satisfactionOther       = CASE satisfactionOther
                            WHEN '004'
                              THEN 'SATISFIED'
                            WHEN '003'
                              THEN 'RATHER_SATISFIED'
                            WHEN '002'
                              THEN 'RATHER_UNSATISFIED'
                            WHEN '001'
                              THEN 'UNSATISFIED'
                            ELSE
                              'NO_JUDGE'
                            END;
