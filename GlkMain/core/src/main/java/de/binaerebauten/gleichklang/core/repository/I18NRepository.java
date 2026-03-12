package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Repository
public interface I18NRepository extends JpaRepository<I18NEntity, Long>, JpaSpecificationExecutor<I18NEntity>
{
	/**
	 * Finds all I18N objects for the given base name and language.
	 *
	 * @param baseName the base name
	 * @param language the language as returned by {@link Locale#getLanguage()}
	 *
	 * @return the list of I18N objects for the given parameters
	 */
	List<I18NEntity> findByBaseNameAndLanguage(BaseName baseName, Language language);

	/**
	 * Finds I18N objects for the given base name and given key for all languages.
	 *
	 * @param baseName the base name
	 * @param key      the key
	 * @return the list of I18N objects for the given parameters
	 */
	// TODO rueckgabetypen vereinheitlich (Set oder List)
	List<I18NEntity> findByBaseNameAndKey(BaseName baseName, String key);

	/**
	 * Finds I18N objects for the given base name and given key for all languages.
	 *
	 * @param key      the key
     * @param language the language as returned by {@link Locale#getLanguage()}
	 * @return the I18N object for the given parameters
	 */
	I18NEntity findByKeyAndLanguage(String key, Language language);

	/**
	 * Returns the last change date for any I18NEntity of the given parameters.
	 *
	 * @param baseName the base name
	 * @param language the language
	 * @return the last change date or null
	 */
	@Query("SELECT MAX(changeDate) FROM I18NEntity i WHERE i.baseName = :baseName AND i.language = :language GROUP BY i.baseName, i.language") LocalDateTime getLastChangeDate(@Param("baseName") BaseName baseName, @Param("language") Language language);


    /**
     * Returns list of I18N objects for given keys in a specified BaseName group.
     *
     * @param baseName base name group
     * @param keys list of i18n keys
     * @return list of I18N entities
     */
    @Query("SELECT i FROM I18NEntity i WHERE i.baseName = :baseName AND i.key IN (:keys)")
    List<I18NEntity> getEntitiesForBaseNameAndKeys(@Param("baseName") BaseName baseName, @Param("keys") List<String> keys);

	@Query("SELECT NEW de.binaerebauten.gleichklang.core.model.UniqueI18NIdKeyPair(i.baseName, i.id, i.key, i.language) FROM I18NEntity i")
	List<IdKeyPairResult> getAllAsIdKeyPair();

	@Modifying
    @Transactional
	@Query("DELETE FROM I18NEntity i WHERE i.baseName = :baseName AND i.key IN (:keys)")
	void deleteTranslationKeysForBaseName(@Param("keys") List<String> keys, @Param("baseName") BaseName baseName);

	@Query(value = "select i18n_value from i18n i where i.i18n_key in(\n" +
			"SELECT c.i18n_key FROM choice c \n" +
			"inner join choice_answer ca on  c.id=ca.choice_id\n" +
			"inner join answer a on a.id=ca.answer_id \n" +
			"inner join question q on q.id=a.question_id\n" +
			"where a.user_id = ?1 and a.DTYPE='ChoiceAnswer' and q.i18n_key='satisfaction')", nativeQuery = true)
	List<Object> satisfactionWithHarmony(long user_id);

	@Query(value = "select i18n_value from i18n i where i.language=?2 and i.i18n_key in ( " +
			" SELECT c.i18n_key FROM choice c " +
			" inner join choice_answer ca on  c.id=ca.choice_id " +
			" inner join answer a on a.id=ca.answer_id " +
			" inner join question q on q.id=a.question_id " +
			" where a.user_id = ?1 and a.DTYPE='ChoiceAnswer' and q.i18n_key='satisfaction') ", nativeQuery = true)
	String satisfactionWithHarmony(long user_id, String language);

	@Query(value = "select i.i18n_value from i18n i where i.i18n_key in(\n" +
			"SELECT c.i18n_key FROM choice c \n" +
			"inner join choice_answer ca on  c.id=ca.choice_id\n" +
			"inner join answer a on a.id=ca.answer_id \n" +
			"inner join question q on q.id=a.question_id \n" +
			"where a.user_id=?1 and a.DTYPE='ChoiceAnswer' and q.i18n_key='Erfolg der Vermittlung') limit 1 ", nativeQuery = true)
	String successOfMediation(long user_id);

	@Query(value = "select i.i18n_value from i18n i where i.i18n_key in(\n" +
			"SELECT c.i18n_key FROM choice c \n" +
			"inner join choice_answer ca on  c.id=ca.choice_id\n" +
			"inner join answer a on a.id=ca.answer_id \n" +
			"inner join question q on q.id=a.question_id \n" +
			"where a.user_id=?1 and a.DTYPE='ChoiceAnswer' and q.i18n_key='Zufriedenheit mit anderen Plattformen')", nativeQuery = true)
	String successOfOtherPlatform(long user_id);
}
