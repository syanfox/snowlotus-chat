package com.qiushui.snowlotuschat.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class SendMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832028888938967753L;
	private Long msid;
	private String sender;
	private Long recipient;
	private String titel;
	private String mType;
	private String content;
	private Date msgTime;
	public Long getMsid() {
		return msid;
	}
	public void setMsid(Long msid) {
		this.msid = msid;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public Long getRecipient() {
		return recipient;
	}
	public void setRecipient(Long recipient) {
		this.recipient = recipient;
	}
	public String getTitel() {
		return titel;
	}
	public void setTitel(String titel) {
		this.titel = titel;
	}
	public String getmType() {
		return mType;
	}
	public void setmType(String mType) {
		this.mType = mType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getMsgTime() {
		return msgTime;
	}
	public void setMsgTime(Date msgTime) {
		this.msgTime = msgTime;
	}
	
	
}
