CALL ADD_COLUMN('media', 'deleted', 'BIT');
CALL ADD_COLUMN('media_gallery', 'deleted', 'BIT');

UPDATE media SET deleted = false;
UPDATE media_gallery SET deleted = false;

ALTER TABLE media MODIFY deleted BIT NOT NULL;
ALTER TABLE media_gallery MODIFY deleted BIT NOT NULL;