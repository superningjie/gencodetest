<template>
  <div class="add-appraiser">
    <div class="header">
      <div class="search-box">
        <div class="left">
          <van-search
            :left-icon="require('@/assets/personage-plans/search.png')"
            v-model="keyword"
            shape="round"
            placeholder="输入姓名搜索"
            @blur="handleSearch"
          />
        </div>
        <div class="right" @click="goOrg">
          <van-image
            fit="cover"
            width="15px"
            height="15px"
            :src="require('@/assets/personage-plans/organization.png')"
          ></van-image>
          <span>组织</span>
        </div>
      </div>
    </div>
    <div class="content">
      <van-radio-group v-model="result" v-if="appraiserList.length">
        <van-list
          v-model="loading"
          :finished="finished"
          finished-text="没有更多了"
          :offset="0"
          @load="onLoad"
        >
          <div
            class="content-item"
            v-for="(item, index) in appraiserList"
            :key="index"
          >
            <div class="item-left">
              <van-image
                style="margin-right: 11px"
                round
                fit="cover"
                width="40px"
                height="40px"
                :src="item.personImg"
              ></van-image>
              <div>
                <div class="top">
                  <span
                    >{{ item.personName }}
                    {{ item.personPosition ? " | " + item.personPosition : ""
                    }}{{
                      item.positionLevel ? " | " + item.positionLevel : ""
                    }}</span
                  >
                </div>
                <div class="bottom">
                  <span>{{ item.personCompany }}</span>
                  <span>{{ item.personOrg }}</span>
                </div>
              </div>
            </div>
            <div class="item-right">
              <van-radio
                shape="square"
                :name="item.number"
                checked-color="#ee0a24"
              ></van-radio>
            </div>
          </div>
        </van-list>
      </van-radio-group>
      <xy-empty v-else></xy-empty>
    </div>
    <div class="operation-box">
      <span class="submit" @click="handleSubmit">提交</span>
    </div>
    <!-- 组织选择弹窗 -->
    <!-- <van-popup v-model="showOrganization" class="organization-box">
      <Organization
        @updateData="updateData"
        :showOrganization="showOrganization"
      />
    </van-popup> -->
  </div>
</template>

