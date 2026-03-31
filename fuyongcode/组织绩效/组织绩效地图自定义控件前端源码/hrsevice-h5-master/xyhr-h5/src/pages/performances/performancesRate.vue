<template>
  <div
    class="performance-rate"
    :class="status === '1' ? 'safe-bottom-btn' : 'safe-bottom'"
  >
    <div v-if="rateData.areaInfoList">
      <!-- 绩效评估 -->
      <div class="approval-page-sticky">
        <div class="user-info-box" ref="userInfoBox">
          <div
            v-if="crosseValuate && status === '1'"
            class="see-box tar"
            @click="navigator('performancesBatchRate')"
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
              <div v-if="!crosseValuate" class="total-text">
                {{
                  rateData.areaInfoList[rateData.areaInfoList.length - 1]
                    .sumAreaInfo.ose.fieldValue
                }}
              </div>
            </div>
            <div class="flex mt10">
              <div class="exam">{{ rateData.activityName }}</div>
              <!-- <div class="exam">{{ rateData.period }}</div> -->
            </div>
          </div>
        </div>
      </div>

      <div class="tabs-box">
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
          <van-form
            ref="form"
            scroll-to-error
            validate-first
            @submit="submit(true)"
          >
            <van-tab
              v-for="(i, iIndex) in rateData.areaInfoList"
              :title="i.areaCustomName"
              :key="i.areaConfId"
              blur
            >
              <div class="tabHeader plain-text">
                <div class="flex justify">
                  {{ i.areaCustomName }}
                  <div
                    class="flex fun"
                    v-if="
                      i.areaRegNumber === 'epa_normindctrarea' &&
                      iIndex < 1 &&
                      !crosseValuate
                    "
                  >
                    <p @click="preview(i.areaInsId, i.areaConfId)">预览</p>
                  </div>
                </div>
                <div
                  class="tabcontent mt10"
                  v-for="(item, index) in i.targetAreaInfoList"
                  :key="item.indicatorId"
                  :ref="`name${item.indicatorId}box`"
                >
                  <div
                    class="tabcontent-top"
                    @click="
                      navigator('performancesIndexDetails', iIndex, index)
                    "
                  >
                    <div class="sub-title fw-text flex middle">
                      <van-image width="16px" height="16px" :src="ICON" />
                      <div class="ovText">
                        <div
                          class="owt"
                          @click="seeMore($event, item.indctrname)"
                        >
                          {{ item.indctrname }}
                        </div>
                      </div>
                      <!-- 分值 -->
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
                        <div class="flex middle" v-else>
                          <div data-v-3550530e>
                            分值：{{ item.indctrscore }}
                          </div>
                        </div>
                      </div>
                    </div>
                    <van-icon name="arrow" />
                  </div>
                  <div>
                    <van-cell-group :border="false">
                      <RateField
                        :ref="`name${item.indicatorId}`"
                        :label-width="60"
                        :decimalDigits="decimalDigits"
                        :readonly="status === '2' || !item.isEdit"
                        v-model.number="item.evalscore.fieldValue"
                        :name="`name${item.indicatorId}`"
                        :placeholder="
                          '评分上下限：' +
                          item.evalscore.minevalscore +
                          '-' +
                          item.evalscore.maxevalscore
                        "
                        :label="item.evalscore.fieldName"
                        :rules="[
                          {
                            required: item.isEdit,
                            message: item.isEdit ? '请输入评分' : '-',
                          },
                          {
                            message: `请输入有效的${item.evalscore.minevalscore}到${item.evalscore.maxevalscore}之间的数字`,
                            validator: (value) => {
                              if (
                                (item.isEdit && value === '') ||
                                Number(value) < item.evalscore.minevalscore ||
                                Number(value) > item.evalscore.maxevalscore
                              ) {
                                // item.evalscore.fieldValue = 0;
                                return false;
                              } else {
                                return true;
                              }
                            },
                          },
                        ]"
                        @blur="total(item)"
                      />
                      <TextareaField
                        @blur="total(`name${item.indicatorId}`)"
                        :label-width="60"
                        v-if="item.evaldesc && item.evaldesc.fieldDisplayItem"
                        :readonly="
                          status === '2' ||
                          !item.isEdit ||
                          !item.evaldesc.fieldModifyItem
                        "
                        v-model="item.evaldesc.fieldValue"
                        :placeholder="item.isEdit ? '请输入说明' : '-'"
                        :autosize="{ maxHeight: 100 }"
                        type="textarea"
                        :label="item.evaldesc.fieldName"
                        rows="1"
                        :rules="[
                          {
                            required: item.evaldesc.fieldMustInputItem,
                            message: '请输入说明',
                          },
                        ]"
                      />
                    </van-cell-group>
                    <!-- 评分 -->
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
                    <!-- 评分 -->
                  </div>
                </div>
                <!-- 加减分区 -->
                <div v-if="i.areaRegNumber === 'epa_plusminusarea'">
                  <div
                    class="tabcontent mt10"
                    v-for="(item, index) in i.plusMinusAreaInfoList"
                    :key="item.indicatorId"
                    :ref="`name${item.indicatorId}box`"
                  >
                    <div
                      class="tabcontent-top"
                      @click="
                        navigator('performancesIndexDetails', iIndex, index)
                      "
                    >
                      <div class="sub-title fw-text flex middle">
                        <van-image width="16px" height="16px" :src="ICON" />
                        <div class="ovText">
                          <div
                            class="owt"
                            @click="seeMore($event, item.indctrname)"
                          >
                            {{ item.indctrname }}
                          </div>
                        </div>
                      </div>
                      <van-icon name="arrow" />
                    </div>
                    <div>
                      <van-cell-group :border="false">
                        <RateField
                          :decimalDigits="decimalDigits"
                          :readonly="status === '2'"
                          :label-width="60"
                          v-model.number="item.evalscore.fieldValue"
                          :name="`name${item.indicatorId}`"
                          :ref="`name${item.indicatorId}`"
                          :placeholder="
                            '评分上下限：' +
                            item.evalscore.minevalscore +
                            '-' +
                            item.evalscore.maxevalscore
                          "
                          :label="item.evalscore.fieldName"
                          :rules="[
                            { required: isSubmit, message: '请输入评分' },
                            {
                              message: `请输入有效的${item.evalscore.minevalscore}到${item.evalscore.maxevalscore}之间的数字`,
                              validator: (value) => {
                                if (
                                  value === '' ||
                                  Number(value) < item.evalscore.minevalscore ||
                                  Number(value) > item.evalscore.maxevalscore
                                ) {
                                  // item.evalscore.fieldValue = 0;
                                  return false;
                                } else {
                                  return true;
                                }
                              },
                            },
                          ]"
                          @blur="total(item)"
                        />
                        <TextareaField
                          :label-width="60"
                          @blur="total(`name${item.indicatorId}`)"
                          :name="`name${item.evaldesc.fieldMustInputItem}`"
                          v-if="item.evaldesc && item.evaldesc.fieldDisplayItem"
                          :readonly="
                            status === '2' || !item.evaldesc.fieldModifyItem
                          "
                          v-model="item.evaldesc.fieldValue"
                          placeholder="请输入"
                          :autosize="{ maxHeight: 100 }"
                          type="textarea"
                          :label="item.evaldesc.fieldName"
                          rows="1"
                        />
                      </van-cell-group>
                      <!-- 评分 -->
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
                      <!-- 评分 -->
                    </div>
                  </div>
                </div>
                <!-- 加减分区end -->
                <!-- 综合评价区 -->
                <div
                  class="field mt10"
                  v-if="i.areaRegNumber === 'epa_customarea'"
                >
                  <CustomInfoArea
                    :customAreaInfo="i.customAreaInfo"
                    :status="status"
                    @initFileList="initFileList"
                    :areaConfId="i.areaConfId"
                    @saveAndSubmit="saveAndSubmit"
                  />
                </div>
                <!-- 综合评价区end -->
                <!-- 总评区 -->
                <div v-if="i.areaRegNumber === 'epa_sumarea'">
                  <div class="tabcontent mt10 field">
                    <van-cell-group :border="false">
                      <van-field
                        :label-width="60"
                        v-if="i.sumAreaInfo.ose.fieldDisplayItem"
                        v-model="i.sumAreaInfo.ose.fieldValue"
                        :label="i.sumAreaInfo.ose.fieldName"
                        placeholder
                        type="number"
                        error
                        :readonly="!i.sumAreaInfo.ose.fieldModifyItem"
                        :rules="[
                          {
                            required: !i.sumAreaInfo.ose.fieldMustInputItem,
                            message: '请输入',
                          },
                        ]"
                      />
                      <TextareaField
                        @blur="saveAndSubmit(false, false)"
                        :label-width="60"
                        v-if="
                          i.sumAreaInfo.eval &&
                          i.sumAreaInfo.eval.fieldDisplayItem
                        "
                        v-model="i.sumAreaInfo.eval.fieldValue"
                        :label="i.sumAreaInfo.eval.fieldName"
                        placeholder="请输入"
                        :autosize="{ maxHeight: 100 }"
                        type="textarea"
                        rows="1"
                        :rules="[
                          {
                            required: i.sumAreaInfo.eval.fieldMustInputItem,
                            message: '请输入',
                          },
                        ]"
                        :readonly="
                          status === '2' || !i.sumAreaInfo.eval.fieldModifyItem
                        "
                      />
                    </van-cell-group>
                  </div>
                  <!-- 总评区其他评论-->
                  <div v-if="i.otherRatingInfoList.length > 0">
                    <div
                      class="tabcontent mt10"
                      v-for="otherItem in i.otherRatingInfoList"
                      :key="otherItem.id"
                    >
                      <div class="pd12">
                        <div class="user-box">
                          <van-image
                            class="head-img"
                            round
                            :src="otherItem.empInfo.headSculpture"
                          />
                          <div class="con">
                            {{ otherItem.empInfo.name }}
                            {{ otherItem.empInfo.position }}
                          </div>
                          <div class="right">
                            <div
                              class="rate-text"
                              v-if="otherItem.score.isShow"
                            >
                              <div>
                                {{ otherItem.nodeName }}：{{
                                  otherItem.score.value
                                }}
                              </div>
                            </div>
                          </div>
                        </div>
                        <div class="comment" v-if="otherItem.desc.isShow">
                          {{ otherItem.desc.value }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 总评区其他评论end-->
                </div>
                <!-- 总评区end -->
              </div>
            </van-tab>
          </van-form>
        </van-tabs>
      </div>

      <div class="footer-fixed" v-if="status === '1'">
        <div class="btn-box">
          <van-button
            v-show="backRes.message == 'showTrue'"
            class="btn"
            @click="back"
            :disabled="submitDisabled"
            >退回</van-button
          >
          <van-button
            class="btn"
            @click="submit(false, true)"
            :disabled="submitDisabled"
            >保存</van-button
          >
          <van-button
            class="btn"
            type="danger"
            @click="submit(true, true)"
            :disabled="submitDisabled"
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
  getPerformancesRate,
  getMessage,
  totalPoints,
  saveRate,
  getPreview,
  backEvaluate,
} from "@/libs/api.js";
import { Toast, ImagePreview, Dialog } from "vant";
import ICON from "@/assets/performances/icon4.png";
import NOTICE from "@/assets/performances/notice.svg";
import WORD from "@/assets/performances/word.png";
import DOWN from "@/assets/performances/down.png";
import RateField from "@/pages/performances/components/RateField";
import RateCountField from "@/pages/performances/components/RateCountField";
import TextareaField from "@/pages/performances/components/TextareaField";
import LabelTextareaField from "@/pages/performances/components/LabelTextareaField";
import CountField from "@/pages/performances/components/CountField";
import Upload from "@/pages/performances/components/Upload";
import CustomInfoArea from "@/pages/performances/components/CustomInfoArea.vue";
import BackPopup from "@/pages/performances/components/BackPopup.vue";

