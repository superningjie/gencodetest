<template>
	<div class="voiceItem">
		<van-icon name="delete-o" class="voiceDelete" v-if="isDelete" @click.stop="voiceDelete(voiceData)" />
		<h1 class="title" :style="isDelete?'padding-right: 20px;':''">{{voiceData.title}}</h1>
			<div class="flex creator" style="position:relative;">
				<div class="face">
					<van-image
						lazy-load
						round
						fit="fill"
						width="14px"
						height="14px"
						:src="voiceData.image?voiceData.image:getAssetsFile('/KHCFDC.svg')"
					></van-image>
				</div>
				<p>{{voiceData.creator}}</p>
				<span>{{voiceData.deptName.length>7?voiceData.deptName.substr(0,7)+'...':voiceData.deptName}}｜{{voiceData.postname && voiceData.postname.length>7?voiceData.postname.substr(0,7)+'...':voiceData.postname}}</span>
				<span style="position:absolute;right:0;" v-if="voiceType != 'SquareVoiceList'" class="myred">{{voiceData.voiceType}}</span>
			</div>
			<div class="digest">
				{{voiceData.content}}
			</div>
			<div class="info flex justify">
				<span class="date">{{voiceData.createTime}}</span>
				<ul class="flex">
					<li><i class="iconfont">&#xe687;</i><span>{{voiceData.readCount}}</span></li>
					<li><i class="iconfont">&#xe681;</i><span>{{voiceData.commCount}}</span></li>
					<li @click.stop="likeClick(voiceData)" :class="voiceData.haveLiked?'myred':''"><i class="iconfont">&#xe680;</i><span>{{voiceData.likeCount}}</span></li>
				</ul>
			</div>
	</div>
</template>

<script>
	import {
		psnVoiceLike,
		psnVoiceCancelLike
	} from '@/libs/api.js';
	import {Toast} from 'vant';
	export default {
		name: 'voiceItem',
		data() {
			return {

			}
		},
		components: {},
		props:{
			voiceData:{
				type:Object,
				default:{}
			},
			voiceType:{
				default:"",
				type:String
			},
			isDelete:{
				default:false,
				type:Boolean
			}
		},
		created() {

		},
		methods: {
			likeClick(item){
				if(item.haveLiked){
					psnVoiceCancelLike({
						pk:item.psnVoicePK
					}).then(res=>{
						if(res.data.statusCode == 200){
							Toast("取消点赞成功")
							this.$emit("likenum",-1,item.psnVoicePK)
						}
					})
				}else{
					psnVoiceLike({
						pk:item.psnVoicePK
					}).then(res=>{
						Toast("点赞成功")
						this.$emit("likenum",1,item.psnVoicePK)
					})
				}
			},
			voiceDelete(item){
				this.$emit('voiceDelete',item)
			}
		}
	}
</script>
