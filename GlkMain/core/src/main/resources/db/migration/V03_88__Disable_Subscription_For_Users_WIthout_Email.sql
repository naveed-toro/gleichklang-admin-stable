UPDATE subscription s
  JOIN user_ u ON s.user_id = u.id
SET s.current = NULL
WHERE s.current = TRUE AND u.email IS NULL;
