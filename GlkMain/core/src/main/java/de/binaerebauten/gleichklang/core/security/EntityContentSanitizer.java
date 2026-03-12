package de.binaerebauten.gleichklang.core.security;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.util.ReflectionUtils;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity listener that sanitizes all string attributes of an entity that are marked
 * with the {@link SanitizeContent} annotation.
 *
 * Uses the OWASP java sanitizer project (https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project) for the actual sanitizing.
 * @see <a href="https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project">OWASP_Java_HTML_Sanitizer_Project</a>
 *
 */
public class EntityContentSanitizer
{
	public static final PolicyFactory SANITIZER =
			Sanitizers.FORMATTING
					.and(Sanitizers.BLOCKS)
					.and(Sanitizers.LINKS)
					.and(Sanitizers.STYLES)
					.and(Sanitizers.TABLES)
					.and(Sanitizers.IMAGES);

	/**
	 * Sanitizes all string attributes marked with the {@link SanitizeContent} annotation
	 * of the given entity.
	 *
	 * @param entity the entity to sanitize
	 */
	@PreUpdate
	@PrePersist
	@PostLoad
	public void sanitizeContent(Object entity)
	{
		List<Field> fieldsToSanitize = new ArrayList<>();

		ReflectionUtils.FieldFilter sanitizeContentFieldFilter =
				f -> f.getType() == String.class && f.getAnnotation(SanitizeContent.class) != null;

		ReflectionUtils.doWithFields(entity.getClass(), fieldsToSanitize::add, sanitizeContentFieldFilter);

		for (Field field : fieldsToSanitize)
		{
			ReflectionUtils.makeAccessible(field);

			String content = (String) ReflectionUtils.getField(field, entity);

			if (content != null) // only sanitize non-null content, because
			{
				// SANITIZER.sanitize will convert null to the empty string, this would change the content
				String sanitizedContent = SANITIZER.sanitize(content);

				ReflectionUtils.setField(field, entity, sanitizedContent);
			}
		}
	}
}
