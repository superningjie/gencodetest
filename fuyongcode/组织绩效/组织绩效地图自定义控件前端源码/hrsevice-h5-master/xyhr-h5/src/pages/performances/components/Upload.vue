<template>
  <div class="tabcontent pd12 mt10">
    <div class="flex justify middle">
      <div class="fs14 c6">{{ label }}</div>
      <van-uploader
        v-model="fileList"
        :preview-image="false"
        :after-read="afterRead"
        v-if="isReadonly"
        :readonly="status == '2'"
      >
        <van-button
          icon="plus"
          size="small"
          :disabled="status == '2'"
        ></van-button>
      </van-uploader>
    </div>
    <!-- 文件列表 -->
    <div
      class="flex fileBox pd12 justify middle mt12"
      v-for="(file, index) in previewList"
      :key="file.id"
    >
      <van-image class="iconWH" :src="file.fileIcon" />
      <div class="fileContent">
        <div class="fileNameBox fs14 ovText">{{ file.fileName }}</div>
        <div class="fs12">
          {{ file.fileDate }} <span class="ml8">{{ file.fileSize }}</span>
        </div>
      </div>
      <van-image
        class="downWH"
        :src="down"
        @click="downFile(file.fileUrl, file.fileName)"
        v-if="status == '2' || !isReadonly"
      />
      <van-icon
        name="clear"
        color="#ee0a24"
        @click="deleteFile(file, index)"
        v-else
      />
    </div>
  </div>
</template>
<script>
import down from "@/assets/performances/down.png";
import pdf from "@/assets/performances/PDF.png";
import ppt from "@/assets/performances/PPT.png";
import excel from "@/assets/performances/excel.png";
import pic from "@/assets/performances/pic.png";
import word from "@/assets/performances/word.png";
import zip from "@/assets/performances/zip.png";
import general from "@/assets/performances/general.png";
import { rateUpload } from "@/libs/api.js";
export default {
  name: "Upload",
  props: {
    label: {
      type: String,
      default: "默认名称",
    },
    defList: {
      type: Array,
      default: [],
    },
    isReadonly: {
      type: Boolean,
      default: false,
    },
    customAreaInfoId: {
      type: String,
      default: "",
    },
    fieldId: {
      type: String,
      default: "",
    },
    // 是否必填
    fieldMustInputItem: {
      type: Boolean,
      default: false,
    },
    // 待办状态
    status: {
      type: String,
      default: "",
    },
  },
  created() {},
  data() {
    return {
      down,
      // 需要上传的文件列表
      fileList: [],
      // 预览的文件列表
      previewList: [],
      // 需要删除的文件列表
      delList: [],
      // 文件对应图标
      fileIconList: [
        {
          icon: pdf,
          type: "pdf",
        },
        {
          icon: ppt,
          type: "ppt",
        },
        {
          icon: excel,
          type: "xls",
        },
        {
          icon: excel,
          type: "xlsx",
        },
        {
          icon: excel,
          type: "xlsm",
        },
        {
          icon: pic,
          type: "pic",
        },
        {
          icon: pic,
          type: "png",
        },
        {
          icon: word,
          type: "docx",
        },
        {
          icon: zip,
          type: "zip",
        },
      ],
      general,
    };
  },
  watch: {
    fileList: function (val) {
      this.$emit("afterRead", val, this.fieldMustInputItem);
    },
  },
  mounted() {
    if (this.defList) {
      this.previewList = this.arrayTransition(this.defList);
      this.$emit("afterRead", this.previewList, this.fieldMustInputItem);
    }
  },
  methods: {
    arrayTransition(obj) {
      const self = this;
      const previewList = obj;
      return previewList.map((item) => {
        const fileIcon = self.fileIcon(item.name);
        return {
          fieldName: this.fieldId,
          fileName: item.name,
          fileSize: item.size,
          fileUrl: item.url,
          fileDate: item.createtime,
          fileIcon: fileIcon,
        };
      });
    },
    fileIcon(type) {
      const fileType = type.split(".")[1];
      const result = this.fileIconList.find((item) => item.type === fileType);
      return result ? result.icon : this.general;
    },
    afterRead(a) {
      // console.log(this.fileList, "this.fileList");

      if (this.fileList.length) {
        this.fileList[this.fileList.length - 1].objcustomareainsId =
          this.customAreaInfoId;
        this.fileList[this.fileList.length - 1].fieldId = this.fieldId;
        this.uploadFile();
      }
    },
    uploadFile() {
      let data = {};
      const list = this.fileList;
      if (list.length) {
        const fileData = list[list.length - 1];
        const base = fileData.content.split(",");
        data = {
          fileBase: base[1],
          fileName: fileData.file.name,
          objcustomareainsId: fileData.objcustomareainsId,
          fields: fileData.fieldId,
        };
      }
      rateUpload(data).then((res) => {
        const loadData = res.data.data.data;
        loadData.fileIcon = this.fileIcon(res.data.data.data.fileName);
        this.previewList.push(loadData);
        this.fileList = this.previewList;
        this.fileList[this.previewList.length - 1].fieldName = this.fieldId;
      });
    },
    // 删除文件
    deleteFile(item, index) {
      // 判断是外部传入数组还是新加数组
      // 外部数组
      console.log(item, "item");
      // return;
      // if (item.nodePath) {
      //   this.delList.push(item.nodePath);
      //   this.$emit("deleteFile", this.delList);
      // } else {
      //   // 删除传出文件列表接口对应的项
      //   this.fileList = this.fileList.filter(
      //     (child) => child.file.lastModified !== item.file.lastModified
      //   );
      // }
      // 删除视图数组的内容
      this.previewList.splice(index, 1);
      this.fileList = this.previewList;
    },
    //下载文件
    downFile(fileUrl, fileName) {
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
<style lang="less" scoped>
.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 200px;
}
.tabcontent {
  overflow: hidden;
  background: white;
  border-radius: 8px;
  .tabcontent-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(to right, #ee0a0a14, #fff);
    padding-right: 6px;
  }
  .fileBox {
    background: #fafafa;
    border: 0.5px solid rgba(229, 229, 229, 1);
    .iconWH {
      width: 32px;
      height: 32px;
    }
    .fileContent {
      color: #999;
      padding: 0 12px;
      flex: 1;
      display: flex;
      justify-content: space-between;
      flex-direction: column;
      line-height: 16px;
      .fileNameBox {
        max-width: 220px;
        line-height: 20px;
        color: black;
      }
    }
    .downWH {
      width: 16px;
      height: 16px;
    }
  }
  .sub-title {
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
    padding: 8px 12px;

    flex: 1;
  }
  .labelText {
    padding: 6px 12px;
  }
  .subContent {
    // padding: 8px 12px;
    .contentTab {
      background: #f2f2f2;
      border-radius: 4px;
      padding: 2px 4px;
      display: inline-block;
      margin-bottom: 6px;
    }
    i {
      color: #666;
    }
  }
}
</style>
