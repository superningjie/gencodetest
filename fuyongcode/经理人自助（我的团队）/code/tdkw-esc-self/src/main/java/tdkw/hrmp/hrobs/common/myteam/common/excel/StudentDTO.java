package tdkw.hrmp.hrobs.common.myteam.common.excel;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/8/2-17:27
 * @description TODO
 */
@ExcelHeadProperty(text = "学生名单", lastCol = 2)
@ExcelRowProperty
public class StudentDTO {

    @ExcelProperty(index = 0, title = "学号")
    private String no;

    @ExcelProperty(index = 1, title = "姓名")
    private String name;

    @ExcelProperty(index = 2, title = "年龄")
    private String age;


    public StudentDTO(String no, String name, String age) {
        this.no = no;
        this.name = name;
        this.age = age;
    }
}
