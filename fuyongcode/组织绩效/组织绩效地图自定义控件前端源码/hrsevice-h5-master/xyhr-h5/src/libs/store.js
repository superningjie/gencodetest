/** @format */

import Vue from "vue"
import Vuex from "vuex"
import { getTokenFromServe, getPsnInfo, seorefQuery } from "@/libs/api.js"
Vue.use(Vuex)
export default new Vuex.Store({
  state: {
    // 状态
    commonTree: {},
    userData: {},
    userInfo: {}, // 用户信息，包含主键
    preTypecolumns: [], // 人员类型
    jobrankcolumns: [], // 岗位层级
    gglxcolumns: [], // 高管岗位表
    jobList: [], // 个人岗位表
    pageInfo: {}, // 弃用
    pkInfoSet: "",
    isNeedNewData: "",
    trainTab: 1,
    trainteacherTab: 0,
    searchType: {},
    keepAlive: [],
    voiceType: "",
    isSSO: false,
    code: "",
    skey: "",
    refreshStatus: false,
    headCount: 0,
    routeId: Date.now(),
    userTargetInfo: {},
    progressTrackingIds: []
  },
  mutations: {
    // 改变方法
    setProgressTrackingIds(state, item) {
      state.progressTrackingIds = item
    },
    setUserTargetInfo(state, item) {
      state.userTargetInfo = item
    },
    increment(state, item) {
      state.commonTree = item
    },
    setUserData(state, item) {
      state.userData = item
      if (item) {
        localStorage.setItem('pk_psndoc', item.pk_psndoc)
        localStorage.setItem('pkPsndoc', item.annualLeaveDetailVo.pkPsndoc || '')
      }
    },
    setUserInfo(state, item) {
      state.userInfo = item
    },
    setpreTypecolumns(state, item) {
      state.preTypecolumns = item
    },
    setjobrankcolumns(state, item) {
      state.jobrankcolumns = item
    },
    setGglxcolumns(state, item) {
      state.gglxcolumns = item
    },
    setPageInfo(state, item) {
      state.pageInfo = item
    },
    setPkInfoSet(state, item) {
      state.pkInfoSet = item
    },
    setisNeedNewData(state, item) {
      state.isNeedNewData = item
    },
    setTrainTab(state, item) {
      state.trainTab = item
    },
    settrainteacherTab(state, item) {
      state.trainteacherTab = item
    },
    setsearchType(state, item) {
      state.searchType = item
    },
    setKeepAlive: (state, keepAlive) => {
      keepAlive = [...new Set(keepAlive)]
      if (keepAlive.indexOf("mySelfhelp") == -1) {
        keepAlive.unshift("mySelfhelp")
      }
      state.keepAlive = keepAlive
    },
    setvoiceType(state, item) {
      state.voiceType = item
    },
    setisSSO(state, item) {
      state.isSSO = true
    },
    setCode(state, item) {
      state.code = item
    },
    setSkey(state, item) {
      state.skey = item
    },
    setrefreshStatus(state, item) {
      state.refreshStatus = item
    },
    setheadCount(state, item) {
      state.headCount = item
    },
    setJobList(state, data) {
      state.jobList = data
    },
    // 添加保存的路由组件name
    addKeepAlive(state, name) {
      state.keepAlive.push(name)
    },
    // 删除保存的路由组件name
    removeKeepAlive(state, name) {
      const index = state.keepAlive.findIndex((item) => {
        return item == name
      })
      if (index > -1) {
        state.keepAlive.splice(index, 1)
      }
    },
  },
  actions: {
    GET_TOKEN({ commit }, params) {
      return new Promise((resolve) => {
        getTokenFromServe(params).then((res) => {
          if (res.data.statusCode == 200) {
            resolve(true)
          }
        })
      })
    },
    GET_USER_INFO({ commit }) {
      return new Promise((resolve) => {
        getPsnInfo().then((res) => {
          if (res.data.statusCode == 200) {
            commit("setUserData", res.data.data[0])
            resolve(true)
          }
        })
      })
    },
    QUERY_ENUME({ commit }, data) {
      return new Promise((resolve) => {
        seorefQuery({
          seotype: data.seotype,
        }).then((res) => {
          if (res.data.statusCode == 200) {
            commit(data.key, res.data.data)
            resolve(true)
          }
        })
      })
    },
  },
  getters: {
    keepAlive: (state) => state.keepAlive,
  },
  modules: {},
})
