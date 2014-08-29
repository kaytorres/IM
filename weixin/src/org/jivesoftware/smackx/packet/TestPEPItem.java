package org.jivesoftware.smackx.packet;

import org.jivesoftware.smackx.packet.PEPItem;

public class TestPEPItem extends PEPItem {

	String node;
	String itemXML;
	
	public TestPEPItem(String id,String node,String itemXML) {
		super(id);
		this.node = node;
		this.itemXML = itemXML;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getItemDetailsXML() {
		// TODO Auto-generated method stub
		return this.itemXML;
	}

	@Override
	public String getNode() {
		// TODO Auto-generated method stub
		return this.node;
	}
	
	

}
