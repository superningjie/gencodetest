package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.AssessObjEntityService;
import cds0.opmc.cea.common.AppflgConstant;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.TempFileCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.*;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.control.events.*;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.attachment.AttachmentFieldServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.FileNameUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static kd.hr.hbp.common.constants.HRBaseConstants.ID;


/**
 * 上传述职报告附件
 */
public class UploadAtt2BillsFormPlugin extends AbstractFormPlugin implements BeforeAttachmentUploadListener, UploadListener, AttachmentOperaClickListener {

    private static final Log log = LogFactory.getLog(UploadAtt2BillsFormPlugin.class);
    private static final AssessObjEntityService assessObjEntityService = AssessObjEntityService.getInstance();
    // 动态表单的附件面板标识
    public static final String ATTACH_KEY = "cds0_attachmentpanelap";
    private static final String NAME = "name";
    private static final String BILLNO = "billno";
    private static final String UPLOAD = "upload";
    private static final String CLEAN = "clean";

    @Override
    public void registerListener(EventObject e) {
        AttachmentPanel attachmentPanel = this.getView().getControl(ATTACH_KEY);
        //监听上传前事件
        attachmentPanel.addBeforeUploadListener(this);
        attachmentPanel.addOperaClickListener(this);
    }

    @Override
    public void beforeAttachmentUpload(BeforeAttachmentUploadEvent evt) {
        List<Map<String, Object>> sourceAttachments = evt.getSourceAttachments();
        List<Map<String, String>> assesserObjNameAndNo = this.getView().getFormShowParameter().getCustomParam("assesserObjNameAndNos");
        List<String> errorMsgs = new ArrayList<>();
        sourceAttachments.stream().forEach(sourceAttachment -> {
            //获取上传的文件名
            String fileName = (String) sourceAttachment.get(NAME);
            int fileCount = 0;
            //判断上传的附件是否属于其中一个测评对象
            for (Map<String, String> stringStringMap : assesserObjNameAndNo) {
                String objName = stringStringMap.get(NAME);
                String objBillno = stringStringMap.get(BILLNO);
                if (fileName.contains(objName + "-" + objBillno)) {
                    fileCount += 1;
                }
            }
            //如果上传的述职报告不属于任何一个对象
            if (fileCount == 0) {
                errorMsgs.add(ResManager.loadKDString("上传失败,文件：{0},不符合命名规则,未匹配到测评对象,请重新上传。", "UploadAtt2BillsFormPlugin_1", AppflgConstant.KEY_APP_NAME, fileName));
                //设置需要取消上传的附件集合
                evt.getCancelAttachments().add(sourceAttachment);
                //是否取消
                evt.setCancel(true);
            }
            if (fileCount > 1) {
                errorMsgs.add(ResManager.loadKDString("上传失败,文件：{0},不符合命名规则,未匹配到测评对象,请重新上传。", "UploadAtt2BillsFormPlugin_2", AppflgConstant.KEY_APP_NAME, fileName));
                //设置需要取消上传的附件集合
                evt.getCancelAttachments().add(sourceAttachment);
                //是否取消
                evt.setCancel(true);
            }
        });
        //组装展示报错消信息
        if (errorMsgs.size() > 0) {
            String errmsg = "";
            for (String errorMsg : errorMsgs) {
                errmsg += errorMsg + "\r\n";
            }
            evt.setMsg(ResManager.loadKDString(errmsg, "uploadReportWork_2", AppflgConstant.KEY_APP_NAME));
        }
    }

