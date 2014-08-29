package com.sj.weixin.web.entity;


public class TypeXHTML
{
	private String Type ;
	private String xhtml;
	
	public void SetTypeImg()	
	{
		this.Type = "IMG";
	}
	
	public String getType()
	{
		return this.Type;
	}

	public String getXhtml() {
		return xhtml;
	}

	public void setXhtml(String xhtml) {
		this.xhtml = xhtml;
	}

	public void setType(String type) {
		Type = type;
	}
	
	
}