function getJumpPath() {
	const env = import.meta.env.MODE
	if (env === 'development') {
		return 'http://hr-dev.xiangyu.cn/ierp/?byPageId=root4d18a7a6773340158ddef21ac9a53044&preview=true'
	} else if (env === 'production' || env === 'uat') {
		return 'https://hr-dev.xiangyu.cn'
	} else {
		return 'https://hr-sit.xiangyu.cn/mobile.html?form=hrom_mob_hsscportalroute#/page/a7b1eec4581144aeaa73635384159c2c/float/bff7933a8f83435eb2c71fcdb0761c59/float/297bf11ace914a7b82366adb288a11d5'
	}
}

export const JUMP_PATH = getJumpPath()
