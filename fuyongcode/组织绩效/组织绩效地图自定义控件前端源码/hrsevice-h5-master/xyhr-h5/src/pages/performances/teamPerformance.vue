<!-- @format -->
<template>
  <div class="team-performance-page" v-if="employeeList">
    <div class="myTeamInfo">
      <!-- 筛选 -->
      <van-sticky>
        <van-search
          v-model="data.queryName"
          placeholder="输入姓名搜索"
          @search="search"
        />
        <van-dropdown-menu>
          <!-- :title="jobName || '岗位'" -->
          <van-dropdown-item
            v-model="jobName"
            :options="jobList"
            :title-class="jobName == 0 ? '' : 'exColor'"
            @change="showJob"
          />
          <van-dropdown-item
            v-model="titleText"
            :title="titleText"
            :title-class="titleText == '组织' ? '' : 'exColor'"
            @open="showFilterPicker = true"
          />
        </van-dropdown-menu>
        <div class="switchBox">
          <van-switch-cell
            active-color="red"
            inactive-color="#e5e5e5"
            v-model="data.directReport"
            title="只看直接下属"
            :style="{ background: '#e5e5e5' }"
            @change="getDirectReport"
            size="16"
          />
        </div>
      </van-sticky>

      <!-- </div> -->
      <!-- 正文列表 -->
      <div class="employeeList">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model="loading"
            :finished="finished"
            :finished-text="employeeList.length > 0 ? '没有更多了' : ''"
            :immediate-check="false"
            class="list-box"
            :offset="30"
            @load="onLoad"
          >
            <template v-for="(item, index) in employeeList">
              <div class="base-cell-group">
                <div class="base-cell" :key="item.id" @click="navigator(item)">
                  <van-image round :src="item.employeePhoto" />
                  <div class="base-cell-value">
                    <div class="title">
                      {{ item.employeeName }} {{ item.jobTitle }}
                    </div>
                    <div class="sub flex middle">
                      <div>
                        {{ item.company }}
                      </div>
                      <!-- <div class="col-line"></div> -->
                      <div class="ml8">
                        {{ item.employeeOrganization }}
                      </div>
                    </div>
                  </div>
                  <div class="base-cell-right">
                    <!-- <div class="exColor">
                    {{ item.scoreResult
                    }}<i v-if="item.scoreResult && item.gradeResult">/</i
                    >{{ item.gradeResult }}
                  </div> -->
                    <div class="exColor">
                      {{ item.performanceResult }}
                    </div>
                    <div class="cycle">
                      <van-icon name="clock-o" v-if="item.cycle" />
                      {{ item.cycle }}
                    </div>
                  </div>
                  <van-icon name="arrow" color="#999" class="ml8" />
                </div>
              </div>
            </template>
          </van-list>
        </van-pull-refresh>
        <xy-empty v-if="!loading && employeeList.length <= 0"></xy-empty>
      </div>
      <!-- <div class="header-box">
      <div class="head-count">
        共查询到 {{ headCount || employeeList.length }} 人
      </div>
    </div> -->
      <!-- <JobList ref="JobList" @change="changeJob" /> -->
      <leadercommonTree
        :resetShow="true"
        :jobInfo="jobInfo"
        :workPosition="workPosition"
        :showFilterPicker="showFilterPicker"
        @isshowFilterPicker="isshowFilterPicker"
        @clickSearch="clickSearch"
      />
    </div>
  </div>
</template>

