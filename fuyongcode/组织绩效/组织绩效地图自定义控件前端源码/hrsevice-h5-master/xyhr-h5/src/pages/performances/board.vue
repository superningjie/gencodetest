<template>
  <div class="page">
    <div class="tabsBox">
      <div class="tabsTop">
        <!-- <div
          class="box"
          :class="active == 1 ? 'active' : ''"
          @click="active = 1"
        >
          <div>进度跟踪</div>
        </div> -->
        <div
          class="box"
          :class="active == 2 ? 'active' : ''"
          @click="active = 2"
        >
          <div>等级分布</div>
        </div>
        <div
          class="box"
          :class="active == 3 ? 'active' : ''"
          @click="active = 3"
        >
          <div>预警分析</div>
        </div>
        <div
          class="box"
          :class="active == 4 ? 'active' : ''"
          @click="active = 4"
        >
          <div>绩效波动</div>
        </div>
        <!-- <div class="box" @click="active = 5">
          <div>亮点盲点</div>
        </div> -->
      </div>
    </div>

    <div class="pd12">
      <div class="content" v-if="active == 1">
        <ProgressTracking :years="years" />
      </div>
      <div class="content" v-if="active == 2">
        <RankedDistribution
          :posList="posList"
          :orgsTreeData="orgsTreeData"
          :years="years"
        />
      </div>
      <div class="content" v-if="active == 3">
        <WarnAnalyse :cycleData="cycleData" :years="years" />
      </div>
      <div class="content" v-if="active == 4">
        <PerUndulate :cycleData="cycleData" :years="years" />
        <!-- <div class="dividerBox"></div>
        <brightScotoma /> -->
      </div>
      <!-- <div class="content" v-if="active == 5">
        <brightScotoma />
      </div> -->
    </div>
  </div>
</template>

<script>
import RankedDistribution from "@/pages/performances/components/RankedDistribution.vue";
import ProgressTracking from "@/pages/performances/components/ProgressTracking.vue";
import WarnAnalyse from "@/pages/performances/components/warnAnalyse.vue";
import PerUndulate from "@/pages/performances/components/perUndulate.vue";
import brightScotoma from "@/pages/performances/components/brightScotoma.vue";
import {
  getOrgBoard,
  getCycle,
  getPosBoard,
  orgsManagerTree,
} from "@/libs/api.js";
export default {
  name: "Board",
  components: {
    ProgressTracking,
    RankedDistribution,
    WarnAnalyse,
    PerUndulate,
    brightScotoma,
  },
  data() {
    return {
      active: 2,
      cycleData: {},
      cycleArr: [],
      posList: [],
      orgsTreeData: [],
      years: [],
    };
  },
  created() {
    this.initData();
    this.getYearList();
  },
  mounted() {},
  methods: {
    initData() {
      this.getCycle();
      this.getOrgBoard();
      this.getPosBoard();
      // this.orgsManagerTree();
    },
    getYearList() {
      const currentYear = new Date().getFullYear();
      const index = currentYear - 2018;
      const years = [];

      for (let i = 0; i < index; i++) {
        years.push((currentYear - i).toString());
      }
      this.years = years;
    },
    orgsManagerTree() {
      const data = {
        retireFlag: false,
        org: "",
      };
      orgsManagerTree(data).then((res) => {
        this.orgsTreeData = res.data.data;
      });
    },
    getCycle() {
      getCycle().then((res) => {
        this.cycleData = res.data.data;
        this.setCycleName();
      });
    },
    setCycleName() {
      this.cycleArr = this.cycleTransition(this.cycleData);
    },
    // 返回周期名称集合
    cycleTransition(obj) {
      return Object.keys(obj).map((key) => {
        return obj[key];
      });
    },
    getOrgBoard() {
      getOrgBoard().then((res) => {
        this.orgsTreeData = res.data.data;
      });
    },
    getPosBoard() {
      getPosBoard().then((res) => {
        this.posList = this.arrayTransition(res.data.data);
      });
    },
    arrayTransition(obj) {
      return Object.keys(obj).map((key) => {
        return {
          text: obj[key],
          value: key,
        };
      });
    },
    search() {},
  },
};
</script>

<style lang="less" scoped>
.page {
  background: #f2f2f2;
  min-height: 100vh;
}
.tabsBox {
  background: #fff;
  padding: 10px 16px;
  border-radius: 0 0 25px 25px;
  .tabsTop {
    display: flex;
    // justify-content: space-around;
    background-color: #f5f5f5;
    border-radius: 15px;
    .box {
      flex: 1;
      text-align: center;
      font-size: 13px;
      color: #666666;
      height: 30px;
      line-height: 30px;
      border-radius: 15px;
      background-color: #f5f5f5;
    }
    .active {
      background-color: #e3423f;
      color: #fff;
    }
  }
}
.content {
  // background-color: #fff;
  // border-radius: 10px;
  .dividerBox {
    width: 100%;
    height: 12px;
    background-color: #f2f2f2;
  }
}
</style>
