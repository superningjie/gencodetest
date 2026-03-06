package tdkw.hrmp.hrobs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.AppInfo;
import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.param.AppParam;
import kd.bos.exception.BosErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.ConfirmTypes;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.IFormView;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.MessageTypes;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.events.LoadCustomControlMetasArgs;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.form.Border;
import kd.bos.metadata.form.Margin;
import kd.bos.metadata.form.Padding;
import kd.bos.metadata.form.Style;
import kd.bos.metadata.form.container.FlexPanelAp;
import kd.bos.metadata.form.control.ImageAp;
import kd.bos.metadata.form.control.LabelAp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.portal.model.BrandUpEnum;
import kd.bos.portal.model.GoAppEnum;
import kd.bos.portal.util.CloudLadderSkipUtils;
import kd.bos.portal.util.OpenPageUtils;
import kd.bos.portal.util.PortalUsableFuncUtil;
import kd.bos.portal.util.YZJSpecialEditionUtils;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.hr.hbp.common.util.HRStringUtils;
import kd.hr.hrcs.bussiness.servicehelper.perm.RoleServiceHelper;
import kd.hrmp.hrobs.business.domain.service.portal.IAppConfigService;
import kd.hrmp.hrobs.formplugin.portal.pc.PcCommonAppPlugin;
import kd.hrmp.hrobs.formplugin.utils.HrobsPageUtil;
import kd.wtc.wtbs.business.mobile.MobileCommonServiceHelper;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.common.monitor.MonitorUtil;
import tdkw.hrmp.hrobs.formplugin.util.AutoOpenUtil;
import tdkw.hrmp.hrobs.common.app.hrmp.HRRoleAndPersonUtils;
import tdkw.hrmp.hrobs.formplugin.util.PopAppUtils;
//import tdkw.inte.inte.common.utils.GetTokenUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @Date 2023/7/14 11:46
 * @Description 常用应用 表单插件
 * @Demander xxx
 * @Document https://www.kdocs.cn/l/chxbmD4Vzaw0
 * @Basedata tdkw_hrobs_pc_appcard_ext
 * @Version 1.0
 **/
public class ExtendsPcCommonAppPlugin extends PcCommonAppPlugin {
    private Log logger = LogFactory.getLog(ExtendsPcCommonAppPlugin.class);
    private static final String KEY_LEVEA_APPID = "tdkw_myteam";
    private static final String KEY_LEAVE_APPLY = "tdkw_myteam_epsilon";
    private static final String tdkw_MYTEAM_POSITION = "tdkw_myteam_positionson";

    //领导视图-角色管理编码
    public String leadNumber = "XY_LEAD_PC_001";

    //领导视图-角色管理编码
    public String mangerNumber = "XY_TEAM_PC_001";

    // 我的团队-职能经理人
    public String teamBusinessManager = "XY_BUS_MGR_APP_001";

