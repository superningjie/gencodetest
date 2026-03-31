package cds0.opmc.cea.business.service;

import cds0.opmc.cea.business.ServiceFactory;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.CollectionUtils;
import kd.hr.hbp.common.constants.HRBaseConstants;
import kd.hr.hbp.common.util.HRObjectUtils;
import kd.hrmp.hrpi.business.domian.repository.HRPIBosUserRepository;
import kd.hrmp.hrpi.business.domian.repository.HRPIPersonRepository;
import kd.hrmp.hrpi.business.infrastructure.utils.DateUtil;
import kd.opmc.pbs.business.external.hrpi.IHRPIPersonService;
import kd.opmc.pbs.business.external.hrpi.IHRPIWorkRoleService;

import java.util.*;
import java.util.stream.Collectors;

import static cds0.opmc.cea.common.AppflgConstant.ID;

public class HandlerFindDomainService {
    private static final Log LOG = LogFactory.getLog(HandlerFindDomainService.class);
    protected static final IHRPIWorkRoleService WORK_ROLE_SERVICE = IHRPIWorkRoleService.getInstance();
    protected static final IHRPIPersonService PERSON_SERVICE = IHRPIPersonService.getInstance();
    private static final HrpiServiceDomianService HRPI_SERVICE_DOMIAN_SERVICE = HrpiServiceDomianService.getInstance();

    public static HandlerFindDomainService getInstance() {
        return ServiceFactory.getService(HandlerFindDomainService.class);
    }

    /**
     * 查找指定人的直接上级
     * @param personIds
     * @return
     */
    public Map<Long, List<Long>> getDirectSuperiorIdsByPersonIds(List<Long> personIds){
        LOG.info("【OPMC】-getSuperiorIdsByPersonIds,personIds is[{}]", SerializationUtils.toJsonString(personIds));
        Map<Long, List<Map<String, Object>>> directSuperior = WORK_ROLE_SERVICE.getDirectSuperior(personIds);
        return getRelationerIdsByPersonIds(personIds,directSuperior);
    }

    /**
     * 查找指定人的直接下级
     * @param personIds
     * @return
     */
    public Map<Long, List<Long>> getDirectChildrenIdsByPersonIds(List<Long> personIds){
        Map<Long, List<Map<String, Object>>> directChilder = HRPI_SERVICE_DOMIAN_SERVICE.getDirectChilder("person.id", personIds, DateUtil.getCurrentDate());
        return getRelationerIdsByPersonIds(personIds,directChilder);
    }

    /**
     * 查找指定人的本人
     * @param personIds
     * @return
     */
    public Map<Long, List<Long>> getPersonSelfByPersonIds(List<Long> personIds){
        Map<Long, List<Long>> retMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        Map<Long, Long> userIdsByPersonIds = getUserIdsByPersonIds(personIds);
        for (Long key : userIdsByPersonIds.keySet()) {
            ArrayList<Long> longs = new ArrayList<>();
            longs.add(userIdsByPersonIds.get(key));
            retMap.put(key,longs);
        }
        return retMap;
    }

