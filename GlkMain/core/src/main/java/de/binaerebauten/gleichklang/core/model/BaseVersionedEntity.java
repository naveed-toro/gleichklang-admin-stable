package de.binaerebauten.gleichklang.core.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * This class extends the {@link BaseEntity} with a version attribute which enables optimistic locking of entities.
 */
@MappedSuperclass
public class BaseVersionedEntity extends BaseEntity
{
    @Version
    private long version;
}
