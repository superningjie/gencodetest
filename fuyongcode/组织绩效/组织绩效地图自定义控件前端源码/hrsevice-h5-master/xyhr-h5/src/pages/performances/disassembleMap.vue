<template>
  <div class="page">
    <TeamGoal v-if="isShow" />
    <IndividualGoal v-else />
  </div>
</template>

<script>
import TeamGoal from "@/pages/performances/teamGoal.vue";
import IndividualGoal from "@/pages/performances/individualGoal.vue";
import { getTargetPlan } from "@/libs/api.js";
export default {
  name: "DisassembleMap",
  components: {
    TeamGoal,
    IndividualGoal,
  },
  data() {
    return {
      isShow: false,
    };
  },
  created() {
    this.initData();
  },
  methods: {
    initData() {
      getTargetPlan().then((res) => {
        this.isShow = res.data.data ? false : true;
      });
    },
  },
};
</script>

<style lang="less" scoped></style>
