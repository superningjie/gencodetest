package tdkw.hrmp.hrobs.formplugin.salary;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.IPageCache;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.DispatchServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.url.UrlService;
import kd.sdk.swc.hspp.mservice.helper.SalarySlipServiceHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * @description: PC端工资条帮助类
 */


public class PCSalarySlipHelper{

        /**
         * 校验类型——仅新密码
         */
        public final static String FIELD_VERIFY_TYPE_NEW_PASSWORD = "1";

        /**
         * 校验类型——仅确认密码
         */
        public final static String FIELD_VERIFY_TYPE_VERIFY_PASSWORD = "2";

        /**
         * 校验类型——全校验
         */
        public final static String FIELD_VERIFY_TYPE_ALL = "3";

        /**
         * 日志
         */
        private static final Log logger = LogFactory.getLog(PCSalarySlipHelper.class);

        /**
         * 获取中台人员ID
         * @return
         */
        public static Long getHrPersonId() {
            Map<String, Object> map = DispatchServiceHelper.invokeBizService("hrmp", "hrpi", "IHRPIPersonService", "getPersonModelId", new Object[0]);
            Long personId;
            if (map != null && (Boolean)map.get("success") && map.get("data") != null) {
                personId = (Long)((Map)map.get("data")).getOrDefault("person", 0L);
            } else {
                personId = 0L;
            }
            return personId;
        }

        /**
         * 获取中台人员ID（缓存）
         * @param pageCache
         * @return
         */
        public static Long getHrPersonId(IPageCache pageCache) {
            String hrPersonIdStr = pageCache.get("personId");
            Long hrPersonId;
            if (StringUtils.isEmpty(hrPersonIdStr)) {
                hrPersonId = getHrPersonId();
                pageCache.put("personId", String.valueOf(hrPersonId));
            } else {
                hrPersonId = Long.valueOf(hrPersonIdStr);
            }

            return hrPersonId;
        }

        /**
         * 根据中台人员ID获取头像路径
         * @param hrPersonId 中台人员ID
         * @return
         */
        public static String getHeadSculptureUrl(Long hrPersonId) {
            if (hrPersonId == null) {
                return null;
            }
            DynamicObject hrPerson = QueryServiceHelper.queryOne("hrpi_person","headsculpture", new QFilter[] { new QFilter("id", QFilter.equals, hrPersonId) });
            return hrPerson == null ? null : getImageFullUrl(hrPerson.getString("headsculpture"));
        }

        /**
         * 获取图片路径
         * @param url
         * @return
         */
        public static String getImageFullUrl(String url) {
            return !StringUtils.isEmpty(url) && !isFullUrl(url) ? UrlService.getImageFullUrl(url.trim()) : url;
        }

        /**
         * 判断是否完整路径
         * @param url
         * @return
         */
        private static boolean isFullUrl(String url) {
            return StringUtils.startsWithIgnoreCase(url, "http");
        }
        /**
         * 校验是否配置密码
         * @param hrPersonId 中台人员ID
         * @return
         */
        public static Map<String, Object> checkHasPassword(Long hrPersonId) {
            Map<String, Object> result = SalarySlipServiceHelper.isHavePassWordByPersonId(hrPersonId);
            return result;
        }

        /**
         * 校验密码是否符合规范
         * @param newPassword 新密码
         * @param verifyPassword 确认密码
         * @param verifyType 校验类型
         * @return
         */
        public static Map<String, Object> verifyPassword(String newPassword, String verifyPassword, String verifyType) {
            Map<String, String> paramMap = new HashMap<>(3);
            paramMap.put("newPwd", newPassword);
            paramMap.put("verifyPwd", verifyPassword);
            paramMap.put("verifyType", verifyType);
            Map<String, Object> result = SalarySlipServiceHelper.verifyPassword(paramMap);
            return result;
        }

