package cds0.opmc.cea.common;

import kd.bos.form.IFormView;
import kd.hr.hbp.common.util.HRStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CommonUtils {
    /**
     * 对象深拷贝
     *
     * @Title: deepClone
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @param @return
     * @param @throws
     *            Exception
     * @return Object 返回类型
     */
    public static Object deepClone(Object obj) throws Exception {
        // 将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        // 从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (oi.readObject());
    }
    /**
     * 设置精度
     *
     * @param ovarallScore
     * @param accuracy
     * @param scaleType
     * @return
     */
    public static BigDecimal setSumScoreScale(BigDecimal ovarallScore, String accuracy, String scaleType) {
        if (HRStringUtils.isNotEmpty(accuracy) && HRStringUtils.isNotEmpty(scaleType)) {
            if ("1".equals(scaleType)) {
                // 四舍五入
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), RoundingMode.HALF_UP);
            } else {
                ovarallScore = ovarallScore.setScale(Integer.parseInt(accuracy), Integer.parseInt(scaleType));
            }
        }
        return ovarallScore;
    }



}
