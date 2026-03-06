<template>
  <div class="page">
    <van-sticky>
      <van-search
        v-model="data.search"
        placeholder="搜索"
        shape="round"
        clearable
      />
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="data.year"
          :options="yearList"
          @change="search"
        />
        <van-dropdown-item
          v-model="periodText"
          :title="periodText"
          :options="cycleList"
          @change="changeCycle"
        />
        <van-dropdown-item
          v-model="titleText"
          :title="titleText"
          :title-class="titleText == '组织' ? '' : 'exColor'"
          @open="showFilterPicker = true"
        />
      </van-dropdown-menu>
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="activityText"
          :options="activityList"
          :title="activityText"
          @change="selectActivity"
        />
      </van-dropdown-menu>
    </van-sticky>
    <xy-empty v-if="list.length" :description="description"></xy-empty>
    <div class="pd12" v-else>
      <div class="switchBox">
        <div class="mr8">仅看直接下属</div>
        <van-switch
          v-model="data.isSubordinate"
          size="16"
          active-color="red"
          @change="search"
        />
      </div>
      <div class="echart">
        <div
          id="warnCharts"
          style="width: 100%; height: 300px"
          v-if="totalCount"
        ></div>
        <xy-empty v-else></xy-empty>
      </div>
      <div class="personnel">
        <div class="flexTab">
          <div
            class="personnelTab"
            :class="data.displayType == 1 ? 'personnelActive' : 'personnelBox'"
            @click="selectTab(1)"
          >
            <div>按人员</div>
            <div class="icon" v-show="data.displayType == 1">
              <van-icon name="play" color="#D80C1E" />
            </div>
          </div>
          <div
            class="indexTab"
            :class="data.displayType == 2 ? 'indexActive' : 'indexTabBox'"
            @click="selectTab(2)"
          >
            <div>按指标</div>
            <div class="icon" v-show="data.displayType == 2">
              <van-icon name="play" color="#D80C1E" />
            </div>
          </div>
        </div>
        <van-tabs
          v-model="personnelTab"
          v-if="data.displayType == 1"
          @click="lightPerson"
        >
          <van-tab title="亮红灯人员">
            <van-list
              v-model="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
              :immediate-check="false"
            >
              <div
                class="personnelInfoBox flex middle"
                @click="navigator('warnPerDetail', empInfo.actEvalObj)"
                v-for="empInfo in pagesList"
              >
                <div class="personnelInfo">
                  <div class="personnelList flex">
                    <div class="avatar">
                      <van-image
                        round
                        width="32px"
                        height="32px"
                        :src="empInfo.headSculpture"
                      />
                    </div>
                    <div class="info">
                      <div class="fw-text flex middle">
                        <p class="over-text">{{ empInfo.name }}</p>
                        <p class="ml8 over-text">{{ empInfo.position }}</p>
                      </div>
                      <div class="plain-text flex middle">
                        <p class="over-text">{{ empInfo.company }}</p>
                        <p class="ml8 over-text">{{ empInfo.organization }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="lightBox plain-text flex middle">
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="RED" />
                      {{ empInfo.redLightCount }}
                    </p>
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="YELLOW" />
                      {{ empInfo.yellowLightCount }}
                    </p>
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="GREEN" />
                      {{ empInfo.greenLightCount }}
                    </p>
                  </div>
                </div>
                <van-icon name="arrow"></van-icon>
              </div>
            </van-list>
          </van-tab>
          <van-tab title="亮黄灯人员">
            <van-list
              v-model="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <div
                class="personnelInfoBox"
                @click="navigator('warnPerDetail', empInfo.actEvalObj)"
                v-for="empInfo in pagesList"
              >
                <div class="personnelInfo">
                  <div class="personnelList flex">
                    <div class="avatar">
                      <van-image
                        round
                        width="32px"
                        height="32px"
                        :src="empInfo.headSculpture"
                      />
                    </div>
                    <div class="info">
                      <div class="fw-text flex middle">
                        <p class="over-text">{{ empInfo.name }}</p>
                        <p class="ml8 over-text">{{ empInfo.position }}</p>
                      </div>
                      <div class="plain-text flex middle">
                        <p class="over-text">{{ empInfo.company }}</p>
                        <p class="ml8 over-text">{{ empInfo.organization }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="lightBox plain-text flex middle">
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="RED" />
                      {{ empInfo.redLightCount }}
                    </p>
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="YELLOW" />
                      {{ empInfo.yellowLightCount }}
                    </p>
                    <p class="over-text">
                      <van-image width="7.5" height="10" :src="GREEN" />
                      {{ empInfo.greenLightCount }}
                    </p>
                  </div>
                </div>
                <van-icon name="arrow"></van-icon>
              </div>
            </van-list>
          </van-tab>
        </van-tabs>
        <van-tabs v-model="personnelTab" v-else @click="lightPerson">
          <van-tab title="亮红灯指标">
            <van-list
              v-model="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <div
                class="indexBox flex"
                @click="getIndexDetail(item)"
                v-for="item in pagesList"
              >
                <div class="indexLeft">
                  <van-image width="11" height="15" :src="RED" />
                </div>
                <div class="indexRight">
                  <div class="indexInfo flex justify">
                    <div>{{ item.indCtrName }}</div>
                    <div>{{ item.period }}</div>
                  </div>
                  <div class="indexUserInfo">
                    <div class="flex">
                      <div class="avatar">
                        <van-image
                          round
                          width="32px"
                          height="32px"
                          :src="item.empInfoDto.headSculpture"
                        />
                      </div>
                      <div class="info">
                        <div class="fw-text flex middle">
                          <p class="over-text">{{ item.empInfoDto.name }}</p>
                          <p class="ml8 over-text">
                            {{ item.empInfoDto.position }}
                          </p>
                        </div>
                        <div class="plain-text flex middle">
                          <p class="over-text">{{ item.empInfoDto.company }}</p>
                          <p class="ml8 over-text">
                            {{ item.empInfoDto.organization }}
                          </p>
                        </div>
                      </div>
                    </div>
                    <div class="indexDetailBox flex justify">
                      <div class="pointBox">评分:{{ item.ies }}</div>
                      <div class="lightDetailBox flex justify">
                        <div>亮灯次数:{{ item.lightUpCount }}</div>
                        <div>详情 <van-icon name="arrow"></van-icon></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </van-list>
          </van-tab>
          <van-tab title="亮黄灯指标">
            <van-list
              v-model="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <div
                class="indexBox flex"
                @click="getIndexDetail(item)"
                v-for="item in pagesList"
              >
                <div class="indexLeft">
                  <van-image width="11" height="15" :src="YELLOW" />
                </div>
                <div class="indexRight">
                  <div class="indexInfo flex justify">
                    <div>{{ item.indCtrName }}</div>
                    <div>2024Q1</div>
                  </div>
                  <div class="indexUserInfo">
                    <div class="flex">
                      <div class="avatar">
                        <van-image
                          round
                          width="32px"
                          height="32px"
                          :src="item.empInfoDto.headSculpture"
                        />
                      </div>
                      <div class="info">
                        <div class="fw-text flex middle">
                          <p class="over-text">{{ item.empInfoDto.name }}</p>
                          <p class="ml8 over-text">
                            {{ item.empInfoDto.position }}
                          </p>
                        </div>
                        <div class="plain-text flex middle">
                          <p class="over-text">{{ item.empInfoDto.company }}</p>
                          <p class="ml8 over-text">
                            {{ item.empInfoDto.organization }}
                          </p>
                        </div>
                      </div>
                    </div>
                    <div class="indexDetailBox flex justify">
                      <div class="pointBox">评分:{{ item.ies }}</div>
                      <div class="lightDetailBox flex justify">
                        <div>亮灯次数:{{ item.lightUpCount }}</div>
                        <div>详情 <van-icon name="arrow"></van-icon></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </van-list>
          </van-tab>
        </van-tabs>
      </div>
    </div>

    <leadercommonTree
      :resetShow="true"
      :jobInfo="jobInfo"
      :workPosition="workPosition"
      :showFilterPicker="showFilterPicker"
      @isshowFilterPicker="isshowFilterPicker"
      @clickSearch="clickSearch"
    />
    <lightDetailPopup :show="show" :detailInfo="detailInfo" @close="close" />
  </div>
</template>

<script>
import leadercommonTree from "@/components/leadercommonTree";
import lightDetailPopup from "@/pages/performances/components/lightDetailPopup.vue";
import {
  getFilterInfo,
  getBrightlighthome,
  getIndexDetail,
} from "@/libs/api.js";
import RED from "@/assets/performances/red.png";
import YELLOW from "@/assets/performances/yellow.png";
import GREEN from "@/assets/performances/green.png";

export default {
  name: "LightWarning",
  components: {
    leadercommonTree,
    lightDetailPopup,
  },
  data() {
    return {
      show: false,
      detailInfo: {},
      RED,
      YELLOW,
      GREEN,
      list: [],
      firstFlag: true,
      loading: false,
      finished: false,
      periodText: "周期",
      activityText: "考核活动",
      data: {
        search: "",
        year: 2024, //年份
        period: "", //周期
        company: "", //部门ID
        isSubordinate: true, //仅查看直属下属
        displayType: 1, //显示类型（1按人员，2按指标）
        lightUpType: "redLight", // 'yellowLight'
        pages: 0,
        limit: 5,
        activityId: "", // 考核活动id
      },
      totalCount: 0,
      pagesList: [],
      description: "当前筛选条件下无已完成评估的考核活动数据",
      personnelTab: "",
      yearList: [
        {
          text: 2022,
          value: 2022,
        },
        {
          text: 2023,
          value: 2023,
        },
        {
          text: 2024,
          value: 2024,
        },
      ],
      cycleList: [],
      titleText: "组织",
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      workPosition: "0",
      showFilterPicker: false,
      activityList: [],
      option: {
        title: {
          text: "红绿灯\n指标占比",
          left: "center",
          top: "center",
          textStyle: {
            lineHeight: 26,
          },
        },
        color: ["#F7B56D", "#D80C1E", "#70B603"],
        series: [
          // {
          //   type: "pie",
          //   radius: ["50%", "70%"],
          //   width: "90%",
          //   height: "90%",
          //   left: "5%",
          //   top: "5%",
          //   // avoidLabelOverlap: false,
          //   label: {
          //     normal: {
          //       show: true,
          //       position: "center", // 展示在中间位置
          //       formatter: [""].join("\n"),
          //       rich: {
          //         a: {
          //           color: "#111928",
          //           fontSize: 24,
          //           fontWeight: 900,
          //         },
          //         b: {
          //           fontSize: 16,
          //           color: "#6B7280",
          //           lineHeight: 40,
          //         },
          //       },
          //     },
          //   },
          //   labelLine: {
          //     show: true,
          //     length: 8,
          //   },
          //   itemStyle: {
          //     borderColor: "#fff",
          //     borderWidth: 4,
          //   },
          //   data: [],
          // },
          {
            type: "pie",
            radius: ["50%", "70%"],
            left: "10%",
            top: "10%",
            width: "80%",
            height: "80%",
            avoidLabelOverlap: false,
            label: {
              padding: [0, -60, 30, -60],
              lineHeight: 16,
            },
            labelLine: {
              show: true,
              length: 30, //第一段线长
              length2: 60, //第二段线长
            },
            // itemStyle: {
            //   borderColor: "#fff",
            //   borderWidth: 4,
            // },
            data: [],
          },
        ],
      },
    };
  },

  created() {},
  mounted() {
    this.getFilterInfo();
  },

  methods: {
    lightPerson() {
      this.data.lightUpType = this.personnelTab ? "yellowLight" : "redLight";
      this.data.pages = 1;
      this.initData();
    },
    onLoad() {
      this.data.pages++;
      this.initData();
    },
    getFilterInfo() {
      this.$xy.showLoad();
      this.data.pages = 1;
      getFilterInfo(this.data)
        .then((res) => {
          this.cycleList = this.arrayTransition(res.data.data.period);
          this.activityList = this.arrayTransition(
            res.data.data.activityIdAndName
          );
          // 如果是第一次页面加载
          if (this.firstFlag) {
            // 默认选择考核活动的条件
            this.setActivity(res.data.data.defaultActivity.value)
            this.firstFlag = false;
          }
          this.initData();
        })
        .catch((err) => {
          this.$xy.hideLoad();
        });
    },
    close(value) {
      this.show = value;
    },
    initData() {
      this.$xy.showLoad();
      this.finished = false;
      this.loading = true;
      getBrightlighthome(this.data)
        .then((res) => {
          this.loading = false;
          this.$xy.hideLoad();
          const lightCount = res.data.data.lightCount;
          const list = res.data.data.indicatorInfo || res.data.data.personInfo;
          const pagesList =
            list.redLightPerson ||
            list.yellowLightPerson ||
            list.redLightInfo ||
            list.yellowLightInfo;
          console.log(pagesList.length, " pagesList.length ");
          if (pagesList.length == 0 || pagesList.length < this.data.limit) {
            console.log(this.data.limit, "this.data.limit");
            this.finished = true;
          }
          if (this.data.pages == 1) {
            this.pagesList = [];
          }
          this.pagesList.push(...pagesList);
          // this.list = list
          // this.list.push(...list);
          console.log(pagesList, "  this.list");
          const totalCount =
            Number(lightCount.yellowLight) +
            Number(lightCount.redLight) +
            Number(lightCount.greenLight);
          this.totalCount = totalCount;
          const yellowPer =
            ((Number(lightCount.yellowLight) / totalCount) * 100).toFixed(2) +
            "%";

          const greenPer =
            ((Number(lightCount.greenLight) / totalCount) * 100).toFixed(2) +
            "%";
          const redPer =
            (
              100 -
              ((Number(lightCount.yellowLight) / totalCount) * 100).toFixed(2) -
              ((Number(lightCount.greenLight) / totalCount) * 100).toFixed(2)
            ).toFixed(2) + "%";

          this.option.series[0].data = [
            {
              value: lightCount.yellowLight,
              name: "黄灯指标",
              label: {
                formatter: [
                  "黄灯指标",
                  // lightCount.yellowLight + "个" + yellowPer,
                  yellowPer,
                ].join("\n"),
                color: "#000",
              },
            },
            {
              value: lightCount.redLight,
              name: "红灯指标",
              label: {
                formatter: [
                  "红灯指标",
                  // lightCount.redLight + "个" + redPer,
                  redPer,
                ].join("\n"),
                color: "#000",
              },
            },
            {
              value: lightCount.greenLight,
              name: "绿灯指标",
              label: {
                formatter: [
                  "绿灯指标",
                  // lightCount.greenLight + "个" + greenPer,
                  greenPer,
                ].join("\n"),
                color: "#000",
              },
            },
          ];
          setTimeout(() => {
            document.getElementById("warnCharts") ? this.initChart() : "";
          });
        })
        .catch((err) => {
          this.$xy.hideLoad();
        });
    },
    selectTab(val) {
      this.data.displayType = val;
      this.data.pages = 1;
      this.initData();
    },
    arrayTransition(obj) {
      const handleStatusList = obj;
      handleStatusList.unshift({ name: "不限", value: "" });
      return handleStatusList.map((item) => {
        // const key = Object.keys(item)[0];
        return {
          text: item.name,
          value: item.value,
        };
      });
    },
    search() {
      this.data.pages = 1;
      this.getFilterInfo();
    },
    changeCycle(val) {
      this.data.period = val;
      const element = this.cycleList.find((element) => element.value === val);
      this.periodText = element ? element.text : this.periodText;
      this.search();
    },
    // 设置考核活动选择的值
    setActivity(val) {
      console.log("setActivity")
      console.log(val)
      this.data.activityId = val;
      const element = this.activityList.find(
          (element) => element.value === val
      );
      this.activityText = element ? element.text : this.activityText;
      console.log(this.activityText)
    },
    selectActivity(val) {
      this.setActivity(val);
      this.search();
    },
    initChart() {
      const self = this;
      var warnCharts = this.$echarts.init(
        document.getElementById("warnCharts")
      );
      warnCharts.setOption(this.option);
      // warnCharts.on("click", function (params) {
      //   self.option.series[0].label.normal.formatter = [
      //     `{a|${params.data.text}}`,
      //     `{b|${params.data.name}占比}`,
      //   ].join("\n");
      //   warnCharts.setOption(self.option);
      // });
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },

    clickSearch(item) {
      console.log(item, "item");
      this.data.company = item.id;
      this.titleText = item.name || "组织";
      this.data.pages = 1;
      this.getFilterInfo();
      this.showFilterPicker = false;
    },
    navigator(path, id) {
      this.$router.push({
        path: path,
        query: {
          id: id,
          year: this.data.year,
          period: this.data.period, //周期
          isSubordinate: this.data.isSubordinate, //仅查看直属下属
        },
      });
    },
    getIndexDetail(item) {
      const data = {
        indexId: item.id,
        actEvalObjId: item.empInfoDto.actEvalObj,
      };
      this.$xy.showLoad();
      getIndexDetail(data).then((res) => {
        this.$xy.hideLoad();
        this.show = true;
        this.detailInfo = res.data.data;
      });
    },
  },
};
</script>

<style lang="less" scoped>
.page {
  background: #f2f2f2;
  min-height: 100vh;
  .indexBox:last-child {
    border-bottom: none;
  }
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
    // border-top: 1px solid #eee;
  }
}
/deep/ .van-empty__description {
  padding: 0;
}
.indexBox {
  padding: 8px 0;
  border-bottom: 0.5px solid #eeeeee;

  // :nth-last-child() {
  //   border-bottom: none;
  // }
  .indexLeft {
    margin-right: 8px;
  }
  .indexRight {
    flex: 1;
    .indexInfo {
    }
    .indexUserInfo {
      padding: 8px 0;
      .info {
        margin-left: 8px;
      }
      .indexDetailBox {
        margin-top: 4px;
        .pointBox {
          width: 100px;
          padding: 8px;
          background-color: #fef4f5;
        }
        .lightDetailBox {
          margin-left: 12px;
          flex: 1;
          padding: 8px;
          background-color: #fef4f5;
        }
      }
    }
  }
}

.personnelInfoBox {
  display: flex;
  align-items: center;

  .personnelInfo {
    padding: 6px 0;
    border-bottom: 1px solid #fef4f5;
    flex: 1;
    .personnelList {
      flex: 1;
      margin-top: 8px;
      align-items: center;
      .info {
        margin-left: 8px;
        flex: 1;
      }
    }
    .lightBox {
      margin-left: 40px;
      p {
        margin-right: 16px;
      }
    }
  }
}

.echart {
  position: relative;
  background-color: #fff;
  border-radius: 8px;

  .indexDetail {
    position: absolute;
    top: 60%;
    left: 40%;
    color: #d80c1e;
  }
}
.personnelActive {
  background-color: #fff;
  &::before {
    content: "";
    width: 0;
    height: 0;
    border-right: 20px solid transparent;
    border-bottom: 44px solid #fff;
    position: absolute;
    top: 0;
    right: -20px;
  }
}
.personnelBox {
  background-color: #f2f2f2;
  &::before {
    content: "";
    width: 0;
    height: 0;
    border-right: 20px solid transparent;
    border-bottom: 44px solid #f2f2f2;
    position: absolute;
    top: 0;
    right: -20px;
  }
}
.indexActive {
  background-color: #fff;
  &::before {
    content: "";
    width: 0;
    height: 0;
    border-left: 21px solid transparent;
    border-bottom: 44px solid #fff;
    position: absolute;
    top: 2px;
    left: -20px;
  }
}
.indexTabBox {
  background-color: #f2f2f2;
  &::before {
    content: "";
    width: 0;
    height: 0;
    border-left: 20px solid transparent;
    border-bottom: 44px solid #f2f2f2;
    position: absolute;
    top: 0;
    left: -18px;
  }
}
.personnel {
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  margin-top: 12px;
  .flexTab {
    display: flex;
    justify-content: center;
    text-align: center;
    // background-color: #f2f2f2;
    .personnelTab {
      flex: 1;
      height: 44px;
      line-height: 44px;
      position: relative;
    }
    .indexTab {
      flex: 1;
      height: 44px;
      line-height: 44px;
      position: relative;
    }
    .icon {
      position: relative;
      top: -24px;
      rotate: -90deg;
    }
  }
}
/deep/ .switchBox {
  display: flex;
  padding-bottom: 12px;
  .mr8 {
    margin-right: 8px;
  }
}
</style>
