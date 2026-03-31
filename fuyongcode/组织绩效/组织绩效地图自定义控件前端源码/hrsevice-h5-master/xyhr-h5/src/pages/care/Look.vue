<template>
	<!-- 查看详情内容 -->
	<div class="content-box">
		<van-search v-model="params.searchKey" shape="round" placeholder="搜索" @search="search" />
		<div class="list-box van-clearfix" v-show="detailList.length > 0">
			<van-list v-model="loading" :finished="finished" finished-text="没有更多了" :offset="50" :immediate-check="false" @load="onLoad">
				<div class="detail-box" v-for="item in detailList" :key="item.id" @click="gotoPage(item.id)">
					<div class="icon">
						<i class="iconfont" :style="{ color: item.iconColor }" v-html="item.icon"></i>
					</div>
					<div class="info">
						<div class="title">{{ item.careTypeName }}</div>
						<div class="text">{{ item.applicantDesc }}</div>
					</div>
					<div class="type">
						<img :src="item.statusSrc" />
					</div>
				</div>
			</van-list>
		</div>
		<xy-empty v-show="detailList.length == 0"></xy-empty>
	</div>
</template>
<script>
import { getCareData } from '@/libs/api.js'
const imgObj = {
	0: import('../../assets/shuiyin_daitijiao.svg'), // 自由态
	1: import('../../assets/shuiying_shenpizhong.svg'), // 审批中
	2: import('../../assets/shuiying_butongyi.svg'), // 不同意
	3: import('../../assets/shuiyin_tongyi.svg'), // 同意
	// 4:import('../../assets/shuiyin_weichuli.svg'),// 未处理
	// 5:import('../../assets/shuiyin_yiguidang.svg'),// 已归档
}
const iconObj = {
	MARRY: {
		icon: '&#xe605;',
		color: '#D0332F',
	}, // 结婚
	BIRTH: {
		icon: '&#xe609;',
		color: '#7FCD93',
	}, // 生育
	SICK: {
		icon: '&#xe602;',
		color: '#7FCD93',
	}, // 生病住院
	FUNERAL: {
		icon: '&#xe608;',
		color: '#999',
	}, // 丧事
}
export default {
	name: 'CARE',
	data() {
		return {
			// 查看详情
			params: {
				pageNum: 1, //分页页码：从1开始
				pageSize: 10, //每一页的数目
				searchKey: '', //搜索内容
			},
			loading: false,
			finished: false,
			detailList: [],
			detailListOne: [
				{
					id: 1,
					icon: '&#xe607;',
					careTypeName: '大多数',
					applicantDesc: 'shuiying_shenpizhong',
					statusSrc: import('../../assets/shuiying_shenpizhong.svg'), // 审批中
				},
				{
					id: 2,
					icon: '&#xe60a;',
					careTypeName: '大多数',
					applicantDesc: 'shuiyin_yiguidang',
					statusSrc: import('../../assets/shuiyin_yiguidang.svg'), // 已归档
				},
				{
					id: 3,
					icon: '&#xe609;',
					careTypeName: '大多数',
					applicantDesc: 'shuiyin_weichuli',
					statusSrc: import('../../assets/shuiyin_weichuli.svg'), // 未处理
				},
				{
					id: 4,
					icon: '&#xe608;',
					careTypeName: '大多数',
					applicantDesc: 'shuiying_butongyi',
					statusSrc: import('../../assets/shuiying_butongyi.svg'), // 不同意
				},
				{
					id: 5,
					icon: '&#xe605;',
					careTypeName: '大多数',
					applicantDesc: 'shuiyin_tongyi',
					statusSrc: import('../../assets/shuiyin_tongyi.svg'), // 同意
				},
				{
					id: 6,
					icon: '&#xe605;',
					careTypeName: '大多数',
					applicantDesc: 'shuiyin_daitijiao',
					statusSrc: import('../../assets/shuiyin_daitijiao.svg'), // 自由态
				},
			],
		}
	},
	mounted() {
		this.getData()
	},
	methods: {
		afterRead() {},
		search() {
			this.params.pageNum = 1
			this.detailList = []
			this.finished = false
			this.getData()
		},
		onLoad() {
			this.params.pageNum++
			console.log('onLoad==')
			this.getData()
		},
		getData() {
			// this.$toast.loading({
			// 	message: '加载中',
			// 	forbidClick: true,
			// })
			getCareData(this.params)
				.then((res) => {
					if (res.data.statusCode == 200) {
						const data = res.data.data
						data.list.forEach((item) => {
							item.icon = iconObj[item.careType].icon
							item.iconColor = iconObj[item.careType].color
							item.statusSrc = imgObj[item.approvalStatus]
						})
						if (data.list.length < this.params.pageSize) {
							console.log('===finished')
							this.finished = true // 返回的条数小于每页获取的条数，说明后面没数据了
						}
						this.loading = false
						this.detailList.push(...data.list)
					}
				})
				.catch(() => {
					this.$toast.clear()
				})
		},
		gotoPage(id) {
			this.$emit('cell-click', id)
		},
	},
}
</script>
<style lang='less' scoped>
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
.van-search {
	padding: 15px 20px 0 20px;
}
</style>
