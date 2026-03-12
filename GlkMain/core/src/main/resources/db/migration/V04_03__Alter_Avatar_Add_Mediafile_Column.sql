CALL ADD_COLUMN('avatar', 'mediafile_id', 'BIGINT');
CALL ADD_FOREIGN_KEY('avatar', 'mediafile_id', 'file', 'id');