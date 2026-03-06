<template>
  <div>
    <!-- 多级指标区 -->
    <van-collapse v-if="isSupMultiInd" v-model="activeNames" :border="false">
      <div class="tabContent" v-for="item in list" :key="item.areaInsId">
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
          <!-- 多级指标 -->
          <!-- 一级 -->
          <div v-for="i in item.children" :key="i.treeId" class="subBox">
            <van-collapse-item :name="i.indctrname" :border="false">
              <template #title>
                <div class="flex middle">
                  <div class="red-line"></div>
                  <div class="ml8">{{ i.indctrname }} ({{ i.weight }}%)</div>
                </div>
              </template>
              <div
                v-if="i.optionalFieldInfo"
                class="subContent plain-text f2Box twoLevelBox"
              >
                <div class="contentTab" v-if="i.evaltype">
                  {{ i.evaltype }}
                </div>
                <div v-for="msg in i.optionalFieldInfo">
                  <p>
                    <i>{{ msg.fieldName }}：</i> {{ msg.fieldValue }}
                  </p>
                </div>
              </div>

              <!-- 二级 -->
              <div v-for="val2 in i.children" :key="val2.treeId">
                <van-collapse-item :name="val2.indctrname" :border="false">
                  <template #title>
                    <div class="flex middle ml8">
                      <div class="subIcon"></div>
                      <div
                        class="ml8 plain-text"
                        :class="val2.optionalFieldInfo ? 'fs12' : ''"
                      >
                        {{ val2.indctrname }} ({{ val2.weight }}%)
                      </div>
                    </div>
                  </template>
                  <div
                    v-if="val2.optionalFieldInfo"
                    class="subContent plain-text f2Box threeLevelBox"
                  >
                    <div class="contentTab" v-if="val2.evaltype">
                      {{ val2.evaltype }}
                    </div>
                    <div v-for="msg in val2.optionalFieldInfo">
                      <p>
                        <i>{{ msg.fieldName }}：</i> {{ msg.fieldValue }}
                      </p>
                    </div>
                  </div>
                  <!-- 三级 -->
                  <div v-for="val3 in val2.children" :key="val3.treeId">
                    <van-collapse-item :name="val3.indctrname" :border="false">
                      <template #title>
                        <div class="flex middle ml8">
                          <div class="subIcon"></div>
                          <div
                            class="ml8 plain-text"
                            :class="val3.optionalFieldInfo ? 'fs12' : ''"
                          >
                            {{ val3.indctrname }} ({{ val3.indctrname }}%)
                          </div>
                        </div>
                      </template>
                      <div
                        v-if="val3.optionalFieldInfo"
                        class="subContent plain-text f2Box threeLevelBox"
                      >
                        <div class="contentTab" v-if="val3.evaltype">
                          {{ val3.evaltype }}
                        </div>
                        <div v-for="msg in val3.optionalFieldInfo">
                          <p>
                            <i>{{ msg.fieldName }}：</i> {{ msg.fieldValue }}
                          </p>
                        </div>
                      </div>
                      <!-- 四级 -->
                      <div v-for="val4 in val3.children" :key="val4.treeId">
                        <van-collapse-item
                          :name="val4.indctrname"
                          :border="false"
                        >
                          <template #title>
                            <div class="flex middle ml12">
                              <div class="subIcon"></div>
                              <div
                                class="ml8 plain-text"
                                :class="val4.optionalFieldInfo ? 'fs12' : ''"
                              >
                                {{ val4.indctrname }} ({{ val4.indctrname }}%)
                              </div>
                            </div>
                          </template>

                          <div
                            v-if="val4.optionalFieldInfo"
                            class="subContent plain-text f2Box threeLevelBox"
                          >
                            <div class="contentTab" v-if="val4.evaltype">
                              {{ val4.evaltype }}
                            </div>
                            <div v-for="msg in val4.optionalFieldInfo">
                              <p>
                                <i>{{ msg.fieldName }}：</i>
                                {{ msg.fieldValue }}
                              </p>
                            </div>
                          </div>
                          <!-- 五级 -->
                          <div v-for="val5 in val4.children" :key="val5.treeId">
                            <van-collapse-item
                              :name="val5.indctrname"
                              :border="false"
                            >
                              <template #title>
                                <div class="flex middle ml12">
                                  <div class="subIcon ml12"></div>
                                  <div
                                    class="ml12 plain-text"
                                    :class="
                                      val5.optionalFieldInfo ? 'fs12' : ''
                                    "
                                  >
                                    {{ val5.indctrname }} ({{
                                      val5.indctrname
                                    }}%)
                                  </div>
                                </div>
                              </template>

                              <div
                                v-if="val5.optionalFieldInfo"
                                class="subContent plain-text f2Box threeLevelBox"
                              >
                                <div class="contentTab" v-if="val5.evaltype">
                                  {{ val5.evaltype }}
                                </div>
                                <div v-for="msg in val5.optionalFieldInfo">
                                  <p>
                                    <i>{{ msg.fieldName }}：</i>
                                    {{ msg.fieldValue }}
                                  </p>
                                </div>
                              </div>
                            </van-collapse-item>
                          </div>
                          <!-- 五级 -->
                        </van-collapse-item>
                      </div>
                      <!-- 四级 -->
                    </van-collapse-item>
                  </div>
                  <!-- 三级 -->
                </van-collapse-item>
              </div>
              <!-- 二级 -->
            </van-collapse-item>
          </div>
          <!-- 一级 -->

          <!-- 多级指标end -->
        </van-collapse-item>

        <!-- <van-divider /> -->
      </div>
    </van-collapse>
    <!-- 多级指标区end -->
    <van-collapse
      v-else
      class="collapseBox"
      v-model="activeNames"
      :border="false"
    >
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
    isSupMultiInd: {
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
    if (this.list) {
      this.activeNames.push(this.traverseTree(this.list));
    }
  },
  methods: {
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
