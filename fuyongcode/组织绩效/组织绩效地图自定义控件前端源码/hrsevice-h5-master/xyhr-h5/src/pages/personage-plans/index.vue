<!--
 * @Author: 张坤伟 1261585833@qq.com
 * @Date: 2024-05-15 16:04:59
 * @LastEditors: 张坤伟 1261585833@qq.com
 * @LastEditTime: 2024-08-07 17:52:20
 * @FilePath: \xyhr-h5\src\pages\plan\index.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
  <div class="plans">
    <!-- 头部 -->
    <div class="header">
      <div class="top">
        <div class="tabs">
          <div
            :class="tabIndex == index ? 'active' : ''"
            v-for="(item, index) in tabList"
            :key="index"
            @click="changeTab(index)"
          >
            {{ item }}
          </div>
        </div>
        <!-- <div class="middle" v-show="tabIndex == 1">
          <span
            v-for="(item, index) in typeList"
            :key="index"
            @click="changeType(index)"
            :class="typeIndex == index ? 'active' : ''"
            >{{ item }}
            <i class="line" v-if="typeIndex == index"></i>
          </span>
        </div> -->
        <div class="search-box">
          <div class="left">
            <van-search
              :left-icon="require('@/assets/personage-plans/search.png')"
              v-model="keyWord"
              @blur="handleSearch"
              shape="round"
              placeholder="输入关键字搜索"
            />
          </div>
          <div class="right">
            <van-dropdown-menu>
              <van-dropdown-item ref="filtrate">
                <template #title>
                  <div class="title-box">
                    <span>筛选</span>
                    <van-image
                      fit="cover"
                      width="14px"
                      height="14px"
                      :src="require('@/assets/personage-plans/filtration.png')"
                    ></van-image>
                  </div>
                </template>
                <van-cell
                  title="组织"
                  v-show="tabIndex == 1"
                  is-link
                  @click="goOrg()"
                />
                <div class="right-status">
                  <span class="condition">状态</span>
                  <div class="status-box">
                    <span
                      :class="statusActive == it.value ? 'active' : ''"
                      v-for="(it, ind) in statusList"
                      :key="ind"
                      @click="changeStatus(it.value)"
                      >{{ it.label }}</span
                    >
                  </div>
                </div>
                <div class="bottom">
                  <div class="bottom-content">
                    <span class="clear" @click="handleClear">清空并退出</span>
                    <span class="confirm" @click="handleConfirm">确定</span>
                  </div>
                </div>
              </van-dropdown-item>
            </van-dropdown-menu>
            <!-- <span>筛选</span>
						<van-image fit="cover" width="14px" height="14px" :src="require('@/assets/personage-plans/filtration.png')"></van-image> -->
          </div>
        </div>
      </div>
    </div>
    <div class="content" v-if="planDataList.length" ref="scrollDiv">
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          class="content-item"
          v-for="(item, index) in planDataList"
          :key="index"
        >
          <div class="item-title">
            <span class="status" style="margin-right: 10px; flex: 1">{{
              item.planName
            }}</span>
            <span style="height: 14px" class="right_status status">{{
              item.planStatus
            }}</span>
          </div>
          <div class="info-box">
            <div class="left">
              <div class="left-item">
                <span class="left-label">发展方向：</span>
                <span class="left-value">{{ item.direction }}</span>
              </div>
              <div class="left-item" style="justify-content: space-between">
                <div
                  style="
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    width: calc(100% - 40px);
                  "
                >
                  <span class="left-label">提升重点：</span>
                  <span class="left-value">{{ item.promote }}</span>
                </div>
                <div class="detail" @click="goDetal(item)">
                  <span>详情</span>
                  <van-image
                    round
                    fit="cover"
                    width="6px"
                    height="10px"
                    :src="require('@/assets/personage-plans/right.png')"
                  ></van-image>
                </div>
              </div>
              <div class="left-item">
                <span class="left-label">发展周期：</span>
                <span class="left-value"
                  >{{ item.startDate }} ~ {{ item.endDate }}</span
                >
              </div>
              <!-- <div class="left-item" v-show="item.planStatus == '已完成'">
                <span class="left-label">完成情况:</span>
                <span class="left-value">{{ item.finishStatus }}</span>
              </div>
              <div class="left-item" v-show="item.planStatus == '已完成'">
                <span class="left-label">结果评价:</span>
                <span class="left-value">{{ item.finishElt }}</span>
              </div> -->
            </div>
          </div>
          <div
            class="info-bottom"
            v-if="
              ['1', '2', '3', '4', '5', '6', '8'].includes(item.handleType) &&
              !item.isDone
            "
          >
            <span
              class="title-button"
              @click="goExamine(item)"
              v-if="item.handleType == 5 && !item.isDone"
              >审批</span
            >
            <span
              class="title-button"
              @click="goExamine(item)"
              v-if="item.handleType == 6 && !item.isDone"
              >信息补录</span
            >
            <span
              class="title-button"
              @click="goExamine(item)"
              v-if="
                ['1', '2', '3', '4', '8'].includes(item.handleType) &&
                !item.isDone
              "
              >评价</span
            >
          </div>
        </div>
      </van-list>
    </div>
    <xy-empty v-else></xy-empty>
    <planDialog
      :status="status"
      :entryId="entryId"
      ref="planDialog"
      v-if="status"
      @refreshList="refreshList"
    ></planDialog>
  </div>
