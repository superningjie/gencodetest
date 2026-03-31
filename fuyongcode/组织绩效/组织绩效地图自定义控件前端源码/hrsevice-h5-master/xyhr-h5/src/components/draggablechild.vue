<template>
    <div class="draggablechild">
        <template v-for="(app,key) in appdata.staff">
            <div class="app" v-if="config.showVo.staffApplications.indexOf(app.id)>=0">
                <div class="icon">
                    <van-icon 
                        name="clear"
                        class="remove"
                        v-if="editmode"
                        @click.stop.parvent="removeApp('staff',key,app)"
                    />
                    <img :src="app.icon" @click.stop.parvent="goPath(app.path)"/>
                </div>
                <p @click.stop.parvent="goPath(app.path)">{{app.label}}</p>
            </div>
        </template>
        <div class="app" v-if="editmode" @click="finishEdit">
            <div class="icon" style="color:#9B9B9C; margin-bottom: 15px;height:46px;line-height:50px;">
                <van-icon name="passed" size="25" />
            </div>
            <p>完成</p>
        </div>
    </div>
</template>

<script>
	import {appdata} from '@/libs/selfhelpdata.js'
    export default {
        name:"draggablechild",
        data(){
            return{
                appdata: appdata(),
            }
        },
        created(){
            
        },
        props:{
            config:{
                type:Object,
                default:{}
            },
            editmode:{
                type:Boolean,
                default:false

            }
        },
        methods:{
            goPath(){

            },
            removeApp(aaa,key,app){
                alert(11111)
            },
            finishEdit(){
                
            }
        }
    }
</script>