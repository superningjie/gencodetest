package cds0.opmc.cea.business;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.TypesContainer;
import kd.bos.dataentity.resource.ResManager;

import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {

    private final static Map<String, String> SERVICE_MAP = new HashMap<>();
    static{
        SERVICE_MAP.put("DimSettingEntityService", "cds0.opmc.cea.business.entityservice.DimSettingEntityService");
        SERVICE_MAP.put("EvalDimSettingEntityService", "cds0.opmc.cea.business.entityservice.EvalDimSettingEntityService");
        SERVICE_MAP.put("AssessTaskEntityService", "cds0.opmc.cea.business.entityservice.AssessTaskEntityService");
        SERVICE_MAP.put("AssessTaskDomainService", "cds0.opmc.cea.business.service.AssessTaskDomainService");
        SERVICE_MAP.put("MessageEntityService", "cds0.opmc.cea.business.entityservice.MessageEntityService");
        SERVICE_MAP.put("MessageService", "cds0.opmc.cea.business.service.MessageService");
        SERVICE_MAP.put("AssessActivityEntityService", "cds0.opmc.cea.business.entityservice.AssessActivityEntityService");
        SERVICE_MAP.put("PerfFileEntityService", "cds0.opmc.cea.business.entityservice.PerfFileEntityService");
        SERVICE_MAP.put("AssessActDimSettingDomainService", "cds0.opmc.cea.business.service.AssessActDimSettingDomainService");
        SERVICE_MAP.put("AssessObjEntityService", "cds0.opmc.cea.business.entityservice.AssessObjEntityService");
        SERVICE_MAP.put("AssessTaskListEntityService", "cds0.opmc.cea.business.entityservice.AssessTaskListEntityService");
        SERVICE_MAP.put("AssObjScoItemInstEntityService", "cds0.opmc.cea.business.entityservice.AssObjScoItemInstEntityService");
        SERVICE_MAP.put("DimassesserDomainService", "cds0.opmc.cea.business.service.DimassesserDomainService");
        SERVICE_MAP.put("DimassesserEntityService", "cds0.opmc.cea.business.entityservice.DimassesserEntityService");
        SERVICE_MAP.put("AssessFormEntityService", "cds0.opmc.cea.business.entityservice.AssessFormEntityService");
        SERVICE_MAP.put("AssessFormRowEntityService", "cds0.opmc.cea.business.entityservice.AssessFormRowEntityService");
        SERVICE_MAP.put("PerformanceLevelEntityService", "cds0.opmc.cea.business.entityservice.PerformanceLevelEntityService");
        SERVICE_MAP.put("AssessObjDomainService", "cds0.opmc.cea.business.service.AssessObjDomainService");
        SERVICE_MAP.put("HandlerFindDomainService", "cds0.opmc.cea.business.service.HandlerFindDomainService");
        SERVICE_MAP.put("HrpiServiceDomianService", "cds0.opmc.cea.business.service.HrpiServiceDomianService");
        SERVICE_MAP.put("ScoreSystemEntityService", "cds0.opmc.cea.business.entityservice.ScoreSystemEntityService");
        SERVICE_MAP.put("AsserSeekConfEntityService", "cds0.opmc.cea.business.entityservice.AsserSeekConfEntityService");
        SERVICE_MAP.put("UserEntityService", "cds0.opmc.cea.business.entityservice.UserEntityService");
        SERVICE_MAP.put("SystemParamEntityService", "cds0.opmc.cea.business.entityservice.SystemParamEntityService");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> clazz) {
        return (T)getService(clazz.getSimpleName());
    }

    /**
     * 获取服务
     *
     * @param serviceName 服务名
     * @return 服务
     */
    public static Object getService(String serviceName) {
        String className = SERVICE_MAP.get(serviceName);
        if (className == null) {
            throw new RuntimeException(String.format(
                    ResManager.loadKDString("%s对应的服务实现未找到", "ServiceFactory_0", AppflgConstant.KEY_APP_NAME), serviceName));
        }
        return TypesContainer.getOrRegisterSingletonInstance(className);
    }
}