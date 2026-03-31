<!-- @format -->

<template>
  <div class="pages-bgc myachievementQuery">
    <div v-if="initData.empInfo">
      <!-- <van-sticky> -->
      <div class="approval-page-sticky">
        <div class="userInfoBox" ref="userInfoBox">
          <div class="seeBox tar" @click="navigator" v-if="showRes">
            查看审批列表
            <van-icon name="arrow" color="#fff" />
          </div>
          <div class="infoBox pd12">
            <div class="flex middle justify">
              <div>
                <van-image class="avatar" round :src="userInfo.headSculpture" />
              </div>
              <div class="info">
                <div class="fw-text flex middle">
                  <div>{{ userInfo.name }}</div>
                  <div class="ml8">{{ userInfo.post }}</div>
                </div>
                <div class="flex middle">
                  <div class="plain-text">{{ userInfo.company }}</div>
                  <div class="plain-text ml8">{{ userInfo.organization }}</div>
                </div>
              </div>
            </div>
            <div class="flex mt10">
              <div class="exam">{{ userInfo.activityName }}</div>
              <!-- <div class="exam">{{ userInfo.period }}</div> -->
            </div>
          </div>
        </div>
      </div>

      <!-- </van-sticky> -->
      <div class="tabsBox">
        <xy-empty v-if="list.length == 0"></xy-empty>
        <van-tabs
          v-model="tabIndex"
          scrollspy
          sticky
          swipe-threshold="3"
          title-active-color="#d80c1e"
          line-width="28"
          line-height="2"
          :offset-top="offsetTop"
        >
          <van-tab
            v-for="(i, index) in list"
            :title="i.areaCustomName"
            :key="i.id"
          >
            <div class="tabHeader plain-text">
              <div class="flex justify">
                {{ i.areaCustomName }}
                <div
                  class="flex fun"
                  v-if="index < 1 && i.areaRegNumber === 'epa_normindctrarea'"
                >
                  <div @click="preview(i)">预览</div>
                  <div v-show="isSupMultiInd" class="flex fun">
                    <div class="btnM">仅看末级指标</div>
                    <van-switch
                      v-model="checked"
                      active-color="#ee0a24"
                      inactive-color="#dcdee0"
                      size="12"
                      @change="changeChecked"
                    />
                  </div>
                </div>
              </div>
              <!-- 指标区 -->
              <SingleIndexCard
                :list="i.targetAreaInfoList"
                :scoreCalcWay="scoreCalcWay === '加权求和' ? true : false"
                :isSupMultiInd="i.isSupMultiInd"
              />
              <!-- 指标区end -->
              <!-- 加减分区 -->
              <SingleIndexCard
                :list="i.plusMinusAreaInfoList"
                :scoreCalcWay="scoreCalcWay === '加权求和' ? true : false"
              />
              <!-- 加减分区end -->
            </div>
          </van-tab>
        </van-tabs>
      </div>
      <van-popup
        round
        rows="3"
        v-model="backData.show"
        :style="{ minWidth: '80%' }"
      >
        <div class="popupBackBox">
          <div class="backTitle"><i class="theme-colors">*</i>退回理由</div>
          <div class="c6 fs12 mt12">点击确定后将退回至上一个任务处理角色</div>
          <div class="backInput">
            <van-field
              v-model="backData.text"
              autosize
              type="textarea"
              placeholder="请输入"
            />
          </div>
        </div>
        <div class="popupFooterBtn">
          <div class="cancel" @click="cancel">取消</div>
          <div class="confirm" @click="confirm">确认</div>
        </div>
      </van-popup>
      <div class="footer flex justify" v-if="showRes">
        <van-button
          class="back"
          @click="back"
          v-if="initData.nodeType != '1070_S'"
          >退回</van-button
        >
        <van-button type="danger" class="pass" @click="approval"
          >通过</van-button
        >
      </div>
    </div>
    <van-popup v-model="isPreview">
      <iframe :src="url"></iframe>
    </van-popup>
  </div>
</template>

