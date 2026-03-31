<template>
	<van-popup v-model="isShow" get-container="body" class="popup-box" position="right">
		<div class="content">
			<div class="select-title-box">高级查询</div>
			<div class="item-box">
				<div class="title">任职类型</div>
				<div class="item">
					<div
						:class="['option', highLevelType == item.value ? 'active' : '']"
						v-for="item in gglxcolumns"
						:key="item.value"
						@click="selectGglxL(item.value)"
					>
						{{ item.title }}
					</div>
				</div>
			</div>
			<div class="item-box">
				<div class="title">人员类型</div>
				<div class="item">
					<div
						:class="['option', psncllist.indexOf(item.value) > -1 ? 'active' : '']"
						v-for="item in preTypecolumns"
						:key="item.value"
						@click="selectPsncl(item.value)"
					>
						{{ item.title }}
					</div>
				</div>
			</div>
		</div>
		<div class="btn-box">
			<div class="btn" @click="reset">重置</div>
			<div class="btn" @click="sure">确定</div>
		</div>
	</van-popup>
</template>
<script>
export default {
	name: 'tongjiselect',
	data() {
		return {
			isShow: false,
			highLevelType: '', // 高管类型
			psncllist: [], // 人员类型
			orgType: '',
			orgname: '',
		}
	},
	computed: {
		gglxcolumns() {
			return this.$store.state.gglxcolumns
		},
		preTypecolumns() {
			return this.$store.state.preTypecolumns
		},
	},
	mounted() {
		this.preTypecolumns.forEach((item) => {
            if (item.isDefault) {
              this.psncllist.push(item.value)
            }
          })
		
	},
	methods: {
		show() {
			this.isShow = true
		},
		selectGglxL(value) {
			this.highLevelType = this.highLevelType == value ? '' : value
		},
		selectPsncl(value) {
			const index = this.psncllist.indexOf(value)
			if (index > -1) {
				this.psncllist.splice(index, 1)
			} else {
				this.psncllist.push(value)
			}
		},
		reset() {
			this.highLevelType = ''
			this.psncllist = []
		},
		sure() {
			const data = {
				highLevelType: this.highLevelType,
				psncllist: this.psncllist,
			}
			this.$emit('sure', data)
			this.isShow = false
		},
	},
}
</script>
<style lang='less' scoped>
.popup-box {
	width: 80%;
	height: 100%;
}
.content {
	margin-bottom: 50px;
}
.select-title-box {
	height: 46px;
	line-height: 46px;
	background-color: #f5f5f5;
	color: #999999;
	display: flex;
	align-items: center;
	padding-left: calc(4% + 10px);
	font-size: 15px;
}
.item-box {
	margin: 10px 0;
	padding: 0 10px;
}
.title {
	margin: 12px 4%;
	font-size: 15px;
}
.item {
	display: flex;
	flex-wrap: wrap;
}
.option {
	width: 44%;
	margin-left: 4%;
	margin-bottom: 10px;
	height: 24px;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f5f5;
	color: #999;
	font-size: 12px;
	border-radius: 4px;
	&.active {
		background-color: #cf3633;
		color: #ffffff;
	}
}
.btn-box {
	display: flex;
	width: 100%;
	height: 50px;
	position: absolute;
	bottom: 0;
	left: 0;
	box-shadow: 0 0 8px 2px #0000001a;
	align-items: center;
	justify-content: center;
}
.btn {
	width: 30%;
	height: 30px;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #cf3633;
	color: #ffffff;
	font-size: 14px;
	border-radius: 4px;
	&.btn + {
		margin-left: 5%;
	}
}
</style>