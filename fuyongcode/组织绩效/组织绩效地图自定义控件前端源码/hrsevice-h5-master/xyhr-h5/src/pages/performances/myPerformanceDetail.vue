<!-- @format -->

<template>
  <div class="perDetailPage myachievementQuery">
    <div v-if="initData.employeePojo">
      <div class="user-info-sticky">
        <!-- <van-sticky> -->
        <div class="userInfoBox" ref="userInfoBox">
          <div class="infoBox pd12">
            <div class="flex middle justify">
              <div>
                <van-image class="avatar" round :src="userInfo.employeePhoto" />
              </div>
              <div class="info">
                <div class="fw-text flex middle">
                  <p class="over-text">{{ userInfo.employeeName }}</p>
                  <p class="ml8 over-text">{{ userInfo.jobTitle }}</p>
                </div>
                <div class="plain-text flex middle">
                  <p class="over-text">{{ userInfo.company }}</p>
                  <p class="ml8 over-text">{{ userInfo.department }}</p>
                </div>
              </div>
              <div class="totalPointsBox" v-if="userInfo.scoreResult">
                <div class="totalPointsText">
                  {{ userInfo.scoreResult
                  }}<span
                    v-show="
                      userInfo.gradeResult && userInfo.gradeResult !== '-'
                    "
                    ><i>/</i>{{ userInfo.gradeResult }}</span
                  >
                </div>
                <div
                  class="cycleText"
                  v-if="userInfo.cycleScoreResult || userInfo.cycleGradeResult"
                >
                  本周期: {{ userInfo.cycleScoreResult }}
                  <span
                    v-show="
                      userInfo.cycleGradeResult &&
                      userInfo.cycleGradeResult !== '-'
                    "
                  >
                    <i>/</i>{{ userInfo.cycleGradeResult }}</span
                  >
                </div>
              </div>
              <div class="totalPointsBox" v-else>
                <div class="totalPointsText">
                  {{ userInfo.cycleScoreResult
                  }}<span
                    v-show="
                      userInfo.cycleGradeResult &&
                      userInfo.cycleGradeResult !== '-'
                    "
                    ><i>/</i>{{ userInfo.cycleGradeResult }}</span
                  >
                </div>
              </div>
            </div>
            <div class="flex mt10">
              <div class="exam">{{ userInfo.activityName }}</div>
            </div>
          </div>
        </div>
        <!-- </van-sticky> -->
      </div>
      <div class="tabsBox">
        <van-tabs
          v-model="tabIndex"
          scrollspy
          swipe-threshold="3"
          sticky
          title-active-color="#d80c1e"
          line-width="28"
          line-height="2"
          :offset-top="offsetTop"
        >
          <van-tab
            v-for="(i, index) in detailData"
            :title="i.title"
            :key="i.id"
          >
            <div class="tabHeader plain-text">
              <div>
                <div class="flex justify">
                  {{ i.title }}
                  <p
                    v-if="i.code === 'epa_sumarea' && isShow"
                    @click="navigator(i.areaMessage)"
                  >
                    查看他人整体评价 <van-icon name="arrow"></van-icon>
                  </p>
                </div>

                <!-- 指标区 -->
                <!-- 加减分区 -->
                <PerformanceAreaCard
                  :list="i.areaMessage"
                  :index="index"
                  :id="id"
                  v-if="
                    i.code == 'epa_normindctrarea' ||
                    i.code == 'epa_plusminusarea'
                  "
                />

                <!-- 加减分区end -->
                <!-- 指标区end -->

                <!-- 综合评价区 -->
                <div v-if="i.code == 'epa_customarea'">
                  <FileList :data="i.areaMessage"></FileList>
                </div>

                <!-- 综合评价区end -->
              </div>
              <!-- 总评区 -->
              <div>
                <div
                  class="tabContent pd12 totalPoints mt12"
                  v-if="i.code === 'epa_sumarea'"
                >
                  <van-field
                    v-for="(totalArea, index) in i.areaMessage"
                    :key="index"
                    label-class="c6"
                    label-width="40"
                    v-model="totalArea.content"
                    :label="totalArea.title"
                    readonly
                    rows="1"
                    autosize
                    type="textarea"
                  />
                </div>
              </div>
              <!-- 总评区end -->
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import DOWN from "@/assets/performances/down.png";
import { getPerformanceDetails } from "@/libs/api.js";
import FileList from "@/pages/performances/components/FileList.vue";
import PerformanceAreaCard from "@/pages/performances/components/PerformanceAreaCard.vue";
export default {
  name: "PerformanceDetail",
  components: {
    FileList,
    PerformanceAreaCard,
  },
  data() {
    return {
      userInfo: {},
      DOWN,
      isFixed: false,
      offsetTop: 0,
      tabs: [],
      tabIndex: 0,
      initData: {},
      detailData: [],
      id: "",
      isShow: true,
    };
  },
  mounted() {
    this.getPerDetailsData();
  },

  methods: {
    initOffsetTop() {
      this.$nextTick(() => {
        this.offsetTop = this.$refs.userInfoBox.offsetHeight;
        console.log(" this.offsetTop", this.offsetTop);
        // 获取元素
        let element = document.querySelector(".van-tabs__content");
        // 设置上外边距
        element.style.marginTop = this.offsetTop + 8 + "px";
      });
    },
    getPerDetailsData() {
      this.$xy.showLoad();
      this.id = this.$route.query.id;
      const data = {
        assessmentObjectId: this.$route.query.id,
      };
      getPerformanceDetails(data).then((res) => {
        const detailData = res.data.data;
        this.initData = detailData;
        this.detailData = detailData.data;
        console.log(this.detailData, "this.detailData");
        this.userInfo = detailData.employeePojo;
        const sumAreaInfo = detailData.data.filter(
          (item) => item.code === "epa_sumarea"
        );
        const isShow = sumAreaInfo[0].areaMessage.some(
          (value) => value.showBol === false
        );
        this.isShow = !isShow;

        this.$xy.hideLoad();
        this.initOffsetTop();
      });
    },
    navigator(i) {
      this.$router.push({
        name: "OverallEvaluationDetails",
        params: {
          id: this.id,
          userInfo: this.userInfo,
          list: i,
        },
      });
    },
    downFile(url) {
      console.log(url);
    },
  },
};
</script>
<style lang="less" scoped>
.over-text {
  max-width: 80px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.perDetailPage {
  background-color: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
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
.myachievementQuery {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.performance-detail-page {
  background-color: #f2efef;
}
.user-info-sticky {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 99;
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
.userInfoBox {
  padding: 8px 12px 10px 12px;
  background-image: url("@/assets/performances/infoBgc.png");
  background-size: 100% 100%;
  background-repeat: no-repeat;
  .totalPointsBox {
    font-size: 16px;
    color: #d80c1e;
    font-weight: bold;
    max-width: 160px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    text-align: right;
    .cycleText {
      font-weight: 800;
      margin-top: 8px;
      font-size: 12px;
    }
  }
  .totalPointsText::after {
    content: "";
    position: relative;
    bottom: 2px;
    right: 0;
    display: block;
    width: 100%;
    height: 6px;
    background: linear-gradient(to left, #ee0a0a14, #fff);
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
/deep/ .van-popup {
  .van-popup__close-icon--top-right {
    top: 12px;
  }
}
.fieldBorder {
  /deep/ .van-field__control {
    border: 0.5px solid rgba(229, 229, 229, 1);
    border-radius: 4px;
  }
}

.tabsBox {
  .tabHeader {
    padding: 0 12px 12px 12px;
    color: #999;
  }
  /deep/ .totalPoints {
    .van-field__control {
      &::placeholder {
        text-align: right;
      }
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
  .collapseBox {
    background-color: #fff;
    border-radius: 8px;
    .seeEvaluate {
      text-align: center;
      margin-top: 8px;
    }
  }
  /deep/ .tabContent {
    position: relative;
    border-radius: 8px;
    background: white;
    margin-top: 8px;
    .contentTab {
      background: #ebebeb;
      border-radius: 4px;
      padding: 2px 4px;
      display: inline-block;
      margin-bottom: 6px;
    }
    textarea {
      text-indent: 0;
    }
    .subTitle {
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
      padding: 8px 12px;
      //   background: linear-gradient(to right, #ee0a0a14, #fff);
      .iconWH {
        width: 16px;
        height: 16px;
      }
      .point {
        color: #d80c1e;
      }
    }
    .van-cell {
      padding: 0px 12px 0px 0;
      background-color: transparent;
    }
    .van-collapse-item__content {
      background-color: transparent;
      padding-top: 0;
    }
    .labelText {
      padding: 6px 0px;
    }
    .subContent {
      padding-top: 0;
      padding: 8px;
      background-color: rgba(242, 242, 242, 0.5);
      border-radius: 4px;

      i {
        color: #666;
      }
    }
  }
}
.score {
  .avatarPer {
    width: 20px;
    height: 20px;
  }
  .iconRate {
    width: 12px;
    height: 12px;
  }
  .rate1Bgc {
    padding: 4px;
    background: rgba(239, 249, 248, 1);
    color: #1a9e98;
    border-radius: 4px;
  }
  .ratingBgc {
    padding: 4px;
    background: rgba(253, 245, 246, 1);
    color: rgba(216, 12, 30, 1);
    border-radius: 4px;
  }
  .rate2Bgc {
    padding: 4px;
    background: rgba(241, 247, 254, 1);
    color: #157de7;
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
