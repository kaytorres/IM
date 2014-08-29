package com.sj.weixin.web.smack.entity;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;


public class PublishPostPacket extends Packet {

	private PubSubNamespace ns = PubSubNamespace.BASIC;
	private String node ;
	
	public String getElementName()
	{
	  return "pubsub";
	}
	
	public String getNamespace()
	{
	  return this.ns.getXmlns();
	}

	public PublishPostPacket(String packetId,String from ,String to, String node)
	{
		setPacketID(packetId);
		setFrom(from);
		setTo(to);
		this.node = node;
	}
	
	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		
		StringBuilder buf = new StringBuilder();
		buf.append("<iq id=\""+this.getPacketID()+"\" to=\""+this.getTo()+"\" type=\"set\">");
		buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
		buf.append("<publish node='"+node+"'> <item id='1cb57d9c-1c46-11dd-838c-001143d5d5db'>    <entry xmlns='http://www.w3.org/2005/Atom'>     <title type='text'>hanging out at the Caf&amp;#233; Napolitano</title>     <id>tag:montague.lit,2008-05-08:posts-1cb57d9c-1c46-11dd-838c-001143d5d5db</id>         <published>2008-05-08T18:30:02Z</published>          <updated>2008-05-08T18:30:02Z</updated>        </entry>      </item></publish>");
		buf.append("</").append(getElementName()).append(">");
		buf.append("</iq>");
		return buf.toString();
		
	}

}