    @Override
    public void click(EventObject evt) {
        Control source = (Control) evt.getSource();
        String key = source.getKey();
        String[] splitStr = key.split("#");
        String formId = splitStr[1];
        String appSource = splitStr[2];
        if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_online", formId)) {
            // 在线学习
            this.openOnlineUrl();
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_performanceval", formId)) {
            // 绩效评估
            this.openPerformanceUrl();
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_statisticanalysis", formId)) {
            // 数据分析
            boolean hasPermission = HRRoleAndPersonUtils.getUserROleEntity(UserServiceHelper.getCurrentUserId(), "tdkw_appauthorityleader_apphome");
            if (hasPermission) {
                //this.gotoApp(GoAppEnum.OldPortalOpen, "3DHP9AXYYV5H", "数据分析");
                AutoOpenUtil.open(this.getView(),"tdkw_appauthorityleader","tdkw_appauthorityleader_apphome","tdkw_personanalysis","数据分析","人员分析");
            } else {
                this.getView().showMessage(ResManager.loadKDString("无数据分析的应用权限。", "ExtendsPcCommonAppPlugin_0", "tdkw-esc-hrobs-formplugin"));
            }
//            // PC运营监控日志记录
            MonitorUtil.save("tdkw_appauthorityleader", "数据分析", "1742823408351075328", "人员分析", "menu", "", "1");
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_leadersquery", formId)) {
            // 综合查询
            //this.gotoApp(GoAppEnum.OldPortalOpen, "3BZG8MQB75L=", "综合查询");
            AutoOpenUtil.openReport(this.getView(),"tdkw_leader_query","tdkw_leaderqury_apphome","tdkw_roster_report","综合查询","在职人员花名册");
//            // PC运营监控日志记录
            MonitorUtil.save("tdkw_leader_query", "综合查询", "1758084224700745728", "在职人员花名册", "menu", "", "1");
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_myteam_apphome", formId)) {
            // 我的团队
            //this.gotoApp(GoAppEnum.OldPortalOpen, "3FFIHFYFQM9X", "我的团队");
            long currentUserId = RequestContext.get().getCurrUserId();
            String userName = RequestContext.get().getUserName();
            // 查询是否是经理人权限
            boolean managerRole = HRRoleAndPersonUtils.getIsRole(currentUserId, "XY_TEAM_APP_001");
            // 查询是否是职能线权限
            boolean functionRole = HRRoleAndPersonUtils.getIsRole(currentUserId, "XY_BUS_MGR_APP_001");
            if (!managerRole && !functionRole) {
                return;
            }
            if (managerRole) {
                AutoOpenUtil.open(this.getView(), KEY_LEVEA_APPID, "tdkw_myteam_apphome", KEY_LEAVE_APPLY, "我的团队", "我的团队");
            } else {
                AutoOpenUtil.openReport(this.getView(), KEY_LEVEA_APPID, "tdkw_myteam_apphome", tdkw_MYTEAM_POSITION, "我的团队", "职能团队");
                //AutoOpenUtil.openReport(this.getView(), KEY_LEVEA_APPID, tdkw_MYTEAM_POSITION, "");
                //AutoOpenUtil.openReport(this.getView(), KEY_LEVEA_APPID, tdkw_MYTEAM_POSITION, tdkw_MYTEAM_POSITION, "职能团队", "我的团队");
            }
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_myinterview", formId)) {
            // 我的面试
            try {
                this.openMyInterview();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new KDBizException("打开我的面试失败");
            }
        } else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_annual_bill", formId)) {
            // 2024年1月17日16:10:47、年度账单
            this.getView().openUrl(openMyYearUrl());
        }

        //绩效档案
        else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("htm_mytodotask", formId)){
            openAppListForm(this.getView(),"tdkw_pself","tdkw_pself_apphome","tdkw_mybacklog","绩效工作台","");
        }

        //人才自助，
        else if (HRStringUtils.equals(appSource, "0") && StringUtils.equals("tdkw_mytask", formId)) {
            openAppListForm(this.getView(), "tdkw_work", "tdkw_work_apphome", "", "人才自助", "");
        }

        else {
            super.click(evt);
        }
    }

    /**
     * @author xxx
     * @description 弹出新应用，并默认打开新列表界面（该方法只能打开列表界面，打开的界面必须是在应用的菜单栏发布的，否则会报错）
     * view = 视图模型，app = 应用编码，home = 应用首页标识，entity = 单据标识，name = 应用名称，entityName = 单据名称
     * @date 2023/11/2
     */
    public static void openAppListForm(IFormView view, String app, String home, String entity, String name, String entityName) {
        //获取最外层的根页面（主页面）
        IFormView mainView = view.getMainView();
        //获取主页面的pageId
        String mainViewPageId = mainView.getPageId();
        //拼接应用的pageId，我们需要按默认格式指定pageId，因为有些场景会直接解析pageId
        String appPageId = app + mainViewPageId;
        //获取指定appid对应的app内码（即根据t_meta_bizapp表的fnumber获取fid）
        String appId = AppMetadataCache.getAppInfo(app).getAppId();
        HashMap<String, Object> hashMap;

        //判断当前应用是否已经被打开
        if (null != mainView.getViewNoPlugin(appPageId)) {
            //已经被打开则激活应用
            hashMap = new HashMap<>();
            hashMap.put("view", view);
            hashMap.put("appname", name);
            hashMap.put("formnumber", home);
            hashMap.put("parametertype", "FormShowParameter");
            PopAppUtils.activatePage(appPageId, view, hashMap);
        } else {
            //没有打开则代打开应用
            //创建弹出的应用对象并设置相关属性
            FormShowParameter showParameter2 = new FormShowParameter();
            //打开的表单页面标识，这里为应用首页
            showParameter2.setFormId(home);
            //打开的表单标题
            showParameter2.setCaption(name);
            //打开的应用appId
            showParameter2.setAppId(app);
            //设置customParam中的appid属性值（必须设置该属性才能出现菜单栏）
            showParameter2.setCustomParam("appid", appId);
            //打开风格，这里为新增页签的写法
            showParameter2.getOpenStyle().setShowType(ShowType.NewTabPage);
            //设置页签容器的key，这里为首页--应用页签容器的标识
            showParameter2.getOpenStyle().setTargetKey("tabap");
            //设置pageId（默认写法为appid+mainViewPageId）
            showParameter2.setPageId(appPageId);
            //获取根页面的页面模型数据
            mainView.showForm(showParameter2);
            view.sendFormAction(mainView);
        }
        // 如果要打开的单据为空，则直接调到应用首页
        if (StringUtils.isEmpty(entity)) {
            return;
        }
        //根据appid获取下属所有menu信息
        List<AppMenuInfo> appMenuInfos = AppMetadataCache.getAppMenusInfoByAppId(app);
        //遍历所有menu信息获取到formId为9z8e_gjt_hr_qjsqd并且是单据的menuId
        String menuId = appMenuInfos
                .stream()
                .filter(appMenuInfo -> entity.equals(appMenuInfo.getFormId()) && "ListShowParameter".equals(appMenuInfo.getParamType()))
                .findFirst()
                .get()
                .getId();
        //拼接单据的pageId，格式为menuId+主页面的pageId
        String billPageId = menuId + mainViewPageId;

        //判断当前单据有没有被打开
        if (null != mainView.getViewNoPlugin(billPageId)) {
            //如果已经被打开则激活该页面
            hashMap = new HashMap<>();
            hashMap.put("view", view);
            hashMap.put("appname", name);
            hashMap.put("formnumber", entity);
            hashMap.put("parametertype", "ListShowParameter");
            PopAppUtils.activatePage(billPageId, view, hashMap);
        }
        else {
            //如果没有打开则弹出该页面
            //创建弹出的单据对象并设置相关属性
            ListShowParameter showParameter = new ListShowParameter();
            //设置FormId，列表的FormId固定为"bos_list"
            showParameter.setFormId("bos_list");
            //打开的单据页面标识
            showParameter.setBillFormId(entity);
            //打开的单据标题
            showParameter.setCaption(entityName);
            //打开的应用appId
            showParameter.setAppId(app);
            //设置customParam中的appid属性值（必须设置该属性才能出现菜单栏）
            showParameter.setCustomParam("appid", appId);
            //打开风格，这里为新增页签的写法
            showParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
            //设置页签容器的key，这里为应用中的页签容器的标识
            showParameter.getOpenStyle().setTargetKey("_submaintab_");
            //设置单据的appid
            showParameter.setPageId(billPageId);
            //设置页面参数
            showParameter.setCustomParam("isMyBacklog", "true");
            //获取弹出应用首页的页面模型
            IFormView qjglView = view.getView(appPageId);
            qjglView.showForm(showParameter);
            view.sendFormAction(qjglView);
        }
    }


    /**
     * @author xxx
     * @Description 跳转应用
     * @Date 2023/7/19 17:03
     */
    private void gotoApp(GoAppEnum goAppEnum, String appId, String appName) {
        AppInfo app;
        try {
            app = AppMetadataCache.getAppInfo(appId);
        } catch (KDException var10) {
            this.getView().showErrorNotification(var10.getStackTraceMessage());
            return;
        }

        if (app == null) {
            this.getView().showErrorNotification(ResManager.loadKDString("应用【", "MyAppPlugin_11", "bos-portal-plugin", new Object[0]) + appId + ResManager.loadKDString("】运行期元数据不存在，请到【开发者门户】中确认该应用是否可见并已启用！", "MyAppPlugin_12", "bos-portal-plugin", new Object[0]));
        } else {
            String mainPageNumber;
            if ("0B+E5YAC2OJF".equals(app.getCloudId())) {
                mainPageNumber = YZJSpecialEditionUtils.isMissYzjParams();
                if (kd.bos.dataentity.utils.StringUtils.isNotBlank(mainPageNumber)) {
                    ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener("btn_config_add", this);
                    Map<Integer, String> btnNameMaps = new HashMap(2);
                    btnNameMaps.put(MessageBoxResult.Cancel.getValue(), ResManager.loadKDString("取消", "MyAppPlugin_13", "bos-portal-plugin", new Object[0]));
                    btnNameMaps.put(MessageBoxResult.Yes.getValue(), ResManager.loadKDString("去添加参数", "MyAppPlugin_14", "bos-portal-plugin", new Object[0]));
                    this.getView().showConfirm(mainPageNumber, "", MessageBoxOptions.OKCancel, ConfirmTypes.Save, confirmCallBacks, btnNameMaps);
                    return;
                }
            }

            String homeURL;
            if ("18Y35CVM2EDB".equals(app.getCloudId()) && "dmo".equals(app.getAppId())) {
                AppParam apm = new AppParam();
                apm.setAppId("198I/T67QJ+P");
                apm.setViewType("15");
                apm.setOrgId(100000L);
                apm.setActBookId(0L);
                homeURL = (String) SystemParamServiceHelper.loadAppParameterFromCache(apm, "dmoserver");
                if (kd.bos.dataentity.utils.StringUtils.isEmpty(homeURL)) {
                    ConfirmCallBackListener confirmCallBacks = new ConfirmCallBackListener("btn_goto_cts", this);
                    Map<Integer, String> btnNameMaps = new HashMap(2);
                    btnNameMaps.put(MessageBoxResult.Cancel.getValue(), ResManager.loadKDString("取消", "MyAppPlugin_13", "bos-portal-plugin", new Object[0]));
                    btnNameMaps.put(MessageBoxResult.Yes.getValue(), ResManager.loadKDString("去添加参数", "MyAppPlugin_14", "bos-portal-plugin", new Object[0]));
                    String missMsg = ResManager.loadKDString("请联系管理员配置数据开发平台服务器地址，配置路径为：系统服务云>>配置工具>>系统参数>>数据服务云>>数据开发平台", "MyAppPlugin_24", "bos-portal-plugin", new Object[0]);
                    this.getView().showConfirm(missMsg, "", MessageBoxOptions.OKCancel, ConfirmTypes.Save, confirmCallBacks, btnNameMaps);
                    return;
                }
            }

            if ("ladder".equals(appId)) {
                this.cloudLadderSkip();
            } else {
                mainPageNumber = app.getHomeNum();
                homeURL = app.getHomeURL();
                if (false && GoAppEnum.NewPortalNewWindowOpen.name().equals(goAppEnum.name()) && !"1".equals(app.getOpenType())) {
                    this.getView().openUrl("?formId=home_page&appNumber=" + app.getNumber());
                } else {
                    if (!false) {
                        List<String> appNumbers = BrandUpEnum.getAppNumbers(appId);
                        if (appNumbers != null) {
                            if (this.isHasPermApps(appNumbers, appName)) {
                                this.getView().openUrl("?appNumber=" + appId);
                            }

                            return;
                        }
                    }

                    if ("1".equals(app.getMainFormType())) {
                        if (kd.bos.dataentity.utils.StringUtils.isNotBlank(homeURL)) {
                            this.getView().openUrl(homeURL);
                        }
                    } else {
                        if (kd.bos.dataentity.utils.StringUtils.isBlank(mainPageNumber)) {
                            IFormView mainView = this.getView().getMainView();
                            mainView = mainView == null ? this.getView() : mainView;
                            mainView.showErrorNotification(ResManager.loadKDString("应用首页没有配置，请到【开发平台】配置后再试！", "MyAppPlugin_15", "bos-portal-plugin", new Object[0]));
                            this.getView().sendFormAction(mainView);
                            return;
                        }

                        try {
                            OpenPageUtils.openApp(appId, (String) null, (Map) null, this.getView(), appName);
                        } catch (KDException var11) {
                            if (!BosErrorCode.metaNotFound.equals(var11.getErrorCode())) {
                                throw var11;
                            }
                            this.getView().showErrorNotification(ResManager.loadKDString("应用配置的首页表单【", "MyAppPlugin_16", "bos-portal-plugin", new Object[0]) + mainPageNumber + ResManager.loadKDString("】已经不存在，请到【开发者门户】进行检查！", "MyAppPlugin_17", "bos-portal-plugin", new Object[0]));
                        }
                    }
                }
            }
        }
    }

    public boolean isHasPermApps(List<String> appNums, String appName) {
        PortalUsableFuncUtil portalUtil = new PortalUsableFuncUtil(this.logger, this.getView());
        Set<String> resultApps = portalUtil.getUserHasPerAppNumbers();
        if (resultApps != null && !resultApps.isEmpty()) {
            Iterator<String> iterator = appNums.iterator();

            while (iterator.hasNext()) {
                String item = (String) iterator.next();
                boolean isExist = resultApps.stream().anyMatch((a) -> {
                    return a.equals(item);
                });
                if (!isExist) {
                    iterator.remove();
                }
            }

            if (CollectionUtils.isEmpty(appNums)) {
                this.getView().showMessage(String.format(ResManager.loadKDString("无%1$s的应用权限", "OpenPageUtils_2", "bos-portal-plugin", new Object[0]), appName));
                return false;
            } else {
                return true;
            }
        } else {
            this.getView().showMessage(String.format(ResManager.loadKDString("无%1$s的应用权限", "OpenPageUtils_2", "bos-portal-plugin", new Object[0]), appName));
            return false;
        }
    }

    private void cloudLadderSkip() {
        Map<String, Object> cloudLadderResp = CloudLadderSkipUtils.cloudLadderAuth();
        if (Objects.isNull(cloudLadderResp)) {
            this.getView().showMessage(ResManager.loadKDString("运维服务返回参数为空，请联系管理员", "MyAppPlugin_21", "bos-portal-plugin", new Object[0]), ResManager.loadKDString("运维服务返回参数为空，请联系管理员", "MyAppPlugin_21", "bos-portal-plugin", new Object[0]), MessageTypes.Default);
        } else if (!"SUCCESS".equals(cloudLadderResp.get("msg"))) {
            Integer error = (Integer) cloudLadderResp.get("error");
            this.getView().showMessage(ResManager.loadKDString("运维服务错误原因：错误码为：", "MyAppPlugin_22", "bos-portal-plugin", new Object[0]) + error + ";" + cloudLadderResp.get("msg") + ResManager.loadKDString(",请联系管理员", "MyAppPlugin_23", "bos-portal-plugin", new Object[0]));
        } else {
            String phone = CloudLadderSkipUtils.getOPSPhone();
            StringBuilder sb = new StringBuilder(CloudLadderSkipUtils.getOPSUrl());
            sb.append("boss/open/redirect?token=");
            sb.append(cloudLadderResp.get("token"));
            sb.append("&uniqueId=");
            sb.append(phone);
            IClientViewProxy proxy = (IClientViewProxy) this.getView().getService(IClientViewProxy.class);
            Map<String, String> mpURL = new HashMap(2);
            mpURL.put("url", sb.toString());
            mpURL.put("openStyle", "1");
            proxy.addAction("openUrl", mpURL);
        }
    }

    /**
     * @author xxx
     * @Description 单点登录跳转外部链接-绩效评估
     * @Date 2023/7/17 10:53
     */
    public void openPerformanceUrl() {
        /*
            http://
            10.91.17.218
            /hrself/#/ssologinKingdee?sid=
            jiay.lin
            &token=
            59700730-d177-4353-888a-6bc005977c09
            &tokenUrl=/perf-todo
        */
        StringBuilder url = new StringBuilder();
        url.append("http://");
        String ip = System.getProperty("hr.ipconfig.ncip");
        url.append(ip);
        url.append("/hrself/#/ssologinKingdee?sid=");
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // TODO 异常 缺少tdkw_domainaccount 字段，临时屏蔽
        // 人员非时序性属性
//        DynamicObject hrpi_pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
//        // sid
//        String tdkw_domainaccount = hrpi_pernontsprop.getString("tdkw_domainaccount");
//        url.append(tdkw_domainaccount);
//        url.append("&token=");
//        String token = GetTokenUtil.getToken(tdkw_domainaccount);
//        url.append(token);
//        url.append("&tokenUrl=/perf-todo");

        url.append("&tokenUrl=/perf-todo");

        this.getView().openUrl(url.toString());
    }

    /**
     * @author xxx
     * @Description 单点登录跳转外部链接-在线学习
     * @Date 2023/7/14 10:28
     */
    public void openOnlineUrl() {
        /*
            http://
            10.91.35.93:8888
            /transfer/el/sso/kingdee/login?sid=
            jiay.lin
            &returnUrl=https://xycs.yunxuetang.cn/study/%23/userhome%20&token=
            test001
            &type=PC
        */
        /*
            http://
            10.91.16.183:8080
            /app/user/kingdee/login
            ?sid=
            jiay.lin
            &returnUrl=
            /app/course/courseTodo
            &token=
            test001
            &type=PC
         */
/*        StringBuilder url = new StringBuilder();
        url.append("https://");
        String ip = System.getProperty("hr.ipconfig.onlineip");
        String middle = System.getProperty("hr.ipconfig.onlinemiddle");
        String returnurl = System.getProperty("hr.ipconfig.onlinereturnurl");
        url.append(ip);
        url.append(middle);
        url.append("?sid=");
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject hrpi_pernontsprop = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // sid
        String tdkw_domainaccount = hrpi_pernontsprop.getString("tdkw_domainaccount");
        url.append(tdkw_domainaccount);
        url.append("&returnUrl=");
        url.append(returnurl);
        url.append("&token=");
        String token = GetTokenUtil.getToken(tdkw_domainaccount);
        url.append(token);
        url.append("&type=PC");*/

//        IClientViewProxy service = this.getView().getService(IClientViewProxy.class);
//        Map<String, Object> map = new HashMap<>();
//        map.put("url", url.toString());
//        map.put("openStyle", "0");
//        service.addAction("openUrl", map);
//        this.getView().openUrl(url.toString());
    }

    /**
     * 单点登录到北森我的面试页面
     */
    public void openMyInterview() throws UnsupportedEncodingException {
//        String ssoUrl = System.getProperty("hr.ipconfig.italentssourl");
//        String redirectUrl = System.getProperty("hr.ipconfig.italentredirecturl");
//        String encodedRedirectUrl = URLEncoder.encode(redirectUrl, "UTF-8");
//        String systemSource = "HR";
//        // TODO tdkw_domainaccount 不存在 临时屏蔽掉
////        String sid = getSid();
//        String sid = "";
//        String token = GetTokenUtil.getToken(sid);
//        // 生成完整的URL连接
//        StringBuilder urlBuilder = new StringBuilder(ssoUrl);
//        urlBuilder.append("?sid=").append(sid)
//                .append("&token=").append(token)
//                .append("&redirectUrl=").append(encodedRedirectUrl)
//                .append("&systemSource=").append(systemSource);
//        logger.info("我的面试url:" + urlBuilder.toString());
//        this.getView().openUrl(urlBuilder.toString());
    }

    /**
     * 生成年度账单单点链接
     *
     * @return
     */
    private String openMyYearUrl() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        String baseUrl = System.getProperty("oa.article");
        String myYearUrl = System.getProperty("oa.myYear", "http://elink.xxx.com/xxx/web/myyear" + year + "pc/");
        // TODO tdkw_domainaccount 不存在 临时屏蔽掉
