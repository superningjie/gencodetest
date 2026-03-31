<template>
	<van-popup v-model="isShow" round class="popup-select-box">
		<div class="select-box">
			<van-radio-group v-model="radioValue" @change="changeGroup">
				<van-radio class="radio-box" :name="index" v-for="(item, index) in list" :key="item[params.value]" @click="confirm">{{
					item[params.key]
				}}</van-radio>
			</van-radio-group>
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
		params: {
			type: Object,
			default() {
				return {
					key: 'name',
					value: 'id',
				}
			},
		},
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
			console.log(value || value == 0)
			if (value || value === 0) {
				
				this.init(value)
			}
			this.isShow = true
		},
		init(value) {
			if (value == 'init') {
				this.radioValue = ''
			} else {
				this.radioValue = this.list.findIndex((item) => {
					return item[this.params.value] == value
				})
			}
		},
		clear() {
			this.radioValue = ''
		},
		changeGroup() {
			console.log('this.radioValue==', this.radioValue)
		},
		confirm() {
			const data = Object.assign({}, this.list[this.radioValue])
			console.log('changeGroup', this.radioValue, data)
			this.$emit('change', data)
			this.isShow = false
		},
	},
}
</script>
<style lang='less' scoped>
.popup-select-box {
	width: 56%;
	max-height: 480px;
	padding: 10px 20px;
}
.radio-box {
	margin-bottom: 15px;
	font-size: 14px;
	&:last-child {
		margin-bottom: 0;
	}
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
/deep/ .van-radio__label{
	margin-left:16px;
}
</style>
