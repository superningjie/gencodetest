package tdkw.esc.recruit.servicehelper;

import kd.bos.dataentity.TypesContainer;
import kd.bos.dataentity.resource.ResManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 服务注册类
 *
 * @author kingdee
 * @since 1990-1-1
 */
public class ServiceFactory {
    private static Map<String, String> serviceMap = new HashMap<>();

    static {
        serviceMap.put("RecDemandService", "tdkw.esc.recruit.mservice.RecDemandServiceImpl");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> clazz) {
        return (T) getService(clazz.getSimpleName());
    }

    /**
     * 获取服务实现
     *
     * @param serviceName 服务名称
     * @return Object
     */
    public static Object getService(String serviceName) {
        String className = serviceMap.get(serviceName);
        if (className == null) {
            throw new RuntimeException(String.format(Locale.ROOT,
                    ResManager.loadKDString("%s对应的服务实现未找到", "ServiceFactory_0", "tdkw.esc.recruit.servicehelper"), serviceName));
        }
        return TypesContainer.getOrRegisterSingletonInstance(className);
    }
}

