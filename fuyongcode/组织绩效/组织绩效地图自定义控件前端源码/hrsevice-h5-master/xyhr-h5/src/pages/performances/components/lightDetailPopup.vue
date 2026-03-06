<template>
  <van-popup
    v-model="show"
    position="bottom"
    :close-on-click-overlay="false"
    safe-area-inset-bottom
  >
    <div class="popup pd12">
      <div class="topBox">
        <div class="flex justify">
          <div>详情</div>
          <van-icon name="cross" size="16" @click="close"></van-icon>
        </div>
        <van-divider />
      </div>
      <div class="popupDetails">
        <div>亮灯详情</div>
        <div class="lightingDetailsBox">
          <div class="flex lightingTitle">
            <div class="f1">考核周期</div>
            <div class="f2">考核活动</div>
            <div class="f3">亮灯类型</div>
          </div>
          <div class="flex pd12" v-for="i in detailInfo.lightUpInfo">
            <div class="f1">{{ i.period }}</div>
            <div class="f2">{{ i.periodActivityName }}</div>
            <div class="f3" v-if="i.lightType == 'redLight'">红灯</div>
            <div class="f3" v-if="i.lightType == 'yellowLight'">黄灯</div>
            <div class="f3" v-if="i.lightType == 'greenLight'">绿灯</div>
          </div>
        </div>
        <div>指标详情</div>
        <div class="indexDetailsBox">
          <div class="infoListBox">
            <p class="box1">员工姓名</p>
            <p class="box2">{{ detailInfo.personName }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">员工工号</p>
            <p class="box2">{{ detailInfo.personNumber }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">考核活动</p>
            <p class="box2">{{ detailInfo.activityName }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">指标类型</p>
            <p class="box2">{{ detailInfo.evalType }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">指标名称</p>
            <p class="box2">{{ detailInfo.indCtrName }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">指标说明</p>
            <p class="box2">{{ detailInfo.indCtrDesc }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">权重</p>
            <p class="box2">{{ detailInfo.weight }}</p>
          </div>
          <div class="infoListBox">
            <p class="box1">评分</p>
            <p class="box2">{{ detailInfo.ies }}</p>
          </div>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script>
export default {
  name: "lightDetailPopup",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    detailInfo: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  methods: {
    close() {
      this.$emit("close", false);
    },
  },
};
</script>

<style lang="less" scoped>
.lightingDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  .lightingTitle {
    padding: 12px;
    background-color: #f2f2f2;
  }
  .f1 {
    width: 80px;
  }
  .f2 {
    flex: 1;
  }
  .f3 {
    width: 80px;
    text-align: center;
  }
}
.popup {
  //   max-height: 70vh;
  .topBox {
    font-weight: bold;
  }
}
.popupDetails {
  max-height: 70vh;
  overflow: auto;
  font-size: 14px;
}
.indexDetailsBox {
  margin: 12px 0;
  border: 0.5px solid #ccc;
  .infoListBox {
    padding: 6px 12px;
    display: flex;
    line-height: 24px;
    .box1 {
      width: 80px;
    }
    .box2 {
      flex: 1;
    }
  }
}
</style>
