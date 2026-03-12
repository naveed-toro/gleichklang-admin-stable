CREATE TABLE IF NOT EXISTS relationship_category
(
    category        VARCHAR(255) NOT NULL,
    relationship_id BIGINT NOT NULL,
    FOREIGN KEY (relationship_id) REFERENCES relationship (id),
    PRIMARY KEY (category, relationship_id)
);

CALL ADD_COLUMN('relationship', 'category', 'VARCHAR(255)');

DELETE r1 FROM relationship r1 JOIN relationship r2 ON  r1.source_user_id = r2.source_user_id AND r1.target_user_id = r2.target_user_id
    WHERE
    r1.category = 'FRIENDSHIP' AND
    r2.category = 'PARTNERSHIP';

INSERT INTO relationship_category (relationship_id, category)
    SELECT  r.id, r.category
    FROM relationship r
    WHERE
        NOT EXISTS (SELECT * FROM relationship_category rc WHERE rc.relationship_id = r.id AND rc.category = r.category);

CALL DROP_COLUMN('relationship', 'category');