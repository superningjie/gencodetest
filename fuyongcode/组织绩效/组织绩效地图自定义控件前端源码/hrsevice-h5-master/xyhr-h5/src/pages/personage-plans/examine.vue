<template>
  <div class="examine">
    <!-- <div class="header">
      <van-nav-bar>
        <template #title>
          <span class="navbar-title">个人发展计划</span>
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
          <span class="label">{{ detailInfo.planName }}</span>
          <span class="status">审核中</span>
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
          <van-collapse-item name="Work" class="infos-item">
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

          <!-- 导师评价 -->
          <!-- <van-collapse-item name="Evaluate" class="infos-item">
						<template #title>
							<div class="title-box">
								<van-image fit="cover" width="15px" height="15px" :src="require('@/assets/personage-plans/evaluate.png')"></van-image>
								<div class="title">导师评价</div>
							</div>
						</template>
						<div class="evaluate-box">
							<div class="evaluate-item">
								<div class="evaluate-header">
									<div class="header-left">
										<van-image round fit="cover" width="25px" height="25px" :src="require('@/assets/KHCFDC.svg')"></van-image>
										<span class="name">张珊珊</span>
										<span>业务导师</span>
									</div>
									<div class="header-right">
										<span class="status">已评价</span>
										<span class="time">2023-12-31</span>
									</div>
								</div>
								<div class="evaluate-info">评价内容：如何以提高水平推动新质生产力发展？习近平总书记提供了重要得方法论：要不断扩大高水</div>
							</div>
							<div class="evaluate-item">
								<div class="evaluate-header">
									<div class="header-left">
										<van-image round fit="cover" width="25px" height="25px" :src="require('@/assets/KHCFDC.svg')"></van-image>
										<span class="name">张珊珊</span>
										<span>业务导师</span>
									</div>
									<div class="header-right">
										<span class="status">已评价</span>
										<span class="time">2023-12-31</span>
									</div>
								</div>
								<div class="evaluate-info">评价内容：如何以提高水平推动新质生产力发展？习近平总书记提供了重要得方法论：要不断扩大高水</div>
							</div>
						</div>
					</van-collapse-item> -->
          <!-- 完成情况 -->
          <!-- <van-collapse-item name="Situation" class="infos-item">
						<template #title>
							<div class="title-box">
								<van-image fit="cover" width="15px" height="15px" :src="require('@/assets/personage-plans/situation.png')"></van-image>
								<div class="title">完成情况</div>
							</div>
						</template>

						<div class="situation-content">
							<van-form ref="form">
								<div class="situation-item">
									<div class="label"><span>自评结果</span> <span style="color: red">*</span></div>
									<van-radio-group v-model="finishForm.radio" direction="horizontal">
										<van-radio name="1" label-disabled checked-color="#ee0a24">超预期完成</van-radio>
										<van-radio name="2" label-disabled checked-color="#ee0a24">按计划完成</van-radio>
										<van-radio name="3" label-disabled checked-color="#ee0a24">逾期完成</van-radio>
									</van-radio-group>
								</div>
								<div class="situation-item">
									<div class="label"><span>自评结果描述</span> <span style="color: red">*</span></div>
									<van-field v-model="finishForm.self" placeholder="请输入用户名" :rules="selfRules" />
								</div>
							</van-form>
						</div>
					</van-collapse-item> -->
        </van-collapse>
      </div>
    </div>
    <div class="operation-box">
      <span class="set" @click="handleApprovalFail('reject')">不通过</span>
      <span class="submit" @click="handleApprovalAdopt('pass')">通过</span>
    </div>
    <van-dialog
      v-model="isShowDialog"
      title="审批"
      theme="round-button"
      @confirm="handleConfirm"
      :before-close="handleClose"
    >
      <div class="examine-content">
        <p v-if="dialogStatus === 'reject'">
          信息补录人<span style="color: red">*</span>
        </p>
        <p
          v-if="dialogStatus === 'reject'"
          class="search-card"
          @click="handleSearch"
        >
          <span style="margin-right: 5px; color: #999">{{ peopleName }}</span>
          <van-icon name="search" color="#999" />
        </p>
        <p>审批信息<span style="color: red">*</span></p>
        <van-field
          v-model="examineValue"
          placeholder="请输入"
          rows="1"
          type="textarea"
          autosize
        />
      </div>
    </van-dialog>
  </div>
</template>

