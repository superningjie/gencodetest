package tdkw.hrmp.hrobs.common.hrobs.pojo;

import java.io.Serializable;
import java.util.Set;

/**
 * @author xxx
 * @Date 2023/5/26 17:55
 * @Description PC端 当月出勤卡片 假勤门户期间汇总项目统计结果 实体类
 * @Demander xxx
 * @Document 员工自助门户与个人卡片-XXX集团需求规格说明书_V2.0(4)
 * @Basedata tdkw_hrobs_pc_attendance
 * @Version 1.0
 **/
public class AttPeriodItemStat implements Serializable {

    private static final long serialVersionUID = -2919216265011713319L;

    /**
     * 期间汇总数据源名称，统计项目显示的名称
     */
    private String name;

    /**
     * 期间汇总数据源 - 考勤项目ID集合，被统计的考勤项目ID
     */
    private Set<Long> attItems;

    /**
     * 统计数值，已经格式化未字符串
     */
    private String attItemValue;

    /**
     * 统计数值单位编码
     */
    private String unit;

    /**
     * 统计数值单位名称
     */
    private String unitName;

    /**
     * 统计数值数据类型
     */
    private String datatype;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getAttItems() {
        return attItems;
    }

    public void setAttItems(Set<Long> attItems) {
        this.attItems = attItems;
    }

    public String getAttItemValue() {
        return attItemValue;
    }

    public void setAttItemValue(String attItemValue) {
        this.attItemValue = attItemValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public String toString() {
        return "AttPeriodItemStat{" +
                "name='" + name + '\'' +
                ", attItems=" + attItems +
                ", attItemValue='" + attItemValue + '\'' +
                ", unit='" + unit + '\'' +
                ", unitName='" + unitName + '\'' +
                ", datatype='" + datatype + '\'' +
                '}';
    }
}