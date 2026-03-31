<template>
  <div class="pageRanked">
    <!-- <div class="van-popover__arrow"></div> -->
    <div class="dropdownBox">
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
    <div>
      <div class="echart">
        <div class="title_box">
          <div class="title_line"></div>
          <div class="title_text">整体分布</div>
        </div>
        <div
          v-if="isShow"
          id="echarts"
          style="width: 100%; height: 300px"
        ></div>
        <xy-empty v-else></xy-empty>
      </div>
      <XyDivider />
      <div class="echart">
        <div class="organizationBox">
          <div class="title_box">
            <div class="title_line"></div>
            <div class="title_text">组织对比</div>
          </div>
          <div class="organizationSelect" @click="orgSelect">
            <div class="orgText nowrap">
              {{ orgText }}
            </div>
            <van-icon name="search" />
          </div>
        </div>
        <div
          id="echarts1"
          style="width: 100%; height: 300px"
          v-if="isShowOrg"
        ></div>
        <xy-empty description="选择组织进行对比" v-else></xy-empty>
      </div>
      <XyDivider />
      <div class="echart">
        <div class="organizationBox">
          <div class="title_box">
            <div class="title_line"></div>
            <div class="title_text">职层对比</div>
          </div>
          <div class="organizationSelect" @click="posSelect">
            <div class="nowrap">
              {{ posText }}
            </div>
            <van-icon name="search" />
          </div>
        </div>
        <div
          id="echarts2"
          style="width: 100%; height: 300px"
          v-if="isShowPos"
        ></div>
        <xy-empty description="选择职层进行对比" v-else></xy-empty>
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
    <van-dialog
      @confirm="confirm"
      v-model="showPosFilterPicker"
      showCancelButton
    >
      <div class="filterPrickTitle">
        <div class="titleBox">职层选择</div>
        <div class="resetBox" @click="resetPos">重置</div>
      </div>
      <div class="filterPrick">
        <van-checkbox-group v-model="result">
          <van-cell
            v-for="item in posList"
            :key="item.value"
            :title="item.text"
          >
            <template #right-icon>
              <van-checkbox
                :name="item.value"
                ref="checkboxes"
                checked-color="#ee0a24"
              />
            </template>
          </van-cell>
        </van-checkbox-group>
      </div>
    </van-dialog>
    <van-dialog
      @confirm="confirmOrg"
      v-model="showOrgFilterPicker"
      showCancelButton
    >
      <div class="filterPrickTitle">
        <div class="titleBox">组织选择</div>
        <div class="resetBox" @click="resetOrg">重置</div>
      </div>
      <div class="filterPrick" v-if="orgsTreeData.length">
        <van-checkbox-group v-model="resultOrg">
          <div v-for="item in orgsTreeData">
            <van-collapse v-model="activeNames">
              <van-collapse-item :title="item.name" :name="item.id">
                <template #right-icon>
                  <div @click.stop>
                    <van-checkbox
                      shape="square"
                      :name="item.id"
                      ref="checkboxes"
                      checked-color="#ee0a24"
                    />
                  </div>
                </template>
                <orgTreeChild :childrenList="item.children" />
              </van-collapse-item>
            </van-collapse>
          </div>
        </van-checkbox-group>
      </div>
      <xy-empty v-else></xy-empty>
    </van-dialog>
  </div>
</template>

<script>
import {
  getAllDistribution,
  getDistributedScreen,
  setDistributedScreen,
  getCycle,
} from "@/libs/api.js";
import boardTree from "@/components/boardTree";
import orgTreeChild from "@/pages/performances/components/orgTreeChild.vue";

