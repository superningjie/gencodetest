<template>
    <div class="headcount">
        <div class="headcountMain">
            <span v-if="realSize">
                共查询到{{count}}条记录
                
            </span>
            <span v-else>
                共查询到{{count}}人
            </span>
        </div>
    </div>
</template>

<script>
export default {
    name:"headcount",
    data(){
        return{

        }
    },
    props:{
        count:{
            default:0,
        },
        realSize:{
            default:0,
        },
    }
}
</script>