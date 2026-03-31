/** @format */

import { defineConfig, loadEnv } from "vite" // 动态配置函数
import { createVuePlugin } from "vite-plugin-vue2"
import path from "path"

export default defineConfig(({ mode }) => {
  return {
    base: loadEnv(mode, process.cwd()).VITE_BASE,
    plugins: [createVuePlugin()],
    resolve: {
      // 别名
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    },
    server: {
      host: "0.0.0.0",
      port: "8083",
      open: true,
      hmr: {
        overlay: false,
      },
      proxy: {
        '^/xyhr-nh5': {
          target: loadEnv(mode, process.cwd()).VITE_BASE_URL,
          changeOrigin: true,
          ws: true,
          // rewrite: (path) => {
          // 	return path.replace(/^\/xyhr-nh5/, "/");
          // }
        },
        // '^/portal': {
        // 	target: loadEnv(mode, process.cwd()).VITE_PORTAL_URL,
        // 	changeOrigin: true,
        // 	ws: true,
        // 	rewrite: (path) => {
        // 		return path.replace('/api', '/')
        // 	}
        // },
        "^/kapi": {
          target: loadEnv(mode, process.cwd()).VITE_BASE_URL,
          changeOrigin: true,
          ws: true,
        },
        // 添加新增模块代理
        '^/xyhr': {
          target: loadEnv(mode, process.cwd()).VITE_BASE_URL,
          changeOrigin: true,
          ws: true,
          rewrite: (path) => {
            return path.replace(/^\/xyhr/, "/");
          }
        },
        // '^/webroot': {
        //   target: "http://10.16.8.97:8080/",
        //   changeOrigin: true,
        //   // 因为sit为自造证书，关闭代理ssl校验
        //   secure: false,
        //   ws: true,
        //   rewrite: (path) => {
        //     return path.replace(/^\/webroot/, "webroot")
        //   }
        // },
      },
    },
  }
})
