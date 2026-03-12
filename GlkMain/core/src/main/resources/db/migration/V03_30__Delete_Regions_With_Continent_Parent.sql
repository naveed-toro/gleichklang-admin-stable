DELETE l1
FROM locatable AS l1
  JOIN locatable AS l2 ON l1.parent_id = l2.id
WHERE l1.DTYPE = 'Region' AND l2.DTYPE != 'Country';