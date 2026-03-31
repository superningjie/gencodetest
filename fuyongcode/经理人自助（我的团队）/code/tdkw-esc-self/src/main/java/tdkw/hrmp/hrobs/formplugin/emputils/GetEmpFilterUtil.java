package tdkw.hrmp.hrobs.formplugin.emputils;

import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.Date;
import java.util.List;

/**
 * GetEmpentrelFilterUtil
 *
 * @author xxx
 * @date 2023/6/30
 */
public class GetEmpFilterUtil {
    /**
     * 获取总入职入职的过滤条件
     * 职业信息基础页面  入职后 一条 更新后 还是一条 离职后  还是一条  再入职 是两条
     * 离职后维护 非在职职业信息
     *
     * @return
     */
    public static QFilter getTotalFilter(Date startDate, Date endDate, List<Long> filterPersonIds) {

        //流入时间 flowtime 在区间内
        QFilter qFilter = new QFilter("flowtime", QCP.large_equals, startDate);
        qFilter.and("flowtime", QCP.less_equals, endDate);
        qFilter.and("person.id", QCP.in, filterPersonIds);
        //变动类型-所属变动大类为 入职
        qFilter.and("tdkw_changetype.tdkw_chgevent.number", QCP.equals,"1010_S");
        return qFilter;
    }


    // 获取在职人数的过滤条件
    public static QFilter getOnJobFilter(Date startDate, Date endDate, List<Long> filterPersonIds) {
        //用工关系类型为用工进行中
        //首次用工时间 firststartdate 在区间内
        QFilter qFilter = new QFilter("firststartdate", QCP.large_equals, startDate);
        qFilter.and("firststartdate", QCP.less_equals, endDate);
        qFilter.and("labrelstatusprd.name", QCP.equals, "用工进行中");
        qFilter.and("person.id", QCP.in, filterPersonIds);
        return qFilter;
    }

    /**
     * 根据人员id获取唯一人事业务档案的过滤器
     */
    public static QFilter getErManFileByIds(List<Long> totalIdList) {
        //根据人员id查询唯一的人事业务档案的过滤器
        QFilter personFiler = new QFilter("person.id", QCP.in, totalIdList);
        // personFiler.and("filetype.number", QCP.equals, "1010_S");
        personFiler.and("businessstatus", QCP.equals, "1");
        personFiler.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // personFiler.and("filetype.postype.number", QCP.equals, "1010_S");
        personFiler.and("filetype.postype.number", QCP.equals, "XY00001");
        personFiler.and("empposrel.businessstatus", QCP.equals, "1");
        personFiler.and("empposrel.datastatus", QCP.equals, "1");
        personFiler.and("empposrel.iscurrentversion", QCP.equals, Boolean.TRUE);
        return personFiler;
    }

}
