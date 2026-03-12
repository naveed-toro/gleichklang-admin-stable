DELETE FROM user_news WHERE id IN (SELECT un_id FROM (SELECT MAX(un.id) AS un_id, count(1) AS cnt FROM user_news un GROUP BY un.user_id, un.news_id HAVING cnt > 1) AS double_entries);

CALL CREATE_UNIQUE_INDEX('user_id_news_id', 'user_news', 'user_id, news_id');