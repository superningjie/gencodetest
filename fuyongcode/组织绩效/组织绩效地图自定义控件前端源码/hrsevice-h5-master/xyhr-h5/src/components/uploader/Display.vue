<template>
  <!--展示文件列表；暂时仅供出差、加班、请假的自由态，审批中，已办结  共计6个页面使用-->
  <ul class="container">
    <li class="no-data" v-if="fileList && fileList.length === 0">
      <span>未添加</span>
    </li>
    <li v-for="(item, index) in fileList" :key="index">
      <span @click="download(item.url || '', item.fileName || '下载文件')">{{
        item.fileName
      }}</span>
    </li>
  </ul>
</template>

<script>
export default {
  name: "FileListDisplay",
  props: ["fileList"],
  methods: {
    download(fileUrl, fileName) {
      return false
      if (fileName === "") return false;
      let link = document.createElement("a"); //创建a标签
      link.style.display = "none"; //将a标签隐藏
      link.href = fileUrl; //给a标签添加下载链接
      link.setAttribute("download", fileName); // 此处注意，要给a标签添加一个download属性，属性值就是文件名称 否则下载出来的文件是没有属性的，空白白
      document.body.appendChild(link);
      link.click(); //执行a标签
    },
  },
};
</script>

<style scoped lang="less">
ul {
  min-height: 37px;
  flex: 1;
  padding-left: 24px;
  box-sizing: border-box;
  width: 100%;
  li {
    width: 100%;
    color: #cf3633;
    display: flex;
    justify-content: flex-end;
    height: 37px;
    line-height: 37px;
    font-size: 14px;
    span {
      max-width: 100%;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      line-height: 37px !important;
    }
  }
  li.no-data {
    color: #999;
  }
}
</style>
