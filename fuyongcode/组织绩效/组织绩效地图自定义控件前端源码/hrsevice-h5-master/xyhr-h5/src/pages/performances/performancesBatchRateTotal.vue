<!--绩效评估-批量整单评总分-->
<template>
  <div class="prt-batch safe-bottom" v-if="show">
    <van-sticky>
      <van-search
        v-model="value"
        placeholder="请输入您想搜索的人名"
        background="#fff"
        @search="onSearch"
      >
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
      <div class="batch-box" v-if="biaoji">
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
                  v-for="item in batchRatingInfoList"
                  :key="item.handleId"
                >
                  <div class="pink-box">
                    <div class="flex middle">
                      <van-checkbox
                        class="mr12"
                        v-if="item.scoreStatusCode != '2'"
                        :name="item.handleId"
                        shape="square"
                        icon-size="20px"
                        checked-color="#ee0a24"
                        :disabled="item.scoreStatusCode == '1'"
                        @click="toggle()"
                      />
                      <div
                        class="flex middle"
                        style="flex: 1"
                        @click="
                          navigator('performancesRateTotal', item.handleId)
                        "
                      >
                        <div class="avatar">
                          <van-image
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
                        :label-width="60"
                        :decimalDigits="decimalDigits"
                        v-model.number="item.sumAreaInfo.ose.fieldValue"
                        :placeholder="
                          '评分上下限：' +
                          rateData.allowMinScore +
                          '-' +
                          rateData.allowMaxScore
                        "
                        :readonly="item.scoreStatus == '已提交'"
                        :name="`${item.handleId}`"
                        :label="item.sumAreaInfo.ose.fieldName"
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
                        @blur="blurSave(item)"
                      />
                      <TextareaField
                        v-if="
                          item.sumAreaInfo.eval &&
                          item.sumAreaInfo.eval.fieldName
                        "
                        :label-width="60"
                        :readonly="item.scoreStatus == '已提交'"
                        v-model="item.sumAreaInfo.eval.fieldValue"
                        placeholder="请输入"
                        :autosize="{ maxHeight: 100 }"
                        type="textarea"
                        :label="item.sumAreaInfo.eval.fieldName"
                        rows="1"
                        @blur="blurSave(item)"
                      />
                    </van-cell-group>
                  </div>
                  <div
                    class="link-box pd12"
                    @click="navigator('otherEvaluateDetails', item.handleId)"
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
    <div class="footer-fixed" v-if="biaoji">
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
            <van-button :disabled="result.length == 0" class="btn" @click="back"
              >退回</van-button
            >
          </van-col>
          <van-col span="5">
            <van-button class="btn" @click="submit(false, true)"
              >保存</van-button
            >
          </van-col>
          <van-col span="8">
            <van-button
              :disabled="result.length == 0"
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
          <div class="user-rate">{{ item.sumAreaInfo.ose.fieldValue }}</div>
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
  getBatchRate,
  submitBatchRateTotal,
  getBatchTotalStatus,
  backEvaluate,
} from "@/libs/api.js";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
import leadercommonTree from "@/components/leadercommonTree";
import BackPopup from "@/pages/performances/components/BackPopup.vue";

