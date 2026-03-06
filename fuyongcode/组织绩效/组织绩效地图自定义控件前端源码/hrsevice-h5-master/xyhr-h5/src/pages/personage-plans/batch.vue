<template>
  <!-- 批量评价页 -->
  <div class="batch">
    <div class="header">
      <div class="top" v-if="evaluateTaskList.length > 0">
        <div class="top-left">
          <van-checkbox
            v-model="checkAll"
            label-disabled
            @click="handleCheckAll"
            checked-color="#ee0a24"
          >
            <span :class="checkAll ? 'active' : ''">全选</span>
          </van-checkbox>
        </div>
        <div class="top-right">
          <span
            v-for="(it, ind) in statusList"
            :key="ind"
            @click="hanldeSelect(it)"
            >{{ it }}</span
          >
        </div>
      </div>
    </div>
    <div class="content" v-if="evaluateTaskList.length">
      <van-form ref="form">
        <div
          class="content-item"
          v-for="(ite, index) in evaluateTaskList"
          :key="index"
        >
          <div class="title">
            <van-checkbox
              v-model="ite.check"
              label-disabled
              checked-color="#ee0a24"
            >
              <span>{{ ite.taskName }}</span>
            </van-checkbox>
          </div>
          <div class="info">
            <div class="info-item">
              <span class="label">期间</span>
              <span class="value">{{ ite.period }}</span>
            </div>
            <div class="info-item">
              <span class="label">自评结果</span>
              <span class="value">{{ ite.selfResult }}</span>
            </div>
            <div class="info-item">
              <span class="label">自评结果描述</span>
              <span class="value">{{ ite.selfResultDes }}</span>
            </div>
            <div class="info-item">
              <span class="label star">评价结果</span>
              <span class="value" @click="handleIsShow(index)">
                <van-field
                  readonly
                  clickable
                  :value="evaluateTaskList[index].evaluatorResult"
                  :rules="eltResultRules"
                  placeholder="请选择"
                />
                <van-image
                  fit="cover"
                  width="6px"
                  height="11px"
                  :src="require('@/assets/personage-plans/right.png')"
                ></van-image>
              </span>
            </div>
            <div class="info-item">
              <span class="label">评价结果描述</span>
              <van-field
                class="value"
                v-model="ite.evaluatorResultDes"
                clearable
                type="textarea"
                placeholder="请输入"
              />
            </div>
          </div>
        </div>
      </van-form>
    </div>
    <div class="no-data" v-else>暂无评价内容</div>
    <div class="operation" v-if="evaluateTaskList.length">
      <span class="set" @click="handleCancel">取消</span>
      <span class="submit" @click="handleSave">保存</span>
    </div>
    <!-- 评价结果 -->
    <van-popup v-model="isShow" round position="bottom">
      <van-picker
        ref="deptPicker"
        show-toolbar
        title="评价结果"
        :columns="statusList"
        @confirm="onConfirm"
        @cancel="onCancel"
      />
    </van-popup>
  </div>
</template>

