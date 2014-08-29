package com.sj.weixin.web.controller;

import java.util.Date;

import javax.servlet.http.HttpSession;

public class UserClient {
	private String uuId;
	private String Status;				//0：请求二维码  1:手机已扫描 2:手机已验证
	private String SmackJid;
	private String SmackPassword;
	private String SmackName;
	private HttpSession Session;
	private String AddsbookJson;
	private String avatarBASE64;
	private String xCardSortJson;
	private Date lastConn;
	
	
	
	public Date getLastConn() {
		return lastConn;
	}
	public void setLastConn(Date lastConn) {
		this.lastConn = lastConn;
	}
	public String getxCardSortJson() {
		return xCardSortJson;
	}
	public void setxCardSortJson(String xCardSortJson) {
		this.xCardSortJson = xCardSortJson;
	}
	public String getAvatarBASE64() {
		return avatarBASE64;
	}
	public String getSmackPassword() {
		return SmackPassword;
	}
	public void setSmackPassword(String smackPassword) {
		SmackPassword = smackPassword;
	}
	public void setAvatarBASE64(String avatarBASE64) {
		this.avatarBASE64 = avatarBASE64;
	}
	public String getAddsbookJson() {
		return AddsbookJson;
	}
	public void setAddsbookJson(String addsbookJson) {
		AddsbookJson = addsbookJson;
	}
	public String getUuId() {
		return uuId;
	}
	public void setUuId(String uuId) {
		this.uuId = uuId;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}

	public String getSmackJid() {
		return SmackJid;
	}
	public void setSmackJid(String smackJid) {
		SmackJid = smackJid;
	}
	
	public String getSmackName() {
		return SmackName;
	}
	public void setSmackName(String smackName) {
		SmackName = smackName;
	}
	public HttpSession getSession() {
		return Session;
	}
	public void setSession(HttpSession session) {
		Session = session;
	}
	
	
}