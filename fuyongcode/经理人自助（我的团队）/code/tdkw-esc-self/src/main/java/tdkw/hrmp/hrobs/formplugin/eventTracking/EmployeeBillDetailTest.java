//package tdkw.hrmp.hrobs.formplugin.eventTracking;
//
//import kd.bos.dataentity.entity.DynamicObject;
//import kd.bos.dataentity.entity.DynamicObjectCollection;
//import kd.bos.logging.Log;
//import kd.bos.logging.LogFactory;
//import kd.bos.orm.query.QCP;
//import kd.bos.orm.query.QFilter;
//import kd.bos.servicehelper.BusinessDataServiceHelper;
//import kd.hr.hbp.business.servicehelper.HRBaseServiceHelper;
//import kd.hr.hbp.common.constants.HRBaseConstants;
//import kd.sdk.hr.hspm.common.constants.HspmCommonConstants;
//import kd.sdk.hr.hspm.common.ext.file.EmployeeBillParamsDTO;
//import kd.sdk.hr.hspm.common.ext.file.EmployeeChangeRecordFieldDTO;
//import kd.sdk.hr.hspm.formplugin.web.file.ermanfile.ext.service.employee.IEmployeeFilePluginService;
//import org.apache.commons.lang3.StringUtils;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.regex.Pattern;
//
///**
// * EmployeeBillDetailTest
// *
// * @author xxx
// * @date 2023/9/18
// */
//public class EmployeeBillDetailTest implements IEmployeeFilePluginService {
//    private static final Log logger = LogFactory.getLog(EmployeeBillDetailTest.class);
//
//
//    /**
//     * 变更单据审批界面某字段的值显示，适用PC端和移动端的单据界面
//     *
//     * @param dto
//     */
//    @Override
//    public void modifyEmployeeBillDetail(EmployeeBillParamsDTO dto) {
//        logger.info("触发了modifyEmployeeBillDetail埋点");
//        logger.info("dto.getFirstGroupNum():" + dto.getFirstGroupNum());
//        logger.info("dto.getEntityName():" + dto.getEntityName());
//        logger.info("dto.getField():" + dto.getField());
//        logger.info("dto.getFirstGroupNum()" + dto.getFirstGroupNum());
//        logger.info("dto.getEntityName()" + dto.getEntityName());
//        logger.info("dto.getField()" + dto.getField());
//
//        logger.info("dto.getEntryId()" + dto.getEntryId());
//        logger.info("dto.getValue()" + dto.getValue());
//        logger.info("dto.getBefore()" + dto.getBefore());
//        logger.info("dto.getAfter()" + dto.getAfter());
//        logger.info("dto.getDataId()" + dto.getDataId());
//        logger.info("dto.getEntryId()" + dto.getEntryId());
//
//
//        // 信息组编码 实体编码 字段编码
//        if (dto.getFirstGroupNum().equals("1460_S") && dto.getEntityName().equals("hrpi_familymemb")
//                && dto.getField().equals("tdkw_obtainyear")) {
//            // 定位具体信息组的具体实体下的某字段`
//            // 当前数据id
//            long dataId = dto.getDataId();
//            logger.info("dataId为:" + dataId);
//            // 变更前或变更后的显示值
//            String value = dto.getValue();
//            logger.info("value为:" + value);
//            // PC端单据设置这两个变量
//            String before = dto.getBefore();
//            String after = dto.getAfter();
//            final String REGEX = "\\d{4}-\\d{2}-\\d{2}$";
//            if (StringUtils.isNotBlank(before)) {
//                if (Pattern.matches(REGEX, before)) {
//                    // 根据业务自定义转换before/after的值
//                    List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                    if (!invalidValues.contains(before)) {
//                        dto.setBefore(handleDate(before));
//                        logger.info("handleDate(before):" + handleDate(before));
//                    }
//                }
//            }
//            if (StringUtils.isNotBlank(after)) {
//                if (Pattern.matches(REGEX, after)) {
//                    List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                    if (!invalidValues.contains(after)) {
//                        dto.setAfter(handleDate(after));
//                        logger.info("handleDate(after):" + handleDate(after));
//                    }
//                }
//            }
//            logger.info("before为:" + before);
//            logger.info("after为:" + after);
//
//            //移动端
//
//
//            // tips:注意一下空指针，移动端的value PC端的before、after均可能为空
//        } else if (dto.getFirstGroupNum().equals("1620_S") && dto.getEntityName().equals("hrpi_pernontsprop")
//                && dto.getField().equals("tdkw_administrative")) {
//            // 定位具体信息组的具体实体下的某字段
//            // 当前数据id
//            long dataId = dto.getDataId();
//            logger.info("dataId为:" + dataId);
//            // 变更前或变更后的显示值
//            String value = dto.getValue();
//            logger.info("value为:" + value);
//            // PC端单据设置这两个变量
//            String before = dto.getBefore();
//            String after = dto.getAfter();
//            logger.info("before为:" + before);
//            logger.info("after为:" + after);
//            // 根据业务自定义转换before/after的值
//            List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//            if (!invalidValues.contains(before)) {
//                dto.setBefore(before);
//                logger.info("beforeAdmindivision:" + before);
//            }
//            if (!invalidValues.contains(after)) {
//                dto.setAfter(after);
//                logger.info("afterAdmindivision:" + after);
//            }
//            if (!invalidValues.contains(value)) {
//                dto.setValue(value);
//                logger.info("appAfterAdmindivision:" + value);
//            }
//        } else if (dto.getFirstGroupNum().equals("1480_S") && dto.getEntityName().equals("hrpi_peraddress")
//                && dto.getField().equals("tdkw_administrative")) {
//            // 定位具体信息组的具体实体下的某字段
//            // 当前数据id
//            long dataId = dto.getDataId();
//            logger.info("dataId为:" + dataId);
//            // 变更前或变更后的显示值
//            String value = dto.getValue();
//            logger.info("value为:" + value);
//            // PC端单据设置这两个变量
//            String before = dto.getBefore();
//            String after = dto.getAfter();
//            logger.info("before为:" + before);
//            logger.info("after为:" + after);
//            // 根据业务自定义转换before/after的值
//            List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//            if (!invalidValues.contains(before)) {
//                dto.setBefore(before);
//                logger.info("beforeAdmindivision:" + before);
//            }
//            if (!invalidValues.contains(after)) {
////                DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(after, "bd_admindivision");
//                dto.setAfter(after);
//                logger.info("afterAdmindivision:" + after);
//            }
//            if (!invalidValues.contains(value)) {
//                dto.setValue(value);
//                logger.info("appAfterAdmindivision:" + value);
//            }
//        }
//        //籍贯
//        else if ((dto.getFirstGroupNum().equals("1620_S") || dto.getFirstGroupNum().equals("hrpi_pernontsprop")) && dto.getEntityName().equals("hrpi_pernontsprop")
//                && dto.getField().equals("tdkw_origin")) {
//
//            // 根据业务自定义转换before/after的值
//            long entryId = dto.getEntryId();
//            HRBaseServiceHelper helper = new HRBaseServiceHelper(HspmCommonConstants.HSPM_INFOAPPROVAL);
//            QFilter qFilter = new QFilter("entryentity.id", QCP.equals, entryId);
//            DynamicObject single = helper.loadDynamicObject(new QFilter[]{qFilter});
//            if (single != null) {
//                // 根据entryId找到对应的分录行
//                DynamicObjectCollection entryEntity = single.getDynamicObjectCollection("entryentity");
//                for (DynamicObject dynamicObject : entryEntity) {
//                    long id = dynamicObject.getLong("id");
//
//                    if (id == entryId) {
//                        logger.info("分录ID" + id);
//                        logger.info("匹配成功");
//                        // 变更前后原始值
//                        String newValue = dynamicObject.getString("newvalue");
//                        logger.info("籍贯变更前：" + newValue);
//                        String oldValue = dynamicObject.getString("oldvalue");
//                        logger.info("籍贯变更后：" + oldValue);
//
//                        try {
//                            if (StringUtils.isNotEmpty(oldValue)) {
//                                //由于取不到变更前的数据id，所以只能用名称去匹配
//                                DynamicObject adminDivision = BusinessDataServiceHelper.loadSingle(oldValue, "tdkw_hbss_admindivision");
//                                dto.setBefore(adminDivision.getString("tdkw_address"));
//                            }
//                            if (StringUtils.isNotEmpty(newValue)) {
//                                //由于取不到变更前的数据id，所以只能用名称去匹配
//                                DynamicObject adminDivision = BusinessDataServiceHelper.loadSingle(newValue, "tdkw_hbss_admindivision");
//                                dto.setAfter(adminDivision.getString("tdkw_address"));
//                            }
//                        } catch (Exception e) {
//                            logger.error("籍贯变更埋点错误" + e.getMessage(), e);
//                        }
//                    }
//                }
//
//                // 当前数据id
//                long dataId = dto.getDataId();
//                logger.info("dataId为:" + dataId);
//                // 变更前或变更后的显示值
//                String value = dto.getValue();
//                logger.info("value为:" + value);
//                // PC端单据设置这两个变量
//                String before = dto.getBefore();
//                String after = dto.getAfter();
//                logger.info("before为:" + before);
//                logger.info("after为:" + after);
//
//                List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                if (!invalidValues.contains(value)) {
//                    //由于取不到变更前的数据id，所以只能用名称去匹配
//                    DynamicObject adminDivision = BusinessDataServiceHelper.loadSingle(value, "tdkw_hbss_admindivision");
//                    logger.info("移动端数据查询：" + value);
//                    logger.info("实体是否为空：" + (null == adminDivision));
//                    dto.setValue(adminDivision.getString("tdkw_address"));
//                    logger.info("appAfterAdmindivision:" + adminDivision.getString("tdkw_address"));
//                }
//            }
//        }
//    }
//
//
//    private String handleDate(String before) {
//        final String REGEX = "\\d{4}-\\d{2}-\\d{2}$";
//        Date parse = null;
//        if (StringUtils.isNotBlank(before)) {
//            if (Pattern.matches(REGEX, before)) {
//                try {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    parse = sdf.parse(before);
//                } catch (ParseException e) {
//                    logger.error("日期解析错误" + before);
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
//        return sdfYear.format(parse);
//
//
//    }
//
//    /**
//     * 变更修改记录里某字段的值显示，适用PC端和移动端的单据界面
//     *
//     * @param dto
//     */
//    @Override
//    public void modifyChangeRecordDetail(EmployeeChangeRecordFieldDTO dto) {
//        logger.info("触发了modifyChangeRecordDetail埋点");
//        logger.info("dto.getAfter().getDataEntityType().getName():" + dto.getAfter().getDataEntityType().getName());
//        logger.info("dto.getField():" + dto.getField());
//        // logger.info("dto.getField():" + dto.getField());
//        // 实体编码
//        if (dto.getAfter().getDataEntityType().getName().equals("hrpi_familymemb")) {
//            // 变更前后的数据id
//            long beforeId = dto.getBefore().getLong(HRBaseConstants.ID);
//            logger.info("beforeId为:" + beforeId);
//            long afterId = dto.getAfter().getLong(HRBaseConstants.ID);
//            logger.info("afterId为:" + afterId);
//            // 设置字段
//            if (dto.getField().equals("tdkw_obtainyear")) {
//                String before = dto.getBeforeValue();
//                String after = dto.getAfterValue();
//                // 设置要显示的值
//                logger.info("before为:" + before);
//                logger.info("after为:" + after);
//                final String REGEX = "\\d{4}-\\d{2}-\\d{2}$";
//                if (StringUtils.isNotBlank(before)) {
//                    if (Pattern.matches(REGEX, before)) {
//                        // 根据业务自定义转换before/after的值
//                        List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                        if (!invalidValues.contains(before)) {
//                            dto.setBeforeValue(handleDate(before));
//                            logger.info("handleDate(before):" + handleDate(before));
//                        }
//                    }
//                    // dto.setBeforeValue("前前:" + dto.getBeforeValue());
//                    // logger.info("dto.getBeforeValue()为:" + dto.getBeforeValue());
//                    // dto.setAfterValue("吼吼" + dto.getAfterValue());
//                    // logger.info("dto.getAfterValue()为:" + dto.getAfterValue());
//                    // tips:注意一下空指针，移动端的value PC端的before、after均可能为空
//                }
//                if (StringUtils.isNotBlank(after)) {
//                    if (Pattern.matches(REGEX, after)) {
//                        List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                        if (!invalidValues.contains(after)) {
//                            dto.setAfterValue(handleDate(after));
//                            logger.info("handleDate(after):" + handleDate(after));
//                        }
//                    }
//                }
//            }
//        } else if (dto.getAfter().getDataEntityType().getName().equals("hrpi_pernontsprop") || dto.getAfter().getDataEntityType().getName().equals("hrpi_peraddress")) {
//            // 变更前后的数据id
//            long beforeId = dto.getBefore().getLong(HRBaseConstants.ID);
//            logger.info("beforeId为:" + beforeId);
//            long afterId = dto.getAfter().getLong(HRBaseConstants.ID);
//            logger.info("afterId为:" + afterId);
//
//            // 设置字段
//            if (dto.getField().equals("tdkw_administrative")) {
//                String before = dto.getBeforeValue();
//                String after = dto.getAfterValue();
//                // 设置要显示的值
//                logger.info("before为:" + before);
//                logger.info("after为:" + after);
//                // 根据业务自定义转换before/after的值
//                List<Object> invalidValues = Arrays.asList("-", "0", 0, "", "无", null);
//                if (!invalidValues.contains(before)) {
////                    DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(before, "bd_admindivision");
//                    dto.setBeforeValue(before);
//                    logger.info("beforeAdmindivisionId:" + before + ":  " + beforeId);
//
//                }
//                if (!invalidValues.contains(after)) {
////                    DynamicObject admindivision = BusinessDataServiceHelper.loadSingle(after, "bd_admindivision");
//                    dto.setAfterValue(after);
//                    logger.info("afterAdmindivisionId:" + after + ":  " + afterId);
//
//                }
//            }
//            // 籍贯
//            else if (dto.getField().equals("tdkw_origin")) {
//                String before = dto.getBeforeValue();
//                String after = dto.getAfterValue();
//                // 设置要显示的值
//                logger.info("before为:" + before);
//                logger.info("after为:" + after);
//
//                // 根据业务自定义转换before/after的值
//                if (beforeId != 0L) {
//                    //人员非时序性属性
//                    DynamicObject nonTemporalProperties = BusinessDataServiceHelper.loadSingle(beforeId, "hrpi_pernontsprop");
//                    if (nonTemporalProperties != null) {
//                        //籍贯
//                        DynamicObject oriGin = nonTemporalProperties.getDynamicObject("tdkw_origin");
//                        if (null != oriGin) {
//                            oriGin = BusinessDataServiceHelper.loadSingle(oriGin.getPkValue(), "tdkw_hbss_admindivision");
//                            //详细地址
//                            dto.setBeforeValue(oriGin.getString("tdkw_address"));
//                        }
//                    }
//                }
//                if (afterId != 0L) {
//                    //人员非时序性属性
//                    DynamicObject nonTemporalProperties = BusinessDataServiceHelper.loadSingle(afterId, "hrpi_pernontsprop");
//                    //籍贯
//                    if (nonTemporalProperties != null) {
//                        DynamicObject oriGin = nonTemporalProperties.getDynamicObject("tdkw_origin");
//                        if (null != oriGin) {
//                            oriGin = BusinessDataServiceHelper.loadSingle(oriGin.getPkValue(), "tdkw_hbss_admindivision");
//
//                            //详细地址
//                            dto.setAfterValue(oriGin.getString("tdkw_address"));
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
