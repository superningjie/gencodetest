package cds0.opmc.cea.common.enums;

public enum ActAnonymousEnum {

    /**
     * 实名
     */
    REAL( "1"),

    /**
     * 匿名
     */
    ANONYMOUS( "2");


    private String code;


    ActAnonymousEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
