<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <div class="approval-list safe-bottom" v-if="activityInfo.activityName">
      <van-sticky>
        <van-search
          v-model="data.searchContent"
          placeholder="请输入您想搜索的员工姓名"
          background="#fff"
          @search="onSearch"
        />
        <div class="flex planBox">
          <div class="exam">
            {{ activityInfo.activityName }}
          </div>
          <!-- <div class="exam">{{ activityInfo.period }}</div> -->
        </div>
        <van-dropdown-menu>
          <van-dropdown-item title="状态" ref="item" :disabled="isChecked">
            <van-cell
              center
              :title="val.text"
              v-for="val in statusList"
              :key="val.id"
              @click="selectStatus(val)"
            >
              <template #right-icon>
                <van-checkbox v-model="val.status" checked-color="#D50C1C" />
              </template>
            </van-cell>
            <div class="flex">
              <div class="btn-box-cancel" @click="onCancel">取消</div>
              <div class="btn-box-confirm" @click="onConfirm">确认</div>
              <!-- <van-button block @click="onConfirm"> 取消 </van-button>
            <van-button type="danger" block >
              确认
            </van-button> -->
            </div>
          </van-dropdown-item>
          <van-dropdown-item
            v-model="titleText"
            :title="titleText"
            :title-class="titleText == '组织' ? '' : 'exColor'"
            @open="showFilterPicker = true"
          />
        </van-dropdown-menu>
      </van-sticky>
      <xy-empty v-if="list.length <= 0 && !loading"></xy-empty>

      <div class="batch-box" v-else>
        <span @click="batchOperation">{{
          isChecked ? "取消批量操作" : "批量操作"
        }}</span>
      </div>

      <div class="base-cell-group">
        <van-checkbox-group v-model="result" ref="checkboxGroup">
          <div class="base-cell" v-for="(item, index) in list" :key="item.id">
            <van-checkbox
              v-if="isChecked"
              :name="item.myBackLogId"
              shape="square"
              icon-size="20px"
              checked-color="#ee0a24"
              @click="toggle"
            />
            <div @click="navigator(item)" class="base-cell-box">
              <div class="base-cell-value">
                <van-image round :src="item.headSculpture" lazy-load />
                <div class="base-cell-value-info">
                  <div class="title over-text">
                    {{ item.name }} {{ item.post }}
                  </div>
                  <div class="sub over-text">
                    {{ item.company }} {{ item.organization }}
                  </div>
                </div>
              </div>
              <div class="base-cell-right">
                <div class="base-cell-status">
                  {{ item.handleStatus }}
                </div>
                <van-icon name="arrow" color="#999" />
              </div>
            </div>
          </div>
        </van-checkbox-group>
      </div>
      <div class="footer-fixed">
        <div class="base-cell-check" v-if="isChecked && list.length > 0">
          <van-checkbox
            v-model="allChecked"
            shape="square"
            icon-size="20px"
            checked-color="#ee0a24"
            @click="allCheckedChange"
            >全选</van-checkbox
          >
          <div class="text">
            已选<span class="red">{{ result.length }}</span
            >个
          </div>
          <van-button
            v-if="isShowReject"
            class="btn"
            type="default"
            @click="batchReject"
            >退回</van-button
          >
          <van-button class="btn" type="danger" @click="batchPass"
            >审批</van-button
          >
        </div>
      </div>
      <leadercommonTree
        :resetShow="true"
        :jobInfo="jobInfo"
        :workPosition="workPosition"
        :showFilterPicker="showFilterPicker"
        @isshowFilterPicker="isshowFilterPicker"
        @clickSearch="clickSearch"
      />
    </div>
  </van-pull-refresh>
</template>

<script>
import {
  getApprovalList,
  approvalListBatchPass,
  approvalListBatchReject,
  getApprovalListStatus,
} from "@/libs/api.js";
import FilterSearch from "@/pages/performances/components/FilterSearch";
import leadercommonTree from "@/components/leadercommonTree";

