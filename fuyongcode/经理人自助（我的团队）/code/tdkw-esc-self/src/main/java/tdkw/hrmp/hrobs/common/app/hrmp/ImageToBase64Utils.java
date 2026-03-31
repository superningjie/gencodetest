package tdkw.hrmp.hrobs.common.app.hrmp;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.net.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author xxx
 * @Date 2023/6/25 16:41
 * 通过人员id查询人员头像并转换为base64字符串
 */
public class ImageToBase64Utils {
    /**
     * 根据人员id获取文件并返回BASE64
     *
     * @param number
     * @return
     */
    public static String fileToBase64ByPersonid(Long number) throws Exception {
        QFilter lateUserFilter = new QFilter("id", QCP.equals, number);
        lateUserFilter.and("iscurrentversion", QCP.equals, "1");
        DynamicObject people = BusinessDataServiceHelper.loadSingle("hrpi_person", "headsculpture,tdkw_head_thumbnail", new QFilter[]{lateUserFilter});
        String resultDate = null;
        String address = people.getString("tdkw_head_thumbnail");
        if (StringUtils.isEmpty(address)){
            address = people.getString("headsculpture");
        }
        if (!StringUtils.isEmpty(address)) {
//            address = HRImageUrlUtil.getImageFullUrl(address);
//            File file = new File(address);
//            resultDate = "data:image/jpeg;base64," + Base64.encodeBase64String(fileToByte(file));
            byte[] bytes = urlToByte(address);
            if (bytes != null){
                resultDate = "data:image/jpeg;base64," + Base64.encodeBase64String(urlToByte(address));
            }
        }
        return resultDate;
    }

    /**
     * 根据地址获取文件并返回BASE64
     *
     * @param address
     * @return
     */
    public static String fileToBase64ByAddress(String address) throws Exception {
        String resultDate = null;
        if (!StringUtils.isEmpty(address)) {
//            address = HRImageUrlUtil.getImageFullUrl(address);
//            File file = new File(address);
//            resultDate = "data:image/jpeg;base64," + Base64.encodeBase64String(fileToByte(file));
            byte[] bytes = urlToByte(address);
            if (bytes != null){
                resultDate = "data:image/jpeg;base64," + Base64.encodeBase64String(urlToByte(address));
            }
        }
        return resultDate;
    }

    /**
     * @description 附件url转byte[]数组
     * @date 2023/5/19
     */
    static byte[] urlToByte(String path) throws Exception
    {
        try
        {
            path = URLDecoder.decode(path, "UTF-8");
            while (path.startsWith("//")) {
                path = path.replaceFirst("//", "/");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        FileService attachmentFileService = FileServiceFactory.getAttachmentFileService();
        if ((!path.contains(".")) || (!path.contains("/")) || (!attachmentFileService.exists(path))) {
            return null;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        attachmentFileService.download(path, outStream, null);
        byte[] file = outStream.toByteArray();
        return file;
    }

    /**
     * 文件File类型转byte[]
     *
     * @param file
     * @return
     */
    private static byte[] fileToByte(File file) {
        byte[] fileBytes = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileBytes;
    }
}
