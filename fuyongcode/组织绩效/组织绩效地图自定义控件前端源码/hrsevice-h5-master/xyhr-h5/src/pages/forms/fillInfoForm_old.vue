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
		<template v-for="(item, index) in info">
			<div class="group" v-if="infoComp.indexOf(item.name) != -1 && item.biaoji" :key="index">
				<div class="flex row">
					<i
						class="iconfont"
						:style="{ color: infoIcon[item.code] ? infoIcon[item.code].color : '#7FCD93' }"
						v-html="infoIcon[item.code] ? infoIcon[item.code].text : '&#xe6a9;'"
					></i>
					<div class="cont">
						<h5 class="title">{{ item.name }}</h5>
						<template v-for="val in item.records[0].fields">
							<van-cell
								:title="val.name"
								v-if="
									(infoNotComplete[item.name].indexOf(val.name) != -1 &&
										((!val.value && val.alterFlag == 'N') || (val.value && val.alterFlag == 'Y'))) ||
									(val.bj && val.bj == 'Y')
								"
								:key="val.value"
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
										<span @click="popupClick(val, item.code)" :class="{ myred: !val.showValue, mygray: val.canEditFlag == 'N' }">
											{{ val.showValue ? val.showValue : '请选择' }}
										</span>
										<van-icon name="arrow" style="line-height: 24px" :class="{ myred: !val.showValue, mygray: val.canEditFlag == 'N' }" />
									</template>
									<template v-else>
										<van-field
											v-model="val.value"
											:type="val.dataType == 1 ? 'digit' : val.dataType == 2 ? 'number' : 'text'"
											:readonly="val.canEditFlag != 'Y'"
											class="fieldPlaceholder"
											placeholder="请完善"
											@input="oninput(val)"
										/>
									</template>
								</template>
							</van-cell>
						</template>
					</div>
				</div>
			</div>
		</template>
		<xy-empty description="暂无需填写数据" v-if="num == 0"></xy-empty>
		<div class="bottombtn sticky" v-if="num > 0">
			<div class="flex">
				<button @click="confirm()">提交</button>
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

