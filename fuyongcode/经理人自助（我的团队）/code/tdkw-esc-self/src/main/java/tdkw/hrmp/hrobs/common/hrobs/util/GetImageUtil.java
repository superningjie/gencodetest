package tdkw.hrmp.hrobs.common.hrobs.util;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GetImagineUtil
 *
 * @author xxx
 * @date 2024/6/6
 */
public class GetImageUtil {

    private static final Log logger = LogFactory.getLog(GetImageUtil.class);


    /**
     * 根据平台人员id查询人员头像
     *
     * @param bosUserId
     * @return
     */
    public static String getUrlByCqId(Long bosUserId) {
        HashMap<Long, String> urlByCqIds = getUrlByCqIds(new ArrayList<>(Collections.singleton(bosUserId)));
        return urlByCqIds.get(bosUserId);
    }


    /**
     * 根据平台人员id查询人员头像
     *
     * @param bosUserIdList
     * @return
     */
    public static HashMap<Long, String> getUrlByCqIds(List<Long> bosUserIdList) {
        HashMap<Long, String> cqIdAndPreviewUrl = new HashMap<>();
        if (null == bosUserIdList || bosUserIdList.size() == 0) {
            logger.info("传入人员为空,直接返回!");
            return cqIdAndPreviewUrl;
        }
        /**
         * UAT前缀：https://iamplus-uat.xxx.cn/
         * 生产前缀：https://iamplus.xxx.cn/
         *
         * https://iamplus-uat.xxx.cn/lmanage/epi/portrait/preview/103795
         */
        String prefix = System.getProperty("opmc.personpicture.url.prefix");
        if (StringUtils.isBlank(prefix)) {
            prefix = "https://iamplus-uat.xxx.cn/";
        }
        logger.info("获取人员头像的url前缀为: " + prefix);
        prefix = prefix + "lmanage/epi/portrait/preview/";
        HashSet<Long> bosUserIds = new HashSet<>(bosUserIdList);
        QFilter userFilter = new QFilter("user", "in", bosUserIds);
        DynamicObject[] persons = BusinessDataServiceHelper.load("hrpi_personuserrel", "id,person,user", userFilter.toArray());
        //获取平台人员id和HR人员id的映射关系
        Map<Long, Long> userAndPersonMap = Arrays.stream(persons).collect(Collectors.toMap(i -> i.getLong("user"), i -> i.getLong("person")));
        //获取hr人员id
        List<Long> personIdList = Arrays.stream(persons).map(m -> m.getLong("person")).collect(Collectors.toList());
        QFilter personFilter = new QFilter("id", QCP.in, personIdList);
        //查询工号
        DynamicObject[] personArray = BusinessDataServiceHelper.load("hrpi_person", "id,number", personFilter.toArray());
        //HR人员和工号的映射
        Map<Long, String> personIdAndNumber = Arrays.stream(personArray).collect(Collectors.toMap(i -> i.getLong("id"), i -> i.getString("number")));
        for (Long bosUserId : bosUserIds) {
            Long personId = userAndPersonMap.getOrDefault(bosUserId, 999999999999L);
            String number = personIdAndNumber.getOrDefault(personId, "0");
            String url = prefix + number;
            logger.info("人员: " + bosUserId + "的头像预览地址为: " + url);
            cqIdAndPreviewUrl.put(bosUserId, url);
        }
        return cqIdAndPreviewUrl;
    }


    /**
     * 根据中台人员id查询人员头像
     *
     * @param personId
     * @return
     */
    public static String getUrlByHrPersonId(Long personId) {
        HashMap<Long, String> urlByHrPersonIds = getUrlByHrPersonIds(new ArrayList<>(Collections.singleton(personId)));
        return urlByHrPersonIds.get(personId);
    }

    /**
     * 根据中台人员id查询人员头像
     *
     * @param personIdList
     * @return
     */
    public static HashMap<Long, String> getUrlByHrPersonIds(List<Long> personIdList) {
        HashMap<Long, String> hrPersonIdAndPreviewUrl = new HashMap<>();
        if (null == personIdList || personIdList.size() == 0) {
            logger.info("传入人员为空,直接返回!");
            return hrPersonIdAndPreviewUrl;
        }
        /**
         * UAT前缀：https://iamplus-uat.xxx.cn/
         * 生产前缀：https://iamplus.xxx.cn/
         *
         * https://iamplus-uat.xxx.cn/lmanage/epi/portrait/preview/103795
         */
        String prefix = System.getProperty("opmc.personpicture.url.prefix");
        if (StringUtils.isBlank(prefix)) {
            prefix = "https://iamplus-uat.xxx.cn/";
        }
        logger.info("获取人员头像的url前缀为: " + prefix);
        prefix = prefix + "lmanage/epi/portrait/preview/";
        HashSet<Long> personIds = new HashSet<>(personIdList);
        QFilter personFilter = new QFilter("id", QCP.in, personIds);
        //查询工号
        DynamicObject[] personArray = BusinessDataServiceHelper.load("hrpi_person", "id,number", personFilter.toArray());
        //HR人员和工号的映射
        Map<Long, String> personIdAndNumber = Arrays.stream(personArray).collect(Collectors.toMap(i -> i.getLong("id"), i -> i.getString("number")));
        for (Long person : personIds) {
            String number = personIdAndNumber.getOrDefault(person, "0");
            String url = prefix + number;
            logger.info("人员: " + person + "的头像预览地址为: " + url);
            hrPersonIdAndPreviewUrl.put(person, url);
        }
        return hrPersonIdAndPreviewUrl;
    }

}
