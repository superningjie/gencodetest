<template>
  <div class="page safe-bottom">
    <van-sticky>
      <van-dropdown-menu>
        <!-- <van-dropdown-item
          v-model="year"
          :options="yearList"
          @change="search"
        /> -->
        <van-dropdown-item
          v-model="target"
          :options="targetList"
          @change="changeSearch"
        />
      </van-dropdown-menu>
      <div class="tabList">
        <van-search
          v-model="search"
          shape="round"
          placeholder="搜索目标"
          @search="onSearch"
        />
        <van-tabs v-model="active">
          <van-tab title="目标对齐"> </van-tab>
          <van-tab title="目标分解"> </van-tab>
        </van-tabs>
      </div>
    </van-sticky>
    <div class="contentList">
      <div class="contentBox">
        <div class="userInfo flex">
          <div class="line"></div>
          <van-image
            width="30px"
            height="30px"
            round
            :src="userInfo.headSculpture"
            lazy-load
          />
          <div>{{ userInfo.name }} {{ userInfo.number }}</div>
          <div class="switchPer" @click="changePer">
            <van-image round width="13px" height="13px" :src="CUT" />
            <span>切换人员</span>
          </div>
        </div>
        <xy-empty v-if="userInfo.targetInfoList.length == 0"></xy-empty>
        <div
          v-else
          class="targetBox"
          v-for="(i, index) in userInfo.targetInfoList"
        >
          <div class="target" @click="go(i, index)">
            <div class="targetTitle">
              <van-image round width="15px" height="15px" :src="TARGET" />
              <div class="targetText ml8">
                {{ i.targetName }}
              </div>
            </div>
            <div class="targetContent">
              {{ i.targetSpecification }}
            </div>

            <div
              class="targetSub"
              v-if="i.subordinateTargetInfoList.length > 0 && active"
            >
              <div class="subBox" v-for="sub in i.subordinateTargetInfoList">
                <van-image
                  round
                  width="20px"
                  height="20px"
                  :src="sub.subordinateHeadSculpture"
                />
                <span class="ml8">{{ sub.subordinateName }}</span>
              </div>
            </div>

            <div
              class="targetSub"
              v-if="i.superiorTargetInfoList.length > 0 && !active"
            >
              <div class="subBox" v-for="sup in i.superiorTargetInfoList">
                <van-image
                  round
                  width="20px"
                  height="20px"
                  :src="sup.superiorhHeadSculpture"
                />
                <span class="ml8">{{ sup.superiorTargetName }}</span>
              </div>
            </div>
          </div>
          <div
            class="ml8"
            v-if="i.subordinateTargetInfoList.length > 0 && active"
          >
            <van-icon name="arrow" />
          </div>
          <div
            class="ml8"
            v-if="i.superiorTargetInfoList.length > 0 && !active"
          >
            <van-icon name="arrow" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import {
  getTargetPlan,
  getTargetDisassembly,
  getTargetDisassemblySub,
  superiorTarget,
} from "@/libs/api.js";
import CUT from "@/assets//performances/cut.png";
import TARGET from "@/assets//performances/target.png";

