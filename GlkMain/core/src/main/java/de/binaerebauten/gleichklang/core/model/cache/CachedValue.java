package de.binaerebauten.gleichklang.core.model.cache;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Created by Domi on 04.10.2016.
 */
@Entity
@Table(name = "cache")
public abstract class CachedValue<T> extends BaseEntity
{

    @NotNull
    @Column(name = "cache_key")
    private String cacheKey;

    @Column(name = "cache_group")
    private String cacheGroup;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getCacheGroup() {
        return cacheGroup;
    }

    public void setCacheGroup(String cacheGroup) {
        this.cacheGroup = cacheGroup;
    }

    public abstract T getValue();

    public abstract void setValue(T value);
}
