package tdkw.hrmp.hrobs.api;

/**
 * @author xxx
 * @Date: 2024/6/24
 * @Description: 人员简历权限
 **/
public interface ResumeQueryAuthorityService {
    /**
     * 人员简历权限
     *
     * @param erfileId 人事业务档案id
     * @param platform PC，APP平台
     * @return 是否有人员简历权限
     */
    boolean queryAuthority(String erfileId, String platform);
}
