package dgdl.odc.homs.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bd.sbd.enums.UnitConvertDirEnum;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.FlexEntityType;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.flex.FlexEntireData;
import kd.bos.entity.flex.FlexEntityMetaUtils;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.FlexProp;
import kd.bos.entity.validate.ValidateResult;
import kd.bos.flex.FlexService;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.scmc.im.business.helper.BillUnitAndQtytHelper;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportJsonUtil {

	/**
	 * ID标识
	 */
	public final static String ID = "id";

	/**
	 * 编码标识
	 */
	public final static String NUMBER = "number";

	/**
	 * 保存操作标识
	 */
	public final static String SAVE = "save";
	/**
	 * 提交操作标识
	 */
	public final static String SUBMIT = "submit";

	/**
	 * 删除操作
	 */
	public final static String DELETE = "delete";

	/**
	 * 表头必填验证
	 * 
	 * @throws BOSException
	 */
	public static void validateJsonDataThrowException(String[] requiredField, String[] requiredMsg, JSONObject jsonData)
			throws Exception {
		for (int i = 0; i < requiredField.length; i++) {
			if (jsonData.get(requiredField[i]) == null || StringUtils.isEmpty(jsonData.getString(requiredField[i]))) {
				throw new Exception(requiredMsg[i]);
			}
		}
	}

	/**
	 * 分录必填验证
	 * 
	 * @param requiredField
	 * @param requiredMsg
	 * @param jsonArr
	 * @return
	 * @throws BOSException
	 */
	public static void validateJsonDataThrowException(String[] requiredField, String[] requiredMsg, JSONArray jsonArr)
			throws Exception {
		for (int i = 0; i < jsonArr.size(); i++) {
			validateJsonDataThrowException(requiredField, requiredMsg, jsonArr.getJSONObject(i));
		}
	}

	/**
	 * 通过编码和单据标识
	 * 
	 * @param number
	 * @param entityName
	 * @return
	 */
	public static DynamicObject loadObj(String number, String entityName) {
		if (StringUtils.isEmpty(number)) {
			return null;
		}
		QFilter[] filter = new QFilter[] { new QFilter(NUMBER, QCP.equals, number) };
		DynamicObject obj = BusinessDataServiceHelper.loadSingle(entityName, ID, filter);
		return obj;
	}

	/**
	 * 
	 * @param number
	 * @param entityName
	 * @return
	 */
	public static DynamicObject loadObj(String fieldName, String number, String entityName) {
		if (StringUtils.isEmpty(number)) {
			return null;
		}
		QFilter[] filter = new QFilter[] { new QFilter(fieldName, QCP.equals, number) };
		DynamicObject obj = BusinessDataServiceHelper.loadSingle(entityName, ID, filter);
		return obj;
	}

	/**
	 * 获取操作失败原因
	 * 
	 * @param opResult
	 * @return
	 */
	public static String getAllErrorInfo(OperationResult opResult) {
		// 验证信息
		StringBuffer validateMsg = new StringBuffer();
		if (opResult.getMessage() != null) {
			validateMsg.append(opResult.getMessage());
		}
		if (opResult.getValidateResult() != null) {
			List<ValidateResult> validateErrors = opResult.getValidateResult().getValidateErrors();
			if (validateErrors != null) {
				for (int j = 0; j < validateErrors.size(); j++) {
					List<OperateErrorInfo> allErrorInfo = validateErrors.get(j).getAllErrorInfo();
					for (int k = 0; k < allErrorInfo.size(); k++) {
						validateMsg.append(allErrorInfo.get(k).getMessage() + ",");
					}
				}
			}
		}

		// 校验信息
		if (opResult.getInteractionContext() != null) {
			validateMsg.append(opResult.getInteractionContext().getSimpleMessage() + ",");

		}
		// 错误信息
		StringBuffer errorMsg = new StringBuffer();
		List<OperateErrorInfo> list = opResult.getAllErrorInfo();
		for (OperateErrorInfo operateErrorInfo : list) {
			errorMsg.append(operateErrorInfo.getMessage());
		}
		return validateMsg.append(errorMsg).toString();
	}

	/**
	 * 在后台创建一条单据数据并给弹性域字段赋值
	 */
	public static DynamicObject newAndSaveFlexFieldVal(String materialId, String entryEntityID, String fieldId,
			String... valuesField) {
		// 查询基础资料-物料数据所启用的辅助属性
		List<Long> flexPropertyIds = FlexService.getBaseUseFlexProperties("bd_material", materialId, "auxpty");
		// 获取待新建单据的单据实体类型
		MainEntityType entityType = EntityMetadataCache.getDataEntityType(entryEntityID);
		// 根据单据实体类型获取其中弹性域字段的属性对象
		FlexProp flexFieldProperty = (FlexProp) entityType.findProperty(fieldId);
		int flexTypeId = flexFieldProperty.getFlexTypeId();
		// 获取弹性域实体类型
		FlexEntityType flexFieldEntityType = FlexEntityMetaUtils.getBasedataPropFlexEntityType(entryEntityID, fieldId,
				flexTypeId, flexPropertyIds);
		// 创建一个弹性域实体对象
		DynamicObject flexFieldVal = new DynamicObject(flexFieldEntityType);
		DataEntityPropertyCollection properties = flexFieldEntityType.getProperties();
		// 设置初始值
		for (IDataEntityProperty prop : properties) {
			String key = prop.getName();
			if ("id".equals(key)) {
				continue;
			}
			if (!(prop instanceof BasedataProp)) {
				if (key.endsWith("_id")) {
					// 可不设置
					// prop.setValue(flexFieldVal, VAL_FLEX1_BASEDATA);
					continue;
				}

				String subKey = key;
				key = subKey.split("__")[1];
				switch (key) {
				case "f000002":
					prop.setValue(flexFieldVal, null);
					break;
				default:
					break;
				}
			} else {
				QFilter[] filter = null;
				DynamicObject obj = null;
				String subKey = key;
				key = subKey.split("__")[1];
				switch (key) {
				case "f000022":// 厂家
					filter = new QFilter[] { new QFilter("number", QCP.equals, valuesField[0]),
							new QFilter("group.number", QCP.equals, "CJ") };
					obj = BusinessDataServiceHelper// 辅助资料
							.loadSingle("bos_assistantdata_detail", ImportJsonUtil.ID, filter);
					prop.setValue(flexFieldVal, obj);
					break;
				case "f000021":// 产地
					filter = new QFilter[] { new QFilter("number", QCP.equals, valuesField[1]),
							new QFilter("group.number", QCP.equals, "CD") };
					obj = BusinessDataServiceHelper// 辅助资料
							.loadSingle("bos_assistantdata_detail", ImportJsonUtil.ID, filter);
					prop.setValue(flexFieldVal, obj);
					break;
				default:
					break;
				}

				// prop.setValue(flexFieldVal,
				// BusinessDataServiceHelper.loadSingle(VAL_FLEX2_BASEDATA,"bos_assistantdata_detai"));//查询辅助资料
			}
		}
		// 将新创建的弹性域实体对象封装成可赋值给单据上弹性域字段数据的格式
		FlexEntireData flexEntireData = new FlexEntireData();
		flexEntireData.setFlexData(flexFieldEntityType, flexFieldVal);
		long id = FlexService.saveFlexData(flexFieldEntityType, flexEntireData);
		DynamicObject flexObject = (DynamicObject) flexFieldProperty.getComplexType().createInstance();
		flexObject.set("id", id);
		Map<String, Object> values = flexEntireData.getFlexValue();
		flexObject.set("value", SerializationUtils.toJsonString(values));
		return flexObject;
		// 创建单据数据,并给各字段赋值(含基础资料 & 弹性域字段)
		// DynamicObject demobillorgObj =
		// BusinessDataServiceHelper.newDynamicObject(KEY_BILLNUMBER);
		// demobillorgObj.set("billno", "FlexFieldDemo2-001");
		// demobillorgObj.set(KEY_BASEDATAFIELD,
		// BusinessDataServiceHelper.loadSingle(VAL_BASEDATA, KEY_BASEDATAENTITYNUM));
		// demobillorgObj.set(KEY_FLEXFIELD, flexObject);
		// demobillorgObj.set("billstatus", "A");
		// demobillorgObj.set("org", "100000");
		// OperationResult result = SaveServiceHelper.saveOperate(KEY_BILLNUMBER, new
		// DynamicObject[] { demobillorgObj }, OperateOption.create());
		// logger.info("result: " + result.getSuccessPkIds());
	}

	/**
	 * 辅助数量
	 */
	public static void Update(DynamicObject bill, String billentry, String bd_material, String baseqty,
			String baseunit) {
		Set<Long> materialIdSet = new HashSet<>();
		DynamicObjectCollection entry = bill.getDynamicObjectCollection(billentry);
		for (DynamicObject dynamicObject : entry) {
			DynamicObject materialInvInfo = dynamicObject.getDynamicObject(bd_material);
			if (materialInvInfo == null)
				continue;
			DynamicObject material = materialInvInfo.getDynamicObject("masterid");
			if (material == null) {
				continue;
			}
			Object auxptyUnitId = null;
			DynamicObject auxptyUnit = material.getDynamicObject("auxptyunit");
			Boolean isUseAuxptyUnit = Boolean.valueOf((auxptyUnit != null));
			if (isUseAuxptyUnit.booleanValue()) {
				auxptyUnitId = auxptyUnit.getPkValue();
				dynamicObject.set("unit2nd_Id", auxptyUnitId);
			}
			BigDecimal baseQty = dynamicObject.getBigDecimal(baseqty);
			DynamicObject baseUnit = dynamicObject.getDynamicObject(baseunit);
			BigDecimal qtyunit2nd = BigDecimal.ZERO;
			String unitConvertDir = material.getString("unitconvertdir");
			boolean isConvert = (UnitConvertDirEnum.UINV_U2ND.getValue().equals(unitConvertDir)
					|| UnitConvertDirEnum.UINVANDU2ND.getValue().equals(unitConvertDir));
			if (isConvert) {
				if (BigDecimal.ZERO.compareTo(baseQty) == 0) {
					dynamicObject.set("qtyunit2nd", qtyunit2nd);
					continue;
				}
				qtyunit2nd = BillUnitAndQtytHelper.getDesQtyConv(material, baseUnit, baseQty, auxptyUnit);
				dynamicObject.set("qtyunit2nd", qtyunit2nd);
			}
		}
		SaveServiceHelper.update(bill);
	}

}
