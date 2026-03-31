<template>
	<label class="user" style="display: block;">
		<van-cell class="user">
			<template #title>
				<div class="left flex middle">
					<van-image
						:src="baseurl + user.img"
						fit="contain"
						width="26" height="26" round
					>
						<template v-slot:error>
							<van-image
								:src="defaultImg"
								fit="fill"
								width="26" height="26" round
							/>
						</template>
					</van-image>
					<input type="checkbox" style="display: none;" name="userspicker" :value="user.lastname">
					<h5>{{user.lastname}}</h5>
				</div>
			</template>
			<template #right-icon>
				<p class="dept">{{user.departmentname}}</p>
			</template>
		</van-cell>
	</label>
</template>

<script>
	export default {
		name: 'user',
		data() {
			return {
				baseurl: 'http://10.16.16.104/',
				defaultImg: import('@/assets/cardbg.jpg'),
			}
		},
		props: {
			user: {type: Object}
		},
		components: {},
		created() {

		},
		methods: {

		}
	}
</script>
