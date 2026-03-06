package cds0.opmc.cea.common.enums;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;

public enum AssessStatusEnum {
    TOASSESS(ResManager.loadKDString("待启动","AssessStatusEnum_0", AppflgConstant.KEY_APP_NAME),"10"),
    ASSESSINGIN(ResManager.loadKDString("测评中","AssessStatusEnum_1",AppflgConstant.KEY_APP_NAME),"20"),
    PROCESSED(ResManager.loadKDString("已完成","AssessStatusEnum_2",AppflgConstant.KEY_APP_NAME),"30");

    private final String name;
    private final String value;

    public String getValue() {
        return value;
    }

    AssessStatusEnum(String name, String value){
        this.name = name;
        this.value = value;
    }
}
