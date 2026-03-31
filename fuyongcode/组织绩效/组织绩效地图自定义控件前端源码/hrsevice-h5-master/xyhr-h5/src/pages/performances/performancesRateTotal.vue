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
            @click="navigator('performancesBatchRateTotal')"
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
                  <p class="over-text">{{ empInfo.name }}</p>
                  <p class="ml8 over-text">{{ empInfo.position }}</p>
                </div>
                <div class="plain-text flex middle">
                  <p class="over-text">{{ empInfo.company }}</p>
                  <p class="ml8 over-text">{{ empInfo.organization }}</p>
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
                    :label-width="60"
                    :decimalDigits="decimalDigits"
                    :readonly="
                      status === '2' ||
                      !sumAreaInfo.areaInfoList[0].sumAreaInfo.ose
                        .fieldModifyItem
                    "
                    @blur="total"
                    @input="initOffsetTop"
                    v-model="
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldValue
                    "
                    :placeholder="
                      '评分上下限：' +
                      rateData.allowMinScore +
                      '-' +
                      rateData.allowMaxScore
                    "
                    :label="
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldName
                    "
                    :rules="[
                      { required: true, message: '请输入评分' },
                      {
                        message: `请输入有效的${rateData.allowMinScore}到${rateData.allowMaxScore}之间的数字`,
                        validator: (value) => {
                          if (
                            value === '' ||
                            Number(value) < rateData.allowMinScore ||
                            Number(value) > rateData.allowMaxScore
                          ) {
                            return false;
                          } else {
                            return true;
                          }
                        },
                      },
                    ]"
                  />
                  <TextareaField
                    :label-width="60"
                    v-if="
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.eval &&
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.eval
                        .fieldDisplayItem
                    "
                    :rules="[
                      {
                        required:
                          sumAreaInfo.areaInfoList[0].sumAreaInfo.eval
                            .fieldMustInputItem,
                        message: '请输入说明',
                      },
                    ]"
                    :readonly="
                      status === '2' ||
                      !sumAreaInfo.areaInfoList[0].sumAreaInfo.eval
                        .fieldModifyItem
                    "
                    v-model="
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldValue
                    "
                    placeholder="请输入"
                    :label="
                      sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldName
                    "
                    type="textarea"
                    rows="1"
                    :autosize="{ maxHeight: 100 }"
                  />
                </van-cell-group>
              </van-form>
            </div>
            <div class="link-box" @click="navigator('otherEvaluateDetails')">
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
              <AreaCard
                v-if="i.areaRegNumber == 'epa_normindctrarea'"
                :list="i.targetAreaInfoList"
                :scoreCalcWay="
                  rateData.scoreCalcWay === '加权求和' ? true : false
                "
                :checked="checked"
              />
              <AreaCard
                v-if="i.areaRegNumber == 'epa_plusminusarea'"
                :list="i.plusMinusAreaInfoList"
                :scoreCalcWay="
                  rateData.scoreCalcWay === '加权求和' ? true : false
                "
                :checked="checked"
              />
              <!-- 指标区 -->
              <van-collapse
                style="display: none"
                class="collapseBox mt10"
                v-model="activeNames"
                v-if="i.areaRegNumber === 'epa_normindctrarea'"
              >
                <div
                  class="tabContent"
                  v-for="item in i.targetAreaInfoList"
                  :key="item.indicatorId"
                >
                  <!-- <div class="stopClick" ></div> -->
                  <van-collapse-item :name="item.indicatorId">
                    <template #title>
                      <div class="subTitle fw-text flex middle">
                        <van-image class="iconWH" :src="ICON" />
                        <div class="ovText">
                          <div
                            class="owt"
                            @click="seeMore($event, item.indctrname)"
                          >
                            {{ item.indctrname }}
                          </div>
                        </div>
                        <div class="flex middle ml12">
                          <div
                            class="flex middle"
                            v-if="rateData.scoreCalcWay != '算术求和'"
                          >
                            <van-circle
                              :value="Number(item.weight)"
                              :rate="item.weight"
                              color="#ff0000"
                              size="16"
                              layer-color="#999"
                              :stroke-width="160"
                            />
                            <p class="ml8">{{ item.weight }}%</p>
                          </div>
                          <div v-else>分值：{{ item.indctrscore }}</div>
                        </div>
                      </div>
                    </template>
                    <!-- 单级指标 -->
                    <div class="subContent plain-text">
                      <div class="contentTab" v-if="item.evaltype">
                        {{ item.evaltype }}
                      </div>
                      <div
                        v-for="msg in item.optionalFieldInfo"
                        :key="msg.fieldId"
                      >
                        <p>
                          <i>{{ msg.fieldName }}：</i>
                          {{ msg.fieldValue }}
                        </p>
                      </div>
                    </div>
                    <!-- 单级指标end -->
                    <!-- 指标评价 -->
                    <div v-if="!isShowConfirmBtn">
                      <div
                        class="score"
                        v-for="otherItem in item.otherRatingList"
                        :key="otherItem.id"
                      >
                        <div class="flex middle justify mt12">
                          <van-image
                            class="avatarPer"
                            round
                            :src="otherItem.empInfo.headSculpture"
                          />
                          <div
                            class="fw-text flex middle ml8 fs12"
                            style="flex: 1"
                          >
                            <div>{{ otherItem.empInfo.name }}</div>
                            <div class="ml8">
                              {{ otherItem.empInfo.position }}
                            </div>
                          </div>
                          <div class="flex middle fs12">
                            <div class="ml4 rate1Bgc">
                              {{ otherItem.nodeName }}
                              <span class="c6">
                                ({{ otherItem.nodeWeight }}%)</span
                              >
                            </div>
                            <div
                              class="ml4 ratingBgc"
                              v-if="otherItem.score.isShow"
                            >
                              评分:
                              <span class="c6">{{
                                otherItem.score.value
                              }}</span>
                            </div>
                          </div>
                        </div>
                        <div class="scoreText" v-if="otherItem.desc.isShow">
                          {{ otherItem.desc.value }}
                        </div>
                      </div>
                    </div>
                    <div v-else>
                      <div class="seeEvaluate fs12">
                        查看评价详情
                        <van-icon name="arrow"></van-icon>
                      </div>
                    </div>

                    <!-- 指标评价end -->
                  </van-collapse-item>
                </div>
              </van-collapse>
              <!-- 指标区end -->
              <!-- 加减分区 -->
              <van-collapse
                style="display: none"
                class="collapseBox mt10"
                v-model="activeNames"
                v-if="i.areaRegNumber === 'epa_plusminusarea'"
              >
                <div
                  class="tabContent"
                  v-for="item in i.plusMinusAreaInfoList"
                  :key="item.indicatorId"
                >
                  <van-collapse-item :name="item.indicatorId">
                    <template #title>
                      <div class="subTitle fw-text flex middle">
                        <van-image class="iconWH" :src="ICON" />
                        <div class="ovText">
                          <div
                            class="owt"
                            @click="seeMore($event, item.indctrname)"
                          >
                            {{ item.indctrname }}
                          </div>
                        </div>
                      </div>
                    </template>
                    <!-- 单级指标 -->
                    <div class="subContent plain-text">
                      <div class="contentTab" v-if="item.evaltype">
                        {{ item.evaltype }}
                      </div>
                      <div
                        v-for="msg in item.optionalFieldInfo"
                        :key="msg.fieldId"
                      >
                        <p>
                          <i>{{ msg.fieldName }}：</i>
                          {{ msg.fieldValue }}
                        </p>
                      </div>
                    </div>
                    <!-- 单级指标end -->
                    <!-- 指标评价 -->
                    <div v-if="!isShowConfirmBtn">
                      <div
                        class="score"
                        v-for="otherItem in item.otherRatingList"
                        :key="otherItem.id"
                      >
                        <div class="flex middle justify mt12">
                          <van-image
                            class="avatarPer"
                            round
                            :src="otherItem.empInfo.headSculpture"
                          />
                          <div
                            class="fw-text flex middle ml8 fs12"
                            style="flex: 1"
                          >
                            <div>{{ otherItem.empInfo.name }}</div>
                            <div class="ml8">
                              {{ otherItem.empInfo.position }}
                            </div>
                          </div>
                          <div class="flex middle fs12">
                            <div class="ml4 rate1Bgc">
                              {{ otherItem.nodeName }}({{
                                otherItem.nodeWeight
                              }}%)
                            </div>
                            <div
                              class="ml4 ratingBgc"
                              v-if="otherItem.score.isShow"
                            >
                              评分:{{ otherItem.score.value }}
                            </div>
                          </div>
                        </div>
                        <div class="scoreText">{{ otherItem.desc.value }}</div>
                      </div>
                    </div>
                    <div v-else>
                      <div class="seeEvaluate fs12">
                        查看评价详情
                        <van-icon name="arrow"></van-icon>
                      </div>
                    </div>

                    <!-- 指标评价end -->
                  </van-collapse-item>
                </div>
              </van-collapse>
              <!-- 加减分区end -->
              <!-- 综合评价区 -->
              <div v-if="i.areaRegNumber === 'epa_customarea'">
                <!-- <div
                  class="tabContent pd12 mt10"
                  v-for="item in i.plusMinusAreaInfoList"
                  :key="item.indicatorId"
                >
                  <div class="flex justify middle">
                    <div class="fs14 c6">{{ item.indctrname}}</div>
                  </div>
                
                  <div
                    class="flex fileBox pd12 justify middle mt12"
                    v-for="file in fileList"
                    :key="file.id"
                  >
                    <van-image class="iconWH" :src="file.icon" />
                    <div class="fileContent">
                      <div class="fileNameBox fs14 ovText">{{ file.fileName }}</div>
                      <div class="fs12">{{ file.time }}{{ file.size }}</div>
                    </div>
                    <van-image class="downWH" :src="DOWN" @click="downFile(file.url)" />
                  </div>
                </div>-->
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
          <van-button class="btn" @click="back" :disabled="submitDisabled"
            >退回</van-button
          >
          <van-button
            :disabled="submitDisabled"
            class="btn"
            @click="submit(false, true)"
            >保存</van-button
          >

          <van-button
            :disabled="submitDisabled"
            class="btn"
            type="danger"
            @click="submit(true, true)"
            >提交</van-button
          >
        </div>
      </div>
      <BackPopup :backData="backData" @confirm="confirm" />
    </div>
  </div>
