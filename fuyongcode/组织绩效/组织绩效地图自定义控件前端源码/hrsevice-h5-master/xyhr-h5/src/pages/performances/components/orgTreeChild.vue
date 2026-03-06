<template>
  <div>
    <div v-for="i in childrenList">
      <van-collapse-item :title="i.name" :name="i.id">
        <template #right-icon>
          <div @click.stop>
            <van-checkbox
              shape="square"
              :name="i.id"
              ref="checkboxes"
              checked-color="#ee0a24"
            />
          </div>
        </template>
        <OrgTreeChild :childrenList="i.children" v-if="i.children" />
      </van-collapse-item>
    </div>
  </div>
</template>

<script>
export default {
  name: "OrgTreeChild",
  props: {
    childrenList: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      activeNames: [],
    };
  },
  methods: {},
};
</script>

<style lang="less" scoped></style>
