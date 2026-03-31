<!-- @format -->

<template>
  <div class="backlogPage" v-if="initData">
    <div class="myachievementQuery">
      <van-sticky>
        <van-tabs @click="onClick" v-model="active">
          <van-tab title="待办"></van-tab>
          <van-tab title="已办"></van-tab>
        </van-tabs>
        <div class="searchBox">
          <van-search
            v-model="search"
            :placeholder="`搜索${title}标题内容`"
            @search="onSearch"
          />
          <div class="tabs pd12">
            <div class="tabsBox">
              <button
                @click="searchTab(item.id, item.tab)"
                class="tab"
                :class="tabIndex == item.id ? 'active' : ''"
                v-for="item in tabs"
                :key="item.id"
              >
                {{ item.tab }}
              </button>
            </div>
          </div>
        </div>
      </van-sticky>
      <div class="modelBox pd12">
        <xy-empty v-if="!biaoji && list.length == 0"></xy-empty>

        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model="loading"
            :finished="finished"
            :finished-text="list.length == 0 ? '' : '没有更多了'"
            class="list-box"
            :immediate-check="false"
            @load="onLoad"
            :offset="60"
          >
            <div
              class="listBox pd12 mt10"
              v-for="i in list"
              :key="i.handleId"
              @click="navigator(i)"
            >
              <div class="listTitle">
                <div class="nameTitle">{{ i.name }}</div>
                <div class="activityStatus">
                  {{ i.activityStatus }}
                </div>
              </div>
              <div class="listText flex justify">
                <p>{{ i.dateTime }}</p>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script>