</template>

<script>
import {
  getCurrentInfoData,
  getPerformancesRate,
  submitRateTotal,
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
  name: "performancesRateTotal",
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
      isFixed: false,
      activeNames: [],
      checked: false,
      offsetTop: 0,
      tabIndex: 0,
      id: "",
      submitDisabled: false,
      backData: {
        show: false,
        text: "",
      },
    };
  },
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
    initOffsetTop() {
      this.$nextTick(() => {
        this.offsetTop = this.$refs.userInfoBox.offsetHeight;
        var element = document.querySelector(".van-tabs__content");
        // 设置上外边距
        element.style.marginTop = this.offsetTop + "px";
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
        handleId: this.id,
      };
      getPerformancesRate(params).then((res) => {
        const data = res.data.data;
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
        console.log(this.rateData, sumAreaInfo);
        this.$xy.hideLoad();
        this.initOffsetTop();
      });
    },
    total() {
      this.submit(false);
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
    //保存提交
    async submit(type, toast) {
      const params = {
        type,
        data: {
          backlogId: this.rateData.handleId,
          score: this.sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldValue,
          content: this.sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldValue,
        },
      };
      const check = await this.validateFormSync();
      this.initOffsetTop();

      if (check) {
        if (toast) {
          this.$xy.showLoad();
        }
        submitRateTotal(params)
          .then((res) => {
            if (res.data.statusCode == 200 && toast) {
              Toast(type ? "提交成功" : "保存成功");
              this.$xy.hideLoad();
              if (type) {
                this.submitDisabled = true;
                setTimeout(() => {
                  this.backPage();
                }, 2000);
              }
            }
          })
          .catch((res) => {});
      }
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
.mr4 {
  margin-right: 4px;
}
.over-text {
  max-width: 100px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.approval-page-sticky {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 99;
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
.fun {
  color: #d80c1e;
  align-items: center;
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
  padding-bottom: env(safe-area-inset-bottom);
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
      padding-top: 0;
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
