<!-- @format -->

<template>
  <iframe class="iframe-box" :src="webUrl" frameborder="0"></iframe>
</template>
<script>
export default {
  data() {
    return {
      webUrl: "",
    }
  },
  mounted() {
    this.webUrl = this.$route.query.url
    // 修改标题文字
    em.changeTitle({
      title: this.$route.query.title,
    })
    console.log("this.webUrl==", this.webUrl, this.$route.query)
  },
}
</script>
<style lang="less" scoped>
.iframe-box {
  height: calc(100vh - 4px);
  width: 100vw;
}
::-webkit-scrollbar {
  display: none;
}
</style>
