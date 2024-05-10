<template>
    <div class="container">
      <el-row class="row"><h2>给自己取个名字吧：</h2></el-row>
      <el-row class="row"><el-input v-model="userName" style="width: 240px" placeholder="请输入你的名字" /></el-row>
      <el-row class="row"><el-button type="primary" @click="enterChatRoom">进入聊天室</el-button></el-row>
    </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      userName: ''
    }
  },
  props: {
    msg: String
  },
  methods: {
    enterChatRoom() {
      this.$router.push({
        name:'ChatRoom',
        params:{
          userName: this.userName
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  width: 500px;
  margin: auto;
}
.row {
  margin-top: 20px;
}
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
