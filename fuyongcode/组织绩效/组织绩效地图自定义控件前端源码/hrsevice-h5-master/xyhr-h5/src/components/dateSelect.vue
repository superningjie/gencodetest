<template>
    <div class="dateSelect">
        
    </div>
</template>

<script>
    export default {
        name:"dateSelect",
        data(){
            return{
                minDate:new Date(1910,0,1),
                maxDate:new Date(),
                currentDate:new Date(),
            }
        },
        props:{
            dateSelectShow:{
                type:Boolean,
                default:false
            }
        },
        methods:{
            onclose(){
                this.$emit("dateSelectclick",false)
            }
        }
    }
</script>