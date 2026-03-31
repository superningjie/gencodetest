package tdkw.opa.tdkw_opa.formplugin.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.form.FormShowParameter;
import kd.bos.form.plugin.AbstractFormPlugin;
import tdkw.opa.tdkw_opa.formplugin.enums.EntityName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;

public class AuditInnerFormPlugin  extends AbstractFormPlugin {
    @Override
    public void afterCreateNewData(EventObject e) {


        super.afterCreateNewData(e);
        FormShowParameter showParameter = this.getView().getFormShowParameter();

        String assessYearStr = showParameter.getCustomParam("assessYear");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date assessYearDate = dateFormat.parse(assessYearStr);
            this.getModel().setValue("tdkw_assess_year", assessYearDate);

        } catch (ParseException ex) {
            throw new KDException(ex, new ErrorCode("TXException", ex.getMessage()));

        }
        JSONArray rows = showParameter.getCustomParam("rows");

        if (!rows.isEmpty()) {
            this.getModel().batchCreateNewEntryRow("tdkw_entryentity",rows.size());

        }

        for (int i = 0, rowsSize = rows.size(); i < rowsSize; i++) {
            JSONObject row = rows.getJSONObject(i);
            for (String field : EntityName.AUDIT_FIELDS) {
                Object o = row.get(field);
                if (o instanceof JSONObject) {
                    this.getModel().setValue(field, ((JSONObject) o).getLong("id"),i);
                }else {
                    this.getModel().setValue(field, o,i);
                }
                this.getModel().setValue("tdkw_metric_type", row.getJSONObject("tdkw_metric_type").getLong("id"),i);
            }
        }

    }
}
