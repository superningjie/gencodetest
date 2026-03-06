<!-- @format -->

<template>
  <div class="home-box">
    <div class="top-bg"></div>
    <van-pull-refresh v-model="isRefresh" @refresh="onRefresh">
      <div class="title-box">人力自助</div>

      <div class="title-icon" @click="changeMe">
        <img class="icon" src="@/assets/home/chenchen.png" />
        <div class="text">计利天下，相与有成</div>
      </div>
      <!-- <img class="image-me" :src="getImage(imageName)" /> -->
      <div class="user-box">
        <van-skeleton avatar :row="3" :loading="isLoading">
          <div
            class="user"
            @click="
              gotoPage({ url: '/mobile.html?form=hspm_moberhome&app=hssc' })
            "
          >
            <img class="image" :src="userInfo.photo" />
            <div class="info">
              <div class="name">
                <div>{{ userInfo.name }}</div>
                <div class="dot"></div>
                <div>{{ userInfo.postname }}</div>
              </div>
              <div class="department">
                <div>{{ userInfo.orgname }}</div>
                <div class="dot"></div>
                <div>{{ userInfo.deptname }}</div>
              </div>
            </div>
          </div>
          <div class="detail">
            <div class="item-box">
              <img class="icon" src="@/assets/home/entry.png" />
              <div class="text">
                今天是您加入XXXX的第<span class="remind">
                  {{ userInfo.joinsysyear }} </span
                >年<span class="remind"> {{ userInfo.joinmonth }} </span>月<span
                  class="remind"
                >
                  {{ userInfo.joinsysdate }}
                </span>
                天
              </div>
            </div>
            <div class="item-box" @click="showLeavebalance">
              <img class="icon" src="@/assets/home/annual-leave.png" />
              <div class="text">
                年假剩余
                <span class="remind underline">{{
                  Number(userInfo.leavebalance)
                }}</span>
                天 <van-icon name="arrow" />
              </div>
            </div>
          </div>
        </van-skeleton>
      </div>
      <div class="model-box" v-if="appShowVo.staffSelfBool">
        <div class="model-title">
          <div class="title">办事大厅</div>
          <div class="edit" @click="showMoreApp">
            <span>更多 </span>
            <van-icon name="arrow" />
          </div>
        </div>
        <van-skeleton :row="25" :loading="isLoading">
          <div class="gird-box">
            <div
              class="gird-item"
              v-for="gird in appShowVo.staffApplications"
              :key="gird.id"
              @click="gotoPage(gird)"
            >
              <img class="icon" :src="gird.icon" />
              <div class="text">{{ gird.label }}</div>
            </div>
          </div>
          <div class="online-box">
            <div
              class="todo"
              @click="
                gotoPage({
                  url: '/mobile.html?form=bos_moblist&billFormId=xyjt_mob_report_inh&type=mobilelist&isFinish=false',
                })
              "
            >
              <div class="text">在办申请</div>
              <div class="number">{{ staffApply.inprogressCount }}</div>
            </div>
            <div class="right">
              <div
                class="todo finish"
                @click="
                  gotoPage({
                    url: '/mobile.html?form=bos_moblist&billFormId=xyjt_mob_report&type=mobilelist&isFinish=true',
                  })
                "
              >
                <div class="text">已办申请</div>
                <div class="number">{{ staffApply.completeCount }}</div>
              </div>
              <div
                class="todo study"
                @click="gotoExternal('VITE_URL_LEARNING')"
              >
                <div class="text">待学课程</div>
                <div class="number">{{ staffApply.learnCount }}</div>
              </div>
            </div>
          </div>
          <div class="func-box" @click="gotoExternal('VITE_URL_LEARN')">
            <div class="left">
              <img class="image" src="@/assets/home/ziaxianxuexi.png" />
              <div class="info">
                <div class="text">在线学习</div>
                <div class="remark">学以致用、学用相成</div>
              </div>
            </div>
            <van-icon name="arrow" color="#666666" />
          </div>
          <div class="func-box" @click="gotoPerformance">
            <div class="left">
              <img class="image" src="@/assets/home/jixiao.png" />
              <div class="info">
                <div class="text">绩效与评估</div>
                <div class="remark">目标导向、赋能经营</div>
              </div>
            </div>
            <van-icon name="arrow" color="#666666" />
          </div>
          <div class="func-box" @click="gotoInsure">
            <div class="left">
              <img class="image" src="@/assets/home/baoxian.png" />
              <div class="info">
                <div class="text">商业保险</div>
                <div class="remark">保险报销、一键即达</div>
              </div>
            </div>
            <van-icon name="arrow" color="#666666" />
          </div>
        </van-skeleton>
      </div>

      <div class="model-box" v-if="appShowVo.leaderSelfBool">
        <div class="model-title">
          <div class="title">领导查询</div>
        </div>
        <van-skeleton :row="5" :loading="isLoading">
          <div class="gird-function">
            <div
              v-if="haveApp('xyjt_thismonthentry')"
              class="gird-item-f"
              @click="gotoLeader('本月入职')"
            >
              <img class="image" src="@/assets/home/ruzhi.png" />
              <div class="content">
                <div class="text">本月入职</div>
                <div class="number">{{ statistic.entryCount }}</div>
              </div>
            </div>
            <div
              v-if="haveApp('xyjt_thismonthdepart')"
              class="gird-item-f"
              @click="gotoLeader('本月离职')"
            >
              <img class="image" src="@/assets/home/lizhi.png" />
              <div class="content">
                <div class="text">本月离职</div>
                <div class="number lizhi">{{ statistic.leaveCount }}</div>
              </div>
            </div>
            <div
              v-if="haveApp('xyjt_monthchanges')"
              class="gird-item-f"
              @click="gotoLeader('本月轮动')"
            >
              <img class="image" src="@/assets/home/biandong.png" />
              <div class="content">
                <div class="text">本月轮动</div>
                <div class="number biandong">{{ statistic.transferCount }}</div>
              </div>
            </div>
            <div
              v-if="haveApp('xyjt_addorganization')"
              class="gird-item-f"
              @click="gotoOrg('OrgAdd')"
            >
              <img class="image" src="@/assets/home/zuzhixinzeng.png" />
              <div class="content">
                <div class="text">组织新增</div>
                <div class="number little">{{ statistic.addCount }}</div>
              </div>
            </div>
            <div
              v-if="haveApp('xyjt_organizationrevoke')"
              class="gird-item-f"
              @click="gotoOrg('OrgRevoke')"
            >
              <img class="image" src="@/assets/home/zuzhichexiao.png" />
              <div class="content">
                <div class="text">组织撤销</div>
                <div class="number little">{{ statistic.backCount }}</div>
              </div>
            </div>
            <div
              v-if="haveApp('xyjt_organizationchange')"
              class="gird-item-f"
              @click="gotoOrg('OrgChange')"
            >
              <img class="image" src="@/assets/home/zuzhibiandong.png" />
              <div class="content">
                <div class="text">组织变动</div>
                <div class="number little">{{ statistic.changeCount }}</div>
              </div>
            </div>
          </div>
          <div class="gird-box in-bottom">
            <div
              class="gird-item"
              v-for="gird in appShowVo.leaderApplications"
              :key="gird.id"
              @click="gotoPage(gird)"
            >
              <img class="icon" :src="gird.icon" />
              <div class="text">{{ gird.label }}</div>
            </div>
            <div class="gird-item" @click="showMoreApp">
              <img class="icon" src="@/assets/home/quanbuyingyong.png" />
              <div class="text">全部应用</div>
            </div>
          </div>
        </van-skeleton>
      </div>
      <div class="model-box" v-if="appShowVo.managerSelfBool">
        <div class="model-title">
          <div class="title">我的团队</div>
        </div>
        <van-skeleton :row="5" :loading="isLoading">
          <div class="gird-function">
            <div class="gird-item-f" @click="gotoMyteam">
              <img class="image" src="@/assets/home/tuandui.png" />
              <div class="content">
                <div class="text">团队人数</div>
                <div class="number">{{ myTeam.psnCount }}</div>
              </div>
            </div>
            <div class="gird-item-f" @click="gotoMyteam">
              <img class="image" src="@/assets/home/siling.png" />
              <div class="content">
                <div class="text">平均司龄</div>
                <div class="number">{{ myTeam.avgJoinGroupAge }}</div>
              </div>
            </div>
            <div class="gird-item-f" @click="gotoMyteam">
              <img class="image" src="@/assets/home/nianling.png" />
              <div class="content">
                <div class="text">平均年龄</div>
                <div class="number">{{ myTeam.avgAge }}</div>
              </div>
            </div>
          </div>
          <div class="gird-box in-bottom team">
            <div
              class="gird-item"
              v-for="gird in appShowVo.managerApplications"
              :key="gird.id"
              @click="gotoPage(gird)"
            >
              <img class="icon" :src="gird.icon" />
              <div class="text">{{ gird.label }}</div>
            </div>
            <div class="gird-item" @click="showMoreApp">
              <img class="icon" src="@/assets/home/quanbuyingyong.png" />
              <div class="text">全部应用</div>
            </div>
          </div>
        </van-skeleton>
      </div>
      <div class="birth-box" v-if="appShowVo.birthDayNewsBool">
        <img class="backgraoun-image" src="@/assets/home/shengrifei.png" />
        <div class="birth-title" @click="gotoBirth">
          <div class="title">
            今日生日动态<span class="remind"> {{ birthList.length }} </span>人
          </div>
          <div class="edit">
            <van-icon name="arrow" size="14" color="#666666" />
          </div>
        </div>
        <van-skeleton :row="3" :loading="isLoading">
          <div class="birth-date">
            <img class="icon" src="@/assets/home/rili.png" />
            <span class="date">{{ currentDate }}</span>
          </div>
          <div class="person-box">
            <div
              class="person-item"
              v-for="(item, index) in birthList"
              :key="index"
              @click="showBirthBlessing(item)"
            >
              <div class="image">
                <img class="header" :src="item.src" />
                <img class="hat" src="@/assets/home/hat.png" />
              </div>
              <div class="name">{{ item.name }}</div>
            </div>
          </div>
          <div class="bulr"></div>
        </van-skeleton>
      </div>
    </van-pull-refresh>

    <!-- <MoreApp ref="MoreApp" /> -->
    <BirthBlessing ref="BirthBlessing" />
    <Leavebalance ref="Leavebalance" />
  </div>
