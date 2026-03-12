CALL ADD_COLUMN('media_gallery', 'secret', 'BIT');

UPDATE media_gallery SET secret = false;

ALTER TABLE media_gallery MODIFY secret BIT NOT NULL;

CREATE TABLE IF NOT EXISTS media_gallery_visible_relationship
(
    media_gallery_id BIGINT NOT NULL,
    relationship_id  BIGINT NOT NULL,
    FOREIGN KEY (media_gallery_id) REFERENCES media_gallery(id),
    FOREIGN KEY (relationship_id)  REFERENCES relationship(id)
);

