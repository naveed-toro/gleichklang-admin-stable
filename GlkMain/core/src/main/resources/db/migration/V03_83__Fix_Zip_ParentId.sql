UPDATE locatable AS z
  JOIN locatable AS r ON z.DTYPE = 'Zip' AND r.DTYPE = 'Region' AND z.parent_id = r.id
SET z.parent_id = r.parent_id;