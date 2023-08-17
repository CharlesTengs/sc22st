package backend.job;

import backend.service.DocService;
import backend.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DocJob {

    private static final Logger LOG = LoggerFactory.getLogger(DocJob.class);

    @Resource
    private DocService docService;

    @Resource
    private SnowFlake snowFlake;

    /**
     * Update e-book information every 30 seconds
     */
    @Scheduled(cron = "5/30 * * * * ?") // Update every 30 seconds
    public void cron() {
        // Add log serial number
        MDC.put("LOG_ID", String.valueOf(snowFlake.nextId()));
        LOG.info("Start updating document data under the e-book");
        long start = System.currentTimeMillis();
        docService.updateEbookInfo();
        LOG.info("End of updating，time taken: {}milliseconds", System.currentTimeMillis() - start);
    }

}
