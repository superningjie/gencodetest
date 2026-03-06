<!-- @format -->

<template>
  <div class="login">
    <div class="title my-wobble">欢迎来到人力自助</div>
    <img class="chenchen my-flash" src="@/assets/home/chenchen-login.png" />
  </div>
</template>

<script>
import { Toast } from "vant"
import { getPsnInfo } from "@/libs/api.js"
import jsCookie from "js-cookie"
export default {
  name: "login",
  mounted() {
    // this.login()
    console.log(11111111111111111111111)
  },
  methods: {
    login() {
      const search = window.location.href.split("?")
      const params = "?" + (search[1] || "")
      // 缓存sid，用于跳转第三方链接的token获取
      jsCookie.set("sid", this.$route.query.sid)
      this.$router.replace({ path: "/selfhelp/" })
    },
  },
}
</script>
<style lang="less">
.login {
  height: 100vh;
  background: linear-gradient(180deg, rgba(216, 12, 30, 0.09) 0%, rgba(238, 28, 64, 0) 100%);
  overflow: hidden;
  text-align: center;
}
.title {
  color: #000000;
  font-size: 24px;
  text-align: center;
  margin-top: 50px;
}
.chenchen {
  width: 140px;
  margin-top: 100px;
}
.my-wobble{
  animation-name: wobble;
  animation-duration: 2s;
  animation-iteration-count: infinite;
}
.my-flash {
  animation-name: flash;
  animation-duration: 2s;
  animation-iteration-count: infinite;
}

@keyframes flash {
  0%,
  50%,
  100% {
    opacity: 1;
  }

  25%,
  75% {
    opacity: 0.6;
  }
}
@keyframes wobble {
	0% {
		transform: translateX(0);
	}

	15% {
		transform: translateX(-40px) rotate(-10deg);
	}

	30% {
		transform: translateX(30px) rotate(5deg);
	}

	45% {
		transform: translateX(-20px) rotate(-5deg);
	}

	60% {
		transform: translateX(10px) rotate(3deg);
	}

	75% {
		transform: translateX(-5px) rotate(-1deg);
	}

	100% {
		transform: translateX(0);
	}
}
</style>
