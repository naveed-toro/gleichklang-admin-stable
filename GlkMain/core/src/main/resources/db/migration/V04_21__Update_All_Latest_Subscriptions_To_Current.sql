-- In V03_87__Migrate_Subscriptions_From_Scratch.sql all of the latest, non-expired subscriptions were set to current = 1
-- Here we update all latest subscriptions to current = 1, even if the subscriptions are expired.

UPDATE subscription SET current = NULL;

UPDATE subscription sub_update
SET sub_update.current = TRUE
WHERE sub_update.id IN
(
  SELECT id FROM
    (
      SELECT s1.id FROM
        subscription s1
      WHERE NOT EXISTS
      (
          SELECT 1 FROM
            subscription s2
          WHERE s2.end > s1.end and s1.user_id = s2.user_id
      )
    ) as tmp
);