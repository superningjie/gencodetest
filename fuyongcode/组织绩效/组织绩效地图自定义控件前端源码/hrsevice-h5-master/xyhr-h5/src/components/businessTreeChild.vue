<!-- @format -->

<template>
  <div class="administrativeFramework">
    <div class="group">
      <h2 class="popUpSearch">
        <span v-for="(item, index) in dataDetail.titleList">
          <span v-if="index == dataDetail.titleList.length - 1 && dataDetail.titleList.length != 1" style="color: #999">{{ item.name }}</span>
          <span v-else @click="goreload(item, index)">{{ item.name }}{{ dataDetail.titleList.length != 1 ? ">" : "" }}</span>
        </span>
      </h2>
      <xy-empty v-if="showData.length == 0"></xy-empty>
      <van-cell is-link v-for="(item, index) in showData">
        <template #title>
          <div class="titleFramework" @click="clickSearch(item)"><van-image class="iconFramework" :src="item.type == 'dept' ? bumen : xingzhengjiagou" />{{ item.name }}</div>
        </template>
        <template #right-icon>
          <van-icon name="arrow" @click="gonextPage(item)" v-if="ischildShow(item)" style="padding: 0 10px; line-height: 24px" />
        </template>
      </van-cell>
    </div>
  </div>
</template>
<script>
import bumen from "@/assets/bumen.svg"
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png"
export default {
  name: "businessTreeChild",
  data() {
    return {
      orgFrameList: [],
      dataDetail: {
        titleList: [{ name: "全部", id: "" }],
      },
      showData: [],
      bumen: bumen,
      xingzhengjiagou: xingzhengjiagou,
    }
  },
  props: {
    orgsCommonList: {
      type: Array,
      default: [],
    },
  },
  computed: {},
  created() {
    let data = []
    this.orgsCommonList.map((val) => {
      if (val.type != "dept") {
        data.push(val)
      }
    })
    this.showData = data
    console.log(this.showData)
  },
  methods: {
    ischildShow: (value) => {
      if (value.children) {
        let bool = false
        value.children.map((item) => {
          if (item.type != "dept") {
            bool = true
          }
        })
        return bool
      } else {
        return false
      }
    },
    init() {},
    goreload(item, index) {
      let data = []
      if (index == 0) {
        if (this.dataDetail.titleList.length == 1) {
          this.$emit("clickSearch", item)
          return
        }
        this.orgsCommonList.map((val) => {
          if (val.type != "dept") {
            data.push(val)
          }
        })
        this.dataDetail = {
          titleList: [{ name: "全部", id: "" }],
        }
      } else {
        item.children.map((val) => {
          if (val.type != "dept") {
            data.push(val)
          }
        })

        this.dataDetail.titleList.splice(index + 1, this.dataDetail.titleList.length - index - 1)
      }
      this.showData = data
    },
    clickSearch(item) {
      this.$emit("clickSearch", item)
    },
    gonextPage(item) {
      this.dataDetail.titleList.push(item)
      let data = []
      item.children.map((val) => {
        if (val.type != "dept") {
          data.push(val)
        }
      })
      this.showData = data
    },
  },
}
</script>
