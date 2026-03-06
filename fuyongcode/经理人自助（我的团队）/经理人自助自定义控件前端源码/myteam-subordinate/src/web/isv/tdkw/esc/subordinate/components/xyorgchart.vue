<template>
  <div id="content-shell">
    <div class="search-header">
      <div class="job-org">
        <label>岗位：</label>
        <el-select
          v-model="positionVal"
          placeholder="请选择"
          @change="changePosition"
        >
          <el-option
            v-for="item in positionOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
          </el-option>
        </el-select>
      </div>
      <div class="work-org">
        <el-select
          v-model="worgOrgVal"
          placeholder="请选择"
          @change="changeWorkOrg"
        >
          <el-option
            v-for="item in workOrgData"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
          </el-option>
        </el-select>
        <el-button type="default" @click="exportAll">导出全部</el-button>
      </div>
    </div>

    <div class="org-chart">
      <div class="wrapper" ref="wrapper" id="chartID"></div>
    </div>
    <div class="my-team" v-if="Object.keys(teamData).length !== 0">
      <div class="title">我的团队</div>
      <div class="team-content">
        <div class="item" @click="toTeamInfo">
          <div>
            <b class="num">{{ teamData.psnCount }}</b
            >人
          </div>
          <span>团队人数</span>
        </div>
        <div class="item" @click="toTeamInfo">
          <div>
            <b class="num">{{ teamData.avgJoinGroupAge }}</b
            >年
          </div>
          <span>平均司龄</span>
        </div>
        <div class="item" @click="toTeamInfo">
          <div>
            <b class="num">{{ teamData.avgAge }}</b
            >岁
          </div>
          <span>平均年龄</span>
        </div>
      </div>
    </div>
    <div class="person-info" v-if="isShowCard">
      <div class="header">
        <i class="el-icon-close" @click="closeCard"></i>
      </div>
      <div class="info-content">
        <div class="top-info">
          <div class="avatar">
            <img :src="personInfo.ion" />
          </div>
          <div class="basic-info">
            <h2 class="name">{{ personInfo.name }}</h2>
            <p class="job">{{ personInfo.department }}</p>
            <div class="dropdown" @click="showDownPop">
              <i class="el-icon-arrow-down"></i>
            </div>
          </div>
        </div>
        <ul class="detail">
          <li>{{ personInfo.position }}</li>
          <li>{{ personInfo.phone }}</li>
          <li>{{ personInfo.phone }}</li>
          <li>{{ personInfo.mail }}</li>
        </ul>
      </div>
      <ul class="el-dropdown-menu" v-if="isShowPop">
        <li class="el-dropdown-menu__item">
          简历下载
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import KDChart from "./orgchart/kdchart.js";
import eventBus from "../../../../../../../util/eventBus";
// import mockData from "./orgchart/data.json";

