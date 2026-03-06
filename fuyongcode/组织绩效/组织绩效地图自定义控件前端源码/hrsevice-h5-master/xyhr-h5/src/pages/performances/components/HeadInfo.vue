<template>
  <div class="userInfoBox">
    <div class="infoBox pd12">
      <div class="flex middle justify">
        <div>
          <van-image class="avatar" round :src="userInfo.employeePhoto" />
        </div>
        <div class="info">
          <div class="fw-text flex middle">
            <div>{{ userInfo.employeeName }}</div>
            <div class="ml8">{{ userInfo.jobTitle }}</div>
          </div>
          <div class="flex middle">
            <div class="plain-text">
              {{ userInfo.company }}
            </div>
            <div class="plain-text ml8">{{ userInfo.department }}</div>
          </div>
        </div>
        <div class="totalPointsText">
          {{ userInfo.scoreResult }}
          <i v-show="userInfo.scoreResult && userInfo.gradeResult">/</i>
          {{ userInfo.gradeResult }}
        </div>
      </div>
      <div class="flex mt10">
        <div class="exam">{{ userInfo.activityName }}</div>
        <!-- <div class="exam">{{ userInfo.cycle }}</div> -->
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    userInfo: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  data() {
    return {};
  },
};
</script>

<style lang="less" scoped>
.userInfoBox {
  padding: 8px 12px 10px 12px;
  background-image: url("@/assets/performances/infoBgc.png");
  background-size: 100% 100%;
  background-repeat: no-repeat;
  .totalPointsText {
    font-size: 20px;
    color: #d80c1e;
    font-weight: bold;
  }
  .totalPointsText::after {
    content: "";
    position: relative;
    bottom: 2px;
    left: 0;
    display: block;
    width: 100%;
    height: 6px;
    background: linear-gradient(to right, #ee0a0a14, #fff);
  }
  .seeBox {
    color: #fff;
    line-height: 20px;
    font-size: 14px;
    margin-bottom: 4px;
  }
  .infoBox {
    background: white;
    border-radius: 8px;
    .avatar {
      width: 32px;
      height: 32px;
    }
    .info {
      flex: 1;
      margin-left: 10px;
    }
    .exam {
      opacity: 0.8;
      font-size: 12px;
      color: #d80c1e;
      padding: 4px;
      background: #ffebea;
      margin-right: 8px;
      border-radius: 4px;
    }
  }
}
</style>
