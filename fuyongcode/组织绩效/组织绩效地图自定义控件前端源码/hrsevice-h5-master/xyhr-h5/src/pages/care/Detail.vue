<template>
	<van-popup v-model="isShow" position="right" class="popup-box">
		<van-nav-bar :title="title" left-arrow class="navStyle" @click-left="goback">
			<template #right>
				<van-icon v-show="careData.isApplicant && careData.approvalStatus == 0" @click="delData" color="#000" name="delete-o" size="22" />
			</template>
		</van-nav-bar>

		<!-- 申请展示内容 -->
		<div
			:class="[
				'content-box',
				careData.isApplicant && (careData.approvalStatus == 0 || (careData.approvalStatus == 1 && isRevoke)) ? 'apply-box' : '',
			]"
		>
			<!-- 基础信息 -->
			<div class="content-item">
				<div class="detail-title-box" v-if="careData.approvalStatus != 0">
					<div class="detail-box">
						<div class="iconfont type-icon">&#xe6b8;</div>
						<div class="title">申请</div>
					</div>
					<div class="text" v-if="careData.approvalStatus == 1">等待 {{ applyName }} 审批</div>
				</div>
				<van-cell :value="careData.careTypeName">
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
				<van-field v-model="careData.applicantDesc" disabled rows="3" autosize type="textarea" />
				<van-cell :value="careData.applicant">
					<template #title>
						<span class="iconfont defind-img">
							<img class="" src="../../assets/care/care_people.png" />
						</span>
						<span class="custom-title">申请人</span>
					</template>
				</van-cell>
				<van-cell :value="careData.byCare">
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
				<div class="content-item" v-for="child in children" :key="child.id">
					<van-cell class="defind" :value="child.name">
						<template #title>
							<span class="iconfont cell-icon" style="color: rgb(129, 206, 149)">&#xe69f;</span>
							<span class="custom-title">子女姓名</span>
						</template>
					</van-cell>
					<van-cell :value="child.relationName">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">与被关怀人关系</span>
						</template>
					</van-cell>
					<van-cell :value="child.birthdate">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">出生日期</span>
						</template>
					</van-cell>
					<van-cell :value="child.isInForeignName">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">是否在国外</span>
						</template>
					</van-cell>
				</div>
			</div>
			<!-- 结婚 -->
			<div v-show="careData.careType === 'MARRY'">
				<div class="content-item">
					<van-cell :value="marryData.relationName">
						<template #title>
							<span class="iconfont cell-icon" style="color: rgb(129, 206, 149)">&#xe69f;</span>
							<span class="custom-title">与被关怀人关系</span>
						</template>
					</van-cell>
					<van-cell class="defind" :value="marryData.name">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">家庭成员姓名</span>
						</template>
					</van-cell>
					<van-cell :value="marryData.birthdate">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">出生日期</span>
						</template>
					</van-cell>
					<van-cell class="defind" :value="marryData.corp">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">工作单位</span>
						</template>
					</van-cell>
					<van-cell class="defind" :value="marryData.job">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">职务</span>
						</template>
					</van-cell>
					<van-cell class="defind" :value="marryData.tel">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">联系电话</span>
						</template>
					</van-cell>
					<van-cell :value="marryData.politicsName">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">政治面貌</span>
						</template>
					</van-cell>
					<van-cell :value="marryData.isInOfficeName">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">是否在XXXX任职</span>
						</template>
					</van-cell>
					<van-cell :value="marryData.isInForeignName">
						<template #title>
							<span class="iconfont empty-span"></span>
							<span class="custom-title">是否在国外</span>
						</template>
					</van-cell>
				</div>
			</div>
			<!-- 附件 -->
			<div class="content-item">
				<div class="file-box">
					<div class="title-file">附件</div>
					<FileDisplay class="file-display" :fileList="fileList" />
				</div>
			</div>
			<!-- 流程 -->
			<div v-show="careData.approvalStatus != 0" class="content-item">
				<div class="title-box">
					<i class="iconfont">&#xe6b9;</i>
					<div class="text">审批流程</div>
				</div>
				<div class="appove-box">
					<div class="appove-item" v-for="(item, index) in applyList" :key="index">
						<div class="avatar">
							<img :src="item.operatePhoto" />
						</div>
						<div class="content">
							<div class="info">
								<div class="job">{{ index == 0 ? '发起申请' : item.operatePostName }}</div>
								<div class="time">{{ item.nodeTime }}</div>
							</div>
							<div v-show="item.operateType" :class="['name', item.operateType == '审批中' ? 'active' : '']">
								<span>{{ item.operateName }}</span>
								<span> | {{ item.operateType }}</span>
							</div>
						</div>
						<div v-if="index != 0" class="empty"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 底部按钮框,申请才有权限操作 -->
		<div class="bottom-box" v-if="careData.isApplicant && (careData.approvalStatus == 0 || (careData.approvalStatus == 1 && isRevoke))">
			<div class="btn-box" v-show="careData.approvalStatus == 0">
				<div class="btn-left" @click="toEdit">编辑</div>
				<div class="btn-right" @click="submitData">提交</div>
			</div>
			<div class="btn-box" v-show="careData.approvalStatus == 1 && isRevoke">
				<div class="btn-revoke" @click="revokeData">撤销申请</div>
			</div>
		</div>
	</van-popup>
