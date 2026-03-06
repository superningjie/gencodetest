<!--
 * @Author: 张坤伟 1261585833@qq.com
 * @Date: 2024-05-17 11:15:49
 * @LastEditors: 张坤伟 1261585833@qq.com
 * @LastEditTime: 2024-07-03 15:15:04
 * @FilePath: \xyhr-h5\src\pages\personage-plans\supplementation.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
  <div class="supplementation">
    <!-- <div class="header">
			<van-nav-bar>
				<template #left>
					<div class="navbar-left" @click="handleBack">
						<van-image fit="cover" width="10px" height="16px" :src="require('@/assets/newready/left.png')"></van-image>
					</div>
				</template>
			</van-nav-bar>
		</div> -->
    <div class="tips">
      移动端暂不支持个人发展计划信息完善操作，请到PC端处理。
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    handleBack() {
      this.$router.go(-1);
    },
  },
};
</script>

<style lang='less' scoped>
.supplementation {
  .header {
    margin-bottom: 10px;
    border-bottom: 1px solid #eeeeee;
  }
  .tips {
    padding: 0 16px;
    font-family: PingFang SC;
    font-weight: 500;
    font-size: 15px;
    color: #333333;
    line-height: 26px;
  }
}
</style>