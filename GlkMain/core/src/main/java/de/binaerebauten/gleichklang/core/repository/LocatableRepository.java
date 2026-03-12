package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPair;
import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by michael on 07/07/15.
 */
@Repository
public interface LocatableRepository extends JpaRepository<LocatableEntity, Long>
{
	@Query("SELECT s FROM LocatableEntity s WHERE TYPE(s) = ?1")
	<T extends LocatableEntity> List<T> findByType(Class<T> type);

	@Query("SELECT s FROM LocatableEntity s WHERE TYPE(s) = :type AND s.parent = :parent")
	<T extends LocatableEntity> List<T> findByParentAndType(@Param("parent") LocatableEntity locatableEntity,
			@Param("type") Class<T> type);

	@Query("SELECT DISTINCT c FROM Zip z, Country c WHERE z.parent = c AND c.parent = :continent")
	List<Country> findCountriesWithZips(@Param("continent") Continent continent);

	@Query("SELECT DISTINCT c2 FROM Zip z, Country c, Continent c2 WHERE z.parent = c AND c.parent = c2")
	List<Continent> findContinentWithZips();

	@Query("SELECT s FROM Zip AS s WHERE s.parent = :parent ORDER BY s.zip NULLS LAST ")
	List<Zip> findOrderedZips(@Param("parent") Country parent);

	// TODO @MW please add country code to the country entity and adapt this query
	@Query("FROM Country WHERE i18n_key = :countryCode")
	Country findByCountryCode(@Param("countryCode") String countryCode);

	@Query("SELECT s FROM Zip AS s WHERE s.region = :region ORDER BY s.zip")
	List<Zip> findOrderedZips(@Param("region") Region region);

	@Query("SELECT new de.binaerebauten.gleichklang.core.model.IdKeyPair(l.id, CONCAT(l.class, '_', l.i18nKey)) FROM LocatableEntity l WHERE TYPE(l) IS NOT Zip")
	List<IdKeyPairResult> getAllNonZipAsIdKeyPair();

    @Query("SELECT new de.binaerebauten.gleichklang.core.model.IdKeyPair(l.id, CONCAT(l.region.i18nKey, '_', l.zip)) FROM LocatableEntity l WHERE TYPE(l) IS Zip")
    List<IdKeyPairResult> getAllZipAsIdKeyPair();

	@Query(value = "SELECT lt.region_name,q.i18n_key FROM locatable lt " +
			"inner join region_search_request_restriction rsrr on lt.id=rsrr.locatable_id " +
			"inner join region_search_request rsr on rsr.id=rsrr.region_search_request_id " +
			"inner join answer a on a.id=rsr.answer_id " +
			"inner join question q on a.question_id=q.id " +
			"where a.user_id=?1 and a.DTYPE='RegionAnswer'",
			nativeQuery = true)
	List<Object> getSavedRegionSearchRequestBySourceUser(User user);

	@Query(value = "SELECT i18n_key FROM locatable where DTYPE='Region' order by i18n_key", nativeQuery = true)
	List<Object> getRegions();

	@Query(value = "select distinct CONCAT(continent2_.i18n_key ,':', country1_.i18n_key ) from locatable zip0_ " +
			       "cross join locatable country1_  cross join locatable continent2_ " +
				   "where zip0_.DTYPE='Zip' and country1_.DTYPE='Country' and continent2_.DTYPE='Continent' and zip0_.parent_id=country1_.id " +
				   "and country1_.parent_id=continent2_.id and zip0_.id =?1 ",
			       nativeQuery = true)
	Object getProximityContriesContinentsByZipId(Long zipIds);
}
