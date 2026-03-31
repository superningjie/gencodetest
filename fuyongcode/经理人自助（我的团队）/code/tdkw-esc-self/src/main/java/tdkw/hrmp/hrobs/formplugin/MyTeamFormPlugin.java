package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.control.Image;
import kd.bos.form.control.Label;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import org.apache.commons.lang3.StringUtils;
import java.util.EventObject;

/**
 * @Metadata ：
 * @Description ：员服门户-我的团队
 * @author xxx
 * @Date ：2023/05/18
 * @Version: 1.0
 */
public class MyTeamFormPlugin extends AbstractFormPlugin {

  public GetSystemParUtils getSystemParUtils = new GetSystemParUtils();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
//        Label label = this.getControl("tdkw_myteamlabel");//我的团队
//        label.addClickListener(this);
        String label = "tdkw_myteamlabel,tdkw_attendlabel,tdkw_trainlabel,tdkw_performancelabel";
        String icon = "tdkw_myteamcion,tdkw_attendicon,tdkw_trainicon,tdkw_performanceicon";
        String field = label+","+icon;
        this.addClickListeners(field.split(","));
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Object source = evt.getSource();
        if(source instanceof Label)
        {
            Label label = (Label) evt.getSource();
            String key = label.getKey();
            String url = null;
            switch (key){
                case "tdkw_myteamlabel":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield");
                    break;
                case "tdkw_attendlabel":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield1");
                    break;
                case "tdkw_trainlabel":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield2");
                    break;
                case "tdkw_performancelabel":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield3");
                    break;
            }
            if(StringUtils.isNotBlank(url)){
                this.getView().openUrl(url);
            }
        }
        if(source instanceof Image){
            Image label = (Image) evt.getSource();
            String key = label.getKey();
            String url = null;
            switch (key){
                case "tdkw_myteamicon":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield");
                    break;
                case "tdkw_attendicon":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield1");
                    break;
                case "tdkw_trainicon":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield2");
                    break;
                case "tdkw_performanceicon":
                    url = getSystemParUtils.getSysParByFieldKey("tdkw_textfield3");
                    break;
            }
            if(StringUtils.isNotBlank(url)){
                this.getView().openUrl(url);
            }
        }

    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
    }

}