<script>
import DOWN from "@/assets/performances/down.png";
import RATE1 from "@/assets/performances/rate1.png";
import RATE2 from "@/assets/performances/rate2.png";
import {
  getApprovalInformationDetails,
  indexApprovalPass,
  indexApprovalReject,
  getCurrentInfoData,
  getPreview,
} from "@/libs/api.js";
import SingleIndexCard from "@/pages/performances/components/SingleIndexCard.vue";

import { Dialog, Toast, ImagePreview } from "vant";
export default {
  name: "ApprovalSingle",
  components: {
    SingleIndexCard,
    [ImagePreview.Component.name]: ImagePreview.Component,
  },

  data() {
    return {
      show: false,
      userInfo: {},
      DOWN: DOWN,
      RATE1: RATE1,
      RATE2: RATE2,
      isFixed: false,
      activeNames: [],
      checked: false,
      offsetTop: 0,
      tabIndex: 0,
      readonly: false,
      backData: {
        show: false,
        text: "",
      },
      list: [],
      showRes: false,
      initData: {},
      scoreCalcWay: "",
      isSupMultiInd: null,
      id: "",
      isPreview: false,
      url: "",
    };
  },
  mounted() {
    this.init();
  },
  computed: {},
  methods: {
    getIsSupMultiInd(list) {
      list.forEach((item) => {
        if (item.isSupMultiInd) {
          this.isSupMultiInd = true;
        }
      });
    },
    preview(item) {
      this.$xy.showLoad();
      const data = {
        handleId: this.$route.query.id,
        areaInsId: item.areaInsId,
        confId: item.areaConfId,
      };
      getPreview(data).then((res) => {
        this.$xy.hideLoad();
        const images = [res.data.data.previewUrl];
        // this.url = res.data.data.previewUrl;
        window.open(res.data.data.previewUrl);
        // this.isPreview = true;
        // ImagePreview(images);
      });
    },
    setOffsetTop() {
      this.$nextTick(() => {
        this.offsetTop = this.$refs.userInfoBox.offsetHeight;
        var element = document.querySelector(".van-tabs__content");
        // 设置上外边距
        element.style.marginTop = this.offsetTop + 8 + "px";
      });
    },
    getCurrentInfoData() {
      this.$xy.showLoad();
      const data = {
        id: this.$route.query.id,
      };
      getCurrentInfoData(data).then((res) => {
        if (res.data.data.backlogState === "1") {
          this.showRes = true;
        }
        this.id = res.data.data.id;
        this.getData();
      });
    },
    getData(val) {
      this.$xy.showLoad();
      const data = {
        handleId: this.id,
        isSupMultiInd: null || val,
      };
      console.log(data, "data");
      getApprovalInformationDetails(data).then((res) => {
        const dataInfo = res.data.data;
        this.initData = dataInfo;
        this.list = dataInfo.areaInfoList;
        if (!this.userInfo.id) {
          this.getIsSupMultiInd(this.list);
        } else {
        }
        this.scoreCalcWay = dataInfo.scoreCalcWay;
        this.userInfo = dataInfo.empInfo;
        this.userInfo.period = dataInfo.period;
        this.userInfo.activityName = dataInfo.activityName;

        this.$xy.hideLoad();
        this.setOffsetTop();
      });
    },

    init() {
      this.getCurrentInfoData();
    },
    approval() {
      const message =
        this.initData.nodeType === "1060_S"
          ? " 即将完成指标审核任务，请确认"
          : " 即将完成指标确认任务，请确认";
      Dialog.confirm({
        message: message,
      })
        .then(() => {
          // on confirm
          const data = {
            taskId: this.id,
          };
          this.$xy.showLoad();
          indexApprovalPass(data).then((res) => {
            this.backData.show = false;
            this.showRes = false;
            this.setOffsetTop();
            this.$xy.hideLoad();
            Toast(res.data.data.message);
            setTimeout(() => {
              this.backPage();
            }, 2000);
          });
        })
        .catch(() => {
          // on cancel
        });
    },
    back() {
      this.backData.show = true;
    },
    cancel() {
      this.backData.show = false;
    },
    confirm() {
      const data = {
        taskId: this.id,
        reason: this.backData.text,
      };
      this.$xy.showLoad();
      indexApprovalReject(data).then((res) => {
        this.backData.show = false;
        this.showRes = false;
        this.setOffsetTop();
        this.$xy.hideLoad();
        Toast(res.data.data.message);
        setTimeout(() => {
          this.backPage();
        }, 2000);
      });
    },
    backPage() {
      this.$store.commit("removeKeepAlive", "MyBacklog");
      const toast = Toast.loading({
        duration: 0, // 持续展示 toast
        forbidClick: true,
        message: "三秒后跳回待办列表",
      });

      let second = 3;
      const timer = setInterval(() => {
        second--;
        if (second) {
          toast.message = ` ${second} 秒后跳回待办列表`;
        } else {
          clearInterval(timer);
          // 手动清除 Toast
          Toast.clear();
          this.$router.push({
            path: "myBacklog",
            query: {
              type: "待办",
            },
          });
        }
      }, 1000);
    },
    changeChecked(val) {
      if (!val) {
        this.getData(!val);
      } else {
        this.getData(!val);
      }
    },
    navigator() {
      this.$router.push({
        path: "approvalList",
        query: {
          handleId: this.id,
          // activityName: this.userInfo.activityName,
          // period: this.userInfo.period,
        },
      });
    },
    seeMore(val) {
      Toast(val);
    },
    showPopup() {
      this.show = true;
    },
  },
};
</script>
<style lang="less" scoped>
// /deep/ .van-sticky--fixed {
//   box-shadow: 0px -3px 0 0 #f2f2f2;
// }
/deep/ .van-tabs__content {
  //margin-top: 8px;
}
/deep/ .van-tabs--line .van-tabs__wrap {
  box-shadow: 0px -1px #fff;

  .van-tabs__nav--line {
    // align-items: self-start;
  }
}
.popupFooterBtn {
  margin-top: 8px;
  display: flex;
  text-align: center;
  border-top: 0.1px solid #ebedf0;

  .cancel {
    padding: 12px;
    flex: 1;
  }
  .confirm {
    padding: 12px;
    flex: 1;
    color: #d80c1e;
    border-left: 0.1px solid #ebedf0;
  }
}
.popupBackBox {
  padding: 12px;
  .backTitle {
    text-align: center;
    font-weight: bold;
    font-size: 20px;
  }
  .backInput {
    margin-top: 8px;
    border: 1px solid #ebedf0;
    border-radius: 8px;
  }
}
.ml4 {
  margin-left: 4px;
}
.subIcon {
  width: 8px;
  height: 8px;
  border-bottom: 1px solid #979797;
  border-left: 1px solid #979797;
}
.stopClick {
  position: absolute;
  left: 0;
  top: 0;
  width: 80vw;
  height: 37px;
  background: transparent;
  z-index: 19;
}
.myachievementQuery {
  padding-bottom: calc(env(safe-area-inset-bottom) + 80px);
}
.pages-bgc {
  background-color: #f2efef;
}
.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 200px;
}
.noticeWH {
  width: 14px;
  height: 14px;
}
.f2Box {
  background: rgba(rgba(242, 242, 242, 0.4));
  padding: 0;
  border-radius: 4px;
  .van-cell::after {
    border-bottom: 1px dashed #d1d1d1;
    right: 0;
  }
}
/deep/ .van-popup {
  .van-popup__close-icon--top-right {
    top: 12px;
  }
}
.popupTitle {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #e5e5e5;
}
.popupContent {
  font-size: 14px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
  div {
    line-height: 24px;
    padding: 8px 12px 0 12px;
  }
  span {
    color: #666;
  }
}
.approval-page-sticky {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 99;
}
.userInfoBox {
  padding: 8px 12px 6px 12px;
  background-image: url("@/assets/performances/infoBgc.png");
  background-size: 100% 100%;
  background-repeat: no-repeat;
  box-sizing: border-box;
  .totalPointsText {
    font-size: 20px;
    color: #d80c1e;
    font-weight: bold;
  }
  .totalPointsText::after {
    content: "";
    position: relative;
    bottom: 2px;
    left: 0;
    display: block;
    width: 100%;
    height: 6px;
    background: linear-gradient(to right, #ee0a0a14, #fff);
  }
  .seeBox {
    color: #fff;
    line-height: 20px;
    font-size: 14px;
    margin-bottom: 4px;
  }
  .infoBox {
    background: white;
    border-radius: 8px;
    .avatar {
      width: 32px;
      height: 32px;
    }
    .info {
      flex: 1;
      margin-left: 10px;
    }
    .exam {
      opacity: 0.8;
      font-size: 12px;
      color: #d80c1e;
      padding: 4px;
      background: #ffebea;
      margin-right: 8px;
      border-radius: 4px;
    }
  }
}
.tabsBox {
  // /deep/ .van-tabs__nav {
  //   background-color: #f2f2f2;
  // }
  /deep/ .van-tab__pane {
    padding-top: 8px;
  }
  .van-tabs__nav--line {
    padding-bottom: 10px;
  }
  .tabHeader {
    padding: 0 12px 0px 12px;
    color: #999;
    .fun {
      color: #d80c1e;
      align-items: center;
    }
    .btnM {
      margin: 0 5px 0 20px;
    }
  }
  .fileBox {
    background: #fafafa;
    border: 0.5px solid rgba(229, 229, 229, 1);
    .iconWH {
      width: 32px;
      height: 32px;
    }
    .fileContent {
      color: #999;
      padding: 0 12px;
      flex: 1;
      display: flex;
      justify-content: space-between;
      flex-direction: column;
      line-height: 16px;
      .fileNameBox {
        max-width: 220px;
        line-height: 20px;
        color: black;
      }
    }
    .downWH {
      width: 16px;
      height: 16px;
    }
  }
  /deep/ .totalPoints {
    .van-field__control {
      &::placeholder {
        text-align: right;
      }
    }

    .van-field__control--right {
      font-size: 16px;
      font-weight: bold;
    }
  }
  /deep/.field,
  .totalPoints {
    .van-cell {
      padding-bottom: 12px !important;
    }
    .van-cell::after {
      border-bottom: 1px solid #e5e5e5;
      left: 0;
    }
  }
  /deep/ .tabContent {
    position: relative;
    overflow: hidden;
    border-radius: 8px;
    background: white;

    textarea {
      text-indent: 0;
    }

    .van-cell {
      padding: 0px 12px 0px 0;
      background-color: transparent;
    }
    .van-collapse-item__content {
      background-color: transparent;
      // padding: 4px 12px;
    }
    .van-collapse-item__title {
      align-items: center;
    }
    .subBox {
      .van-cell {
        padding: 0;
      }
      .van-collapse-item__content {
        padding: 0px;
        .subContent {
          padding: 8px;
        }
      }
    }
    .labelText {
      padding: 6px 0px;
    }
    .subContent {
      // padding: 8px 12px;
      .contentTab {
        background: #f2f2f2;
        border-radius: 4px;
        padding: 2px 4px;
        display: inline-block;
        margin-bottom: 6px;
      }
      i {
        color: #666;
      }
    }
  }
}
.score {
  .avatar {
    width: 24px;
    height: 24px;
  }
  .iconRate {
    width: 12px;
    height: 12px;
  }
  .rate1Bgc {
    padding: 4px;
    background: rgba(27, 163, 157, 0.07);
    border-radius: 4px;
  }
  .rate2Bgc {
    padding: 4px;
    background: rgba(21, 125, 231, 0.06);
    border-radius: 4px;
  }
  .scoreText {
    font-size: 12px;
    padding: 8px 0;
    line-height: 18px;
  }
}
.footer {
  width: 100vw;
  box-sizing: border-box;
  padding: 8px 12px 20px 12px;
  background-color: #fff;
  position: fixed;
  bottom: 0;
  font-weight: bold;
  .back {
    flex: 1;
  }
  .pass {
    margin-left: 12px;
    flex: 1;
  }
}
</style>