</template>

<script>
import planDialog from "./plan-dialog.vue";
import { getDevPlanList } from "@/libs/api.js";
import { Toast, Dialog } from "vant";

export default {
  components: {
    planDialog,
  },
  data() {
    return {
      isShow: true,
      keyWord: "", //关键字搜索
      tabIndex: 1,
      tabList: ["我个人的", "我参与的"],
      active: "pending",
      typeList: ["待处理", "已处理"],
      typeIndex: 0,
      value: "",
      List: [],
      statusList: [
        {
          label: "审批中",
          value: "B",
        },
        {
          label: "审批不通过",
          value: "C",
        },
        {
          label: "信息补录中",
          value: "D",
        },
        {
          label: "进行中",
          value: "E",
        },
        {
          label: "导师已评价",
          value: "F",
        },
        {
          label: "已完成",
          value: "G",
        },
        {
          label: "已终止",
          value: "H",
        },
      ],
      statusActive: "",
      planDataList: [], //个人发展计划列表数据
      total: null, //总条数
      pageNum: 1, //页码
      ids: [],
      loading: false,
      finished: false,
      personNum: localStorage.getItem("pk_psndoc") || "",
      plansParams: JSON.parse(localStorage.getItem("plansParams")) || {},
      resultRadio: 0,
      radioList: [
        { label: "超预期完成", value: "A" },
        { label: "按计划完成", value: "B" },
        { label: "未达预期", value: "C" },
      ],
      entryId: "",
      status: "",
    };
  },
  mounted() {
    this.ids = this.plansParams.FineResult
      ? this.plansParams.FineResult.map((ite) => ite.id)
      : [];
    this.tabIndex = this.$route.query.tabIndex || 1;
    this.typeIndex = this.plansParams.typeIndex || 0;
    this.getList();
  },
  methods: {
    NewAdd() {
      this.isShow = true;
    },
    // 获取个人发展计划列表
    getList() {
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      });
      let params = {
        personNum: this.personNum,
        type: this.tabIndex == 1 ? 2 : 1, //个人/参与 -- 1个人 、 2参与
        keyword: this.keyWord, //关键字
        status: this.statusActive, //状态
        // isDeal: this.tabIndex == 0 ? "" : this.typeIndex == 0 ? "N" : "Y", //【参与情况下】处理/未处理
        pageNum: this.pageNum, //页面
        ids: this.ids,
      };
      getDevPlanList(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == 200) {
          Toast.clear();
          let { planDataList, total } = res.data.data;
          this.planDataList = planDataList;
          this.total = total;
          if (this.planDataList.length == this.total) {
            this.finished = true;
          }
          this.loading = false;
        }
      });
    },
    changeTab(v) {
      localStorage.removeItem("plansParams");
      this.tabIndex = v;
      this.planDataList = [];
      this.total = 0;
      this.keyWord = "";
      this.ids = [];
      this.statusActive = "";
      this.pageNum = 1;
      this.loading = false;
      this.finished = false;
      this.getList();
    },
    changeType(v) {
      this.typeIndex = v;
      this.planDataList = [];
      this.total = 0;
      this.keyWord = "";
      this.ids = [];
      this.statusActive = "";
      this.pageNum = 1;
      this.loading = false;
      this.finished = false;
      this.getList();
    },
    goDetal(item) {
      this.$router.push({
        name: "plans-detail",
        query: {
          planId: item.planId,
        },
      });
    },
    // 前往个人发展计划 -- 审批页面
    goExamine(item) {
      if (["1", "2", "3", "4", "8"].includes(item.handleType)) {
        switch (item.handleType) {
          case "1":
            this.status = 5;
            break;
          case "2":
            // 导师评价
            this.status = 4;

            break;
          case "3":
            this.status = 2;
            break;
          case "4":
            this.status = 3;

            break;
          case "8":
            this.status = 1;
        }
        this.entryId = item.planId;
        this.$nextTick(() => {
          this.$refs.planDialog.isShow = true;
          this.$refs.planDialog.getDetail(item);
        });
      } else if (item.handleType == "5") {
        this.$router.push({
          name: "plans-examine",
          query: {
            planId: item.planId,
            tabIndex: this.tabIndex,
          },
        });
      } else if (item.handleType == "6") {
        // 信息补录
        Dialog.alert({
          title: "提示",
          message: "移动端暂不支持个人发展计划信息完善操作,请到PC端处理!",
          theme: "round-button",
        }).then(() => {
          // on close
        });
      }
    },
    refreshList() {
      this.getList();
    },
    goSelf(item) {
      this.$router.push({
        name: "plans-selfs",
        query: {
          planId: item.planId,
        },
      });
    },
    goFinish(item) {
      if (item.planStatus === "导师已评价") {
        // 完成情况评价页
        this.$router.push({
          name: "plans-finsh",
          query: {
            planId: item.planId,
          },
        });
      } else if (item.planStatus === "进行中") {
        // 导师评价页
        this.$router.push({
          name: "plans-mentorevaluate",
          query: {
            planId: item.planId,
          },
        });
      }
    },
    goSupplementation() {
      this.$router.push({
        name: "plans-supplementation",
      });
    },
    // 评价待确认页
    goConfirm(item) {
      // 完成情况评价页
      this.$router.push({
        name: "plans-confirms",
        query: {
          planId: item.planId,
        },
      });
    },
    // 状态切换
    changeStatus(v) {
      this.statusActive = v;
    },
    handleClear() {
      this.ids = [];
      this.statusActive = "";
      // 关闭筛选下拉
      this.$refs.filtrate.toggle();
      this.getList();
    },
    handleConfirm() {
      // 关闭筛选下拉
      this.$refs.filtrate.toggle();
      this.getList();
    },
    // 组织
    goOrg() {
      localStorage.removeItem("plansParams");
      this.$router.push({
        name: "board-organization",
        query: {
          isPlans: true,
          typeIndex: this.typeIndex,
        },
      });
      // this.showOrganization = true;
    },
    onLoad() {
      if (this.planDataList.length < this.total) {
        this.pageNum += 1;
        this.getList();
      }
    },
    // // 更新数据
    // updateData(paramsObj) {
    //   if (paramsObj) {
    //     let { FineResult } = paramsObj;
    //     this.ids = FineResult.map((ite) => ite.id);
    //   } else {
    //     this.ids = [];
    //   }
    //   this.showOrganization = false;
    //   this.getList();
    // },
    handleSearch() {
      this.pageNum = 1;
      this.getList();
    },
  },
};
</script>

