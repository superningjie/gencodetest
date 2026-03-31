<!-- @format -->

<template>
  <van-empty :description="description" :image="Empty"></van-empty>
</template>
<script>
import Empty from "@/assets/components/empty-image.png"
export default {
  props: {
    description: {
      type: String,
      default: "暂无数据",
    },
  },
  data() {
    return {
      Empty,
    }
  },
  mounted() {},
}
</script>
<style lang="less" scoped>
/deep/.van-empty__image{
    height: auto;
}
</style>
