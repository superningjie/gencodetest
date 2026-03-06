<template>
  <van-search v-model="value" placeholder="请输入您想搜索的内容" background="#fff"  show-action @search="onSearch">
    <template #action>
        <div class="filter-box" @click="onSearch">
            <div class="filter-text">筛选</div>
            <van-image :src="shaixuan" class="filter-icon"></van-image>
        </div>
    </template>
</van-search>
</template>
<script>
import shaixuan from "@/assets/performances/shaixuan.svg"
export default {
  data() {
    return {
        value:"",
        shaixuan:shaixuan,
    }
  },
  methods: {
    onSearch(){}
  },
}
</script>
<style lang="less" scoped>
/deep/ .van-search__action:active {
    background-color: inherit;
}
.filter-box{
    display: flex;
    justify-content: center;
    flex-wrap: nowrap;
    margin-left: 6px;
    .filter-text{
        padding-left: 12px;
        padding-right:6px;
       border-left: 1px solid #E5E5E5;
       color:#666;
       font-size:14px;
    }
    .filter-icon{
        width:18px;
    }
}
</style>
