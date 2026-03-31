<template>
  <div class="rate-field">
    <van-field
    v-if="isReadonly"
      v-bind="$attrs"
      v-model="tempValue"
      readonly
      input-align="right"
    />
    <van-field
    v-else
      v-bind="$attrs"
      v-model="tempValue"
      readonly
      input-align="right"
      @focus="inputFocus"
    />
    <van-dialog
      class="rate-dialog"
      v-model="show"
      title="评分"
      show-cancel-button
      @confirm="confirm"
      @cancel="cancel"
      :before-close="beforeClose"
    >
      <p class="tip">评分上下限：0-{{score}}</p>
      <van-form ref="form">
        <van-field
          readonly
          clickable
          type="number"
          :value="setValue"
          :rules="[
            { required: true, message: '请输入评分' },
            { 
                message:`请输入有效的0到${score}之间的数字`,
                validator: (value) => {
                if (value === '' || Number(value) < 0 || Number(value) >score) {
                    return false
                } else {
                    return true;
                }
                }
            }
        ]"
          @touchstart.native.stop="keyboardShow = true"
        />
      </van-form>
    </van-dialog>
    <van-number-keyboard
      :show="keyboardShow"
      v-model="setValue"
      theme="custom"
      extra-key="."
      close-button-text="完成"
      @blur="keyboardShow = false"
      z-index="3000"
    />
  </div>
</template>
<script>
export default {
  name: "RateField",
  props: {
    value: {
      required: true
    },
    score: {
      type: Number,
      default: 10
    },
    isReadonly:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      show: false,
      keyboardShow: false,
      setValue: ""
    };
  },
  computed: {
    tempValue: {
      get() {
        console.log("###", this.value, typeof this.value);
        if (typeof this.value === "number") {
          this.setValue = String(this.value);
        } else {
        }
        return this.value;
      },
      set(newValue) {
        this.$emit("input", newValue);
      }
    }
  },
  mounted() {},
  methods: {
    inputFocus() {
      this.show = true;
    },
    async beforeClose(action, done) {
      if (action === "confirm") {
        try {
          await this.$refs.form.validate();
          this.tempValue=this.setValue
          done();
        } catch (error) {
          done(false);
        }
      } else {
        this.setValue=""
        done();
      }
    },
    // 弹框确认
    confirm() {
      // this.rateDialog.show = true
      // return
      console.log("可以提交");
      //this.$emit("input", Number(this.rateDialog.value));
    },
    // 弹框取消
    cancel() {
      //this.tempValue=null
      this.$refs.form.resetValidation();
      console.log("取消");
    }
  }
};
</script>
<style lang="less" scoped>
.rate-field {
  width: 100%;
  .rate-input {
    &::placeholder {
      color: red;
    }
  }
}
.rate-dialog {
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