import { getReference, personalInfoEdit, infoNotCompleteGet } from '@/libs/api.js'
import profiletree from '@/components/profiletree'
export default {
	name: 'fillInfoForm',
	data() {
		return {
			defaultDatetime: new Date(),
			markDate: [],
			isdatehide: true,
			thisdate: '2021-01-01',
			keyword: '',
			pickList: [],
			info: [],
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
			info: {},
			editDataList: [],
			profileTree: [],
			code: '',
			parentCode: '',
			parentInfo: '',
			editDataList: {},
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
		}
	},
	components: {
		textFormComp: () => import('@/pages/forms/textFormComp'),
		calender: () => import('@/components/calenderPicker'),
		profiletree,
	},
	created() {
		this.infoIcon = userInfoIcon()
		this.info = this.$route.params.info
		console.log('created==', this.info)
		this.init()
	},
	methods: {
		dateConfirm(date) {
			if (this.isdatehide) {
				this.info.map((item) => {
					if (item.code == this.parentCode) {
						item.records[0].fields.map((value) => {
							if (value.code == this.code) {
								let showValue =
									date.year + '-' + (date.month < 9 ? '0' + (date.month + 1) : date.month + 1) + '-' + (date.day < 10 ? '0' + date.day : date.day)
								value.showValue = showValue
								value.value = showValue
								value.alterFlag = 'Y'
								this.showCalender = false
							}
						})
					}
				})
			} else {
				this.isdatehide = true
			}
		},
		oninput(val) {
			val.alterFlag = 'Y'
			val.bj = 'Y'
		},
		popupClick(value, item) {
			this.toast = Toast.loading({
				duration: 0,
				forbidClick: true,
				message: '加载中',
			})
			this.keyword = ''
			if (value.value) {
				this.radioValue = value.value
			} else {
				this.radioValue = ''
			}
			if (!this.editDataList[value.code]) {
				this.code = value.code
				this.parentCode = item
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
			// this.info.fields.map(item=>{
			// 	if(item.code==this.code){
			// 		console.log(item.showValue)
			// 		item.showValue=val.name
			// 		item.value=val.id
			// 		item.alterFlag='Y'
			// 	}
			// })
			this.info.map((item) => {
				if (item.code == this.parentCode) {
					item.records[0].fields.map((value) => {
						if (value.code == this.code) {
							value.showValue = val.name
							value.value = val.id
							value.alterFlag = 'Y'
							this.showFilterPicker = false
						}
					})
				}
			})
			this.showPicker = false
		},
		isshowFilterPicker(value) {
			this.showFilterPicker = value
		},
		clickSearch(val) {
			this.info.map((item) => {
				if (item.code == this.parentCode) {
					item.records[0].fields.map((value) => {
						if (value.code == this.code) {
							value.showValue = val.name
							value.value = val.id
							value.alterFlag = 'Y'
							this.showFilterPicker = false
						}
					})
				}
			})
		},
		confirmDate(val) {
			this.info.map((item) => {
				if (item.code == this.parentCode) {
					item.records[0].fields.map((value) => {
						if (value.code == this.code) {
							value.showValue = val
							value.value = val
							value.alterFlag = 'Y'
							this.showCalender = false
						}
					})
				}
			})
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
			let jishu = 0
			this.info.map((item) => {
				item.records[0].fields.map((val) => {
					if (this.infoNotComplete[item.name] && this.infoNotComplete[item.name].indexOf(val.name) != -1) {
						console.log(111)
						if (!val.value || (val.value && val.value == '')) jishu++
					}
				})
				if (this.infoComp.indexOf(item.name) != -1) {
					if (!item.records[0].editType) {
						item.records[0].editType = 1
					}
				}
			})

			console.log(this.info)
			if (jishu > 0) {
				Toast('请完善所有数据')
				return
			}
			console.log(this.info)
			this.isedit()
		},
		isedit() {
			console.log(this.info)
			personalInfoEdit({ infos: this.info }).then((res) => {
				console.log(res.data)
				// this.$store.commit("setisNeedNewData",this.parentInfo.code);
				this.goback()
			})
		},
		init() {
			infoNotCompleteGet().then((res) => {
				console.log('infoNotCompleteGet==', res.data)
				res.data.data.map((item) => {
					this.infoNotComplete[item.infoName] = item.fieldNames
					this.infoComp.push(item.infoName)
				})
				console.log('infoNotComplete==', this.infoNotComplete)
				console.log('infoComp==', this.infoComp)
				let data = this.$route.params.info
				for (let i = data.length - 1; i >= 0; i--) {
					if (data[i].records.length == 0 && this.infoComp.indexOf(data[i].name) != -1) {
						data[i].records = [{ fields: data[i].fieldTemplates, editType: 2 }]
					}
					if ((data[i].auditStatus && data[i].auditStatus == 1) || data[i].canEditFlag == 'N' || this.infoComp.indexOf(data[i].name) == -1) {
						data.splice(i, 1)
					}
				}
				this.info = data
				this.num = 0
				console.log('info==', this.info)
				this.info.map((item) => {
					if (this.infoComp.indexOf(item.name) != -1) {
						if (item.records.length > 0) {
							let biaoji = 0
							item.records[0].fields.map((val) => {
								if (this.infoNotComplete[item.name].indexOf(val.name) != -1 && !val.value) {
									this.num++
									biaoji++
								}
							})
							if (biaoji > 0) {
								item.biaoji = 1
							}
						}
					}
				})
				console.log('info=finally=', this.info)
				if (this.num == 0) {
					return
				}
			})
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
.btn-add {
	width: 200px;
	height: 40px;
	display: flex;
	justify-content: center;
	align-items: center;
	color: #cf3633;
	margin: 0 auto;
}
</style>