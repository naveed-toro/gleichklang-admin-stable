UPDATE question AS q JOIN choice_group as cg ON q.choice_group_id = cg.id
SET q.representation_type = 'RADIO'
WHERE cg.name = 'Ja/Nein';