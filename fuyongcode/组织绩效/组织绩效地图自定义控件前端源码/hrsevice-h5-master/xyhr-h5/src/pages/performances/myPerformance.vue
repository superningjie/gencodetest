<template>
  <div class="my-performance-page">
    <div class="myachievementQuery">
      <van-sticky>
        <van-search
          v-model="data.searchVal"
          placeholder="请输入您想搜索的内容"
          background="#fff"
          show-action
          @search="onSearch"
        >
          <!-- <template #action>
          <div class="filter-box" @click="onSearch">
            <div class="filter-text">筛选</div>
            <van-image :src="shaixuan" class="filter-icon"></van-image>
          </div>
        </template> -->
          <template #action>
            <van-dropdown-menu class="selectBox">
              <van-dropdown-item ref="item" title="筛选">
                <div class="selectYearTitle pd12">年份</div>
                <van-picker
                  show-toolbar
                  :columns="columns"
                  @confirm="onConfirm"
                  @cancel="onCancel"
                  cancel-button-text="清空并退出"
                  confirm-button-text="确定"
                  toolbar-position="bottom"
                  default-index="74"
                >
                </van-picker>
              </van-dropdown-item>
            </van-dropdown-menu>
          </template>
        </van-search>
      </van-sticky>
      <div class="modelBox pd12">
        <xy-empty v-if="!biaoji && list.length == 0"></xy-empty>
        <van-list v-model="loading" :finished="finished" @load="onLoad">
          <div class="listBox pd12 mt12" v-for="(i, index) in list" :key="i.id">
            <div class="flex">
              <div class="exam">{{ i.year }}</div>
              <div class="exam">{{ i.activityName }}</div>
            </div>
            <div class="listTitle flex mt8 middle">
              <div>{{ i.organization }}</div>
              <div class="ml8">{{ i.jobTitle }}</div>
            </div>
            <van-divider :style="{ margin: '10px 0' }" />
            <div>
              <van-collapse
                v-if="
                  i.targetYearExamineList && i.targetYearExamineList.length > 0
                "
                v-model="activeNames"
                :border="false"
              >
                <van-collapse-item :ref="i.id" :name="index">
                  <template #title>
                    <div class="flex listCol middle">
                      <div class="listText">考核结果：</div>
                      <div class="fs14 exColor fwb">
                        {{ i.assessmentResult }}
                      </div>
                    </div>
                  </template>
                  <div
                    class="subList flex justify middle fs12"
                    v-for="item in i.targetYearExamineList"
                    :key="item.id"
                  >
                    <div class="">
                      {{ item.title }}
                    </div>
                    <div class="flex">
                      <div>考核结果：</div>
                      <div class="exColor">{{ item.result }}</div>
                    </div>
                    <div @click="navigator('performanceDetail')">
                      <van-icon name="arrow" />
                    </div>
                  </div>
                </van-collapse-item>
              </van-collapse>
              <div v-else class="flex justify middle detailCenter">
                <div class="flex listCol">
                  <div class="listText">考核结果：</div>
                  <div class="fs14 exColor fwb">
                    {{ i.scoreResult }} <i v-if="i.gradeResult"> / </i>
                    {{ i.gradeResult }}
                  </div>
                </div>
                <div @click="gotoDetail(i)" class="flex middle">
                  <div class="listText">详情</div>
                  <van-icon name="arrow" color="#666" size="14" />
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </div>
    </div>
  </div>
</template>

