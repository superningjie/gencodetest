/** @format */
import Vue from "vue"
import Loading from "./loading"
// import Toast from "./toast/Index.vue"
// const comments = [Loading, Toast]
// comments.forEach((item) => {
//   Vue.use(item)
// })
export default {
  showLoad(options) {
    Loading.showLoad(options)
  },
  hideLoad() {
    Loading.hideLoad()
  },
}
