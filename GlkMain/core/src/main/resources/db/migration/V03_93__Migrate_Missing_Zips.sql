UPDATE address a
    LEFT JOIN user_ u ON u.id = a.user_id
    LEFT JOIN compuser cu ON u.legacy_id = cu.no
    JOIN locatable z ON z.zip = cu.plz AND z.parent_id = a.country_id
SET
    a.zip_id = z.id,
    a.region_id = z.region_id
WHERE
    a.payment = true AND a.region_id IS NULL and a.zip_id IS NULL;