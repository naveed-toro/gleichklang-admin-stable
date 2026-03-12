package de.binaerebauten.gleichklang.core.repository.systemconfig;

import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.repository.DeleteRepository;
import de.binaerebauten.gleichklang.core.repository.NaturalKeyRepository;
import de.binaerebauten.gleichklang.core.repository.SortRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//TODO Task 3 changes
@Repository
public interface EmailDomainMappingRepository extends  JpaRepository<EmailDomainMapping, Long>, JpaSpecificationExecutor<EmailDomainMapping>, DeleteRepository<EmailDomainMapping, Long>
{
	@Modifying
	@Transactional
	@Query(value = "UPDATE email_domain_mapping SET mapping_name = :mappingName,mapping_value = :mappingValue , active = :active WHERE id= :id"
			, nativeQuery = true)
	void updateEmailDomainMapping(@Param("mappingName")String mappingName, @Param("mappingValue")String mappingValue, @Param("active")boolean active, @Param("id")Long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE email_domain_mapping SET active = :active WHERE id= :id"
			, nativeQuery = true)
	void updateActiveEmailDomainMapping(@Param("active")boolean active , @Param("id")Long id);

/*	// Query to check how many profiles has the current logged in user seen */
	@Query("SELECT count(*) FROM EmailDomainMapping e WHERE e.mappingName = :mappingName")
	Long checkIfEmailDomainMappingNameExists(@Param("mappingName") String mappingName);

	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingValue LIKE :mappingValue") JPA Query, No annotation required
	List<EmailDomainMapping> findByMappingValueContaining(@Param("mappingValue") String mappingValue);


	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingValue LIKE :mappingValue") JPA Query, No annotation required
	List<EmailDomainMapping> findByActiveTrueAndMappingValueContaining(@Param("mappingValue") String mappingValue);


	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingName = :mappingName") JPA Query, No annotation required
	List<EmailDomainMapping> findByMappingName(@Param("mappingName") String mappingName);

	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingName = :mappingName") JPA Query, No annotation required
	@Modifying
	@Transactional
	void deleteById(@Param("id") Long id);




}
