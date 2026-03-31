<!-- @format -->

<template>
  <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }" get-container="body" :lazy-render="false">
      <van-nav-bar :title="title" class="navStyle" @click-left="isshowFilterPicker(false)">
        <template #left> <van-icon class="close-icon" name="cross" /> </template>
      </van-nav-bar>
      <div class="subnav flex" v-if="isBusiness === true">
        <div :class="activeTab === 'xz' ? 'active' : ''" @click="activeTab = 'xz'">行政架构</div>
        <div :class="activeTab === 'sy' ? 'active' : ''" @click="activeTab = 'sy'">事业部架构</div>
      </div>
      <van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
        <van-tab title="行政架构" :name="'xz'">
          <mycommonTreeChild ref="mycommonTreeChild" :name="'xz'" v-if="orgsCommonList.length > 0" :noDet="noDet" :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
        </van-tab>
        <van-tab title="事业部架构" :name="'sy'">
          <mycommonTreeChild ref="mycommonTreeChild" :name="'xz'" v-if="businessList.length > 0" :noDet="noDet" :orgsCommonList="businessList" @clickSearch="clickSearch" />
        </van-tab>
      </van-tabs>
    </van-popup>
  </div>
</template>
<script>
import { orgsAuthTreeAdminorg,getAnalysisOrgFrameBusi } from "@/libs/api.js"
import mycommonTreeChild from "@/components/mycommonTreeChild"
import { Toast } from "vant"
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
      businessList:[]
    }
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
      default: "选择组织",
    },
    noDet: {
      type: Boolean,
      default: false,
    },
    isBusiness: {
      type: Boolean,
      default: false,
    },
    businessType: {
      type: String,
      default: "",
    },
  },
  watch: {
    showFilterPicker: {
      deep: true,
      handler: function (newValue, oldValue) {
        if (newValue) {
          if (this.init) {
            this.$nextTick(() => {
              this.init = false
              this.orgsCommon()
              this.orgsBusiness()
            })
          }
          history.pushState(null, null, document.URL)
        } else {
          if (!this.ispipstate) {
            this.$router.go(-1)
          }
          this.ispipstate = false
        }
      },
    },
  },
  components: { mycommonTreeChild },

  created() {
    let _this = this
    window.addEventListener("popstate", function () {
      if (_this.showFilterPicker) {
        _this.ispipstate = true
        _this.isshowFilterPicker(false)
        return
      }
    })
  },
  methods: {
    orgsCommon() {
      Toast.loading({
        duration: 0,
        forbidClick: true,
        message: "加载中",
      })
      orgsAuthTreeAdminorg(this.data).then((res) => {
        if (res.data.statusCode == 200) {
          Toast.clear()
          this.orgsCommonList = res.data.data
          if (res.data.data.length == 1 && res.data.data[0].children) {
            this.$nextTick(() => {
              this.$refs.mycommonTreeChild.gonextPage(res.data.data[0])
            })
          }
        }
      })
    },
    // orgsBusiness() {
    //   // 有事业部权限的请求获取事业部数据
    //   if (this.isBusiness) {
    //     if (this.businessType === '人员分析' || this.businessType === '入职分析' || this.businessType === '离职结构') {
    //       let typeStr = "";
    //       if (this.businessType === '人员分析') {
    //         typeStr = "person";
    //       } else if (this.businessType === '入职分析') {
    //         typeStr = "entry";
    //       } else if (this.businessType === '离职结构') {
    //         typeStr = "leave";
    //       }
    //       let param = {
    //         type: typeStr
    //       }
    //       getAnalysisOrgFrameBusi(param).then((res) => {
    //         if (res.data.statusCode === 200) {
    //           Toast.clear()
    //           this.businessList = res.data.data
    //           if (res.data.data.length === 1 && res.data.data[0].children) {
    //             this.$nextTick(() => {
    //               this.$refs.mycommonTreeChild.gonextPage(res.data.data[0])
    //             })
    //           }
    //         }
    //       })
    //     }
    //   }
    // },

    orgsBusiness() {
      // 若有事业部权限且业务类型需要处理，则调用处理逻辑
      if (this.isBusiness && this.needsBusinessData()) {
        const typeStr = this.mapBusinessTypeToTypeStr();
        const param = { type: typeStr };
        this.fetchAndProcessBusinessData(param);
      }
    },

    // 判断业务类型是否需要特殊处理
    needsBusinessData() {
      return ['人员分析', '入职分析', '离职分析','本月入职','本月离职/退休','本月轮动'].includes(this.businessType);
    },

    // 根据业务类型映射到对应的type字符串
    mapBusinessTypeToTypeStr() {
      return {
        '人员分析': 'person',
        '入职分析': 'entry',
        '离职分析': 'leave',
        '本月入职': 'monthEntry',
        '本月离职/退休': 'monthLeave',
        '本月轮动': 'monthChange',
      }[this.businessType];
    },

    // 获取并处理事业部数据
    fetchAndProcessBusinessData(param) {
      getAnalysisOrgFrameBusi(param)
          .then(res => {
            if (res.data.statusCode === 200) {
              Toast.clear();
              this.businessList = res.data.data;
              // 若只有一项且含有子元素，则自动展开
              if (this.businessList.length === 1 && this.businessList[0].children) {
                this.$nextTick(() => {
                  this.$refs.mycommonTreeChild.gonextPage(this.businessList[0]);
                });
              }
            }
          });
    },
    frameClick(e, item) {
      e.stopPropagation()
      this.$store.commit("increment", item)
      this.$emit("isshowFilterPicker", false)
    },
    isshowFilterPicker(value) {
      this.$emit("isshowFilterPicker", value)
    },
    clickSearch(item) {
      this.$emit("clickSearch", item)
    },
  },
}
</script>
<style lang="less" scoped>
    .close-icon{
        font-size: 18px !important;
    }
    .subnav {
      margin: 20px;
      margin-bottom: 14px;
      box-shadow: 0px 0px 4px 2px rgba(0, 0, 0, 0.05);
      border-radius: 30px;
      overflow: hidden;
      display: flex;
      div {
        flex: 1;
        width: 100%;
        text-align: center;
        line-height: 30px;
        font-size: 14px;
        font-weight: 600;
        background: #fff;
        &.active {
          background: #CF3633;
          color: #fff;
        }
      }
    }
</style>
