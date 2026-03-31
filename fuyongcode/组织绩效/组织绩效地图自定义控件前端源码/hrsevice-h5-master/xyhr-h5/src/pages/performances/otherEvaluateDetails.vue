<!-- @format -->

<template>
  <div class="pages-bgc myachievementQuery" v-if="rateData.areaInfoList">
    <van-sticky>
      <div class="userInfoBox" ref="userInfoBox">
        <div class="infoBox">
          <div class="pd12">
            <div class="flex middle justify">
              <div>
                <van-image class="avatar" round :src="empInfo.headSculpture" />
              </div>
              <div class="info">
                <div class="fw-text flex middle">
                  <div>{{ empInfo.name }}</div>
                  <div class="ml8">{{ empInfo.position }}</div>
                </div>
                <div class="flex middle">
                  <div class="plain-text">{{ empInfo.company }}</div>
                  <div class="plain-text ml8">{{ empInfo.organization }}</div>
                </div>
              </div>
            </div>
            <div class="flex mt10">
              <div class="exam">{{ rateData.activityName }}</div>
              <!-- <div class="exam">{{ rateData.period }}</div> -->
            </div>
          </div>
          <div class="resBox">
            <van-form ref="form">
              <van-cell-group :border="false">
                <RateField
                  :decimalDigits="decimalDigits"
                  :readonly="
                    status === '2' ||
                    !sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldModifyItem
                  "
                  v-model="
                    sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldValue
                  "
                  :placeholder="
                    '评分上下限：' +
                    rateData.allowMinScore +
                    '-' +
                    rateData.allowMaxScore
                  "
                  :label="sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldName"
                  :rules="[
                    { required: true, message: '请输入评分' },
                    {
                      message: `请输入有效的${rateData.allowMinScore}到${rateData.allowMaxScore}之间的数字`,
                      validator: (value) => {
                        if (
                          value === '' ||
                          Number(value) < rateData.allowMinScore ||
                          Number(value) > rateData.allowMaxScore
                        ) {
                          return false;
                        } else {
                          return true;
                        }
                      },
                    },
                  ]"
                />
                <TextareaField
                  v-if="
                    sumAreaInfo.areaInfoList[0].sumAreaInfo.eval &&
                    sumAreaInfo.areaInfoList[0].sumAreaInfo.eval
                      .fieldDisplayItem
                  "
                  :label-width="60"
                  :readonly="status === '2'"
                  v-model="
                    sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldValue
                  "
                  placeholder="请输入"
                  :label="
                    sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldName
                  "
                  type="textarea"
                  rows="1"
                  :autosize="{ maxHeight: 100 }"
                />
              </van-cell-group>
            </van-form>
          </div>
        </div>
      </div>
    </van-sticky>
    <div class="eval">
      <div class="evalBox">
        <div class="evalText">他人评分详情</div>
        <div
          class="score"
          v-for="otherItem in sumAreaInfo.areaInfoList[0].otherRatingInfoList"
          :key="otherItem.id"
        >
          <div class="flex middle justify mt12">
            <van-image
              class="avatarPer"
              round
              :src="otherItem.empInfo.headSculpture"
            />
            <div class="fw-text flex middle ml8 fs12" style="flex: 1">
              <div>{{ otherItem.empInfo.name }}</div>
              <div class="ml8">{{ otherItem.empInfo.position }}</div>
            </div>
            <div class="flex middle fs12">
              <div class="rate-text" v-if="otherItem.score.isShow">
                <span class="rate-text-label">{{ otherItem.nodeName }}：</span>
                {{ otherItem.score.value }}
              </div>
            </div>
          </div>
          <div class="scoreText" v-if="otherItem.desc.isShow">
            {{ otherItem.desc.value }}
          </div>
        </div>
      </div>
    </div>
    <div class="footerBtn" v-if="status == '1'">
      <van-button class="btn" type="danger" @click="submit(false)"
        >保存</van-button
      >
    </div>
  </div>
</template>

<script>
import {
  getPerformancesRate,
  submitRateTotal,
  getCurrentInfoData,
} from "@/libs/api.js";
import RateField from "@/pages/performances/components/RateField";
import TextareaField from "@/pages/performances/components/TextareaField";
import { Toast } from "vant";
export default {
  name: "OtherEvaluateDetails",
  components: {
    RateField,
    TextareaField,
  },
  data() {
    return {
      sumAreaInfo: {},
      empInfo: {},
      rateData: {},
      decimalDigits: null, //小数位
      status: "1", //1是待办2 是已办
    };
  },
  mounted() {
    this.initGetPerformancesRate();
  },

  methods: {
    async initGetPerformancesRate() {
      const handleId = this.$route.query.id;
      const {
        data: { data: initValue },
      } = await getCurrentInfoData({ id: handleId });
      this.status = initValue.backlogState;
      this.$xy.showLoad();
      let params = {
        handleId: this.$route.query.id,
      };
      getPerformancesRate(params).then((res) => {
        const data = res.data.data;
        const { areaInfoList } = data;
        const rateData = {
          ...data,
          areaInfoList: areaInfoList.filter(
            ({ areaRegNumber }) => areaRegNumber != "epa_sumarea"
          ),
        };
        const sumAreaInfo = {
          ...data,
          areaInfoList: areaInfoList.filter(
            ({ areaRegNumber }) => areaRegNumber === "epa_sumarea"
          ),
        };
        this.sumAreaInfo = sumAreaInfo;
        this.rateData = rateData;
        this.empInfo = res.data.data.empInfo;
        this.decimalDigits = Number(data.numAccuracy);
        console.log("总评分数据", sumAreaInfo);
        this.$xy.hideLoad();
      });
    },
    async submit(type) {
      const params = {
        type,
        data: {
          backlogId: this.rateData.handleId,
          score: this.sumAreaInfo.areaInfoList[0].sumAreaInfo.ose.fieldValue,
          content: this.sumAreaInfo.areaInfoList[0].sumAreaInfo.eval.fieldValue,
        },
      };
      console.log(params, "params");
      const check = await this.validateFormSync();
      if (check) {
        submitRateTotal(params)
          .then((res) => {
            if (res.data.statusCode == 200) {
              Toast(res.data.data);
            }
          })
          .catch((res) => {});
      }
    },
    //全局校验 保存提交时候需要
    validateFormSync() {
      return new Promise((resolve, reject) => {
        this.$refs.form
          .validate()
          .then(() => {
            resolve(true);
          })
          .catch(() => {
            resolve(false);
          });
      });
    },
  },
};
</script>
<style lang="less" scoped>
.footerBtn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 5px 12px 12px 12px;
  background-color: #fff;
  .btn {
    width: 100%;
    border-radius: 4px;
  }
}
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
}
.rate-text {
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
  flex-direction: row;
  margin-right: 6px;
  margin-bottom: 3px;
  font-size: 12px;
  color: #212121;
  border-radius: 4px;
  background: #f2f2f2;
  padding: 3px 6px;
  .rate-text-label {
    color: #666;
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
