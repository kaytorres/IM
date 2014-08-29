package com.sj.weixin.web.entity;

public class SimpleXCard {
	private String jid;
	private String name;
	
	public SimpleXCard(String jid,String name)
	{
		this.jid = jid;
		this.name = name;
	}
	
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
