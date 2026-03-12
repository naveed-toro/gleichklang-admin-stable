CREATE TEMPORARY TABLE IF NOT EXISTS tmp_roles (role VARCHAR(255));
DELETE FROM admin_roles;

INSERT INTO tmp_roles VALUES
  ('QUESTIONNAIRE'),
  ('MATCHING'),
  ('BANK_ACCOUNTS'),
  ('TRANSLATION'),
  ('SUBSCRIPTIONS'),
  ('INVOICE'),
  ('USER_CONTROL'),
  ('FILTER'),
  ('NEWS'),
  ('ADMIN_MANAGEMENT'),
  ('PRODUCTS');

INSERT INTO admin_roles(admin_id, roles)
    SELECT distinct u.id, tr.role FROM user_ u, tmp_roles tr WHERE u.DTYPE = 'Admin';