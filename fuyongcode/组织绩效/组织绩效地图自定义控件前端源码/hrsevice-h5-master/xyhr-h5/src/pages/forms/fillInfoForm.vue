<template>
	<div class="fillInfoForm rlform">
		<van-nav-bar title="信息补录" left-arrow class="navStyle" @click-left="goback" />
		<div class="group">
			<div class="flex row">
				<div class="iconfont red">&#xe6ca;</div>
				<h5 class="tip" v-if="num != 0">系统稽核到您的以下信息存在不完善，请按系统提醒完善个人信息。</h5>
				<h5 class="tip" style="line-height: 37px" v-if="num == 0">信息已填写完整，所有信息已提交审核中。</h5>
			</div>
		</div>
		<template v-for="item in dataArr">
			<div class="group" :key="item.code">
				<div class="flex row" v-for="records in item.records" v-if="records.editType!=3" :key="records.tempId">
					<i
						class="iconfont"
						:style="{ color: infoIcon[item.code] ? infoIcon[item.code].color : '#7FCD93' }"
						v-html="infoIcon[item.code] ? infoIcon[item.code].text : '&#xe6a9;'"
					></i>
					<div class="cont" >
						<h5 class="title">
							{{ item.name }}
							<button class="btn-del" v-if="item.records && item.records.filter(record=>record.editType!=3).length > 1" @click="delItem(item, records)">移除此条</button>
						</h5>
						<template v-for="val in records.fields">
							<van-cell
								class="label"
								:title="val.name"
								:required="val.mustFlag == 'Y'"
								v-if="
									( (val.value && val.alterFlag == 'Y') || (val.bj && val.bj == 'Y') || (val.showFlag === 'Y' ))

								"
								:key="val.code"
							>
								<template #right-icon>
									<template
										v-if="
											val.dataType == 3 ||
											val.dataType == 5 ||
											val.dataType == 6 ||
											val.dataType == 20 ||
											val.dataType == 4 ||
											val.dataType == 20 ||
											val.dataType == 4 ||
											val.dataType == 101 ||
											val.dataType == 102 ||
											val.dataType == 100
										"
									>
										<span
											@click="popupClick(val, item.code)"
											:class="{ mygray: val.canEditFlag == 'N' }"
											:style="val.canEditFlag == 'N' ? 'padding-right: 14px;' : ''"
										>
											{{ val.showValue ? val.showValue : '请选择' }}
										</span>
										<van-icon v-if="val.canEditFlag == 'Y'" name="arrow" style="line-height: 24px" :class="{ mygray: val.canEditFlag == 'N' }" />
									</template>
									<template v-else>
										<van-field
											v-model="val.value"
											:type="val.dataType == 1 ? 'digit' : val.dataType == 2 ? 'number' : 'text'"
											:readonly="val.canEditFlag != 'Y'"
											placeholder="请填写"
											@input="oninput(val)"
										/>
									</template>
								</template>
							</van-cell>
						</template>
					</div>
				</div>
				<div class="btn-add" v-if="item.addFlag == 'Y'" @click="addItem(item)"><van-icon :size="20" name="plus" /></div>
			</div>
		</template>
		<xy-empty description="暂无需填写数据" v-if="num == 0"></xy-empty>
		<div class="bottombtn sticky" v-if="num > 0">
			<div class="flex">
				<button @click="confirm">提交</button>
			</div>
		</div>
		<van-popup v-model="showForm" :style="{ width: '100%', height: '100%' }" position="right">
			<textFormComp :title="formTitle" :defaultText="defaultText" @exit="showForm = false" @done="doneEdit" />
		</van-popup>
		<van-popup v-model="popupshow" position="bottom">
			<van-picker title="" show-toolbar value-key="name" :columns="columns" @confirm="onConfirm" @cancel="onCancel" />
		</van-popup>
		<van-popup v-model="showPicker" class="pickerPopup" :overlay-style="{ background: 'rgba(120,120,120,.5)' }">
			<van-search
				shape="round"
				v-if="pickList.length > 20 || keyword.length > 0"
				style="padding: 0"
				v-model="keyword"
				:clearable="false"
				placeholder="输入关键字搜索"
				@search="onsearch"
			/>
			<template v-for="item in datas.pickData">
				<label @click="pick(item)" :key="item.id">
					<input type="radio" :value="item.id" v-model="radioValue" name="tempPicker" />
					<div class="flex middle">
						<i></i>
						<span>{{ item.name }}</span>
					</div>
				</label>
			</template>
		</van-popup>

		<van-popup v-model="showCalender" class="calenderPop" :overlay-style="{ background: 'rgba(120,120,120,.5)' }">
			<Calendar
				:default-date="new Date(thisdate)"
				:is-show-week-view="false"
				:mark-date="markDate"
				v-if="showCalender"
				@confirm="dateConfirm"
			></Calendar>
		</van-popup>
		<profiletree
			v-if="showFilterPicker"
			:showFilterPicker="showFilterPicker"
			@isshowFilterPicker="isshowFilterPicker"
			@clickSearch="clickSearch"
			:columns="profileTree"
		/>
	</div>
