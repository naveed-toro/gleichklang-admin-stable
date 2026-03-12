package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.config.MigrationPersistenceTestConfig;
import de.binaerebauten.gleichklang.core.config.RootTestConfig;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.model.BaseMigrationTest.EmptyConstraintsValidationMatcher.noConstraintViolations;

/**
 * Base class for migration test. The migration test classes have to conform to the naming scheme *MigrationTest.java,
 * otherwise they won't be run by maven!
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RootTestConfig.class, MigrationPersistenceTestConfig.class })
public abstract class BaseMigrationTest
{
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private Validator validator;

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	protected abstract List<Class<?>> getEntityClasses();

	protected SingularAttribute<?, ?> getIdAttribute(Class<?> entityClass)
	{
		Metamodel metamodel = entityManager.getMetamodel();
		EntityType<?> entityType = metamodel.entity(entityClass);
		Class<?> idJavaType = entityType.getIdType().getJavaType();
		return entityType.getId(idJavaType);
	}

	@Test
	public void testValidateAllEntities()
	{
		for (Class<?> entityClass : getEntityClasses())
		{
			SingularAttribute<?, ?> idAttribute = getIdAttribute(entityClass);

			String idQueryString = String.format("select %s from %s", idAttribute.getName(), entityClass.getSimpleName());
			TypedQuery<?> idQuery = entityManager.createQuery(idQueryString, idAttribute.getJavaType());

			List<?> idQueryResultList = idQuery.getResultList();

			for (Object id : idQueryResultList)
			{
				try
				{
					Object entity = entityManager.find(entityClass, id);
					validate(entity, id);
				}
				catch (DataIntegrityViolationException e)
				{
					errorCollector.addError(e);
				}
			}
		}
	}

	/**
	 * Override for additional validations, this implementation validates the given entity
	 * via the {@link #validator}.
	 *
	 * @param entity
	 * @param id
	 */
	protected void validate(Object entity, Object id)
	{
		Set<ConstraintViolation<Object>> validate = validator.validate(entity);
		Class<?> entityClass = entity.getClass();
		SingularAttribute<?, ?> idAttribute = getIdAttribute(entityClass);
		errorCollector.checkThat(validate, noConstraintViolations(entityClass, idAttribute, id));
	}

	/**
	 * This matcher checks if the given set of {@link ConstraintViolation}s is emoty.
	 * If it's not empty the failed constraint violations will be reported.
	 */
	protected static class EmptyConstraintsValidationMatcher
			extends TypeSafeDiagnosingMatcher<Set<ConstraintViolation<Object>>>
	{
		private final Class<?> entityClass;
		private final SingularAttribute<?, ?> idAttribute;
		private final Object id;

		protected EmptyConstraintsValidationMatcher(Class<?> entityClass, SingularAttribute<?, ?> idAttribute, Object id)
		{
			this.entityClass = entityClass;
			this.idAttribute = idAttribute;
			this.id = id;
		}

		public static EmptyConstraintsValidationMatcher noConstraintViolations(Class<?> entityClass, SingularAttribute<?, ?> idAttribute, Object id)
		{
			return new EmptyConstraintsValidationMatcher(entityClass, idAttribute, id);
		}

		@Override
		protected boolean matchesSafely(Set<ConstraintViolation<Object>> item, Description mismatchDescription)
		{
			if (!item.isEmpty())
			{
				mismatchDescription.appendText("validation failed with the following violations:");
				List<String> constraintMessages = item.stream()
						.map(v -> String.format("%s (%s='%s')",
								v.getMessage(),
								v.getPropertyPath(),
								v.getInvalidValue()))
						.collect(Collectors.toList());
				mismatchDescription.appendValueList("[", ",", "]", constraintMessages);
				return false;
			}
			return true;
		}

		@Override
		public void describeTo(Description description)
		{
			description.appendText("no constraint violations for ")
					.appendValue(entityClass.getSimpleName())
					.appendText(" with ")
					.appendValue(idAttribute.getName()).appendText(" = ").appendValue(id);
		}
	}
}
