<!-- @format -->

<template>
  <div class="background">
    <img class="loading" src="@/assets/lottie/loading.gif" />
  </div>
</template>
<script>
export default {
  data() {
    return {
        msg:''
    }
  },
  mounted() {
    // setTimeout(()=>{
    //     this.$xy.hideLoad()
    // },2000)
  },
  methods: {},
}
</script>
<style lang="less" scoped>
.background {
  width: 100vw;
  height: 100vh;
  background-color: transparent;
  z-index: 9999;
  position: fixed;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  .loading {
    width: 100px;
  }
}
</style>
