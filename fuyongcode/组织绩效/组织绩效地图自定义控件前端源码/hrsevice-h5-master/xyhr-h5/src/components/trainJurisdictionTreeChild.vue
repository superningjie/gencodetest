<!-- @format -->

<template>
  <div class="administrativeFramework">
    <div class="group">
      <h2 class="popUpSearch">
        <span v-for="(item, index) in dataDetail.titleList" :key="index">
          <span v-if="index == dataDetail.titleList.length - 1 && dataDetail.titleList.length != 1" style="color: #999">{{ item.name }}</span>
          <span v-else @click="goreload(item, index)">{{ item.name }}{{ dataDetail.titleList.length != 1 ? ">" : "" }}</span>
        </span>
      </h2>
      <van-cell is-link v-for="(item, index) in showData" :key="index">
        <template #title>
          <div class="titleFramework" @click="clickSearch(item)">
            <van-image class="iconFramework" :src="item.type == 'org' ? xingzhengjiagou : bumen" />{{ item.name }}
          </div>
        </template>
        <template #right-icon>
          <van-icon name="arrow" @click="gonextPage(item)" v-if="item.children" style="padding: 0 10px; line-height: 24px" />
        </template>
      </van-cell>
      <div class="nullBox" v-if="showData.length == 0">
        <xy-empty></xy-empty>
      </div>
    </div>
  </div>
</template>
<script>
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png"
import bumen from "@/assets/bumen.svg"
export default {
  name: "trainJurisdictionTreeChild",
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
  created() {
    this.showData = this.orgsCommonList
    console.log(this.showData)
  },
  methods: {
    init() {},
    goreload(item, index) {
      if (index == 0) {
        if (this.dataDetail.titleList.length == 1) {
          this.$emit("clickSearch", item)
          return
        }
        this.showData = this.orgsCommonList
        this.dataDetail = {
          titleList: [{ name: "全部", id: "" }],
        }
      } else {
        this.showData = item.children
        this.dataDetail.titleList.splice(index + 1, this.dataDetail.titleList.length - index - 1)
      }
    },
    clickSearch(item) {
      this.$emit("clickSearch", item)
    },
    gonextPage(item) {
      this.dataDetail.titleList.push(item)
      this.showData = item.children
      // let arr = [];
      // item.children.map(box=>{
      //     if(box.type!=='dept'){
      //         arr.push(box);
      //     }
      // });
      // this.showData = arr;
    },
  },
}
</script>
