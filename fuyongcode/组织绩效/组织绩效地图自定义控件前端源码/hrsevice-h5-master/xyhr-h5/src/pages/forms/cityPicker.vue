<!-- @format -->

<template>
  <div class="askForEvection cityPicker rlform">
    <van-nav-bar title="选择城市" left-arrow class="navStyle" style="z-index: 2055" @click-left="goback">
      <template #right>
        <van-button @click="confirmUsers" type="primary" style="height: 30px; background-color: #cf3633; border-color: #cf3633; border-radius: 4px">确定</van-button>
      </template>
    </van-nav-bar>

    <van-search v-model="SearchVal" class="searchItem" shape="round" placeholder="搜索城市" style="padding-bottom: 0" @input="searchGo" />
    <div class="subnav flex">
      <div
        :class="activeTab == 'china' ? 'active' : ''"
        @click="
          activeTab = 'china'
          searchGo(SearchVal)
        "
      >
        国内城市
      </div>
      <div
        :class="activeTab == 'aboard' ? 'active' : ''"
        @click="
          activeTab = 'aboard'
          searchGo(SearchVal)
        "
      >
        国际城市/港澳台
      </div>
    </div>

    <div class="group" v-show="activeTab == 'china' && !isSearch" style="padding-right: 40px">
      <h5>当前城市</h5>
      <div class="items">
        <div class="item">
          <input type="checkbox" value="厦门" @change="chk" name="city" /><a href="javascript:void(0);"><i class="iconfont" style="margin-right: 4px">&#xe67b;</i> 厦门</a>
        </div>
      </div>
      <h5>历史城市</h5>
      <div class="items">
        <div class="item" v-for="item in history" :key="item.id">
          <input type="checkbox" @change="chk" :value="item" name="city" /><a href="javascript:void(0);">{{ item }}</a>
        </div>
        <!-- <div class="item"><input type="checkbox" value='上海' name="city"/><a href="javascript:void(0);">上海</a></div>
				<div class="item"><input type="checkbox" value='广州' name="city"/><a href="javascript:void(0);">广州</a></div>
				<div class="item"><input type="checkbox" value='福州' name="city"/><a href="javascript:void(0);">福州</a></div>
				<div class="item"><input type="checkbox" value='浙江' name="city"/><a href="javascript:void(0);">浙江</a></div> -->
      </div>
      <h5>热门城市</h5>
      <div class="items">
        <div class="item"><input type="checkbox" @change="chk" value="北京" name="city" /><a href="javascript:void(0);">北京</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="上海" name="city" /><a href="javascript:void(0);">上海</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="厦门" name="city" /><a href="javascript:void(0);">厦门</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="哈尔滨" name="city" /><a href="javascript:void(0);">哈尔滨</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="福州" name="city" /><a href="javascript:void(0);">福州</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="天津" name="city" /><a href="javascript:void(0);">天津</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="深圳" name="city" /><a href="javascript:void(0);">深圳</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="绥化" name="city" /><a href="javascript:void(0);">绥化</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="广州" name="city" /><a href="javascript:void(0);">广州</a></div>
      </div>
    </div>
    <div class="group" v-show="activeTab == 'aboard' && !isSearch">
      <h5>热门城市</h5>
      <div class="items">
        <div class="item"><input type="checkbox" @change="chk" value="香港" name="city" /><a href="javascript:void(0);">香港</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="澳门" name="city" /><a href="javascript:void(0);">澳门</a></div>
        <div class="item"><input type="checkbox" @change="chk" value="台湾" name="city" /><a href="javascript:void(0);">台湾</a></div>
      </div>
    </div>

    <van-index-bar v-show="activeTab == 'china' && !isSearch" :sticky-offset-top="46" :index-list="indexList">
      <div class="cityItem" v-for="item in area" :key="item.id" style="padding-top: 10px">
        <van-index-anchor :index="item.initial" />
        <van-cell v-for="(child, i) in item.list" :key="child.code" style="margin-right: 50px">
          <template #title>
            <input type="checkBox" name="city" @change="chk" :value="child.name" style="width: 400px; position: absolute; top: 0; right: 0; height: 40px" />
            <span class="custom-title">{{ child.name }}</span>
            <span style="background-color: transparent; display: inline-block; vertical-align: text-top; width: 30px; position: relative; height: 19px; text-align: center">
              <van-icon name="success" />
            </span>
          </template>
        </van-cell>
      </div>
    </van-index-bar>

    <van-index-bar v-show="activeTab == 'aboard' && !isSearch" :sticky-offset-top="46">
      <div class="cityItem" v-for="item in guoji" :key="item.initial" style="padding-top: 10px">
        <van-index-anchor :index="item.initial" />
        <van-cell v-for="(child, i) in item.list" :key="child.name" style="margin-right: 50px">
          <template #title>
            <input type="checkBox" name="city" @change="chk" :value="child.name" style="width: 400px; position: absolute; top: 0; right: 0; height: 40px" />
            <span class="custom-title">{{ child.name }}</span>
            <span style="background-color: transparent; display: inline-block; vertical-align: text-top; width: 30px; position: relative; height: 19px; text-align: center">
              <van-icon name="success" />
            </span>
          </template>
        </van-cell>
      </div>
    </van-index-bar>

    <div v-show="isSearch" class="group" style="border: none">
      <h5>搜索结果:</h5>
      <div class="items">
		<xy-empty v-if="searchArea.length == 0"></xy-empty>
        <div class="item" v-for="(child, i) in searchArea">
          <input type="checkbox" @change="chk" :value="child.name" name="city" /><a href="javascript:void(0);">{{ child.name }}</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { searchGeneralInfo } from "@/libs/api.js"
