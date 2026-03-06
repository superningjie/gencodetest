<template>
  <div>
    <van-popup
      round
      rows="3"
      v-model="backData.show"
      :style="{ minWidth: '80%' }"
    >
      <div class="popupBackBox">
        <div class="backTitle"><i class="theme-colors">*</i>退回理由</div>
        <div class="c6 fs12 mt12">点击确定后将退回至上一个任务处理角色</div>
        <div class="backInput">
          <van-field
            v-model="backData.text"
            autosize
            type="textarea"
            placeholder="请输入"
            @input="onInput"
            :error-message="isValue ? '' : '请输入退回原因'"
          />
        </div>
      </div>
      <div class="popupFooterBtn">
        <div class="cancel" @click="cancel">取消</div>
        <div class="confirm" @click="confirm">确认</div>
      </div>
    </van-popup>
  </div>
</template>

<script>
export default {
  props: {
    backData: {
      type: Object,
      default: () => {
        return {
          show: false,
          text: "",
        };
      },
    },
  },
  data() {
    return {
      isValue: true,
    };
  },
  methods: {
    cancel() {
      this.backData.show = false;
      this.backData.text = "";
    },
    formatter(value) {
      if (value) {
        this.isValue = true;
      } else {
        this.isValue = false;
        console.log("输入框中没有值");
      }
    },
    confirm() {
      if (this.backData.text) {
        this.isValue = true;
        this.$emit("confirm");
      } else {
        this.isValue = false;
      }
    },
    onInput() {
      this.isValue = this.backData.text;
    },
  },
};
</script>

<style lang="less" scoped>
.popupFooterBtn {
  margin-top: 8px;
  display: flex;
  text-align: center;
  border-top: 0.1px solid #ebedf0;

  .cancel {
    padding: 12px;
    flex: 1;
  }
  .confirm {
    padding: 12px;
    flex: 1;
    color: #d80c1e;
    border-left: 0.1px solid #ebedf0;
  }
}
.popupBackBox {
  padding: 12px;
  .backTitle {
    text-align: center;
    font-weight: bold;
    font-size: 20px;
  }
  .backInput {
    margin-top: 8px;
    border: 1px solid #ebedf0;
    border-radius: 8px;
  }
}
</style>
