<template>
  <div class="textarea-field">
    <van-field v-bind="$attrs" v-on="$listeners"></van-field>
  </div>
</template>
<script>
export default {
  name:"TextareaField",
  props:{},
  data() {
    return {};
  },
  watch: {},
  created() {},
  methods: {}
};
</script>
<style lang="less" scoped>
.textarea-field {
  width: 100%;
  /deep/ .van-field__control{
    text-indent:0;
    &::placeholder{
      text-align: right;
      color: #aaa;
    }
  }
  /deep/ .van-field .van-field__error-message {
    text-align: right;
  }
   /deep/ .van-field--error .van-field__control::placeholder{
      color: #aaa;
  }
}
</style>
