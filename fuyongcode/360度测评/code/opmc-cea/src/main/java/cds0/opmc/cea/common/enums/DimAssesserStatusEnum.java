package cds0.opmc.cea.common.enums;

import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.dataentity.resource.ResManager;

public enum DimAssesserStatusEnum {

    UNFILLED(ResManager.loadKDString("未填报","DimAssesserStatusEnum_0",AppflgConstant.KEY_APP_NAME),"10"),
    FILLING(ResManager.loadKDString("填报中","DimAssesserStatusEnum_1",AppflgConstant.KEY_APP_NAME),"20"),
    FILLED(ResManager.loadKDString("已填报","DimAssesserStatusEnum_2",AppflgConstant.KEY_APP_NAME),"30");

    private final String name;
    private final String value;

    public String getValue() {
        return value;
    }
    public String getName() {
        return name;
    }

    DimAssesserStatusEnum(String name, String value){
        this.name = name;
        this.value = value;
    }
}
