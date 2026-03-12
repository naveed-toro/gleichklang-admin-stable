package de.binaerebauten.gleichklang.core.repository.systemconfig;


import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.repository.DeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//TODO Task 3 changes
@Service
@Repository
public interface EmailTemplateRepository extends  JpaRepository<EmailTemplateMapping, Long>, JpaSpecificationExecutor<EmailTemplateMapping>
{
	@Modifying
	@Transactional
	@Query(value = "UPDATE email_template_mapping SET template_text = :templateText , template_description = :templateDescription WHERE id= :id"
			, nativeQuery = true)
	void updateEmailTemplate(@Param("templateText") String templateText,@Param("templateDescription") String templateDescription, @Param("id") Long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE email_template_mapping SET active = :active WHERE id= :id"
			, nativeQuery = true)
	void updateActiveEmailTemplate(@Param("active") boolean active, @Param("id") Long id);


	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingName = :mappingName") //JPA Query, No annotation required
	List<EmailTemplateMapping> findByTemplateNameAndTemplateLanguageAndActiveTrue(@Param("templateName") String mappingName, @Param("templateLanguage") String templateLanguage);

	List<EmailTemplateMapping> findByTemplateDescriptionAndTemplateLanguage(@Param("templateDescription") String templateDescription, @Param("templateLanguage") String templateLanguage);

	List<EmailTemplateMapping> findById(@Param("id") Long id);

/*	// Query to check how many profiles has the current logged in user seen */
/*	@Query("SELECT count(*) FROM EmailDomainMapping e WHERE e.mappingName = :mappingName")
	Long checkIfEmailDomainMappingNameExists(@Param("mappingName") String mappingName);

	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingValue LIKE :mappingValue") JPA Query, No annotation required
	List<EmailTemplateMapping> findByMappingValueContaining(@Param("mappingValue") String mappingValue);


	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingValue LIKE :mappingValue") JPA Query, No annotation required
	List<EmailTemplateMapping> findByActiveTrueAndMappingValueContaining(@Param("mappingValue") String mappingValue);


	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingName = :mappingName") JPA Query, No annotation required
	List<EmailTemplateMapping> findByMappingName(@Param("mappingName") String mappingName);

	//@Query("SELECT * FROM EmailDomainMapping e WHERE e.mappingName = :mappingName") JPA Query, No annotation required
	@Modifying
	@Transactional
	void deleteById(@Param("id") Long id);

*/


}
