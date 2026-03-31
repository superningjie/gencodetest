import store from "@/libs/store.js"
import vue from '@/main'
const {getAssetsFile} = vue
console.log(getAssetsFile('/selfhelpIcons/icon_gerenxinxi_30@2x.png'), 123123)
export function appdata() {
	return {
		staff: [
			{
				id: 'PERSONAL_INFO',
				label: '个人信息',
				icon: getAssetsFile('/selfhelpIcons/icon_gerenxinxi_30@2x.png'),
				path: 'myInfo'
			},
			{
				id: 'SALARY',
				label: '薪酬',
				icon: getAssetsFile('/selfhelpIcons/icon_tuanduixinchou_30@2x.png'),
				path: 'salaryEntry'
			},
			{
				id: 'PERFORMANCE',
				label: '绩效',
				icon: getAssetsFile('/selfhelpIcons/icon_jixiao_30@2x.png'),
				// path: 'achievementChart'
				path: 'myachievementQuery'

			},
			{
				id: 'LEAVE',
				label: '请假',
				icon: getAssetsFile('/selfhelpIcons/icon_qingjia_30@2x.png'),
				path: 'askForLeave'
			},
			{
				id: 'EVECTION',
				label: '出差',
				icon: getAssetsFile('/selfhelpIcons/icon_chuchai_30@2x.png'),
				path: 'askForEvection'
			},
			{
				id: 'ATTENDANCE',
				label: '考勤',
				icon: getAssetsFile('/selfhelpIcons/icon_kaoqin_30@2x.png'),
				path: 'clockin'
			},
			{
				id: 'OVER_TIME',
				label: '加班',
				icon: getAssetsFile('/selfhelpIcons/icon_jiaban_30@2x.png'),
				path: 'askForOvertime'
			},
			{
				id: 'SIGN_CARD',
				label: '补签',
				icon: getAssetsFile('/selfhelpIcons/icon_qianka_30@2x.png'),
				path: 'reClockin'
			},
			{
				id: 'LINE_VOICE',
				label: '一线声音',
				icon: getAssetsFile('/selfhelpIcons/icon_yixianshengyin_30@2x.png'),
				path: 'firstLineVoice'
			},
			{
				id: 'PROOF',
				label: '证明',
				icon: getAssetsFile('/selfhelpIcons/icon_zhengming_30@2x.png'),
				path: 'askForProve'
			},
			{
				id: 'CONTRACT',
				label: '合同',
				icon: getAssetsFile('/selfhelpIcons/icon_hetong_30@2x.png'),
				path: ''
			},
			{
				id: 'CONVERSION',
				label: '转正',
				icon: getAssetsFile('/selfhelpIcons/icon_zhuanzheng_30@2x.png'),
				path: 'askForRegular'
			},
			{
				id: 'DIMISSION',
				label: '离职',
				icon: getAssetsFile('/selfhelpIcons/icon_lizhi_30@2x.png'),
				path: 'askForQuit'
			},
			{
				id: 'CHANGE_SHIFT',
				label: '调班',
				icon: getAssetsFile('/selfhelpIcons/icon_zhaopin_30@2x.png'),
				path: 'askClass'
			},
			{
				id: 'CARE',
				label: '员工关怀',
				icon: getAssetsFile('/selfhelpIcons/yuan_gong_guan_huai.png'),
				path: 'care'
			},
      {
        id: 'EL',
        label: '在线学习',
        icon: getAssetsFile('/selfhelpIcons/icon_zaixianxuexi1_30@2x.png'),
        path: ''
      },
		],
		leader: [
			{
				id: 'EMPLOYEE_RESUME',
				label: '员工简历',
				icon: getAssetsFile('/selfhelpIcons/icon_yuangongjianli_30@2x.png'),
				path: 'resumeQuery'
			},
			{
				id: 'STATISTICAL_ANALYSIS',
				label: '统计分析',
				icon: getAssetsFile('/selfhelpIcons/icon_tongjifenxi_30@2x.png'),
				path: 'statistic'
			},
			{
				id: 'PERFORMANCE_QUERY',
				label: '绩效查询',
				icon: getAssetsFile('/selfhelpIcons/icon_jixiaochaxun_30@2x.png'),
				path: 'leaderCharts'
			},
			{
				id: 'TRAINING_QUERY',
				label: '培训查询',
				icon: getAssetsFile('/selfhelpIcons/icon_peixunchaxun_30@2x.png'),
				path: 'trainQuery'
			},
			{
				id: 'SALARY_QUERY',
				label: '薪酬查询',
				icon: getAssetsFile('/selfhelpIcons/icon_xinchouchaxun_30@2x.png'),
				path: 'leadersalaryEntry'
			},
			{
				id: 'ATTENDANCE_QUERY',
				label: '考勤查询',
				icon: getAssetsFile('/selfhelpIcons/icon_kaoqinchaxun_30@2x.png'),
				path: 'leaderClockIn'
			},
			// {
			// 	id: 'PERSONAL_INFO',
			// 	label: '成本分析',
			// 	icon: getAssetsFile('/selfHelpIcon/icon_chengbenfenxi@2x.png'),
			// 	path: ''
			// },
			{
				id: 'RECRUITMENT',
				label: '招聘查询',
				icon: getAssetsFile('/selfhelpIcons/icon_zhaopinchaxun_30@2x.png'),
				path: 'hiringQuery'
			},
		],
		manager: [
			{
				id: 'MY_TEAM',
				label: '我的团队',
				icon: getAssetsFile('/selfhelpIcons/icon_wodetuandui_30@2x.png'),
				path: 'myTeamInfo'
			},
			{
				id: 'TEAM_ATTENDANCE',
				label: '团队考勤',
				icon: getAssetsFile('/selfhelpIcons/icon_tuanduikaoxin_30@2x.png'),
				path: 'teamClockIn'
			},
			{
				id: 'TEAM_TRAINING',
				label: '团队培训',
				icon: getAssetsFile('/selfhelpIcons/icon_tuanduipeixun_30@2x.png'),
				path: 'teamTrainInfo'
			},
			{
				id: 'TEAM_SALARY',
				label: '团队薪酬',
				icon: getAssetsFile('/selfhelpIcons/icon_tuanduixinchou_30@2x.png'),
				path: 'managesalaryEntry'
			},
			{
				id: 'TEAM_PERFORMANCE',
				label: '团队绩效',
				icon: getAssetsFile('/selfhelpIcons/icon_tuanduijixiao_30@2x.png'),
				path: 'teamChart'
				// path: 'teamAchievements'
			},
		]
	}
}

