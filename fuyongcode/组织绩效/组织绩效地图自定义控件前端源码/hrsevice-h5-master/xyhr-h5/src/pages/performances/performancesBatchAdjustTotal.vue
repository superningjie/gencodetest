<!--绩效评估-批量整单调整总分-->
<template>
  <div class="prt-batch safe-bottom" v-if="show">
    <van-sticky>
      <van-search
        v-model="value"
        placeholder="请输入您想搜索的人名"
        background="#fff"
        @search="onSearch"
      >
        <template #action>
          <div class="filter-box">
            <div class="filter-text" @click="showFilterPicker = true">筛选</div>
            <van-image :src="shaixuan" class="filter-icon"></van-image>
          </div>
        </template>
      </van-search>
      <van-dropdown-menu>
        <van-dropdown-item title="状态" ref="item">
          <van-cell
            center
            :title="val.text"
            v-for="val in statusList"
            :key="val.id"
            @click="selectStatus(val)"
          >
            <template #right-icon>
              <van-checkbox v-model="val.status" checked-color="#D50C1C" />
            </template>
          </van-cell>
          <div class="flex">
            <div class="btn-box-cancel" @click="onCancel">取消</div>
            <div class="btn-box-confirm" @click="onConfirm">确认</div>
          </div>
        </van-dropdown-item>
        <van-dropdown-item title="组织" ref="organ">
          <van-cell
            center
            :title="val.text"
            v-for="val in organizationList"
            :key="val.id"
            @click="selectOrganization(val)"
          >
            <template #right-icon>
              <van-checkbox v-model="val.status" checked-color="#D50C1C" />
            </template>
          </van-cell>
          <div class="flex">
            <div class="btn-box-cancel" @click="onCancel">取消</div>
            <div class="btn-box-confirm" @click="onConfirm">确认</div>
          </div>
        </van-dropdown-item>
      </van-dropdown-menu>
    </van-sticky>
    <xy-empty v-if="!biaoji && batchRatingInfoList.length == 0"></xy-empty>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="batch-box">
        <div class="batch-info">
          <div class="title">
            已提交/总人数：
            <span>{{ rateData.submittedNumber }}</span>
            /{{ rateData.totalNumber }}
          </div>
          <div class="link-list" @click="navigator('rankingList')">
            查看排名
          </div>
        </div>
        <div class="batch-list">
          <van-form ref="form">
            <van-checkbox-group v-model="result" ref="checkboxGroup">
              <van-list
                v-model="loading"
                :finished="finished"
                :finished-text="
                  batchRatingInfoList.length == 0 ? '' : '没有更多了'
                "
                class="list-box"
                :immediate-check="false"
                @load="onLoad"
                :offset="60"
              >
                <div
                  class="info-box mb12"
                  v-for="(item, index) in batchRatingInfoList"
                  :key="item.handleId"
                >
                  <div class="pink-box">
                    <div class="flex middle">
                      <van-checkbox
                        class="mr12"
                        v-if="item.scoreStatusCode != '2'"
                        :disabled="item.scoreStatusCode == '1'"
                        :name="item.handleId"
                        shape="square"
                        icon-size="20px"
                        checked-color="#ee0a24"
                        @click="toggle()"
                      />
                      <div
                        class="flex middle"
                        style="flex: 1"
                        @click="
                          navigator(
                            'performancesRateTotalAdjust',
                            item.handleId
                          )
                        "
                      >
                        <div class="avatar">
                          <van-image
                            lazy-load
                            round
                            width="32px"
                            height="32px"
                            :src="item.empInfo.headSculpture"
                          />
                        </div>
                        <div class="info">
                          <div class="fw-text flex middle">
                            <p>{{ item.empInfo.name }}</p>
                            <p class="ml8">{{ item.empInfo.position }}</p>
                          </div>
                          <div class="plain-text">
                            {{ item.empInfo.company }}
                            <span class="ml8">
                              {{ item.empInfo.organization }}</span
                            >
                          </div>
                        </div>
                        <div>
                          <van-tag color="#ED6A0C" size="medium" plain>{{
                            item.scoreStatus
                          }}</van-tag>
                        </div>
                        <van-icon name="arrow" color="#666" />
                      </div>
                    </div>
                  </div>
                  <div class="ml12 flex mt10">
                    <div class="exam">{{ rateData.activityName }}</div>
                    <!-- <div class="exam">{{ rateData.period }}</div> -->
                  </div>
                  <div>
                    <van-cell-group :border="false">
                      <RateField
                        :name="item.handleId"
                        @blur="total(item.handleId, index)"
                        :decimalDigits="decimalDigits"
                        v-model.number="item.adjustedScore.atv.fieldValue"
                        :placeholder="
                          '评分上下限：' +
                          item.adjustedScore.atv.minevalscore +
                          '-' +
                          item.adjustedScore.atv.maxevalscore
                        "
                        :label="item.adjustedScore.atv.fieldName"
                        :readonly="item.scoreStatusCode == '2'"
                        :rules="[
                          { required: true, message: '请输入调整分' },
                          {
                            message: `请输入有效的${item.adjustedScore.atv.minevalscore}到${item.adjustedScore.atv.maxevalscore}之间的数字`,
                            validator: (value) => {
                              if (
                                value === '' ||
                                Number(value) <
                                  item.adjustedScore.atv.minevalscore ||
                                Number(value) >
                                  item.adjustedScore.atv.maxevalscore
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
                        <p>总分: {{ item.adjustedScore.batcs.fieldValue }}</p>

                        <p class="ml8">→</p>
                        <p class="ml8 theme-colors">
                          {{ item.adjustedScore.atcs.fieldValue }}
                        </p>
                        <div
                          class="gradeDropdown flex ml8"
                          v-if="item.adjustedScore.atcl.isEnable == 'true'"
                        >
                          <div>等级：</div>
                          <van-dropdown-menu>
                            <van-dropdown-item
                              v-model="item.adjustedScore.atcl.fieldValue"
                              :options="item.adjustedScore.atcl.level"
                              @change="selectGrade(item.handleId, index)"
                            />
                          </van-dropdown-menu>
                        </div>
                        <p class="ml8" v-else>
                          {{
                            item.adjustedScore.atcl.atClName
                              ? "等级：" + item.adjustedScore.atcl.atClName
                              : item.adjustedScore.atcl.atClName
                          }}
                        </p>
                      </div>
                      <TextareaField
                        v-model="item.adjustedScore.ins.fieldValue"
                        placeholder="请输入"
                        :autosize="{ maxHeight: 100 }"
                        type="textarea"
                        :label="item.adjustedScore.ins.fieldName"
                        rows="1"
                        :readonly="item.scoreStatusCode == '2'"
                      />
                    </van-cell-group>
                  </div>
                  <div
                    class="link-box pd12"
                    @click="navigator('adjustEvaluateDetails', item.handleId)"
                  >
                    查看他人整体评价
                    <van-icon name="arrow" color="#666" />
                  </div>
                </div>
              </van-list>
            </van-checkbox-group>
          </van-form>
        </div>
      </div>
    </van-pull-refresh>
    <div class="footer-fixed">
      <div class="btn-box">
        <van-row type="flex" gutter="12">
          <van-col span="6" class="checkBox">
            <van-checkbox
              v-model="allChecked"
              shape="square"
              icon-size="20px"
              checked-color="#ee0a24"
              @click="allCheckedChange"
              >全选</van-checkbox
            >
          </van-col>
          <van-col span="5">
            <van-button
              :disabled="result.length == 0 || submitDisabled"
              class="btn"
              @click="back"
              >退回</van-button
            >
          </van-col>
          <van-col span="5">
            <van-button
              :disabled="
                rateData.submittedNumber == rateData.totalNumber ||
                submitDisabled
              "
              class="btn"
              @click="saveChecked"
              >保存</van-button
            >
          </van-col>
          <van-col span="8">
            <van-button
              :disabled="result.length == 0 || submitDisabled"
              class="btn"
              type="danger"
              @click="submitChecked"
              >提交已选（{{ result.length }}个)</van-button
            >
          </van-col>
        </van-row>
      </div>
    </div>
    <van-dialog
      v-model="confirmUserShow"
      title="确认提交以下人员的绩效评估"
      show-cancel-button
      @confirm="confirm"
    >
      <div class="confirm-user">
        <div class="user-box" v-for="item in checkedList">
          <van-image round class="user-img" :src="item.empInfo.headSculpture" />
          <div class="user-text">
            {{ item.empInfo.name }} {{ item.empInfo.position }}
          </div>
          <div class="user-rate">{{ item.adjustedScore.atcs.fieldValue }}</div>
        </div>
      </div>
    </van-dialog>
    <leadercommonTree
      :resetShow="true"
      :jobInfo="jobInfo"
      :showFilterPicker="showFilterPicker"
      @isshowFilterPicker="isshowFilterPicker"
      @clickSearch="clickSearch"
    />
    <BackPopup :backData="backData" @confirm="batchConfirm" />
  </div>
</template>

<script>
import {
  getBatchAdjustedList,
  submitBatchRateTotal,
  getAdjustedScoreChange,
  getBatchAdjustedSave,
  getBatchAdjustedSubmit,
  getAdjustedScoreStatus,
  backEvaluate,
} from "@/libs/api.js";
import shaixuan from "@/assets/performances/shaixuan.svg";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
import leadercommonTree from "@/components/leadercommonTree";
import BackPopup from "@/pages/performances/components/BackPopup.vue";

import { Toast } from "vant";
export default {
  name: "PerformancesBatchAdjustTotal",
  components: {
    RateField,
    TextareaField,
    leadercommonTree,
    BackPopup,
  },
  data() {
    return {
      shaixuan,
      statusList: [],
      show: false,
      decimalDigits: null, //小数位
      rateData: {},
      batchRatingInfoList: [],
      rateFieldValue: undefined,
      textareaFieldValue: "",
      confirmUserShow: false,
      value: "",
      organizationId: [],
      jobInfo: {},
      workPosition: "0",
      showFilterPicker: false,
      checkedList: [],
      titleText: "组织",
      handleStatusIds: [],
      organizationList: [],
      biaoji: true,
      refreshing: false,
      submitDisabled: false,
      allChecked: false,
      result: [],
      paramsPages: {
        pages: 1,
        limit: 10,
      },
      loading: false,
      finished: false,
      backData: {
        show: false,
        text: "",
      },
    };
  },
  mounted() {
    this.getStatus();
    this.init();
  },
  methods: {
    onLoad() {
      this.paramsPages.pages++;
      this.init();
    },
    getStatus() {
      const data = {
        handleId: this.$route.query.id,
      };
      getAdjustedScoreStatus(data).then((res) => {
        this.statusList = this.arrayTransition(res.data.data.handleStatusList);
        this.organizationList = this.arrayTransition(
          res.data.data.organizationList
        );
      });
    },
    allCheckedChange() {
      this.$refs.checkboxGroup.toggleAll({
        checked: this.allChecked,
        skipDisabled: true,
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
    selectStatus(v) {
      v.status = !v.status;
    },
    selectOrganization(v) {
      v.status = !v.status;
    },
    toggle() {
      console.log(this.result, "res");
      this.allChecked =
        this.result.length ==
        this.rateData.totalNumber - this.rateData.submittedNumber
          ? true
          : false;
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    // 去重
    uniqueById(arr) {
      const ids = new Set();
      return arr.filter((item) => {
        if (ids.has(item.handleId)) {
          return false;
        } else {
          ids.add(item.handleId);
          return true;
        }
      });
    },
    selectAll() {
      const array = this.result;
      let batchRatingInfoList = [];
      const list = this.batchRatingInfoList;
      for (let i = 0; i < list.length; i++) {
        if (array.includes(list[i].handleId)) {
          batchRatingInfoList.push(list[i]);
        }
      }
      this.checkedList = this.uniqueById(batchRatingInfoList);
    },
    submitChecked() {
      this.selectAll();
      this.confirmUserShow = true;
    },
    clickSearch(item) {
      this.organizationId = item.code;
      this.showFilterPicker = false;
      this.onSearch();
    },
    onSearch() {
      this.init();
    },
    back() {
      this.backData.show = true;
    },
    batchConfirm() {
      const backData = {
        backBol: true,
        waitIds: this.result,
        message: this.backData.text,
      };
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
              this.paramsPages.pages = 1;
              this.init();
            }, 2000);
          }
        })
        .catch(() => {
          this.$xy.hideLoad();
        });
    },
    async init() {
      this.$xy.showLoad();
      this.loading = true;
      this.refreshing = false;
      this.finished = false;
      this.biaoji = true;
      let params = {
        handleId: this.$route.query.id,
        searchContent: this.value, //搜索内容
        handleStatusIds: this.handleStatusIds,
        organizationalIds: this.organizationalIds, //组织ID
        ...this.paramsPages,
      };
      if (this.paramsPages.pages <= 1) {
        this.batchRatingInfoList = [];
        this.result = [];
      }
      await getBatchAdjustedList(params).then((res) => {
        this.rateData = res.data.data;
        this.batchRatingInfoList.push(...res.data.data.batchRatingInfoList);
        if (this.rateData.batchRatingInfoList == 0) {
          this.biaoji = false;
        }
        if (res.data.data.batchRatingInfoList.length < this.paramsPages.limit) {
          this.finished = true;
        }
        this.decimalDigits = Number(this.rateData.numAccuracy);
        this.checkedList = [];
        this.submitDisabled = false;
        this.loading = false;
        this.$xy.hideLoad();
        this.show = true;
      });
      if (this.allChecked) {
        this.$nextTick(() => {
          this.allCheckedChange();
        });
      }
    },
    navigator(path, id) {
      this.$router.push({
        path,
        query: {
          id: id != undefined ? id : this.$route.query.id,
        },
      });
    },
    async total(handleId, index) {
      const item = this.batchRatingInfoList[index];
      const data = {
        handleId: handleId,
        atv: item.adjustedScore.atv.fieldValue,
        batcs: item.adjustedScore.batcs.fieldValue,
      };
      this.$refs.form
        .validate(handleId)
        .then(() => {
          getAdjustedScoreChange(data).then((res) => {
            const initData = res.data.data;
            if (initData.message) {
              // Toast(initData.message);
            } else {
              this.batchRatingInfoList[index].adjustedScore.atcl.fieldValue =
                initData.atcl;
              this.batchRatingInfoList[index].adjustedScore.atcs.fieldValue =
                initData.atcs;
              const params = {
                data: [
                  {
                    handleId: handleId,
                    ...this.batchRatingInfoList[index].adjustedScore,
                  },
                ],
              };
              this.save(params);
              this.batchRatingInfoList[index].scoreStatus = "待提交";
              this.batchRatingInfoList[index].scoreStatusCode = "3";
            }
          });
        })
        .catch(() => {
          console.log("不通过");
        });
    },
    onCancel() {
      this.$refs.item.toggle(false);
      this.$refs.organ.toggle(false);
    },
    onConfirm() {
      const arr = this.statusList;
      const statusList = [];
      arr.map((item) => {
        if (item.status) {
          statusList.push(item.value);
        }
      });
      this.handleStatusIds = statusList;
      const organ = this.organizationList;
      const organList = [];
      organ.map((item) => {
        if (item.status) {
          organList.push(item.value);
        }
      });
      this.organizationalIds = organList;
      this.$refs.item.toggle(false);
      this.$refs.organ.toggle(false);
      this.init();
    },
    async onRefresh() {
      this.value = ""; //搜索内容
      this.organizationalIds = []; //组织ID
      this.handleStatusIds = [];
      this.getStatus();
      await this.init();
    },
    save(params) {
      const data = {
        data: [],
      };
      data.data.push(params);
      this.adjustedSave(params);
    },
    adjustedSave(data, toast) {
      if (toast) {
        this.$xy.showLoad();
      }
      getBatchAdjustedSave(data).then((res) => {
        if (toast) {
          Toast("保存成功");
          this.$xy.hideLoad();
        }
      });
      // this.$refs.form
      //   .validate()
      //   .then(() => {

      //   })
      //   .catch((err) => {
      //     this.$xy.hideLoad();
      //     if (err && err[0].name) {
      //       this.$refs.form.scrollToField(err[0].name, false);
      //     }
      //   });
    },
    adjustedSubmit(data) {
      this.$xy.showLoad();
      getBatchAdjustedSubmit(data).then((res) => {
        this.submitDisabled = true;
        Toast("提交成功");
        this.$xy.hideLoad();
        setTimeout(() => {
          this.init();
        }, 2000);
      });
    },
    saveChecked() {
      const data = {
        data: [],
      };
      // this.checkedList.forEach((item) => {
      //   const itemData = {
      //     handleId: item.handleId,
      //     ...item.adjustedScore,
      //   };
      //   data.data.push(itemData);
      // });
      const saveList = [];
      this.batchRatingInfoList.forEach((item) => {
        if (item.scoreStatusCode === "3") {
          const itemData = {
            handleId: item.handleId,
            ...item.adjustedScore,
          };
          saveList.push(itemData);
        }
      });
      data.data = saveList;
      this.adjustedSave(data, true);
    },
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
    submit() {
      const data = {
        data: [],
      };
      this.checkedList.forEach((item) => {
        const itemData = {
          handleId: item.handleId,
          ...item.adjustedScore,
        };
        data.data.push(itemData);
      });
      this.adjustedSubmit(data);
    },
    confirm() {
      this.submit();
    },
    selectGrade(handleId, index) {
      const params = {
        data: [
          {
            handleId: handleId,
            ...this.batchRatingInfoList[index].adjustedScore,
          },
        ],
      };
      this.adjustedSave(params);
    },
  },
};
</script>
<style lang="less" scoped>
/deep/ .van-search__action:active {
  background-color: inherit;
}
/deep/ .van-dropdown-menu__bar {
  box-shadow: none;
}
.van-row--flex {
  align-items: center;
}
.btn-box-confirm,
.btn-box-cancel {
  flex: 1;
  text-align: center;
  height: 40px;
  line-height: 40px;
}
.btn-box-confirm {
  color: #d80c1e;
  border-left: 1px solid #ebedf0;
}
.adjustBox {
  justify-content: end;
  padding: 4px 16px;
  /deep/ .van-dropdown-menu__bar {
    height: 10px;
  }
}
.filter-box {
  display: flex;
  justify-content: center;
  flex-wrap: nowrap;
  margin-left: 6px;
  .filter-text {
    padding-left: 12px;
    padding-right: 6px;
    border-left: 1px solid #e5e5e5;
    color: #666;
    font-size: 14px;
  }
  .filter-icon {
    width: 18px;
  }
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.prt-batch {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.batch-box {
  padding: 0 12px;
  .batch-info {
    display: flex;
    font-size: 12px;
    justify-content: space-between;
    padding: 12px 0;
    .title {
      color: #212121;
    }
    .link-list {
      color: #d80c1e;
    }
  }
  .batch-list {
    .see-box {
      color: #fff;
      line-height: 20px;
      font-size: 14px;
      margin-bottom: 4px;
    }
    .info-box {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      .pink-box {
        background: linear-gradient(to right, #fff6fa, #fff);
        padding: 12px;
      }
      .info {
        margin-left: 10px;
        flex: 1;
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

      .link-box {
        font-size: 12px;
        color: #666666;
        line-height: 18px;
        text-align: center;
      }
    }
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
  .btn {
    width: 100%;
  }
}
.confirm-user {
  max-height: 70vh;
  overflow: auto;
  .user-box {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    padding: 12px;
    .user-img {
      width: 24px;
      height: 24px;
    }
    .user-text {
      flex: 1;
      font-size: 14px;
      color: #212121;
      padding: 0 12px;
    }
    .user-rate {
      background: #fdf5f6;
      border-radius: 4px;
      font-size: 14px;
      color: #d80c1e;
      padding: 6px;
    }
  }
}
</style>
