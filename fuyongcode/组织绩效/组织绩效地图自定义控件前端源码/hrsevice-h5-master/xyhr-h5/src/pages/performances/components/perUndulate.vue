<template>
  <div class="pagePer">
    <div class="dropdownBox">
      <!-- <div class="van-popover__arrow"></div> -->
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
      <van-collapse v-model="activeNames" v-if="true">
        <van-collapse-item
          name="1"
          title="提升2级或以上"
          icon="shop-o"
          :value="pageData.upTwo.count"
          :disabled="pageData.upTwo.count == 0"
        >
          <!-- <template #title>
            <div><van-icon name="question-o" />提升2级或以上</div>
          </template> -->
          <!-- <div class="tableBox">
            <div class="thBox">
              <div class="th">员工</div>
              <div class="th">岗位</div>
              <div class="th">等级</div>
            </div>
            <div class="tdBox" v-for="i in pageData.upTwo.personInfo">
              <div class="td">
                <div class="avatar">
                  <van-image
                    round
                    width="24px"
                    height="24px"
                    src="https://img01.yzcdn.cn/vant/cat.jpeg"
                  />{{ i.personName }}
                </div>
              </div>
              <div class="td">{{ i.position }}</div>
              <div class="td">{{ i.changeLevelTxt }}</div>
            </div>
          </div> -->
          <PersonInfoList :list="pageData.upTwo.personInfo" />
        </van-collapse-item>
        <van-collapse-item
          title="提升1级"
          name="2"
          icon="shop-o"
          :value="pageData.upOne.count"
          :disabled="pageData.upOne.count == 0"
        >
          <PersonInfoList :list="pageData.upOne.personInfo" />
        </van-collapse-item>
        <van-collapse-item
          title="下降1级"
          name="3"
          icon="shop-o"
          :value="pageData.downOne.count"
          :disabled="pageData.downOne.count == 0"
        >
          <PersonInfoList :list="pageData.downOne.personInfo" />
        </van-collapse-item>
        <van-collapse-item
          title="下级2级或以上"
          name="4"
          icon="shop-o"
          :value="pageData.downTwo.count"
          :disabled="pageData.downTwo.count == 0"
        >
          <PersonInfoList :list="pageData.downTwo.personInfo" />
        </van-collapse-item>
      </van-collapse>
      <div class="perBox" v-else>
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">绩效波动统计</div>
        </div>
        <div class="perContentBox">
          <div class="upBox">
            <div class="box">
              <div class="boxTop">
                <van-image width="10" height="10" :src="UP2" />
                <div class="perTitle">提升2级或以上</div>
                <van-icon name="arrow"></van-icon>
              </div>
              <div class="perNumber">{{ pageData.upTwo.count }}人</div>
            </div>
            <div class="box ml12">
              <div class="boxTop">
                <van-image width="10" height="10" :src="UP1" />
                <div class="perTitle">提升1级</div>
                <van-icon name="arrow"></van-icon>
              </div>
              <div class="perNumber">{{ pageData.upOne.count }}人</div>
            </div>
          </div>
          <div class="downBox">
            <div class="box">
              <div class="boxTop">
                <van-image width="10" height="10" :src="DOWN1" />
                <div class="perTitle">下降2级或以上</div>
                <van-icon name="arrow"></van-icon>
              </div>
              <div class="perNumber">{{ pageData.downTwo.count }}人</div>
            </div>
            <div class="box ml12">
              <div class="boxTop">
                <van-image width="10" height="10" :src="DOWN2" />
                <div class="perTitle">下降1级</div>
                <van-icon name="arrow"></van-icon>
              </div>
              <div class="perNumber">{{ pageData.downOne.count }}人</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="dividerBox"></div>
    <div class="content pd12">
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">连续两年AA及以上员工人数</div>
        </div>
        <div id="echarts" style="width: 100%; height: 300px"></div>
      </div>
      <XyDivider />
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">连续两年B及以下员工人数</div>
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
        :columns="yearColumns"
        @cancel="showYearPicker = false"
        @confirm="onConfirm"
      />
    </van-popup>
  </div>
</template>

<script>
import {
  getFluctuate,
  getDistributedScreen,
  setDistributedScreen,
  getPerformanceTrendAnalytics,
} from "@/libs/api.js";
import brightScotoma from "@/pages/performances/components/brightScotoma.vue";

