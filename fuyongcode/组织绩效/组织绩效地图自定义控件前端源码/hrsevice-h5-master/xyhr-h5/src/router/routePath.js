/** @format */

export default [
  {
    path: "/",
    // name: "登录",
    // meta: { title: "登录" },
    // component: () => import("@/pages/login.vue"),
    redirect: { path: "/selfhelp" },
  },
  {
    path: "/selfhelp",
    name: "Selfhelp",
    component: () => import("@/pages/selfhelp/router.vue"),
    children: [
      {
        path: "",
        name: "Home",
        meta: { title: "人力自助" },
        component: () => import("@/pages/selfhelp/main.vue"),
      },
      // {path: 'app_edit', name: '应用管理', component:() => import('@/pages/selfhelp/main.vue')},
      {
        path: "message",
        name: "message",
        meta: { title: "消息" },
        component: () => import("@/pages/selfhelp/message.vue"),
      },
      {
        path: "birthday",
        name: "Birthday",
        meta: { title: "生日" },
        component: () => import("@/pages/selfhelp/birthday.vue"),
      },
      {
        path: "specialAttention",
        name: "SpecialAttention",
        meta: { title: "特别关注" },
        component: () => import("@/pages/selfhelp/specialAttention.vue"),
      },
      {
        path: "teamGallery",
        name: "teamGallery",
        meta: { title: "团队相册" },
        component: () => import("@/pages/selfhelp/teamGallery.vue"),
      },
      {
        path: "myInfo",
        name: "myInfo",
        meta: { title: "我的信息" },
        component: () => import("@/pages/selfhelp/myInfo.vue"),
      },
      {
        path: "profile",
        name: "profile",
        meta: { title: "个人信息" },
        component: () => import("@/pages/selfhelp/profile.vue"),
      },
      {
        path: "firstLineVoice",
        name: "firstLineVoice",
        meta: { title: "一线声音" },
        component: () => import("@/pages/selfhelp/firstLineVoice.vue"),
      },
      {
        path: "employeeVoice",
        name: "employeeVoice",
        meta: { title: "员工声音" },
        component: () => import("@/pages/selfhelp/employeeVoice.vue"),
      },
      {
        path: "myVoice",
        name: "myVoice",
        meta: { title: "我的声音" },
        component: () => import("@/pages/selfhelp/myVoice.vue"),
      },
      {
        path: "myReply",
        name: "myReply",
        meta: { title: "我的回复" },
        component: () => import("@/pages/selfhelp/myReply.vue"),
      },
      {
        path: "myDraft",
        name: "myDraft",
        meta: { title: "我的草稿" },
        component: () => import("@/pages/selfhelp/myDraft.vue"),
      },
      {
        path: "voiceDetail",
        name: "voiceDetail",
        meta: { title: "声音详情" },
        component: () => import("@/pages/selfhelp/voiceDetail.vue"),
      },
      {
        path: "salaryEntry",
        name: "salaryEntry",
        meta: { title: "薪酬查询入口" },
        component: () => import("@/pages/selfhelp/salaryEntry.vue"),
      },
      {
        path: "mySalary",
        name: "mySalary",
        meta: { title: "薪酬查询" },
        component: () => import("@/pages/selfhelp/mySalary.vue"),
      },
      // {path: 'mySalaryDetail', name: '我的薪酬详情', component: () => import('@/pages/selfhelp/mySalaryDetail.vue')},

      {
        path: "salaryDetail",
        name: "salaryDetail",
        meta: { title: "薪酬详情" },
        component: () => import("@/pages/selfhelp/salaryDetail.vue"),
      },
      {
        path: "myAchievements",
        name: "myAchievements",
        meta: { title: "我的绩效" },
        component: () => import("@/pages/selfhelp/myAchievements.vue"),
      },
      {
        path: "clockin",
        name: "clockin",
        meta: { title: "考勤汇总" },
        component: () => import("@/pages/selfhelp/clockin.vue"),
      },
      // 请假
      {
        path: "askForLeave",
        name: "askForLeave",
        meta: { title: "请假申请" },
        component: () => import("@/pages/selfhelp/askForLeave.vue"),
      },
      {
        path: "leaveDetail",
        name: "leaveDetail",
        meta: { title: "请假详情" },
        component: () => import("@/pages/selfhelp/leaveDetail.vue"),
      },
      {
        path: "leaveEdit",
        name: "leaveEdit",
        meta: { title: "请假编辑" },
        component: () => import("@/pages/selfhelp/leaveEdit.vue"),
      },
      {
        path: "leaveApproval",
        name: "leaveApproval",
        meta: { title: "请假审批" },
        component: () => import("@/pages/selfhelp/leaveApproval.vue"),
      },
      // 出差
      {
        path: "askForEvection",
        name: "askForEvection",
        meta: { title: "出差申请" },
        component: () => import("@/pages/selfhelp/askForEvection.vue"),
      },
      {
        path: "evectionDetail",
        name: "evectionDetail",
        meta: { title: "出差详情" },
        component: () => import("@/pages/selfhelp/evectionDetail.vue"),
      },
      {
        path: "evectionEdit",
        name: "evectionEdit",
        meta: { title: "出差编辑" },
        component: () => import("@/pages/selfhelp/evectionEdit.vue"),
      },
      {
        path: "evectionApproval",
        name: "evectionApproval",
        meta: { title: "出差审批" },
        component: () => import("@/pages/selfhelp/evectionApproval.vue"),
      },
      // 加班
      {
        path: "askForOvertime",
        name: "askForOvertime",
        meta: { title: "加班申请" },
        component: () => import("@/pages/selfhelp/askForOvertime.vue"),
      },
      {
        path: "overtimeDetail",
        name: "overtimeDetail",
        meta: { title: "加班详情" },
        component: () => import("@/pages/selfhelp/overtimeDetail.vue"),
      },
      {
        path: "overtimeEdit",
        name: "overtimeEdit",
        meta: { title: "加班编辑" },
        component: () => import("@/pages/selfhelp/overtimeEdit.vue"),
      },
      {
        path: "overtimeApproval",
        name: "overtimeApproval",
        meta: { title: "加班审批" },
        component: () => import("@/pages/selfhelp/overtimeApproval.vue"),
      },
      // 补签
      {
        path: "reClockin",
        name: "reClockin",
        meta: { title: "补签申请" },
        component: () => import("@/pages/selfhelp/reClockin.vue"),
      },
      {
        path: "reClockinDetail",
        name: "reClockinDetail",
        meta: { title: "补签详情" },
        component: () => import("@/pages/selfhelp/reClockinDetail.vue"),
      },
      {
        path: "reClockinEdit",
        name: "reClockinEdit",
        meta: { title: "补签编辑" },
        component: () => import("@/pages/selfhelp/reClockinEdit.vue"),
      },
      {
        path: "reClockinApproval",
        name: "reClockinApproval",
        meta: { title: "补签审批" },
        component: () => import("@/pages/selfhelp/reClockinApproval.vue"),
      },
      // 证明
      {
        path: "askForProve",
        name: "askForProve",
        meta: { title: "证明申请" },
        component: () => import("@/pages/selfhelp/askForProve.vue"),
      },
      {
        path: "proveDetail",
        name: "proveDetail",
        meta: { title: "证明详情" },
        component: () => import("@/pages/selfhelp/proveDetail.vue"),
      },
      {
        path: "proveEdit",
        name: "proveEdit",
        meta: { title: "证明编辑" },
        component: () => import("@/pages/selfhelp/proveEdit.vue"),
      },
      {
        path: "proveApproval",
        name: "proveApproval",
        meta: { title: "证明审批" },
        component: () => import("@/pages/selfhelp/proveApproval.vue"),
      },
      // 转正申请
      {
        path: "askForRegular",
        name: "askForRegular",
        meta: { title: "转正申请" },
        component: () => import("@/pages/selfhelp/askForRegular.vue"),
      },
      {
        path: "regularEmplApproval",
        name: "regularEmplApproval",
        meta: { title: "转正审批" },
        component: () => import("@/pages/selfhelp/regularEmplApproval.vue"),
      },
      // 离职
      {
        path: "askForQuit",
        name: "askForQuit",
        meta: { title: "离职面谈" },
        component: () => import("@/pages/selfhelp/askForQuit.vue"),
      },
      //员工关怀
      // {path: 'askEmployeesCare', name: '员工关怀', component: () => import('@/pages/selfhelp/askEmployeesCare.vue')},
      //调班
      {
        path: "askClass",
        name: "askClass",
        meta: { title: "调班列表" },
        component: () => import("@/pages/selfhelp/askClass.vue"),
      },
      {
        path: "classModel",
        name: "classModel",
        meta: { title: "调班申请" },
        component: () => import("@/pages/selfhelp/classModel.vue"),
      },

      // 我的团队
      {
        path: "myTeamInfo",
        name: "myTeamInfo",
        meta: { title: "我的团队" },
        component: () => import("@/pages/team/myTeamInfo.vue"),
      },
      {
        path: "employeeInfo",
        name: "employeeInfo",
        meta: { title: "成员信息" },
        component: () => import("@/pages/team/employeeInfo.vue"),
      },
      {
        path: "heigSearch",
        name: "heigSearch",
        meta: { title: "简历查询" },
        component: () => import("@/pages/leader/heigSearch.vue"),
      },
      //领导查询-考勤

      {
        path: "leaderClockIn",
        name: "leaderClockIn",
        component: () => import("@/pages/team/leaderClockIn.vue"),
        meta: {
          keepAlive: true,
          title: "考勤查询",
        },
      },
      {
        path: "leaderClockInDetail",
        name: "leaderClockInDetail",
        meta: { title: "考勤查询详情" },
        component: () => import("@/pages/team/leaderClockInDetail.vue"),
      },

      // 团队考勤
      {
        path: "teamClockIn",
        name: "teamClockIn",
        component: () => import("@/pages/team/teamClockIn.vue"),
        meta: {
          keepAlive: true,
          title: "团队考勤",
        },
      },
      {
        path: "teamClockInDetail",
        name: "TeamClockInDetail",
        meta: { title: "团队考勤详情" },
        component: () => import("@/pages/team/teamClockInDetail.vue"),
      },
      // {path: 'leaveDetail', name: '出差详情', component: () => import('@/pages/team/leaveDetail.vue')},
      // 团队培训
      {
        path: "teamTrainInfo",
        name: "teamTrainInfo",
        meta: { title: "团队培训" },
        component: () => import("@/pages/team/teamTrainInfo.vue"),
      },
      {
        path: "trainDetail",
        name: "trainDetail",
        meta: { title: "学习记录" },
        component: () => import("@/pages/team/trainDetail.vue"),
      },
      // 团队薪酬
      {
        path: "teamSalary",
        name: "teamSalary",
        component: () => import("@/pages/team/teamSalary.vue"),
        meta: {
          keepAlive: true,
          title: "团队薪酬",
        },
      },
      // 团队绩效
      {
        path: "teamAchievements",
        name: "teamAchievements",
        meta: { title: "团队薪酬" },
        component: () => import("@/pages/team/teamAchievements.vue"),
      },
      {
        path: "teamChart",
        name: "teamChart",
        component: () => import("@/pages/team/teamChart.vue"),
        meta: {
          keepAlive: true,
          title: "团队绩效",
        },
      },

      // 领导自助

      {
        path: "chartsPage",
        name: "chartsPage",
        meta: { title: "绩效信息" },
        component: () => import("@/pages/team/chartsPage.vue"),
      },
      {
        path: "schoolRecruit",
        name: "schoolRecruit",
        meta: { title: "近三年任职情况" },
        component: () => import("@/pages/leader/schoolRecruit.vue"),
      },
      {
        path: "sybSearch",
        name: "sybSearch",
        meta: { title: "统计分析事业部" },
        component: () => import("@/pages/leader/sybSearch.vue"),
      },
      {
        path: "leaderCharts",
        name: "leaderCharts",
        component: () => import("@/pages/leader/leaderCharts.vue"),
        meta: {
          keepAlive: true,
          title: "领导绩效查询",
        },
      },
      {
        path: "leaderInfo",
        name: "leaderInfo",
        meta: { title: "" },
        component: () => import("@/pages/leader/leaderInfo.vue"),
      },
      {
        path: "statisticInfo",
        name: "statisticInfo",
        meta: { title: "统计入离职分析信息" },
        component: () => import("@/pages/leader/statisticInfo.vue"),
      },

      {
        path: "trainListDetail",
        name: "trainListDetail",
        meta: { title: "培训课程详情" },
        component: () => import("@/pages/leader/trainListDetail.vue"),
      },
      {
        path: "teacherTrain",
        name: "teacherTrain",
        meta: { title: "导师培训" },
        component: () => import("@/pages/leader/teacherTrain.vue"),
      },

      // 简历查询
      {
        path: "resumeQuery",
        name: "resumeQuery",
        component: () => import("@/pages/leader/resumeQuery.vue"),
        meta: {
          keepAlive: true,
          title: "简历查询",
        },
      },
      {
        path: "achievementQuery",
        name: "achievementQuery",
        meta: { title: "绩效查询" },
        component: () => import("@/pages/leader/achievementQuery.vue"),
      },
      //领导绩效
      {
        path: "leaderachievementQuery",
        name: "leaderachievementQuery",
        meta: { title: "绩效查询" },
        component: () => import("@/pages/leader/leaderachievementQuery.vue"),
      },
      {
        path: "myachievementQuery",
        name: "myachievementQuery",
        meta: { title: "绩效与评估", keepAlive: true },
        component: () => import("@/pages/leader/myachievementQuery.vue"),
      },
      {
        path: "myachievementQuerySit",
        name: "myachievementQuerySit",
        meta: { title: "我的绩效" },
        component: () => import("@/pages/leader/myachievementQuerySit.vue"),
      },

      {
        path: "trainQuery",
        name: "trainQuery",
        meta: { title: "培训查询" },
        component: () => import("@/pages/leader/train.vue"),
      },
      {
        path: "trainQuery2",
        name: "trainQuery2",
        meta: { title: "培训查询" },
        component: () => import("@/pages/leader/trainQuery.vue"),
      },
      {
        path: "trainRecord",
        name: "trainRecord",
        meta: { title: "学习记录详情" },
        component: () => import("@/pages/leader/trainRecord.vue"),
      },
      {
        path: "trainSearch",
        name: "trainSearch",
        meta: { title: "高级搜索" },
        component: () => import("@/pages/team/trainSearch.vue"),
      },
      {
        path: "salaryQuery",
        name: "salaryQuery",
        component: () => import("@/pages/leader/salaryQuery.vue"),
        meta: {
          keepAlive: true,
          title: "薪酬查询",
        },
      },
      {
        path: "leadersalaryEntry",
        name: "leadersalaryEntry",
        meta: { title: "领导薪酬密码" },
        component: () => import("@/pages/selfhelp/leadersalaryEntry.vue"),
      },
      {
        path: "managesalaryEntry",
        name: "managesalaryEntry",
        meta: { title: "经理薪酬密码" },
        component: () => import("@/pages/selfhelp/managesalaryEntry.vue"),
      },
      {
        path: "teamSalaryQuery",
        name: "teamSalaryQuery",
        meta: { title: "薪酬列表" },
        component: () => import("@/pages/selfhelp/teamSalaryQuery.vue"),
      },
      {
        path: "hiringQuery",
        name: "hiringQuery",
        meta: { title: "招聘查询" },
        component: () => import("@/pages/leader/hiringQuery.vue"),
      },
      {
        path: "statistic",
        name: "statistic",
        meta: { title: "统计分析" },
        component: () => import("@/pages/leader/statistic.vue"),
      },
      {
        path: "orgStructure",
        name: "orgStructure",
        component: () => import("@/pages/leader/orgStructure.vue"),
        meta: {
          keepAlive: true,
          title: "组织查询",
        },
      },

      {
        path: "fulltextSearch",
        name: "fulltextSearch",
        meta: { title: "全文搜索" },
        component: () => import("@/pages/leader/fulltextSearch.vue"),
      },
      // 表单
      {
        path: "fillInfoForm",
        name: "fillInfoForm",
        meta: { title: "信息补录" },
        component: () => import("@/pages/forms/fillInfoForm.vue"),
      },
      {
        path: "cityPicker",
        name: "cityPicker",
        meta: { title: "城市选择" },
        component: () => import("@/pages/forms/cityPicker.vue"),
      },
      {
        path: "textFormComp",
        name: "textFormComp",
        meta: { title: "文本框" },
        component: () => import("@/pages/forms/textFormComp.vue"),
      },
      {
        path: "jobHistory",
        name: "jobHistory",
        meta: { title: "工作记录" },
        component: () => import("@/pages/forms/jobHistory.vue"),
      },
      {
        path: "editJobHistory",
        name: "editJobHistory",
        meta: { title: "修改工作记录" },
        component: () => import("@/pages/forms/editJobHistory.vue"),
      },
      {
        path: "addJobHistory",
        name: "addJobHistory",
        meta: { title: "新增工作记录" },
        component: () => import("@/pages/forms/addJobHistory.vue"),
      },

      {
        path: "achievementChart",
        name: "achievementChart",
        meta: { title: "绩效图表" },
        component: () => import("@/pages/selfhelp/achievementChart.vue"),
      },

      // 员工祝福
      {
        path: "care",
        name: "Care",
        meta: { title: "员工祝福" },
        component: () => import("@/pages/care/Index.vue"),
      },

      // 主页更多app
      // { path: "home", name: "Home", meta: { title: "主页" }, component: () => import("@/pages/home.vue") },
      {
        path: "moreApp",
        name: "MoreApp",
        meta: { title: "应用中心" },
        component: () => import("@/pages/selfhelp/components/home/MoreApp.vue"),
      },
      // 组织新增
      {
        path: "orgAdd",
        name: "OrgAdd",
        meta: { title: "组织新增" },
        component: () => import("@/pages/organization/Index.vue"),
      },
      {
        path: "orgRevoke",
        name: "OrgRevoke",
        meta: { title: "组织撤销" },
        component: () => import("@/pages/organization/Index.vue"),
      },
      {
        path: "orgChange",
        name: "OrgChange",
        meta: { title: "组织变更" },
        component: () => import("@/pages/organization/Index.vue"),
      },
      // webview外部链接
      {
        path: "webview",
        name: "Webview",
        meta: { title: "" },
        component: () => import("@/pages/webview/Index.vue"),
      },

      // 绩效与评估
      {
        path: "myBacklog",
        name: "MyBacklog",
        meta: { title: "我的消息", keepAlive: true },
        component: () => import("@/pages/performances/myBacklog.vue"),
      },
      {
        path: "myPerformance",
        name: "MyPerformance",
        meta: { title: "我的绩效", keepAlive: true },
        component: () => import("@/pages/performances/myPerformance.vue"),
      },
      {
        path: "teamPerformance",
        name: "teamPerformance",
        meta: { title: "团队绩效" },
        component: () => import("@/pages/performances/teamPerformance.vue"),
      },
      {
        path: "teamPerformanceSit",
        name: "TeamPerformanceSit",
        meta: { title: "团队绩效" },
        component: () => import("@/pages/performances/teamPerformanceSit.vue"),
      },
      {
        path: "approvalSingle",
        name: "ApprovalSingle",
        meta: { title: "指标审批" },
        component: () => import("@/pages/performances/approvalSingle.vue"),
      },
      {
        path: "performancesRate",
        name: "PerformancesRate",
        meta: { title: "绩效评估" },
        component: () => import("@/pages/performances/performancesRate.vue"),
      },
      {
        path: "performancesBatchRate",
        name: "PerformancesBatchRate",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/performancesBatchRate.vue"),
      },
      {
        path: "performancesIndexDetails",
        name: "PerformancesIndexDetails",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/performancesIndexDetails.vue"),
      },
      {
        path: "performancesRateTotal",
        name: "PerformancesRateTotal",
        meta: { title: "绩效评估", keepAlive: true },
        component: () =>
          import("@/pages/performances/performancesRateTotal.vue"),
      },
      {
        path: "performancesRateTotalAdjust",
        name: "PerformancesRateTotalAdjust",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/performancesRateTotalAdjust.vue"),
      },
      {
        path: "performancesBatchRateTotal",
        name: "PerformancesBatchRateTotal",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/performancesBatchRateTotal.vue"),
      },
      {
        path: "performancesBatchAdjustTotal",
        name: "PerformancesBatchAdjustTotal",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/performancesBatchAdjustTotal.vue"),
      },

      {
        path: "approvalList",
        name: "ApprovalList",
        meta: { title: "指标审批列表", keepAlive: true },
        component: () => import("@/pages/performances/approvalList.vue"),
      },
      {
        path: "otherEvaluateDetails",
        name: "OtherEvaluateDetails",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/otherEvaluateDetails.vue"),
      },
      {
        path: "adjustEvaluateDetails",
        name: "AdjustEvaluateDetails",
        meta: { title: "绩效评估" },
        component: () =>
          import("@/pages/performances/adjustEvaluateDetails.vue"),
      },
      {
        path: "rankingList",
        name: "RankingList",
        meta: { title: "排名" },
        component: () => import("@/pages/performances/rankingList.vue"),
      },
      {
        path: "performanceDetail",
        name: "PerformanceDetail",
        meta: { title: "结果确认" },
        component: () => import("@/pages/performances/performanceDetail.vue"),
      },
      {
        path: "myPerformanceDetail",
        name: "MyPerformanceDetail",
        meta: { title: "绩效详情" },
        component: () => import("@/pages/performances/myPerformanceDetail.vue"),
      },
      {
        path: "performancesAppraiseDetails",
        name: "PerformancesAppraiseDetails",
        meta: { title: "指标评价详情" },
        component: () =>
          import("@/pages/performances/performancesAppraiseDetails.vue"),
      },
      {
        path: "overallEvaluationDetails",
        name: "OverallEvaluationDetails",
        meta: { title: "总体评价详情" },
        component: () =>
          import("@/pages/performances/overallEvaluationDetails.vue"),
      },
      // 绩效与评估二期
      {
        path: "teamGoal",
        name: "TeamGoal",
        meta: { title: "目标分解地图" },
        component: () => import("@/pages/performances/teamGoal.vue"),
      },
      {
        path: "disassembleMap",
        name: "DisassembleMap",
        meta: { title: "目标分解地图" },
        component: () => import("@/pages/performances/disassembleMap.vue"),
      },
      {
        path: "individualGoal",
        name: "IndividualGoal",
        meta: { title: "个人目标地图" },
        component: () => import("@/pages/performances/individualGoal.vue"),
      },
      {
        path: "lightWarning",
        name: "LightWarning",
        meta: { title: "绩效亮灯预警" },
        component: () => import("@/pages/performances/lightWarning.vue"),
      },
      {
        path: "targetSuperior",
        name: "TargetSuperior",
        meta: { title: "个人目标上级" },
        component: () => import("@/pages/performances/targetSuperior.vue"),
      },
      {
        path: "targetSubordinate",
        name: "TargetSubordinate",
        meta: { title: "个人目标下级" },
        component: () => import("@/pages/performances/targetSubordinate.vue"),
      },
      {
        path: "warnIndexDetail",
        name: "WarnIndexDetail",
        meta: { title: "绩效亮灯预警" },
        component: () => import("@/pages/performances/warnIndexDetail.vue"),
      },
      {
        path: "warnPerDetail",
        name: "warnPerDetail",
        meta: { title: "绩效亮灯预警" },
        component: () => import("@/pages/performances/warnPerDetail.vue"),
      },
      {
        path: "board",
        name: "board",
        meta: { title: "绩效看板" },
        component: () => import("@/pages/performances/board.vue"),
      },
      {
        path: "planPerList",
        name: "PlanPerList",
        meta: { title: "人员详情" },
        component: () => import("@/pages/performances/planPerList.vue"),
      },

      // 个人画像
      {
        path: "portrait",
        name: "portrait",
        meta: { title: "个人画像" },
        component: () => import("@/pages/newready/index.vue"),
      },
      {
        path: "addtags",
        name: "addtags",
        meta: { title: "添加个性标签" },
        component: () => import("@/pages/newready/addTags.vue"),
      },
      {
        path: "plans",
        name: "plans",
        meta: { title: "个人发展计划" },
        component: () => import("@/pages/personage-plans/index.vue"),
      },
      // 个人发展计划详情页
      {
        path: "plans-detail",
        name: "plans-detail",
        meta: { title: "个人发展计划" },
        component: () => import("@/pages/personage-plans/detail"),
      },
      // 个人发展计划审批页面
      {
        path: "plans-examine",
        name: "plans-examine",
        meta: { title: "个人发展计划" },
        component: () => import("@/pages/personage-plans/examine"),
      },
      // 选择信息补录人员
      {
        path: "examine-add",
        name: "examine-add",
        meta: { title: "设置信息补录人" },
        component: () => import("@/pages/personage-plans/add-msg"),
      },
      // 个人发展计划完成情况评价页面
      {
        path: "plans-finsh",
        name: "plans-finsh",
        meta: { title: "完成情况评价" },
        component: () => import("@/pages/personage-plans/FinishEvaluation"),
      },
      // 信息补录页
      {
        path: "plans-supplementation",
        name: "plans-supplementation",
        component: () => import("@/pages/personage-plans/supplementation"),
      },
      // 员工总自评页面
      {
        path: "plans-selfevaluate",
        name: "plans-selfevaluate",
        meta: { title: "个人发展计划" },
        component: () => import("@/pages/personage-plans/self-evaluation"),
      },
      // 导师评价页面
      {
        path: "plans-mentorevaluate",
        name: "plans-mentorevaluate",
        meta: { title: "个人发展计划" },
        component: () => import("@/pages/personage-plans/mentorevaluate"),
      },
      // 期间任务评价
      {
        path: "plans-periodtask",
        name: "plans-periodtask",
        meta: { title: "期间任务评价" },
        component: () => import("@/pages/personage-plans/periodtask"),
      },
      // 评价人确认页
      {
        path: "plans-confirms",
        name: "plans-confirms",
        meta: { title: "期间任务评价" },
        component: () => import("@/pages/personage-plans/confirm-more"),
      },
      {
        path: "plans-confirm",
        name: "plans-confirm",
        meta: { title: "期间任务评价" },
        component: () => import("@/pages/personage-plans/confirm"),
      },
      {
        path: "plans-selfs",
        name: "plans-selfs",
        meta: { title: "期间任务自评" },
        component: () => import("@/pages/personage-plans/self-more"),
      },
      {
        path: "plans-self",
        name: "plans-self",
        meta: { title: "期间任务自评" },
        component: () => import("@/pages/personage-plans/self"),
      },
      // 设置评价补充页面
      {
        path: "plans-add",
        name: "plans-add",
        meta: { title: "设置评价补充人" },
        component: () => import("@/pages/personage-plans/add-appraiser"),
      },
      // 批量评价页
      {
        path: "plans-batch",
        name: "plans-batch",
        meta: { title: "期间任务评价" },
        component: () => import("@/pages/personage-plans/batch"),
      },

      // 人才看板
      {
        path: "talent-board",
        name: "talent-board",
        meta: { title: "人才看板" },
        component: () => import("@/pages/talent-board/index.vue"),
      },
      {
        path: "board-organization",
        name: "board-organization",
        meta: { title: "组织选择" },
        component: () => import("@/pages/talent-board/organization"),
      },
      // 组织选择页 --单选
      {
        path: "board-singleorganization",
        name: "board-singleorganization",
        meta: { title: "组织选择" },
        component: () => import("@/pages/talent-board/singleorganization"),
      },
      {
        path: "talentpool-detail",
        name: "talentpool-detail",
        component: () => import("@/pages/talent-board/TalentDetail"),
      },
      // 人员列表页
      {
        path: "board-personnel",
        name: "board-personnel",
        meta: { title: "人员列表" },
        component: () => import("@/pages/talent-board/personnel-list"),
      },
    ],
  },
];
