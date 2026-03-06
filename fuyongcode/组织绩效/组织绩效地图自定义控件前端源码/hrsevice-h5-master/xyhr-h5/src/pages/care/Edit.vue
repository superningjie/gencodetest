<template>
	<van-popup v-model="isShow" position="right" class="popup-box">
		<van-nav-bar title="关怀申请" left-arrow class="navStyle" @click-left="close"></van-nav-bar>
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
							<img class="" src="../../assets/care/care_people.png" />
						</span>
						<span class="custom-title">申请人</span>
					</template>
				</van-cell>
				<van-cell :value="careData.byCare || '必选且单选'" is-link @click="showUserlist = true">
					<template #title>
						<span class="iconfont defind-img">
							<img src="../../assets/care/cared_people.png" />
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
							<span class="custom-title">家庭成员姓名</span>
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
				<Uploader ref="UploaderEdit" :defList="fileList" @afterRead="afterRead" @deleteFile="deleteFile" />
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
				<userlist ref="UserPcikEdit" title="被关怀人" @closeList="showUserlist = false" @pickuser="pickuser" />
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
	</van-popup>
</template>
<script>
import { getCareType, getUserSetting, getReference, saveCare, getCareDetail, fileList, fileDelete, upload } from '@/libs/api.js'
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
			isShow: false,
			showType: false,
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
			children: [],
			marryData: {
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
			inChinaTemp: {}, // 是否在国外缓存
			fileList: [], // 初始化文件数组
			uploadFileList: [], // 需上传文件数组
			delFileList: [], // 需删除文件数组
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
	methods: {
		async show(id) {
			this.careData.applicant = this.userInfo.name
			this.careData.applicantPk = this.userInfo.pk_psndoc
			if (this.typeList == 0) {
				this.getCareType()
				await this.getUserSetting()
			}
			this.isShow = true
			this.getData(id)
		},
		getData(id) {
			Toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			getCareDetail(id).then((res) => {
				if (res.data.statusCode == 200) {
					const data = res.data.data
					data.id = id // 补充id
					this.careData = data
					if (data.careType == 'MARRY') {
						const tempData = data.detailList[0] // 结婚数据为1条
						tempData.isInOfficeName = tempData.isInOffice == 'Y' ? '是' : '否'
						tempData.isInForeignName = tempData.isInForeign == 'Y' ? '是' : '否'
						this.marryData = Object.assign({}, tempData)
						this.$nextTick(() => {
							this.$refs.Politics.init(this.marryData.politics)
							this.$refs.Xywork.init(this.marryData.isInOffice)
							this.$refs.InChina.init(this.marryData.isInForeign)
						})
					}
					if (data.careType == 'BIRTH') {
						this.children = data.detailList.map((item) => {
							item.isInOfficeName = item.isInOffice == 'Y' ? '是' : '否'
							item.isInForeignName = item.isInForeign == 'Y' ? '是' : '否'
							return Object.assign({}, item)
						})
					}
					this.changeRelationList(data.careType)
					Toast.clear()
				}
			})
			const data_ = new FormData()
			data_.append('filePath', id)
			fileList(data_).then((res) => {
				this.fileList = res.data.data || []
				console.log('fileList==', this.fileList)
			})
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
				console.log('typeList===', res.data.data)
			})
		},
		getUserSetting() {
			return new Promise((resolve) => {
				getUserSetting(this.careData.applicantPk).then((res) => {
					console.log('getUserSetting==', res)
					if (res.data.statusCode == 200) {
						// 获取家庭信息
						const family = res.data.data.find((item) => {
							return item.code == 'hi_psndoc_family'
						})
						// 获取对应的配置数据
						Object.keys(this.enumeration).forEach((key) => {
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
					resolve(true)
				})
			})
		},
		selectType(item) {
			this.careData.careType = item.type
			this.careData.careTypeName = item.name
			this.changeRelationList(item.type)
		},
		changeRelationList(type) {
			if (type == 'BIRTH') {
				this.relationList = this.enumeration.mem_relation.filter((item) => {
					return item.name == '儿子' || item.name == '女儿'
				})
			}
			if (type == 'MARRY') {
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
			console.log('saveData==', this.careData)
			const isContinue = this.checkimport()
			if (!isContinue) {
				return false
			}
			Toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			saveCare(this.careData).then(async (res) => {
				if (res.data.statusCode == 200) {
					const pkCode = res.data.data
					Toast.loading({
						type: 'success',
						message: '保存成功',
						duration: 1000,
					})
					if (this.delFileList.length > 0) {
						// 删除文件 , 先删除后保存，保证用户重复提交相同数据导致的保存失败
						await this.delFile(pkCode)
					}
					if (this.uploadFileList.length > 0) {
						//上传文件
						await this.uploadFile(pkCode)
					}
					if (isSubmit) {
						this.$emit('submit-click', pkCode)
					} else {
						this.$emit('success', pkCode)
					}
					this.close()
				}
			})
		},
		afterRead(list) {
			this.uploadFileList = list
		},
		deleteFile(list) {
			this.delFileList = list
		},
		uploadFile(pkCode) {
			const promiseArr = this.uploadFileList.map((item) => {
				const formData = new FormData()
				formData.append('file', item.file)
				formData.append('filepath', pkCode)
				formData.append('grouptype', '')
				return upload(formData)
			})
			return Promise.all(promiseArr).then((responseFile) => {
				console.log('uploadFile==res=', responseFile)
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
		},
		delFile() {
			const promiseArr = this.delFileList.map((item) => {
				const formData = new FormData()
				formData.append('filePath', item)
				return fileDelete(formData)
			})
			return Promise.all(promiseArr).then((responseFile) => {
				console.log('delFile==res=', responseFile)
				const isSuccess = responseFile.every((item) => {
					return item.data.statusCode == 200
				})
				if (!isSuccess) {
					Toast.loading({
						message: '删除失败',
						duration: 1000,
					})
				}
			})
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
						message: '是否国外不能为空',
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
		close() {
			this.careData = {}
			this.children = []
			this.marryData = {}
			this.fileList = []
			this.uploadFileList = []
			this.delFileList = []
			this.isShow = false
			this.$refs.UploaderEdit.clear() // 清空upload组件数据
			this.$refs.UserPcikEdit.init()
		},
	},
}
</script>
<style lang='less' scoped>
.popup-box {
	width: 100%;
	height: 100%;
}
.content-box {
	position: relative;
	height: calc(100vh - 50px);
	overflow-y: auto;
	&.apply-box {
		padding-bottom: 80px;
		height: calc(100vh - 130px);
	}
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
		.defind-img {
			display: flex;
			width: 30px;
			img{
				width: 16px;
				height: 16px;
			}
		}
		.custom-title {
			font-size: 15px;
			color: black;
		}
		.title-box {
			display: flex;
			font-size: 24px;
			height: 30px;
			align-items: center;
			color: #cf3633;
			padding: 10px 20px;
			.text {
				font-size: 16px;
				color: black;
				margin-left: 10px;
				margin-bottom: 4px;
				font-weight: 600;
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
		.btn-revoke {
			width: 92%;
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
	}
}
.detail-title-box {
	padding: 10px 20px;
	.detail-box {
		font-size: 18px;
		font-weight: 600;
		line-height: 22px;
		display: flex;
		align-items: center;
	}
	.type-icon {
		color: #cf3633;
		font-size: 22px;
		margin-right: 10px;
	}
	.text {
		color: #cf3633;
		font-size: 14px;
		margin-top: 10px;
		margin-left: 32px;
	}
}
.appove-box {
	padding: 10px 20px 20px 54px;
	.appove-item {
		margin-bottom: 30px;
		display: flex;
		align-items: center;
		position: relative;
		&:last-child {
			margin-bottom: 0;
		}
		.avatar {
			width: 40px;
			height: 40px;
			overflow: hidden;
			border-radius: 50%;
			border: 1px solid #cf3633;
			margin-right: 10px;
			img {
				width: 100%;
				height: 100%;
			}
		}
		.empty {
			width: 2px;
			height: 20px;
			background-color: #e9e9e9;
			position: absolute;
			left: 19px;
			top: -25px;
			border-radius: 2px;
		}
		.content {
			flex: 1 0 auto;
		}
		.info {
			display: flex;
			font-size: 14px;
			justify-content: space-between;
			line-height: 14px;
			.time {
				color: #999999;
				font-size: 12px;
			}
		}
		.name {
			font-size: 13px;
			line-height: 13px;
			margin-top: 8px;
			color: #999999;
			&.active {
				color: #cf3633;
			}
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
