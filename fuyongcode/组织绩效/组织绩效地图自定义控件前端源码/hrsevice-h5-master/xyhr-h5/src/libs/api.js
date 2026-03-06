/** @format */

import axios from "axios";
import qs from "qs";
import { Notify } from "vant";
import Loading from "@/components/xyMessage/loading";
import { Toast, Dialog } from "vant";
/*cookie 储存读取*/
const $cookie = {
  set: function (c_name, value, expiredays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + expiredays);
    document.cookie =
      c_name +
      "=" +
      escape(value) +
      (expiredays == null ? "" : ";expires=" + exdate.toGMTString());
  },
  get: function (c_name) {
    if (document.cookie.length > 0) {
      var c_start = document.cookie.indexOf(c_name + "=");
      if (c_start != -1) {
        c_start = c_start + c_name.length + 1;
        var c_end = document.cookie.indexOf(";", c_start);
        if (c_end == -1) {
          c_end = document.cookie.length;
        }
        return unescape(document.cookie.substring(c_start, c_end));
      }
    }
    return false;
  },
};

// axios配置
const XY_URL = import.meta.env.VITE_PREFIX_XY;
const service = axios.create({
  baseURL: import.meta.env.VITE_PREFIX,
  timeout: 30e3, //响应超时时间为30秒
});
service.interceptors.request.use(
  (config) => {
    // 统一处理请求参数data，防止苹果键盘%E2%80%86对请求数据产生影响
    if (config.data) {
      // formdata 类型的请求不做处理
      if (!(config.data instanceof FormData)) {
        let dataStr = JSON.stringify(config.data);
        dataStr = encodeURI(dataStr);
        dataStr = dataStr.replace(/%E2%80%86/g, "%20");
        dataStr = dataStr.replace(/%C2%A0/g, "");
        dataStr = decodeURI(dataStr);
        config.data = JSON.parse(dataStr);
      }
    }
    if (config.multipart) {
      config.headers["Content-Type"] = "multipart/form-data";
    }
    if (config.json) {
      // 如果配置了json则body请求类型直接丢个json
      config.headers["Content-Type"] = "application/json";
    }
    if (!config.multipart && !config.json) {
      // 如果没有还是正常走formdata
      if (config.data) {
        config.data = qs.stringify(config.data);
      }
    }
    if ($cookie.get("token")) {
      // 预留的token验证请求头
      let token = $cookie.get("token");
      config.headers["HrToken"] = token;
    }
    return config;
  },
  (error) => {
    Promise.reject(error);
  }
);

// 错误状态码返回
service.interceptors.response.use(
  (response) => {
    if (response.data.statusCode && response.data.statusCode != 200) {
      console.log("错误状态码返回==", response.data);
      // Toast.clear()
      const message = response.data.message || "服务异常";
      Toast(message);
    }
    return response;
  },
  // error => response
  (error) => {
    // Toast.clear()
    Loading.hideLoad();
    if (error && error.response) {
      switch (error.response.status) {
        case 400:
          error.message = "请求错误(400)";
          break;
        case 401:
          error.message = "未授权，请重新登录(401)";
          break;
        case 403:
          error.message = "拒绝访问(403)";
          break;
        case 404:
          error.message = "请求出错(404)";
          break;
        case 408:
          error.message = "请求超时(408)";
          break;
        case 500:
          error.message = "服务器错误(500)";
          break;
        case 501:
          error.message = "服务未实现(501)";
          break;
        case 502:
          error.message = "网络错误(502)";
          break;
        case 503:
          error.message = "服务不可用(503)";
          break;
        case 504:
          error.message = "网络超时(504)";
          break;
        case 505:
          error.message = "HTTP版本不受支持(505)";
          break;
        default:
          error.message = `连接出错(${error.response.status})!`;
      }
      if (error.response.status == 401) {
        // token到期则重新单点登录
        // if(window.location.href.indexOf('localhost')>=0) {
        // 	window.location.href = '/#/';
        // }else{
        // 	window.location.href =  '/';
        // }
        Dialog.alert({
          message: "登录失效，请退出重新进入。",
        }).then(() => {
          // on close
          window.closeWindow();
        });
      } else if (error.response.status == 502) {
        Dialog.alert({
          message: "网络异常，请退出重新进入。",
        }).then(() => {
          // on close
          window.closeWindow();
        });
      } else {
        // 提示网络异常信息
        Notify({ type: "danger", message: "网络异常：" + error.message });
      }
    } else {
      error.message = "连接服务器失败!";
      Notify({ type: "danger", message: "网络异常：" + error.message });
    }
    return Promise.reject(error.response);
  }
);

export function clone($obj) {
  return Object.assign({}, $obj);
}

export function csave(key, val, day) {
  $cookie.set(key, val, day);
}
export function cload(key) {
  return $cookie.get(key);
}
export function formatTime(ts) {
  if (!ts) {
    return "从未";
  }
  ts = ts.length < 13 ? parseInt(ts) * 1e3 : parseInt(ts);
  ts = new Date(ts);
  var year = ts.getFullYear();
  var month = ts.getMonth() + 1;
  var date = ts.getDate();
  var hour = ts.getHours();
  var minute = ts.getMinutes();
  var second = ts.getSeconds();
  minute = minute < 10 ? "0" + minute : minute;
  second = second < 10 ? "0" + second : second;
  return (
    year +
    "年" +
    month +
    "月" +
    date +
    "日 " +
    hour +
    ":" +
    minute +
    ":" +
    second
  );
}
//月份判断是否小于10，小于10在前面补0
export function monthFormat(minute) {
  let month = minute < 10 ? "0" + minute : minute;
  return month;
}
//格式化时间
export function dateFormat(fmt, date) {
  let ret;
  const opt = {
    "Y+": date.getFullYear().toString(), // 年
    "m+": (date.getMonth() + 1).toString(), // 月
    "d+": date.getDate().toString(), // 日
    "H+": date.getHours().toString(), // 时
    "M+": date.getMinutes().toString(), // 分
    "S+": date.getSeconds().toString(), // 秒
    // 有其他格式化字符需求可以继续添加，必须转化成字符串
  };
  for (let k in opt) {
    ret = new RegExp("(" + k + ")").exec(fmt);
    if (ret) {
      fmt = fmt.replace(
        ret[1],
        ret[1].length == 1 ? opt[k] : opt[k].padStart(ret[1].length, "0")
      );
    }
  }
  return fmt;
}

export function timeoffset(time1) {
  if (!time1) {
    return "从未";
  }
  let offset = (new Date().getTime() - parseInt(time1) * 1000) / 1000;
  if (offset >= 86400 * 365) {
    return "超过" + Math.floor(offset / (86400 * 365)) + "年前";
  } else if (offset >= 86400 * 30) {
    return "超过" + Math.floor(offset / (86400 * 30)) + "月前";
  } else if (offset >= 86400) {
    return Math.floor(offset / 86400) + "天前";
  } else if (offset >= 3600) {
    return Math.floor(offset / 3600) + "小时前";
  } else if (offset >= 60) {
    return Math.floor(offset / 60) + "分钟前";
  } else {
    return Math.floor(offset / 60) + "秒钟前";
  }
}

export function tagColor(jobtype) {
  let data = "";
  switch (jobtype) {
    case "兼职":
      data = "red";
      break;
    case "离职":
      data = "blue";
      break;
    case "退休":
      data = "orange";
      break;
    default:
      break;
  }
  return data;
}

// 登录
export function login(data) {
  return service({
    url: "emp/passport/login",
    method: "post",
    json: true,
    data: data,
  });
}

//获取工作台数据
export function accessAppList(data) {
  return service({
    url: "api/system/appPage/appEntrancePage/accessAppList",
    method: "post",
    json: true,
    data: data,
  });
}
//获取待办统计
export function getViewData(data) {
  return service({
    url: "api/portal/element/todocount/getViewData",
    method: "post",
    json: true,
    data: data,
  });
}
//获取页面基础框架元素
export function elementJson(data) {
  return service({
    url: "api/mobile/portal/elements/elementJson",
    method: "post",
    json: true,
    data: data,
  });
}
//获取文章列表
export function getnewstab(data) {
  return service({
    url: "api/xyDoc/getAppDocList",
    method: "post",
    data: data,
  });
}
// 获取图片
export function getElinkBanner(data) {
  return service({
    url: "api/mobile/portal/elements/tab",
    method: "post",
    json: true,
    data: data,
  });
}

////////
// 待办 //
////////

