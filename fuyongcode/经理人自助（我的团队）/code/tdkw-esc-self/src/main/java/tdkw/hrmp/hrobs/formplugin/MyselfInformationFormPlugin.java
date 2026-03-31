package tdkw.hrmp.hrobs.formplugin;


import kd.bos.context.RequestContext;
import kd.bos.dataentity.Tuple;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.form.*;
import kd.bos.form.control.Label;
import kd.bos.form.control.ProgressBar;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.sdk.hr.hspm.business.repository.ErmanFileRepository;
import kd.sdk.hr.hspm.business.service.AttacheHandlerService;
import kd.sdk.hr.hspm.common.enums.ClientTypeEnum;
import org.apache.commons.lang3.StringUtils;
//import tdkw.hrmp.hbss.common.PersonnelFileUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Location 员服门户-个人信息（hrobs_pc_empinfocard）
 * @Descripse 参考：kd.hr.hspm.formplugin.web.schedule.draw.head.HeadDrawPlugin（hspm_dynfilehead）、
 * kd.hr.hpfs.formplugin.file.ERManFileCommonListPlugin（hspm_erfilelist）
 * @Autor ZZL
 * @Createdate 2023-05-22
 */
public class MyselfInformationFormPlugin extends AbstractFormPlugin {
    private Map<String, DynamicObject> data;
    public GateWayUtils gateWayUtils = new GateWayUtils();
    private static final Map<String, String> VECTORAP_FIELD = new HashMap();
    private int vectorApRowIndex = 1;
    private static final ArrayList<String> MAIN_ERFILE_LIST = null;

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("tdkw_myfiles_l", "tdkw_myfiles_i", "tdkw_myfiles_f","tdkw_flexpanelap1","tdkw_flexpanelap7","tdkw_labelap","tdkw_schedule");
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        // 用户基本信息
        setValueBaseInfor();
        //或者主职位、部门、公司
        //  setValueDept();
        BigDecimal pro = BigDecimal.ZERO;
        Map<String, Long> personModelId = this.getPersonModelId();
        if (personModelId != null) {
            Long personId = personModelId.get("person");
            //员工信息
            DynamicObject person = BusinessDataServiceHelper.loadSingle(personId, "hrpi_person");
            //获取人员完整度 TODO 缺少 PersonnelFileUtil 工具类  先写死百分比，根据需要调整
//            Double progressNum = PersonnelFileUtil.personnelFile(person);
            Double progressNum = 1.00;
            progressNum = progressNum * 100;
            pro = BigDecimal.valueOf(progressNum).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        Label schedule = this.getControl("tdkw_schedule");
        schedule.setText(String.valueOf(pro) + "%");
        ProgressBar bar = this.getView().getControl("tdkw_inforprogress");
        IClientViewProxy proxy = this.getView().getService(IClientViewProxy.class);
        proxy.setFieldProperty(bar.getKey(), ClientProperties.Percent, pro);
        bar.start();
    }


    @Override
    public void click(EventObject evt) {
        super.click(evt);
        IFormView view = this.getView();
        String key = gateWayUtils.getClickKey(evt);
        //打开我的档案
        if (StringUtils.equals("tdkw_myfiles_i", key) || StringUtils.equals("tdkw_myfiles_l", key)
                || StringUtils.equals("tdkw_myfiles_f", key)  || StringUtils.equals("tdkw_flexpanelap1", key)
                || StringUtils.equals("tdkw_flexpanelap7", key)  || StringUtils.equals("tdkw_labelap", key)
                || StringUtils.equals("tdkw_schedule", key)) {
            Map<String, Long> personModelId = this.getPersonModelId();
            if (personModelId != null) {
                FormShowParameter formShowParameter = new FormShowParameter();
                long personId = personModelId.get("person");
                DynamicObject erFileDy = ErmanFileRepository.getPrimaryErmanFile(personId);
                Tuple tuple = AttacheHandlerService.getInstance().handleRuleEngine(this.getView(), erFileDy.getLong("id"),
                        erFileDy, "hspm" + ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), ClientTypeEnum.EMPLOYEE_MOBILE.getCode(), (Map) null, false);
                Long configId = Long.parseLong(tuple.item2.toString());
                IAppCache appCache = AppCache.get("hspm");
                Long currUserId = RequestContext.get().getCurrUserId();
                appCache.put(currUserId + "hspm_myermanfile" + "personId", personId);
                appCache.put(currUserId + "hspm_myermanfile" + "configId", configId);
                formShowParameter.setFormId("hspm_myermanfile");
                formShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
                getView().showForm(formShowParameter);
            }
        }
    }


    /**
     * 用户基本信息赋值
     */
    public void setValueBaseInfor() {
        long currUserId = RequestContext.get().getCurrUserId();
        String properties = "name,number,entryentity.position,entryentity.ispartjob,entryentity.dpt";
        DynamicObject user = BusinessDataServiceHelper.loadSingle(currUserId, "bos_user", properties);
        if (user != null) {
            Label number = this.getControl("tdkw_number");
            number.setText(user.getString("number"));
            Label name = this.getControl("tdkw_name");
            name.setText(user.getString("name"));
            //使用财务云下面的工具类获取头像地址
//            String applierPicUrl = ErCommonUtils.getUserImageFullPath(currUserId);
            //头像赋值
//            this.getModel().setValue("headsculpture", applierPicUrl);
//            this.getView().updateView("headsculpture");

            //获取主职位--根据人员信息bos_user
//            DynamicObjectCollection position_DOC = user.getDynamicObjectCollection("entryentity");
//            for (DynamicObject item : position_DOC) {
//                Boolean ispartjob = (Boolean) item.get("ispartjob");
//                if (!ispartjob) {
//                    //主职位赋值
//                    String position = item.getLocaleString("position").getLocaleValue_zh_CN();
//                    Label job = this.getControl("job");
//                    job.setText(position);
//                    //部门赋值
//                    DynamicObject dpt = item.getDynamicObject("dpt");
//                    String dptName = dpt.getLocaleString("name").getLocaleValue_zh_CN();
//                    Label adminorg = this.getControl("adminorg");
//                    adminorg.setText(dptName);
//                    //公司赋值
//                    List<Long> userIds = new ArrayList<>(1);
//                    userIds.add(currUserId);
//                    Map<Long, Long> companyMap = UserServiceHelper.getCompanyByUserIds(userIds);
//                    DynamicObject companyDo = BusinessDataServiceHelper.loadSingle(companyMap.get(currUserId), "bos_org", "name");
//                    String companyName = companyDo.getLocaleString("name").getLocaleValue_zh_CN();
//                    Label company = this.getControl("company");
//                    company.setText(companyName);
//                }
//            }
        }
    }


    private Map<String, Long> getPersonModelId() {
        Map<String, Object> personModelIdMap = (Map) HRMServiceHelper.invokeHRMPService("hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
        Map<String, Long> personModelInfo = personModelIdMap != null ? (Map) personModelIdMap.get("data") : null;
        return personModelInfo;
    }
}
