<!-- @format -->

<template>
  <div class="teaminfo myTeamInfo leaderInfo">
    <!-- 正文列表 -->
    <div class="container">
      <!-- 部门筛选 -->
      <div class="flex justify hl filter">
        <a class="flex middle">
          <a @click="showFilterPicker = true" class="rangeText">{{ searchText.orgName }} <van-icon name="arrow-down" /></a>
        </a>
        <a class="flex middle">
          <!-- titleText2.substr(0, 4) -->
          <a @click="$refs.Jobrank.show()" class="rangeText">{{ searchText.jobName }} <van-icon name="arrow-down" /></a>
        </a>
        <a class="flex middle">
          <a @click="$refs.ConvenientCander.show()" class="rangeText">{{ this.params.start || searchText.timeName }}<van-icon name="arrow-down" /></a>
        </a>
      </div>
      <!-- 搜索框 -->
      <form action="/">
        <van-search class="searchItem" shape="round" placeholder="输入内容提示" v-model="params.name" @search="initData" />
      </form>
      <!-- 数据列表 -->
      <div class="employeeList">
        <xy-empty v-if="finished == true && orgList.length == 0"></xy-empty>
        <van-list v-model="loading" :finished-text="orgList.length > 0 ? '没有更多了' : ''" :finished="finished" @load="getData" :immediate-check="false">
          <div class="org-item" v-for="organize in orgList">
            <div class="title-box">
              <div class="title">{{ organize.orgName }}</div>
              <div class="title-tag">{{ organize.orgType }}</div>
            </div>
            <div class="text-box">
              <div class="text">所属板块：{{ organize.sector }}</div>
              <div class="text">所属公司：{{ organize.company }}</div>
              <div class="text">{{ type == "OrgRevoke" ? "撤销日期" : "生效日期" }}：{{ organize.effectiveDate }}</div>
            </div>
            <!-- 组织变更展示变更记录 -->
            <div class="org-tag" v-if="type == 'OrgChange'">{{ organize.changeScene }}</div>
            <template v-if="type == 'OrgChange' && organize.changeDetails.length > 0">
              <div class="line-box" @click="organize.isShow = !organize.isShow">
                <div class="icon-box">
                  <van-icon :class="[organize.isShow ? 'roate' : '']" name="arrow-down" size="14" color="#D80C1E" />
                </div>
              </div>
              <div class="detail-box" v-show="organize.isShow">
                <div class="detail-item" v-for="(changeItem, index) in organize.changeDetails" :key="index">
                  <div class="title">{{ changeItem.elementContent }}：</div>
                  <div class="detail-text">
                    <div class="text">变动前：{{ changeItem.beforeVal }}</div>
                    <div class="text">变动后：{{ changeItem.afterVal }}</div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </van-list>
      </div>
    </div>
    <!-- 所属组织 -->
    <mycommonTree :showFilterPicker="showFilterPicker" title="选择组织" @isshowFilterPicker="isshowFilterPicker" @clickSearch="clickSearch" :scope="1" />
    <!-- 行政组织 -->
    <Jobrank ref="Jobrank" @success="checkJobrank" />
    <!-- 日期 -->
    <ConvenientCander ref="ConvenientCander" @success="checkDate" />
  </div>
</template>

