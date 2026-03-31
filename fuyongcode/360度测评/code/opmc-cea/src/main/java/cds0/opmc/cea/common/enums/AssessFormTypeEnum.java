package cds0.opmc.cea.common.enums;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;

public enum AssessFormTypeEnum {
    GRADE(ResManager.loadKDString("打等","AssessFormTypeEnum_0", AppflgConstant.KEY_APP_NAME),"20"),
    SCORE(ResManager.loadKDString("打分","AssessFormTypeEnum_1", AppflgConstant.KEY_APP_NAME),"10");

    private String key;

    private String value;

    AssessFormTypeEnum(String key, String value) {
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
