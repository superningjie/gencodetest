<!-- @format -->

<template>
  <div>
    <div class="flex justify hl filter" :style="styleParam">
      <!-- 所属组织 -->
      <a class="flex middle">
        <a @click="showFilterPicker = true" class="rangeText">{{ titleText.substr(0, 4) }} <van-icon name="arrow-down" /></a>
      </a>
      <!-- 岗位 -->
      <a class="flex middle">
        <a @click="postStation()" class="rangeText">{{ titleText2 }} <van-icon name="arrow-down" /></a>
      </a>
      <!-- 日期 -->
      <a class="flex middle">
        <a @click=";(showImgPreview = true), (isdatehide = true)" class="rangeText">{{ data.enddate }}<van-icon name="arrow-down" /></a>
      </a>
      <!-- 更多 -->
      <a class="flex middle">
        <a @click="$refs.TongjiselectMore.show()" class="rangeText">更多 <van-icon name="more-o" /></a>
      </a>
    </div>

    <van-popup v-model="showImgPreview" class="calenderPop" get-container="body" :overlay-style="{ background: 'rgba(120,120,120,.5)' }">
      <Calendar :default-date="new Date(currentDate)" :is-show-week-view="false" :mark-date="markDate" v-if="showImgPreview" @confirm="dateConfirm" />
    </van-popup>

    <van-popup v-model="showPicker" class="checkboxPopup" :overlay-style="{ background: 'rgba(120,120,120,.5)' }" :style="{ width: '60%' }" get-container="body">
      <van-checkbox-group v-model="jobresult" checked-color="#ee0a24">
        <template v-for="item in jobrankcolumns">
          <van-checkbox :name="item.value" :key="item.value">{{ item.title }}</van-checkbox>
        </template>
      </van-checkbox-group>
      <div class="operationControl">
        <van-button color="#D0332F" round type="info" @click="checkboxSearch()">确定</van-button>
      </div>
    </van-popup>
    <van-popup v-model="showpreType" class="checkboxPopup" :overlay-style="{ background: 'rgba(120,120,120,.5)' }" :style="{ width: '60%' }" get-container="body">
      <van-checkbox-group v-model="preresult" checked-color="#ee0a24">
        <template v-for="item in preTypecolumns">
          <van-checkbox :name="item.value" :key="item.value">{{ item.title }}</van-checkbox>
        </template>
      </van-checkbox-group>
      <div class="operationControl">
        <van-button color="#D0332F" round type="info" @click="preTypeSearch()">确定</van-button>
      </div>
    </van-popup>
    <!-- 更多选项 -->
    <TongjiselectMore ref="TongjiselectMore" @sure="sureMore" />
    <!-- 所选组织 -->
    <mycommonTree :showFilterPicker="showFilterPicker" :isBusiness="isBusiness" :businessType="businessType" title="选择组织" @isshowFilterPicker="isshowFilterPicker" @clickSearch="clickSearch" :scope="1" />
  </div>
</template>

<script>
import { dateFormat } from "@/libs/api.js"
import mycommonTree from "@/components/mycommonTree"
export default {
  name: "tongjiselect",
  props: {
    isMore: {
      type: Boolean,
      default: false,
    },
    styleParam: {
      type: String,
      default: "padding:20px 20px 10px",
    },
    isBusiness: {
      type: Boolean,
      default: false,
    },
    businessType: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      markDate: [],
      showPicker: false,
      quickPickIndex: 0,
      showFilterPicker: false,
      preType: {},
      jobrank: {},
      titleText: "所属组织",
      titleText1: "人员类型",
      titleText2: "岗位层级",
      showpreType: false,
      showImgPreview: false,
      showjobrank: false,
      minDate: new Date(new Date().getTime() - 86400 * 1e3 * 7300),
      maxDate: new Date(),
      currentDate: dateFormat("YYYY-mm-dd", new Date()),
      data: {
        enddate: "",
        psncllist: [],
        postgradelist: [],
        pkOrg: "",
      },
      preresult: [],
      jobresult: [],
      middleColumns: [],
      preTypecolumns: [],
      jobrankcolumns: [],
      isdatehide: true,
    }
  },
  components: {
    mycommonTree,
    calender: () => import("@/components/calenderPicker"),
    TongjiselectMore: () => import("./tongjiselect_more.vue"),
  },
  created() {
    this.preTypecolumns = this.$store.state.preTypecolumns
    this.jobrankcolumns = this.$store.state.jobrankcolumns
    this.$store.state.preTypecolumns.map((item, index) => {
      if (item.isDefault) {
        this.preresult.push(item.value)
      }
    })
    this.data.enddate = dateFormat("YYYY-mm-dd", new Date())
  },
  methods: {
    dateConfirm(date) {
      if (this.isdatehide) {
        this.data.enddate = date.year + "-" + (date.month < 9 ? "0" + (date.month + 1) : date.month + 1) + "-" + (date.day < 10 ? "0" + date.day : date.day)
        this.currentDate = this.data.enddate
        this.showImgPreview = false
        this.onsearch()
      } else {
        this.isdatehide = true
      }
    },
    isshowFilterPicker(value) {
      this.showFilterPicker = value
    },
    clickSearch(item) {
      this.data.pkOrg = item.id
      this.titleText = item.name
      if (item.id == "") {
        this.titleText = "所属组织"
      }
      this.showFilterPicker = false
      this.onsearch()
    },
    // preTypeonConfirm(value){
    // 	this.showpreType=false
    // 	this.preType=value
    //     this.data.psncllist=[value.value]
    //     this.onsearch()
    // },
    // jobrankonConfirm(value){
    // 	this.showjobrank=false
    // 	this.jobrank=value
    //     this.data.postgradelist=[value.value]
    //     this.onsearch()
    // },
    postStation() {
      this.showPicker = true
      this.middleColumns = JSON.parse(JSON.stringify(this.jobresult))
      console.log(this.middleColumns)
    },
    checkboxSearch() {
      this.onsearch()
      this.showPicker = false
    },
    checkboxSearchcencel() {
      this.showPicker = false
      this.jobresult = this.middleColumns
    },
    postpreType() {
      this.showpreType = true
      this.middleColumns = JSON.parse(JSON.stringify(this.preresult))
    },
    preTypeSearch() {
      this.onsearch()
      this.showpreType = false
    },
    preSearchcencel() {
      this.showpreType = false
      this.preresult = this.middleColumns
    },
    preTypeonCancel() {
      this.showpreType = false
    },
    jobrankonCancel() {
      this.showjobrank = false
    },
    onconfirm(val) {
      this.data.enddate = val
      // this.showImgPreview=false
      this.onsearch()
    },
    oncancel() {
      // this.showImgPreview=false
    },
    sureMore(data) {
      this.data.psncllist = data.psncllist
      this.preresult = data.psncllist
      this.data.highLevelType = data.highLevelType
      this.data.postgradelist = this.jobresult
      this.$emit("onsearch", this.data)
    },
    onsearch() {
      this.data.psncllist = this.preresult
      this.data.postgradelist = this.jobresult
      this.$emit("onsearch", this.data)
    },
  },
}
</script>
<style scoped>
.rangeText {
  display: flex;
  align-items: center;
}
.van-icon {
  margin-left: 2px;
}
</style>
