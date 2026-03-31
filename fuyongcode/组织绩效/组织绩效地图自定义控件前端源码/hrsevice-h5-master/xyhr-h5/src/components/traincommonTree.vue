<template>
    <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }">
	<van-nav-bar
			:title="title"
			left-arrow
			class="navStyle" 
			@click-left="isshowFilterPicker(false)"
		/>
		<van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
			<van-tab title="行政架构" :name="'xz'">
				<traincommonTreeChild :name="'xz'" v-if="orgsCommonList.length>0" :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
			</van-tab>
		</van-tabs>
    
       </van-popup>
    </div>
</template>
<script>

import {
		orgsManagerTree
	} from '@/libs/api.js';
    
	import traincommonTreeChild from '@/components/traincommonTreeChild';
    export default {
        name:"commonTree",
        data(){
            return{
                activeTab:"xz",
                orgsCommonList:[],
                data:{
                    retireFlag:false
                },
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
            }
        },
        components:{traincommonTreeChild},
        created(){
            this.init()
            this.orgsCommon()
        },
        methods:{
            init(){

            },
            orgsCommon(){
				orgsManagerTree(this.data).then(res=>{
                        this.orgsCommonList=res.data.data
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