package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.cache.CachedValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing Cached Values
 */
@Repository
public interface CachedValueRepository extends JpaRepository<CachedValue, Long> {

    List<CachedValue> findByCacheGroup(String cacheGroup);

    List<CachedValue> findByCacheKey(String cacheKey);

    List<CachedValue> findByCacheKeyAndCacheGroup(String cacheKey, String cacheGroup);
}