import boardTree from "@/components/boardTree";
import PersonInfoList from "@/pages/performances/components/PersonInfoList.vue";
import UP1 from "@/assets/performances/up1.png";
import UP2 from "@/assets/performances/up2.png";
import DOWN1 from "@/assets/performances/down1.png";
import DOWN2 from "@/assets/performances/down2.png";

export default {
  components: {
    boardTree,
    PersonInfoList,
    brightScotoma,
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
      UP1,
      UP2,
      DOWN1,
      DOWN2,
      cycleName: "2024-年度",
      year: 2024,
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
      yearColumns: [
        {
          values: ["2020", "2021", "2022", "2023", "2024"],
          defaultIndex: 4,
        },
      ],
      showYearPicker: false,
      titleText: "组织",
      showFilterPicker: false,
      activeNames: [],
      data: {
        year: "2024", //年份
        // period: "", //周期编码
        company: "", //部门ID
      },
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      showWorkPositionFlag: false,
      workPosition: "0",
      pageData: {
        downTwo: {
          count: 0,
          personInfo: [],
        },
        upTwo: {
          count: 0,
          personInfo: [],
        },
        downOne: {
          count: 0,
          personInfo: [],
        },
        upOne: {
          count: 0,
          personInfo: [],
        },
      },
      cycleArr: [],
      cycleValueArr: [],
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
      option: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          left: "0%",
          right: "4%",
          top: "10%",
          bottom: "2%",
          containLabel: true,
        },
        dataZoom: [
          {
            show: false,
            start: 50, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            type: "inside",
            start: 50, //滚动条开始位置
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
          axisLine: {
            show: false,
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
            interval: 0,
            rotate: 45,
          },
        },
        series: [
          {
            type: "bar",
            data: [],
          },
        ],
      },
      option1: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          left: "0%",
          right: "4%",
          top: "10%",
          containLabel: true,
        },
        dataZoom: [
          {
            show: false,
            start: 50, //滚动条开始位置
            end: 100, //滚动条结束位置
          },
          {
            type: "inside",
            start: 50, //滚动条开始位置
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
          axisLine: {
            show: false,
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
            interval: 0,
            rotate: 45,
          },
        },
        series: [
          {
            type: "bar",
            data: [],
          },
        ],
      },
    };
  },
  mounted() {
    this.setCycle();
    this.getDistributedScreen();
  },
  methods: {
    setCycle() {
      this.yearColumns[0].values = this.columns[0].values = this.years;
      const cycleArr = this.cycleTransition(this.cycleData);
      this.cycleArr = cycleArr;
      this.columns[1].values = cycleArr;
      let index = cycleArr.indexOf("年度");
      this.columns[1].defaultIndex = index;
      this.cycleValueArr = Object.keys(this.cycleData).map((key) => key);
      this.filterData.cycle = this.cycleValueArr[index];
    },
    getDistributedScreen() {
      getDistributedScreen().then((res) => {
        if (res.data.data == "等级分布筛选条件为空!") {
          this.search();
        } else {
          this.filterData = res.data.data;

          this.data.company = this.filterData.org;
          this.data.year = this.filterData.year;
          // this.data.period = this.filterData.cycle;
          this.titleText = this.filterData.orgName || "组织";
          let index = this.cycleValueArr.indexOf(res.data.data.cycle);
          this.yearColumns[0].defaultIndex = this.columns[0].defaultIndex =
            this.columns[0].values.indexOf(res.data.data.year);
          this.columns[1].defaultIndex = index;
          let cycleName = this.cycleArr[index];
          this.cycleName = this.data.year;
          this.saveData.orgqueryscope = Object.keys(
            res.data.data.orgqueryscope
          );
          this.saveData.posqueryscope = Object.keys(
            res.data.data.posqueryscope
          );
          this.saveData.org = this.filterData.org;
          this.saveData.year = this.filterData.year;
          this.saveData.cycle = this.filterData.cycle;

          this.getFluctuate(this.data);
          // const firstData = {
          //   org: "" || this.$store.state.userData.pkOrg,
          //   year: this.filterData.year,
          //   type: "first",
          // };

          // this.getPerformanceTrendAnalytics(firstData);
          const secondData = {
            org: this.filterData.org || this.$store.state.userData.pkOrg,
            year: this.filterData.year,
            type: "second",
          };
          if (this.filterData.org == "0") {
            secondData.org = "";
          }
          this.getPerformanceTrendAnalytics();
        }
      });
    },
    setDistributedScreen() {
      setDistributedScreen(this.saveData).then((res) => {});
    },
    getPerformanceTrendAnalytics() {
      const secondData = {
        org: this.data.company,
        year: this.data.year,
        type: "second",
      };
      if (this.filterData.org == "0") {
        secondData.org = this.$store.state.userData.pkOrg;
      }
      this.$xy.showLoad();
      getPerformanceTrendAnalytics(secondData).then((res) => {
        this.$xy.hideLoad();
        const firstDataList = res.data.data.firstData.compareDataListVOList;
        const countList = firstDataList.map((element) => element.peopleCount);
        const orgNameList = firstDataList.map((element) => element.orgName);
        this.option.xAxis.data = orgNameList;
        this.option.series[0].data = countList;
        this.option.dataZoom[0].end = orgNameList.length;

        const secondDataList = res.data.data.secondData.compareDataListVOList;
        const secondCountList = secondDataList.map(
          (element) => element.peopleCount
        );
        const secondOrgNameList = secondDataList.map(
          (element) => element.orgName
        );
        this.option1.xAxis.data = secondOrgNameList;
        this.option1.series[0].data = secondCountList;
        this.option1.dataZoom[0].end = secondOrgNameList.length;

        setTimeout(() => {
          this.initChart();
        });
      });
    },
    initChart() {
      var echarts = this.$echarts.init(document.getElementById("echarts"));
      echarts.setOption(this.option);
      var echarts1 = this.$echarts.init(document.getElementById("echarts1"));
      echarts1.setOption(this.option1);
    },
    cycleTransition(obj) {
      return Object.keys(obj).map((key) => {
        return obj[key];
      });
    },
    search() {
      this.setCycle();
      this.getFluctuate(this.data);
    },
    getFluctuate(data) {
      this.$xy.showLoad();
      getFluctuate(data).then((res) => {
        this.pageData = res.data.data;
        this.$xy.hideLoad();
      });
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    onConfirm(value) {
      this.cycleName = value.join("");
      this.saveData.year = this.data.year = value[0];
      // const index = this.cycleArr.indexOf(value[1]);
      this.saveData.cycle = this.filterData.cycle;
      this.showYearPicker = false;
      const data = this.data;
      this.getFluctuate(data);
      this.setDistributedScreen();
      this.getPerformanceTrendAnalytics();
    },
    clickSearch(item, index) {
      this.showFilterPicker = false;
      this.saveData.org = this.data.company = this.data.org = item.id;
      this.titleText = item.name;
      const data = this.data;
      this.getFluctuate(data);
      this.setDistributedScreen();
      this.getPerformanceTrendAnalytics();
    },
  },
};
</script>

