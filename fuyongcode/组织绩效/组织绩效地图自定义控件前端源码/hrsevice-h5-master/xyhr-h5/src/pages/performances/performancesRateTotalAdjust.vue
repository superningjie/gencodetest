<!--绩效评估-整单评总分-->
<template>
  <div
    class="prt-details"
    :class="status === '1' ? 'safe-bottom-btn' : 'safe-bottom'"
  >
    <div v-if="rateData.areaInfoList">
      <div class="approval-page-sticky">
        <div class="user-info-box" ref="userInfoBox">
          <div
            class="see-box tar"
            v-if="status === '1'"
            @click="navigator('performancesBatchAdjustTotal')"
          >
            批量处理
            <van-icon name="arrow" color="#fff" />
          </div>
          <div class="info-box pd12">
            <div class="flex">
              <div class="avatar">
                <van-image
                  round
                  width="32px"
                  height="32px"
                  :src="empInfo.headSculpture"
                />
              </div>
              <div class="info">
                <div class="fw-text flex middle">
                  <p>{{ empInfo.name }}</p>
                  <p class="ml8">{{ empInfo.position }}</p>
                </div>
                <div class="plain-text">
                  {{ empInfo.company }}
                  <span class="col-line"></span>
                  {{ empInfo.organization }}
                </div>
              </div>
            </div>
            <div class="flex mt10">
              <div class="exam">{{ rateData.activityName }}</div>
              <!-- <div class="exam">{{ rateData.period }}</div> -->
            </div>
            <div class="info-box-rate">
              <van-form ref="form">
                <van-cell-group :border="false">
                  <RateField
                    :decimalDigits="decimalDigits"
                    :readonly="status === '2'"
                    v-model="adjustedScore.atv.fieldValue"
                    :name="rateData.handleId"
                    :placeholder="
                      '评分上下限：' +
                      adjustedScore.atv.minevalscore +
                      '-' +
                      adjustedScore.atv.maxevalscore
                    "
                    :label="adjustedScore.atv.fieldName"
                    @blur="total"
                    @input="initOffsetTop"
                    :rules="[
                      {
                        required: true,
                        message: '请输入调整分',
                      },
                      {
                        message: `请输入有效的${adjustedScore.atv.minevalscore}到${adjustedScore.atv.maxevalscore}之间的数字`,
                        validator: (value) => {
                          if (
                            value === '' ||
                            Number(value) < adjustedScore.atv.minevalscore ||
                            Number(value) > adjustedScore.atv.maxevalscore
                          ) {
                            return false;
                          } else {
                            return true;
                          }
                        },
                      },
                    ]"
                  />
                  <div class="adjustBox flex fs12">
                    <p>总分: {{ adjustedScore.batcs.fieldValue }}</p>

                    <p class="ml8">→</p>
                    <p class="ml8 theme-colors">
                      {{ adjustedScore.atcs.fieldValue }}
                    </p>
                    <div
                      class="gradeDropdown flex ml8"
                      v-if="
                        adjustedScore.atcl.isEnable == 'true' && status === '1'
                      "
                    >
                      <div>等级：</div>
                      <van-dropdown-menu>
                        <van-dropdown-item
                          v-model="adjustedScore.atcl.fieldValue"
                          :options="adjustedScore.atcl.level"
                          @change="selectGrade"
                        />
                      </van-dropdown-menu>
                    </div>
                    <p class="ml8" v-else>
                      {{
                        adjustedScore.atcl.atClName
                          ? "等级：" + adjustedScore.atcl.atClName
                          : adjustedScore.atcl.atClName
                      }}
                    </p>
                  </div>
                  <TextareaField
                    v-if="adjustedScore.ins"
                    :rules="[
                      {
                        required: false,
                        message: '请输入说明',
                      },
                    ]"
                    :readonly="status === '2'"
                    v-model="adjustedScore.ins.fieldValue"
                    :placeholder="status === '2' ? '' : '请输入'"
                    :label="adjustedScore.ins.fieldName"
                    rows="1"
                  />
                </van-cell-group>
              </van-form>
            </div>
            <div class="link-box" @click="navigator('adjustEvaluateDetails')">
              查看他人整体评价
              <van-icon name="arrow" color="#666" />
            </div>
          </div>
        </div>
      </div>
      <div class="tabsBox">
        <van-tabs
          v-model="tabIndex"
          scrollspy
          sticky
          swipe-threshold="3"
          title-active-color="#d80c1e"
          line-width="28"
          line-height="2"
          @scroll="scroll"
          :offset-top="offsetTop"
        >
          <van-tab
            v-for="(i, index) in rateData.areaInfoList"
            :title="i.areaCustomName"
            :key="i.areaConfId"
          >
            <div class="tabHeader plain-text">
              <div class="flex justify">
                <div>{{ i.areaCustomName }}</div>
                <div
                  class="flex fun"
                  v-show="i.areaRegNumber === 'epa_normindctrarea' && index < 1"
                >
                  <p class="mr4">{{ checked ? "收起全部" : "展开全部" }}</p>
                  <van-switch
                    v-model="checked"
                    active-color="#ee0a24"
                    inactive-color="#dcdee0"
                    size="12"
                    @change="changeChecked"
                  />
                </div>
                <div v-show="i.tab === '总评区'">
                  查看他人整体评价
                  <van-icon name="arrow"></van-icon>
                </div>
              </div>
              <!-- 指标区 -->
              <AreaCard
                v-if="i.areaRegNumber == 'epa_normindctrarea'"
                :list="i.targetAreaInfoList"
                :scoreCalcWay="
                  rateData.scoreCalcWay === '加权求和' ? true : false
                "
                :checked="checked"
              />
              <!-- 指标区end -->
              <!-- 加减分区 -->
              <AreaCard
                v-if="i.areaRegNumber == 'epa_plusminusarea'"
                :list="i.plusMinusAreaInfoList"
                :scoreCalcWay="
                  rateData.scoreCalcWay === '加权求和' ? true : false
                "
                :checked="checked"
              />
              <!-- 加减分区end -->
              <!-- 综合评价区 -->
              <div v-if="i.areaRegNumber === 'epa_customarea'">
                <div
                  class
                  v-for="item in i.customAreaInfo.optionalFieldInfo"
                  :key="item.fieldId"
                >
                  <Upload
                    v-if="
                      item.fieldId == 'customfield4' &&
                      'customfield9' &&
                      'customfield10'
                    "
                    :label="item.fieldName"
                    :defList="item.fieldValue"
                    @afterRead="afterRead"
                    :isReadonly="false"
                  />
                </div>
                <div
                  class="tabContent pd12 mt10 field"
                  style="padding-top: 0px; padding-bottom: 0; overflow: hidden"
                >
                  <template v-for="item in i.customAreaInfo.optionalFieldInfo">
                    <van-field
                      :key="item.fieldId"
                      v-if="
                        item.fieldId != 'customfield4' &&
                        'customfield9' &&
                        'customfield10'
                      "
                      class="mt12"
                      label-class="c6"
                      label-width="100"
                      :label="item.fieldName"
                      v-model="item.fieldValue"
                      rows="1"
                      type="textarea"
                      :autosize="{ maxHeight: 80 }"
                      readonly
                    />
                  </template>
                </div>
              </div>
              <!-- 综合评价区end -->
            </div>
          </van-tab>
        </van-tabs>
      </div>
      <div class="footer-fixed" v-if="status === '1'">
        <div class="btn-box">
          <van-button class="btn" @click="back">退回</van-button>
          <van-button class="btn" @click="save(true)">保存</van-button>
          <van-button class="btn" type="danger" @click="submit(true)"
            >提交</van-button
          >
        </div>
      </div>
    </div>
    <BackPopup :backData="backData" @confirm="confirm" />
  </div>