    /**
     * 查找指定人的同级 （先找上级，然后再好上级的下级，即找到同级，再剔除掉当前的人员）
     * @param personIds
     * @return
     */
    public Map<Long, List<Long>> getPeerColegeIdsByPersonIds(List<Long> personIds){
        Map<Long, List<Long>> retMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        Map<Long, List<Long>> peerColegeIds = new HashMap<>();
        Map<Long, List<Map<String, Object>>> directSuperior = WORK_ROLE_SERVICE.getDirectSuperior(personIds);
        // 先取直接上级
        Map<Long, List<Long>> directSuperiorRelations = getRelationerPersonIds(personIds,directSuperior);
        directSuperiorRelations.entrySet().stream().forEach(entry -> {
            Set<Long> peerColegeSet = new HashSet<>();
            // 当前测评对象人员的上级人员，取对应的直接下级(即当前person对应的同级)
            Map<Long, List<Long>> currPersonPeerColegeMap = getRelationerPersonIds(entry.getValue(),HRPI_SERVICE_DOMIAN_SERVICE.getDirectChilder("person.id", entry.getValue(), DateUtil.getCurrentDate()));
            currPersonPeerColegeMap.values().stream().forEach(peerColege -> {
                peerColegeSet.addAll(peerColege.stream().collect(Collectors.toSet()));
            });
            // 同级中包含当前person，剔除掉
            if(peerColegeSet.contains(entry.getKey())){
                peerColegeSet.remove(entry.getKey());
            }
            // 每个person对应的同级person
            peerColegeIds.put(entry.getKey(), peerColegeSet.stream().collect(Collectors.toList()));
        });
        // 可能出现多个人存在相同的同级person的情况，这里用Set去重
        Set<Long> peerIds = new HashSet<>();
        peerColegeIds.values().stream().forEach(peers -> {
            peerIds.addAll(peers.stream().collect(Collectors.toSet()));
        });
        // 通过上级的自然人id查找到自然人id和平台id对应关系, <superior,user>
        Map<Long, Long> superiorUserMap = getUserIdsByPersonIds(peerIds.stream().collect(Collectors.toList()));
        // 封装结果集，结果集为<personId,上级的userid>
        for (Map.Entry<Long, List<Long>> personSuperiorEntry : peerColegeIds.entrySet()) {
            List<Long> superiorPerson = personSuperiorEntry.getValue();
            List<Long> superiorUser = new ArrayList<>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
            for (Long user : superiorPerson) {
                // 判断平台是否可用，如果禁用没有数据，则不放入，不然这里会放入0
                if (superiorUserMap.containsKey(user)) {
                    superiorUser.add(superiorUserMap.get(user));
                }
            }
            retMap.put(personSuperiorEntry.getKey(), superiorUser);
        }
        return retMap;
    }

    /**
     * 查找关系对应的person信息
     * @param personIds
     * @param directRelationer
     * @return
     */
    public Map<Long, List<Long>> getRelationerPersonIds(List<Long> personIds , Map<Long, List<Map<String, Object>>> directRelationer){
        // 如果有上级，那么批量去构造上级的自然人id
        Map<Long, List<Long>> personSuperiorMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        List<Long> superiorIds = new ArrayList<>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
        for (Long personId : personIds) {
            List<Map<String, Object>> superiorList = directRelationer.get(personId);
            if (!HRObjectUtils.isEmpty(superiorList)) {
                // 每一个人员的上级list
                List<Long> superiorListPer = new ArrayList<>(HRBaseConstants.INITCAPACITY_HASHMAP);
                for (Map<String, Object> superiorInfo : superiorList) {
                    // 这里调组织接口后返回的是上级自然人的id
                    Long superiorId = (Long) superiorInfo.get("person.id");
                    // 确保返回过来的是非空
                    if (!HRObjectUtils.isEmpty(superiorId)) {
                        superiorListPer.add(superiorId);
                        superiorIds.add(superiorId);
                    }
                }
                personSuperiorMap.put(personId, superiorListPer);
            }
        }
        return personSuperiorMap;
    }

    /**
     * 查找指定人的上级/同级/下级 关系数据
     * @param personIds
     * @return
     */
    public Map<Long, List<Long>> getRelationerIdsByPersonIds(List<Long> personIds , Map<Long, List<Map<String, Object>>> directRelationer) {
        Map<Long, List<Long>> retMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        if (directRelationer != null) {
            // 如果有上级，那么批量去构造上级的自然人id
            Map<Long, List<Long>> personSuperiorMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
            List<Long> superiorIds = new ArrayList<>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
            for (Long personId : personIds) {
                List<Map<String, Object>> superiorList = directRelationer.get(personId);
                if (!HRObjectUtils.isEmpty(superiorList)) {
                    // 每一个人员的上级list
                    List<Long> superiorListPer = new ArrayList<>(HRBaseConstants.INITCAPACITY_HASHMAP);
                    for (Map<String, Object> superiorInfo : superiorList) {
                        // 这里调组织接口后返回的是上级自然人的id
                        Long superiorId = (Long) superiorInfo.get("person.id");
                        // 确保返回过来的是非空
                        if (!HRObjectUtils.isEmpty(superiorId)) {
                            superiorListPer.add(superiorId);
                            superiorIds.add(superiorId);
                        }
                    }
                    personSuperiorMap.put(personId, superiorListPer);
                }
            }
            LOG.info("【OPMC】-getSuperiorIdsByPersonIds,superiorIds is[{}]",SerializationUtils.toJsonString(superiorIds));
            // 通过上级的自然人id查找到自然人id和平台id对应关系, <superior,user>
            Map<Long, Long> superiorUserMap = getUserIdsByPersonIds(superiorIds);
            LOG.info("【OPMC】-getSuperiorIdsByPersonIds- getUserIdsByPersonIds,superiorUserMap is[{}]",SerializationUtils.toJsonString(superiorUserMap));
            // 封装结果集，结果集为<personId,上级的userid>
            for (Map.Entry<Long, List<Long>> personSuperiorEntry : personSuperiorMap.entrySet()) {
                List<Long> superiorPerson = personSuperiorEntry.getValue();
                List<Long> superiorUser = new ArrayList<>(HRBaseConstants.INITCAPACITY_ARRAYLIST);
                for (Long user : superiorPerson) {
                    // 判断平台是否可用，如果禁用没有数据，则不放入，不然这里会放入0
                    if (superiorUserMap.containsKey(user)) {
                        superiorUser.add(superiorUserMap.get(user));
                    }
                }
                retMap.put(personSuperiorEntry.getKey(), superiorUser);

            }
        }
        LOG.info("【OPMC】-getSuperiorIdsByPersonIds ,retMap is : {}",SerializationUtils.toJsonString(retMap));
        return retMap;
    }

