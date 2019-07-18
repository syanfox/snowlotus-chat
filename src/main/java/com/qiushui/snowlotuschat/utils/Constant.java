package com.qiushui.snowlotuschat.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

public class Constant {


	 public static Map<String, WebSocketServerHandshaker> webSocketHandshakerMap = 
	            new ConcurrentHashMap<String, WebSocketServerHandshaker>();
	
	public static Map<Long, ChannelHandlerContext> onlineUserMap = new ConcurrentHashMap<Long, ChannelHandlerContext>();
}