export default {
  name: "PerformancesRate",
  components: {
    RateField,
    RateCountField,
    TextareaField,
    LabelTextareaField,
    CountField,
    Upload,
    CustomInfoArea,
    [ImagePreview.Component.name]: ImagePreview.Component,
    BackPopup,
  },
  data() {
    return {
      status: "1", //1是待办2 是已办
      crosseValuate: false,
      empInfo: {},
      rateData: {},
      decimalDigits: null, //小数位
      ICON,
      message: "",
      constValue: 9,
      rateFieldValue: 5,
      rateDialog: {
        show: false,
        value: "",
        keyboardShow: false,
      },
      NOTICE,
      WORD,
      DOWN,
      isFixed: false, //
      advantage: "",
      lack: "",
      offsetTop: 0,
      tabIndex: 0,
      fileList: [], // 初始化文件数组
      uploadFileList: [],
      id: "",
      fileData: [], // 需上传文件数组
      fieldMustInputItem: false, // 文件是否必填
      subtotalData: null,
      submitDisabled: false,
      backRes: {},
      backData: {
        show: false,
        text: "",
      },
      isSubmit: false,
      pendingAsyncOperations: 0,
      isSave: true,
    };
  },
  mounted() {
    console.log("999");
    this.initGetPerformancesRate();
  },
  computed: {},
  methods: {
    initFileList(list) {
      console.log(list, "list");
      // this.fileData = list;
    },
    preview(areaInsId, confId) {
      this.$xy.showLoad();
      const data = {
        handleId: this.id,
        areaInsId: areaInsId,
        confId: confId,
      };
      getPreview(data).then((res) => {
        this.$xy.hideLoad();
        const images = [res.data.data.previewUrl];
        window.open(res.data.data.previewUrl);
        // ImagePreview(images);
      });
    },
    seeMore(e, info) {
      e.stopPropagation();
      Toast(info);
    },
    initFile(data) {
      console.log(data, "data");
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
      const batchTaskId = this.$route.query.batchTaskId;
      const {
        data: { data: initValue },
      } = await getCurrentInfoData({ id: handleId, batchTaskId: batchTaskId });
      this.crosseValuate = initValue.crosseValuate === "true";
      this.status = initValue.backlogState;
      this.id = initValue.id;
      this.$xy.showLoad();
      let params = { handleId: initValue.id };
      const res = await (this.crosseValuate
        ? getMessage(params)
        : getPerformancesRate(params));
      this.$xy.hideLoad();

      console.log(res.data.data.areaInfoList, "res583");
      const isBackArr = res.data.data.areaInfoList;
      let isBack = false;
      for (let index = 0; index < isBackArr.length; index++) {
        const element = isBackArr[index];

        if (element.popupOrNot || element.sumAreaInfo?.ole?.fieldModifyItem) {
          isBack = true;
        }
      }
      if (isBack) {
        Dialog.alert({
          message:
            "移动端暂不支持填写指标信息和手动赋予绩效等级，请使用电脑端进行操作。",
          confirmButtonText: "返回待办列表",
        }).then(() => {
          // on close
          this.$router.go(-1);
          // this.$router.push({
          //   path: "myBacklog",
          //   query: {
          //     type: "待办",
          //   },
          // });
        });
        return;
      }
      this.rateData = res.data.data;
      this.empInfo = res.data.data.empInfo;
      this.decimalDigits = Number(this.rateData.numAccuracy);
      const backData = {
        backBol: false,
        waitId: this.id,
        message: "",
      };
      this.backEvaluate(backData);

      this.initOffsetTop();
    },
    scroll(e) {
      this.isFixed = e.isFixed;
    },
    navigator(path, arrayIndex, index) {
      if (this.pendingAsyncOperations) {
        this.$watch("pendingAsyncOperations", (newVal, oldVal) => {
          if (newVal != oldVal) {
            if (arrayIndex != undefined) {
              this.$router.push({
                path,
                query: {
                  id: this.$route.query.id,
                  arrayIndex,
                  index,
                },
              });
            } else {
              this.$router.push({
                path,
                query: {
                  id: this.$route.query.id,
                },
              });
            }
          }
        });
      } else {
        if (arrayIndex != undefined) {
          this.$router.push({
            path,
            query: {
              id: this.$route.query.id,
              arrayIndex,
              index,
            },
          });
        } else {
          this.$router.push({
            path,
            query: {
              id: this.$route.query.id,
            },
          });
        }
      }
    },
    afterRead(list, fieldMustInputItem) {
      // console.log(list, "list", fieldMustInputItem);
      this.fileData = list;
      this.fieldMustInputItem = fieldMustInputItem;
    },
    async totalPoints(item) {
      if (item.evalscore) {
        await this.$refs.form
          .validate(`name${item.indicatorId}`)
          .then(() => {})
          .catch((err) => {
            console.log(err, "err");
            if (err) {
              const element = this.$refs[err.name + "box"];
              window.scrollTo({
                top: element[0].offsetTop - this.offsetTop - 50,
                // behavior: "smooth",
              });
            }

            // item.evalscore.fieldValue = 0;
          });
      }
      const params = this.rateData;
      this.pendingAsyncOperations++;
      this.isSave = true;
      totalPoints(params).then((res) => {
        this.pendingAsyncOperations--;
        if (res.data.data.ose) {
          const sum = res.data.data.ose;
          this.rateData.areaInfoList[
            this.rateData.areaInfoList.length - 1
          ].sumAreaInfo.ose.fieldValue = sum;
        }
        this.subtotalData = res.data.data.subtotalData;
        this.isSave ? this.submit(false) : "";
      });
    },
    total(item) {
      this.totalPoints(item);
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
      // console.log(this.fileData, " this.fileData");
    },
    arrayTransition(obj) {
      const self = this;
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
    // // 为0的软校验
    // isZeros(arr) {
    //   // 取出常规指标区
    //   const areaList = [];
    //   for (let i = 0; i < arr.length; i++) {
    //     const element = arr[i];
    //     if (element.areaRegNumber == "epa_normindctrarea") {
    //       areaList.push(element);
    //     }
    //   }
    //   // 合并常规指标区的所有指标
    //   let indexList = [];
    //   for (let index = 0; index < areaList.length; index++) {
    //     const element = areaList[index];
    //     indexList.push(...element.targetAreaInfoList);
    //   }
    //   let importList = [];
    //   let exportList = [];

    //   for (let index = 0; index < indexList.length; index++) {
    //     const element = indexList[index];
    //     // 非导入指标
    //     if (element.isEdit) {
    //       importList.push(element);
    //     } else {
    //       exportList.push(element);
    //     }
    //     if (index + 1 == indexList.length) {
    //       console.log(importList, "importList", exportList);
    //       for (let i = 0; i < exportList.length; i++) {
    //         if (exportList[i].evalscore.fieldValue === "") {
    //           exportList[i].evalscore.fieldValue = 0;
    //         }
    //         if (i + 1 == exportList.length) {
    //           for (let x = 0; x < importList.length; x++) {
    //             if (Number(importList[x].evalscore.fieldValue) == 0) {
    //               this.zerosDialog(importList[x]);
    //               return;
    //             }
    //             if (
    //               x + 1 == importList.length &&
    //               Number(importList[x].evalscore.fieldValue) != 0
    //             ) {
    //               this.saveAndSubmit(true, true);
    //             }
    //           }
    //         }
    //       }
    //     }
    //   }
    // },
    isZeros(arr) {
      // 取出常规指标区
      const areaList = arr.filter(
        (element) => element.areaRegNumber === "epa_normindctrarea"
      );
      // 合并常规指标区的所有指标
      const indexList = areaList.flatMap(
        (element) => element.targetAreaInfoList
      );
      // 分离导入指标和非导入指标
      const importList = indexList.filter((element) => element.isEdit);
      // const exportList = indexList.filter((element) => !element.isEdit);
      // 处理导出指标中的空值
      // exportList.forEach((element) => {
      //   if (element.evalscore.fieldValue === "") {
      //     element.evalscore.fieldValue = 0;
      //   }
      // });
      // 检查导入指标是否为零，并执行相应操作
      for (let i = 0; i < importList.length; i++) {
        const element = importList[i];
        if (Number(element.evalscore.fieldValue) === 0) {
          this.zerosDialog(element);
          return;
        }
        if (
          i + 1 === importList.length &&
          Number(element.evalscore.fieldValue) !== 0
        ) {
          this.saveAndSubmit(true, true);
        }
      }
    },
    zerosDialog(val) {
      console.log(val, "val");
      const message = `指标：「${val.indctrname}」的评分为0，请确认。`;
      Dialog.confirm({
        message: message,
        cancelButtonText: "返回修改",
        confirmButtonText: "确认提交",
      })
        .then(() => {
          // on confirm
          this.saveAndSubmit(true, true);
        })
        .catch(() => {
          // on cancel
          const element = this.$refs["name" + val.indicatorId + "box"];
          window.scrollTo({
            top: element[0].offsetTop - this.offsetTop - 50,
            // behavior: "smooth",
          });
        });
    },
    saveAndSubmit(type, toast) {
      const params = {
        backlogId: this.rateData.handleId,
        type,
        fileData: this.fileData,
        subtotalData: this.subtotalData,
        data: {
          activityName: this.rateData.activityName,
          areaInfolist: this.rateData.areaInfoList,
          empInfo: this.rateData.empInfo,
          handleId: this.rateData.handleId,
          period: this.rateData.period,
          scoreCalcWay: this.rateData.scoreCalcWay,
        },
      };
      if (!type) {
        console.log("保存");
        if (toast) {
          this.$xy.showLoad();
        }
        if (!this.pendingAsyncOperations) {
          this.pendingAsyncOperations++;
          saveRate(params)
            .then((res) => {
              this.pendingAsyncOperations--;
              if (toast) {
                Toast(res.data.data.result);
              }
              this.$xy.hideLoad();
            })
            .catch((res) => {
              this.$xy.hideLoad();
            });
        }
      } else {
        console.log("提交,不保存");
        this.isSave = false;
        if (!this.pendingAsyncOperations) {
          this.$refs.form
            .validate()
            .then(() => {
              this.$xy.showLoad();
              saveRate(params)
                .then((res) => {
                  this.submitDisabled = true;
                  if (res.data.statusCode == 200 && toast) {
                    Toast(res.data.data.result);
                    this.$xy.hideLoad();
                    if (type) {
                      setTimeout(() => {
                        this.backPage();
                      }, 2000);
                    }
                  }
                })
                .catch((err) => {
                  this.$xy.hideLoad();
                });
            })
            .catch((err) => {
              if (err && err[0].name) {
                const element = this.$refs[err[0].name + "box"];
                window.scrollTo({
                  top: element[0].offsetTop - this.offsetTop - 50,
                  // behavior: "smooth",
                });
              }
            });
        } else {
          this.$watch("pendingAsyncOperations", (newVal, oldVal) => {
            if (newVal !== oldVal) {
              this.$refs.form
                .validate()
                .then(() => {
                  this.$xy.showLoad();
                  saveRate(params)
                    .then((res) => {
                      this.submitDisabled = true;
                      if (res.data.statusCode == 200 && toast) {
                        Toast(res.data.data.result);
                        this.$xy.hideLoad();
                        if (type) {
                          setTimeout(() => {
                            this.backPage();
                          }, 2000);
                        }
                      }
                    })
                    .catch((err) => {
                      this.$xy.hideLoad();
                    });
                })
                .catch((err) => {
                  if (err && err[0].name) {
                    const element = this.$refs[err[0].name + "box"];
                    window.scrollTo({
                      top: element[0].offsetTop - this.offsetTop - 50,
                      // behavior: "smooth",
                    });
                  }
                });
            }
          });
        }
      }
    },
    async submit(type, toast) {
      this.isSubmit = type;
      this.fileTransition();
      if (this.fieldMustInputItem && this.fileData.length >= 0) {
        return Toast("附件为必填项");
      } else {
        if (type) {
          this.$refs.form
            .validate()
            .then(() => {
              // this.$xy.showLoad();
              this.isZeros(this.rateData.areaInfoList);
            })
            .catch((err) => {
              console.log(err, "err");
              if (err && err[0].name) {
                const element = this.$refs[err[0].name + "box"];
                console.log(element, "element");
                window.scrollTo({
                  top: element[0].offsetTop - this.offsetTop - 50,
                  // behavior: "smooth",
                });
              }
            });
        } else {
          this.saveAndSubmit(type, toast);
        }
      }
    },
    back() {
      this.backData.show = true;
    },
    cancel() {
      this.backData.show = false;
      this.backData.text = "";
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
.over-text {
  max-width: 100px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.safe-bottom-btn {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
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
.noticeWH {
  width: 14px;
  height: 14px;
}
.labelText {
  padding: 6px 0px;
}
.performance-rate {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.user-info-box {
  min-height: 92px;
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
    .total-text {
      font-size: 5.333vw;
      color: #d80c1e;
      font-weight: bold;
    }
    .total-text::after {
      content: "";
      position: relative;
      bottom: 0.533vw;
      left: 0;
      display: block;
      width: 100%;
      height: 1.6vw;
      background: linear-gradient(to right, #ee0a0a14, #fff);
    }
  }
}
.tabs-box {
  /deep/ .van-tabs__nav {
    // background-color: #f2efef;
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
      margin-right: 12px;
    }
    .btnM {
      margin: 0 5px 0 20px;
    }
  }
  /deep/ .tabcontent {
    overflow: hidden;
    background: white;
    border-radius: 8px;
    .tabcontent-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(to right, #ee0a0a14, #fff);
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
.comment {
  padding-top: 6px;
  font-size: 12px;
  color: #212121;
  line-height: 18px;
}

.rate-dialog {
  .tip {
    font-size: 14px;
    color: #666666;
    letter-spacing: 0;
    text-align: center;
    line-height: 20px;
    font-weight: 400;
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
