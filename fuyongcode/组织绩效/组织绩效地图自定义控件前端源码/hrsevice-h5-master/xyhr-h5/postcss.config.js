const autoprefixer = require('autoprefixer')
const mobile = require('postcss-mobile-forever')
module.exports = {
	plugins: [
		autoprefixer(),
		mobile({
			rootSelector: '#app',
			viewportWidth: 375,
			// maxDisplaywidth: 760, //最大宽度
			// disableDesktop: false, // 关闭桌面端媒体查询disableLandscape: true，// 关闭横屏媒体查询
            rootContainingBlockSelectorList: ["van-tabbar",'van-popup', 'van-popup--righ','ivu-drawer-mask','top-bg']
		}),
	],
}