import { FOREIGN_CITY_LIST, CHINA_CITY_LIST } from "@/libs/city.js"
export default {
  name: "cityPicker",
  data() {
    return {
      activeTab: "china",
      chooseCity: null,
      SearchVal: "",
      isSearch: false,
      history: [],
      guoji: FOREIGN_CITY_LIST,
      area: CHINA_CITY_LIST,
      indexList: [],
      guojiList: [],
      searchArea: [],
      chooseArr: [],
      arr: ["香港", "澳门", "台湾"],
    }
  },
  props: {
    postArr: {
      type: String,
    },
  },
  components: {},
  created() {
    if (this.postArr.indexOf("必选") >= 0) {
      this.chooseArr = []
    } else {
      this.chooseArr = this.postArr.split(",")
    }
    this.init()
  },
  watch: {
    postArr(val) {
      if (val.indexOf("必选") >= 0) {
        this.chooseArr = []
      } else {
        this.chooseArr = val.split(",")
      }
      this.pik()
    },
  },
  methods: {
    goback() {
      this.$emit("closeList", true)
    },
    init() {
      this.area.map((a) => {
        this.indexList.push(a.initial)
      })
      searchGeneralInfo({ dataType: "awayAddress" }).then((res) => {
        let arr = []
        res.data.data.map((item) => {
          arr.push(item.data)
        })
        this.history = arr
        var self = this
        setTimeout(function () {
          self.pik()
        }, 10)
      })
    },
    chk(e) {
      var self = this
      if (e.target.checked) {
        this.chooseArr.push(e.target.value)
        setTimeout(function () {
          self.pik()
        }, 100)
      } else {
        let arr = []
        this.chooseArr.map((item) => {
          if (item !== e.target.value) {
            arr.push(item)
          }
        })
        this.chooseArr = arr
        setTimeout(function () {
          self.pik()
        }, 100)
      }
    },
    pik() {
      let users = document.getElementsByName("city")
      for (let i = 0; i < users.length; i++) {
        if (this.chooseArr.length > 0) {
          for (let n = 0; n < this.chooseArr.length; n++) {
            if (this.chooseArr[n].indexOf(users[i].value) == 0 && this.chooseArr[n].length === users[i].value.length) {
              users[i].checked = true
              break
            } else {
              users[i].checked = false
            }
          }
        } else {
          users[i].checked = false
        }
      }
    },
    confirmUsers() {
      if (this.chooseArr.length == 0) {
        this.chooseArr.push("必选")
      }
      if (this.chooseArr.length > 1) {
        this.chooseArr.forEach((item, index) => {
          if (item == "必选") {
            this.chooseArr.splice(index, 1)
          }
        })
      }
      this.$emit("pickLocal", this.chooseArr)
      this.$emit("closeList", true)
    },
    searchGo(value) {
      var self = this
      if (value == "") {
        this.isSearch = false
      } else {
        this.isSearch = true
        this.searchArea = []
        if (this.activeTab == "china") {
          this.area.map((item) => {
            item.list.map((child) => {
              if (child.name.indexOf(value) != -1) {
                this.searchArea.push(child)
              }
            })
          })
        } else {
          this.guoji.map((item) => {
            item.list.map((child) => {
              if (child.name.indexOf(value) != -1) {
                this.searchArea.push(child)
              }
            })
          })
          console.log(this.searchArea)
        }
        setTimeout(function () {
          self.pik()
        }, 100)
      }
    },
    // getCity (event) {
    // 	this.chooseCity = event;
    // 	this.$emit('pickLocal', this.chooseCity);
    // 	// this.$emit('closeList', true);
    // },
  },
}
</script>
