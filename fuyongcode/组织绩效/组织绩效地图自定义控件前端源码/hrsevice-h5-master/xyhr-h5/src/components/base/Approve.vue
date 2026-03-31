<template>
	<van-popup v-model="isShow" class="popup-box" position="right">
		<div class="userlist">
			<!-- 顶部菜单 -->
			<van-nav-bar title="流程节点" left-arrow class="navStyle" @click-left="goBack">
				<template #right>
					<van-button @click="confirmUsers" type="primary" style="height: 30px; background-color: #cf3633; border-color: #cf3633; border-radius: 4px"
						>提交</van-button
					>
				</template>
			</van-nav-bar>
			<div class="container" style="margin-top: 10px">
				<van-tabs v-model="active" animated>
					<van-tab>
						<van-cell
							@click="getNode(item.pkNode, item.pkFlow, item.operators)"
							v-for="item in nodeArr"
							:key="item.pkNode"
							:title="item.nodeName"
							is-link
						/>
					</van-tab>
					<van-tab>
						<div class="box">
							<oalist @pickuser="pickuser" :list="operators" :change="isChange" ref="oalist" />
						</div>
					</van-tab>
				</van-tabs>
			</div>
		</div>
	</van-popup>
</template>

<script>
import { Toast } from 'vant'
import { queryProcessNodeList } from '@/libs/api.js'
export default {
	name: 'userlist',
	data() {
		return {
			isShow: false,
			active: 0,
			nodeCode: '',
			pk: '',
			isChange: false,
			params: {},
			operators: [],
			nodeArr:[]
		}
	},
	props: {
		upData: { type: Boolean, default: false },
		go: { type: String },
		backNum: { type: Number, default: 2 },
	},
	components: {
		oalist: () => import('@/pages/selfhelp/oaPerson'),
	},
	methods: {
		show(params) {
			this.isShow = true
			this.params = params
			this.getData()
		},
		goBack() {
			if (this.active == 1) {
				this.active = 0
			} else {
				// this.delData();
				this.isShow = false
			}
		},
		getData() {
			this.$toast.loading({
				message: '加载中',
				forbidClick: true,
			})
			queryProcessNodeList(this.params).then((result) => {
				const {flowvos} = result.data.data
				this.nodeArr = flowvos
				this.$toast.clear()
			})
		},
		confirmUsers() {
			this.params.nodeId = this.nodeCode.toString()
			this.params.flowId = this.flowCode
			this.params.operatorId = this.pk
			if (this.pk.length > 0) {
				this.isShow = false
				this.$emit('pick-people',this.params)
			} else {
				Toast('您还未选择审批人')
			}
		},
		getNode(id, flow, operators) {
			this.isChange = !this.isChange
			this.active = 1
			this.nodeCode = id
			this.flowCode = flow
			this.operators = operators
			// setTimeout(()=>{
			// 	console.log(this.$refs.oalist)
			// 	this.$refs.oalist.initData(operators)
			// })
		},
		pickuser(e) {
			if (e.length > 0) {
				this.pk = ''
				e.map((item, index) => {
					this.pk += item.pk.split(',')[0]
					if (index < e.length - 1) {
						this.pk += ','
					}
				})
			} else {
				this.pk = ''
			}
			// this.pk = e.join(',');
		},
	},
}
</script>
<style lang='less' scoped>
.container /deep/ .van-tabs__wrap {
	display: none;
}
.popup-box {
	width: 100%;
	height: 100%;
}
</style>
