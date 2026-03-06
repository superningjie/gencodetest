<template>
  <div class="administrativeFramework">
    <van-popup
      v-model="showFilterPicker"
      position="top"
      :style="{ height: '100%' }"
    >
      <van-nav-bar
        title="选择组织"
        left-arrow
        class="navStyle"
        @click-left="isshowFilterPicker(false)"
      >
        <template #right>
          <van-button
            v-if="resetShow"
            type="danger"
            size="mini"
            @click="
              clickSearch({ id: '', name: $store.state.userData.deptName })
            "
            >重置</van-button
          >
        </template>
      </van-nav-bar>
      <div class="subnav flex">
        <div
          :class="activeTab == 'xz' ? 'active' : ''"
          @click="activeTab = 'xz'"
        >
          行政架构
        </div>
        <!-- <div
          v-if="isSy"
          :class="activeTab == 'sy' ? 'active' : ''"
          @click="activeTab = 'sy'"
        >
          事业部架构
        </div> -->
      </div>
      <van-tabs class="hideTabTitle" animated v-model="activeTab">
        <van-tab title="行政架构" :name="'xz'">
          <leadercommonTreeChild ref='XZTree' :name="'xz'" :jobInfo='jobInfo' :workPosition='workPosition' @clickSearch="clickSearch" />
        </van-tab>
        <van-tab title="事业部架构" :name="'sy'">
          <leadercommonTreeChild ref='SYTree' :name="'sy'" :jobInfo='jobInfo' :workPosition='workPosition' @clickSearch="clickSearch" />
        </van-tab>
      </van-tabs>
    </van-popup>
  </div>
</template>
<script>
import leadercommonTreeChild from "@/components/leadercommonTreeChild";
export default {
  name: "leadercommonTree",
  props:{
    showFilterPicker: {
      type: Boolean,
      default: false
    },
    resetShow: {
      type: Boolean,
      default: false
    },
    jobInfo:{
        type:Object,
        default(){
            return {}
        }
    },
    workPosition:{
      type: String,
      default: '0'
    },
    isSy:{
      type:Boolean,
      default:true
    }
  },
  data() {
    return {
      activeTab: "xz",
      orgsCommonList: [],
      ispipstate: false
    };
  },
  watch: {
    showFilterPicker: {
      deep: true,
      handler: function(newValue, oldValue) {
        if (newValue) {
          history.pushState(null, null, document.URL);
        } else {
          if (!this.ispipstate) {
            this.$router.go(-1);
          }
          this.ispipstate = false;
        }
      }
    },
    jobInfo(newVal){
        console.log('jobInfo-change')
        this.$refs.XZTree.init(newVal)
        this.$refs.SYTree.init(newVal)
    },
    workPosition(newVal){
      this.$refs.XZTree.init(newVal)
    }
  },
  components: { leadercommonTreeChild },
  created() {
    let _this = this;
    window.addEventListener("popstate", function() {
      if (_this.showFilterPicker) {
        _this.ispipstate = true;
        _this.isshowFilterPicker(false);
        return;
      }
    });
  },
  methods: {
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
    }
  }
};
</script>