// 获取待办事项 来源系统的分类
export function getTodoCalc(data) {
  return service({
    url: "todo/getTodoCalc",
    method: "post",
    data: data,
    json: true,
  });
}
// app端分页查询
export function queryAppAllPage(data) {
  return service({
    url: "todo/queryAppAllPage",
    method: "post",
    data: data,
    json: true,
  });
}
// app端搜索查询
export function queryAppPage(data) {
  return service({
    url: "todo/queryAppPage",
    method: "post",
    data: data,
    json: true,
  });
}
// 获取通讯录列表
export function getHandler(data) {
  return service({
    url: "/api/public/browser/data/17",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量提交
export function supportBatch(data) {
  return new Promise(function (reslove, reject) {
    axios
      .post("http://10.133.9.15/api/todo/supportBatch", data, {
        headers: {
          "Content-Type": "application/json",
        },
      })
      .then((res) => {
        reslove(res);
      });
  });
}

// 公共
// 登录
export function loginByPassword(data) {
  return service({
    url: "/portal/plantform/loginByPassword",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取个人岗位列表
export function getJobList() {
  return service({
    url: "/personal/info/post/list",
    method: "post",
  });
}
//本月入职、离职、岗位变动人数

export function statisticCount() {
  return service({
    url: "/portal/statistic/count",
    method: "post",
  });
}
//本月离职
export function statisticLeaveCount() {
  return service({
    url: "/portal/statistic/count/leave",
    method: "post",
  });
}
//本月入职
export function statisticEntryCount() {
  return service({
    url: "/portal/statistic/count/join",
    method: "post",
  });
}
//本月变动
export function statisticChangeCount() {
  return service({
    url: "/portal/statistic/count/change",
    method: "post",
  });
}

//领导自助-简历查询：判断人员是否有事业部花名册的权限
export function businesspower(data) {
  return service({
    //url: "/portal/resume/business/power",
    url: "/portal/orgs/getHrOrStruct",
    method: "post",
    data: data,
    json: true,
  });
}

//领导自助-员工简历，模糊查询：组织、部门、人员
export function resumefulltext(data) {
  return service({
    url: "/portal/resume/new/full/text/search",
    method: "post",
    data: data,
    json: true,
  });
}

//领导自助-员工简历-行政组织树穿透，有权限过滤
export function getOrgFrame(data) {
  return service({
    url: "/portal/orgs/leader_tree",
    method: "post",
    data: data,
    json: true,
  });
}
//领导自助-员工简历-事业部组织树穿透
export function getOrgFrameBusi(data) {
  return service({
    // url: "/portal/orgs/leader_tree/busi",
    url: "/portal/orgs/divsion_tree",
    method: "post",
    data: data,
    json: true,
  });
}
//领导自助-简历查询：查询本人的简历查看记录
export function getResumeRecord(data) {
  return service({
    url: "/portal/resume/search_record",
    method: "post",
  });
}
//在职状态下拉
export function jobStatusList(params) {
  return service({
    url: "/portal/resume/job/status/list",
    method: "post",
  });
}

//领导自助-简历查询：查询本人的简历查看记录
export function getTextSearch(data) {
  return service({
    url: "/portal/resume/full/text/search",
    method: "post",
    data: data,
    json: true,
  });
}

//
export function getOrgAuthTree(data) {
  return service({
    url: "/portal/orgs/auth_tree",
    method: "post",
    data: data,
    json: true,
  });
}
//领导简历综合查询/高级查询
export function getResumeSearch(data) {
  return service({
    url: "/portal/resume/senior_search",
    method: "post",
    data: data,
    json: true,
  });
}

// 首页
// 基本信息
export function getPsnInfo(params = "", data = {}) {
  return service({
    url: "/portal/psnbase/getPsnInfo" + params,
    method: "post",
    json: true,
    data: data,
  });
}
// 获取个人信息明细
export function getPsnInfoDetail(data) {
  return service({
    url: "/personal/info/detail",
    method: "post",
  });
}
// 年度绩效结果（个人）
export function personalYearRank(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/employ/perf/personalYearRank",
    method: "post",
  });
}

// 获取我的应用列表
export function getMyApplist(data) {
  return service({
    url: "/portal/app/my",
    method: "post",
  });
}

// 编辑我的应用列表
export function updateMyApplist(data) {
  return service({
    url: "/portal/app/edit",
    method: "post",
    json: true,
    data: data,
  });
}
//未读消息总数
export function getUnreadCount() {
  return service({
    url: "/portal/message/unread/count",
    method: "post",
  });
}

//消息列表
export function getMyMsgList() {
  return service({
    url: "portal/message/list",
    method: "post",
  });
}
//消息已读
export function setMsgReaded(data) {
  return service({
    json: true,
    url: "/portal/message/read",
    method: "put",
    data: data,
  });
}
//清除已读
export function clearReadedMsg() {
  return service({
    json: true,
    url: "/portal/message/unread/clear",
    method: "put",
  });
}

//领导查询个人薪酬列表
export function leadersalaryquery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_SALARY,
    url: "/portal/leader/salary/personal/query",
    method: "post",
    json: true,
    data: data,
  });
}
//通用方法：获取所有的公司、部门，不过滤权限,用于相册
export function orgsCommonTree(data) {
  return service({
    url: "/portal/orgs/common_tree",
    method: "post",
    json: true,
    data: data,
  });
}
//用于经理自助模块-获取行政组织（行政线）下有经理权限的公司、部门
export function orgsManagerTree(data) {
  return service({
    url: "/portal/orgs/manager_tree/adminorg",
    method: "post",
    json: true,
    data: data,
  });
}
//经理自助（我的团队）-获取事业部组织（业务线）下有经理权限的公司、部门
export function orgsBusinessTree(data) {
  return service({
    url: "/portal/orgs/manager_tree/business",
    method: "post",
    json: true,
    data: data,
  });
}

//获取培训单位，不区分权限
export function orgsTrainCommonList(data) {
  return service({
    url: "/portal/orgs/train/common/list",
    method: "post",
    json: true,
    data: data,
  });
}

//获取培训单位，不区分权限
export function orgsTrainAuthList(data) {
  return service({
    url: "/portal/orgs/train/auth/list",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
// 团队相册 //
//////////
export function queryImageAbsolutePath(path) {
  //   return HRSSC + "/portal/album/picture/query/?url=" + path
  // return service({
  // 	baseURL: HRSSC,
  // 	url: '/portal/album/picture/query',
  // 	method: 'get',
  // 	params: {
  // 		url : path
  // 	}
  // })
}
export function queryImageAbsolutePath1(path, isOriginal) {
  // return HRSSC + 'portal/album/picture/query/?url=' + path;
  return service({
    url: "/portal/album/picture/query",
    method: "post",
    data: {
      url: path,
      isOriginal: isOriginal ? true : false,
    },
    json: true,
  });
}
//创建相册
export function albumLike(data) {
  return service({
    url: "/portal/album/like",
    method: "put",
    json: true,
    data: data,
  });
}

export function albumDelete(data) {
  return service({
    url: "/portal/album/delete/" + data,
    method: "delete",
  });
}

// 团队相册
export function getAlbumCover(data) {
  return service({
    url: "/portal/album/cover/get",
    method: "post",
  });
}

// 获取最新相册的照片
export function newestGet(data) {
  return service({
    url: "/portal/album/picture/newest/get",
    method: "post",
    json: true,
    data: data,
  });
}
// 上传照片
export function uploadAlbumImage(data) {
  return service({
    url: "/portal/album/picture/upload",
    method: "post",
    json: true,
    data: { base64: data },
  });
}
//获取新增相册数量
export function portalTodayAdd(data) {
  return service({
    url: "/portal/album/picture/today/add/count",
    method: "post",
  });
}
//删除照片
export function pictureDelete(data) {
  return service({
    url: "/portal/album/picture/delete/",
    method: "delete",
    json: true,
    data: data,
  });
}
//获取新增相册数量
export function likeList(data) {
  return service({
    url: "/portal/album/like/list",
    method: "post",
    json: true,
    data: data,
  });
}

//创建相册
export function createAlbum(data) {
  return service({
    url: "/portal/album/create",
    method: "post",
    json: true,
    data: data,
  });
}
export function updateAlbum(data) {
  return service({
    url: "/portal/album/update",
    method: "post",
    json: true,
    data: data,
  });
}

//获取我的团队人数、平均司龄、平均年龄
export function myteamCount(data) {
  return service({
    url: "/portal/manager/myteam/psn_count",
    method: "post",
    json: true,
    data: data,
  });
}
//获取相册列表
export function getAlbumList(data) {
  return service({
    url: "/portal/album/list",
    method: "post",
    json: true,
    data: data,
  });
}
//下载照片
export function pictureDownload(path) {
  return service({
    url: "/portal/album/picture/download",
    method: "post",
    json: true,
    data: {
      url: path,
    },
  });
}

//获取相册列表
export function getAlbumListDetail(data) {
  return service({
    url: "/portal/album/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}
//获取单个相册详情

export function albumDetailGet(data) {
  return service({
    url: "/portal/album/detail/get/" + data,
    method: "post",
  });
}

//////////
// 生日祝福 //
////////////

// 获取近期员工生日接口
export function queryNearlyBirthday(data) {
  return service({
    url: "/portal/birthday/list",
    method: "post",
    data: data,
    json: true,
  });
}
// 新:获取近期员工生日接口、特别关注
export function queryNearlyBirthdayAttention(data) {
  return service({
    url: "/portal/birthday/specialList",
    method: "post",
    data: data,
    json: true,
  });
}
//是否有事业部
export function getBirthdayStruct(data) {
  return service({
    url: "/portal/birthday/getHrOrStruct",
    method: "post",
    json: true,
    data: data,
  });
}
//生日行政组织树
export function getBirthdayAdminorg(data) {
  return service({
    url: "/portal/birthday/adminorg",
    method: "post",
    json: true,
    data: data,
  });
}
//生日事业部组织树
export function getBirthdayBusiness(data) {
  return service({
    url: "/portal/birthday/business",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取常用祝福语句
export function getQuickBlessingList() {
  return service({
    url: "/portal/birthday/blessing/list",
    method: "post",
  });
}
// 发送祝福语句
export function sendBlessing(data) {
  return service({
    url: "/portal/birthday/blessing/send",
    method: "post",
    json: true,
    data: data,
  });
}

// 发送祝福语句
export function sendBlessingBatchsend(data) {
  return service({
    url: "/portal/birthday/blessing/batchsend",
    method: "post",
    json: true,
    data: data,
  });
}
// 设置特别关注
export function setMaintenance(data) {
  return service({
    url: "/portal/birthday/maintenance",
    method: "post",
    json: true,
    data: data,
  });
}
/////////
//绩效查询 //
/////////

//组织树-行政
export function getXzTree(data) {
  return service({
    json: true,
    url: "/portal/orgs/queryOrgsOrDeptForLeaderRef2",
    method: "post",
    data: data,
  });
}
//组织树-事业
export function getSyTree(data) {
  return service({
    json: true,
    url: "/portal/orgs/queryBusiOrgsForApp",
    method: "post",
    data: data,
  });
}

//获取员工信息列表
export function getCurrentPsnInfo(data) {
  return service({
    url: "/portal/psnbase/getCurrentPsnInfo",
    method: "post",
    data: data,
  });
}

//获取员工信息列表
export function getMyPsnList(data) {
  return service({
    url: "/portal/psnbase/getPsnInfo",
    method: "post",
    data: data,
  });
}
//查询简历
export function 接口命名(data) {
  return service({
    url: "接口地址",
    method: "post",
    data: data,
  });
}

//领导自助-简历查询：插入一条简历查看记录
export function searchrecordinsert(data) {
  return service({
    url: "/portal/resume/search_record/insert",
    json: true,
    method: "post",
    data: data,
  });
}

//领导查询和员工绩效页签信息
export function getYearPerfByPsndoc(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/getYearPerfByPsndoc",
    json: true,
    method: "post",
    data: data,
  });
}
//领导自助-查询本月入职人员详细信息
export function getentryMonthDetail(data) {
  return service({
    url: "/portal/statistic/entry_month/detail",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-查询本月离职人员列表
export function getdimissionMonthDetail(data) {
  return service({
    url: "/portal/statistic/dimission_month/detail",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-查询本月岗位变动人员列表
export function gettrnsMonthDetail(data) {
  return service({
    url: "/portal/statistic/trns_month/detail",
    method: "post",
    json: true,
    data: data,
  });
}

//分页查询入职详情列表
export function getstatisticDetailList(data) {
  return service({
    url: "/portal/statistic/entry/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}

//分页查询离职详情列表
export function getstatisticleaveDetailList(data) {
  return service({
    url: "/portal/statistic/leave/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}

//分页查询岗位变动详情列表
export function getstatistictransferDetailList(data) {
  return service({
    url: "/portal/statistic/transfer/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}

//获取领导查询-统计分析，页签权限
export function statistictabpower() {
  return service({
    url: "/portal/statistic/tab/power",
    method: "post",
    json: true,
  });
}

export function personalBusinessList(data) {
  return service({
    url: "/portal/statistic/personal/business/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-人员分析-人数统计表
export function getPsnCountStatistic(data) {
  return service({
    url: "/portal/statistic/personal/psncount",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-人员分析-年龄统计表
export function getPsnAgeStatistic(data) {
  return service({
    url: "/portal/statistic/personal/age",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-近三年校招人员任职情况
export function getSchoolRecruit(data) {
  return service({
    url: "/portal/statistic/school/recruit",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-查询校招总的人员列表
export function getSchoolAllList(data) {
  return service({
    url: "/portal/statistic/school/recruit/all/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-查询校招在职的人员列表
export function getSchoolJobList(data) {
  return service({
    url: "/portal/statistic/school/recruit/on/job/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-查询校招晋升的人员列表
export function getSchoolPromoteList(data) {
  return service({
    url: "/portal/statistic/school/recruit/promote/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-查询校招离职的人员列表
export function getSchoolLeaveList(data) {
  return service({
    url: "/portal/statistic/school/recruit/leave/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-人员分析-工作年限统计表
export function getPsnWorkStatistic(data) {
  return service({
    url: "/portal/statistic/personal/workage",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-人员分析-事业部统计表
export function getPsnSybStatistic(data) {
  return service({
    url: "/portal/statistic/personal/business",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-入职分析
export function getJoinCountAnalysis(data) {
  return service({
    url: "/portal/statistic/entry",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-入职分析-显示入职人数详情
export function getStatisticEntryDetail(data) {
  return service({
    url: "/portal/statistic/entry/detail",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-离职分析
export function getQuitCountAnalysis(data) {
  return service({
    url: "/portal/statistic/dimission",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-离职分析
export function getleavedetail(data) {
  return service({
    url: "/portal/statistic/leave/detail",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计分析-离职分析-离职人数、离职率
export function getQuitPercent(data) {
  return service({
    url: "/portal/statistic/dimission/percent",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-入职分析
export function getCostAnalysis(data) {
  return service({
    url: "/portal/statistic/cost",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计分析-入职分析
export function getmyteamDetail(data) {
  return service({
    url: "/portal/manager/myteam/psn_detail",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-查询本月入职人数
export function entryMonthCount(data) {
  return service({
    url: "/portal/statistic/entry_month/count",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-查询本月离职人数
export function dimissionMonthCount(data) {
  return service({
    url: "/portal/statistic/dimission_month/count",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-查询本月岗位变动人数
export function trnsMonthCount(data) {
  return service({
    url: "/portal/statistic/trns_month/count",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-薪酬查询
export function leaderAnnualQuery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_SALARY,
    url: "/portal/leader/salary/annual/query",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-薪酬查询
export function leaderCheckPwd(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_SALARY,
    url: "/portal/leader/salary/check/pwd",
    method: "post",
    json: true,
    data: data,
  });
}
export function managerCheckPwd(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/salary/check/pwd",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计
export function leaderAttendanceCount(data) {
  return service({
    url: "/portal/leader/attendance/count",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-统计详情
export function leaderAttendanceCountDetail(data) {
  return service({
    url: "/portal/leader/attendance/count/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计个人统计
export function attendancepersonalcount(data) {
  return service({
    url: "/portal/leader/attendance/personal/count/query",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-统计个人统计
export function yeardetaillist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/year/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}

//绩效查询-领导查询，获取年度绩效等级的下拉列表
export function yeargradelist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/grade/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导查询-绩效查询-查询公司前百分比的绩效人员
export function perfYearDistr(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/year_distr",
    method: "post",
    json: true,
    data: data,
  });
}
//领导查询-绩效查询-取年度低绩效人员，排名最后10位
export function perfLowScore(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/low_score",
    method: "post",
    json: true,
    data: data,
  });
}
//领导查询-绩效查询-查询绩效列表数据
export function perfLeaderList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/leader_list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-员工简历-查询员工简历的个人信息页签
export function getresumepersonalInfo(data) {
  return service({
    url: "/portal/resume/personal/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}

//领导自助-员工简历-根据员工主键，查询已公布的绩效信息
export function getperformanceList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/resume/personal/performance/list",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-员工简历-查询奖励信息
export function getRewardList(data) {
  return service({
    url: "/portal/resume/personal/reward/list",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助-员工简历-查询奖励信息
export function employgetPerFormanceInfo(data) {
  return service({
    url: "/employ/perf/getPerFormanceInfo",
    method: "post",
    json: true,
    data: data,
  });
}

//领导查询-绩效查询-查询绩效列表数据
export function getPerFormanceInfo(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/getPerFormanceInfo",
    method: "post",
    json: true,
    data: data,
  });
}
//领导查询-分页学员查询
export function leadertraineesQuery(data) {
  return service({
    url: "/portal/leader/train/trainees/query",
    method: "post",
    json: true,
    data: data,
  });
}

//我的团队-获取绩效方案下拉列表
export function performancePlanList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/plan/list",
    method: "post",
    json: true,
    data: data,
  });
}

export function distributeGrade(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/distribute/grade",
    method: "get",
    params: data,
  });
}

//领导查询-获取绩效方案下拉列表
export function perfleaderYearList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/leader/year/list",
    method: "post",
    json: true,
    data: data,
  });
}

//领导查询-绩效查询-按照绩效等级分布
export function perfDistributeGrade(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/distribute/grade",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
// 薪酬查询 //
//////////

//校验密码并生成临时的密码
export function checkSalaryEntryPsw(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/salary/checkPwd",
    json: true,
    method: "post",
    data: data,
  });
}
//获取薪酬列表
export function querySalarylist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/salary/query",
    json: true,
    method: "post",
    data: data,
  });
}
//修改薪酬查询密码
export function updateSalaryPwd(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/salary/updatePwd",
    json: true,
    method: "post",
    data: data,
  });
}
//重置薪酬查询密码
export function resetSalaryPwd(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/salary/restPwd",
    json: true,
    method: "post",
    data: data,
  });
}

//elink 点赞收藏
export function praiseDoc(data) {
  return service({
    url: "/api/doc/mobile/systemDoc/praiseDoc",
    method: "post",
    data: data,
  });
}
//elink 收藏
export function coluteDoc(data) {
  return service({
    url: "/api/doc/mobile/systemDoc/coluteDoc",
    method: "post",
    data: data,
  });
}

////////
// 培训 //
////////

//分页查询培训
export function leaderTrainQuery(data) {
  return service({
    url: "/portal/leader/train/query",
    json: true,
    method: "post",
    data: data,
  });
}
//分页查询课程
export function leaderLessonQuery(data) {
  return service({
    url: "/portal/leader/train/lesson/query",
    method: "post",
    json: true,
    data: data,
  });
}
//分页学员查询
export function leaderTraineesQuery(data) {
  return service({
    url: "/portal/leader/train/trainees/query",
    method: "post",
    json: true,
    data: data,
  });
}
//学员详情查询
export function leaderTraineesDetail(data) {
  return service({
    url: "/portal/leader/train/trainees/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}

//分页查询内部导师
export function leaderTutorQuery(data) {
  return service({
    url: "/portal/leader/train/in/class/inner/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}
//查询内部导师详情
export function leaderTutorDetail(data) {
  return service({
    url: "/portal/leader/train/inner/tutor/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}

//查询导师授课情况
export function leaderTeachQuery(data) {
  return service({
    url: "/portal/leader/train/tutor/teach/query",
    method: "post",
    json: true,
    data: data,
  });
}
//分页查询外部导师
export function leaderOuterTutorQuery(data) {
  return service({
    url: "/portal/leader/train/in/class/outer/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}
//查询外部导师详情
export function leaderOuterTutorDetail(data) {
  return service({
    url: "/portal/leader/train/outer/tutor/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}
//查询培训进程
export function leaderScheduleQuery(data) {
  return service({
    url: "/portal/leader/train/schedule/query",
    method: "post",
    json: true,
    data: data,
  });
}
/////////
//团队考勤 //
/////////

// 统计
export function teamClockInCount(data) {
  return service({
    url: "/portal/manager/attendance/count",
    method: "post",
    json: true,
    data: data,
  });
}

export function performancechild(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/child/get",
    method: "post",
    json: true,
    data: data,
  });
}
// 分页查询统计详细
export function teamClockInDetail(data) {
  return service({
    url: "/portal/manager/attendance/count/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 分页查询个人统计
export function teamClockInListQuery(data) {
  return service({
    url: "/portal/manager/attendance/personal/count/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 分页查询年假
export function teamAnnualQuery(data) {
  return service({
    url: "/portal/manager/attendance/annual/leave/query",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
// 团队绩效 //
//////////

// 查询公司前百分比的绩效人员
export function queryPerformanceHighlist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/year/distribute/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 取年度低绩效人员，排名最后10位
export function queryPerformanceLowlist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/low/score/list",
    method: "post",
    json: true,
    data: data,
  });
}
// 根据主键集合分页查询绩效列表数据
export function queryPerformancelist(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/list",
    method: "post",
    json: true,
    data: data,
  });
}

// 领导自助-组织树-获取行政线的人事组织，有领导权限过滤
export function orgsAuthTreeAdminorg(data) {
  return service({
    url: "/portal/orgs/auth_tree/adminorg",
    method: "post",
    json: true,
    data: data,
  });
}
// 领导薪酬查询-组织树-获取行政线的人事组织，有领导权限过滤
export function orgsAuthTreeAdminorgSalary(data) {
  return service({
    // baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/orgs/auth_tree/adminorg/salary",
    method: "post",
    json: true,
    data: data,
  });
}
// 领导自助-组织树-获取某个模块查询的组织树，有具体模块的权限过滤。
export function orgsAuthTreeAdminorgmodel(data, model) {
  return service({
    url: "/portal/orgs/auth_tree/adminorg/" + model,
    method: "post",
    json: true,
    data: data,
  });
}

// 领导自助-组织树-获取行政线的人事组织，有领导权限过滤
export function orgsAuthTreeBusiness(data) {
  return service({
    //url: "/portal/orgs/auth_tree/business",
    url: "/portal/orgs/auth_tree/getstructorg",
    method: "post",
    json: true,
    data: data,
  });
}

// 根据主键集合分页查询绩效列表数据
export function queryPerformancequery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 查询个人绩效详情
export function queryPsnPerformance(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/performance/personal/detail/get/" + data,
    method: "post",
  });
}

//领导自助/经理自助-员工简历-培训查询-判断员工是否是讲师
export function trainChecktutor(data) {
  return service({
    url: "/portal/resume/personal/train/checktutor",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助/经理自助-员工简历-培训查询-讲师信息
export function personalTrainTutorDetail(data) {
  return service({
    url: "/portal/resume/personal/train/tutor/detail",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助/经理自助-员工简历-培训查询-查询导师授课情况
export function personalTrainteachList(data) {
  return service({
    url: "/portal/resume/personal/train/teach/list",
    method: "post",
    json: true,
    data: data,
  });
}
//领导自助/经理自助-员工简历-培训查询-学习记录
export function personaltrainstudylist(data) {
  return service({
    url: "/portal/resume/personal/train/study/list",
    method: "post",
    json: true,
    data: data,
  });
}
// 团队培训学员查询
export function traineesQuery(data) {
  return service({
    url: "/portal/manager/train/trainees/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 学员课程查询
export function trainQuery(data) {
  return service({
    url: "/portal/manager/train/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 学员培训课程详情查询
export function lessonQuery(data) {
  return service({
    url: "/portal/manager/train/lesson/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 学员培训课程学习结果查询
export function detailQuery(data) {
  return service({
    url: "/portal/manager/train/trainees/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 学员培训内部讲师课程查询
export function tutorQuery(data) {
  return service({
    url: "/portal/manager/train/inner/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 学员培训外部讲师课程查询
export function outertutorQuery(data) {
  return service({
    url: "/portal/manager/train/outer/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 查询内部导师详情
export function innerDetailQuery(data) {
  return service({
    url: "/portal/manager/train/inner/tutor/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 查询外部导师详情
export function outerDetailQuery(data) {
  return service({
    url: "/portal/manager/train/outer/tutor/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}
//年薪查询
export function annualQuery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/salary/annual/query",
    method: "post",
    json: true,
    data: data,
  });
}
//年薪查询
export function personalQuery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/manager/salary/personal/query",
    method: "post",
    json: true,
    data: data,
  });
}

//员工简历，获取各种档案、参照
export function seorefQuery(data) {
  return service({
    url: "/portal/resume/seo_ref/query",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
//  请假 //
//////////

//获取请假类型
export function getLeaveType(data) {
  return service({
    url: "/portal/tbmquery/queryLeaveType",
    method: "post",
    json: true,
    data: data,
  });
}
//获取居家类型
export function queryLeaveHomeReason(data) {
  return service({
    url: "/portal/tbmquery/queryLeaveHomeReason",
    method: "post",
    json: true,
  });
}
//获取请假时长可用时长
export function getLeaveBalance(data) {
  return service({
    url: "/portal/tbmquery/getLeaveBalance",
    method: "post",
    json: true,
    data: data,
  });
}
//计算请假区间时长
export function calculateLeaveLength(data) {
  return service({
    url: "/portal/tbmquery/calculateLeaveLength",
    method: "post",
    json: true,
    data: data,
  });
}
//请假单保存或者更新
export function saveLeave(data) {
  return service({
    url: "/portal/tbmquery/saveLeave",
    method: "post",
    json: true,
    data: data,
  });
}

//请假获取单据详情数据接口
export function getBillInfo(data) {
  return service({
    url: "/portal/tbmquery/getBillInfo",
    method: "post",
    json: true,
    data: data,
  });
}

//请假获取单据详情数据接口
export function getBillPkStatus(data) {
  return service({
    url: "/portal/tbmquery/bill/pk/get",
    method: "post",
    json: true,
    data: data,
  });
}

//获取单据列表
export function getMyApplication(data) {
  return service({
    url: "/portal/tbmquery/getMyApplication",
    method: "post",
    json: true,
    data: data,
  });
}
//查询OA流程节点的信息
export function queryProcessNodeList(data) {
  return service({
    url: "/portal/tbmquery/queryProcessNodeList",
    method: "post",
    json: true,
    data: data,
  });
}
//计算加班时长
export function queryOvertimeLength(data) {
  return service({
    url: "/portal/tbmOvertime/queryOvertimeLength",
    method: "post",
    json: true,
    data: data,
  });
}
//加班单据保存于更新接口
export function saveOvertime(data) {
  return service({
    url: "/portal/tbmOvertime/saveOvertime",
    method: "post",
    json: true,
    data: data,
  });
}

//删除单据接口
export function deleteBill(data) {
  return service({
    url: "/portal/tbmquery/deleteBill",
    method: "post",
    json: true,
    data: data,
  });
}
//出差单申请界面判断是否展示的字段
export function getApplySettings(data) {
  return service({
    url: "/portal/tbmquery/getApplySettings",
    method: "post",
    json: true,
    data: data,
  });
}
//获取出差人信息
export function getAwayUserInfo(data) {
  return service({
    url: "/portal/tbmAway/getAwayUserInfo",
    method: "post",
    json: true,
    data: data,
  });
}
//查询通用的公司、部门、人员的组织树
export function queryOrgDeptOrPsn(data) {
  return service({
    url: "/portal/orgs/queryOrgDeptOrPsn",
    method: "post",
    json: true,
    data: data,
  });
}
//计算出差时长的方法
export function caculateAway(data) {
  return service({
    url: "/portal/tbmAway/caculateAway",
    method: "post",
    json: true,
    data: data,
  });
}

//出差单保存与更新接口
export function saveAway(data) {
  return service({
    url: "/portal/tbmAway/saveAway",
    method: "post",
    json: true,
    data: data,
  });
}

//出差单公寓信息
export function getApartmentInfo(data) {
  return service({
    url: "/portal/tbmAway/getApartmentInfo",
    method: "post",
  });
}

//保存补签
export function saveSigncard(data) {
  return service({
    url: "/portal/tbmSigncard/saveSigncard",
    method: "post",
    json: true,
    data: data,
  });
}

//根据补签时间获取考勤信息
export function getCheckInRecord(data) {
  return service({
    url: "/portal/tbmSigncard/getCheckInRecord/" + data,
    method: "post",
  });
}

//获取证明类型和受理人
export function getDocRef(data) {
  return service({
    url: "/portal/attest/getDocRef",
    method: "post",
  });
}
//证明模板预览
export function preview(data) {
  return service({
    url: "/portal/attest/preview/" + data,
    method: "post",
  });
}

//保存开证明申请
export function DocRefcommit(data) {
  return service({
    url: "/portal/attest/commit",
    method: "post",
    json: true,
    data: data,
  });
}
//获取证明人列表
export function getApplyer(data) {
  return service({
    url: "/portal/attest/getApplyer",
    method: "post",
    json: true,
    data: data,
  });
}
//获取单据提交行为
export function getSubmitBehavior(data) {
  return service({
    url: "/portal/tbmquery/getSubmitBehavior",
    method: "post",
    json: true,
    data: data,
  });
}
//获取单据提交行为
export function getQueryApproverList(data) {
  return service({
    url: "/portal/tbmquery/approver/list",
    method: "post",
    json: true,
    data: data,
  });
}

//提交表单
export function submitBill(data) {
  return service({
    url: "/portal/tbmquery/submitBill",
    method: "post",
    json: true,
    data: data,
  });
}

//绩效查询-查看季度绩效（子方案）的详情
export function perfChildGet(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/child/get",
    method: "post",
    json: true,
    data: data,
  });
}

//获取培训单位，不区分权限
export function trainCommonList(data) {
  return service({
    url: "/portal/orgs/train/common/list",
    method: "post",
    json: true,
    data: data,
  });
}

//年度绩效结果（个人）
export function personalYearPerf(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/self/perf/personal/year/perf/" + data,
    method: "get",
  });
}
//年度绩效结果（部门）
export function deptYearPerf(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/self/perf/dept/year/perf/" + data,
    method: "get",
  });
}
//季度绩效详情
export function getdeptYearinfo(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/self/perf/get/perf/info/" + data,
    method: "get",
  });
}

//文件上传
export function upload(data) {
  return service({
    url: "/portal/fileManager/upload",
    method: "post",
    multipart: true,
    data: data,
  });
}
//上传文件删除
export function fileDelete(data) {
  return service({
    url: "/portal/fileManager/delete",
    method: "post",
    multipart: true,
    data: data,
  });
}

//获取撤销审批状态节点
export function getRequestCurrentstatus(data) {
  return service({
    url: "/portal/tbmquery/getRequestCurrentstatus",
    method: "post",
    json: true,
    data: data,
  });
}
//强制归档接口
export function xyRevoke(data) {
  return service({
    url: "/portal/tbmquery/xyRevoke",
    method: "post",
    json: true,
    data: data,
  });
}
//文件列表
export function fileList(data) {
  return service({
    url: "/portal/fileManager/fileList",
    method: "post",
    multipart: true,
    data: data,
  });
}

//获取个人信息简要
export function getinfosimple() {
  return service({
    url: "/personal/info/simple",
    method: "post",
  });
}
export function getReference(data) {
  return service({
    url: "/personal/info/reference/get",
    method: "post",
    json: true,
    data: data,
  });
}
//本月汇总接口(矿工 迟到 早退 请假)
export function queryCalendarMonthSum(data) {
  return service({
    url: "/portal/tbmquery/queryCalendarMonthSum",
    method: "post",
    json: true,
    data: data,
  });
}
//获取一天考勤具体详情接口
export function queryCalendarDayDetails(data) {
  return service({
    url: "/portal/tbmquery/queryCalendarDayDetails",
    method: "post",
    json: true,
    data: data,
  });
}
//获取考勤日历中心接口
export function queryCalendar(data) {
  return service({
    url: "/portal/tbmquery/queryCalendar",
    method: "post",
    json: true,
    data: data,
  });
}

//查看流程图接口
export function lookFlowChart(data) {
  return service({
    url: "/portal/hrTodoBpm/lookFlowChart",
    method: "post",
    json: true,
    data: data,
  });
}
//撤销个人信息提交
export function infoRevoke(data) {
  return service({
    url: "/personal/info/revoke",
    method: "post",
    json: true,
    data: data,
  });
}

export function loginSsoCrm(data) {
  return service({
    url: "/portal/plantform/loginSsoCrm",
    method: "post",
    json: true,
    data: data,
  });
}
export function fetchData(data) {
  return service({
    url: "/portal/plantform/loginSsoFw",
    method: "post",
    json: true,
    data: data,
  });
}
export function loginSsoEl(data) {
  return service({
    url: "/portal/plantform/loginSsoEl",
    method: "post",
    json: true,
    data: data,
  });
}

export function loginSsoEcs(data) {
  return service({
    url: "/portal/plantform/loginSsoEcs",
    method: "post",
    json: true,
    data: data,
  });
}

//提交个人信息编辑
export function personalInfoEdit(data) {
  return service({
    url: "/personal/info/edit",
    method: "post",
    json: true,
    data: data,
  });
}
//提交个人信息编辑
export function infoNotCompleteGet(data) {
  return service({
    url: "/personal/info/not/complete/get",
    json: true,
    data: data,
  });
}

//分页查询班级外的内部讲师
export function outInnerQuery(data) {
  return service({
    url: "/portal/leader/train/out/class/inner/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}

//分页查询班级外的外部讲师
export function outOuterQuery(data) {
  return service({
    url: "/portal/leader/train/out/class/outer/tutor/query",
    method: "post",
    json: true,
    data: data,
  });
}

//查询导师授课情况
export function trainteachQuery(data) {
  return service({
    url: "/portal/leader/train/tutor/teach/query",
    method: "post",
    json: true,
    data: data,
  });
}

//获取常用信息
export function searchGeneralInfo(data) {
  return service({
    url: "/portal/generalInfo/searchGeneralInfo",
    method: "post",
    json: true,
    data: data,
  });
}

//获取常用信息
export function getStaffCodeIsSalesMan(data) {
  return service({
    url: "/portal/tbmquery/crm/sales/get",
    method: "post",
    json: true,
    data: data,
  });
}

//一线声音模块
//声音类别接口
export function psnVoiceQueryVoiceTypeList(data) {
  return service({
    url: "/portal/psnVoice/queryVoiceTypeList",
    method: "post",
    json: true,
    data: data,
  });
}

//获取声音列表
export function psnVoiceQueryVoiceListByPage(data) {
  return service({
    url: "/portal/psnVoice/queryVoiceListByPage",
    method: "post",
    json: true,
    data: data,
  });
}
//获取声音列表
export function psnVoiceQueryVoiceByKey(data) {
  return service({
    url: "/portal/psnVoice/queryVoiceByKey",
    method: "post",
    data: data,
  });
}
//获取未读个数
export function psnVoiceGetUnreadCount(data) {
  return service({
    url: "/portal/psnVoice/getUnreadCount/" + data,
    method: "post",
  });
}
//声音或评论点赞
export function psnVoiceLike(data) {
  return service({
    url: "/portal/psnVoice/like",
    method: "post",
    data: data,
  });
}

//取消声音或评论点赞
export function psnVoiceCancelLike(data) {
  return service({
    url: "/portal/psnVoice/cancelLike",
    method: "post",
    data: data,
  });
}

//获取评论列表
export function psnVoiceGetComm(data) {
  return service({
    url: "/portal/psnVoice/getComm",
    method: "post",
    json: true,
    data: data,
  });
}
//添加评论
export function psnVoiceAddComm(data) {
  return service({
    url: "/portal/psnVoice/addComm",
    method: "post",
    json: true,
    data: data,
  });
}

//上传图片
export function uploadPicture(data) {
  return service({
    url: "/portal/psnVoice/upload/picture",
    method: "post",
    multipart: true,
    data: data,
  });
}

//发布声音
export function psnVoicePublishVoice(data) {
  return service({
    url: "/portal/psnVoice/publishVoice",
    method: "post",
    json: true,
    data: data,
  });
}
//发布声音
export function psnVoiceupset(data) {
  return service({
    url: "/portal/psnVoice/upset",
    method: "post",
    json: true,
    data: data,
  });
}

//发布声音
export function saveFile(data) {
  return service({
    url: "/portal/file/saveFile",
    method: "post",
    json: true,
    data: data,
  });
}
//获取声音图片
export function selectFile(data) {
  return service({
    url: "/portal/file/selectFile",
    method: "post",
    json: true,
    data: data,
  });
}
//获取图片base64
export function getImageByte(data) {
  return service({
    url: "/portal/file/getImageByte",
    method: "post",
    json: true,
    data: data,
  });
}
//获取我的回复
export function getMyComm() {
  return service({
    url: "/portal/psnVoice/getMyComm",
    method: "post",
  });
}
//置顶评论
export function psnVoiceVoiceTop(data) {
  return service({
    url: "/portal/psnVoice/voiceTop",
    method: "post",
    json: true,
    data: data,
  });
}

//获取当前登录人是否有置顶权限

export function getAuthority() {
  return service({
    url: "/portal/psnVoice/getAuthority",
    method: "post",
  });
}
//取消置顶权限
export function psnVoiceCancelVoiceTop(data) {
  return service({
    url: "/portal/psnVoice/cancelVoiceTop",
    method: "post",
    json: true,
    data: data,
  });
}
//删除声音
export function psnVoicedelVoice(data) {
  return service({
    url: "/portal/psnVoice/delVoice",
    method: "post",
    // json: true,
    data: data,
  });
}
//删除评论
export function psnVoiceDelComm(data) {
  return service({
    url: "/portal/psnVoice/delComm",
    method: "post",
    // json: true,
    data: data,
  });
}
//获取评论个数
export function commcount(mod, pk) {
  return service({
    url: "/portal/psnVoice/comm/count/" + mod + "/" + pk,
    method: "post",
  });
}

//判断员工简历详情-全部页签的权限
export function personalTabPower() {
  return service({
    url: "/portal/resume/personal/tab/power",
    method: "post",
    // json: true,
  });
}

//领导查询-绩效查询-取年度低绩效人员详情
export function lowScoreInfo(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_XY,
    url: "/portal/perf/lowScore/info",
    method: "post",
    data: data,
    json: true,
  });
}

//补签 判断登录人板块-true不需要补签原因
export function psnOrg(data) {
  return service({
    url: "/portal/tbmSigncard/psnOrg",
    method: "post",
    data: data,
  });
}

//获取顶级培训体系列表
export function trainingSysList() {
  return service({
    url: "/portal/leader/train/top/training/sys/list",
    method: "post",
  });
}
//获取下一级培训体系列表
export function trainingSysQuery(id) {
  return service({
    url: "/portal/leader/train/training/sys/query/" + id,
    method: "post",
  });
}

//分页获取活动列表
export function activityQuery(data) {
  return service({
    url: "/portal/leader/train/activity/query",
    method: "post",
    json: true,
    data: data,
  });
}
//获取活动列表
export function trainActivityList(data) {
  return service({
    url: "/portal/leader/train/activity/list",
    method: "post",
    json: true,
    data: data,
  });
}

//分页获取培训班列表
export function trainingQuery(id) {
  return service({
    url: "/portal/leader/train/query/" + id,
    method: "post",
  });
}

//分页查询我的调班列表
export function getTbmShiftList(data) {
  return service({
    json: true,
    url: "/portal/changeShift/getTbmShiftList",
    method: "post",
    data: data,
  });
}
//获取调班详情
export function changeShiftBillQuery(id) {
  return service({
    url: "/portal/changeShift/bill/query/" + id,
    method: "post",
  });
}
//员工调班申请提交
export function submitTbmShift(data) {
  return service({
    json: true,
    url: "/portal/changeShift/submitTbmShift",
    method: "post",
    data: data,
  });
}

// 需要商旅预订跳转需要调用的接口
export function saveToken(data) {
  return service({
    json: true,
    url: "/portal/app/save/token",
    method: "post",
    data: data,
  });
}
// 获取跳转url
export function getUrl(data) {
  return service({
    url: "/portal/app/url/get",
    method: "post",
    json: true,
    data: data,
  });
}

// =====员工关怀=======================================
// 关怀类型
export function getCareType() {
  return service({
    url: "/portal/care/type/list",
    method: "post",
  });
}
// 获取个人信息配置
export function getUserSetting(pkPsnDoc) {
  return service({
    url: "/personal/info/set/list",
    method: "post",
    json: true,
    data: { pkPsnDoc },
  });
}
// 分页查询
export function getCareData(data) {
  return service({
    url: "/portal/care/bill/list",
    method: "post",
    data: data,
    json: true,
  });
}
// 单个关怀详情
export function getCareDetail(id) {
  return service({
    url: "/portal/care/bill/query/" + id,
  });
}
// 保存
export function saveCare(data) {
  return service({
    url: "/portal/care/bill/save",
    method: "post",
    data: data,
    json: true,
  });
}
// 提交
export function submitCare(data) {
  return service({
    url: "/portal/care/bill/commit",
    method: "put",
    data: data,
    json: true,
  });
}
// 撤销关怀
export function revokeCare(id) {
  return service({
    url: "/portal/care/bill/revoke/" + id,
    method: "put",
    json: true,
  });
}
// 删除关怀
export function deleteCare(id) {
  return service({
    url: "/portal/care/bill/delete/" + id,
    method: "delete",
    json: true,
  });
}
// 在办已办申请数量
export function getMyApply(id) {
  return service({
    url: "/manager/myteam/apply_count",
    method: "post",
    json: true,
  });
}
// 组织新增，撤销，变动数量查询
export function getOrgCount(id) {
  return service({
    url: "/portal/manager/myteam/org_count",
    method: "post",
    json: true,
  });
}
// 获取最新的token，跳转异构系统使用
export function getTokenRealTime(data) {
  return service({
    url: "/portal/getToken",
    method: "post",
    json: true,
    data,
  });
}
// 获取组织新增明细
export function getAdddetail(data) {
  return service({
    url: "/portal/manager/myteam/org_adddetail",
    method: "post",
    json: true,
    data,
  });
}
// 组织撤销明细
export function getBackdetail(data) {
  return service({
    url: "/portal/manager/myteam/org_backdetail",
    method: "post",
    json: true,
    data,
  });
}
// 组织变动明细
export function getChangedetail(data) {
  return service({
    url: "/portal/manager/myteam/org_changedetail",
    method: "post",
    json: true,
    data,
  });
}
// 获取行政组织类型
export function getOrgNumber(data) {
  return service({
    url: "/portal/getOrgNumber",
    method: "post",
    json: true,
    data,
  });
}

// 云学堂token校验链接
export function learnCheck(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_LEARN,
    url: "/transfer/el/sso/kingdee/check",
    method: "post",
    json: true,
    data,
  });
}
// 一个随意的接口，要求响应快，不做任何操作，只为金蝶注入token
export function getTokenFromServe(data) {
  return service({
    url: "/portal/resume/seo_ref/query" + data,
    method: "post",
    data: { seotype: "psncl" },
  });
}

// 职位子序列查询接口
export function getWorkPosition(data) {
  return service({
    url: "/portal/manager/myteam/function/querysubsequence",
    method: "post",
    json: true,
    data: data,
  });
}

// 职位子序列-我的团队信息合计接口
export function getSeniorityOfGroup(data) {
  return service({
    url: "/portal/manager/myteam/function/psn_count",
    method: "post",
    json: true,
    data: data,
  });
}

// 职位子序列-团队人员信息接口
export function getPositionPsnDetail(data) {
  return service({
    url: "/portal/manager/myteam/function/psn_detail",
    method: "post",
    json: true,
    data: data,
  });
}

// 岗位子序列-团队考勤-团队考勤统计
export function teamPositionClockInCount(data) {
  return service({
    url: "/portal/manager/myteam/attendance/count",
    method: "post",
    json: true,
    data: data,
  });
}

// 岗位子序列-团队考勤-分页查询年假
export function teamPositionAnnualQuery(data) {
  return service({
    url: "/portal/manager/myteam/attendance/annual/leave/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 岗位子序列-行政组织
export function teamOrgsManagerTree(data) {
  return service({
    url: "/portal/orgs/auth_tree/function/adminorg",
    method: "post",
    json: true,
    data: data,
  });
}
// 岗位子序列-团队考勤-分页查询个人统计
export function teamPositionClockInListQuery(data) {
  return service({
    url: "/portal/manager/myteam/attendance/personal/count/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 岗位子序列-团队考勤-统计-穿透
export function teamPositionClockInDetail(data) {
  return service({
    url: "/portal/manager/myteam/attendance/count/detail/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 离职退休分页查询列表
export function getStatisticLeaveRetireDetailList(data) {
  return service({
    url: "/portal/statistic/leave_retire/detail/list",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效与评估入口页
export function getPerformancesInfo() {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/getInfo",
    method: "post",
  });
}
//排行榜
export function getRankInformation(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/bath/getRankInformation",
    method: "post",
    // json: true,
    data: data,
  });
}
// 我的待办，已办列表
export function getWorksList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation//handle/getInfo",
    method: "post",
    data: data,
  });
}
// 我的绩效
export function getPerformanceInformationList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/my_performance/getPerformanceInformationList",
    method: "post",
    data: data,
  });
}
// 组织列表
export function getOrganizationList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/team_performance/getOrganizationList",
    method: "post",
    data: data,
  });
}

// 团队绩效
export function getTeamPerList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/team_performance/getPerformanceInformationList",
    method: "post",
    data: data,
  });
}
// 我的绩效详情
export function getPerformanceDetails(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/my_performance/getPerformanceDetails",
    method: "post",
    data: data,
  });
}
// 待办-指标审批
export function getApprovalInformationDetails(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/getApprovalInformationDetails",
    method: "post",
    data: data,
  });
}

// 待办-指标审批-通过
export function indexApprovalPass(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/pass",
    method: "post",
    data: data,
  });
}
// 待办-指标审批-退回
export function indexApprovalReject(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/reject",
    method: "post",
    data: data,
  });
}
// 待办-指标审批-审批列表
export function getApprovalList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/approvalList/getInfo",
    method: "post",
    json: true,
    data: data,
  });
}
// 待办-指标审批-审批列表-批量通过
export function approvalListBatchPass(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/approvalList/batchPass",
    method: "post",
    json: true,
    data: data,
  });
}
// 待办-指标审批-审批列表-批量退回
export function approvalListBatchReject(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/approvalList/batchReject",
    method: "post",
    json: true,
    data: data,
  });
}

// 整单评分
export function getPerformancesRate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_evaluation_details/getInfo",
    method: "post",
    json: true,
    data,
  });
}
//整单评分计算
export function totalPoints(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/totalPoints",
    method: "post",
    json: true,
    data,
  });
}
//整单评分保存提交
export function saveRate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/complete_order/savebtn",
    method: "post",
    json: true,
    data,
  });
}
//整单评分-上传
export function rateUpload(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/file/upload",
    method: "post",
    json: true,
    multipart: true,
    data,
  });
}
//整单评分-交叉
export function getMessage(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/cross_scoring/solo/getMessage",
    method: "post",
    json: true,
    data,
  });
}
//整单评分交叉-批量
export function getBatchOperationList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/cross_scoring/batchProcessing/getBatchOperationList",
    method: "post",
    json: true,
    data,
  });
}
//整单评分交叉-批量保存提交
export function batchSavebtn(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/batch/savebtn",
    method: "post",
    json: true,
    data,
  });
}
//整单评分提交和整单评总分提交一样
export function submitRate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/performance_evaluation/submit",
    method: "post",
    json: true,
    data: data,
  });
}
//整单评总分
export function getPerformancesTotalScore(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/batch/get",
    method: "post",
    data: data,
  });
}
//整单评总分-单人保存或提交
export function submitRateTotal(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/overall_comment/saveorsubmit",
    method: "post",
    json: true,
    data: data,
  });
}
//整单评总分-批量保存或提交接口
export function submitBatchRateTotal(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/overall_comment/batch/saveorsubmit",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效与评估-待办-绩效评估-查看排名
export function getRankList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/batch/getRankInformation",
    method: "post",
    json: true,
    data: data,
  });
}
//整单评总分-批量
export function getBatchRate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/batch/get",
    method: "post",
    json: true,
    data: data,
  });
}
//总体评价详情
export function getAllAreaEvaluateDate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/my_performance/getAllAreaEvaluateDate",
    method: "post",
    data: data,
  });
}
//指标审批-待办-结果确认
export function getResultData(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/result_confirmation/getMessage",
    method: "post",
    data: data,
  });
}
//指标审批-待办-结果确认-认同/不认同
export function resultPassOrReject(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/result_confirmation/passOrReject",
    method: "post",
    json: true,
    data: data,
  });
}
//指标审批-待办-审批列表-状态
export function getApprovalListStatus(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_approval/filter_conditions/getInfo",
    method: "post",
    json: true,
    data: data,
  });
}
// 当前信息参数数据
export function getCurrentInfoData(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/alphacall",
    method: "post",
    json: true,
    data: data,
  });
}
// 多级指标预览
export function getPreview(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/view/preview",
    method: "post",
    json: true,
    data: data,
  });
}
// 整单评分调整总分
export function getAdjustedscore(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_evaluation_details/adjustedscore/getInfo",
    method: "post",
    json: true,
    data: data,
  });
}
// 调整总分-总分与总等级更新接口
export function getAdjustedScoreChange(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/handle/indicator_evaluation_details/adjustedscore/change",
    method: "post",
    json: true,
    data: data,
  });
}
// 单人-调整总分-保存接口
export function getAdjustedScoreSave(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/indicator_evaluation_details/adjustedscore/save",
    method: "post",
    json: true,
    data: data,
  });
}

// 单人-调整总分-提交接口
export function getAdjustedScoreSubmit(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/indicator_evaluation_details/adjustedscore/submit",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量-调整总分-总分信息查询接口
export function getBatchAdjustedList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/adjustedscore/batch/get",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量-调整总分-保存接口
export function getBatchAdjustedSave(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/indicator_evaluation_details/adjustedscore/batch/save",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量-调整总分-提交接口
export function getBatchAdjustedSubmit(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_btn/indicator_evaluation_details/adjustedscore/batch/submit",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量评总分状态
export function getBatchTotalStatus(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/batch/filter_conditions",
    method: "post",
    json: true,
    data: data,
  });
}
// 批量调整总分状态
export function getAdjustedScoreStatus(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/performance_evaluation/adjustedscore/batch/filter_conditions",
    method: "post",
    json: true,
    data: data,
  });
}

// 本月入职、离职、异动、判断是否有穿透权限
export function listPass(data) {
  return service({
    url: "portal/statistic/detail/list/pass",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效评估-退回
export function backEvaluate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/back/evaluate",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效亮灯预警首页-过滤条件获取接口
export function getFilterInfo(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/lightwarning/brightlighthome/getfilterinfo",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效亮灯预警首页
export function getBrightlighthome(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/lightwarning/brightlighthome/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板- 亮点盲点
export function getPerformanceTrendAnalytics(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_and_valuation/get_performance_trend_analytics",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板- 等级分布
export function getAllDistribution(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_all_distribution",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板- 预警分析
export function getStatisticsCycleWarn(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_statistics_cycle_warn",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板- 预警分析-周期接口
export function getCycle(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_cycle",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板- 预警分析-组织对比
export function getStatisticsOrgWarn(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_statistics_org_warn",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板- 绩效波动
export function getFluctuate(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/board/fluctuate/query",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板-进度跟踪页面数据获取
export function getBoardData(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_board_data",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板-组织获取
export function getOrgBoard(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_org",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板-职层获取
export function getPosBoard(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_pos",
    method: "post",
    json: true,
    data: data,
  });
}

//个人目标分解地图-人员列表
export function getTargetDisassemblyList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetDisassembly/underling/query",
    method: "post",
    json: true,
    data: data,
  });
}

//个人目标分解地图-查询目标分解
export function getTargetDisassembly(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetDisassembly/query",
    method: "post",
    json: true,
    data: data,
  });
}

//个人目标分解地图-查询目标计划执行活动
export function getTargetPlan(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetPlan/query",
    method: "post",
    json: true,
    data: data,
  });
}
//个人目标分解地图-查询目标分解下级目标
export function getTargetDisassemblySub(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetDisassembly/subordinateTarget/query",
    method: "post",
    json: true,
    data: data,
  });
}
//个人目标分解地图-目标对齐查询上级目标接口
export function superiorTarget(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetDisassembly/superiorTarget/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 查看员工绩效指标亮灯概览-数据查询
export function employeeLightView(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/lightwarning/employeelightingoverview/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板-考核活动获取
export function getActivity(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_activity",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板-默认查询条件查询接口-进度跟踪
export function getScreeningCondition(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_screening_condition",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板-默认查询条件保存接口-进度跟踪
export function setPanlecondition(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/set_panlecondition",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板-默认查询条件查询接口-等级分布
export function getDistributedScreen(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_distributed_screen",
    method: "post",
    json: true,
    data: data,
  });
}
// 绩效看板-默认查询条件保存接口-等级分布
export function setDistributedScreen(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/set_distributed_screen",
    method: "post",
    json: true,
    data: data,
  });
}

// 绩效看板-进度跟踪-饼图穿透
export function getPieChartInfo(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/performance_kanban/get_pie_chart_info",
    method: "post",
    json: true,
    data: data,
  });
}

// 亮灯预警-指标详情
export function getIndexDetail(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/lightwarning/employeelightingoverview/getInfo",
    method: "post",
    json: true,
    data: data,
  });
}

// 个人目标分解地图-亮灯详情
export function getLightUp(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TGPLAN,
    url: "/targetManagement/targetDisassembly/lightUp/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 领导自助-人员分析报表：判断人员是否有事业部花名册的权限
export function psnBusinessPower(data) {
  return service({
    url: "/portal/structuretree/structuretree/getstructauthority",
    method: "post",
    data: data,
    json: true,
  });
}

// 领导自助-人员分析报表：判断人员是否有事业部花名册的组织树
export function getPsnOrgFrameBusi(data) {
  return service({
    url: "/portal/structuretree/structuretree/getstructuretree",
    method: "post",
    data: data,
    json: true,
  });
}

// 领导自助-统计分析：判断人员是否有事业部花名册的权限
export function analysisBusinessPower(data) {
  return service({
    url: "/portal/structuretree/structuretree/getstructauthority",
    method: "post",
    data: data,
    json: true,
  });
}

// 领导自助-统计分析：获取人员事业部花名册的组织树
export function getAnalysisOrgFrameBusi(data) {
  return service({
    url: "/portal/structuretree/structuretree/getstructuretree",
    method: "post",
    data: data,
    json: true,
  });
}

//////////
// 个人画像 //
//////////
// 画像权限
export function portraitLimit(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getAuth",
    method: "post",
    json: true,
    data: data,
  });
}
//获取人员主岗位人才档案
export function TalentMasterFile(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getTalentMasterFile",
    method: "post",
    json: true,
    data: data,
  });
}
// 我的标签
export function getTalentLabel(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getTalentLabel",
    method: "post",
    json: true,
    data: data,
  });
}

// 操作员工参考标签
export function operationTalentLabel(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/operationTalentLabel",
    method: "post",
    json: true,
    data: data,
  });
}

// 三年绩效
export function getPerfVO(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getPerfVO",
    method: "post",
    json: true,
    data: data,
  });
}
// 综合评价
export function getComprehensiveVO(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getComprehensiveVO",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取我的历程 、 关键实践 、培训信息
export function getTalentData(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getTalentData",
    method: "post",
    json: true,
    data: data,
  });
}

// 盘点活动
export function getActivityFilter(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/countingActivityFilter",
    method: "post",
    json: true,
    data: data,
  });
}

// 盘点结果
export function getInventoryResult(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/inventoryResult",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取领导力潜质
export function getLeader(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getLeader",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取个性风格
export function getPersonality(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getPersonality",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取管理风格
export function getManage(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getManage",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取岗位胜任力
export function getCompetency(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getCompetency",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取人才培养池
export function getPool(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getPool",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取梯队信息 getEchelon
export function getEchelon(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getEchelon",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取我的个人发展计划
export function myPlanList(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getMyList",
    method: "post",
    json: true,
    data: data,
  });
}
// 个人画像 -- 任职资格
export function getQualification(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/getCerApply",
    method: "post",
    json: true,
    data: data,
  });
}
// 梯队信息穿透人员列表
export function getEchelonToList(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_PORTRAIT,
    url: "/tlmg/echelonToList",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
// 人才看板 //
//////////
// 获取组织架构数据
export function OrgTree(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getOrgTreeInPerLicense",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取人才九宫格-单组织跨年度【多年度】
export function getTalent9BoxGridByYears(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getTalent9BoxGridByYears",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取人才九宫格 - 多组织同年【多组织】
export function getTalent9BoxGridByOrgIds(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getTalent9BoxGridByOrgIds",
    method: "post",
    json: true,
    data: data,
  });
}

// 梯队情况 -- 单组织多年度 getOneOrgEchelonCondition
export function getOneOrgEchelonConditionByYears(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/work/getOneOrgEchelonCondition",
    method: "post",
    json: true,
    data: data,
  });
}
// 梯队情况 -- 多组织
export function getMoreOrgEchelonCondition(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/work/getMoreOrgEchelonCondition",
    method: "post",
    json: true,
    data: data,
  });
}

// 1、团队能力 -- 单组织多年度
export function getTeamAbilityByMoreYear(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: `/tdc/getTermScoreManyYear`,
    method: "post",
    json: true,
    data: data,
  });
}
// 2、团队能力 -- 多组织单年度
export function getTeamAbilityByMoreOrgan(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getTermScoreManyOrgan",
    method: "post",
    json: true,
    data: data,
  });
}

// 组织氛围 -- 单组织多年度  getAtmosphereManyYear
export function getAtmosphereByManyYear(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getAtmosphereManyYear",
    method: "post",
    json: true,
    data: data,
  });
}
// 组织氛围 -- 多组织单年度
export function getAtmosphereByManyOrgan(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getAtmosphereManyOrgan",
    method: "post",
    json: true,
    data: data,
  });
}

// 人才池 getTalentPoolTree
export function getTalentPoolTree() {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getTalentPoolTree",
    method: "post",
  });
}

// 人才池穿透详情 -- 传人才池id
export function talentPoolDetail(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getTalentPoolPanel",
    method: "post",
    json: true,
    data: data,
  });
}
// 全员统计列表获取
export function talentAllStaff(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getPenetratorsAll",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取筛选枚举
export function talentQuery(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/query",
    method: "post",
    json: true,
    data: data,
  });
}

// 获取穿透人员根据结果落位 -- 人员列表
export function PenetratorsByPositionNumber(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getPenetratorsByPositionNumber",
    method: "post",
    json: true,
    data: data,
  });
}
// 获取穿透人员列表根据人才池 -- 人员列表
export function PenetratorsByPool(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/getPenetratorsByPool",
    method: "post",
    json: true,
    data: data,
  });
}

// 单组织梯队情况人员穿透 -- 人员列表
export function OneOrgEchelonTalentFile(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/work/getOneOrgEchelonTalentFile",
    method: "post",
    json: true,
    data: data,
  });
}

// 多组织梯队情况人员穿透
export function MoreOrgEchelonTalentFile(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/work/getMoreOrgEchelonTalentFile",
    method: "post",
    json: true,
    data: data,
  });
}

// 多组织梯队情况在岗人员穿透
export function MoreOrgOnJobPersonTalentFile(data) {
  return service({
    baseURL: import.meta.env.VITE_PREFIX_TALENT,
    url: "/tdc/work/getOnJobPersonTalentFile",
    method: "post",
    json: true,
    data: data,
  });
}

//////////
// 个人发展计划 //
//////////

// 1、获取所有人才档案数据
export function getAlltTalents(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/getTalentFile",
    method: "post",
    json: true,
    data: data,
  });
}

// 2、个人发展计划列表
export function getDevPlanList(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/getList",
    method: "post",
    json: true,
    data: data,
  });
}

// 3、个人发展计划详情
export function getDetails(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/detail",
    method: "post",
    json: true,
    data: data,
  });
}
// 個人發展計劃  員工縂自評提交
export function submitEvaluate(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "idpOper/empEvaluation",
    method: "post",
    json: true,
    data: data,
  });
}

