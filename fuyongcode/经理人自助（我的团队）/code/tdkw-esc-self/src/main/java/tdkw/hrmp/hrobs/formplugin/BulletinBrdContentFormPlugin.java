package tdkw.hrmp.hrobs.formplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.control.Control;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import tdkw.hrmp.hrobs.common.hrobs.util.HttpUtils;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.util.Base64;
import java.util.EventObject;
import java.util.HashMap;

/**
 * @author xxx
 * @date： 2023/7/18
 * @description :设置公告栏内容
 */
public class BulletinBrdContentFormPlugin extends AbstractFormPlugin {
    private static final Log LOGGER = LogFactory.getLog(BulletinBrdContentFormPlugin.class);
    /**
     * 超链接标识
     */
    private static final String SCHOOL_CONTENT_LINK = "tdkw_content0";
    /**
     * 标签标识
     */
    private static final String SCHOOL_CONTENT_LAB = "tdkw_lab0";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addClickListeners("tdkw_content00", "tdkw_content01", "tdkw_content02",
                "tdkw_content03", "tdkw_content04", "tdkw_content05");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        String typeNumber = this.getView().getFormShowParameter().getCustomParam("typeNumber");
     // TODO 异常临时屏蔽处理
      /*  JSONArray dataArray = getResponse();
        //设置公告内容
        if ("XY_003".equals(typeNumber) && !dataArray.isEmpty()) {
            setRecruitAnnouncement(dataArray);
            LOGGER.info("设置最新招聘公告");
        } else {
            setAnnouncement(typeNumber);
            LOGGER.info("从数据库获取公告");
        }*/
        setAnnouncement(typeNumber);
        LOGGER.info("从数据库获取公告");
    }

    @Override
    public void click(EventObject evt) {
        String typeNumber = this.getView().getFormShowParameter().getCustomParam("typeNumber");
        String recruitNumber = "XY_003";
        super.click(evt);
        JSONArray dataArray = getResponse();
        String key = ((Control) evt.getSource()).getKey();

        switch (key) {
            case SCHOOL_CONTENT_LINK + "0":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(0, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(0);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "1":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(1, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(1);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "2":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(2, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(2);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "3":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(3, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(3);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "4":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(4, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(4);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "5":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(5, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(5);
                    LOGGER.info("打开数据库公告");
                }
                break;
            case SCHOOL_CONTENT_LINK + "6":
                if (StringUtils.equals(typeNumber, recruitNumber) && !dataArray.isEmpty()) {
                    openRecruitArticle(6, dataArray);
                    LOGGER.info("打开最新招聘公告");
                } else {
                    openArticle(6);
                    LOGGER.info("打开数据库公告");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置公告内容
     *
     * @param number 公告类型编码
     */
    private void setAnnouncement(String number) {
        QFilter filter = new QFilter("tdkw_type.number", QCP.equals, number);
        //查找公告标题基础资料

        DynamicObject[] announcements = BusinessDataServiceHelper.load("tdkw_announcement_title",
                "tdkw_title,name,tdkw_url", new QFilter[]{filter}, "createtime desc", 6);
        for (int i = 0; i < announcements.length; i++) {
            //公告标题
            setLabName(SCHOOL_CONTENT_LAB + i, announcements[i].getString("tdkw_title"));
            //公告内容
            setLabName(SCHOOL_CONTENT_LINK + i, announcements[i].getString("name"));
            this.getView().setVisible(true, SCHOOL_CONTENT_LINK + i + "_wrapper");
        }
    }

    private void setRecruitAnnouncement(JSONArray dataArray) {
        int size = Math.min(dataArray.size(), 6);
        for (int i = 0; i < size; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);

            String glbdef1 = dataObject.getString("glbdef1");
            String orgFullName = dataObject.getString("orgFullName");
            if (StringUtils.isNoneBlank(orgFullName)) {
                orgFullName = orgFullName + "-";
            }
            String positionName = dataObject.getString("positionName");
            String workCity = dataObject.getString("workCity");

            if (StringUtils.equals(glbdef1, "@0@")) {
                glbdef1 = "内招";
            } else if (StringUtils.equals(glbdef1, "@1@")) {
                glbdef1 = "社招";
            } else if (StringUtils.equals(glbdef1, "@2@")) {
                glbdef1 = "校招";
            } else if (StringUtils.equals(glbdef1, "@3@")) {
                glbdef1 = "实习生";
            } else if (StringUtils.equals(glbdef1, "@4@")) {
                glbdef1 = "兼职招聘";
            }


            setLabName(SCHOOL_CONTENT_LAB + i, "招聘信息");
            //公告内容
            setLabName(SCHOOL_CONTENT_LINK + i, "【" + glbdef1 + "】" + orgFullName + positionName + "-" + workCity);
            this.getView().setVisible(true, SCHOOL_CONTENT_LINK + i + "_wrapper");
        }

    }

    private JSONArray getResponse() {
        JSONArray dataArray;
        // 创建JSON对象作为请求体
        String adAccount = this.getSid();
        JSONObject requestBody = getJsonObject(adAccount);
        // 创建一个空的哈希表来存储请求头信息
        HashMap<String, String> header = new HashMap<>();
        // 将编码后的基本认证信息添加到请求头中的Authorization字段
        String username = System.getProperty("tdkw.esb.username");
        LOGGER.info("username:" + username);
        String password = System.getProperty("tdkw.esb.password");
        LOGGER.info("password:" + password);
        header.put("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes()));
        String domain = System.getProperty("oa.newContent");
        LOGGER.info("domain:" + domain);
        String url = "/SAAST/RECRUIT/ProxyServices/getPositionListPS";
        // 发送POST请求并获取响应的请求体
        try {
            String responseBody = HttpUtils.sendHttpPost(domain + url, header, requestBody);
            JSONObject response = JSONObject.parseObject(responseBody);
            dataArray = response.getJSONArray("data");
        }  catch (Exception e){
            LOGGER.error("获取最新招聘公告失败", e);
            dataArray = new JSONArray();
        }
        return dataArray;
    }

    @NotNull
    private JSONObject getJsonObject(String adAccount) {
        JSONObject requestBody = new JSONObject();
        JSONObject in0 = new JSONObject();
        in0.put("pageTotal", "1");
        in0.put("docType", "1");
        in0.put("pageNo", "1");
        in0.put("property", "");
        in0.put("docCode", "");
        in0.put("source", "HR");
        in0.put("target", "EL");
        requestBody.put("in0", in0);
        requestBody.put("adAccount", adAccount);
        requestBody.put("recruitType", "0");
        requestBody.put("rootOrgId", "");
        return requestBody;
    }

    private void openArticle(int index) {
       /* String number = this.getView().getFormShowParameter().getCustomParam("typeNumber");
        QFilter filter = new QFilter("tdkw_type.number", QCP.equals, number);
        //查找公告标题基础资料
        DynamicObject[] announcements = BusinessDataServiceHelper.load("tdkw_announcement_title",
                "tdkw_title,name,tdkw_url", new QFilter[]{filter}, "createtime desc", 6);
        String url = announcements[index].getString("tdkw_url");
        this.getView().openUrl(getUrl(url));*/
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://salesdemo.kingdee.com/ierp/?formId=home_page");
        this.getView().openUrl(urlBuilder.toString());
    }

    private void openRecruitArticle(int index, JSONArray dataArray) {
//        JSONObject dataObject = dataArray.getJSONObject(index);
//        String sid = getSid();
//        String saasT = System.getProperty("oa.articleSaast");
//        StringBuilder urlBuilder = new StringBuilder();
//        urlBuilder.append("https://")
//                .append(saasT)
//                .append(":8808/rec/pc/?systemSource=HR&skipPage=PositionDetail&recPostId=")
//                .append(dataObject.getInteger("rsmsPositionId"))
//                .append("&sid=")
//                .append(sid)
//                .append("&token=")
//                .append(GetTokenUtil.getToken(sid));
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://salesdemo.kingdee.com/ierp/?formId=home_page");
        this.getView().openUrl(urlBuilder.toString());
    }

    /**
     * 设置控件名称
     *
     * @param property 控件标识
     * @param name     控件名称
     */
    private void setLabName(String property, String name) {
        Label label = this.getControl(property);
        label.setText(name);
    }

    private String getUrl(String url) {
//        String property = System.getProperty("oa.article");
//        String sid = getSid();
//        String token = GetTokenUtil.getToken(sid);
//        String subUrl = getSubUrl(url);
//        String completeUrl = property + "?sid=" + sid + "&token=" + token + "&url=" + subUrl;
//        LOGGER.info("completeUrl=" + completeUrl);
//        return completeUrl;
        return "";
    }

    private String getSubUrl(String url) {
        int index = url.indexOf("url=");
        if (index == -1) {
            return url;
        } else {
            return url.substring(index + 4);
        }
    }

    private String getSid() {
//        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
//        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
//        // 人员非时序性属性
//        DynamicObject pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
//        if (pernontsprop == null) {
//            LOGGER.error("人员非时序性属性不存在当前人员");
//            return null;
//        }
//        return pernontsprop.getString("tdkw_domainaccount");
        return "";
    }
}
