package tdkw.hr.odc.haos.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.TreeEntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.RowClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : BZchangeFrom
 * @Description : 页面显示隐藏
 * @Author : XYP
 * @Date: 2024-07-27 10:14
 */
public class BZchangeFromPlugin extends AbstractBillPlugIn implements RowClickEventListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid entryGrid = this.getView().getControl("bentryentity");
        entryGrid.addRowClickListener(this);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String name = e.getProperty().getName();
        if (StringUtils.equals("tdkw_bcontrolstrategy", name)) {//使用组织 b
            String tdkw_bcontrolstrategy = (String) this.getModel().getValue("tdkw_bcontrolstrategy");
            if (tdkw_bcontrolstrategy!=null && tdkw_bcontrolstrategy.equals("3")) {
                this.getView().setVisible(true, "tdkw_belasticcontrol");
                this.getView().setVisible(true, "tdkw_belasticcount");
                this.getView().setVisible(true, "tdkw_bgroup31");
            } else {
                DynamicObjectCollection bentity = this.getModel().getEntryEntity("bentryentity");
                for (DynamicObject dynamicObject : bentity) {
                    String tdkw_bcontrolstrategy1 = dynamicObject.getString("tdkw_bcontrolstrategy");
                    //存在弹性控编直接跳出
                    if (tdkw_bcontrolstrategy1!=null && tdkw_bcontrolstrategy1.equals("3")) {
                        return;
                    }
                }
                this.getView().setVisible(false, "tdkw_belasticcontrol");
                this.getView().setVisible(false, "tdkw_belasticcount");
                this.getView().setVisible(false, "tdkw_bgroup31");
            }
        } else if (StringUtils.equals("tdkw_ccontrolstrategy", name)) {//岗位 c
            String tdkw_ccontrolstrategy = (String) this.getModel().getValue("tdkw_ccontrolstrategy");
            if (tdkw_ccontrolstrategy!=null && tdkw_ccontrolstrategy.equals("3")) {
                this.getView().setVisible(true, "tdkw_celasticcontrol");
                this.getView().setVisible(true, "tdkw_celasticcount");
                this.getView().setVisible(true, "tdkw_cgroup21");
                this.getView().setVisible(true, "tdkw_cyearstaffnum");
            } else {
                DynamicObjectCollection centity = this.getModel().getEntryEntity("centryentity");
                for (DynamicObject dynamicObject : centity) {
                    String tdkw_ccontrolstrategy1 = dynamicObject.getString("tdkw_ccontrolstrategy");
                    //存在弹性控编直接跳出
                    if (tdkw_ccontrolstrategy1!=null && tdkw_ccontrolstrategy1.equals("3")) {
                        return;
                    }
                }
                this.getView().setVisible(false, "tdkw_celasticcontrol");
                this.getView().setVisible(false, "tdkw_celasticcount");
                this.getView().setVisible(false, "tdkw_cgroup21");
                this.getView().setVisible(false, "tdkw_cyearstaffnum");
            }

        } else if (StringUtils.equals("tdkw_dcontrolstrategy", name)) {//职位 d
            String tdkw_dcontrolstrategy = (String) this.getModel().getValue("tdkw_dcontrolstrategy");
            if (tdkw_dcontrolstrategy!=null && tdkw_dcontrolstrategy.equals("3")) {
                this.getView().setVisible(true, "tdkw_delasticcontrol");
                this.getView().setVisible(true, "tdkw_delasticcount");
                this.getView().setVisible(true, "tdkw_dgroup21");
                this.getView().setVisible(true, "tdkw_dyearstaffnum");
            } else {
                DynamicObjectCollection dentity = this.getModel().getEntryEntity("dentryentity");
                for (DynamicObject dynamicObject : dentity) {
                    String tdkw_dcontrolstrategy1 = dynamicObject.getString("tdkw_dcontrolstrategy");
                    //存在弹性控编直接跳出
                    if (tdkw_dcontrolstrategy1!=null && tdkw_dcontrolstrategy1.equals("3")) {
                        return;
                    }
                }
                this.getView().setVisible(false, "tdkw_delasticcontrol");
                this.getView().setVisible(false, "tdkw_delasticcount");
                this.getView().setVisible(false, "tdkw_dgroup21");
                this.getView().setVisible(false, "tdkw_dyearstaffnum");
            }

        } else if (StringUtils.equals("tdkw_econtrolstrategy", name)) {//用工 e
            String tdkw_econtrolstrategy = (String) this.getModel().getValue("tdkw_econtrolstrategy");
            if (tdkw_econtrolstrategy!=null &&tdkw_econtrolstrategy.equals("3")) {
                this.getView().setVisible(true, "tdkw_eelasticcontrol");
                this.getView().setVisible(true, "tdkw_eelasticcount");
                this.getView().setVisible(true, "tdkw_egroup21");
                this.getView().setVisible(true, "tdkw_eyearstaffnum");
            } else {
                DynamicObjectCollection eentity = this.getModel().getEntryEntity("eentryentity");
                for (DynamicObject dynamicObject : eentity) {
                    String tdkw_econtrolstrategy1 = dynamicObject.getString("tdkw_econtrolstrategy");
                    //存在弹性控编直接跳出
                    if (tdkw_econtrolstrategy1!=null && tdkw_econtrolstrategy1.equals("3")) {
                        return;
                    }
                }
                this.getView().setVisible(false, "tdkw_eelasticcontrol");
                this.getView().setVisible(false, "tdkw_eelasticcount");
                this.getView().setVisible(false, "tdkw_egroup21");
                this.getView().setVisible(false, "tdkw_eyearstaffnum");
            }

        } else if (StringUtils.equals("tdkw_fcontrolstrategy", name)) {//职级 f
            String tdkw_fcontrolstrategy = (String) this.getModel().getValue("tdkw_fcontrolstrategy");
            if (tdkw_fcontrolstrategy!=null && tdkw_fcontrolstrategy.equals("3")) {
                this.getView().setVisible(true, "tdkw_felasticcontrol");
                this.getView().setVisible(true, "tdkw_felasticcount");
                this.getView().setVisible(true, "tdkw_fgroup21");
                this.getView().setVisible(true, "tdkw_fyearstaffnum");
            } else {
                DynamicObjectCollection fentity = this.getModel().getEntryEntity("fentryentity");
                for (DynamicObject dynamicObject : fentity) {
                    String tdkw_fcontrolstrategy1 = dynamicObject.getString("tdkw_fcontrolstrategy");
                    //存在弹性控编直接跳出
                    if (tdkw_fcontrolstrategy1!=null && tdkw_fcontrolstrategy1.equals("3")) {
                        return;
                    }
                }
                this.getView().setVisible(false, "tdkw_felasticcontrol");
                this.getView().setVisible(false, "tdkw_felasticcount");
                this.getView().setVisible(false, "tdkw_fgroup21");
                this.getView().setVisible(false, "tdkw_fyearstaffnum");
            }
        } else if (StringUtils.equals("tdkw_baddendum", name)) {
            int rowIndex = e.getChangeSet()[0].getRowIndex();
            //改变后的值
            Integer newValue = (e.getChangeSet()[0].getNewValue() != null) ? (Integer) e.getChangeSet()[0].getNewValue() : 0;
            //改变前的值
            Integer oldValue = (e.getChangeSet()[0].getOldValue() != null) ? (Integer) e.getChangeSet()[0].getOldValue() : 0;
            //获取上级id
            Long pid = (Long) this.getModel().getValue("pid", rowIndex);
            DynamicObjectCollection entity = this.getModel().getEntryEntity("bentryentity");
            //存储使用组织id集合
            Map<Long, DynamicObject> map = new HashMap<>();
            for (DynamicObject bentsetmap : entity) {
                Long buseorg = bentsetmap.getLong("id");
                map.put(buseorg, bentsetmap);
            }
            if (map.get(pid) != null) {
                setbaddendum(pid, map, newValue, oldValue);
            }
        }
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        String itemKey = evt.getItemKey();
        if (StringUtils.equals(itemKey, "bar_save") || StringUtils.equals(itemKey, "submit")) {
            this.getModel().setValue("tdkw_isincrease", false);
            DynamicObjectCollection entity = this.getModel().getEntryEntity("bentryentity");
            for (DynamicObject dynamicObject : entity) {
                String number = dynamicObject.getDynamicObject("buseorg").getDynamicObject("adminorgtype").getString("number");
                if (number.equals("1040_S")) {//一级部门
                    int tdkw_baddendum = dynamicObject.getInt("tdkw_baddendum2");
                    if (tdkw_baddendum > 0) {
                        this.getModel().setValue("tdkw_isincrease", true);
                        break;
                    }
                }
            }
            Boolean boo=false;
            for (DynamicObject dyop : entity) {
                int tdkw_tzh = dyop.getInt("tdkw_tzh");
                if (tdkw_tzh>0){
                    boo=true;
                }
                DynamicObjectCollection centryentity = dyop.getDynamicObjectCollection("centryentity");
                for (DynamicObject dyc : centryentity) {
                    int addendum = dyc.getInt("tdkw_caddendum1");
                    if (addendum>0){
                        boo=true;
                    }
                }
                DynamicObjectCollection dentryentity = dyop.getDynamicObjectCollection("dentryentity");
                for (DynamicObject dyd : dentryentity) {
                    int addendum = dyd.getInt("tdkw_daddendum1");
                    if (addendum>0){
                        boo=true;
                    }
                }
                DynamicObjectCollection eentryentity = dyop.getDynamicObjectCollection("eentryentity");
                for (DynamicObject dye : eentryentity) {
                    int addendum = dye.getInt("tdkw_eaddendum1");
                    if (addendum>0){
                        boo=true;
                    }
                }
                DynamicObjectCollection fentryentity = dyop.getDynamicObjectCollection("fentryentity");
                for (DynamicObject dyf : fentryentity) {
                    int addendum = dyf.getInt("tdkw_faddendum1");
                    if (addendum>0){
                        boo=true;
                    }
                }
            }
            if (!boo){
                this.getView().showTipNotification("请至少修改一项");
                evt.setCancel(true);
            }
        }
    }

    @Override
    public void entryRowClick(RowClickEvent evt) {
        RowClickEventListener.super.entryRowClick(evt);
        TreeEntryGrid treeEntryGrid = (TreeEntryGrid) evt.getSource();
        String entryKey = treeEntryGrid.getEntryKey();
        if (entryKey.equals("bentryentity")) {
            DynamicObjectCollection fentity = this.getModel().getEntryEntity("fentryentity");
            this.getView().setVisible(false, "tdkw_felasticcontrol");
            this.getView().setVisible(false, "tdkw_felasticcount");
            this.getView().setVisible(false, "tdkw_fgroup21");
            this.getView().setVisible(false, "tdkw_fyearstaffnum");
            for (DynamicObject dynamicObject : fentity) {
                String tdkw_fcontrolstrategy1 = dynamicObject.getString("tdkw_fcontrolstrategy");
                //存在弹性控编直接跳出
                if (tdkw_fcontrolstrategy1!=null && tdkw_fcontrolstrategy1.equals("3")) {
                    this.getView().setVisible(true, "tdkw_felasticcontrol");
                    this.getView().setVisible(true, "tdkw_felasticcount");
                    this.getView().setVisible(true, "tdkw_fgroup21");
                    this.getView().setVisible(true, "tdkw_fyearstaffnum");
                    break;
                }
            }


            DynamicObjectCollection eentity = this.getModel().getEntryEntity("eentryentity");
            this.getView().setVisible(false, "tdkw_eelasticcontrol");
            this.getView().setVisible(false, "tdkw_eelasticcount");
            this.getView().setVisible(false, "tdkw_egroup21");
            this.getView().setVisible(false, "tdkw_eyearstaffnum");
            for (DynamicObject dynamicObject : eentity) {
                String tdkw_econtrolstrategy1 = dynamicObject.getString("tdkw_econtrolstrategy");
                //存在弹性控编直接跳出
                if (tdkw_econtrolstrategy1!=null && tdkw_econtrolstrategy1.equals("3")) {
                    this.getView().setVisible(true, "tdkw_eelasticcontrol");
                    this.getView().setVisible(true, "tdkw_eelasticcount");
                    this.getView().setVisible(true, "tdkw_egroup21");
                    this.getView().setVisible(true, "tdkw_eyearstaffnum");
                    break;
                }
            }


            DynamicObjectCollection dentity = this.getModel().getEntryEntity("dentryentity");
            this.getView().setVisible(false, "tdkw_delasticcontrol");
            this.getView().setVisible(false, "tdkw_delasticcount");
            this.getView().setVisible(false, "tdkw_dgroup21");
            this.getView().setVisible(false, "tdkw_dyearstaffnum");
            for (DynamicObject dynamicObject : dentity) {
                String tdkw_dcontrolstrategy1 = dynamicObject.getString("tdkw_dcontrolstrategy");
                //存在弹性控编直接跳出
                if (tdkw_dcontrolstrategy1!=null &&  tdkw_dcontrolstrategy1.equals("3")) {
                    this.getView().setVisible(true, "tdkw_delasticcontrol");
                    this.getView().setVisible(true, "tdkw_delasticcount");
                    this.getView().setVisible(true, "tdkw_dgroup21");
                    this.getView().setVisible(true, "tdkw_dyearstaffnum");
                    break;
                }
            }


            this.getView().setVisible(false, "tdkw_celasticcontrol");
            this.getView().setVisible(false, "tdkw_celasticcount");
            this.getView().setVisible(false, "tdkw_cgroup21");
            this.getView().setVisible(false, "tdkw_cyearstaffnum");
            DynamicObjectCollection centity = this.getModel().getEntryEntity("centryentity");
            for (DynamicObject dynamicObject : centity) {
                String tdkw_ccontrolstrategy1 = dynamicObject.getString("tdkw_ccontrolstrategy");
                //存在弹性控编直接跳出
                if (tdkw_ccontrolstrategy1!=null && tdkw_ccontrolstrategy1.equals("3")) {
                    this.getView().setVisible(true, "tdkw_celasticcontrol");
                    this.getView().setVisible(true, "tdkw_celasticcount");
                    this.getView().setVisible(true, "tdkw_cgroup21");
                    this.getView().setVisible(true, "tdkw_ckd.cd.tw.twhr.biz.hr.plugin.form.haos_staff.StaffWorkyearstaffnum");
                    break;
                }
            }

        }
    }

    void setbaddendum(Long pid, Map<Long, DynamicObject> map, Integer newValue, Integer oldValue) {
        if (map.get(pid) != null) {
            DynamicObject dynamicObject = map.get(pid);
            //获取本级的上级id
            Long pids = dynamicObject.getLong("pid");
            //获取序号
            int seq = dynamicObject.getInt("seq");
            //当编制变更不为空的时候 给这一行赋值
//            if (dynamicObject.get("tdkw_baddendum") != null) {
//                int tdkw_baddendum = dynamicObject.getInt("tdkw_baddendum1");
            int tdkw_baddendum = (dynamicObject.get("tdkw_baddendum1") != null) ? dynamicObject.getInt("tdkw_baddendum1") : 0;
            tdkw_baddendum = tdkw_baddendum - oldValue + newValue;
            this.getModel().setValue("tdkw_baddendum1", tdkw_baddendum, seq - 1);
//            }
            this.setbaddendum(pids, map, newValue, oldValue);
        }
    }
}
