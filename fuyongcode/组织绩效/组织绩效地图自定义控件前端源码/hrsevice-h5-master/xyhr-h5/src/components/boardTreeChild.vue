<!-- @format -->

<template>
  <div class="administrativeFramework">
    <div class="group">
      <h2 class="popUpSearch">
        <span v-for="(item, index) in dataDetail.titleList" :key="index">
          <span
            v-if="index == dataDetail.titleList.length - 1"
            style="color: #999; font-size: 14px"
            >{{ item.name }}</span
          >
          <span style="font-size: 14px" v-else @click="goreload(item, index)"
            >{{ item.name
            }}<van-icon
              name="arrow"
              style="margin: 0 3px; vertical-align: middle"
          /></span>
        </span>
      </h2>
      <van-cell is-link v-for="(item, index) in showData" :key="index">
        <template #title>
          <div class="titleFramework" @click="clickSearch(item)">
            <van-image
              class="iconFramework"
              :src="item.type == 'org' ? xingzhengjiagou : bumen"
            />{{ item.name }}
            <span v-if="item.psncount && item.psncount != -1"
              >({{ item.psncount }}人)</span
            >
          </div>
        </template>
        <template #right-icon>
          <van-icon
            name="arrow"
            @click="gonextPage(item)"
            v-if="item.children"
            style="padding: 0 10px; line-height: 24px"
          />
        </template>
      </van-cell>
      <div class="nullBox" v-if="showData.length == 0 && dataInit">
        <xy-empty></xy-empty>
      </div>
    </div>
  </div>
</template>
<script>
import {
  getOrgBoard,
  orgsBusinessTree,
  teamOrgsManagerTree,
} from "@/libs/api.js";
import { Toast } from "vant";
import xingzhengjiagou from "@/assets/icon_xingzhengjiagou_20.png";
import bumen from "@/assets/bumen.svg";
export default {
  name: "leadercommonTreeChild",
  data() {
    return {
      data: {
        retireFlag: false,
        pkPost: "",
      },
      dataInit: false,
      orgFrameList: [],
      dataDetail: {
        titleList: [{ name: "全部", id: "0" }],
      },
      showData: [],
      bumen: bumen,
      xingzhengjiagou: xingzhengjiagou,
    };
  },
  props: {
    name: {
      type: String,
      default: "",
    },
    workPosition: {
      type: String,
      default: "0",
    },
    jobInfo: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  created() {
    this.init(this.jobInfo);
  },
  methods: {
    init(jobInfo) {
      this.workPosition =
        this.workPosition || localStorage.getItem("workPosition");
      this.data.pkPost = jobInfo.pkPost;
      this.dataDetail.titleList = [{ name: "全部", id: "0" }];
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      });
      console.log("workPosition==", this.workPosition);
      console.log("init==", this.data);
      if (this.name == "xz") {
        if (this.workPosition === "0") {
          getOrgBoard(this.data).then((res) => {
            this.dataInit = true;
            Toast.clear();
            if (res.data.data) {
              this.orgFrameList = res.data.data;
              this.showData = res.data.data;
            }
          });
        } else {
          this.data.number = this.workPosition;
          teamOrgsManagerTree(this.data).then((res) => {
            this.dataInit = true;
            Toast.clear();
            if (res.data.data) {
              this.orgFrameList = res.data.data;
              this.showData = res.data.data;
            }
          });
        }
      } else if (this.name == "sy") {
        orgsBusinessTree(this.data).then((res) => {
          this.dataInit = true;
          Toast.clear();
          if (res.data.data) {
            this.orgFrameList = res.data.data;
            this.showData = res.data.data;
          }
        });
      }
    },
    clickSearch(item) {
      this.$emit("clickSearch", item);
    },
    gonextPage(item) {
      this.dataDetail.titleList.push(item);
      this.showData = item.children;
    },
    goreload(item, index) {
      if (index == 0) {
        this.showData = this.orgFrameList;
        this.dataDetail = {
          titleList: [{ name: "全部", id: "0" }],
        };
      } else {
        this.showData = item.children;
        this.dataDetail.titleList.splice(
          index + 1,
          this.dataDetail.titleList.length - index - 1
        );
      }
    },
  },
};
</script>
