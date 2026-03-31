<template>
    <div class="fulltextSearch" style="height: 100%;">
        <van-nav-bar
			left-arrow
			class="navStyle"
            title="全文搜索"
			@click-left="goback"
		/>
        <div class="group">
            <van-search
				class="searchItem"
				shape="round"
                v-model="data.keyword"
                clearable
				placeholder="输入关键字搜索"
                @search="onsearch"
			/>
        </div>
        <div class="group" style="border-bottom: 8px solid #f5f5f5;" v-if="fullTextData.psnData && data.searchType=='all'">
            <h2>人员&nbsp;&nbsp;</h2>
            <div class="flex middle proplefull" @click="viewEmployeeDetail(item)" v-for="(item,index) in fullTextData.psnData" :class="index+1==3?'proplefulllast':''"
            v-if="index<3">
                <van-image
                    :src="item.photo?item.photo:KHCFDC"
                    round
                    width="36px" height="36px"
                    fit="fill"
                />
                <div class="info">
                    <h1>{{item.name}} | {{item.postName}} | {{item.postLevel}}</h1>
                    <p style="color:#999999;font-size:12px;margin-bottom:8px;line-height:20px;">{{item.orgName}} | {{item.deptName}}</p>
                    <p style="color:#999999;font-size:12px;" v-for="highlight in item.highlightList" v-html="'['+highlight.fieldComment+'] '+highlight.content"></p>
                </div>
            </div>
            <div class="filltextMore" v-if="fullTextData.psnData.length > 3" @click="fulltextmore('psn')">
                <van-image
                    :src="sousuo"
                    round
                    width="20px" height="20px"
                    fit="fill"
                    style="margin-right:10px;"
                />
                <span>查看更多“人员”搜索结果</span>
                <van-icon name="arrow" class="arrowRight" />
            </div>
        </div>
        <div class="group" style="border-bottom: 8px solid #f5f5f5;" v-if="fullTextData.orgData && data.searchType=='all'">
            <h2 style="margin-top:20px; padding-bottom:20px;">组织&nbsp;&nbsp;({{fullTextData.orgCount}}个)</h2>
            <div class="filltextMore" v-for="(orgdata,index) in fullTextData.orgData"
            v-if="index<3"
             @click="gonextPage(orgdata)">
                <van-image class="iconFramework" :src="xingzhengjiagou" /> {{orgdata.name}}
            </div>
            <div class="filltextMore"  v-if="fullTextData.orgCount > 3" @click="fulltextmore('org')">
                <van-image
                    :src="KHCFDC"
                    round
                    width="20px" height="20px"
                    fit="fill"
                    style="margin-right:10px;"
                />
                <span>查看更多“组织”搜索结果</span>
                <van-icon name="arrow" class="arrowRight" />
            </div>
        </div>
        <div class="group" v-if="fullTextData.deptData && data.searchType=='all'">
            <h2 style="margin-top:20px; padding-bottom:20px;">部门&nbsp;&nbsp;({{fullTextData.deptCount}}个)</h2>
            <div class="filltextMore" v-for="(deptdata,index) in fullTextData.deptData"
            v-if="index<3"
             @click="gonextPage(deptData)">
                <van-image class="iconFramework" :src="xingzhengjiagou" /> {{deptdata.name}}
            </div>
            <div class="filltextMore" v-if="fullTextData.deptCount > 3" @click="fulltextmore('dept')">
                <van-image
                    :src="sousuo"
                    round
                    width="20px" height="20px"
                    fit="fill"
                    style="margin-right:10px;"
                />
                <span>查看更多“部门”搜索结果</span>
                <van-icon name="arrow" class="arrowRight" />
            </div>
        </div>

        <div class="group" style="border-bottom: 8px solid #f5f5f5;" v-if="fullTextData.psnData && data.searchType=='psn'">
            <h2>人员&nbsp;&nbsp;</h2>
            <van-list
				v-model="loading"
				:finished="finished"
				finished-text="没有更多了"
				@load="onLoad"
                :immediate-check="false"
				>
            <div class="flex middle proplefull" @click="viewEmployeeDetail(item)" v-for="(item,index) in fullTextData.psnData" :class="index+1==fullTextData.psnData.length?'proplefulllast':''">
                <van-image
                    :src="item.photo?item.photo:KHCFDC"
                    round
                    width="36px" height="36px"
                    fit="fill"
                />
                <div class="info">
                    <h1>{{item.name}} | {{item.postName}} | {{item.postLevel}}</h1>
                    <p style="color:#999999;font-size:12px;margin-bottom:8px;line-height:20px;">{{item.orgName}} | {{item.deptName}}</p>
                    <p style="color:#999999;font-size:12px;" v-for="highlight in item.highlightList" v-html="'['+highlight.fieldComment+'] '+highlight.content"></p>
                </div>
            </div>
            </van-list>
        </div>

        <div class="group" v-if="fullTextData.orgData && data.searchType=='org'">
            <h2 style="margin-top:20px; padding-bottom:20px;">组织&nbsp;&nbsp;({{fullTextData.orgCount}}个)</h2>
            <van-list
				v-model="loading"
				:finished="finished"
				finished-text="没有更多了"
				@load="onLoad"
                :immediate-check="false"
				>
            <div class="filltextMore" v-for="(orgdata,index) in fullTextData.orgData" @click="gonextPage(orgdata)">
                <van-image class="iconFramework" :src="xingzhengjiagou" /> {{orgdata.name}}
            </div>
            </van-list>
        </div>

        <div class="group" v-if="fullTextData.deptData && data.searchType=='dept'" @click="gonextPage(deptData)">
            <h2 style="margin-top:20px; padding-bottom:20px;">部门&nbsp;&nbsp;({{fullTextData.deptCount}}个)</h2>
             <van-list
				v-model="loading"
				:finished="finished"
				finished-text="没有更多了"
				@load="onLoad"
                :immediate-check="false"
				>
            <div class="filltextMore" v-for="(deptdata,index) in fullTextData.deptData">
                <van-image class="iconFramework" :src="xingzhengjiagou" /> {{deptdata.name}}
            </div>
            </van-list>
        </div>
    </div>
