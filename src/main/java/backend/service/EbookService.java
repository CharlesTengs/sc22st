package backend.service;

import backend.domain.Ebook;
import backend.req.EbookSaveReq;
import backend.resp.EbookQueryResp;
import backend.resp.PageResp;
import backend.util.CopyUtil;
import backend.util.SnowFlake;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import backend.domain.EbookExample;
import backend.mapper.EbookMapper;
import backend.req.EbookQueryReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookService {

    private static final Logger LOG = LoggerFactory.getLogger(EbookService.class);

    @Resource
    private EbookMapper ebookMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<EbookQueryResp> list(EbookQueryReq req) {
        EbookExample ebookExample = new EbookExample();
        //Initialize query criteria
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        if (!ObjectUtils.isEmpty(req.getName())) {
            // Criteria for fuzzy search
            criteria.andNameLike("%" + req.getName() + "%");
        }
            // Criteria for searching by Category
        if (!ObjectUtils.isEmpty(req.getCategoryId2())) {
            criteria.andCategory2IdEqualTo(req.getCategoryId2());
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

        PageInfo<Ebook> pageInfo = new PageInfo<>(ebookList);
        // list copy and Data transformation
        List<EbookQueryResp> list = CopyUtil.copyList(ebookList, EbookQueryResp.class);
        PageResp<EbookQueryResp> pageResp = new PageResp();
        pageResp.setTotal(pageInfo.getTotal());  // useful to the client side
        pageResp.setList(list);

        return pageResp;
    }



    public void save(EbookSaveReq req) {  // method in EbookService Class
        Ebook ebook = CopyUtil.copy(req, Ebook.class);
        if (ObjectUtils.isEmpty(req.getId())) {  // ebook id is empty
            // add
            ebook.setId(snowFlake.nextId());
            ebook.setDocCount(0);
            ebook.setViewCount(0);
            ebook.setVoteCount(0);
            ebookMapper.insert(ebook);
        } else {
            // update existing ebook
            ebookMapper.updateByPrimaryKey(ebook);
        }
    }


    public void delete(Long id) {
        ebookMapper.deleteByPrimaryKey(id);
    }
}
