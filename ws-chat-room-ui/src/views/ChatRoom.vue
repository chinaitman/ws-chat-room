<template>
    <div class="container">
      <div style="line-height: 50px; margin-bottom: 2px; background: #82CAFF; color: #303133; font-size: 20px;">聊天室</div>
      <div>
        <div class="main">
          
          <div class="content">
            <el-input
              v-model="content"
              :rows="20"
              resize="none" 
              type="textarea"
              readonly="false"
            />
          </div>

          <div style="width: 99%;">
            <el-input
              v-model="message"
              style="margin-left: 2px;"
              :rows="5"
              resize="none" 
              type="textarea"
              @keyup.enter = "sendMessage"
            />
          </div>
          <div style="margin: 20px 10px 20px 0;text-align: right;">
            <el-button type="primary" @click="sendMessage">发送/[Enter]</el-button>
          </div>

        </div>

        <div class="onlines">
          <div style="padding: 10px 0 10px 0; border-bottom: 1px solid #E4E7ED;">
                <strong>在线用户 {{onlineNum}}</strong>
            </div>
          <ul>
            <li v-for="(item, index) in onlineUsers" :key="index" @click="friend(item)">
                {{item}}
            </li>
        </ul>
        </div>
      </div>
     
    </div>
</template>

<script>
import { ElMessage } from 'element-plus'

var webSocket = null;
export default {
  name: 'ChatRoom',
  data() {
    return {
      userName: '',
      toUserName: '',
      content: '',
      message: '',
      onlineNum: 0,
      onlineUsers: []
    }
  },
  mounted() {
    this.userName = this.$route.params.userName;
    // 初始化websocket
    this.initWebSocket();
  },
  methods: {
    // 显示消息
    setMessage(newMessage) {
        var old = this.content;
        var data = JSON.parse(newMessage);
        this.onlineNum = data.onlineNum;
        this.onlineUsers = data.onlineUsers;
        if(data.messageType==1) {
          this.content = old + "\n" + data.userName + " 进入聊天室";
        } else if(data.messageType==3) {
          this.content = old + "\n" + data.userName + "：" + data.textMessage;
        }
    },
    // websocket初始化
    initWebSocket() {
        const _this = this;
        // 判断当前浏览器是否支持WebSocket, 主要此处要更换为自己的地址
        if ('WebSocket' in window) {
            webSocket = new WebSocket("ws://localhost:8080/websocket/" + this.userName);
        } else {
            alert('浏览器不支持websocket');
        }

        //连接发生错误的回调方法
        webSocket.onerror = function () {
          _this.setMessage("系统消息: 网络异常！");
        };
 
        //连接成功建立的回调方法
        webSocket.onopen = function (event) {
          console.log("建立连接"+event);
        }
 
        //接收到消息的回调方法
        webSocket.onmessage = function (event) {
          console.log("收到消息", event.data)
          _this.setMessage(event.data);
        }
 
        //连接关闭的回调方法
        webSocket.onclose = function () {
          console.log("断开连接");
        }
 
        //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
        window.onbeforeunload = function () {
          webSocket.close();
        }
    },
    // 发送消息
    sendMessage() {
      let message = this.message;
      let type = "群发";
      if(message.charAt(0)=="@") {
        type = "私发";
      }
      var param = {};
			param['userName'] = this.userName;
			param['message'] = message;
			param['type'] = type;
			param['toUserName'] = this.toUserName;
      webSocket.send(JSON.stringify(param));
      this.message = '';
    },
    // 选择用户
    friend(user) {
      if(user==this.userName) {
        ElMessage.error('不能选择自己！');
        return;
      }
      this.message = "@" + user + " ";
      this.toUserName = user;
    }
  }
}
</script>

<style scoped>
.container {
  width: 800px;
  margin: auto;
}
.row {
  margin-top: 20px;
}
.main {
  width: 75%;
  border: 5px solid #F2F6FC;
  float: left;
}
.content {
  width: 100%;
  border-right: 5px solid #F2F6FC;
}
.onlines {
  width: 23%;
  height: 622px;
  border: 2px solid #F2F6FC;
  border-radius: 1px;
  float: right
}
:deep(.content .el-textarea__inner) {
        box-shadow: 0 0 0 0px;
            }
h2 {
  text-align: left;
  padding-left: 10px;
  color: #67C23A;
}
ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}
li {
  width: 100%;
  line-height: 30px;
  border-bottom: 1px solid #F5F7FA;
}
a {
  color: #42b983;
}
</style>
