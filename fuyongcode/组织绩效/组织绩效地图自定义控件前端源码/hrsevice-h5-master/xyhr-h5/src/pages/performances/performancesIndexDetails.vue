<template>
  <!-- 绩效评估指标详情 -->
  <div class="pi-details safe-bottom" v-if="rateData.areaInfoList">
    <div class="pi-details-top">
      <div class="user-box">
        <van-image class="head-img" round :src="empInfo.headSculpture" />
        <div class="con">{{ empInfo.name }} {{ empInfo.position }}</div>
        <div class="right">
          <div class="num" v-if="!crosseValuate">
            {{ sumArea }}
            <i class="line"></i>
          </div>
        </div>
      </div>
    </div>
    <!-- 常规指标区和加减分区-->

    <div class="pi-details-con" v-if="list[index] && isShowArea">
      <div class="tabcontent mt10">
        <!-- 常规指标区 -->
        <div v-if="curData.areaRegNumber == 'epa_normindctrarea'">
          <div class="tabcontent-top">
            <div class="subTitle fw-text flex middle">
              <van-image width="16px" height="16px" :src="ICON" />
              <div class="ovText">
                <div
                  class="owt"
                  @click="seeMore($event, list[index].indctrname)"
                >
                  {{ list[index].indctrname }}
                </div>
              </div>
              <div class="flex middle ml12">
                <div
                  class="flex middle"
                  v-if="rateData.scoreCalcWay != '算术求和'"
                >
                  <van-circle
                    :value="Number(list[index].weight)"
                    :rate="list[index].weight"
                    color="#ff0000"
                    size="16"
                    layer-color="#999"
                    :stroke-width="160"
                  />
                  <p class="ml8">{{ list[index].weight }}%</p>
                </div>
                <div class="flex middle" v-else>
                  <div data-v-3550530e>分值：{{ list[index].indctrscore }}</div>
                </div>
              </div>
            </div>
          </div>
          <div>
            <van-form ref="form">
              <van-cell-group :border="false">
                <RateField
                  :readonly="status === '2' || !list[index].isEdit"
                  type="number"
                  :decimalDigits="decimalDigits"
                  v-model.number="list[index].evalscore.fieldValue"
                  :placeholder="
                    list[index].isEdit
                      ? '评分上下限：' +
                        list[index].evalscore.minevalscore +
                        '-' +
                        list[index].evalscore.maxevalscore
                      : '-'
                  "
                  :label="list[index].evalscore.fieldName"
                  :name="`name${list[index].indicatorId}`"
                  :rules="[
                    {
                      required: list[index].isEdit,
                      message: list[index].isEdit ? '请输入评分' : '-',
                    },
                    {
                      message: `请输入有效的${list[index].evalscore.minevalscore}到${list[index].evalscore.maxevalscore}之间的数字`,
                      validator: (value) => {
                        if (
                          value === '' ||
                          Number(value) < list[index].evalscore.minevalscore ||
                          Number(value) > list[index].evalscore.maxevalscore
                        ) {
                          return false;
                        } else {
                          return true;
                        }
                      },
                    },
                  ]"
                  @blur="total(`name${list[index].indicatorId}`)"
                />
                <TextareaField
                  :readonly="status === '2' || !list[index].isEdit"
                  v-if="list[index].evaldesc"
                  v-model="list[index].evaldesc.fieldValue"
                  :placeholder="list[index].isEdit ? '请输入' : '-'"
                  :label="list[index].evaldesc.fieldName"
                  :autosize="{ maxHeight: 100 }"
                  type="textarea"
                  rows="1"
                  :rules="[
                    {
                      required: list[index].evaldesc.fieldMustInputItem,
                      message: '请输入说明',
                    },
                  ]"
                />
              </van-cell-group>
            </van-form>
          </div>
        </div>
        <!-- 常规指标区end -->
        <!-- 加减分区 -->
        <div v-if="curData.areaRegNumber == 'epa_plusminusarea'">
          <div class="tabcontent-top">
            <div class="subTitle fw-text flex middle">
              <van-image width="16px" height="16px" :src="ICON" />
              <div class="ovText">
                <div
                  class="owt"
                  @click="seeMore($event, list[index].indctrname)"
                >
                  {{ list[index].indctrname }}
                </div>
              </div>
            </div>
          </div>
          <div>
            <van-form ref="form">
              <van-cell-group :border="false">
                <RateField
                  :readonly="status === '2'"
                  type="number"
                  :decimalDigits="decimalDigits"
                  v-model.number="list[index].evalscore.fieldValue"
                  :placeholder="
                    '评分上下限：' +
                    list[index].evalscore.minevalscore +
                    '-' +
                    list[index].evalscore.maxevalscore
                  "
                  :label="list[index].evalscore.fieldName"
                  :name="`name${list[index].indicatorId}`"
                  :rules="[
                    {
                      required: list[index].isEdit ? list[index].isEdit : true,
                      message: '请输入评分',
                    },
                    {
                      message: `请输入有效的${list[index].evalscore.minevalscore}到${list[index].evalscore.maxevalscore}之间的数字`,
                      validator: (value) => {
                        if (
                          value === '' ||
                          Number(value) < list[index].evalscore.minevalscore ||
                          Number(value) > list[index].evalscore.maxevalscore
                        ) {
                          return false;
                        } else {
                          return true;
                        }
                      },
                    },
                  ]"
                  @blur="total(`name${list[index].indicatorId}`)"
                />
                <TextareaField
                  :readonly="status === '2'"
                  v-if="list[index].evaldesc"
                  v-model="list[index].evaldesc.fieldValue"
                  placeholder="请输入"
                  :label="list[index].evaldesc.fieldName"
                  :autosize="{ maxHeight: 100 }"
                  type="textarea"
                  rows="1"
                  :rules="[
                    {
                      required: list[index].evaldesc.fieldMustInputItem,
                      message: '请输入说明',
                    },
                  ]"
                />
              </van-cell-group>
            </van-form>
          </div>
        </div>
        <!-- 加减分区end -->

        <div class="subcontent-box">
          <div class="subcontent plain-text">
            <div class="content" v-if="list[index].evaltype">
              {{ list[index].evaltype }}
            </div>
            <div
              v-for="msg in list[index].optionalFieldInfo"
              :key="msg.fieldId"
            >
              <p>
                <i>{{ msg.fieldName }}：</i>
                {{ msg.fieldValue }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <div
        v-if="list[index].otherRatingList.length > 0"
        class="tabcontent mt10"
        v-for="item in list[index].otherRatingList"
        :key="item.id"
      >
        <div class="pd12">
          <div class="user-box">
            <van-image
              class="head-img"
              round
              :src="item.empInfo.headSculpture"
            />
            <div class="con">
              {{ item.empInfo.name }} {{ item.empInfo.position }}
            </div>
            <div class="right" v-if="item.score.isShow">
              <div class="rate-text">
                <span class="rate-text-label">{{ item.nodeName }}：</span>
                {{ item.score.value }}
              </div>
            </div>
          </div>
          <div class="comment" v-if="item.desc.isShow">
            {{ item.desc.value }}
          </div>
        </div>
      </div>
    </div>
    <!-- 常规指标区和加减分区end -->
    <!-- 综合评价区 -->
    <div class="customInfoArea" v-else>
      <CustomInfoArea
        :customAreaInfo="customAreaInfo"
        :status="status"
        @initFileList="initFileList"
        :areaConfId="areaConfId"
        @saveAndSubmit="submit"
      />
    </div>

    <!-- 综合评价区end -->
    <!-- 底部切换区域 -->
    <div class="footer-fixed">
      <div class="btn-page">
        <van-row type="flex" ustify="space-between" gutter="12">
          <van-col span="5" class="prev">
            <span @click="prev"> <van-icon name="arrow-left" />上一条 </span>
          </van-col>
          <van-col span="14" class="title"
            >{{ curData.areaCustomName }}
            <span v-if="isShowArea">
              （{{ index + 1 }}/{{ list.length }}）
            </span></van-col
          >
          <van-col span="5" class="next">
            <span @click="next">
              下一条
              <van-icon name="arrow" />
            </span>
          </van-col>
        </van-row>
      </div>
      <div class="btn-box" v-if="status != '2'">
        <van-row type="flex" ustify="space-between" gutter="12">
          <van-col span="12">
            <van-button class="btn" @click="navigator">返回</van-button>
          </van-col>
          <van-col span="12">
            <van-button class="btn" type="danger" @click="submit(false, true)"
              >保存</van-button
            >
          </van-col>
        </van-row>
      </div>
    </div>
    <!-- 底部切换区域end -->
  </div>
</template>
<script>
import {
  getCurrentInfoData,
  getPerformancesRate,
  getMessage,
  totalPoints,
  saveRate,
} from "@/libs/api.js";
import CustomInfoArea from "@/pages/performances/components/CustomInfoArea.vue";

import ICON from "@/assets/performances/icon4.png";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
import { Toast } from "vant";
export default {
  name: "PerformancesIndexDetails",
  components: {
    RateField,
    TextareaField,
    CustomInfoArea,
  },
  data() {
    return {
      id: this.$route.query.id,
      arrayIndex: 0,
      index: 0,
      decimalDigits: null, //小数位
      empInfo: {},
      rateData: {},
      curData: {}, //当前区域
      list: [], //当前区域集合
      ICON,
      status: "",
      isShowArea: true, // 显示区域是否常规指标区和加减分区
      customAreaInfo: {},
      areaConfId: "",
      subtotalData: null,
      fileData: [], // 需上传文件数组
      pendingAsyncOperations: 0,
      isFish: true,
    };
  },
  computed: {
    sumArea() {
      let index = this.rateData.areaInfoList.length - 1;
      return this.rateData.areaInfoList[index].sumAreaInfo.ose.fieldValue;
    },
  },
  mounted() {
    this.arrayIndex = Number(this.$route.query.arrayIndex);
    this.index = Number(this.$route.query.index);
    this.initGetPerformancesRate();
  },
  methods: {
    initFileList(list) {
      // this.fileData = list;
    },
    seeMore(e, info) {
      e.stopPropagation();
      Toast(info);
    },
    async initGetPerformancesRate() {
      const handleId = this.$route.query.id;
      const {
        data: { data: initValue },
      } = await getCurrentInfoData({ id: handleId });
      this.crosseValuate = initValue.crosseValuate === "true";
      this.status = initValue.backlogState;
      this.$xy.showLoad();
      let params = { handleId: initValue.id };
      const res = await (this.crosseValuate
        ? getMessage(params)
        : getPerformancesRate(params));
      this.rateData = res.data.data;
      this.curData = this.rateData.areaInfoList[this.arrayIndex];
      console.log(this.curData.areaRegNumber);

      if (this.curData.areaRegNumber == "epa_normindctrarea") {
        this.list = this.curData.targetAreaInfoList;
      } else if (this.curData.areaRegNumber == "epa_plusminusarea") {
        this.list = this.curData.plusMinusAreaInfoList;
      }

      this.empInfo = res.data.data.empInfo;
      this.decimalDigits = Number(this.rateData.numAccuracy);
      this.$xy.hideLoad();
    },
    prev() {
      const areaInfo =
        this.arrayIndex > 0
          ? this.rateData.areaInfoList[this.arrayIndex - 1]
          : this.rateData.areaInfoList[this.arrayIndex];
      if (areaInfo.areaRegNumber === "epa_customarea") {
        this.index = 0;
      }
      if (this.index > 0) {
        this.index--;
        return;
      }

      if (this.arrayIndex <= 0) {
        return Toast("已经是第一条了");
      }
      if (areaInfo.areaRegNumber === "epa_normindctrarea") {
        this.arrayIndex--;
        this.curData = areaInfo;
        this.list = this.curData.targetAreaInfoList;
        this.index = this.list.length - 1;
        this.isShowArea = true;
      } else if (areaInfo.areaRegNumber === "epa_plusminusarea") {
        console.log("加减");
        this.arrayIndex--;
        this.curData = areaInfo;
        this.list = this.curData.plusMinusAreaInfoList;
        this.index = this.list.length - 1;
        this.isShowArea = true;
      } else if (areaInfo.areaRegNumber === "epa_customarea") {
        console.log("综合");
        this.arrayIndex--;
        this.curData = areaInfo;
        this.areaConfId = areaInfo.areaConfId;
        this.customAreaInfo = this.curData.customAreaInfo;
        this.isShowArea = false;
      } else {
        return Toast("已经是第一条了");
      }
    },
    next() {
      if (this.list.length > this.index + 1) {
        this.index++;
        return;
      }
      const nextArea = this.rateData.areaInfoList[this.arrayIndex + 1];
      if (!nextArea) {
        return Toast("最后一条了");
      }

      switch (nextArea.areaRegNumber) {
        case "epa_normindctrarea":
          this.arrayIndex++;
          this.curData = this.rateData.areaInfoList[this.arrayIndex];
          this.list = this.curData.targetAreaInfoList;
          this.index = 0;
          break;
        case "epa_plusminusarea":
          this.arrayIndex++;
          this.curData = this.rateData.areaInfoList[this.arrayIndex];
          this.list = this.curData.plusMinusAreaInfoList;
          this.index = 0;
          break;
        case "epa_customarea":
          this.arrayIndex++;
          this.curData = this.rateData.areaInfoList[this.arrayIndex];
          this.areaConfId = this.curData.areaConfId;
          this.customAreaInfo = this.curData.customAreaInfo;
          this.isShowArea = false;
          break;
        default:
          return Toast("最后一条了");
      }
    },
    total(name) {
      this.$refs.form
        .validate(name)
        .then(() => {
          if (!this.crosseValuate) {
            // this.$xy.showLoad();
            const params = this.rateData;
            this.pendingAsyncOperations++;
            totalPoints(params).then((res) => {
              this.pendingAsyncOperations--;
              const sum = res.data.data.ose;
              this.rateData.areaInfoList[
                this.rateData.areaInfoList.length - 1
              ].sumAreaInfo.ose.fieldValue = sum;
              this.subtotalData = res.data.data.subtotalData;
              this.submit(false);
              // this.$xy.hideLoad();
            });
          } else {
            this.submit(false);
          }
        })
        .catch(() => {
          console.log("不通过");
        });
    },
    navigator() {
      if (this.pendingAsyncOperations) {
        this.$watch("pendingAsyncOperations", (newVal, oldVal) => {
          if (newVal != oldVal) {
            this.$watch("isFish", (newV, oldV) => {
              console.log(newV, oldV, "newVal, oldVal", this.isFish);
              const randomNumber = Math.floor(Math.random() * 1000000); // 生成一个0到999999之间的随机数
              this.$router.push({
                path: "performancesRate",
                query: {
                  id: this.$route.query.id,
                  random: randomNumber, // 添加随机数作为查询参数
                },
              });
            });
          }
        });
      } else {
        const randomNumber = Math.floor(Math.random() * 1000000); // 生成一个0到999999之间的随机数
        this.$router.push({
          path: "performancesRate",
          query: {
            id: this.$route.query.id,
            random: randomNumber, // 添加随机数作为查询参数
          },
        });
      }
    },
    // 将自定义信息区的信息汇总
    fileTransition() {
      const array = this.rateData.areaInfoList;
      const fileList = [];
      for (let i = 0; i < array.length; i++) {
        const element = array[i];
        if (element.areaRegNumber == "epa_customarea") {
          element.customAreaInfo.optionalFieldInfo?.map((value) =>
            fileList.push(value)
          );
        }
      }
      this.fileListData(fileList);
    },
    // 将自定义信息区汇总的信息过滤，只需要文件列表
    fileListData(array) {
      const fileList = [];
      for (let i = 0; i < array.length; i++) {
        const element = array[i];
        if (
          element.fieldId == "customfield4" ||
          element.fieldId == "customfield9" ||
          element.fieldId == "customfield10"
        ) {
          element.fieldValue?.map((value) => fileList.push(value));
        }
      }
      this.fileData = this.arrayTransition(fileList);
      console.log(this.fileData, " this.fileData");
    },
    // 给每一个文件赋值上当前区域的id
    arrayTransition(obj) {
      console.log(obj, "obj");
      const fileList = obj;
      return fileList.map((item) => {
        return {
          areaConfId: item.areaConfId,
          fieldName: item.fieldName,
          fileName: item.name,
          fileSize: item.size,
          fileUrl: item.url,
          fileDate: item.createtime,
        };
      });
    },
    submit(type, toast = false) {
      if (!this.pendingAsyncOperations) {
        this.fileTransition();
        const totalArea =
          this.rateData.areaInfoList[this.rateData.areaInfoList.length - 1];

        const params = {
          backlogId: this.rateData.handleId,
          type,
          fileData: this.fileData,
          subtotalData: this.subtotalData,
          data: {
            activityName: this.rateData.activityName,
            areaInfolist: [this.curData, totalArea],
            empInfo: this.rateData.empInfo,
            handleId: this.rateData.handleId,
            period: this.rateData.period,
            scoreCalcWay: this.rateData.scoreCalcWay,
          },
        };
        if (toast) {
          console.log(toast, "toast");
          this.$xy.showLoad();
        }
        this.isFish = false;
        saveRate(params)
          .then((res) => {
            console.log(222, "to222ast");
            this.isFish = true;
            if (res.data.statusCode == 200 && toast) {
              Toast(res.data.message);
            }
            this.$xy.hideLoad();
          })
          .catch((res) => {
            this.isFish = true;
            this.$xy.hideLoad();
          });
      }
    },
  },
};
</script>

<style lang="less" scoped>
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 130px);
}
.customInfoArea {
  padding: 12px;
  margin-top: 10px;
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
.pi-details {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.pi-details-top {
  background: #fff;
  padding: 10px 12px;
}
.pi-details-con {
  padding: 12px;
  /deep/ .tabcontent {
    background: white;
    border-radius: 8px;
    .tabcontent-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .subTitle {
      border-top-left-radius: 8px;
      border-top-right-radius: 8px;
      padding: 8px 12px;
      background: linear-gradient(to right, #ee0a0a14, #fff);
      flex: 1;
    }
    .labelText {
      padding: 6px 12px;
    }
    .subcontent-box {
      padding: 12px;
    }
    .subcontent {
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;
      .content {
        background: #ededed;
        border-radius: 4px;
        padding: 2px 4px;
        display: inline-block;
        margin-bottom: 6px;
      }
      i {
        color: #666;
      }
    }
    .comment {
      padding-top: 6px;
      font-size: 12px;
      color: #212121;
      line-height: 18px;
    }
  }
}
.user-box {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  .head-img {
    width: 24px;
    height: 24px;
  }
  .con {
    font-size: 14px;
    color: #212121;
    letter-spacing: 0;
    line-height: 20px;
    font-weight: bold;
    flex: 1;
    padding: 0 12px;
  }
  .num {
    font-size: 20px;
    color: #d80c1e;
    letter-spacing: 0;
    font-weight: bold;
    position: relative;
    .line {
      position: absolute;
      left: 0;
      right: 0;
      bottom: -1px;
      background: linear-gradient(to right, #ff8993, #fff);
      height: 3px;
      border-radius: 50%;
    }
  }
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
  .rate-text-label {
    color: #666;
  }
}
.rt01 {
  color: #1a9e98;
  background: rgba(27, 163, 157, 0.07);
  border-radius: 4px;
  padding: 6px;
}
.rt02 {
  background: rgba(21, 125, 231, 0.06);
  border-radius: 4px;
  color: #157de7;
  padding: 6px;
}
.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
}
.btn-page {
  line-height: 24px;
  padding: 12px;
  position: relative;
  &::before {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    top: 0;
    left: 0;
    border-bottom: 1px solid #e5e5e5;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
  }
  &::after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    bottom: 0;
    left: 0;
    border-bottom: 1px solid #e5e5e5;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
  }

  .prev {
    font-size: 14px;
    color: #d80c1e;
  }
  .title {
    color: #212121;
    text-align: center;
  }
  .next {
    text-align: right;
    font-size: 14px;
    color: #d80c1e;
  }
}

.btn-box {
  padding: 12px;
  .btn {
    width: 100%;
  }
}
</style>
