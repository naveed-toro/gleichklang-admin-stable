package de.binaerebauten.gleichklang.adminweb.service.serialization;

import de.binaerebauten.gleichklang.core.model.*;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.monitoring.AppInfoApi;
import de.binaerebauten.gleichklang.core.monitoring.BuildInfo;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.ResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Project: gleichklang-parent
 * Created by Domi on 13.05.2016.
 */
@Service
@PropertySource("classpath:/build.properties")
public class SerializationService {

    private static final Logger LOG = LoggerFactory.getLogger(SerializationService.class);

    public enum SerializationGroup {
        QUESTIONNAIRE,
        TRANSLATION,
        MATRIX,
        PRODUCT,
        MAPPER,
        ACTIVATOR,
        FILTER,
        ALL
    }


    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ChoiceGroupRepository choiceGroupRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private I18NRepository i18NRepository;

    @Autowired
    private MatrixRepository matrixRepository;

    @Autowired
    private MatrixValueRepository matrixValueRepository;

    @Autowired
    private QuestionMappingRepository questionMappingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SubscriptionOfferCategoryRepository subscriptionOfferCategoryRepository;

    @Autowired
    private ServiceOfferRequiredCategoryRepository serviceOfferRequiredCategoryRepository;

    @Autowired
    private ActivatorRepository activatorRepository;

    @Autowired
    private FilterRepository filterRepository;

    @Autowired
    private TemplateFilterRepository templateFilterRepository;

    @Autowired
    private LocatableRepository locatableRepository;

    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private Environment environment;

    private String buildVersion;

    private String buildRevision;


    @PostConstruct
    public void init() {

        buildVersion = environment.getProperty("build.version");

        // fix for eclipse: can't replace property placeholder maven.build.time
        try {
            buildRevision = environment.getProperty("build.revision");
        } catch (final IllegalArgumentException e) {
            buildRevision = "undefined";
        }
    }

    // ===> Export <===

    @Transactional
    public void exportData(SerializationGroup type, OutputStream outputStream) {
        SerializationContainer container = new SerializationContainer();
        container.setVersion(buildVersion);
        container.setRevision(buildRevision);

        switch (type) {
            case QUESTIONNAIRE:
                getQuestionnaires(container);
                break;

            case MATRIX:
                getMatrices(container);
                break;

            case TRANSLATION:
                getTranslations(container);
                break;

            case MAPPER:
                getQuestionMappers(container);
                break;

            case PRODUCT:
                getProducts(container);
                break;

            case ACTIVATOR:
                getActivators(container);
                break;

            case FILTER:
                getFilters(container);
                break;

            case ALL:
                getAllObjects(container);
                break;
        }

        marshalContainer(container, outputStream);
    }


    // ===> Import <===

    public SerializationContainer deserialize(InputStream inputStream) throws JAXBException {
        final SerializationContainer container = unmarshalContainer(SerializationContainer.class, inputStream);

        return container;
    }

    public boolean checkVersion(SerializationContainer container) {
        return container.getVersion().equals(buildVersion) && container.getRevision().equals(buildRevision);
    }


    @Transactional
    public String importData(SerializationContainer container) throws UniqueValidationException {
        if (container == null) return null;

        return importAllObjects(container);
    }

    /**
     * Imports serialized Data
     *
     * @param container XML data
     */
    private String importAllObjects(SerializationContainer container) {
        LOG.info("Starting Import Process for: All Objects.");

        Map<String, String> translationCorrectionMap = new HashMap<>();
        StringBuilder errorList = new StringBuilder();

        LOG.info("Starting mergeChoiceGroups");
        errorList.append(mergeChoiceGroups(container.getChoiceGroups(), translationCorrectionMap));
        LOG.info("Starting mergeQuestionnaires");
        errorList.append(mergeQuestionnaires(container.getQuestionnaires()));
        LOG.info("Starting mergeMatrices");
        errorList.append(mergeMatrices(container.getMatchingMatrices()));
        LOG.info("Starting mergeQuestionMappers");
        errorList.append(mergeQuestionMappers(container.getQuestionMappings()));
        LOG.info("Starting mergeLocatables");
        errorList.append(mergeLocatables(container.getLocatableEntities()));
        LOG.info("Starting mergeProducts: " + container.getTranslations().size());
        errorList.append(mergeProducts(createProductsMap(container.getProducts()), translationCorrectionMap, container.getTranslations()));
        LOG.info("Starting mergeActivators");
        errorList.append(mergeActivators(container.getActivators()));
        LOG.info("Starting mergeFilters");
        errorList.append(mergeFilters(container.getFilters(), null));
        LOG.info("Starting mergeTranslations: " + container.getTranslations().size());
        errorList.append(mergeTranslations(container.getTranslations(), translationCorrectionMap));

        LOG.info("End Import Process for: All Objects.");
        return errorList.toString();
    }


    // ===> Container <===

    private void getMatrices(SerializationContainer container) {
        List<MatchingMatrix> matchingMatrices = matrixRepository.findAll();
        collectMatchingMatrices(container, new HashSet<>(matchingMatrices));
    }

    /**
     * Creates a Serialization Container for Prodcuts with all products.
     *
     * @return Product XML data container
     */
    private void getProducts(SerializationContainer container) {
        List<Product> products = productRepository.findAll();
        collectProducts(container, new HashSet<>(products));
    }

    private void getActivators(SerializationContainer container) {
        List<Activator> activators = activatorRepository.findAll();
        collectActivators(container, new HashSet<>(activators));
    }

    private void getTranslations(SerializationContainer container) {
        container.setTranslations(new HashSet<>(i18NRepository.findAll()));
    }

    private void getFilters(SerializationContainer container) {

        List<AbstractFilter> filters = filterRepository.findAll();
        collectFilters(container, new HashSet<>(filters));
    }


    /**
     * Creates a Serialization Container for Questionnaires with all questionnaires.
     *
     * @return Questionnaire XML data container
     */
    private void getQuestionnaires(SerializationContainer container) {
        // Questionnaires
        List<Questionnaire> questionnaires = questionnaireRepository.findAll(new Sort(new Sort.Order(Sort.Direction.ASC, Questionnaire_.sortOrder.getName())));
        collectQuestionnaires(container, new HashSet<>(questionnaires));

        // ChoiceGroups
        List<ChoiceGroup> choiceGroups = choiceGroupRepository.findAll();
        collectChoiceGroups(container, new HashSet<>(choiceGroups));
    }


