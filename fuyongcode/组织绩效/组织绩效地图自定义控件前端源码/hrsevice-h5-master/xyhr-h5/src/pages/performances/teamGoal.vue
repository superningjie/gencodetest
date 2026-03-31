<!-- @format -->
<template>
  <div class="team-performance-page" v-if="employeeList">
    <div class="myTeamInfo">
      <!-- 筛选 -->
      <van-sticky>
        <van-search
          v-model="data.requestVO.search"
          placeholder="搜索人员"
          @search="search"
        />
        <van-dropdown-menu>
          <!-- :title="jobName || '岗位'" -->
          <van-dropdown-item
            v-model="jobName"
            :options="jobList"
            @change="showJob"
          />
          <van-dropdown-item
            v-model="titleText"
            :title="titleText"
            @open="showFilterPicker = true"
          />
        </van-dropdown-menu>
      </van-sticky>

      <!-- </div> -->
      <!-- 正文列表 -->
      <div class="employeeList">
        <!-- <van-pull-refresh v-model="refreshing" @refresh="onRefresh"> -->
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
                <van-image round :src="item.picture" />
                <div class="base-cell-value">
                  <div class="title">
                    {{ item.personName }} {{ item.positionName }}
                  </div>
                  <div class="sub flex middle">
                    <div>
                      {{ item.companyName }}
                    </div>
                    <div class="ml8">
                      {{ item.orgName }}
                    </div>
                  </div>
                </div>
                <van-icon name="arrow" color="#999" class="ml8" />
              </div>
            </div>
          </template>
        </van-list>
        <!-- </van-pull-refresh> -->
        <xy-empty v-if="!loading && employeeList.length <= 0"></xy-empty>
      </div>
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
import { getTargetDisassemblyList } from "@/libs/api.js";
import leadercommonTree from "@/components/leadercommonTree";
export default {
  name: "TeamGoal",
  data() {
    return {
      list: [],
      showActiveOnly: false,
      jobList: [],
      jobName: 0,
      employeeList: [],
      showFilterPicker: false,
      loading: false,
      finished: false,
      data: {
        // directReport: true,
        // jobTitleId: "",
        // organizationId: "",
        // queryName: "",
        // limit: 15,
        // pages: 1,
        requestVO: {
          search: "",
          positionId: "",
          orgId: "",
        },
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
    this.data.requestVO.positionId = this.jobName =
      this.$store.state.jobList[0]?.pkPost;
  },
  mounted() {
    this.getJobList();
    this.initData();
  },
  methods: {
    getDirectReport() {},
    getJobList() {
      setTimeout(() => {
        const userJob = this.arrayTransition(this.$store.state.jobList);
        this.jobList = this.jobList.concat(userJob);
      }, 1000);
    },
    initData() {
      this.getTargetDisassemblyList(this.data);
    },
    getTargetDisassemblyList(data) {
      this.$xy.showLoad();
      getTargetDisassemblyList(data)
        .then((res) => {
          this.$xy.hideLoad();
          this.employeeList = res.data.data.personInfoList;
          this.finished = true;
        })
        .catch((err) => {
          this.$xy.hideLoad();
        });
    },
    arrayTransition(arr) {
      return arr.map((item) => ({
        value: item.pkPost,
        text: item.name,
      }));
    },
    navigator(item) {
      console.log(item, "item");
      this.$router.push({
        path: "individualGoal",
        query: {
          id: item.personId,
        },
      });
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    search() {
      this.initData();
    },
    onRefresh() {},
    onLoad() {},
    clickSearch(item, index) {
      this.titleText = item.name || "组织";
      this.showFilterPicker = false;
      this.data.requestVO.orgId = item.id;
      this.getTargetDisassemblyList(this.data);
    },
    showJob() {
      this.data.requestVO.positionId = this.jobName;
      this.getTargetDisassemblyList(this.data);
    },
  },
};
</script>
<style lang="less" scoped>
.employeeList {
  margin-top: 12px;
}
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