</template>

<script>
import { Toast } from 'vant'
import { userInfoIcon } from '@/libs/selfhelpdata.js'

import { getReference, personalInfoEdit, getPsnInfoDetail, infoNotCompleteGet,dateFormat} from '@/libs/api.js'
import profiletree from '@/components/profiletree'
export default {
	name: 'fillInfoForm',
	data() {
		return {
			defaultDatetime: new Date(),
			markDate: [],
			isdatehide: true,
			thisdate: dateFormat('YYYY-mm-dd', new Date()),
			keyword: '',
			pickList: [],
			infoIcon: [],
			infoNotComplete: {},
			infoComp: [],
			columns: [],
			datas: {
				pickData: [],
				cshy: [
					{ label: '计算机', value: '计算机' },
					{ label: '教育', value: '教育' },
					{ label: '销售', value: '销售' },
				],
				cs: [
					{ label: '测试', value: '测试' },
					{ label: '1', value: '1' },
					{ label: '2', value: '2' },
				],
			},
			title: '',
			editDataList: [],
			profileTree: [],
			code: '',
			parentCode: '',
			parentInfo: '',
			formTitle: null,
			defaultText: null,
			dateSelectShow: false,
			popupshow: false,
			showFilterPicker: false,
			showForm: false,
			showPicker: false,
			showCalender: false,
			num: 0,
			initList: [
				{
					id: 'Y',
					name: '是',
				},
				{
					id: 'N',
					name: '否',
				},
			],
			radioValue: '',
			toast: '',
			dataArr: [],
			fieldObj: {},
			currentField: {}, // 当前操作的字段
		}
	},
	components: {
		textFormComp: () => import('@/pages/forms/textFormComp'),
		calender: () => import('@/components/calenderPicker'),
		profiletree,
	},
	created() {
		this.infoIcon = userInfoIcon()
		this.init()
	},
	methods: {
		dateConfirm(date) {
			if (this.isdatehide) {
				console.log('dateConfirm====', date)
				let showValue =
					date.year + '-' + (date.month < 9 ? '0' + (date.month + 1) : date.month + 1) + '-' + (date.day < 10 ? '0' + date.day : date.day)

				this.currentField.showValue = showValue
				this.currentField.value = showValue
				this.currentField.alterFlag = 'Y'
				this.showCalender = false

				// this.info.map((item) => {
				// 	if (item.code == this.parentCode) {
				// 		item.records[0].fields.map((value) => {
				// 			if (value.code == this.code) {
				// 				let showValue =
				// 					date.year + '-' + (date.month < 9 ? '0' + (date.month + 1) : date.month + 1) + '-' + (date.day < 10 ? '0' + date.day : date.day)
				// 				value.showValue = showValue
				// 				value.value = showValue
				// 				value.alterFlag = 'Y'
				// 				this.showCalender = false
				// 			}
				// 		})
				// 	}
				// })
			} else {
				this.isdatehide = true
			}
		},
		oninput(val) {
			val.alterFlag = 'Y'
			val.bj = 'Y'
		},
		popupClick(value, item) {
			if (value.canEditFlag == 'N') {
				return
			}
			console.log('popupClick===', value, item)
			this.toast = Toast.loading({
				duration: 0,
				forbidClick: true,
				message: '加载中',
			})
			this.keyword = ''
			this.currentField = value
			if (value.value) {
				this.radioValue = value.value
			} else {
				this.radioValue = ''
			}
			if (!this.editDataList[value.code]) {
				this.code = value.code
				if (value.canEditFlag == 'Y' && value.showFlag == 'Y') {
					let datas = {}
					if (value.dataType == 5) {
						datas = {
							dataType: value.dataType,
							pkRefInfo: value.pkRefInfo,
							refModel: value.refModel,
						}
						this.getReference(datas, value.code)
					} else if (value.dataType == 6) {
						datas = {
							dataType: value.dataType,
							refModel: value.refModel,
						}
						this.getReference(datas, value.code)
					} else if (value.dataType == 20 || value.dataType == 3 || value.dataType == 101 || value.dataType == 102 || value.dataType == 100) {
						this.toast.clear()
						this.showCalender = true
						this.isdatehide = true
						if (value.value && value.value != '') {
							this.thisdate = value.value
						}
					} else if (value.dataType == 4) {
						this.editDataList[this.code] = { references: this.initList, dataType: 'LIST' }
						this.showlistortree(this.code)
					}
				}
			} else {
				this.showlistortree(value.code)
			}
		},
		showlistortree(code) {
			this.code = code
			if (this.editDataList[code].dataType == 'LIST') {
				// this.columns=this.editDataList[code].references
				// this.popupshow=true
				this.datas.pickData = this.editDataList[code].references
				this.pickList = JSON.parse(JSON.stringify(this.editDataList[code].references))
				this.showPicker = true
			} else if (this.editDataList[code].dataType == 'TREE') {
				this.profileTree = this.editDataList[code].references
				this.showFilterPicker = true
			}
			this.toast.clear()
		},
		getReference(datas, code) {
			getReference(datas).then((res) => {
				this.editDataList[code] = res.data.data
				this.showlistortree(code)
			})
		},
		pick(val) {
			console.log('pick====', val)
			this.currentField.showValue = val.name
			this.currentField.value = val.id
			this.currentField.alterFlag = 'Y'
			this.showFilterPicker = false
			this.showPicker = false
		},
		isshowFilterPicker(value) {
			this.showFilterPicker = value
		},
		clickSearch(val) {
			console.log('clickSearch====', val)
			this.currentField.showValue = val.name
			this.currentField.value = val.id
			this.currentField.alterFlag = 'Y'
			this.showFilterPicker = false
		},
		confirmDate(val) {
			console.log('confirmDate====', val)
			this.currentField.showValue = val.name
			this.currentField.value = val.id
			this.currentField.alterFlag = 'Y'
			this.showCalender = false
			// this.info.map((item) => {
			// 	if (item.code == this.parentCode) {
			// 		item.records[0].fields.map((value) => {
			// 			if (value.code == this.code) {
			// 				value.showValue = val
			// 				value.value = val
			// 				value.alterFlag = 'Y'
			// 				this.showCalender = false
			// 			}
			// 		})
			// 	}
			// })
		},
		onConfirm(val) {
			console.log(this.code)
			// this.info.records[0].fields.map(item=>{
			// 	if(item.code==this.code){
			// 		item.showValue=val.name
			// 		item.value=val.id
			// 		item.alterFlag='Y'
			// 		this.popupshow=false
			// 	}
			// })
		},
		onCancel() {
			this.popupshow = false
		},
		confirm() {
			console.log('confirm=dataArr===', this.dataArr)
			// 必填校验
			const isBreak = this.dataArr.some((info) => {
				console.log('info.==', info)
				const empty = info.records.find((record) => {
					// return !record.value
					const field = record.fields.find((field) => {
						// 信息补录，只校验必填的
						return field.mustFlag === 'Y' && !field.value && record.editType!=3
					})
					if (field) {
						Toast(info.name + '的' + field.name + ' 不能为空')
					}
					return field
				})
				console.log('info.records==', empty)
				return empty
			})

			if (!isBreak) {
				// console.log("this.originFilledDataArr",this.originFilledDataArr)
				// console.log("this.dataArr",this.dataArr)
				// // 字段是否修改判断 alterFlag,添加的逻辑，人员信息身高一直会被提示修改
				// if(this.originFilledDataArr&&this.originFilledDataArr.length){
				// 	this.dataArr.map(data=>{
				// 		data.records&&data.records.map(record=>{
				// 			record.fields&&record.fields.map(field=>{
				// 				this.originFilledDataArr.map(originRecord=>{
				// 					originRecord.fields&&originRecord.fields.map(originField=>{
				// 						if(field.pkInfoSet==originField.pkInfoSet&&field.code==originField.code){
				// 							field.alterFlag = field.value!==originField.value?'Y':'N';
				// 						}
				// 					})
				// 				})
				// 			})
				// 		})
				// 	})
				// }
				// 添加删除的记录
				this.deleteInfos&&this.deleteInfos.map(deleteInfo=>{
					this.dataArr.map(data=>{
						if(data.code == deleteInfo.code){
							data.records = data.records.concat(deleteInfo.records);
						}
					})
				})
				console.log("commit this.deleteInfos",this.deleteInfos)
				console.log("commit this.dataArr",this.dataArr)
        const familyIndex = this.dataArr.findIndex(item => {
          return item.code === 'hi_psndoc_family'
        })
        const eduIndex = this.dataArr.findIndex(item => {
          return item.code === 'hi_psndoc_edu'
        })
        if (familyIndex === -1 && eduIndex === -1) {
          // 参数校验完成提交
          personalInfoEdit({infos: this.dataArr}).then((res) => {
            if (res.data.statusCode === 200) {
              this.goback()
            }
          })
        }
        if (familyIndex !== -1 && eduIndex === -1) {
          personalInfoEdit({infos: [this.dataArr[familyIndex]]}).then(res => {
            if (res.data.statusCode === 200) {
              this.dataArr.splice(familyIndex, 1)
              personalInfoEdit({infos: this.dataArr}).then((res) => {
                if (res.data.statusCode === 200) {
                  this.goback()
                }
              })
            }
          })
        }
        if (familyIndex === -1 && eduIndex !== -1) {
          personalInfoEdit({infos: [this.dataArr[eduIndex]]}).then(res => {
            if (res.data.statusCode === 200) {
              this.dataArr.splice(eduIndex, 1)
              personalInfoEdit({infos: this.dataArr}).then((res) => {
                if (res.data.statusCode === 200) {
                  this.goback()
                }
              })
            }
          })
        }
        if (familyIndex !== -1 && eduIndex !== -1) {
          personalInfoEdit({infos: [this.dataArr[familyIndex]]}).then(res => {
            if (res.data.statusCode === 200) {
              this.dataArr.splice(familyIndex, 1)
              personalInfoEdit({infos: [this.dataArr[eduIndex]]}).then(res => {
                if (res.data.statusCode === 200) {
                  this.dataArr.splice(eduIndex, 1)
                  personalInfoEdit({infos: this.dataArr}).then((res) => {
                    if (res.data.statusCode === 200) {
                      this.goback()
                    }
                  })
                }
              })
            }
          })
        }
      }
		},
		init() {
			Toast.loading({
				duration: 0,
				forbidClick: true,
				message: '加载中',
			})
			getPsnInfoDetail().then((res) => {
				if (res.data.statusCode == 200) {
					Toast.clear()
					// 所有的信息模块
					const infos = res.data.data.infos
					infoNotCompleteGet().then((res) => {
						// 所有未填写的信息模块和模块必填的字段名
						const unFillInfo = res.data.data
						this.num = unFillInfo.length
						const dataArr = [] // 渲染的数据
						const fieldObj = {}
						let originFilledDataArr = []; // 用于校验哪项改动了
						unFillInfo.forEach((info) => {
							// 匹配
							const temp = infos.find((item) => {
								return info.infoName == item.name
							})
							// auditStatus = 1为待审核，不进行补充
							if (temp.auditStatus != 1 && temp.canEditFlag == 'Y') {
								// xywuxl 2022-02-28 这里需要展示全部字段，所以改成不过滤
								// const fieldTemplates = temp.fieldTemplates.filter((filed) => {
								// 	return info.fieldNames.indexOf(filed.name) > -1
								// })
								const fieldTemplates = temp.fieldTemplates
								// .filter((filed) => {
								// 	return filed.showFlag === 'Y' && filed.canEditFlag === 'Y'
								// })
								temp.fieldTemplates = fieldTemplates // 仅保存必填的
								if (temp.records.length > 0) {
									// 已生成的信息记录，此次补录为编辑，editType = 1
									// 需要把已填信息默认带入，比如家庭信息需要有两项,temp.records[0].fields
									let newRecords = []
									let newFilledRecords = []
									for (let index = 0; index < temp.records.length; index++) {
										const record = temp.records[index]
										const oldFields = record.fields
										// .filter((filed) => {
										// 	return filed.showFlag === 'Y'
										// })
										// 修改成已改动，在提交的时候遍历判断是否被修改
										// oldFields.map((filed) => {
										// 	filed.alterFlag = 'Y'
										// })
										newRecords.push({ recordId: record.recordId, fields: oldFields, editType: 1, tempId: Date.now() + index })
										newFilledRecords.push({ recordId: record.recordId, fields: oldFields, editType: 1, tempId: Date.now() + index })
									}
									console.log('newRecords', newRecords)
									originFilledDataArr = originFilledDataArr.concat(newFilledRecords);
									temp.records = newRecords.length
										? newRecords // 已经填过的记录
										: [{ fields: fieldTemplates, editType: 2, tempId: Date.now() }] // 补充一条信息记录
								} else {
									// 新增的补录信息
									temp.records = [{ fields: fieldTemplates, editType: 2, tempId: Date.now() }] // 补充一条信息记录
								}
								dataArr.push(temp)
								const tempField = fieldTemplates.map((field) => {
									// 对fieldTemplates拷贝
									return { ...field }
								})
								fieldObj[info.infoName] = tempField // 缓存字段信息数据，用于新增
							}
						})
						this.dataArr = dataArr
						this.fieldObj = fieldObj
						this.originFilledDataArr = originFilledDataArr;
						console.log('infos==', infos)
						console.log('unFillInfo==', unFillInfo)
						console.log('dataArr==', this.dataArr)
						console.log('fieldObj==', fieldObj)
						console.log('originFilledDataArr==', originFilledDataArr)
					})
				}
			})
		},
		addItem(info) {
			const fieldArr = this.fieldObj[info.name] // 获取该模块信息的fields
			const tempField = fieldArr.map((field) => {
				// 复制该模块信息的fields
				return { ...field }
			})
			const tempRecord = {
				fields: tempField,
				editType: 2,
				tempId: Date.now(),
			}
			info.records.push(tempRecord) // 补充一条信息记录
		},
		delItem(info, record) {
			// 删除匹配的数据项
			let deleteRecords = info.records.filter((item) => !item.recordId && item.recordId != null && item.recordId!=='' && item.tempId === record.tempId );
		  if(deleteRecords && deleteRecords.length > 0){
        let deleteInfos = this.deleteInfos?this.deleteInfos.filter((item) => item.pkFiledCode == info.pkFiledCode):[];
        // 标记为删除 editType：3
        deleteRecords&&deleteRecords.map(deleteRecord=>{
          deleteRecord.editType = 3;
        })
        if(deleteInfos&&deleteInfos.length){
          // 已有的信息项的删除记录
          deleteInfos[0].records = deleteInfos[0].records.concat(deleteRecords);
        }else{
          // 新的项的删除记录
          let newDeleteInfo = Object.assign({},info);
          newDeleteInfo.records = deleteRecords;
          deleteInfos.push(newDeleteInfo);
        }
        this.deleteInfos = deleteInfos;
        console.log("deleteInfos",deleteInfos)
      }
			console.log("info.records",info.records)
			const newRecords = info.records.filter((item) => item.tempId != record.tempId);
			console.log("info.newRecords",newRecords)
			info.records = newRecords;
			Toast('已移除一项' + info.name)
		},
		onsearch() {
			let data = []
			for (let i = 0; i < this.pickList.length; i++) {
				if (this.pickList[i].name.indexOf(this.keyword) != -1) {
					data.push(this.pickList[i])
				}
			}
			this.datas.pickData = data
		},
		doneEdit(val) {
			this.form[this.editKey] = val
			this.showForm = false
		},
		goback() {
			this.$router.go(-1)
		},
	},
}
</script>
<style scoped>
.group {
	padding: 0 !important;
	border-bottom: none !important;
}
.title {
	display: flex;
	justify-content: space-between;
	height: 30px;
	padding-top: 10px;
	line-height: unset !important;
}
.btn-del {
	flex-shrink: 0;
	font-size: 12px;
	margin-right: 16px;
	background: #cf3633;
	border: none;
	color: #fff;
	padding: 0 8px;
	border-radius: 4px;
	font-weight: 400;
	height: 20px;
}
.label {
	padding: 8px 20px !important;
	margin-left: -20px !important;
}
.flex.row {
	padding: 10px 20px;
	border-bottom: 8px solid #f5f5f5;
}
.btn-add {
	width: 100%;
	height: 40px;
	display: flex;
	justify-content: center;
	align-items: center;
	color: #cf3633;
	margin: 0 auto;
	border-bottom: 8px solid #f5f5f5;
}
</style>
