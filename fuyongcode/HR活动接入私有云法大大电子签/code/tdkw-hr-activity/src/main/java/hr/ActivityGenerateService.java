package hr;

import kd.bos.dataentity.entity.DynamicObject;

/**
 * @Description 活动生成服务
 * @Version 1.0.0
 * @Date 2024/9/5 17:07
 * @Created by sxf
 */
public interface ActivityGenerateService {

    void initActivities(DynamicObject[] dynamicObjects,Long activityInsId);

    DynamicObject generateQuitCertifyActivity(Long id, DynamicObject dyn);
}
