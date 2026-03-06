<template>
  <div class="pages">
    <div v-for="item in customAreaInfo.optionalFieldInfo" :key="item.fieldId">
      <van-cell-group :border="false">
        <div v-if="item.fieldDisplayItem">
          <div
            class="tabcontent pd12 mt10"
            v-if="
              item.fieldId == 'customfield4' ||
              item.fieldId == 'customfield9' ||
              item.fieldId == 'customfield10'
            "
          >
            <div class="flex justify middle">
              <div class="fs14 c6">{{ item.fieldName }}</div>
              <van-uploader
                v-model="uploadFileList"
                :name="`${customAreaInfo.customAreaInfoId}&${item.fieldId}`"
                :preview-image="false"
                :after-read="afterRead"
                v-if="item.fieldModifyItem"
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
              v-for="(file, index) in item.fieldValue"
              :key="file.id"
            >
              <van-image class="iconWH" :src="file.fileIcon" />
              <div class="fileContent">
                <div class="fileNameBox fs14 ovText">{{ file.name }}</div>
                <div class="fs12">
                  {{ file.createtime }}
                  <span class="ml8">{{ file.size }}</span>
                </div>
              </div>
              <van-icon
                name="clear"
                color="#ee0a24"
                size="16"
                v-if="status == '1' && item.fieldModifyItem"
                @click="deleteFile(item, index)"
              />
              <van-image
                class="downWH ml8"
                :src="down"
                @click="downFile(file.url, file.createtime)"
              />
            </div>
          </div>
          <LabelTextareaField
            v-else
            @blur="total(`name${item.indicatorId}`)"
            :name="`name${item.fieldIndex}`"
            :readonly="status === '2' || !item.fieldModifyItem"
            :rules="[
              {
                required: item.fieldMustInputItem,
                message: '该项为必填项',
              },
            ]"
            v-model="item.fieldValue"
            :placeholder="
              status === '2' || !item.fieldModifyItem ? '-' : '请输入'
            "
            :autosize="{ maxHeight: 100 }"
            type="textarea"
            :title="item.fieldName"
            rows="1"
          />
        </div>
      </van-cell-group>
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
import LabelTextareaField from "@/pages/performances/components/LabelTextareaField";
import Upload from "@/pages/performances/components/Upload";
import { rateUpload } from "@/libs/api.js";
export default {
  name: "CustInfoArea",
  components: {
    LabelTextareaField,
    Upload,
  },
  props: {
    customAreaInfo: {
      type: Object,
      default: {},
    },
    // 待办状态
    status: {
      type: String,
      default: "",
    },
    areaConfId: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      down,
      // 文件列表
      fileList: [],
      // 上传文件列表
      uploadFileList: [],
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
  async mounted() {
    await this.initPreviewList();
    this.fileListData();
  },
  watch: {
    fileList: function (val) {
      this.$emit("initFileList", val);
    },
    areaConfId(oldValue, newValue) {
      this.initPreviewList();
    },
  },
  computed: {},
  methods: {
    initPreviewList() {
      const self = this;
      this.customAreaInfo.optionalFieldInfo?.map((item) => {
        if (
          item.fieldId == "customfield4" ||
          item.fieldId == "customfield9" ||
          item.fieldId == "customfield10"
        ) {
          item.fieldValue = self.setIcon(item);
        }
      });
    },
    initFileList() {
      this.$emit("initFileList", this.fileList);
    },
    fileListData() {
      const array = this.customAreaInfo.optionalFieldInfo;
      const fileList = [];
      for (let i = 0; i < array?.length; i++) {
        const element = array[i];
        if (
          element.fieldId == "customfield4" ||
          element.fieldId == "customfield9" ||
          element.fieldId == "customfield10"
        ) {
          element.fieldValue?.map((value) => fileList.push(value));
        }
      }
      this.fileList = this.arrayTransition(fileList);
      this.initFileList();
    },
    setIcon(obj) {
      const self = this;
      const previewList = obj.fieldValue;
      return previewList?.map((item) => {
        const fileIcon = self.fileIcon(item.name);
        return {
          areaConfId: this.areaConfId,
          fieldName: obj.fieldId,
          name: item.name,
          size: item.size,
          url: item.url,
          createtime: item.createtime,
          fileIcon: fileIcon,
        };
      });
    },
    arrayTransition(obj) {
      console.log(this.areaConfId, "areaConfId");
      const self = this;
      const fileList = obj;
      return fileList.map((item) => {
        const fileIcon = self.fileIcon(item.name);
        return {
          areaConfId: this.areaConfId,
          fieldName: item.fieldName,
          fileName: item.name,
          fileSize: item.size,
          fileUrl: item.url,
          fileDate: item.createtime,
          fileIcon: fileIcon,
        };
      });
    },
    fileIcon(type) {
      console.log(type, "type");
      const fileType = type.split(".")[1];
      const result = this.fileIconList.find((item) => item.type === fileType);
      return result ? result.icon : this.general;
    },
    afterRead(a, item) {
      console.log(a, item, "afterRead");
      const fields = item.name.split("&")[1];
      const objcustomareainsId = item.name.split("&")[0];
      // this.uploadFileList[item.index].objcustomareainsId = objcustomareainsId;
      this.uploadFileList[item.index].fields = fields;
      if (this.uploadFileList.length) {
        this.uploadFile(item.index);
      }
    },
    total() {
      this.$emit("saveAndSubmit", false, false);
    },
    uploadFile(index) {
      let data = {};
      const list = this.uploadFileList;
      if (list.length) {
        const fileData = list[index];
        // console.log(fileData, "fileData");
        const base = fileData.content.split(",");
        data = {
          fileBase: base[1],
          fileName: fileData.file.name,
          // objcustomareainsId: fileData.objcustomareainsId,
          fields: fileData.fields,
        };
      }
      rateUpload(data).then((res) => {
        const loadData = res.data.data.data;
        loadData.fieldName = list[index].fields;
        loadData.fileIcon = this.fileIcon(res.data.data.data.fileName);
        this.fileList.push(loadData);
        const array = this.customAreaInfo.optionalFieldInfo;
        const previewFile = {
          areaConfId: this.areaConfId,
          fieldName: loadData.fieldName,
          name: loadData.fileName,
          size: loadData.fileSize,
          url: loadData.fileUrl,
          createtime: loadData.fileDate,
          fileIcon: loadData.fileIcon,
        };
        for (let i = 0; i < array?.length; i++) {
          const element = array[i];
          if (element.fieldId == list[index].fields) {
            if (element.fieldValue == null) {
              element.fieldValue = [];
              element.fieldValue.push(previewFile);
            } else {
              element.fieldValue.push(previewFile);
            }
          }
        }
      });
    },
    // 删除文件
    deleteFile(item, index) {
      console.log(item, "item", index);
      item.fieldValue.splice(index, 1);
      this.fileList.splice(index, 1);
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
