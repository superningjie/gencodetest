<!-- @format -->

<template>
  <div class="administrativeFramework">
    <div class="group">
      <h2 class="popUpSearch">
        <span v-for="(item, index) in dataDetail.titleList" :key="index">
          <span v-if="index == dataDetail.titleList.length - 1" style="color: #999">{{ item.name }}</span>
          <span v-else @click="goreload(item, index)">{{ item.name }}></span>
        </span>
      </h2>
      <van-cell is-link v-for="(item, index) in showData" :key="index">
        <template #title>
          <div class="titleFramework" @click="clickSearch(item)"><van-image class="iconFramework" style="border: none" :src="item.type == 'org' ? xingzhengjiagou : bumen" />{{ item.name }}</div>
        </template>
        <template #right-icon>
          <van-icon name="arrow" @click="gonextPage(item)" v-if="item.children" style="padding: 0 10px; line-height: 24px" />
        </template>
      </van-cell>
    </div>
  </div>
</template>
<script>
import { orgsAuthTreeAdminorg, orgsAuthTreeBusiness } from "@/libs/api.js"
import { Toast } from "vant"
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png"
import bumen from "@/assets/bumen.svg"
export default {
  name: "allSearchCommonTreeChild",
  data() {
    return {
      data: {
        retireFlag: false,
      },
      orgFrameList: [],
      dataDetail: {
        titleList: [{ name: "全部", id: "" }],
      },
      showData: [],
      // 图片资源
      xingzhengjiagou: xingzhengjiagou,
      bumen: bumen,
    }
  },
  props: {
    name: {
      type: String,
      default: "",
    },
  },
  created() {
    console.log()
    this.init()
  },
  methods: {
    init() {
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      })
      if (this.name === "adminorg") {
        orgsAuthTreeAdminorg(this.data).then((res) => {
          Toast.clear()
          this.orgFrameList = res.data.data
          this.showData = res.data.data
        })
      } else if (this.name === "business") {
        orgsAuthTreeBusiness(this.data).then((res) => {
          this.orgFrameList = res.data.data
          this.showData = res.data.data
        })
      }
    },
    clickSearch(item) {
      this.$emit("clickSearch", item)
    },
    gonextPage(item) {
      this.dataDetail.titleList.push(item)
      this.showData = item.children
    },
    goreload(item, index) {
      if (index == 0) {
        this.showData = this.orgFrameList
        this.dataDetail = {
          titleList: [{ name: "全部", id: "" }],
        }
      } else {
        this.showData = item.children
        this.dataDetail.titleList.splice(index + 1, this.dataDetail.titleList.length - index - 1)
      }
    },
  },
}
</script>