    /**
     * 校验关闭上传附件弹窗
     */
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        if (attachmentData.size() > 0) {
            e.setCancel(true);
            this.getView().showConfirm(ResManager.LoadKDString("是否放弃已上传的附件？", "UploadAtt2BillsFormPlugin_3"),
                    MessageBoxOptions.YesNo, new ConfirmCallBackListener("cancel_callback", this));
        }
    }

    /**
     * 上传附件弹窗按钮回调监控
     */
    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        switch (evt.getCallBackId()) {
            case "cancel_callback":
                if (MessageBoxResult.Yes.equals(evt.getResult())) {
                    this.cleanAllAttach();
                    this.getView().close();
                }
                break;
            case "clean_callback":
                if (MessageBoxResult.Yes.equals(evt.getResult())) {
                    this.cleanAllAttach();
                }
                break;
        }
    }

    /**
     * 清除已上传附件
     */
    private void cleanAllAttach() {
        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        for (Map<String, Object> data : attachmentData) {
            attachmentPanel.remove(data);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        if (this.hasAttachmentUploading()) {
            this.getView().showTipNotification(ResManager.LoadKDString("附件上传中，请稍后再试。", "UploadAtt2BillsFormPlugin_4"));
            return;
        }
        String operateKey = args.getOperateKey();
        //”上传“ 按钮
        if (UPLOAD.equals(operateKey)) {
            List<DynamicObject> timeOutAttList = AttachmentFieldServiceHelper.getTimeOutAttList(this.getView().getPageId());
            if (!CollectionUtils.isEmpty(timeOutAttList)) {
                StringBuilder timeoutMessage = new StringBuilder(ResManager.LoadKDString("临时附件已超时，请重新上传以下文件：\r\n", "UploadAtt2BillsFormPlugin_5"));
                for (DynamicObject attDynamicObj : timeOutAttList) {
                    timeoutMessage.append(attDynamicObj.getLocaleString(NAME).getLocaleValue()).append("\r\n");
                }
                this.getView().showConfirm(timeoutMessage.toString(), MessageBoxOptions.OK);
                return;
            }
            this.doUpload();
        } else if (CLEAN.equals(operateKey)) {  //清空按钮
            AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
            List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
            if (attachmentData.size() > 0) {
                this.getView().showConfirm(ResManager.LoadKDString("是否清空当前附件面板？", "UploadAtt2BillsFormPlugin_6"),
                        MessageBoxOptions.YesNo, new ConfirmCallBackListener("clean_callback", this));
            } else {
                this.getView().showTipNotification(ResManager.LoadKDString("附件面板已清空！", "UploadAtt2BillsFormPlugin_7"));
            }
        }
    }

    /**
     * 判断附件是否正在上传
     */
    private boolean hasAttachmentUploading() {
        IPageCache cache = this.getView().getService(IPageCache.class);
        String uploadingAttJson = cache.get("UploadingAtt" + this.getView().getPageId());
        return StringUtils.isNotBlank(uploadingAttJson);
    }

    /**
     * 上传附件
     */
    private void doUpload() {
        FormShowParameter showParameter = this.getView().getFormShowParameter();
        Map<String, Object> params = showParameter.getCustomParams();
        Map<String, Map<String, String>> assesserObjs = (Map<String, Map<String, String>>) params.get("assesserObjs");

        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        //将上传的附件与测评对象绑定
        for (Map<String, Object> attachmentDatum : attachmentData) {
            String name = (String) attachmentDatum.get(NAME);
            assesserObjs.forEach((assessObjId, nameAndNoMap) -> {
                if (name.contains(nameAndNoMap.get(NAME)) && name.contains(nameAndNoMap.get(BILLNO))) {
                    attachmentDatum.put(ID, assessObjId);
                }
            });
        }
        //校验文件是否上传
        if (attachmentData.size() == 0) {
            this.getView().showTipNotification(ResManager.LoadKDString("请先上传文件。", "UploadAtt2BillsFormPlugin_8"));
            return;
        }

        String formData = (String) params.get("formData");
        if (StringUtils.isBlank(formData)) {
            //表单直接预览
            this.getView().showTipNotification("formData is blank !");
            return;
        }
        List<Map<String, Object>> formDataList = SerializationUtils.fromJsonString(formData, List.class);
        List<Map<String, Object>> formDataErr = new ArrayList<>();
        boolean hasAttachmentDataUploadedErr = false, hasFormDataErr = false;

        ORM orm = ORM.create();
        List<DynamicObject> attBillRelList = new ArrayList<>();
        List<Map<String, Object>> attachmentDataUploaded = new ArrayList<>();
        //将需要保存的数据提取出来统一进行数据库操作，避免在循环中执行数据库操作
        //需要保存的测评对象的附件信息
        List<DynamicObject> assesserObjAttachmentInfo = new ArrayList<>();
        //需要保存的附件信息
        List<DynamicObject> attachmentInfos = new ArrayList<>();

        for (Map<String, Object> attachment : attachmentData) {
            // 临时文件持久化
            String path = this.uploadFileServer(attachment);
            if (StringUtils.isBlank(path)) {
                hasAttachmentDataUploadedErr = true;
                continue;
            }
            Map<String, Object> map = new HashMap<>(attachment);
            map.put("url", path);
            attachmentDataUploaded.add(map);
            attBillRelList.clear();
            // 保存所有单据附件关联，必要：entityNumber，billId，attKey
            for (Map<String, Object> data : formDataList) {
                if (data.get("billId").toString().equals(attachment.get(ID))) {
                    String entityNumber = (String) data.get("entityNumber"); // 单据实体编码
                    Integer rowKey = (Integer) data.get("rowKey"); // 列表行序号
                    Object billId = data.get("billId"); // 单据主键
                    String billNo = (String) data.get(BILLNO); // 单据编码
                    String attKey = (String) data.get("attKey"); // 单据面板标识，eg."attachmentpanel"
                    try {
                        attBillRelList.addAll(this.genAttachmentRel(entityNumber, billId + "", attKey, attachmentDataUploaded, orm));
                    } catch (Exception e) {
                        hasFormDataErr = true;
                        log.error(String.format("genAttachmentRel[rowKey:%s, entityNumber:%s, billId:%s, billNo:%s], err: %s",
                                entityNumber, rowKey, billId, billNo, e.getMessage()));
                        formDataErr.add(data);
                    }
                }
            }
            if (formDataErr.isEmpty()) {
                // 删除面板附件及临时文件
                attachmentPanel.remove(attachment);
            } else {
                List<Object> billNos = formDataErr.stream().map(e -> e.get(BILLNO)).collect(Collectors.toList());
                log.error(String.format("附件[%s]，%s条单据绑定附件数据失败：\r\n%s", attachment, billNos.size(), billNos));
                formDataErr.clear();
            }
            attachmentDataUploaded.clear();
            //将要保存的附件信息放入列表里统一处理
            attachmentInfos.addAll(attBillRelList);
            //这里要把附件id存到测评对象里
            List<Long> assessObjIds = new ArrayList<>();
            assesserObjs.forEach((id, nameAndno) -> {
                assessObjIds.add(Long.parseLong(id));
            });
            //查出上传述职报告的测评对象信息
            DynamicObject[] assesserObjInfos = assessObjEntityService.queryAssesserInfoByIds(assessObjIds);
            Arrays.stream(assesserObjInfos).forEach(assesserObjInfo -> {
                if (attachment.get(NAME).toString().contains(assesserObjInfo.getString("perffile.name"))
                        && attachment.get(NAME).toString().contains(assesserObjInfo.getString("perffile.billno"))) {
                    assesserObjInfo.set(ID, assesserObjInfo.getLong(ID));
                    assesserObjInfo.set("reportwork", attBillRelList.get(0).get(ID));
                }
            });
            assesserObjAttachmentInfo.addAll(Arrays.stream(assesserObjInfos).collect(Collectors.toList()));
        }
        //统一入库到附件服务器表
        DynamicObject[] bosAttachments = attachmentInfos.toArray(new DynamicObject[0]);
        SaveServiceHelper.save(bosAttachments);
        //统一保存测评对象附件信息
        DynamicObject[] assesserObjAttachmentInfos = assesserObjAttachmentInfo.toArray(assesserObjAttachmentInfo.toArray(new DynamicObject[0]));
        assessObjEntityService.save(assesserObjAttachmentInfos);

        if (!hasAttachmentDataUploadedErr && !hasFormDataErr) {
            this.getView().showConfirm(ResManager.LoadKDString("上传成功！", "UploadAtt2BillsFormPlugin_9"),
                    MessageBoxOptions.OK, null);
            this.getView().returnDataToParent("refresh");
            this.getView().close();
        } else if (hasAttachmentDataUploadedErr && !hasFormDataErr) {
            this.getView().showTipNotification(ResManager.LoadKDString("以下附件上传失败，请重新上传文件！", "UploadAtt2BillsFormPlugin_10"));
        } else {
            this.getView().showErrorNotification(ResManager.LoadKDString("附件上传出现未知异常，请联系管理员查询日志分析！", "UploadAtt2BillsFormPlugin_11"));
        }
    }

    /**
     * 保存临时文件到文件服务器进行持久化
     *
     * @param attDataItem 已上传的附件临时文件Map信息
     * @return 上传文件服务器返回url
     * @see AttachmentServiceHelper#saveTempToFileService
     */
    private String uploadFileServer(Map<String, Object> attDataItem) {
        try {
            FileService fs = FileServiceFactory.getAttachmentFileService();
            RequestContext requestContext = RequestContext.get();
            TempFileCache fileCache = CacheFactory.getCommonCacheFactory().getTempFileCache();
            String filename = (String) attDataItem.get(NAME);
            String tempUrl = (String) attDataItem.get("url");
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String filepath = FileNameUtils.getAttachmentFileName( // 此处 attachmentpanel 用于文件路径的唯一标识
                    requestContext.getTenantId(), requestContext.getAccountId(), "attachmentpanel", uuid + "/" + filename);
            InputStream inputStream = fileCache.getInputStream(tempUrl);
            FileItem item = new FileItem(filename, filepath, inputStream);
            String[] splits = filename.trim().split("\\.");
            String fileType = splits[splits.length - 1];
            long compressPicSize = 0L;
            int fileSize = inputStream.available() / 1024;
            if ("jpg,jpeg,png,gif,bmp,tiff,tga,ico,dib,rle,emf,jpe,jfif,pcx,dcx,pic,tif,wmf".contains(fileType.toLowerCase())) {
                compressPicSize = AttachmentServiceHelper.getCompressPicSize();
            }
            if (compressPicSize != 0L && fileSize > compressPicSize) {
                return fs.compressPicUpload(item, compressPicSize);
            } else {
                return fs.upload(item);
            }
        } catch (Exception e) {
            log.error("uploadFileServer err: " + e.getMessage());
            return null;
        }
    }

    /**
     * 绑定附件到单据的附件面板
     *
     * @param entityNumber 实体编码
     * @param billPkId     单据主键
     * @param attList      附件信息
     * @param orm          orm实例
     * @return 附件面板实体数据，用于入库记录
     * @see AttachmentServiceHelper#upload
     */
    private DynamicObjectCollection genAttachmentRel(String entityNumber, String billPkId, String attachKey, List<Map<String, Object>> attList, ORM orm) {
        DynamicObjectType entityType = (DynamicObjectType) orm.getDataEntityType("bos_attachment");
        DynamicObjectCollection dynColl = new DynamicObjectCollection(entityType, null);
        if (attList == null || attList.size() == 0) {
            return dynColl;
        }
        long[] ids = orm.genLongIds(entityType, attList.size());
        Date today = new Date();
        for (int i = 0; i < attList.size(); i++) {
            Map<String, Object> attach = attList.get(i);
            DynamicObject dynamicObject = new DynamicObject(entityType);
            dynamicObject.set(ID, ids[i]);
            dynamicObject.set("FNUMBER", attach.get("uid"));
            dynamicObject.set("FBillType", entityNumber);
            dynamicObject.set("FInterID", billPkId);
            Object lastModified = attach.get("lastModified");
            if (lastModified instanceof Date) {
                dynamicObject.set("FModifyTime", lastModified);
            } else if (lastModified instanceof Long) {
                dynamicObject.set("FModifyTime", new Date((Long) lastModified));
            } else {
                dynamicObject.set("FModifyTime", today);
            }
            dynamicObject.set("fcreatetime", attach.getOrDefault("uploadTime", today));
            String name = (String) attach.get(NAME);
            dynamicObject.set("FaliasFileName", name);
            dynamicObject.set("FAttachmentName", name);
            String extName = name != null ? name.substring(name.lastIndexOf(46) + 1) : "";
            dynamicObject.set("FExtName", extName);
            long compressPicSize = AttachmentServiceHelper.getCompressPicSize();
            if ("jpg,jpeg,png,gif,bmp,tiff,tga,ico,dib,rle,emf,jpe,jfif,pcx,dcx,pic,tif,wmf".contains(extName.toLowerCase()) && compressPicSize != 0L && Long.parseLong(attach.get("size").toString()) > compressPicSize * 1024L) {
                dynamicObject.set("FATTACHMENTSIZE", compressPicSize * 1024L);
            } else {
                dynamicObject.set("FATTACHMENTSIZE", attach.get("size"));
            }
            dynamicObject.set("FFileId", attach.get("url"));
            dynamicObject.set("FCREATEMEN", RequestContext.get().getCurrUserId());
            dynamicObject.set("fattachmentpanel", attachKey);
            dynamicObject.set("filesource", attach.get("filesource"));
            if (attach.containsKey("description")) {
                dynamicObject.set("fdescription", attach.get("description"));
            }
            dynColl.add(dynamicObject);
        }
        return dynColl;
    }

}