import { Dialog, Toast } from "vant";
export default {
  name: "ApprovalList",
  components: {
    FilterSearch,
    leadercommonTree,
  },
  data() {
    return {
      statusList: [],
      titleText: "组织",
      jobInfo: {},
      isChecked: false,
      allChecked: false,
      showFilterPicker: false,
      result: [],
      list: [],
      refreshing: false,
      activityInfo: {
        activityName: "",
        period: "",
      },
      data: {
        handleId: "",
        searchContent: "",
        organizationalId: "",
        handleStatusIds: [],
      },
      loading: false,
      workPosition: "0",
      isShowReject: true,
    };
  },
  created() {
    this.$store.commit("setKeepAlive", ["ApprovalList"]);
    this.getStatus();
  },
  beforeRouteLeave(to, form, next) {
    if (to.query.keepAlive) {
      let data = this.$store.state.keepAlive;
      data.push("ApprovalList");
      this.$store.commit("setKeepAlive", data);
      next();
    } else {
      this.$store.commit("removeKeepAlive", "ApprovalList");
      next();
    }
  },
  mounted() {},
  computed: {},
  methods: {
    getStatus() {
      const data = {
        handleId: this.$route.query.handleId,
      };
      getApprovalListStatus(data).then((res) => {
        this.statusList = this.arrayTransition(res.data.data.handleStatusList);
        this.statusList.forEach((item) => {
          if (item.text === "待审批") {
            this.data.handleStatusIds.push(item.value);
            item.status = true;
          }
        });
        this.data.handleId = this.$route.query.handleId;
        this.onSearch();
      });
    },
    batchOperation() {
      this.data.handleStatusIds = [];
      this.statusList.forEach((item) => {
        if (item.text === "待审批") {
          this.data.handleStatusIds.push(item.value);
          item.status = true;
        }
      });
      this.onSearch();

      this.isChecked = !this.isChecked;
    },
    onConfirm() {
      const arr = this.statusList;
      const statusList = [];
      arr.map((item) => {
        if (item.status) {
          statusList.push(item.value);
          // this.data.handleStatusIds.concat(item.value);
        }
      });
      this.data.handleStatusIds = statusList;
      this.$refs.item.toggle();
      this.onSearch();
    },
    onCancel() {
      this.$refs.item.toggle();
    },
    selectStatus(v) {
      v.status = !v.status;
    },
    arrayTransition(obj) {
      const handleStatusList = obj;
      return handleStatusList.map((item) => {
        const key = Object.keys(item)[0];
        return {
          text: item[key],
          value: key,
          status: false,
        };
      });
    },
    onRefresh() {
      this.list = [];
      this.getStatus();
    },
    onSearch() {
      this.$xy.showLoad();
      this.loading = true;
      this.refreshing = false;
      const data = this.data;
      console.log(data, "data");
      getApprovalList(data).then((res) => {
        if (res.data.statusCode === 200) {
          const dataInfo = res.data.data;
          this.isShow = data;
          this.activityInfo.activityName = dataInfo.activityName;
          this.activityInfo.period = dataInfo.period;
          this.list = dataInfo.empInfoDtoList;
          this.isShowReject = dataInfo.nodeType != "1070_S";
          this.$xy.hideLoad();
          this.loading = false;
        } else {
          this.$xy.hideLoad();
          Toast(res.data.message);
          setTimeout(() => {
            this.$router.go(-1);
          }, 2000);
        }
      });
    },
    batchPass() {
      const self = this;
      Dialog.confirm({
        message: "即将发送至下一节点，请确认 ？",
      })
        .then(() => {
          // const taskIdList = this.result.map((item) => ({ taskId: item }));
          const data = {
            taskIdList: this.result,
          };
          console.log(JSON.stringify(data));

          approvalListBatchPass(data).then((res) => {
            Toast(res.data.message);
            setTimeout(() => {
              self.onSearch();
            }, 2000);
          });
        })
        .catch(() => {
          // on cancel
        });
    },
    batchReject() {
      const self = this;
      Dialog.confirm({
        message: "即将退回至上一节点，请确认？",
      })
        .then(() => {
          const data = {
            taskIdList: this.result,
          };
          approvalListBatchReject(data).then((res) => {
            Toast(res.data.message);
            setTimeout(() => {
              self.onSearch();
            }, 2000);
          });
        })
        .catch(() => {
          // on cancel
        });
    },
    navigator(item) {
      this.$router.push({
        path: "approvalSingle",
        query: {
          id: item.myBackLogId,
          type: "待办",
          keepAlive: true,
        },
      });
    },
    toggle() {
      this.allChecked = this.result.length == this.list.length ? true : false;
    },
    allCheckedChange() {
      this.$refs.checkboxGroup.toggleAll(this.allChecked);
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value;
    },
    clickSearch(item) {
      this.data.pages = 0;
      this.data.organizationId = item.code;
      this.showFilterPicker = false;
      this.employeeList = [];
      this.onSearch();
    },
  },
};
</script>

<style lang="less" scoped>
/deep/ .exColor {
  color: #d80c1e;
}
.btn-box-confirm,
.btn-box-cancel {
  flex: 1;
  text-align: center;
  height: 40px;
  line-height: 40px;
}
.btn-box-confirm {
  // background-color: #d80c1e;
  // color: #fff;
  color: #d80c1e;
  border-left: 1px solid #ebedf0;
}
.safe-bottom {
  padding-bottom: calc(env(safe-area-inset-bottom) + 72px);
}
.approval-list {
  background: #f2f2f2;
  min-height: 100vh;
  box-sizing: border-box;
  /deep/ .van-dropdown-menu__bar {
    box-shadow: none;
    // border-bottom: 1px solid #ebedf0;
  }
  .planBox {
    background-color: #fff;
    padding: 0 12px 12px;
  }
  .exam {
    opacity: 0.8;
    font-size: 12px;
    color: #d80c1e;
    padding: 4px;
    background: #ffebea;
    margin-right: 8px;
    border-radius: 4px;
  }
}
.batch-box {
  display: flex;
  justify-content: flex-end;
  font-size: 12px;
  color: #d80c1e;
  padding: 12px;
}
.base-cell-group {
  background: #fff;
  // margin-bottom: 70px;
}
.base-cell-box {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  flex: 1;
  justify-content: space-around;
  .base-cell-right {
    display: flex;
    align-items: center;
  }
  .base-cell-status {
    line-height: 20px;
    font-size: 12px;
    margin-left: 8px;
    width: 52px;
    height: 20px;
    color: #ed6a0c;
    background: #fff7f0;
    border: 0.5px solid rgba(237, 106, 12, 1);
    border-radius: 2px;
    text-align: center;
    margin-right: 12px;
  }
}
.base-cell {
  display: flex;
  // align-items: center;
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
    display: flex;
    .base-cell-value-info {
      max-width: 188px;
    }
  }

  .over-text {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
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
  }
}
.base-cell-check {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  padding: 12px;
  background-color: #fff;
  position: relative;
  &:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 0;
    top: 0;
    left: 0;
    border-bottom: 1px solid #ebedf0;
    transform: scaleY(0.5);
  }
  .van-checkbox {
    margin-right: 12px;
  }
  .text {
    font-size: 14px;
    flex: 1;
    .red {
      color: red;
    }
  }
  .btn {
    flex: 1;
    margin: 0 6px;
    font-weight: bold;
  }
}
.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}
</style>
