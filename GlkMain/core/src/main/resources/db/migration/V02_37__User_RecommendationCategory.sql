DROP TABLE IF EXISTS user_recommendation_category;

CREATE TABLE user_recommendation_category
(
  user_id  BIGINT       NOT NULL,
  categories VARCHAR(255) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ (id),
  PRIMARY KEY (user_id, categories)
);

INSERT INTO user_recommendation_category
  SELECT
    u.id,
    'PARTNERSHIP'
  FROM user_ AS u
    JOIN (
      SELECT cu.no, cu.teilnahmeart FROM compuser AS cu WHERE cu.teilnahmeart LIKE '%-p%')
      AS cu ON u.legacy_id = cu.no;

INSERT INTO user_recommendation_category
  SELECT
    u.id,
    'FRIENDSHIP'
  FROM user_ AS u
    JOIN (
           SELECT cu.no, cu.teilnahmeart FROM compuser AS cu WHERE cu.teilnahmeart LIKE '%-f%')
      AS cu ON u.legacy_id = cu.no;
