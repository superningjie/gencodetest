/** @format */

// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from "vue"
import App from "./App"
import router from "./router"
import Vant from "vant"
import "vant/lib/index.css"
import "@/libs/main.less"
import '@/libs/styles.css'
import { Lazyload } from "vant"
// 引入echarts组件
import echarts from "echarts"
import md5 from "js-md5"
import vueSwiper from "vue-awesome-swiper"
// 导入组件库
import Calendar from "./components"
import store from "@/libs/store.js"

// 引入自定义全局弹窗（ loading、message）等
import XyMessage from "@/components/xyMessage/index.js"
import XyEmpty from '@/components/xyEmpty/Index.vue'
import XyDivider from '@/components/xyDivider/'

// 注册组件库
Vue.use(Calendar)
// import 'swiper/dist/css/swiper.css'

// 自定义组件全局注册
Vue.component('XyEmpty', XyEmpty)
Vue.component('XyDivider', XyDivider)


Vue.use(vueSwiper)
Vue.config.productionTip = false
Vue.use(Vant)

Vue.use(Lazyload, {
  lazyComponent: true, //可实现组件懒加载
})

const getAssetsFile = (url) => {
  return new URL(`./assets${url}`, import.meta.url).href
}

let Base64 = import("js-base64").Base64
Vue.prototype.$base64 = Base64
Vue.prototype.$echarts = echarts
Vue.prototype.$md5 = md5
Vue.prototype.getAssetsFile = getAssetsFile
Vue.prototype.$xy = XyMessage

// 是否微信，为后续接入小程序准备
const userAgent = navigator.userAgent
const isWx = /micromessenger/i.test(userAgent)
Vue.prototype.$isWx = isWx

export default new Vue({
  render: (h) => h(App),
  store,
  router,
  components: { App },
}).$mount("#app");

// 运营监控脚本
(function () {
  // // 将cookie字符串分割成数组
  // var cookies = document.cookie.split(';');
  // for (var i = 0; i < cookies.length; i++) {
  //   var cookie = cookies[i].trim();
  //   // 判断是否为目标cookie
  //   if (cookie.startsWith('sid' + '=')) {
  //     // 返回cookie的值
  //     console.log("获取的sid为："+cookie.substring('sid'.length + 1)) ;
  //   }
  // }
  let t = document.createElement("script");
  let date = new Date().getTime();
  const env = import.meta.env.MODE
  if (env == "prod") {
    t.src = "https://elink.xiangyu.cn:9443/cloudstore/release/4b8399eb0d384933ad22ce2f71b9d1f2/resources/interfaceHrdp.js?date=" + date;
  } else if (env == "test" || env == "uat") {
    t.src = "https://elinkuat.xiangyu.cn:9443/cloudstore/release/4b8399eb0d384933ad22ce2f71b9d1f2/resources/interfaceHrdp.js?date=" + date;
  }
  t.id = "sensorsdata";
  let s = document.getElementsByTagName("script")[0];
  s.parentNode.insertBefore(t, s);
})();


