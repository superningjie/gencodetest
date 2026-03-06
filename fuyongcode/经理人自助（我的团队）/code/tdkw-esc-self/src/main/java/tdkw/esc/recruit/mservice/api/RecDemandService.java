package tdkw.esc.recruit.mservice.api;

/**
 * 招聘需求服务接口
 *
 * @author xysusj
 */
public interface RecDemandService {


    /**
     * 在北森创建招聘需求
     *
     * @param billNo 单据编号
     */
    void createDemand(String billNo);

}
