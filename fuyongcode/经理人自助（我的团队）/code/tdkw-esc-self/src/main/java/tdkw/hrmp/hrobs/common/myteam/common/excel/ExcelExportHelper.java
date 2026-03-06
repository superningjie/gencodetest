package tdkw.hrmp.hrobs.common.myteam.common.excel;


import com.alibaba.dubbo.common.utils.CollectionUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;


import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-17:00
 * @description TODO
 */
public class ExcelExportHelper {


    /**
     * 创建工作簿并写入数据
     *
     * @param os                输出流
     * @param xssfWorkbook      工作簿
     * @param sheetName         sheet名称
     * @param dataList          需要导出的数据集
     * @param dataClass         数据类型
     * @param headCellStyle     头部标题单元格的样式
     * @param titleCellStyle    数据标题行单元格样式
     * @param dataCellStyleList 数据列样式集，多个样式可循环遍历使用实现斑马纹
     * @param writeFailMsg      是否写入错误信息列
     * @param <T>               数据内容泛型对象
     */
    public static <T> void write(OutputStream os, SXSSFWorkbook xssfWorkbook, String sheetName, List<ExcelRowBaseData<T>> dataList, Class<T> dataClass, CellStyle headCellStyle, CellStyle titleCellStyle, List<CellStyle> dataCellStyleList, boolean writeFailMsg) throws IllegalAccessException, IOException {
        //创建工作表sheet
        SXSSFSheet sheet = xssfWorkbook.createSheet(sheetName);
        //写入头部标题
        createHeadCell(sheet, headCellStyle, dataClass);
        //写入行标题
        writeTitleRow(sheet, dataClass, sheet.getLastRowNum() + 1, headCellStyle, writeFailMsg);
        //写入数据行
        writeDataRow(sheet, dataList, sheet.getLastRowNum() + 1, dataCellStyleList, writeFailMsg);
        //设置边框
        setBorder(sheet);
        //写入输出流
        try {
            xssfWorkbook.write(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建头部标题单元格
     *
     * @param xssfSheet     sheet页
     * @param headCellStyle 头部标题单元格的样式
     * @param dataClass     数据类型
     * @param <T>
     */
    private static <T> void createHeadCell(SXSSFSheet xssfSheet, CellStyle headCellStyle, Class<T> dataClass) {
        //不需要创建
        if (dataClass == null) {
            return;
        }
        //获取头部配置信息注解
        ExcelHeadProperty headProperty = dataClass.getAnnotation(ExcelHeadProperty.class);
        //如果存在注解信息，根据注解进行头部标题创建
        if (headProperty != null) {
            //循环创建单元格
            for (int i = headProperty.firstRow(); i <= headProperty.lastRow(); i++) {
                SXSSFRow row = xssfSheet.createRow(i);
                row.setHeight(headProperty.height());
                for (int j = headProperty.firstCol(); j <= headProperty.lastCol(); j++) {
                    SXSSFCell cell = row.createCell(j);
                    cell.setCellStyle(headCellStyle);
                    if (i == headProperty.firstRow() && j == headProperty.firstCol()) {
                        cell.setCellValue(headProperty.text());
                    }
                }
            }
            //合并单元格
            CellRangeAddress region = new CellRangeAddress(headProperty.firstRow(), headProperty.lastRow(), headProperty.firstCol(), headProperty.lastCol());
            xssfSheet.addMergedRegion(region);
        }
    }

    /**
     * 写入标题行
     *
     * @param xssfSheet      sheet
     * @param dataClass      数据类型
     * @param startRowIndex  起始行号
     * @param titleCellStyle 单元格样式
     * @param writeFailMsg   是否写入错误信息列
     * @param <T>            数据内容泛型对象
     */
    private static <T> void writeTitleRow(SXSSFSheet xssfSheet, Class<T> dataClass, int startRowIndex, CellStyle titleCellStyle, boolean writeFailMsg) {
        //没有数据或者入参为空，直接返回
        if (dataClass == null || xssfSheet == null) {
            return;
        }
        //创建标题行
        SXSSFRow titleRow = xssfSheet.createRow(startRowIndex);
        //设置行高
        ExcelRowProperty rowProperty = dataClass.getAnnotation(ExcelRowProperty.class);
        if (rowProperty != null) {
            titleRow.setHeight(rowProperty.titleHeight());
        }
        //获取声明的属性及其注解信息，并创建标题行
        Field[] fields = dataClass.getDeclaredFields();
        for (Field field : fields) {
            ExcelProperty property = field.getAnnotation(ExcelProperty.class);
            if (property != null && property.index() >= 0) {
                //创建单元格
                SXSSFCell cell = titleRow.createCell(property.index());
                cell.setCellValue(property.title());
                cell.setCellStyle(titleCellStyle);
                xssfSheet.setColumnWidth(property.index(), property.minWidth() * 256);
            }
        }
        //错误原因列
        if (writeFailMsg) {
            SXSSFCell cell = titleRow.createCell(titleRow.getLastCellNum());
            cell.setCellValue("错误原因");
        }
    }


    /**
     * 写入数据行
     *
     * @param xssfSheet         工作簿sheet页
     * @param dataList          需要导出的数据集
     * @param startRowIndex     起始行号
     * @param dataCellStyleList 数据列样式集，多个样式可循环遍历使用实现斑马纹
     * @param writeFailMsg      是否写入错误信息列
     * @param <T>               数据内容泛型对象
     */
    private static <T> void writeDataRow(SXSSFSheet xssfSheet, List<ExcelRowBaseData<T>> dataList, int startRowIndex, List<CellStyle> dataCellStyleList, boolean writeFailMsg) throws IllegalAccessException {
        //数据为空，直接返回
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        //根据行号进行排序
        //dataList.sort((o1, o2) -> o1.getRowNum() == null || o2.getRowNum() == null ? 0 : o1.getRowNum() - o2.getRowNum());
        //获取声明的属性及其注解信息，并创建标题行
        Field[] fields = dataList.get(0).getRowData().getClass().getDeclaredFields();
        ExcelRowProperty rowProperty = dataList.get(0).getRowData().getClass().getAnnotation(ExcelRowProperty.class);
        //样式数量，循环交替实现斑马纹
        int styleSize = CollectionUtils.isEmpty(dataCellStyleList) ? 0 : dataCellStyleList.size();
        //初始化列宽
        Map<Integer, Integer> colWidthMap = new HashMap<>();
        if (rowProperty != null && rowProperty.autoFitCol()) {
            for (Field field : fields) {
                ExcelProperty property = field.getAnnotation(ExcelProperty.class);
                if (property != null) {
                    int length = property.title().getBytes(Charset.forName("GBK")).length;
                    colWidthMap.put(property.index(), length > property.minWidth() ? length : property.minWidth());
                }
            }
        }
        //设置最大行数
        xssfSheet.setRandomAccessWindowSize(dataList.size()+5);
        //写入数据
        for (ExcelRowBaseData<T> data : dataList) {
            //创建行,并设置行高
            SXSSFRow row = xssfSheet.createRow(startRowIndex++);
            if (rowProperty != null) {
                row.setHeight(rowProperty.height());
            }
            //写入cell值
            for (Field field : fields) {
                ExcelProperty property = field.getAnnotation(ExcelProperty.class);
                if (property != null && property.index() >= 0) {
                    //创建单元格
                    SXSSFCell cell = row.createCell(property.index());
                    //写入cell值
                    field.setAccessible(true);
                    cell.setCellValue((String) field.get(data.getRowData()));
                    //设置样式
                    if (styleSize > 0) {
                        cell.setCellStyle(dataCellStyleList.get(startRowIndex % styleSize));
                    }
                    //列宽自适应
                    if (rowProperty != null && rowProperty.autoFitCol()) {
                        setCellWidth(colWidthMap, property.index(), cell.getStringCellValue());
                    }
                }
            }
            //错误原因列
            if (writeFailMsg) {
                SXSSFCell cell = row.createCell(row.getLastCellNum());
                cell.setCellValue(data.getFailMsg());
                setCellWidth(colWidthMap, cell.getColumnIndex(), cell.getStringCellValue());
            }
        }
        //设置宽度
        Iterator<Map.Entry<Integer, Integer>> iterator = colWidthMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> next = iterator.next();
//            xssfSheet.getColumnHelper().setColBestFit(next.getKey(), true);
            xssfSheet.setColumnWidth(next.getKey(), next.getValue() * 256);
        }
    }

    /**
     * 边框设置
     *
     * @param sheet 工作簿sheet页
     */
    private static void setBorder(SXSSFSheet sheet) {
        //获取最大行号
        int rowNum = sheet.getLastRowNum();
        Color color = new Color(161, 161, 161);
        //线性颜色
//        XSSFColor lineColor = new XSSFColor(new Color(161, 161, 161));
//        XSSFColor lineColor = new XSSFColor();
        short black = IndexedColors.BLACK.getIndex();
        //循环设置cell的线框
        for (int i = 0; i <= rowNum; i++) {
            int cellNum = sheet.getRow(i).getPhysicalNumberOfCells();
            for (int j = 0; j <= cellNum; j++) {
                SXSSFCell cell = sheet.getRow(i).getCell(j);
                if (cell != null) {
                    CellStyle style = cell.getCellStyle();
                    style.setBorderBottom(BorderStyle.THIN);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderLeft(BorderStyle.THIN);
                    style.setBorderRight(BorderStyle.THIN);
                    style.setBottomBorderColor(black);
                    style.setTopBorderColor(black);
                    style.setLeftBorderColor(black);
                    style.setRightBorderColor(black);
                    cell.setCellStyle(style);
                }
            }
        }
    }


    /**
     * 初始化列宽
     *
     * @param colWidthMap 列宽信息Map
     * @param colIndex    列小标
     * @param cellValue   单元格值
     */
    private static void setCellWidth(Map<Integer, Integer> colWidthMap, int colIndex, String cellValue) {
        int length = cellValue.trim().getBytes(Charset.forName("GBK")).length;
        Integer width = colWidthMap.get(colIndex);
        if (width == null || width < length) {
            colWidthMap.put(colIndex, length);
        }
    }

}
