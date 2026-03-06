<template>
  <div class="otherSearch">
    <van-popup
      v-model="showsequenceFilterPicker"
      position="top"
      :style="{ height: '100%' }"
    >
      <div class="pop-box">
        <van-nav-bar
          :title="OtherPickerTitle"
          class="navStyle"
          @click-left="isShowPicker(false)"
        >
          <template #left>
            <van-icon class="close-icon" name="cross" />
          </template>
        </van-nav-bar>
        <div class="content-box">
          <van-collapse v-model="activeNames">
            <van-checkbox-group v-model="checkResult">
              <van-collapse-item
                :name="item.value"
                v-for="(item, index) in enumerationList"
                :key="item.value"
              >
                <template #title>
                  <div class="title-box" @click.stop>
                    <van-checkbox
                      @click.stop="checkSelect(item.value)"
                      label-disabled
                      shape="square"
                      :name="item.value"
                      checked-color="#d03329"
                      ref="checkboxes"
                      style="margin-right: 6px"
                    />
                    <span>{{ item.title }}</span>
                  </div>
                </template>
                <div class="tag-box">
                  <van-tag
                    type="primary"
                    class="mytag"
                    @click="maturityfun(ite.value)"
                    :class="selectList.indexOf(ite.value) != -1 ? 'active' : ''"
                    v-for="(ite, ind) in item.children"
                    :key="ind"
                    >{{ ite.title }}</van-tag
                  >
                </div>
              </van-collapse-item>
            </van-checkbox-group>
          </van-collapse>
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
      activeNames: [], //折叠面板展开项集合 -- 默认都不要展开
      checkResult: [], //多选框选中的集合
      selectList: [], //选中的values集合
      Labels: [], //选中的文本集合
      enumerationList: [
        // {
        //   title: "通用管理序列",
        //   value: "1",
        //   children: [
        //     {
        //       title: "资深工匠",
        //       value: "AA",
        //     },
        //     {
        //       title: "资深工匠2",
        //       value: "BB",
        //     },
        //     {
        //       title: "资深工匠3",
        //       value: "CC",
        //     },
        //   ], //子序列
        // },
        // {
        //   title: "经验管理序列",
        //   value: "2",
        //   children: [
        //     {
        //       title: "经验工匠",
        //       value: "DD",
        //     },
        //     {
        //       title: "经验工匠2",
        //       value: "EE",
        //     },
        //     {
        //       title: "经验工匠3",
        //       value: "FF",
        //     },
        //     {
        //       title: "经验工匠4",
        //       value: "GG",
        //     },
        //   ], //子序列
        // },
      ],
      OtherPickerTitle: "职位序列", //标题
      connetIds: [], //用于请求职层的values
    };
  },
  props: {
    showsequenceFilterPicker: {
      type: Boolean,
      default: false,
    },
    // 选中的数据缓存
    otherSelectList: {
      type: Array,
      default: [],
    },
    sequenceList: {
      type: Array,
      default: [],
    },
  },
  methods: {
    // 子级全选-更新父级复选框状态
    updateCheckResult() {
      let checkResult = [];
      this.enumerationList.forEach((parent) => {
        const allChildrenSelected = parent.children.every((child) =>
          this.selectList.includes(child.value)
        );

        if (allChildrenSelected) {
          checkResult.push(parent.value);
        }
      });
      this.checkResult = checkResult;
    },
    // 变更父级全选子级
    getChildrenFromCheckResult(v) {
      let selectList = [];
      this.enumerationList.forEach((parent) => {
        if (this.checkResult.includes(parent.value)) {
          parent.children.forEach((child) => {
            selectList.push(child.value);
          });
        }
      });
      this.selectList = [...new Set(this.selectList.concat(selectList))];
      if (!this.checkResult.includes(v)) {
        let childrenValues = this.getChildrenValues(v);
        if (childrenValues.length) {
          this.selectList = this.filterArray(this.selectList, childrenValues);
        }
      }
    },
    // 过滤掉取消全勾选的内容
    filterArray(arr, arr2) {
      return arr.filter((item) => !arr2.includes(item));
    },
    // 根据当前勾选 - 取出其children所有value
    getChildrenValues(v) {
      let result = [];
      this.enumerationList.forEach((parent) => {
        if (parent.value === v) {
          result = parent.children.map((child) => child.value);
        }
      });

      return result;
    },
    // 只要一项/全部子序列 -- 那么序列就push --用于请求职层
    FindParentResult() {
      let connetIds = [];
      this.enumerationList.forEach((parent) => {
        const FindSelected = parent.children.some((child) =>
          this.selectList.includes(child.value)
        );
        const allChildrenSelected = parent.children.every((child) =>
          this.selectList.includes(child.value)
        );

        if (FindSelected || allChildrenSelected) {
          connetIds.push(parent.value);
        }
      });
      this.connetIds = connetIds;
    },
    // 点击复选框
    checkSelect(v) {
      // console.log("当前点击的复选框是:", v);
      // 更新父级时 -- 同步全选子级
      this.getChildrenFromCheckResult(v);
      this.FindParentResult();
    },
    isShowPicker(value) {
      let isReset = false;
      // 重置后退出
      if (this.selectList.length == 0) {
        isReset = true;
      }
      this.$emit("isShowPicker", value, isReset);
    },
    maturityfun(value) {
      let index = this.selectList.indexOf(value);
      if (index > -1) {
        this.selectList.splice(index, 1);
      } else {
        this.selectList.push(value);
      }
      console.log("selectList", this.selectList);
      // 每次更新选项块 -- 调用updateCheckResult更新对象父级复选框-是否选中
      this.updateCheckResult();
      this.FindParentResult();
    },
    handleReset() {
      this.checkResult = [];
      this.selectList = [];
      this.Labels = [];
      this.connetIds = [];
    },
    handleSubmit() {
      this.Labels = [];
      // 取出对应的文本集合
      this.Labels = this.findMatchingTitles(
        this.enumerationList,
        this.selectList
      );
      this.$emit(
        "isShowSequenceFilterPicker",
        false,
        this.selectList,
        this.Labels,
        this.connetIds
      );
    },
    // 根据选中value -- 查找对应文本集合
    findMatchingTitles(arr, arr2) {
      const Labels = [];
      function searchItem(item) {
        if (arr2.includes(item.value)) {
          Labels.push(item.title);
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
    sequenceList: {
      handler(v) {
        if (this.sequenceList.length) {
          this.enumerationList = this.sequenceList;
        }
      },
      deep: true,
      immediate: true,
    },
    otherSelectList: {
      handler(v) {
        if (this.otherSelectList.length) {
          this.selectList = this.otherSelectList;
          this.updateCheckResult();
        } else {
          this.selectList = [];
          this.checkResult = [];
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
.content-box {
  flex: 1;
  overflow: auto;
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