//水印模块
export function witerWork(){
	return ['salaryEntry','myInfo','leaderInfo','resumeQuery','statistic','leaderCharts','trainQuery','leadersalaryEntry','leaderClockIn','myTeamInfo','teamClockIn','teamTrainInfo','managesalaryEntry','teamChart']
}

export function userInfoIcon() {
	return {
		bd_psndoc: {
			color: "#FFA935",
			text: "&#xe69f;"
		},
		hi_psnjob: {
			color: "#7FCD93",
			text: "&#xe699;"
		},
		hi_psndoc_trial: {
			color: "#7FCD93",
			text: "&#xe6a9;"
		},
		hi_psndoc_psnchg: {
			color: "#7FCD93",
			text: "&#xe6a9;"
		},
		hi_psndoc_work: {
			color: "#00B2EA",
			text: "&#xe6a6;"
		},
		hi_psndoc_edu: {
			color: "#D23B38",
			text: "&#xe6a1;"
		},
		hi_psndoc_family: {
			color: "#FFA500",
			text: "&#xe69b;"
		},
		hi_psndoc_cert: {
			color: "#00B4F0",
			text: "&#xe6a3;"
		},
		hi_psndoc_linkman: {
			color: "#D0332F",
			text: "&#xe69d;"
		},
		hi_psndoc_title: {
			color: "#7FCD93",
			text: "&#xe6a8;"
		},
		hi_psndoc_nationduty: {
			color: "#00B6F6",
			text: "&#xe6a7;"
		},
		hi_psndoc_langability: {
			color: "#7FCD93",
			text: "&#xe6aa;"
		},
		hi_psndoc_enc: {
			color: "#FFA100",
			text: "&#xe6a0;"
		},
		hi_psndoc_glbdef1: {
			color: "#00B8FC",
			text: "&#xe6a4;"
		},
		hi_psndoc_glbdef5: {
			color: "#D0332F",
			text: "&#xe6a5;"
		}
	}
}