</template>

<script>
import { appdata } from "@/libs/selfhelpdata";
import {
  getPsnInfo,
  getMyApplist,
  getUrl,
  queryNearlyBirthday,
  seorefQuery,
  myteamCount,
  statisticCount,
  getMyApply,
  getOrgCount,
  getTokenRealTime,
} from "@/libs/api.js";
import BirthBlessing from "@/pages/selfhelp/components/home/BirthBlessing";
import Leavebalance from "@/pages/selfhelp/components/home/Leavebalance";
import { Toast } from "vant";
import Dayjs from "dayjs";
import jsCookie from "js-cookie";
export default {
  name: "mySelfhelp",
  components: {
    BirthBlessing,
    Leavebalance,
  },
  data() {
    return {
      imageName: "blue1",
      isRefresh: false,
      isLoading: false,
      userInfo: {},
      appAuth: {},
      appShowVo: {
        // staffApplications: appdata.staffApplications,
        // leaderApplications: appdata.leaderApplications,
        // managerApplications: appdata.managerApplications,
        staffApplications: [],
        leaderApplications: [],
        managerApplications: [],
        staffSelfBool: true,
        leaderSelfBool: false,
        managerSelfBool: false,
        birthDayNewsBool: false,
      },
      // 员工自助模块
      staffApply: {
        completeCount: 0, // 已办
        inprogressCount: 0, // 待办
        learnCount: 0, // 已学
      },
      // 领导查询
      statistic: {
        entryCount: 0, // 入职人数
        leaveCount: 0, // 离职人数
        transferCount: 0, // 本月轮动
        addCount: 0, // 组织新增
        backCount: 0, // 组织撤销
        changeCount: 0, // 组织变动
      },
      // 我的团队
      myTeam: {
        avgAge: 0,
        avgJoinGroupAge: 0,
        psnCount: 0,
      },
      // 生日员工
      birthList: [
        {
          name: "李美玲",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big10001.jpg",
        },
        {
          name: "阿卡丽",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big25011.jpg",
        },
        {
          name: "欧阳登登",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big21016.jpg",
        },
        {
          name: "李美玲",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big99008.jpg",
        },
        {
          name: "李美玲",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big84000.jpg",
        },
        {
          name: "李美玲",
          src: "https://ossweb-img.qq.com/images/lol/web201310/skin/big37006.jpg",
        },
      ],
      currentDate: Dayjs().format("YYYY-MM-DD"),
    };
  },
  mounted() {
    // 获取系统枚举
    this.getEnumeration();
    // 获取当前用户信息和用户的权限应用以及各应用数据
    this.initData();
  },
  methods: {
    onRefresh() {
      this.isRefresh = false;
      this.initData();
    },
    gotoPage(app) {
      // window.location.origin
      if (app.url) {
        window.location.href = app.url;
      } else if (app.external) {
        this.gotoExternal(app.external);
      } else {
        this.$router.push({
          name: app.path,
        });
      }
    },
    showMoreApp() {
      this.$router.push({
        name: "MoreApp",
        // params: this.appAuth,
      });
    },
    showBirthBlessing(people) {
      this.$refs.BirthBlessing.show(people);
    },
    showLeavebalance() {
      this.$refs.Leavebalance.show(this.userInfo.annualLeaveDetailVo);
    },
    initData() {
      this.isLoading = true;
      this.$xy.showLoad();
      Promise.all([getPsnInfo(), getMyApplist(), getUrl()]).then((response) => {
        console.log("response==", response);
        // 用户信息
        const resUserInfo = response[0];
        if (resUserInfo.data.statusCode == 200) {
          this.userInfo = resUserInfo.data.data[0];
          this.$store.commit("setUserData", this.userInfo);
        }
        //用户app权限列表
        const resApp = response[1];
        if (resApp.data.statusCode == 200) {
          const appAuth = resApp.data.data;
          this.appAuth = appAuth;
          if (appAuth.showVo && appAuth.showVo.staffSelfBool) {
            // 在办已办申请数量
            getMyApply().then((res) => {
              if (resApp.data.statusCode == 200) {
                Object.assign(this.staffApply, res.data.data[0]);
              }
            });
          }
          // 领导模块数据查询
          if (appAuth.showVo && appAuth.showVo.leaderSelfBool) {
            //获取本月入职、离职、岗位变动人数
            statisticCount().then((res) => {
              if (res.data.statusCode == 200) {
                Object.assign(this.statistic, res.data.data);
              }
            });
            // 获取组织新增，撤销，变动数量
            getOrgCount().then((res) => {
              if (res.data.statusCode == 200) {
                Object.assign(this.statistic, res.data.data);
              }
            });
          }
          //获取我的团队人数、平均司龄、平均年龄
          if (appAuth.showVo && appAuth.showVo.managerSelfBool) {
            myteamCount({
              pkPost: "",
              isMainJob: "",
            }).then((res) => {
              if (res.data.statusCode == 200) {
                Object.assign(this.myTeam, res.data.data);
              }
            });
          }
          // 获取近期员工生日
          if (appAuth.showVo && appAuth.showVo.birthDayNewsBool) {
            queryNearlyBirthday({
              type: "",
              pk: "",
              origin: "",
              pkPost: "",
              isMainJob: "",
            }).then((res) => {
              if (res.data.statusCode == 200) {
                this.birthList = res.data.data.list;
              }
            });
          }
          // 处理app列表
          const attributeArr = [
            "staffApplications",
            "leaderApplications",
            "managerApplications",
          ];
          attributeArr.forEach((attribute) => {
            // 为保证排序的需求，以appAuth.showVo做循环获取
            const showVoTemp = [];
            appAuth.showVo[attribute].forEach((id) => {
              const app = appdata[attribute].find((app) => {
                return app.id == id;
              });
              if (app) {
                showVoTemp.push(app);
              }
            });
            this.appShowVo[attribute] = showVoTemp;
          });
          this.appShowVo.staffSelfBool = appAuth.showVo.staffSelfBool;
          this.appShowVo.leaderSelfBool = appAuth.showVo.leaderSelfBool;
          this.appShowVo.managerSelfBool = appAuth.showVo.managerSelfBool;
          this.appShowVo.birthDayNewsBool = appAuth.showVo.birthDayNewsBool;
          this.isRefresh = false;
        }
        // 外部跳转链接，目前待使用
        const resUrl = response[2];
        if (resUrl.data.statusCode == 200) {
        }
        this.$xy.hideLoad();
        this.isLoading = false;
        this.$store.commit("addKeepAlive", "mySelfhelp");
      });
    },
    getEnumeration() {
      seorefQuery({
        seotype: "psncl",
      }).then((res) => {
        this.$store.commit("setpreTypecolumns", res.data.data);
      });
      seorefQuery({
        seotype: "jobrank",
      }).then((res) => {
        this.$store.commit("setjobrankcolumns", res.data.data);
      });
      seorefQuery({
        seotype: "gglx",
      }).then((res) => {
        this.$store.commit("setGglxcolumns", res.data.data);
      });
    },
    gotoBirth() {
      this.$router.push({
        name: "Birthday",
        params: {
          isLeader: this.appAuth.showVo.leaderSelfBool,
        },
      });
    },
    gotoLeader(name) {
      this.$router.push({
        name: "leaderInfo",
        query: {
          name: name,
        },
      });
    },
    gotoPerformance(item) {
      this.$router.push({
        name: "myachievementQuery",
      });
    },
    gotoExternal(externalKey) {
      this.$xy.showLoad();
      const prefix = import.meta.env[externalKey];
      const sid = jsCookie.get("sid");
      getTokenRealTime({
        sid,
      })
        .then((res) => {
          this.$xy.hideLoad();
          if (res.data.statusCode == 200) {
            const token = res.data.token;
            const url = `${prefix}&sid=${sid}&token=${token}`;
            window.location.href = url;
          }
        })
        .catch(() => {
          this.$xy.hideLoad();
        });
    },
    gotoInsure() {
      window.location.href =
        "https://scrm.wisdominsurance.cn/mobile/h5/index.html#/";
    },
    gotoOrg(name) {
      this.$router.push({
        name: name,
      });
    },
    gotoMyteam() {
      this.$router.push({
        name: "myTeamInfo",
      });
    },
    haveApp(id) {
      return this.appAuth.showVo.leaderApplications.includes(id);
    },
  },
};
</script>
<style lang="less" scoped>
.home-box {
  color: #212121;
  padding: 0 16px;
  background-color: rgba(242, 242, 242, 1);
  padding-bottom: calc(18px + env(safe-area-inset-bottom));
  overflow: hidden;

  .title-box {
    // margin-top: 52px;
    margin-top: 30px;
    text-align: center;
    line-height: 28px;
    color: #ffffff;
    font-size: 18px;
    font-weight: bold;
    overflow: hidden;
    position: relative;
    z-index: 2;
  }

  .top-bg {
    background-image: url("@/assets/home/user-bg.png");
    background-repeat: no-repeat;
    background-size: 100% 100%;
    position: absolute;
    width: 375px;
    height: 280px;
    top: 0;
    left: 0;
    z-index: 1;
  }
}

