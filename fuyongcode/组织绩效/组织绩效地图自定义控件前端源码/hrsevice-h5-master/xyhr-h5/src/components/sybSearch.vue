<template>
  <div class="administrativeFramework">
    <van-nav-bar left-arrow class="navStyle" @click-left="goback">
      <template #title>
        <van-search class="searchItem" shape="round" v-model="data.keyword" placeholder="输入关键字搜索"
          style="padding-bottom: 0;" @search="onSearch" />
      </template>
    </van-nav-bar>
    <xy-empty v-if="employeeList.length==0"></xy-empty>
    <van-list v-model="loading" :finished="finished" :finished-text="employeeList.length>0?'没有更多了':''" @load="onLoad"
      :immediate-check="false">
      <div class="employeeList">
        <div class="employee flex middle" @click="viewEmployeeDetail(employee)" v-for="employee in employeeList">
          <van-image :src="employee.photo" round width="48px" height="48px" fit="fill" />
          <div class="info">
            <i :class="'tag '+tagColor(employee.jobStatus)" v-if="employee.jobStatus && employee.jobStatus != '兼职'">{{employee.jobStatus}}</i>
            <h1>{{employee.name}} | {{employee.postName}}</h1>
            <p>{{employee.age}}岁|{{employee.education}}|司龄{{employee.workAgeOfCompany}}年|工龄{{employee.workAge}}年
              <van-icon name="arrow" size="10" />
            </p>
            <p v-html="'['+item.tableComment+']'+item.fieldComment+':'+item.content+'；'"
              v-for="item in employee.highlightList"></p>
          </div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script>
  import {
    jobStatusList,
    getTextSearch,
    tagColor
  } from '@/libs/api.js';
  import {
    Toast
  } from 'vant';
  export default {
    name: "resumeSearch",
    data() {
      return {
        data: {
          pageNum: 1,
          pageSize: 10,
          keyword: "",
          jobStatus: ""
        },
        loading: false,
        finished: false,
        showDate: false,
        employeeList: {

        },
        columns: [],
      }
    },
    created() {
      this.jobStatusList()
    },
    methods: {
      jobStatusList() {
        jobStatusList().then(res => {
          this.columns = res.data.data;
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
      viewEmployeeDetail() {

      },
      onConfirm(item) {
        this.statusSelected = item
        this.getTextSearch()
        this.showDate = false;
      },
      onCancel() {
        this.showDate = false;
      },
      getTextSearch() {

      },
      onSearch() {
        this.data.pageNum = 1
        this.loading = false
        this.finished = false
        this.employeeList = []
        this.getTextSearch()
      },
      goback() {
        this.$router.go(-1)
      },
      viewEmployeeDetail(employee) {
        this.$router.push({
          name: 'employeeInfo',
          query: {
            pkPsndoc: employee.pkPsnDoc,
            goback: -1
          }
        })
      },
    }
  }
</script>
