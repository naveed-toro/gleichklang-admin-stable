//package de.binaerebauten.gleichklang.core.utils;
//
//import de.binaerebauten.gleichklang.core.model.cache.CachedValue;
//import de.binaerebauten.gleichklang.core.model.cache.NumberCachedValue;
//import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
//import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
//import de.binaerebauten.gleichklang.core.repository.CachedValueRepository;
//import de.binaerebauten.gleichklang.core.service.report.AffinityResult;
//import de.binaerebauten.gleichklang.core.service.report.AffinityUserResult;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;
//
///**
// * Unit tests for {@link ResultMapper}.
// */
//public class ResultMapperTest extends BasePersistenceTest {
//
//    @Autowired
//    private ResultMapper resultMapper;
//
//    @Autowired
//    private CachedValueRepository cachedValueRepository;
//
//    private static final int NUMBER_OF_SAMPLES = 5;
//    private static final String CACHE_GROUP = "test_cacheGroup";
//
//    @Before
//    public void setup() {
//        cachedValueRepository.deleteAll();
//        for (int i=0; i < NUMBER_OF_SAMPLES; i++) {
//            NumberCachedValue meanCachedValue = new NumberCachedValue("mean_test$" + i, (double) i);
//            NumberCachedValue stddevCachedValue = new NumberCachedValue("stddev_test$" + i, (double) i);
//            meanCachedValue.setCacheGroup(CACHE_GROUP);
//            stddevCachedValue.setCacheGroup(CACHE_GROUP);
//            cachedValueRepository.save(meanCachedValue);
//            cachedValueRepository.save(stddevCachedValue);
//        }
//    }
//
//
//    @Test
//    public void testConvertToAffinityResult() {
//
//        final float mean = 10.5f;
//        final float std = 2.32f;
//        final String category = "test";
//
//        List<Object[]> sampleList = new ArrayList<>();
//        sampleList.add(new Object[] {new BigDecimal(mean), new BigDecimal(std), category});
//
//
//        List<AffinityResult> mappingResult = resultMapper.convertToAffinityResult(sampleList);
//
//        assertThat("Number of mapped results", mappingResult.size(), is(1));
//        assertThat("Type of mapped item", mappingResult.get(0).getClass(), equalTo(AffinityResult.class));
//        AffinityResult affinityResult = mappingResult.get(0);
//        assertThat(affinityResult.getMean(), is(mean));
//        assertThat(affinityResult.getStandardDeviation(), is(std));
//        assertThat(affinityResult.getCategory(), is(category));
//    }
//
//    @Test
//    public void testConvertToAffinityUserResult() {
//        final Integer sum = 25;
//        final String category = "test";
//
//        List<Object[]> samples = new ArrayList<>();
//        samples.add(new Object[] {new BigDecimal(sum), category});
//
//        List<AffinityUserResult> mappingResult = resultMapper.convertToAffinityUserResult(samples);
//        assertThat("Number of mapped results", mappingResult.size(), is(1));
//        assertThat("Type of mapped item", mappingResult.get(0).getClass(), equalTo(AffinityUserResult.class));
//        AffinityUserResult affinityUserResult = mappingResult.get(0);
//        assertThat(affinityUserResult.getSum(), is(sum));
//        assertThat(affinityUserResult.getCategory(), is(category));
//    }
//
//    @Test
//    public void testConvertToCachedAffinityResult() {
//        List<CachedValue> cachedValues = cachedValueRepository.findByCacheGroup(CACHE_GROUP);
//        List<AffinityResult> convertedAffinityResults = resultMapper.convertToCachedAffinityResult(cachedValues);
//
//        assertThat(convertedAffinityResults.size(), is(cachedValues.size() / 2));
//        for (AffinityResult affinityResult : convertedAffinityResults) {
//            final float sampleNumber = new Float(affinityResult.getCategory().split("\\$")[1]);
//            assertThat(affinityResult.getMean(), is(sampleNumber));
//            assertThat(affinityResult.getStandardDeviation(), is(sampleNumber));
//        }
//
//    }
//
//    @Test
//    public void testConvertToQuestionGroupMap() {
//        final String key = "test_key";
//
//        QuestionGroup questionGroup = new QuestionGroup();
//        questionGroup.setI18nKey("test_key");
//        List<QuestionGroup> questionGroups = new ArrayList<>();
//        questionGroups.add(questionGroup);
//
//        Map<String, QuestionGroup> convertedResult = resultMapper.convertToQuestionGroupMap(questionGroups);
//        assertThat("Number of converted items", convertedResult.size(), is(1));
//        assertThat("Item is in Map", convertedResult.keySet().contains(key), is(true));
//        assertThat("Is same item", convertedResult.get(key), equalTo(questionGroup));
//    }
//}
