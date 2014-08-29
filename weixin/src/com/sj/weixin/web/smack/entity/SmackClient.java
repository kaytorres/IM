package com.sj.weixin.web.smack.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.TestPEPProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.PEPListener;
import org.jivesoftware.smackx.PEPManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.PEPEvent;
import org.jivesoftware.smackx.packet.PEPItem;
import org.jivesoftware.smackx.packet.TestPEPItem;
import org.jivesoftware.smackx.provider.PEPProvider;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.packet.PubSub;

import com.sj.weixin.web.cometd.server.MessageService;
import com.sj.weixin.web.controller.UserClient;
import com.sj.weixin.web.smack.cache.JidToCometdSession;
import com.sj.weixin.web.smack.common.Function;

public class SmackClient {
	private XMPPConnection conn;
	private ConnectionConfiguration conf;
	private String username;
	private String password;
	private String jid = "";
	private static Map participantToChatThread = Collections.synchronizedMap(new HashMap());
	private MessageListener messageListerner;
	private ChatManagerListener chatManagerListener;
	private PubSubManager pubSubManager;
	private Map<String , Node> EventNodeMap = Collections.synchronizedMap(new HashMap<String, Node>());
	
	private Mode ClientMode;
	private String ClientStatus;
	
	// private PacketListener mucMessageListener;
	private InvitationListener invitationListener;
	private Map<String, MultiUserChat> MUCMap = Collections.synchronizedMap(new HashMap<String, MultiUserChat>());

	public SmackClient(String username, String password, String server, int port) {
		this.username = username;
		this.password = password;
		this.conf = new ConnectionConfiguration(server, port,"fe.shenj.com");
		conn = new XMPPConnection(conf);
	}

	public SmackClient(String username, String password, String server) {
		this.username = username;
		this.password = password;
		conn = new XMPPConnection(server);
	}

