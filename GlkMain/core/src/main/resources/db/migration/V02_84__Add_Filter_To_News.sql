CALL ADD_COLUMN('news', 'filter_id', 'BIGINT');

CALL ADD_FOREIGN_KEY('news', 'filter_id', 'filter', 'id');