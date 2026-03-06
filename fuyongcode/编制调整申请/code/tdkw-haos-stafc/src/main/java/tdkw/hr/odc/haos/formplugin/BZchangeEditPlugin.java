package tdkw.hr.odc.haos.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IFormView;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cd.common.form.handler.AutoCloseViewHandler;
import tdkw.hr.odc.haos.common.BzSelect;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName : 编制变更申请
 * @Description :
 * @Author : XYP
 * @Date: 2024-07-22 11:02
 */
public class BZchangeEditPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener, AfterF7SelectListener {

    private static final Log LOGGER = LogFactory.getLog(BZchangeEditPlugin.class);

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit tdkw_joblevelhr = this.getView().getControl("tdkw_staff");
        tdkw_joblevelhr.addBeforeF7SelectListener(this);
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        Map<String, Object> map = new HashMap<>();
//        map.put("teci", "tdkw_teci");
        this.getView().updateControlMetadata("bentryentity", map);
        if (this.getModel().getValue("tdkw_staff") != null) {
            //获取当前页面单据体--使用组织
            DynamicObjectCollection treeEntryColl = this.getModel().getEntryEntity("bentryentity");//获取
            this.getView().setVisible(false, "tdkw_belasticcontrol");
            this.getView().setVisible(false, "tdkw_belasticcount");
            this.getView().setVisible(false, "tdkw_bgroup3");
            this.getView().setVisible(false, "tdkw_bgroup31");

            this.getView().setVisible(false, "tdkw_felasticcontrol");
            this.getView().setVisible(false, "tdkw_felasticcount");
            this.getView().setVisible(false, "tdkw_fgroup2");
            this.getView().setVisible(false, "tdkw_fgroup21");

            this.getView().setVisible(false, "tdkw_eelasticcontrol");
            this.getView().setVisible(false, "tdkw_eelasticcount");
            this.getView().setVisible(false, "tdkw_egroup2");
            this.getView().setVisible(false, "tdkw_egroup21");

            this.getView().setVisible(false, "tdkw_delasticcontrol");
            this.getView().setVisible(false, "tdkw_delasticcount");
            this.getView().setVisible(false, "tdkw_dgroup2");
            this.getView().setVisible(false, "tdkw_dgroup21");

            this.getView().setVisible(false, "tdkw_celasticcontrol");
            this.getView().setVisible(false, "tdkw_celasticcount");
            this.getView().setVisible(false, "tdkw_cgroup2");
            this.getView().setVisible(false, "tdkw_cgroup21");

            if (treeEntryColl.size() > 0) {
                EntryGrid entryGrid = this.getView().getControl("bentryentity");
                entryGrid.focusCell(0, "buseorgname");
                DynamicObjectCollection entity = this.getModel().getEntryEntity("bentryentity");
                for (DynamicObject dyn1 : entity) {
                    String tdkw_ccontrolstrategy = dyn1.getString("tdkw_bcontrolstrategy");
                    if (tdkw_ccontrolstrategy.equals("3")) {
                        this.getView().setVisible(true, "tdkw_belasticcontrol");
                        this.getView().setVisible(true, "tdkw_belasticcount");
                        this.getView().setVisible(true, "tdkw_bgroup3");
                        this.getView().setVisible(true, "tdkw_bgroup31");
                        break;
                    }

                    DynamicObjectCollection fentity = this.getModel().getEntryEntity("fentryentity");
                    for (DynamicObject dyn2 : fentity) {
                        String tdkw_fcontrolstrategy1 = dyn2.getString("tdkw_fcontrolstrategy");
                        //存在弹性控编直接跳出
                        if (tdkw_fcontrolstrategy1.equals("3")) {
                            this.getView().setVisible(true, "tdkw_felasticcontrol");
                            this.getView().setVisible(true, "tdkw_felasticcount");
                            this.getView().setVisible(true, "tdkw_fgroup2");
                            this.getView().setVisible(true, "tdkw_fgroup21");
                            break;
                        }
                    }

                    DynamicObjectCollection eentity = this.getModel().getEntryEntity("eentryentity");
                    for (DynamicObject dyn3 : eentity) {
                        String tdkw_econtrolstrategy1 = dyn3.getString("tdkw_econtrolstrategy");
                        //存在弹性控编直接跳出
                        if (tdkw_econtrolstrategy1.equals("3")) {
                            this.getView().setVisible(true, "tdkw_eelasticcontrol");
                            this.getView().setVisible(true, "tdkw_eelasticcount");
                            this.getView().setVisible(true, "tdkw_egroup2");
                            this.getView().setVisible(true, "tdkw_egroup21");
                            break;
                        }
                    }
                    DynamicObjectCollection dentity = this.getModel().getEntryEntity("dentryentity");
                    for (DynamicObject dyn4 : dentity) {
                        String tdkw_dcontrolstrategy1 = dyn4.getString("tdkw_dcontrolstrategy");
                        //存在弹性控编直接跳出
                        if (tdkw_dcontrolstrategy1.equals("3")) {
                            this.getView().setVisible(true, "tdkw_delasticcontrol");
                            this.getView().setVisible(true, "tdkw_delasticcount");
                            this.getView().setVisible(true, "tdkw_dgroup2");
                            this.getView().setVisible(true, "tdkw_dgroup21");
                            break;
                        }
                    }


                    DynamicObjectCollection centity = this.getModel().getEntryEntity("centryentity");
                    for (DynamicObject dyn5 : centity) {
                        String tdkw_ccontrolstrategy1 = dyn5.getString("tdkw_ccontrolstrategy");
                        //存在弹性控编直接跳出
                        if (tdkw_ccontrolstrategy1.equals("3")) {
                            this.getView().setVisible(false, "tdkw_celasticcontrol");
                            this.getView().setVisible(false, "tdkw_celasticcount");
                            this.getView().setVisible(false, "tdkw_cgroup2");
                            this.getView().setVisible(false, "tdkw_cgroup21");
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        beforeF7SelectEvent.getFormShowParameter();
        String name = beforeF7SelectEvent.getProperty().getName();
        if (name.equals("tdkw_staff")) {
            ListShowParameter showParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            showParameter.setShowApproved(false);
            DynamicObject[] dynamicObject = BusinessDataServiceHelper.load("haos_staff", "id,year", new QFilter[]{
                    new QFilter("enable", QCP.equals, "1")
            });
            List<Long> ids = new ArrayList<>();
            for (DynamicObject object : dynamicObject) {
                Date staffYear = object.getDate("year");
                if (this.isUsing(staffYear)) {
                    ids.add(object.getLong("id"));
                }
            }
            showParameter.getListFilterParameter().getQFilters().add(new QFilter("id", QCP.in, ids));
        }
    }

    private boolean isUsing(Date staffYear) {
        return HRDateTimeUtils.getYear(staffYear) >= HRDateTimeUtils.getYear(new Date());
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        afterF7SelectEvent.getActionId();
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if (StringUtils.equals("tdkw_staff", name) && this.getModel().getValue("tdkw_staff") != null) {
            this.getView().showLoading(new LocaleString("数据加载中请稍等......"));
            this.getModel().beginInit();
            this.getModel().deleteEntryData("bentryentity");
            //获取编制信息
            DynamicObject tdkw_staff = (DynamicObject) this.getModel().getValue("tdkw_staff");
            Long tdkw_staffid = tdkw_staff.getLong("id");
            Long pkid = (Long) this.getModel().getValue("id");
            DynamicObject[] isstaff = BusinessDataServiceHelper.load("tdkw_bzchange_request", "id", new QFilter[]{
                    new QFilter("status", QCP.equals, "B").or(new QFilter("status", QCP.equals, "D")),
                    new QFilter("tdkw_staff", QCP.equals, tdkw_staffid),
                    new QFilter("id", QCP.not_equals, pkid)
            });
            if (isstaff.length > 0) {
                this.getView().showMessage("该编制信息存在已提交的变更单");
                //放开加载圈
                this.getView().hideLoading();
                return;
            }

            //获取编制维度
            DynamicObjectCollection tdkw_staffdimension = (DynamicObjectCollection) this.getModel().getValue("tdkw_staffdimension");
            Set<String> staffdimensionnumber = tdkw_staffdimension.stream().map(k -> k.getDynamicObject("fbasedataid").getString("number")).collect(Collectors.toSet());
            //显示 隐藏页签
            if (staffdimensionnumber.contains("1010_S")) {
                this.getView().setVisible(true, "tdkw_tabpage_position");
            } else {
                this.getView().setVisible(false, "tdkw_tabpage_position");
            }

            if (staffdimensionnumber.contains("1020_S")) {
                this.getView().setVisible(true, "tdkw_tabpage_job");
            } else {
                this.getView().setVisible(false, "tdkw_tabpage_job");
            }

            if (staffdimensionnumber.contains("1050_S")) {
                this.getView().setVisible(true, "tdkw_tabpage_laborreltype");
            } else {
                this.getView().setVisible(false, "tdkw_tabpage_laborreltype");
            }

            if (staffdimensionnumber.contains("1099_S")) {
                this.getView().setVisible(true, "tdkw_tabpageap");
            } else {
                this.getView().setVisible(false, "tdkw_tabpageap");
            }


            //使用组织渲染集合
            List<String> bentryentitylist = BzSelect.bentryentity();
            bentryentitylist.remove("bstaffdimension");
            try (AutoCloseViewHandler viewHandler = AutoCloseViewHandler.of("haos_staff")) {
                //打开视图
                IFormView compileinfoView = (IFormView) viewHandler.openModifyView(tdkw_staffid);
                //获取单据体
                DynamicObjectCollection bentryentitys = compileinfoView.getModel().getEntryEntity("bentryentity");
                //获取当前页面单据体--使用组织
                DynamicObjectCollection treeEntryColl = this.getModel().getEntryEntity("bentryentity");//获取
                DynamicObjectType treeEntryType = treeEntryColl.getDynamicObjectType();
                // 旧id  ---  新id  ---使用组织
                Map<Long, Long> id_bid = new HashMap<>();
                for (DynamicObject bentryentity : bentryentitys) {
//                    DynamicObject parentEntryObj = treeEntryColl.addNew();
                    DynamicObject parentEntryObj = new DynamicObject(treeEntryType);
                    long b = ORM.create().genLongId(treeEntryType);//生产出新的id
                    //添加数据
                    for (String bs : bentryentitylist) {
                        parentEntryObj.set(bs, bentryentity.get(bs));
                    }
                    //是否包含下级
                    long aLong = bentryentity.getDynamicObject("buseorg").getLong("boid");
                    List<Long> xj = new ArrayList<>();
                    xj.add(aLong);
                    List<Map<String, Object>> all = HRMServiceHelper.invokeHRMPService("haos", "IHAOSBatchAdminOrgInfoQueryService", "batchGetAllSubOrg", xj, new Date());
                    if (all.size() > 1) {
                        parentEntryObj.set("bhavesubentry", true);
                    }
                    //多选基础资料赋值
                    DynamicObjectCollection bstaffdimension = bentryentity.getDynamicObjectCollection("bstaffdimension");
                    List<DynamicObject> bstaffdimensionid = bstaffdimension.stream().map(k -> k).collect(Collectors.toList());
                    DynamicObjectCollection bbstaffdimension = parentEntryObj.getDynamicObjectCollection("bstaffdimension");
                    for (DynamicObject wor : bstaffdimensionid) {
                        DynamicObject base = new DynamicObject(bbstaffdimension.getDynamicObjectType());
                        base.set("fbasedataid", wor.get("fbasedataid"));
                        bbstaffdimension.add(base);
                    }

                    parentEntryObj.set("tdkw_bid", bentryentity.get("id"));
                    parentEntryObj.set("tdkw_bpid", bentryentity.get("pid"));
                    id_bid.put(bentryentity.getLong("id"), b);
                    parentEntryObj.set("id", b);
                    if (bentryentity.getLong("pid") != 0L) {
                        Long pid = id_bid.get(bentryentity.getLong("pid"));
                        parentEntryObj.set("pid", pid);
                    } else {
                        parentEntryObj.set("pid", 0L);
                    }
                    if (staffdimensionnumber.contains("1010_S")) {//岗位
                        //子单据体
                        DynamicObjectCollection tdkw_centryentity = parentEntryObj.getDynamicObjectCollection("centryentity");
                        //原 子单据体
                        DynamicObjectCollection centryentity = bentryentity.getDynamicObjectCollection("centryentity");
                        //岗位单据体
                        for (DynamicObject cdy : centryentity) {
                            DynamicObject dynamicObject = tdkw_centryentity.addNew();
                            List<String> centryentitylist = BzSelect.centryentity();
                            for (String cs : centryentitylist) {
                                dynamicObject.set(cs, cdy.get(cs));
                            }
                            dynamicObject.set("tdkw_cid", cdy.get("id"));
                        }
                    }

                    if (staffdimensionnumber.contains("1020_S")) {//职位
                        //子单据体
                        DynamicObjectCollection tdkw_dentryentity = parentEntryObj.getDynamicObjectCollection("dentryentity");
                        //原 子单据体
                        DynamicObjectCollection dentryentity = bentryentity.getDynamicObjectCollection("dentryentity");
                        //职位编制单据体
                        for (DynamicObject ddy : dentryentity) {
                            DynamicObject dynamicObject = tdkw_dentryentity.addNew();
                            List<String> dentryentitylist = BzSelect.dentryentity();
                            for (String ds : dentryentitylist) {
                                dynamicObject.set(ds, ddy.get(ds));
                            }

                            dynamicObject.set("tdkw_did", ddy.get("id"));
                        }

                    }


                    if (staffdimensionnumber.contains("1050_S")) {//用工关系
                        //子单据体
                        DynamicObjectCollection tdkw_eentryentity = parentEntryObj.getDynamicObjectCollection("eentryentity");
                        //原 子单据体
                        DynamicObjectCollection eentryentity = bentryentity.getDynamicObjectCollection("eentryentity");
                        //用工关系类型单据体
                        for (DynamicObject edy : eentryentity) {
                            DynamicObject dynamicObject = tdkw_eentryentity.addNew();
                            List<String> eentryentitylist = BzSelect.eentryentity();
                            for (String es : eentryentitylist) {
                                dynamicObject.set(es, edy.get(es));
                            }

                            dynamicObject.set("tdkw_eid", edy.get("id"));
                        }
                    }

                    if (staffdimensionnumber.contains("1099_S")) {//职级
                        //子单据体
                        DynamicObjectCollection tdkw_fentryentity = parentEntryObj.getDynamicObjectCollection("fentryentity");
                        //原 子单据体
                        DynamicObjectCollection fentryentity = bentryentity.getDynamicObjectCollection("fentryentity");
                        //职级编制单据体
                        for (DynamicObject fdy : fentryentity) {
                            DynamicObject dynamicObject = tdkw_fentryentity.addNew();
                            List<String> fentryentitylist = BzSelect.fentryentity();
                            for (String fs : fentryentitylist) {
                                dynamicObject.set(fs, fdy.get(fs));
                            }

                            dynamicObject.set("tdkw_fid", fdy.get("id"));
                        }
                    }

                    //数据添加入树形单据体
                    treeEntryColl.add(parentEntryObj);
                }

                this.getModel().updateEntryCache(treeEntryColl);
                this.getView().setVisible(false, "belasticcontrol");
                this.getView().setVisible(false, "belasticcount");
                this.getView().setVisible(false, "tdkw_bgroup3");

                this.getView().setVisible(false, "celasticcontrol");
                this.getView().setVisible(false, "celasticcount");
                this.getView().setVisible(false, "tdkw_fgroup2");

                this.getView().setVisible(false, "delasticcontrol");
                this.getView().setVisible(false, "delasticcount");
                this.getView().setVisible(false, "tdkw_egroup2");

                this.getView().setVisible(true, "eelasticcontrol");
                this.getView().setVisible(true, "eelasticcount");
                this.getView().setVisible(false, "tdkw_dgroup2");

                this.getView().setVisible(true, "felasticcontrol");
                this.getView().setVisible(true, "felasticcount");
                this.getView().setVisible(false, "tdkw_cgroup2");

                if (treeEntryColl.size() > 0) {
                    EntryGrid entryGrid = this.getView().getControl("bentryentity");
                    entryGrid.focusCell(0, "buseorgname");
                    DynamicObjectCollection entity = this.getModel().getEntryEntity("bentryentity");
                    for (DynamicObject dyn1 : entity) {
                        String bcontrolstrategy = dyn1.getString("bcontrolstrategy");
                        if (bcontrolstrategy.equals("3")) {
                            this.getView().setVisible(true, "belasticcontrol");
                            this.getView().setVisible(true, "belasticcount");
                            this.getView().setVisible(true, "tdkw_bgroup3");
                            break;
                        }

                        DynamicObjectCollection fentity = this.getModel().getEntryEntity("fentryentity");
                        for (DynamicObject dyn2 : fentity) {
                            String fcontrolstrategy = dyn2.getString("fcontrolstrategy");
                            //存在弹性控编直接跳出
                            if (fcontrolstrategy.equals("3")) {
                                this.getView().setVisible(true, "felasticcontrol");
                                this.getView().setVisible(true, "felasticcount");
                                this.getView().setVisible(true, "tdkw_fgroup2");
                                break;
                            }
                        }

                        DynamicObjectCollection eentity = this.getModel().getEntryEntity("eentryentity");
                        for (DynamicObject dyn3 : eentity) {
                            String econtrolstrategy = dyn3.getString("econtrolstrategy");
                            //存在弹性控编直接跳出
                            if (econtrolstrategy.equals("3")) {
                                this.getView().setVisible(true, "eelasticcontrol");
                                this.getView().setVisible(true, "eelasticcount");
                                this.getView().setVisible(true, "tdkw_egroup2");
                                break;
                            }
                        }
                        DynamicObjectCollection dentity = this.getModel().getEntryEntity("dentryentity");
                        for (DynamicObject dyn4 : dentity) {
                            String dcontrolstrategy = dyn4.getString("dcontrolstrategy");
                            //存在弹性控编直接跳出
                            if (dcontrolstrategy.equals("3")) {
                                this.getView().setVisible(true, "delasticcontrol");
                                this.getView().setVisible(true, "delasticcount");
                                this.getView().setVisible(true, "tdkw_dgroup2");
                                break;
                            }
                        }


                        DynamicObjectCollection centity = this.getModel().getEntryEntity("centryentity");
                        for (DynamicObject dyn5 : centity) {
                            String ccontrolstrategy = dyn5.getString("ccontrolstrategy");
                            //存在弹性控编直接跳出
                            if (ccontrolstrategy.equals("3")) {
                                this.getView().setVisible(true, "celasticcontrol");
                                this.getView().setVisible(true, "celasticcount");
                                this.getView().setVisible(true, "tdkw_cgroup2");
                                break;
                            }
                        }
                    }
                }

                //放开加载圈
                this.getView().updateView("bentryentity");
                this.getView().hideLoading();
                this.getModel().endInit();

            } catch (Exception err) {
                //放开加载圈
                this.getView().hideLoading();
                // 异常处理
                LOGGER.error("初始化失败:" + ExceptionUtils.getStackTrace(err));
            }
        }
    }
}