<script>
import { getPerformanceInformationList } from "@/libs/api.js";
export default {
  name: "MyPerformance",
  data() {
    return {
      list: [],
      activeNames: [],
      finished: false,
      loading: false,
      value: "",
      columns: [],
      yearValue: "",
      oldTitle: "",
      biaoji: true,
      data: {
        queryCycle: 0,
        employeeNum: "",
        pages: 1,
        limit: 10,
        searchVal: "",
      },
    };
  },
  created() {
    this.$store.commit("setKeepAlive", ["MyPerformance"]);
  },
  beforeRouteLeave(to, form, next) {
    if (to.name === "myachievementQuery") {
      this.$store.commit("removeKeepAlive", "MyPerformance");
    }

    next();
  },
  mounted() {
    if (this.$route.query.name) {
      const title = this.$route.query.name + "的考核历史";
      em.changeTitle({
        title: title,
      });
      console.log("title: " + title);
    }
    // this.init();
    this.generateYears(2000);
  },
  methods: {
    init() {
      if (this.$route.query.employeeNum) {
        this.data.employeeNum = this.$route.query.employeeNum;
      }
      // this.$xy.showLoad();
      this.finished = false;
      const data = this.data;
      getPerformanceInformationList(data)
        .then((res) => {
          if (res.data.statusCode == 200) {
            this.loading = false;
            this.list.push(...res.data.data.performancePlanList);

            if (
              !res.data.data.performancePlanList.length ||
              res.data.data.performancePlanList.length < this.data.limit
            ) {
              this.biaoji = false;
              this.finished = true;
            }
          } else {
            this.finished = true;
          }

          // this.$xy.hideLoad();
        })
        .catch((err) => {
          this.loading = false;

          this.finished = true;
        });
    },
    onLoad() {
      this.init();
      this.data.pages++;
    },
    // 初始年份至今年
    generateYears(startYear) {
      const currentYear = new Date().getFullYear();
      for (let i = startYear; i <= currentYear; i++) {
        this.columns.push(i);
      }
    },

    navigator(path) {
      this.$router.push(path);
      // this.$router.push({ path: path, query: item });
    },
    gotoDetail(item) {
      this.$router.push({
        path: "myPerformanceDetail",
        query: {
          id: item.assessmentObjectId,
        },
      });
    },
    onSearch() {
      this.data.pages = 1;
      this.list = [];
      this.loading = true;
      this.onLoad();
    },
    onConfirm(year) {
      console.log(year);
      this.data.queryCycle = year;
      this.onSearch();
      this.$refs.item.toggle();
    },
    onCancel() {
      this.data.queryCycle = 0;
      this.onSearch();
      this.$refs.item.toggle();
    },
  },
};
</script>

<style lang="less" scoped>
.my-performance-page {
  background-color: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.myachievementQuery {
  padding-bottom: env(safe-area-inset-bottom);
}
.detailCenter {
  line-height: 16px;
}
.exColor {
  color: #d80c1e;
}
/deep/ .van-search__action:active {
  background-color: inherit;
}
/deep/.van-dropdown-menu__bar {
  box-shadow: none;
}
/deep/.selectBox {
  padding-right: 12px;
  .selectYearTitle {
    text-align: left;
  }
  .van-picker__cancel {
    color: #d80c1e;
  }
  .van-picker__confirm {
    width: 88px;
    height: 36px;
    background-color: #d80c1e;
    color: #fff;
    border-radius: 4px;
  }
}
.modelBox {
  padding-top: 0;
  .listBox {
    border-radius: 8px;
    background-color: #fff;
    .exam {
      opacity: 0.8;
      font-size: 12px;
      color: #d80c1e;
      padding: 4px;
      background: #ffebea;
      margin-right: 8px;
      border-radius: 4px;
    }
    .listTitle {
      font-size: 12px;
      color: #212121;
      margin-top: 12px;
    }
    .listCol {
      text-align: center;
    }
    .listText {
      font-size: 12px;
      color: #999999;
    }
    .subList {
      padding: 8px 12px;
      background-color: rgba(242, 242, 242, 0.6);
      border-radius: 4px;
      margin-top: 8px;
    }
    /deep/ .van-cell {
      padding: 0;
    }
    /deep/ .van-collapse-item__content {
      padding: 0;
    }
    /deep/.van-collapse-item__title--expanded .van-cell__right-icon::after {
      content: "收起";
      font-size: 12px;
      width: 24px;
      color: #666666;
      position: absolute;
      right: 20px;
      z-index: 9;
    }
    /deep/ .van-cell__right-icon::after {
      content: "更多";
      font-size: 12px;
      width: 24px;
      color: #666666;
      position: absolute;
      right: 20px;
    }
  }
}
</style>
