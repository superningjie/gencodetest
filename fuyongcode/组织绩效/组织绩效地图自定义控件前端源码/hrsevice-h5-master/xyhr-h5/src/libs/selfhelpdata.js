/** @format */

import store from "@/libs/store.js"
// app权限图标
import geren from "@/assets/home/geren.png"
import xinchou from "@/assets/home/xinchou.png"
import qingjia from "@/assets/home/qingjia.png"
import chuchai from "@/assets/home/chuchai.png"
import kaoqin from "@/assets/home/kaoqin.png"
import zhengming from "@/assets/home/zhengming.png"
import jiaban from "@/assets/home/jiaban.png"
import qianka from "@/assets/home/qianka.png"
import wenxun from "@/assets/home/wenxun.png"
import wodemianshi from "@/assets/home/wodemianshi.png"
import yuangonglizhi from "@/assets/home/yuangonglizhi.png"
import jianli from "@/assets/home/jianli.png"
import fenxi from "@/assets/home/fenxi.png"
import jixiaochaxun from "@/assets/home/jixiaochaxun.png"
import peixun from "@/assets/home/peixun.png"
import xinchouchaxun from "@/assets/home/xinchouchaxun.png"
import kaoqinchaxun from "@/assets/home/kaoqinchaxun.png"
import zhaopinkanban from "@/assets/home/zhaopinkanban.png"
import myteam from "@/assets/home/myteam.png"
import teamkaoqin from "@/assets/home/teamkaoqin.png"
import teamtrain from "@/assets/home/teamtrain.png"
import teamperformanc from "@/assets/home/teamperformanc.png"
import zhaopinxuqiu from "@/assets/home/zhaopinxuqiu.png"
import kanban from "@/assets/performances/kanban.png"
import gerenfazhanjihua from "@/assets/home/gerenfazhanjihua.png"
import wodehuaxiang from "@/assets/home/wodehuaxiang.png"
import rencaikanban from "@/assets/home/rencaikanban.png"


// 个人信息图标
// import danganshenpi from "@/assets/person/danganshenpi.png"
// import danganxiugai from "@/assets/person/danganxiugai.png"
import jiangcheng from "@/assets/person/jiangcheng.png"
import jiaoyujingli from "@/assets/person/jiaoyujingli.png"
import jiatingchengyuan from "@/assets/person/jiatingchengyuan.png"
import jibenxinxi from "@/assets/person/jibenxinxi.png"
import jinjiren from "@/assets/person/jinjiren.png"
import lianxifangshi from "@/assets/person/lianxifangshi.png"
import qiangongzuo from "@/assets/person/qiangongzuo.png"
import qitarenzhi from "@/assets/person/qitarenzhi.png"
import renyuandizhi from "@/assets/person/renyuandizhi.png"
import renzhijingli from "@/assets/person/renzhijingli.png"
import techangaihao from "@/assets/person/techangaihao.png"
import yuyannengli from "@/assets/person/yuyannengli.png"
import zhengjianxinxi from "@/assets/person/zhengjianxinxi.png"
import zhichengxinxi from "@/assets/person/zhichengxinxi.png"
import zhiyejineng from "@/assets/person/zhiyejineng.png"
import zhiyezige from "@/assets/person/zhiyezige.png"
import zhuzhiguanxi from "@/assets/person/zhuzhiguanxi.png"
import yinhangka from "@/assets/person/yinhangka.png"
import shehuituanti from "@/assets/person/shehuituanti.png"


