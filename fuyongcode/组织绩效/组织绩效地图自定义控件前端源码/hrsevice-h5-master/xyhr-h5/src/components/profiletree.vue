<!-- @format -->

<template>
  <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }" get-container="body">
      <van-nav-bar title="选择" left-arrow class="navStyle" @click-left="isshowFilterPicker(false)" />
      <div class="group">
        <h2 class="popUpSearch" style="padding: 20px">
          <span v-for="(item, index) in dataDetail" :key="item.name">
            <span v-if="index == dataDetail.length - 1" @click="goreload(item, index)">{{ item.name }}</span>
            <span v-else @click="goreload(item, index)">{{ item.name }}></span>
          </span>
        </h2>
        <van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
          <van-tab title="行政架构" :name="'xz'">
            <van-cell is-link v-for="item in orgsCommonList" :key="item.id" @click="clickSearch(item)">
              <template #title>
                <div class="titleFramework">
                  <van-image class="iconFramework" :src="item.type == 'org' ? xingzhengjiagou : bumen" style="margin-right: 10px" />{{ item.name }}
                </div>
              </template>
              <template #right-icon>
                <!-- @click.stop="gonextPage(item)" -->
                <van-icon v-if="item.nextArrow" name="arrow" style="padding: 0 10px; line-height: 24px" />
              </template>
            </van-cell>
          </van-tab>
        </van-tabs>
      </div>
    </van-popup>
  </div>
</template>
<script>
import { Toast } from "vant"
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png"
import bumen from "@/assets/bumen.svg"
export default {
  name: "profiletree",
  data() {
    return {
      activeTab: "xz",
      orgsCommonList: [],
      data: {
        retireFlag: false,
      },
      pid: "",
      dataDetail: [
        {
          name: "全部",
          pid: "",
        },
      ],
      bumen: bumen,
      xingzhengjiagou: xingzhengjiagou,
    }
  },
  props: {
    showFilterPicker: {
      type: Boolean,
      default: false,
    },
    columns: {
      type: Array,
      default: [],
    },
  },
  created() {
    this.showData()
  },
  methods: {
    frameClick(e, item) {
      e.stopPropagation()
      this.$store.commit("increment", item)
      this.$emit("isshowFilterPicker", false)
    },
    showData(val) {
      let data = []
      this.columns.map((item, index) => {
        if (this.pid == "" && !item.pid) {
          item.nextArrow = false
          data.push(item)
        } else if (this.pid == item.pid) {
          item.nextArrow = false
          data.push(item)
        }
      })
      data.map((item) => {
        try {
          this.columns.map((value, index) => {
            if (value.pid == item.id) {
              item.nextArrow = true
              throw Error()
            }
          })
        } catch (error) {}
      })
      if (data.length == 0) {
        Toast("已无下级")
      } else {
        if (val) {
          this.dataDetail.push(val)
        }
        this.orgsCommonList = data
        console.log("orgsCommonList==", this.orgsCommonList)
      }
    },
    gonextPage(item) {
      this.pid = item.id
      this.showData(item)
    },
    goreload(item, index) {
      if (index == 0) {
        this.dataDetail = [
          {
            name: "全部",
            pid: "",
          },
        ]
      } else {
        this.dataDetail.splice(index + 1, this.dataDetail.length - index - 1)
      }

      this.pid = item.id
      this.showData()
    },
    isshowFilterPicker(value) {
      this.$emit("isshowFilterPicker", value)
    },
    clickSearch(item) {
      // if (item.nextArrow && !item.pid) { // 从市开始选，目前从是否有父级做判断，没有父级的为省级，不能选择
      if (item.nextArrow) {
        this.pid = item.id
        this.showData(item)
      } else {
        this.$emit("clickSearch", item)
      }
    },
  },
}
</script>