<style lang="less" scoped>
.pagePer {
  padding: 12px 0;
  background-color: #fff;
  border-radius: 10px;
  .van-popover__arrow {
    right: 28%;
    border-bottom-color: currentColor;
    transform: translate(-50%, -100%);
    color: #fff;
  }
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
  }
  .dropdownBox {
    padding: 0 12px;
    border-bottom: 1px solid #eee;
  }
}
.exColor {
  color: #d80c1e;
}
.content {
  padding: 12px;
  .tableBox {
    width: 100%;
    text-align: left;
    .thBox {
      display: flex;
      border: 1px solid #ccc;
      // background-color: #f2f2f2;
      .th:nth-child(3) {
        border-right: none;
      }
      .th {
        font-weight: bold;
        flex: 1;
        padding: 12px;
        border-right: 1px solid #ccc;
      }
    }
    .tdBox {
      display: flex;
      border-bottom: 1px solid #ccc;
      border-left: 1px solid #ccc;
      .avatar {
        display: flex;
        align-items: center;
      }
      .td {
        flex: 1;
        padding: 12px;
        border-right: 1px solid #ccc;
      }
    }
  }
  .dividerBox {
    width: 100%;
    height: 12px;
    background-color: #f2f2f2;
  }
  .perBox {
    .perContentBox {
      .upBox {
        display: flex;
      }
      .downBox {
        display: flex;
      }
      .box {
        margin-top: 12px;
        padding: 12px;
        background: #f4f8fb;
        border-radius: 4px;
        flex: 1;
        .boxTop {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #999;
          .perTitle {
            flex: 1;
            margin-left: 6px;
          }
        }
        .perNumber {
          padding: 12px;
          padding-bottom: 0;
        }
      }
      .ml12 {
        margin-left: 12px;
      }
    }
  }
}
</style>
