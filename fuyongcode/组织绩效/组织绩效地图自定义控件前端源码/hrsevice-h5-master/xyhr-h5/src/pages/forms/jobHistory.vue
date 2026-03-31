<template>
  <div class="myProfile jobHistory">
    <van-nav-bar :title="info.name" left-arrow class="navStyle" @click-left="goback" />
    <div class="examine" v-if="info.auditStatus == 1">
      修改需经过HR审批，请耐心等候
    </div>
    <div class="examine" v-if="
        info.records &&
          info.records.length == 0 &&
          info.auditStatus != 1 &&
          info.addFlag != 'N'
      " @click="add">
      无数据，请点击 <span>+</span> 号添加数据
    </div>
    <div class="group-box">
      <template v-for="(item, index) in info.records">
        <!-- editType=3为删除状态，不展示 -->
        <van-cell-group v-if="item.editType != 3" class="group" :key="index">
          <div class="editbtn" v-if="
            info.auditStatus != 1 && info.code != 'hi_psnjob' && !item.isLook
          ">
            <!-- 审批状态和工作记录的不允许修改 -->
            <i class="iconfont" @click="edit(item, index)">&#xe6db;</i>
          </div>
          <!-- <div :class="['approve',item.approve ? 'approve-true':'approve-false']">{{item.approve ? '已审核':'待审核'}}</div> -->
          <template v-for="val in item.fields">
            <van-cell :title="val.name" :value="val.showValue" v-if="val.showFlag == 'Y'" :key="val.code" />
          </template>
        </van-cell-group>
      </template>
    </div>
    <a class="addbtn" href="javascript:void(0)" v-if="info.auditStatus != 1 && info.addFlag != 'N'">
      <i @click="add" class="iconfont">&#xe693;</i>
    </a>
    <div class="bottombtn sticky submit-box" v-show="
        (info.records && info.records.length > 0 && info.code != 'hi_psnjob') ||
          (info.auditStatus == 0 && info.code == 'hi_psnjob') || info.auditStatus == 1
      ">
      <div class="flex" v-show="info.auditStatus == 1">
        <button @click="infoRevoke">撤回审批</button>
      </div>
      <div class="flex" v-show="info.auditStatus != 1">
        <button @click="submit">提交</button>
      </div>
    </div>
    <AddJob ref="AddJob" />
    <EditJob ref="EditJob" />
  </div>
</template>

<script>
  import {
    infoRevoke,
    getPsnInfoDetail,
    personalInfoEdit
  } from "@/libs/api.js";
  import {
    Toast
  } from "vant";
  import AddJob from "@/components/myInfo/AddJob.vue";
  import EditJob from "@/components/myInfo/EditJob.vue";

  export default {
    name: "jobHistory",
    data() {
      return {
        pkInfoSet: "", // 主键key
        info: {}
      };
    },
    components: {
      AddJob,
      EditJob
    },
    created() {
      window.scrollTo(0, 1);
      setTimeout(() => {
        window.scrollTo(0, 0);
      });
      this.pkInfoSet = this.$store.state.pkInfoSet;
      this.getNewInfo();
    },
    methods: {
      goback() {
        this.$router.back();
      },
      edit(item, index) {
        if (this.info.auditStatus == 1) {
          return;
        }
        const itemString = JSON.stringify(item);
        const data = JSON.parse(itemString);
        this.$refs.EditJob.show(data, index);
      },
      add() {
        this.$refs.AddJob.show();
      },
      infoRevoke() {
        infoRevoke({
          pkInfoSet: this.info.pkInfoSet,
          dataStatus: 0
        }).then(res => {
          let data = res.data;
          if (data.statusCode == 200) {
            // this.getNewInfo(); // 重新加载
            console.log("this.info==", this.info);
            this.info.auditStatus = 0
            Toast("撤销成功");
          } else {
            Toast(res.data.message);
          }
        });
      },
      getNewInfo() {
        this.$toast.loading({
          message: "加载中...",
          forbidClick: true
        });
        getPsnInfoDetail().then(res => {
          let data = res.data;
          if (data.statusCode == 200) {
            // 找出对应的模块数据
            const tempData = data.data.infos.find(item => {
              return item.pkInfoSet == this.pkInfoSet;
            });
            tempData.records.forEach(item => {
              item.editType = item.recordId ? "" : 2;
              item.approve = item.recordId ? true : false; // 是否通过审批
            });
            //证件类型的身份证不允许编辑和删除
            if (this.pkInfoSet == "1001Z71000000000D5RL") {
              const temp = tempData.records.find(items => {
                const isIncludes = items.fields.some(item => {
                  return (
                    item.code == "idtype" && item.value == "1001Z01000000000AI36"
                  ); // 身份证
                });
                return isIncludes;
              });
              if (temp) {
                temp.isLook = true;
              }
            }
            this.info = tempData;
          }
          console.log("this.info==", this.info);
          this.$toast.clear();
          this.$refs.AddJob.init(this.info);
          this.$refs.EditJob.init(this.info);
        });
      },
      submit() {
        console.log("submit==", this.info);
        const isContinue = this.checkIdtype();
        if (!isContinue) {
          this.$notify({
            type: "warning",
            message: "必须得有身份证"
          });
          return false;
        }
        const loading = Toast.loading({
          message: "加载中...",
          forbidClick: true
        });
        personalInfoEdit({
          infos: [this.info]
        }).then(res => {
          if (res.data.statusCode == 200) {
            this.$store.commit("setisNeedNewData", this.info.code);
            Toast(res.data.data);
            this.goback();
          } else {
            this.$notify({
              type: "danger",
              message: "提交失败"
            });
          }
        });
      },
      // 身份证校验
      checkIdtype() {
        let flag = true;
        if (this.info.pkInfoSet == "1001Z71000000000D5RL") {
          flag = this.info.records.some(items => {
            const isIncludes = items.fields.some(item => {
              return (
                item.code == "idtype" && item.value == "1001Z01000000000AI36"
              ); // 身份证
            });
            return isIncludes;
          });
        }
        return flag;
      }
    }
  };
</script>
<style lang="less" scoped>
  .submit-box {
    position: fixed !important;
  }

  .myProfile {
    position: relative;
  }

  .group {
    // padding-top: 30px;
  }

  .group-box {
    padding-bottom: 70px;
  }

  .editbtn {
    position: relative;
    padding-top: 10px;
  }

  .approve {
    position: absolute;
    top: 0;
    left: 0;
    font-size: 13px;
    color: white;
    padding: 4px 8px;
    height: 16px;
    line-height: 16px;
    border-bottom-right-radius: 12px;

    &.approve-true {
      background-color: #67c23a;
    }

    &.approve-false {
      background-color: #e6a23c;
    }
  }
</style>
