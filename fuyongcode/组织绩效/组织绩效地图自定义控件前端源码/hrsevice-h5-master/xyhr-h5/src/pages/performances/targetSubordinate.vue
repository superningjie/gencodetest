<template>
  <div class="page safe-bottom" v-if="list.length">
    <div class="target pd12">
      <div
        v-for="(i, num) in titleList"
        class="titleBox"
        @click="selectTitle(i, num)"
      >
        <div class="titleText">
          {{ i.targetName }}
        </div>
        <van-icon name="arrow" v-show="num < titleList.length - 1"></van-icon>
      </div>
      <!-- {{ userTargetInfo.targetInfoList[index].targetName }} -->
    </div>

    <div class="contentList">
      <div class="contentBox">
        <div class="userInfo flex">
          <div class="line"></div>
          <van-image
            round
            :src="data.headSculpture"
            width="30px"
            height="30px"
          />
          <div>{{ data.name }}</div>
        </div>
        <div class="targetBox pd12">
          <div class="target">
            <div class="flex justify">
              <div class="pdb8 targetText">
                {{ data.targetName }}
              </div>
              <div
                class="lightingDetails"
                @click="show = true"
                v-if="detailList.length"
              >
                <van-image
                  round
                  width="9px"
                  height="12px"
                  :src="RED"
                  v-if="isShowRed"
                />
                <van-image round width="9px" height="12px" :src="GARY" v-else />
                <span class="ml4"> 亮灯详情 </span>
              </div>
              <div class="lightingDetails" v-else>
                <span class="ml4"> 未关联绩效指标 </span>
              </div>
            </div>

            <div class="targetContent">
              {{ data.targetSpecification }}
            </div>
          </div>
          <!-- <div>
            <van-icon name="arrow" />
          </div> -->
        </div>
      </div>
    </div>

    <div class="subList">
      <div class="contentBox">
        <div class="subTarget pd12">下级目标</div>
        <div class="subBox" v-for="i in list">
          <div class="subInfo">
            <van-image round :src="i.subordinateHeadSculpture" lazy-load />
            <div>{{ i.subordinateName }} {{ i.subordinateNumber }}</div>
          </div>
          <div class="targetBox" @click="seeSub(i)">
            <div class="target">
              <div class="targetTitle pdb4 fs14">
                <div class="dian"></div>
                <div class="subTitleText">
                  {{ i.subordinateTargetName }}
                </div>
              </div>
              <div class="targetContent">
                {{ i.subordinateTargetDesc }}
              </div>
              <div class="targetSub" v-if="i.subordinateTargetInfoList">
                <div class="subBox" v-for="sub in i.subordinateTargetInfoList">
                  <van-image
                    round
                    width="20px"
                    height="20px"
                    :src="sub.subordinateHeadSculpture"
                  />
                  <span class="ml8">{{ sub.subordinateName }}</span>
                </div>
              </div>
            </div>
            <div v-if="i.subordinateTargetInfoList.length">
              <van-icon name="arrow" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <van-popup
      v-model="show"
      position="bottom"
      :close-on-click-overlay="false"
      safe-area-inset-bottom
    >
      <div class="popup pd12">
        <div class="topBox">
          <div class="flex justify">
            <div>指标亮灯详情</div>
            <van-icon name="cross" size="16" @click="show = false"></van-icon>
          </div>
          <van-divider />
        </div>
        <div class="popupDetails">
          <!-- <div>亮灯详情</div> -->
          <div class="lightingDetailsBox">
            <div class="flex lightingTitle">
              <div class="f1">考核周期</div>
              <div class="f2">考核活动</div>
              <div class="f3">亮灯类型</div>
            </div>
            <div class="flex lightingContent" v-for="i in detailList">
              <div class="f1">{{ i.period }}</div>
              <div class="f2">{{ i.activityName }}</div>
              <div class="f3">{{ i.lightType }}</div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script>
import { getTargetDisassemblySub, getLightUp } from "@/libs/api.js";
import RED from "@/assets/performances/red.png";
import GARY from "@/assets/performances/gray.png";
import { Toast } from "vant";

