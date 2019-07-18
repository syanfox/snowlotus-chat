package com.qiushui.snowlotuschat.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.qiushui.snowlotuschat.entity.MessageInfo;
import com.qiushui.snowlotuschat.entity.SendMessage;
import com.qiushui.snowlotuschat.service.MessageService;
import com.qiushui.snowlotuschat.utils.Constant;
import com.qiushui.snowlotuschat.utils.RedisUtil;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService{

	@Autowired
	private RedisUtil redisUtil;
	
	
	
	
	@Override
	public void register(Long userId, ChannelHandlerContext ctx) {
		Constant.onlineUserMap.put(userId, ctx);
		 log.info("userId为 {0} 的用户登记到在线用户表，当前在线人数为：{1}", userId, Constant.onlineUserMap.size());
	}

	@Override
	public void sendMessage(MessageInfo msg) {
		if(msg!=null) {
			List<Long> recipients = msg.getRecipients();
			for(Long userId: recipients) {
				SendMessage sendMessage = new SendMessage();
				sendMessage.setMsid(msg.getMsid());
				sendMessage.setSender(msg.getSender());
				sendMessage.setTitel(msg.getTitel());
				sendMessage.setmType(msg.getmType());
				sendMessage.setContent(msg.getContent());
				sendMessage.setRecipient(userId);
				sendMessage.setMsgTime(msg.getMsgTime());
				 ChannelHandlerContext toUserCtx = Constant.onlineUserMap.get(userId);
			     if (toUserCtx == null) {
			        log.info("用户{}没连接，缓存信息",userId);
			        redisUtil.sSet(String.valueOf(userId), JSON.toJSONString(sendMessage));
			     }
			     else {
			    	 sendMessage(toUserCtx, JSON.toJSONString(sendMessage));
			     }
				
			}
		}
		
	}
	
	

	@Override
	public void remove(ChannelHandlerContext ctx) {
		 Iterator<Entry<Long, ChannelHandlerContext>> iterator = Constant.onlineUserMap.entrySet().iterator();
	        while(iterator.hasNext()) {
	            Entry<Long, ChannelHandlerContext> entry = iterator.next();
	            if (entry.getValue() == ctx) {
	                log.info("正在移除握手实例...");
	              
	                log.info("userId为 {0} 的用户已退出聊天，当前在线人数为：{1}"
	                        , entry.getKey(), Constant.onlineUserMap.size());
	                break;
	            }
	        }
		
	}

	@Override
	public void typeError(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	  private void sendMessage(ChannelHandlerContext ctx, String message) {
	        ChannelFuture writeAndFlush = ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
	        if(writeAndFlush.isSuccess()) {
	        	log.info("send message {}  success",message);
	        }
	  }

	@Override
	public void inspectMessageAndSend(Long userId) {
		Set<Object> sGet = redisUtil.sGet(String.valueOf(userId));
		if(sGet!=null) {
			for (Object object : sGet) {  
			     String message =  (String) object;
			     log.info("userId:"+userId+"--message:"+message);
			     ChannelHandlerContext toUserCtx = Constant.onlineUserMap.get(userId);
			     if (toUserCtx == null) {
			        log.info("用户{}没连接，缓存信息",userId);
			        redisUtil.sSet(String.valueOf(userId),message);
			     }
			     else {
			    	 sendMessage(toUserCtx, message);
			    	 redisUtil.setRemove(String.valueOf(userId),message);
			     }
			}  
		}
		
	}

}