export default {
  inject: ["model", "myInvoke", "KDApi", "props"],
  data() {
    return {
      positionOptions: [],
      workOrgData: [],
      teamData: {},
      positionVal: null,
      worgOrgVal: "1010",
      isShowCard: false,
      isShowPop: false,
      chart: null,
      data: null,
      baseCardSize: {
        width: 210,
        height: 88,
        xSpace: 36,
        ySpace: 48,
      },
      personInfo: {},
      selectPosition: null,
      selectWorkOrg: null,
    };
  },
  created() {
    const { positionData, collaborativeData } = this.props.data;
    this.positionOptions = positionData;
    this.positionVal =
      positionData && positionData.length > 0
        ? positionData.find((item) => item.isprimary === "1").id
        : null;
    this.workOrgData = collaborativeData;
    this.getTeamData();
  },
  mounted() {
    this.initChart = eventBus.sub(this.model, "initChart", (props) => {
      if (props.data) {
        this.data = props.data.data;
        this.teamData = props.data.teamBaseInfo[0];
      }
      this.init();
    });

    // this.data = mockData.data;
    // this.init();
  },
  computed: {},
  destroyed() {
    eventBus.unsub(this.initChart);
  },
  methods: {
    changeWorkOrg() {
      this.fetchCollaborative();
    },
    changePosition() {
      this.fetchCollaborative();
    },
    getTeamData() {
      const { positionData } = this.props.data;
      const positionid =
        positionData && positionData.length > 0
          ? positionData.find((item) => item.isprimary === "1").id
          : null;
      this.myInvoke("initdata", {
        positionid,
      });
    },
    fetchCollaborative() {
      this.myInvoke("collaborative", {
        positionid: this.positionVal,
        collaborativeId: this.worgOrgVal,
      });
    },
    exportAll() {
      this.myInvoke("export", {});
    },
    closeCard() {
      this.isShowCard = false;
    },
    showDownPop() {
      this.isShowPop = !this.isShowPop;
    },
    init() {
      if (!!this.data) {
        this.constructData([this.data], null);
        console.log(this.data, "data");
        this.chart = new KDChart({
          dom: this.$refs.wrapper,
          data: this.data,
          cardModel: this.getCardModel(),
          cardConfig: {
            baseCardSize: this.baseCardSize,
            expandLevel: 2,
            limitLevel: 5,
            marginLeft: this.marginLeft,
            actionBtnConfig: this.actionBtnConfig,
          },
          getCardStyle: this.getCardStyle,
          getCollapseStyle: this.getCollapseStyle,
          toCardDetail: this.toCardDetail,
        });
      } else {
        this.$refs.wrapper.innerHTML = `<div class="no-content">暂无数据</div>`;
      }
    },
    getCardModelType(node) {
      let cardModel = node.otclassify;
      return cardModel ? cardModel : "normal";
    },
    getCardModel() {
      return {
        normal: [
          {
            tag: "img",
            x: 17,
            y: 12,
            w: 70,
            h: 60,
            url: (node) => {
              return node.ion;
              // return "https://mmbiz.qpic.cn/mmbiz_jpg/URGhbghw5oWULXbic8KGU3fbiaBmBsiaNMswF1hwLlD6pfWbMIcZk8WRnicFVXMrfImUCE1GCu8bLDEA9j6bP5ibzQw/0?wx_fmt=jpeg";
            },
          },
          // 姓名
          {
            tag: "text",
            x: 87,
            y: 7,
            w: 120,
            h: 30,
            fontSize: 18,
            fontWeight: 600,
            color: (node) => {
              return node.parent ? "#333" : "#fff";
            },
            wMode: "lr",
            valign: "top",
            text: (node) => {
              return [node.name];
            },
          },
          // 部门
          {
            tag: "text",
            x: 87,
            y: 23,
            w: 120,
            h: 30,
            fontSize: 12,
            fontWeight: 400,
            color: (node) => {
              return node.parent ? "#333" : "#fff";
            },
            wMode: "lr",
            text: (node) => {
              return [node.department];
            },
          },
          // 手机号码
          {
            tag: "text",
            x: 87,
            y: 39,
            w: 120,
            h: 30,
            fontSize: 12,
            fontWeight: 400,
            color: (node) => {
              return node.parent ? "#333" : "#fff";
            },
            wMode: "lr",
            text: (node) => {
              return [node.phone];
            },
          },
          // 邮箱
          {
            tag: "text",
            x: 87,
            y: 54,
            w: 120,
            h: 30,
            fontSize: 12,
            fontWeight: 400,
            color: (node) => {
              return node.parent ? "#333" : "#fff";
            },
            wMode: "lr",
            text: (node) => {
              return [node.mail];
            },
          },
        ],
      };
    },
    getCardStyle() {},
    getCollapseStyle() {
      return {
        background: "#fff",
        border: "#CCCCCC",
        width: 0,
        height: 16,
      };
    },
    toCardDetail(node) {
      console.log(node, "node");
      this.myInvoke("showPersonFile", {
        positionid: node.positionid,
        personid: node.id,
      });
    },
    toTeamInfo() {
      this.myInvoke("showSubordinate", {
        positionid: this.data.positionid,
        personid: this.data.id,
        collaborativeId: this.worgOrgVal,
      });
    },
    constructData(data, pData) {
      for (let i = 0; i < data.length; i++) {
        data[i].cardModel = this.getCardModelType(data[i]);
        data[i].hasParent = !!pData || !!data[i].hasParent;
        data[i].parent = pData;
        data[i].hasChild = data[i].children && data[i].children.length > 0;
        data[i]._children = data[i].children;
        let children = data[i].children;
        if (data[i].islatestrecord === "true" && data[i].parent) {
          // 需要保证根节点的isHide永远为true
          let child = data[i].parent.children.concat(children || []);
          data[i].parent.children = child.filter(
            (item) => item.id !== data[i].id
          );
        }
        if (children && children.length > 0) {
          this.constructData(children, data[i]);
        }
      }
    },
  },
};
</script>

