<template>
    <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }"
			get-container="body">
	<van-nav-bar
			title="选择事业部"
			left-arrow
			class="navStyle" 
			@click-left="isshowFilterPicker(false)"
		>
         <template #right>
                <!-- <van-button type="danger" size="mini" @click="clickSearch({id:'',name:$store.state.userData.deptName})">重置</van-button> -->
            </template>       
        </van-nav-bar>
		<van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
			<van-tab title="事业部" :name="'xz'">
				<businessTreeChild :name="'xz'" v-if="orgsCommonList.length>0" :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
			</van-tab>
		</van-tabs>
    
       </van-popup>
    </div>
</template>
<script>

import {
		orgsAuthTreeBusiness
	} from '@/libs/api.js';
	import {Toast} from 'vant';
    
	import businessTreeChild from '@/components/businessTreeChild';
    export default {
        name:"businessTree",
        data(){
            return{
                activeTab:"xz",
                orgsCommonList:[],
                data:{
                    retireFlag:false
                },
                ispipstate:false,
                init:true
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
        props:{
            showFilterPicker:{
                type:Boolean,
                default:false
            },
        },
        components:{businessTreeChild},
        created(){
            let _this=this
			window.addEventListener('popstate', function () {
				if(_this.showFilterPicker){
					_this.ispipstate=true
					_this.isshowFilterPicker(false)
					return
				}
			});
        },
        methods:{
            orgsCommon(){
                Toast.loading({
                    duration: 0,
                    forbidClick: true,
                    message: '加载中',
                });
              let param = {
                params: {
                  pk: 100000,
                },
              }
				// orgsAuthTreeBusiness(this.data).then(res=>{
              orgsAuthTreeBusiness(param).then(res=>{
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