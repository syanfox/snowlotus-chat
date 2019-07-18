package com.qiushui.snowlotuschat.service;


import com.qiushui.snowlotuschat.entity.MessageInfo;

import io.netty.channel.ChannelHandlerContext;

public interface MessageService {

	 public void register(Long userId, ChannelHandlerContext ctx);
	 
	 public void sendMessage(MessageInfo msg);
	 
	 public void inspectMessageAndSend(Long userId);
	 
//	 public void singleSend(JSONObject param, ChannelHandlerContext ctx);
//	    
//	 public void groupSend(JSONObject param, ChannelHandlerContext ctx);
//	    
//	 public void FileMsgSingleSend(JSONObject param, ChannelHandlerContext ctx);
//	    
//	 public void FileMsgGroupSend(JSONObject param, ChannelHandlerContext ctx);
	    
	 public void remove(ChannelHandlerContext ctx);
	    
	  public void typeError(ChannelHandlerContext ctx);
}
