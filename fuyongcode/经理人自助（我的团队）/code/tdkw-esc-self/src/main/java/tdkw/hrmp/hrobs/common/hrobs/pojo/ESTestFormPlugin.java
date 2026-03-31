package tdkw.hrmp.hrobs.common.hrobs.pojo;

import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.entity.api.ApiResult;
import kd.hr.hbp.business.service.query.es.storage.EsResultVo;
import kd.hr.hbp.business.service.query.es.storage.highlevel.EsStorageImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description ： 查找ES所有数据
 * @ClassName ：ESTestFormPlugin
 * @author xxx
 * @Date ：2023/8/29 16:12
 * @Version: 1.0
 */
public class ESTestFormPlugin implements IBillWebApiPlugin {

    public static final  String  region = "HR";

    public static final  String  indexName = "hrpi_person";

    @Override
    public ApiResult doCustomService(Map<String, Object> params) {
        EsStorageImpl esStorage = new EsStorageImpl(region);
        int count = esStorage.getCount(indexName, null, null);

        EsResultVo query = esStorage.query(indexName, null, null, null, null, true, 0, count);
        List<Map<String, Object>> resultData = query.getResultData();
        int size = resultData.size();
        Map<String,Object> date = new HashMap<>();
        Map<String,Integer> dgtj =new HashMap<>();
        int dang =0;
        int gong = 0;
        int tuan =0;
        int ji = 0;
        int cai =0;
        int jin = 0;
        int dong =0;

        List<Map<String, Object>>  namelist= new ArrayList<>();
        for (Map<String, Object> resultDatum : resultData) {
            Map<String,Object> namemap = new HashMap<>();
            String personappoint = (String) resultDatum.get("personappoint");
            Object id = resultDatum.get("hrpi_person-boid");
            Object name = resultDatum.get("hspm_personinfo-name");
            namemap.put("name",name);
            namemap.put("id",id);
            namemap.put("personappoint",personappoint);
            namelist.add(namemap);
            if(personappoint.contains("党")){
                dang++;
            }else if(personappoint.contains("工")){
                gong++;
            }else if(personappoint.contains("团")){
                tuan++;
            }
            else if(personappoint.contains("纪")){
                ji++;
            }else if(personappoint.contains("财务")){
                cai++;
            }else if(personappoint.contains("经营")){
                jin++;
            }else if(personappoint.contains("董")){
                dong++;
            }
        }
        dgtj.put("dang",dang);
        dgtj.put("gong",gong);
        dgtj.put("tuan",tuan);
        dgtj.put("ji",ji);
        dgtj.put("cai",cai);
        dgtj.put("jin",jin);
        dgtj.put("dong",dong);

        date.put("size",count);
        date.put("alldata",namelist);
        date.put("alldata_size",size);
        date.put("dgtj",dgtj);
        return ApiResult.success(date);
    }



}
