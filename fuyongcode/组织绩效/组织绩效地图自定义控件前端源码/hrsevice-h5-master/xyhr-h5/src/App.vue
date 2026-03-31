<!-- @format -->

<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      transitionName: null,
    }
  },
  watch: {
    $route(to, from) {
      const toDepth = to.path.split("/").length
      const fromDepth = from.path.split("/").length
      this.transitionName = toDepth < fromDepth ? "slide-right" : "slide-left"
    },
  },
  computed: {
    keepAlive() {
      return this.$store.getters.keepAlive
    },
  },
}
</script>
<style>
#app {
}
::-webkit-scrollbar {
  display: none;
  /* Chrome Safari */
}
</style>
