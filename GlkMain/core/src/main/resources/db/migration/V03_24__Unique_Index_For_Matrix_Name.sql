UPDATE matching_matrix mm1 JOIN matching_matrix mm2 ON mm1.name = mm2.name AND mm1.id > mm2.id SET mm1.name = CONCAT(mm1.name, mm1.id);

CALL CREATE_UNIQUE_INDEX('matching_matrix_name', 'matching_matrix', 'name');