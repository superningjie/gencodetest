/** @format */

import Message from "./Message.vue"

/**
 * des: 封装一个可以直接类似 this.$msg.success() 调用的全局弹框插件
 * @param duration {Number} 弹框显示时间
 * @param msg {String} 弹框内容
 * @param callBack {Function} 回调函数
 */
export default function install(Vue) {
  //   注册组件
  Vue.component("message", Message)

  function createMessage(options) {
    // 为了简便，这里我们不再对有关参数进行校验
    const { type, msg, duration, callBack } = options
    // 弹框构造器
    let MsgProfile
    // 弹框实例
    let msgInstance

    function _createMsg() {
      // 创建弹框构造器
      MsgProfile = Vue.extend({
        render(h) {
          const props = {
            show: this.show,
            type,
            msg,
          }
          // 使用 render 函数，渲染 message 组件，并将 props 传入
          return h("message", { props })
        },
        data() {
          return {
            show: false,
          }
        },
      })
    }

    function _mountMsg() {
      // 创建弹框实例
      msgInstance = new MsgProfile().$mount()
      // 将弹框挂载到body
      document.body.appendChild(msgInstance.$el)
      // 显示弹框
      msgInstance.show = true
    }

    function _destroyMsg() {
      // 用于销毁弹框的定时器
      let t1 = setTimeout(function () {
        clearTimeout(t1)

        // 隐藏弹框
        msgInstance.show = false
        // 用于显示淡入效果的定时器
        let t2 = setTimeout(function () {
          clearTimeout(t2)
          // 移除节点
          document.body.removeChild(msgInstance.$el)
          // 销毁示例
          msgInstance.$destroy()
          msgInstance = null
          // 触发回调
          callBack && typeof callBack === "function" && callBack()
        }, 1000)
      }, duration)
    }

    _createMsg()
    _mountMsg()
    _destroyMsg()
  }

  Vue.prototype.$msg = {
    success(duration, msg, callBack) {
      createMessage({
        type: "success",
        duration,
        msg,
        callBack,
      })
    },
    error(duration, msg, callBack) {
      createMessage({
        type: "error",
        duration,
        msg,
        callBack,
      })
    },
  }
}