    /**
     * Get all Question Mappers and their dependend objects as serialization container.
     *
     * @return container with objects for serialization
     */
    private void getQuestionMappers(SerializationContainer container) {
        final Set<AbstractQuestionsMapping> questionsMappings = new HashSet<>(questionMappingRepository.findAll());
        final Set<Questionnaire> questionnaires = new HashSet<>();
        final Set<MatchingMatrix> matchingMatrices = new HashSet<>();


        questionsMappings.stream().forEach(questionsMapping -> {
            if (questionsMapping instanceof AffinityMapping) {
                AffinityMapping affinityMapping = (AffinityMapping) questionsMapping;

                affinityMapping.getQuestions().forEach(question -> {
                    questionnaires.add(question.getQuestionGroup().getQuestionnaire());
                });


            } else if (questionsMapping instanceof ChoiceQuestionsMapping) {
                ChoiceQuestionsMapping choiceQuestionsMapping = (ChoiceQuestionsMapping) questionsMapping;

                matchingMatrices.add(choiceQuestionsMapping.getMatrix());
                questionnaires.add(choiceQuestionsMapping.getSourceQuestion().getQuestionGroup().getQuestionnaire());
                questionnaires.add(choiceQuestionsMapping.getTargetQuestion().getQuestionGroup().getQuestionnaire());


            } else if (questionsMapping instanceof AgeQuestionMapping) {
                AgeQuestionMapping ageQuestionMapping = (AgeQuestionMapping) questionsMapping;

                questionnaires.add(ageQuestionMapping.getMaxAgeQuestion().getQuestionGroup().getQuestionnaire());
                questionnaires.add(ageQuestionMapping.getMinAgeQuestion().getQuestionGroup().getQuestionnaire());


            } else if (questionsMapping instanceof AvatarQuestionMapping) {
                AvatarQuestionMapping avatarQuestionMapping = (AvatarQuestionMapping) questionsMapping;

                questionnaires.add(avatarQuestionMapping.getAvatarQuestion().getQuestionGroup().getQuestionnaire());


            } else if (questionsMapping instanceof NumberQuestionsMapping) {
                NumberQuestionsMapping numberQuestionsMapping = (NumberQuestionsMapping) questionsMapping;

                questionnaires.add(numberQuestionsMapping.getFactQuestion().getQuestionGroup().getQuestionnaire());
                questionnaires.add(numberQuestionsMapping.getMaxQuestion().getQuestionGroup().getQuestionnaire());
                questionnaires.add(numberQuestionsMapping.getMinQuestion().getQuestionGroup().getQuestionnaire());


            }


        });

        collectMatchingMatrices(container, matchingMatrices);
        collectQuestionnaires(container, questionnaires);

        container.setQuestionMappings(questionsMappings);
    }

    /**
     * Collects all related objects from data and stores in given XML container.
     *
     * @param container XML Container
     */
    private void getAllObjects(SerializationContainer container) {

        getQuestionnaires(container);
        getTranslations(container);
        getActivators(container);
        getFilters(container);
        getQuestionMappers(container);
        getMatrices(container);
        getProducts(container);
    }

    // ===> Helper methods <===

    private void collectMatchingMatrices(SerializationContainer container, Set<MatchingMatrix> matchingMatrices) {
        final Set<ChoiceGroup> choiceGroups = new HashSet<>();
        final Map<I18NEntity.BaseName, List<String>> translationMap = new HashMap<>();

        matchingMatrices.stream().forEach(matchingMatrix -> {
            choiceGroups.add(matchingMatrix.getSourceChoiceGroup());
            matchingMatrix.getSourceChoiceGroup().getChoices().stream().forEach(choice -> addI18NKeyPair(translationMap, choice));

            choiceGroups.add(matchingMatrix.getTargetChoiceGroup());
            matchingMatrix.getTargetChoiceGroup().getChoices().stream().forEach(choice -> addI18NKeyPair(translationMap, choice));
        });

        container.addAllMatchingMatrices(matchingMatrices);
        container.addAllTranslations(collectTranslations(translationMap));

        collectChoiceGroups(container, choiceGroups);
    }


    /**
     * Collects all ChoiceGroups and and translations for choice groups
     *
     * @param container    serialization container result is saved in
     * @param choiceGroups set of choice groups
     */
    private void collectChoiceGroups(SerializationContainer container, Set<ChoiceGroup> choiceGroups) {
        final Map<I18NEntity.BaseName, List<String>> translationMap = new HashMap<>();
        choiceGroups.stream().forEach(choiceGroup -> choiceGroup.getChoices().stream().forEach(choice -> addI18NKeyPair(translationMap, choice)));

        container.addAllChoiceGroups(choiceGroups);
        container.addAllTranslations(collectTranslations(translationMap));
    }

    /**
     * Collects all Questionnaires and their translations including depending objects.
     *
     * @param container      serialization container result is added to
     * @param questionnaires set of questionnaires
     */
    private void collectQuestionnaires(SerializationContainer container, Set<Questionnaire> questionnaires) {
        final Set<ChoiceGroup> choiceGroups = new HashSet<>();
        final Map<I18NEntity.BaseName, List<String>> translationMap = new HashMap<>();

        questionnaires.stream().forEach(questionnaire -> {
            addI18NKeyPair(translationMap, questionnaire);
            addI18NKeyPair(translationMap, questionnaire, I18NEntity.BaseName.QUESTIONNAIRE_DESCRIPTION);

            questionnaire.getQuestionGroups().stream().forEach(questionGroup -> {
                addI18NKeyPair(translationMap, questionGroup);
                addI18NKeyPair(translationMap, questionGroup, I18NEntity.BaseName.QUESTION_GROUP_DESCRIPTION);

                questionGroup.getQuestions().stream().forEach(question -> {
                    addI18NKeyPair(translationMap, question);
                    addI18NKeyPair(translationMap, question, I18NEntity.BaseName.QUESTION_DESCRIPTION);
                    if (question instanceof ChoiceQuestion)
                        choiceGroups.add(((ChoiceQuestion) question).getChoiceGroup());
                });
            });
        });

        container.addAllQuestionnaires(questionnaires);
        container.addAllTranslations(collectTranslations(translationMap));

        collectChoiceGroups(container, choiceGroups);
    }


    private void collectProducts(SerializationContainer container, Set<Product> products) {
        final Map<I18NEntity.BaseName, List<String>> translationMap = new HashMap<>();

        products.stream().forEach(product -> {
            addI18NKeyPair(translationMap, product);
        });

        container.addAllProducts(products);
        container.addAllTranslations(collectTranslations(translationMap));
    }

