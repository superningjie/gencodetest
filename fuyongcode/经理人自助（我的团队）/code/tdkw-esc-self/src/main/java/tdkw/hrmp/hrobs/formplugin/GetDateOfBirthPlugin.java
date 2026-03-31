package tdkw.hrmp.hrobs.formplugin;

import kd.bos.cache.CacheConfigInfo;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.LocalMemoryCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.print.core.data.DataRowSet;
import kd.bos.print.core.data.datasource.PrtDataSource;
import kd.bos.print.core.data.field.TextField;
import kd.bos.print.core.plugin.AbstractPrintPlugin;
import kd.bos.print.core.plugin.event.AfterLoadDataEvent;
import kd.bos.print.core.plugin.event.BeforeLoadDataEvent;
import kd.bos.print.core.plugin.event.CustomDataLoadEvent;
import kd.bos.servicehelper.QueryServiceHelper;

import java.text.SimpleDateFormat;
import java.util.*;

public class GetDateOfBirthPlugin extends AbstractPrintPlugin {
    private static final Log logger = LogFactory.getLog(GetDateOfBirthPlugin.class);

    @Override
    public void beforeLoadData(BeforeLoadDataEvent evt) {
        super.beforeLoadData(evt);
    }

    @Override
    public void afterLoadData(AfterLoadDataEvent evt) {
        super.afterLoadData(evt);
        PrtDataSource dataSource = evt.getDataSource();
        // 加载缓存
        CacheConfigInfo localConfig = new CacheConfigInfo();
        localConfig.setMaxItemSize(30000);
        localConfig.setTimeout(60 * 5);
        LocalMemoryCache localCache = CacheFactory.getCommonCacheFactory().$getOrCreateLocalMemoryCache("getDateOfBirthPlugin", "personId", localConfig);

        if ("tdkw_prove_handle".equals(dataSource.getDsName())) {
            Long fid = Long.valueOf(evt.getDataRowSets().get(0).getField("tdkw_basedatafield5.id").toString());
            QFilter filter = new QFilter("id", QCP.equals, fid);
            filter.and("businessstatus", QCP.equals, "1");
            filter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            filter.and("datastatus", QCP.equals, "1");
            DynamicObject dynamicObject = QueryServiceHelper.queryOne("hspm_ermanfile", "person.name,person.id", filter.toArray());
            logger.info("查出人员信息:" + dynamicObject);
            QFilter qFilter = new QFilter("person.id", QCP.equals, dynamicObject.getLong("person.id"));
            qFilter.and("iscurrentversion", QCP.equals, Boolean.TRUE);
            qFilter.and("datastatus", QCP.equals, "1");
            DynamicObject dynamicObject1 = QueryServiceHelper.queryOne("hrpi_pernontsprop", "person.name,birthday", qFilter.toArray());
            logger.info("查出人员非时序性属性信息：" + dynamicObject1);
            Date birthday = dynamicObject1.getDate("birthday");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthday);
            // 加上一个小时
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            Date birthNewDate = calendar.getTime();
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            List<Object> invalidValues = Arrays.asList("-", "0", 0, "", null);
            if (!invalidValues.contains(birthNewDate)) {
                String newbirthday = yearFormat.format(birthNewDate) + "年" + monthFormat.format(birthNewDate) + "月" + dayFormat.format(birthNewDate) + "日";
                localCache.put("birthday", newbirthday);
            } else {
                throw new KDBizException("请检查人员非时序性属性中出生日期");
            }

        }

        if ("getDateOfBirth".equals(dataSource.getDsName())) {
            String birthday = localCache.get("birthday").toString();
            logger.info("人员出生日期:" + birthday);
            List<Object> invalidValues = Arrays.asList("-", "0", 0, "", null);
            if (!invalidValues.contains(birthday)) {
                List<DataRowSet> dataRowSets = new ArrayList<>();
                DataRowSet dataRowSet = new DataRowSet();
                dataRowSet.put("dateOfBirth", new TextField(birthday));
                dataRowSets.add(dataRowSet);
                evt.setDataRowSets(dataRowSets);
            } else {
                throw new KDBizException("请检查人员非时序性属性中出生日期");
            }
        }
    }

    @Override
    public void loadCustomData(CustomDataLoadEvent evt) {
        super.loadCustomData(evt);
    }


}