<script>
import { getAdddetail, getBackdetail, getChangedetail } from "@/libs/api.js"
import mycommonTree from "@/components/mycommonTree"
import Jobrank from "./Jobrank"
// import ConvenientCander from "./ConvenientCander" 
import ConvenientCander from "@/components/xyCalendar/Index.vue"
export default {
  name: "leaderInfo",
  data() {
    return {
      type: "", // 组织路由类型
      loading: true,
      finished: false,
      showFilterPicker: false, // 选择组织
      params: {
        pageNum: 0,
        pageSize: 10,
        orgNum: "", //组织类型编码
        orgId: "", //组织d
        start: "",
        end: "",
        orgType: [],
      },
      middleColumns: [],
      preTypecolumns: [],
      jobrankcolumns: [],
      headCount: 0,
      // orgList
      orgList: [],
      searchText: {
        orgName: "所属组织",
        jobName: "行政组织类型",
        timeName: "本月",
      },
    }
  },
  components: {
    mycommonTree,
    Jobrank,
    ConvenientCander,
  },
  mounted() {
    this.type = this.$route.name
    this.initData()
  },
  methods: {
    isshowFilterPicker(value) {
      this.showFilterPicker = value
    },
    initData() {
      this.orgList = []
      this.params.pageNum = 1
      this.finished = false
      this.getData()
    },
    clickSearch(data) {
      console.log("clickSearch==", data)
      this.params.orgNum = data.code
      this.params.orgId = data.id
      this.initData()
      this.showFilterPicker = false
    },
    checkJobrank(data) {
      this.params.orgType = data
      this.initData()
    },

    checkDate(data) {
      this.params.start = data[0]
      this.params.end = data[1]
      this.initData()
    },
    goback() {
      this.$router.go(-1)
    },
    getData() {
      const method = {
        OrgAdd: "getAdddetail",
        OrgRevoke: "getBackdetail",
        OrgChange: "getChangedetail",
      }
      this[method[this.type]]()
      // this.getAdddetail()
      // this.getBackdetail()
      // this.getChangedetail()
    },
    // 获取组织新增明细
    getAdddetail() {
      this.loading = true
      getAdddetail(this.params).then((res) => {
        if (res.data.statusCode == 200) {
          const list = res.data.data || []
          if (list.length < this.params.pageSize) {
            console.log("finished")
            this.finished = true // 返回的条数小于每页获取的条数，说明后面没数据了
          }
          this.orgList.push(...list)
        }
        this.loading = false
        this.params.pageNum++
      })
    },
    // 组织撤销明细
    getBackdetail() {
      this.loading = true
      getBackdetail(this.params).then((res) => {
        if (res.data.statusCode == 200) {
          const list = res.data.data || []
          if (list.length < this.params.pageSize) {
            console.log("finished")
            this.finished = true // 返回的条数小于每页获取的条数，说明后面没数据了
          }
          this.orgList.push(...list)
        }
        this.loading = false
        this.params.pageNum++
      })
    },
    // 组织变动明细
    getChangedetail() {
      this.loading = true
      getChangedetail(this.params).then((res) => {
        if (res.data.statusCode == 200) {
          const list = res.data.data || []
          list.forEach((item) => {
            item.isShow = false
            // if (item.changeDetails.length > 0) {
            //   Object.assign(item, item.changeDetails[0])
            // }
          })
          if (list.length < this.params.pageSize) {
            console.log("finished")
            this.finished = true // 返回的条数小于每页获取的条数，说明后面没数据了
          }
          this.orgList.push(...list)
        }
        this.loading = false
        this.params.pageNum++
      })
    },
    handleData() {},
  },
}
</script>
<style lang="less" scoped>
.teaminfo {
  height: 100vh;
}

.container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 20px - env(safe-area-inset-bottom));
  padding-bottom: 0;
}

.org-item {
  color: #121212;
  padding: 16px;
  background: #ffffff;
  box-shadow: 0px 1px 6px 0px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  margin-bottom: 14px;
  position: relative;
  .title-box {
    .title {
      font-size: 14px;
      font-weight: bold;
      line-height: 20px;
      display: inline;
      text-align: justify;
      word-break: break-all;
    }

    .title-tag {
      background-color: #12ace0;
      display: inline-block;
      font-size: 12px;
      color: white;
      border-radius: 8px;
      line-height: 16px;
      padding: 0 8px;
      margin-left: 4px;
    }
  }

  .text-box {
    font-size: 12px;
    color: #444444;
    line-height: 18px;

    .text {
      margin-top: 10px;
      text-align: justify;
      word-break: break-all;
    }
  }

  .line-box {
    height: 0.5px;
    background-color: #dedede;
    position: relative;
    margin-top: 10px;

    .icon-box {
      width: 30px;
      height: 14px;
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      top: -7px;
      background-color: white;
      text-align: center;
      line-height: 14px;

      .roate {
        transform: rotate(180deg);
      }
    }
  }

  .detail-box {
    .detail-item {
      margin-top: 10px;
      font-size: 12px;
      line-height: 17px;

      .title {
        color: #121212;
        font-weight: bold;
      }

      .detail-text {
        color: #666666;

        .text {
          margin-top: 2px;
        }
      }
    }
  }
}

.employeeList {
  flex: 1;
  overflow-y: auto;
  padding: 4px;
}

.searchItem {
  padding: 20px 0 10px 0;
}
.org-tag {
  width: 64px;
  background: #d80c1e;
  border-radius: 0px 8px 0px 8px;
  position: absolute;
  right: 0;
  top: 0;
  color: white;
  line-height: 18px;
  font-size: 12px;
  text-align: center;
}
</style>
