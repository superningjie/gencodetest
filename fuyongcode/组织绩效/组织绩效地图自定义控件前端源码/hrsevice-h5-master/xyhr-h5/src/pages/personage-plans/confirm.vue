<template>
  <div class="confirm">
    <!-- <div class="header">
      <van-nav-bar>
        <template #title>
          <span class="navbar-title">期间任务评价</span>
        </template>
        <template #left>
          <div class="navbar-left" @click="handleBack">
            <van-image
              fit="cover"
              width="10px"
              height="16px"
              :src="require('@/assets/newready/left.png')"
            ></van-image>
          </div>
        </template>
      </van-nav-bar>
    </div> -->
    <div class="content">
      <div class="top">
        <div class="top-title">
          <span class="label">{{ selfData.idpName }}</span>
          <span class="status">进行中</span>
        </div>
        <div class="disposes">
          <div class="disposer">当前处理人：{{ handlePerson }}</div>
          <div class="show" @click="showAll = true" v-show="!showAll">
            <van-image
              fit="cover"
              width="11px"
              height="11px"
              :src="require('@/assets/personage-plans/all.png')"
            ></van-image>
            <span>展示全部</span>
          </div>
        </div>
      </div>
      <div class="infos">
        <van-collapse v-model="activeCollapse">
          <!-- 完成情况 -->
          <van-collapse-item name="Situation" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/task.png')"
                ></van-image>
                <div class="title">任务信息</div>
              </div>
            </template>
            <div class="situation-content-info">
              <div class="info-item">
                <span class="label">提升能力</span>
                <span class="value">{{ selfData.ability }}</span>
              </div>
              <div class="info-item">
                <span class="label">培养方式</span>
                <span class="value">{{ selfData.method }}</span>
              </div>
              <div class="info-item">
                <span class="label">任务安排</span>
                <span class="value">{{ selfData.taskDetail }}</span>
              </div>
              <div class="info-item">
                <span class="label">预期输出结果</span>
                <span class="value">{{ selfData.output }}</span>
              </div>
              <div class="info-item">
                <span class="label">计划完成时间</span>
                <span class="value">{{ selfData.finishDate }}</span>
              </div>
            </div>
            <div class="evaluate-list">
              <van-form ref="form">
                <div
                  class="list-item"
                  v-for="(ite, index) in evaluateList"
                  :key="index"
                >
                  <!-- <div class="item-header">2023年3季度评价</div> -->
                  <div class="item-header">{{ ite.name }}</div>

                  <span
                    :class="[
                      'item-status',
                      ite.taskStatus == '已评价' ? 'rated' : 'other',
                    ]"
                    >{{ ite.taskStatus }}</span
                  >
                  <div class="list-content">
                    <div class="content-item">
                      <span class="item-label"> 自评结果 </span>
                      <span class="item-value">
                        {{ resultLabel(ite.selfResult) }}
                      </span>
                    </div>
                    <div class="content-item">
                      <span class="item-label"> 自评结果描述 </span>
                      <span class="item-value"> {{ ite.selfResultDes }} </span>
                    </div>
                    <div class="content-item">
                      <span>评价结果</span>
                      <span style="color: red">*</span>
                    </div>
                    <div class="content-item">
                      <van-field name="finish" :rules="finish">
                        <template #input>
                          <van-radio-group
                            v-model="evaluateList[index].evaluatorResult"
                            direction="horizontal"
                          >
                            <van-radio
                              v-for="(item, ind) in CompleteList"
                              :key="item.name"
                              :name="item.name"
                              label-disabled
                              checked-color="#ee0a24"
                              :disabled="ite.status == 1 || ite.status == 2"
                              >{{ item.label }}</van-radio
                            >
                          </van-radio-group>
                        </template>
                      </van-field>
                    </div>
                    <div class="content-item">
                      <span class="item-label"> 评价结果描述 </span>
                    </div>
                    <div class="content-item">
                      <van-field
                        v-model="evaluateList[index].evaluatorResultDes"
                        :disabled="ite.status"
                        clearable
                        type="textarea"
                        placeholder="请输入"
                      />
                    </div>
                  </div>
                </div>
              </van-form>
            </div>
          </van-collapse-item>
        </van-collapse>
      </div>
    </div>
    <div class="operation">
      <span class="submit" @click="handleSubmit">提交</span>
    </div>
  </div>
</template>

