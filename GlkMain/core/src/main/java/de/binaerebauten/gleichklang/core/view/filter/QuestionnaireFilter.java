package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QuestionnaireFilter implements Specification<Questionnaire>
{
	private final Collection<RecommendationCategory> categories;
	private final boolean withNullCategory;
	private final boolean withAdminVisible;

	/**
	 * Creates a new filter which filters questionnaires which are not deleted and have the specified categories.
	 *
	 * @param categories can contain null for NULL category
	 */
	public QuestionnaireFilter(Collection<RecommendationCategory> categories, boolean withAdminVisible)
	{
		this.categories = categories.stream().filter(Objects::nonNull).collect(Collectors.toList());
		this.withNullCategory = categories.contains(null);
		this.withAdminVisible = withAdminVisible;
	}

	@Override
	public Predicate toPredicate(Root<Questionnaire> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		final List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isFalse(root.get(Questionnaire_.deleted)));

		final Expression<RecommendationCategory> questionnaireCategoryExpression = root.get(Questionnaire_.recommendationCategory);

		Predicate categoriesFilter = null;

		if (!this.categories.isEmpty())
		{
			categoriesFilter = questionnaireCategoryExpression.in(categories);
		}

		if (withNullCategory)
		{
			final Predicate nullCategoryFilter = questionnaireCategoryExpression.isNull();

			categoriesFilter = categoriesFilter == null ? nullCategoryFilter :
					cb.or(categoriesFilter, nullCategoryFilter);
		}
		Objects.requireNonNull(categoriesFilter, "categoriesFilter == null");

		predicates.add(categoriesFilter);
		
		final Subquery<Question> subQuery = query.subquery(Question.class);
		final Root<Question> question = subQuery.from(Question.class);
		subQuery.select(question);
		
		final Join<Question, QuestionGroup> questionGroupJoin = question.join(Question_.questionGroup);
		final Join<QuestionGroup, Questionnaire> questionnaireJoin = questionGroupJoin.join(QuestionGroup_.questionnaire);
		
		Predicate notDeleted = cb.and(
				cb.equal(question.get(Question_.deleted), false),
				cb.equal(questionGroupJoin.get(QuestionGroup_.deleted), false),
				cb.equal(questionnaireJoin.get(Questionnaire_.deleted), false),
				cb.equal(questionnaireJoin, root));
		
		if(!withAdminVisible)
		{
			notDeleted = cb.and(notDeleted, cb.equal(question.get(Question_.onlyAdminVisible), false));
		}
		
		predicates.add(cb.exists(subQuery.where(notDeleted)));
		
		return predicates.stream().reduce(cb::and).orElseGet(null);
	}
}
