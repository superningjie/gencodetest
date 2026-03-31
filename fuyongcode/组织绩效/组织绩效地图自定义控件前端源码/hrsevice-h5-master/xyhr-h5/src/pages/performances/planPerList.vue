<template>
  <div class="pageList">
    <div class="lightingDetailsBox">
      <div class="flex lightingTitle">
        <div class="f1">姓名</div>
        <div class="f2">所属公司</div>
        <div class="f3">所属部门</div>
        <div class="f4">岗位</div>
      </div>
      <div class="lightingListBox">
        <div class="flex pd12 lightingList" v-for="i in list">
          <div class="f1">{{ i.employeeName }}</div>
          <div class="f2">{{ i.company }}</div>
          <div class="f3">{{ i.department }}</div>
          <div class="f4">{{ i.jobTitle }}</div>
        </div>
      </div>
    </div>
    <div class="footer flex justify">
      <!-- <van-button class="back">一键催办</van-button> -->
      <van-button type="danger" class="pass" @click="back()">返回</van-button>
    </div>
  </div>
</template>

<script>
import { getPieChartInfo } from "@/libs/api.js";
export default {
  name: "PlanPerList",
  data() {
    return {
      ids: [],
      list: [],
    };
  },
  created() {
    this.ids = this.$store.state.progressTrackingIds;
    console.log(this.ids, " this.ids");
  },
  mounted() {
    this.getInfo();
  },
  methods: {
    getInfo() {
      const data = {
        actevalobjList: this.ids,
      };
      getPieChartInfo(data).then((res) => {
        this.list = res.data.data;
      });
    },
    back() {
      this.$router.go(-1);
    },
  },
};
</script>

<style lang="less" scoped>
.pageList {
  background-color: #f2f2f2;
  min-height: 100vh;
}
.lightingDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  background-color: #fff;
  padding-bottom: 12px;

  .lightingTitle {
    padding: 12px;
    background-color: #d80c1e;
    color: #fff;
    display: flex;
    justify-content: space-around;
  }
  .lightingListBox {
    max-height: 78vh;
    overflow: auto;
    font-size: 14px;
  }
  .lightingList {
    display: flex;
    justify-content: space-around;
    border-bottom: 1px solid #f2f2f2;
    div {
      padding: 0 2px;
    }
  }
  .f1 {
    width: 80px;
  }
  .f2 {
    flex: 1;
  }
  .f3 {
    flex: 1;
  }
  .f4 {
    width: 80px;
  }
}
.footer {
  background-color: #fff;
  width: 100vw;
  box-sizing: border-box;
  padding: 8px 12px 20px 12px;
  position: fixed;
  bottom: 0;
  font-weight: bold;
  .back {
    flex: 1;
    color: #d80c1e;
    border: 1px solid #d80c1e;
  }
  .pass {
    flex: 1;
  }
}
</style>
