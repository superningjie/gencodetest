<!--绩效评估-批量整单评总分-->
<template>
  <div class="prt-batch safe-bottom" v-if="show">
    <van-sticky>
      <div class="header-box">
        <div class="search">
          <van-search
            v-model="value"
            placeholder="请输入您想搜索的人名"
            background="#fff"
            @search="onSearch"
          >
          </van-search>
        </div>
        <div class="select">
          <van-dropdown-menu>
            <van-dropdown-item
              v-model="sortType"
              :options="option"
              @change="onSearch"
              title="排序方式"
            />
            <van-dropdown-item title="组织" ref="item">
              <van-cell
                center
                :title="val.text"
                v-for="val in organizationList"
                :key="val.id"
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
        </div>
      </div>
    </van-sticky>
    <xy-empty v-if="batchRatingInfoList.length <= 0"></xy-empty>
    <div class="batch-box">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-form ref="form" scroll-to-error validate-first>
          <van-list
            v-model="loading"
            :finished="finished"
            :finished-text="batchRatingInfoList.length == 0 ? '' : '没有更多了'"
            class="list-box"
            :immediate-check="false"
            :offset="60"
          >
            <div class="batch-list">
              <div
                class="info-box mb12"
                v-for="(item, index) in batchRatingInfoList"
                :key="index"
              >
                <div
                  class="pink-box"
                  @click="navigator('performancesRate', item.handleId)"
                >
                  <div class="flex middle">
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
                        {{ item.empInfo.organization }}
                      </div>
                    </div>
                    <van-icon name="arrow" color="#666" />
                  </div>
                </div>
                <div
                  class="tabcontent-top"
                  :ref="`${index + item.handleId}box`"
                >
                  <div class="sub-title fw-text flex middle">
                    <van-image width="16px" height="16px" :src="ICON" />
                    <div class="ovText">
                      <div
                        class="owt"
                        @click="seeMore($event, item.targetAreaInfo.indctrname)"
                      >
                        {{ item.targetAreaInfo.indctrname }}
                      </div>
                    </div>
                    <!-- 分值 -->
                    <div class="flex middle ml12">
                      <div
                        class="flex middle"
                        v-if="rateData.scoreCalcWay != '算术求和'"
                      >
                        <van-circle
                          :value="Number(item.targetAreaInfo.weight)"
                          :rate="item.targetAreaInfo.weight"
                          color="#ff0000"
                          size="16"
                          layer-color="#999"
                          :stroke-width="160"
                        />
                        <p class="ml8">{{ item.targetAreaInfo.weight }}%</p>
                      </div>
                      <div class="flex middle" v-else>
                        <div data-v-3550530e>
                          分值：{{ item.targetAreaInfo.indctrscore }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <van-cell-group :border="false">
                    <RateField
                      :readonly="status === '2'"
                      :decimalDigits="decimalDigits"
                      v-model.number="item.targetAreaInfo.evalscore.fieldValue"
                      :placeholder="
                        '评分上下限：' +
                        item.targetAreaInfo.evalscore.minevalscore +
                        '-' +
                        item.targetAreaInfo.evalscore.maxevalscore
                      "
                      :label="item.targetAreaInfo.evalscore.fieldName"
                      :rules="[
                        { required: true, message: '请输入评分' },
                        {
                          message: `请输入有效的${item.targetAreaInfo.evalscore.minevalscore}到${item.targetAreaInfo.evalscore.maxevalscore}之间的数字`,
                          validator: (value) => {
                            if (
                              value === '' ||
                              Number(value) <
                                item.targetAreaInfo.evalscore.minevalscore ||
                              Number(value) >
                                item.targetAreaInfo.evalscore.maxevalscore
                            ) {
                              return false;
                            } else {
                              return true;
                            }
                          },
                        },
                      ]"
                      :name="index + item.handleId"
                      :ref="index + item.handleId"
                      @blur="total(item)"
                    />
                    <TextareaField
                      :readonly="status === '2'"
                      v-if="item.targetAreaInfo.evaldesc"
                      v-model="item.targetAreaInfo.evaldesc.fieldValue"
                      placeholder="请输入"
                      :autosize="{ maxHeight: 100 }"
                      type="textarea"
                      :label="item.targetAreaInfo.evaldesc.fieldName"
                      rows="1"
                    />
                  </van-cell-group>
                </div>
                <div
                  class="rate-text-box"
                  v-if="item.otherRatingList.length > 0"
                >
                  <div
                    v-for="otherItem in item.otherRatingList"
                    :key="otherItem.id"
                    class="rate-text"
                    v-if="otherItem.score.isShow"
                  >
                    <span class="rate-text-label"
                      >{{ otherItem.nodeName }}：</span
                    >
                    {{ otherItem.score.value }}
                  </div>
                </div>
              </div>
            </div>
          </van-list>
        </van-form>
      </van-pull-refresh>
    </div>

    <div class="footer-fixed" v-if="status === '1'">
      <div class="btn-box">
        <van-button class="btn" type="danger" @click="submit(false, true)"
          >保存</van-button
        >
        <!-- <van-button class="btn" type="danger" @click="batchSubmit"
          >提交</van-button
        > -->
      </div>
    </div>
    <van-dialog
      v-model="confirmUserShow"
      title="确认提交以下人员的绩效评估"
      show-cancel-button
      @confirm="confirm"
      :showConfirmButton="result.length ? true : false"
      :confirmButtonText="`提交(${result.length}个)`"
    >
      <div class="confirm-user">
        <div class="user-box" v-for="item in checkedList">
          <van-checkbox-group v-model="result" ref="checkboxGroup">
            <van-checkbox
              :name="item.handleId"
              shape="square"
              checked-color="#ee0a24"
              icon-size="16px"
            >
              <div class="flex">
                <van-image
                  round
                  class="user-img"
                  :src="item.empInfo.headSculpture"
                />
                <div class="user-text">
                  {{ item.empInfo.name }}
                  <span class="ml8">
                    {{ item.empInfo.company }}
                  </span>
                </div>
              </div>
            </van-checkbox>
          </van-checkbox-group>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script>
