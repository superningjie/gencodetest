<template>
	<van-popup v-model="isShow" round :style="{ width: '70%' }">
		<div class="select-box">
			<van-radio-group v-model="radioValue" @change="changeGroup">
				<van-radio class="radio-box" :name="index" v-for="(item, index) in list" :key="item[params.value]" @click="clickRadio">{{ item[params.key] }}</van-radio>
			</van-radio-group>
		</div>
		<div class="btn-box">
			<div class="btn-submit" @click="confirm">确 定</div>
		</div>
	</van-popup>
</template>
<script>
export default {
	props: {
		list: {
			type: Array,
			default() {
				return []
			},
		},
		params:{
			type: Object,
			default() {
				return {
					key:'name',
					value:'id'
				}
			},
		}
	},
	data() {
		return {
			isShow: false,
			radioValue: '',
			radioChange: false,
		}
	},
	methods: {
		show(value = '') {
			if (value) {
				this.radioValue = this.list.findIndex((item) => {
					return item[this.params.value] == value
				})
			}
			this.isShow = true
		},
		clickRadio() {
			if (!this.radioChange) {
				this.radioValue = ''
			}
			this.radioChange = false
		},
		changeGroup() {
			this.radioChange = true
		},
		confirm() {
			const data = Object.assign({}, this.list[this.radioValue])
			console.log('single-confirm',this.radioValue,data)
			this.$emit('change', data)
			this.isShow = false
		},
	},
}
</script>
<style lang='less' scoped>
.select-box {
	padding: 10px 20px;
	padding-bottom: 54px;
	position: relative;
	overflow-y: auto;
	max-height: 270px;
}
.radio-box {
	margin-bottom: 15px;
	font-size: 14px;
}
.btn-box {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 54px;
	background-color: white;
	display: flex;
	justify-content: center;
	align-items: center;
}
.btn-submit {
	width: 80%;
	height: 32px;
	line-height: 32px;
	color: white;
	background-color: #d0332f;
	border-radius: 20px;
	font-size: 15px;
	text-align: center;
}
/deep/ .van-radio__icon--checked .van-icon {
	background-color: #d0332f;
	border-color: #d0332f;
}
</style>
