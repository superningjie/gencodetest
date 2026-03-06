<template>
  <!--  仅供出差、请假、加班 三个页面的上传附件使用-->
  <div class="flex spe">
    <div class="flex row opt">
      <div class="iconfont upload-icon">&#xe6ae;</div>
      <van-uploader
        class="btn-uploader"
        accept=".doc,.docx,image/*,.txt"
        v-model="fileList"
        :preview-image="false"
        :after-read="afterRead"
      >
        <van-cell title="添加附件">
          <template #right-icon>
            <van-icon
              name="add"
              class="hl"
              size="20"
              style="line-height: 22px"
            />
          </template>
        </van-cell>
      </van-uploader>
    </div>
    <ul class="pre-box">
      <li v-for="(item, index) in previewList" :key="index">
        <span>{{ item.fileName || "文件" }}</span>
        <van-icon
          class="clear-icon"
          name="clear"
          @click="deleteFile(item, index)"
        />
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "Uploader",
  data() {
    return {
      // 需要上传的文件列表
      fileList: [],
      // 预览的文件列表
      previewList: [],
      // 需要删除的文件列表
      delList: [],
    };
  },
  props: [
    // 初始化的list
    "defList",
  ],
  watch: {
    fileList: function (val) {
      this.$emit("afterRead", val);
    },
    defList: function (val) {
      this.previewList = [...this.previewList, ...val];
    },
  },
  methods: {
    deleteFile(item, index) {
      // 判断是外部传入数组还是新加数组
      // 外部数组
      if (item.nodePath) {
        this.delList.push(item.nodePath);
        this.$emit("deleteFile", this.delList);
      } else {
        // 删除传出文件列表接口对应的项
        this.fileList = this.fileList.filter(
          (child) => child.file.lastModified !== item.file.lastModified
        );
      }
      // 删除视图数组的内容
      this.previewList.splice(index, 1);
    },
    afterRead(a) {
      this.previewList.push({
        file: a.file,
        fileName: a.file.name,
      });
    },
    clear(){
      console.log('Uploader-clear')
      this.fileList = []
      this.previewList = []
      this.delList = []
    }
  },
};
</script>

<style scoped lang="less">
.spe {
  flex-direction: column;
  .opt {
    .btn-uploader {
      flex: 1;
    }
    /deep/ .van-uploader {
      width: 100%;
    }
    /deep/ .van-uploader__wrapper {
      width: 100%;
    }
    /deep/ .van-uploader__input-wrapper {
      width: 100%;
    }
  }
  .pre-box {
    display: flex;
    flex-direction: column;

    li {
      color: rgb(207, 54, 51);
      font-size: 14px;
      display: flex;
      height: 38px;
      align-items: center;
      justify-content: right;
      span {
        display: inline-block;
        height: 100%;
        line-height: 38px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 40px;
      }
      .clear-icon {
        color: #cf3633;
        font-size: 20px;
        padding-left: 16px;
        flex-shrink: 0;
      }
    }
    flex: 1;
  }
}
.upload-icon {
  color: #999;
  display: flex;
  align-items: center;
  margin-right: 10px;
}
.van-cell {
	padding: 8px 0;
}
.van-cell::after{
  border-bottom:none;
}
</style>
