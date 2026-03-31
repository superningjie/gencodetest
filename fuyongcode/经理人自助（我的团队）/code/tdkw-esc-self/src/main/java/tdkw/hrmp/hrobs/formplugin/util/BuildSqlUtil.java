package tdkw.hrmp.hrobs.formplugin.util;

import java.util.List;

/**
 * @author xxx
 * @date 2024年01月31日 09:34
 * @version: 1.0
 */
public class BuildSqlUtil {

    /**
     * 获取in查询sql语句，不带单引号的
     * @param condition
     * @param pkList
     * @param quotes 为true 则参数带上引号，false 不带单引号
     * @return
     */
    public static String getInSql(String condition, List pkList, boolean quotes) {
        if (pkList == null || pkList.size() == 0) {
            if(quotes){
                return condition + " (' ') ";
            } else {
                return " 1 = 2 ";
            }
        }
        if (pkList.size()<=1000) {
            return condition + " ("+appendInValues(pkList, quotes)+") ";
        }

        int count = pkList.size() / 1000;
//        System.out.println(count);
        String sql = " (";
        for (int i = 0; i <= count; i++) {
            int start = i * 1000;
            int end = (i + 1) * 1000;
            if (i == count) {
                end = pkList.size();
            }
            List subList = pkList.subList(start, end);
            if(i > 0){
                sql += " or ";
            }
            sql += condition + " ("+appendInValues(subList, quotes)+") ";
        }
        sql += ") ";
        return sql;
    }

    /**
     * 拼接in查询sql
     *
     * @param pkList
     * @param quotes
     * @return
     */
    private static String appendInValues(List pkList, boolean quotes){
        StringBuffer sql = new StringBuffer();
        for (int i = 0; i < pkList.size(); i++) {
            if(quotes){
                sql.append("'").append(pkList.get(i)).append("'");
            } else {
                sql.append(pkList.get(i));
            }
            if(i < pkList.size() - 1){
                sql.append(",");
            }
        }
        return sql.toString();
    }
}
