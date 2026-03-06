package tdkw.hrmp.hrobs.common.business;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GetQTDetailService {
    Map<String, Map<String, BigDecimal>> getQtDetail(List<String> numberList, String qtTypeNumber) throws Exception;
}
