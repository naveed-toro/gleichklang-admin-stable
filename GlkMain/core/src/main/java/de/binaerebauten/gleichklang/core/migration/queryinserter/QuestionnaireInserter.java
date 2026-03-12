package de.binaerebauten.gleichklang.core.migration.queryinserter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.ChoiceGroupSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.migration.querybuilder.ChoiceValueSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.migration.querybuilder.LegacyChoiceSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionGroupSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionnaireSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.model.legacy.persistence.Attribute;
import de.binaerebauten.gleichklang.core.model.legacy.persistence.Component;
import de.binaerebauten.gleichklang.core.model.legacy.persistence.Option;
import de.binaerebauten.gleichklang.core.model.legacy.persistence.Persistence;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion.SelectionType;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by michael on 29/04/15.
 */
public class QuestionnaireInserter extends BaseInserter
{

	private static final Logger LOG = LoggerFactory.getLogger(QuestionnaireInserter.class);
	private final TranslationPropertiesManager translationPropertiesManager;
	private final Map<String, MigrationChoiceGroup> choiceGroupIdMap = new HashMap<>();
	private final HashMultimap<String, MigrationQuestionGroup> questionGroupIdentityMap = HashMultimap.create();
	private final HashMultimap<String, MigrationChoiceValue> choiceValueIdentityMap = HashMultimap.create();
	private Map<MigrationQuestionGroup, Integer> questionLastSortOrder;

	public QuestionnaireInserter(JdbcTemplate jdbcTemplate)
	{
		super(jdbcTemplate);
		translationPropertiesManager = new TranslationPropertiesManager("CHOICE_VALUE", Locale.GERMAN);
	}

	public void insertQuestionnaires(InputStream inputStream)
	{
		final Persistence persistence = XMLParser.unmarshalXML(Persistence.class, inputStream);
		if (persistence == null)
		{
			return;
		}

		int questionnaireSortOrder = 0;
		for (final Component component : persistence.getComponents().getComponent())
		{
			final String questionnaireName = component.getName();
			if ("QuestionaireService".equals(component.getService()) && isRelevantQuestionnaire(questionnaireName))
			{
				LOG.info("Inserting questionnaire: {}", questionnaireName);
				MigrationQuestionnaire questionnaire = insertQuestionnaire(questionnaireName, questionnaireSortOrder);
				insertQuestions(component, questionnaire);
				questionnaireSortOrder++;
			}
		}
	}

	private boolean isRelevantQuestionnaire(String questionnaireName)
	{
		List<String> irrelevantQuestionnaires = Lists.newArrayList("hobby_e", "kritische_lebensereignisse", "adjektive",
				"proband_socio_economics");
		return !(questionnaireName.startsWith("ext_") || irrelevantQuestionnaires.contains(questionnaireName));
	}

	private boolean isRelevantQuestion(String questionName)
	{
		List<String> irrelevantQuestionNames = Arrays.asList("partner.umzug", "partner.p_region_umzug");
		return !irrelevantQuestionNames.contains(questionName);
	}

	private MigrationQuestionnaire insertQuestionnaire(String questionnaireName, int sortOrder)
	{
		final MigrationQuestionnaire questionnaire = new MigrationQuestionnaire(questionnaireName, sortOrder);
		RecommendationCategory recommendationCategory = getRecommendationCategory(questionnaireName);
		questionnaire.setRecommendationCategory(recommendationCategory);
		String questionnaireInsert = new QuestionnaireSQLQueryBuilder().getInsertStatementFor(questionnaire);

		executeOne(questionnaireInsert);
		return questionnaire;
	}

	private RecommendationCategory getRecommendationCategory(String questionnaireName)
	{
		List<String> partnershipQuestionnaires = Arrays.asList("partner", "ptext", "partnerschaft");
		List<String> friendshipQuestionnaires = Arrays.asList("freund", "ftext", "freundschaft");
		if (partnershipQuestionnaires.contains(questionnaireName))
		{
			return RecommendationCategory.PARTNERSHIP;
		}
		if (friendshipQuestionnaires.contains(questionnaireName))
		{
			return RecommendationCategory.FRIENDSHIP;
		}
		return null;
	}

