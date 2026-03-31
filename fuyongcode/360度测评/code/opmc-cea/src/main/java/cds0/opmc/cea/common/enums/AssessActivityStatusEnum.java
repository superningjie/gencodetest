package cds0.opmc.cea.common.enums;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;

public enum AssessActivityStatusEnum {
    TOBESTARTUP(ResManager.loadKDString("待启动","AssessActivityStatusEnum_0",AppflgConstant.KEY_APP_NAME),"10"),
    HAVINGIN(ResManager.loadKDString("进行中","AssessActivityStatusEnum_1",AppflgConstant.KEY_APP_NAME),"20"),
    FINISHED(ResManager.loadKDString("已结束","AssessActivityStatusEnum_2",AppflgConstant.KEY_APP_NAME),"30");

    private final String name;
    private final String value;

    public String getValue() {
        return value;
    }

    AssessActivityStatusEnum(String name, String value){
        this.name = name;
        this.value = value;
    }
}
