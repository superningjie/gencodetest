<template>
  <div>
    <van-dialog
      :title="'个人发展计划评价'"
      v-model="isShow"
      theme="round-button"
      confirm-button-text="提交"
      show-cancel-button
      @confirm="handleConfirm"
      :before-close="handleClose"
      ref="planDialogs"
    >
      <div class="dialog-box">
        <div v-if="status == 1 && form">
          <div class="box_card" v-for="e in form.evaluateSelf">
            <p class="card_title">{{ e.taskName }}</p>
            <div class="card_content" v-for="ite in e.evaluateVOList">
              <p class="content_title">{{ ite.evaluateName }}</p>
              <div>
                <p>自评结果<span style="color: red">*</span>：</p>
                <van-radio-group
                  v-model="ite.selfResult"
                  style="display: flex; margin-top: 5px"
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">自评结果描述：</p>
                <van-field
                  autosize
                  rows="1"
                  type="textarea"
                  v-model="ite.selfResultDes"
                  placeholder="请输入"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="box_card" v-if="status == 2 && form">
          <p class="card_title">员工总体自评</p>
          <div class="card_content">
            <!-- <p class="content_title"></p> -->
            <div>
              <p>完成情况<span style="color: red">*</span>：</p>
              <van-radio-group
                v-model="resultRadio"
                style="display: flex; margin-top: 5px"
              >
                <van-radio
                  v-for="item in radioList"
                  :name="item.value"
                  icon-size="14"
                  style="margin-right: 10px"
                  >{{ item.label }}</van-radio
                >
              </van-radio-group>
              <p style="margin: 10px 0">自评结果描述：</p>
              <van-field
                autosize
                rows="1"
                type="textarea"
                v-model="resultValue"
                placeholder="请输入"
              />
            </div>
          </div>
        </div>
        <div v-if="status == 3 && form">
          <div
            class="box_card"
            v-for="(item, index) in form.evaluateOther"
            :style="index !== 0 ? 'margin-top: 15px' : ''"
          >
            <p class="card_title">{{ item.taskName }}</p>
            <div class="card_content" v-for="ite in item.evaluateVOList">
              <p class="content_title">{{ ite.evaluateName }}</p>
              <div>
                <p>自评结果：</p>
                <van-radio-group
                  v-model="ite.selfResult"
                  style="display: flex; margin-top: 5px"
                  disabled
                >
                  <van-radio
                    v-for="e in radioList"
                    :name="e.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ e.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">自评结果描述：</p>
                <van-field
                  disabled
                  v-model="ite.selfResultDes"
                  autosize
                  rows="1"
                  type="textarea"
                />
              </div>
              <div style="margin-top: 15px">
                <p>评价结果<span style="color: red">*</span>：</p>
                <van-radio-group
                  v-model="ite.evaluatorResult"
                  style="display: flex; margin-top: 5px"
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">评价结果描述：</p>
                <van-field
                  v-model="ite.evaluatorResultDes"
                  placeholder="请输入"
                  autosize
                  rows="1"
                  type="textarea"
                />
              </div>
            </div>
          </div>
        </div>

        <div v-if="status == 4 && form">
          <div class="box_card">
            <p class="card_title">
              {{ form.evaluateMentor ? form.evaluateMentor.taskNameSelf : "" }}
            </p>
            <div class="card_content">
              <!-- <p class="content_title"></p> -->
              <div class="content_item">
                <p>完成情况：</p>
                <van-radio-group
                  v-model="form.evaluateMentor.completeStatusSelf"
                  style="display: flex; margin-top: 5px"
                  disabled
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">自评结果描述：</p>
                <van-field
                  disabled
                  v-model="form.evaluateMentor.completeDesSelf"
                  autosize
                  rows="1"
                  type="textarea"
                />
              </div>
            </div>
          </div>
          <div class="box_card" style="margin-top: 15px">
            <p class="card_title">
              {{ form.evaluateMentor ? form.evaluateMentor.taskNameDept : "" }}
            </p>
            <div class="card_content">
              <!-- <p class="content_title"></p> -->
              <div>
                <p>完成情况<span style="color: red">*</span>：</p>
                <van-radio-group
                  v-model="resultRadio"
                  style="display: flex; margin-top: 5px"
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">结果评价：</p>
                <van-field
                  autosize
                  rows="1"
                  type="textarea"
                  v-model="resultValue"
                  placeholder="请输入"
                />
              </div>
            </div>
          </div>
        </div>
        <div v-if="status == 5 && form">
          <div class="box_card">
            <p class="card_title">
              {{ form.evaluateDept ? form.evaluateDept.taskNameSelf : "" }}
            </p>
            <div class="card_content">
              <!-- <p class="content_title"></p> -->
              <div>
                <p>完成情况：</p>
                <van-radio-group
                  v-model="form.evaluateDept.completeStatusSelf"
                  style="display: flex; margin-top: 5px"
                  disabled
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
                <p style="margin: 10px 0">完成情况描述：</p>
                <van-field
                  disabled
                  autosize
                  rows="1"
                  type="textarea"
                  v-model="form.evaluateDept.completeDesSelf"
                />
              </div>
            </div>
          </div>
          <div class="box_card" style="margin-top: 15px">
            <p class="card_title">{{ form.evaluateDept.taskNameDept }}</p>
            <div class="card_content">
              <!-- <p class="content_title"></p> -->
              <div
                v-for="item in form.evaluateDept.mentorAndEvaluates"
                class="teacher_item"
              >
                <div style="margin-bottom: 10px">
                  <p>
                    <span class="left">{{ item.position }}： </span>
                    <span class="right">{{ item.name }}</span>
                  </p>
                  <p>
                    <span class="left">完成情况：</span>
                    <span class="right">{{
                      filterRadio(item.completeStatusMentor)
                    }}</span>
                  </p>
                </div>
                <p style="padding-bottom: 5px">
                  <span class="left"> 结果评价：</span
                  ><span class="right">{{ item.result }}</span>
                </p>
              </div>
            </div>
          </div>
          <div class="box_card" style="margin-top: 15px">
            <p class="card_title">{{ form.evaluateDept.completeTaskName }}</p>
            <div class="card_content">
              <!-- <p class="content_title"></p> -->
              <div>
                <p style="margin: 10px 0">
                  完成情况 <span style="color: red">*</span> ：
                </p>
                <van-radio-group
                  v-model="resultRadio"
                  style="display: flex; margin-top: 5px"
                >
                  <van-radio
                    v-for="item in radioList"
                    :name="item.value"
                    icon-size="14"
                    style="margin-right: 10px"
                    >{{ item.label }}</van-radio
                  >
                </van-radio-group>
              </div>
              <div>
                <p style="margin: 10px 0">完成情况描述：</p>
                <van-field
                  autosize
                  rows="1"
                  type="textarea"
                  v-model="resultValue"
                  placeholder="请输入"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script>
