package tdkw.hrmp.hrobs.mservice;

import tdkw.hrmp.hrobs.api.ResumeQueryAuthorityService;
import tdkw.hrmp.hrobs.common.result.ResumePermissions;

/**
 * @author xxx
 * @Date: 2024/6/24
 * @Description: 人员简历权限
 **/
public class ResumeQueryAuthorityServiceImpl implements ResumeQueryAuthorityService {
    @Override
    public boolean queryAuthority(String erfileId, String platform) {
        return ResumePermissions.queryAuthority(erfileId, platform);
    }
}
