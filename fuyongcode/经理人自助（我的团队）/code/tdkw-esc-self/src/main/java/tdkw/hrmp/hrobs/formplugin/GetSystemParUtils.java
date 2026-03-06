package tdkw.hrmp.hrobs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.entity.AppInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.param.AppParam;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;

import java.util.Map;
/**
 * @Metadata：
 * @Description ： 获取系统参数
 * @author xxx
 * @Date ：2023/05/22
 * @Version: 1.0
 */
public class GetSystemParUtils {


    public String getSysParByFieldKey(String field){
        //获取当前登录业务单元id
        long orgId = RequestContext.get().getOrgId();
        //从缓存中获取应用信息,应用编码
        AppInfo ptstDemo = AppMetadataCache.getAppInfo("tdkw_hr");
        //获取应用的主键
        String appId = ptstDemo.getId();
        AppParam apm = new AppParam();
        apm.setAppId(appId);
        apm.setOrgId(orgId);
        //获取整体应用参数
        Map<String,Object> paramWhole = SystemParamServiceHelper.loadAppParameterFromCache(apm);
        if(paramWhole.containsKey(field)){
            return String.valueOf(paramWhole.get(field));
        }else{
            return null;
        }
    }
}