import { getEvaluateForm, submitFormEvaluate } from "@/libs/api.js";
import { Toast } from "vant";
import { evaluatorEvaluation } from "../../libs/api";
export default {
  props: {
    status: {
      type: Number,
    },
    entryId: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      //   titleList: [
      //     "（员工期间自评）",
      //     "（员工总自评）",
      //     "（评价人确认）",
      //     "（导师评价）",
      //     "(带教部门负责人评价)",
      //   ],
      isShow: false,
      resultRadio: 0,
      resultValue: "",
      radioList: [
        { label: "超预期完成", value: "A" },
        { label: "按计划完成", value: "B" },
        { label: "未达预期", value: "C" },
      ],

      form: null,
      number: localStorage.getItem("pk_psndoc") || "",
      handleType: "",
      params: "",
    };
  },
  methods: {
    filterRadio(val) {
      if (!val) return;
      console.log(val);
      return this.radioList.find((item) => {
        return item.value == val;
      }).label;
    },
    getDetail(item) {
      this.handleType = item.handleType;
      getEvaluateForm({
        idpId: item.planId,
        handleType: item.handleType,
        number: this.number,
      }).then((res) => {
        console.log(res, "res");
        this.form = res.data.data;
      });
    },
    handleConfirm() {
      let params = {
        number: this.number,
        idpId: this.entryId,
      };
      let list = [];
      let isTrue = false;
      switch (this.status) {
        case 1:
          // 员工自评
          this.form.evaluateSelf.forEach((item) => {
            item.evaluateVOList.forEach((item2) => {
              list.push({
                taskId: item2.id,
                selfResult: item2.selfResult,
                selfResultDes: item2.selfResultDes,
              });
            });
          });

          list.forEach((item) => {
            if (!item.selfResult) {
              isTrue = true;
            }
          });
          if (isTrue) {
            Toast("请先填写自评");
            return;
          }
          params.selfData = list;
          break;

        case 2:
          if (!this.resultRadio) {
            Toast("请先选择完成情况");
            return;
          } else {
            params.finish = this.resultRadio;
            params.finishEvaluation = this.resultValue;
            // this.isShow = false;
          }
          break;
        case 3:
          this.form.evaluateOther.forEach((item) => {
            item.evaluateVOList.forEach((item2) => {
              list.push({
                taskId: item2.id,
                evaluatorResult: item2.evaluatorResult,
                evaluatorResultDes: item2.evaluatorResultDes,
              });
            });
          });
          list.forEach((item) => {
            if (!item.evaluatorResult) {
              isTrue = true;
            }
          });
          if (isTrue) {
            Toast("请先选择评价结果");
            return;
          }
          params.evaluatorData = list;
          break;
        case 4:
          if (!this.resultRadio) {
            Toast("请先选择完成情况");
            return;
          }
          params.finish = this.resultRadio;
          params.evaluate = this.resultValue;
          break;
        case 5:
          if (!this.resultRadio) {
            Toast("请先选择完成情况");
            return;
          } else {
            params.finish = this.resultRadio;
            params.finishEvaluation = this.resultValue;
            console.log(this.params, "this.params");
            // this.isShow = false;
          }
          break;
      }
      this.params = params;
    },
    handleClose(action, done) {
      if (action === "confirm") {
        submitFormEvaluate({ param: this.params, type: this.handleType }).then(
          (res) => {
            if (res.data.returnMsg == "success") {
              Toast("操作成功");
              this.form = null;
              this.resultValue = "";
              this.resultRadio = "";
              done();
              this.$emit("refreshList");
            } else {
              done(false);
            }
          }
        );
      } else {
        this.form = null;
        this.resultValue = "";
        this.resultRadio = "";
        done();
      }
    },
  },
};
</script>

