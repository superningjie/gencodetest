package tdkw.hrmp.hrobs.common.myteam.common.bean;

import kd.bos.dataentity.entity.DynamicObject;
import kd.hr.hbp.common.model.AuthorizedOrgResult;

import java.util.Set;

/**
 * ResultMap
 *
 * @author xxx
 * @date 2023/10/20
 */
public class TeamResultMap {
    /**
     * 要查找的职能id集合 如职位子序列的id集合
     */
    private Set<String> dimValueIds;
    /**
     * 有多少个职能 如多少个职位子序列
     */
    private Integer positionSubSize;
    /**
     * 职能对象集合 如每个职位子序列的对象
     */
    private DynamicObject[] positionSub;
    /**
     * 权限返回结果 封装了是否有全部权限和权限组织id集合
     */
    private AuthorizedOrgResult result;

    public TeamResultMap(Set<String> dimValueIds, Integer positionSubSize, DynamicObject[] positionSub) {
        this.dimValueIds = dimValueIds;
        this.positionSubSize = positionSubSize;
        this.positionSub = positionSub;
    }



    public TeamResultMap(Set<String> dimValueIds, Integer positionSubSize, DynamicObject[] positionSub, AuthorizedOrgResult result) {
        this.dimValueIds = dimValueIds;
        this.positionSubSize = positionSubSize;
        this.positionSub = positionSub;
        this.result = result;
    }

    public TeamResultMap() {
    }

    public Set<String> getDimValueIds() {
        return dimValueIds;
    }

    public void setDimValueIds(Set<String> dimValueIds) {
        this.dimValueIds = dimValueIds;
    }

    public Integer getPositionSubSize() {
        return positionSubSize;
    }

    public void setPositionSubSize(Integer positionSubSize) {
        this.positionSubSize = positionSubSize;
    }

    public DynamicObject[] getPositionSub() {
        return positionSub;
    }

    public void setPositionSub(DynamicObject[] positionSub) {
        this.positionSub = positionSub;
    }

    public AuthorizedOrgResult getResult() {
        return result;
    }

    public void setResult(AuthorizedOrgResult result) {
        this.result = result;
    }
}
