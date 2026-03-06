<template>
  <transition>
    <div v-show="show" :class="`message-container message-container-${type}`">
      <div>{{ msg }}</div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'Message',
  props: {
    type: {
      type: String,
      default: 'success'
    },
    msg: {
      type: String
    },
    show: {
      type: Boolean
    }
  }
}
</script>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: all 1s;
}

.v-enter,
.v-leave-to {
  opacity: 0;
}

.message-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 400px;
  padding: 5px 20px;
  border-radius: 4px;
  z-index: 9999;
  color: #ffffff;
  background-color: #fff;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.message-container-success {
  background-color: blue;
}

.message-container-error {
  background-color: red;
}
</style>