export const appdata = {
  staffApplications: [
    {
      id: "hspm_myermanfile",
      label: "个人信息",
      icon: geren,
      url: "/mobile.html?form=hspm_moberhome&app=hssc", // 跳转苍穹，优先级比path高，无url时才跳转path
      title: '我的档案'
    },
    {
      id: "xyjt_salaryqx",
      label: "薪酬",
      icon: xinchou,
      path: "salaryEntry",
      url: '/mobile.html?form=hspp_mobpwdinit&app=hssc&isperm=0',
      title: '薪酬'
    },
    {
      id: "wtabm_vaapplyself",
      label: "请假",
      icon: qingjia,
      url: "/mobile.html?form=wtabm_vaapplymob_self&app=hssc",
      title: '请假申请'
    },
    {
      id: "xyjt_reqtripe",
      label: "出差",
      icon: chuchai,
      url: "/mobile.html?form=xyjt_reqtripe_mob&app=xyjt_trip",
      title: '出差申请'
    },
    {
      id: "wtss_mobilehomepage",
      label: "考勤",
      icon: kaoqin,
      url: "/mobile.html?form=wtss_mobilehomepage&app=hssc",
      title: '我的假勤（APP）'
    },
    {
      id: "xyjt_prove_handle",
      label: "证明",
      icon: zhengming,
      url: "/mobile.html?form=xyjt_prove_handle_mob&app=xyjt_employee_service",
      title: '新增证明办理申请'
    },
    {
      id: "wtom_otbillself",
      label: "加班",
      icon: jiaban,
      url: "/mobile.html?form=wtom_otbillselef_m&app=hssc",
      title: '加班申请'
    },
    {
      id: "wtpm_supsignself",
      label: "补签",
      icon: qianka,
      url: "/mobile.html?form=wtpm_supsignself_m&app=hssc",
      title: '我的补签'
    },
    {
      id: "xyjt_employee_inquiries",
      label: "问询",
      icon: wenxun,
      url: "/mobile.html?form=xyjt_employee_inquiries_mob&app=xyjt_employee_service",
      title: '员工问询'
    },
    {
      id: "htm_quitapplyemp",
      label: "离职",
      icon: yuangonglizhi,
      url: "/mobile.html?form=htm_quitapplyemp_mob&app=htm",
      title: '新增离职申请'
    },
    {
      id: "xyjt_myinterview_mobile",
      label: "我的面试",
      icon: wodemianshi,
      external: "VITE_URL_INTERVIEW",
      tokenName: 'token',
      title: '我的面试'
    },
    {
      id: "hspp_bankcardperview",
      label: "银行卡",
      icon: yinhangka,
      url: "/mobile.html?form=hspp_bankcardperview&app=hssc",
      title: '银行卡'
    },
    // 添加个人发展计划入口配置
    {
      id: "development_plan",
      label: "个人发展计划",
      icon: gerenfazhanjihua,
      path: "plans",
      title: '个人发展计划'
    },
    // 添加个人画像入口配置
    {
      id: "personal_portrait",
      label: "我的画像",
      icon: wodehuaxiang,
      path: "portrait",
      title: '我的画像'
    },
  ],
  leaderApplications: [
    {
      id: "xyjt_personcv",
      label: "员工简历",
      icon: jianli,
      path: "resumeQuery",
    },
    {
      id: "xyjt_statistic",
      label: "统计分析",
      icon: fenxi,
      path: "statistic",
    },
    {
      id: "xyjt_performancequery",
      label: "绩效查询",
      icon: jixiaochaxun,
      path: "leaderCharts",
    },
    // {
    //   id: "xyjt_cultivatenotice",
    //   label: "培训查询",
    //   icon: peixun,
    //   path: "trainQuery"
    // },
    {
      id: "xyjt_salaryquery",
      label: "薪酬查询",
      icon: xinchouchaxun,
      path: "leadersalaryEntry",
    },
    {
      id: "xyjt_queryattendance",
      label: "考勤查询",
      icon: kaoqinchaxun,
      path: "leaderClockIn",
    },
    {
      id: "xyjt_recruitbulletin",
      label: "招聘看板",
      icon: zhaopinkanban,
      external: "VITE_URL_RECRUIT",
      tokenName: 'token',
      title: '招聘看板'
    },
    {
      id: "xyjt_cultivatenotice",
      label: "培训看板",
      icon: peixun,
      external: "VITE_URL_TRAIN_H5",
      tokenName: 'auth',
      title: '培训看板'
    },
    {
      id: "xyjt_board",
      label: "绩效看板",
      icon: kanban,
      title: '绩效看板',
      path: "board",
    },
    // 添加人才看板入口配置
    {
      id: "xyjt_talentkanban",
      label: "人才看板",
      icon: rencaikanban,
      title: '人才看板',
      path: "talent-board",
    },
  ],
  managerApplications: [
    {
      id: "xyjt_app_myteam",
      label: "我的团队",
      icon: myteam,
      path: "myTeamInfo",
    },
    {
      id: "xyjt_app_teamattendance",
      label: "团队考勤",
      icon: teamkaoqin,
      path: "teamClockIn",
    },
    // {
    //   id: "xyjt_teamtrain",
    //   label: "团队培训",
    //   icon: teamtrain,
    //   path: "teamTrainInfo"
    // },
    // {
    //   id: "xyjt_teamperformance",
    //   label: "团队绩效",
    //   icon: teamperformanc,
    //   path: "teamChart",
    // },
    {
      id: "xyjt_teamperformance",
      label: "团队绩效",
      icon: teamperformanc,
      path: "teamPerformance",
    },
    {
      id: "xyjt_rec_apply_bill",
      label: "招聘需求",
      icon: zhaopinxuqiu,
      path: "teamChart",
      url: "/mobile.html?form=xyjt_rec_apply_bill_mob",
    },
  ],
}
//水印模块
export function witerWork() {
  return [
    "salaryEntry",
    "myInfo",
    "leaderInfo",
    "resumeQuery",
    "statistic",
    "leaderCharts",
    "trainQuery",
    "leadersalaryEntry",
    "leaderClockIn",
    "myTeamInfo",
    "teamClockIn",
    "teamTrainInfo",
    "managesalaryEntry",
    "teamChart",
  ]
}

