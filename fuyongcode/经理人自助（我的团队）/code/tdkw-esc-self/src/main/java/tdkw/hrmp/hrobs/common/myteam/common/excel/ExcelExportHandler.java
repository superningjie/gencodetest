package tdkw.hrmp.hrobs.common.myteam.common.excel;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-17:06
 * @description TODO
 */
public class ExcelExportHandler<E> {

    /**
     * 头部单元格样式
     */
//    private CellStyleParam headStyleParam = new CellStyleParam(new XSSFColor(Color.BLACK), (short) 14, true);
    private CellStyleParam headStyleParam = new CellStyleParam(IndexedColors.BLACK.getIndex(), (short) 14, true);
    /**
     * 标题列样式
     */
//    private CellStyleParam titleStyleParam = new CellStyleParam(new XSSFColor(new Color(236, 238, 242)), new XSSFColor(new Color(31, 31, 31)), (short) 11, true);
    private CellStyleParam titleStyleParam = new CellStyleParam(IndexedColors.BLACK.getIndex(),IndexedColors.BLACK.getIndex(), (short) 11, true);

    /**
     * 数据列样式
     */
    private List<CellStyleParam> cellStyleParamList = Arrays.asList(new CellStyleParam());

    /**
     * 是否写入错误信息
     */
    private boolean writeFailMsg = false;


    /**
     * 创建工作簿并写入数据
     *
     * @param os        输出流
     * @param sheetName sheet名称
     * @param dataList  需要导出的数据集
     * @param dataClass 数据类型
     */
    public void write(OutputStream os, String sheetName, List dataList, Class<E> dataClass) throws IOException, IllegalAccessException {

        //创建工作簿
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        //头部样式
        CellStyle headCellStyle = createCellStyle(workbook, headStyleParam);
        //行标题样式
        CellStyle titleCellStyle = createCellStyle(workbook, titleStyleParam);
        //数据行样式
        List<CellStyle> cellStyleList = new ArrayList<>();
        List<String> aa = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(cellStyleList)) {
            for (CellStyleParam styleParam : cellStyleParamList) {
                cellStyleList.add(createCellStyle(workbook, styleParam));
            }
        }
        //调用助手类，执行写入操作
        ExcelExportHelper.write(os, workbook, sheetName, dataList, dataClass, headCellStyle, titleCellStyle, cellStyleList, writeFailMsg);
    }


    /**
     * 创建样式
     *
     * @param workbook   工作簿
     * @param styleParam 样式参数
     * @return
     */
    private static CellStyle createCellStyle(Workbook workbook, CellStyleParam styleParam) {
        CellStyle style = workbook.createCellStyle();
        if (styleParam != null) {
            //设置水平垂直对齐方式
            style.setAlignment(styleParam.getHorizontalAlignment());
            style.setVerticalAlignment(styleParam.getVerticalAlignment());
            //设置背景颜色
            style.setFillPattern(styleParam.getFillPatternType());
            style.setFillForegroundColor(styleParam.getForegroundColor());
            //设置字体
            Font font = workbook.createFont();
            font.setColor(styleParam.getFontColor());
            font.setFontName(styleParam.getFontName());
            font.setFontHeightInPoints(styleParam.getFontHeight());
            font.setItalic(styleParam.isItalic());
            font.setBold(styleParam.isBold());
            style.setFont(font);
        }
        return style;
    }


    public void setHeadStyleParam(CellStyleParam headStyleParam) {
        this.headStyleParam = headStyleParam;
    }

    public void setTitleStyleParam(CellStyleParam titleStyleParam) {
        this.titleStyleParam = titleStyleParam;
    }

    public void setCellStyleParamList(List<CellStyleParam> cellStyleParamList) {
        this.cellStyleParamList = cellStyleParamList;
    }

    public void setWriteFailMsg(boolean writeFailMsg) {
        this.writeFailMsg = writeFailMsg;
    }
}
