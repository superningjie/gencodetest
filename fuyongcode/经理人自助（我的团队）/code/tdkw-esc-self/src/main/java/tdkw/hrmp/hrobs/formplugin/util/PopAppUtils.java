package tdkw.hrmp.hrobs.formplugin.util;

import kd.bos.context.RequestContext;
import kd.bos.form.IFormView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PopAppUtils {

    public static void activatePage(String pageID, IFormView view, Map<String, Object> map) {
        String menuId = pageID.split("root")[0];
        IFormView childView = view.getViewNoPlugin(pageID);
        if ("bos".equals(childView.getFormShowParameter().getServiceAppId())) {
            childView = view.getView(pageID);
        }

        if (childView != null) {
            if (!view.getPageId().equalsIgnoreCase(childView.getPageId())) {
                Map<String, Object> customparameters = (HashMap) map.get("customparameters");
                childView.getFormShowParameter().getCustomParams().remove("messageId");
                childView.getFormShowParameter().getCustomParams().remove("tabType");
                childView.getFormShowParameter().getCustomParams().remove("openPage");
                if (customparameters != null) {
                    if (customparameters.get("messageId") != null) {
                        childView.getFormShowParameter().setCustomParam("messageId", customparameters.get("messageId"));
                    }

                    if (customparameters.get("tabType") != null) {
                        childView.getFormShowParameter().setCustomParam("tabType", customparameters.get("tabType"));
                    }

                    if (customparameters.get("openPage") != null) {
                        childView.getFormShowParameter().setCustomParam("openPage", customparameters.get("openPage"));
                    }
                }

                childView.getFormShowParameter().setHasRight(true);
                childView.activate();
                if ("bos".equals(childView.getFormShowParameter().getServiceAppId())) {
                    childView.updateView();
                }

                view.sendFormAction(childView);
                String userId = RequestContext.get().getUserId();
                String time = (new SimpleDateFormat("yyy-MM-dd hh:mm:ss")).format(new Date(System.currentTimeMillis()));
            }
        } else {
            view.showTipNotification("系统已超时，请重新刷新登录!");
        }
    }
}