CALL ADD_COLUMN('user_news', 'viewed', 'BIT');

UPDATE user_news SET viewed = false;
UPDATE user_news SET hide = false WHERE hide IS NULL;

ALTER TABLE user_news MODIFY viewed BIT NOT NULL;
ALTER TABLE user_news MODIFY hide BIT NOT NULL;