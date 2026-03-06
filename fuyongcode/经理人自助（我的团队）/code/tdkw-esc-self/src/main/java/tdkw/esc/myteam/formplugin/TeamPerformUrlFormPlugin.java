package tdkw.esc.myteam.formplugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.util.EventObject;

/**
 * @Author wyj
 * @Date 2023/8/4 16:23
 * @Description 团队绩效 表单插件
 * @Demander 腾云
 * @Document http://ones.xiangyu.com/wiki/#/team/JbjqrWit/share/TuWQbyFG/page/3zSP1AYo
 * @Basedata tdkw_teamperform
 * @Version 1.0
 **/
public class TeamPerformUrlFormPlugin extends AbstractFormPlugin {
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.openTeamPerformanceUrl();
        this.getView().close();
    }

    /**
      * @Author wyj
      * @Description 单点登录跳转外部链接-团队绩效
      * @Date 2023/8/4 16:51
      */
    public void openTeamPerformanceUrl() {
        /*
            http://
            10.91.17.218
            /hrself/#/ssologinKingdee?sid=
            jiay.lin
            &token=
            59700730-d177-4353-888a-6bc005977c09
            &tokenUrl=/my-team
        */
        /*StringBuilder url = new StringBuilder();
        url.append("http://");
        String ip = System.getProperty("hr.ipconfig.ncip");
        url.append(ip);
        url.append("/hrself/#/ssologinKingdee?sid=");
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject hrpi_pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // sid
        String tdkw_domainaccount = hrpi_pernontsprop.getString("tdkw_domainaccount");
        url.append(tdkw_domainaccount);
        url.append("&token=");
        String token = GetTokenUtil.getToken(tdkw_domainaccount);
        url.append(token);
        url.append("&tokenUrl=/my-team");*/

        this.getView().openUrl("http://www.baidu.com");
    }
}