package tdkw.hrmp.hrobs.common.hrobs;

/**
 * 定额来源
 */
public enum QuoteSourceEnum {

    SYS("系统生成", "DT-000"),
    CARRY_FORWARD("往期结转", "DT-001"),
    MANUAL("手动生成", "DT-002"),
    PAST_OVERDRAFT("往期透支", "DT-003"),
    CARRY_BU("跨考勤管理组织结算", "DT-005");

    private final String name;

    private final String value;


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    QuoteSourceEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }


}