<style lang="less">
html,
body {
  height: 100%;
  padding: 0;
  margin: 0;
}
#content-shell {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background-color: #fff;
  overflow: hidden;
  position: relative;
  .search-header {
    width: 100%;
    padding: 8px 0 0 10px;
    font-family: Microsoft YaHei, sans-serif;
    color: #333;
    font-size: 14px;
    .job-org {
      display: inline-block;
      .el-select {
        width: 300px;
        .el-input__inner {
          height: 36px;
        }
        .el-input__icon {
          line-height: 36px;
        }
      }
    }
    .work-org {
      display: inline-block;
      margin-left: 25px;
      .el-input__inner {
        height: 36px;
      }
      .el-input__icon {
        line-height: 36px;
      }
      .el-button {
        margin-left: 20px;
        background: #0caef5;
        border-color: #0caef5;
        color: #fff;
      }
    }
  }
  .org-chart {
    width: 100%;
    height: 700px;
    .wrapper {
      width: 100%;
      height: 100%;
      position: relative;
      .no-content {
        margin: 100px;
        display: flex;
        justify-content: center;
      }
    }
  }
  .my-team {
    position: absolute;
    top: 0px;
    right: 20px;
    padding-top: 5px;
    .title {
      font-size: 14px;
      font-weight: 600;
    }
    .team-content {
      display: flex;
      margin-top: 8px;
      .item {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        width: 100px;
        height: 45px;
        background-color: #f4f6f8;
        cursor: pointer;
        &:not(:first-child) {
          margin-left: 20px;
        }
        b {
          font-size: 18px;
        }
        span {
          color: #bababa;
        }
      }
    }
  }
  .person-info {
    position: absolute;
    top: 80px;
    right: 20px;
    border: 1px solid #ddd;
    border-radius: 4px 0 0 4px;
    box-shadow: 0 5px 20px 0 #eee;
    width: 340px;
    height: 420px;
    background: #fff;
    .header {
      text-align: right;
      padding: 14px 14px 0 0;
      cursor: pointer;
      i {
        font-size: 28px;
        color: #ccc;
        cursor: pointer;
      }
    }
    .info-content {
      width: 260px;
      margin: 0 auto;
      .top-info {
        border-bottom: 1px solid #ddd;
        .avatar {
          margin: 0 auto;
          border: 1px solid #ddd;
          padding: 3px;
          width: 100px;
          height: 100px;
          border-radius: 50%;
          background-color: #fff;
        }
        .basic-info {
          position: relative;
          margin: 10px 0 17px;
          .name {
            margin: 0 auto;
            height: 30px;
            font-size: 24px;
            text-align: center;
          }
          .job {
            margin: 0 auto;
            width: 100%;
            height: 20px;
            line-height: 20px;
            font-size: 14px;
            color: #666;
            text-align: center;
          }
          .dropdown {
            position: absolute;
            top: 5px;
            right: 0;
            cursor: pointer;
            .el-icon-arrow-down {
              font-size: 15px;
            }
          }
        }
      }
      .detail {
        margin-top: 18px;
        li {
          height: 24px;
          font-size: 14px;
          color: #666;
          letter-spacing: 0;
          line-height: 24px;
          text-align: left;
        }
      }
    }
  }

  .el-dropdown-menu {
    margin: 5px 0;
    border: 1px solid #d1dbe5;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.12);
    padding: 6px 0;
    transform-origin: center top;
    z-index: 2008;
    position: absolute;
    left: 198px;
    top: 170px;
    width: 102px;
    height: 50px;
    line-height: 50px;
    .el-dropdown-menu__item {
      list-style: none;
      padding: 0 10px;
      margin: 0;
      cursor: pointer;
    }
  }
}

.el-select-dropdown__item.hover {
  color: #606266;
  // background-color: #1c8de0;
}
.el-select-dropdown__item.selected {
  color: #fff;
  background-color: #20a0ff;
}
</style>
