package tdkw.esc.myteam.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.report.ReportShowParameter;
import tdkw.esc.leaderquery.common.hrmp.HRRoleAndPersonUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

/**
 * 我的团队主页插件
 */
public class MyTeamHomePageAutoOpenFormPlugin extends AbstractFormPlugin {

    private static final Log logger = LogFactory.getLog(MyTeamHomePageAutoOpenFormPlugin.class);

    private static final String TDKW_MYTEAM_APPID = "tdkw_myteam";
    /**
     * 我的团队首页
     */
    private static final String TDKW_MYTEAM_APPHOME = "tdkw_myteam_apphome";
    /**
     * 我的团队
     */
    private static final String TDKW_MYTEAM_ESPLION = "tdkw_myteam_epsilon";
    /**
     * 职能团队
     */
    private static final String TDKW_MYTEAM_POSITION = "tdkw_myteam_positionson";


    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        long currentUserId = RequestContext.get().getCurrUserId();
        String userName = RequestContext.get().getUserName();
        // 查询是否是经理人权限
        boolean managerRole = HRRoleAndPersonUtils.getIsRole(currentUserId, "XY_TEAM_APP_001");
        // 查询是否是职能线权限
        boolean functionRole = HRRoleAndPersonUtils.getIsRole(currentUserId, "XY_BUS_MGR_APP_001");
        logger.info(userName + ",managerRole:" + managerRole + ",functionRole:" + functionRole);
        if (!managerRole && !functionRole) {
            return;
        }
        if (managerRole) {
            open(this.getView(), TDKW_MYTEAM_APPID, TDKW_MYTEAM_APPHOME, TDKW_MYTEAM_ESPLION, "我的团队", "");
        } else {
            openReport(this.getView(), TDKW_MYTEAM_APPID, TDKW_MYTEAM_POSITION, "");
        }
    }

    public static void open(IFormView view, String app, String home, String entity, String name, String entityName) {
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
        //根据appid获取下属所有menu信息
        List<AppMenuInfo> appMenuInfos = AppMetadataCache.getAppMenusInfoByAppId(app);
        //遍历所有menu信息获取到formId为9z8e_gjt_hr_qjsqd并且是单据的menuId
        String menuId = appMenuInfos
                .stream()
                .filter(appMenuInfo -> entity.equals(appMenuInfo.getFormId()) && "FormShowParameter".equals(appMenuInfo.getParamType()))
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
            hashMap.put("parametertype", "FormShowParameter");
            PopAppUtils.activatePage(billPageId, view, hashMap);
        } else {
            //如果没有打开则弹出该页面
            //创建弹出的单据对象并设置相关属性
            FormShowParameter showParameter = new FormShowParameter();
            //打开的单据页面标识
            showParameter.setFormId(entity);
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
            //获取弹出应用首页的页面模型
            IFormView qjglView = view.getView(appPageId);
            qjglView.showForm(showParameter);
            view.sendFormAction(qjglView);
        }
    }

    public static void openReport(IFormView view, String app, String entity, String entityName) {
//        //获取最外层的根页面（主页面）
//        IFormView mainView = view.getMainView();
//        //获取主页面的pageId
//        String mainViewPageId = mainView.getPageId();
//        //拼接应用的pageId，我们需要按默认格式指定pageId，因为有些场景会直接解析pageId
//        String appPageId = app + mainViewPageId;
//        //获取指定appid对应的app内码（即根据t_meta_bizapp表的fnumber获取fid）
//        String appId = AppMetadataCache.getAppInfo(app).getAppId();
//        HashMap<String, Object> hashMap;

//        //没有打开则代打开应用
//        //创建弹出的应用对象并设置相关属性
//        FormShowParameter showParameter2 = new FormShowParameter();
//        //打开的表单页面标识，这里为应用首页
//        showParameter2.setFormId(home);
//        //打开的表单标题
//        showParameter2.setCaption(name);
//        //打开的应用appId
//        showParameter2.setAppId(app);
//        //设置customParam中的appid属性值（必须设置该属性才能出现菜单栏）
//        showParameter2.setCustomParam("appid", appId);
//        //打开风格，这里为新增页签的写法
//        showParameter2.getOpenStyle().setShowType(ShowType.NewTabPage);
//        //设置页签容器的key，这里为首页--应用页签容器的标识
//        showParameter2.getOpenStyle().setTargetKey("tabap");
//        //设置pageId（默认写法为appid+mainViewPageId）
//        showParameter2.setPageId(appPageId);
//        //获取根页面的页面模型数据
//        mainView.showForm(showParameter2);
//        view.sendFormAction(mainView);

        //如果没有打开则弹出该页面
        //创建弹出的单据对象并设置相关属性
        ReportShowParameter showParameter = new ReportShowParameter();
        //打开的单据页面标识
        showParameter.setFormId(entity);
        //打开的应用appId
        showParameter.setAppId(app);
        //打开风格，这里为新增页签的写法
        showParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
        //设置页签容器的key，这里为应用中的页签容器的标识
        showParameter.getOpenStyle().setTargetKey("_submaintab_");
        view.showForm(showParameter);
    }
}
