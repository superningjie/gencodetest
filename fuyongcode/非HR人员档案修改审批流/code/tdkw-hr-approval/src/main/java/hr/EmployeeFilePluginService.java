package hr;

import kd.bos.context.RequestContext;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.sdk.hr.hspm.common.ext.file.EmployeeBillParamsDTO;
import kd.sdk.hr.hspm.common.ext.file.EmployeeChangeRecordFieldDTO;
import kd.sdk.hr.hspm.formplugin.web.file.ermanfile.ext.service.employee.IEmployeeFilePluginService;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description  我的档案埋点
 */
public class EmployeeFilePluginService implements IEmployeeFilePluginService {
    private static final Log LOGGER = LogFactory.getLog(EmployeeFilePluginService.class);

    public EmployeeFilePluginService() {
    }

    @Override
    public void modifyEmployeeBillDetail(EmployeeBillParamsDTO employeeBillParamsDTO) {

    }

    @Override
    public void modifyChangeRecordDetail(EmployeeChangeRecordFieldDTO employeeChangeRecordFieldDTO) {

    }

    @Override
    public Map<String, Object> openAssignMobileFile() {
        Map<String, Object> map = new HashMap<>();
        IAppCache appCache = AppCache.get("hspm");
        Long currUserId = RequestContext.get().getCurrUserId();
        if(appCache.get(currUserId+"hspm_myermanfile"+"personId",Object.class)!=null){
            map.put("personId",appCache.get(currUserId+"hspm_myermanfile"+"personId",Object.class));
        }
        if(appCache.get(currUserId+"hspm_myermanfile"+"configId",Object.class)!=null){
            map.put("configId",appCache.get(currUserId+"hspm_myermanfile"+"configId",Object.class));
        }
        // 2024.12.26 注释缓存赋NULL代码，解决档案修改提交成功后人员信息变为登录人信息问题
//        appCache.put(currUserId+"hspm_myermanfile"+"personId",null);
//        appCache.put(currUserId+"hspm_myermanfile"+"configId",null);
        return map;
    }
}
