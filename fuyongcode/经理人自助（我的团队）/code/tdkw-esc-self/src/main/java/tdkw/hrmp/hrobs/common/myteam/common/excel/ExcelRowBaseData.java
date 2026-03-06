package tdkw.hrmp.hrobs.common.myteam.common.excel;

import java.io.Serializable;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-16:47
 * @description TODO
 */
public class ExcelRowBaseData<T> implements Serializable {

    private static final long serialVersionUID = 8649711415082247228L;

    /**
     * 行号
     */
    private Integer rowNum;

    /**
     * 数据状态
     */
    private boolean success = true;

    /**
     * 失败原因
     */
    private String failMsg;

    /**
     * 行数据
     */
    private T rowData;


    public ExcelRowBaseData(T rowData) {
        this.rowData = rowData;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg;
    }

    public T getRowData() {
        return rowData;
    }

    public void setRowData(T rowData) {
        this.rowData = rowData;
    }
}
