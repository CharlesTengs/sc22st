package backend.service;

import backend.resp.StatisticResp;
import backend.mapper.EbookSnapshotMapperCust;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookSnapshotService {

    @Resource
    private EbookSnapshotMapperCust ebookSnapshotMapperCust;

    public void genSnapshot() {
        ebookSnapshotMapperCust.genSnapshot();
    }

    /**
     * Fetching homepage numerical data: Total reads, total likes
     * today's reads, today's likes, today's estimated reads, today's estimated read growth.
     */
    public List<StatisticResp> getStatistic() {
        return ebookSnapshotMapperCust.getStatistic();
    }

    /**
     * 30 day statistic
     */
    public List<StatisticResp> get30Statistic() {
        return ebookSnapshotMapperCust.get30Statistic();
    }

}