<style lang="less" scoped>
/deep/ .van-dialog {
  top: 52%;
  height: 80vh;
  display: flex;
  flex-direction: column;
  .van-dialog__content {
    flex: 1;
    overflow-y: scroll;
    .dialog-box {
      height: 100%;
      padding: 10px;
      box-sizing: border-box;
      .box_card {
        border: 1px solid #dbdbdb;
        border-radius: 5px;
        font-size: 13px;
        margin-bottom: 14px;
        .card_title {
          background-color: rgb(242, 242, 242);
          min-height: 10px;
          padding: 5px;
          border-bottom: 1px solid #dbdbdb;
        }
        .card_content {
          padding: 10px;
          .content_item:not(:last-of-type) {
            margin-bottom: 15px;
          }
          .content_title {
            margin-bottom: 10px;
          }
          .van-cell {
            padding: 0;
            border-bottom: 1px solid #999999;
          }
          .teacher_item {
            margin-bottom: 10px;
            border-bottom: 1px solid #999999;
            p {
              display: flex;
            }
            div {
              display: flex;
              p {
                flex: 1;
              }
            }
            .left {
              // width: 80px;
              display: inline-block;
              // 文本不换行
              white-space: nowrap;
            }
            .right {
              color: #c8c9cc;
            }
          }
        }
      }
    }
  }
}
</style>