    /**
     * <personId,userId>
     *
     * @param personIds 通过自然人查找平台人id
     * @return 自然人和平台人对应关系，如果没有平台人，这里会返回空值
     */
    private Map<Long, Long> getUserIdsByPersonIds(List<Long> personIds) {
        Map<Long, Long> retMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);

        Map<String, List<Long>> personIdMap = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        personIdMap.put(HRBaseConstants.PERSON, personIds);
        Map<String, Object> userIdByPersonInfo = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        try{
            userIdByPersonInfo = PERSON_SERVICE.getUserIdByPersonInfo(personIdMap);
        }catch(Exception e){
            LOG.error("【OPMC】-getUserIdsByPersonIds error,personIds:{}",personIds);
            LOG.error(e);
        }
        LOG.info("【OPMC】-getUserIdsByPersonIds,param personIds : {},userIdByPersonInfo : {}",personIds,userIdByPersonInfo);
        if(userIdByPersonInfo.isEmpty()){
            return retMap;
        }
        Boolean success = (Boolean) userIdByPersonInfo.get("success");
        if (success) {
            @SuppressWarnings("unchecked")
            Map<Long, Object> userInfo = (Map<Long, Object>) userIdByPersonInfo.get("data");
            List<Long> userIds = new ArrayList<>(personIds.size());
            for (Long personId : personIds) {
                @SuppressWarnings("unchecked")
                Map<String, Long> user = (Map<String, Long>) userInfo.get(personId);
                if (user != null) {
                    Long userId = user.get("user");
                    userIds.add(userId);
                }
            }
            Map<Long, Boolean> userIsEnableMap = batchQueryUserIsEnable(userIds);
            for (Long personId : personIds) {
                @SuppressWarnings("unchecked")
                Map<String, Long> user = (Map<String, Long>) userInfo.get(personId);
                if (user != null) {
                    Long userId = user.get("user");
                    if (userIsEnableMap.getOrDefault(userId, false)) {
                        retMap.put(personId, userId);
                    }
                }
            }
        }
        return retMap;
    }


    /**
     * 批量查询用户是否可用,包含未禁用
     *
     * @param userIds 平台用户id
     * @return 是否可用,包含未禁用
     */
    public Map<Long, Boolean> batchQueryUserIsEnable(List<Long> userIds) {
        List<Map<String, Object>> userInfo = UserServiceHelper.get(userIds, new String[]{"id", "isforbidden","enable"}, null);
        Map<Long, Boolean> ret = new HashMap<>(HRBaseConstants.INITCAPACITY_HASHMAP);
        if (CollectionUtils.isEmpty(userInfo)) {
            return ret;
        } else {
            for (Map<String, Object> user : userInfo) {
                ret.put((Long) user.get(ID), !(Boolean) user.get("isforbidden") && "1".equals(user.get("enable")));
            }
            return ret;
        }
    }
}
