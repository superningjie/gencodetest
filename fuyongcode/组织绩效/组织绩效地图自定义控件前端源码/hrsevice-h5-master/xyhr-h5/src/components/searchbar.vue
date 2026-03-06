<template>
	<div :class="'searchbar '+(isFocus?'focus':'')">
		<div class="logo">
			<i class="iconfont">&#xe67f;</i>
		</div>
		<div class="input">
			<van-icon name="search" />
			<input type="text" 
				placeholder="输入关键字" 
				v-model="keyword" 
				@focus1="isFocus=true" 
				@focus="goSearchPage" 
				@blur="searchBlur"
				ref="keyword"
			/>
			<input 
				type="button"
				value="搜索"
				v-show="isFocus"
				@click="search"
			/>
		</div>
		<div class="scaner">
			<van-icon name="scan" />
		</div>
	</div>
</template>

<script>
	import { Toast } from 'vant';
	export default {
		name: "searchbar",
		data () {
			return {
				isFocus: false,
				keyword: null
			}
		},
		methods: {
			search() {
				let _this = this;
				if(this.keyword) {
					console.log(this.keyword);
				}else{
					Toast('搜索关键词不能为空');
				}
			},
			searchBlur() {
				let _this = this;
				setTimeout(function() {
					_this.isFocus = false;
				},50);
			},
			goSearchPage() {
				window.location.href="http://10.16.16.104/spa/esearch/static4mobile/index.html#/esearch?type=ALL";
			}
		}
	}
</script>