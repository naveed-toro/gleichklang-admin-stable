package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity;
import de.binaerebauten.gleichklang.core.model.cache.CachedValue;
import de.binaerebauten.gleichklang.core.model.cache.NumberCachedValue;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.CachedValueRepository;
import de.binaerebauten.gleichklang.core.service.report.AffinityResult;
import de.binaerebauten.gleichklang.core.utils.ResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Domi on 04.10.2016.
 */
@Service
public class ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private CachedValueRepository cachedValueRepository;

    private static final String PREFIX_MEAN = "mean_";
    private static final String PREFIX_STANDARD_DEVIATION = "stddev_";

    /**
     * Recalculates the mean and standard deviation for affinity answers for
     * all users.
     *
     * Runs on every 6th of the month at 0:00
     */
    @Scheduled(cron = "0 0 0 6 * ?")
    @Transactional
    public void recalculateAffinityValues() {
        LOG.info("Re-calculating affinity values for all users");
        try {
	        List<NaturalKeyEntity.NaturalKey> naturalKeys = new ArrayList<NaturalKeyEntity.NaturalKey>();
	        naturalKeys.add(NaturalKeyEntity.NaturalKey.PROFILE_FREUNDSCHAFT);
	        naturalKeys.add(NaturalKeyEntity.NaturalKey.PROFILE_GESELLSCHAFT);
	        naturalKeys.add(NaturalKeyEntity.NaturalKey.PROFILE_PARTNERSCHAFT);
	        naturalKeys.add(NaturalKeyEntity.NaturalKey.PROFILE_PERSOENLICHKEIT);
	
	        naturalKeys.forEach(naturalKey -> {
	            final List<AffinityResult> result = resultMapper.convertToAffinityResult(answerRepository.getAggregatedAffinityResultsForKey(naturalKey.naturalKey));
	            saveCachedAffinityValues(result, naturalKey.naturalKey);
	        });
        }catch (Exception e) {
			LOG.error("recalculateAffinityValues", e);
		}
    }

    public void saveCachedAffinityValues(List<AffinityResult> affinityResults, String cacheGroup) {
        List<CachedValue> cachedValues = cachedValueRepository.findByCacheGroup(cacheGroup);
        Map<String, NumberCachedValue> numberCachedValueMap = cachedValues.stream().collect(Collectors.toMap(item -> item.getCacheKey(), item -> (NumberCachedValue)item));


        for (AffinityResult affinityResult : affinityResults) {
            NumberCachedValue meanCachedValue = numberCachedValueMap.get(PREFIX_MEAN + affinityResult.getCategory());
            NumberCachedValue stddevCachedValue = numberCachedValueMap.get(PREFIX_STANDARD_DEVIATION + affinityResult.getCategory());

            meanCachedValue.setValue(new Double(affinityResult.getMean()));
            stddevCachedValue.setValue(new Double(affinityResult.getStandardDeviation()));
        }

        cachedValueRepository.save(cachedValues);
    }
}