export const userInfoIcon = {
  hrpi_pernontsprop: jibenxinxi,
  hrpi_empposorgrel: renzhijingli,// 任职经历
  hrpi_emporgrelall: renzhijingli,// 任职经历总表
  hrpi_preworkexp: qiangongzuo,
  hrpi_perserlen: zhuzhiguanxi,
  xyjt_hrpi_otheremployinf: qitarenzhi,
  hrpi_pereduexp: jiaoyujingli,
  hrpi_familymemb: jiatingchengyuan,
  hrpi_percre: zhengjianxinxi,
  hrpi_percontact: lianxifangshi,
  hrpi_emrgcontact: jinjiren,
  hrpi_peraddress: renyuandizhi,
  hrpi_perprotitle: zhichengxinxi,
  hrpi_perocpqual: zhiyezige,
  hrpi_languageskills: yuyannengli,
  hrpi_perrprecord: jiangcheng,
  xyjt_hrpi_skillidentify: zhiyejineng,
  hrpi_perhobby: techangaihao,
  xyjt_hrpi_social_group: shehuituanti
}

export function dendrogram() {
  return {
    title: {
      text: "",
      color: "#020202",
      fontSize: "16px",
      fontWeight: "normal",
      top: "5%",
      left: "3%",
    },
    grid: {
      top: "20px",
      left: "5%",
    },
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c} ({d}%)",
      position: "inside",
    },
    legend: {
      orient: "horizontal",
      x: "right",
      bottom: "2%",
      left: "4%",
    },
    series: [
      {
        name: "",
        color: ["#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de", "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#87E8DE", "#6C85A8", "#36CFC9", "#FFAB50", "#FF92C4"],
        type: "pie",
        radius: ["26%", "40%"],
        center: ["50%", "37%"],
        //center : ['50%','40%'],
        barWidth: 30,
        avoidLabelOverlap: true,
        startAngle: -135,
        label: {
          normal: {
            show: true,
            position: "outer",
            formatter: "{c}({d}%)\n",
            textStyle: {
              color: ["#000"],
            },
          },
          emphasis: {
            show: true,
            position: "outer",
            formatter: "{c}({d}%)",
            textStyle: {
              color: ["#000"],
            },
          },
        },
        labelLine: {
          normal: {
            length: 10,
            length2: 5,
            show: true,
          },
          emphasis: {
            length: 5,
            length2: 5,
            show: true,
          },
        },
        data: [],
      },
    ],
  }
}