export default {
  name: "RankedDistribution",
  components: {
    boardTree,
    orgTreeChild,
  },
  props: {
    posList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    orgsTreeData: {
      type: Array,
      default: () => {
        return [];
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
      cycleData: {},
      titleText: "组织",
      showYearPicker: false,
      showOrgFilterPicker: false,
      showPosFilterPicker: false,
      cycleName: "2024-年度",
      orgText: "组织选择",
      posText: "职层选择",
      result: [],
      resultOrg: [],
      activeNames: [],
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      showWorkPositionFlag: false,
      workPosition: "0",
      showFilterPicker: false,
      isShow: false,
      isShowOrg: false,
      isShowPos: false,
      cycleArr: [],
      cycleKeyList: [],
      // 默认查询请求体
      data: {
        queryScope: [],
        queryOrgScope: [],
        queryPosScope: [],
        year: new Date().getFullYear(),
        cycleName: "年度",
        type: "all", // 整体(all)、组织(org)、职层(pos))
        org: "",
      },
      // 保存的筛选条件数据
      saveData: {
        orgqueryscope: [],
        posqueryscope: [],
        year: "",
        cycle: "",
        org: "",
      },
      // 过滤条件的请求体
      filterData: {
        orgqueryscope: [],
        posqueryscope: [],
        year: "",
        cycle: "",
        org: "",
      },
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

      options: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "cross",
            crossStyle: {
              color: "#999",
            },
          },
        },
        legend: {
          data: ["员工人数"],
          bottom: 10,
        },
        grid: {
          left: "3%",
          right: "4%",
          top: "10%",
          containLabel: true,
        },
        xAxis: [
          {
            type: "category",
            data: [],
            axisPointer: {
              type: "shadow",
            },
          },
        ],
        yAxis: [
          {
            type: "value",
            axisLabel: {
              formatter: "{value}",
            },
            axisLine: {
              show: false,
            },
            axisTick: {
              show: false,
            },
          },
        ],
        series: [
          {
            name: "员工人数",
            type: "bar",
            tooltip: {
              valueFormatter: function (value) {
                return value;
              },
            },
            data: [],
            barWidth: "30%",
            // itemStyle: {
            //   color: "#000",
            // },
          },
        ],
      },
      options1: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        legend: {
          bottom: 10,
        },
        grid: {
          left: "3%",
          right: "4%",
          top: "10%",
          containLabel: true,
        },
        yAxis: {
          type: "value",
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
          },
          max: 100,
          axisLabel: {
            formatter: function (value) {
              return value + "%";
            },
          },
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
        xAxis: {
          type: "category",
          data: [],
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
            name: "AAA",
            type: "bar",
            stack: "total",
            emphasis: {
              focus: "series",
            },
            data: [320, 302, 301, 334, 390, 330, 320],
            barWidth: "30%",
          },
        ],
      },
      options2: {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            // Use axis to trigger tooltip
            type: "shadow", // 'shadow' as default; can also be 'line' or 'shadow'
          },
        },
        dataZoom: [
          {
            // type: "slider",
            yAxisIndex: 0, // 指定 y 轴索引
            start: 0, // 滚动条开始位置
            end: 100, // 滚动条结束位置
            show: false,
          },
          {
            type: "inside",
            yAxisIndex: 0, // 指定 y 轴索引
            start: 0, // 滚动条开始位置
            end: 100, // 滚动条结束位置
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
        legend: {
          bottom: 10,
          // itemStyle: {
          //   color: [
          //     "#73A0FA",
          //     "#45DAD1",
          //     "#F59922",
          //     "#73D13D",
          //     "#EC808D",
          //     "#7F7F7F",
          //   ],
          // },
        },
        grid: {
          left: "3%",
          right: "5%",
          top: "10%",
          containLabel: true,
        },
        xAxis: {
          type: "value",
          axisLine: {
            show: false,
          },
          axisTick: {
            show: false,
          },
          max: 100,
          axisLabel: {
            formatter: function (value) {
              return value + "%";
            },
          },
        },
        yAxis: {
          type: "category",
          data: [],
          axisTick: {
            show: false,
          },
        },
        series: [],
      },
    };
  },
  created() {
    this.getCycle();
  },
  mounted() {
    this.getDistributedScreen();
  },
  methods: {
    changeOrg(v) {
      console.log(v, "v");
    },
    changeCollapse(e) {
      console.log(e, "e");
    },
    getCycle() {
      getCycle().then((res) => {
        this.cycleData = res.data.data;
        this.setCycle();
      });
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
    resetPos() {
      this.result = [];
    },
    resetOrg() {
      this.resultOrg = [];
    },
    getDistributedScreen() {
      getDistributedScreen().then((res) => {
        if (res.data.data == "等级分布筛选条件为空!") {
          console.log("默认值查询");
          const data = this.data;
          this.getAllDistribution(data, "all");
        } else {
          // this.data = res.data.data
          this.data.cycle = res.data.data.cycle;
          let index = this.cycleKeyList.indexOf(res.data.data.cycle);
          this.data.cycleName = this.cycleArr[index] || "年度";
          this.columns[0].defaultIndex = this.columns[0].values.indexOf(
            res.data.data.year
          );
          this.columns[1].defaultIndex = index;
          console.log(this.cycleName, "cycleName");
          this.data.org = res.data.data.org;
          this.titleText = res.data.data.orgName;
          this.data.year =
            res.data.data.year == "0" ? "2024" : res.data.data.year;
          this.cycleName = this.data.year + "-" + this.data.cycleName;
          this.saveData = res.data.data;
          this.getAllDistribution(this.data, "all");
          // 组织
          if (
            res.data.data.orgqueryscope &&
            Object.keys(res.data.data.orgqueryscope).length > 0
          ) {
            setTimeout(() => {
              // this.data.type = "org";
              this.resultOrg = this.data.queryOrgScope = Object.keys(
                res.data.data.orgqueryscope
              );
              const data = this.data;
              const orgList = this.findNamesById(this.resultOrg);
              const orgIds = orgList.map((item) => item.id);
              this.resultOrg = this.data.queryOrgScope = orgIds;
              this.getAllDistribution(data, "org");
            });
            console.log("有组织对比", this.data);
          }
          // 职层
          if (
            res.data.data.posqueryscope &&
            Object.keys(res.data.data.posqueryscope).length > 0
          ) {
            this.result = this.data.queryPosScope = Object.keys(
              res.data.data.posqueryscope
            );
            const data = this.data;
            this.getAllDistribution(data, "pos");
          }
        }
      });
    },
    setDistributedScreen() {
      this.saveData.org = this.data.org;
      this.saveData.year = this.data.year;
      this.saveData.cycle = this.data.cycle;
      const orgList = this.findNamesById(this.resultOrg);
      const orgIds = orgList.map((item) => item.id);
      this.saveData.orgqueryscope = orgIds;
      // this.saveData.orgqueryscope = this.findIdsByName(orgNames);
      this.saveData.orgqueryscope = this.resultOrg;
      this.saveData.posqueryscope = this.result;
      setDistributedScreen(this.saveData).then((res) => {});
    },
    getAllDistribution(data, type) {
      data.type = type;
      this.$xy.showLoad();
      getAllDistribution(data).then((res) => {
        this.$xy.hideLoad();
        if (Object.keys(res.data.data).length === 0 && type == "all") {
          this.isShow = false;
          return;
        }
        let index = this.cycleArr.indexOf(data.cycleName);
        let cycle = data.cycleName;
        if (index) {
          const cycleKey = Object.keys(this.cycleData).map((key) => {
            return key;
          });
          cycle = cycleKey[index];
          data.cycle = cycle;
        }
        // const cycle = this.cycleArr[index];
        let filterData = {
          orgqueryscope: [],
          posqueryscope: [],
          year: data.year,
          cycle: data.cycle || data.cycleName,
          org: data.org,
        };
        if (data.type == "org") {
          filterData.orgqueryscope = data.queryOrgScope;
        }
        if (data.type == "pos") {
          filterData.posqueryscope = data.queryPosScope;
        }
        // this.setDistributedScreen(filterData);
        const obj = res.data.data;
        let xAxisData = [];
        let redTotalArray = [];
        let yellowTotalArray = [];
        if (type == "all") {
          this.isShow = true;
          console.log("整体分布对比");
          const xAxisData = Object.keys(obj).map((key) => {
            return key;
          });
          const seriesData = Object.keys(obj).map((key) => {
            return obj[key];
          });
          console.log(xAxisData, seriesData, "seriesData");
          this.options.xAxis[0].data = xAxisData;
          this.options.series[0].data = seriesData;
          setTimeout(() => {
            var echarts = this.$echarts.init(
              document.getElementById("echarts")
            );
            echarts.setOption(this.options);
          });
        }
        if (type == "org") {
          if (
            res.data.data == "未获取到相关数据！" ||
            Object.keys(res.data.data).length === 0
          ) {
            this.isShowOrg = false;
            this.orgText = "组织选择";
            return;
          }
          const orgNameList = res.data.data;
          const text = Object.keys(orgNameList).map((key) => {
            return key;
          });
          const pos = this.findNamesById(text);
          const orgText = pos.map((item) => item.name);
          // const orgIds = pos.map((item) => item.id);
          this.orgText = orgText.join(",");
          for (let key in obj) {
            xAxisData.push(key);
            redTotalArray.push(obj[key].redIndex);
            yellowTotalArray.push(obj[key].yellowIndex);
          }
          this.isShowOrg = true;
          this.options1.xAxis.data = orgText;
          const extractedData = this.extractData(obj);
          this.options1.series = extractedData;
          setTimeout(() => {
            var eharts1 = this.$echarts.init(
              document.getElementById("echarts1")
            );
            eharts1.setOption(this.options1);
          });
        }
        if (type == "pos") {
          if (
            res.data.data == "未获取到相关数据！" ||
            Object.keys(res.data.data).length === 0
          ) {
            this.isShowPos = false;
            return;
          }
          const posNameList = res.data.data;
          const text = Object.keys(posNameList).map((key) => {
            return key;
          });

          const pos = this.getValues(this.posList, text);
          console.log("职层对比", posNameList, res.data, pos);

          this.posText = pos.join(",");
          for (let key in obj) {
            xAxisData.push(key);
            redTotalArray.push(obj[key].redIndex);
            yellowTotalArray.push(obj[key].yellowIndex);
          }
          this.isShowPos = true;
          this.options2.yAxis.data = pos;
          const extractedData = this.extractData(obj);
          this.options2.series = extractedData;
          setTimeout(() => {
            var eharts2 = this.$echarts.init(
              document.getElementById("echarts2")
            );
            eharts2.setOption(this.options2);
          });
        }
      });
    },

    getPercentage(arr) {
      const sum = arr.reduce((a, b) => a + b, 0);
      return arr.map((item) => (item / sum) * 100);
    },
    getValues(collection, array) {
      console.log(collection, "collection", array);
      let map = {};
      for (let i = 0; i < collection.length; i++) {
        map[collection[i].value] = collection[i].text;
      }
      let result = [];
      for (let i = 0; i < array.length; i++) {
        if (map[array[i]] !== undefined) {
          result.push(map[array[i]]);
        }
      }
      return result;
    },
    findNamesById(ids) {
      const findNames = (items, names) => {
        items.forEach((item) => {
          if (ids.includes(item.id)) {
            names.push({ id: item.id, name: item.name });
          }
          if (item.children) {
            findNames(item.children, names);
          }
        });
      };
      const names = [];
      findNames(this.orgsTreeData, names);
      return names;
    },

    findIdsByName(names) {
      const findIds = (items, ids) => {
        items.forEach((item) => {
          if (names.includes(item.name)) {
            ids.push(item.id);
          }
          if (item.children) {
            findIds(item.children, ids);
          }
        });
      };
      const ids = [];
      findIds(this.orgsTreeData, ids);
      return ids;
    },
    extractData(data) {
      const result = [];
      for (const company in data) {
        for (const rating in data[company]) {
          const index = result.findIndex((item) => item.name === rating);
          if (index !== -1) {
            result[index].data.push(data[company][rating]);
          } else {
            result.push({
              name: rating,
              type: "bar",
              stack: "total",
              emphasis: {
                focus: "series",
              },
              data: [data[company][rating]],
              barWidth: "30%",
            });
          }
        }
      }

      return result;
    },
    orgSelect() {
      this.showOrgFilterPicker = true;
    },
    posSelect() {
      this.showPosFilterPicker = true;
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    clickSearch(item) {
      console.log(item, "item");
      this.data.org = item.id;
      this.titleText = item.name || "组织";
      this.showFilterPicker = false;
      this.getAllDistribution(this.data, "all");
      this.getAllDistribution(this.data, "org");
      this.getAllDistribution(this.data, "pos");
      this.setDistributedScreen();
    },
    confirm() {
      const orgTextArr = Object.values(this.posList)
        .filter((element) => this.result.includes(element.value))
        .map((element) => element.text);
      this.posText = orgTextArr.join(",") || "组织选择";
      this.data.queryPosScope = this.result;
      const data = this.data;
      this.getAllDistribution(data, "pos");
      if (this.filterData.orgqueryscope) {
        this.filterData.orgqueryscope = Object.keys(
          this.saveData.orgqueryscope
        ).map((key) => {
          return key;
        });
      }

      this.filterData.posqueryscope = this.data.queryPosScope;
      this.filterData.year = this.data.year;
      this.filterData.cycleName = this.data.cycleName;
      let index = this.cycleArr.indexOf(this.data.cycleName);
      let cycle = "";
      if (index) {
        const cycleKey = Object.keys(this.cycleData).map((key) => {
          return key;
        });
        cycle = cycleKey[index];
      }
      this.filterData.cycle = cycle;
      this.filterData.org = this.data.org;
      this.setDistributedScreen();
    },
    confirmOrg() {
      if (this.orgsTreeData.length) {
        this.activeNames;

        this.data.type = "org";
        console.log("org", this.data);
        const orgList = this.findNamesById(this.resultOrg);
        const orgIds = orgList.map((item) => item.id);
        this.filterData.orgqueryscope =
          this.data.queryOrgScope =
          this.resultOrg =
            orgIds;
        this.getAllDistribution(this.data, "org");
        if (this.filterData.posqueryscope) {
          this.filterData.posqueryscope = Object.keys(
            this.saveData.posqueryscope
          ).map((key) => {
            return key;
          });
        }
        this.filterData.year = this.data.year;
        this.filterData.cycleName = this.data.cycleName;
        let index = this.cycleArr.indexOf(this.data.cycleName);
        let cycle = "";
        if (index) {
          const cycleKey = Object.keys(this.cycleData).map((key) => {
            return key;
          });
          cycle = cycleKey[index];
        }
        this.filterData.cycle = cycle;
        this.filterData.org = this.data.org;
        this.setDistributedScreen();
      }
    },
    onConfirm(value) {
      this.cycleName = value[0] + "-" + value[1];
      this.data.year = value[0];
      this.data.cycleName = value[1];
      let index = this.cycleArr.indexOf(this.data.cycleName);
      this.data.cycle = this.cycleKeyList[index];
      this.data.queryScope = [];
      this.showYearPicker = false;
      console.log(this.data, "this.data", this.filterData);
      this.getAllDistribution(this.data, "all");
      this.getAllDistribution(this.data, "org");
      this.getAllDistribution(this.data, "pos");
      this.setDistributedScreen();
    },
  },
};
</script>

<style lang="less" scoped>
/deep/ .van-collapse-item__content {
  padding: 0 8px;
}
.filterPrickTitle {
  display: flex;
  justify-content: center;
  border-bottom: 1px solid #ccc;
  align-items: center;
  padding: 12px;
  .titleBox {
    flex: 1;
    font-size: 18px;
    font-weight: bold;
    text-align: center;
  }
  .resetBox {
    text-align: right;
    color: #fff;
    background-color: #cf3633;
    font-size: 12px;
    padding: 8px;
    border-radius: 4px;
  }
}
.filterPrick {
  margin-top: 12px;
  width: 80vw;
  height: 70vh;
  overflow: auto;
}
.pageRanked {
  padding: 12px 0;
  border-radius: 10px;
  background-color: #fff;
  .van-popover__arrow {
    left: 30%;
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
.echart {
  padding: 12px;
}
.organizationBox {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .organizationSelect {
    display: flex;
    justify-content: space-between;
    width: 160px;
    background-color: #fff;
    border: 1px solid #eee;
    border-radius: 16px;
    font-size: 12px;
    padding: 10px;
  }
}
</style>
