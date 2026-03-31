<template>
  <div class="otherSearch">
    <van-popup
      v-model="showjobgradeFilterPicker"
      position="top"
      :style="{ height: '100%' }"
    >
      <div class="pop-box">
        <van-nav-bar
          :title="Title"
          class="navStyle"
          @click-left="isShowJobPicker(false)"
        >
          <template #left>
            <van-icon class="close-icon" name="cross" />
          </template>
        </van-nav-bar>
        <div class="content-box" v-if="jobgradeList.length">
          <div
            class="content-item"
            v-for="(item, index) in jobgradeList"
            :key="index"
          >
            <div class="item-title">
              职位序列：{{ isTalent ? item.name : item.title }}
            </div>
            <div class="tag-box">
              <van-tag
                type="primary"
                class="mytag"
                @click="maturityfun(isTalent ? ite.number : ite.value)"
                :class="
                  selectList.indexOf(isTalent ? ite.number : ite.value) != -1
                    ? 'active'
                    : ''
                "
                v-for="(ite, ind) in item.children"
                :key="ind"
                >{{ isTalent ? ite.name : ite.title }}</van-tag
              >
            </div>
          </div>
        </div>
        <div v-else class="content-box no-data">请选择职位序列</div>
        <!-- 操作 -->
        <div class="operation">
          <span class="set" @click="handleReset">重置</span>
          <span class="submit" @click="handleSubmit">确认</span>
        </div>
      </div>
    </van-popup>
  </div>
</template>
<script>
export default {
  name: "otherSearch",
  data() {
    return {
      Title: "职层",
      selectList: [], //职层选中的values
      Labels: [], //选中的文本
    };
  },
  props: {
    showjobgradeFilterPicker: {
      type: Boolean,
      default: false,
    },
    jobgradeList: {
      type: Array,
      default: [],
    },
    // 选中的数据缓存
    otherSelectList: {
      type: Array,
      default: [],
    },
    // 是否是人员名单
    isTalent: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    isShowJobPicker(value) {
      this.$emit("isShowJobPicker", value);
    },
    maturityfun(value) {
      let index = this.selectList.indexOf(value);
      if (index > -1) {
        this.selectList.splice(index, 1);
      } else {
        this.selectList.push(value);
      }
      console.log("selectList", this.selectList);
    },
    handleReset() {
      this.selectList = [];
    },
    handleSubmit() {
      this.Labels = [];
      // 取出对应的文本集合
      this.Labels = this.findMatchingTitles(this.jobgradeList, this.selectList);
      this.$emit("isShowJobFilterPicker", false, this.selectList, this.Labels);
    },
    // 根据选中value -- 查找对应文本集合
    findMatchingTitles(arr, arr2) {
      let isTalent = this.isTalent;
      const Labels = [];
      function searchItem(item) {
        if (arr2.includes(isTalent ? item.number : item.value)) {
          Labels.push(isTalent ? item.name : item.title);
        }
        // 处理 children
        if (item.children) {
          item.children.forEach((child) => searchItem(child));
        }
      }
      // 遍历 arr 数组
      arr.forEach((item) => searchItem(item));
      return Labels;
    },
  },
  watch: {
    jobgradeList: {
      handler(v) {
        console.log("传过来的职层数据是：", this.jobgradeList);
      },
      deep: true,
      immediate: true,
    },
    otherSelectList: {
      handler(v) {
        if (this.otherSelectList.length) {
          this.selectList = this.otherSelectList;
        } else {
          this.selectList = [];
        }
      },
      deep: true,
    },
    isTalent: {
      handler(v) {
        console.log("是否isTalent:", this.isTalent);
      },
      deep: true,
      immediate: true,
    },
  },
};
</script>
<style lang="less" scoped>
.pop-box {
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}
.close-icon {
  font-size: 18px !important;
}
.tag-box {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 三列，每列平均分配剩余空间 */
  gap: 10px; /* 列之间的间距 */
  padding: 0 20px;
}
.content-box {
  flex: 1;
  overflow: auto;
  padding: 10px 5px;
  .content-item {
    padding: 10px 0 15px 0;
    margin-bottom: 10px;
    border: 1px solid rgb(202, 198, 198);
    border-radius: 6px;
    .item-title {
      margin: 0 0 10px 10px;
      color: #858080;
      font-size: 14px;
      font-weight: bold;
    }
  }
  .content-item:last-child {
    margin-bottom: 0;
  }
}
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 15px;
  color: #958f8f;
}
.mytag {
  display: flex;
  justify-content: center;
  align-items: center;
  line-height: 24px;
  background-color: #f5f5f5;
  color: #999;
}
.title-box {
  display: flex;
}
.active {
  background-color: #cf3633;
  color: #ffffff;
}
.operation {
  background-color: #ffffff;
  display: flex;
  //   position: fixed;
  //   bottom: 0;
  //   left: 0;
  //   right: 0;
  padding: 10px 16px;
  span {
    display: inline-block;
    flex: 1;
    padding: 8px 0;
    text-align: center;
    font-family: PingFang SC;
    font-weight: 500;
    font-size: 18px;
  }
  .set {
    margin-right: 6px;
    color: #ffffff;
    background: #d80c1e;
    border: 1px solid #d80c1e;
    border-top-left-radius: 32px;
    border-bottom-left-radius: 32px;
  }
  .submit {
    color: #ffffff;
    background: #d80c1e;
    border-top-right-radius: 32px;
    border-bottom-right-radius: 32px;
  }
}
</style>
