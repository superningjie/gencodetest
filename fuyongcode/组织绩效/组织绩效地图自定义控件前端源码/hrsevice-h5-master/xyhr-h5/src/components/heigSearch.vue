<!-- @format -->

<template>
  <div class="administrativeFramework" :class="data.pageno == 1 ? 'hegihtSearch' : ''">
    <van-nav-bar left-arrow class="navStyle" title="简历查询" @click-left="goback">
      <template #right>
        <!--<van-button @click="data.retireFlag=!data.retireFlag" round type="info" size="mini" :class="data.retireFlag?'noActive':'active'">是否在职</van-button>-->
        <a class="hl">
          <!-- <button :class="'navRightBtn ' + (showActiveOnly?'active':'')" @click="onlyzz">只看在职</button> -->
          <div style="display: flex; flex-wrap: wrap; justify-content: flex-end">
            <van-switch style="right: 7px" size="16px" v-model="showActiveOnly" active-color="#CF3633" inactive-color="#dcdee0" @change="onlyzz()" />
            <div style="font-size: 12px; width: 100%; text-align: right">{{ showActiveOnly ? "查看全部" : "只看在职" }}</div>
          </div>
        </a>
      </template>
    </van-nav-bar>
     <xy-empty v-if="employeeList.length == 0 && finished == true"></xy-empty>
    <van-list
      v-model="loading"
      :finished="finished"
      :finished-text="employeeList.length > 0 ? '没有更多了' : ''"
      :loading-text="employeeList.length == 0 ? ' ' : '加载中...'"
      @load="onLoad"
      :immediate-check="false"
    >
      <div class="employeeList">
        <div class="employee flex middle" style="flex-wrap: wrap" v-for="employee in employeeList" v-if="(data.retireFlag && employee.jobtype != '离职') || !data.retireFlag">
          <div class="flex" style="width: 100%; position: relative">
            <van-image :src="employee.image ? employee.image : KHCFDC" round width="48px" height="48px" @click="viewEmployeeDetail(employee)" fit="fill" />
            <div class="info" @click="viewEmployeeDetail(employee)" style="width: calc(100% - 48px)">
              <i :class="'tag ' + tagColor(employee.jobtype)" v-if="employee.jobtype && employee.jobtype != '兼职'">{{ employee.jobtype }}</i>
              <h1>{{ employee.name }} {{ employee.postname ? " | " + employee.postname : "" }} {{ employee.gwcj ? " | " + employee.gwcj : "" }}</h1>
              <p style="color: #999999; font-size: 12px">{{ employee.orgname }}{{ employee.deptname ? " | " + employee.deptname : "" }}</p>
            </div>
            <van-icon name="arrow-down" size="16" style="position: absolute; right: 0; bottom: 0" v-if="employee.partjobs" @click="partjobsShow(employee.pk)" />
          </div>
          <div style="width: 100%" v-if="employee.partjobs && isjobsShow == employee.pk">
            <h2 style="margin: 10px 0; padding-top: 10px; border-top: 2px solid #f5f5f5">兼职信息 ({{ employee.partjobs.length }})</h2>
            <div class="employee flex middle" @click="viewEmployeeDetail(employee)" v-for="item in employee.partjobs" style="margin-bottom: 10px; box-shadow: none; padding: 0">
              <van-image :src="employee.image ? employee.image : KHCFDC" round width="36px" height="36px" fit="fill" />
              <div class="info" style="width: calc(100% - 60px)">
                <p style="color: #999999; font-size: 12px">{{ item.orgname }}&nbsp;{{ item.deptname }}&nbsp;{{ item.postname }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script>
import { getResumeSearch, tagColor } from "@/libs/api.js"
import { Toast, Dialog } from "vant"
import KHCFDC from '@/assets/KHCFDC.svg'
export default {
  name: "heigSearch",
  data() {
    return {
      data: {
        pageno: 1,
        pagesize: 10,
      },
      loading: false,
      finished: false,
      showDate: false,
      employeeList: [],
      columns: [],
      showActiveOnly: false,
      isjobsShow: false,
      KHCFDC:KHCFDC
    }
  },
  props: {
    heigSearchList: {
      type: Object,
      default: {},
    },
  },
  created() {
    this.getTextSearch()
  },
  methods: {
    partjobsShow(val) {
      if (this.isjobsShow == val) {
        this.isjobsShow = ""
      } else {
        this.isjobsShow = val
      }
    },
    onlyzz() {
      // this.showActiveOnly=!this.showActiveOnly
      this.data.pageno = 0
      this.employeeList = []
      this.finished = false
      setTimeout(() => {
        if (this.data.pageno == 0) {
          this.data.pageno = 1
          this.getTextSearch()
        }
      }, 200)
    },
    tagColor(jobtype) {
      return tagColor(jobtype)
    },
    onLoad() {
      this.data.pageno++
      this.getTextSearch()
    },
    viewEmployeeDetail() {},
    onConfirm(item) {
      this.statusSelected = item
      this.showDate = false
    },
    onCancel() {
      this.showDate = false
    },
    getTextSearch() {
      let data = JSON.parse(JSON.stringify(this.data))
      data = Object.assign(data, this.heigSearchList)
      data["retireFlag"] = this.showActiveOnly
      for (let key in data) {
        if (Array.isArray(data[key]) && data[key].length == 0) {
          delete data[key]
        }
      }
      if (data.pageno == 1) {
        Toast.loading({
          duration: 0,
          forbidClick: true,
          message: "加载中",
        })
      }
      getResumeSearch(data).then((res) => {
        Toast.clear()
        if (res.data.statusCode == 200) {
          if (res.data.data.psndata) {
            this.loading = false
            this.employeeList = [...this.employeeList, ...res.data.data.psndata]
            if (res.data.data.psndata.length < this.data.pagesize) {
              this.finished = true
            }
          } else {
            this.finished = true
          }
        }
      })
    },
    onSearch() {
      this.data.pageNum = 0
      this.loading = false
      this.finished = false
      this.employeeList = []
      this.getTextSearch()
    },
    goback() {
      this.$emit("showzongh", false)
    },
    viewEmployeeDetail(employee) {
      this.$router.push({
        name: "employeeInfo",
        query: {
          pkPsndoc: employee.pk,
          ncId: employee.ncId,
          goback: -1,
        },
      })
    },
  },
}
</script>