.title-icon {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
  margin-top: 14px;

  .icon {
    width: 34px;
  }

  .text {
    color: #ffffff;
    font-size: 12px;
    margin-left: 4px;
    line-height: 1;
  }
}

.user-box {
  position: relative;
  z-index: 2;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.5) 0%,
    #ffffff 100%
  );
  border-radius: 10px;
  border: 1px solid #ffffff;
  padding: 16px;
  margin-top: 8px;

  .user {
    display: flex;
    align-items: center;

    .image {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
    }

    .info {
      margin-left: 12px;

      .name {
        font-size: 16px;
        line-height: 24px;
        display: flex;
        align-items: center;
      }

      .department {
        font-size: 12px;
        line-height: 18px;
        margin-top: 4px;
        display: flex;
        align-items: center;
      }

      .dot {
        width: 2px;
        height: 2px;
        background: #212121;
        margin: 0 8px;
        border-radius: 50%;
      }
    }
  }

  .detail {
    background-color: #f7f7f7;
    border-radius: 6px;
    margin-top: 26px;
    padding: 6px 10px;

    .item-box {
      display: flex;
      align-items: center;

      &:last-child {
        margin-top: 4px;
      }

      .icon {
        width: 20px;
        height: 20px;
      }

      .text {
        margin-left: 10px;
        font-size: 12px;
        color: #666666;
        line-height: 18px;

        .remind {
          color: rgba(216, 12, 30, 1);

          &.underline {
            text-decoration: underline;
          }
        }
      }
    }
  }
}