<style lang="less" scoped>
.plans {
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  .header {
    background-color: #ffffff;
    width: 100%;
    z-index: 99;
    // position: fixed;
    // top: 0;
    .top {
      .tabs {
        margin: 22px 64px 16px 64px;
        display: flex;
        background: #f5f5f5;
        border-radius: 30px;
        div {
          flex: 1;
          padding: 8px 26px;
          font-family: PingFangSC-Regular;
          font-size: 14px;
          color: #b7b7b7;
          letter-spacing: 0;
          text-align: center;
          line-height: 16px;
          font-weight: 400;
        }
        .active {
          background: linear-gradient(90deg, #e3423f 0%, #c42c29 100%);
          box-shadow: 0px 6px 18px 0px rgba(55, 4, 3, 0.2);
          border-radius: 30px;
          font-weight: 500;
          color: #ffffff;
        }
        // div {
        // 	padding: 8px 20px;
        // }
      }
      .middle {
        display: flex;
        padding: 0 13px;
        span {
          display: flex;
          justify-content: center;
          padding-bottom: 12px;
          position: relative;
          flex: 1;
          font-family: PingFang SC;
          font-size: 14px;
          color: #333333;
          .line {
            bottom: 0;
            position: absolute;
            width: 27px;
            height: 2px;
            background: #d80c1e;
            border-radius: 3px;
          }
        }
        .active {
          font-weight: 700;
          color: #d80c1e;
        }
      }
      .search-box {
        display: flex;
        align-items: center;
        padding: 0 14px 12px 16px;
        .left {
          width: 279px;
        }
        .right {
          flex: 1;
          display: flex;
          justify-content: center;
          align-items: center;
          font-family: PingFang SC;
          font-weight: 500;
          .title-box {
            display: flex;
            align-items: center;
            span {
              font-size: 15px;
              color: #333333;
              margin-right: 5px;
            }
          }
          .right-status {
            padding: 0 16px;
            margin-bottom: 20px;
            .condition {
              padding: 10px 0;
              display: inline-block;
              font-size: 15px;
              color: #323233;
            }
            .status-box {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              grid-gap: 10px; /* 设置间距 */
              span {
                border-radius: 4px;
                padding: 4px 0;
                text-align: center;
                font-size: 13px;
                color: #999999;
                border: 1px solid #cccccc;
              }
              .active {
                border: 1px solid #d80c1e;
                color: #d80c1e;
              }
            }
          }
          .bottom {
            border-top: 1px solid #cccccc;
            .bottom-content {
              display: flex;
              align-items: center;
              justify-content: space-between;
              padding: 10px 16px;
              .clear {
                font-size: 15px;
                color: #d80c1e;
              }
              .confirm {
                padding: 5px 16px;
                border-radius: 4px;
                font-size: 13px;
                color: #ffffff;
                background-color: #d80c1e;
              }
            }
          }
          /deep/ .van-dropdown-menu__bar {
            box-shadow: none;
          }
          /deep/ .van-dropdown-menu__title::after {
            display: none;
          }
          /deep/ .van-cell {
            font-size: 15px;
            border-bottom: 1px solid #e8e8e8;
          }
        }
        /deep/ .van-field__left-icon {
          display: flex;
          align-items: center;
        }
      }
    }
  }
  .content {
    flex: 1;
    overflow: auto;
    background-color: #f2f2f2;
    .content-item {
      margin-top: 12px;
      background-color: #ffffff;
      box-sizing: border-box;
      border-radius: 15px;
      width: calc(100% - 20px);
      margin: 12px auto 0;
      // padding: 12px 16px;
      .item-title {
        position: relative;
        display: flex;
        justify-content: space-between;
        padding: 6px 12px;
        border-top-left-radius: 15px;
        font-family: PingFang SC;
        background: linear-gradient(
          to right,
          rgba(232, 196, 196, 1) 0%,
          rgba(232, 196, 196, 0.6) 50%,
          rgba(232, 196, 196, 0) 100%
        );
        .status {
          padding: 3px 0;
          // position: absolute;
          // top: 0;
          // right: 0;
          line-height: 16px;
          font-weight: 500;
          font-size: 12px;
          color: #d80c1e;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .right_status {
          color: rgb(254, 130, 92);
          // flex: 1;
          width: 70px;
          text-align: center;
        }
      }
      .info-box {
        display: flex;
        align-items: center;
        padding: 0px 12px;
        // padding: 12px 0 13px 0;
        border-top: 1px solid #e8e8e8;
        border-bottom: 1px solid #e8e8e8;
        .left {
          // width: 287px;
          width: 100%;
          .left-item {
            display: flex;
            font-family: PingFang SC;
            font-weight: 500;
            font-size: 13px;
            line-height: 26px;
            flex: 1;
            .left-label {
              // margin-right: 10px;
              color: #999999;
            }
            .left-value {
              display: inline-block;
              // width: 200px;
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              color: #333333;
            }
          }
        }
        .detail {
          display: flex;
          justify-content: flex-end;
          align-items: center;
          width: 50px;
          span {
            margin-right: 4px;
            font-family: PingFang SC;
            font-weight: 500;
            font-size: 12px;
            color: #999999;
          }
        }
      }
      .info-bottom {
        display: flex;
        justify-content: flex-end;
        padding: 8px 12px;
        .title-button {
          // position: absolute;
          // right: 0;
          // top: 0;
          display: inline-block;
          // padding: 0 8px;
          width: 70px;
          text-align: center;
          line-height: 20px;
          text-align: center;
          background: #ffffff;
          border-radius: 5px;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 12px;
          color: #fff;
          background-color: #d80c1e;
        }
      }
      .operation {
        gap: 5px;
        padding-top: 12px;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        span {
          display: inline-block;
          padding: 0 10px;
          line-height: 25px;
          text-align: center;
          background: #ffffff;
          border-radius: 12px;
          border: 1px solid #cccccc;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 15px;
          color: #333333;
        }
      }
    }
  }
}
</style>
