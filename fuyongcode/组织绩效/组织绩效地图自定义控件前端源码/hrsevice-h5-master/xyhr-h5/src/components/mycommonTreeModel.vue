<template>
    <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }"
			get-container="body" :lazy-render="false">
	<van-nav-bar
			:title="title"
			left-arrow
			class="navStyle"
			@click-left="isshowFilterPicker(false)"
		>
        <template #right>
                <!-- <van-button type="danger" size="mini" @click="clickSearch({id:'',name:$store.state.userData.orgName})">重置</van-button> -->
            </template>
        </van-nav-bar>
		<van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
			<van-tab title="行政架构" :name="'xz'">
				<mycommonTreeModelChild ref="mycommonTreeModelChild" :name="'xz'" v-if="orgsCommonList.length>0" :noDet='noDet' :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
			</van-tab>
		</van-tabs>

       </van-popup>
    </div>
</template>
<script>

import {
		orgsAuthTreeAdminorgmodel
	} from '@/libs/api.js';
	import {Toast} from 'vant';

	import mycommonTreeModelChild from '@/components/mycommonTreeModelChild';
    export default {
        name:"mycommonTreeModel",
        data(){
            return{
                activeTab:"xz",
                orgsCommonList:[],
                data:{
                    retireFlag:false,
                    scope:this.scope
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
            scope:{
                type:Number,
                default:2
            },
            title:{
                type:String,
                default:'选择部门'
            },
            noDet:{
                type:Boolean,
                default:false
            },
            model:{
                type:String,
                default:"costAnaly",
            }
        },
        components:{mycommonTreeModelChild},
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
				orgsAuthTreeAdminorgmodel(this.data,this.model).then(res=>{
                    if(res.data.statusCode == 200){
                        Toast.clear()
                        this.orgsCommonList=res.data.data || []
                        if(res.data.data && res.data.data.length==1 && res.data.data[0].children){
                            this.$nextTick(()=>{
                                this.$refs.mycommonTreeModelChild.gonextPage(res.data.data[0])
                            })
                        }
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
