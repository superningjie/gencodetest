<template>
	<!-- v-if="isShow" 使每次的ImagePreview都为新建 -->
	<van-image-preview v-if="isShow" v-model="isShow" :images="images" :start-position="startPosition" @change="onChange">
		<template v-slot:index> {{ index + 1 }} / {{ images.length }}</template>
		<template v-slot:cover
			><div class="cover-box">
				<i class="iconfont" @click="downloadImg">&#xe695;</i>
			</div>
		</template>
	</van-image-preview>
</template>
<script>
import { Toast } from 'vant'
export default {
	data() {
		return {
			isShow: false,
			index: 0,
			startPosition: 0,
			images: [],
		}
	},
	methods: {
		show(images, startPosition = 0) {
			this.images = images
			this.startPosition = startPosition
			this.index = startPosition
			this.$nextTick(()=>{
				this.isShow = true
			})
		},
		onChange(index) {
			this.index = index
		},
		downloadImg() {
			em.downloadImage({
				url: this.images[this.index],
				isShowProgressTips: 1, // 默认为1，显示进度提示
				success: function (res) {
					Toast('下载成功')
				},
			})
		},
	},
}
</script>
<style lang='less' scoped>
.cover-box {
	position: absolute;
	left: 30px;
	top: calc(100vh - 50px);
	display: flex;
	.iconfont {
		font-size: 26px;
		color: white;
	}
}
</style>