<script>
import { getTeamPerList } from "@/libs/api.js";
import leadercommonTree from "@/components/leadercommonTree";
export default {
  name: "teamPerformance",
  data() {
    return {
      list: [],
      showActiveOnly: false,
      jobList: [{ text: "岗位", value: 0 }],
      jobName: 0,
      employeeList: [],
      showFilterPicker: false,
      loading: false,
      finished: false,
      data: {
        directReport: true,
        jobTitleId: "",
        organizationId: "",
        queryName: "",
        limit: 15,
        pages: 1,
      },
      titleText: "组织",
      headCount: 0,
      // 是否已加载完成
      jobInfo: {}, // 个人岗位信息
      // 岗位子序列
      showWorkPositionFlag: false,
      workPosition: "0",
      refreshing: false,
    };
  },
  components: {
    leadercommonTree,
  },
  created() {
    this.titleText = "组织";
    this.getJobList();
  },
  mounted() {
    this.getTeamList();
  },
  methods: {
    getDirectReport() {
      this.employeeList = [];
      this.data.pages = 1;
      this.finished = false;
      this.getTeamList();
    },
    getJobList() {
      setTimeout(() => {
        const userJob = this.arrayTransition(this.$store.state.jobList);
        this.jobList = this.jobList.concat(userJob);
      }, 1000);
    },
    arrayTransition(arr) {
      return arr.map((item) => ({
        value: item.pkPost,
        text: item.name,
      }));
    },
    navigator(item) {
      this.$store.commit("removeKeepAlive", "MyPerformance");
      this.$router.push({
        path: "myPerformance",
        query: {
          employeeNum: item.employeeNum,
          name: item.employeeName,
        },
      });
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    search() {
      this.employeeList = [];
      this.data.pages = 1;
      this.finished = false;
      this.getTeamList();
    },
    onRefresh() {
      this.list = [];
      this.data.pages = 1;
      this.finished = true;
      this.getTeamList();
    },
    onLoad() {
      this.data.pages++;
      this.getTeamList();
    },
    getTeamList() {
      this.$xy.showLoad();
      this.loading = true;
      this.finished = false;
      this.refreshing = false;

      const data = this.data;
      if (this.data.pages === 1) {
        this.employeeList = [];
      }
      getTeamPerList(data)
        .then((res) => {
          if (!this.finished) {
            this.employeeList.push(...res.data.data.personnelList);
            if (res.data.data.msgNum <= 10) {
              this.finished = true;
            } else {
              this.finished =
                res.data.data.msgNum / this.data.pages / this.data.limit < 1;
            }
          } else {
            this.finished = true;
          }
          // return;
          this.$xy.hideLoad();
          this.loading = false;
        })
        .catch((err) => {
          this.$xy.hideLoad();
          this.loading = false;
          this.finished = true;
        });
    },
    goback() {
      this.$router.go(-1);
    },
    viewEmployeeDetail(employee) {
      this.$store.commit("addKeepAlive", "myTeamInfo");
      this.$router.push({
        name: "employeeInfo",
        query: {
          pkPsndoc: employee.pkPsndoc,
          ncId: employee.ncId,
          goback: -1,
        },
      });
    },
    clickSearch(item, index) {
      this.data.pages = 0;
      this.data.organizationId = item.code;
      this.finished = false;
      this.showFilterPicker = false;
      this.employeeList = [];
      this.search();
    },
    showJob() {
      console.log("jobName: " + this.jobName);
      this.data.jobTitleId = this.jobName;
      this.finished = false;
      this.search();
    },
    // changeJob(job) {
    //   if (job.pkPost != this.jobInfo.pkPost) {
    //     this.jobInfo = job;
    //     this.data.pk = "";
    //     this.data.origin = "";
    //     this.data.type = "";
    //     this.titleText = "所属组织";
    //     this.search();
    //   }
    // },
  },
};
</script>
<style lang="less" scoped>
.team-performance-page {
  background-color: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
/deep/ .exColor {
  color: #d80c1e;
}
/deep/ .van-dropdown-menu__bar {
  box-shadow: none;
}
.myTeamInfo {
  padding-bottom: env(safe-area-inset-bottom);
}
/deep/ .switchBox {
  .van-cell__value {
    flex: 3;
  }
  .van-switch-cell .van-switch {
    float: left;
  }
}
.sideline {
  position: absolute;
  top: 0;
  right: 0;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  font-size: 12px;
  font-weight: 400;
  color: #ffffff;
  line-height: 20px;
  padding: 0 8px;
  background-color: #00bcff;
}
.list-box {
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
}
.header-box {
  position: fixed;
  left: 0;
  width: 100%;
  bottom: 0;
  padding-bottom: env(safe-area-inset-bottom);
  background-color: #ffffff;
  .head-count {
    background-color: #ffdcdc;
    color: #e31524;
    height: 36px;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
  }
}
.base-cell-group {
  background: #fff;
}
.base-cell {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  padding: 12px 16px;
  position: relative;
  &:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    bottom: 0;
    left: 16px;
    border-bottom: 1px solid #ebedf0;
    transform: scaleY(0.5);
  }
  .van-checkbox {
    margin-right: 12px;
  }
  .van-image {
    width: 32px;
    height: 32px;
  }
  .base-cell-value {
    flex: 1;
  }
  .base-cell-right {
    display: flex;
    flex-direction: column;
    text-align: right;
    .exColor {
      font-size: 14px;
    }
    .cycle {
      color: #666;
      font-size: 12px;
      line-height: 18px;
    }
  }
  .title {
    font-size: 14px;
    color: #212121;
    font-weight: bold;
    padding: 2px 12px;
  }
  .sub {
    font-size: 12px;
    color: #666;
    padding: 0 12px;
    line-height: 18px;
  }
}
</style>
