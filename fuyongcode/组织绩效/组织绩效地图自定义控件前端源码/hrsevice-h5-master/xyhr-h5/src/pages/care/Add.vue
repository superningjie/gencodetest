<template>
	<!-- 申请展示内容 -->
	<div class="content-box apply-box">
		<!-- 基础信息 -->
		<div class="content-item">
			<van-cell :value="careData.careTypeName || '必选'" is-link @click="$refs.CareType.show()">
				<template #title>
					<span class="iconfont cell-icon" style="color: #ff9700">&#xe6b2;</span>
					<span class="custom-title">关怀类型</span>
				</template>
			</van-cell>
			<van-cell>
				<template #title>
					<span class="iconfont cell-icon" style="color: rgb(8, 190, 255)">&#xe6be;</span>
					<span class="custom-title">申请说明</span>
				</template>
			</van-cell>
			<van-field v-model="careData.applicantDesc" rows="3" autosize type="textarea" :maxlength="100" placeholder="必填，请输入.." show-word-limit />
			<van-cell :value="careData.applicant">
				<template #title>
					<span class="iconfont defind-img">
						<img class="" src="../../assets/care/care_people.png" >
					</span>
					<span class="custom-title">申请人</span>
				</template>
			</van-cell>
			<van-cell :value="careData.byCare || '必选且单选'" is-link @click="showUserlist = true">
				<template #title>
					<span class="iconfont defind-img">
						<img src="../../assets/care/cared_people.png" >
					</span>
					<span class="custom-title">被关怀人</span>
				</template>
			</van-cell>
		</div>
		<!-- 生育 -->
		<div v-show="careData.careType === 'BIRTH'">
			<!-- 子女信息 -->
			<div class="content-item" v-for="(child, index) in children" :key="child.id">
				<van-cell class="defind">
					<template #title>
						<span class="iconfont cell-icon" style="color: rgb(129, 206, 149)">&#xe69f;</span>
						<span class="custom-title">子女姓名</span>
					</template>
					<van-field v-model="child.name" placeholder="必填" input-align="right"></van-field>
				</van-cell>
				<van-cell :value="child.relationName || '必选'" is-link @click="showRelation(child)">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">与被关怀人关系</span>
					</template>
				</van-cell>
				<van-cell :value="child.birthdate || '必选'" is-link @click="showTime(child)">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">出生日期</span>
					</template>
				</van-cell>
				<van-cell :value="child.isInForeignName || '必选'" is-link @click="showInChina(child)">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">是否在国外</span>
					</template>
				</van-cell>
				<div class="delete-icon" v-if="index != 0">
					<van-icon name="delete-o" @click="deletePeople(index)" />
				</div>
			</div>
			<!-- 添加按钮 -->
			<div class="content-item">
				<div class="add-btn" @click="addPeople">
					<i class="iconfont add-icon">&#xe6b0;</i>
					<span>增加家庭成员</span>
				</div>
			</div>
		</div>
		<!-- 结婚 -->
		<div v-show="careData.careType === 'MARRY'">
			<div class="content-item">
				<van-cell :value="marryData.relationName || '必选'" is-link @click="showRelation(marryData)">
					<template #title>
						<span class="iconfont cell-icon" style="color: rgb(129, 206, 149)">&#xe69f;</span>
						<span class="custom-title">与被关怀人关系</span>
					</template>
				</van-cell>
				<van-cell class="defind">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title"> 家庭成员姓名</span>
					</template>
					<van-field v-model="marryData.name" placeholder="必填" input-align="right"></van-field>
				</van-cell>
				<van-cell :value="marryData.birthdate || '必选'" is-link @click="showTime(marryData)">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">出生日期</span>
					</template>
				</van-cell>
				<van-cell class="defind">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">工作单位</span>
					</template>
					<van-field v-model="marryData.corp" placeholder="必填" input-align="right"></van-field>
				</van-cell>
				<van-cell class="defind">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">职务</span>
					</template>
					<van-field v-model="marryData.job" placeholder="必填" input-align="right"></van-field>
				</van-cell>
				<van-cell class="defind">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">联系电话</span>
					</template>
					<van-field v-model="marryData.tel" placeholder="请输入" input-align="right"></van-field>
				</van-cell>
				<van-cell :value="marryData.politicsName || '必选'" is-link @click="$refs.Politics.show()">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">政治面貌</span>
					</template>
				</van-cell>
				<van-cell :value="marryData.isInOfficeName || '必选'" is-link @click="$refs.Xywork.show()">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">是否在XXXX任职</span>
					</template>
				</van-cell>
				<van-cell :value="marryData.isInForeignName || '必选'" is-link @click="showInChina(marryData)">
					<template #title>
						<span class="iconfont empty-span"></span>
						<span class="custom-title">是否在国外</span>
					</template>
				</van-cell>
			</div>
		</div>
		<!-- 附件 -->
		<div class="content-item" style="padding: 10px 20px">
			<Uploader ref="UploaderAdd" @afterRead="afterRead" />
		</div>
		<div class="content-item">
			<div class="explain-box">
				<p>说明：</p>
				<p>1、生育福利申请需提供出生证明，子女信息需要维护，如果暂时没有取名字可以使用姓名代替（例如 张某某）。</p>
				<p>2、结婚福利申请需提供结婚证明。</p>
				<p>3、生病住院慰问申请需提供病历材料。</p>
			</div>
		</div>
		<!-- 底部按钮框 -->
		<div class="bottom-box">
			<div class="btn-box">
				<div class="btn-left" @click="saveData(false)">保存</div>
				<div class="btn-right" @click="saveData(true)">提交</div>
			</div>
		</div>
		<!-- 人员选择 -->
		<van-popup v-model="showUserlist" :style="{ width: '100%', height: '100%' }" position="right">
			<userlist ref="UserPcikAdd" title="被关怀人" @closeList="showUserlist = false" @pickuser="pickuser" />
		</van-popup>
		<!-- 出生日期弹窗 -->
		<TimeSelect ref="TimeSelect" @change="confirmTime" />
		<!-- 关怀类型 -->
		<SingleSelect ref="CareType" :params="{ key: 'name', value: 'type' }" :list="typeList" @change="selectType" />
		<!-- 与被关怀人关系 -->
		<SingleSelect ref="Relation" :list="relationList" @change="selectRelation" />
		<!-- 政治面貌 -->
		<SingleSelect ref="Politics" :list="enumeration.politics" @change="selectPolitics" />
		<!-- 是否在XXXX任职 -->
		<SingleSelect ref="Xywork" :list="enumeration.glbdef3" @change="selectXywork" />
		<!-- 是否在国外 -->
		<SingleSelect ref="InChina" :list="enumeration.glbdef4" @change="selectInChina" />
	</div>
