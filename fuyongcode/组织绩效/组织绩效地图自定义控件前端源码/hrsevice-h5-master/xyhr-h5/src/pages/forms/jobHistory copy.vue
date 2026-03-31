<template>
  <div class="myProfile jobHistory">
    <van-nav-bar :title="info.name" left-arrow class="navStyle" @click-left="goback" />
    <div class="examine" v-if="info.auditStatus == 1">
      修改需经过HR审批，请耐心等候
    </div>
    <div class="examine" v-if="info.records && info.records.length==0 && info.auditStatus != 1 && info.addFlag != 'N'"
      @click="add">
      无数据，请点击 <span>+</span> 号添加数据
    </div>

    <van-cell-group class="group" v-for="(item,index) in info.records" :key="item.recordId">
      <div class="editbtn" v-if="info.auditStatus != 1 && !(info.code=='hi_psnjob' && index != 0)">
        <i class="iconfont" @click="edit(item)">&#xe6db;</i>
      </div>
      <template v-for="val in item.fields" >
        <van-cell :title="val.name" :value="val.showValue" v-if="val.showFlag=='Y'" :key="val.code"/>
      </template>
    </van-cell-group>

    <a class="addbtn" href="javascript:void(0)" v-if="info.auditStatus != 1 && info.addFlag != 'N'">
      <i @click="add" class="iconfont">&#xe693;</i>
    </a>
    <div class="bottombtn sticky" v-if="info.auditStatus == 1">
      <div class="flex">
        <button @click="infoRevoke">撤回修改</button>
      </div>
    </div>
  </div>
</template>

<script>
  import {
    infoRevoke,
    getPsnInfoDetail
  } from '@/libs/api.js'
  import {
    Toast
  } from 'vant';
  export default {
    name: 'jobHistory',
    data() {
      return {
        info: {}
      }
    },
    components: {},
    created() {
      window.scrollTo(0, 1)
      setTimeout(() => {
        window.scrollTo(0, 0)
      })
      console.log('this.$store.state.pageInfo==',this.$store.state.pageInfo)
      if (this.$store.state.isNeedNewData == '') {
        this.info = this.$store.state.pageInfo
      } else {
        this.getNewInfo()
      }
    },
    methods: {
      goback() {
        this.$router.back()
      },
      edit(item) {
        if (this.info.auditStatus == 1) {
          return
        }
        this.$router.push({
          name: '修改工作记录',
          params: {
            info: item,
            title: this.info.name,
            parentInfo: this.info
          }
        })
      },
      add() {
        this.$router.push({
          name: '新增工作记录',
          params: {
            info: this.info,
            title: this.info.name,
            parentInfo: this.info
          }
        })
      },
      infoRevoke() {
        infoRevoke({
          pkInfoSet: this.info.pkInfoSet
        }).then(res => {
          let data = res.data
          console.log(data)
          if (data.statusCode == 200) {
            Toast('撤销成功')
            this.$router.go(-1)

          } else {
            Toast(res.data.message)
          }

        })
      },
      getNewInfo() {
        this.$toast.loading({
          message: '加载中...',
          forbidClick: true,
        })
        getPsnInfoDetail().then(res => {
          let data = res.data.data
          let code = this.$store.state.isNeedNewData
          data.infos.map(item => {
            if (item.code == code) {
              this.info = item
              this.$store.commit("setPageInfo", item)
              this.$store.commit("setisNeedNewData", '');
            }
          })
          this.$toast.clear()
        })
      }
    }
  }
</script>
