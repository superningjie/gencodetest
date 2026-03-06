package tdkw.hrmp.hrobs.formplugin;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.AppInfo;
import kd.bos.entity.AppMenuInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.portal.pluginnew.CardUtils;
import kd.bos.portal.util.OpenPageUtils;
import kd.bos.portal.util.PortalMessageUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import tdkw.hrmp.hrobs.formplugin.util.PopAppUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Metadata： home_page
 * @Description ： 登录打开员服-首页
 * @ClassName ：MyHomeFormPlugin
 * @author xxx
 * @Date ：2023/6/2 17:46
 * @Version: 1.0
 */
public class MyHomeFormPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(MyHomeFormPlugin.class);

    public void afterCreateNewData(EventObject e) {
        logger.info("HR自助门户-开始");
        //如果工作流打开，不进入HR自助门户
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        logger.info("customParams：" + customParams);
        logger.info("customParams：" + (ObjectUtils.isNotEmpty(customParams)) + "，jumpFormId：" + (ObjectUtils.isNotEmpty(customParams.get("jumpFormId"))) + "，tdkw_pself_apphome：" + ("tdkw_pself_apphome".equals(customParams.get("jumpFormId"))));
        //绩效工作台跳转
        if (ObjectUtils.isNotEmpty(customParams) && ObjectUtils.isNotEmpty(customParams.get("jumpFormId")) && "tdkw_pself_apphome".equals(customParams.get("jumpFormId"))) {
            //初始化时打开我的待办列表
            openAppListForm(this.getView(), "tdkw_pself", "tdkw_pself_apphome", "绩效工作台");
        } else if (ObjectUtils.isNotEmpty(customParams) && "tdkw_cvsalarypchange".equals(customParams.get("jumpFormId"))) {
            // 跳转简历薪酬
            openAppListForm(this.getView(), "hspm", "hspm_apphome", "人员信息");
        }
        //HR自助服务中心跳转
        else {
            if (customParams.containsKey("appNumber")) {
                String appNumber = (String) customParams.get("appNumber");
                if (StringUtils.equals("wf", appNumber)) {
                    return;
                }
            }
            logger.info("HR自助门户-首页打开准备");
            IPageCache pageCache = this.getView().getPageCache();
            String countStr = pageCache.get(OpenPageUtils.HOMEPAGE_TABAP_COUNT);
            int count = countStr == null ? 3 : Integer.parseInt(countStr);
            //基于性能考虑，除去2个固定显示的页签，平台限制最多只能打开10个应用
            if (count > 12) {
                return;
            }
//        //todo 打开应用之前，需要校验用户是否有应用权限
//        PortalUsableFuncUtil portalUtil = new PortalUsableFuncUtil(logger, getView());
//        // 获取用户有权限的应用
//        Set<String> resultApps = portalUtil.getUserHasPerAppNumbers();
            //应用为HR自助服务中心
            String appNum = "hssc";
            Map<String, Object> formShowCustomMap = new HashMap<>();
            formShowCustomMap.put("view", this.getView());
            AppInfo app = AppMetadataCache.getAppInfo(appNum);
            formShowCustomMap.put("appname", app.getName().getLocaleValue());
            formShowCustomMap.put("appmainnumber", app.getHomeNum());
            //不让光标直接选中应用，需要再使用
            //formShowCustomMap.put("noSwitchFocus", "true");
            // 打开应用
            // 打开应用
            this.getView().sendFormAction(this.getView());

            //判断权限，去除提示语
//        boolean isHavePer = checkAppPermission(appNum, null, formShowCustomMap, this.getView(), "");
//        if (!isHavePer) {
//            return;
//        }
            logger.info("HR自助门户-首页打开-1");
            OpenPageUtils.openApp(appNum, null, formShowCustomMap, this.getView());
            logger.info("HR自助门户-首页打开-2");
        }
    }

    /**
     * @author xxx
     * @description 弹出新应用，并默认打开新列表界面（该方法只能打开列表界面，打开的界面必须是在应用的菜单栏发布的，否则会报错）
     * view = 视图模型，app = 应用编码，home = 应用首页标识，entity = 单据标识，name = 应用名称，entityName = 单据名称
     * @date 2024/3/12
     */
    public static void openAppListForm(IFormView view, String app, String home, String name) {
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
            logger.info("tdkw_pself send - 0");
            //已经被打开则激活应用
            hashMap = new HashMap<>();
            hashMap.put("view", view);
            hashMap.put("appname", name);
            hashMap.put("formnumber", home);
            hashMap.put("parametertype", "FormShowParameter");
            PopAppUtils.activatePage(appPageId, view, hashMap);
        } else {
            logger.info("tdkw_pself send");
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
    }


    /**
     * 判断权限，解决bug-#72777 员工自助门户与个人卡片-PC，无HR自助服务中心权限人员，需不提示无权限弹窗
     * http://ones.xxx.com/project/#/team/JbjqrWit/task/PkQto5oDcnUsp1Lw
     * 参考标准：kd.bos.portal.util.OpenPageUtils#openApp
     *
     * @param appId
     * @param menuId
     * @param map
     * @param myappView
     * @param selfAppName
     * @return
     */
    private static boolean checkAppPermission(String appId, String menuId, Map<String, Object> map, IFormView myappView, String selfAppName) {
        AppInfo app = AppMetadataCache.getAppInfo(appId);
        if (app != null && myappView != null) {
            String showMessage = (String) myappView.getFormShowParameter().getCustomParam("showMessage");
            String appMainNumber = app.getHomeNum();
            IFormView mainPageView = getMainPageView(myappView);
            if (mainPageView == null) {
                logger.error("OpenPageUtils----openApp----mainPageID or mainPageView is null");
            } else {
                boolean isMainPage = isMainPage(myappView);
                if (!isMainPage) {
                    myappView.showTipNotification(ResManager.loadKDString("只可预览应用菜单信息", "BizAppHomePlugin_5", "bos-portal-plugin", new Object[0]));
                } else {
                    String mainPageId = mainPageView.getPageId();
                    if (!"false".equals(showMessage)) {
                        PortalMessageUtils.showActivityMessageAsync(mainPageId, appId);
                    }

                    Long userId = Long.valueOf(RequestContext.get().getUserId());
                    boolean isSuperAdmin = PermissionServiceHelper.isAdminUser(userId, "10");
                    logger.info("openApp isSuperAdmin:" + isSuperAdmin);
                    String appName = "";
                    if (kd.bos.dataentity.utils.StringUtils.isNotBlank(selfAppName)) {
                        appName = selfAppName;
                    } else {
                        appName = app.getName().getLocaleValue();
                    }

                    if (kd.bos.dataentity.utils.StringUtils.isBlank(appName)) {
                        appName = app.getNumber();
                    }

                    if (map == null) {
                        map = new HashMap();
                        ((Map) map).put("view", myappView);
                        ((Map) map).put("appname", appName);
                        ((Map) map).put("appmainnumber", appMainNumber);
                    }

                    ((Map) map).put("appImageUrl", app.getImage());
                    if (!checkAppMenuPermission(appId, menuId, (Map) map, isSuperAdmin)) {
                        if (kd.bos.dataentity.utils.StringUtils.isEmpty(menuId)) {
                            if (((Map) map).containsKey("openUserFixedApp")) {
                                DeleteServiceHelper.delete("bos_portal_userfixedapp", new QFilter[]{new QFilter("user", "=", userId), new QFilter("bizapp", "=", appId)});
                            } else {
                                return false;
                                //   myappView.showMessage(String.format(ResManager.loadKDString("无%1$s的应用权限。", "OpenPageUtils_2", "bos-portal-plugin", new Object[0]), appName));
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static IFormView getMainPageView(IFormView myappView) {
        IFormView mainPageView = myappView.getMainView();
        if (mainPageView == null) {
            logger.info("OpenPageUtils----openApp----mainView is null");
            String rootPageId = myappView.getFormShowParameter().getRootPageId();
            mainPageView = myappView.getView(rootPageId);
            if (mainPageView == null) {
                logger.info("OpenPageUtils----openApp----getViewByRootPageId is null");
                throw new KDException(new ErrorCode("mainView is null", "mainView is null"), new Object[0]);
            }
        }

        return mainPageView;
    }

    private static boolean isMainPage(IFormView view) {
        String rootPageId = view.getFormShowParameter().getRootPageId();
        IFormView mainView = view.getView(rootPageId);
        String formId = mainView.getFormShowParameter().getFormId();
        String mainPageFormId = CardUtils.getMainViewFormId(view);
        return mainPageFormId.equals(formId);
    }

    private static boolean checkAppMenuPermission(String appId, String menuId, Map<String, Object> map, boolean isSuperAdmin) {
        logger.info("checkAppMenuPermission begin..., menuId:" + menuId);
        AppInfo app = AppMetadataCache.getAppInfo(appId);
        if (app == null) {
            return false;
        } else {
            String formNumber = (String) map.get("formnumber");
            boolean isAllUserApp = app.isAllUserApp();
            if (isAllUserApp) {
                if (appId.equals("wftask")) {
                    return true;
                }

                if (isSuperAdmin && !isAllowAdminBizOperate()) {
                    return false;
                }

                Set<String> appBlackSet = PermissionServiceHelper.getAppBlackSet(RequestContext.get().getCurrUserId());
                if (appBlackSet.contains(app.getId())) {
                    return false;
                }

                Map<String, List<String>> checkPermAllUserAppEntMap = PermissionServiceHelper.getCheckPermAllUserAppEntMap();
                List<String> entNums = (List) checkPermAllUserAppEntMap.get(app.getId());
                if (entNums == null || !entNums.contains(formNumber)) {
                    return true;
                }
            }

            long userId = RequestContext.get().getCurrUserId();
            boolean isPermission = PermissionServiceHelper.checkUserBizApp(userId, app.getId());
            if (!isPermission) {
                return isPermission;
            } else {
                if (kd.bos.dataentity.utils.StringUtils.isNotBlank(menuId)) {
                    boolean isNotSpecialMenu = true;
                    AppMenuInfo menu = null;

                    try {
                        menu = AppMetadataCache.getAppMenuInfo(appId, menuId);
                    } catch (Exception var18) {
                        logger.error("checkAppMenuPermission", var18);
                    }

                    IFormView view = (IFormView) map.get("view");
                    if (menu != null && view != null) {
                        Short menuSeq = menu.getSeq();
                        if (menuSeq == -1) {
                            isNotSpecialMenu = false;
                        }

                        if (isNotSpecialMenu) {
                            if (formNumber != null) {
                                String parameterType = "";
                                if (map.get("parametertype") != null) {
                                    parameterType = (String) map.get("parametertype");
                                }

                                String permItem = map.get("permItem") == null ? null : map.get("permItem").toString();
                                boolean isOnlyCheckView = false;
                                String formId = view.getFormShowParameter().getFormId();
                                if (formId.equals("bos_card_numstatisticcard") || formId.equals("bos_card_sumstatisticcard")) {
                                    isOnlyCheckView = true;
                                }

                                if (isOnlyCheckView) {
                                    isPermission = PermissionServiceHelper.hasViewPermission(userId, app.getId(), formNumber);
                                } else if (kd.bos.dataentity.utils.StringUtils.isBlank(permItem)) {
                                    if (!"ListShowParameter".equalsIgnoreCase(parameterType) && !"ReportShowParameter".equalsIgnoreCase(parameterType) && !"FormShowParameter".equalsIgnoreCase(parameterType)) {
                                        if (!"BillShowParameter".equalsIgnoreCase(parameterType) && !"BaseShowParameter".equalsIgnoreCase(parameterType)) {
                                            isPermission = PermissionServiceHelper.hasViewPermission(userId, app.getId(), formNumber);
                                        } else {
                                            isPermission = PermissionServiceHelper.hasNewPermission(userId, app.getId(), formNumber);
                                        }
                                    } else {
                                        isPermission = PermissionServiceHelper.hasViewPermission(userId, app.getId(), formNumber);
                                    }
                                } else {
                                    isPermission = PermissionServiceHelper.hasSpecificPerm(userId, app.getId(), formNumber, permItem);
                                }
                            } else {
                                view.showTipNotification(ResManager.loadKDString("当前菜单未绑定表单，请在【开发平台】重新配置。", "OpenPageUtils_6", "bos-portal-plugin", new Object[0]));
                                isPermission = false;
                            }
                        }
                    }
                }

                logger.info("checkAppMenuPermission end..., isPermission:" + isPermission);
                return isPermission;
            }
        }
    }

    public static boolean isAllowAdminBizOperate() {
        boolean result = false;
        DynamicObject enableScheme = BusinessDataServiceHelper.loadSingle("perm_adminscheme", "isallowbizoperate", new QFilter[]{new QFilter("enable", "=", true)});
        if (enableScheme != null) {
            result = enableScheme.getBoolean("isallowbizoperate");
        }

        return result;
    }
}
