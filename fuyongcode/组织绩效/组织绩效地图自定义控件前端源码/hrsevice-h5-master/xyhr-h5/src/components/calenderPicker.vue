<template>
	<div :class="'calender ' + name">
		<div class="head" v-html="displayDate()">
			<!-- {{displayDate()}} -->
		</div>
		<div class="switcher" >
			<div class="prev" @click="prevYear">
				<van-image style="width:16px;height:16px;" :src="prev"></van-image>
			</div>
			<div class="prev" @click="prevMonth">
				<van-icon name="arrow-left" />
			</div>
			<div class="center">{{year}} 年 {{month<9?'0'+month:month}} 月</div>
			<div class="next" @click="nextMonth">
				<van-icon name="arrow" />
			</div>
			<div class="next" @click="nextYear">
				<van-image style="width:16px;height:16px;" :src="next"></van-image>
			</div>
		</div>
		<div :class="'monthPage single'">
			<ul class="title">
				<li v-for="i in data.weektitle"><span>{{i}}</span></li>
			</ul>
			<ul class="dates">
				<li v-for="i in data.beginWeekdays" class="disable" @click="prevMonth">
					<span :class="inRange((data.lastMonthDays - data.beginWeekdays + i), -1)">
						{{data.lastMonthDays - data.beginWeekdays + i}}
					</span>
				</li>
				<li v-for="i in data.thisMonthDays" @click="pick(i)" :class="day==i?'active':''">
					<span :class="inRange(i, 0)">
						{{i}}
					</span>
				</li>
				<li v-for="i in data.daysFromNextMonth" class="disable" @click="nextMonth">
					<span :class="inRange(i, 1)">{{i}}</span>
				</li>
			</ul>
		</div>
	</div>
</template>

<script>
import prev from '@/assets/prev.svg'
    import next from '@/assets/next.svg'
	export default {
		name: 'calender',
		data() {
			return {
				prev:prev,
                next:next,
				year: null,
				month: null,
				day: null,
				pickDate: [],
				data: {
					weektitle: ['日', '一', '二', '三', '四', '五', '六'],
					daysInMonth: [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],
					beginWeekdays: null,
					lastWeekdays: null,
					lastMonthDays: null,
					thisMonthDays: null,
					daysFromLastMonth: null,
					daysFromNextMonth: null
				}
			}
		},
		components: {},
		props: {
			name: {type: String, default: ()=> {return 'default';}},
			date: {type: String},
			readonly: {type: Boolean}
		},
		watch: {
			'date': function() {
				this.onMounted();
			}
		},
		mounted() {
			this.onMounted();
		},
		methods: {
			pick(day) {
				// 重新拾取日期
				this.pickDate[0] = `${this.year}-${this.month>10?this.month:"0"+this.month}-${day>9?day:"0"+day}`;
				this.day = day;
				this.$emit('onpick', this.pickDate[0]);
			},
			prevYear(){
				this.year-=1;
				this.initCalender();
			},
			nextYear(){
				this.year+=1;
				this.initCalender();
			},
			prevMonth() {
				if(this.month==1) {
					this.year-=1;
					this.month = 12;
				}else{
					this.month-=1;
				}
				this.initCalender();
			},
			nextMonth() {
				if(this.month==12) {
					this.year+=1;
					this.month = 1;
				}else{
					this.month+=1;
				}
				this.initCalender();
			},
			inRange(day, monthHandle) {
				return;
				if(!this.pickRange) {
					return;
				}
				var year = this.year;
				var month = this.month + monthHandle;
				if(month<1) {
					month = 12; year = this.year-1;
				}
				if(month>12) {
					month = 1; year = this.year+1;
				}
				var min = new Date(this.pickDate[0]);
				var max;
				if(this.pickDate[1]) {
					max = new Date(this.pickDate[1]);
				}else{
					max = new Date(this.pickDate[0]);
				}
				let test = new Date(`${year}-${month}-${day}`);
				if(min<=test&&test<=max) {
					if(this.pickDate[0]==this.pickDate[1]) { return 'inRange oneDay';}
					if(this.pickDate[0]==`${year}-${month}-${day}`) {
						if(!this.pickDate[1]) {
							return 'inRange startDate wait';
						}
						return 'inRange startDate';
					}
					if(this.pickDate[1]==`${year}-${month}-${day}`) {
						return 'inRange endDate';
					}
					return 'inRange';
				}else{
					return;
				}
			},
			onMounted() {
				// 获取日历初始化的时间，如果没有输入时间，则默认为今天。
				if(this.date) {
					var dateArray;
					if(this.date.indexOf('/')>0) {
						dateArray=this.date.split('/');
					}else if(this.date.indexOf('-')>0) {
						dateArray=this.date.split('-');
					}
					this.year = parseInt(dateArray[0]);
					this.month = parseInt(dateArray[1]);
					this.day = parseInt(dateArray[2]);
				}else{
					const date = new Date();
					this.year = date.getFullYear();
					this.month = date.getMonth() + 1;
					this.day = date.getDate();
				}
				this.initCalender();
			},
			initCalender() {
				// 判断当前所在是不是闰年，如果是，则更新2月日期
				if((this.year%4===0&&this.year%100!==0) || this.year%400===0) {
					this.data.daysInMonth[1] = 29;
				}else{
					this.data.daysInMonth[1] = 28;
				}
				// 上月剩余天数和本月天数
				let lastMonthDays = this.month-1==0?31:this.data.daysInMonth[this.month-2];
				let thisMonthDays = this.data.daysInMonth[this.month-1];
				// 计算起始日期是周几和从上月补齐的天数
				let beginWeekdays = new Date(`${this.year}/${this.month}/1`).getDay();
				let daysFromLastMonth = beginWeekdays===7?0:beginWeekdays;
				// 本月最后一天是周几和从下月补齐的天数
				let lastWeekdays = new Date(`${this.year}/${this.month}/${this.data.daysInMonth[this.month - 1]}`).getDay();
				let daysFromNextMonth = lastWeekdays===7?6:6-lastWeekdays;
				this.data.lastMonthDays = lastMonthDays;
				this.data.thisMonthDays = thisMonthDays;
				this.data.lastWeekdays = lastWeekdays;
				this.data.beginWeekdays = beginWeekdays;
				this.data.daysFromLastMonth = daysFromLastMonth;
				this.data.daysFromNextMonth = daysFromNextMonth;
			},
			displayDate() {
				//<h5></h5>
				return `<h1>${this.year}年${this.month}月${this.day}日</h1>`;
			}
		}
	}
</script>
