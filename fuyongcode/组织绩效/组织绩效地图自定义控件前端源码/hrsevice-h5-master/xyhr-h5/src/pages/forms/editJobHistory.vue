<template>
	<div class="myProfile editJobHistory">
		<van-nav-bar
			:title="'编辑'+title"
			left-arrow
			class="navStyle"
			@click-left="goback"
		/>
		<van-cell-group class="group">
			<van-cell :title="item.name"  v-for="item in info.fields" v-if="item.showFlag=='Y'"  is-link  :required="item.mustFlag=='Y'">
				<template #right-icon>
					<template v-if="item.dataType==3 || item.dataType==5 || item.dataType==6  || item.dataType==20 || item.dataType==4  || item.dataType==20 || item.dataType==4 || item.dataType==101  || item.dataType==102 || item.dataType==100">
						<span @click="popupClick(item)" v-if="item.canEditFlag =='Y'" style="display:inline-block;max-width:50%;">
							{{item.showValue==""?"请选择":item.showValue}}
						</span>
						<span v-else-if="item.canEditFlag =='N'" :class="item.canEditFlag =='Y'?'':'mygray'">
							{{item.showValue==""?"请选择":item.showValue}}
						</span>
						<van-icon  name="arrow" style="line-height:24px;" v-if="item.canEditFlag =='Y'" :class="item.canEditFlag =='Y'?'':'mygray'" />
					</template>
					<template v-else>						
							<van-field v-model="item.value" :type="item.dataType==1?'digit':item.dataType==2?'number':'text'" :readonly="item.canEditFlag !='Y'" placeholder="请填写" @input="oninput(item)" />
					</template>
				</template>
			</van-cell>
			<van-popup
			v-model="showUserlist"
			:style="{ width: '100%', height: '100%' }"
			position="right"
		>
			<myuserlist
				@closeList="showUserlist=false" 
				@pick="pick"
				:initArr='person'
				:plist='plist'
				v-if="showUserlist"
			/>
		</van-popup>
		</van-cell-group>
		<div class="bottombtn sticky">
			<div class="flex">
				<button v-if="parentInfo.canDeleteFlag && parentInfo.canDeleteFlag=='Y'" @click="deleteData()">删除</button>
				<button @click="confirm()">保存</button>
			</div>
		</div>

		<van-popup
			v-model="showForm"
			:style="{'width': '100%', 'height': '100%'}"
			position="right"
		>
			<textFormComp
				:title="formTitle"
				:defaultText="defaultText"
				@exit="showForm=false"
				@done="doneEdit"
			/>
		</van-popup>
		<van-popup v-model="popupshow"
				position="bottom">
			<van-picker
				title=""
				show-toolbar
				value-key="name"
				:columns="columns"
				@confirm="onConfirm"
				@cancel="onCancel"
				/>
		</van-popup>
		<van-popup
			v-model="showPicker"
			class="pickerPopup"
			:overlay-style="{'background': 'rgba(120,120,120,.5)'}"
		>
			<van-search
				shape="round"
				v-if="pickList.length>20 ||  keyword.length>0"
				style="padding:0;"
                v-model="keyword"
				:clearable="false"
				placeholder="输入关键字搜索"
                @search="onsearch"
			/>
			<div style="max-height:500px;overflow-y:scroll;width:100%;">
			<template v-for="item in datas.pickData">
				<label @click="pick(item)">
					<input type="radio"  :value="item.id" v-model="radioValue" name="tempPicker" />
					<div class="flex middle">
						<i></i>
						<span>{{item.name}}</span>
					</div>
				</label>
			</template>
			</div>
		</van-popup>

		<van-popup
			v-model="showCalender"
			class="calenderPop"
			:overlay-style="{'background': 'rgba(120,120,120,.5)'}"
		>
			<Calendar
				:default-date="new Date(thisdate)"
				:is-show-week-view="false"
				:mark-date="markDate"
				v-if="showCalender"
				@confirm="dateConfirm"
			></Calendar>
		</van-popup>

		

		<profiletree v-if="showFilterPicker" :showFilterPicker="showFilterPicker" @isshowFilterPicker="isshowFilterPicker" @clickSearch="clickSearch" :columns="profileTree" />		
	</div>
</template>

