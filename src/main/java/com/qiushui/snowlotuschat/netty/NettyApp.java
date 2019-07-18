package com.qiushui.snowlotuschat.netty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.qiushui.snowlotuschat.service.impl.MessageServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("singleton")
public class NettyApp {

	
	
	@Value("${netty.server.port}")
    public Integer port;

	@Autowired
	private MessageServiceImpl messageService;
	
	private Thread nettyThread;
	
	private NettyServer nettyServer;
	
	/**
	 * 描述：Tomcat加载完ApplicationContext-main和netty文件后： 1. 启动Netty WebSocket服务器； 2.
	 * 加载用户数据； 3. 加载用户交流群数据。
	 */
	@PostConstruct
	public void init() {
		nettyServer = new NettyServer(messageService,port);
		nettyThread = new Thread(nettyServer);
		log.info("开启独立线程，启动Netty WebSocket服务器...");
		nettyThread.start();

	}

	/**
	 * 描述：Tomcat服务器关闭前需要手动关闭Netty Websocket相关资源，否则会造成内存泄漏。 1. 释放Netty Websocket相关连接；
	 * 2. 关闭Netty Websocket服务器线程。（强行关闭，是否有必要？）
	 */
	@SuppressWarnings("deprecation")
	@PreDestroy
	public void close() {
		log.info("正在释放Netty Websocket相关连接...");
		nettyServer.close();
		log.info("正在关闭Netty Websocket服务器线程...");
		nettyThread.interrupt();
		log.info("系统成功关闭！");
	}
}
