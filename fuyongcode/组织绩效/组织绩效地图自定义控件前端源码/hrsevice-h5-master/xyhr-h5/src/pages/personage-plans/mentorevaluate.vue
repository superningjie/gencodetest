<template>
  <!-- 导师评价页 -->
  <div class="examine">
    <div class="content">
      <div class="top">
        <div class="top-title">
          <span class="label">{{ detailInfo.planName }}</span>
          <span class="status" v-show="detailInfo.planStatus">{{
            detailInfo.planStatus
          }}</span>
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
          <!-- 基本信息 -->
          <van-collapse-item name="BasicInfo" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/basic-info.png')"
                ></van-image>
                <div class="title">基本信息</div>
              </div>
            </template>
            <div class="info-box">
              <div class="info-item" v-for="(ite, ind) in base" :key="ind">
                <span class="label">{{ ite.name }}</span>
                <span class="value">{{ ite.value }}</span>
              </div>
            </div>
          </van-collapse-item>
          <!-- 轮入岗位信息 -->
          <van-collapse-item
            name="Work"
            class="infos-item"
            v-if="detailInfo.isView"
          >
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/work.png')"
                ></van-image>
                <div class="title">轮入岗位信息</div>
              </div>
            </template>
            <div class="info-box">
              <div class="info-item" v-for="(it, idx) in rotation" :key="idx">
                <span class="label">{{ it.name }}</span>
                <span class="value">{{ it.value }}</span>
              </div>
            </div>
          </van-collapse-item>
          <!-- 带教信息 -->
          <van-collapse-item name="TeachingInfo" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/teaching-info.png')"
                ></van-image>
                <div class="title">带教信息</div>
              </div>
            </template>
            <div class="info-box">
              <div
                class="info-item"
                v-for="(ite, index) in teaching"
                :key="index"
              >
                <span class="label">{{ ite.name }}</span>
                <span class="value">{{ ite.value }}</span>
              </div>
            </div>
          </van-collapse-item>
          <!-- 发展目标 -->
          <van-collapse-item name="Goal" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/goal.png')"
                ></van-image>
                <div class="title">发展目标</div>
              </div>
            </template>
            <div class="info-box">
              <div class="info-item">
                <span class="label">优势</span>
                <span class="value">{{ target.advantage }}</span>
              </div>
              <div class="info-item">
                <span class="label">不足</span>
                <span class="value">{{ target.lack }}</span>
              </div>
              <div class="info-item">
                <span class="label">本年度提升重点</span>
                <span class="value">{{ target.promote }}</span>
              </div>
            </div>
          </van-collapse-item>
          <!-- 计划任务 -->
          <van-collapse-item name="Plans" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/plan.png')"
                ></van-image>
                <div class="title">计划任务</div>
              </div>
            </template>
            <div class="plans-box">
              <div
                class="plan-item"
                v-for="(ite, index) in taskList"
                :key="index"
              >
                <div class="plan-item-title">
                  <!-- <van-checkbox v-model="ite.checkStatus" shape="square" checked-color="#ee0a24">{{ ite.projectnName }}</van-checkbox> -->
                  <span>XXXX股份投资分析项目</span>
                  <span class="fold" @click="handleCommand(ite, index)">
                    {{ ite.isShow ? "收起" : "展开" }}</span
                  >
                </div>
                <transition name="fade">
                  <div class="plan-item-content" v-show="ite.isShow">
                    <div class="content-item">
                      <span class="item-label">任务安排</span>
                      <span class="item-value">{{ ite.arrange }} </span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">任务周期</span>
                      <span class="item-value">{{ ite.period }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">提升能力</span>
                      <span class="item-value">{{ ite.ability }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">培养方式</span>
                      <span class="item-value">{{ ite.culture }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">预期输出结果</span>
                      <span class="item-value">{{ ite.result }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">计划完成时间</span>
                      <span class="item-value">{{ ite.finishDate }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">自评进度</span>
                      <span class="item-value">{{ ite.self }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">评价进度</span>
                      <span class="item-value">{{ ite.one }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">评价人</span>
                      <span class="item-value">{{ ite.evaluator }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">最后评价日期</span>
                      <span class="item-value">{{ ite.lastDate }}</span>
                    </div>
                    <div class="content-item">
                      <span class="item-label">任务评价</span>
                      <span class="item-value detail" @click="goFinsh(ite)"
                        >详情>></span
                      >
                    </div>
                  </div>
                </transition>
              </div>
            </div>
          </van-collapse-item>
          <!-- 员工总体评价 -->
          <!-- <van-collapse-item name="SelfTest" id="selfTest">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/evaluate.png')"
                ></van-image>
                <div class="title">员工总体评价</div>
              </div>
              <div>
                员工总体自评的内容
              </div>
            </template>
          </van-collapse-item> -->
          <van-collapse-item name="selfEvaluate" class="infos-item">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/basic-info.png')"
                ></van-image>
                <div class="title">员工总自评</div>
              </div>
            </template>
            <div class="info-box">
              <div class="info-item">
                <span class="label">评价人</span>
                <span class="value">{{ selfEvaluate.evaluator }}</span>
              </div>
              <div class="info-item">
                <span class="label">评价时间</span>
                <span class="value">{{ selfEvaluate.evaltDate }}</span>
              </div>
              <div class="info-item">
                <span class="label">完成情况</span>
                <span class="value">{{ selfEvaluate.finishStatus }}</span>
              </div>
              <div class="info-item">
                <span class="label">完成情况评价</span>
                <span class="value">{{ selfEvaluate.evaluate }}</span>
              </div>
            </div>
          </van-collapse-item>
          <!-- 导师评价 -->
          <van-collapse-item name="Evaluate" class="infos-item" id="evaluate">
            <template #title>
              <div class="title-box">
                <van-image
                  fit="cover"
                  width="15px"
                  height="15px"
                  :src="require('@/assets/personage-plans/evaluate.png')"
                ></van-image>
                <div class="title">导师评价</div>
              </div>
            </template>
            <div class="evaluate-box">
              <van-form ref="form">
                <div
                  class="evaluate-item"
                  v-for="(item, index) in mentorEvaltList"
                  :key="index"
                >
                  <div class="evaluate-header">
                    <div class="header-left">
                      <van-image
                        round
                        fit="cover"
                        width="22px"
                        height="22px"
                        :src="require('@/assets/KHCFDC.svg')"
                      ></van-image>
                      <span class="name">{{ item.evaluator }}</span>
                      <span>{{ item.role }}</span>
                    </div>
                    <div class="header-right">
                      <span class="status">{{ item.status }}</span>
                      <span class="time">{{ item.evaltDate }}</span>
                    </div>
                  </div>
                  <div class="evaluate-info" v-if="item.status === '已评价'">
                    <p>完成情况：{{ item.finishStatus }}</p>
                    <p class="info-content">评价内容：{{ item.evaluate }}</p>
                  </div>
                  <div class="evaluate-info" v-else>
                    <p>完成情况<span style="color: red">*</span>：</p>
                    <van-radio-group
                      v-model="resultRadio"
                      direction="horizontal"
                    >
                      <van-radio
                        v-for="(ite, ind) in radioList2"
                        :key="ind"
                        :name="ite.value"
                        checked-color="#ee0a24"
                        icon-size="16px"
                        >{{ ite.label }}</van-radio
                      >
                    </van-radio-group>
                    <p class="info-content">评价内容：</p>
                    <van-field v-model="resultValue" placeholder="请输入" />
                  </div>
                </div>
              </van-form>
            </div>
          </van-collapse-item>
        </van-collapse>
      </div>
    </div>
    <div class="operation-box">
      <span class="submit" @click="handleSubmit">提交</span>
    </div>
  </div>
</template>

<script>
import {
  getDetails,
  mentorEvaluation,
  submitFormEvaluate,
} from "@/libs/api.js";
export default {
  data() {
    return {
      activeCollapse: [
        "BasicInfo",
        "Work",
        "TeachingInfo",
        "Goal",
        "Plans",
        "SelfTest",
        "Situation",
        "Evaluate",
        "selfEvaluate",
      ],
      checked: null,
      SelfAssessmentDescription: "",
      detailInfo: {}, //顶部信息
      base: [], //基本信息
      rotation: [], //转入轮岗
      teaching: [], //带教信息
      taskList: [], //计划任务
      target: [], //发展目标
      mentorEvaltList: [], //导师评价
      selfEvaluate: {}, //员工总自评
      planId: null,
      evaluateRule: [{ required: true, message: "", trigger: "onBlur" }],
      showAll: false,
      personNum: localStorage.getItem("pk_psndoc") || "",
      radioList2: [
        { label: "超预期完成", value: "A" },
        { label: "按计划完成", value: "B" },
        { label: "未达预期", value: "C" },
      ],
      resultRadio: "",
      resultValue: "",
    };
  },
  computed: {
    // 处理人默认展示一个
    handlePerson() {
      let text = "";
      if (this.showAll) {
        text = this.detailInfo.handlePerson;
      } else {
        let defaultPerson = this.detailInfo.handlePerson
          ? this.detailInfo.handlePerson.split("、")
          : [];
        if (defaultPerson.length) {
          text = defaultPerson[0];
        }
      }
      return text;
    },
  },
  mounted() {
    let { planId } = this.$route.query;
    this.planId = planId ? planId : null;
    this.getDetails();
  },
  methods: {
    // 展开收起
    handleCommand(ite, index) {
      ite.isShow = !ite.isShow;
    },
    // 提交
    handleSubmit() {
      if (this.resultRadio) {
        this.Evaluation();
      } else {
        this.$toast.warning("请选择完成情况");
      }
    },
    // 获取--详情数据
    getDetails() {
      let params = {
        planId: this.planId, //个人发展计划id
        number: this.personNum,
      };
      getDetails(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == "200") {
          this.detailInfo = res.data.data;
          let {
            base,
            rotation,
            teaching,
            taskList,
            target,
            mentorEvaltList,
            selfEvaluate,
          } = res.data.data;
          this.base = base;
          this.rotation = rotation;
          this.teaching = teaching;
          this.taskList = taskList;
          this.target = target;
          // 当前导师评价的数据 --> 我们上方传了当前登录人的工号，只返回自己的评价信息。
          this.mentorEvaltList = mentorEvaltList;
          this.selfEvaluate = selfEvaluate;
          // if (selfEvaluate) {
          //   this.resultValue = selfEvaluate.evaluate;
          // }
          this.taskList.forEach((ite) => {
            this.$set(ite, "isShow", true);
          });
          this.scrollToBottom();
        }
      });
    },
    // 导师评价
    Evaluation() {
      let params = {
        param: {
          idpId: this.planId,
          number: this.personNum,
          evaluate: this.resultValue,
          finish: this.resultRadio,
        },
      };
      mentorEvaluation(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == "200") {
          this.$toast.success("提交成功");
          setTimeout(() => {
            em.closeWindow();
            // this.$router.go(-1);
          }, 2000);
        }
      });
    },

    //
    goFinsh(item) {
      let { planName, planStatus, handlePerson } = this.detailInfo;
      let { ability, culture, arrange, result, finishDate, entryId } = item;
      let Obj = {
        planName,
        planStatus,
        handlePerson,
        ability,
        culture,
        arrange,
        result,
        finishDate,
        entryId,
      };
      this.$router.push({
        name: "plans-periodtask",
        query: {
          Obj: JSON.stringify(Obj),
        },
      });
    },
    // 数据请求完成dom渲染完成后 -- 滚动至最低处
    scrollToBottom() {
      const container = document.querySelector(".content");
      const evaluateDom = document.querySelector("#evaluate");
      // const selfDom = document.querySelector()
      // console.log(
      //   "content元素是：",
      //   container.scrollHeight,
      //   evaluateDom,
      //   container
      // );
      // 等待页面渲染完成后，滚动到 .content 容器的最底部
      this.$nextTick(() => {
        container.scrollTop = container.scrollHeight;
        console.log(
          "接口请求完成，dom渲染完成后父级的scrollHeight！",
          container.scrollHeight
        );
      });
    },
  },
};
</script>

<style lang="less" scoped>
.examine {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #eff1f4;
  .content {
    flex: 1;
    overflow: scroll;
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
      .info-box {
        .info-item {
          display: flex;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 15px;
          line-height: 36px;

          .label {
            margin-right: 20px;
            width: 105px;
            color: #999999;
          }
          .value {
            flex: 1;
            color: #333333;
          }
        }
      }
      .plans-box {
        .plan-item {
          margin-bottom: 16px;
          padding-bottom: 16px;
          border-bottom: 1px solid #eeeeee;
          .plan-item-title {
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-family: PingFang SC;
            font-weight: 500;
            font-size: 15px;
            color: #333333;
            .project-name {
              color: #333333;
            }
            .fold {
              font-size: 12px;
              color: #999999;
            }
          }
          .plan-item-content {
            padding: 12px 10px;
            background-color: #f5f5f5;
            .content-item {
              display: flex;
              align-items: center;
              line-height: 25px;
              font-family: PingFang SC;
              font-weight: 500;
              font-size: 12px;
              .item-label {
                margin-right: 20px;
                width: 72px;
                color: #999999;
              }
              .item-value {
                flex: 1;
                color: #333333;
              }
              .detail {
                color: #d80c1e;
              }
            }
          }
        }
        .plan-item:last-child {
          margin-bottom: 0px;
        }
      }
      .evaluate-box {
        padding-bottom: 10px;
        .evaluate-item {
          padding: 0 10px 10px 10px;
          border: 1px solid #eeeeee;
          border-radius: 10px;
          margin-bottom: 10px;

          .evaluate-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-family: PingFang SC;
            padding: 8px 0;
            border-bottom: 1px solid #eeeeee;
          }
          .header-left {
            display: flex;
            align-items: center;

            // font-weight: 700;
            font-size: 13px;
            color: #999999;
            .name {
              margin: 0 9px;
            }
          }
          .header-right {
            font-weight: 500;
            font-size: 12px;
            color: #999999;
            .status {
              padding-right: 10px;
              // border-right: 1px solid #999999;
            }
            .time {
              padding-left: 8px;
            }
          }
          .evaluate-info {
            // width: 306px;
            width: 100%;
            // margin-left: 35px;
            // text-align: justify;
            // padding: 16px 0;
            font-size: 12px;
            p {
              line-height: 20px;
            }
            .info-content {
              overflow: hidden; // 溢出隐藏
              text-overflow: ellipsis; // 溢出用省略号显示
              display: -webkit-box; // 作为弹性伸缩盒子模型显示。
              -webkit-box-orient: vertical; // 设置伸缩盒子的子元素排列方式：从上到下垂直排列
              -webkit-line-clamp: 2; // 显示的行数
            }
          }
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
    }
  }
  .operation-box {
    margin-top: 12px;
    background: #ffffff;
    display: flex;
    padding: 10px 16px;
    align-items: center;
    span {
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
  /deep/ textarea {
    text-indent: 0;
  }
  /deep/ .evaluate-box .van-cell {
    padding: 0;
  }
}
</style>