</template>
<script>
import { getCareType, getUserSetting, getReference, saveCare, submitCare, upload } from '@/libs/api.js'
import Dayjs from 'dayjs'
import { Toast } from 'vant'
export default {
	name: 'CARE',
	components: {
		userlist: () => import('@/pages/selfhelp/evectionPerson'),
		SingleSelect: () => import('@/components/base/SingleSelect'),
		TimeSelect: () => import('@/components/base/TimeSelect'),
		Uploader: () => import('@/components/uploader'),
	},
	data() {
		return {
			activeTab: 'detail', // apply,detail
			showUserlist: false,
			typeList: [],
			careData: {
				careType: '', //关怀类型【必须】
				careTypeName: '', //关怀类型名称【必须】
				applicant: '', //申请人【必须】
				applicantPk: '', //申请人主键【必须】
				applicantDesc: '', //申请说明
				byCare: '', //被关怀人【必须】
				byCarePk: '', //被关怀人主键【必须】
				byCarePkJob: '', //被关怀人工作记录主键【必须】
				detailList: [], // 详细
			},
			children: [
				// 生子
				{
					id: Date.now(),
					name: '', //姓名
					relation: '', //关系主键
					relationName: '', //关系名称
					birthdate: Dayjs().format('YYYY-MM-DD'), //出生日期
					corp: '', //工作单位
					job: '', //职务
					tel: '', //联系电话
					politics: '', //政治面貌
					politicsName: '', //政治面貌名称
					isInOffice: '', //是否在XXXX任职,N=否 Y=是
					isInOfficeName: '',
					isInForeign: '', //是否在国外,N=否 Y=是
					isInForeignName: '',
				},
			],
			marryData: {
				// 结婚详细数据
				name: '', //姓名
				relation: '', //关系主键
				relationName: '', //关系名称
				birthdate: Dayjs().format('YYYY-MM-DD'), //出生日期
				corp: '', //工作单位
				job: '', //职务
				tel: '', //联系电话
				politics: '', //政治面貌
				politicsName: '', //政治面貌名称
				isInOffice: '', //是否在XXXX任职,N=否 Y=是
				isInOfficeName: '',
				isInForeign: '', //是否在国外,N=否 Y=是
				isInForeignName: '',
			},
			timeTemp: { birthdate: '' }, // 出生日期缓存
			relationTemp: {}, // 与被关怀人关系缓存
			inChinaTemp: {}, // 是否在缓存
			fileList: [], // 上传所需file
			enumeration: {
				mem_relation: [], //"与被关怀人关系"
				politics: [], //"政治面貌"
				glbdef3: [], //"是否在XXXX任职"
				glbdef4: [], //"是否在国外"
			},
			relationList: [], // 过滤后的关系数组
		}
	},
	computed: {
		userInfo() {
			return this.$store.state.userInfo
		},
	},
	mounted() {
		console.log('userInfo==', this.userInfo)
		this.careData.applicant = this.userInfo.name
		this.careData.applicantPk = this.userInfo.pk_psndoc
		this.getCareType()
		this.getUserSetting()
	},
	methods: {
		goback() {
			this.$router.go(-1)
		},
		addPeople() {
			this.children.push({
				id: Date.now(),
				name: '',
				relation: '',
				relationName: '',
				birthdate: Dayjs().format('YYYY-MM-DD'),
				corp: '',
				job: '',
				tel: '',
				politics: '',
				politicsName: '',
				isInOffice: '',
				isInOfficeName: '',
				isInForeign: '',
				isInForeignName: '',
			})
		},
		deletePeople(index) {
			this.$dialog
				.confirm({
					message: '确认删除该条信息吗？',
				})
				.then(() => {
					this.children.splice(index, 1)
				})
		},
		showTime(item) {
			this.timeObj = item
			this.$refs.TimeSelect.show(item.birthdate)
		},
		showRelation(item) {
			this.relationTemp = item
			this.$refs.Relation.show(item.relation || 'init')
		},
		showInChina(item) {
			this.inChinaTemp = item
			this.$refs.InChina.show(item.isInForeign || 'init')
		},
		confirmTime(value) {
			this.timeObj.birthdate = value
		},
		getCareType() {
			getCareType().then((res) => {
				this.typeList = res.data.data
				console.log('===', res.data.data)
			})
		},
		getUserSetting() {
			getUserSetting(this.careData.applicantPk).then((res) => {
				console.log('getUserSetting==', res)
				if (res.data.statusCode == 200) {
					// 获取家庭信息
					const family = res.data.data.find((item) => {
						return item.code == 'hi_psndoc_family'
					})
					// 获取对应的配置数据
					Object.keys(this.enumeration).forEach((key) => {
						console.log('key==', key)
						const temp = family.fieldTemplates.find((params) => {
							return params.code == key
						})
						getReference({
							dataType: temp.dataType,
							pkRefInfo: temp.pkRefInfo,
							refModel: temp.refModel,
						}).then((emData) => {
							if (emData.data.statusCode == 200) {
								this.enumeration[key] = emData.data.data.references
							}
						})
					})
				}
			})
		},
		selectType(item) {
			this.careData.careType = item.type
			this.careData.careTypeName = item.name
			if (item.type == 'BIRTH') {
				this.relationList = this.enumeration.mem_relation.filter((item) => {
					return item.name == '儿子' || item.name == '女儿'
				})
			}
			if (item.type == 'MARRY') {
				this.relationList = this.enumeration.mem_relation.filter((item) => {
					return item.name == '丈夫' || item.name == '妻子'
				})
			}
		},
		selectRelation(item) {
			this.relationTemp.relation = item.id
			this.relationTemp.relationName = item.name
		},
		selectPolitics(item) {
			this.marryData.politics = item.id
			this.marryData.politicsName = item.name
		},
		selectXywork(item) {
			this.marryData.isInOffice = item.id
			this.marryData.isInOfficeName = item.name
		},
		selectInChina(item) {
			this.inChinaTemp.isInForeign = item.id
			this.inChinaTemp.isInForeignName = item.name
		},
		pickuser(userList) {
			if (userList.length == 1) {
				const user = userList[0]
				const pkArr = user.pk.split(',')
				console.log('pickuser==', user.name, pkArr)
				this.careData.byCare = user.name
				this.careData.byCarePk = pkArr[0]
				this.careData.byCarePkJob = pkArr[1]
			}
			if (userList.length > 1) {
				Toast('只能选择一个用户')
				return false
			}
		},
		saveData(isSubmit) {
			const isContinue = this.checkimport()
			if (!isContinue) {
				return false
			}
			console.log('saveData==', this.careData)
			Toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			saveCare(this.careData).then((res) => {
				if (res.data.statusCode == 200) {
					console.log('fileList==', this.fileList.length)
					const pkCode = res.data.data // 单据id
					if (this.fileList.length > 0) {
						//上传文件
						const promiseArr = this.fileList.map((item) => {
							const formData = new FormData()
							formData.append('file', item.file)
							formData.append('filepath', pkCode)
							formData.append('grouptype', '')
							return upload(formData)
						})
						Promise.all(promiseArr).then((responseFile) => {
							const isSuccess = responseFile.every((item) => {
								return item.data.statusCode == 200
							})
							if (isSuccess) {
								Toast.loading({
									type: 'success',
									message: '保存成功',
									duration: 1000,
								})
							} else {
								Toast.loading({
									type: 'success',
									message: '附件保存失败',
									duration: 1000,
								})
							}
						})
					} else {
						Toast.loading({
							type: 'success',
							message: '保存成功',
							duration: 1000,
						})
					}
					if (isSubmit) {
						this.$emit('submit-click', pkCode)
					} else {
						this.$emit('success', pkCode)
					}
					this.init()
				}
			})
		},
		submitData(data) {
			const submitData = {
				id: data.pkH,
				nodeId: data.nodeId,
				approveNodeIds: data.operatorId,
			}
			console.log('saveData==', this.careData)
			Toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			submitCare(submitData).then((res) => {
				if (res.data.statusCode == 200) {
					Toast.loading({
						type: 'success',
						message: '提交成功',
						duration: 1000,
					})
					this.$emit('success', res.data.data)
				}
			})
		},
		afterRead(list) {
			console.log('afterRead==', list)
			this.fileList = list
		},
		checkimport() {
			const careMessage = [
				{
					key: 'careType',
					message: '关怀类型不能为空',
				},
				{
					key: 'applicantPk',
					message: '申请人不能为空',
				},
				{
					key: 'byCarePk',
					message: '被关怀人不能为空',
				},
				{
					key: 'applicantDesc',
					message: '申请说明不能为空',
				},
			]
			const message = careMessage.find((item) => {
				return !Boolean(this.careData[item.key])
			})
			if (message) {
				// 存在未填写
				Toast(message.message)
				return false
			}
			if (this.careData.careType === 'MARRY') {
				const marryMessage = [
					{
						key: 'name',
						message: '家庭成员姓名不能为空',
					},
					{
						key: 'relation',
						message: '与被关怀人关系不能为空',
					},
					{
						key: 'corp',
						message: '工作单位不能为空',
					},
					{
						key: 'job',
						message: '职务不能为空',
					},
					{
						key: 'birthdate',
						message: '出生日期不能为空',
					},
					{
						key: 'politics',
						message: '政治面貌不能为空',
					},
					{
						key: 'isInOffice',
						message: '是否在XXXX任职不能为空',
					},
					{
						key: 'isInForeign',
						message: '是否在国外不能为空',
					},
				]
				const message = marryMessage.find((item) => {
					return !Boolean(this.marryData[item.key])
				})
				if (message) {
					// 存在未填写
					Toast(message.message)
					return false
				}
			} else if (this.careData.careType === 'BIRTH') {
				const birthMessage = [
					{
						key: 'name',
						message: '子女姓名不能为空',
					},
					{
						key: 'relation',
						message: '与被关怀人关系不能为空',
					},
					{
						key: 'birthdate',
						message: '出生日期不能为空',
					},
					{
						key: 'isInForeign',
						message: '是否在国外不能为空',
					},
				]
				const isStop = this.children.some((child) => {
					const message = birthMessage.find((item) => {
						return !Boolean(child[item.key])
					})
					if (message) {
						// 存在未填写
						Toast(message.message)
					}
					return message
				})
				if (isStop) {
					return false
				}
			}
			// 校验通过补充careData.detailList数据
			if (this.careData.careType == 'MARRY') {
				this.careData.detailList = [this.marryData]
			} else if (this.careData.careType == 'BIRTH') {
				this.careData.detailList = this.children
			} else {
				this.careData.detailList = []
			}
			return true
		},
		init() {
			this.$refs.UploaderAdd.clear()
			this.$refs.UserPcikAdd.init()
			this.careData = {
				careType: '', //关怀类型【必须】
				careTypeName: '', //关怀类型名称【必须】
				applicant: this.userInfo.name, //申请人【必须】
				applicantPk: this.userInfo.pk_psndoc, //申请人主键【必须】
				applicantDesc: '', //申请说明
				byCare: '', //被关怀人【必须】
				byCarePk: '', //被关怀人主键【必须】
				byCarePkJob: '', //被关怀人工作记录主键【必须】
				detailList: [], // 详细
			}
			this.children = [
				// 生子
				{
					id: Date.now(),
					name: '', //姓名
					relation: '', //关系主键
					relationName: '', //关系名称
					birthdate: Dayjs().format('YYYY-MM-DD'), //出生日期
					corp: '', //工作单位
					job: '', //职务
					tel: '', //联系电话
					politics: '', //政治面貌
					politicsName: '', //政治面貌名称
					isInOffice: '', //是否在XXXX任职,N=否 Y=是
					isInForeign: '', //是否在国外,N=否 Y=是
				},
			]
			this.marryData = {
				// 结婚详细数据
				name: '', //姓名
				relation: '', //关系主键
				relationName: '', //关系名称
				birthdate: Dayjs().format('YYYY-MM-DD'), //出生日期
				corp: '', //工作单位
				job: '', //职务
				tel: '', //联系电话
				politics: '', //政治面貌
				politicsName: '', //政治面貌名称
				isInOffice: '', //是否在XXXX任职,N=否 Y=是
				isInOfficeName: '',
				isInForeign: '', //是否在国外,N=否 Y=是
				isInForeignName: '',
			}
		},
	},
}
</script>
<style lang='less' scoped>
.apply-box {
	padding-bottom: 80px;
}
.tab-box {
	margin: 20px 20px 5px 20px;
	display: flex;
	box-shadow: 0px 0px 4px 2px rgb(0 0 0 / 5%);
	background-color: white;
	border-radius: 30px;
	overflow: hidden;
	.btn-base {
		width: 50%;
		text-align: center;
		line-height: 30px;
		font-size: 16px;
		font-weight: 600;
		background: #fff;
		&.active {
			background: #cf3633;
			color: #fff;
		}
	}
}
.content-box {
	.content-item {
		border-bottom: 8px solid #f5f5f5;
		.cell-icon {
			font-size: 20px;
			padding-right: 10px;
			color: #ff9700;
		}
		.empty-span {
			display: inline-block;
			width: 30px;
		}
		.custom-title {
			font-size: 15px;
			color: black;
		}
		.defind-img{
			display: flex;
			width: 30px;
			img{
				width: 16px;
				height: 16px;
			}
		}
		/deep/ .van-cell__title {
			display: flex;
			align-items: center;
		}
	}
	.add-btn {
		color: #d0332f;
		height: 60px;
		width: 100%;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 14px;
		.add-icon {
			font-size: 20px;
			margin-right: 10px;
		}
	}
	.file-title {
		display: flex;
		padding: 10px 20px;
		align-items: center;
		justify-content: space-between;
		.title {
			display: flex;
			align-items: center;
		}
		.icon {
			font-size: 20px;
			color: #d0332f;
		}
	}
	.explain-box {
		color: #7f7f7f;
		font-size: 14px;
		padding: 10px;
		p {
			line-height: 20px;
			margin-bottom: 5px;
			word-break: break-all;
			text-align: justify;
		}
	}
}
.bottom-box {
	z-index: 99;
	background: #fff;
	border-top: 1px solid #e9e9e9;
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 0 20px;
	padding-bottom: 30px;
	padding-top: 9px;
	.btn-box {
		width: 100%;
		display: flex;
		justify-content: center;
		.btn-left,
		.btn-right {
			width: 48%;
			height: 36px;
			font-size: 16px;
			border-radius: 12px;
			font-weight: bold;
			background-color: #cf3633;
			color: white;
			display: flex;
			align-items: center;
			justify-content: center;
		}
		.btn-left {
			border-top-right-radius: 0;
			border-bottom-right-radius: 0;
		}
		.btn-right {
			border-top-left-radius: 0;
			border-bottom-left-radius: 0;
			margin-left: 2px;
		}
	}
}
.type-box {
	.type-item {
		line-height: 40px;
		text-align: center;
		font-size: 15px;
		&.cancel {
			border-top: 6px solid #f5f5f5;
			color: gray;
		}
		&.active {
			color: #cf3633;
		}
	}
}
.van-cell {
	padding: 10px 20px;
}
.defind .van-cell.van-field {
	padding: 0;
}
.delete-icon {
	height: 36px;
	display: flex;
	align-items: center;
	justify-content: flex-end;
	padding: 0 20px;
	font-size: 20px;
	color: #cf3633;
}
/deep/ textarea {
	text-indent: 28px;
}
.van-uploader {
	width: 100%;
	/deep/ .van-uploader__input-wrapper {
		width: 100%;
	}
}
.remind-text {
	font-size: 16px;
	color: #cf3633;
	padding-right: 4px;
}
</style>
