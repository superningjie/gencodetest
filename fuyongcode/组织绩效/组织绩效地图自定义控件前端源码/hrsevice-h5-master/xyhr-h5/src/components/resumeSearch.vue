<!-- @format -->

<template>
  <div class="administrativeFramework">
    <van-nav-bar left-arrow class="navStyle" @click-left="goback">
      <template #title>
        <van-search class="searchItem" shape="round" v-model="data.keyword" placeholder="输入关键字搜索" style="padding-bottom: 0" @search="onSearch" ref="fieldPhone" />
      </template>
      <template #right>
        <van-button @click="zaizhi()" round type="info" size="mini" :class="data.jobStatus == 'ALL' ? 'noActive' : 'active'">只看在职</van-button>
        <!--<a class="hl"  @click="showDate=true">
				<span>{{statusSelected.name}}</span>
				<van-icon name="arrow-down" color="#CF3633" />
			</a>-->
      </template>
    </van-nav-bar>
    <van-list v-model="loading" :finished="finished" finished-text="没有更多了" @load="onLoad" :immediate-check="false">
      <div class="employeeList">
        <div class="employee flex middle" style="align-items: start" @click="viewEmployeeDetail(employee)" v-for="employee in employeeList">
          <van-image :src="employee.photo ? employee.photo : KHCFDC" round width="48px" height="48px" fit="fill" />
          <div class="info" style="width: calc(100% - 48px)">
            <i :class="'tag ' + tagColor(employee.jobStatus)" v-if="employee.jobStatus && employee.jobStatus != '兼职'">{{ employee.jobStatus }}</i>
            <h1>{{ employee.name }} | {{ employee.orgName }} | {{ employee.postLevel }}</h1>
            <p style="color: #999999; font-size: 12px">{{ employee.orgName }} | {{ employee.deptName }} <van-icon name="arrow" size="10" /></p>
            <p v-html="'[' + item.tableComment + ']' + item.fieldComment + ':' + item.content + '；'" v-for="item in employee.highlightList"></p>
          </div>
        </div>
      </div>
    </van-list>
    <van-popup v-model="showDate" position="bottom">
      <van-picker title="在职状态" show-toolbar :columns="columns" @confirm="onConfirm" @cancel="onCancel" value-key="name" />
    </van-popup>
  </div>
</template>

<script>
import KHCFDC from "@/assets/KHCFDC.svg"
import { jobStatusList, getTextSearch, tagColor } from "@/libs/api.js"
import { Toast } from "vant"
export default {
  name: "resumeSearch",
  data() {
    return {
      data: {
        pageNum: 1,
        pageSize: 10,
        keyword: "",
        jobStatus: "ALL",
      },
      loading: false,
      finished: false,
      showDate: false,
      employeeList: {},
      columns: [],
      statusSelected: {
        name: "全部",
      },
      KHCFDC: KHCFDC,
    }
  },
  created() {
    this.jobStatusList()
  },
  mounted() {
    this.$nextTick(() => {
      try {
        const input = document.getElementsByClassName("van-field__control")[document.getElementsByClassName("van-field__control").length - 1]
        console.log(input)
        input.focus()
      } catch (e) {}
    })
  },
  methods: {
    zaizhi() {
      if (this.data.jobStatus == "ALL") {
        this.data.jobStatus = "ON_JOB"
      } else {
        this.data.jobStatus = "ALL"
      }
      this.onSearch()
    },
    jobStatusList() {
      jobStatusList().then((res) => {
        this.columns = res.data.data
        this.statusSelected = this.columns[0]
        this.onSearch()
      })
    },
    tagColor(jobtype) {
      return tagColor(jobtype)
    },
    onLoad() {
      this.data.pageNum++
      this.getTextSearch()
    },
    viewEmployeeDetail() {},
    onConfirm(item) {
      this.statusSelected = item
      this.getTextSearch()
      this.showDate = false
    },
    onCancel() {
      this.showDate = false
    },
    getTextSearch() {
      let data = JSON.parse(JSON.stringify(this.data))
      if (data.keyword == "") {
        Toast("关键词能为空")
        return
      }
      getTextSearch(data).then((res) => {
        this.loading = false
        this.employeeList = [...this.employeeList, ...res.data.data]
        if (res.data.data.length < this.data.pageSize) this.finished = true
      })
    },
    onSearch() {
      this.data.pageNum = 1
      this.loading = false
      this.finished = false
      this.employeeList = []
      this.getTextSearch()
    },
    goback() {
      this.$emit("showresume", false)
    },
    viewEmployeeDetail(employee) {
      this.$router.push({
        name: "employeeInfo",
        query: {
          pkPsndoc: employee.pkPsnDoc,
          ncId: employee.ncId,
          goback: -1,
        },
      })
    },
  },
}
</script>