<script>
import { evaluatorEvaluation, getEvaluate } from "@/libs/api.js";
export default {
  data() {
    return {
      activeCollapse: ["Situation"],
      CompleteList: [
        {
          name: "A",
          label: "超预期完成",
        },
        {
          name: "B",
          label: "按计划完成",
        },
        {
          name: "C",
          label: "未达预期",
        },
      ],
      evaluateList: [],
      selfData: {},
      finish: [
        { required: true, message: "请选择评价结果", trigger: "onBlur" },
      ],
      showAll: false,
      idpId: null,
      taskId: null,
      entryId: null,
      number: localStorage.getItem("pk_psndoc") || "",
    };
  },
  computed: {
    // 处理人默认展示一个
    handlePerson() {
      let text = "";
      if (this.showAll) {
        text = this.selfData.processorName;
      } else {
        let defaultPerson = this.selfData.processorName
          ? this.selfData.processorName.split("、")
          : [];
        if (defaultPerson.length) {
          text = defaultPerson[0];
        }
      }
      return text;
    },
  },
  mounted() {
    this.idpId = this.$route.query.idpId;
    this.taskId = this.$route.query.taskId;
    this.entryId = this.$route.query.entryId;
    if (this.entryId) {
      this.getEvaluateData();
    }
  },
  methods: {
    // handleBack() {
    //   this.$router.go(-1);
    // },
    handleSubmit() {
      this.$refs.form
        .validate()
        .then(() => {
          console.log("// 验证通过");
          let evaluatorData = this.evaluateList.map((ite) => {
            return {
              taskId: ite.id,
              evaluatorResult: ite.evaluatorResult,
              evaluatorResultDes: ite.evaluatorResultDes,
            };
          });
          let params = {
            param: {
              idpId: this.idpId,
              number: this.number,
              evaluatorData,
            },
          };
          evaluatorEvaluation(params).then((res) => {
            if (res.status == 200 && res.data.statusCode == "200") {
              this.$toast.success("提交成功");
              setTimeout(() => {
                em.closeWindow();
                // this.$router.go(-1);
              }, 2000);
            }
          });
        })
        .catch(() => {
          //验证失败
        });
    },
    // 获取详情进入的确认数据
    getEvaluateData() {
      let params = {
        entryId: this.entryId,
        status: "C", //代表获取点击详情【自评】的活动
      };
      getEvaluate(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == "200") {
          this.selfData = res.data.data;
          this.evaluateList = res.data.data.evaluateList;
        }
      });
    },
    resultLabel(Result) {
      switch (Result) {
        case "A":
          return "超预期完成";
        case "B":
          return "按计划完成";
        case "C":
          return "逾期完成";
        default:
          return "";
      }
    },
  },
};
</script>

<style lang="less" scoped>
.confirm {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #eff1f4;
  .header {
    background-color: #ffffff;
    width: 100%;
    z-index: 99;
    .navbar-title {
      font-family: PingFang SC;
      font-weight: 800;
      font-size: 18px;
      color: #333333;
    }
  }
  .content {
    flex: 1;
    overflow: auto;
    .top {
      background: #ffffff;
      padding: 16px;
      .top-title {
        margin-bottom: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-family: PingFang SC;
        .label {
          font-weight: 700;
          font-size: 15px;
          color: #333333;
        }
        .status {
          padding: 7px 5px;
          background: #fef5f6;
          border-radius: 3px;
          font-weight: 500;
          font-size: 14px;
          color: #d80c1e;
        }
      }
      .disposes {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-family: PingFang SC;
        font-weight: 500;
        font-size: 12px;
        color: #333333;
        .show {
          display: flex;
          align-items: center;
          span {
            margin-left: 6px;
          }
        }
      }
    }
    .infos {
      .infos-item {
        margin-top: 12px;
      }
      .title-box {
        display: flex;
        align-items: center;
        .title {
          margin-left: 6px;
          font-family: PingFang SC;
          font-weight: 700;
          font-size: 15px;
          color: #333333;
        }
      }
      .situation-content-info {
        padding-bottom: 12px;
        .info-item {
          display: flex;
          align-items: center;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 15px;
          line-height: 36px;
          .label {
            width: 90px;
            margin-right: 20px;
            color: #999999;
          }
          .value {
            flex: 1;
            color: #333333;
          }
        }
      }
      .evaluate-list {
        padding-bottom: 55px;
        .list-item {
          margin-bottom: 12px;
          position: relative;
          // padding: 14px 0 11px 11px;
          box-sizing: border-box;
          border-radius: 5px;
          border: 1px solid #eeeeee;
          .item-header {
            padding-left: 11px;
            position: relative;
            font-family: PingFang SC;
            font-weight: 700;
            font-size: 15px;
            color: #333333;
            line-height: 36px;
            &::before {
              content: "";
              position: absolute;
              left: 0px;
              top: 10px;
              width: 5px;
              height: 20px;
              background: #d80c1e;
              border-radius: 0px 3px 3px 0px;
            }
          }
          .item-status {
            position: absolute;
            top: 0;
            right: 0;
            padding: 7px 10px;
            border-radius: 0px 5px 0px 5px;
            font-family: PingFang SC;
            font-weight: 500;
            font-size: 14px;
          }
          .rated {
            background: #fef5f6;
            color: #d80c1e;
          }
          .other {
            background: #eff1f4;
            color: #999999;
          }
          .list-content {
            padding: 0 0 11px 11px;
            .content-item {
              display: flex;
              font-family: PingFang SC;
              font-weight: 500;
              font-size: 14px;
              line-height: 36px;
              .item-label {
                width: 90px;
                margin-right: 18px;
                color: #999999;
              }
              .item-value {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: space-between;
                color: #333333;
              }
              /deep/ .van-cell {
                padding: 0;
              }
              /deep/ textarea {
                text-indent: 0;
              }
            }
          }
        }
      }
    }
  }

  .operation {
    margin-top: 15px;
    background-color: #ffffff;
    display: flex;
    // position: fixed;
    // bottom: 0;
    // left: 0;
    // right: 0;
    padding: 10px 16px;
    span {
      display: inline-block;
      flex: 1;
      padding: 16px 0;
      border-radius: 4px;
      text-align: center;
      font-family: PingFang SC;
      font-weight: 500;
      font-size: 18px;
    }
    .submit {
      color: #ffffff;
      background: #d80c1e;
    }
  }
  /deep/ .van-collapse-item__content {
    padding: 0 16px;
    // padding: 0 0 0 16px;
  }
}
</style>
