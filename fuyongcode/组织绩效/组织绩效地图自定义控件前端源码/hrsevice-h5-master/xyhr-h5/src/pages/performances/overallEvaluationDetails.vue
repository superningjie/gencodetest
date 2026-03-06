<!-- @format -->

<template>
  <div class="pages-bgc myachievementQuery">
    <van-sticky>
      <div class="userInfoBox" ref="userInfoBox">
        <div class="infoBox">
          <div class="pd12" style="padding-bottom: 0">
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
            </div>
            <div class="flex mt10">
              <div class="exam">{{ userInfo.activityName }}</div>
              <!-- <div class="exam">{{ userInfo.cycle }}</div> -->
            </div>
          </div>
          <div class="resBox">
            <van-field
              v-model="v.content"
              :label="v.title"
              input-align="right"
              readonly
              error
              v-for="(v, i) in totalPointsList"
              :key="i"
            />
          </div>
        </div>
      </div>
    </van-sticky>
    <div class="eval">
      <div class="evalBox">
        <div class="evalText">他人评分详情</div>
        <div class="tabcontent mt12" v-for="item in list">
          <div class="user-box">
            <van-image
              class="head-img"
              round
              :src="item.employeePojo.employeePhoto"
            />
            <div class="con ov-text">
              {{ item.employeePojo.employeeName }}
              {{ item.employeePojo.jobTitle }}
            </div>
            <div class="right">
              <div class="rate-text">
                <div>
                  <i class="c6"> {{ item.employeePojo.performanceResult }}：</i
                  >{{ item.totalPoints }}
                </div>
              </div>
            </div>
          </div>
          <div class="comment">
            {{ item.content }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllAreaEvaluateDate } from "@/libs/api.js";
export default {
  name: "OverallEvaluationDetails",
  data() {
    return {
      userInfo: {
        totalPoints: 92,
        grade: "",
        jobTitle: "",
        department: "",
        employeeName: "",
        company: "",
        id: "",
        taskId: null,
        employeePhoto: "",
        activityName: "",
        cycle: "",
      },
      list: [],
      totalPointsList: [],
    };
  },
  mounted() {
    this.init();
  },

  methods: {
    init() {
      this.$xy.showLoad();
      this.id = this.$route.params.id;
      this.userInfo = this.$route.params.userInfo;
      this.totalPointsList = this.$route.params.list;

      const data = {
        assessmentObjectId: this.id,
      };
      getAllAreaEvaluateDate(data).then((res) => {
        this.$xy.hideLoad();
        const detailData = res.data.data;
        this.list = detailData.data;
        console.log(this.list);
      });
    },
  },
};
</script>
<style lang="less" scoped>
/deep/ .van-tabs__content {
  margin-top: 8px;
}
.ml4 {
  margin-left: 4px;
}
.subIcon {
  width: 8px;
  height: 8px;
  border-bottom: 1px solid #979797;
  border-left: 1px solid #979797;
}
.stopClick {
  position: absolute;
  left: 0;
  top: 0;
  width: 60vw;
  height: 37px;
  background: transparent;
  z-index: 1;
}
.myachievementQuery {
  padding-bottom: env(safe-area-inset-bottom);
}
.pages-bgc {
  background-color: #f2efef;
}
.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 130px;
}

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
    .resBox {
      padding-bottom: 12px;
    }
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
.eval {
  padding: 12px;
  padding-top: 0;
  .evalBox {
    background: white;
    border-radius: 8px;
    padding: 12px;
    .evalText {
      color: #666;
    }
  }
}

.tabcontent {
  .comment {
    padding-top: 6px;
    font-size: 12px;
    color: #212121;
    line-height: 18px;
  }
  .user-box {
    display: flex;
    align-items: center;
    box-sizing: border-box;
    width: 100%;
    .head-img {
      width: 24px;
      height: 24px;
    }
    .con {
      font-size: 14px;
      color: #212121;
      letter-spacing: 0;
      line-height: 20px;
      font-weight: bold;
      flex: 1;
      padding: 0 12px;
    }
    .num {
      font-size: 20px;
      color: #d80c1e;
      letter-spacing: 0;
      font-weight: bold;
      position: relative;
      .line {
        position: absolute;
        left: 0;
        right: 0;
        bottom: -1px;
        background: linear-gradient(to right, #ff8993, #fff);
        height: 3px;
        border-radius: 50%;
      }
    }
  }
  .rate-text {
    display: inline-flex;
    flex-wrap: nowrap;
    align-items: center;
    flex-direction: row;
    font-size: 12px;
    margin-right: 6px;
    background: #f2f2f2;
    border-radius: 4px;
    padding: 6px;
  }
}

.score {
  .avatarPer {
    width: 20px;
    height: 20px;
  }
  .iconRate {
    width: 12px;
    height: 12px;
  }
  .rate1Bgc {
    padding: 4px;
    background: rgba(239, 249, 248, 1);
    color: #1a9e98;
    border-radius: 4px;
  }
  .ratingBgc {
    padding: 4px;
    background: rgba(253, 245, 246, 1);
    color: rgba(216, 12, 30, 1);
    border-radius: 4px;
  }
  .rate2Bgc {
    padding: 4px;
    background: rgba(241, 247, 254, 1);
    color: #157de7;
    border-radius: 4px;
  }
  .scoreText {
    font-size: 12px;
    padding: 8px 0;
    line-height: 18px;
  }
}
.footer {
  width: 100vw;
  box-sizing: border-box;
  padding: 8px 12px 20px 12px;
  background-color: #fff;
  position: fixed;
  bottom: 0;
  font-weight: bold;
  .back {
    flex: 1;
  }
  .pass {
    margin-left: 12px;
    flex: 1;
  }
}
</style>