export default {
  name: "TargetSubordinate",
  data() {
    return {
      GARY,
      list: [],
      titleList: [],
      data: {
        targetId: "",
        headSculpture: "",
        name: "",
        number: "",
        targetName: "",
        targetSpecification: "",
      },
      index: 0,
      RED,
      show: false,
      lightDetail: {},
      detailList: [],
      id: "",
      isShowRed: true,
    };
  },
  created() {
    const userTargetInfo = this.$store.state.userTargetInfo;
    this.index = Number(this.$route.query.index);

    this.data.name = userTargetInfo.name;
    this.data.headSculpture = userTargetInfo.headSculpture;
    this.data.targetName = userTargetInfo.targetInfoList[this.index].targetName;
    this.data.targetSpecification =
      userTargetInfo.targetInfoList[this.index].targetSpecification;
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.data.targetId = this.id = this.$route.query.id;
      const data = {
        requestVO: this.data,
      };
      this.titleList.push(this.data);
      this.getTargetDisassemblySub(data);
    },
    getTargetDisassemblySub(data) {
      this.$xy.showLoad();
      getTargetDisassemblySub(data).then((res) => {
        this.$xy.hideLoad();
        this.data = res.data.data;
        this.list = res.data.data.subordinateTargetInfoList;
        this.lightDetail = res.data.data.lightMapVOList[0];
        this.detailList = res.data.data.lightMapVOList[0].lightList || [];
        if (this.detailList.length) {
          const hasLightType = this.detailList.some(
            (item) =>
              item.lightType === "红灯" ||
              item.lightType === "黄灯" ||
              item.lightType === "绿灯"
          );
          this.isShowRed = hasLightType;
        }
      });
    },
    seeSub(item) {
      if (item.subordinateTargetInfoList.length > 0) {
        this.data.targetId = item.subordinateTargetId;
        this.data.headSculpture = item.subordinateHeadSculpture;
        this.data.name = item.subordinateName;
        this.data.targetName = item.subordinateTargetName;
        this.data.targetSpecification = item.subordinateTargetDesc;
        const data = {
          requestVO: this.data,
        };
        const obj = {
          ...this.data,
        };
        this.titleList.push(obj);
        this.subTargetInfo = item;
        this.getTargetDisassemblySub(data);
      }
    },
    // lightDetail() {
    //   const data = {
    //     entryid: this.id,
    //   };
    //   this.$xy.showLoad();
    //   getLightUp(data).then((res) => {
    //     this.$xy.hideLoad();
    //     if (res.data.data[0].lightList) {
    //       this.detailList = res.data.data[0].lightList;
    //       this.show = true;
    //     } else {
    //       Toast("暂无详情");
    //     }
    //   });
    // },
    selectTitle(item, num) {
      const data = {
        requestVO: item,
      };
      this.getTargetDisassemblySub(data);

      this.titleList.splice(num + 1, this.titleList.length - num - 1);
    },
  },
};
</script>

<style lang="less" scoped>
.fs14 {
  font-size: 14px !important;
}
.pdb4 {
  padding-bottom: 4px;
}
.pdb8 {
  padding-bottom: 8px;
}
.dian {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #d80c1e;
  margin-right: 8px;
}
.popup {
  //   max-height: 70vh;
  .topBox {
    font-weight: bold;
  }
}
.popupDetails {
  max-height: 70vh;
  overflow: auto;
  font-size: 14px;
}
.lightingDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  line-height: 24px;
  .lightingTitle {
    padding: 12px;
    background-color: #f2f2f2;
  }
  .lightingContent {
    padding: 6px 12px;
  }
  .f1 {
    width: 80px;
  }
  .f2 {
    flex: 1;
  }
  .f3 {
    width: 80px;
    text-align: center;
  }
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.page {
  background-color: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.target {
  background-color: #fff;
  font-size: 14px;
  color: #d80c1e;
  display: flex;
  .titleBox {
    display: flex;
    flex: 1;
    overflow: hidden; /* 超出的文本隐藏 */
    white-space: nowrap; /* 文本不换行 */
    text-overflow: ellipsis; /* 超出部分显示省略号 */
    .titleText {
      overflow: hidden; /* 超出的文本隐藏 */
      white-space: nowrap; /* 文本不换行 */
      text-overflow: ellipsis; /* 超出部分显示省略号 */
    }
  }
}
.lightingDetails {
  padding: 0 12px 12px 0;
  color: #d80c1e;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  .ml4 {
    margin-left: 4px;
  }
}
.contentList,
.subList {
  padding: 12px;
  .contentBox {
    border-radius: 8px;
    background-color: #fff;
    .subBox {
      border-bottom: 1px solid #f2f2f2;
      padding: 12px;
    }
    .subTarget {
      border-bottom: 1px solid #f2f2f2;
    }
    .subInfo {
      display: flex;
      align-items: center;
      font-size: 12px;
      .van-image {
        width: 20px;
        height: 20px;
        margin: 8px 12px 8px 0;
      }
    }
    .userInfo {
      align-items: center;
      border-bottom: 1px solid #f2f2f2;
      .line {
        width: 4px;
        height: 20px;
        background: #d80c1e;
        border-radius: 0px 2px 2px 0px;
      }
      .van-image {
        width: 32px;
        height: 32px;
        margin: 12px 8px;
      }
    }
    .targetBox {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .target {
        display: flex;
        flex-direction: column;
        .targetTitle {
          display: flex;
          align-items: center;
          font-size: 16px;
          color: #212121;
          font-weight: 500;
        }
        .targetContent {
          font-size: 12px;
          color: #666666;
          word-break: break-all;
          line-height: 18px;
          font-weight: 400;
        }
        .targetSub {
          display: flex;
          align-items: center;
          font-size: 12px;
          margin-top: 12px;
          background-color: #fef5f6;
          width: 100%;
          .subBox {
            margin-left: 8px;
            padding: 5px;
            display: flex;
            align-items: center;
          }
        }
      }
    }
  }
}
.targetText {
  width: 224px;
  font-size: 16px;
  color: #212121;
  font-weight: 500;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.subTitleText {
  width: 300px;
  font-size: 16px;
  color: #212121;
  font-weight: 500;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
