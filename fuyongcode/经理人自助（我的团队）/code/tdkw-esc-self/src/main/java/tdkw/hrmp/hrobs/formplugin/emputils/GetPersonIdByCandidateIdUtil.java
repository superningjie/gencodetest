package tdkw.hrmp.hrobs.formplugin.emputils;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * getPersonIdByCandidateIdUtil
 *
 * @author xxx
 * @date 2023/6/29
 */
public class GetPersonIdByCandidateIdUtil {
    public static List<Long> getPersonIdByCandidateId(Date startDay, Date endDay) {
        ArrayList<Long> personIdList = new ArrayList<>();
        QFilter qFilter = new QFilter("synchstatus", QCP.equals, "30");
        qFilter.and("checkinstatus", QCP.equals, "3");
        // effectdate  预计入职日期
        if (startDay != null) {
            qFilter.and("effectdate", QCP.large_equals, startDay);
        }
        if (endDay != null) {
            qFilter.and("effectdate", QCP.less_equals, endDay);
        }
        //查找当月入职人的候选人
        HRBaseServiceHelper serviceHelper = new HRBaseServiceHelper("hom_onbrdinfo");
        DynamicObject[] load = serviceHelper.query("candidate", qFilter.toArray());
        ArrayList<Long> candidateList = new ArrayList<>();
        for (DynamicObject object : load) {
            long candidateId = object.getLong("candidate.id");
            candidateList.add(candidateId);
        }
        //查找企业人 通过企业人找到人员
        if (candidateList.size() > 0) {
            QFilter filter = new QFilter("candidate.id", QCP.in, candidateList);
            filter.and("iscurrentversion", QCP.equals, "1");
            HRBaseServiceHelper baseServiceHelper = new HRBaseServiceHelper("hrpi_employee");
            DynamicObject[] employee = baseServiceHelper.query("person", filter.toArray());

            for (DynamicObject object : employee) {
                long personId = object.getLong("person.id");
                personIdList.add(personId);
            }


        }
        return personIdList;
    }
}


