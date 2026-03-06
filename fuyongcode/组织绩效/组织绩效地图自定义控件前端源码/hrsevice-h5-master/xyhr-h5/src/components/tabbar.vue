<template>
	<div class="tabbar sticky">
		<van-tabbar v-model="active" @change="onChangeTab" class="mainMenu">
			<template v-for="item in items">
				<van-tabbar-item badge1="3" :key='item.title'>
					<span>{{item.title}}</span>
					<template #icon="props">
						<i class="iconfont" v-html="item.icon"></i>
					</template>
				</van-tabbar-item>
			</template>
		</van-tabbar>
	</div>
</template>

<script>
	export default {
		name: 'tabbar',
		data() {
			return {
				active: null,
				items: [
					{
						title: '工作台',
						icon: '&#xe676;',
						path: ''
					},
					{
						title: '屿信',
						icon: '&#xe68a;',
						path: ''
					},
					{
						title: 'E-LINK',
						icon: '&#xe68b;',
						path: ''
					},
					{
						title: '通讯录',
						icon: '&#xe686;',
						path: ''
					},
					{
						title: '我的',
						icon: '&#xe689;',
						path: ''
					}
				]
			}
		},
		components: {},
		props: {
			activeBar: {
				type: Number,
				default: null
			},
			badge: {
				type: Number
			}
		},
		mounted() {
			this.active = this.activeBar;
		},
		methods: {
			onChangeTab(index) {
				if(index==0) {
					this.$router.push({path: '/selfhelp/'})
				}
				// this.$emit('onchange', index);
				// console.log(index);
			}
		}
	}
</script>