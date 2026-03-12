-- update restrictions
UPDATE region_search_request rsr
    JOIN region_search_request rsr_delete ON rsr_delete.answer_id = rsr.answer_id
    JOIN region_search_request_restriction rsrr_update ON rsrr_update.region_search_request_id = rsr_delete.id
SET
    rsrr_update.region_search_request_id = rsr.id
WHERE
    rsr.id < rsr_delete.id AND
    rsr.country_id = rsr_delete.country_id AND
    NOT EXISTS (SELECT 1 FROM region_search_request rsr2 WHERE rsr2.country_id = rsr.country_id AND rsr2.answer_id = rsr.answer_id AND rsr2.id < rsr.id);

-- delete remaining search requests
DELETE rsr . * FROM region_search_request rsr
        JOIN
    region_search_request rsr_delete ON rsr_delete.id = rsr.id
        JOIN
    region_search_request rsr_answer ON rsr_answer.answer_id = rsr.answer_id
WHERE
    rsr.id < rsr_delete.id
    AND rsr.country_id = rsr_delete.country_id
    AND rsr_answer.country_id != rsr.country_id
    AND rsr_answer.answer_id != rsr.answer_id
    AND rsr_answer.id >= rsr.id