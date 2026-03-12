UPDATE question AS q
SET q.max_length    = 65535,
  q.number_of_lines = 3
WHERE q.DTYPE = 'TextQuestion';