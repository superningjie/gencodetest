<!-- @format -->

<template>
	<van-popup v-model="isShow" class="checkboxPopup" :overlay-style="{ background: 'rgba(120,120,120,.5)' }" :style="{ width: '60%' }">
		<van-checkbox-group v-model="result" checked-color="#ee0a24">
			<van-checkbox v-for="item in orgNumber" :name="item.code" :key="item.code">{{ item.title }}</van-checkbox>
		</van-checkbox-group>
		<div class="operationControl">
			<van-button color="#D0332F" round type="info" @click="checkboxSearch">确定</van-button>
		</div>
	</van-popup>
</template>
<script>
	import {
		getOrgNumber
	} from '@/libs/api.js'
	export default {
		data() {
			return {
				isShow: false,
				result: [],
				orgNumber: []
			}
		},
		computed: {
			jobrankcolumns() {
				return this.$store.state.jobrankcolumns
			},
		},
		mounted() {
			getOrgNumber().then(res => {
				if (res.data.statusCode == 200) {
					this.orgNumber = res.data.data
				}
			})
		},
		methods: {
			show() {
				this.isShow = true
			},
			checkboxSearch() {
				this.$emit("success", this.result)
				this.isShow = false
			},
		},
	}
</script>
<style lang="less" scoped>
	/deep/ .van-checkbox__icon {
		height: auto;
	}
	.van-checkbox{
		margin-bottom: 14px;
	}
</style>