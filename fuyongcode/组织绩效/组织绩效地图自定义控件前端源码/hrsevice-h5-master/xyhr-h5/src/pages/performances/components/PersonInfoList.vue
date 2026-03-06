<template>
  <div class="tableBox" v-if="list.length > 0">
    <div class="thBox">
      <div class="th">员工</div>
      <div class="th">岗位</div>
      <div class="th">等级</div>
    </div>
    <div class="tdBox" v-for="i in list">
      <div class="td">
        <div class="avatar">
          <!-- <van-image
            round
            width="24px"
            height="24px"
            src="https://img01.yzcdn.cn/vant/cat.jpeg"
          /> -->
          {{ i.personName }}
        </div>
      </div>
      <div class="td">{{ i.position }}</div>
      <div class="td">{{ i.changeLevelTxt }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: "PersonInfoList",
  props: {
    list: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
};
</script>

<style lang="less" scoped>
.tableBox {
  width: 100%;
  text-align: left;
  .thBox {
    display: flex;
    border: 1px solid #ccc;
    background-color: #f2f2f2;
    .th:nth-child(3) {
      border-right: none;
    }
    .th {
      font-weight: bold;
      flex: 1;
      padding: 12px;
      border-right: 1px solid #ccc;
    }
  }
  .tdBox {
    display: flex;
    border-bottom: 1px solid #ccc;
    border-left: 1px solid #ccc;
    .avatar {
      display: flex;
      align-items: center;
    }
    .td {
      flex: 1;
      padding: 12px;
      border-right: 1px solid #ccc;
    }
  }
}
</style>
