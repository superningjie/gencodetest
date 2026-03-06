<template>
	<div class="userlist">
		<!-- 顶部菜单 -->
		<van-nav-bar
			title="选择发起人"	
			left-arrow
			class="navStyle"
			@click-left="goBack"
		>
			<template #right>
				<van-icon name="success" color="#000" size="22" @click="confirmUsers" />
			</template>
		</van-nav-bar>
		<div class="container">
			<van-search
				class="searchItem"
				shape="round"
				v-model="searchKeyword"
				placeholder="搜索姓名"
				@search="onSearch"
			/>
			<div class="group" v-if="firstSearch">
				<div class="title flex middle">
					<i class="iconfont green">&#xe69f;</i>
					<h1>搜索结果</h1>
				</div>
				<template v-if="searching">
					<van-loading size="24px">正在检索人员...</van-loading>
				</template>
				<template v-else>
					<template v-if="searchlist.length>0">
						<template v-for="(user,i) in searchlist">
							<div @click1="pickuser(user)">
								<user :user="user"  />
							</div>
						</template>
					</template>
					<template v-else>
						<xy-empty description="没有符合条件的人员"></xy-empty>
						<div class="tac">
							<van-button @click="firstSearch=false">了解</van-button>
						</div>
					</template>
				</template>
			</div>
			<div class="group companytree" v-if="!firstSearch">
				<div class="title flex middle" @click="showOrg = !showOrg">
					<i class="iconfont green">&#xe6ce;</i>
					<h1>组织架构</h1>
				</div>
				<div v-show="showOrg">
					<template v-for="(com,i) in companys">
						<companytree :comdata="com" @pick="pick"></companytree>
					</template>
				</div>
					
				<!-- 同部门 -->
				<div class="title flex middle"  @click="showSameDept = !showSameDept" style="margin-top: 20px;">
					<i class="iconfont blue">&#xe6ce;</i>
					<h1>同部门</h1>
				</div>
				<div v-show="showSameDept">
					<div class="sub" v-for="(user,i) in deptPsnList">
						<user :user="user"  />
					</div>
				</div>
				
				<!-- 常用组 -->
				<div class="title flex middle" @click="showGroup = !showGroup" style="margin-top: 20px;">
					<i class="iconfont green">&#xe6ce;</i>
					<h1>常用组</h1>
				</div>
				<div v-show="showGroup">
					<template v-for="(com,i) in usedCompanys">
						<companytree :comdata="com" @pick="pick"></companytree>
					</template>
				</div>
					

				<!-- 常用联系人 -->
				<div class="title flex middle" @click="showNewly = !showNewly"  style="margin-top: 20px;">
					<i class="iconfont orange">&#xe69f;</i>
					<h1>常用联系人</h1>
				</div>
				<div v-show="showNewly">
					<div class="sub" v-for="(user,i) in newlylist">
						<user :user="user"  />
					</div>
				</div>
				
			</div>

			<div class="group" v-if="false">
				<div class="title flex middle">
					<i class="iconfont orange">&#xe69f;</i>
					<h1>常用联系人</h1>
				</div>
				<template v-for="(user,i) in newlylist">
					<div @click1="pickuser(user)">
						<user :user="user"  />
					</div>
				</template>
			</div>
		</div>
	</div>
</template>

<script>
	import { Toast } from 'vant';
	import {
		getHandler
	} from '@/libs/api.js';
	export default {
		name: 'userlist',
		data() {
			return {
				searchKeyword: null,
				firstSearch: false,
				newlylist: [],
				deptPsnList: [],
				searchlist: [],
				companys: [],
				usedCompanys: [],
				searching: false,
				showNewly: true,
				showSameDept: false,
				showGroup: false,
				showOrg: false,
			}
		},
		components: {
			user: ()=>import('./user'),
			companytree: {
				name: 'companytree',
				template: `
<div class="sub">
	<template v-if="comdata.type!='resource'">
		<div class="flex middle">
			<h5 @click="querysub">{{comdata.lastname}}</h5>
			<van-loading 
				size="20"
				v-if="isloading"
				color="#CF3633"
				style="margin-left: 5px;"
			/>
		</div>
	</template>
	<template v-else>
		<label class="flex justify">
			<input type="checkbox" name="userspicker" style="display: none;" :value="comdata.lastname" />
			<h5>{{comdata.lastname}}</h5><span style="font-size: 12px; color: #999; line-height: 40px;">{{comdata.jobtitlename}}</span>
		</label>
	</template>
	<template v-for="(com,i) in comdata.children">
		<companytree :comdata="com" @pick="pick" v-show="showSub"></companytree>
	</template>
</div>
				`,
				data() {
					return {
						subQueryed: false,
						showSub: true,
						isloading: false
					}
				},
				props: {
					comdata: Object,
					subarr: Array,
					subid: Number
				},
				methods: {
					querysub() {
						if(this.subQueryed) {
							this.showSub = !this.showSub;
							console.log(this.showSub);
							return;
						}
						let company = this.comdata;
						if(company.isParent=="true") {
							this.isloading = true;
							// 获取下级子公司
							getHandler({
								cmd: 'v2resourcetree',
								type: company.type,
								id: company.id,
								level: company.level
							}).then(res=> {
								this.subQueryed = true;
								this.isloading = false;
								this.comdata.children = res.data.datas;
							});
						}else{
							this.$emit('pick', this.comdata);
						}
					},
					pick(data) {
						this.$emit('pick', data);
					}
				}
			}
		},
		created() {
			this.init();
		},
		methods: {
			confirmUsers() {
				let users = document.getElementsByName("userspicker");
				let arr = [];
				for (var i = 0; i < users.length; i++) {
					if (users[i].checked) arr.push(users[i].value);
				}
				this.$emit('pickuser', arr);
			},
			init() {
				// 获取组织架构
				getHandler({
					cmd: 'v2resourcetree'
				}).then(res=> {
					this.companys = res.data.datas.children;
				});
				// 获取常用组
				getHandler({
					cmd: 'v2grouptree'
				}).then(res=> {
					this.companys = res.data.datas.children;
				});
				// 获取同部门
				getHandler({
					cmd: 'underling'
				}).then(res=> {
					this.deptPsnList = res.data.datas;
				})
				// 获取常用联系人
				getHandler({
					cmd: 'newly'
				}).then(res=> {
					this.newlylist = res.data.datas;
				});
			},
			pick(data) {
				this.$emit('pickuser', data);
			},
			querysub(company, comid) {
				console.log(comid.join('->'));
				if(company.isParent) {
					// 获取下级子公司
					getHandler({
						cmd: 'v2resourcetree',
						type: company.type,
						id: company.id,
						level: company.level
					}).then(res=> {
						var data = this.companys;
						comid.map(i=> {
							data[i].children = res.data.datas;
							data = data[i];
						});
					});
				}
			},
			pickuser(user) {
				this.$emit('pickuser', user);
			},
			onSearch() {
				this.searching = true;
				this.firstSearch = true;
				let queryData = {
					cmd: 'search',
					lastname: this.searchKeyword
				}
				getHandler(queryData).then(res=> {
					this.searchlist = res.data.datas;
					this.searching = false;
				});
			},
			goBack() {
				this.$emit('closeList', true);
			}
		}
	}
</script>