import { Toast } from "vant";
import {
  getBatchOperationList,
  batchSavebtn,
  getCurrentInfoData,
  getBatchTotalStatus,
} from "@/libs/api.js";
import ICON from "@/assets/performances/icon4.png";
import shaixuan from "@/assets/performances/shaixuan.svg";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
export default {
  name: "PerformancesBatchRateTotal",
  components: {
    RateField,
    TextareaField,
  },
  data() {
    return {
      refreshing: false,
      loading: false,
      finished: false,
      ICON,
      shaixuan: shaixuan,
      decimalDigits: null, //小数位
      show: false,
      rateData: {},
      batchRatingInfoList: [],
      value: "",
      organizationId: [],
      jobInfo: {},
      workPosition: "0",
      showFilterPicker: false,
      crosseValuate: true,
      status: "",
      id: "",
      organizationList: [],
      sortType: "people",
      option: [
        { text: "按指标排序", value: "index" },
        { text: "按人员排序", value: "people" },
      ],
      confirmUserShow: false,
      checkedList: [],
      result: [],
    };
  },
  mounted() {
    this.getStatus();
    this.init();
  },
  methods: {
    getStatus() {
      const data = {
        handleId: this.$route.query.id,
      };
      getBatchTotalStatus(data).then((res) => {
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
    onRefresh() {
      this.batchRatingInfoList = [];
      this.finished = true;
      this.init();
    },
    seeMore(e, info) {
      e.stopPropagation();
      Toast(info);
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    clickSearch(item) {
      this.organizationId = item.code;
      this.showFilterPicker = false;
      this.onSearch();
    },
    onSearch() {
      this.init();
    },
    total(item) {
      this.$refs.form
        .validate(item.handleId)
        .then(() => {
          // const batchRatingInfoList = [];
          // batchRatingInfoList.push(item);
          let params = {
            batchRatingInfoList: this.batchRatingInfoList,
            type: false,
          };
          // console.log("失焦保存通过", params);
          this.save(params);
        })
        .catch((err) => {
          if (err && err.name) {
            this.$refs.form.scrollToField(err.name, false);
          }
        });
    },
    async init() {
      const handleId = this.$route.query.id;
      const {
        data: { data: initValue },
      } = await getCurrentInfoData({ id: handleId });
      this.crosseValuate = initValue.crosseValuate === "true";
      this.status = initValue.backlogState;
      this.id = initValue.id;
      this.refreshing = false;
      this.finished = false;
      this.$xy.showLoad();
      let params = {
        handleId: this.id,
        searchContent: this.value, //搜索内容
        organizationalIds: this.organizationalIds, //组织ID
        sortType: this.sortType, //排序
      };
      getBatchOperationList(params)
        .then((res) => {
          this.show = true;
          this.rateData = res.data.data;
          this.batchRatingInfoList = res.data.data.batchRatingInfoList;
          // this.organizationList = this.arrayTransition(
          //   res.data.data.organizationList
          // );
          this.finished = true;
          this.decimalDigits = Number(this.rateData.numAccuracy);
          this.$xy.hideLoad();
        })
        .catch((err) => {
          this.$xy.hideLoad();
          this.backPage();
        });
    },
    backPage() {
      const toast = Toast.loading({
        duration: 0, // 持续展示 toast
        forbidClick: true,
        message: "接口异常，三秒后返回",
      });
      let second = 3;
      const timer = setInterval(() => {
        second--;
        if (second) {
          toast.message = ` ${second} 秒后返回`;
        } else {
          clearInterval(timer);
          // 手动清除 Toast
          Toast.clear();
          this.$router.go(-1);
        }
      }, 1000);
    },
    navigator(path, id) {
      this.$router.push({
        path,
        query: {
          id: id ? id : this.$route.query.id,
          crosseValuate: true,
        },
      });
    },
    save(params) {
      batchSavebtn(params)
        .then((res) => {})
        .catch((res) => {});
    },
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
    async batchSubmit() {
      this.$refs.form
        .validate()
        .then(() => {
          this.checkedList = this.uniqueById(this.batchRatingInfoList);
          this.confirmUserShow = true;
        })
        .catch((err) => {
          const element = this.$refs[err[0].name + "box"];
          console.log(element[0].offsetTop);
          window.scrollTo({
            top: element[0].offsetTop - 60,
            // behavior: "smooth",
          });
        });
      return;
    },
    confirm() {
      const params = {
        batchRatingInfoList: [],
        backlogId: this.result,
        type: true,
      };
      this.$xy.showLoad();
      batchSavebtn(params)
        .then((res) => {
          Toast(res.data.message);
          this.$xy.hideLoad();
          this.onRefresh();
        })
        .catch((res) => {
          this.$xy.hideLoad();
        });
    },
    submit(type, toast = false) {
      let params = {
        batchRatingInfoList: this.batchRatingInfoList,
        type,
      };
      if (toast) {
        this.$xy.showLoad();
      }
      batchSavebtn(params)
        .then((res) => {
          if (res.data.statusCode == 200) {
            Toast(res.data.message);
          }
          this.$xy.hideLoad();
        })
        .catch((res) => {
          this.$xy.hideLoad();
        });
    },
    onCancel() {
      this.$refs.item.toggle(false);
    },
    onConfirm() {
      this.$refs.item.toggle(false);
      const organ = this.organizationList;
      const organList = [];
      organ.map((item) => {
        if (item.status) {
          organList.push(item.value);
        }
      });
      this.organizationalIds = organList;
      this.onRefresh();
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
  padding: 3px 12px 3px 0px;
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
.tabcontent-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 6px;
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
.sub-title {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  padding: 8px 12px;

  flex: 1;
}
.labelText {
  padding: 6px 12px;
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
.rate-text-box {
  padding: 0 12px 6px;
}
.rate-text {
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
  flex-direction: row;
  margin-right: 6px;
  margin-bottom: 3px;
  font-size: 12px;
  color: #212121;
  border-radius: 4px;
  background: #f2f2f2;
  padding: 3px 6px;
}
.rate-text-label {
  color: #666666;
}
.prt-batch {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
  .header-box {
    // display: flex;
    .search {
      flex: 5;
    }
    .select {
      flex: 1;
    }
  }
}
.batch-box {
  padding: 12px;
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
}

.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
}
.btn-box {
  display: flex;
  padding: 12px;
  padding-left: 0;
  .btn {
    width: 100%;
    margin-left: 12px;
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
