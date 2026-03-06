/** @format */
import Vue from "vue"
import Loading from "./Index.vue"
// Vue.component("loading", Loading)
let instance = null

/**
 * des: 封装一个可以直接类似 this.$xy.showLoad() 调用的全局弹框插件
 * @param duration {Number} 弹框显示时间
 * @param msg {String} 弹框内容
 * @param callBack {Function} 回调函数
 */
function createLoad(options) {
  // 为了简便，这里我们不再对有关参数进行校验
  const { type, msg, duration, callBack } = options
  // 创建弹框构造器
  let LoadProfile = Vue.extend(Loading)
  // 创建弹框实例
  return new LoadProfile().$mount()
}
export default {
  showLoad(options = {}) {
    // 仅存在一个loading
    console.log("instance==", instance)
    if (instance == null) {
      // 将弹框挂载到body
      instance = createLoad(options)
      document.body.appendChild(instance.$el)
      // 禁止背景滚动
      document.body.classList.add("van-toast--unclickable")
    }
    // else{
    //   instance.msg = options.msg
    // }
  },
  hideLoad() {
    // loading存在就删除
    if (instance) {
      // 解除背景滚动
      document.body.classList.remove("van-toast--unclickable")
      // 移除节点
      document.body.removeChild(instance.$el)
      // 销毁示例
      instance.$destroy()
      // 方便垃圾机制回收
      instance = null
    }
  },
}
