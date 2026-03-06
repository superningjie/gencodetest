<template>
	<div class="sign">
		<div class="signPanel">
			<i class="edge topleft"></i>
			<i class="edge topright"></i>
			<i class="edge bottomleft"></i>
			<i class="edge bottomright"></i>
			<span class="centerText">签字区域</span>
			<div class="func">
				<van-button @click="cancelSign" type="default" style="margin-bottom: 5px;">取消签名</van-button><br>
				<van-button @click="reDraw" type="info" style="margin-bottom: 5px;">重新绘制</van-button><br>
				<van-button @click="saveSign" type="primary">确认签名</van-button>
			</div>
			<canvas id="canvas"></canvas>
		</div>
	</div>
</template>

<script>
	export default {
		name: 'sign',
		data() {
			return {
				base64: null,
				quality: 1,
				w: 0, h: 0
			}
		},
		components: {},
		created() {
		},
		mounted() {
			let _this = this;
			setTimeout(_=>{
				_this.init();
			},1000)
		},
		methods: {
			init() {
				let _this = this;
				setTimeout(_=> {
					// 获取容器宽高
					let container = document.getElementsByClassName('signPanel')[0];
					let w = container.offsetWidth;
					let h = container.offsetHeight;
					this.w = w;
					this.h = h;
					// 获取画布
					var canvas = document.getElementById("canvas"),
					drawer = canvas.getContext("2d");
					canvas.width = w; canvas.height = h; // 设置画布宽高
					this.reDraw();
					// 绘制画布事件
					var start,
						touchStartTime,
						isTouch,
						scale=1,
						rotation=0,
						moveX=0,
						moveY=0,
						scaleOffset=0,
						rotationOffset=0,
						moveOffsetX=0,
						moveOffsetY=0,
						touchType,
						tempScale,
						tempRotation,
						tempMove = {x:0, y: 0},
						ofx=0,
						ofy=canvas.offsetTop;

					container.addEventListener('touchstart', e=> {
						touchType=e.touches.length;
						start=e.touches;
						if(touchType==1) {
							var touch = e.touches[0];
							drawer.strokeStyle = "#1F58C3";
							drawer.lineWidth = 1;
							drawer.beginPath();
							drawer.moveTo(touch.clientX-20,touch.clientY-20);
						}
					});
					container.addEventListener("touchmove", e=>{
						if(e.touches.length==1&&touchType==1) {
							var touch = e.touches[0];
							drawer.lineTo(touch.clientX-20,touch.clientY-20);
							drawer.stroke();
						}
					});
					document.addEventListener("touchend",function(e){
						_this.base64 = canvas.toDataURL("image/jpeg", this.quality);
					},false);

				},10)
			},
			cancelSign() {this.$emit('cancelSign')},
			reDraw() {
				let _this = this;
				var canvas = document.getElementById("canvas"),
					drawer = canvas.getContext("2d");
				drawer.clearRect(0,0,canvas.width,canvas.height); 
				drawer.fillStyle="#fff";
				drawer.fillRect(0,0,canvas.width,canvas.height); 
			},
			saveSign() {
				// console.log(this.base64);
				// var zipimg = this.dataURItoBlob(this.base64);
				this.$emit('sign', this.base64);
			}
		}
	}
</script>