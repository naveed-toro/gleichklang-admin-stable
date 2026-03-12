package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix;
import de.binaerebauten.gleichklang.core.repository.ActivatorRepository;
import de.binaerebauten.gleichklang.core.repository.MatrixRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionMappingRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import java.util.Objects;

@Service
public class SetupMatchingService
{
	@Autowired
	private QuestionMappingRepository questionMappingRepository;

	@Autowired
	private MatrixRepository matrixRepository;

	@Autowired
	private ActivatorRepository activatorRepository;

	@CheckedTransactional
	public AbstractQuestionsMapping saveQuestionsMapping(AbstractQuestionsMapping questionsMapping)
			throws ValidationException
	{
		return saveAndFlush(questionsMapping, questionMappingRepository);
	}

	@CheckedTransactional
	public void deleteMatrix(MatchingMatrix matchingMatrix) throws ValidationException
	{
		Objects.requireNonNull(matchingMatrix);
		try
		{
			matrixRepository.delete(matchingMatrix);
			matrixRepository.flush(); // triggers DB constraints and allows us to wrap the spring exception
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new ValidationException(e.getMessage());
		}
	}

	@CheckedTransactional
	public MatchingMatrix saveMatrix(MatchingMatrix matchingMatrix) throws ValidationException
	{
		return saveAndFlush(matchingMatrix, matrixRepository);
	}

	@CheckedTransactional
	public Activator saveActivator(Activator activator) throws ValidationException
	{
		return saveAndFlush(activator, activatorRepository);
	}

	private <T> T saveAndFlush(T entity, JpaRepository<T, Long> repository) throws ValidationException
	{
		try
		{
			return repository.saveAndFlush(entity);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(I18N.SETUPMATCHINGSERVICE_VALIDATION_UNIQUE.msg());
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
}