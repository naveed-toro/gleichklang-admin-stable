CALL ADD_COLUMN('question', 'adjustable_relationship_visibility', 'BIT');
CALL ADD_COLUMN('question', 'only_admin_visible', 'BIT');
CALL ADD_COLUMN('question', 'visible', 'BIT');

UPDATE question SET adjustable_relationship_visibility = false;
UPDATE question SET only_admin_visible = NOT visible WHERE visible IS NOT NULL;

ALTER TABLE question MODIFY adjustable_relationship_visibility BIT NOT NULL;
ALTER TABLE question MODIFY only_admin_visible BIT NOT NULL;

CALL DROP_COLUMN('question', 'visible');