</template>

<script>
import {
  getCurrentInfoData,
  getAdjustedscore,
  submitRateTotal,
  getAdjustedScoreChange,
  getAdjustedScoreSave,
  getAdjustedScoreSubmit,
  backEvaluate,
} from "@/libs/api.js";
import { Toast } from "vant";
import ICON from "@/assets/performances/icon4.png";
import WORD from "@/assets/performances/word.png";
import DOWN from "@/assets/performances/down.png";
import NOTICE from "@/assets/performances/notice.svg";

import RATE1 from "@/assets/performances/rate1.png";
import RATE2 from "@/assets/performances/rate2.png";
import FilterSearch from "@/pages/performances/components/FilterSearch";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
import Upload from "@/pages/performances/components/Upload";
import AreaCard from "@/pages/performances/components/AreaCard.vue";
import BackPopup from "@/pages/performances/components/BackPopup.vue";

export default {
  name: "PerformancesRateTotalAdjust",
  components: {
    FilterSearch,
    RateField,
    TextareaField,
    Upload,
    AreaCard,
    BackPopup,
  },
  data() {
    return {
      status: "1", //1是待办2 是已办
      sumAreaInfo: {},
      empInfo: {},
      rateData: {},
      ICON,
      DOWN: DOWN,
      NOTICE: NOTICE,
      RATE1: RATE1,
      RATE2: RATE2,
      isShowConfirmBtn: false,
      rateFieldValue: undefined,
      textareaFieldValue: "",
      isFixed: false, //
      decimalDigits: null, //小数位
      fileList: [],
      isFixed: false,
      activeNames: [],
      checked: true,
      offsetTop: 0,
      tabIndex: 0,
      adjustedScore: {},
      checked: false,
      id: "",
      backData: {
        show: false,
        text: "",
      },
      pendingAsyncOperations: 0,
    };
  },
  // beforeRouteLeave(to, form, next) {
  //   let data = this.$store.state.keepAlive
  //   data.push("performancesRateTotal")
  //   this.$store.commit("setKeepAlive", data)
  //   next()
  // },
  mounted() {
    this.initGetPerformancesRate();
  },

  methods: {
    changeChecked() {
      console.log(this.checked, "checked");
    },
    seeMore(e, info) {
      e.stopPropagation();
      Toast(info);
    },
    selectGrade() {
      this.save();
    },
    initOffsetTop() {
      this.$nextTick(() => {
        this.offsetTop = this.$refs.userInfoBox.offsetHeight;
        var element = document.querySelector(".van-tabs__content");
        // 设置上外边距
        element.style.marginTop = this.offsetTop + "px";
      });
    },
    arrayTransition(obj) {
      const handleStatusList = obj;
      return handleStatusList.map((item) => {
        const key = Object.keys(item)[0];
        return {
          text: item[key],
          value: key,
          status: false,
        };
      });
    },
    async initGetPerformancesRate() {
      const handleId = this.$route.query.id;
      const {
        data: { data: initValue },
      } = await getCurrentInfoData({ id: handleId });
      this.status = initValue.backlogState;
      this.id = initValue.id;
      this.$xy.showLoad();
      let params = {
        handleId,
      };
      getAdjustedscore(params)
        .then((res) => {
          const data = res.data.data;
          this.adjustedScore = data.adjustedScore;
          this.adjustedScore.atv.fieldValue = this.adjustedScore.atv.fieldValue
            ? this.adjustedScore.atv.fieldValue
            : 0;
          const { areaInfoList } = data;
          const rateData = {
            ...data,
            areaInfoList: areaInfoList.filter(
              ({ areaRegNumber }) => areaRegNumber != "epa_sumarea"
            ),
          };
          const sumAreaInfo = {
            ...data,
            areaInfoList: areaInfoList.filter(
              ({ areaRegNumber }) => areaRegNumber === "epa_sumarea"
            ),
          };
          this.sumAreaInfo = sumAreaInfo;
          this.rateData = rateData;
          this.decimalDigits = Number(data.numAccuracy);
          this.empInfo = res.data.data.empInfo;
          // console.log(this.rateData, sumAreaInfo);
          this.$xy.hideLoad();
          this.initOffsetTop();
        })
        .catch((err) => {
          this.$xy.hideLoad();
          this.backPage();
        });
    },
    async total() {
      const data = {
        handleId: this.id,
        atv: this.adjustedScore.atv.fieldValue,
        batcs: this.adjustedScore.batcs.fieldValue,
      };
      const check = await this.validateFormSync();
      this.initOffsetTop();
      if (check) {
        this.pendingAsyncOperations++;
        getAdjustedScoreChange(data).then((res) => {
          const initData = res.data.data;
          if (initData.message) {
            Toast(initData.message);
          } else {
            this.adjustedScore.atcs.fieldValue = initData.atcs;
            this.adjustedScore.atcl.fieldValue = initData.atcl;
            this.adjustedScore.atcl.atClName = initData.atClName;
            this.pendingAsyncOperations--;
            this.save();
          }
        });
      }
    },

    async save(toast) {
      console.log("保存");
      const params = {
        handleId: this.id,
        ...this.adjustedScore,
      };
      const check = await this.validateFormSync();
      if (check) {
        if (toast) {
          this.$xy.showLoad();
        }
        this.pendingAsyncOperations++;
        getAdjustedScoreSave(params).then((res) => {
          this.pendingAsyncOperations--;
          if (toast) {
            Toast("保存成功");
          }
          this.$xy.hideLoad();
        });
      }
    },
    scroll(e) {
      this.isFixed = e.isFixed;
    },
    navigator(path) {
      this.$router.push({
        path,
        query: {
          id: this.id,
        },
      });
    },
    afterRead() {},
    //全局校验 保存提交时候需要
    validateFormSync() {
      return new Promise((resolve, reject) => {
        this.$refs.form
          .validate()
          .then(() => {
            resolve(true);
          })
          .catch(() => {
            resolve(false);
          });
      });
    },
    async submit(toast = false) {
      // 使用 while 循环等待 pendingAsyncOperations 变为 0
      while (this.pendingAsyncOperations > 0) {
        // 等待一段时间再检查，避免密集循环
        await new Promise((resolve) => setTimeout(resolve, 100));
      }
      console.log("提交", this.pendingAsyncOperations);
      // return;
      const params = {
        handleId: this.id,
        ...this.adjustedScore,
      };
      const check = await this.validateFormSync();
      if (check) {
        if (toast) {
          this.$xy.showLoad();
        }
        getAdjustedScoreSubmit(params).then((res) => {
          if (res.data.data.length > 0) {
            // Toast("失败");
          } else {
            this.$xy.hideLoad();
            Toast("提交成功");
            setTimeout(() => {
              this.backPage();
            }, 2000);
          }
        });
      }
    },
    back() {
      this.backData.show = true;
    },
    confirm() {
      const backData = {
        backBol: true,
        waitId: this.id,
        message: this.backData.text,
      };
      this.$xy.showLoad();
      this.backEvaluate(backData, true);
    },
    backEvaluate(data, val = false) {
      backEvaluate(data)
        .then((res) => {
          this.$xy.hideLoad();
          this.backRes = res.data;
          if (val) {
            this.backData.show = false;
            Toast(res.data.message);
            setTimeout(() => {
              this.backPage();
            }, 2000);
          }
        })
        .catch(() => {
          this.$xy.hideLoad();
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
  },
};
</script>
<style lang="less" scoped>
.approval-page-sticky {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 99;
}
.mr4 {
  margin-right: 4px;
}
.fun {
  color: #d80c1e;
  align-items: center;
}
.ml4 {
  margin-left: 4px;
}
.adjustBox {
  justify-content: end;
  padding: 4px 16px;
  /deep/ .van-dropdown-menu__bar {
    height: 10px;
  }
}
/deep/ .van-tab__pane {
  padding-top: 8px;
}
/deep/ .van-tabs--line .van-tabs__wrap {
  box-shadow: 0px -1px #fff;

  .van-tabs__nav--line {
    // align-items: self-start;
  }
}
.ml4 {
  margin-left: 4px;
}
.ovText {
  padding-left: 12px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  flex: 1;
  .owt {
    max-width: 160px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    display: inline-block;
    vertical-align: middle;
  }
}

.safe-bottom-btn {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 20px);
}
.prt-details {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.user-info-box {
  padding: 8px 12px 0 12px;
  background-image: url("@/assets/performances/infoBgc.png");
  background-size: 100% 100%;
  background-repeat: no-repeat;
  padding-bottom: 6px;
  .see-box {
    color: #fff;
    line-height: 20px;
    font-size: 14px;
    margin-bottom: 4px;
  }
  .info-box {
    background: white;
    border-radius: 8px;
    .info {
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
    .info-box-rate {
      margin-left: -10px;
      margin-right: -10px;
    }
    .link-box {
      font-size: 12px;
      color: #666666;
      line-height: 18px;
      text-align: center;
    }
  }
}
.tabsBox {
  /deep/ .van-sticky--fixed {
    z-index: 98;
  }
  /deep/ .van-tabs__nav {
    //background-color: #f2efef;
  }
  /deep/ .van-tab--active {
    font-weight: bold;
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
  .collapseBox {
    background-color: #fff;
    border-radius: 8px;
  }
  /deep/ .tabContent {
    position: relative;
    border-radius: 8px;
    background: white;
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
      align-items: center;
      background-color: transparent;
    }
    .van-collapse-item__content {
      background-color: transparent;
    }
    .subBox {
      .van-cell {
        padding: 0;
      }
      .van-collapse-item__content {
        padding: 8px 0;
        .subContent {
          padding: 8px;
        }
      }
    }
    .labelText {
      padding: 6px 0px;
    }
    .subContent {
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
    display: inline-flex;
    flex-wrap: nowrap;
    align-items: center;
    flex-direction: row;
    font-size: 12px;
    margin-right: 6px;
    background: #f2f2f2;
    border-radius: 4px;
    padding: 6px;
  }
  .ratingBgc {
    padding: 4px;
    background-color: rgba(242, 242, 242, 0.5);
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
.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
}
.btn-box {
  padding: 12px;
  padding-left: 0;
  display: flex;
  .btn {
    margin-left: 12px;
    width: 100%;
  }
}
</style>
