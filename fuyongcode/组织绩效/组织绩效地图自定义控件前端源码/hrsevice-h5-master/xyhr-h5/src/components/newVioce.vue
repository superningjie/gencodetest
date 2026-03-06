<template>
	<div>
    <!-- 发布声音弹出 -->
		<van-popup 
			:style="{'width':'100%', 'height': '100%','box-sizing':'border-box'}"
			class="popupForm popupForm1"
			get-container="body"
			v-model="showNewVioceForm"
		>
			<van-nav-bar
				title="发布声音"
				left-arrow
				class="navStyle"
				@click-left="showNewVioceFun(false)"
			/>
			<div class="container">
				<div class="group">
					<div class="title">标题</div>
					<textarea v-model="createForm.title" placeholder="必填，请输入…" maxlength="100"></textarea>
					<div class="tar limit" v-if="createForm.title">{{createForm.title.length}}/100</div>
					<div class="tar limit" v-else>0/100</div>
				</div>
				<div class="group">
					<div class="title">内容</div>
					<textarea v-model="createForm.content" placeholder="必填，请输入…" maxlength="800" rows="5"></textarea>
					<div class="tar limit" v-if="createForm.content">{{createForm.content.length}}/800</div>
					<div class="tar limit" v-else>0/800</div>
				</div>
				<div class="group nopadding">
					<van-cell is-link title="分类" @click="showPicker=true" :value="classifyName"></van-cell>
				</div>
				<div class="group">
					<div class="title">添加图片</div>
					<van-uploader :before-delete="imgdelete" v-model="imgUrl" multiple :max-count="12" />
					<div class="tar limit">
						{{imgUrl.length}}/12
					</div>
				</div>
			</div>
			<div class="bottombtn">
				<div class="flex">
					<button @click="issueVioce(1)">保存草稿</button>
					<button @click="issueVioce(2)">发布声音</button>
				</div>
			</div>
		</van-popup>

		<van-popup
			v-model="showPicker"
			class="pickerPopup"
			:overlay-style="{'background': 'rgba(120,120,120,.5)'}"
		>
			<template v-for="item in pickData">
				<label @click="pick(item)">
					<input type="radio" :value="item.id" name="tempPicker" />
					<div class="flex middle">
						<i></i>
						<span>{{item.name}}</span>
					</div>
				</label>
			</template>
		</van-popup>
		</div>
</template>

<script>
	import {psnVoiceQueryVoiceTypeList,uploadPicture,psnVoicePublishVoice,saveFile,psnVoiceupset,psnVoiceQueryVoiceByKey,getImageByte} from "@/libs/api.js";
	import { Toast } from 'vant';
    export default {
        name:"newVioce",
        data(){
            return {
                createForm: {
					title: '',
					content: '',
					type: '',
					fileInfoList:[]
				},
				imgsList:[],
				pkPsnVoice:"",
				issue:1,
				imgUrl: [],
				classifyName:"必选",
				pickData:[],
				showPicker:false,
				voiceData:{},
				mytoast:null,
				vioceClick:"",
            }
        },
        props:{
            showNewVioceForm:{
                type:Boolean,
                default:false
            }
        },
		created(){
			this.init()
		},
		methods:{
			init(){
				psnVoiceQueryVoiceTypeList({}).then(res=>{
					if(res.data.statusCode == 200){
						this.pickData=res.data.data
					}
				})
			},
			showNewVioceFun(val){
				this.$emit("showNewVioceFun",val)
			},
			classifyFun(){

			},
			issueVioce(issue){
				this.issue=issue
				if(this.createForm.title.trim() == ""){
					Toast("标题不能为空")
					return
				}
				if(this.createForm.content.trim() == ""){
					Toast("内容不能为空")
					return
				}
				if(this.createForm.type==''){
					Toast("请先选择类型")
					return
				}
					if(this.vioceClick !=""){
						return
					}			
					this.vioceClick=setTimeout(()=>{
						this.uploadImg()
					},100)
				
				
			},
			pubilcVoice(values){
				this.createForm.fileInfoList=[...values,...this.imgsList]
				if(this.pkPsnVoice !=""){
					this.createForm.pkPsnVoice=this.pkPsnVoice
				}
				if(this.issue == 1){
					
					psnVoiceupset(this.createForm).then(res=>{
						this.vioceClick=""
						this.mytoast.clear()
						if(res.data.statusCode == 200){
							Toast("保存草稿成功")
							this.showNewVioceFun(false)
							this.$emit("initdata")
							// this.uploadImg(res.data.data)
						}
						
					})	
				}else{
					
					psnVoicePublishVoice(this.createForm).then(res=>{
						this.vioceClick=""
						this.mytoast.clear()
						if(res.data.statusCode == 200){
							Toast("发布成功")
							// this.uploadImg(res.data.data)
							this.showNewVioceFun(false)
							this.$emit("initdata",this.createForm.type)
						}
					})
				}
			},
			uploadImg(){
				console.log(this.imgUrl)
				if(this.issue == 1){
					this.mytoast=Toast.loading({
						message:"保存中",
						duration:0
						})
				}else{
					this.mytoast=Toast.loading({
						message:"发布中",
						duration:0
						})
				}
				
				// return
				const promises=this.imgUrl.map((item,index)=>{
					if(!item.isImage){
						return this.fileList(item)
					}
					
				})
				Promise.all(promises).then(values => {
					values= values.filter((x) => x !== undefined);
					console.log(values);
					if(values.length+this.imgsList.length==this.imgUrl.length){
						this.pubilcVoice(values)
					}
					
				});
			},
			fileList(item){
				var formData = new FormData();
				formData.append('file', item.file);
				return new Promise((resolve, reject) => {
					uploadPicture(formData).then(res=>{
						if(res.data.statusCode == 200){
							resolve(res.data.data)
						}else{
							resolve(null)
						}
						
					})
				})
			},
			imgdelete(file,item){
				if(file.isImage){
					this.imgsList.map((val,index)=>{
						if(val.id==file.id){
							this.imgsList.splice(index,1)
						}
					})
				}
				return true
			},
			pick(item){
				this.classifyName=item.name;
				this.createForm.type=item.voicecode;
				this.showPicker=false;
			},
			caogaoData(item){
				this.createForm={
					title: item.title,
					content: item.content,
					type: '',
				},
				
				this.pkPsnVoice=item.psnVoicePK
				this.classifyName=item.voiceType
				psnVoiceQueryVoiceByKey({pkVoice:item.psnVoicePK}).then(res=>{
					if(res.data.statusCode==200){
						this.voiceData=res.data.data.psnVoiceVo
						this.imgsList=res.data.data.fileList
						if(res.data.data.fileList.length > 0){
							this.selectFile(res.data.data.fileList)
						}
						this.pickData.map(item=>{
						if(this.classifyName == item.name){
							this.createForm.type=item.voicecode
							console.log(this.createForm)
						}
					})
					}else{
						
					}
				})
			
				console.log(item)
			},
			selectFile(data){
				Toast.loading({
					duration: 0,
					forbidClick: true,
					message: '加载中',
				});
				const promises=data.map((item,index)=>{
					return this.imgList(item)
				})
				Promise.all(promises).then(values => {
					Toast.clear()
					this.imgUrl=values
					console.log(this.imgUrl)
				});
					
				
			},
			imgList(item){
				return new Promise((resolve, reject) => {
					getImageByte({url:item.url}).then(res=>{
						if(res.data.statusCode == 200){
							resolve({url:"data:image/png;base64,"+res.data.data,isImage: true,id:item.id})
						}else{
							resolve(null)
						}
						
					})
				})
			},
		}
    }
</script>