	private MigrationQuestionGroup insertQuestionGroup(String questionGroupName, int questionGroupSortOrder,
			MigrationQuestionnaire questionnaire)
	{
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup(questionGroupName, questionGroupSortOrder, questionnaire);

		String oldKey = questionGroup.getI18nKey();
		Set<MigrationQuestionGroup> migrationQuestionGroups = questionGroupIdentityMap.get(oldKey);

		Integer number = migrationQuestionGroups.size();
		if (number != 0)
		{
			questionGroup.setI18nKey(questionGroup.getI18nKey() + "$" + number);
			questionGroup.setLegacyId(migrationQuestionGroups.iterator().next().getLegacyId());
		}

		questionGroupIdentityMap.put(oldKey, questionGroup);

		String questionGroupInsert = new QuestionGroupSQLQueryBuilder().getInsertStatementFor(questionGroup);
		questionnaire.getQuestionGroups().add(questionGroup);
		executeOne(questionGroupInsert);
		return questionGroup;
	}

	private void insertQuestions(Component component, MigrationQuestionnaire questionnaire)
	{
		questionLastSortOrder = new HashMap<>();

		final QuestionSQLQueryBuilder questionSQLQueryBuilder = new QuestionSQLQueryBuilder();
		final ChoiceGroupSQLQueryBuilder choiceGroupSQLQueryBuilder = new ChoiceGroupSQLQueryBuilder();
		List<String> choiceStatements = new ArrayList<>();

		int questionGroupSortOrder = 1;
		for (final Attribute attribute : component.getAttribute())
		{
			final String label = attribute.getName();

			if (attribute.getSet() == null || !isRelevantQuestion(attribute.getName()))
			{
				continue;
			}

			final String questionGroupKey = attribute.getSet().toLowerCase();

			final String dataType = attribute.getDataType();

			MigrationQuestionGroup questionGroup = findQuestionGroup(questionnaire, questionGroupKey);

			if (questionGroup == null)
			{
				questionGroup = insertQuestionGroup(questionGroupKey, questionGroupSortOrder, questionnaire);

				questionLastSortOrder.put(questionGroup, 1);
				questionGroupSortOrder++;
			}

			Integer questionSortOrder = questionLastSortOrder.get(questionGroup);
			final MigrationQuestion question = createMigrationQuestion(attribute, dataType, questionGroup,
					questionSortOrder);
			if (question != null)
			{
				if (question.getDTYPE().equals(MigrationQuestion.QUESTION_DTYPE.ChoiceQuestion))
				{
					MigrationChoiceGroup choiceGroup = new MigrationChoiceGroup(question);
					final List<MigrationChoiceValue> choices = getChoices(attribute, label);

					choiceGroup.createName(choices);

					MigrationChoiceGroup foundChoiceGroup = choiceGroupIdMap.get(choiceGroup.getName());

					if (foundChoiceGroup == null)
					{
						choiceGroup.addChoices(choices);
						choiceGroupSQLQueryBuilder.addValue(choiceGroup);
						choiceGroupIdMap.put(choiceGroup.getName(), choiceGroup);

						String choiceStatement = getChoiceStatements(choices);
						choiceStatements.add(choiceStatement);
					}
					else
					{
						question.setChoiceGroup(foundChoiceGroup);
					}
					choiceStatements.add(getLegacyChoiceStatements(question, choices));
				}
				questionSQLQueryBuilder.addValue(question);
			}
		}

		executeOne(choiceGroupSQLQueryBuilder.getSQLQuery());
		executeOne(questionSQLQueryBuilder.getSQLQuery());
		executeMultiple(choiceStatements);
	}

	private MigrationQuestionGroup findQuestionGroup(MigrationQuestionnaire questionnaire, String questionGroupName)
	{
		final Optional<MigrationQuestionGroup> optionalQuestionGroup = questionnaire.getQuestionGroups().stream().filter(
				questionGroup -> questionGroup.getLegacyId().equals(questionGroupName)).findFirst();

		if (optionalQuestionGroup.isPresent())
		{
			final MigrationQuestionGroup questionGroup = optionalQuestionGroup.get();
			Integer lastSortOrder = questionLastSortOrder.get(questionGroup);
			questionLastSortOrder.put(questionGroup, ++lastSortOrder);

			return questionGroup;
		}

		return null;
	}
	
