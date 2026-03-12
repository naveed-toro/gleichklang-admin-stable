package de.binaerebauten.gleichklang.core.service.userStatstics;

import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UserStatisticsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserStatisticsService.class);
    private static final int PAGE_SIZE = 1000;

    @Autowired
    private UserRepository userRepository;

    /**
     * This method regularly checks active users and updates their stats
     */
    @Scheduled(cron = "0 45 2 * * *", zone = "Europe/Berlin")//every day at 2:45 am
//    @Scheduled(fixedRate = 180000)
    public void userStatistics() {
        LOG.info("Starting generateUserStatistics");
        generateUserStatistics();
    }

    private void generateUserStatistics() {
        boolean recordsRemaining = true;
        long totalRecordsProcessed = 0L;
        try {
            while (recordsRemaining) {
                Long result = userRepository.generateUserStatistics(PAGE_SIZE);//.get(0, Long.class);
                totalRecordsProcessed = totalRecordsProcessed + result;
                if (result == 0)
                    recordsRemaining = false;
            }
        } catch (Exception e) {
            LOG.error("error in generateUserStatistics {}", e.getMessage());
        }
        LOG.info("end of generateUserStatistics with total records processed: {}", totalRecordsProcessed);
    }

}
