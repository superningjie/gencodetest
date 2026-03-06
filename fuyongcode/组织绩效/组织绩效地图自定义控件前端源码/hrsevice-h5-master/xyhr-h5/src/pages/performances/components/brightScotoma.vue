<template>
  <div class="pageBright">
    <div class="search">
      <!-- <div class="van-popover__arrow"></div> -->
      <!-- <van-dropdown-menu>
        <van-dropdown-item
          v-model="titleText"
          :title="titleText"
          @open="showFilterPicker = true"
        />
      </van-dropdown-menu> -->
      <div class="tab1Box">
        <div
          class="tab"
          @click="changeTab(1)"
          :class="active == 1 ? 'active' : ''"
        >
          高绩效
        </div>
        <div
          class="tab"
          @click="changeTab(2)"
          :class="active == 2 ? 'active' : ''"
        >
          低绩效
        </div>
      </div>
    </div>
    <div class="content pd12">
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">
            {{
              active == 1 ? "连续两年AA绩效员工人数" : "连续两年B以下员工人数"
            }}
          </div>
        </div>
        <div id="echarts" style="width: 100%; height: 300px"></div>
      </div>
      <!-- <div class="listBox">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">
            {{
              active == 1 ? "连续两年AA绩效员工人数" : "连续两年B以下员工人数"
            }}
          </div>
        </div>
        <div class="list" v-for="(i, index) in list">
          <div>{{ index + 1 }}</div>
          <div>{{ i.orgName }}</div>
          <div>{{ i.peopleCount }}</div>
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
  </div>
</template>

<script>
import { getPerformanceTrendAnalytics } from "@/libs/api.js";
import boardTree from "@/components/boardTree";

export default {
  components: {
    boardTree,
  },
  data() {
    return {
      titleText: "组织",
      showFilterPicker: false,
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      workPosition: "0",
      active: 1,
      list: [],
      data: {
        org: "",
        year: 2024,
        type: "first", //first--高;second--低
      },
      option: {
        grid: {
          left: "0%",
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
    this.initData();
  },
  methods: {
    initData() {
      const data = this.data;
      data.org = this.$store.state.userData.pkOrg;

      this.getPerformanceTrendAnalytics(data);
    },
    getPerformanceTrendAnalytics(data) {
      this.$xy.showLoad();
      getPerformanceTrendAnalytics(data).then((res) => {
        this.$xy.hideLoad();
        const compareDataListVOList = res.data.data.compareDataListVOList;
        this.list = compareDataListVOList;
        const countList = compareDataListVOList.map(
          (element) => element.peopleCount
        );

        const orgNameList = compareDataListVOList.map(
          (element) => element.orgName
        );
        this.option.xAxis.data = orgNameList;
        this.option.series[0].data = countList;
        this.option.dataZoom[0].end = orgNameList.length;
        setTimeout(() => {
          this.initChart();
        });
      });
    },
    initChart() {
      var echarts = this.$echarts.init(document.getElementById("echarts"));
      echarts.setOption(this.option);
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    clickSearch(item) {
      console.log(item, "item");
      this.data.org = item.id;
      this.titleText = item.name || "组织";
      this.getPerformanceTrendAnalytics(this.data);
      this.showFilterPicker = false;
    },
    changeTab(val) {
      this.active = val;
      this.data.type = val == 1 ? "first" : "second";
      this.getPerformanceTrendAnalytics(this.data);
    },
  },
};
</script>

<style lang="less" scoped>
.pageBright {
  padding: 12px 0;
  background-color: #fff;
  border-radius: 10px;
  .van-popover__arrow {
    right: 8%;
    border-bottom-color: currentColor;
    transform: translate(-50%, -100%);
    color: #fff;
  }
}
.exColor {
  color: #5287da;
}
// .echart {
//   padding: 12px;
// }
.content {
  .listBox {
    .list {
      display: flex;
      padding: 12px;
      justify-content: space-between;
    }
  }
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
    border-bottom: 1px solid #5287da;
  }
}
</style>
