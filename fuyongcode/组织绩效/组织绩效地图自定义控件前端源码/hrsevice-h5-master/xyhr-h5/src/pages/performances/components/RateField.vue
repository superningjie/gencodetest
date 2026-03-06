<template>
  <div class="rate-field">
    <van-field
      v-bind="$attrs"
      v-on="$listeners"
      :formatter="formatter"
      input-align="right"
    />
  </div>
</template>
<script>
export default {
  name: "RateField",
  props: {
    decimalDigits: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {};
  },
  watch: {},
  created() {},
  methods: {
    // formatter(value) {
    //   const formattedValue = value.replace(/[^\d.-]/g, "");
    //   const regExpString = "^(-)*(\\d+)\\.(\\d{" + this.decimalDigits + "}).*$";
    //   const regExp = new RegExp(regExpString);
    //   return formattedValue.replace(regExp, "$1$2.$3");
    // },
    formatter(value) {
      const formattedValue = value
        .replace(/[^\-\d.]/g, "")
        .replace(/\-{2,}/g, "-")
        .replace(/(\d+|\.)-/g, "$1");
      const regExpString = "^(-)?(\\d*)\\.(\\d{" + this.decimalDigits + "}).*$";
      const regExp = new RegExp(regExpString);
      return formattedValue.replace(regExp, "$1$2.$3");
    },
  },
};
</script>
<style lang="less" scoped>
.rate-field {
  width: 100%;
  position: relative;
  &:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 12px;
    bottom: 0;
    left: 12px;
    border-bottom: 1px solid #ebedf0;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
  }
  &:last-child:after {
    border: none;
  }
  /deep/ .van-field .van-field__error-message {
    text-align: right;
  }
}
</style>
