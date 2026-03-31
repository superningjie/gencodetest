<!-- @format -->

<template>
  <!-- 日期 -->
  <van-popup v-model="isShow" :lazy-render="false" round :style="{ width: '80%' }">
    <div class="group leaderInfo" style="padding: 10px">
      <div class="flex justify quickPick">
        <span @click="setDate(btn.value)" :class="quickPickIndex == btn.value ? 'active' : ''" v-for="btn in btnList" :key="btn.value">{{ btn.label }}</span>
      </div>
      <van-calendar
        ref="calendar"
        :show-title="false"
        :poppable="false"
        type="range"
        :lazy-render="false"
        :min-date="new Date(2000, 0, 1)"
        :max-date="new Date()"
        :style="{ height: '400px' }"
        :first-day-of-week="1"
        :allow-same-day="true"
        :row-height="48"
        @confirm="onconfirm"
        v-model="isShow"
        :show-subtitle='false'
        :show-mark='false'
      />
    </div>
  </van-popup>
</template>
<script>
import dayjs from "dayjs"
export default {
  data() {
    return {
      isShow: false,
      btnList: [
        {
          label: "本月",
          value: dayjs().date() - 1,
        },
        {
          label: "近三月",
          value: 90,
        },
        {
          label: "近半年",
          value: 183,
        },
        {
          label: "近一年",
          value: 365,
        },
      ],
      quickPickIndex: 0,
      result: [],
    }
  },
  mounted() {
    // 设置默认值 - 本月
    this.setDate(this.btnList[0].value)
  },
  methods: {
    show() {
      this.isShow = true
    },
    onconfirm([start, end]) {
      const result = [dayjs(start).format("YYYY-MM-DD"), dayjs(end).format("YYYY-MM-DD")]
      this.$emit("success", result)
      this.isShow = false
    },
    setDate(value) {
      this.quickPickIndex = value
      const beginDate = dayjs().subtract(value, "day")
      const endDate = dayjs()
      this.result = [beginDate.format("YYYY-MM-DD"), endDate.format("YYYY-MM-DD")]
      this.$refs.calendar.reset([beginDate.toDate(), endDate.toDate()])
      console.log("this.result ==", this.result)
    },
  },
}
</script>
<style lang="less" scoped>
/deep/ .van-checkbox__icon {
  height: auto;
}

/deep/ .van-calendar__bottom-info {
  bottom: 0px;
  transform: scale(0.9);
}
</style>
