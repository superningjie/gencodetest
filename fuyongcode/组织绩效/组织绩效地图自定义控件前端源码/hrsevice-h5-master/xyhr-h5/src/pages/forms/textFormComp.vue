<template>
	<div class="textFormComp rlform">
		<van-nav-bar
			:title="title"
			left-arrow
			class="navStyle"
			@click-left="goback"
		/>
		<div class="textarea">
			<textarea v-model="text" placeholder="请输入.."></textarea>
			<div class="limit" v-if="text&&maxLength">{{maxLength-text.length}}/{{maxLength}}</div>
		</div>
		<div class="bottombtn sticky">
			<div class="flex">
				<button @click="submitclick">提交</button>
			</div>
		</div>
	</div>
</template>

<script>
	import {clone} from '@/libs/api.js';
	export default {
		name: 'textFormComp',
		data() {
			return {
				text: null
			}
		},
		props: {
			title: {
				type: String,
				default: '未配置title'
			},
			maxLength: {
				type: Number,
				default: 250
			},
			defaultText: {
				type: String,
				default: null
			}
		},
		components: {},
		watch: {
			defaultText(val) {
				this.text = val;
			}
		},
		created() {
			
		},
		methods: {
			goback() {
				this.$emit('exit');
			},
			submitclick() {
				if(this.text) {
					let subStr = this.text.split('').join('');
					this.text = null;
					this.$emit('done', subStr);
				}else{
					this.$emit('done', '');
				}
			}
		}
	}
</script>