export function waterMarkWrap() {
  console.log('waterMarkWrap水印已经被注释')
  // if (document.querySelector(".water-mark-wrap")) {
  // } else {
  //   let t = timeFormat(new Date())
  //   if (document.querySelector(".water-mark-wrap")) {
  //     document.querySelector(".water-mark-wrap").remove()
  //   }
  //   let waterMarkName = store.state.userData.name + "," + t
  //   if (!waterMarkName) {
  //     return
  //   }
  //   let width = window.parseInt(document.body.clientWidth)
  //   let canvasWidth = width / window.parseInt(width / 120)
  //   let fontFamily = window.getComputedStyle(document.body)["font-family"]
  //   const fragment = document.createDocumentFragment()
  //   let waterMarkDOM = document.createElement("div")
  //   waterMarkDOM.className = "water-mark-wrap"
  //   let spanStr = ""
  //   for (let i = 0; i < 100; i++) {
  //     spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(",")[0]}</small><small>${
  //       waterMarkName.split(",")[1]
  //     }</small></span>`
  //   }
  //   waterMarkDOM.innerHTML = spanStr
  //   fragment.appendChild(waterMarkDOM)
  //   document.body.appendChild(fragment)
  // }
}

export function popupMarkWrap() {
  console.log('popupMarkWrap水印已经被注释')
  // if (document.querySelector(".popup-mark-wrap")) {
  // } else {
  //   let t = timeFormat(new Date())
  //   if (document.querySelector(".popup-mark-wrap")) {
  //     document.querySelector(".popup-mark-wrap").remove()
  //   }
  //   let waterMarkName = store.state.userData.name + "," + t
  //   if (!waterMarkName) {
  //     return
  //   }
  //   let width = window.parseInt(document.body.clientWidth)
  //   let canvasWidth = width / window.parseInt(width / 120)
  //   let fontFamily = window.getComputedStyle(document.body)["font-family"]
  //   const fragment = document.createDocumentFragment()
  //   let waterMarkDOM = document.createElement("div")
  //   waterMarkDOM.className = "popup-mark-wrap"
  //   let spanStr = ""
  //   for (let i = 0; i < 100; i++) {
  //     spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(",")[0]}</small><small>${
  //       waterMarkName.split(",")[1]
  //     }</small></span>`
  //   }
  //   waterMarkDOM.innerHTML = spanStr
  //   fragment.appendChild(waterMarkDOM)
  //   document.getElementById("wind").appendChild(fragment)
  // }
}

export function trainMarkWrap(id) {
  let t = timeFormat(new Date())
  if (document.querySelector(".popup-mark-wrap")) {
    document.querySelector(".popup-mark-wrap").remove()
  }
  let waterMarkName = store.state.userData.name + "," + t
  if (!waterMarkName) {
    return
  }
  let width = window.parseInt(document.body.clientWidth)
  let canvasWidth = width / window.parseInt(width / 120)
  let fontFamily = window.getComputedStyle(document.body)["font-family"]
  const fragment = document.createDocumentFragment()
  let waterMarkDOM = document.createElement("div")
  waterMarkDOM.className = "popup-mark-wrap"
  let spanStr = ""
  for (let i = 0; i < 100; i++) {
    spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(",")[0]}</small><small>${waterMarkName.split(",")[1]
      }</small></span>`
  }
  waterMarkDOM.innerHTML = spanStr
  fragment.appendChild(waterMarkDOM)
  document.getElementById(id).appendChild(fragment)
}

function timeFormat(time) {
  // 时间格式化 2019-09-08
  let year = time.getFullYear()
  let month = ("0" + (time.getMonth() + 1)).slice(-2)
  let day = ("0" + time.getDate()).slice(-2)
  return year + "-" + month + "-" + day
}
