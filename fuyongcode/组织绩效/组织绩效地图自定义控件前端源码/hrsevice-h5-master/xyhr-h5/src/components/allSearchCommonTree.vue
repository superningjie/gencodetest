<!-- @format -->

<template>
  <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }">
      <van-nav-bar title="选择部门" class="navStyle" @click-left="isshowFilterPicker(false)">
        <template #left> <van-icon class="close-icon" name="cross" /> </template>
      </van-nav-bar>
      <div class="subnav flex" v-if="isBusiness == true">
        <div :class="activeTab === 'adminorg' ? 'active' : ''" @click="activeTab = 'adminorg'">行政架构</div>
        <div :class="activeTab === 'business' ? 'active' : ''" @click="activeTab = 'business'">事业部架构</div>
      </div>
      <van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
        <van-tab title="行政架构" :name="'adminorg'">
          <allSearchCommonTreeChild :name="'adminorg'" @clickSearch="clickSearch" />
        </van-tab>
        <van-tab title="事业部架构" :name="'business'" v-if="isBusiness == true">
          <allSearchCommonTreeChild :name="'business'" @clickSearch="clickSearch" />
        </van-tab>
      </van-tabs>
    </van-popup>
  </div>
</template>
<script>
import allSearchCommonTreeChild from "@/components/allSearchCommonTreeChild"

export default {
  name: "allSearchCommonTree",
  data() {
    return {
      activeTab: "adminorg",
      orgsCommonList: [],
    }
  },
  props: {
    showFilterPicker: {
      type: Boolean,
      default: false,
    },
    isBusiness: {
      type: Boolean,
      default: false,
    },
  },
  components: { allSearchCommonTreeChild },
  methods: {
    frameClick(e, item) {
      e.stopPropagation()
      this.$store.commit("increment", item)
      this.$emit("isshowFilterPicker", false)
    },
    isshowFilterPicker(value) {
      this.$emit("isshowFilterPicker", value)
    },
    clickSearch(item) {
      this.$emit("clickSearch", { ...item, origin: this.activeTab })
    },
  },
}
</script>
<style lang="less" scoped>
    .close-icon{
        font-size: 18px !important;
    }
</style>