.model-box {
  background: #ffffff;
  border-radius: 10px;
  padding: 12px 16px 20px;
  margin-top: 12px;

  .model-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .title {
      font-size: 16px;
      line-height: 24px;
      color: #212121;
      font-weight: bold;
    }

    .edit {
      color: #666666;
      font-size: 12px;
    }
  }

  .gird-box {
    display: flex;
    flex-wrap: wrap;

    &.in-bottom {
      margin-bottom: -20px;
    }

    &.team {
      margin-top: 10px;
    }

    .gird-item {
      width: 60px;
      color: #212121;
      text-align: center;
      margin-left: 23px;
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      align-items: center;

      &:nth-child(4n + 1) {
        margin-left: 0;
      }

      .icon {
        width: 40px;
        height: 40px;
        display: block;
      }

      .text {
        font-size: 12px;
        text-align: center;
        margin-top: 8px;
        line-height: 18px;
      }
    }
  }

  .online-box {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .todo {
      width: 136px;
      height: 134px;
      background-image: url("@/assets/home/zaiban.png");
      background-repeat: no-repeat;
      background-size: 100% 100%;
      overflow: hidden;
      color: #212121;

      &.finish {
        width: 165px;
        height: 62px;
        background-image: url("@/assets/home/yibanshenqing.png");

        .text {
          margin-top: 8px;
          margin-left: 18px;
          font-size: 12px;
        }

        .number {
          font-size: 20px;
          margin-top: 4px;
          margin-left: 18px;
          line-height: 24px;
        }
      }

      &.study {
        margin-top: 10px;
        width: 165px;
        height: 62px;
        background-image: url("@/assets/home/daixuekecheng.png");

        .text {
          margin-top: 8px;
          margin-left: 18px;
          font-size: 12px;
        }

        .number {
          font-size: 20px;
          margin-top: 4px;
          margin-left: 18px;
          line-height: 24px;
        }
      }

      .text {
        margin-top: 20px;
        margin-left: 20px;
        font-size: 14px;
        line-height: 18px;
      }

      .number {
        font-size: 24px;
        margin-top: 12px;
        margin-left: 20px;
        font-weight: bold;
      }
    }

    .right {
      margin-left: 10px;
    }
  }

  .func-box {
    background: #f7f9fa;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    margin-top: 14px;

    .left {
      display: flex;
      align-items: center;
    }

    .image {
      width: 40px;
      height: 40px;
    }

    .info {
      margin-left: 14px;
      font-size: 12px;

      .text {
        color: #212121;
        font-weight: bold;
        line-height: 18px;
      }

      .remark {
        color: #666666;
        margin-top: 6px;
        line-height: 16px;
      }
    }
  }

  .gird-function {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 4px;

    .gird-item-f {
      width: 95.5px;
      height: 72px;
      margin-left: 12px;
      margin-bottom: 12px;
      position: relative;
      color: #212121;

      &:nth-child(3n + 1) {
        margin-left: 0;
      }

      .image {
        position: absolute;
        width: 100%;
        left: 0;
        top: 0;
        z-index: 1;
      }

      .content {
        position: relative;
        z-index: 2;

        .text {
          font-size: 12px;
          font-weight: 400;
          line-height: 18px;
          margin-top: 12px;
          margin-left: 12px;
        }

        .number {
          font-size: 24px;
          line-height: 24px;
          margin-top: 8px;
          margin-left: 12px;

          &.little {
            font-size: 20px;
          }

          &.lizhi {
            color: #d80c1e;
          }

          &.biandong {
            color: #ed6a0c;
          }
        }
      }
    }
  }
}

