<template>
	<div :class="'groupAvatar ' + (avatars.length>4?'multi':'')">
		<template v-for="(avatar,index) in avatars">
			<div class="avatar">
				<i :style="'background-image: url('+avatar+')'" v-if="index<9"></i>
			</div>
		</template>
	</div>
</template>

<script>
	export default {
		name: 'groupAvatar',
		props: {
			avatars: {
				type: Array
			}
		},
		mounted() {
			
		}
	}
</script>