    private void collectActivators(SerializationContainer container, Set<Activator> activators) {
        final Set<Questionnaire> questionnaires = new HashSet<>();
        final Set<ChoiceGroup> choiceGroups = new HashSet<>();


        activators.stream().forEach(activator -> {
            questionnaires.add(activator.getActivatingQuestion().getQuestionGroup().getQuestionnaire());
            activator.getActivatingChoices().stream().forEach(choice -> {
                choiceGroups.add(choice.getChoiceGroup());
            });

            if (activator instanceof QuestionActivator) {
                questionnaires.add(((QuestionActivator) activator).getEnablesQuestion().getQuestionGroup().getQuestionnaire());
            } else if (activator instanceof QuestionGroupActivator) {
                questionnaires.add(((QuestionGroupActivator) activator).getEnablesQuestionGroup().getQuestionnaire());
            } else if (activator instanceof QuestionnaireActivator) {
                questionnaires.add(((QuestionnaireActivator) activator).getEnablesQuestionnaire());
            }
        });

        container.addAllActivators(activators);

        collectChoiceGroups(container, choiceGroups);
        collectQuestionnaires(container, questionnaires);
    }


    private void collectFilters(SerializationContainer container, Set<AbstractFilter> filters) {
        final Set<Questionnaire> questionnaires = new HashSet<>();
        final Set<ChoiceGroup> choiceGroups = new HashSet<>();
        final Set<LocatableEntity> locatableEntities = new HashSet<>();

        filters.stream().forEach(filter -> {
            if (filter instanceof ChoiceQuestionFilter) {
                ChoiceQuestionFilter choiceQuestionFilter = (ChoiceQuestionFilter) filter;
                questionnaires.add(choiceQuestionFilter.getChoiceQuestion().getQuestionGroup().getQuestionnaire());
                choiceGroups.add(choiceQuestionFilter.getChoice().getChoiceGroup());
            } else if (filter instanceof RegionFilter) {
                locatableEntities.add(((RegionFilter) filter).getLocatableEntity());
            } else if (filter instanceof ProximityFilter) {
                locatableEntities.add(((ProximityFilter) filter).getZip());
            } else if (filter instanceof TextQuestionFilter) {
                questionnaires.add(((TextQuestionFilter) filter).getTextQuestion().getQuestionGroup().getQuestionnaire());
            }
        });

        collectChoiceGroups(container, choiceGroups);
        collectQuestionnaires(container, questionnaires);
        collectLocatableEntities(container, locatableEntities);

        container.addAllFilters(filters);
    }


    private void collectLocatableEntities(SerializationContainer container, Set<LocatableEntity> locatableEntities) {
        final Map<I18NEntity.BaseName, List<String>> translationMap = new HashMap<>();

        locatableEntities.stream().forEach(locatableEntity -> {
            Set<LocatableEntity> locatableEntitySet = new HashSet<>();

            if (locatableEntity.getParent() != null) {
                locatableEntitySet.add(locatableEntity.getParent());
            }
            if (locatableEntity instanceof Zip) {
                locatableEntitySet.add(((Zip) locatableEntity).getRegion());
            }
            if (locatableEntitySet.size() > 0) collectLocatableEntities(container, locatableEntitySet);
        });

        container.addAllLocatableEntities(locatableEntities);
        container.addAllTranslations(collectTranslations(translationMap));
    }


    /**
     * Collects translations from map of basename and translation key.
     *
     * @param translationMap map with basename:translation key list
     * @return set of I18NEntity objects
     */
    private Set<I18NEntity> collectTranslations(Map<I18NEntity.BaseName, List<String>> translationMap) {
        final Set<I18NEntity> translations = new HashSet<>();
        translationMap.forEach((baseName, keys) -> {
            if (keys == null) return;
            if (keys.size() == 0) return;
            translations.addAll(i18NRepository.getEntitiesForBaseNameAndKeys(baseName, keys));
        });

        return translations;
    }

    /**
     * Adds a BaseName and I18NKey combination to a translation mapping map.
     *
     * @param translationMap  map the combination should be insert
     * @param localizedEntity localized entity for which the base name and i18nkey should be stored in the map
     */
    private void addI18NKeyPair(Map<I18NEntity.BaseName, List<String>> translationMap, LocalizedEntity localizedEntity) {
        I18NEntity.BaseName baseName = I18NEntity.BaseName.valueOf(localizedEntity.getBaseName());
        List<String> i18nKeys = translationMap.get(baseName);
        if (i18nKeys == null) {
            i18nKeys = new ArrayList<>();
            translationMap.put(baseName, i18nKeys);
        }
        if (localizedEntity.getI18nKey() != null) {
            i18nKeys.add(localizedEntity.getI18nKey());
        }
    }


    private void addI18NKeyPair(Map<I18NEntity.BaseName, List<String>> translationMap, LocalizedEntity localizedEntity, I18NEntity.BaseName baseName) {
        List<String> i18nKeys = translationMap.get(baseName);
        if (i18nKeys == null) {
            i18nKeys = new ArrayList<>();
            translationMap.put(baseName, i18nKeys);
        }
        i18nKeys.add(localizedEntity.getI18nKey());
    }


    // ===> Merging <===


