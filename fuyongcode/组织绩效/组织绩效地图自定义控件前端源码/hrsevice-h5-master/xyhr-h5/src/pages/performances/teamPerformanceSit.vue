<template>
  <div class="pages-bgc myachievementQuery">
    <van-sticky>
      <van-search
        v-model="value"
        placeholder="请输入您想搜索的内容"
        background="#fff"
        @search="onSearch"
      ></van-search>
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="value1"
          :options="option1"
          :title="value1 == '' ? '岗位' : ''"
          :title-class="value1 == '' ? '' : 'exColor'"
          @change="changeSelect1"
        />
        <van-dropdown-item
          v-model="value2"
          :title="value2 == '' ? '组织' : ''"
          :title-class="value2 == '' ? '' : 'exColor'"
          @open="changeSelect2"
        />
      </van-dropdown-menu>
    </van-sticky>
    <div class="base-cell-group mt12">
      <div
        class="base-cell"
        v-for="item in list"
        :key="item.id"
        @click="navigator(item.employeeName)"
      >
        <van-image round src="https://img01.yzcdn.cn/vant/cat.jpeg" />
        <div class="base-cell-value">
          <div class="title">
            {{ item.employeeName }} {{ item.employeeNum }}
          </div>
          <div class="sub flex middle">
            <div>
              {{ item.employeeOrganization }}
            </div>
            <div class="col-line"></div>
            <div>
              {{ item.jobTitle }}
            </div>
          </div>
        </div>
        <div class="exColor">{{ item.performanceResult }}</div>
        <van-icon name="arrow" color="#999" class="ml8" />
      </div>
    </div>
    <leaderCommonTree
      :resetShow="true"
      :jobInfo="jobInfo"
      :workPosition="workPosition"
      :showFilterPicker="showFilterPicker"
      @isshowFilterPicker="isshowFilterPicker"
      @clickSearch="clickSearch"
    />
  </div>
</template>

<script>
import {
  getOrganizationList,
  getTeamPerList,
  getmyteamDetail,
  getPositionPsnDetail,
} from "@/libs/api.js";
import leaderCommonTree from "@/components/leadercommonTree";

export default {
  name: "TeamPerformance",
  components: {
    leaderCommonTree,
  },
  data() {
    return {
      showActiveOnly: true,
      showFilterPicker: false,
      jobInfo: {}, // 个人岗位信息
      workPosition: "0",
      titleText: "所属组织",
      loading: true,
      finished: false,
      data: {
        pageNum: 1,
        pageSize: 10,
        name: "",
        retireFlag: !this.showActiveOnly,
        pk: "",
        type: "",
        isMainJob: "",
        pkPost: "",
      },
      list: [
        {
          employeeName: "陈峰",
          employeeNum: "2342",
          employeeOrganization: "XXXX集团本部",
          jobTitle: "人力资源",
          performanceResult: "AA",
        },
      ],
      value: "",
      value1: "",
      value2: "",
      option1: [
        { text: "岗位", value: 0 },
        { text: "XXXX科技-经理", value: 1 },
        { text: "集团本部-总监", value: 2 },
      ],
      option2: [
        { text: "组织", value: 0 },
        { text: "人力资源部", value: "a" },
        { text: "研发部", value: "b" },
        { text: "总裁办", value: "c" },
      ],
      data: {},
    };
  },
  created() {
    this.init();
  },
  mounted() {
    // this.init();
  },
  computed: {},
  methods: {
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    search() {
      this.employeeList = [];
      this.data.pageNum = 1;
      this.finished = false;
      this.getmyteam();
    },
    getmyteam() {
      this.workPosition =
        this.$route.params.workPosition || localStorage.getItem("workPosition");
      this.loading = true;
      this.data.retireFlag = !this.showActiveOnly;
      this.data.pkPost = this.jobInfo.pkPost;
      this.data.isMainJob = this.jobInfo.isMainJob;
      // 有岗位序列 带上岗位序列参数
      if (this.workPosition === "0") {
        // 展示左上角岗位
        this.showWorkPositionFlag = false;
        // 岗位子序列为'0' 查询原来的接口
        getmyteamDetail(this.data).then((res) => {
          if (res.data.statusCode == 200) {
            if (res.data.data.pages <= this.data.pageNum) {
              this.finished = true;
            }
            this.headCount = res.data.data.total;
            this.employeeList.push(...res.data.data.list);
          } else {
            Toast(res.data.message);
            this.finished = true;
          }
          this.loading = false;
        });
      } else {
        // 隐藏左上角岗位
        this.showWorkPositionFlag = true;
        // 不为'0' 则查询新接口
        this.data.postSubsequence = this.workPosition;
        getPositionPsnDetail(this.data).then((res) => {
          if (res.data.statusCode == 200) {
            if (res.data.data.pages <= this.data.pageNum) {
              this.finished = true;
            }
            this.headCount = res.data.data.total;
            this.employeeList.push(...res.data.data.list);
          } else {
            Toast(res.data.message);
            this.finished = true;
          }
          this.loading = false;
        });
      }
    },
    onLoad() {
      this.data.pageNum++;
      this.getmyteam();
    },
    init() {
      // getOrganizationList().then((res) => {
      //   this.option2 = this.arrayTransition(res.data.data.organizationList);
      // });
      this.getList();
      this.getmyteam();
      this.titleText = "所属组织";
    },
    getList() {
      const data = {};
      getTeamPerList().then((res) => {
        this.list = res.data.data.personnelList;
      });
    },
    arrayTransition(arr) {
      return arr.map((item) => ({
        value: item.pk,
        text: item.title,
      }));
    },
    onSearch() {},
    navigator(name) {
      this.$router.push({
        name: "MyPerformance",
        params: { name: name },
      });
    },
    changeSelect1(v) {
      // const item = this.option1.filter((item) => item.value == v);
      // console.log("v", item[0].text);
      // this.value1 = item[0].text;
    },
    changeSelect2() {
      this.showFilterPicker = true;
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    clickSearch(item) {
      console.log("item", item);
      this.data.pageNum = 0;
      this.data.pk = item.id;
      this.data.origin = item.origin;
      this.finished = false;
      this.data.type = item.type;
      this.titleText = item.name;
      if (item.id == "") {
        this.titleText = "所属组织";
      }
      this.showFilterPicker = false;
      this.employeeList = [];
      setTimeout(() => {
        if (this.data.pageNum == 0) {
          this.data.pageNum = 1;
          this.search();
        }
      }, 100);
    },
  },
};
</script>

<style lang="less" scoped>
/deep/.van-dropdown-menu__bar {
  box-shadow: none;
}
.myachievementQuery {
  padding-bottom: env(safe-area-inset-bottom);
}
/deep/ .exColor {
  color: #d80c1e;
}
.batch-box {
  display: flex;
  justify-content: flex-end;
  font-size: 12px;
  color: #d80c1e;
}
.base-cell-group {
  background: #fff;
}
.base-cell {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  padding: 10px 16px;
  position: relative;
  &:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    bottom: 0;
    left: 16px;
    border-bottom: 1px solid #ebedf0;
    transform: scaleY(0.5);
  }
  .van-checkbox {
    margin-right: 12px;
  }
  .van-image {
    width: 32px;
    height: 32px;
  }
  .base-cell-value {
    flex: 1;
  }
  .title {
    font-size: 14px;
    color: #212121;
    font-weight: bold;
    padding: 2px 12px;
  }
  .sub {
    font-size: 12px;
    color: #666;
    padding: 2px 12px;
    line-height: 18px;
  }
}
</style>
