package com.sj.weixin.web.entity;

public class XCard
{
	String id;
	String name;
	String jid;
	String linkId;
	String index;
	VCard vCard;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public VCard getvCard() {
		return vCard;
	}
	public void setvCard(VCard vCard) {
		this.vCard = vCard;
	}
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	public String getLinkId() {
		return linkId;
	}
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	
}