<script>
import { getDetails, approvalFail, approvalAdopt } from "@/libs/api.js";
import { before } from "lodash";
import { Toast } from "vant";
export default {
  data() {
    return {
      activeCollapse: [
        "BasicInfo",
        "Work",
        "TeachingInfo",
        "Goal",
        "Plans",
        "Evaluate",
        "Situation",
      ],
      checked: null,
      finishForm: {
        radio: "1",
        self: "",
      },
      selfRules: [
        {
          required: true,
          message: "自评结果描述不能为空",
          trigger: "onBlur",
        },
      ],
      detailInfo: {}, //顶部信息
      base: [], //基本信息
      rotation: [], //转入轮岗
      teaching: [], //带教信息
      taskList: [], //计划任务
      target: [], //发展目标
      planId: null,
      showAll: false,
      number: localStorage.getItem("pk_psndoc") || "",
      isShowDialog: false,
      examineValue: "",
      peopleName: "",
      dialogStatus: "",
      examineParams: {},
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
    this.getDetailsData();
  },
  methods: {
    // 获取--详情数据
    getDetailsData() {
      let params = {
        planId: this.planId, //个人发展计划id
        number: this.number,
      };
      getDetails(params).then((res) => {
        console.log("审核页面的详情：", res);
        if (res.status == 200 && res.data.statusCode == "200") {
          this.detailInfo = res.data.data;
          let { base, rotation, teaching, taskList, target } = res.data.data;
          this.base = base;
          this.rotation = rotation;
          this.teaching = teaching;
          this.taskList = taskList;
          this.target = target;
          this.taskList.forEach((ite) => {
            this.$set(ite, "isShow", true);
          });
          const obj = JSON.parse(localStorage.getItem("examineParams"));
          obj.planId = this.planId;

          if (obj.result) {
            this.isShowDialog = true;
            this.dialogStatus = "reject";
            this.peopleName = obj.resultName;
            this.examineValue = obj.examineValue;
          }
          this.examineParams = obj;
        }
      });
    },
    // 弹窗提交
    handleConfirm() {},
    async handleClose(action, done) {
      console.log(action, "action");
      if (this.dialogStatus === "reject") {
        console.log("1111", this.$route.query.result, this.examineValue);
        if (!this.examineParams.result.length || !this.examineValue) {
          Toast.fail("请先填写完整信息");
          done(false);
          return;
        }
        await this.handleApprovalFail();
        done();
      } else {
        if (!this.examineValue) {
          Toast.fail("请先填写审批信息");
          done(false);
          return;
        }
        await this.handleApprovalAdopt();
        done();
      }
    },
    // 选择信息补录人
    handleSearch() {
      const obj = {
        planId: this.planId,
        examineValue: this.examineValue,
        tabIndex: this.$route.query.tabIndex,
      };
      localStorage.setItem("examineParams", JSON.stringify(obj));
      this.$router.push({
        name: "examine-add",
      });
    },
    // 展开收起
    handleCommand(ite, index) {
      ite.isShow = !ite.isShow;
    },
    // 提交
    handleSubmit() {
      this.$refs.form
        .validate()
        .then(() => {
          this.$toast.success("提交成功");
        })
        .catch(() => {
          // this.$toast.fail('提交失败')
        });
    },
    // handleBack() {
    //   this.$router.go(-1);
    // },
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
    // 审批 - 不通过
    handleApprovalFail(val) {
      if (val) {
        this.isShowDialog = true;
        this.dialogStatus = val;
        this.examineValue = "";
        return;
      }
      let params = {
        param: {
          idpId: this.planId,
          number: this.number, //工号
          supplementNumber: this.examineParams.result,
          opinion: this.examineValue,
        },
      };
      approvalFail(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == 200) {
          Toast.success("驳回成功");
          setTimeout(() => {
            localStorage.setItem("examineParams", "");
            em.closeWindow();
            // this.$router.go(-1);
          }, 2000);

          // this.$router.go(-1);
        } else {
          Toast.success("审批失败");
        }
      });
    },
    // 审批 - 通过
    handleApprovalAdopt(val) {
      if (val) {
        this.isShowDialog = true;
        this.dialogStatus = val;
        this.examineValue = "";
        return;
      }
      let params = {
        param: {
          idpId: this.planId,
          number: this.number,
          opinion: this.examineValue,
        },
      };
      approvalAdopt(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == 200) {
          Toast.success("审批成功");
          setTimeout(() => {
            localStorage.setItem("examineParams", "");
            em.closeWindow();
            // this.$router.go(-1);
          }, 2000);
        } else {
          Toast.success("审批失败");
        }
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
    // padding-top: 46px;
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
        .evaluate-item {
          padding: 10px 0 16px 0;
          border-bottom: 1px solid #eeeeee;
          .evaluate-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-family: PingFang SC;
          }
          .header-left {
            display: flex;
            align-items: center;

            font-weight: 700;
            font-size: 14px;
            color: #333333;
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
              border-right: 1px solid #999999;
            }
            .time {
              padding-left: 8px;
            }
          }
          .evaluate-info {
            width: 306px;
            margin-left: 35px;
            text-align: justify;
            // padding: 16px 0;
            overflow: hidden; // 溢出隐藏
            text-overflow: ellipsis; // 溢出用省略号显示
            display: -webkit-box; // 作为弹性伸缩盒子模型显示。
            -webkit-box-orient: vertical; // 设置伸缩盒子的子元素排列方式：从上到下垂直排列
            -webkit-line-clamp: 2; // 显示的行数
          }
        }
      }
      .situation-content {
        padding-bottom: 12px;
        .situation-item {
          // margin: 4px 0 12px 0;
          padding-top: 4px;
          margin-bottom: 12px;
          .label {
            margin-bottom: 10px;
            font-family: PingFang SC;
            font-weight: 500;
            font-size: 14px;
            color: #999999;
          }
          /deep/ .van-cell {
            padding: 0;
          }
          /deep/ .van-field__control {
            border-radius: 5px;
            border: 1px solid #cccccc;
          }
        }
        .situation-item:last-child {
          margin-bottom: 0;
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
    .set {
      margin-right: 16px;
      color: #d80c1e;
      background: #ffffff;
      border: 1px solid #d80c1e;
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
}
.examine-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
  .search-card {
    border: 1px solid #999999;
    margin: 10px 0;
    padding: 5px;
    border-radius: 5px;
  }
  /deep/ .van-field {
    padding: 10px 0;
    .van-field__value {
      padding: 0;
      border-bottom: 1px solid #999;
      textarea {
        text-indent: 0;
      }
    }
  }
}
/deep/ .van-dialog--round-button .van-dialog__confirm {
  border-radius: 10px !important;
}
</style>
