package tdkw.opa.tdkw_opa.formplugin.enums;

import kd.bos.dataentity.entity.DynamicObject;

import java.util.HashMap;
import java.util.List;

/**
 * @author: xxx
 * @create: 2024/08/26 16:20
 * @description: 组织绩效地图
 **/
public class OrgMapVO {
    /**
     * 指标分解
     */
    private OrgHeaderVO decomposeHeaderVO;

    /**
     * 指标对齐
     */
    private OrgHeaderVO alignmentHeaderVO;


    private List<Target> target;

    public List<Target> getTarget() {
        return target;
    }

    public void setTarget(List<Target> target) {
        this.target = target;
    }

    public OrgHeaderVO getDecomposeHeaderVO() {
        return decomposeHeaderVO;
    }

    public void setDecomposeHeaderVO(OrgHeaderVO decomposeHeaderVO) {
        this.decomposeHeaderVO = decomposeHeaderVO;
    }

    public OrgHeaderVO getAlignmentHeaderVO() {
        return alignmentHeaderVO;
    }

    public void setAlignmentHeaderVO(OrgHeaderVO alignmentHeaderVO) {
        this.alignmentHeaderVO = alignmentHeaderVO;
    }
}
