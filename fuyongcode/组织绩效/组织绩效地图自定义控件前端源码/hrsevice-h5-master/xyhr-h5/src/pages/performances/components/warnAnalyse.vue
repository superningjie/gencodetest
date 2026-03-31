<template>
  <div class="pageWarn">
    <!-- <div class="van-popover__arrow"></div> -->
    <div class="search">
      <van-dropdown-menu>
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
      </van-dropdown-menu>
    </div>
    <div class="content">
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">预警指标统计</div>
        </div>
        <div id="echarts" style="width: 100%; height: 300px"></div>
      </div>
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">预警人数统计</div>
        </div>
        <div id="echarts1" style="width: 100%; height: 300px"></div>
      </div>
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
import boardTree from "@/components/boardTree";
import {
  getDistributedScreen,
  setDistributedScreen,
  getStatisticsCycleWarn,
  getStatisticsOrgWarn,
} from "@/libs/api.js";
export default {
  components: {
    boardTree,
  },
  props: {
    cycleData: {
      type: Object,
      default: () => {
        return {};
      },
    },
    years: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      cycleName: "2024-年度",
      showYearPicker: false,
      columns: [
        {
          values: ["2020", "2021", "2022", "2023", "2024"],
          defaultIndex: 4,
        },
        // 第二列
        {
          values: [],
          defaultIndex: 0,
        },
      ],
      titleText: "组织",
      showFilterPicker: false,
      data: {
        orgId: "",
        assessedYear: 2024,
        cycle: "1570349099625117696",
        // radioValue: "1", // 统计类型（“1”：指标；”2”：人数）
      },
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      showWorkPositionFlag: false,
      workPosition: "0",
      showCyclePicker: false,
      option: {
        legend: {
          bottom: 10,
        },
        tooltip: {},
        grid: {
          left: "3%",
          right: "4%",
          top: "10%",
          containLabel: true,
        },
        dataZoom: [
          {
            show: false,
            start: 0, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            type: "inside",
            start: 0, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            show: false,
            yAxisIndex: 0,
            filterMode: "empty",
            width: 30,
            height: "80%",
            showDataShadow: false,
            left: "93%",
          },
        ],
        yAxis: {
          type: "value",
          max: 100,
          axisLine: {
            show: false,
          },
          axisLabel: {
            formatter: function (value) {
              return value + "%";
            },
          },
          axisTick: {
            show: false,
          },
        },
        xAxis: {
          type: "category",
          data: [],
          xAxisIndex: 1,
          yAxisIndex: 1,
          axisTick: {
            show: false,
          },
          axisLabel: {
            //坐标轴刻度标签的显示间隔，默认会采用标签不重叠的策略间隔显示标签,可以设置成 0 强制显示所有标签。
            interval: 0,
            rotate: 45,
          },
        },
        series: [
          {
            name: "红灯指标占比",
            type: "bar",
            data: [],
            label: {
              show: true,
              position: "top",
            },
            itemStyle: {
              color: "#DD1F3B",
            },
          },
          {
            name: "黄灯指标占比",
            type: "bar",
            label: {
              show: true,
              position: "top",
            },
            data: [],
            itemStyle: {
              color: "#F7B56D",
            },
          },
        ],
      },
      option1: {
        legend: {
          bottom: 10,
        },
        tooltip: {},
        dataZoom: [
          {
            show: false,
            start: 0, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            type: "inside",
            start: 0, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            show: false,
            yAxisIndex: 0,
            filterMode: "empty",
            width: 30,
            // height: "80%",
            showDataShadow: false,
            // left: "93%",
          },
        ],
        grid: {
          left: "3%",
          right: "4%",
          top: "10%",
          containLabel: true,
        },
        yAxis: {
          type: "value",
          max: 100,
          axisLine: {
            show: false,
          },
          axisTick: {
            show: false,
          },
          axisLabel: {
            formatter: function (value) {
              return value + "%";
            },
          },
        },
        xAxis: {
          type: "category",
          data: [],
          xAxisIndex: 1,
          yAxisIndex: 1,
          axisTick: {
            show: false,
          },
          axisLabel: {
            //坐标轴刻度标签的显示间隔，默认会采用标签不重叠的策略间隔显示标签,可以设置成 0 强制显示所有标签。
            interval: 0,
            rotate: 45,
          },
        },
        series: [
          {
            name: "红灯人数占比",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#DD1F3B",
            },
            label: {
              show: true,
              position: "top",
            },
          },
          {
            name: "黄灯人数占比",
            type: "bar",
            data: [],
            itemStyle: {
              color: "#F7B56D",
            },
            label: {
              show: true,
              position: "top",
            },
          },
        ],
      },
      cycleArr: [],
      cycleKeyList: [],
      // 保存的筛选条件数据
      saveData: {
        orgqueryscope: [],
        posqueryscope: [],
        year: "",
        cycle: "",
        org: "",
      },
      // 获取的筛选条件
      filterData: {
        posqueryscope: {},
        orgName: "",
        year: "",
        org: "",
        orgqueryscope: {},
        cycle: "",
      },
    };
  },
  mounted() {
    this.setCycle();
    this.getDistributedScreen();
    // this.initData();
  },
  methods: {
    initData() {
      const yearIndex = this.columns[0].defaultIndex;
      const year = this.columns[0].values[yearIndex];
      const cycleIndex = this.columns[1].defaultIndex;
      const cycle = this.cycleKeyList[cycleIndex];
      const data = {
        orgId: this.data.orgId,
        assessedYear: year,
        cycle: cycle,
      };
      console.log(data, "data");
      this.getStatisticsCycleWarn(data);
    },
    getDistributedScreen() {
      getDistributedScreen().then((res) => {
        if (res.data.data == "等级分布筛选条件为空!") {
          console.log("默认值查询");
          this.initData();
        } else {
          this.filterData = res.data.data;
          this.data.orgId = this.filterData.org;
          this.data.assessedYear = this.filterData.year;

          this.data.cycle = this.filterData.cycle;
          //    == "0"
          //     ? this.cycleArr.indexOf("年度")
          //     : this.filterData.cycle;
          // console.log(this.data.cycle, "  this.data.cycle");
          // if (this.data.cycle == "0") {
          //   console.log(this.columns[1].values);

          //   this.data.cycle = this.columns[1].values[index];
          // }
          if (this.data.cycle == "0") {
            const index = this.cycleArr.indexOf("年度");
            console.log(this.cycleArr, "this.cycleArr", this.cycleKeyList);
            this.data.cycle = this.cycleKeyList[index];
          }

          this.titleText = this.filterData.orgName || "组织";
          let index = this.cycleKeyList.indexOf(this.data.cycle);
          this.columns[0].defaultIndex = this.columns[0].values.indexOf(
            res.data.data.year
          );
          this.columns[1].defaultIndex = index;

          let cycleName = this.cycleArr[index] || "年度";
          this.cycleName = this.data.assessedYear + "-" + cycleName;
          this.saveData.orgqueryscope = Object.keys(
            res.data.data.orgqueryscope
          );
          this.saveData.posqueryscope = Object.keys(
            res.data.data.posqueryscope
          );
          this.saveData.org = this.filterData.org;
          this.saveData.year = this.filterData.year;
          this.saveData.cycle = this.filterData.cycle;

          this.getStatisticsCycleWarn(this.data);
        }
      });
    },
    setDistributedScreen() {
      setDistributedScreen(this.saveData).then((res) => {});
    },
    setCycle() {
      this.columns[0].values = this.years;
      const cycleArr = this.cycleTransition(this.cycleData);
      this.cycleArr = cycleArr;
      this.cycleKeyList = Object.keys(this.cycleData).map((key) => {
        return key;
      });
      this.columns[1].values = cycleArr;
      let index = cycleArr.indexOf("年度");
      this.columns[1].defaultIndex = index;
    },
    cycleTransition(obj) {
      return Object.keys(obj).map((key) => {
        return obj[key];
      });
    },
    onConfirm(value) {
      this.cycleName = value[0] + "-" + value[1];
      this.saveData.year = this.data.assessedYear = value[0];
      // this.data.cycleName = this.cycleData[value[1]];
      const index = this.cycleArr.indexOf(value[1]);
      this.saveData.cycle = this.data.cycle = this.cycleKeyList[index];
      this.showYearPicker = false;
      const data = this.data;
      console.log(data, "data");
      this.getStatisticsCycleWarn(data);
      this.setDistributedScreen();
    },
    arrayTransition(obj) {
      return Object.keys(obj).map((key) => {
        return key;
      });
    },
    getStatisticsCycleWarn(data) {
      this.$xy.showLoad();

      getStatisticsCycleWarn(data).then((res) => {
        this.$xy.hideLoad();
        const list = res.data.data;
        let yearCycleList = list.map((item) => item.yearCycle);
        let redIndexPercent = list.map((item) => item.redIndexPercent); // 红灯指标占比
        let yellowIndexPercent = list.map((item) => item.yellowIndexPercent); // 黄灯指标占比

        let redPersonPercent = list.map((item) => item.redPersonPercent); // 红灯人数占比
        let yellowPersonPercent = list.map((item) => item.yellowPersonPercent); // 黄灯人数占比

        this.option.xAxis.data = yearCycleList;
        this.option.series[0].data = redIndexPercent;
        this.option.series[1].data = yellowIndexPercent;

        this.option1.xAxis.data = yearCycleList;
        this.option1.series[0].data = redPersonPercent;
        this.option1.series[1].data = yellowPersonPercent;

        setTimeout(() => {
          var echarts = this.$echarts.init(document.getElementById("echarts"));
          echarts.setOption(this.option);
          var echarts1 = this.$echarts.init(
            document.getElementById("echarts1")
          );
          echarts1.setOption(this.option1);
        });
      });
    },
    getStatisticsOrgWarn() {
      const data = this.data;
      getStatisticsOrgWarn(data).then((res) => {
        const obj = res.data.data;
        let quarterArray = [];
        let redTotalArray = [];
        let yellowTotalArray = [];

        for (let key in obj) {
          quarterArray.push(key);
          redTotalArray.push(obj[key].redTotal);
          yellowTotalArray.push(obj[key].yellowTotal);
        }

        this.option1.xAxis.data = quarterArray;
        this.option1.series[0].data = redTotalArray;
        this.option1.series[1].data = yellowTotalArray;
        var echarts1 = this.$echarts.init(document.getElementById("echarts1"));
        echarts1.setOption(this.option1);
      });
    },
    changeData() {},
    // arr(obj) {
    //   let quarterArray = [];
    //   let redTotalArray = [];
    //   let yellowTotalArray = [];

    //   for (let key in obj) {
    //     quarterArray.push(key);
    //     redTotalArray.push(obj[key].redTotal);
    //     yellowTotalArray.push(obj[key].yellowTotal);
    //   }
    // },
    onConfirmYear(value) {
      this.data.assessedYear = value;
      this.showYearPicker = false;
    },
    clickSearch(item) {
      console.log(item, "item");
      this.titleText = item.name;
      this.saveData.org = this.data.orgId = item.id;

      this.showFilterPicker = false;
      this.getStatisticsCycleWarn(this.data);
      this.setDistributedScreen();
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    onConfirmCycle(value) {
      // this.data.cycleName = value;
      this.showCyclePicker = false;
    },
    changeTab(val) {
      this.active = val;
      this.initData();
    },
  },
};
</script>

<style lang="less" scoped>
.pageWarn {
  border-radius: 10px;
  background-color: #fff;
  padding: 10px 0;
  .van-popover__arrow {
    left: 50%;
    border-bottom-color: currentColor;
    transform: translate(-50%, -100%);
    color: #fff;
  }
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
    border-bottom: 1px solid #eee;
  }
}
.search {
  background-color: #fff;
  padding: 0 12px;
  border-bottom: 1px solid #eee;
  .select {
    display: flex;
    justify-content: space-between;
  }
}
.content {
  background-color: #fff;
  .select {
    padding: 12px;
    display: flex;
    justify-content: space-between;
    .selectYear {
      // background-color: #f2f2f2;
      .van-cell {
        background-color: #f2f2f2;
      }
    }
  }
}
.echart {
  padding: 12px;
}
.tab1Box {
  display: flex;
  justify-content: space-between;
  border-bottom: 0.5px solid #ccc;
  .tab {
    flex: 1;
    text-align: center;
    padding: 8px;
  }
  .active {
    border-bottom: 1px solid #d80c1e;
  }
}
</style>
