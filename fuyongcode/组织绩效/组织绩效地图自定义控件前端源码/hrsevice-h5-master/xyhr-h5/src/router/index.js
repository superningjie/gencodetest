/** @format */

import Vue from "vue"
import Router from "vue-router"
import routes from "@/router/routePath"
import store from "../libs/store"
import EnterPage from "@/components/xyMessage/entry"
import jsCookie from "js-cookie"
Vue.use(Router)
// 绩效模块的路由数组
const performancesArrayRoute = ['myachievementQuery', 'myBacklog', 'myPerformance', 'teamPerformance', 'approvalSingle', 'performancesRate', 'performancesBatchRate', 'performancesIndexDetails', 'performancesRateTotal', 'performancesRateTotalAdjust', 'performancesBatchRateTotal', 'performancesBatchAdjustTotal', 'approvalList', 'otherEvaluateDetails', 'adjustEvaluateDetails', 'rankingList', 'performanceDetail', 'myPerformanceDetail', 'performancesAppraiseDetails', 'overallEvaluationDetails']
const router = new Router({
  routes: routes,
})
router.beforeEach(async (to, from, next) => {
  // 修改标题文字
  em.changeTitle({
    title: to.meta.title,
  })
  console.log(' window.location', window.location);
  const isSkipPage = window.location.hash.includes('skipPage')
  console.log('isSkipPage', isSkipPage);
  let isShow = true
  if (isSkipPage) {
    // 判断是否显示加载动画
    const parts = window.location.hash.split('?')[1]?.split('&')
    // console.log('parts', parts);
    const path = parts[0]?.split('=')[1].toLowerCase()
    // console.log('path', path);
    const lowercaseArray = performancesArrayRoute.map(item => item.toLowerCase());
    // console.log('lowercaseArray', lowercaseArray);
    isShow = !lowercaseArray.includes(path)
  }

  console.log('isShow', isShow);
  // 路由跳转校验
  if (store.state.preTypecolumns.length == 0) {
    // 首次进入保存sid且进入的name = Home
    if (to.name == "Home") {
      jsCookie.set("sid", to.query.sid)
      localStorage.setItem('sid', to.query.sid)
    }
    const secondStart = Date.now()
    isShow ? EnterPage.showEntry() : ''
    // EnterPage.showEntry()
    // 随便调用一个接口，主要目的在于金蝶注入token
    const search = window.location.href.split("?")
    const params = "?" + (search[1] || "")
    await store.dispatch("GET_TOKEN", params)
    // 注入token后，获取全局使用的枚举值和用户信息
    const httpArr = [
      store.dispatch("GET_USER_INFO"), //用户信息
      store.dispatch("QUERY_ENUME", { seotype: "psncl", key: "setpreTypecolumns" }), // 人员类型
      store.dispatch("QUERY_ENUME", { seotype: "jobrank", key: "setjobrankcolumns" }), // 岗位层级
      store.dispatch("QUERY_ENUME", { seotype: "gglx", key: "setGglxcolumns" }), // 高管岗位表
    ]
    await Promise.all(httpArr)
    const secondEnd = Date.now()
    const consume = secondEnd - secondStart
    console.log("响应时间====", consume)
    const skipPage = to.query.skipPage
    if (consume < 2000) {
      setTimeout(() => {
        isShow ? EnterPage.hideEntry() : ''
        if (skipPage) {
          next({ name: skipPage, query: to.query, replace: true })
        } else {
          next()
        }
      }, 2000 - consume)
    } else {
      isShow ? EnterPage.hideEntry() : ''
      if (skipPage) {
        next({ name: skipPage, query: to.query, replace: true })
      } else {
        next()
      }
    }
  } else {
    next()
  }
})
export default router
