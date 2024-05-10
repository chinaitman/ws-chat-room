package com.chatroom.core;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chatroom.constant.MessageTypeConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
// 定义websocket服务器端,它的功能主要是将目前的类定义成一个websocket服务器端,注解的值将被用于监听用户连接的终端访问URL地址
@ServerEndpoint("/websocket/{username}")
@Slf4j
public class WebSocket {

    // websocket的session
    private Session session;

    // 存放当前用户名
    private String userName;

    // 存放在线的用户数量
    private static AtomicInteger onlineNum = new AtomicInteger();

    // 存放websocket的集合
    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 前端请求一个websocket
     *
     * @param session
     * @param userName
     * @throws IOException
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String userName) throws IOException {
        this.session = session;
        // 将当前对象放入webSocketSet
        webSocketSet.add(this);
        // 增加在线人数
        onlineNum.incrementAndGet();
        // 保存当前用户名
        this.userName = userName;
        // 获得所有的在线用户
        Set<String> userLists = getOnlineUsers();
        // 将所有信息包装好传到客户端
        Map<String, Object> dataMap = new HashMap<>();
        // 所有用户列表
        dataMap.put("onlineUsers", userLists);
        // 消息类型为上线
        dataMap.put("messageType", MessageTypeConsts.ONLINE);
        // 上线用户的用户名
        dataMap.put("userName", userName);
        // 在线人数
        dataMap.put("onlineNum", onlineNum.get());
        // 发送给所有用户谁上线了
        sendMessageAll(JSONUtil.toJsonStr(dataMap));
        log.info("【websocket消息】有新的连接, 总数：{}", onlineNum.get());
    }

    /**
     * 前端关闭一个websocket
     *
     * @throws IOException
     */
    @OnClose
    public void onClose() throws IOException {
        // 从集合中移除当前对象
        webSocketSet.remove(this);
        // 在线用户数减少
        onlineNum.decrementAndGet();
        // 获得所有的在线用户
        Set<String> userLists = getOnlineUsers();
        // 将所有信息包装好传到客户端
        Map<String, Object> dataMap = new HashMap<>();
        // 消息类型为下线
        dataMap.put("messageType", MessageTypeConsts.OFFLINE);
        // 所有用户列表
        dataMap.put("onlineUsers", userLists);
        // 下线用户的用户名
        dataMap.put("userName", this.userName);
        // 在线人数
        dataMap.put("onlineNum", onlineNum.get());
        // 发送给所有用户谁下线了
        sendMessageAll(JSONUtil.toJsonStr(dataMap));
        log.info("【websocket消息】连接断开, 总数：{}", webSocketSet.size());
    }

    /**
     * 前端向后端发送消息
     *
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("【websocket消息】收到客户端发来的消息：{}", message);
        // 将前端传来的数据进行转型
        JSONObject jsonObject = JSONUtil.parseObj(message);
        // 获取所有数据
        String textMessage = jsonObject.getStr("message");
        String userName = jsonObject.getStr("userName");
        String type = jsonObject.getStr("type");
        String toUserName = jsonObject.getStr("toUserName");
        // 获得所有的在线用户
        Set<String> userLists = getOnlineUsers();
        // 将所有信息包装好传到客户端
        Map<String, Object> dataMap = new HashMap<>();
        // 群发
        if (type.equals("群发")) {
            // 消息类型为普通消息
            dataMap.put("messageType", MessageTypeConsts.SIMPLE);
            // 所有用户列表
            dataMap.put("onlineUsers", userLists);
            // 发送消息的用户名
            dataMap.put("userName", userName);
            // 在线人数
            dataMap.put("onlineNum", onlineNum.get());
            // 发送的消息
            dataMap.put("textMessage", textMessage);
            // 发送信息给所有人
            sendMessageAll(JSONUtil.toJsonStr(dataMap));
        }
        //私发
        else {
            // 消息类型为普通消息
            dataMap.put("messageType", MessageTypeConsts.SIMPLE);
            // 所有用户列表
            dataMap.put("onlineUsers", userLists);
            // 发送消息的用户名
            dataMap.put("userName", userName);
            // 在线人数
            dataMap.put("onlineNum", onlineNum.get());
            // 发送的消息
            dataMap.put("textMessage", textMessage);
            // 发送信息给指定的人
            sendMessageTo(JSONUtil.toJsonStr(dataMap), toUserName);
            // 发送给自己
            sendMessageTo(JSONUtil.toJsonStr(dataMap), userName);
        }
    }

    /**
     * 消息发送给所有人
     */
    public void sendMessageAll(String message) throws IOException {
        for (WebSocket webSocket : webSocketSet) {
            // 异步发送消息
            webSocket.session.getAsyncRemote().sendText(message);
        }
    }

    /**
     * 消息发送指定人
     */
    public void sendMessageTo(String message, String toUserName) throws IOException {
        // 遍历所有用户
        for (WebSocket webSocket : webSocketSet) {
            if (webSocket.userName.equals(toUserName)) {
                // 异步发送消息给指定用户
                webSocket.session.getAsyncRemote().sendText(message);
                log.info("【发送消息】{} 向 {} 发送消息：{}", this.userName, toUserName, message);
                break;
            }
        }
    }

    /**
     * 获取当前所有在线用户
     * @return
     */
    private Set<String> getOnlineUsers() {
        Set<String> userLists = new TreeSet<>();
        for (WebSocket webSocket : webSocketSet) {
            userLists.add(webSocket.userName);
        }
        return userLists;
    }

}
