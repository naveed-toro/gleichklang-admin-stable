DELETE FROM relationship_category WHERE relationship_id IN (SELECT r_id FROM (SELECT MAX(r.id) AS r_id, count(1) AS cnt FROM relationship r GROUP BY r.source_user_id, r.target_user_id HAVING cnt > 1) AS double_entries);
DELETE FROM relationship WHERE id IN (SELECT r_id FROM (SELECT MAX(r.id) AS r_id, count(1) AS cnt FROM relationship r GROUP BY r.source_user_id, r.target_user_id HAVING cnt > 1) AS double_entries);

CALL CREATE_UNIQUE_INDEX('source_user_id_target_user_id', 'relationship', 'source_user_id, target_user_id');