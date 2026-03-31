// 在main.js上通过import引入Vue库和自己写的Vue组件库
import Vue from "vue";
import eventBus from "../../../../../../util/eventBus";
import element from "./element/index.js";
import "element-ui/lib/theme-chalk/index.css";
import XyOrgChart from "./components/xyorgchart.vue";

/**
 * Vue实例在setHtml方法中声明，初始化执行init的时候就能够创建
 * 声明Vue实例对象时，在其挂载完毕的生命周期里声明一个订阅，用于订阅update方法中发布的消息，从而更新实例数据
 * update方法中，发布一个消息，让Vue实例接收消息，更新数据
 * 在Vue实例的destroyed中，结束订阅
 * 注意loadFile中index.css的引入路径，因为webpack打包后将其放在了css文件夹里，所以路径是./css/index.css
 */
(function(KDApi) {
  function MyComponent(model) {
    this._setModel(model);
  }

  MyComponent.prototype = {
    _setModel: function(model) {
      this.model = model;
    },
    init: function(props) {
      console.log("-----init", props.data);
      setHtml(this.model, props);
      // getTeamData(this.model, props);
    },
    update: function(props) {
      console.log("-----update", props.data);
      eventBus.pub(this.model, "initChart", props);
    },
    destoryed: function() {
      console.log("-----destoryed", this.model);
    },
  };

  const setHtml = (model, props) => {
    const { invoke, dom } = model;
    KDApi.loadFile("./css/index.css", model, () => {
      Vue.use(element);
      new Vue({
        el: model.dom,
        template: "<XyOrgChart props/>",
        components: {
          XyOrgChart,
        },
        data: {
          model,
          KDApi,
        },
        destoryed() {
          console.log("Vue destory");
        },
        provide: function() {
          return {
            model,
            KDApi,
            myInvoke: this.invoke,
            props,
          };
        },
        methods: {
          invoke: function(eventName, args) {
            invoke(eventName, args);
          },
        },
      });
    });
  };

  // 注册自定义组件
  KDApi.register("cxsk_userprofileteams", MyComponent, {
    isMulLang: true,
  });
})(window.KDApi);
