package tdkw.hrmp.hrobs.common.business;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicCollectionProperty;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicProperty;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.EntityTypeUtil;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.earlywarn.kit.StringTemplateParser;
import kd.bos.entity.earlywarn.kit.StringUtil;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnDataSource;
import kd.bos.entity.earlywarn.warnschedule.MessageContent;
import kd.bos.entity.earlywarn.warnschedule.MessageTableColumn;
import kd.bos.entity.earlywarn.warnschedule.WarnMessageConfig;
import kd.bos.entity.earlywarn.warnschedule.messageconfig.WarnMessageReceiver;
import kd.bos.entity.earlywarn.warnschedule.messageconfig.WarnMessageReceiverType;
import kd.bos.entity.earlywarn.warnschedule.messageconfig.WarnMessageReceiverTypeEnum;
import kd.bos.entity.filter.FilterBuilder;
import kd.bos.entity.filter.FilterCondition;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.EntryProp;
import kd.bos.entity.property.SubEntryProp;
import kd.bos.entity.tree.TreeNode;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.metadata.treebuilder.FormTreeBuilder;
import kd.bos.metadata.treebuilder.PropTreeBuildOption;
import kd.bos.orm.query.QFilter;
import kd.bos.service.earlywarn.formula.EWFormulaHandler;
import kd.bos.service.earlywarn.var.IVariableMode;
import kd.bos.service.earlywarn.var.SourceFieldVariable;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author xxx
 * @date 2024/3/4
 * @description 权限调整确认数据源插件（预警插件）
 */
public class DataSource implements IEarlyWarnDataSource {
    private final static Log logger = LogFactory.getLog(DataSource.class);