import { getWorksList } from "@/libs/api.js";
export default {
  name: "MyBacklog",
  data() {
    return {
      active: "",
      tabs: [
        {
          id: 1,
          tab: "全部",
        },
        {
          id: 2,
          tab: "指标制定",
        },
        {
          id: 3,
          tab: "绩效评估",
        },
        {
          id: 4,
          tab: "结果确认",
        },
        {
          id: 5,
          tab: "其他事项",
        },
      ],
      biaoji: true,
      loading: false,
      refreshing: false,
      list: [],
      search: "",
      tabIndex: 1,
      assessmentActivities: "全部",
      finished: false,
      title: "待办",
      initData: {},
      navList_old: [
        {
          name: "待评分",
          url: "performancesRate",
        },
        {
          name: "已评分",
          url: "performancesRate",
        },
        {
          name: "整单待评分",
          url: "performancesRateTotal",
        },
        {
          name: "指标待审批",
          url: "approvalSingle",
        },
        {
          name: "指标已审批",
          url: "approvalSingle",
        },
        {
          name: "待确定",
          url: "performanceDetail?title=结果确认",
        },
      ],
      navList: [
        {
          name: "整单评分",
          type: "1010_S",
          url: "performancesRate",
        },
        {
          name: "整单评总分",
          type: "1020_S",
          url: "performancesRateTotal",
        },
        {
          name: "整单评总分调整分",
          type: "1030_S",
          url: "performancesRateTotalAdjust",
        },
        {
          name: "指标制定(制定)",
          type: "1050_S",
          url: "approvalSingle",
        },
        {
          name: "指标制定(审核)",
          type: "1060_S",
          url: "approvalSingle",
        },
        {
          name: "指标制定(确认)",
          type: "1070_S",
          url: "approvalSingle",
        },
        {
          name: "待确认",
          type: "1080_S",
          url: "performanceDetail",
        },
        {
          name: "已确认",
          type: "1090_S",
          url: "performanceDetail",
        },
      ],
      pages: {
        pageNumber: 1,
        pageSize: 10,
      },
    };
  },
  created() {
    if (this.$route.query.type) {
      this.title = this.$route.query.type;
      this.active = this.title == "待办" ? 0 : 1;

      console.log("active", this.active);
    }
  },
  beforeRouteLeave(to, form, next) {
    let data = this.$store.state.keepAlive;
    data.push("MyBacklog");
    this.$store.commit("setKeepAlive", data);
    next();
  },
  watch: {
    $route(to, from) {
      // 判断页面内容是否发生改变的逻辑
      if (from.name === "myachievementQuery" && to.name === "MyBacklog") {
        this.title = to.query.type;
        this.active = this.title == "待办" ? 0 : 1;
        this.tabIndex = 1;
        this.search = "";
        this.init();
      }

      // if (to.name == "MyBacklog" && to.query.type != this.title) {
      //   this.title = to.query.type;
      //   this.active = this.title == "待办" ? 0 : 1;
      //   this.tabIndex = 1;
      //   this.search = "";
      //   this.init();
      // }
    },
  },
  mounted() {
    this.init();
  },
  computed: {},
  methods: {
    onClick(name, title) {
      this.title = title;
      this.onSearch();
    },
    init() {
      this.$xy.showLoad();
      this.loading = true;
      this.refreshing = false;
      this.finished = false;
      this.biaoji = true;
      if (this.assessmentActivities === "全部") {
        this.assessmentActivities = "";
      }
      const data = {
        handleType: this.title,
        titleContent: this.search,
        assessmentActivities: this.assessmentActivities,
        ...this.pages,
      };
      if (data.pageNumber === 1) {
        this.list = [];
      }
      getWorksList(data).then((res) => {
        if (res.data.data.totalPage >= res.data.data.pageNumber) {
          this.initData = res.data.data;
          this.list.push(...res.data.data.handleTaskList);
          if (res.data.data.totalPage == res.data.data.pageNumber) {
            this.finished = true;
          } else {
            this.finished = false;
          }
        } else {
          this.finished = true;
        }
        if (this.list.length == 0) {
          this.biaoji = false;
        }

        this.loading = false;
        this.$xy.hideLoad();
      });
    },
    onLoad() {
      this.pages.pageNumber++;
      this.init();
    },
    navigator(i) {
      console.log("######", i);
      const res = this.navList.filter((item) => item.type === i.nodeTypeNumber);
      const path = res[0].url;
      const id = i.handleId;

      this.$router.push({
        path,
        query: {
          id,
        },
      });
    },
    searchTab(index, val) {
      console.log(index, val);
      this.tabIndex = index;
      this.assessmentActivities = val;
      this.pages.pageNumber = 1;
      this.list = [];
      this.init();
    },
    onSearch() {
      this.pages.pageNumber = 1;
      this.list = [];
      this.init();
    },
    onRefresh() {
      this.finished = true;
      this.pages.pageNumber = 1;
      this.list = [];
      this.init();
    },
  },
};
</script>
<style lang="less" scoped>
.list-box {
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
}
.backlogPage {
  background-color: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.myachievementQuery {
  padding-bottom: env(safe-area-inset-bottom);
}
.searchBox {
  background-color: #fff;

  .tabs {
    padding-top: 4px;
  }
  .tabsBox {
    box-sizing: border-box;
    padding-bottom: 2px;
    font-size: 12px;
    width: 100%;
    display: inline-block;
    white-space: nowrap;
    overflow-x: auto;

    .tab {
      width: 60px;
      height: 28px;
      background-color: #f5f5f5;
      border: none;
      border-radius: 4px;
      margin-left: 12px;
      &:first-child {
        margin-left: 0;
      }
    }

    .active {
      color: #d80c1e;
      border: 0.5px solid #d80c1e;
      background: rgba(216, 12, 30, 0.08);
    }
  }
}
.modelBox {
  padding-top: 0;
  .listBox {
    border-radius: 8px;
    background-color: #fff;
    .listTitle {
      display: flex;
      justify-content: space-between;
      .nameTitle {
        flex: 1;
        line-height: 20px;
      }
      .activityStatus {
        line-height: 20px;
        font-size: 12px;
        margin-left: 8px;
        width: 52px;
        height: 20px;
        color: #ed6a0c;
        background: #fff7f0;
        border: 0.5px solid rgba(237, 106, 12, 1);
        border-radius: 2px;
        text-align: center;
      }
    }
    .listText {
      margin-top: 8px;
      font-size: 12px;
      color: #999999;
      line-height: 18px;
    }
  }
}
</style>
