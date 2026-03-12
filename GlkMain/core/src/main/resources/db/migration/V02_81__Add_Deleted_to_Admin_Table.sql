CALL DROP_COLUMN('admin', 'deleted');
CALL ADD_COLUMN('admin', 'deleted', 'BIT DEFAULT FALSE');

CALL ADD_FOREIGN_KEY('admin_roles', 'admin_id', 'admin', 'id');

CALL CHANGE_COLUMN('admin', 'password', 'password', 'VARCHAR(255) NOT NULL');