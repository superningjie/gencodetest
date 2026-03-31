<template>
  <div class="ranking-list safe-bottom" v-if="list.length > 0">
    <van-sticky>
      <div class="header-box">
        <!-- <div class="search">
          <van-search
            v-model="data.queryContent"
            placeholder="请输入您想搜索的内容"
            background="#fff"
            @search="onSearch"
          >
          </van-search>
        </div> -->
        <div class="select">
          <van-dropdown-menu>
            <van-dropdown-item title="组织" ref="organ">
              <van-cell
                center
                :title="val.text"
                v-for="val in organizationList"
                :key="val.id"
                @click="selectOrganization(val)"
              >
                <template #right-icon>
                  <van-checkbox v-model="val.status" checked-color="#D50C1C" />
                </template>
              </van-cell>
              <div class="flex">
                <div class="btn-box-cancel" @click="onCancel">取消</div>
                <div class="btn-box-confirm" @click="onConfirm">确认</div>
              </div>
            </van-dropdown-item>
          </van-dropdown-menu>
        </div>
      </div>
    </van-sticky>
    <div class="ranking-list-box">
      <div class="rank-list">
        <!--表头-->
        <van-row class="thead" gutter="2">
          <van-col class="tc" span="3">排名</van-col>
          <van-col span="6">员工</van-col>
          <van-col span="10">组织</van-col>
          <van-col class="tr" span="5">绩效结果</van-col>
        </van-row>
        <!--内容-->
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
            <van-row
              class="tbody"
              v-for="(item, index) in list"
              :key="index"
              gutter="2"
            >
              <van-col class="tc num" span="3">
                <span>{{ index + 1 }}</span>
              </van-col>
              <van-col class="flex" span="6">
                <van-image round :src="item.employeePhoto" />
                <span>{{ item.employeeName }}</span>
              </van-col>
              <van-col span="10"
                >{{ item.company }} {{ item.employeeOrganization }}</van-col
              >

              <van-col class="tr red" span="5">
                <div v-if="item.scoreResult !== null">
                  {{ item.scoreResult
                  }}<span v-if="item.gradeResult">/{{ item.gradeResult }}</span>
                </div>
                <div v-else>--</div>
              </van-col>
            </van-row>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>
<script>
import { getRankList, getBatchTotalStatus } from "@/libs/api.js";
export default {
  name: "RankingList",
  data() {
    return {
      list: [],
      organizationList: [],
      data: {
        limit: 100,
        pages: 1,
        mybacklogId: "",
        queryContent: "",
        affiliateadminorgIds: [],
      },
      loading: false,
      refreshing: false,
      finished: false,
    };
  },
  mounted() {
    this.data.mybacklogId = this.$route.query.id;
    this.getStatus();
    this.rankList();
  },
  computed: {},
  methods: {
    getStatus() {
      const data = {
        handleId: this.$route.query.id,
      };
      getBatchTotalStatus(data).then((res) => {
        this.organizationList = this.arrayTransition(
          res.data.data.organizationList
        );
      });
    },
    arrayTransition(obj) {
      const handleStatusList = obj;
      return handleStatusList.map((item) => {
        const key = Object.keys(item)[0];
        return {
          text: item[key],
          value: key,
          status: false,
        };
      });
    },
    rankList() {
      this.$xy.showLoad();
      this.loading = true;
      this.refreshing = false;
      this.finished = false;
      let params = this.data;
      getRankList(params).then((res) => {
        this.list.push(...res.data.data.userList);
        this.$xy.hideLoad();

        if (res.data.data.userList.length < this.data.limit) {
          this.finished = true;
        }
        this.loading = false;
      });
    },
    onLoad() {
      this.data.pages++;
      this.rankList();
    },
    selectOrganization() {},
    onSearch() {},
    onCancel() {
      this.$refs.item.toggle(false);
    },
    onConfirm() {
      const organ = this.organizationList;
      const organList = [];
      organ.map((item) => {
        if (item.status) {
          organList.push(item.value);
        }
      });
      this.organizationalIds = organList;
      this.data.affiliateadminorgIds = this.organizationalIds;
      this.onRefresh();
    },
    onRefresh() {
      this.data.pages = 1;
      this.list = [];
      this.rankList();
    },
  },
};
</script>

<style lang="less" scoped>
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
/deep/ .van-dropdown-menu__bar {
  box-shadow: none;
  padding: 3px 12px 3px 0px;
}
.btn-box-confirm,
.btn-box-cancel {
  flex: 1;
  text-align: center;
  height: 40px;
  line-height: 40px;
}
.btn-box-confirm {
  color: #d80c1e;
  border-left: 1px solid #ebedf0;
}
.ranking-list {
  background: #f2f2f2;
  height: 100vh;
  box-sizing: border-box;
  .header-box {
    display: flex;
    .search {
      flex: 5;
    }
    .select {
      flex: 1;
    }
  }
}
.ranking-list-box {
  .rank-update {
    opacity: 0.8;
    font-size: 12px;
    color: #ffffff;
    letter-spacing: 0;
    font-weight: 400;
  }
  .rank-list {
    margin-top: 12px;
    background: #ffffff;
    min-height: 100vh;
    .tc {
      text-align: center;
    }
    .tr {
      text-align: right;
    }
    .thead {
      font-size: 12px;
      color: #999999;
      background-color: #fafafa;
      padding: 12px 12px 12px 0px;
      letter-spacing: 0;
      line-height: 18px;
      font-weight: 400;
      margin-bottom: 6px;
    }
    .num {
      font-size: 16px;
      color: #999999;
    }
    .red {
      color: #d80c1e;
    }
    .van-image {
      width: 20px;
      height: 20px;
      margin-right: 6px;
    }
    .flex {
      display: flex;
      align-items: center;
    }
    .tbody {
      font-size: 14px;
      color: #212121;
      letter-spacing: 0;
      line-height: 20px;
      font-weight: 400;
      padding-bottom: 6px;
      padding-right: 12px;
      .van-col {
        padding: 6px 0;
      }
    }
  }
}
</style>
