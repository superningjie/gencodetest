<template>
  <div class="pagePro">
    <!-- <div class="van-popover__arrow"></div> -->
    <div class="dropdownBox">
      <van-dropdown-menu>
        <!-- :title="jobName || '岗位'" -->
        <van-dropdown-item
          v-model="cycleName"
          :title="cycleName"
          @open="showYearPicker = true"
        />
        <van-dropdown-item
          v-model="titleText"
          :title="titleText"
          @open="showFilterPicker = true"
        />
        <van-dropdown-item
          v-model="plan"
          :options="planList"
          @change="search"
        />
      </van-dropdown-menu>
    </div>

    <div class="tabs1">
      <div class="tab1Box">
        <div @click="changeTab(1)" :class="active == 1 ? 'active' : ''">
          绩效制定
        </div>
        <div @click="changeTab(2)" :class="active == 2 ? 'active' : ''">
          绩效评估
        </div>
        <div @click="changeTab(3)" :class="active == 3 ? 'active' : ''">
          绩效确认
        </div>
      </div>
      <div class="echart">
        <div
          v-if="isShow"
          id="eCharts"
          style="width: 100%; height: 320px"
        ></div>
        <xy-empty v-else></xy-empty>
      </div>
      <!-- <div class="tab1List">
        <div class="indexUserInfo">
          <div class="avatar">
            <van-image
              round
              width="32px"
              height="32px"
              :src="empInfo.headSculpture"
            />
          </div>
          <div class="flex tab1ListBox">
            <div class="info">
              <div class="fw-text flex middle">
                <p class="over-text">{{ empInfo.name }}</p>
                <p class="ml8 over-text">{{ empInfo.position }}</p>
              </div>
              <div class="plain-text flex middle">
                <p class="over-text">{{ empInfo.company }}</p>
                <p class="ml8 over-text">{{ empInfo.organization }}</p>
              </div>
              <div class="plain-text flex middle">
                <p class="over-text">2023Q3科技员工考核绩效</p>
              </div>
            </div>
            <div class="tab1Icon">员工确认</div>
          </div>
        </div>
      </div> -->
    </div>
    <boardTree
      :resetShow="true"
      :jobInfo="jobInfo"
      :workPosition="workPosition"
      :showFilterPicker="showFilterPicker"
      @isshowFilterPicker="isshowFilterPicker"
      @clickSearch="clickSearch"
    />
    <van-popup v-model="showYearPicker" round position="bottom">
      <van-picker
        show-toolbar
        :columns="columns"
        @cancel="showYearPicker = false"
        @confirm="onConfirm"
      />
    </van-popup>
  </div>
</template>

