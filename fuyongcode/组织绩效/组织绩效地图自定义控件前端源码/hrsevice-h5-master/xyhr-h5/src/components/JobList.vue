<template>
  <van-popup v-model="isShow" round :style="{ width:'70%'}">
    <div class="select-box">
      <!--  #107643 我的团队APP-行政线-岗位（包括团队人数、平均司龄、平均年龄、我的团队页面），名称调整为公司-部门-岗位
      http://ones.xiangyu.com/project/#/team/JbjqrWit/task/DBzAg9wBHYUfGdyL    -->
        <van-radio-group v-model="radioValue" @change="changeGroup">
            <van-radio class="radio-box" :name="index" v-for="(item,index) in jobList"
                       :key="item.pkPost"
                       @click="clickRadio">{{item.orgName}}-{{item.adminorg}}-{{item.postName}}</van-radio>
        </van-radio-group>
    </div>
    <div class="btn-box">
      <div class="btn-submit" @click="confirm">确 定</div>
    </div>
  </van-popup>
</template>
<script>
export default {
  data() {
    return {
      isShow: false,
      radioValue:'',
      radioChange:false,
    };
  },

  computed: {
    jobList() {
      return this.$store.state.jobList
    }
  },
  methods: {
    show() {
      this.isShow = true
    },
    clickRadio(){
        console.log('clickRadio==')
        if(!this.radioChange){
            this.radioValue = ''
        }
        this.radioChange = false
    },
    changeGroup(){
        this.radioChange = true
    },
    confirm(){
        const data = Object.assign({},this.jobList[this.radioValue])
        this.$emit('change', data)
        this.isShow = false
    }
  }
};
</script>
<style lang='less' scoped>
.select-box{
 padding: 10px;
 padding-bottom: 54px;
 position: relative;
 overflow-y: auto;
 max-height: 270px;
}
.radio-box{
    margin-bottom: 10px;
    font-size: 14px;
}
.btn-box{
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 54px;
  background-color: white;
  display: flex;
  justify-content: center;
  align-items: center;
}
.btn-submit{
  width: 80%;
  height: 32px;
  line-height: 32px;
  color: white;
  background-color: #d0332f;
  border-radius: 20px;
  font-size: 15px;
  text-align: center;
}
/deep/ .van-radio__icon--checked .van-icon{
    background-color: #d0332f;
    border-color: #d0332f;
}
</style>