<script>
import { Dialog,Toast } from 'vant';
	import {clone,
		getReference,personalInfoEdit,dateFormat} from '@/libs/api.js';
	import profiletree from "@/components/profiletree"
	export default {
		name: 'editJobHistory',
		data() { 
			return {
				 defaultDatetime: new Date(),
      markDate: [],
				showUserlist:false,
				keyword:"",
				pickList:[],
				popupshow:false,
				showFilterPicker:false,
				showForm: false,
				showPicker: false,
				showCalender: false,
				formTitle: null,
				defaultText: null,
				editKey: null,
				columns:[],
				person:'',
				plist:[],
				thisdate:dateFormat('YYYY-mm-dd',new Date()),
				datas: {
					pickData: [],
					cshy: [
						{label: '计算机', value: '计算机'},
						{label: '教育', value: '教育'},
						{label: '销售', value: '销售'},
					],
					cs: [
						{label: '测试', value: '测试'},
						{label: '1', value: '1'},
						{label: '2', value: '2'},
					]
				},
				form: {
					place: null,
					dept: null,
					job: null,
					cshy: null
				},
				title:"",
				info:{},
				editDataList:[],
				profileTree:[],
				code:"",
				parentInfo:"",
				initList:[{
					id:"Y",
					name:"是"
				},{
					id:"N",
					name:"否"
				}],
				radioValue:"",
				toast:"",
				isdatehide:true,
				ispipstate:false
			}
		},
		watch :{
			"showUserlist":{
				deep: true,
				handler:function(newValue, oldValue){
					if(newValue){
						history.pushState(null, null, document.URL);
					}else{
						if(!this.ispipstate){
							this.goback()
						}
						this.ispipstate=false
					}
				}
			}
		},
		components: {
			textFormComp: ()=>import('@/pages/forms/textFormComp'),
			calender: ()=>import('@/components/calenderPicker'),
			myuserlist: ()=>import('@/components/myuserlist'),
			profiletree
		},
		created() {
			this.title=this.$route.params.title
			this.info=JSON.parse(JSON.stringify(this.$route.params.info))
			this.parentInfo=this.$route.params.parentInfo
            let _this=this
			window.addEventListener('popstate', function () {
				if(_this.showUserlist){
					_this.ispipstate=true
					_this.showUserlist=false
					return
				}
			});
		},
		methods: {
			dateConfirm(date) {
				if(this.isdatehide){
					this.info.fields.map(item=>{
					if(item.code==this.code){
						item.showValue=date.year+'-'+(date.month<9?'0'+(date.month+1):date.month+1)+'-'+(date.day<10?'0'+date.day:date.day)
						item.value=date.year+'-'+(date.month<9?'0'+(date.month+1):date.month+1)+'-'+(date.day<10?'0'+date.day:date.day)
						item.alterFlag='Y'
						this.showCalender = false;
					}
				})
				}else{
					this.isdatehide=true
				}
				
			},
			pickuser(user){

			},
			oninput(val){
				console.log(val)
				val.alterFlag='Y'
				console.log(this.info)
			},
			popupClick(value){
				if(value.code == "jobglbdef8" || value.code == "jobglbdef9"){
					this.code=value.code
					this.showUserlist=true
					return
				}
				this.toast=Toast.loading({
					duration: 0,
					forbidClick: true,
					message: '加载中',
				});
				if(value.value){
					this.radioValue=value.value
				}else{
					this.radioValue=''
				}
				if(!this.editDataList[value.code]){
					this.code=value.code
				if(value.canEditFlag=="Y" && value.showFlag=="Y"){
						let datas={}
					if(value.dataType==5){
						datas={
							dataType:value.dataType,
							pkRefInfo:value.pkRefInfo,
							refModel:value.refModel,
						}
						this.getReference(datas,value.code)
					}else if(value.dataType==6){
						datas={
							dataType:value.dataType,
							refModel:value.refModel,
						}
						this.getReference(datas,value.code)
					}else if(value.dataType==20 || value.dataType==3 || value.dataType==101  || value.dataType==102 || value.dataType==100){
						this.toast.clear()
						this.showCalender=true
							this.isdatehide=true
						if(value.value && value.value !=''){
							this.thisdate=value.value
						}
						
					}else if(value.dataType==4){
						this.editDataList[this.code]={references:this.initList,dataType:'LIST'}
						this.showlistortree(this.code)
					}
				}
				}else{	
					this.showlistortree(value.code)
				}
			},
			showlistortree(code){
				this.code=code
				this.keyword=''
				if(this.editDataList[code].dataType=='LIST'){
					// this.columns=this.editDataList[code].references
					// this.popupshow=true
					 this.datas.pickData=JSON.parse(JSON.stringify(this.editDataList[code].references))
					 this.pickList=JSON.parse(JSON.stringify(this.editDataList[code].references))
					 this.showPicker=true
				}else if(this.editDataList[code].dataType=='TREE'){
					this.profileTree=this.editDataList[code].references
					this.showFilterPicker=true
				}
				this.toast.clear()
			},
			onsearch(){
				let data=[]
				for(let i=0;i<this.pickList.length;i++){
					if(this.pickList[i].name.indexOf(this.keyword) != -1){
						data.push(this.pickList[i])
					}
				}
				this.datas.pickData=data
			},
			getReference(datas,code){
				getReference(datas).then(res=>{
					this.editDataList[code]=res.data.data;
					this.showlistortree(code)	
				})
			},
			isshowFilterPicker(value){
				this.showFilterPicker=value
			},
			clickSearch(val){
				console.log(val)
				this.info.fields.map(item=>{
					if(item.code==this.code){
						item.showValue=val.name
						item.value=val.id
						item.alterFlag='Y'
						this.showFilterPicker=false
					}
				})
			},
			goback() {this.$router.go(-1)},
			dv(key,pick=false) {
				// display values 显示值，key为form下的键。
				// 如果pick为真，则是选择，显示必选，否则显示必填
				if(this.form[key]) {
					return this.form[key];
				}else{
					if(pick=='hide') {return '';}
					return pick?'必选':'必填';
				}
			},
			pk(key, datakey) {
				if(key!=this.editKey) this.datas.pickData = [];
				setTimeout(_=> {
					this.editKey = key;
					this.datas.pickData = clone(this.datas[datakey]);
					this.showPicker = true;
				},10)
			},
			pick(val) {
				this.info.fields.map(item=>{
					if(item.code==this.code){
						item.showValue=val.name
						item.value=val.id
						item.alterFlag='Y'
					}
				})
				this.showUserlist=false
				this.showPicker = false;
			},
			fl(key, title) {
				this.editKey = key;
				this.formTitle = title;
				if(this.form[key]) {
					this.defaultText = this.form[key];
				}
				this.showForm = true;
			},
			confirmDate(val) {
				console.log(val)
				this.info.fields.map(item=>{
					if(item.code==this.code){
						item.showValue=val
						item.value=val
						item.alterFlag='Y'
						this.showCalender = false;
					}
				})
				
			},
			doneEdit(val) {
				this.form[this.editKey] = val;
				this.showForm = false;
			},
			onConfirm(val){
				console.log(this.code)
				this.info.fields.map(item=>{
					if(item.code==this.code){
						console.log(item.showValue)
						item.showValue=val.name
						item.value=val.id
						item.alterFlag='Y'
						this.popupshow=false
					}
				})
			},
			onCancel(){
				this.popupshow=false
			},
			confirm(){
				Dialog.confirm({
					title: '修改确认',
					message: '修改需经过HR审批，请耐心等候',
					})
					.then(() => {
						let num=0
						this.info.fields.map(item=>{
							if(item.mustFlag=='Y' && item.canEditFlag=='Y'  && (!item.value || item.value=='')){
								num++
							}
						})
						console.log(num)
						if(num > 0){
							Toast("有必填项未填")
							return
						}
						this.parentInfo.records.map(item=>{
							if(item.recordId == this.info.recordId){
								item.editType=1;
								item.fields=JSON.parse(JSON.stringify(this.info.fields))
							}
						})	
						this.isedit()
					})
					.catch(() => {
						// on cancel
				});
			},
			deleteData(){
				Dialog.confirm({
					title: '删除确认',
					message: '是否确定删除该条信息？',
					})
					.then(() => {
						this.parentInfo.records.map(item=>{
							if(item.recordId == this.info.recordId){
								item.editType=3;
							}
						})	
						this.isedit()
					})
					.catch(() => {
						// on cancel
				});
			},
			isedit(){
				console.log(this.parentInfo)
				personalInfoEdit({infos:[this.parentInfo]}).then(res=>{
					console.log(res.data)
					this.$store.commit("setisNeedNewData",this.parentInfo.code);
					this.goback()
				})
			}
		}
	}
</script>