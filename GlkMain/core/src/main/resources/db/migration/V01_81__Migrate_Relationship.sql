CALL ADD_COLUMN('relationship', 'viewed', 'BIT NOT NULL');
CALL ADD_COLUMN('relationship', 'deleted', 'BIT');
UPDATE relationship SET deleted = true;
ALTER TABLE relationship MODIFY deleted BIT NOT NULL;

DELETE FROM relationship WHERE legacy_id IS NOT NULL;
INSERT INTO relationship (legacy_id, source_user_id, target_user_id, affiliation, category, viewed, deleted, change_date, create_date)
SELECT
    old_relationship.no,
    source_user.id,
    target_user.id,
    CASE old_relationship.ablage
        WHEN 'allgemein' THEN 'NEUTRAL'
        WHEN 'deleted' THEN 'NEUTRAL'
        WHEN 'minus' THEN 'NEGATIVE'
        WHEN 'plus' THEN 'POSITIVE'
    END,
    CASE old_relationship.rubrik
        WHEN 'p' THEN 'PARTNERSHIP'
        WHEN 'f' THEN 'FRIENDSHIP'
    END,
    CASE old_relationship.activitystate
        WHEN 'new' THEN false
        WHEN 'open' THEN true
    END,
    old_relationship.ablage = 'deleted',
    old_relationship.changedate,
    old_relationship.vorschlagsdatum
FROM compvorschlag old_relationship
INNER JOIN user_ source_user ON source_user.legacy_id = old_relationship.suchender
INNER JOIN user_ target_user ON target_user.legacy_id = old_relationship.vorschlag;
