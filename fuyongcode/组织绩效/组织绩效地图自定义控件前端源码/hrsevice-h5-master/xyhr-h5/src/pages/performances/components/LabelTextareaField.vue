<template>
  <div class="textarea-field">
    <div class="label">{{title}}</div>
    <van-field v-bind="$attrs" v-on="$listeners" />
  </div> 
</template>
<script>
export default {
  name:"LabelTextareaField",
  props:{
    title:{
      type:String,
      default:""
    }
  },
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
  position: relative;
  &:after {position: absolute;
    box-sizing: border-box;
    content: ' ';
    pointer-events: none;
    right:12px;
    bottom: 0;
    left:12px;
    border-bottom: 1px solid #ebedf0;
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);}
  &:last-child:after{
    border:none
  }  
  .label{
    font-size:14px;
    color: #666;
    padding:12px 14px 0 14px;
  }
  /deep/ .van-field__control{
    text-indent:0;
    // &::placeholder{
    //   text-align: right;
    // }
  }
}
</style>
