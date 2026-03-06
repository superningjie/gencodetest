<template>
  <div class="page">
    <div class="contentList">
      <div class="contentBox">
        <div class="userInfo flex">
          <div class="line"></div>
          <van-image
            round
            width="30px"
            height="30px"
            :src="userTargetInfo.headSculpture"
            lazy-load
          />
          <div>{{ userTargetInfo.name }} {{ userTargetInfo.number }}</div>
        </div>
        <div class="targetBox pd12">
          <div class="target">
            <div class="targetTitleBox">
              <div class="targetTitle pdb8">
                <van-image round width="15px" height="15px" :src="TARGET" />
                <div class="ml8 targetText">
                  {{ userTargetInfo.targetInfoList[index].targetName }}
                </div>
              </div>
              <div
                class="lightingDetails"
                @click="show = true"
                v-if="detailList.length"
              >
                <van-image
                  round
                  width="9px"
                  height="12px"
                  :src="RED"
                  v-if="isShowRed"
                />
                <van-image round width="9px" height="12px" :src="GARY" v-else />
                <span class="ml4"> 亮灯详情 </span>
              </div>
              <div class="lightingDetails" v-else>
                <span class="ml4"> 未关联绩效指标 </span>
              </div>
            </div>

            <div class="targetContent">
              {{ userTargetInfo.targetInfoList[index].targetSpecification }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="subList">
      <div class="contentBox">
        <div class="subTarget pd12">上级目标</div>
        <div class="subBox" v-for="i in list">
          <div class="subInfo">
            <van-image round :src="i.superiorhHeadSculpture" />
            <div>{{ i.superiorName }}</div>
          </div>
          <div class="targetBox">
            <div class="target">
              <div class="targetTitle pdb4 fs14">
                <div class="dian"></div>
                <div class="supTargetText">
                  {{ i.superiorTargetName }}
                </div>
              </div>
              <div class="targetContent">
                {{ i.superiorTargetSpecification }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <van-popup
      v-model="show"
      position="bottom"
      :close-on-click-overlay="false"
      safe-area-inset-bottom
    >
      <div class="popup pd12">
        <div class="topBox">
          <div class="flex justify">
            <div>指标亮灯详情</div>
            <van-icon name="cross" size="16" @click="show = false"></van-icon>
          </div>
          <van-divider />
        </div>
        <div class="popupDetails">
          <!-- <div>亮灯详情</div> -->
          <div class="lightingDetailsBox">
            <div class="flex lightingTitle">
              <div class="f1">考核周期</div>
              <div class="f2">考核活动</div>
              <div class="f3">亮灯类型</div>
            </div>
            <div class="flex pd12" v-for="i in detailList">
              <div class="f1">{{ i.period }}</div>
              <div class="f2">{{ i.activityName }}</div>
              <div class="f3">{{ i.lightType }}</div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script>
import { superiorTarget } from "@/libs/api.js";
import TARGET from "@/assets//performances/target.png";
import GARY from "@/assets/performances/gray.png";
import RED from "@/assets/performances/red.png";

export default {
  name: "TargetSuperior",
  data() {
    return {
      TARGET,
      RED,
      GARY,
      list: [],
      userTargetInfo: {},
      index: 0,
      show: false,
      detailList: [],
      isShowRed: true,
      // targetPlanId: "",
    };
  },
  created() {
    this.index = Number(this.$route.query.index);
    // this.targetPlanId = this.$route.query.targetPlanId;
    this.userTargetInfo = this.$store.state.userTargetInfo;
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      const data = {
        requestVO: {
          targetId: this.$route.query.id,
        },
      };
      superiorTarget(data).then((res) => {
        this.list = res.data.data.superiorTargetList;
        this.detailList = res.data.data.lightMapVOList[0].lightList || [];
        if (this.detailList.length) {
          const hasLightType = this.detailList.some(
            (item) =>
              item.lightType === "红灯" ||
              item.lightType === "黄灯" ||
              item.lightType === "绿灯"
          );
          this.isShowRed = hasLightType;
        }
      });
    },
  },
};
</script>

<style lang="less" scoped>
.page {
  background: #f2f2f2;
  min-height: 100vh;
}
.fs14 {
  font-size: 14px !important;
}
.pdb4 {
  padding-bottom: 4px;
}
.pdb8 {
  padding-bottom: 8px;
}
.dian {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #d80c1e;
  margin-right: 8px;
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.contentList,
.subList {
  padding: 12px;
  .contentBox {
    border-radius: 8px;
    background-color: #fff;
    .subBox {
      border-bottom: 1px solid #f2f2f2;
      padding: 12px;
      .targetTitle {
        font-size: 13px;
      }
      .targetContent {
        font-size: 13px;
        word-break: break-all;
      }
    }
    .subTarget {
      border-bottom: 1px solid #f2f2f2;
    }
    .subInfo {
      display: flex;
      align-items: center;
      font-size: 12px;
      .van-image {
        width: 20px;
        height: 20px;
        margin: 8px 12px 8px 0;
      }
    }
    .userInfo {
      align-items: center;
      border-bottom: 1px solid #f2f2f2;
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
      .target {
        display: flex;
        flex-direction: column;
        .targetTitleBox {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
        .targetTitle {
          display: flex;
          align-items: center;
          font-size: 15px;
          color: #333;
          font-weight: bold;
        }
        .targetContent {
          font-size: 13px;
          color: #666666;
          line-height: 24px;
          font-weight: 400;
        }
      }
    }
    .lightingDetails {
      padding: 0 12px 12px 0;
      color: #d80c1e;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      .ml4 {
        margin-left: 4px;
      }
    }
  }
}
.targetText {
  width: 200px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.supTargetText {
  width: 300px;
  font-size: 16px;
  color: #212121;
  font-weight: 500;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.indexBox {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  .indexLeft {
    margin-right: 8px;
  }
  .indexRight {
    flex: 1;
    .indexInfo {
    }
    .indexUserInfo {
      padding: 8px 0;
      .info {
        margin-left: 8px;
      }
      .indexDetailBox {
        margin-top: 4px;
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
    text-align: center;
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
  }
  .box1 {
    width: 80px;
  }
}
</style>
