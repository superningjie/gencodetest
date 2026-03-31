<template>
  <!-- 绩效评估指标详情 -->
  <div class="pi-details safe-bottom" v-if="userInfo.employeeName">
    <div class="pi-details-top">
      <div class="user-box">
        <van-image class="head-img" round :src="userInfo.employeePhoto" />
        <div class="con">
          {{ userInfo.employeeName }} {{ userInfo.jobTitle }}
        </div>
        <div class="right">
          <div class="num">
            {{ userInfo.scoreResult }}
            <i v-if="userInfo.gradeResult && userInfo.scoreResult">/</i>
            {{ userInfo.gradeResult }}

            <i class="line"></i>
          </div>
        </div>
      </div>
    </div>
    <div class="pi-details-con" v-if="data[active]">
      <div class="tabcontent mt10">
        <div class="tabcontent-top">
          <div class="subTitleBgc fw-text flex middle">
            <van-image width="16px" height="16px" :src="ICON" />
            <div class="flex middle ml12 flex1 justify">
              <div
                class="ovText ov-text flex1"
                @click="seeMore(data[active].title)"
              >
                {{ data[active].title }}
              </div>
              <div class="flex middle">
                <div class="flex middle" v-if="data[active].weight > 0">
                  <van-circle
                    v-model="data[active].weight"
                    :rate="data[active].weight"
                    color="#ff0000"
                    size="16"
                    layer-color="#999"
                    :stroke-width="160"
                  />
                  <p class="ml4">{{ data[active].weight }}%</p>
                </div>
                <div v-else>分值：{{ data[active].weightNum }}</div>
                <div class="col-line" v-show="data[active].score"></div>
                <div class="point fs16 fwb ml8">{{ data[active].score }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="subcontent-box">
          <div class="subcontent plain-text">
            <!-- <div class="content">{{ data[active].type }}</div> -->
            <div v-for="msg in data[active].unMessageList">
              <p>
                <i>{{ msg.title }}：</i>{{ msg.content }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <div class="tabcontent mt10" v-for="item in data[active].evaluationList">
        <div class="pd12">
          <div class="user-box">
            <van-image class="head-img" round :src="item.reviewerPhoto" />
            <div class="con ov-text">
              {{ item.reviewerName }} {{ item.reviewGrade }}
            </div>
            <div class="right">
              <div class="rate-text">
                <div>
                  <i class="c6"> {{ item.relation }}：</i
                  >{{ item.reviewerScore }}
                </div>
              </div>
            </div>
          </div>
          <div class="comment">
            {{ item.reviewerContent }}
          </div>
        </div>
      </div>
    </div>
    <div class="footer-fixed">
      <div class="btn-page">
        <van-row type="flex" ustify="space-between" gutter="12">
          <van-col span="6" class="prev" @click="up">
            <span><van-icon name="arrow-left" />上一条</span>
          </van-col>
          <van-col span="12" class="title"
            >{{ areaName }}（{{ active + 1 }}/{{ data.length }}）</van-col
          >
          <van-col span="6" class="next" @click="next">
            <span>下一条 <van-icon name="arrow" /></span>
          </van-col>
        </van-row>
      </div>
    </div>
  </div>
</template>
<script>
import ICON from "@/assets/performances/icon4.png";
import { Toast } from "vant";
import { getPerformanceDetails, getResultData } from "@/libs/api.js";

export default {
  name: "PerformancesAppraiseDetails",
  data() {
    return {
      ICON,
      areaName: "",
      data: [],
      active: 0,
      userInfo: {},
    };
  },

  mounted() {
    this.init();
  },

  methods: {
    init() {
      this.$xy.showLoad();
      this.id = this.$route.query.id;
      this.active = +this.$route.query.active;
      const isRes = JSON.parse(this.$route.query.res);
      if (isRes) {
        const data = {
          mybacklogId: this.$route.query.id,
        };
        getResultData(data).then((res) => {
          const detailData = res.data.data;
          this.data = detailData.data[this.$route.query.index].areaMessage;
          this.areaName = detailData.data[this.$route.query.index].title;
          this.userInfo = detailData.employeePojo;
          this.$xy.hideLoad();
        });
      } else {
        const data = {
          assessmentObjectId: this.id,
        };
        getPerformanceDetails(data).then((res) => {
          const detailData = res.data.data;
          this.userInfo = detailData.employeePojo;
          this.data = detailData.data[this.$route.query.index].areaMessage;
          this.areaName = detailData.data[this.$route.query.index].title;
          this.$xy.hideLoad();
        });
      }
    },
    seeMore(val) {
      Toast(val);
    },
    up() {
      if (this.active > 0) {
        this.active--;
        return;
      }
      Toast("已经是第一条了");
    },

    next() {
      if (this.data.length > this.active + 1) {
        this.active++;
        return;
      }

      Toast("最后一条了");
    },
  },
};
</script>

<style lang="less" scoped>
.ml4 {
  margin-left: 4px;
}
.ovText {
  max-width: 160px;
}
.flex1 {
  flex: 1;
}
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
.subTitleBgc {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  padding: 8px 12px;
  background: linear-gradient(
    to right,
    rgba(238, 10, 10, 0.078) 0%,
    rgb(255, 255, 255) 100%
  );
  // background: linear-gradient(to right, #ee0a0a14, #fff);
  // background: #f3cacd;
  flex: 1;
}
.pi-details {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
}
.pi-details-top {
  background: #fff;
  padding: 10px 12px;
}
.pi-details-con {
  padding: 12px;
  /deep/ .tabcontent {
    background: white;
    border-radius: 8px;
    .tabcontent-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .point {
      color: #d80c1e;
      font-size: 20px;
    }

    .labelText {
      padding: 6px 12px;
    }
    .subcontent-box {
      padding: 12px;
    }
    .subcontent {
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;
      .content {
        background: #ededed;
        border-radius: 4px;
        padding: 2px 4px;
        display: inline-block;
        margin-bottom: 6px;
      }
      i {
        color: #666;
      }
    }
    .comment {
      padding-top: 6px;
      font-size: 12px;
      color: #212121;
      line-height: 18px;
    }
  }
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
    // max-width: 130px;
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
.right {
  text-align: right;
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

  .van-image {
    margin-right: 3px;
  }
}
.rt01 {
  color: #1a9e98;
  background: rgba(27, 163, 157, 0.07);
  border-radius: 4px;
  padding: 6px;
}
.rt02 {
  background: rgba(21, 125, 231, 0.06);
  border-radius: 4px;
  color: #157de7;
  padding: 6px;
}
.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
}
.btn-page {
  line-height: 24px;
  padding: 12px;
  position: relative;
  &::before {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    top: 0;
    left: 0;
    border-bottom: 1px solid #e5e5e5;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
  }
  &::after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    bottom: 0;
    left: 0;
    border-bottom: 1px solid #e5e5e5;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
  }

  .prev {
    font-size: 14px;
    color: #d80c1e;
  }
  .title {
    color: #212121;
    text-align: center;
  }
  .next {
    text-align: right;
    font-size: 14px;
    color: #d80c1e;
  }
}

.btn-box {
  padding: 12px;
  .btn {
    width: 100%;
  }
}
</style>
