<template>
  <div class="administrativeFramework">
    <van-popup
      v-model="showFilterPicker"
      position="top"
      :style="{ height: '100%' }"
      get-container="body"
      :lazy-render="false"
    >
      <van-nav-bar
        :title="title"
        left-arrow
        class="navStyle"
        @click-left="isshowFilterPicker(false)"
      >
        <template #right>
          <!-- <van-button type="danger" size="mini" @click="clickSearch({id:'',name:'全部'})">重置</van-button> -->
        </template>
      </van-nav-bar>
      <van-tabs
        class="hideTabTitle"
        animated
        v-model="activeTab"
        :lazy-render="false"
      >
        <van-tab title="行政架构" :name="'xz'">
          <trainUnitTreeChild
            :name="'xz'"
            ref="trainUnitTreeChild"
            v-if="orgsCommonList.length > 0"
            :orgsCommonList="orgsCommonList"
            @clickSearch="clickSearch"
            :scope="1"
          />
        </van-tab>
      </van-tabs>
    </van-popup>
  </div>
</template>
<script>
import { orgsTrainCommonList } from "@/libs/api.js";

import trainUnitTreeChild from "@/components/trainUnitTreeChild";
import { Toast } from "vant";
export default {
  name: "mycommonTree",
  data() {
    return {
      activeTab: "xz",
      orgsCommonList: [],
      data: {
        retireFlag: false,
        scope: this.scope,
      },
      ispipstate: false,
      init: true,
    };
  },
  props: {
    showFilterPicker: {
      type: Boolean,
      default: false,
    },
    scope: {
      type: Number,
      default: 2,
    },
    title: {
      type: String,
      default: "培训单位",
    },
    initBack: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    showFilterPicker: {
      deep: true,
      handler: function (newValue, oldValue) {
        if (newValue) {
          if (this.init) {
            this.$nextTick(() => {
              this.orgsCommon();
            });
          }
          history.pushState(null, null, document.URL);
        } else {
          if (!this.ispipstate) {
            this.$router.go(-1);
          }
          this.ispipstate = false;
        }
      },
    },
  },
  components: { trainUnitTreeChild },
  created() {
    if (this.initBack) this.orgsCommon();
    let _this = this;
    window.addEventListener("popstate", function () {
      if (_this.showFilterPicker) {
        _this.ispipstate = true;
        _this.isshowFilterPicker(false);
        return;
      }
    });
  },
  methods: {
    orgsCommon() {
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      });
      orgsTrainCommonList(this.data).then((res) => {
        if (res.data.statusCode == 200) {
          Toast.clear();
          if (!this.initBack) {
            Toast.clear();
          }
          this.init = false;
          this.orgsCommonList = res.data.data;
          this.orgsCommonList = res.data.data;
          if (res.data.data.length == 1 && res.data.data[0].children) {
            this.$nextTick(() => {
              this.$refs.trainUnitTreeChild.gonextPage(
                res.data.data[0]
              );
              this.$nextTick(() => {
                if (this.initBack) {
                  this.clickSearch(res.data.data[0].children[0]);
                }
              });
            });
          } else {
            if (this.initBack) {
              this.clickSearch(res.data.data[0]);
            }
          }
        }
      });
    },
    frameClick(e, item) {
      e.stopPropagation();
      this.$store.commit("increment", item);
      this.$emit("isshowFilterPicker", false);
    },
    isshowFilterPicker(value) {
      this.$emit("isshowFilterPicker", value);
    },
    clickSearch(item) {
      this.$emit("clickSearch", item);
    },
  },
};
</script>