</template>

<script>
import KHCFDC from '@/assets/KHCFDC.svg'
import xingzhengjiagou from '@/assets/icon_xingzhengjiagou_20.png'
import sousuo from '@/assets/icon_sousuo_20.png'
import {
		resumefulltext
	} from '@/libs/api.js'
    export default {
        name:"fulltextSearch",
        data(){
            return{
                fulltext:"",
                data:{
                    pageNum:1,
                    pageSize:10,
                    keyword:"",
                    jobStatus:'ALL',
                    searchType:"all",
                },
                fullTextData:{},
                loading:false,
                finished:false,
                KHCFDC:KHCFDC,
                xingzhengjiagou:xingzhengjiagou,
                sousuo:sousuo,
            }
        },
        methods:{
            goback(){
                this.$emit("showresume",false)
            },
            init(){
                resumefulltext(this.data).then(res=>{
                    this.fullTextData=res.data.data
                })
            },
            onsearch(){
                this.finished=false
                this.data.pageNum=1
                this.data.searchType="all"
                this.init()
            },
            onLoad(){
                this.data.pageNum++,
                this.initMore()
            },
            initMore(){
                resumefulltext(this.data).then(res=>{
                    let data=[]
                    if(this.data.searchType=="psn"){
                        data=res.data.data.psnData
                        this.fullTextData.psnData=[...this.fullTextData.psnData,...res.data.data.psnData]
                    }else if(this.data.searchType=="org"){
                        data=res.data.data.orgData
                        this.fullTextData.orgData=[...this.fullTextData.orgData,...res.data.data.orgData]
                    }else if(this.data.searchType=="dept"){
                        data=res.data.data.deptData
                        this.fullTextData.deptData=[...this.fullTextData.deptData,...res.data.data.deptData]
                    }
                    this.loading=false
                    if(data.length < this.data.pageSize){
                        this.finished=true
                    }
                })
            },
            fulltextmore(val){
                this.loading=false
                this.finished=false
                this.data.searchType=val
            },
            viewEmployeeDetail(employee){
                this.$router.push({
					name: 'employeeInfo',
					query:{
						pkPsndoc:employee.pk,
						goback:-1
					}
				})
            },
            gonextPage(item){
                let data=Object.assign({titleList:[{name:"组织架构"},item],listIndex:this.name},item)
                console.log(data)
                this.$router.push({
                    name:"orgStructure",
                    query:data
                })
            }
        }
    }
</script>
