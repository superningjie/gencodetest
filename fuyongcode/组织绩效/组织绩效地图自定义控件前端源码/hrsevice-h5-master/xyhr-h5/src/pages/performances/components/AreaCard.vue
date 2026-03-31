<template>
  <div>
    <van-collapse class="collapseBox" v-model="activeNames" :border="false">
      <div class="tabContent" v-for="item in list" :key="item.indicatorId">
        <van-collapse-item :name="item.indctrname">
          <template #title>
            <div class="subTitleBgc fw-text flex middle justify">
              <van-image class="iconWH" :src="ICON" />
              <div
                class="flex middle ml12"
                style="flex: 1"
                @click.stop="seeMore(item.indctrname)"
              >
                <div class="ovText">
                  {{ item.indctrname }}
                </div>
              </div>
              <div class="flex middle" v-if="item.indctrscore || item.weight">
                <div class="flex middle" v-if="scoreCalcWay && item.weight">
                  <van-circle
                    :value="Number(item.weight)"
                    :rate="item.weight"
                    color="#ff0000"
                    size="16"
                    layer-color="#999"
                    :stroke-width="160"
                  />
                  <p class="ml8">{{ item.weight }}%</p>
                </div>
                <div v-else>分值：{{ item.indctrscore }}</div>
              </div>
            </div>
          </template>
          <div class="subContent plain-text">
            <div class="contentTab" v-if="item.evaltype">
              {{ item.evaltype }}
            </div>
            <div v-for="msg in item.optionalFieldInfo">
              <p>
                <i>{{ msg.fieldName }}：</i> {{ msg.fieldValue }}
              </p>
            </div>
          </div>
          <div
            class="score"
            v-for="otherItem in item.otherRatingList"
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
                <div class="ml8">
                  {{ otherItem.empInfo.position }}
                </div>
              </div>
              <div class="flex middle fs12">
                <div class="ml4 rate1Bgc">
                  {{ otherItem.nodeName }}
                  <span class="c6"> ({{ otherItem.nodeWeight }}%)</span>
                </div>
                <div class="ml4 rate1Bgc" v-if="otherItem.score.isShow">
                  评分:
                  <span class="c6">{{ otherItem.score.value }}</span>
                </div>
              </div>
            </div>
            <div class="scoreText" v-if="otherItem.desc.isShow">
              {{ otherItem.desc.value }}
            </div>
          </div>
        </van-collapse-item>
      </div>
    </van-collapse>
  </div>
</template>

<script>
import ICON from "@/assets/performances/icon4.png";
import { Toast } from "vant";

export default {
  name: "SingleIndexCard",
  props: {
    list: {
      type: Array,
      default: () => {
        return [];
      },
    },
    scoreCalcWay: {
      type: Boolean,
      default: true,
    },
    checked: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      activeNames: [],
      ICON,
    };
  },
  created() {
    this.changeChecked();
  },
  watch: {
    checked(oldValue, newValue) {
      // console.log(oldValue, "checked", newValue);
      this.changeChecked();
    },
  },
  methods: {
    changeChecked() {
      if (this.checked) {
        this.activeNames.push(this.traverseTree(this.list));
      } else {
        this.activeNames = [];
      }
    },
    traverseTree(node) {
      if (node) {
        for (let i = 0; i < node.length; i++) {
          let element = node[i];
          this.activeNames.push(element.indctrname);
          let children = element.children;
          this.traverseTree(children);
        }
      }
    },
    seeMore(val) {
      Toast(val);
    },
  },
};
</script>

<style lang="less" scoped>
.ml4 {
  margin-left: 4px;
}
.subIcon {
  width: 8px;
  height: 8px;
  border-bottom: 1px solid #979797;
  border-left: 1px solid #979797;
}
.myachievementQuery {
  padding-bottom: env(safe-area-inset-bottom);
}
.pages-bgc {
  background-color: #f2efef;
  padding-bottom: 72px;
}
.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 160px;
}
.noticeWH {
  width: 14px;
  height: 14px;
}
.f2Box {
  background: rgba(rgba(242, 242, 242, 0.4));
  padding: 0;
  border-radius: 4px;
  .van-cell::after {
    border-bottom: 1px dashed #d1d1d1;
    right: 0;
  }
}
/deep/ .van-popup {
  .van-popup__close-icon--top-right {
    top: 12px;
  }
}
.fieldBorder {
  /deep/ .van-field__control {
    border: 0.5px solid rgba(229, 229, 229, 1);
    border-radius: 4px;
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
/deep/ .tabContent {
  margin-top: 10px;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  background: white;
  .twoLevelBox {
  }
  .threeLevelBox {
    margin-left: 8px;
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
  }

  textarea {
    text-indent: 0;
  }
  .iconWH {
    width: 16px;
    height: 16px;
  }

  .van-cell {
    padding: 0px 12px 0px 0;
    background-color: transparent;
  }
  .van-collapse-item__content {
    background-color: transparent;
    padding: 4px 12px;
  }
  .van-collapse-item__title {
    align-items: center;
  }
  .subBox {
    .van-cell {
      padding: 0;
    }
    .van-collapse-item__content {
      padding: 0px;
      .subContent {
        // padding: 8px;
        word-wrap: break-word;
      }
    }
  }

  .labelText {
    padding: 6px 0px;
  }
  .subContent {
    // padding: 8px 12px;
    .contentTab {
      background: #f2f2f2;
      border-radius: 4px;
      padding: 2px 4px;
      display: inline-block;
      margin-bottom: 6px;
    }
    i {
      color: #666;
    }
  }
}
</style>
