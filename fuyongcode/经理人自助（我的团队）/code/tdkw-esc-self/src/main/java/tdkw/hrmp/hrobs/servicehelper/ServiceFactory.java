package tdkw.hrmp.hrobs.servicehelper;

import kd.bos.dataentity.TypesContainer;
import kd.bos.dataentity.resource.ResManager;

import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {
    private static Map<String, String> serviceMap = new HashMap<>();

    public ServiceFactory() {
    }

    public static Object getService(String serviceName) {
        String className = serviceMap.get(serviceName);
        if (className == null) {
            throw new RuntimeException((ResManager.loadKDString("%s对应的服务实现未找到", "ServiceFactory_0", "tdkw_tlmg-servicehelper-ext", serviceName)));
        } else {
            return TypesContainer.getOrRegisterSingletonInstance(className);
        }
    }

    static {
        serviceMap.put("ResumeQueryAuthorityService", "tdkw.hrmp.hrobs.mservice.ResumeQueryAuthorityServiceImpl");
    }
}
