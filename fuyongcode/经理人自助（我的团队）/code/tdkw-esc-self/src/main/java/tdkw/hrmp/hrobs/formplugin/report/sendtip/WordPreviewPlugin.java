//package tdkw.hrmp.hrobs.formplugin.report.sendtip;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import kd.bos.context.RequestContext;
//import kd.bos.dataentity.entity.LocaleString;
//import kd.bos.dataentity.utils.ObjectUtils;
//import kd.bos.dataentity.utils.StringUtils;
//import kd.bos.exception.KDBizException;
//import kd.bos.fileservice.FileItem;
//import kd.bos.fileservice.FileService;
//import kd.bos.fileservice.FileServiceFactory;
//import kd.bos.fileservice.preview.PreviewServiceFactory;
//import kd.bos.form.events.AfterDoOperationEventArgs;
//import kd.bos.form.events.BeforeDoOperationEventArgs;
//import kd.bos.form.field.ComboEdit;
//import kd.bos.form.field.ComboItem;
//import kd.bos.form.operate.FormOperate;
//import kd.bos.form.plugin.AbstractFormPlugin;
//import tdkw.hr.hspm.business.service.impl.FileConversionImpl;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.EventObject;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * @author xxx
// * @ClassName WordPrintSelectTemplatePlugin
// * @description: word打印选择模板弹窗  tdkw_hspm_wordtempprin_ex   tdkw_hrobs_erfilelistdv
// * @ModifiedBy: （填入修改人）
// * @ModifyTime: （修改时间）
// * @date 2023年07月17日
// * @version: 1.0
// */
//public class WordPreviewPlugin extends AbstractFormPlugin {
//
//    private static final String tdkw_SELECTTEMPLATE = "tdkw_selecttemplate";
//    private static final String WORD = "word";
//
//    /*@Override
//    public void afterCreateNewData(EventObject e) {
//        super.afterCreateNewData(e);
//        //全部的word模板
//        JSONArray allWordTemplate = this.getView().getFormShowParameter().getCustomParam("wordTemplate");
//        if (!ObjectUtils.isEmpty(allWordTemplate)) {
//            List<ComboItem> data = new ArrayList<>();
//            //动态设置下拉列表值
//            for (int i = 0; i < allWordTemplate.size(); i++) {
//                JSONObject wordTemplate = allWordTemplate.getJSONObject(i);
//                String name = wordTemplate.getString("name");
//                String number = wordTemplate.getString("number");
//                data.add(new ComboItem(new LocaleString(name), number));
//            }
//            ComboEdit selectTemplateCombo = this.getControl(tdkw_SELECTTEMPLATE);
//            selectTemplateCombo.setComboItems(data);
//        }
//    }*/
//
//    @Override
//    public void afterDoOperation(AfterDoOperationEventArgs e) {
//        super.afterDoOperation(e);
//        String operateKey = e.getOperateKey();
//        String selectTemplateNumber ="";
//        //e.getSource()
//        switch (operateKey) {
//            case "word_download1":
//            case "word_preview1":
//                selectTemplateNumber = "YGJL_01";
//                break;
//            case "word_download2":
//            case "word_preview2":
//                selectTemplateNumber = "YGLL_01";
//                break;
//            case "word_download3":
//            case "word_preview3":
//                selectTemplateNumber = "GBRMB_01";
//                break;
//            case "word_download4":
//            case "word_preview4":
//                selectTemplateNumber = "PXJL_01";
//                break;
//            default:
//                break;
//        }
//        if (StringUtils.equals("word_download1", operateKey) ||
//                StringUtils.equals("word_download2", operateKey) ||
//                StringUtils.equals("word_download3", operateKey) ||
//                StringUtils.equals("word_download4", operateKey)) {
//            // selectTemplateNumber = "YGJL_01";//YGJL_01:员工简历  YGLL_01:员工履历 PXJL_01:培训简历 GBRMB_01:干部任免审批表
//            //所有选中的人员数据
//            JSONArray ermanFileArray = this.getView().getFormShowParameter().getCustomParam("ermanFile");
//            for (int i = 0; i < ermanFileArray.size(); i++) {
//                JSONObject ermanFileObject = ermanFileArray.getJSONObject(i);
//                Long personId = ermanFileObject.getLong("person.id");
//                String personName = ermanFileObject.getString("person.name");
//                FileConversionImpl fileConversion = new FileConversionImpl();
//                Map<String, Object> fileConversionMap = fileConversion.fileConversion(personId, personName, selectTemplateNumber);
//                //将生成的附件地址传回父页面进行下载
//                this.getView().returnDataToParent(fileConversionMap);
//                this.getView().close();
//            }
//        } else if (StringUtils.equals("word_preview1", operateKey) ||
//                StringUtils.equals("word_preview2", operateKey) ||
//                StringUtils.equals("word_preview3", operateKey) ||
//                StringUtils.equals("word_preview4", operateKey)) {
//            //所有选中的人员数据
//            // selectTemplateNumber = "YGJL_01"; //YGJL_01:员工简历  YGLL_01:员工履历 PXJL_01:培训简历 GBRMB_01:干部任免审批表
//            JSONArray ermanFileArray = this.getView().getFormShowParameter().getCustomParam("ermanFile");
//            for (int i = 0; i < ermanFileArray.size(); i++) {
//                JSONObject ermanFileObject = ermanFileArray.getJSONObject(i);
//                Long personId = ermanFileObject.getLong("person.id");
//                String personName = ermanFileObject.getString("person.name");
//                FileConversionImpl fileConversion = new FileConversionImpl();
//                //文件信息
//                Map<String, Object> fileConversionMap = fileConversion.fileConversion(personId, personName, selectTemplateNumber);
//                //获取文件名称
//                String fileName = (String) fileConversionMap.get("name");
//                //获取文件输出流
//                ByteArrayOutputStream outStream = (ByteArrayOutputStream) fileConversionMap.get("outStream");
//                if (outStream == null) {
//                    continue;
//                }
//                FileService fileService = FileServiceFactory.getAttachmentFileService();
//                //转为输入流
//                ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
//                //租户id
//                String tenantId = RequestContext.get().getTenantId();
//                //数据中心id
//                String accountId = RequestContext.get().getAccountId();
//                //uuid
//                UUID uuid = UUID.randomUUID();
//                Date now = new Date();
//                // 创建日期格式化器
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
//                // 将日期对象格式化为字符串并打印输出
//                String formattedDate = sdf.format(now);
//                String path = fileService.upload(new FileItem(fileName, "/" + tenantId + "/" + accountId + "/" + formattedDate + "/" + "htm/" + "person" + "/" + "person" + "/ attachments/" + uuid + fileName, inStream));
//                Map<String, String> map = new HashMap<>();
//                map.put("yunHome.apiGateway.clientSecret", System.getProperty("yunHome.apiGateway.clientSecret"));
//                map.put("yunHome.serverUrl", System.getProperty("yunHome.serverUrl"));
//                String decodeUrl = null;
//                try {
//                    decodeUrl = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
//                } catch (UnsupportedEncodingException ex) {
//                    throw new RuntimeException(ex);
//                }
//                Map<String, Object> finalPreviewUrl = PreviewServiceFactory.getPreviewService(FileServiceFactory.getAttachmentFileService()).previewWPS(fileName, decodeUrl, "", map);
//                this.getView().openUrl(finalPreviewUrl.get("result").toString());
//            }
//        }
//    }
//
//    @Override
//    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
//        super.beforeDoOperation(args);
//        FormOperate operateKey = (FormOperate) args.getSource();
//        if (StringUtils.equals(WORD, operateKey.getOperateKey())||StringUtils.equals("preview", operateKey.getOperateKey())) {
//            String selectTemplateNumber = (String) this.getModel().getValue(tdkw_SELECTTEMPLATE);
//            if (StringUtils.isEmpty(selectTemplateNumber)) {
//                throw new KDBizException("请选择打印模板。");
//            }
//        }
//    }
//}
