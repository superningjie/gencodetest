<template>
  <div>
    <van-collapse
      class="collapseBox mt10"
      v-model="activeNames"
      :border="false"
    >
      <div
        class="tabContent"
        v-for="(item, index) in list"
        v-if="item.title"
        :key="item.indexId"
      >
        <van-collapse-item :name="item.id" :border="false">
          <template #title>
            <div class="subTitle fw-text flex middle justify">
              <van-image class="iconWH" :src="ICON" />
              <div
                class="flex middle ml12"
                style="flex: 1"
                @click.stop="seeMore(item.title)"
              >
                <div class="ovText">
                  {{ item.title }}
                </div>
              </div>
              <div class="flex middle" v-if="item.num || item.num == 0">
                <div class="point ml8">{{ item.num }}</div>
              </div>
              <div class="flex middle" v-else>
                <div class="flex middle" v-if="item.weight > 0">
                  <van-circle
                    v-model="item.weight"
                    :rate="item.weight"
                    color="#ff0000"
                    size="16"
                    layer-color="#999"
                    :stroke-width="160"
                  />
                  <p class="ml4">{{ item.weight }}%</p>
                </div>
                <div v-else>分值：{{ item.weightNum }}</div>
                <!-- <div class="col-line" v-show="item.score"></div> -->
                <div class="point ml8">{{ item.score }}</div>
              </div>
            </div>
          </template>
          <!-- 单级指标 -->

          <div class="subContent plain-text">
            <div class="contentTab" v-if="item.type">
              {{ item.type }}
            </div>
            <div v-for="msg in item.unMessageList">
              <p>
                <i>{{ msg.title }}：</i> {{ msg.content }}
              </p>
            </div>
          </div>
          <!-- 单级指标end -->
          <!-- 指标评价 -->
          <div v-show="item.evaluationList && item.evaluationList.length > 0">
            <div class="seeEvaluate fs12" @click="viewDetail(item, index)">
              查看评价详情
              <van-icon name="arrow"></van-icon>
            </div>
          </div>

          <!-- 指标评价end -->
        </van-collapse-item>
        <van-divider :style="{ margin: 0 }" />
      </div>
    </van-collapse>
  </div>
</template>

<script>
import { Toast } from "vant";
import ICON from "@/assets/performances/icon4.png";

export default {
  props: {
    list: {
      type: Array,
      default: () => {
        return [];
      },
    },
    index: {
      type: Number,
      default: 0,
    },
    isRes: {
      type: Boolean,
      default: false,
    },
    id: {
      type: String,
      default: "",
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
      this.activeNames = this.list.map((obj) => obj.id);
    }
  },
  methods: {
    seeMore(val) {
      Toast(val);
    },
    viewDetail(i, active) {
      const id = this.id || this.$route.query.id;
      this.$router.push({
        path: "performancesAppraiseDetails",
        query: {
          id: id,
          index: this.index,
          active,
          res: this.isRes,
        },
      });
    },
  },
};
</script>

<style lang="less" scoped>
.tabContent {
  border-radius: 8px;
  background: white;
  margin-top: 8px;
  .contentTab {
    background: #ebebeb;
    border-radius: 4px;
    padding: 2px 4px;
    display: inline-block;
    margin-bottom: 6px;
  }
  textarea {
    text-indent: 0;
  }
  /deep/ .van-cell {
    padding: 0px 12px 0px 0;
    background-color: transparent;
    align-items: center;
  }
  /deep/ .van-collapse-item__content {
    background-color: transparent;
    padding-top: 0px;
  }
  .labelText {
    padding: 6px 0px;
  }
}
.subContent {
  padding-top: 0;
  padding: 8px;
  background-color: rgba(242, 242, 242, 0.5);
  border-radius: 4px;

  i {
    color: #666;
  }
}
.subTitle {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  padding: 8px 12px;
  background: linear-gradient(
    to right,
    rgba(238, 10, 10, 0.078) 0%,
    rgb(255, 255, 255) 100%
  );
  .iconWH {
    width: 16px;
    height: 16px;
  }
  .point {
    color: #d80c1e;
  }
}

.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 160px;
}
.collapseBox {
  border-radius: 8px;
  .seeEvaluate {
    text-align: center;
    margin-top: 8px;
  }
}
</style>
