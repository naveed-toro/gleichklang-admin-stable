package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.core.model.cache.CachedValue;
import de.binaerebauten.gleichklang.core.model.cache.NumberCachedValue;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.CachedValueRepository;
import de.binaerebauten.gleichklang.core.service.report.AffinityResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Domi on 04.10.2016.
 */
@ContextConfiguration(classes = { AdminTestConfig.class })
public class ProfileServiceTest extends BasePersistenceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CachedValueRepository cachedValueRepository;

    private static final int NUMBER_OF_SAMPLES = 5;
    private static final String CACHE_GROUP = "testCacheGroup";

    @Before
    public void setup() {
        cachedValueRepository.deleteAll();

        for (int i=0; i < NUMBER_OF_SAMPLES; i++) {
            NumberCachedValue meanCachedValue = new NumberCachedValue("mean_test$" + i, (double) i);
            NumberCachedValue stddevCachedValue = new NumberCachedValue("stddev_test$" + i, (double) i);
            meanCachedValue.setCacheGroup(CACHE_GROUP);
            stddevCachedValue.setCacheGroup(CACHE_GROUP);
            cachedValueRepository.save(meanCachedValue);
            cachedValueRepository.save(stddevCachedValue);
        }
    }

    @Test
    public void testSavingAffinityResults() {
        List<AffinityResult> affinityResults = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
            affinityResults.add(new AffinityResult((float) (i * 10), (float) (i / 10.0), "test$"+  i));
        }

        profileService.saveCachedAffinityValues(affinityResults, CACHE_GROUP);

        List<CachedValue> persistedCachedValues = cachedValueRepository.findByCacheGroup(CACHE_GROUP);
        assertThat(persistedCachedValues.size(), is(NUMBER_OF_SAMPLES * 2));

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        for (CachedValue cachedValue : persistedCachedValues) {
            NumberCachedValue numberCachedValue = (NumberCachedValue)cachedValue;
            final String[] keyComponents = numberCachedValue.getCacheKey().split("\\$");
            assertThat(keyComponents.length, is(2));

            final int sampleNumber = new Integer(keyComponents[1]);

            final String[] typeComponents = numberCachedValue.getCacheKey().split("_");
            assertThat(typeComponents.length, is(2));

            if (typeComponents[0].equals("mean")) {
                assertThat(decimalFormat.format(numberCachedValue.getValue()), is(decimalFormat.format((double)(sampleNumber * 10))));
            } else {
                assertThat(decimalFormat.format(numberCachedValue.getValue()), is(decimalFormat.format(sampleNumber / 10.0)));
            }
        }
    }
}
