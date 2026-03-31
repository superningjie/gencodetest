package cds0.opmc.cea.plugin.form;

import cds0.opmc.cea.business.entityservice.DimassesserEntityService;
import cds0.opmc.cea.common.AppflgConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import kd.bos.algo.util.ZipUtil;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.TempFileCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.fileservice.BatchDownloadRequest;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.IPageCache;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.url.UrlService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CheckWorkReportPlugin extends AbstractFormPlugin {
    private final static String KEY_ENTRYENTITY = "cds0_entryentity11";
    private static final DimassesserEntityService dimassesserEntityService = DimassesserEntityService.getInstance();
    @Override
    public void createNewData(BizDataEventArgs e) {
        IPageCache iPageCache = this.getView().getParentView().getService(IPageCache.class);
        List<Long> dimAssessorIds = JSON.parseObject(iPageCache.get("dimAssessorList"), new TypeReference<List<Long>>() {});
        DynamicObject[] assObjScoItemInstList = dimassesserEntityService.query("assobj.person,assobj.person.number,assobj.person.name,assobj.reportwork.id", new QFilter[]{
                new QFilter("id", QCP.in, dimAssessorIds)
        }, "id desc");
        DynamicObject dataEntity = new DynamicObject(this.getModel().getDataEntityType());
        DynamicObjectCollection rows = dataEntity.getDynamicObjectCollection(KEY_ENTRYENTITY);
        for (DynamicObject assObjScoItemInst : assObjScoItemInstList) {
            DynamicObject newRow = new DynamicObject(rows.getDynamicObjectType());
            newRow.set("cds0_textfield31", assObjScoItemInst.get("assobj.person.name"));
            newRow.set("cds0_textfield111", assObjScoItemInst.get("assobj.person.number"));
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load("bos_attachment", "id,fattachmentname,url",
                    new QFilter[]{new QFilter("id", QCP.equals, assObjScoItemInst.get("assobj.reportwork.id"))});
            if (dynamicObjects.length != 0) {
                newRow.set("cds0_enclosure", dynamicObjects[0]);
            }
            rows.add(newRow);
        }
        e.setDataEntity(dataEntity);
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if (args.getOperateKey().contains("donothing")) {
            // 获取选中的行
            EntryGrid entry = this.getControl("cds0_entryentity11");
            int[] selectRows = entry.getSelectRows();
            if (selectRows != null && selectRows.length > 0) {
                int selectRow = selectRows[0];
                DynamicObject dynamicObject = this.getModel().getEntryEntity("cds0_entryentity11").get(selectRow);
                String url = dynamicObject.getString("cds0_enclosure.ffileid");
                if (url != null && !url.equals("")) {
                    if (args.getOperateKey().equals("donothing1")) {
                        this.getView().openUrl(UrlService.getAttachmentPreviewUrl(url));
                    } else if (args.getOperateKey().equals("donothing2")) {
                        this.getView().download(UrlService.getAttachmentDownloadUrl(url));
                    }
                }
            }
        }
    }

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addClickListeners("cds0_buttonap1");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control)evt.getSource();
        if (source.getKey().equals("cds0_buttonap1")){
            DynamicObjectCollection dynamicObjects = this.getModel().getEntryEntity("cds0_entryentity11");
            List<Long> attachmentIds = new ArrayList<>();
            dynamicObjects.forEach(dynamicObject -> attachmentIds.add(dynamicObject.getLong("cds0_enclosure_id")));
            getView().download(downloadByAttachmentIds(null, attachmentIds));
        }
    }

    public static String download(String fileName, InputStream in) {
        TempFileCache cache = CacheFactory.getCommonCacheFactory().getTempFileCache();
        return cache.saveAsUrl(fileName, in, 2 * 60);
    }

    public static String downloadByFilePath(String fileName, String filePath) {
        return download(fileName, new File(filePath));
    }

    public static String download(String fileName, File file) {
        if (!file.exists()) {
            return null;
        }
        if (fileName == null) {
            fileName = file.getName();
        }
        if (file.isDirectory()) {
            fileName = fileName.endsWith(".zip") ? fileName : fileName + ".zip";
            file = byte2File(ZipUtil.zip(file2byte(file)), file.getPath(), fileName);
        }
        try {
            InputStream in = new FileInputStream(file);
            return download(fileName, in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] file2byte(File file){
        byte[] buffer = null;
        try{
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return buffer;
    }

    public static File byte2File(byte[] buf, String filePath, String fileName){
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try{
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()){
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            if (bos != null){
                try{
                    bos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (fos != null){
                try{
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return file;
    }



    public static String downloadByAttachmentIds(String fileName, List<Long> attachmentIds) {
        QFilter qFilter = new QFilter("id", QCP.in, attachmentIds);
        DynamicObject[] attachments = BusinessDataServiceHelper.load("bos_attachment", "id,fattachmentname,url",
                new QFilter[]{qFilter});
        String[] fileNames = Arrays.stream(attachments).map(attachment -> attachment.getString("fattachmentname"))
                .toArray(String[]::new);
        String[] filePaths = Arrays.stream(attachments).map(attachment -> attachment.getString("ffileid"))
                .toArray(String[]::new);
        return downloadByAttachmentUrls(fileName, fileNames, filePaths);
    }

    public static String downloadByAttachmentUrls(String fileName, String[] fileNames, String... attachmentUrls) {
        if (attachmentUrls == null || attachmentUrls.length == 0) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (attachmentUrls.length > 1) {
            fileName = fileName == null ? ResManager.loadKDString("述职报告", "CheckWorkReportPlugin_0", AppflgConstant.KEY_APP_NAME) + df.format(new Date()) +  ".zip" : !fileName.toLowerCase().endsWith(".zip") ? fileName + ".zip" : fileName;
        } else if (fileName == null) {
            fileName = fileNames[0];
        }
        FileService fs = FileServiceFactory.getAttachmentFileService();
        // 模拟浏览器userAgent访问
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (attachmentUrls.length > 1) {
                // 构造BatchDownloadRequest对象
                BatchDownloadRequest bdr = getBatchDownloadRequest(fileNames, attachmentUrls);
                fs.batchDownload(bdr, out, userAgent);
            } else {
                String attachmentUrl = attachmentUrls[0];
                fs.download(attachmentUrl, out, userAgent);
            }
            out.flush();
            return download(fileName, out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String download(String fileName, byte[] bytes) {
        InputStream in = new ByteArrayInputStream(bytes);
        return download(fileName, in);
    }

    private static BatchDownloadRequest getBatchDownloadRequest(String[] fileNames, String[] attachmentUrls) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BatchDownloadRequest.Dir srcDir = new BatchDownloadRequest.Dir(ResManager.loadKDString("述职报告", "CheckWorkReportPlugin_0", AppflgConstant.KEY_APP_NAME) + df.format(new Date()));
        List<BatchDownloadRequest.File> srcFiles = new ArrayList<>();
        int index = 0;
        for (String attachmentUrl : attachmentUrls) {
            String attachmentName = fileNames[index++];
            //加密路径获取
//            FileServiceFactory.getAttachmentFileService().getFileServiceExt().getRealPath(url);
            srcFiles.add(new BatchDownloadRequest.File(attachmentName, attachmentUrl));
        }
        srcDir.setFiles(srcFiles.toArray(new BatchDownloadRequest.File[0]));
        BatchDownloadRequest bdr = new BatchDownloadRequest("test-batch-download");
        bdr.setDirs(new BatchDownloadRequest.Dir[]{srcDir});
        return bdr;
    }
}