import { Toast } from "vant";
export default {
  name: "IndividualGoal",
  data() {
    return {
      CUT,
      TARGET,
      year: 2024,
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
      target: "",
      targetList: [],
      active: 1,
      search: "",
      resolveValue: "",
      userInfo: {
        targetInfoList: [],
      },
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.getTargetPlan();
    },
    getTargetPlan() {
      const data = {
        personId: this.$route.query.id || "",
      };
      getTargetPlan(data).then((res) => {
        if (res.data.data.targetList) {
          this.target = res.data.data.targetList[0]?.targetId;
          this.targetList = this.arrayTransition(res.data.data.targetList);
          this.getTargetDisassembly(res.data.data.personId);
        } else {
          console.log(res.data.message, "res.data.message");
          Toast(res.data.message);
        }
      });
    },
    backPage() {
      const toast = Toast.loading({
        duration: 0, // 持续展示 toast
        forbidClick: true,
        message: "三秒后跳回人员列表",
      });

      let second = 3;
      const timer = setInterval(() => {
        second--;
        if (second) {
          toast.message = ` ${second} 秒后跳回人员列表`;
        } else {
          clearInterval(timer);
          // 手动清除 Toast
          Toast.clear();
          this.$router.push({
            path: "teamGoal",
          });
        }
      }, 1000);
    },
    changePer() {
      this.$router.push({
        path: "teamGoal",
      });
    },
    changeSearch(v) {
      console.log(v, "v");
      this.getTargetDisassembly();
    },
    onSearch() {
      this.getTargetDisassembly();
    },
    getTargetDisassembly(id) {
      const userId = this.$route.query.id ? this.$route.query.id : id;
      const data = {
        requestVO: {
          // year: this.year,
          // targetPlanId: "1913951134939214848",
          targetPlanId: this.target,
          userId: userId,
          search: this.search,
        },
      };
      this.$xy.showLoad();
      getTargetDisassembly(data).then((res) => {
        // if (res.data.data.targetInfoList.length) {
        this.userInfo = res.data.data;
        this.$store.commit("setUserTargetInfo", this.userInfo);
        // } else {
        //   this.backPage();
        // }
        this.$xy.hideLoad();
      });
    },
    arrayTransition(obj) {
      const handleStatusList = obj;
      return handleStatusList.map((item) => {
        // const key = Object.keys(item)[0];
        return {
          text: item.targetName,
          value: item.targetId,
        };
      });
    },
    async go(item, index) {
      // const data = {
      //   requestVO: {
      //     targetId: item.targetId,
      //   },
      // };
      // console.log(item, "item");

      if (this.active) {
        const res = item.subordinateTargetInfoList.length;
        // const res = await getTargetDisassemblySub(data);
        res ? this.navigator(item.targetId, index) : "";
      } else {
        const res = item.superiorTargetInfoList.length;
        // const res = await superiorTarget(data);
        res ? this.navigator(item.targetId, index) : "";
      }
    },
    navigator(id, index) {
      const path = this.active ? "targetSubordinate" : "targetSuperior";
      this.$router.push({
        path: path,
        query: {
          id: id,
          index: index,
          // targetPlanId: this.target,
        },
      });
    },
  },
};
</script>

<style lang="less" scoped>
.page {
  background-color: #f2f2f2;
  min-height: 100vh;
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
  }
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom));
}

.contentList {
  padding: 12px;
  .contentBox {
    border-radius: 8px;
    background-color: #fff;
    .userInfo {
      position: relative;
      align-items: center;
      border-bottom: 1px solid #f2f2f2;
      .switchPer {
        position: absolute;
        right: 12px;
        font-size: 12px;
        color: #999999;
        display: flex;
        align-items: center;
        .rotate90 {
          rotate: 90deg;
        }
      }
      .line {
        width: 4px;
        height: 20px;
        background: #d80c1e;
        border-radius: 0px 2px 2px 0px;
      }
      .van-image {
        width: 32px;
        height: 32px;
        margin: 12px 8px;
      }
    }
    .targetBox {
      display: flex;
      align-items: center;
      padding: 12px;
      justify-content: space-between;
      border-bottom: 1px solid #fef5f6;
      .target {
        display: flex;
        flex-direction: column;
        .targetTitle {
          display: flex;
          align-items: center;
          font-size: 15px;
          color: #212121;
          line-height: 24px;
          font-weight: 500;
          .targetText {
            width: 265px;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
          }
        }
        .targetContent {
          font-size: 12px;
          color: #666666;
          line-height: 24px;
          font-weight: 400;
          word-break: break-all;
        }
        .targetSub {
          display: flex;
          align-items: center;
          font-size: 12px;
          margin-top: 12px;
          background-color: #fef5f6;
          max-width: 300px;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: auto;
          .subBox {
            margin-left: 8px;
            padding: 5px;
            display: flex;
            align-items: center;
          }
        }
      }
    }
  }
}
</style>
