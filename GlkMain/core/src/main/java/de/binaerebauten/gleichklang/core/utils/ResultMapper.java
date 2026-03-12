package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.*;
import de.binaerebauten.gleichklang.core.model.cache.CachedValue;
import de.binaerebauten.gleichklang.core.model.cache.NumberCachedValue;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.service.report.AffinityResult;
import de.binaerebauten.gleichklang.core.service.report.AffinityUserResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Result Mapper for Repository results
 */
@Component
public class ResultMapper {

    private final static String CACHE_KEY_DEVIDER = "_";
    private final static String CACHE_KEY_MEAN = "mean";
    private final static String CACHE_KEY_STDDEV = "stddev";

    /**
     * Converts the query result from @see AnswerRepository#getAggregatedAffinityResult list of Object[] to a list of AffinityResults
     *
     * @param queryResultAsObjects list of Object arrays
     * @return converted typed list
     */
    public List<AffinityResult> convertToAffinityResult(List<Object[]> queryResultAsObjects) {
        return queryResultAsObjects.stream().map(item -> new AffinityResult(((BigDecimal)item[0]).floatValue(), ((BigDecimal)item[1]).floatValue(), (String)item[2])).collect(Collectors.toList());
    }

    public List<AffinityResult> convertToCachedAffinityResult(List<CachedValue> queryResultAsCachedResult) {
        Map<String, AffinityResult> affinityResultMap = new HashMap<>();

        queryResultAsCachedResult.forEach(cachedValue -> {
            String[] key = cachedValue.getCacheKey().split(CACHE_KEY_DEVIDER, 2);

            AffinityResult affinityResult = affinityResultMap.get(key[1]);
            if (affinityResult == null) {
                affinityResult = new AffinityResult(key[1]);
                affinityResultMap.put(key[1], affinityResult);
            }

            if (key[0].equals(CACHE_KEY_MEAN)) {
                affinityResult.setMean(new Float(((NumberCachedValue)cachedValue).getValue()));
            } else {
                affinityResult.setStandardDeviation(new Float(((NumberCachedValue)cachedValue).getValue()));
            }
        });

        return affinityResultMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Converts the query result list of Object[] to a list of AffinityUserResults
     *
     * @param queryResultsAsObjects list of Objects arrays
     * @return converted typed list
     */
    public List<AffinityUserResult> convertToAffinityUserResult(List<Object[]> queryResultsAsObjects) {


        return queryResultsAsObjects.stream().map(item -> new AffinityUserResult((String)item[1], ((BigDecimal)item[0]).intValue())).collect(Collectors.toList());
    }

    /**
     * Converts the result of QuestionGroup Query to a map of i18nKey and QuestionGroup.
     *
     * @param queryResultAsObject
     * @return
     */
    public Map<String, QuestionGroup> convertToQuestionGroupMap(List<QuestionGroup> queryResultAsObject) {
        return queryResultAsObject.stream().collect(Collectors.toMap(item -> convertToLowercasedIdentityKey(item.getI18nKey()), item -> item));
    }

    public Map<String, Long> convertToStringLongPairMap(List<IdKeyPairResult> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(item -> convertToLowercasedIdentityKey(item.getKeyValue()), IdKeyPairResult::getIdValue, (i1,i2) -> i1));
    }

    public Map<String, PersistedEntityIdentifier> convertToKeyIdentifierMap(List<IdKeyPairResult> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(item -> convertToLowercasedIdentityKey(item.getKeyValue()), item -> new PersistedEntityIdentifier(item.getIdValue(), item.getClassType())));
    }

    public <T extends BaseEntity> Map<Long, T> convertToIdObjectMap(List<T> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
    }

    public <T extends SortableEntity> Map<Integer, T> convertToSortOrderObjectMap(List<T> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(SortableEntity::getSortOrder, item -> item));
    }

    public <T extends LocalizedEntity> Map<String, T> convertToI18NKeyObjectMap(List<T> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(item -> convertToLowercasedIdentityKey(item.getI18nKey()), item -> item));
    }

    public Map<Integer, Choice> convertToSortOrderChoiceMap(List<Choice> queryResult) {
        return queryResult.stream().collect(Collectors.toMap(Choice::getSortOrder, item -> item));
    }

    public Set<String> normalizeResult(Set<String> queryResult) {
        return queryResult.stream().map(this::convertToLowercasedIdentityKey).collect(Collectors.toSet());
    }

    public String convertToLowercasedIdentityKey(String key) {
        if (key == null) return null;

        String normalizedString = Normalizer.normalize(key.toLowerCase(), Normalizer.Form.NFKC);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(normalizedString.toLowerCase()).replaceAll("").replaceAll("[ä]", "a").replaceAll("[ü]", "u").replaceAll("[ö]", "o");
    }
}
