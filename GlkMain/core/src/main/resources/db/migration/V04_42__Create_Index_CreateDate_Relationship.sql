-- for match-statistics @see RelationshipRepository#countByCreateDateBetween
CALL CREATE_INDEX('relationship_createdate_idx', 'relationship', 'create_date');