    @Override
    public List<QFilter> buildFilter(String s, FilterCondition filterCondition, EarlyWarnContext earlyWarnContext) {
        if (!StringUtil.isBlank(s) && null != filterCondition) {
            MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(s);
            FilterBuilder filterBuilder = new FilterBuilder(mainEntityType, filterCondition);
            filterBuilder.buildFilter();
            return filterBuilder.getQFilters();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public DynamicObjectCollection getData(String s, List<QFilter> list, EarlyWarnContext earlyWarnContext) {
        if (StringUtils.isBlank(s)) {
            return null;
        } else {
            MainEntityType mainType = EntityMetadataCache.getDataEntityType(s);
            if (StringUtils.isBlank(mainType.getAlias())) {
                throw new KDException(new ErrorCode("", String.format(ResManager.loadKDString("数据源%1$s(%2$s)表名为空。", "DefaultEarlyWarnBillDataSource_6", "bos-earlywarn"), mainType.getDisplayName() == null ? "" : mainType.getDisplayName().getLocaleValue(), mainType.getName())));
            } else {
                EWFormulaHandler formulaHandler = new EWFormulaHandler(earlyWarnContext, (BillEntityType) mainType);
                QFilter[] qFilterSql = null;
                FilterCondition filterCondition = earlyWarnContext.getWarnSchedule().getWarnCondition().getFilterCondition();
                QFilter[] filters = list != null && !list.isEmpty() ? list.toArray(new QFilter[0]) : null;
                if (filterCondition != null) {
                    FilterBuilder filterBuilder = new FilterBuilder(mainType, filterCondition);
                    filterBuilder.buildFilter();
                    if (filterBuilder.getQFilter() != null) {
                        qFilterSql = (new ArrayList<>(Collections.singletonList(filterBuilder.getQFilter()))).toArray(new QFilter[1]);
                    }
                } else {
                    qFilterSql = filters;
                }

                Set<String> selectProperties = this.getSearchFields(mainType, earlyWarnContext, filters, false);
                Set<String> formulaFields = this.addFormulaSourceFields(formulaHandler.getVars(), selectProperties);
                String useToQuerySelectedProp = this.handleComplexProps(selectProperties);
                DynamicObjectCollection queryResults = new DynamicObjectCollection();
                DynamicObjectCollection dynamicObjects = queryData(s, useToQuerySelectedProp, qFilterSql);
                List<Object> pkValues = this.filterAndFillData(formulaFields, dynamicObjects, queryResults, formulaHandler);
                if (earlyWarnContext.getWarnSchedule().isByEntry() && queryResults.size() > 0) {
                    String entrySelectedProp = StringUtils.join(this.getSearchFields(mainType, earlyWarnContext, filters, true).toArray(), ",");
                    DynamicObjectCollection allData = QueryServiceHelper.query(s, entrySelectedProp, this.addPKValueQFilter(pkValues, qFilterSql), "id asc");
                    earlyWarnContext.setPlainWarnData(allData);
                }

                return queryResults;
            }
        }
    }

    private QFilter[] addPKValueQFilter(List<Object> pkValues, QFilter[] qFilters) {
        if (CollectionUtils.isEmpty(pkValues)) {
            return qFilters;
        } else {
            List<QFilter> qFilterList = new ArrayList<>();
            if (null != qFilters && qFilters.length > 0) {
                Collections.addAll(qFilterList, qFilters);
            }

            QFilter pkFilter = new QFilter("id", "in", pkValues);
            qFilterList.add(pkFilter);
            return qFilterList.toArray(new QFilter[0]);
        }
    }

    private List<Object> filterAndFillData(Set<String> formulaFields, DynamicObjectCollection dynamicObjects, DynamicObjectCollection queryResults, EWFormulaHandler formulaHandler) {
        List<Object> pkList = new ArrayList<>(dynamicObjects.size());
        if (dynamicObjects.size() != 0 && !StringUtils.isBlank(formulaHandler.getFormula())) {
            Map<String, DynamicProperty> fieldProps = new HashMap<>(formulaFields.size());
            IDataEntityType dataEntityType = dynamicObjects.get(0).getDataEntityType();

            for (String field : formulaFields) {
                DynamicProperty dynamicProperty = this.getFieldProp(field, dataEntityType);
                fieldProps.put(field, dynamicProperty);
            }

            try {
                for (DynamicObject row : dynamicObjects) {
                    if ((Boolean) formulaHandler.getValue(fieldProps, row)) {
                        queryResults.add(row);
                        pkList.add(row.getPkValue());
                    }
                }
                return pkList;
            } catch (Exception var12) {
                logger.error(var12);
                throw new KDBizException(ResManager.loadKDString("表达式执行出错，请检查表达式。", "DefaultEarlyWarnBillDataSource_0", "bos-earlywarn"));
            }
        } else {
            queryResults.addAll(dynamicObjects);
            return pkList;
        }
    }

    private DynamicProperty getFieldProp(String field, IDataEntityType dataEntityType) {
        if (StringUtils.isBlank(field)) {
            return null;
        } else {
            String[] keys = StringUtil.split(field, ".");
            if (keys.length == 1) {
                IDataEntityProperty prop = dataEntityType.getProperties().get(field);
                return prop == null ? null : (DynamicProperty) prop;
            } else {
                IDataEntityType propertyType = dataEntityType;
                IDataEntityProperty property = null;

                for (int i = 0; i < keys.length; ++i) {
                    property = propertyType.getProperties().get(keys[i]);
                    if (i == keys.length - 1) {
                        break;
                    }

                    if (!(property instanceof BasedataProp)) {
                        property = null;
                        break;
                    }

                    propertyType = ((BasedataProp) property).getComplexType();
                }

                return property == null ? null : (DynamicProperty) property;
            }
        }
    }

    private Set<String> getSearchFields(MainEntityType mainEntityType, EarlyWarnContext context, QFilter[] filters, boolean isOnlyEntry) {
        Set<String> fields = new HashSet<>();
        fields.add(mainEntityType.getPrimaryKey().getName());
        if (null != filters) {
            for (QFilter filter : filters) {
                if (StringUtils.isNotBlank(filter.getProperty())) {
                    this.fetchField(mainEntityType, filter.getProperty(), fields, isOnlyEntry);
                }
            }
        }

        WarnMessageConfig messageConfig = context.getWarnSchedule().getMessageConfig();
        if (null != messageConfig) {
            this.fetchFields(mainEntityType, messageConfig.getSingleContent(), fields, isOnlyEntry);
            if (messageConfig.isMergeSendMessage()) {
                MessageContent messageContent = messageConfig.getMergeMessageContent();
                if (null != messageContent) {
                    this.fetchFields(mainEntityType, messageContent.getMessageHead(), fields, isOnlyEntry);
                    this.fetchFields(mainEntityType, messageContent.getMessageEnd(), fields, isOnlyEntry);
                    List<MessageTableColumn> tableColumns = messageContent.getTableColumns();
                    if (CollectionUtils.isNotEmpty(tableColumns)) {
                        tableColumns.forEach((col) -> {
                            if (StringUtils.isNotBlank(col.getField())) {
                                this.fetchField(mainEntityType, col.getField(), fields, isOnlyEntry);
                            }

                        });
                    }
                } else {
                    this.fetchFields(mainEntityType, messageConfig.getMergeContent(), fields, isOnlyEntry);
                }
            }

            List<WarnMessageReceiverType> receiverTypes = messageConfig.getReceiverTypes();

            for (WarnMessageReceiverType type : receiverTypes) {

                for (WarnMessageReceiver receiver : type.getReceivers()) {
                    if (StringUtils.isNotBlank(receiver.getField())) {
                        WarnMessageReceiverTypeEnum receiverType = receiver.getReceiverTypeObj();
                        if (WarnMessageReceiverTypeEnum.RelationPerson == receiverType) {
                            this.fetchField(mainEntityType, receiver.getField(), fields, isOnlyEntry);
                        }
                    }
                }
            }

            if (StringUtils.isNotBlank(messageConfig.getGroupField())) {
                this.fetchField(mainEntityType, messageConfig.getGroupField(), fields, isOnlyEntry);
            }
        }

        return fields;
    }

    private void fetchField(MainEntityType mainEntityType, String macroName, Set<String> fields, boolean isOnlyEntry) {
        if (!StringUtils.isBlank(macroName)) {
            String[] arr = StringUtil.split(macroName, ".");
            if (arr.length != 0) {
                IDataEntityProperty property = mainEntityType.findProperty(arr[0]);
                if (null != property) {
                    DynamicObjectType type;
                    String id;
                    if (!isOnlyEntry) {
                        fields.add(macroName);
                        if (property instanceof DynamicCollectionProperty) {
                            type = ((DynamicCollectionProperty) property).getDynamicCollectionItemPropertyType();
                            if (null != type) {
                                id = type.getPrimaryKey().getName();
                                fields.add(StringUtils.join(new Object[]{arr[0], id}, "."));
                            }
                        }
                    } else {
                        if (property instanceof SubEntryProp) {
                            return;
                        }

                        if (property instanceof EntryProp) {
                            type = ((DynamicCollectionProperty) property).getDynamicCollectionItemPropertyType();
                            if (null != type) {
                                id = type.getPrimaryKey().getName();
                                fields.add(StringUtils.join(new Object[]{arr[0], id}, "."));
                            }
                        }
                    }

                }
            }
        }
    }

    private void fetchFields(MainEntityType mainEntityType, String template, Set<String> fields, boolean isOnlyEntry) {
        if (null != mainEntityType && !StringUtils.isBlank(template)) {
            StringTemplateParser parser = new StringTemplateParser();
            parser.parse(template, (macroName) -> {
                this.fetchField(mainEntityType, macroName, fields, isOnlyEntry);
                return "";
            });
        }
    }

    private String handleComplexProps(Set<String> selectProperties) {
        String[] querySelectProperties = selectProperties.toArray(new String[0]);

        for (int i = 0; i < querySelectProperties.length; ++i) {
            if (querySelectProperties[i].contains(".")) {
                int index = querySelectProperties[i].indexOf(".");
                index = querySelectProperties[i].indexOf(".", index + 1);
                if (index != -1) {
                    querySelectProperties[i] = querySelectProperties[i].substring(0, index);
                }
            }
        }

        return StringUtils.join(querySelectProperties, ",");
    }

    private Set<String> addFormulaSourceFields(List<IVariableMode> vars, Set<String> selectFldStrings) {
        Set<String> formulaFields = new HashSet<>();

        for (IVariableMode var : vars) {
            if (var instanceof SourceFieldVariable) {
                SourceFieldVariable srcField = (SourceFieldVariable) var;
                String fldString = srcField.getFullPropName();
                selectFldStrings.add(fldString);
                formulaFields.add(fldString);
            }
        }

        return formulaFields;
    }

    @Override
    public List<Map<String, Object>> getCommonFilterColumns(String s) {
        if (StringUtils.isBlank(s)) {
            return new ArrayList<>();
        } else {
            MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(s);
            EntityTypeUtil helper = new EntityTypeUtil();
            return helper.getFilterColumns(mainEntityType);
        }
    }

    @Override
    public TreeNode getSingleMessageFieldTree(String s) {
        if (StringUtils.isBlank(s)) {
            return new TreeNode();
        } else {
            PropTreeBuildOption option = new PropTreeBuildOption();
            option.setIncludeEntryEntityAsKeyPrefix(true);
            MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(s);
            TreeNode root = FormTreeBuilder.buildDynamicPropertyTree(mainEntityType, option);
            root.setIsOpened(true);
            return root;
        }
    }

    @Override
    public TreeNode getMergeMessageFieldTree(String s) {
        TreeNode root = this.getSingleMessageFieldTree(s);
        TreeNode totalCount = new TreeNode(root.getId(), "totalCount", ResManager.loadKDString("总条数", "DefaultEarlyWarnBillDataSource_1", "bos-mservice-operation"));
        root.addChild(0, totalCount);
        TreeNode warnSchedule = new TreeNode(root.getId(), "warnSchedule", ResManager.loadKDString("监控方案", "DefaultEarlyWarnBillDataSource_2", "bos-mservice-operation"));
        warnSchedule.addChild(new TreeNode("warnSchedule", "warnSchedule.name", ResManager.loadKDString("名称", "DefaultEarlyWarnBillDataSource_3", "bos-mservice-operation")));
        warnSchedule.addChild(new TreeNode("warnSchedule", "warnSchedule.number", ResManager.loadKDString("编码", "DefaultEarlyWarnBillDataSource_4", "bos-mservice-operation")));
        root.addChild(1, warnSchedule);
        TreeNode earlyEarn = new TreeNode(root.getId(), "earlyWarn", ResManager.loadKDString("业务预警对象", "DefaultEarlyWarnBillDataSource_5", "bos-mservice-operation"));
        earlyEarn.addChild(new TreeNode("earlyWarn", "earlyWarn.name", ResManager.loadKDString("名称", "DefaultEarlyWarnBillDataSource_3", "bos-mservice-operation")));
        earlyEarn.addChild(new TreeNode("earlyWarn", "earlyWarn.number", ResManager.loadKDString("编码", "DefaultEarlyWarnBillDataSource_4", "bos-mservice-operation")));
        root.addChild(2, earlyEarn);
        return root;
    }

    private DynamicObjectCollection queryData(String s, String useToQuerySelectedProp, QFilter[] qFilterSql) {
        DynamicObject[] load = BusinessDataServiceHelper.load(s, useToQuerySelectedProp + ",tdkw_change_user", qFilterSql);
        DynamicObjectCollection dynamicObjects = new DynamicObjectCollection();
        logger.info("取出" + load.length + "条数据");
        if (load.length > 0) {
            dynamicObjects.add(load[0]);
            return dynamicObjects;
        }
        logger.info("传递" + load.length + "条数据");
        return dynamicObjects;
    }
}