	private MigrationQuestion createMigrationQuestion(Attribute attribute, String dataType,
			MigrationQuestionGroup questionGroup, int questionSortOrder)
	{
		final String label = attribute.getName();

		MigrationQuestion question = new MigrationQuestion(label, questionGroup, questionSortOrder);

		if ("true".equals(attribute.getNotNull()))
		{
			question.setRequired(true);
		}

		switch (dataType)
		{
			case "boolean":
				question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.BooleanQuestion);
				break;
			case "integer":
			case "scale":
				question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.NumberQuestion);
				if ("scale".equals(dataType))
				{
					final String scaleType = attribute.getSkalentyp();
					// Skalentyp="R[1,5]"
					final String[] range = getRange(scaleType);

					question.setMinVal(Integer.valueOf(range[0]));
					question.setMaxVal(Integer.valueOf(range[1]));
				}
				break;
			case "string":
				question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.TextQuestion);
				break;
			case "choice":
			case "integerchoice":
			case "multiplechoice":
				final SelectionType selectionType = getSelectionType(dataType);
				question.setSelectionType(selectionType);
				question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.ChoiceQuestion);
				break;
			case "componentnumber":
				return null;
			default:
				question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.TextQuestion);
				break;
		}

		return question;
	}

	private String[] getRange(String scaleType)
	{
		return scaleType.replaceAll("R\\[", "").replaceAll("\\]", "").split(",");
	}

	private SelectionType getSelectionType(String dataType)
	{
		SelectionType selectionType;
		if ("multiplechoice".equals(dataType))
		{
			selectionType = SelectionType.MULTIPLE;
		}
		else
		{
			selectionType = SelectionType.SINGLE;
		}
		return selectionType;
	}

	private String getLegacyChoiceStatements(MigrationQuestion question, List<MigrationChoiceValue> choices)
	{
		LegacyChoiceSQLQueryBuilder legacyChoiceSQLQueryBuilder = new LegacyChoiceSQLQueryBuilder();
		choices.forEach((choice) -> {
			choice.setQuestion(question);
			legacyChoiceSQLQueryBuilder.addValue(choice);
		});
		return legacyChoiceSQLQueryBuilder.getSQLQuery();
	}

	private String getChoiceStatements(List<MigrationChoiceValue> choices)
	{
		final ChoiceValueSQLQueryBuilder choiceValueSQLQueryBuilder = new ChoiceValueSQLQueryBuilder();

		for (MigrationChoiceValue choice : choices)
		{
			final String oldKey = choice.getI18nKey();

			final Set<MigrationChoiceValue> migrationChoiceValues = choiceValueIdentityMap.get(oldKey);

			Integer number = migrationChoiceValues.size();

			if (number != 0)
			{
				choice.setI18nKey(choice.getI18nKey() + "$" + number);
				choice.setLegacyId(migrationChoiceValues.iterator().next().getLegacyId());
			}

			choiceValueIdentityMap.put(oldKey, choice);

			choiceValueSQLQueryBuilder.addValue(choice);

		}
		return choiceValueSQLQueryBuilder.getSQLQuery();

	}

	private List<MigrationChoiceValue> getChoices(Attribute attribute, String parentId)
	{
		List<MigrationChoiceValue> choiceValues = new ArrayList<>();
		int choiceSortOrder = 0;
		for (final Option option : attribute.getOption())
		{
			final String choiceString = option.getValue();
			final String legacyId = String.format("%s.%s", parentId, choiceString);
			final String value = translationPropertiesManager.getProperties().getProperty(legacyId);

			final MigrationChoiceValue choiceValue = new MigrationChoiceValue(parentId, choiceString, value,
					choiceSortOrder);
			choiceValues.add(choiceValue);

			choiceSortOrder++;
		}
		return choiceValues;
	}

}