<script>
import { getAlltTalents, addReplenishers } from "@/libs/api.js";
// import Organization from "../talent-board/components/organization.vue";
import { Toast } from "vant";
export default {
  // components: { Organization },
  data() {
    return {
      result: "",
      activeCollapse: ["selected"],
      appraiserList: [],
      pageNum: 1,
      keyword: "",
      ids: [],
      total: 0,
      loading: false,
      finished: false,
      // showOrganization: false,
      planId: null,
      AllData: JSON.parse(localStorage.getItem("AllData")) || [],
      plansParams: JSON.parse(localStorage.getItem("plansParams")) || {},
      examineParams: JSON.parse(localStorage.getItem("examineParams")) || {},
    };
  },
  mounted() {
    this.ids = this.plansParams.FineResult
      ? this.plansParams.FineResult.map((ite) => ite.id)
      : [];
    if (this.AllData.length == 0) {
      this.getAllData();
    }
    this.planId = this.examineParams.planId;
    this.getList();
  },
  methods: {
    // 获取所有人才档案数据--员工数据
    getList() {
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      });
      let params = {
        keyword: this.keyword,
        ids: this.ids, //组织过滤
        pageNum: this.pageNum,
      };
      getAlltTalents(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == 200) {
          Toast.clear();
          this.total = res.data.data.total;
          let parsedData = res.data.data.data;
          // 将数组中的每个元素转换为对象
          if (parsedData.length) {
            parsedData = parsedData.map((item) => JSON.parse(item));
          }
          this.appraiserList = parsedData;
          if (this.appraiserList.length == this.total) {
            this.finished = true;
          }
        } else {
          Toast.fail(res.data.message);
          Toast.clear();
          this.appraiserList = [];
        }
        this.loading = false;
      });
    },
    getAllData() {
      let params = {
        keyword: "",
        ids: [],
        pageNum: 2000,
      };
      getAlltTalents(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == 200) {
          let AllData = res.data.data.data;
          if (AllData.length) {
            AllData = AllData.map((item) => JSON.parse(item));
          }
          console.log(res.data.data, "AllData", AllData);
          this.AllData = AllData;
          localStorage.setItem("AllData", JSON.stringify(AllData));
        }
      });
    },
    handleSubmit() {
      if (this.result) {
        this.examineParams = {
          ...this.examineParams,
          result: this.result,
          resultName: this.handlePersonName(this.result),
        };
        localStorage.setItem(
          "examineParams",
          JSON.stringify(this.examineParams)
        );
        // this.$router.push({
        //   name: "plans-examine",
        //   // query: {
        //   //   result: this.result,
        //   //   resultName: this.handlePersonName(this.result),
        //   //   planId: this.$route.query.planId,
        //   //   examineValue: this.$route.query.examineValue,
        //   //   tabIndex: this.$route.query.tabIndex,
        //   // },
        // });
        this.$router.go(-1);
      } else {
        Toast.fail("请选择评价人员");
        return;
      }
    },
    onLoad() {
      if (this.appraiserList.length < this.total) {
        this.pageNum += 1;
        this.getList();
      }
    },
    handleSearch() {
      this.pageNum = 1;
      this.appraiserList = [];
      this.loading = false;
      this.finished = false;
      this.getList();
    },
    // updateData(paramsObj) {
    //   if (paramsObj) {
    //     let { FineResult } = paramsObj;
    //     this.ids = FineResult.map((ite) => ite.id);
    //   } else {
    //     this.ids = [];
    //   }
    //   this.showOrganization = false;
    //   this.handleSearch();
    // },
    // 员工名称处理
    handlePersonName(number) {
      let PersonName = "";
      this.AllData.forEach((ite) => {
        if (ite.number == number) {
          console.log(ite, "ite");
          PersonName = ite.personName;
        }
      });
      return PersonName;
    },
    goOrg() {
      localStorage.removeItem("plansParams");
      this.$router.push({
        name: "board-organization",
        query: {
          isPlans: true,
        },
      });
    },
  },
};
</script>

<style lang="less" scoped>
.add-appraiser {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #eff1f4;
  .header {
    background-color: #ffffff;
    width: 100%;
    z-index: 99;
    .search-box {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .left {
        width: 300px;
        /deep/ .van-field__left-icon {
          display: flex;
          align-items: center;
        }
      }
      .right {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 5px;
        span {
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 15px;
          color: #d80c1e;
        }
      }
    }
  }
  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px; /* 设置间距 */
  }
  .title {
    font-family: PingFang SC;
    font-weight: 500;
    font-size: 13px;
    color: #999999;
  }
  .content {
    flex: 1;
    overflow: auto;
    padding-bottom: 14px;
    margin: 0 16px;
    .content-item {
      margin-top: 14px;
      padding: 17px 12px 17px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      background-color: #ffffff;
      box-shadow: 0px 0px 12px 0px rgba(0, 0, 0, 0.08);
      border-radius: 5px;
      .item-left {
        margin-right: 10px;
        display: flex;
        align-items: center;
        font-family: PingFang SC;
        font-weight: 500;
        .top {
          margin-bottom: 12px;
          line-height: 16px;
          font-size: 15px;
          color: #333333;
        }
        .bottom {
          box-sizing: border-box;
          font-size: 13px;
          color: #999999;
          span {
            padding-right: 12px;
            border-right: 1px solid #999999;
          }
          span:last-child {
            padding-right: 0px;
            padding-left: 12px;
            border-right: none;
          }
        }
      }
      .item-right {
      }
    }
  }
  .operation-box {
    // margin-top: 12px;
    background: #ffffff;
    display: flex;
    padding: 10px 16px;
    align-items: center;
    span {
      flex: 1;
      padding: 16px 0;
      border-radius: 4px;
      text-align: center;
      font-family: PingFang SC;
      font-weight: 500;
      font-size: 18px;
    }
    .submit {
      color: #ffffff;
      background: #d80c1e;
    }
  }
  .organization-box {
    width: 100%;
    height: 100%;
  }
}
/deep/ .van-radio__icon .van-icon {
  border-radius: 50%;
}
</style>
