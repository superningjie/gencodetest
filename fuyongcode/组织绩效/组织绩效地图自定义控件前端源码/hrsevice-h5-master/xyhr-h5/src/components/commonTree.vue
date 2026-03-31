<template>
    <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }" get-container="body">
	<van-nav-bar
			:title="title"
			left-arrow
			class="navStyle" 
			@click-left="isshowFilterPicker(false)"
		>
            <template #right>
                <!-- <van-button type="danger" size="mini" @click="clickSearch({id:'',name:'全部'})">重置</van-button> -->
            </template>       
        </van-nav-bar>
		<van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
			<van-tab title="行政架构" :name="'xz'">
				<commonTreeChild :name="'xz'" :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
			</van-tab>
		</van-tabs>
    
       </van-popup>
    </div>
</template>
<script>

import {
		orgsCommonTree
	} from '@/libs/api.js';
	import {Toast} from 'vant';
    
	import commonTreeChild from '@/components/commonTreeChild';
    export default {
        name:"commonTree",
        data(){
            return{
                activeTab:"xz",
                orgsCommonList:[],
                ispipstate:false,
                init:true
            }
        },
        props:{
            showFilterPicker:{
                type:Boolean,
                default:false
            },
            title:{
                type:String,
                default:'选择部门'
            },
            scope:{
                type:Number,
                default:2
            }
        },
        watch :{
			"showFilterPicker":{
				deep: true,
				handler:function(newValue, oldValue){
					if(newValue){
                        if(this.init){
                            this.$nextTick(()=>{   
                                this.init=false
                                this.orgsCommon()
                            })
                        }
						history.pushState(null, null, document.URL);
					}else{
						if(!this.ispipstate){
							this.$router.go(-1)
						}
						this.ispipstate=false
					}
				}
			}
		},
        components:{commonTreeChild},
        created(){
            this.orgsCommon()
            let _this=this
            if(this.scope==1){
                window.addEventListener('popstate', function () {
                    if(_this.showFilterPicker){
                        _this.ispipstate=true
                        _this.isshowFilterPicker(false)
                        return
                    }
                });
            }
			
        },
        methods:{
            orgsCommon(){
                Toast.loading({
                    duration: 0,
                    forbidClick: true,
                    message: '加载中',
                });
				orgsCommonTree({retireFlag:true,scope:this.scope}).then(res=> {
					if(res.data.statusCode == 200){
                        Toast.clear()
                        this.orgsCommonList=res.data.data
                    }
				})
			},
            frameClick(e,item){
                e.stopPropagation()
                this.$store.commit("increment",item)
                this.$emit("isshowFilterPicker",false)
            },
            isshowFilterPicker(value){
                this.$emit("isshowFilterPicker",value)
            },
             clickSearch(item){
                this.$emit("clickSearch",item)
            },
        }
    }
</script>