    private String mergeChoiceGroups(Set<ChoiceGroup> choiceGroups, Map<String, String> translationCorrectionMap) {
        if (choiceGroups == null) return "";

        final Map<String, Long> choiceGroupsMap = resultMapper.convertToStringLongPairMap(choiceGroupRepository.getAllAsIdKeyPair());

        final Set<String> choiceKeys = resultMapper.normalizeResult(choiceRepository.getAllKeys());

        final List<String> deletableTranslations = new ArrayList<>();
        try {
            choiceGroups.forEach(choiceGroup -> {
                validateChoices(new HashSet<>(choiceGroup.getChoices()), choiceGroup);

                Long persistedChoiceGroupId = choiceGroupsMap.get(resultMapper.convertToLowercasedIdentityKey(choiceGroup.getI18nKey()));
                if (persistedChoiceGroupId != null) {
                    choiceGroup.setId(persistedChoiceGroupId);

                    mergeChoices(choiceGroup, deletableTranslations, translationCorrectionMap, choiceKeys);
                    choiceGroup.getChoices().forEach(choice -> choice.setSortOrder(choice.getSortOrder() * -1));

                }


                choiceGroupRepository.save(choiceGroup);
                choiceGroupRepository.flush();

                final ChoiceGroup persistedChoiceGroup = choiceGroupRepository.findOne(choiceGroup.getId());


                int minSortOrder = choiceRepository.findMinSortOrder(null);
                List<Choice> oldChoices = choiceRepository.getAllForChoiceGroupWithPositiveSortOrder(persistedChoiceGroup);
                for (Choice c : oldChoices) {
                    minSortOrder--;
                    c.setSortOrder(minSortOrder);
                }
                choiceRepository.save(oldChoices);
                choiceRepository.flush();
                for (Choice choice : persistedChoiceGroup.getChoices()) {
                    choice.setSortOrder(Math.abs(choice.getSortOrder()));
                }
                for (Choice choice : oldChoices) {
                    choice.setSortOrder(Math.abs(choice.getSortOrder()));
                }
                choiceRepository.save(persistedChoiceGroup.getChoices());
                choiceRepository.save(oldChoices);

                mergeChoices(choiceGroup, deletableTranslations, translationCorrectionMap, choiceKeys);
            });

            if (deletableTranslations.size() > 0) {
                i18NRepository.deleteTranslationKeysForBaseName(deletableTranslations, I18NEntity.BaseName.CHOICE_VALUE);
            }
        }  catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Choice Groups: %s \n",e.getLocalizedMessage());
        }
        return "";
    }


    private void mergeChoices(ChoiceGroup choiceGroup, List<String> deletableTranslations, Map<String, String> translationCorrectionMap, Set<String> choiceKeys) {
        final Map<String, Choice> currentChoices = resultMapper.convertToI18NKeyObjectMap(choiceRepository.getChoicesForChoiceGroupKey(choiceGroup.getI18nKey()));
        choiceGroup.getChoices().forEach(choice -> {
            Choice persistedChoice = currentChoices.get(resultMapper.convertToLowercasedIdentityKey(choice.getI18nKey()));
            if (persistedChoice != null) {
                choice.setId(persistedChoice.getId());

                if (!resultMapper.convertToLowercasedIdentityKey(persistedChoice.getI18nKey()).equals(resultMapper.convertToLowercasedIdentityKey(choice.getI18nKey()))) {
                    deletableTranslations.add(persistedChoice.getI18nKey());
                    String newKey = nextKey(resultMapper.convertToLowercasedIdentityKey(choice.getI18nKey()), choiceKeys, choice.getI18nKey());
                    collectChangedTranslationKeys(choice.getBaseName(), choice.getI18nKey(), newKey, translationCorrectionMap);
                    choice.setI18nKey(newKey);
                }
            }
        });
    }

