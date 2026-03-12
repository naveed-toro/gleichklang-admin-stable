package de.binaerebauten.gleichklang.core.model;

/**
 * Project: Import Export
 * Created by Domi on 22.06.2016.
 */
public class PersistedEntityIdentifier {
    private Long persistedId;
    private Class persistedType;

    public PersistedEntityIdentifier(Long persistedId, Class persistedType) {
        this.persistedId = persistedId;
        this.persistedType = persistedType;
    }

    public Long getPersistedId() {
        return persistedId;
    }

    public Class getPersistedType() {
        return persistedType;
    }
}
