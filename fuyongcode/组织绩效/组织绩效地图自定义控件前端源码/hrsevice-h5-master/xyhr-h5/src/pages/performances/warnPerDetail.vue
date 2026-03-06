<template>
  <div class="page">
    <div class="userInfo" v-if="empInfo.name">
      <div class="user-info-box" ref="userInfoBox">
        <div class="info-box pd12">
          <div class="flex">
            <div class="avatar">
              <van-image
                round
                width="32px"
                height="32px"
                :src="empInfo.headSculpture"
              />
            </div>
            <div class="info">
              <div>
                <div class="fw-text flex middle">
                  <p class="over-text">{{ empInfo.name }}</p>
                  <p class="ml8 over-text">{{ empInfo.position }}</p>
                </div>
                <div class="plain-text flex middle">
                  <p class="over-text">{{ empInfo.company }}</p>
                  <p class="ml8 over-text">{{ empInfo.organization }}</p>
                </div>
              </div>

              <div class="lightBox">
                <div>
                  <van-image width="7.5" height="10" :src="RED" />
                  <span class="ml4">
                    {{ empInfo.redLightCount }}
                  </span>
                </div>

                <div class="ml12">
                  <van-image width="7.5" height="10" :src="GREEN" />
                  <span class="ml4">
                    {{ empInfo.greenLightCount }}
                  </span>
                </div>
                <div class="ml12">
                  <van-image width="7.5" height="10" :src="YELLOW" />
                  <span class="ml4">{{ empInfo.yellowLightCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="indexList pd12">
      <div class="indexBox flex pd12" v-for="item in list">
        <div class="indexRight">
          <div class="indexInfo flex justify">
            <div class="indexLeft">
              <van-image
                width="11px"
                height="15px"
                :src="RED"
                v-if="item.lightUpType == 'redLight'"
              />
              <van-image
                width="11px"
                height="15px"
                :src="YELLOW"
                v-if="item.lightUpType == 'yellowLight'"
              />
              <van-image
                width="11px"
                height="15px"
                :src="GREEN"
                v-if="item.lightUpType == 'greenLight'"
              />
            </div>
            <div class="indCtrName">{{ item.indCtrName }}</div>
            <div>{{ item.period }}</div>
          </div>
          <div class="indexUserInfo">
            <div
              class="indexDetailBox flex justify"
              @click="getIndexDetail(item.id)"
            >
              <div class="pointBox">评分:{{ item.ies }}</div>
              <div class="lightDetailBox flex justify">
                <div>亮灯次数:{{ item.lightUpCount }}</div>
                <div>详情 <van-icon name="arrow"></van-icon></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <lightDetailPopup :show="show" :detailInfo="detailInfo" @close="close" />
  </div>
</template>

<script>
import { employeeLightView, getIndexDetail } from "@/libs/api.js";
import RED from "@/assets/performances/red.png";
import YELLOW from "@/assets/performances/yellow.png";
import GREEN from "@/assets/performances/green.png";
import lightDetailPopup from "@/pages/performances/components/lightDetailPopup.vue";

export default {
  name: "WarnPerDetail",
  data() {
    return {
      RED,
      YELLOW,
      GREEN,
      show: false,
      empInfo: {},
      list: [],
      detailInfo: {},
    };
  },
  components: {
    lightDetailPopup,
  },
  mounted() {
    this.initData();
  },
  methods: {
    close(value) {
      this.show = value;
    },
    initData() {
      const data = {
        year: this.$route.query.year, //年份
        period: this.$route.query.period, //周期
        actEvalObjId: this.$route.query.id, //评估对象ID
        isSubordinate: this.$route.query.isSubordinate, //仅查看直属下属
      };
      this.$xy.showLoad();
      employeeLightView(data)
        .then((res) => {
          this.empInfo = res.data.data.empInfo;
          this.list = res.data.data.targetAreaInfoList;
        })
        .finally(() => {
          this.$xy.hideLoad();
        });
    },
    getIndexDetail(id) {
      const data = {
        indexId: id,
        actEvalObjId: this.empInfo.actEvalObj,
      };
      getIndexDetail(data).then((res) => {
        this.detailInfo = res.data.data;
        this.show = true;
      });
    },
  },
};
</script>

<style lang="less" scoped>
.page {
  background-color: #f2f2f2;
  min-height: 100vh;
}
.user-info-box {
  min-height: 92px;
  padding: 8px 12px 0 12px;
  background-image: url("@/assets/performances/infoBgc.png");
  background-size: 100% 100%;
  background-repeat: no-repeat;
  padding-bottom: 6px;
  .over-text {
    line-height: 20px;
  }
  .see-box {
    color: #fff;
    line-height: 20px;
    font-size: 14px;
    margin-bottom: 4px;
  }
  .info-box {
    background: white;
    border-radius: 8px;
    .info {
      margin-left: 10px;
      flex: 1;
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
    .total-text {
      font-size: 5.333vw;
      color: #d80c1e;
      font-weight: bold;
    }
    .total-text::after {
      content: "";
      position: relative;
      bottom: 0.533vw;
      left: 0;
      display: block;
      width: 100%;
      height: 1.6vw;
      background: linear-gradient(to right, #ee0a0a14, #fff);
    }
  }
}
.lightBox {
  margin-top: 4px;
  display: flex;
  font-size: 11px;
  div {
    display: flex;
    align-items: center;
  }
  .ml4 {
    margin-left: 4px;
  }
}
.indexList {
  padding-top: 0;
}
.indexBox {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  .indexLeft {
    margin-right: 8px;
    width: 10px;
  }
  .indexRight {
    flex: 1;
    .indexInfo {
      // overflow: hidden;
      // white-space: nowrap;
      // text-overflow: ellipsis;
      .indCtrName {
        flex: 1;
        max-width: 245px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
    }
    .indexUserInfo {
      padding: 8px 0;
      .info {
        margin-left: 8px;
      }
      .indexDetailBox {
        margin-top: 4px;
        font-size: 14px;
        .pointBox {
          width: 100px;
          padding: 8px;
          background-color: #fef4f5;
        }
        .lightDetailBox {
          margin-left: 12px;
          flex: 1;
          padding: 8px;
          background-color: #fef4f5;
        }
      }
    }
  }
}
.lightingDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  .lightingTitle {
    padding: 12px;
    background-color: #f2f2f2;
  }
  .f1 {
    width: 80px;
  }
  .f2 {
    flex: 1;
  }
  .f3 {
    width: 80px;
  }
}
.popup {
  //   max-height: 70vh;
  .topBox {
    font-weight: bold;
  }
}
.popupDetails {
  max-height: 70vh;
  overflow: auto;
  font-size: 14px;
}
.indexDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  .infoListBox {
    padding: 12px;
    display: flex;
    .box1 {
      width: 80px;
    }
    .box2 {
      flex: 1;
    }
  }
}
</style>