//    private void mergeTranslations(Set<I18NEntity> translations) {
//        mergeTranslations(translations, new HashMap<>());
//    }


    private String mergeTranslations(Set<I18NEntity> translations, Map<String, String> translationsCorrectionMap) {
        if (translations == null) return "";
        try {
            final Map<String, Long> i18nMap = resultMapper.convertToStringLongPairMap(i18NRepository.getAllAsIdKeyPair());

            translations.forEach(i18NEntity -> {
                if (translationsCorrectionMap.size() != 0) {
                    String newKey = translationsCorrectionMap.get(i18NEntity.getBaseName().toString() + '_' + i18NEntity.getKey());
                    if (newKey != null) {
                        i18NEntity.setKey(newKey);
                    }
                }
                Long persistedEntityId = i18nMap.get(resultMapper.convertToLowercasedIdentityKey(i18NEntity.getBaseName().toString() + ":" + i18NEntity.getLanguage().toString() + ":" + i18NEntity.getKey()));
                if (persistedEntityId != null) i18NEntity.setId(persistedEntityId);
            });
            i18NRepository.save(translations);
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Translations: %s \n",e.getLocalizedMessage());
        }
        return "";
    }


    private String mergeMatrices(Set<MatchingMatrix> matchingMatrices) {
        if (matchingMatrices == null) return "";
        try {
            final Map<String, Long> matchingMatricesMap = resultMapper.convertToStringLongPairMap(matrixRepository.getAllAsIdKeyPair());
            final Map<String, Long> matrixValuesMap = resultMapper.convertToStringLongPairMap(matrixValueRepository.getAllAsIdKeyPair());

            matchingMatrices.forEach(matchingMatrix -> {
                Long persistedMatchingMatrixId = matchingMatricesMap.get(resultMapper.convertToLowercasedIdentityKey(matchingMatrix.getName()));
                if (persistedMatchingMatrixId != null) matchingMatrix.setId(persistedMatchingMatrixId);

                matchingMatrix.getMatrixValues().forEach(matrixValue -> {
                    Long persistedMatrixValueId = matrixValuesMap.get(resultMapper.convertToLowercasedIdentityKey(matrixValue.getUniqueKey()));
                    if (persistedMatrixValueId != null) matrixValue.setId(persistedMatrixValueId);
                });

                matrixRepository.save(matchingMatrix);
            });
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging matching Matrices: %s \n",e.getLocalizedMessage());
        }
        return "";
    }

    private String mergeQuestionnaires(Set<Questionnaire> questionnaires) {
        if (questionnaires == null) return "";
        try {
            // get all persisted objects as key:id pair
            Map<String, Long> questionnairesMap = resultMapper.convertToStringLongPairMap(questionnaireRepository.getAllAsIdKeyPair());
            Map<String, Long> questionGroupsMap = resultMapper.convertToStringLongPairMap(questionGroupRepository.getAllAsIdKeyPair());
            Map<String, Long> questionsMap = resultMapper.convertToStringLongPairMap(questionRepository.getAllAsIdKeyPair());

            validateQuestionnairs(questionnaires);

            for (Questionnaire questionnaire : questionnaires) {
                questionnaire.setSortOrder(questionnaire.getSortOrder() * -1);

                Long persistedQuestionnaireId = questionnairesMap.get(resultMapper.convertToLowercasedIdentityKey(questionnaire.getI18nKey()));
                if (persistedQuestionnaireId != null) {
                    questionnaire.setId(persistedQuestionnaireId);
                }
                questionnaireRepository.save(questionnaire);


                for (QuestionGroup questionGroup : questionnaire.getQuestionGroups()) {
                    questionGroup.setSortOrder(questionGroup.getSortOrder() * -1);

                    Long persistedQuestionGroupId = questionGroupsMap.get(resultMapper.convertToLowercasedIdentityKey(questionGroup.getI18nKey()));
                    if (persistedQuestionGroupId != null) {
                        questionGroup.setId(persistedQuestionGroupId);
                    }
                    questionGroupRepository.save(questionGroup);


                    for (Question question : questionGroup.getQuestions()) {
                        question.setSortOrder(question.getSortOrder() * -1);

                        Long persistedQuestionId = questionsMap.get(resultMapper.convertToLowercasedIdentityKey(question.getI18nKey()));
                        if (persistedQuestionId != null) {
                            question.setId(persistedQuestionId);
                        }
                        questionRepository.save(question);
                    }
                    questionRepository.flush();
                    Integer minSortOrder = questionRepository.findMinSortOrder((root, query, cb) -> cb.equal(root.get(Question_.questionGroup), questionGroup));
                    List<Question> oldQuestions = questionRepository.getAllForQuestionGroupWithPositiveSortOrder(questionGroup);
                    for (Question q : oldQuestions) {
                        minSortOrder--;
                        q.setSortOrder(minSortOrder);
                    }
                    questionRepository.save(oldQuestions);
                    questionRepository.flush();
                    questionGroup.getQuestions().forEach(question -> question.setSortOrder(question.getSortOrder() * -1));
                    oldQuestions.forEach(question -> question.setSortOrder(question.getSortOrder() * -1));
                    questionRepository.save(questionGroup.getQuestions());
                    questionRepository.save(oldQuestions);
                }
                questionGroupRepository.flush();
                Integer minSortOrder = questionGroupRepository.findMinSortOrder((root, query, cb) -> cb.equal(root.get(QuestionGroup_.questionnaire), questionnaire));
                List<QuestionGroup> oldQuestionGroups = questionGroupRepository.getAllQForQuestionnaireWithPositiveSortOrder(questionnaire);
                for (QuestionGroup qg : oldQuestionGroups) {
                    minSortOrder--;
                    qg.setSortOrder(minSortOrder);
                }
                questionGroupRepository.save(oldQuestionGroups);
                questionGroupRepository.flush();
                questionnaire.getQuestionGroups().forEach(questionGroup -> questionGroup.setSortOrder(questionGroup.getSortOrder() * -1));
                oldQuestionGroups.forEach(questionGroup -> questionGroup.setSortOrder(questionGroup.getSortOrder() * -1));
                questionGroupRepository.save(questionnaire.getQuestionGroups());
                questionGroupRepository.save(oldQuestionGroups);
            }
            questionnaireRepository.flush();
            Integer minSortOrder = questionnaireRepository.findMinSortOrder(null);
            List<Questionnaire> oldQuestionnaires = questionnaireRepository.getAllWithPositivSortOrder();
            for (Questionnaire questionnaire : oldQuestionnaires) {
                minSortOrder--;
                questionnaire.setSortOrder(minSortOrder);
            }
            questionnaireRepository.save(oldQuestionnaires);
            questionnaireRepository.flush();
            questionnaires.forEach(questionnaire -> questionnaire.setSortOrder(questionnaire.getSortOrder() * -1));
            oldQuestionnaires.forEach(questionnaire -> questionnaire.setSortOrder(questionnaire.getSortOrder() * -1));
            questionnaireRepository.save(questionnaires);
            questionnaireRepository.save(oldQuestionnaires);
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Questionnaires: %s \n",e.getLocalizedMessage());
        }
        return "";
    }


    private String mergeQuestionMappers(Set<AbstractQuestionsMapping> questionsMappings) {
        if (questionsMappings == null) return "";
        try {
            final Map<String, PersistedEntityIdentifier> questionMappingMap = resultMapper.convertToKeyIdentifierMap(questionMappingRepository.getAllAsIdKeyPair());

            questionsMappings.forEach(questionsMapping -> {
                PersistedEntityIdentifier persistedQuestionMapping = questionMappingMap.get(resultMapper.convertToLowercasedIdentityKey(questionsMapping.getNaturalKey()));
                if (persistedQuestionMapping != null) {
                    if (questionsMapping.getClass().equals(persistedQuestionMapping.getPersistedType())) {
                        questionsMapping.setId(persistedQuestionMapping.getPersistedId());
                    } else {
                        questionsMapping.setNaturalKey(nextKey(resultMapper.convertToLowercasedIdentityKey(questionsMapping.getNaturalKey()), questionMappingMap.keySet(), questionsMapping.getNaturalKey()));
                    }
                }
            });
            questionMappingRepository.save(questionsMappings);
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Question Mappers: %s \n",e.getLocalizedMessage());
        }
        return "";
    }

    private String mergeProducts(Map<String, Product> products, Map<String, String> translationCorrectionMap, Set<I18NEntity> uploadedTranslations) {
        if (products == null) return "";

        try {
            validateProducts(products, uploadedTranslations);
            Map<String, Product> productsMap = createProductsMap(productRepository.getAll());
            final Map<String, Long> subscriptionOfferCategoriesMap = resultMapper.convertToStringLongPairMap(subscriptionOfferCategoryRepository.getAllAsIdKeyPair());
            final Map<String, Long> serviceOfferCategoriesMap = resultMapper.convertToStringLongPairMap(serviceOfferRequiredCategoryRepository.getAllAsIdKeyPair());

            Iterator<Map.Entry<String,Product>> iterator = productsMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Product> entry = iterator.next();
                Product product = entry.getValue();
                // compare i18nkey if unique then persist else deep compare then if same then skip+remove else update key and persist
                if (productsMap.containsKey(product.getI18nKey())) {
                    if (checkRedundantProduct(product, productsMap)) {
                        uploadedTranslations.removeAll(uploadedTranslations.stream().filter(entity ->
                                entity.getKey().equals(product.getI18nKey())).collect(Collectors.toSet()));
                        iterator.remove();
                        continue;
                    }
                }

                if (product instanceof SubscriptionOffer) {
                    mergeSubscriptionOffer((SubscriptionOffer) product, productsMap, subscriptionOfferCategoriesMap
                            , translationCorrectionMap, uploadedTranslations);
                }

                if (product instanceof ServiceOffer) {
                    checkProductId(product, productsMap, translationCorrectionMap);
                    ((ServiceOffer) product).getRequiredCategories().forEach(serviceOfferRequiredCategory -> {
                        if (serviceOfferRequiredCategory.getId() == null) {
                            Long persistedServiceOfferCategoryId = serviceOfferCategoriesMap.get(resultMapper
                                    .convertToLowercasedIdentityKey(product.getI18nKey() + "_" + serviceOfferRequiredCategory.getCategory().name()));
                            if (persistedServiceOfferCategoryId != null) {
                                serviceOfferRequiredCategory.setId(persistedServiceOfferCategoryId);
                            }
                        }

                        if (product.getId() != null) {
                            serviceOfferRequiredCategoryRepository.save(serviceOfferRequiredCategory);
                        }
                    });
                }

                productRepository.save(product);
            }
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Products: %s \n",e.getLocalizedMessage());
        }
        return "";
    }

    private Map<String, Product> createProductsMap(Set<Product> products) {
        return products.stream().collect(Collectors.toMap(Product::getI18nKey, item -> item));
    }

    private boolean checkRedundantProduct(Product product, Map<String, Product> productsMap) {//returns true if products are same in deep compare
        Product persistedProduct = productsMap.get(product.getI18nKey());

        if (persistedProduct.getClass().equals(product.getClass())) {
            if (product.getProductType().equals(Product.ProductType.CHARGEBACK)) {
                Chargeback chargeback = (Chargeback) product;

                if (persistedProduct instanceof Chargeback)
                    return chargeback.getForMethod().equals(((Chargeback) persistedProduct).getForMethod()) &&
                            chargeback.getAmount().equals(persistedProduct.getAmount());
                else
                    return false;
            }
            else if (product.getProductType().equals(Product.ProductType.SERVICE_OFFER)) {
                ServiceOffer serviceOffer = (ServiceOffer) product;

                if (persistedProduct instanceof ServiceOffer)
                    return serviceOffer.getRequiredCategories().equals(((ServiceOffer) persistedProduct).getRequiredCategories())
                            && serviceOffer.getAmount().equals(persistedProduct.getAmount());
                else
                    return false;
            }
            else if (product.getProductType().equals(Product.ProductType.UPGRADE_OFFER)) {
                UpgradeOffer upgradeOffer = (UpgradeOffer) product;

                if (persistedProduct instanceof UpgradeOffer)
                    return upgradeOffer.getSubscriptionOffers().equals(((UpgradeOffer) persistedProduct).getSubscriptionOffers())
                            && upgradeOffer.getUpgradeType().equals(((UpgradeOffer) persistedProduct).getUpgradeType())
                            && upgradeOffer.getAutoRenewalOffer().equals(((UpgradeOffer) persistedProduct).getAutoRenewalOffer())
                            && upgradeOffer.getAmount().equals(persistedProduct.getAmount()) && upgradeOffer.getDurationUnit()
                            .equals(((UpgradeOffer) persistedProduct).getDurationUnit()) && upgradeOffer.getDuration()
                            == ((UpgradeOffer) persistedProduct).getDuration() && upgradeOffer.getTariff()
                            .equals(((UpgradeOffer) persistedProduct).getTariff()) && upgradeOffer.getCategories()
                            .equals(((UpgradeOffer) persistedProduct).getCategories());
                else
                    return false;
            }
            else if (product.getProductType().equals(Product.ProductType.RENEWAL_OFFER)) {
                RenewalOffer renewalOffer = (RenewalOffer) product;

                if (persistedProduct instanceof RenewalOffer)
                    return renewalOffer.getAutoRenewalOffer().equals(((RenewalOffer) persistedProduct).getAutoRenewalOffer())
                            && renewalOffer.getAmount().equals(persistedProduct.getAmount())&& renewalOffer.getDurationUnit()
                            .equals(((RenewalOffer) persistedProduct).getDurationUnit()) && renewalOffer.getDuration()
                            == ((RenewalOffer) persistedProduct).getDuration() && renewalOffer.getTariff()
                            .equals(((RenewalOffer) persistedProduct).getTariff()) && renewalOffer.getCategories()
                            .equals(((RenewalOffer) persistedProduct).getCategories());
                else
                    return false;
            }
            else if (product.getProductType().equals(Product.ProductType.INITIAL_SUBSCRIPTION_OFFER)) {
                InitialSubscriptionOffer initialOffer = (InitialSubscriptionOffer) product;

                if (persistedProduct instanceof InitialSubscriptionOffer)
                    return initialOffer.getAutoRenewalOffer().equals(((InitialSubscriptionOffer) persistedProduct).getAutoRenewalOffer())
                            &&((initialOffer.getActionCode()==null && persistedProduct.getActionCode()==null)
                            || (initialOffer.getActionCode() !=null && persistedProduct.getActionCode()!=null &&
                            initialOffer.getActionCode().equals(persistedProduct.getActionCode()))) &&
                            initialOffer.getAmount().equals(persistedProduct.getAmount())&& initialOffer.getDurationUnit()
                            .equals(((InitialSubscriptionOffer) persistedProduct).getDurationUnit()) && initialOffer.getDuration()
                            == ((InitialSubscriptionOffer) persistedProduct).getDuration() && initialOffer.getTariff()
                            .equals(((InitialSubscriptionOffer) persistedProduct).getTariff()) && initialOffer.getCategories()
                            .equals(((InitialSubscriptionOffer) persistedProduct).getCategories());
                else
                    return false;
            }
        }
        // product has same key but different class then key must be updated, so we return false
        return false;
    }

    private String mergeActivators(Set<Activator> activators) {
        if (activators == null) return "";
        try {
            final Map<String, PersistedEntityIdentifier> activatorMap = resultMapper.convertToKeyIdentifierMap(activatorRepository.getAllAsIdKeyPair());

            activators.stream().forEach(activator -> {
                PersistedEntityIdentifier persistedActivator = activatorMap.get(resultMapper.convertToLowercasedIdentityKey(activator.getNaturalKey()));
                if (persistedActivator != null) {
                    if (activator.getClass().equals(persistedActivator.getPersistedType())) {
                        activator.setId(persistedActivator.getPersistedId());
                    } else {
                        activator.setNaturalKey(nextKey(resultMapper.convertToLowercasedIdentityKey(activator.getNaturalKey()), activatorMap.keySet(), activator.getNaturalKey()));
                    }
                }
            });

            activatorRepository.save(activators);
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Activators: %s \n",e.getLocalizedMessage());
        }
        return "";
    }


    private void checkProductId(Product product, Map<String, Product> productsMap, Map<String, String> translationCorrectionMap) {
        if (product.getId() != null) return;

        Product persistedProductId = productsMap.get(product.getI18nKey());
        if (persistedProductId != null) {
            if (product.getClass().equals(persistedProductId.getClass())) {
                product.setId(persistedProductId.getId());
            } else {
                String newKey = nextKey(resultMapper.convertToLowercasedIdentityKey(product.getI18nKey()), productsMap.keySet(), product.getKey());
                collectChangedTranslationKeys(product.getBaseName(), product.getI18nKey(), newKey, translationCorrectionMap);
                product.setI18nKey(newKey);
            }
        }
    }

    private void mergeSubscriptionOffer(SubscriptionOffer subscriptionOffer, Map<String, Product> productsMap
            , Map<String, Long> subscriptionOfferCategoriesMap, Map<String, String> translationCorrectionMap, Set<I18NEntity> uploadedTranslations) {

        checkProductId(subscriptionOffer, productsMap, translationCorrectionMap);

        subscriptionOffer.getCategories().forEach(subscriptionOfferCategory -> {
            if (subscriptionOfferCategory.getId() == null) {
                Long persistedCategoryId = subscriptionOfferCategoriesMap.get(resultMapper
                        .convertToLowercasedIdentityKey(subscriptionOffer.getI18nKey() + "_" + subscriptionOfferCategory.getCategory().name()));
                if (persistedCategoryId != null) {
                    subscriptionOfferCategory.setId(persistedCategoryId);
                }
            }

            if (subscriptionOffer.getId() != null) {
                subscriptionOfferCategoryRepository.save(subscriptionOfferCategory);
            }
        });

        if (!subscriptionOffer.getAutoRenewalOffer().equals(subscriptionOffer)) {
            mergeSubscriptionOffer(subscriptionOffer.getAutoRenewalOffer(), productsMap, subscriptionOfferCategoriesMap
                    , translationCorrectionMap, uploadedTranslations);
        }

        if (subscriptionOffer instanceof UpgradeOffer) {
            Iterator<SubscriptionOffer> subIterator = ((UpgradeOffer) subscriptionOffer).getSubscriptionOffers().iterator();
            while (subIterator.hasNext()) {
                SubscriptionOffer offer = subIterator.next();
                Product identifier = productsMap.get(offer.getI18nKey());

                if(identifier != null && identifier.getClass().equals(offer.getDtype())) {
                    offer.setId(identifier.getId());
                    productRepository.save(offer);    //if using these 2 then comment below 2
//                    subIterator.remove();
//                    uploadedTranslations.removeAll(uploadedTranslations.stream().filter(entity ->
//                            entity.getKey().equals(offer.getI18nKey())).collect(Collectors.toSet()));
                    continue;
                }

                mergeSubscriptionOffer(offer, productsMap, subscriptionOfferCategoriesMap, translationCorrectionMap, uploadedTranslations);
            }
        }

        productRepository.save(subscriptionOffer);
    }


    private String mergeLocatables(Set<LocatableEntity> locatableEntities) {
        if (locatableEntities == null) return "";
        try {
            validateLocatables(locatableEntities);

            final Map<String, Long> locatablesMap = resultMapper.convertToStringLongPairMap(locatableRepository.getAllNonZipAsIdKeyPair());
            locatablesMap.putAll(resultMapper.convertToStringLongPairMap(locatableRepository.getAllZipAsIdKeyPair()));

            locatableEntities.stream().forEach(locatableEntity -> {
                String key = "";
                if (locatableEntity.getClass().equals(Zip.class)) {
                    Zip importedZip = (Zip) locatableEntity;
                    key = importedZip.getRegion().getI18nKey() + '_' + importedZip.getZip();
                } else {
                    key = locatableEntity.getUniqueXmlKey();
                }
                Long persistedLocatableId = locatablesMap.get(resultMapper.convertToLowercasedIdentityKey(key));
                if (persistedLocatableId != null) locatableEntity.setId(persistedLocatableId);
            });

            locatableRepository.save(locatableEntities);
        }   catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Locatables: %s \n",e.getLocalizedMessage());
        }
        return "";
    }

    private String mergeFilters(Set<AbstractFilter> filters, Map<String, Long> persistedFilterMap) {
        if (filters == null) return "";
        try {
            Map<String, Long> filterMap = persistedFilterMap;
            if (persistedFilterMap == null) {
                filterMap = resultMapper.convertToStringLongPairMap(templateFilterRepository.getAllAsIdKeyPair());
            }

            for (AbstractFilter filter : filters) {
                if (filter instanceof TemplateFilter) {
                    Long persistedFilterId = filterMap.get(resultMapper.convertToLowercasedIdentityKey(((TemplateFilter) filter).getName()));
                    if (persistedFilterId != null) filter.setId(persistedFilterId);
                    Set<AbstractFilter> subFilter = new HashSet<>();
                    subFilter.add(((TemplateFilter) filter).getFilter());
                    mergeFilters(subFilter, filterMap);
                }

                if (filter instanceof BinaryOperatorFilter) {
                    BinaryOperatorFilter importedFilter = (BinaryOperatorFilter) filter;
                    Set<AbstractFilter> subFilter = new HashSet<>();
                    subFilter.add(importedFilter.getLeftFilter());
                    subFilter.add(importedFilter.getRightFilter());
                    mergeFilters(subFilter, filterMap);
                }

                if (filter instanceof UnaryOperatorFilter) {
                    UnaryOperatorFilter importedFilter = (UnaryOperatorFilter) filter;
                    Set<AbstractFilter> subFilter = new HashSet<>();
                    subFilter.add(importedFilter.getFilter());
                    mergeFilters(subFilter, filterMap);
                }

                filterRepository.save(filter);
            }
        }  catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return String.format("Error in merging Filters: %s \n",e.getLocalizedMessage());
        }
        return "";
    }


    // ===> Utils <===

    private String nextKey(String key, Set<String> keys, String originalKey) {
        Pattern pattern = Pattern.compile("^((" + key + ")(_\\d+)?)");
        List<String> sortedList = keys.stream().filter(item -> pattern.matcher(item).matches()).sorted().collect(Collectors.toList());
        if (sortedList.size() == 0) return originalKey;

        Pattern numberPattern = Pattern.compile("(\\d+)$");
        Matcher matcher = numberPattern.matcher(sortedList.get(sortedList.size() - 1));
        Long nextId = 1L;
        if (matcher.find()) {
            nextId = Long.parseLong(matcher.group(1)) + 1;
        }
        return originalKey + "_" + nextId;
    }

    private void collectChangedTranslationKeys(String basename, String oldKey, String newKey, Map<String, String> translationCorrection) {
        translationCorrection.put(basename + '_' + oldKey, newKey);
    }


    /* === Validations === */

    /**
     * Validates questionnaires if sort order is unique.
     *
     * @param questionnaires list of questionnaires
     */
    private void validateQuestionnairs(Set<Questionnaire> questionnaires) {
        Set<Integer> uniqueCombinations = new HashSet<>();

        Set<Questionnaire> violations = new HashSet<>();

        questionnaires.forEach(questionnaire -> {
            if (!uniqueCombinations.contains(questionnaire.getSortOrder())) {
                uniqueCombinations.add(questionnaire.getSortOrder());
            } else {
                violations.add(questionnaire);
            }
            validateQuestionGroups(new HashSet<>(questionnaire.getQuestionGroups()), questionnaire);
        });

        if (violations.size() > 0) {
            final Integer currentMaxSortOrder = questionnaireRepository.getMaxSortOrder();
            setNewSortOrder(violations, currentMaxSortOrder, uniqueCombinations);
        }
    }


    /**
     * Validates question groups if sort order is unique.
     *
     * @param questionGroups list of question groups for a questionnaire
     * @param questionnaire  questionnaire
     */
    private void validateQuestionGroups(Set<QuestionGroup> questionGroups, Questionnaire questionnaire) {
        Set<Integer> uniqueSortOrder = new HashSet<>();
        Set<QuestionGroup> violations = new HashSet<>();

        questionGroups.forEach(questionGroup -> {
            if (!uniqueSortOrder.contains(questionGroup.getSortOrder())) {
                uniqueSortOrder.add(questionGroup.getSortOrder());
            } else {
                violations.add(questionGroup);
            }

            validateQuestions(new HashSet<>(questionGroup.getQuestions()), questionGroup);
        });

        if (violations.size() > 0) {
            final Integer currentMaxSortOrder = questionGroupRepository.getMaxSortOrderForQuestionnaire(questionnaire);
            setNewSortOrder(violations, currentMaxSortOrder, uniqueSortOrder);
        }
    }

    /**
     * Validates questions if their sort order is unique.
     *
     * @param questions     list of questions for a question group
     * @param questionGroup question group
     */
    private void validateQuestions(Set<Question> questions, QuestionGroup questionGroup) {
        Set<Integer> uniqueSortOrder = new HashSet<>();
        Set<Question> violations = new HashSet<>();

        questions.forEach(question -> {
            if (!uniqueSortOrder.contains(question.getSortOrder())) {
                uniqueSortOrder.add(question.getSortOrder());
            } else {
                violations.add(question);
            }
        });

        if (violations.size() > 0) {
            final Integer currentMaxSortOrder = questionRepository.getMaxSortOrderForQuestionGroup(questionGroup);
            setNewSortOrder(violations, currentMaxSortOrder, uniqueSortOrder);
        }
    }

    /**
     * Validates choices if their sort oder is unique.
     *
     * @param choices     list of choices for a choice group
     * @param choiceGroup choice group
     */
    private void validateChoices(Set<Choice> choices, ChoiceGroup choiceGroup) {
        Set<Integer> uniqueSortOrder = new HashSet<>();
        Set<Choice> violations = new HashSet<>();

        choices.forEach(choice -> {
            if (!uniqueSortOrder.contains(choice.getSortOrder())) {
                uniqueSortOrder.add(choice.getSortOrder());
            } else {
                violations.add(choice);
            }
        });

        if (violations.size() > 0) {
            final Integer currentMaxSortOrder = choiceRepository.getMaxSortOrderForChoiceGroup(choiceGroup);
            setNewSortOrder(violations, currentMaxSortOrder, uniqueSortOrder);
        }
    }

    private void validateLocatables(Set<LocatableEntity> locatableEntities) {

    }

    /**
     * Validate products if they match the constraints requirements.
     *
     * @param products list of products
     */
    private void validateProducts(Map<String, Product> products, Set<I18NEntity> uploadedTranslations) {
        Set<String> invalidProductKeys = new HashSet<>();
        Set<I18NEntity> unwantedTranslations = new HashSet<>();
        LOG.info("start validating total products: "+products.size());
        for (Product product : products.values()) {
            if (product instanceof UpgradeOffer) {
                UpgradeOffer upgradeOffer = (UpgradeOffer) product;

                if (upgradeOffer.getAutoRenewalOffer() == null) {
                    invalidProductKeys.add(product.getI18nKey());
                    addToUnwantedTranslations(product, unwantedTranslations, uploadedTranslations);
                }

                if (upgradeOffer.getUpgradeType() == null) {
                    invalidProductKeys.add(product.getI18nKey());
                    addToUnwantedTranslations(product, unwantedTranslations, uploadedTranslations);
                }

                if (upgradeOffer.getCategories().isEmpty()) {
                    invalidProductKeys.add(product.getI18nKey());
                    addToUnwantedTranslations(product, unwantedTranslations, uploadedTranslations);
                }
            }

            else if (product instanceof RenewalOffer) {
                if (((RenewalOffer) product).getAutoRenewalOffer() == null) {
                    invalidProductKeys.add(product.getI18nKey());
                    addToUnwantedTranslations(product, unwantedTranslations, uploadedTranslations);
                }
            }

            else if (product.getI18nKey() == null) {
                invalidProductKeys.add(product.getI18nKey());
                addToUnwantedTranslations(product, unwantedTranslations, uploadedTranslations);
            }
        }

        invalidProductKeys.forEach(products::remove);
        uploadedTranslations.removeAll(unwantedTranslations);
    }

    private void addToUnwantedTranslations(Product product, Set<I18NEntity> unwantedTranslations, Set<I18NEntity> uploadedTranslations) {
        unwantedTranslations.addAll(uploadedTranslations.stream().filter(entity ->
                entity.getKey().equals(product.getI18nKey())).collect(Collectors.toSet()));
    }

    /**
     * Sets a new sort order for a given list of sortable entities starting with max available sort order.
     *
     * @param sortableObjects    List of sortable entities
     * @param persistedSortOrder Max sort order in database
     * @param importedSortOrders List of sort orders of importing objects
     * @param <T>                Sortable Entitie type
     */
    private <T extends SortableEntity> void setNewSortOrder(Set<T> sortableObjects, Integer persistedSortOrder, Set<Integer> importedSortOrders) {
        final Integer importedMaxSortOrder = importedSortOrders.stream().max(Integer::compare).get();
        Integer newSortOrder = persistedSortOrder > importedMaxSortOrder ? persistedSortOrder : importedMaxSortOrder;

        for (T sortableObject : sortableObjects) {
            sortableObject.setSortOrder(++newSortOrder);
        }
    }

    // ===> Marshalling / Unmarshalling <===

    private Marshaller getMarshaller(JAXBContext context) {
        Marshaller marshaller = null;

        try {
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("com.sun.xml.internal.bind.characterEscapeHandler", new CDATACharacterEscapeHandler());
        } catch (JAXBException e) {
            LOG.error(e.getLocalizedMessage());
        }

        return marshaller;
    }


    private <T> T unmarshalContainer(Class<T> aClass, InputStream inputStream) throws JAXBException {
        JAXBContext jaxbContext;
        jaxbContext = JAXBContext.newInstance(aClass);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return (T) unmarshaller.unmarshal(inputStream);
    }

    private void marshalContainer(SerializationContainer container, OutputStream outputStream) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(container.getClass());

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler", new CDATACharacterEscapeHandler());
            marshaller.marshal(container, outputStream);

        } catch (JAXBException e) {
            LOG.error("Could not marshal container", e);
        }
    }


    private class BaseNameKeyPair {
        private String baseName;
        private String key;

        public BaseNameKeyPair(String baseName, String key) {
            this.baseName = baseName;
            this.key = key;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!obj.getClass().equals(this.getClass())) return false;
            BaseNameKeyPair compairObject = (BaseNameKeyPair) obj;
            return compairObject.baseName.equals(this.baseName) && compairObject.key.equals(this.key);
        }
    }
}
