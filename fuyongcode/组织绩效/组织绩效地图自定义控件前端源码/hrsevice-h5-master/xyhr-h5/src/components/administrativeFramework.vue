<!-- @format -->

<template>
  <div class="administrativeFramework">
    <div class="group">
      <van-cell is-link v-for="item in orgFrameList">
        <template #title>
          <div class="titleFramework" @click="gonextPage(item)">
            <van-image class="iconFramework" style="border: none" :src="item.type == 'org' ? xingzhengjiagou : bumen" />
            {{ item.name }}
          </div>
        </template>
        <template #right-icon>
          <van-icon name="arrow" @click="gonextPage(item)" />
        </template>
      </van-cell>
    </div>
  </div>
</template>
<script>
import { getOrgFrame, getOrgFrameBusi } from "@/libs/api.js"
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png"
import bumen from "@/assets/bumen.svg"
export default {
  name: "administrativeFramework",
  data() {
    return {
      data: {
        pageNum: 1,
        pageSize: 999,
        retireFlag: false,
      },
      orgFrameList: [],
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
    this.init()
  },
  methods: {
    init() {
      if (this.name == "xz") {
        let param = {
          params: {
            pk: 100000,
          },
        }

        getOrgFrame(param).then((res) => {
          this.orgFrameList = res.data.data.content
        })
      } else if (this.name == "sy") {
        let param = {
          params: {
            pk: 100000,
          },
        }
        // getOrgFrameBusi(this.data).then((res) => {
        //   this.orgFrameList = res.data.data.content
        // })
        getOrgFrameBusi(param).then((res) => {
          this.orgFrameList = res.data.data.content
        })
      }
    },
    gonextPage(item) {
      let data = Object.assign({ titleList: [{ name: "组织架构" }, item], listIndex: this.name }, item)
      let keepAlive = this.$store.state.keepAlive
      keepAlive.push("orgStructure")
      this.$store.commit("setKeepAlive", keepAlive)
      this.$router.push({
        name: "orgStructure",
        query: data,
      })
    },
  },
}
</script>