//        String sid = getSid();
        String sid = "";
       /* String token = GetTokenUtil.getToken(sid);
        String completeUrl = baseUrl + "?sid=" + sid + "&token=" + token + "&url=" + myYearUrl;
        logger.info("myYearUrl=" + completeUrl);
        return completeUrl;*/
        return "";
    }

    /**
     * 获取域账号
     * @return
     */
    private String getSid() {
        QFilter filter = new QFilter("person", QCP.equals, MobileCommonServiceHelper.getInstance().getUserId());
        filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
        // 人员非时序性属性
        DynamicObject personProp = QueryServiceHelper.queryOne("hrpi_pernontsprop", "tdkw_domainaccount,person", filter.toArray());
        // sid
        return personProp.getString("tdkw_domainaccount");
    }

    @Override
    public void loadCustomControlMetas(LoadCustomControlMetasArgs args) {
        // super.loadCustomControlMetas(args);
        this.buildCardTitle(args);
        this.buildAllApp(args);
    }

    private void buildCardTitle(LoadCustomControlMetasArgs args) {
        FormShowParameter formShowParameter = (FormShowParameter)args.getSource();
        String cardIdStr = (String)formShowParameter.getOpenStyle().getCustParam().get("cardId");
        if (!StringUtils.isBlank(cardIdStr)) {
            Long cardId = Long.valueOf(cardIdStr);
            String title = IAppConfigService.getInstance().getCardTitle(cardId);
            if (!StringUtils.isEmpty(title)) {
                FlexPanelAp titleFlex = new FlexPanelAp();
                titleFlex.setKey("titleflexpanelap");
                titleFlex.setHeight(new LocaleString("32px"));
                LabelAp appFlex = this.getTitleFlex(title);
                titleFlex.getItems().add(appFlex);
                this.addFlexPanel("titleflexpanelap", titleFlex, args);
            }
        }
    }

    private LabelAp getTitleFlex(String title) {
        Style style = new Style();
        Margin margin = new Margin();
        margin.setTop("10px");
        margin.setLeft("20px");
        style.setMargin(margin);
        LabelAp titleLabelAp = HrobsPageUtil.customLabel("titlelabelap", new LocaleString(title), 14, "#212121");
        titleLabelAp.setStyle(style);
        titleLabelAp.setClickable(true);
        return titleLabelAp;
    }
    private void buildAllApp(LoadCustomControlMetasArgs args) {
        FormShowParameter formShowParameter = (FormShowParameter)args.getSource();
        String cardIdStr = (String)formShowParameter.getOpenStyle().getCustParam().get("cardId");
        String cardLayoutIdStr = (String)formShowParameter.getOpenStyle().getCustParam().get("cardLayoutId");
        if (!StringUtils.isBlank(cardIdStr) && !StringUtils.isBlank(cardLayoutIdStr)) {
            Long cardId = Long.valueOf(cardIdStr);
            Long cardLayoutId = Long.valueOf(cardLayoutIdStr);
            //logger.info("cardLayoutId:" + cardLayoutId);
            List<Map<String, Object>> appConfigList = IAppConfigService.getInstance().getCardLayoutApps("0", cardId, cardLayoutId);
            //logger.info("appConfigList:" + appConfigList);
            if (appConfigList != null && !appConfigList.isEmpty()) {
                FlexPanelAp appListFlex = new FlexPanelAp();
                appListFlex.setKey("contentflexpanelap");
                long userId = RequestContext.get().getCurrUserId();
                String appId = formShowParameter.getAppId();
                String view = "47150e89000000ac";
                for(int i = 0; i < appConfigList.size(); ++i) {
                    Map<String, Object> appConfig = (Map)appConfigList.get(i);
                    //logger.info("appConfig:" + appConfig);
                    if ("0".equals(appConfig.get("appsource"))) {
                        boolean checkPermission = PermissionServiceHelper.checkPermission(userId, appId, (String) appConfig.get("formId"), view);
                        //logger.info("判断是否有权限:" + appConfig.get("formId") + ":权限:" + checkPermission);
                        if (!checkPermission) {
                            //logger.info("判断是否有权限:" + appConfig.get("formId") + ":没权限");
                            continue;
                        }
                    }
                    //我的团队-经理人角色判断我的团队显示隐藏
                    if (StringUtils.equals((String) appConfig.get("formId"), "tdkw_myteam_apphome") && !isManager()) {
                        continue;
                    }
                    FlexPanelAp appFlex = this.getAppFlex(appConfig, i);
                    appListFlex.getItems().add(appFlex);
                }

                this.addFlexPanel("contentflexpanelap", appListFlex, args);
            }
        }
    }


    //判断是否有经理人角色
    public boolean isManager() {
        boolean result = false;
        // 获取用户ID
        Long userId = RequestContext.get().getCurrUserId();
        //获取角色管理的id
        Set<String> userRoleIds = PermissionServiceHelper.getRolesByUser(userId);
        List<String> userRoleIdsList = new ArrayList<>(userRoleIds);
        Map<String, Map<String, Object>> roleMembers = RoleServiceHelper.getRoleMembers(userRoleIdsList);
        //获取角色管理的编码
        List<String> roleNumber = roleMembers.values().stream().map(item -> (String) item.get("number")).collect(Collectors.toList());
        //logger.warn("roleNumber:" + JSONObject.toJSONString(roleNumber));
        if (roleNumber.contains(leadNumber) || roleNumber.contains(mangerNumber) || roleNumber.contains(teamBusinessManager)) {
            result = true;
        }
        return result;
    }



    private void addFlexPanel(String id, FlexPanelAp flexPanelAp, LoadCustomControlMetasArgs args) {
        Map<String, Object> params = new HashMap(16);
        params.put("id", id);
        params.put("items", flexPanelAp.createControl().get("items"));
        args.getItems().add(params);
    }

    /**
     * 原本  appFlex 96*74  改成76*54
     * margin 22*20 改成5*20
     * @param appConfigDy
     * @param index
     * @return
     */
    private FlexPanelAp getAppFlex(Map<String, Object> appConfigDy, int index) {
        Margin margin = new Margin();
        margin.setTop("5px");
        margin.setLeft("20px");
        String apKey = "appflex#" + appConfigDy.get("formId") + "#" + appConfigDy.get("appsource") + "#" + appConfigDy.get("modeltype") + "#" + appConfigDy.get("id") + "#" + appConfigDy.get("businessobjecttype");
        FlexPanelAp appFlex = HrobsPageUtil.customFlexPane(apKey, margin, (Padding)null, (Border)null, "76px", "54px");
        HrobsPageUtil.setFlexGrowAndShrink(appFlex, 0, 1);
        HrobsPageUtil.setFlexDirection(appFlex, "column", false, "center", "center");
        appFlex.setBackColor("#ffffff");
        appFlex.setClickable(true);
        ImageAp imageAp = this.buildImageAp((String)appConfigDy.get("systemicon"), index);
        LabelAp titleLabel = this.buildAppTitleLabel(new LocaleString(appConfigDy.get("name").toString()), index);
        titleLabel.setTextAlign("center");
        titleLabel.setWidth(new LocaleString("96px"));
        appFlex.getItems().add(imageAp);
        appFlex.getItems().add(titleLabel);
        return appFlex;
    }

    private ImageAp buildImageAp(String imageUrl, int index) {
        ImageAp imageAp = new ImageAp();
        imageAp.setImageKey(imageUrl);
        imageAp.setKey("imagekey" + index);
        imageAp.setHeight(new LocaleString("35px"));
        imageAp.setWidth(new LocaleString("35px"));
        return imageAp;
    }

    private LabelAp buildAppTitleLabel(LocaleString title, int index) {
        Style style = new Style();
        Margin margin = new Margin();
        margin.setTop("6px");
        style.setMargin(margin);
        LabelAp titleLabelAp = HrobsPageUtil.customLabel("labelname" + index, title, 12, "#666666");
        titleLabelAp.setStyle(style);
        return titleLabelAp;
    }


}
