<!-- @format -->

<template>
  <div class="login">
    <div class="box my-wobble">
      <div class="title">欢迎来到人力自助</div>
      <img class="chenchen" src="@/assets/home/chenchen-login.png" />
    </div>
  </div>
</template>

<script>
export default {
  name: "login",
}
</script>
<style lang="less" scoped>
.login {
  height: 100vh;
  background: #fff;
  overflow: hidden;
  text-align: center;
  position: relative;
  .box {
    position: relative;
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    margin: 0 auto;
    text-align: center;
  }
  .title {
    color: #000000;
    font-size: 24px;
    text-align: center;
    margin-top: 50px;
  }
  .chenchen {
    width: 130px;
    display: block;
    margin: 0 auto;
    position: relative;
    top: 30%;
  }
}

.my-wobble {
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
