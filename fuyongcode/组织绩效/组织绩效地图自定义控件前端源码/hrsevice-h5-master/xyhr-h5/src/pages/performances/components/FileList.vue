<template>
  <div>
    <div>
      <!-- 文件列表 -->
      <div class="tabContent pd12 mt10" v-for="item in data" :key="item.id">
        <div v-if="item.fileData">
          <div class="flex justify middle">
            <div class="fs14 c6">{{ item.title }}</div>
          </div>
          <div
            class="flex fileBox pd12 justify middle mt12"
            v-for="file in item.fileData"
            :key="file.id"
          >
            <van-image class="iconWH" :src="fileIcon(file.name)" />
            <div class="fileContent">
              <div class="fileNameBox fs14 ovText">
                {{ file.name }}
              </div>
              <div class="fs12">{{ file.createtime }} {{ file.size }}</div>
            </div>
            <van-image
              class="downWH"
              :src="down"
              @click="downFile(file.url, file.name)"
            />
          </div>
        </div>
        <van-field
          v-else
          label-class="c6"
          v-model="item.content"
          :label="item.title"
          rows="1"
          autosize
          type="textarea"
          readonly
        />
      </div>
      <!-- 文本列表 -->
      <!-- <div class="tabContent mt10 field">

      </div> -->
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

export default {
  props: {
    data: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      down,
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
  mounted() {},
  methods: {
    //下载文件
    downFile(fileUrl, fileName) {
      let link = document.createElement("a"); //创建a标签
      link.style.display = "none"; //将a标签隐藏
      link.href = fileUrl; //给a标签添加下载链接
      link.setAttribute("download", fileName); // 此处注意，要给a标签添加一个download属性，属性值就是文件名称 否则下载出来的文件是没有属性的，空白白
      document.body.appendChild(link);
      link.click(); //执行a标签
    },
    fileIcon(type) {
      const fileType = type.split(".")[1];
      const result = this.fileIconList.find((item) => item.type === fileType);
      return result ? result.icon : this.general;
    },
  },
};
</script>

<style lang="less" scoped>
/deep/ .field {
  padding: 0.1px 12px;
  .van-cell {
    display: flex;
    flex-direction: column;
    align-items: normal !important;
  }
}
.ovText {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 130px;
}
.tabContent {
  position: relative;
  border-radius: 8px;
  background: white;
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
}
</style>