</template>
<script>
import { getCareDetail, getRequestCurrentstatus, revokeCare, deleteCare, fileList } from '@/libs/api.js'
import { Toast } from 'vant'
export default {
	name: 'CARE',
	data() {
		return {
			id: '', // 单据id
			isShow: false,
			isEdit: true, // 是否是编辑状态
			title: '',
			careData: {
				careType: '', //关怀类型【必须】
				careTypeName: '', //关怀类型名称【必须】
				applicant: '', //申请人【必须】
				applicantPk: '', //申请人主键【必须】
				applicantDesc: '', //申请说明
				byCare: '', //被关怀人【必须】
				byCarePk: '', //被关怀人主键【必须】
				byCarePkJob: '', //被关怀人工作记录主键【必须】
				detailList: [
					{
						name: '', //姓名
						relation: '', //关系主键
						relationName: '', //关系名称
						birthdate: new Date(), //出生日期
						corp: '', //工作单位
						job: '', //职务
						tel: '', //联系电话
						politics: '', //政治面貌
						politicsName: '', //政治面貌名称
						isInOffice: '', //是否在XXXX任职,N=否 Y=是
						isInForeign: '', //是否在国外,N=否 Y=是
					},
				],
			},
			children: [],
			marryData: {},
			isRevoke: false, // 审批状态的撤销按钮是否展示
			applyList: [], // 审批流程
			fileList: [],
		}
	},
	components: {
		FileDisplay: () => import('@/components/uploader/Display'),
	},
	computed: {
		applyName() {
			const arr = []
			this.applyList.forEach((item) => {
				if (item.operateType == '审批中') {
					arr.push(item.operateName)
				}
			})
			return arr.join(',')
		},
	},
	methods: {
		show(id) {
			// 清空
			this.careData = {}
			this.children = []
			this.marryData = {}
			this.applyList = []
			this.isShow = true
			this.id = id
			this.getData()
		},
		goback() {
			console.log('goback==')
			this.isShow = false
		},
		getData() {
			Toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			getCareDetail(this.id).then((res) => {
				if (res.data.statusCode == 200) {
					const data = res.data.data
					this.careData = data
					if (data.careType == 'MARRY') {
						const tempData = data.detailList[0] // 结婚数据为1条
						tempData.isInOfficeName = tempData.isInOffice == 'Y' ? '是' : '否'
						tempData.isInForeignName = tempData.isInForeign == 'Y' ? '是' : '否'
						this.marryData = tempData
					}
					if (data.careType == 'BIRTH') {
						data.detailList.forEach((item) => {
							item.isInForeignName = item.isInForeign == 'Y' ? '是' : '否'
						})
						this.children = data.detailList
					}
					const titles = {
						0: '待提交',
						1: '待审批',
						2: '不同意',
						3: '已同意',
					}
					this.title = titles[data.approvalStatus]
					if (data.approvalStatus != 0) {
						// 已提交 获取审批流程
						getRequestCurrentstatus({
							billType: 'care',
							pkH: res.data.data.id,
							requestIds: res.data.data.requestId,
						}).then((request) => {
							if (request.data.statusCode == 200) {
								this.isRevoke = request.data.data.isRevoke
								this.applyList = request.data.data.workflowNodeList
								console.log('this.applyList==', this.applyList)
								Toast.clear()
							}
						})
					} else {
						Toast.clear()
					}
				}
			})
			const data_ = new FormData()
			data_.append('filePath', this.id)
			fileList(data_).then((res) => {
				this.fileList = res.data.data || []
			})
		},
		revokeData() {
			this.$dialog
				.confirm({
					message: '是否撤销申请',
				})
				.then(() => {
					Toast.loading({
						message: '加载中',
						forbidClick: true,
					})
					revokeCare(this.careData.id).then((res) => {
						Toast('撤销成功')
						this.getData()
						this.$emit('fresh')
					})
				})
				.catch(() => {
					// on cancel
				})
		},
		delData() {
			this.$dialog
				.confirm({
					message: '是否删除申请',
				})
				.then(() => {
					Toast.loading({
						message: '加载中',
						forbidClick: true,
					})
					deleteCare(this.careData.id).then((res) => {
						Toast('删除成功')
						this.$emit('fresh')
						this.isShow = false
					})
				})
				.catch(() => {
					// on cancel
				})
		},
		toEdit() {
			this.$emit('edit-click', this.id)
		},
		submitData() {
			this.$emit('submit-click', this.id)
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

.file-box {
	padding: 10px 20px;
	display: flex;
	align-items: flex-start;
	.title-file {
		font-size: 15px;
		line-height: 37px;
		width: 36px;
		display: inline-block;
	}
	.file-display {
		width: calc(100% - 50px);
		display: inline-block;
	}
}
</style>