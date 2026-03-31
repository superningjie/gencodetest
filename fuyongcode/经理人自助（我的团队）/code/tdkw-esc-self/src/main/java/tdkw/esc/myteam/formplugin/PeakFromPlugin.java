package tdkw.esc.myteam.formplugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.IFormView;
import kd.bos.form.IPageCache;
import kd.bos.form.ShowType;
import kd.bos.form.events.CustomEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.report.ReportShowParameter;
import tdkw.hrmp.hrobs.common.myteam.common.PersonSubordinateInfoUtils;
import tdkw.hrmp.hrobs.common.myteam.common.SubordinatePersonnelVo;

import java.io.IOException;
import java.util.EventObject;

/**
 * @author kangy
 * @version 1.0
 * @date 2023/7/27-17:20
 * @description TODO
 */
public class PeakFromPlugin extends AbstractFormPlugin {
    private static final Log logger = LogFactory.getLog(PeakFromPlugin.class);


    private final static String EPSILON = "tdkw_epsilon";
    private final static String EVENT_CUSTOM = "customevent";

    @Override
    public void customEvent(CustomEventArgs e) {
        logger.info("customEvent开始执行");
        logger.info(e.getKey() + "/" + e.getVarMap() + "/" + e.getEventArgs() + "/" + e.getEventName());
        String eventName = e.getEventName();
        logger.info("eventName:" + eventName);
        CustomControl control = this.getView().getControl(EPSILON);
        String eventArgs = e.getEventArgs();
        //将字符串转成json
        JSONObject param = JSONObject.parseObject(eventArgs);

        //返回协作关系下属
        if ("collaborative".equals(eventName) || "position".equals(eventName)) {
            String positionId = String.valueOf(param.get("positionid"));
            String collaborativeId = String.valueOf(param.get("collaborativeId"));
            if (collaborativeId == null || "null".equals(collaborativeId)) {
                collaborativeId = "1010";
            }
            if (positionId == null) {
                positionId = "0";
            }
            logger.info("岗位id:" + positionId);
            logger.info("线路id:" + collaborativeId);
            JSONObject personnelSubordinates = PersonSubordinateInfoUtils.getPersonnelSubordinates(Long.valueOf(positionId), Long.valueOf(collaborativeId));
            SubordinatePersonnelVo data = (SubordinatePersonnelVo) personnelSubordinates.get("data");
            logger.info("下属信息1:" + data);
            IPageCache pageCache = this.getView().getPageCache();
            pageCache.put("data", String.valueOf(personnelSubordinates));
            control.setData(personnelSubordinates);
        }
        //返回直接下属
        if ("initdata".equals(eventName)) {

            String positionId = String.valueOf(param.get("positionid"));
            String collaborativeId = String.valueOf(param.get("collaborativeId"));
            if (positionId == null) {
                positionId = "0";
            }
            if (collaborativeId == null || "null".equals(collaborativeId)) {
                collaborativeId = "1010";
            }
            logger.info("岗位2id:" + positionId);
            logger.info("线路2id:" + collaborativeId);
            JSONObject personnelSubordinates = PersonSubordinateInfoUtils.getPersonnelSubordinates(Long.valueOf(positionId), Long.valueOf(collaborativeId));

            Object data = personnelSubordinates.get("data");
            logger.info("下属信息2:" + data);
            IPageCache pageCache = this.getView().getPageCache();
            pageCache.put("data", String.valueOf(personnelSubordinates));
            control.setData(personnelSubordinates);
        }

        //查看人员档案
        if ("showPersonFile".equals(eventName)) {
            IFormView view = this.getView();
            String positionId = String.valueOf(param.get("positionid"));
            String personId = String.valueOf(param.get("personid"));
            if (StringUtils.isEmpty(positionId)) {
                positionId = "0";
            }
            if (StringUtils.isEmpty(personId)) {
                personId = "0";
            }
            PersonSubordinateInfoUtils.viewPersonFile(Long.valueOf(positionId), Long.valueOf(personId), view);
        }
        //查看下属列表
        if ("showSubordinate".equals(eventName)) {
            String positionId = String.valueOf(param.get("positionid"));
            String personId = String.valueOf(param.get("personid"));
            String collaborativeId = String.valueOf(param.get("collaborativeId"));
            if (StringUtils.isEmpty(collaborativeId) || "null".equals(collaborativeId)) {
                collaborativeId = "0";
            }
            if (StringUtils.isEmpty(positionId) || "null".equals(positionId)) {
                positionId = "0";
            }
            if (StringUtils.isEmpty(personId) || "null".equals(personId)) {
                personId = "0";
            }
            ReportShowParameter billShowParameter = new ReportShowParameter();
            billShowParameter.setFormId("tdkw_myteam_personinfo");
            billShowParameter.setCustomParam("positionId", Long.parseLong(positionId));
            billShowParameter.setCustomParam("personId", Long.parseLong(personId));
            billShowParameter.setCustomParam("collaborativeId", Long.parseLong(collaborativeId));
            billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
            //设置为查看
            this.getView().showForm(billShowParameter);
        }

        //导出Excel
        if ("export".equals(eventName)) {
            IPageCache pageCache = this.getView().getPageCache();
            String data = pageCache.get("data");
            try {
                JSONObject parse = (JSONObject) JSONObject.parse(data);
                Gson gson = new Gson();
                SubordinatePersonnelVo myTeamBriefInfoBean = gson.fromJson(String.valueOf(parse.get("data")), SubordinatePersonnelVo.class);
//                SubordinatePersonnelVo subordinatePersonnelVo = (SubordinatePersonnelVo) parse.get("data");
                PersonSubordinateInfoUtils.buildExcel(this.getView(), myTeamBriefInfoBean);
            } catch (IOException ex) {
                logger.info(ex.getMessage());
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                logger.info(ex.getMessage());
                throw new RuntimeException(ex);
            }

        }

    }

    @Override
    public void afterBindData(EventObject e) {
        logger.info("afterBindData开始执行");
        //tdkw_epsilon
        CustomControl customcontrol = this.getView().getControl(EPSILON);
        //岗位
        JSONArray positionJson = PersonSubordinateInfoUtils.getPositionSet();
        //协作关系
        JSONArray collaborativeJson = PersonSubordinateInfoUtils.queryAllCollaborativeType();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("positionData", positionJson);
        jsonObject.put("collaborativeData", collaborativeJson);

        jsonObject.put("status", 200);
        jsonObject.put("message", "初始化成功");
        // 通过setData给自定义控件传输数据，前端就能通过props.data获取数据
        customcontrol.setData(jsonObject);

    }


}
