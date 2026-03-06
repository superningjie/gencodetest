<template>
  <div class="otherSearch">
    <van-popup
      v-model="showOtherFilterPicker"
      position="top"
      :style="{ height: '100%' }"
    >
      <div class="pop-box">
        <van-nav-bar
          :title="OtherPickerTitle"
          class="navStyle"
          @click-left="isshowOtherPicker(false)"
        >
          <template #left>
            <van-icon class="close-icon" name="cross" />
          </template>
        </van-nav-bar>
        <div class="content-box">
          <div class="tag-box">
            <van-tag
              type="primary"
              class="mytag"
              @click="maturityfun(isTalent ? item.name : item.title)"
              :class="
                selectList.indexOf(isTalent ? item.name : item.title) != -1
                  ? 'active'
                  : ''
              "
              v-for="(item, index) in enumerationList"
              :key="index"
              >{{ isTalent ? item.name : item.title }}</van-tag
            >
          </div>
        </div>
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
      selectList: [], //选中的文本
      type: "",
      typeMapping: {
        人才标签: "talentTags",
        行业标签: "industryTag",
        // 职位序列: "seqPositions",
        // 职位子序列: "seqChPosition",
        // 职层: "jobgrade",
      },
      ids: [], //职位序列id
    };
  },
  props: {
    showOtherFilterPicker: {
      type: Boolean,
      default: false,
    },
    isTalent: {
      type: Boolean,
      default: false,
    },
    OtherPickerTitle: {
      type: String,
      default: "",
    },
    enumerationList: {
      type: Array,
      default: [],
    },
    // 其他选中的数据缓存
    otherSelectList: {
      type: Array,
      default: [],
    },
  },
  methods: {
    isshowOtherPicker(value) {
      this.$emit("isshowOtherPicker", value);
    },
    maturityfun(title) {
      console.log("点击了！", title);
      let index = this.selectList.indexOf(title);
      if (index > -1) {
        this.selectList.splice(index, 1);
      } else {
        this.selectList.push(title);
      }
      console.log("selectList", this.selectList);
    },
    handleReset() {
      this.selectList = [];
    },
    handleSubmit() {
      this.ids = [];
      if (this.OtherPickerTitle === "职位序列") {
        // 职位序列需要返回职位序列id -- 去请求子序列
        this.enumerationList.forEach((ite) => {
          this.selectList.forEach((item) => {
            // if (ite.name == item) {
            //   this.ids.push(ite.id);
            // }
            if (
              (this.isTalent && ite.name === item) ||
              (!this.isTalent && ite.title === item)
            ) {
              // 人才池是要id / 简历要value
              let valueToPush = this.isTalent ? ite.id : ite.value;
              this.ids.push(valueToPush);
            }
          });
        });
        if (this.ids.length == 0) {
          this.selectList = [];
        }
      }
      console.log("ids是：", this.ids);
      this.type = this.typeMapping[this.OtherPickerTitle];
      if (this.OtherPickerTitle === "职位序列") {
        this.$emit(
          "isshowOtherPicker",
          false,
          this.selectList,
          this.type,
          this.ids
        );
      } else {
        this.$emit("isshowOtherPicker", false, this.selectList, this.type);
      }
    },
  },
  watch: {
    enumerationList: {
      handler(v) {
        console.log(
          "enumerationList:",
          this.enumerationList,
          this.OtherPickerTitle
        );
      },
      deep: true,
    },
    OtherPickerTitle: {
      handler(v) {
        console.log("OtherPickerTitle:", this.OtherPickerTitle);
        this.selectList = [];
      },
    },
    otherSelectList: {
      handler(v) {
        console.log("otherSelectList改变了！", this.otherSelectList);
        if (this.otherSelectList.length) {
          this.selectList = this.otherSelectList;
        }
      },
      deep: true,
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
.content-box {
  flex: 1;
  overflow: auto;
}
.tag-box {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 三列，每列平均分配剩余空间 */
  gap: 10px; /* 列之间的间距 */
  padding: 10px 10px 0px 20px;
}
.mytag {
  display: flex;
  justify-content: center;
  align-items: center;
  line-height: 24px;
  background-color: #f5f5f5;
  color: #999;
}
.active {
  background-color: #cf3633;
  color: #ffffff;
}
.operation {
  background-color: #ffffff;
  display: flex;
  // position: fixed;
  // bottom: 0;
  // left: 0;
  // right: 0;
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
