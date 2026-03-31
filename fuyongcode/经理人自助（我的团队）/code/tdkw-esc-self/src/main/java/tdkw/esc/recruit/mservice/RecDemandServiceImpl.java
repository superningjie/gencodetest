package tdkw.esc.recruit.mservice;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import tdkw.esc.recruit.common.RecruitDemandUtil;
import tdkw.esc.recruit.mservice.api.RecDemandService;

/**
 * 公寓系统服务接口实现类
 *
 * @author xysusj
 */
public class RecDemandServiceImpl implements RecDemandService {

    private final static Log logger = LogFactory.getLog(RecDemandServiceImpl.class);


    @Override
    public void createDemand(String billNo) {
        logger.info(String.format("创建招聘需求微服务，createDemand，单据编号=%s", billNo));
        RecruitDemandUtil.createRequirement(billNo);
        logger.info("创建招聘需求微服务，createDemand，结束");
    }
}