<script>
import { batchEvaluate, batchEvaluationSave } from "@/libs/api.js";
export default {
  data() {
    return {
      evaluateTaskList: [],
      statusList: ["超预期完成", "按计划完成", "未达预期"],
      isShow: false,
      SelectIndex: null,
      eltResultRules: [
        { required: true, message: "请选择评价结果", trigger: "onBlur" },
      ],
      planId: null,
      personNum: localStorage.getItem("pk_psndoc") || "",
    };
  },
  computed: {
    // 全选
    checkAll: {
      get() {
        // 默认没数据时为false
        let result = false;
        result = this.evaluateTaskList
          ? this.evaluateTaskList.every((ite) => ite.check)
          : false;
        return result;
      },
      set(newValue) {
        if (this.evaluateTaskList) {
          this.evaluateTaskList.forEach((ite) => {
            ite.check = newValue;
          });
        }
      },
    },
  },
  mounted() {
    this.planId = this.$route.query.planId;
    this.getBatchEvaluateData();
  },
  methods: {
    // 获取批量评价数据
    getBatchEvaluateData() {
      let params = {
        planId: this.planId,
        number: this.personNum,
      };
      batchEvaluate(params).then((res) => {
        if (res.status == 200 && res.data.statusCode == "200") {
          let evaluateTaskList = res.data.data.evaluateTaskList;
          evaluateTaskList.forEach((ite) => {
            this.$set(ite, "check", false);
          });
          this.evaluateTaskList = evaluateTaskList;
        }
      });
    },
    // 全选
    handleCheckAll() {
      this.evaluateTaskList.forEach((ite) => {
        ite.check = this.checkAll;
      });
    },
    onConfirm(value) {
      this.evaluateTaskList[this.SelectIndex].evaluatorResult = value;
      this.isShow = false;
    },
    onCancel() {
      this.isShow = false;
      this.SelectIndex = null;
    },
    handleIsShow(index) {
      this.SelectIndex = index;
      this.isShow = true;
      // 使用Picker示例身上的setColumnIndex方法 -- 让每次打开Picker 都默认为第一项
      this.$refs.deptPicker.setColumnIndex(0);
    },
    hanldeSelect(v) {
      if (this.evaluateTaskList) {
        this.evaluateTaskList.forEach((ite) => {
          if (ite.check) {
            ite.evaluatorResult = v;
          }
        });
      }
    },
    // 保存
    handleSave() {
      this.handleVerify();
    },
    handleCancel() {
      this.$router.go(-1);
    },
    // 校验
    handleVerify() {
      let evaluateTaskList = this.evaluateTaskList;
      // 是否都勾选 -- 都勾选true
      let isAnyUnchecked = evaluateTaskList.every((item) => item.check);
      if (isAnyUnchecked) {
        // 都勾选情况
        this.$refs.form.validate().then(() => {
          let params = [];
          const resultMapping = {
            超预期完成: "A",
            按计划完成: "B",
            未达预期: "C",
          };
          if (this.evaluateTaskList.length) {
            params = this.evaluateTaskList.map((ite) => {
              return {
                id: ite.id,
                eltResult: resultMapping[ite.evaluatorResult],
                eltResultDes: ite.evaluatorResultDes,
              };
            });
          }
          batchEvaluationSave({ params: params }).then((res) => {
            if (res.status == 200 && res.data.statusCode == "200") {
              this.$toast.success("保存成功");
              this.$router.go(-1);
            }
          });
        });
      } else {
        this.$toast.fail("未全部勾选");
        return;
      }
    },
    // batchEvaluationSave
    finallySave() {},
  },
};
</script>

<style lang="less" scoped>
.batch {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #eff1f4;
  .header {
    background-color: #ffffff;
    width: 100%;
    z-index: 99;
    .top {
      display: flex;
      align-items: center;
      box-sizing: border-box;
      padding: 12px 16px;
      .top-left {
        margin-right: 25px;
        span {
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 13px;
          color: #333;
        }
        .active {
          color: #d80c1e;
        }
      }
      .top-right {
        flex: 1;
        display: flex;
        justify-content: space-evenly;
        span {
          padding: 6px 5px;
          border-radius: 2px;
          border: 1px solid #d80c1e;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 14px;
          color: #d80c1e;
        }
      }
    }
  }
  .content {
    flex: 1;
    overflow: auto;
    .content-item {
      background-color: #ffffff;
      margin-top: 12px;
      .title {
        padding: 17px 0 17px 16px;
        border-bottom: 1px solid #eeeeee;
        span {
          font-family: PingFang SC;
          font-weight: 700;
          font-size: 15px;
          color: #333333;
        }
      }
      .info {
        box-sizing: border-box;
        padding: 0 16px 14px 16px;
        .info-item {
          position: relative;
          line-height: 40px;
          display: flex;
          // align-items: center;
          font-family: PingFang SC;
          font-weight: 500;
          font-size: 15px;
          .label {
            margin-right: 20px;
            width: 90px;
            color: #999999;
          }
          .star::after {
            display: inline-block;
            content: "*";
            position: absolute;
            color: #d80c1e;
          }
          .value {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex: 1;
            border-bottom: 1px solid #eeeeee;
            color: #333333;
          }

          /deep/ input {
            text-indent: 0px;
            font-size: 15px;
          }
          /deep/ .van-cell {
            line-height: 40px;
            padding: 0;
          }
          /deep/ textarea {
            text-indent: 0;
            font-size: 15px;
          }
        }
      }
    }
  }
  .no-data {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .operation {
    margin-top: 12px;
    background-color: #ffffff;
    display: flex;
    padding: 10px 16px;
    span {
      display: inline-block;
      flex: 1;
      padding: 16px 0;
      border-radius: 4px;
      text-align: center;
      font-family: PingFang SC;
      font-weight: 500;
      font-size: 18px;
    }
    .set {
      margin-right: 16px;
      color: #d80c1e;
      background: #ffffff;
      border: 1px solid #d80c1e;
    }
    .submit {
      color: #ffffff;
      background: #d80c1e;
    }
  }
  /deep/ .info-item .value .van-cell {
    border-bottom: none;
  }
  /deep/ .van-field__control::-webkit-input-placeholder {
    font-family: PingFang SC;
    font-weight: 500;
    color: #b8b8bd;
  }
  /deep/ .van-cell::after {
    border-bottom: none;
  }
}
</style>
