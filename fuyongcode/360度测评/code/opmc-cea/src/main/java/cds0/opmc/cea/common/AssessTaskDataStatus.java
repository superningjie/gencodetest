package cds0.opmc.cea.common;

public enum AssessTaskDataStatus {
    VALID("有效", "0"),
    INVALID("失效", "1");
    private String key;

    private String value;

    AssessTaskDataStatus(String key, String value) {
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