<script>
import boardTree from "@/components/boardTree.vue";
import {
  getBoardData,
  getActivity,
  getCycle,
  getOrgBoard,
  setPanlecondition,
  getScreeningCondition,
} from "@/libs/api.js";
export default {
  components: {
    boardTree,
  },
  props: {
    years: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      isShow: false, // 无组织权限则不显示图表
      cycleName: "2024-年度", // 年份周期显示
      titleText: "组织", // 组织默认显示
      plan: "0", // 考核活动默认显示
      // 年份和周期选择器数据
      columns: [
        {
          values: [],
          defaultIndex: 0,
        },
        // 第二列
        {
          values: [],
          defaultIndex: 0,
        },
      ],
      showYearPicker: false, // 年份周期选择弹窗
      active: 1,
      planList: [
        {
          text: "考核活动",
          value: "0",
        },
      ], // 考核活动列表数据
      // 是否已加载完成
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      showWorkPositionFlag: false,
      workPosition: "0",
      showFilterPicker: false,
      empInfo: {},
      options: {
        title: {
          text: "指标制定",
          subtext: "进度",
          left: "center",
          top: "center",
        },
        tooltip: {
          trigger: "item",
        },
        legend: {
          orient: "horizontal",
          top: "top",
        },
        series: [
          {
            type: "pie",
            radius: ["50%", "70%"],
            left: "center",
            data: [],
            label: {
              show: true,
              // padding: [0, -50, 30, -50],
              // lineHeight: 16,
            },
            labelLine: {
              show: true,
              // length: 30, //第一段线长
              // length2: 50, //第二段线长
            },
          },
        ],
      },
      data: {
        type: 1,
        year: "2024",
        cycleName: "年度",
        org: "",
        orgId: "",
        activity: "",
        cycle: "",
      },
      cycleData: {},
      cycleArr: [], // 周期名字集合
      cycleValueArr: [], // 周期id集合
      enactedPieVoList: [], //指标制定分布
      evaluationPieVoList: [], // 绩效评估分布
      resultSurePieVoList: [], //结果确认分布
    };
  },
  created() {
    this.getCycle();
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.getOrgBoard();
    },
    getCycle() {
      getCycle().then((res) => {
        this.cycleData = res.data.data;
        this.cycleArr = this.cycleTransition(res.data.data);
        this.cycleValueArr = Object.keys(res.data.data).map((key) => key);
        this.setCycle();
      });
    },
    // 设置周期数据
    setCycle() {
      this.columns[0].values = this.years;
      this.columns[1].values = this.cycleArr;
    },
    setDefaultCycle() {
      this.columns[1].defaultIndex = this.cycleArr.indexOf("年度");
    },
    // 返回周期名称集合
    cycleTransition(obj) {
      return Object.keys(obj).map((key) => {
        return obj[key];
      });
    },
    keyTransition(obj) {
      return Object.keys(obj).map((key) => {
        return key;
      });
    },
    // 获取用户是否有存储筛选条件
    async getScreeningCondition() {
      getScreeningCondition().then((res) => {
        if (res.data.data == "当前人员没有记录筛选条件!") {
          console.log("当前人员没有记录筛选条件");
          let index = this.cycleArr.indexOf("年度");
          const cycle = this.cycleValueArr[index];
          const data = {
            cycle: cycle,
            year: this.data.year,
            orgId: this.data.orgId,
          };
          this.getActivity(data);
        } else {
          this.data.year = res.data.data.year;
          this.data.cycle = res.data.data.cycle;
          this.data.orgId = this.data.org = res.data.data.org;
          this.titleText = res.data.data.orgName || "组织";
          this.data.activity = res.data.data.activity;
          this.active = res.data.data.kanbantype;
          this.data.type = res.data.data.kanbantype || 1;
          this.plan = res.data.data.activity;
          let index = this.cycleValueArr.indexOf(res.data.data.cycle);
          let cname = index ? this.cycleArr[index] : "年度";
          console.log(index, this.cycleValueArr, res.data.data.cycle, cname);
          this.cycleName = res.data.data.year + "-" + cname;
          this.data.cycleName = cname;
          this.columns[0].defaultIndex = this.columns[0].values.indexOf(
            res.data.data.year
          );
          this.columns[1].defaultIndex = index;
          const data = {
            cycle: res.data.data.cycle,
            year: res.data.data.year,
            orgId: this.data.orgId,
          };
          // console.log(data, "data");
          // return;
          this.getActivity(data);
          // this.getBoardData();
          console.log("当前人员有记录筛选条件");
        }
      });
    },
    // 获取组织权限
    getOrgBoard() {
      const data = {
        orgId: "",
      };
      getOrgBoard(data).then((res) => {
        if (!res.data.data) {
          this.isShow = false;
        } else {
          this.getScreeningCondition();
        }
      });
    },
    // 进度跟踪数据获取
    getActObjId(arr) {
      const ids = arr.map((item) => item.actObjId);
      return ids;
    },
    getBoardData() {
      this.$xy.showLoad();
      this.setPanlecondition();
      getBoardData(this.data).then((res) => {
        this.$xy.hideLoad();
        const data = res.data.data;
        this.enactedPieVoList = data.enactedPieMap;
        this.evaluationPieVoList = data.evaluationPieMap;
        this.resultSurePieVoList = data.resultSurePieMap;
        let name1 = "";
        let name2 = "";
        let name3 = "";
        const name4 = "未启动";

        let totalCount = 0;
        let arr = {};
        if (this.active == 1) {
          arr = this.enactedPieVoList;
          this.options.title.text = "绩效制定";
          name1 = "制定完成";
          name2 = "指标填报中";
          name3 = "指标审批中";
        }
        if (this.active == 2) {
          arr = this.evaluationPieVoList;
          this.options.title.text = "绩效评估";
          name1 = "评估完成";
          name2 = "他人评估中";
          name3 = "自我评估中";
        }
        if (this.active == 3) {
          arr = this.resultSurePieVoList;
          this.options.title.text = "绩效确认";
          name1 = "确认完成";
          name2 = "上级确认中";
          name3 = "员工确认中";
        }

        if (Object.keys(arr).length === 0) {
          this.isShow = false;
          return;
        }

        const arr1 =
          arr.zhidingwancheng || arr.pingguwancheng || arr.querenwancheng || [];
        const arr2 =
          arr.zhibiaotianbaozhong ||
          arr.tarenpingguzhong ||
          arr.shangjiquerenzhong ||
          [];
        const arr3 =
          arr.zhibiaoshenpizhong ||
          arr.ziwopingguzhong ||
          arr.yuangongquerenzhong ||
          [];
        const arr4 = arr.weiqidong || [];
        const num1 = arr1.length || 0;
        const num2 = arr2.length || 0;
        const num3 = arr3.length || 0;
        const num4 = arr4.length || 0;
        totalCount = num1 + num2 + num3 + num4;
        if (totalCount == 0) {
          this.isShow = false;
          return;
        }

        const status1Ids = this.getActObjId(arr1);
        const status2Ids = this.getActObjId(arr2);
        const status3Ids = this.getActObjId(arr3);
        const status4Ids = this.getActObjId(arr4);
        const seriesData = [
          {
            value: num1,
            name: name1,
            // url:'planPerList',
            ids: status1Ids,
            text: ((num1 / totalCount) * 100).toFixed(0) + "%",
            label: {
              formatter: [
                name1,
                `${((num1 / totalCount) * 100).toFixed(2) + "%"}(${num1}人)`,
              ].join("\n"),
            },
            itemStyle: {
              color: "#95DE64",
            },
          },
          {
            value: num2,
            name: name2,
            ids: status2Ids,
            text: ((num2 / totalCount) * 100).toFixed(0) + "%",
            label: {
              formatter: [
                name2,
                `${((num2 / totalCount) * 100).toFixed(2) + "%"}(${num2}人)`,
              ].join("\n"),
            },
            itemStyle: {
              color: "#26C9C4",
            },
          },
          {
            value: num3,
            name: name3,
            ids: status3Ids,
            text: ((num3 / totalCount) * 100).toFixed(0) + "%",
            label: {
              formatter: [
                name3,
                `${((num3 / totalCount) * 100).toFixed(2) + "%"}(${num3}人)`,
              ].join("\n"),
            },
            itemStyle: {
              color: "#1990FF",
            },
          },
          {
            value: num4,
            name: name4,
            ids: status4Ids,
            text: ((num4 / totalCount) * 100).toFixed(0) + "%",
            label: {
              formatter: [
                name4,
                `${((num4 / totalCount) * 100).toFixed(2) + "%"}(${num4}人)`,
              ].join("\n"),
            },
            itemStyle: {
              color: "#F57581",
            },
          },
        ];
        this.isShow = true;
        this.options.series[0].data = seriesData;
        const self = this;
        setTimeout(() => {
          var eCharts = this.$echarts.init(document.getElementById("eCharts"));
          eCharts.setOption(this.options);
          eCharts.on("click", function (param) {
            self.$store.commit("setProgressTrackingIds", param.data.ids);
            self.$router.push({
              path: "planPerList",
            });
            // window.location.href = "/nh5/#/selfhelp/planPerList";
          });
        });
        // enactedPieVoList 指标制定分布 evaluationPieVoList 绩效评估分布 resultSurePieVoList结果确认分布
      });
    },

    // 设置筛选条件的保存
    setPanlecondition() {
      let data = this.data;
      if (data.cycle.length < 5) {
        let index = this.cycleArr.indexOf("年度");
        data.cycle = this.cycleValueArr[index];
      }
      setPanlecondition(data).then((res) => {});
    },
    // 获取考核活动
    async getActivity(data) {
      await getActivity(data).then((res) => {
        if (Object.keys(res.data.data).length != 0) {
          this.planList = this.arrayTransition(res.data.data);
          const obj = {
            text: "不限",
            value: "0",
          };
          this.planList.unshift(obj);
        } else {
          this.planList = [
            {
              text: "考核活动",
              value: "0",
            },
          ];
          this.plan = "0";
          this.data.activity = "";
        }
        this.getBoardData();
      });
    },
    arrayTransition(obj) {
      return Object.keys(obj).map((key) => {
        return {
          text: obj[key],
          value: key,
        };
      });
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    onConfirm(value) {
      this.cycleName = value[0] + "-" + value[1];
      this.data.year = value[0];
      let index = this.cycleArr.indexOf(value[1]);
      const cycleValue = this.cycleValueArr[index];
      this.data.type = this.active;
      this.data.cycle = cycleValue;
      this.data.cycleName = value[1];
      this.showYearPicker = false;
      const data = {
        cycle: cycleValue,
        year: this.data.year,
        orgId: this.data.orgId,
      };
      this.getActivity(data);
    },
    async clickSearch(item) {
      console.log(item, "item");
      this.data.org = this.data.orgId = item.id;
      this.titleText = item.name || "组织";
      this.showFilterPicker = false;
      const data = {
        cycle: this.data.cycle,
        year: this.data.year,
        orgId: this.data.orgId,
      };
      await this.getActivity(data);
      this.getBoardData();
    },
    search(val) {
      this.data.activity = val;
      this.getBoardData();
    },
    changeTab(val) {
      this.active = val;
      this.data.type = val;
      this.getBoardData();
    },
  },
};
</script>

