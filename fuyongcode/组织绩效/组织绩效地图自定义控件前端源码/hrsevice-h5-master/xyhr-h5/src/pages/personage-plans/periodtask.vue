<template>
  <div class="periodtask">
    <div class="content">
      <div class="top">
        <div class="top-title">
          <span class="label">{{ EvaluateList.idpName }}</span>
          <span class="status"> 进行中 </span>
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
                <span class="value">{{ EvaluateList.ability }}</span>
              </div>
              <div class="info-item">
                <span class="label">培养方式</span>
                <span class="value">{{ EvaluateList.method }}</span>
              </div>
              <div class="info-item">
                <span class="label">任务安排</span>
                <span class="value">{{ EvaluateList.taskDetail }}</span>
              </div>
              <div class="info-item">
                <span class="label">预期输出结果</span>
                <span class="value">{{ EvaluateList.output }}</span>
              </div>
              <div class="info-item">
                <span class="label">计划完成时间</span>
                <span class="value">{{ EvaluateList.finishDate }}</span>
              </div>
            </div>
            <div class="evaluate-list">
              <div
                class="list-item"
                v-for="(item, index) in EvaluateList.evaluateList"
                :key="index"
              >
                <div class="item-header">
                  <span>{{ item.name }}</span>
                  <span class="item-status rated">{{ item.taskStatus }}</span>
                </div>

                <div class="list-content" v-show="item.taskStatus !== '未开始'">
                  <div
                    class="content-item"
                    v-show="
                      item.taskStatus === '已评价' ||
                      item.taskStatus === '员工已自评-评价人待评价'
                    "
                  >
                    <span class="item-label"> 自评结果 </span>
                    <span class="item-value">
                      {{ resultLabel(item.selfResult) }}
                    </span>
                  </div>
                  <div
                    class="content-item"
                    v-show="
                      item.taskStatus === '已评价' ||
                      item.taskStatus === '员工已自评-评价人待评价'
                    "
                  >
                    <span class="item-label"> 自评结果描述 </span>
                    <span class="item-value"> {{ item.selfResultDes }} </span>
                  </div>
                  <div
                    class="content-item"
                    v-show="item.taskStatus === '已评价'"
                  >
                    <span class="item-label"> 评定结果 </span>
                    <span class="item-value">
                      {{ resultLabel(item.evaluatorResult) }}
                    </span>
                  </div>
                  <div
                    class="content-item"
                    v-show="item.taskStatus === '已评价'"
                  >
                    <span class="item-label"> 评定结果描述 </span>
                    <span class="item-value">
                      {{ item.evaluatorResultDes }}
                    </span>
                  </div>
                  <div
                    class="content-item"
                    v-show="item.taskStatus === '已评价'"
                  >
                    <span class="item-label"> 评定人 </span>
                    <span class="item-value"> {{ item.evaluator }} </span>
                  </div>
                  <div
                    class="content-item"
                    v-show="item.taskStatus === '已评价'"
                  >
                    <span class="item-label"> 评定时间 </span>
                    <span class="item-value">
                      {{ item.evaluateTime }}
                    </span>
                  </div>

                  <!-- 评价 -- 需要当前【登录工号与评价人工号匹配】才显示员工已自评-评价人待评价入口 -->
                  <div
                    class="content-item"
                    v-if="
                      item.taskStatus === '员工已自评-评价人待评价' &&
                      EvaluateList.number === number
                    "
                  >
                    <span class="item-label"> 评价 </span>
                    <span class="item-value" @click="goConfirm(item)">
                      <span>员工已自评-评价人待评价</span>
                      <van-image
                        style="margin-right: 10px"
                        fit="cover"
                        width="5px"
                        height="9px"
                        :src="require('@/assets/personage-plans/right.png')"
                      ></van-image
                    ></span>
                  </div>
                  <div class="content-item" v-if="item.taskStatus === '待自评'">
                    <span class="item-label"> 评价 </span>
                    <span class="item-value" @click="goSelf(item)">
                      <span>去评价</span>
                      <van-image
                        style="margin-right: 10px"
                        fit="cover"
                        width="5px"
                        height="9px"
                        :src="require('@/assets/personage-plans/right.png')"
                      ></van-image
                    ></span>
                  </div>
                </div>
              </div>
            </div>
          </van-collapse-item>
        </van-collapse>
      </div>
    </div>
  </div>
</template>

<script>
// 查看任务评价详情
import { getEvaluate } from "@/libs/api.js";

export default {
  data() {
    return {
      activeCollapse: ["Situation"],
      EvaluateList: {},
      Obj: {},
      showAll: false,
      number: localStorage.getItem("pk_psndoc") || "",
    };
  },
  computed: {
    // 处理人默认展示一个
    handlePerson() {
      let text = "";
      if (this.showAll) {
        text = this.EvaluateList.processorName;
      } else {
        let defaultPerson = this.EvaluateList.processorName
          ? this.EvaluateList.processorName.split("、")
          : [];
        if (defaultPerson.length) {
          text = defaultPerson[0];
        }
      }
      return text;
    },
  },
  mounted() {
    let Obj = this.$route.query.Obj;
    this.Obj = Obj ? JSON.parse(Obj) : {};
    this.getEvaluateData();
  },
  methods: {
    getEvaluateData() {
      let params = {
        entryId: this.Obj.entryId ? this.Obj.entryId : "",
        status: "A", //代表获取详情所有的评价活动
      };
      getEvaluate(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == "200") {
          this.EvaluateList = res.data.data;
        }
      });
    },
    // 前往评价人确认页
    goConfirm(item) {
      this.$router.push({
        name: "plans-confirm",
        query: {
          idpId: item.idpId,
          taskId: item.id,
          entryId: this.Obj.entryId,
        },
      });
    },
    // 前往自评页
    goSelf(item) {
      this.$router.push({
        name: "plans-self",
        query: {
          idpId: item.idpId,
          taskId: item.id,
          entryId: this.Obj.entryId,
        },
      });
    },
    resultLabel(Result) {
      switch (Result) {
        case "A":
          return "超预期完成";
        case "B":
          return "按计划完成";
        case "C":
          return "未达预期";
        default:
          return "";
      }
    },
  },
};
</script>

<style lang="less" scoped>
.periodtask {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #eff1f4;
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
        padding-bottom: 24px;
        .list-item {
          margin-bottom: 12px;
          // position: relative;
          // padding: 14px 0 11px 11px;
          box-sizing: border-box;
          border-radius: 5px;
          border: 1px solid #eeeeee;
          .item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-left: 11px;
            position: relative;
            font-family: PingFang SC;
            font-weight: 700;
            font-size: 15px;
            color: #333333;
            // line-height: 36px;
            &::before {
              content: "";
              position: absolute;
              left: 0px;
              top: 8px;
              width: 5px;
              height: 20px;
              background: #d80c1e;
              border-radius: 0px 3px 3px 0px;
            }
          }
          .item-status {
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
            }
          }
        }
      }
    }
  }
  .fade-enter-active,
  .fade-leave-active {
    transition: all 0.2s;
  }

  .fade-enter,
  .fade-leave-to {
    opacity: 0;
  }
  /deep/ .van-collapse-item__content {
    padding: 0 16px;
    // padding: 0 0 0 16px;
  }
}
</style>
