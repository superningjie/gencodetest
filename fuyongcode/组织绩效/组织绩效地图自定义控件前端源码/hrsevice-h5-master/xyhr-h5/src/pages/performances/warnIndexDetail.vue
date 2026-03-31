<template>
  <div class="page">
    <van-sticky>
      <van-search
        v-model="data.queryName"
        placeholder="搜索人员"
        @search="search"
      />
      <van-dropdown-menu>
        <!-- :title="jobName || '岗位'" -->
        <van-dropdown-item
          v-model="data.year"
          :options="yearList"
          :title-class="data.year ? '' : 'exColor'"
          @change="search"
        />
        <van-dropdown-item
          v-model="type"
          :options="typeList"
          :title-class="type ? '' : 'exColor'"
          @open="search"
        />
      </van-dropdown-menu>
    </van-sticky>
    <div class="indexList pd12">
      <div class="indexNum">红灯指标4个</div>
      <div class="indexBox flex pd12" v-for="empInfo in list">
        <div class="indexLeft">灯</div>
        <div class="indexRight">
          <div class="indexInfo flex justify">
            <div>提升重点项目的过程管理能力</div>
            <div>2024Q1</div>
          </div>
          <div class="indexUserInfo">
            <div class="flex">
              <div class="avatar">
                <van-image
                  round
                  width="32px"
                  height="32px"
                  :src="empInfo.headSculpture"
                />
              </div>
              <div class="info">
                <div class="fw-text flex middle">
                  <p class="over-text">{{ empInfo.name }}</p>
                  <p class="ml8 over-text">{{ empInfo.position }}</p>
                </div>
                <div class="plain-text flex middle">
                  <p class="over-text">{{ empInfo.company }}</p>
                  <p class="ml8 over-text">{{ empInfo.organization }}</p>
                </div>
              </div>
            </div>
            <div class="indexDetailBox flex justify">
              <div class="pointBox">评分:3分</div>
              <div class="lightDetailBox flex justify">
                <div>亮灯次数:1</div>
                <div>详情 <van-icon name="arrow"></van-icon></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { employeeLightView } from "@/libs/api.js";
export default {
  name: "WarnIndexDetail",
  data() {
    return {
      type: 2,
      data: {
        year: 2024, //年份
        period: "", //周期
        actEvalObjId: "", //评估对象ID
        isSubordinate: false, //仅查看直属下属
      },
      yearList: [
        {
          text: 2022,
          value: 2022,
        },
        {
          text: 2023,
          value: 2023,
        },
        {
          text: 2024,
          value: 2024,
        },
      ],
      typeList: [
        {
          text: "红灯",
          value: 2,
        },
        {
          text: "绿灯",
          value: 4,
        },
      ],
      list: [
        {
          organization: "财务中心资金部",
          name: "徐蛟莉",
          company: "股份本部",
          id: "1758887363637000315",
          position: "资金专员",
          jobNumber: null,
          headSculpture: "https://img01.yzcdn.cn/vant/cat.jpeg",
          isShow: null,
        },
        {
          organization: "财务中心资金部",
          name: "徐蛟莉",
          company: "股份本部",
          id: "1758887363637000315",
          position: "资金专员",
          jobNumber: null,
          headSculpture: "https://img01.yzcdn.cn/vant/cat.jpeg",
          isShow: null,
        },
      ],
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.data.actEvalObjId = this.$route.query.id;
      const data = this.data;
      this.getList(data);
    },
    getList(data) {
      employeeLightView(data).then((res) => {});
    },
    search() {},
  },
};
</script>

<style lang="less" scoped>
.page {
  background: #f2f2f2;
  min-height: 100vh;
}
.indexNum {
  // margin-bottom: 12px;
}
.indexBox {
  margin-top: 12px;
  background-color: #fff;
  border-radius: 8px;
  .indexLeft {
    margin-right: 8px;
  }
  .indexRight {
    flex: 1;
    .indexInfo {
    }
    .indexUserInfo {
      padding: 8px 0;
      .info {
        margin-left: 8px;
      }
      .indexDetailBox {
        margin-top: 4px;
        .pointBox {
          width: 100px;
          padding: 8px;
          background-color: #fef4f5;
        }
        .lightDetailBox {
          margin-left: 12px;
          flex: 1;
          padding: 8px;
          background-color: #fef4f5;
        }
      }
    }
  }
}
</style>
