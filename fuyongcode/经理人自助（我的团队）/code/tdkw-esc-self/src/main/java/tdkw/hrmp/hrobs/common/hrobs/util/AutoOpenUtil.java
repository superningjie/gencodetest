package tdkw.hrmp.hrobs.common.hrobs.util;

import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.report.ReportShowParameter;

import java.util.HashMap;
import java.util.List;

public class AutoOpenUtil {

    private static final Log logger = LogFactory.getLog(AutoOpenUtil.class);

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

    /*public static void openReport(IFormView view, String app, String home, String entity, String name, String entityName) {
        //获取最外层的根页面（主页面）
        IFormView mainView = view.getMainView();
        //获取主页面的pageId
        String mainViewPageId = mainView.getPageId();
        //拼接应用的pageId，我们需要按默认格式指定pageId，因为有些场景会直接解析pageId
        String appPageId = app + mainViewPageId;
        //获取指定appid对应的app内码（即根据t_meta_bizapp表的fnumber获取fid）
        String appId = AppMetadataCache.getAppInfo(app).getAppId();
        HashMap<String, Object> hashMap;

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
        //showParameter2.setPageId(appPageId);
        //获取根页面的页面模型数据
        mainView.showForm(showParameter2);
        view.sendFormAction(mainView);

        //根据appid获取下属所有menu信息
        List<AppMenuInfo> appMenuInfos = AppMetadataCache.getAppMenusInfoByAppId(app);
        //遍历所有menu信息获取到formId为9z8e_gjt_hr_qjsqd并且是单据的menuId
        String menuId = appMenuInfos
                .stream()
                .filter(appMenuInfo -> entity.equals(appMenuInfo.getFormId()) && "ReportShowParameter".equals(appMenuInfo.getParamType()))
                .findFirst()
                .get()
                .getId();
        //拼接单据的pageId，格式为menuId+主页面的pageId
        String billPageId = menuId + mainViewPageId;

        //创建弹出的单据对象并设置相关属性
        ReportShowParameter showParameter = new ReportShowParameter();
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
        //showParameter.setPageId(billPageId);
        //获取弹出应用首页的页面模型
        IFormView qjglView = view.getView(appPageId);

        qjglView.showForm(showParameter);
        view.sendFormAction(qjglView);

    }*/
    public static void openReport(IFormView view, String app, String home, String entity, String name, String entityName) {
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

            //如果没有打开则弹出该页面
            //创建弹出的单据对象并设置相关属性
            ReportShowParameter showParameter = new ReportShowParameter();
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
            //showParameter.setPageId(billPageId);
            //获取弹出应用首页的页面模型
            IFormView qjglView = view.getView(appPageId);

            logger.info("AutoOpenUtil.openReport appPageId = " + appPageId + ", qjglView=" + qjglView);
            if (qjglView != null) {
                qjglView.showForm(showParameter);
                view.sendFormAction(qjglView);
            } else {
                view.getMainView().getPageCache().put("autoopen", "1");
            }
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