	public String login() throws XMPPException {
		if (conn != null) {
			conn.connect();
			
			conn.addPacketWriterListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					// TODO Auto-generated method stub
					System.out.println("send:" + packet.toXML());
				}
			}, null);
			
			conn.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					// TODO Auto-generated method stub
					System.out.println("reci:" + packet.toXML());
				}
			}, null);
			
			conn.login(username, password);
			jid = conn.getUser();
			
			Presence loginPresence = new Presence(Presence.Type.available);
			loginPresence.setMode(Presence.Mode.chat);
			setClientMode(Presence.Mode.chat);
			conn.sendPacket(loginPresence);
			
			// 普通会话监听
			ChatManager chatmanager = conn.getChatManager();
			chatmanager.addChatListener(chatManagerListener);

			// 群邀请（多人会话邀请）监听
			MultiUserChat.addInvitationListener(conn, invitationListener);
			
			//创建微博发布节点
			/*
			pubSubManager = new PubSubManager(conn);  
            String nodeId = "http://neekle.com/xmpp/protocol/media/"+Function.getPureId(jid);  

            LeafNode myNode = null;  
            try {  
                myNode = pubSubManager.getNode(nodeId);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            if(myNode == null){  

                ConfigureForm f = new ConfigureForm(FormType.submit);
                f.setNotifyRetract(true);
                f.setAccessModel(AccessModel.open);
                f.setSubscribe(true);
                
                myNode = pubSubManager.createNode(nodeId);  
            }  
            
			SubFriendsMicroblog();
			
            String msg = "hahaha I am "+jid;  
            
            SimplePayload payload = new SimplePayload("message","pubsub:test:message", msg);  
            PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(payload);  
            
            //PubSub pubSubPacket = PubSubManager.createPubsubPacket("yyyy@fe.shenj.com", IQ.Type.SET, new NodeExtension(PubSubElementType.PUBLISH, myNode.getId()));
            //conn.sendPacket(packet)
            myNode.publish(item);  
            

			PEPManager pepManager = new PEPManager(conn);
			
			
			PEPProvider pepProvider = new PEPProvider();
			TestPEPProvider pepItemParser = new TestPEPProvider();
			
			pepProvider.registerPEPParserExtension("urn:xmpp:microblog:0", pepItemParser);
			
			
			pepManager.addPEPListener(
					new PEPListener()
					{

						@Override
						public void eventReceived(String arg0, PEPEvent arg1) {
							// TODO Auto-generated method stub
							System.out.println("PEP:  "+arg0+";"+arg1.toXML());
						}
						
					}
				);
				
			String pepItemId = "57QH6-15";
			String pepNode = "urn:xmpp:microblog:0";
			String pepXML = " <entry xmlns=\"http://www.w3.org/2005/Atom\"> "+
         " <author>jyx001@fe.shenj.com</author>"+
         " <content type=\"text\">微博测试</content>"+
         " <published>2014-6-3T15:16:18</published>"+
         " <geoloc xmlns=\"http://jabber.org/protocol/geoloc\">"+
         "   <locality>中国上海</locality>"+
         " </geoloc>"+
         " <device>PC</device>"+
      "  </entry>";
			if(jid.split("@")[0].equals("yyyy"))
			{
			TestPEPItem testPEPItem = new TestPEPItem(pepItemId, pepNode, pepXML);
			pepManager.publish(testPEPItem);
			
			
			
			
				//PublishPostPacket publishPostPacket = new PublishPostPacket("57QH6-15","yyyy@fe.shenj.com","yar5@fe.shenj.com/Smack","urn:xmpp:microblog:0");
				//conn.sendPacket(publishPostPacket);
			}
			*/
		}
		return Function.getPureId(jid);
	}

	private void SubFriendsMicroblog() {
		// TODO Auto-generated method stub
		Roster roster = getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			Collection<RosterGroup> groups = entry.getGroups();
			for (RosterGroup group : groups) {
				if (!"Tuple".equals(group.getName())) 
				{
					String nodeId = "http://neekle.com/xmpp/protocol/media/"+Function.getPureId(entry.getUser());
					Node eventNode = null;
					try {
						eventNode = pubSubManager.getNode(nodeId);
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
					if(eventNode != null)
					{
						ItemEventListener itemEventListener = getEventNodeListener();
						eventNode.addItemEventListener(itemEventListener);
						try {
							eventNode.subscribe(conn.getUser());
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						EventNodeMap.put(Function.getPureId(entry.getUser()), eventNode);
						
					}
				}
			}
		}
		
		
	}

	public void logout() throws XMPPException {
		if (conn != null) {
			Presence logoutPresence = new Presence(Presence.Type.available);
			logoutPresence.setMode(Presence.Mode.xa);
			conn.sendPacket(logoutPresence);
			
			
			conn.getChatManager().removeChatListener(chatManagerListener);
			chatManagerListener = null;
			Set set = participantToChatThread.entrySet();
			for (Object threadId : set) {
				String idStr = threadId.toString();
				Chat tmpChat = conn.getChatManager().getThreadChat(idStr);
				if(tmpChat!=null)
				{
					tmpChat.removeMessageListener(messageListerner);
				}
			}
			/*Iterator iterator = participantToChatThread.entrySet().iterator();
			while(iterator.hasNext())
			{
				 Map.Entry entry =   (Entry) iterator.next();
				 String value = entry.getValue().toString();
				 if(value.contains(username))
				 {
					 iterator.remove();
				 }
			}
			*/
			
			messageListerner = null;
			participantToChatThread.clear();
			MUCMap.clear();
			//participantToChatThread = null;
			conn.disconnect();
		}
	}

	public Roster getRoster() {
		return conn.getRoster();
	}
	
	public String getUsername()
	{
		return this.username;
	}

	public Chat createChat(String toJid) {
		String threadId = jid + "#" + toJid;
		Chat chat = conn.getChatManager().createChat(toJid, threadId,messageListerner);
		participantToChatThread.put(toJid, threadId);
		return chat;
	}

	public void sendImg(String toJid, String imgURL,String thumbnailURL) throws XMPPException
	{
		String threadId = (String) participantToChatThread.get(toJid);
		Chat chat = conn.getChatManager().getThreadChat(threadId);
		
		String body = "[图片]";
		String exXML = "<html xmlns='http://jabber.org/protocol/xhtml-im'>"+
	    				"<body xmlns='http://www.w3.org/1999/xhtml'>"+
	    				"<p><a href=\""+imgURL+"\"><img src=\""+thumbnailURL+"\"/></a></p>"+
	    				"</body>"+
	    				"</html>";
		try {
			if (chat != null) {
				Message messageXML = new Message(chat.getParticipant(), Message.Type.chat,body, exXML,chat.getThreadID());
				chat.sendMessage(messageXML);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				System.out.println("SEND TIME:" + df.format(new Date()));
			}
			else
			{
				chat = this.createChat(toJid);
				Message messageXML = new Message(chat.getParticipant(), Message.Type.chat,body, exXML,chat.getThreadID());
				chat.sendMessage(messageXML);
			}
		} catch (XMPPException e) {
			throw e;
		}
	}
	
	
	
	public void sendMessage(String toJid, String message) throws XMPPException {
		String threadId = (String) participantToChatThread.get(toJid);
		Chat chat = conn.getChatManager().getThreadChat(threadId);
		
		
		String exXML = "<html xmlns='http://jabber.org/protocol/xhtml-im'>"+
	    				"<body xmlns='http://www.w3.org/1999/xhtml'>"+
	    				"<p>"+message+"</p>"+
	    				"</body>"+
	    				"</html>";
		
		
		
		
		try {
			if (chat != null) {
				Message messageXML = new Message(chat.getParticipant(), Message.Type.chat,message, exXML,chat.getThreadID());
				chat.sendMessage(messageXML);
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				System.out.println("SEND TIME:" + df.format(new Date()));
			}
			else
			{
				chat = this.createChat(toJid);
				Message messageXML = new Message(chat.getParticipant(), Message.Type.chat,message, exXML,chat.getThreadID());
				chat.sendMessage(messageXML);
			}
		} catch (XMPPException e) {
			throw e;
		}
	}

	public MultiUserChat getMUCInstance(String tupleJid) {
		if (MUCMap.containsKey(tupleJid)) {
			return MUCMap.get(tupleJid);
		}
		final MultiUserChat multiUserChat = new MultiUserChat(conn, tupleJid);
		// multiUserChat.addMessageListener(mucMessageListener);
		// MUCMap.put(tupleJid, multiUserChat);
		return multiUserChat;
	}

	public void setMessageAndParticipantStatusListener(String smackJid , String tupleJid,
			MultiUserChat multiUserChat, PacketListener mucMessageListener,
			ParticipantStatusListener participantStatusListener,
			UserStatusListener userStatusListener
	) {
		if (MUCMap.containsKey(tupleJid)) {
			//participantStatusListener = null;
			//mucMessageListener = null;
			//return;
			multiUserChat.addMessageListener(mucMessageListener);
			multiUserChat.addParticipantStatusListener(participantStatusListener);
			multiUserChat.addUserStatusListener(userStatusListener);
			MUCMap.put(tupleJid, multiUserChat);
		} else {
			multiUserChat.addMessageListener(mucMessageListener);
			multiUserChat.addParticipantStatusListener(participantStatusListener);
			multiUserChat.addUserStatusListener(userStatusListener);
			MUCMap.put(tupleJid, multiUserChat);
		}
	}
	
	public void MUCMapRemove(String tupleJid)
	{
		MUCMap.remove(tupleJid);
		
	}
	
	public void RosterRemove(String tupleJid)
	{
		Roster roster  = conn.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			if(entry.getUser().equals(tupleJid))
			{
				try {
					roster.removeEntry(entry);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*
	public void setMessageAndParticipantStatusListenerForMUCCreation(String tupleJid,
			MultiUserChat multiUserChat, PacketListener mucMessageListener,
			ParticipantStatusListener participantStatusListener) {

			multiUserChat.addMessageListener(mucMessageListener);
			multiUserChat.addParticipantStatusListener(participantStatusListener);
			MUCMap.put(tupleJid, multiUserChat);
		
	}
	*/
	
	
	
	
	/*
	
	public MultiUserChat putMUCInstance(String tupleJid,
			MultiUserChat multiUserChat) {
		MUCMap.put(tupleJid, multiUserChat);
		return multiUserChat;
	}
	*/
/*
	public MultiUserChat getNewMUCInstance(String tupleJid,
			PacketListener mucMessageListener) {
		MultiUserChat multiUserChat = new MultiUserChat(conn, tupleJid);
		multiUserChat.addMessageListener(mucMessageListener);

		ParticipantStatusListener participantStatusListener = new ParticipantStatusListener() {

			@Override
			public void adminGranted(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void adminRevoked(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void banned(String arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void joined(String participant) {

			}

			@Override
			public void kicked(String arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void left(String participant) {
				// TODO Auto-generated method stub
				System.out.println(participant + "  离开");
			}

			@Override
			public void membershipGranted(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void membershipRevoked(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void moderatorGranted(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void moderatorRevoked(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void nicknameChanged(String arg0, String arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ownershipGranted(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ownershipRevoked(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void voiceGranted(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void voiceRevoked(String arg0) {
				// TODO Auto-generated method stub

			}

		};

		multiUserChat.addParticipantStatusListener(participantStatusListener);
		MUCMap.put(tupleJid, multiUserChat);
		return multiUserChat;
	}
*/
	public XMPPConnection getConnection() {
		return conn;
	}

	public Map getParticipantToChatThread() {
		return participantToChatThread;
	}

	public MessageListener getMessageListerner() {
		return messageListerner;
	}

	public void setMessageListerner(MessageListener messageListerner) {
		this.messageListerner = messageListerner;
	}

	public ChatManagerListener getChatManagerListener() {
		return chatManagerListener;
	}

	public void setChatManagerListener(ChatManagerListener chatManagerListener) {
		this.chatManagerListener = chatManagerListener;
	}

	public InvitationListener getInvitationListener() {
		return invitationListener;
	}

	public void setInvitationListener(InvitationListener invitationListener) {
		this.invitationListener = invitationListener;
	}
	
	public String getJid()
	{
		return jid;
	}
	
	public ItemEventListener getEventNodeListener() {
		ItemEventListener itemEventListener = new ItemEventListener()
		{
			@Override
			public void handlePublishedItems(ItemPublishEvent evt) {
				// TODO Auto-generated method stub
				 for (Object obj : evt.getItems()) {  
	                    PayloadItem item = (PayloadItem) obj;  
	                   
	                    System.out.println("--:Payload=" + item.getPayload().toString());  
	                }  
			}
		};
		return itemEventListener;
	}
	


	public void UpdatePresence(String presence) {
		// TODO Auto-generated method stub
		if(presence.equals("chat"))
		{
			Presence presencePacket = new Presence(Presence.Type.available);
			presencePacket.setMode(Presence.Mode.chat);
			setClientMode(Presence.Mode.chat);
			if(getClientStatus()!=null)
			{
				presencePacket.setStatus(getClientStatus());
			}
			
			conn.sendPacket(presencePacket);
		}
		if(presence.equals("away"))
		{
			Presence presencePacket = new Presence(Presence.Type.available);
			presencePacket.setMode(Presence.Mode.away);
			setClientMode(Presence.Mode.away);
			if(getClientStatus()!=null)
			{
				presencePacket.setStatus(getClientStatus());
			}
			conn.sendPacket(presencePacket);
		}
		if(presence.equals("dnd"))
		{
			Presence presencePacket = new Presence(Presence.Type.available);
			presencePacket.setMode(Presence.Mode.dnd);
			setClientMode(Presence.Mode.dnd);
			if(getClientStatus()!=null)
			{
				presencePacket.setStatus(getClientStatus());
			}
			conn.sendPacket(presencePacket);
		}
		if(presence.equals("xa"))
		{
			Presence presencePacket = new Presence(Presence.Type.available);
			presencePacket.setMode(Presence.Mode.xa);
			setClientMode(Presence.Mode.xa);
			if(getClientStatus()!=null)
			{
				presencePacket.setStatus(getClientStatus());
			}
			conn.sendPacket(presencePacket);
		}
		
		
	}
	

	public void UpdateStatus(String status) {
		// TODO Auto-generated method stub
		setClientStatus(status);
		
		Presence presencePacket = new Presence(Presence.Type.available);
		presencePacket.setStatus(status);
		if(getClientMode()!=null)
		{
			presencePacket.setMode(getClientMode());
		}
		conn.sendPacket(presencePacket);
	}
	
	public static void main(String[] args) {
		SmackClient client = new SmackClient("yar2", "123456", "192.168.1.23", 5222);
		try {
			client.login();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	private void setClientMode(Mode mode)
	{
		this.ClientMode = mode;
	}
	
	private Mode getClientMode()
	{
		return this.ClientMode;
	}
	
	private void setClientStatus(String status)
	{
		this.ClientStatus = status;
	}

	private String getClientStatus()
	{
		return this.ClientStatus;
	}
}
