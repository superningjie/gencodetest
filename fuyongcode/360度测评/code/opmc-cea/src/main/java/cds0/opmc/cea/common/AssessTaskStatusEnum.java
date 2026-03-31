package cds0.opmc.cea.common;

import kd.bos.dataentity.resource.ResManager;

public enum AssessTaskStatusEnum {
    WAITTING(ResManager.loadKDString("待处理", "AssessTaskStatus_0", AppflgConstant.KEY_APP_NAME), "10"),
    TEMPSTORAGE(ResManager.loadKDString("暂存", "AssessTaskStatus_1", AppflgConstant.KEY_APP_NAME),"20"),
    PROCESSED(ResManager.loadKDString("已处理", "AssessTaskStatus_2", AppflgConstant.KEY_APP_NAME), "30"),
    EXPRIED(ResManager.loadKDString("已失效", "AssessTaskStatus_3", AppflgConstant.KEY_APP_NAME), "40");

    private String key;

    private String value;

    AssessTaskStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