.birth-box {
  background: linear-gradient(359deg, #feeeee 0%, #ffffff 100%);
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 12px;
  height: 136px;
  position: relative;

  .backgraoun-image {
    width: 100px;
    position: absolute;
    top: 40px;
    left: 50%;
    transform: translateX(-50%);
  }

  .birth-title {
    color: #212121;
    line-height: 24px;
    font-size: 14px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    position: relative;
    z-index: 2;

    .remind {
      color: #d80c1e;
    }
  }

  .birth-date {
    font-size: 12px;
    color: #212121;
    line-height: 18px;
    display: flex;
    align-items: center;
    margin-top: 8px;
    position: relative;
    z-index: 2;

    .icon {
      width: 12px;
      height: 12px;
      margin-right: 8px;
    }
  }

  .person-box {
    position: absolute;
    z-index: 3;
    width: 100%;
    left: 0;
    top: 79px;
    overflow-x: scroll;
    white-space: nowrap;

    .person-item {
      width: 56px;
      margin-left: 16px;
      display: inline-block;
      text-align: center;
      position: relative;

      &:last-child {
        margin-right: 16px;
      }

      .image {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: linear-gradient(
          360deg,
          rgba(255, 80, 67, 1),
          rgba(255, 160, 153, 1)
        );
        display: flex;
        align-items: center;
        margin-left: 10px;
        margin-top: 8px;
        position: relative;

        .header {
          width: 33px;
          height: 33px;
          border-radius: 50%;
          overflow: hidden;
          display: inline-block;
          margin-left: 1.5px;
        }

        .hat {
          width: 25px;
          height: 25px;
          top: -10px;
          left: 19px;
          position: absolute;
        }
      }

      .name {
        height: 18px;
        margin-top: -6px;
        background: linear-gradient(178deg, #ffa18c 0%, #ff5043 100%);
        border-radius: 9px;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #ffffff;
        font-size: 12px;
        position: relative;
        z-index: 4;
      }
    }
  }

  .bulr {
    width: 15px;
    height: 83px;
    position: absolute;
    top: 72px;
    right: 0;
    background: #feeeee;
    border-radius: 2px;
    filter: blur(7px);
    z-index: 10;
  }
}

/deep/ .van-pull-refresh__track {
  z-index: 2;
}

/deep/ .van-loading__text {
  color: white;
}

/deep/ .van-loading__circular {
  color: white;
}

/deep/ .van-pull-refresh__head {
  color: white;
}
.image-me {
  width: 40px;
}
</style>
