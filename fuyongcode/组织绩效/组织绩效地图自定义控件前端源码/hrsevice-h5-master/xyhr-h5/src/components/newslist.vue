<template>
	<div class="news_list">
		<template v-if="data.length>0">
			<template v-for="item in data">
				<div class="item">
					<h1 @click="viewDetail(item.url)">{{item.title}}</h1>
					<span @click="viewDetail(item.url)">{{item.date}}</span>
					<ul>
						<li>
							<i class="iconfont">&#xe687;</i>
							<span>{{item.view}}</span>
						</li>
						<li @click="goComment(item.docid)">
							<i class="iconfont">&#xe681;</i>
							<span>{{item.comment}}</span>
						</li>
						<li :class="item.isColute?'hl':''" @click="colute(item)">
							<i class="iconfont">&#xe688;</i>
							<span>{{item.fav}}</span>
						</li>
						<li :class="item.isPraise?'hl':''" @click="praise(item)">
							<i class="iconfont">&#xe680;</i>
							<span>{{item.like}}</span>
						</li>
					</ul>
				</div>
			</template>
			<template v-if="isloadingMore">
				<p class="tac" style="font-size: 14px; line-height: 40px; color: #aaa;">加载中...</p>
			</template>
			<template v-else>
				<p class="tac" style="font-size: 14px; line-height: 40px; color: #aaa;" @click="loadmore">点击加载更多</p>
			</template>
				
		</template>		
		<template v-else>
			<xy-empty></xy-empty>
		</template>
	</div>
</template>

<script>
	import {praiseDoc,coluteDoc} from '@/libs/api.js';
	export default {
		name: 'newslist',
		data() {
			return {
				
			}
		},
		props: {
			data: {
				type: Array,
				default: ()=> {return []}
			},
			isloadingMore: {
				type: Boolean,
				default: ()=> {return false}
			}
		},
		watch: {
			isloadingMore(val, newVal) {
				console.log(val, newVal);
			}
		},
		components: {},
		mounted() {
			let _this = this;
			setTimeout(function() {
				console.log(_this.data);
			}, 1e3)
		},
		methods: {
			loadmore() {
				this.$emit('loadmore');
			},
			praise(item) {
				item.isPraise = item.isPraise?0:1;
				// console.log(item);
				let queryData = {
					docid: item.docid,
					isPraise: item.isPraise?1:0,
					ismobile: 1,
					_ec_ismobile: true,
					_ec_device: 'mobile_ec'
				}
				praiseDoc(queryData).then(res=> {
					console.log('点赞成功');
				})
			},
			colute(item) {
				item.isColute = item.isColute?0:1;
				let queryData = {
					docid: item.docid,
					isColute: item.isColute?1:0,
					ismobile: 1,
					_ec_ismobile: true,
					_ec_device: 'mobile_ec'
				}
				coluteDoc(queryData).then(res=> {
					console.log('收藏成功');
				})
			},
			goComment(id) {
				window.location.href=`/spa/document/static4mobile/index.html#/doc/${id}/comment`;
			},
			viewDetail(path) {
				window.location.href=path;
				// console.log(path);
			}
		}
	}
</script>