import { Toast } from "vant";
export default {
  name: "PerformancesBatchRateTotal",
  components: {
    RateField,
    TextareaField,
    leadercommonTree,
    BackPopup,
  },
  data() {
    return {
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
      organizationList: [],
      handleStatusIds: [],
      biaoji: true,
      refreshing: false,
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
    async init() {
      this.$xy.showLoad();
      this.loading = true;
      this.refreshing = false;
      this.finished = false;
      this.biaoji = true;
      let params = {
        handleId: this.$route.query.id,
        searchContent: this.value, //搜索内容
        organizationalIds: this.organizationalIds, //组织ID
        handleStatusIds: this.handleStatusIds,
        ...this.paramsPages,
      };
      if (this.paramsPages.pages <= 1) {
        this.batchRatingInfoList = [];
        this.result = [];
      }
      await getBatchRate(params).then((res) => {
        this.rateData = res.data.data;
        this.batchRatingInfoList.push(...res.data.data.batchRatingInfoList);
        if (this.rateData.batchRatingInfoList == 0) {
          this.biaoji = false;
        }
        this.decimalDigits = Number(this.rateData.numAccuracy);
        this.checkedList = [];
        if (res.data.data.batchRatingInfoList.length < this.paramsPages.limit) {
          this.finished = true;
        }
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
    getStatus() {
      const data = {
        handleId: this.$route.query.id,
      };
      getBatchTotalStatus(data).then((res) => {
        this.statusList = this.arrayTransition(res.data.data.handleStatusList);
        this.organizationList = this.arrayTransition(
          res.data.data.organizationList
        );
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
    allCheckedChange() {
      this.$refs.checkboxGroup.toggleAll({
        checked: this.allChecked,
        skipDisabled: true,
      });
    },
    toggle() {
      console.log(this.result, "res");
      this.allChecked =
        this.result.length ==
        this.rateData.totalNumber - this.rateData.submittedNumber
          ? true
          : false;
    },
    selectStatus(v) {
      v.status = !v.status;
    },
    selectOrganization(v) {
      v.status = !v.status;
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
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
    onCancel() {
      this.$refs.item.toggle(false);
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

    navigator(path, id) {
      this.$router.push({
        path,
        query: {
          id: id != undefined ? id : this.$route.query.id,
        },
      });
    },
    confirm() {
      this.submit(true, true);
    },
    blurSave(item) {
      this.$refs.form.validate(item.handleId).then(() => {
        const data = {
          backlogId: item.handleId,
          score: item.sumAreaInfo.ose.fieldValue,
          content: item.sumAreaInfo.eval.fieldValue,
        };

        const params = {
          type: false,
          data: data,
        };
        submitBatchRateTotal(params)
          .then((res) => {
            if (res.data.statusCode == 200) {
              item.scoreStatus = "待提交";
              item.scoreStatusCode = "3";
            }
          })
          .catch((res) => {});
      });
    },
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
    back() {
      this.backData.show = true;
    },
    batchConfirm() {
      const backData = {
        backBol: true,
        waitIds: this.result,
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
              this.paramsPages.pages = 1;
              this.init();
            }, 2000);
          }
        })
        .catch(() => {
          this.$xy.hideLoad();
        });
    },
    batchSubmit(params, toast = false) {
      if (toast) {
        this.$xy.showLoad();
      }
      submitBatchRateTotal(params)
        .then((res) => {
          if (res.data.statusCode == 200) {
            if (toast) {
              Toast(params.type ? "提交成功" : "保存成功");
              this.$xy.hideLoad();
            }
            if (params.type) {
              this.paramsPages.pages = 1;
              setTimeout(() => {
                this.init();
              }, 2000);
            }
          }
        })
        .catch((res) => {
          this.$xy.hideLoad();
        });
    },
    async onRefresh() {
      this.value = ""; //搜索内容
      this.organizationalIds = []; //组织ID
      this.handleStatusIds = [];
      this.getStatus();
      await this.init();
    },
    submit(type, toast = false) {
      let batchRatingInfoList = this.checkedList.map((item) => ({
        backlogId: item.handleId,
        score: item.sumAreaInfo.ose.fieldValue,
        content: item.sumAreaInfo.eval.fieldValue,
      }));
      const params = {
        type,
        data: [],
      };
      batchRatingInfoList.forEach((item) => {
        params.data.push(item);
      });
      console.log(params, "params");
      console.log(this.checkedList, "checkedList");
      if (params.type) {
        params.data.length
          ? this.batchSubmit(params, toast)
          : Toast("请选择需要保存的数据");
      } else {
        console.log(this.batchRatingInfoList, "list");
        const saveList = [];
        this.batchRatingInfoList.forEach((item) => {
          if (item.scoreStatusCode === "3") {
            saveList.push(item);
          }
        });
        const saveData = saveList.map((item) => ({
          backlogId: item.handleId,
          score: item.sumAreaInfo.ose.fieldValue,
          content: item.sumAreaInfo.eval.fieldValue,
        }));
        params.data = saveData;
        console.log(params, "params");
        params.data.length
          ? this.batchSubmit(params, toast)
          : Toast("暂无需要保存的数据");
      }
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
.list-box {
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
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
  .checkBox {
    height: 100%;
    line-height: 100%;
  }
  .btn {
    width: 100%;
  }
}
.van-row--flex {
  align-items: center;
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