        /**
         * 保存或修改密码
         * @param hrPersonId 中台人员ID
         * @param newPassword 新密码
         * @param verifyPassword 确认密码
         * @return
         */
        public static Map<String, Object> saveOrUpdatePassword(Long hrPersonId, String newPassword, String verifyPassword) {
            logger.info("personId:" + hrPersonId);
            Map<String, Object> paramMap = new HashMap<>(3);
            paramMap.put("personId", hrPersonId);
            paramMap.put("newPwd", newPassword);
            paramMap.put("verifyPwd", verifyPassword);
            Map<String, Object> result = SalarySlipServiceHelper.saveOrUpdatePassword(paramMap);
            return result;
        }

        /**
         * 使用密码登录
         * @return
         */
        public static Map<String, Object> authenticatePassword(Long hrPersonId, String password) {
            Map<String, Object> paramMap = new HashMap<>(2);
            paramMap.put("personId", hrPersonId);
            paramMap.put("newPwd", password);
            Map<String, Object> result = SalarySlipServiceHelper.authenticatePassword(paramMap);
            return result;
        }

        /**
         * 向指定手机号发送验证码
         * @param hrPersonId 中台人员ID
         * @param phone 手机号
         * @param duration 验证码有效时长
         * @return
         */
        public static Map<String, Object> sendCodeMessage(Long hrPersonId, String phone, Integer duration) {
            if (duration == null) {
                duration = 60;
            }

            Map<String, Object> paramMap = new HashMap<>(3);
            paramMap.put("personId", hrPersonId);
            paramMap.put("phone", phone);
            paramMap.put("duration", duration);
            Map<String, Object> result = SalarySlipServiceHelper.sendCodeMessage(paramMap);
            return result;
        }

        /**
         * 校验验证码
         * @param hrPersonId 中台人员ID
         * @param code 验证码
         * @return
         */
        public static Map<String, Object> validPhoneCode(Long hrPersonId, String code) {
            Map<String, Object> paramMap = new HashMap<>(2);
            paramMap.put("personId", hrPersonId);
            paramMap.put("code", code);
            Map<String, Object> result = SalarySlipServiceHelper.validPhoneCode(paramMap);
            return result;
        }

        /**
         * 查询工资条明细信息
         * @param hrPersonId 中台人员ID
         * @param date 年月日期
         * @return
         */
        public static Map<String, Object> querySalarySlipDetail(Long hrPersonId, Date date) {
            //获取起始结束日期
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startDate = calendar.getTime();
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.SECOND, -1);
            Date endDate = calendar.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Map<String, Object> paramMap = new HashMap<>(3);
            paramMap.put("personId", hrPersonId);
            paramMap.put("startDate", format.format(startDate));
            paramMap.put("endDate", format.format(endDate));
            Map<String, Object> result = SalarySlipServiceHelper.querySalarySlipDetail(paramMap);
            return result;
        }

        /**
         * 获取样式实体
         * @param fontSize
         * @param fontWeight
         * @param foreColor
         * @return
         */
        public static PCSalarySlipDataStyleModel getStyle(int fontSize, String fontWeight, String foreColor) {
            PCSalarySlipDataStyleModel styleModel = new PCSalarySlipDataStyleModel();
            styleModel.setFontSize(fontSize);
            styleModel.setFontWeight(fontWeight);
            styleModel.setForeColor(foreColor);
            return styleModel;
        }

        /**
         * 格式化数字为千分位显示；
         *
         * @param text；
         * @return
         */
        public static String fmtMicrometer(String text) {
            DecimalFormat df;
            if (text.indexOf('.') > 0) {
                int scale = text.length() - text.indexOf('.') - 1;
                if (scale == 0) {
                    df = new DecimalFormat("###,##0");
                } else {
                    StringBuilder format = new StringBuilder("#,##0.");
                    for (int i = 0; i < scale; i++) {
                        format.append('0');
                    }
                    df = new DecimalFormat(format.toString());
                }
            } else {
                df = new DecimalFormat("###,##0");
            }
            double number;
            try {
                number = Double.parseDouble(text);
            } catch (Exception e) {
                number = 0.0;
            }
            return df.format(number);
        }
}
