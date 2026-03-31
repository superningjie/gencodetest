package tdkw.hrmp.hrobs.common.business;

import tdkw.hrmp.hrobs.common.hrobs.util.QTDetailUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 查询年假信息
 *
 * @author : zhousy
 * @date : 2023/8/24 10:23
 */
public class GetQTDetailServiceImpl implements GetQTDetailService {

    @Override
    public Map<String, Map<String, BigDecimal>> getQtDetail(List<String> numberList, String qtTypeNumber) throws Exception {
        return QTDetailUtil.getQTDetailByUserNumberList(numberList, qtTypeNumber);
    }
}
