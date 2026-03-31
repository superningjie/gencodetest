package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.exception.KDBizException;
import kd.hr.hbp.business.servicehelper.HRMServiceHelper;
import kd.hr.hbp.common.util.HRArrayUtils;
import kd.hrmp.hrpi.business.domian.repository.HRPIPersonRolerelRepository;

import java.util.*;
import java.util.stream.Collectors;

public class HrpiServiceDomianService {

    public static HrpiServiceDomianService getInstance() {
        return ServiceFactory.getService(HrpiServiceDomianService.class);
    }

    /**
     * 取直接下级
     * @param queryKey
     * @param ids
     * @param queryDate
     * @return
     */
    public Map<Long, List<Map<String, Object>>> getDirectChilder(String queryKey, List<Long> ids, Date queryDate) {
        Map<Long, List<Map<String, Object>>> perChildren = new HashMap();
        DynamicObjectCollection roleColl = HRPIPersonRolerelRepository.getRolesByPersonId(queryKey, ids, queryDate);
        if (roleColl.size() > 0) {
            Multimap<Long, Long> personRoleMap = ArrayListMultimap.create();
            roleColl.forEach((role) -> {
                personRoleMap.put(role.getLong(queryKey), role.getLong("role.id"));
            });
            Map<Long, Long> allChildrenRelationIds = getAllChildRelationIds(new HashSet(personRoleMap.values()), queryDate);
            if (!allChildrenRelationIds.isEmpty()) {
                Map<Long, Set<Long>> personChildRoles = getPersonChildRole(ids, personRoleMap, allChildrenRelationIds);
                DynamicObject[] childerDyCols = HRPIPersonRolerelRepository.getParentRoleRel(new HashSet(allChildrenRelationIds.values()), queryDate);
                if (!HRArrayUtils.isEmpty(childerDyCols)) {
                    Map<Long, List<DynamicObject>> personGroupByOrg = (Map)Arrays.stream(childerDyCols).collect(Collectors.groupingBy((dy) -> {
                        return dy.getLong("role.id");
                    }));
                    Map<Long, List<Map<String, Object>>> result = new HashMap(personGroupByOrg.size());
                    Iterator var15 = personChildRoles.entrySet().iterator();

                    while(var15.hasNext()) {
                        Map.Entry<Long, Set<Long>> personParentRole = (Map.Entry)var15.next();
                        List<DynamicObject> childrenList = findChildrenList(personGroupByOrg, (Set)personParentRole.getValue());
                        List<Map<String, Object>> list = new ArrayList(childrenList.size());
                        addchilderList(list, childrenList);
                        result.put(personParentRole.getKey(), list);
                    }
                    perChildren.putAll(result);
                }
            }
        } else {

        }
        return perChildren;
    }


    private static void addchilderList(List<Map<String, Object>> result, List<DynamicObject> superiorList) {
        Iterator var3 = superiorList.iterator();

        while(var3.hasNext()) {
            DynamicObject superior = (DynamicObject)var3.next();
            Map<String, Object> infoMap = new HashMap(16);
            infoMap.put("superior.number", superior.getString("person.number"));
            infoMap.put("superior.name", superior.getString("person.name"));
            infoMap.put("superior.headsculpture", superior.getString("person.headsculpture"));
            infoMap.put("superioradminorg.id", superior.getLong("adminorg.id"));
            infoMap.put("superioradminorg.name", superior.getString("adminorg.name"));
            infoMap.put("superioradminorg.number", superior.getString("adminorg.number"));
            infoMap.put("person.id", superior.getLong("person.id"));
            infoMap.put("depemp.id", superior.getLong("depemp.id"));
            infoMap.put("employee.id", superior.getLong("employee.id"));
            result.add(infoMap);
        }

    }

    private static List<DynamicObject> findChildrenList(Map<Long, List<DynamicObject>> result, Set<Long> childRoles) {
        List<DynamicObject> superiorList = new LinkedList();
        childRoles.forEach((roleId) -> {
            List<DynamicObject> infoList = (List)result.get(roleId);
            if (null != infoList) {
                superiorList.addAll(infoList);
            }

        });
        return superiorList;
    }

    private static Map<Long, Set<Long>> getPersonChildRole(List<Long> ids, Multimap<Long, Long> personRoleMap, Map<Long, Long> allChildRelationIds) {
        Map<Long, Set<Long>> personChildRoles = new HashMap(ids.size());
        Iterator var5 = personRoleMap.entries().iterator();

        while(var5.hasNext()) {
            Map.Entry<Long, Long> personRole = (Map.Entry)var5.next();
            Long parentId = (Long)allChildRelationIds.get(personRole.getValue());
            if (null != parentId && parentId > 0L) {
                Set<Long> parentRoleList = (Set)personChildRoles.get(personRole.getKey());
                if (null == parentRoleList) {
                    parentRoleList = new HashSet();
                }

                ((Set)parentRoleList).add(parentId);
                personChildRoles.put(personRole.getKey(), parentRoleList);
            }
        }

        return personChildRoles;
    }

    /**
     * 获取指定角色的所有下级
     * @param roleIds
     * @param queryDate
     * @return
     */
    private static Map<Long, Long> getAllChildRelationIds(Set<Long> roleIds, Date queryDate) {
        List<DynamicObject> roleList = getAllChildrenRelationIds(roleIds, queryDate);
        Map<Long, Long> childRoleIds = new HashMap(roleList.size());
        roleList.stream().filter((role) -> {
            return roleIds.contains(role.getLong("parent.id")); // role.id
        }).forEach((parRole) -> {
            Long var10000 = (Long)childRoleIds.put(parRole.getLong("parent.id"), parRole.getLong("role.id"));
        });
        return childRoleIds;
    }

    private static List<DynamicObject> getAllChildrenRelationIds(Set<Long> roleIds, Date queryDate){
        try {
            Map<String, Object> roleMap = (Map) HRMServiceHelper.invokeHRMPService("hbpm", "IWorkRoleService", "queryAllChildRelations", new Object[]{queryDate, new ArrayList(roleIds), 1010L});
            return !roleMap.isEmpty() && (Boolean)roleMap.get("success") ? (List)roleMap.get("data") : Collections.emptyList();
        } catch (Exception var3) {
            throw new KDBizException(ResManager.loadKDString("查询上级工作角色异常。", "PersonRoleServiceImpl_5", "hrmp-hrpi-business", new Object[0]));
        }
    }
}
