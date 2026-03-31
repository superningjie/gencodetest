<template>
	<van-popup v-model="isShow" position="bottom">
		<van-datetime-picker v-model="currentDate" :min-date="minDate" :type="type" :title="title" @confirm="confirmTime" @cancel="showTime = false" />
	</van-popup>
</template>
<script>
import Dayjs from 'dayjs'
export default {
	props: {
		type: {
			type: String,
			default: 'date',
		},
		title: {
			type: String,
			default: '选择年月日',
		},
	},
	data() {
		return {
			isShow: false,
			minDate: new Date(1900, 0, 1),
			currentDate: new Date(),
			tempObj: {}, // 缓存当前调用时间控件的对象
		}
	},
	methods: {
		show(date = '') {
			if (date) {
				const dateStr = date.split('-')
				this.currentDate = new Date(dateStr[0],dateStr[1],dateStr[2])
				
			}else {
				this.currentDate = new Date()
			}
			console.log('this.currentDate==',this.currentDate)
			this.isShow = true
		},
		confirmTime(value) {
			const time = Dayjs(value).format('YYYY-MM-DD')
			console.log('time==', time)
			this.$emit('change', time)
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
