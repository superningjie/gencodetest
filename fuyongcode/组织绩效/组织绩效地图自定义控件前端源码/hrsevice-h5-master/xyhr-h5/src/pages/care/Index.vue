<template>
	<div class="page-content">
		<!-- 导航 -->
		<van-nav-bar title="员工关怀" left-arrow class="navStyle" @click-left="goback" />
		<!-- tap切换 -->
		<div class="tab-box">
			<div :class="['btn-base', 'left', activeTab == 'apply' ? 'active' : '']" @click="activeTab = 'apply'">关怀申请</div>
			<div :class="['btn-base', 'left', activeTab == 'detail' ? 'active' : '']" @click="activeTab = 'detail'">查看数据</div>
		</div>
		<!-- 展示内容 -->
		<Add ref="Add" v-show="activeTab == 'apply'" @success="gotoPage" @submit-click="goApprove" />
		<Look ref="Look" v-show="activeTab == 'detail'" @cell-click="gotoPage" />
		<!-- 详情 -->
		<Detail ref="Detail" @fresh="fresh" @edit-click="gotoEdit" @submit-click="goApprove"/>
		<!-- 提交审批 -->
		<Approve ref="Approve" @back="approveSubmit" @pick-people="pickPeople" />
		<!-- 编辑 -->
		<Edit ref="Edit" @success="gotoPage" @submit-click="goApprove" />
	</div>
</template>
<script>
import Add from './Add.vue'
import Edit from './Edit.vue'
import Look from './Look.vue'
import Detail from './Detail.vue'
import Approve from '@/components/base/Approve.vue'
import { submitCare } from '@/libs/api.js'
export default {
	name: 'CARE',
	components: {
		Add,
		Edit,
		Look,
		Detail,
		Approve,
	},
	data() {
		return {
			activeTab: 'apply', // apply,detail
			showNode: false,
		}
	},
	methods: {
		goback() {
			this.$router.go(-1)
		},
		gotoPage(id) {
			this.$refs.Detail.show(id)
			this.$refs.Look.search()
		},
		goApprove(id) {
			this.$refs.Approve.show({ billtype: 'care', pkH: id })
			this.$refs.Look.search()
		},
		gotoEdit(id) {
			this.$refs.Edit.show(id)
		},
		pickPeople(data) {
			console.log('pickPeople==', data)
			// 由于提交的功能分布在三个模块，因此提交接口放置最外层
			const submitData = {
				id: data.pkH,
				nodeId: data.nodeId,
				approveNodeIds: data.operatorId,
			}
			this.$toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			submitCare(submitData).then((res) => {
				if (res.data.statusCode == 200) {
					this.$toast.loading({
						type: 'success',
						message: '提交成功',
						duration: 1000,
					})
					this.gotoPage(res.data.data)
				}
			})
		},
		fresh() {
			// 刷新查询数据
			this.$refs.Look.search()
		},
		approveSubmit() {},
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
.list-box {
	padding: 20px;
}
.detail-box {
	box-shadow: 0 0 4px 2px rgb(0 0 0 / 5%);
	padding: 20px;
	border-radius: 12px;
	background: #fff;
	overflow: hidden;
	position: relative;
	display: flex;
	margin-bottom: 20px;
	.icon {
		font-size: 20px;
	}
	.info {
		margin-left: 20px;
		.title {
			font-size: 16px;
			line-height: 22px;
		}
		.text {
			font-size: 10px;
			color: #999;
			text-align: justify;
			word-break: break-all;
			margin-top: 10px;
		}
	}
	.type {
		position: absolute;
		top: -10px;
		right: -10px;
		width: 65px;
	}
}

.van-cell {
	padding: 10px 20px;
}
.defind .van-cell.van-field {
	padding: 0;
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
.van-search {
	padding: 15px 20px 0 20px;
}
</style>