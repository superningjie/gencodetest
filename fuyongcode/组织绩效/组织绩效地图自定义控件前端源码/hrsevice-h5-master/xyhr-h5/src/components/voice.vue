<template>
    <div>
        <xy-empty v-if="dataList.length==0 && finished==true"></xy-empty>
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <van-list
            v-model="loading"
            :finished="finished"
            :finished-text="dataList.length>0?'没有更多了':''"
            @load="onLoad"
            :immediate-check="false">
                <div class="container">            
                    <div v-for="(item,index) in dataList" @click="voiceDetail(item)" :key="index">
                        <voiceItem :voiceData="item" @likenum="likenum" :voiceType="voiceType" :isDelete="isDelete" @voiceDelete="voiceDelete" />
                    </div>  
                </div>
            </van-list>
        </van-pull-refresh>
    </div>
</template>

<script>
import {
		psnVoiceQueryVoiceListByPage,
        psnVoicedelVoice,
        cload,
        commcount,
        csave
	} from '@/libs/api.js';
    import { Dialog,Toast } from 'vant';
    export default {
        name:"voice",
        data(){
            return {
                initCondition:{
                    pageSize:5,
                    pageNum:1,
                    voiceType:this.voiceType,
                    type:this.condition.voicecode,
                    userId:"",
                    dataType:this.voiceType=="MyVoiceList"?'1':''
                },
                loading:false,
                finished:true,
                refreshing:false,
                dataList:[]
            }
        },
        props:{
            condition:{
                type:Object,
                default:{},
            },
            voiceType:{
                type:String,
                default:"",
            },
            isDelete:{
                default:false,
				type:Boolean
            },
            activeType:{
                default:"",
				type:String
            }
        },
        components: {
			voiceItem: ()=>import('@/components/voiceItem'),
		},
        created(){
            Toast.loading({
                message:"加载中",
                duration:0
            })
            this.init()
        },
        activated(){
			let pkVoice=cload("pkVoice")
            if(pkVoice){
                this.dataList.map(item=>{
                    if(item.psnVoicePK==pkVoice){
                        csave("pkVoice","", -1)
                        commcount("voice",pkVoice).then(res=>{                         
                            item.commCount=res.data.data
                        })
                        
                    }
                })
                this.$forceUpdate();
            }
            
            
		},
        methods:{
            init(){
                psnVoiceQueryVoiceListByPage(this.initCondition).then(res=>{
                     Toast.clear()
                    this.loading=false;
                    this.refreshing = false;
                    if(res.data.data){
                        if(this.initCondition.pageNum == 1){
                            this.dataList=[]
                        }
                        this.dataList=[...this.dataList,...res.data.data.list]
                        if(res.data.data.pages <= this.initCondition.pageNum){
                            this.finished=true;
                        }else{
                            this.finished=false
                        }
                    }
                    
                }).catch(e=> {
                    Toast.clear()
					this.refreshing = false;
					this.loading = false;
				})
            },
            onLoad(){
                if(this.activeType == this.condition.voicecode || this.activeType ==""){
                    this.initCondition.pageNum++
                    this.init()
                }else{
                    this.loading=false
                }
                
            },
            newVioceRefresh(type){
                console.log(type)
                // if(this.condition.voicecode == type){
                    this.onRefresh()
                // }
            },
            onRefresh(){
                this.initCondition.pageNum=1
                this.init()
            },
            voiceDetail(item) {
				this.$router.push({path: '/selfhelp/voiceDetail',query:{
                    pkVoice:item.psnVoicePK
                }})
			},
            likenum(num,id){
                this.dataList.map(item=>{
                    if(id==item.psnVoicePK){
                        item.likeCount+=num
                        if(num == 1){
                            item.haveLiked=true
                        }else{
                            item.haveLiked=false
                        }
                    }
                })
            },
            voiceDelete(value){
                Dialog.confirm({
                    title: '删除确认',
                    message: '是否确定删除该条消息？',
                    })
                    .then(() => {
                        this.dataList.map((item,index)=>{
                            if(item.psnVoicePK == value.psnVoicePK){
                                psnVoicedelVoice({
                                    pkVoice:item.psnVoicePK
                                }).then(res=>{
                                    if(res.data.statusCode==200){
                                        this.dataList.splice(index,1)
                                    }
                                })
                            }
                        })
                    })
                    .catch(() => {
                        // on cancel
                    });
                
                // this.$emit("voiceDelete",item)
            }
        }
    }
</script>