<template>
    <div class="administrativeFramework">
    <van-popup v-model="showFilterPicker" position="top" :style="{ height: '100%' }"
			get-container="body">
	<van-nav-bar
			title="选择部门"
			left-arrow
			class="navStyle" 
			@click-left="isshowFilterPicker(false)"
		/>
		<van-tabs class="hideTabTitle" animated v-model="activeTab" :lazy-render="false">
			<van-tab title="行政架构" :name="'xz'">
				<resumecommonTreeChild :name="'xz'" v-if="orgsCommonList.length>0" :orgsCommonList="orgsCommonList" @clickSearch="clickSearch" />
			</van-tab>
		</van-tabs>
    
       </van-popup>
    </div>
</template>
<script>

import {
		trainCommonList
	} from '@/libs/api.js';
    
	import resumecommonTreeChild from '@/components/resumecommonTreeChild';
    export default {
        name:"mycommonTree",
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
        },
        components:{resumecommonTreeChild},
        created(){
            this.orgsCommon()
        },
        methods:{
            orgsCommon(){
				trainCommonList(this.data).then(res=>{
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