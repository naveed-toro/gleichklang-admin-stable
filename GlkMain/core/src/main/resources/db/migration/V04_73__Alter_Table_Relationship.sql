ALTER TABLE relationship
  ADD column  first_viewed DATETIME DEFAULT NULL;
  commit;

ALTER TABLE relationship
  ADD column  delete_date DATETIME DEFAULT NULL;
  commit;