// 4、个人发展计划--查看任务评价详情
export function getEvaluate(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/evaluate",
    method: "post",
    json: true,
    data: data,
  });
}
// 5、个人发展计划 -- 获取批量评价数据
export function batchEvaluate(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/batchEvaluate",
    method: "post",
    json: true,
    data: data,
  });
}
// 6、批量评价保存操作 batchEvaluationSave
export function batchEvaluationSave(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/batchEvaluationSave",
    method: "post",
    json: true,
    data: data,
  });
}
// 7、完成情况评价 -- 提交
export function finishEvaluation(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/finishEvaluation ",
    method: "post",
    json: true,
    data: data,
  });
}
// 8、审批不通过
export function approvalFail(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/approvalFail",
    method: "post",
    json: true,
    data: data,
  });
}
// 9、审核通过
export function approvalAdopt(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/approvalAdopt",
    method: "post",
    json: true,
    data: data,
  });
}
// 查看评价
export function getEvaluateForm(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idp/getEvaluateForm",
    method: "post",
    json: true,
    data: data,
  });
}
// 弹窗评价
export function submitFormEvaluate(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/formEvaluate",
    method: "post",
    json: true,
    data: data,
  });
}
// 10 自评
export function selfEvaluation(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/selfEvaluation",
    method: "post",
    json: true,
    data: data,
  });
}

// 11、评价人确定评价
export function evaluatorEvaluation(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/evaluatorEvaluation",
    method: "post",
    json: true,
    data: data,
  });
}

// 12、导师评价  mentorEvaluation
export function mentorEvaluation(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/mentorEvaluation",
    method: "post",
    json: true,
    data: data,
  });
}

//13、列表界面进入自评/评价人确认获取数据
export function EvaluateByList(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/evaluateByList",
    method: "post",
    json: true,
    data: data,
  });
}

// 14、设置评价补充人提交
export function addReplenishers(data) {
  return service({
    // baseURL: '/xyhr',
    baseURL: import.meta.env.VITE_PREFIX_PLAN,
    url: "/idpOper/addSupplement",
    method: "post",
    json: true,
    data: data,
  });
}
