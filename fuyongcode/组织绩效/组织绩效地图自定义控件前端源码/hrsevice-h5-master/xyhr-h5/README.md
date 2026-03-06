# Vue 3 + Vite

npm install 
npm run dev
npm run build

## Recommended IDE Setup

- [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=johnsoncodehk.volar)

nvm use v14.17.3
如果失败，进入nvm安装目录，修改settings.txt
修改下列镜像：
node_mirror: http://npmmirror.com/mirrors/node/
npm_mirror: http://npmmirror.com/mirrors/npm/

### npm 安装失败，使用淘宝镜像：
npm install --registry=https://registry.npmmirror.com

### 开发本地调试
先登录OA：
http://10.91.17.16/wui/index.html#/?logintype=1&_key=3flpkb
然后打开另一个页签，访问一下页面进行单点跳转：
http://10.91.17.16/xiangyu/search/oaByC.jsp?url=http://localhost:8083/%23/
需要本地接口调试，修改.env.dev文件里面，这个地址改成本地的
VITE_BASE_URL = 'http://localhost:8080/ierp/'
