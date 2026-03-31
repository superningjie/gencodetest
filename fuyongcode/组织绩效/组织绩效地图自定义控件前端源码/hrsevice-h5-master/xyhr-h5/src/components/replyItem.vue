<!-- @format -->

<template>
  <div class="voiceItem" @click="voiceDetail(voiceData)">
    <van-icon name="delete-o" class="voiceDelete" @click.stop="voiceDelete(voiceData)" />
    <van-image src="@/assets/deleteed.svg" v-if="voiceData.voiceIsDelete == 'Y'" class="voicedeleteed" />
    <h1 class="title">{{ voiceData.commentInfo }}</h1>
    <div class="flex creator" style="position: relative">
      <div class="face">
        <van-image lazy-load round fit="fill" width="14px" height="14px" :src="voiceData.image ? voiceData.image : KHCFDC"></van-image>
      </div>
      <p>{{ voiceData.creatorName }}</p>
      <span
        >{{ voiceData.deptName.length > 7 ? voiceData.deptName.substr(0, 7) + "..." : voiceData.deptName }}｜{{
          voiceData.postName.length > 7 ? voiceData.postName.substr(0, 7) + "..." : voiceData.postName
        }}</span
      >
      <span style="position: absolute; right: 0" v-if="voiceType != 'SquareVoiceList'" class="myred">{{ voiceData.voiceType }}</span>
    </div>
    <div class="digest" style="color: #999">
      {{ voiceData.title }}

      <!-- <div class="voicedeleteed">已<br>删<br>除</div> -->
    </div>
    <div class="info flex justify">
      <span class="date">{{ voiceData.commentTime }}</span>
      <ul class="flex">
        <li>
          <i class="iconfont">&#xe687;</i><span>{{ voiceData.readCount }}</span>
        </li>
        <li>
          <i class="iconfont">&#xe681;</i><span>{{ voiceData.commCount }}</span>
        </li>
        <li @click.stop="likeClick(voiceData)" :class="voiceData.haveLiked ? 'myred' : ''">
          <i class="iconfont">&#xe680;</i><span>{{ voiceData.likeCount }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { psnVoiceLike, psnVoiceCancelLike } from "@/libs/api.js"
import { Toast } from "vant"
import KHCFDC from '@/assets/KHCFDC.svg'
export default {
  name: "voiceItem",
  data() {
    return {
		KHCFDC:KHCFDC
	}
  },
  components: {},
  props: {
    voiceData: {
      type: Object,
      default: {},
    },
    voiceType: {
      default: "",
      type: String,
    },
  },
  created() {
    // console.log(this.voiceData)
  },
  methods: {
    voiceDetail(item) {
      if (item.voiceIsDelete == "Y") {
        return
      }
      this.$router.push({
        name: "/selfhelp/voiceDetail",
        query: {
          pkVoice: item.pkPsnVoice,
        },
      })
    },
    voiceDelete(voiceData) {
      this.$emit("voiceDelete", voiceData)
    },
    likeClick(item) {
      // if(item.haveLiked){
      // 	psnVoiceCancelLike({
      // 		pk:item.pkComment
      // 	}).then(res=>{
      // 		if(res.data.statusCode == 200){
      // 			Toast("取消点赞成功")
      // 			this.$emit("likenum",-1,item.pkComment)
      // 		}
      // 	})
      // }else{
      // 	psnVoiceLike({
      // 		pk:item.pkComment
      // 	}).then(res=>{
      // 		Toast("点赞成功")
      // 		this.$emit("likenum",1,item.psnVoicePK)
      // 	})
      // }
    },
  },
}
</script>