<style lang="less" scoped>
.exColor {
  color: #d80c1e;
}
.pagePro {
  padding: 12px 0;
  background-color: #fff;
  border-radius: 10px;
  // .van-popover__arrow {
  //   left: 12%;
  //   border-bottom-color: currentColor;
  //   transform: translate(-50%, -100%);
  //   color: #fff;
  // }
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
  }
  .dropdownBox {
    padding: 0 12px;
    border-bottom: 1px solid #eee;
  }
}
.tab1Box {
  display: flex;
  margin: 13px 59px;
  background-color: #fbf1f2;
  padding: 1px;
  font-size: 13px;
  // border: 1px solid #d80c1e;
  color: #999;
  div {
    flex: 1;
    text-align: center;
    padding: 8px;
  }
  .active {
    background-color: #fff;
    color: #d80c1e;
  }
}
.tab1List {
  border-top: 0.5px solid #eee;
  .indexUserInfo {
    display: flex;
    padding: 12px;
    .tab1ListBox {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .tab1Icon {
        border: 1px solid greenyellow;
        background-color: greenyellow;
        padding: 4px;
        font-size: 12px;
        color: green;
      }
      .info {
        margin-left: 8px;
        flex: 1;
      }
    }

    .indexDetailBox {
      margin-top: 4px;
      .lightDetailBox {
        margin-left: 12px;
        flex: 1;
        padding: 8px;
        background-color: #fef4f5;
      }
    }
  }
}
</style>