export function dendrogram() {
	return {
		title: {
			text: '',
			color: '#020202',
			fontSize: '16px',
			fontWeight: 'normal',
			top: '5%',
			left: '3%'
		},
		grid: {
			top: '20px',
			left: '5%'
		},
		tooltip: {
			trigger: 'item',
			formatter: "{b}: {c} ({d}%)",
			position: "inside"
		},
		legend: {
			orient: 'horizontal',
			x: 'right',
			bottom: "2%",
			left: '4%',

		},
		series: [
			{
				name: '',
				color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#87E8DE', '#6C85A8', '#36CFC9', "#FFAB50", "#FF92C4"],
				type: 'pie',
				radius: ['26%', '40%'],
				center: ['50%', '37%'],
				//center : ['50%','40%'],
				barWidth: 30,
				avoidLabelOverlap: true,
				startAngle: -135,
				label: {
					normal: {
						show: true,
						position: 'outer',
						formatter: '{c}({d}%)\n',
						textStyle: {
							color: ["#000"]
						}
					},
					emphasis: {
						show: true,
						position: 'outer',
						formatter: '{c}({d}%)',
						textStyle: {
							color: ["#000"]
						}
					}
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
				data: [

				]
			}
		]
	}
}

export function waterMarkWrap() {
	if (document.querySelector('.water-mark-wrap')) {

	} else {
		let t = timeFormat(new Date());
		if (document.querySelector('.water-mark-wrap')) {
			document.querySelector('.water-mark-wrap').remove();
		}
		let waterMarkName = store.state.userData.name + ',' + t
		if (!waterMarkName) {
			return;
		}
		let width = window.parseInt(document.body.clientWidth);
		let canvasWidth = width / window.parseInt(width / 120);
		let fontFamily = window.getComputedStyle(document.body)['font-family'];
		const fragment = document.createDocumentFragment();
		let waterMarkDOM = document.createElement('div');
		waterMarkDOM.className = 'water-mark-wrap';
		let spanStr = '';
		for (let i = 0; i < 100; i++) {
			spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(',')[0]}</small><small>${waterMarkName.split(',')[1]}</small></span>`;
		}
		waterMarkDOM.innerHTML = spanStr;
		fragment.appendChild(waterMarkDOM);
		document.body.appendChild(fragment);

	}
}

export function popupMarkWrap() {
	if (document.querySelector('.popup-mark-wrap')) {

	} else {
		let t = timeFormat(new Date());
		if (document.querySelector('.popup-mark-wrap')) {
			document.querySelector('.popup-mark-wrap').remove();
		}
		let waterMarkName = store.state.userData.name + ',' + t
		if (!waterMarkName) {
			return;
		}
		let width = window.parseInt(document.body.clientWidth);
		let canvasWidth = width / window.parseInt(width / 120);
		let fontFamily = window.getComputedStyle(document.body)['font-family'];
		const fragment = document.createDocumentFragment();
		let waterMarkDOM = document.createElement('div');
		waterMarkDOM.className = 'popup-mark-wrap';
		let spanStr = '';
		for (let i = 0; i < 100; i++) {
			spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(',')[0]}</small><small>${waterMarkName.split(',')[1]}</small></span>`;
		}
		waterMarkDOM.innerHTML = spanStr;
		fragment.appendChild(waterMarkDOM);
		document.getElementById("wind").appendChild(fragment);

	}
}

export function trainMarkWrap(id) {

		let t = timeFormat(new Date());
		if (document.querySelector('.popup-mark-wrap')) {
			document.querySelector('.popup-mark-wrap').remove();
		}
		let waterMarkName = store.state.userData.name + ',' + t
		if (!waterMarkName) {
			return;
		}
		let width = window.parseInt(document.body.clientWidth);
		let canvasWidth = width / window.parseInt(width / 120);
		let fontFamily = window.getComputedStyle(document.body)['font-family'];
		const fragment = document.createDocumentFragment();
		let waterMarkDOM = document.createElement('div');
		waterMarkDOM.className = 'popup-mark-wrap';
		let spanStr = '';
		for (let i = 0; i < 100; i++) {
			spanStr += `<span class="water-word" style=width:${canvasWidth}px;height:130px;font: ${fontFamily}><small>${waterMarkName.split(',')[0]}</small><small>${waterMarkName.split(',')[1]}</small></span>`;
		}
		waterMarkDOM.innerHTML = spanStr;
		fragment.appendChild(waterMarkDOM);
		document.getElementById(id).appendChild(fragment);


}

function timeFormat(time) { // 时间格式化 2019-09-08
	let year = time.getFullYear();
	let month = ('0'+(time.getMonth() + 1)).slice(-2);
	let day = ('0'+(time.getDate())).slice(-2);
	return year + '-' + month + '-' + day
}
