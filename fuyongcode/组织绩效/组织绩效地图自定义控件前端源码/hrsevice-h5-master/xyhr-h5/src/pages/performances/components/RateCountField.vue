<template>
  <!--调整分组件  -->
  <div class="count-field">
    <van-field v-if="isReadonly" v-bind="$attrs" v-model="tempValue" readonly></van-field>
    <van-field v-else v-bind="$attrs" v-model="tempValue" readonly @focus="inputFocus"></van-field>
    <van-dialog
      class="count-dialog"
      v-model="dialoShow"
      title="调整分"
      show-cancel-button
      @confirm="confirm"
      @cancel="cancel"
      :before-close="beforeClose"
    >
      <p class="tip">评分上下限：0分-{{scoreMax}}分</p>
      <div class="count-step">
        <!-- :decimal-length="1" step="0.5" -->
        <van-stepper
          v-model="stepValue"
          :min="scoreMin"
          :max="scoreMax"
          input-width="100px"
          allow-empty
        />
      </div>
    </van-dialog>
  </div>
</template>
<script>
export default {
  name: "CountField",
  props: {
    value: {
      required: true
    },
    scoreMin: {
      type: Number,
      default: 0
    },
    scoreMax: {
      type: Number,
      default: 10
    },
    isReadonly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialoShow: false,
      stepValue: undefined
    };
  },
  computed: {
    tempValue: {
      get() {
        return this.value;
      },
      set(newValue) {
        this.$emit("input", newValue);
      }
    }
  },
  watch: {},
  created() {},
  methods: {
    inputFocus() {
      this.dialoShow = true;
      this.stepValue = this.tempValue;
    },
    beforeClose(action, done) {
      done();
    },
    //确认
    confirm() {
      this.tempValue = this.stepValue;
    },
    //取消
    cancel() {
      //this.tempValue=this.initTempValue
      console.log(this.value, this.tempValue);
      //this.tempValue=null
      //this.dialoShow = true
    },

    save() {
      console.log(this.tempValue);
    }
  }
};
</script>
<style lang="less" scoped>
.count-field {
  width: 100%;
  /deep/ .van-field__control {
    text-indent: 0;
    &::placeholder {
      text-align: right;
    }
  }
}
.count-step {
  display: flex;
  justify-content: center;
  padding: 12px 0 24px;
}
.count-dialog {
  .tip {
    font-size: 14px;
    color: #666666;
    letter-spacing: 0;
    text-align: center;
    line-height: 20px;
    font-weight: 400;
  }
  /deep/ .van-field .van-field__control {
    border: 0.5px solid rgba(229, 229, 229, 1);
    border-radius: 4px;
  }
  /deep/ .van-field.van-field--error .van-field__control {
    border: 0.5px solid red;
  }
}
</style>
