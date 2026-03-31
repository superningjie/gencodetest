package tdkw.hrmp.hrobs.common.myteam.common.excel;


import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-16:48
 * @description TODO
 */
public class CellStyleParam {
    /**
     * 水平对齐方式，默认居中
     */
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

    /**
     * 垂直对齐方式，默认居中
     */
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    /**
     * 填充类型
     */
    private FillPatternType fillPatternType = FillPatternType.SOLID_FOREGROUND;

    /**
     * 填充颜色
     */
//    private XSSFColor foregroundColor = new XSSFColor(Color.WHEAT);
    private short foregroundColor = IndexedColors.WHITE.getIndex();
    /**
     * 填充颜色
     */
//    private XSSFColor fontColor = new XSSFColor(new Color(31, 31, 31));
//    private XSSFColor fontColor = new XSSFColor();

    private short fontColor = IndexedColors.BLACK.getIndex();
    /**
     * 字体类型,默认微软雅黑
     */
    private String fontName = "微软雅黑";

    /**
     * 字体大小,默认11号
     */
    private short fontHeight = 11;

    /**
     * 是否加粗
     */
    private boolean bold = false;

    /**
     * 是否斜体
     */
    private boolean italic = false;

    /**
     * 构造函数
     */
    public CellStyleParam() {
    }

    /**
     * 构造函数
     */
    public CellStyleParam(short fontColor, short fontHeight, boolean bold) {
        this.fontColor = fontColor;
        this.fontHeight = fontHeight;
        this.bold = bold;
    }

    /**
     * 构造函数
     */
    public CellStyleParam(short foregroundColor, short fontColor, short fontHeight, boolean bold) {
        this.foregroundColor = foregroundColor;
        this.fontColor = fontColor;
        this.fontHeight = fontHeight;
        this.bold = bold;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public FillPatternType getFillPatternType() {
        return fillPatternType;
    }

    public void setFillPatternType(FillPatternType fillPatternType) {
        this.fillPatternType = fillPatternType;
    }

    public short getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(short foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public short getFontColor() {
        return fontColor;
    }

    public void setFontColor(short fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public short getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(short fontHeight) {
        this.fontHeight = fontHeight;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }
}

