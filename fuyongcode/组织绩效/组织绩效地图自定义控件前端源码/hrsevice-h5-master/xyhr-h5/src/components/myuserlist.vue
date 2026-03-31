<template>
	<div class="userlist myuserlist">
		<!-- 顶部菜单 -->
		<van-nav-bar
			:title="title"
			left-arrow
			class="navStyle"
			@click-left="goBack"
		>
			<template #right>
				<!-- <van-button @click="confirmUsers" type="primary" style="height:30px;background-color:#CF3633;border-color:#CF3633;border-radius: 4px;">确定</van-button>
				<van-icon name="success" color="#000" size="22" @click="confirmUsers" /> -->
			</template>
		</van-nav-bar>
		<div class="container">
			<van-search
				class="searchItem"
				shape="round"
				v-model="searchKeyword"
				placeholder="搜索姓名"
				@search="onSearch"
                @cancel="onCancel"
			/>
			<div class="group chooseArr" v-if="chooseArr.length>0" style="border-bottom:0;">
				<van-badge v-for="(item,n) in chooseArr" :key="item.id">
					<div class="child">{{item.name}}</div>
					<template #content>
						<van-icon @click="delPerson(n)" name="cross" class="badge-icon" />
					</template>
				</van-badge>
			</div>

            <div class="group" style="margin-top:0;" v-if="firstSearch">
				<div class="title flex middle">
					<i class="iconfont green">&#xe69f;</i>
					<h1>搜索结果</h1>
				</div>
				<template v-if="searching">
					<van-loading size="24px">正在检索人员...</van-loading>
				</template>
				<template v-else>
					<template v-if="searchlist.length>0">
						<template v-for="(user) in searchlist">
							<div>
								<template>
                                    <label class="user" style="display: block;">
                                        <van-cell class="user">
                                            <template #title>
                                                <div class="left flex middle" @click="confirmUser(user)">
                                                    <van-image
													round
													width="32px"
													height="32px"
													fit="fill"
													v-if="user.type=='psn'"
													:src="user.image?user.image:KHCFDC"/>
                                                    <h5 style="width:calc(100% - 60px)">{{user.name}}</h5>
                                                </div>
                                            </template>
                                            <template #right-icon>
                                                <p class="dept" style="width:60%;text-align:right;">{{user.postname}}</p>
                                            </template>
                                        </van-cell>
                                    </label>
                                </template>
							</div>
						</template>
					</template>
					<template v-else>
						<xy-empty description="没有符合条件的人员"></xy-empty>
					</template>
				</template>
			</div>
		</div>
	</div>
</template>

<script>
	import { Toast } from 'vant';
	import {
		queryOrgDeptOrPsn,searchGeneralInfo
	} from '@/libs/api.js';
	import KHCFDC from "@/assets/KHCFDC.svg"
	export default {
		name: 'userlist',
		props:{
			title:{type:String,default:'选择人员'},
			initArr:{
				type:String,
			},
			plist:{
				type:Array
			}
		},
		data() {
			return {
				defaultImg: import('@/assets/cardbg.jpg'),

				searchKeyword: null,
				firstSearch: false,
				newlylist: [],
				deptPsnList: [],
				searchlist: [],
				companys: [],
				usedCompanys: [],
                zIndex:16,
				searching: false,
				showNewly: true,
				showSameDept: false,
				showGroup: false,
				showOrg: false,

				isloading:false,
				chooseArr:[],
				curItem:[],
				prevNum:0,
				prevName:[],
				prevPk:[],
				prevType:[],
				resumeRecord:[],
				KHCFDC:KHCFDC
			}
		},
		components: {},
		created() {

		},
		methods: {
			onSearch() {
                if(this.searchKeyword.match(/^[ ]*$/)){
                    return
                }
                this.searchKeyword=this.searchKeyword.trim();
                this.searching = true;
				this.firstSearch = true;
				let queryData = {
					pageNum:1,
                    pageSize: 20000,
					"searchKey": this.searchKeyword
				}
				queryOrgDeptOrPsn(queryData).then(res=> {
					this.searchlist = res.data.data.content;
					this.searching = false;
					var self = this;
				});
			},
            confirmUser(user){
                console.log(user)
                let data={
                    name:user.name,
                    id:user.pk
                }
                this.$emit("pick",data)
            },
            onCancel () {
				this.firstSearch = false;
            },
            confirmUsers() {
				this.$emit('pickuser', this.chooseArr);
                this.$emit('closeList', true);
			},
            goBack() {
				this.$emit('closeList', true);
			}
		}
	}
</script>
